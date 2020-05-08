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
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 
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
 
     private final DiscoveryNameProvider nameProvider;
     private final BrokerService broker;
     private final ManagementClient mgmtClient;
     private String hostname;
     private int port;
     private String connectString;
 
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
         this(new DiscoveryNameProvider(applicationName));
     }
 
     /**
      * Creates and starts the broker for the messaging fabric using the given
      * {@link DiscoveryNameProvider} instance
      * 
      * @param nameProvider
      *            the {@link DiscoveryNameProvider} to use
      * @throws Exception
      *             if the broker cannot be started
      */
     public MessageBroker(DiscoveryNameProvider nameProvider) throws Exception {
         LOG.info("Starting message broker...");
         LOG.trace("This is broker {} in this VM",
                 VMTransportFactory.SERVERS.size() + 1);
         this.nameProvider = nameProvider;
         broker = new BrokerService();
         setBrokerName();
         broker.setPersistent(false);
         broker.getSystemUsage().getTempUsage().setLimit(1024 * 1000); // 1000kB
         setJmxProperties();
 
         connectTcpTransport();
         connectBrokerInterconnect();
 
         broker.start();
         connectString = broker.getDefaultSocketURIString();
 
         mgmtClient =
                 new ManagementClient(broker.getBrokerName(), "broker");
         connectAnnouncementListener();
 
         LOG.info("Broker {} started.", broker.getBrokerName());
     }
 
     /**
      * Set relevant JMX properties.
      * 
      * <p>
      * Ensures that each broker listens on a free port.
      */
     private BrokerService setJmxProperties() {
         // broker.setUseJmx(false);
         ManagementContext managementContext = broker.getManagementContext();
 
         /**
          * Set unique free port for management connector in case there are
          * multiple brokers running on one machine.
          */
         managementContext.setConnectorPort(getFreePort());
         return broker;
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
      * Connect the TCP transport for the active broker.
      * 
      * @return the broker, after modification
      * @throws URISyntaxException
      *             if hostname and/or port were invalid
      * @throws Exception
      *             if something goes wrong
      */
     private BrokerService connectTcpTransport() throws URISyntaxException,
             Exception {
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
     private BrokerService setBrokerName() {
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
      * <p>
      * TODO: Should probably make sure that only one broker interconnect exists.
      * 
      * @param broker
      *            the broker to be modified
      * @return the broker, modified.
      * @throws Exception
      *             if something goes wrong
      */
     private BrokerService connectBrokerInterconnect() throws Exception {
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
         final String announcementsTopic = "broker.announcements";
         mgmtClient.registerBroadcastListener(new MessageListener() {
             @Override
             public void onMessage(Message message) {
                 String brokerName = broker.getBrokerName();
                 String newBrokerName = null;
                 try {
                     newBrokerName = message.getStringProperty("brokerName");
                 } catch(JMSException e) {
                     LOG.error("Could not understand message!", e);
                 }
                 if(!brokerName.equals(newBrokerName))
                     LOG.info("ANN ({}): Broker {} joined.", brokerName,
                             newBrokerName);
 
             }
         }, announcementsTopic);
 
         Message announcement = mgmtClient.createMapMessage();
         announcement.setStringProperty("brokerName", broker.getBrokerName());
         mgmtClient.sendBroadcast(announcement, announcementsTopic);
     }
 
     /**
      * Shuts down the messaging fabric
      * 
      * @throws Exception
      *             if something goes wrong
      */
     @Destroy
     public void destroy() throws Exception {
         LOG.debug("Shutting down message broker {}...", broker.getBrokerName());
 
         mgmtClient.stop();
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
