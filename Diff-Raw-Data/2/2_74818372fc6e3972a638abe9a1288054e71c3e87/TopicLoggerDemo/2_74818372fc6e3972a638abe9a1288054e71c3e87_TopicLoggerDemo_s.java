 package amp.gel.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import cmf.eventing.IEventBus;
 
 public class TopicLoggerDemo {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(TopicLoggerDemo.class);
 
 	public static void main(String[] args) {
 		ApplicationContext appContext = new FileSystemXmlApplicationContext(
				"src/test/resources/cmf-test-context.xml");
 		IEventBus eventBus = appContext.getBean("eventBus", IEventBus.class);
 
 		long counter = 0;
 		while (true) {
 			try {
 				eventBus.publish(new SimplePojo("Test Event", counter++));
 				Thread.sleep(1000);
 			} catch (Exception e) {
 				logger.error("Unable to publish event", e);
 			}
 		}
 	}
 
 	static public class SimplePojo {
 		private String attribute1;
 
 		private long attribute2;
 
 		public SimplePojo(String attribute1, long attribute2) {
 			super();
 			this.attribute1 = attribute1;
 			this.attribute2 = attribute2;
 		}
 
 		public String getAttribute1() {
 			return attribute1;
 		}
 
 		public long getAttribute2() {
 			return attribute2;
 		}
 
 		@Override
 		public String toString() {
 			return "SimplePojo [attribute1=" + attribute1 + ", attribute2="
 					+ attribute2 + "]";
 		}
 	}
 }
