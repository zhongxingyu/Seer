 /**
  * 
  */
 package eu.indenica.messaging;
 
 import static org.hamcrest.Matchers.hasItem;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.net.URI;
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
 import eu.indenica.common.EventListener;
 import eu.indenica.common.LoggerFactory;
 import eu.indenica.common.PubSub;
 import eu.indenica.common.RuntimeComponent;
 import eu.indenica.events.Event;
 
 /**
  * Messaging Fabric test suite.
  * 
  * Tests ActiveMQ broker/client connectivity, broker interconnect.
  * 
  * @author Christian Inzinger
  */
 public class TestConnectivity {
 	private final static Logger LOG = LoggerFactory.getLogger();
 	final static URI mcastDiscoveryUri = URI.create("discovery:("
 			+ MessageBroker.discoveryUri.toString() + ")");
 
 	public static class CustomBrokerUriPubSub extends ActivemqPubSub {
 		public CustomBrokerUriPubSub(URI brokerUri) throws Exception {
 			super(brokerUri);
 		}
 	}
 
 	/**
 	 * A sample Event
 	 * 
 	 * @author Christian Inzinger
 	 */
 	public static class EventOne extends Event {
		private static long typeId = System.currentTimeMillis();
 		private String attr1;
 		private int anAttribute;
 
 		public EventOne() {
 			super("test.EventOne." + typeId);
 		}
 
 		public void setAttr1(String attr1) {
 			this.attr1 = attr1;
 		}
 
 		public void setAnAttribute(int anAttribute) {
 			this.anAttribute = anAttribute;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return new StringBuilder().append("#<")
 					.append(getClass().getName()).append(": ")
 					.append("attr1: ").append(attr1).append(", anAttribute: ")
 					.append(anAttribute).append(">").toString();
 		}
 	}
 
 	private MessageBroker defaultBroker;
 	private PubSub defaultPubSub;
 	private Semaphore msgWaitLock;
 	private Collection<Event> observedEvents;
 
 	@BeforeClass
 	public static void setUpBeforeClass() {
 		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"))
 				.setLevel(ch.qos.logback.classic.Level.INFO);
 		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("eu.indenica"))
 				.setLevel(ch.qos.logback.classic.Level.TRACE);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		defaultBroker = new MessageBroker();
 		defaultPubSub = new ActivemqPubSub();
 		msgWaitLock = new Semaphore(0);
 		observedEvents = Lists.newArrayList();
 
 		assertThat(defaultBroker, is(notNullValue()));
 		assertThat(defaultPubSub, is(notNullValue()));
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		observedEvents.clear();
 		msgWaitLock.drainPermits();
 		defaultPubSub.destroy();
 		defaultBroker.destroy();
 	}
 
 	/**
 	 * Test if events can be sent and received at all.
 	 * 
 	 * One broker, default messaging behavior. Send and receive events using
 	 * same pubsub instance.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testSimpleConnection() throws Exception {
 		createEventListener(defaultPubSub, observedEvents, msgWaitLock);
 
 		LOG.debug("Sending empty message...");
 		defaultPubSub.publish(null, new EventOne());
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents.size(), is(1));
 
 		LOG.debug("Sending message w/ content");
 		Event e = new EventOne();
 		((EventOne) e).setAttr1("a value" + System.currentTimeMillis());
 		defaultPubSub.publish(null, e);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(e));
 
 		observedEvents.clear();
 		int nEvents = 13;
 		for(int i = 0; i < nEvents; i++) {
 			Event event = new EventOne();
 			((EventOne) event).setAnAttribute(i);
 			((EventOne) event)
 					.setAttr1("message " + System.currentTimeMillis());
 			LOG.debug("Sending message with event: {}...", e);
 			defaultPubSub.publish(null, event);
 		}
 		msgWaitLock.tryAcquire(nEvents, 2, TimeUnit.SECONDS);
 		assertThat(observedEvents.size(), is(nEvents));
 	}
 
 	/**
 	 * Client should be able to establish connection to broker using multicast
 	 * discovery.
 	 * 
 	 * Messages should be delivered between pubsub instances.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testMcastDiscovery() throws Exception {
 		PubSub mcastPubSub = new CustomBrokerUriPubSub(mcastDiscoveryUri);
 		assertThat(mcastPubSub, is(notNullValue()));
 		createEventListener(mcastPubSub, observedEvents, msgWaitLock);
 
 		LOG.debug("Send event in same pubsub instance...");
 		Event e = new EventOne();
 		mcastPubSub.publish(null, e);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(e));
 
 		LOG.debug("Send event from defaultPubSub to mcastPubSub...");
 		e = new EventOne();
 		defaultPubSub.publish(null, e);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(e));
 
 		observedEvents.clear();
 
 		createEventListener(defaultPubSub, observedEvents, msgWaitLock);
 
 		LOG.debug("Send event from defaultPubSub to be received by both...");
 		e = new EventOne();
 		defaultPubSub.publish(null, e);
 		msgWaitLock.tryAcquire(2, 2, TimeUnit.SECONDS);
 		assertThat(observedEvents.size(), is(2));
 	}
 
 	/**
 	 * Multiple brokers should be able to discover each other and pass messages
 	 * for consumers.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testBrokerDiscovery() throws Exception {
 		MessageBroker secondBroker = new MessageBroker();
 		PubSub secondPubSub =
 				new CustomBrokerUriPubSub(URI.create(secondBroker
 						.getConnectString()));
 		assertThat(secondBroker, is(notNullValue()));
 		assertThat(secondPubSub, is(notNullValue()));
 
 		createEventListener(secondPubSub, observedEvents, msgWaitLock);
 
 		LOG.info("Sending message from new broker to new broker...");
 		Event event = new EventOne();
 		secondPubSub.publish(null, event);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(event));
 
 		msgWaitLock.drainPermits();
 		observedEvents.clear();
 		createEventListener(defaultPubSub, observedEvents, msgWaitLock);
 		LOG.info("Sending message from new broker to default broker...");
 		event = new EventOne();
 		secondPubSub.publish(null, event);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(event));
 
 		msgWaitLock.drainPermits();
 		observedEvents.clear();
 		LOG.info("Sending message from default broker to new broker...");
 		event = new EventOne();
 		defaultPubSub.publish(null, event);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(event));
 
 		secondPubSub.destroy();
 		secondBroker.destroy();
 	}
 
 	/**
 	 * Multiple brokers should be able to discover each other and pass messages
 	 * for consumers.
 	 * 
 	 * This should also work if the first message is sent from the old broker
 	 * and should arrive at the new one.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testBrokerDiscoveryOtherWay() throws Exception {
 		MessageBroker secondBroker = new MessageBroker();
 		LOG.info("Second broker connect string: {}",
 				secondBroker.getConnectString());
 		PubSub secondPubSub =
 				new CustomBrokerUriPubSub(URI.create(secondBroker
 						.getConnectString()));
 		assertThat(secondBroker, is(notNullValue()));
 		assertThat(secondPubSub, is(notNullValue()));
 
 		createEventListener(secondPubSub, observedEvents, msgWaitLock);
 
 		LOG.info("Sending message from new broker to new broker...");
 		Event event = new EventOne();
 		secondPubSub.publish(null, event);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(event));
 
 		msgWaitLock.drainPermits();
 		observedEvents.clear();
 		LOG.info("Sending message from default broker to new broker...");
 		event = new EventOne();
 		defaultPubSub.publish(null, event);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(event));
 
 		msgWaitLock.drainPermits();
 		observedEvents.clear();
 		createEventListener(defaultPubSub, observedEvents, msgWaitLock);
 		LOG.info("Sending message from new broker to default broker...");
 		event = new EventOne();
 		secondPubSub.publish(null, event);
 		msgWaitLock.tryAcquire(2, TimeUnit.SECONDS);
 		assertThat(observedEvents, hasItem(event));
 
 		secondPubSub.destroy();
 		secondBroker.destroy();
 	}
 
 	/**
 	 * Creates a default event listener for {@link EventOne}
 	 * 
 	 * @param pubSub
 	 *            the messaging fabric to use
 	 * @param observedEvents
 	 *            a collection to put received events in
 	 * @param msgWaitLock
 	 *            a semaphore to lock for external synchronization
 	 */
 	private static void
 			createEventListener(final PubSub pubSub,
 					final Collection<Event> observedEvents,
 					final Semaphore msgWaitLock) {
 		createEventListener(pubSub, new EventOne().getEventType(),
 				observedEvents, msgWaitLock);
 	}
 
 	/**
 	 * Creates an event listener for the specified event type.
 	 * 
 	 * @param pubSub
 	 *            the messaging fabric to use
 	 * @param eventType
 	 *            the event type to listen for
 	 * @param observedEvents
 	 *            a collection to put received events in
 	 * @param msgWaitLock
 	 *            a semaphore to lock for external synchronization
 	 */
 	private static void createEventListener(final PubSub pubSub,
 			final String eventType, final Collection<Event> observedEvents,
 			final Semaphore msgWaitLock) {
 		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
 		final String caller =
 				stackTrace[stackTrace[2].getMethodName().startsWith(
 						"createEvent") ? 3 : 2].getMethodName();
 		pubSub.registerListener(new EventListener() {
 			@Override
 			public void eventReceived(RuntimeComponent source, Event event) {
 				LOG.debug("{} Received event {} in {} from {}, in {}",
 						new Object[] { caller, event, pubSub, source });
 				observedEvents.add(event);
 				msgWaitLock.release();
 			}
 		}, null, eventType);
 	}
 }
