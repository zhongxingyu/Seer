 package amp.gel.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.UUID;
 
 import org.apache.accumulo.core.client.Connector;
 import org.apache.accumulo.core.client.admin.TableOperations;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import amp.gel.dao.impl.accumulo.AccumuloWriter;
 import amp.gel.dao.impl.accumulo.BatchWriterFactoryImpl;
 import amp.gel.dao.impl.accumulo.DefaultMutationsGenerator;
 import amp.gel.dao.impl.accumulo.MockConnectorFactoryImpl;
 import amp.gel.dao.impl.accumulo.TableOperationsFactoryImpl;
 import cmf.bus.IEnvelopeBus;
 import cmf.eventing.IEventBus;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:cmf-test-context.xml" })
 public class TopicLoggerIntegrationTest {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(TopicLoggerIntegrationTest.class);
 
 	private static final int NUM_EVENTS = 100;
 
 	private static final SimplePojo EVENT = new SimplePojo("Michael Jordan", 23);
 
 	private static final String TOPIC = EVENT.getClass().getCanonicalName();
 
 	@Autowired
 	private IEnvelopeBus envelopeBus;
 
 	@Autowired
 	private IEventBus eventBus;
 
 	private String tableName;
 
 	private TopicLogger topicLogger;
 
 	private AccumuloTestHelper tableHelper;
 
 	@Before
 	public void setUp() throws Exception {
 		// Connector connector = new
 		// ConnectorFactoryImpl().setInstanceName("gel")
 		// .setZooKeepers("10.1.30.133").setUser("root")
 		// .setPassword("root").createConnector();
 
 		Connector connector = new MockConnectorFactoryImpl()
 				.setInstanceName("gel").setUser("test").setPassword("test")
 				.createConnector();
 		TableOperations tableOperations = new TableOperationsFactoryImpl()
 				.setConnector(connector).createTableOperations();
 
 		tableName = createUniqueTableName();
 
 		AccumuloWriter writer = new AccumuloWriter();
 		writer.setBatchWriter(new BatchWriterFactoryImpl()
 				.setConnector(connector).setTableName(tableName)
 				.setTableOperations(tableOperations).createBatchWriter());
 		writer.setMutationsGenerator(new DefaultMutationsGenerator());
 
 		topicLogger = new TopicLogger();
 		topicLogger.setEnvelopeBus(envelopeBus).setDatastoreWriter(writer)
 				.setTopic(TOPIC);
 		topicLogger.start();
 
 		tableHelper = new AccumuloTestHelper().setConnector(connector)
 				.setTableOperations(tableOperations);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		topicLogger.stop();
 
 		tableHelper.delete(tableName);
 	}
 
 	@Test
 	public void test() {
 		TopicCounter topicCounter = new TopicCounter(TOPIC);
 
 		try {
 			envelopeBus.register(topicCounter);
 		} catch (Exception e) {
 			String message = "Unable to register for messages";
 			logger.error(message, e);
 			fail(message);
 		}
 
 		for (int i = 0; i < NUM_EVENTS; i++) {
 			try {
 				eventBus.publish(EVENT);
 			} catch (Exception e) {
 				String message = "Unable to publish messages";
 				logger.error(message, e);
 				fail(message);
 			}
 		}
 
 		while (topicCounter.getCount() < NUM_EVENTS) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// do nothing
 			}
 		}
 
 		try {
 			long count = tableHelper.getEntryCount(tableName);
 
 			assertEquals(6 * NUM_EVENTS, count);
 			logger.info("Table '" + tableName + "' had " + count + " values");
 		} catch (Exception e) {
 			String message = "Unable to count entries in table";
 			logger.error(message, e);
 			fail(message);
 		}
 	}
 
 	private String createUniqueTableName() {
 		return UUID.randomUUID().toString().replaceAll("-", "_");
 	}
 
 	static public class SimplePojo {
 		private String attribute1;
 
 		private int attribute2;
 
 		public SimplePojo(String attribute1, int attribute2) {
 			super();
 			this.attribute1 = attribute1;
 			this.attribute2 = attribute2;
 		}
 
 		public String getAttribute1() {
 			return attribute1;
 		}
 
 		public void setAttribute1(String attribute1) {
 			this.attribute1 = attribute1;
 		}
 
 		public int getAttribute2() {
 			return attribute2;
 		}
 
 		public void setAttribute2(int attribute2) {
 			this.attribute2 = attribute2;
 		}
 
 		@Override
 		public String toString() {
 			return "SimplePojo [attribute1=" + attribute1 + ", attribute2="
 					+ attribute2 + "]";
 		}
 	}
 }
