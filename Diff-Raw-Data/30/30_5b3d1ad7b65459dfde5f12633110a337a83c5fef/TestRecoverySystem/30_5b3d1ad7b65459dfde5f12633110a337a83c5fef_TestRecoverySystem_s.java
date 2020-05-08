 package ca.utoronto.msrg.padres.test.configService;
 
 import static org.junit.Assert.assertTrue;
 
 import java.net.InetAddress;
 import java.util.Iterator;
 
 import org.junit.After;
import org.junit.AfterClass;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 
 import ca.utoronto.msrg.padres.broker.brokercore.BrokerCore;
 import ca.utoronto.msrg.padres.client.Client;
 import ca.utoronto.msrg.padres.client.ClientException;
 import ca.utoronto.msrg.padres.common.message.Advertisement;
 import ca.utoronto.msrg.padres.common.message.AdvertisementMessage;
 import ca.utoronto.msrg.padres.common.message.Publication;
 import ca.utoronto.msrg.padres.common.message.Subscription;
 import ca.utoronto.msrg.padres.common.message.SubscriptionMessage;
 import ca.utoronto.msrg.padres.common.message.parser.MessageFactory;
 import ca.utoronto.msrg.padres.common.message.parser.ParseException;
 import ca.utoronto.msrg.padres.configService.CSClientImpl;
 import ca.utoronto.msrg.padres.configService.RecoverySystem;
import ca.utoronto.msrg.padres.configService.SSHConnection;
 import ca.utoronto.msrg.padres.configService.schema.Backup;
 import ca.utoronto.msrg.padres.configService.schema.Broker;
 import ca.utoronto.msrg.padres.configService.schema.Config;
 import ca.utoronto.msrg.padres.configService.schema.Neighbours;
 import ca.utoronto.msrg.padres.configService.schema.Node;
 import ca.utoronto.msrg.padres.configService.schema.Topology;
 import ca.utoronto.msrg.padres.test.junit.MessageWatchAppender;
 import ca.utoronto.msrg.padres.test.junit.PatternFilter;
 
 public class TestRecoverySystem {
 	private BrokerCore broker1;
 	private BrokerCore broker2;
 	private BrokerCore broker3;
 	private BrokerCore broker4;
 	private BrokerCore broker5;
 
 	private Client clientA;
 	private Client clientB;
 
 	private RecoverySystem recSys;
 
 	private MessageWatchAppender messageWatcher;
 
 	private PatternFilter msgFilter;
 
 	private InetAddress thisIP;
 
 	private Broker broker;
 
 	@Before
 	public void setUp() throws Exception {
 		messageWatcher = new MessageWatchAppender();
 		msgFilter = new PatternFilter(Client.class.getName());
 
 		thisIP = InetAddress.getLocalHost();
 		String hostAddress = thisIP.getHostAddress();
 
 		// build test topology
 		Config config = new Config();
 		Topology topology = new Topology();
 		for (int i = 0; i < 5; i++) {
 			broker = new Broker();
 			broker.setHost(hostAddress);
 			broker.setPort(5000 + i);
 			broker.setName("broker" + (i + 1));
 			broker.setType("rmi");
 
 			if (i == 4) {
 				Neighbours neighbours = new Neighbours();
 				neighbours.getNeighbour().add("broker1");
 				broker.setNeighbours(neighbours);
 			} else {
 				Neighbours neighbours = new Neighbours();
 				neighbours.getNeighbour().add("broker" + (i + 2));
 				broker.setNeighbours(neighbours);
 			}
 
 			topology.getBroker().add(broker);
 		}
 		config.setTopology(topology);
 
 		// prepare backup nodes
 		Backup backup = new Backup();
 		Node node1 = new Node();
 		node1.setHost(hostAddress);
 		node1.setPort(5006);
 		backup.getNode().add(node1);
 		
 		Node node2 = new Node();
 		node2.setHost(hostAddress);
 		node2.setPort(5007);
 		backup.getNode().add(node2);
 		
 		Node node3 = new Node();
 		node3.setHost(hostAddress);
		node3.setPort(5007);
 		backup.getNode().add(node3);
 		
 		config.setBackup(backup);
 
 		RecoverySystem.isDebug(true);
 		recSys = new RecoverySystem("recoverySystem", config);
		
//		for(Broker broker : config.getTopology().getBroker()){
//			System.out.println("Broker: "+recSys.createStartCommand(broker));
//		}
 
 		// start brokers
 		broker = config.getTopology().getBroker().get(0);
 		broker1 = new BrokerCore(recSys.createStartCommand(broker));
 		broker1.initialize();
 		broker = config.getTopology().getBroker().get(1);
 		broker2 = new BrokerCore(recSys.createStartCommand(broker));
 		broker2.initialize();
 		broker = config.getTopology().getBroker().get(2);
 		broker3 = new BrokerCore(recSys.createStartCommand(broker));
 		broker3.initialize();
 		broker = config.getTopology().getBroker().get(3);
 		broker4 = new BrokerCore(recSys.createStartCommand(broker));
 		broker4.initialize();
 		broker = config.getTopology().getBroker().get(4);
 		broker5 = new BrokerCore(recSys.createStartCommand(broker));
 		broker5.initialize();
 
 		recSys.initialize();
 
 		// start clients
 		clientA = new CSClientImpl("ClientA");
 		clientA.connect(broker1.getBrokerURI());
 		clientB = new CSClientImpl("ClientB");
 		clientB.connect(broker3.getBrokerURI());
 
 		msgFilter.setPattern(".*Client " + clientA.getClientID()
 				+ ".+Publication.+stock.+");
 		messageWatcher.addFilter(msgFilter);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		clientA.shutdown();
 		clientB.shutdown();
 		
 		recSys.shutdown();
 
 		broker1.shutdown();
 		broker2.shutdown();
 		broker3.shutdown();
 		broker4.shutdown();
 		broker5.shutdown();
 	}
	
//	@After
//	public void restartClientB() throws ClientException{
//		if(!clientB.isConnected()){
//			clientB = new CSClientImpl("ClientB");
//			clientB.connect(broker3.getBrokerURI());
//		}
//	}
 
 	@Test
 	public void testBrokersCorrectlyConnected() throws Exception {
 		clientA.advertise(MessageFactory
 				.createAdvertisementFromString("[class,eq,'stock'],[price,<,100]"));
 		clientB.subscribe(MessageFactory
 				.createSubscriptionFromString("[class,eq,'stock'],[price,<,50]"));
 		messageWatcher.getMessage();
 		clientA.publish(MessageFactory
 				.createPublicationFromString("[class,'stock'],[price,20]"));
 
 		Publication pub = MessageFactory
 				.createPublicationFromString("[class,'stock'],[price,20]");
 
 		// waiting for the message to be received
 		messageWatcher.getMessage();
 		
 		Publication expectedPub = clientB.getCurrentPub();
 		assertTrue(
 				"The publication: [class,'stock'],[price,20] should be matched at clientB",
 				expectedPub.equalVals(pub));
 	}
 
 	@Test
 	public void testClientsRegisteredSuccessfully() {
 		assertTrue("Two clients should be registered to the service",
 				recSys.getRegClientsNumber() == 2);
 	}
 
 	@Test
 	public void testDeregisterClient() throws ClientException {
 		clientB.shutdown();
 		assertTrue("Only one client should be registered to the service",
 				recSys.getRegClientsNumber() == 1);
 	}
 
 	@Test
 	// Must be client.store_detail_state=ON in the client.properties
 	public void testResendAdvertisements() throws ClientException,
 			ParseException {
 		Advertisement adv1 = MessageFactory
 				.createAdvertisementFromString("[class,eq,'stock'],[price,=,100]");
 		Advertisement adv2 = MessageFactory
 				.createAdvertisementFromString("[class,eq,'stock'],[price,=,200]");
		
//		clientA.getAdvertisements().clear();
//		clientB.getAdvertisements().clear();
 
 		clientA.advertise(adv1);
 		clientB.advertise(adv2);
 
 		recSys.readvertiseAll();
 
 		assertTrue(clientA.getAdvertisements().size() == 2);
 		assertTrue(clientB.getAdvertisements().size() == 2);
 
 		Iterator it = clientA.getAdvertisements().values().iterator();
 		it.next();
 		AdvertisementMessage am = (AdvertisementMessage) it.next();
 		assertTrue(am.getAdvertisement().equals(adv1));
 
 		it = clientB.getAdvertisements().values().iterator();
 		it.next();
 		am = (AdvertisementMessage) it.next();
 		assertTrue(am.getAdvertisement().equals(adv2));
 	}
 
 	@Test
 	// Must be client.store_detail_state=ON in the client.properties
 	public void testResendSubscriptions() throws ClientException,
 			ParseException {
 		Subscription sub1 = MessageFactory
 				.createSubscriptionFromString("[class,eq,'stock'],[price,=,100]");
 		Subscription sub2 = MessageFactory
 				.createSubscriptionFromString("[class,eq,'stock'],[price,=,200]");
		
//		clientA.getSubscriptions().clear();
//		clientB.getSubscriptions().clear();
 
 		clientA.subscribe(sub1);
 		clientB.subscribe(sub2);
 
 		recSys.resubscribeAll();
 
 		assertTrue(clientA.getSubscriptions().size() == 2);
 		assertTrue(clientB.getSubscriptions().size() == 2);
 
 		Iterator it = clientA.getSubscriptions().values().iterator();
 		it.next();
 		SubscriptionMessage sm = (SubscriptionMessage) it.next();
 		assertTrue(sm.getSubscription().equals(sub1));
 
 		it = clientB.getSubscriptions().values().iterator();
 		it.next();
 		sm = (SubscriptionMessage) it.next();
 		assertTrue(sm.getSubscription().equals(sub2));
 	}
 
 	@Test
 	public void recoverBroker1() throws ClientException, ParseException,
 			InterruptedException {
 		clientA.advertise(MessageFactory
 				.createAdvertisementFromString("[class,eq,'stock'],[price,<,100]"));
 		clientB.subscribe(MessageFactory
 				.createSubscriptionFromString("[class,eq,'stock'],[price,<,50]"));
 		clientA.publish(MessageFactory
 				.createPublicationFromString("[class,'stock'],[price,20]"));
 
 		// waiting for the message to be received
 		messageWatcher.getMessage();
 
 		broker2.stop();
 		messageWatcher.getMessage(20);
 		
 		Publication pub = MessageFactory
 				.createPublicationFromString("[class,'stock'],[price,10]");
 		clientA.publish(pub);
 
 		messageWatcher.getMessage();
 		Publication expectedPub = clientB.getCurrentPub();
 		assertTrue(
 				"The publication: [class,'stock'],[price,10] should be matched at clientB",
 				expectedPub.equalVals(pub));
 	}
 	
 	@Test
 	public void recoverBroker2() throws ClientException, ParseException,
 			InterruptedException {
 		clientB.disconnectAll();	
 		clientB.connect(broker4.getBrokerURI());
 	
 		clientA.advertise(MessageFactory
 				.createAdvertisementFromString("[class,eq,'stock'],[price,<,100]"));
 		clientB.subscribe(MessageFactory
 				.createSubscriptionFromString("[class,eq,'stock'],[price,<,50]"));
 		clientA.publish(MessageFactory
 				.createPublicationFromString("[class,'stock'],[price,20]"));
 
 		// waiting for the message to be received
 		messageWatcher.getMessage();
 
 		broker5.stop();
 		messageWatcher.getMessage(20);
 		
 		Publication pub = MessageFactory
 				.createPublicationFromString("[class,'stock'],[price,10]");
 		clientA.publish(pub);
 
 		messageWatcher.getMessage();
 		Publication expectedPub = clientB.getCurrentPub();
 		assertTrue(
 				"The publication: [class,'stock'],[price,10] should be matched at clientB",
 				expectedPub.equalVals(pub));
 	}
 }
