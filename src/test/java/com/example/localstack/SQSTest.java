package com.example.localstack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
public class SQSTest {

    @Container
    public GenericContainer<?> localstack = new GenericContainer<>("localstack/localstack:latest")
            .withExposedPorts(4566);

    @Test
    void testSQS() {

        Integer mappedPort = localstack.getMappedPort(4566);
        String sqsEndpoint = "http://localhost:" +mappedPort;
        String queueName = "test-queue1";
        String queueURL = sqsEndpoint + "/000000000000/" + queueName;

        //initialize SQS client
        SqsClient sqsClient = getSqsClient(sqsEndpoint);

        // Create an SQS queue
        CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                .queueName(queueName)
                .build();
        sqsClient.createQueue(createQueueRequest);

        // Send a message to the queue
        String messageBody = "Hello Localstack!";
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueURL)
                .messageBody(messageBody)
                .build();
        sqsClient.sendMessage(sendMessageRequest);

        // Receive and verify the message
        // add the logic based on application requirement.
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueURL)
                .maxNumberOfMessages(5)
                .build();
        List<Message> receivedMessage = sqsClient.receiveMessage(receiveMessageRequest).messages();

        assertEquals(messageBody, receivedMessage.get(0).body());
    }

    private SqsClient getSqsClient(String sqsEndpoint) {
        return SqsClient.builder()
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(sqsEndpoint))
                .credentialsProvider(() -> AwsBasicCredentials.create("dummy-access-key", "dummy-secret-key"))
                .build();
    }
}
