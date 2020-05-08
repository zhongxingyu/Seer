 package com.bbytes.zorba.messaging.rabbitmq;
 
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.bbytes.zorba.messaging.IQueueStatsService;
 import com.bbytes.zorba.messaging.rabbitmq.impl.RabbitMQSender;
 
 /**
  * Test class for {@link RabbitMQSender}
  * 
  * @author Dhanush Gopinath
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations ={ "classpath*:/spring/zorba-messaging-test-context.xml" })
 public class RabbitMQStatsTest {
 
 	@Autowired
 	IQueueStatsService queueStatsService;
 	
 	
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
	public void testSendZorbaRequestPriority() {
 		List<String> queueNames = queueStatsService.getQueueNames();
 		Assert.assertNotNull(queueNames);
 	}
 
 	
 
 }
