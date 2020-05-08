 package fr.emn.criojo.network;
 
 import java.io.ByteArrayInputStream;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.hornetq.api.core.HornetQException;
 import org.hornetq.api.core.SimpleString;
 import org.hornetq.api.core.TransportConfiguration;
 import org.hornetq.api.core.client.ClientConsumer;
 import org.hornetq.api.core.client.ClientMessage;
 import org.hornetq.api.core.client.ClientProducer;
 import org.hornetq.api.core.client.ClientSession;
 import org.hornetq.api.core.client.ClientSessionFactory;
 import org.hornetq.api.core.client.HornetQClient;
 import org.hornetq.api.core.client.MessageHandler;
 import org.hornetq.api.core.client.ServerLocator;
 import org.hornetq.core.config.Configuration;
 import org.hornetq.core.deployers.impl.FileConfigurationParser;
 import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
 import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
 import org.hornetq.core.remoting.impl.netty.TransportConstants;
 import org.hornetq.core.server.HornetQServer;
 import org.hornetq.core.server.HornetQServers;
 
 /**
  * {@link BusConnectorLocalHornetQ} connect to a HornetQ server.
  * 
  * HornetQ is an open source asynchronous messaging project from JBoss. It is an
  * example of Message Oriented Middleware. HornetQ is an open source project to
  * build a multi-protocol, embeddable, very high performance, clustered,
  * asynchronous messaging system.
  * 
  * This connector comes with an implementation of HornetQ server. If no server
  * start on {@link BusConnectorLocalHornetQ}.getHost with given port, the
  * connector start a new HornetQ server. To connect to bus, the HornetQ server
  * configuration will specifying connectors using Netty implementation.
  * 
  * @see http://www.jboss.org/hornetq
  */
 public class BusConnectorLocalHornetQ implements BusConnectorHornetQ {
 	protected static Lock l = new ReentrantLock();
 	public static final String DEFAULT_BROADCAST_ADDRESS = "231.7.7.7";
 	public static final int DEFAULT_BROADCAST_PORT = 9876;
 	public static final int DEFAULT_STOMPWEBSOCKET_PORT = 61614;
 
 	private final int port;
 	private final String name;
 	private final String login;
 	private final String password;
 	private final int stompWebSocketPort;
 	private final String broadcastAddress;
 	private final int broadcastPort;
 	private boolean disconected;
 
 	/**
 	 * Consume personal messages.
 	 */
 	protected ClientConsumer personalConsumer = null;
 	
 	/**
 	 * Consume broadcast messages.
 	 */
 	protected ClientConsumer bcastConsumer = null;
 	
 	protected ClientSession session = null;
 	protected ClientProducer producer = null;
 	protected ServerLocator locator = null;
 	protected ClientSessionFactory factory = null;
 	protected HornetQServer server = null;
 	protected static String hostValue = null;
 
 	/**
 	 * BusConnection Using HornetQ Message Oriented Middleware.
 	 * 
 	 * If no connection point defined on local HornetQ server, this will start a
 	 * new HornetQ server on {@link BusConnectorLocalHornetQ}.getHost with given
 	 * port. If new server started, the new server is start in clustered mode with
 	 * given broadcast address.
 	 * 
 	 * @param port
 	 *          The host port (HornetQ Connector port, with Netty).
 	 * @param name
 	 *          The name of current connection (should be unique in cluster).
 	 * @param login
 	 *          The session login.
 	 * @param password
 	 *          The session password.
 	 * @param stompWebSocketPort
 	 *          The Stomp over WebSocket acceptor port.
 	 * @param broadcastAddress
 	 *          The broadcast address (HornetQ server).
 	 * @param broadcastPort
 	 *          The broadcast port to diffuse and listen other HornetQ server.
 	 * @throws BusConnectorException
 	 *           Troubles append during connection to Bus or Bus start.
 	 */
 	public BusConnectorLocalHornetQ(int port, String name, String login,
 	    String password, int stompWebSocketPort, String broadcastAddress,
 	    int broadcastPort) throws BusConnectorException {
 		this.port = port;
 		this.name = name;
 		this.login = login;
 		this.password = password;
 		this.stompWebSocketPort = stompWebSocketPort;
 		this.broadcastAddress = broadcastAddress;
 		this.broadcastPort = broadcastPort;
 		this.disconected = false;
 
 		try {
 			l.lock();
 			connect();
 			startQueueProducerConsumer();
 			l.unlock();
 		} catch (Exception e) {
 			throw new BusConnectorException(e.getMessage());
 		}
 	}
 
 	/**
 	 * BusConnection Using HornetQ Message Oriented Middleware.
 	 * 
 	 * If no connection point defined on local HornetQ server, this will start a
 	 * new HornetQ server on {@link BusConnectorLocalHornetQ}.getHost with given
 	 * port. If new server started, the new server is start in clustered mode with
 	 * broadcast address BusConnectorLocalHornetQ.DEFAULT_BROADCAST_ADDRESS and
 	 * broadcast port BusConnectorLocalHrnetQ.DEFAULT_BROADCAST_PORT. Moreover an
 	 * acceptor for Stomp connection over WebSocket is started on
 	 * BusConnectorLocalHornetQ.DEFAULT_STOMPWEBSOCKET_PORT.
 	 * 
 	 * @param port
 	 *          The host port (HornetQ Connector port, with Netty).
 	 * @param name
 	 *          The name of current connection (should be unique in cluster).
 	 * @param login
 	 *          The session login.
 	 * @param password
 	 *          The session password.
 	 * @throws BusConnectorException
 	 *           Troubles append during connection to Bus or Bus start.
 	 */
   public BusConnectorLocalHornetQ(int port, String name, String login,
 	    String password) throws BusConnectorException {
 		this(port, name, login, password, DEFAULT_STOMPWEBSOCKET_PORT,
 		    DEFAULT_BROADCAST_ADDRESS, DEFAULT_BROADCAST_PORT);
 	}
 
 	@Override
 	public void send(String message, String recipient)
 	    throws BusConnectorException {
 		ClientMessage msg = session.createMessage(false);
 
 		try {
 			// Use NullableSimpleString cause it is default Stomp message form.
 			msg.getBodyBuffer().writeNullableSimpleString(new SimpleString(message));
 			msg.setDurable(true);
 			
 			// If recipient is empty, the message is broadcasted over the network.
 			if (!recipient.isEmpty()) {
 				producer.send(PERSONAL + "." + recipient, msg);
 			} else {
 				producer.send(BROADCAST, msg);
 			}
 		} catch (HornetQException hqe) {
 			throw hornetQExceptionToBusException(hqe);
 		}
 	}
 
 	@Override
 	public void broadcast(String message) throws BusConnectorException {
 		send(message, "");
 	}
 
 	@Override
 	public void setReceiveHandler(final ReceiveHandler receiveHandler)
 	    throws BusConnectorException {
 		try {
 			MessageHandler mh = new MessageHandler() {
 				@Override
 				public void onMessage(ClientMessage message) {
 					try {
 						message.acknowledge();
 					} catch (HornetQException hqe) {
 						hqe.printStackTrace();
 					}
 					// Use NullableSimpleString cause it is default Stomp message form.
 					try {
 						String msg = message.getBodyBuffer().readNullableSimpleString()
 								.toString();
 						
 						if (!msg.startsWith(ACK_MESSAGE)) {
 							receiveHandler.onReceive(msg);
 						}
 					} catch (NullPointerException npe) {
 						receiveHandler.onReceive(message.toString());
 					}
 				}
 			};
 			
 			bcastConsumer.setMessageHandler(mh);
 			personalConsumer.setMessageHandler(mh);
 		} catch (HornetQException hqe) {
 			throw hornetQExceptionToBusException(hqe);
 		}
 	}
 
 	@Override
 	public void disconnect() {
 		try {
 			if (bcastConsumer != null) {
 				bcastConsumer.close();
 			}
 			if (personalConsumer != null) {
 				personalConsumer.close();
 			}
 			if (producer != null) {
 				producer.close();
 			}
 			if (session != null) {
 				session.close();
 			}
 			if (factory != null) {
 				factory.close();
 			}
 			if (locator != null) {
 				locator.close();
 			}
 		} catch (HornetQException hqe) {
 			hornetQExceptionToBusException(hqe).printStackTrace();
 		} finally {
 			disconected = true;
 		}
 	}
 
 	@Override
 	public boolean isDisconected() {
 		return disconected;
 	}
 
 	/**
 	 * Connect to HornetQ server using host, port, login and password properties.
 	 * 
 	 * @throws Exception
 	 *           Bus is unreachable.
 	 */
 	private void connect() throws Exception {
 		TransportConfiguration tConfiguration = null;
 		Map<String, Object> connectionParams = new HashMap<String, Object>();
 
 		connectionParams.put(TransportConstants.HOST_PROP_NAME, getHost());
 		connectionParams.put(TransportConstants.PORT_PROP_NAME, port);
 
 		tConfiguration = new TransportConfiguration(
 		    NettyConnectorFactory.class.getName(), connectionParams);
 
 		// Get the connection factory to connect on server.
 		locator = HornetQClient.createServerLocatorWithoutHA(tConfiguration);
 
 		// Connect to the server
 		try {
 			factory = locator.createSessionFactory();
 		} catch (HornetQException hqe) {
 			switch (hqe.getCode()) {
 			case HornetQException.NOT_CONNECTED:
 				// Start localhost HornetQ server on given port and broadcast address.
 				startServer();
 				break;
 			default:
 				throw hornetQExceptionToBusException(hqe);
 			}
 		}
 
 		session = factory
 				.createSession(login, password, false, true, true, false, 0);
 	}
 
 	/**
 	 * Starts the Queue, Producer and Consumer.
 	 * 
 	 * Start the queue BusRemoteHornetQ.getName() and start a consumer on it. If
 	 * queue already exists, the consumer is connected to existing queue. This
 	 * method although allocate a producer and start the ClientSession.
 	 * 
 	 * @throws HornetQException
 	 */
 	private void startQueueProducerConsumer() throws HornetQException {
 		// Create Queue.
 		deployQueue(BROADCAST, getBroadcastQueueName());
 		deployQueue(getPersonalQueueName(), getPersonalQueueName());
 
 		// Create Producer and Consumer.
 		producer = session.createProducer();
 		bcastConsumer = session.createConsumer(getBroadcastQueueName());
 		personalConsumer = session.createConsumer(getPersonalQueueName());
 
 		session.start();
 	}
 
 	/**
 	 * Start Local HornetQ Server.
 	 * 
 	 * @throws Exception
 	 */
 	protected void startServer() throws Exception {
 		Configuration configuration = new FileConfigurationParser()
 		    .parseMainConfig(new ByteArrayInputStream(configurationXML().getBytes()));
 
 		configuration.setPersistenceEnabled(false);
 		configuration.setSecurityEnabled(false);
 
		configuration.getAcceptorConfigurations().add(
		    new TransportConfiguration(NettyAcceptorFactory.class.getName()));
 
		System.err.println("[BUS] Start local server using " + configurationXML());
 		server = HornetQServers.newHornetQServer(configuration);
 		server.start();
 
		TransportConfiguration tConfiguration = null;
 		Map<String, Object> connectionParams = new HashMap<String, Object>();
 
 		connectionParams.put(TransportConstants.HOST_PROP_NAME, getHost());
 		connectionParams.put(TransportConstants.PORT_PROP_NAME, port);
 
		tConfiguration = new TransportConfiguration(
 		    NettyConnectorFactory.class.getName(), connectionParams);
 
 		// Get the connection factory to connect on server.
 		locator = HornetQClient.createServerLocatorWithoutHA(tConfiguration);
 
 		// Connect to the server
 		factory = locator.createSessionFactory();
 	}
 
 	/**
 	 * Deploy HornetQ Queue and handle error.
 	 * 
 	 * @param address
 	 *          Address to deploy queue.
 	 * @param name
 	 *          Name of queue.
 	 * @throws HornetQException
 	 */
 	private void deployQueue(String address, String name) throws HornetQException {
 		try {
 			session.createQueue(address, name, false);
 		} catch (HornetQException hqe) {
 			switch (hqe.getCode()) {
 			case HornetQException.QUEUE_EXISTS:
 				System.err
 				    .println("[BUS] Queue " + name + " already exists.");
 				break;
 			default:
 				throw hqe;
 			}
 		}
 	}
 	
 	/**
 	 * Transforms a HornetQException to BusException.
 	 * 
 	 * @param hqe
 	 *          The HorneQException
 	 * @return The BusException.
 	 */
 	private BusConnectorException hornetQExceptionToBusException(
 	    HornetQException hqe) {
 		BusConnectorException be = null;
 
 		switch (hqe.getCode()) {
 		case HornetQException.NOT_CONNECTED:
 			be = new BusConnectorException(BusConnectorException.NOT_CONNECTED,
 					hqe.getMessage());
 			break;
 		 case HornetQException.OBJECT_CLOSED:
 			 be = new BusConnectorException(BusConnectorException.CLOSED, name);
 			 break;
 		 default:
 			 be = new BusConnectorException(BusConnectorException.INTERNAL_ERROR,
 					 hqe.getMessage());
 		 }
 
 		return be;
 	}
 
 	/**
 	 * Returns the queue name were using to store received msg of bus connector.
 	 * 
 	 * @return The HornetQ queue name.
 	 */
 	public String getPersonalQueueName() {
 		return PERSONAL + "." + getName();
 	}
 
 	/**
 	 * Returns the queue name were using to store broadcasted msg of bus connector.
 	 * 
 	 * @return The HornetQ queue name.
 	 */
 	public String getBroadcastQueueName() {
 	  return BROADCAST + "." + getName();
   }
 	
 	/**
 	 * Test if current instance run server.
 	 * 
 	 * @return <code>true</code> if connector run server, <code>else</code>
 	 *         otherwise
 	 */
 	public boolean hasServer() {
 		return (server != null);
 	}
 
 	/**
 	 * Stop the server.
 	 * 
 	 * Server is stopped if current connector run a server.
 	 */
 	public void stopServer() {
 		if (server != null) {
 			try {
 	      server.stop();	      
       } catch (Exception e) {
 	      e.printStackTrace();
       }
 		}
 	}
 	
 	/**
 	 * Count number of connection still open on server.
 	 * 
 	 * @return Number of connection open on server.
 	 */
 	public int openedConnectionServer() {
 		return server.getHornetQServerControl().getConnectionCount();
 	}
 	
 	/**
 	 * Returns the host value.
 	 * 
 	 * @return The host value;
 	 */
 	public static String getHost() {
 		if (hostValue == null) {
 			hostValue = "127.0.0.1";
 
 			try {
 				hostValue = InetAddress.getLocalHost().getHostAddress();
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return hostValue;
 	}
 	
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public int getPort() {
 		return port;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public int getStompWebSocketPort() {
 		return stompWebSocketPort;
 	}
 
 	public String getBroadcastAddress() {
 		return broadcastAddress;
 	}
 
 	public int getBroadcastPort() {
 		return broadcastPort;
 	}
 	
 
 	/**
 	 * Returns HornetQ Configuration XML stream 
 	 * 
 	 * @return HornetQ Configuration XML stream
 	 */
 	protected String configurationXML() {
 	  return "<configuration xmlns=\"urn:hornetq\""
 	      + "\n               xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
 	      + "\n               xsi:schemaLocation=\"urn:hornetq /schema/hornetq-configuration.xsd\">"
 	      + "\n"
 	      + "\n   <clustered>true</clustered>"
 	      + "\n   "
 	      + "\n   <paging-directory>data/paging</paging-directory>"
 	      + "\n   "
 	      + "\n   <bindings-directory>data/bindings</bindings-directory>"
 	      + "\n   "
 	      + "\n   <journal-directory>data/journal</journal-directory>"
 	      + "\n   "
 	      + "\n   <large-messages-directory>data/large-messages</large-messages-directory>"
 	      + "\n"
 	      + "\n   <connectors>"
 	      + "\n      <connector name=\"netty\">"
 	      + "\n         <factory-class>org.hornetq.core.remoting.impl.netty.NettyConnectorFactory</factory-class>"
 	      + "\n         <param key=\"host\"  value=\"" + getHost() + "\"/>"
 	      + "\n         <param key=\"port\"  value=\"" + port + "\"/>"
 	      + "\n      </connector>"
 	      + "\n   </connectors>"
 	      + "\n"
 	      + "\n   <acceptors>"
 	      + "\n      <acceptor name=\"netty\">"
 	      + "\n         <factory-class>org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory</factory-class>"
 	      + "\n         <param key=\"host\"  value=\"0.0.0.0\"/>"
 	      + "\n         <param key=\"port\"  value=\"" + port + "\"/>"
 	      + "\n      </acceptor>"
 	      + "\n      "
 	      + "\n      <acceptor name=\"stomp-acceptor\">"
 	      + "\n        <factory-class>org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory</factory-class>"   
 	      + "\n        <param key=\"protocol\" value=\"stomp_ws\" />"
 	      + "\n        <param key=\"port\" value=\"" + getStompWebSocketPort() + "\" />"
 	      + "\n      </acceptor>"
 	      + "\n   </acceptors>"
 	      + "\n"
 	      + "\n   <broadcast-groups>"
 	      + "\n      <broadcast-group name=\"bg-group1\">"
 	      + "\n         <group-address>" + broadcastAddress + "</group-address>"
 	      + "\n         <group-port>" + broadcastPort + "</group-port>"
 	      + "\n         <broadcast-period>5000</broadcast-period>"
 	      + "\n         <connector-ref>netty</connector-ref>"
 	      + "\n      </broadcast-group>"
 	      + "\n   </broadcast-groups>"
 	      + "\n"
 	      + "\n   <discovery-groups>"
 	      + "\n      <discovery-group name=\"dg-group1\">"
 	      + "\n         <group-address>" + broadcastAddress + "</group-address>"
 	      + "\n         <group-port>" + broadcastPort + "</group-port>"
 	      + "\n         <refresh-timeout>10000</refresh-timeout>"
 	      + "\n      </discovery-group>"
 	      + "\n   </discovery-groups>"
 	      + "\n   "
 	      + "\n   <cluster-connections>"
 	      + "\n      <cluster-connection name=\"my-cluster\">"
 	      + "\n         <address>jms</address>	 "
 	      + "\n         <connector-ref>netty</connector-ref>"
 	      + "\n	      <discovery-group-ref discovery-group-name=\"dg-group1\"/>"
 	      + "\n      </cluster-connection>"
 	      + "\n   </cluster-connections>"
 	      + "\n   "
 	      + "\n   <security-settings>"
 	      + "\n      <security-setting match=\"#\">"
 	      + "\n         <permission type=\"createDurableQueue\" roles=\"guest\"/>"
 	      + "\n         <permission type=\"deleteDurableQueue\" roles=\"guest\"/>"
 	      + "\n         <permission type=\"createNonDurableQueue\" roles=\"guest\"/>"
 	      + "\n         <permission type=\"deleteNonDurableQueue\" roles=\"guest\"/>"
 	      + "\n         <permission type=\"consume\" roles=\"guest\"/>"
 	      + "\n         <permission type=\"send\" roles=\"guest\"/>"
 	      + "\n      </security-setting>"
 	      + "\n   </security-settings>"
 	      + "\n"
 	      + "\n</configuration>";
   }
 }
