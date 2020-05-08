 /**
  * 
  */
 package eu.indenica.messaging;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import javax.jms.Connection;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.activemq.broker.BrokerService;
 import org.apache.activemq.broker.TransportConnector;
 import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.network.NetworkConnector;
 import org.apache.activemq.transport.vm.VMTransportFactory;
 import org.osoa.sca.annotations.Destroy;
 import org.osoa.sca.annotations.EagerInit;
 import org.osoa.sca.annotations.Property;
 import org.osoa.sca.annotations.Scope;
 import org.slf4j.Logger;
 
 import eu.indenica.common.LoggerFactory;
 
 /**
  * Messaging fabric using ActiveMQ embedded brokers and multicast discovery to
  * deliver messages in a distributed deployment.
  * 
  * @author Christian Inzinger
  * 
  */
 @EagerInit
 @Scope("COMPOSITE")
 public class MessageBroker {
     private final static Logger LOG = LoggerFactory.getLogger();
     private final static String ANNOUNCEMENTS_TOPIC_NAME =
             "control.announcements";;
 
     private final NameProvider nameProvider;
     private final BrokerService broker;
     private String hostname;
     private int port;
     private String connectString;
     private Connection announcementsConnection;
     private ExecutorService announcements = Executors.newSingleThreadExecutor();
 
     /**
      * Creates and starts the broker for the messaging fabric using a default
      * application name.
      * 
      * <p>
      * <b>Note:</b> If you intend to start multiple applications in the same
      * network, you should supply a name for each application using constructor
      * {@link #MessageBroker(String)}.
      * 
      * @throws Exception
      *             if the broker cannot be started
      */
     public MessageBroker() throws Exception {
         this("default");
     }
 
     /**
      * Creates and starts the broker for the messaging fabric using the
      * specified application name.
      * 
      * @param applicationName
      *            the application name to use.
      * @throws Exception
      *             if the broker cannot be started
      */
     public MessageBroker(String applicationName) throws Exception {
         this(new NameProvider(applicationName));
     }
 
     /**
      * Creates and starts the broker for the messaging fabric using the given
      * {@link NameProvider} instance
      * 
      * @param nameProvider
      *            the {@link NameProvider} to use
      * @throws Exception
      *             if the broker cannot be started
      */
     public MessageBroker(NameProvider nameProvider) throws Exception {
         LOG.info("Starting message broker...");
         LOG.trace("This is broker {} in this VM",
                 VMTransportFactory.SERVERS.size() + 1);
         this.nameProvider = nameProvider;
         broker = new BrokerService();
         setBrokerName(broker);
         broker.setPersistent(false);
         // broker.setUseJmx(false);
         broker.getSystemUsage().getTempUsage().setLimit(1024 * 1000); // 1000kB
         ManagementContext managementContext = broker.getManagementContext();
 
         /**
          * Set unique free port for management connector in case there are
          * multiple brokers running on one machine.
          */
         managementContext.setConnectorPort(getFreePort());
 
         connectTcpTransport(broker);
         connectBrokerInterconnect(broker);
 
         broker.start();
         connectString = broker.getDefaultSocketURIString();
 
         connectAnnouncementListener();
 
         LOG.info("Broker {} started.", broker.getBrokerName());
     }
 
     /**
      * This method finds a free port on the machine.
      * 
      * <b>NOTE:</b> There is a possible race condition here!
      * 
      * @return a free port
      */
     private int getFreePort() {
         int port = 1099 + VMTransportFactory.SERVERS.size();
         try {
             ServerSocket socket = new ServerSocket(0);
             port = socket.getLocalPort();
             socket.close();
         } catch(IOException e) {
             LOG.warn("Could not find free port, falling back to default.", e);
         }
         return port;
     }
 
     /**
      * Connect the TCP transport for the given broker.
      * 
      * @param broker
      *            the broker to be modified
      * @return the broker, after modification
      * @throws URISyntaxException
      *             if hostname and/or port were invalid
      * @throws Exception
      *             if something goes wrong
      */
     private BrokerService connectTcpTransport(BrokerService broker)
             throws URISyntaxException, Exception {
         TransportConnector connector = new TransportConnector();
         connector.setUri(new URI("tcp://" + getHostname() + ":" + getPort()));
         connector.setDiscoveryUri(nameProvider.getMulticastGroupUri());
         broker.addConnector(connector);
         return broker;
     }
 
     /**
      * Sets unique name for this broker.
      * 
      * @param broker
      *            the broker needing a name
      * @return the broker
      */
     private BrokerService setBrokerName(BrokerService broker) {
         StringBuilder brokerName =
                 new StringBuilder()
                         .append(nameProvider.getMulticastGroupName());
         brokerName.append(".").append(getHostname());
         brokerName.append(".").append(VMTransportFactory.SERVERS.size());
         /**
          * FIXME: As long as it creates the persistent store (which it should
          * not) giving a UUID as broker name will fill up your disk in 32MB
          * increments. Also, broker names should be consistent across restarts.
          */
         // brokerName.append(UUID.randomUUID().toString());
         broker.setBrokerName(brokerName.toString());
         return broker;
     }
 
     /**
      * Connect broker interconnect multicast discovery network connector.
      * 
      * @param broker
      *            the broker to be modified
      * @return the broker, modified.
      * @throws Exception
      *             if something goes wrong
      */
     private BrokerService connectBrokerInterconnect(BrokerService broker)
             throws Exception {
        NetworkConnector networkConnector =
                broker.addNetworkConnector(nameProvider.getMulticastGroupUri());
        networkConnector.setName(UUID.randomUUID().toString());
         // networkConnector.setSuppressDuplicateTopicSubscriptions(true);
        networkConnector.setDuplex(true);
         return broker;
     }
 
     /**
      * Listens to new broker announcements.
      * 
      * (Now mostly here to ensure proper n-way communication between brokers)
      * 
      * @throws JMSException
      *             if something goes wrong
      */
     private void connectAnnouncementListener() throws JMSException {
         LOG.debug("Connecting announcement listener at {}", connectString);
         announcementsConnection =
                 new ActiveMQConnectionFactory(connectString).createConnection();
         announcementsConnection.start();
 
         announcements.submit(new Callable<Void>() {
             @Override
             public Void call() throws Exception {
                 Session session =
                         announcementsConnection.createSession(false,
                                 Session.AUTO_ACKNOWLEDGE);
                 MessageConsumer consumer =
                         session.createConsumer(session
                                 .createTopic(ANNOUNCEMENTS_TOPIC_NAME));
                 consumer.setMessageListener(new MessageListener() {
                     @Override
                     public void onMessage(Message message) {
                         String brokerName = broker.getBrokerName();
                         String newBrokerName = null;
                         try {
                             newBrokerName =
                                     message.getStringProperty("brokerName");
                         } catch(JMSException e) {
                             LOG.error("Could not understand message!", e);
                         }
                         if(!brokerName.equals(newBrokerName))
                             LOG.info("ANN ({}): Broker {} joined.", brokerName,
                                     newBrokerName);
 
                     }
                 });
 
                 MessageProducer producer =
                         session.createProducer(session
                                 .createTopic(ANNOUNCEMENTS_TOPIC_NAME));
                 Message announcement = session.createMapMessage();
                 announcement.setStringProperty("brokerName",
                         broker.getBrokerName());
                 producer.send(announcement);
                 return null;
             }
         });
     }
 
     /**
      * Shuts down the messaging fabric
      * 
      * @throws Exception
      *             if something goes wrong
      */
     @Destroy
     public void destroy() throws Exception {
         LOG.debug("Shutting down message broker...");
         announcements.shutdown();
         if(!announcements.awaitTermination(2, TimeUnit.SECONDS))
             announcements.shutdownNow();
         if(announcementsConnection != null)
             announcementsConnection.close();
 
         broker.stop();
         LOG.info("Message broker shut down.");
     }
 
     /**
      * Returns this machine's host name
      * 
      * @return the hostname
      */
     public String getHostname() {
         if(hostname == null) {
             try {
                 return java.net.InetAddress.getLocalHost().getHostName();
             } catch(UnknownHostException e) {
                 LOG.warn("Could not get host name for this machine!", e);
                 return "localhost";
             }
         }
         return hostname;
     }
 
     /**
      * @param hostname
      *            the hostname to set
      */
     @Property
     public void setHostname(final String hostname) {
         this.hostname = hostname;
     }
 
     /**
      * @return the port
      */
     public int getPort() {
         return port;
     }
 
     /**
      * @param port
      *            the port to set
      */
     @Property
     public void setPort(final int port) {
         this.port = port;
     }
 
     /**
      * @return the connectString
      */
     public String getConnectString() {
         return connectString;
     }
 
     /**
      * @param newPeers
      *            the peers to set
      */
     @Property
     public void setPeers(final String[] newPeers) {
         for(String newPeer : newPeers)
             connectPeer(newPeer);
     }
 
     /**
      * Adds connection to peer broker
      * 
      * @param peerAddress
      *            address of the peer broker
      */
     private void connectPeer(final String peerAddress) {
         try {
             LOG.info("Connecting to new peer: {}", peerAddress);
             broker.addNetworkConnector("static://(" + peerAddress + ")");
         } catch(Exception e) {
             LOG.error("Failed to connect peer", e);
         }
     }
 }
