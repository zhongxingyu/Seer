 /**
  * 
  */
 package eu.indenica.monitoring.esper;
 
 import static eu.indenica.common.TestUtils.createEventListener;
 import static eu.indenica.common.TestUtils.setLogLevels;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
 
 import java.util.Collection;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 
 import com.google.common.collect.Lists;
 
import eu.indenica.common.ActivemqPubSub;
 import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
 import eu.indenica.events.Event;
 import eu.indenica.events.EventOne;
 import eu.indenica.messaging.MessageBroker;
 import eu.indenica.messaging.NameProvider;
 import eu.indenica.monitoring.MonitoringEngine;
import eu.indenica.monitoring.MonitoringQuery;
 import eu.indenica.monitoring.MonitoringQueryImpl;
 
 /**
  * Test the various bits and pieces of the {@link EsperMonitoringEngine}.
  * 
  * @author Christian Inzinger
  */
 public class TestEsperMonitoringEngine {
 	private static Logger LOG = LoggerFactory.getLogger();
 	private Semaphore msgWaitLock;
 	private NameProvider nameProvider;
	private PubSub pubSub;
 	private MessageBroker broker;
 	private MonitoringEngine monitoringEngine;
 
 	@BeforeClass
 	public static void setUpBeforeClass() {
 		setLogLevels();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		msgWaitLock = new Semaphore(0);
 		nameProvider = new NameProvider("test-" + System.currentTimeMillis());
 		broker = new MessageBroker(nameProvider);
		pubSub = new ActivemqPubSub();
 		monitoringEngine = new EsperMonitoringEngine();
 		monitoringEngine.init();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		msgWaitLock.drainPermits();
 		monitoringEngine.destroy();
		pubSub.destroy();
 		broker.destroy();
 	}
 
 	/**
 	 * The monitoring engine should allow to add a {@link MonitoringQuery} and
 	 * run it. The query should receive events and emit them as appropriate
 	 * 
 	 * @throws Exception
 	 *             if something goes wrong
 	 */
 	@Test
 	public void testAddSimpleQuery() throws Exception {
 		LOG.debug("Test adding simple query...");
 		MonitoringQueryImpl query = new MonitoringQueryImpl();
 		query.setInputEventTypes(new String[] { "input,"
 				+ EventOne.class.getCanonicalName() });
 		query.setOutputEventTypes(new String[] { EventOne.class
 				.getCanonicalName() });
 		query.setStatement("select * from EventOne");
 		monitoringEngine.addQuery(query);
 
 		Collection<Event> observedEvents = Lists.newArrayList();
		createEventListener(pubSub, new EventOne().getEventType(),
				observedEvents, msgWaitLock);
 
 		EventOne event = new EventOne();
 		event.setMessage("message " + System.currentTimeMillis());
		pubSub.publish("input", event);
 
 		assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
 		assertThat(observedEvents, hasItem((Event) event));
 	}
 
 }
