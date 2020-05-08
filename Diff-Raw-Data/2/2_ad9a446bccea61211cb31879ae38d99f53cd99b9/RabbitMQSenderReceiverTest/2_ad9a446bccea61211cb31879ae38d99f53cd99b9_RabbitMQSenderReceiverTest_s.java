 package com.bbytes.zorba.messaging.rabbitmq;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.bbytes.zorba.domain.Priority;
 import com.bbytes.zorba.domain.testing.ZorbaBaseTesting;
 import com.bbytes.zorba.jobworker.domain.ZorbaRequest;
 import com.bbytes.zorba.jobworker.domain.ZorbaResponse;
 import com.bbytes.zorba.messaging.IQueueStatsService;
 import com.bbytes.zorba.messaging.exception.MessagingException;
 import com.bbytes.zorba.messaging.rabbitmq.impl.RabbitMQReceiver;
 import com.bbytes.zorba.messaging.rabbitmq.impl.RabbitMQSender;
 
 /**
  * Test class for {@link RabbitMQSender}
  * 
  * @author Dhanush Gopinath
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations ={ "classpath*:/spring/zorba-messaging-test-context.xml" })
 public class RabbitMQSenderReceiverTest extends ZorbaBaseTesting {
 	
 	@Autowired
 	ZorbaRequest zorbaRequest;
 
 	@Autowired
 	ZorbaResponse zorbaResponse;
 	
 	@Autowired
 	RabbitMQSender sender;
 	
 	@Autowired
 	RabbitMQReceiver receiver;
 	
 	@Autowired
 	IQueueStatsService statsService;
 	
 	@Before
 	public void setUp() throws Exception {
		insertPriorityQueues();
 	}
 	
 	@Test
 	public void testSendZorbaRequestPriority() throws MessagingException, InterruptedException {
 		Priority p = Priority.HIGH;
 		String queueName = p.getQueueName();
 		long queueSize = statsService.getQueueMessageSize(queueName);
 		String id = UUID.randomUUID().toString();
 		zorbaRequest.setId(id);
 		sender.send(zorbaRequest,p);
 		Thread.sleep(5000);
 		assertEquals(queueSize+1, statsService.getQueueMessageSize(queueName));
 		ZorbaRequest req = receiver.receive(p);
 		assertNotNull(req);
 		assertEquals(id, req.getId());
 	}
 
 	@Test
 	public void testSendZorbaRequestString() throws MessagingException, InterruptedException {
 		Priority p = Priority.HIGH;
 		String queueName = p.getQueueName();
 //		long queueSize = statsService.getQueueMessageSize(queueName);
 		String id = UUID.randomUUID().toString();
 		zorbaRequest.setId(id);
 		sender.send(zorbaRequest, queueName);
 		Thread.sleep(5000);
 //		assertEquals(queueSize+1, statsService.getQueueMessageSize(queueName));
 		ZorbaRequest req = receiver.receive(queueName);
 		assertNotNull(req);
 		assertEquals(id, req.getId());
 	}
 
 	@Test
 	public void testReceiveResponseString() throws MessagingException {
 		Priority p = Priority.LOW;
 		String queueName = p.getQueueName();
 		String id = UUID.randomUUID().toString();
 		zorbaResponse.setId(id);
 		receiver.sendResponse(zorbaResponse, queueName);
 		ZorbaResponse response = sender.receiveResponse(queueName);
 		assertNotNull(response);
 		assertEquals(id, response.getId());
 	}
 
 	@Test
 	public void testReceiveResponsePriority() throws MessagingException {
 		Priority p = Priority.LOW;
 		String id = UUID.randomUUID().toString();
 		zorbaResponse.setId(id);
 		receiver.sendResponse(zorbaResponse, p);
 		ZorbaResponse response = sender.receiveResponse(p);
 		assertNotNull(response);
 		assertEquals(id, response.getId());
 	}
 
 }
