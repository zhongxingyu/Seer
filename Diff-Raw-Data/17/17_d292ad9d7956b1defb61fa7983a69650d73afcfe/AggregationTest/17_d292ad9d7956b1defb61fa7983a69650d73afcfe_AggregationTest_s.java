 package gov.usgs.cida.ncetl.sis;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.integration.Message;
 import org.springframework.integration.channel.QueueChannel;
 import org.springframework.integration.store.MessageGroupStore;
 
 public class AggregationTest {
 
 	private ConfigurableApplicationContext context;
 	private AggregationChecker aggregationChecker;
 	
 	@Before
 	public void init() {
 		context = new ClassPathXmlApplicationContext("aggregation-test-context.xml");
 		aggregationChecker = context.getBean("aggregationChecker", AggregationChecker.class);
 	}
 	
 	@Test
 	public void testVariedIncomplete() throws Exception {
 		try {
 			System.out.printf("loaded\n");
 
 			aggregationChecker.setTestCase(AggregationChecker.TestCase.VARIED_INCOMPLETE);
 			context.start();
 
 			Thread.sleep(8*1000);
 
 			assertTrue("survived", true);
 
			MessageGroupStore store = context.getBean("message-store", MessageGroupStore.class);
			store.expireMessageGroups(0);
			
 			context.stop();
 
 			QueueChannel errorChannel = context.getBean("errorChannel", QueueChannel.class);
 			Message<?> msg = errorChannel.receive(1);
 
 			assertNull("error message", msg);
 
 		} finally {
 			context.close();
 		}
 	}
 	
 	@Test
 	public void testSingleComplete() throws Exception {
 		try {
 			System.out.printf("loaded\n");
 
 			aggregationChecker.setTestCase(AggregationChecker.TestCase.SINGLE_COMPLETE);
 			context.start();
 
 			Thread.sleep(8*1000);
 
 			assertTrue("survived", true);
 
			MessageGroupStore store = context.getBean("message-store", MessageGroupStore.class);
			// the next should not be necessary, as the aggregator should notice that the group is complete and release it
			store.expireMessageGroups(0);
			
 			context.stop();
 
 			QueueChannel errorChannel = context.getBean("errorChannel", QueueChannel.class);
 			Message<?> msg = errorChannel.receive(1);
 
 			assertNull("error message", msg);
 
 		} finally {
 			context.close();
 		}
 	}
 
 	@Test
 	public void testTwoComplete() throws Exception {
 		try {
 			System.out.printf("loaded\n");
 
 			aggregationChecker.setTestCase(AggregationChecker.TestCase.TWO_COMPLETE);
 			context.start();
 
 			Thread.sleep(8*1000);
 
 			assertTrue("survived", true);
 
			MessageGroupStore store = context.getBean("message-store", MessageGroupStore.class);
			// the next should not be necessary, as the aggregator should notice that the group is complete and release it
			store.expireMessageGroups(0);
			
 			context.stop();
 
 			QueueChannel errorChannel = context.getBean("errorChannel", QueueChannel.class);
 			Message<?> msg = errorChannel.receive(1);
 
 			assertNull("error message", msg);
 
 		} finally {
 			context.close();
 		}
 	}
 
 }
