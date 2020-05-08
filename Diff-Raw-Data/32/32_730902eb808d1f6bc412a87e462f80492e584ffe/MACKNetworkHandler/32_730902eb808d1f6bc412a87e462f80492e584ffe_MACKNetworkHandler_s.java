 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.communication.mack;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.jivesoftware.smack.AccountManager;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.PacketCollector;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.FromContainsFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 
 import de.uniluebeck.imis.casi.CASi;
 import de.uniluebeck.imis.casi.communication.ICommunicationComponent;
 import de.uniluebeck.imis.casi.communication.ICommunicationHandler;
 import de.uniluebeck.imis.casi.simulation.model.AbstractInteractionComponent;
 
 /**
  * The MACKNetworkHandler handles the communication with the MACK Framework (the
  * jabber server).
  * 
  * @author Tobias Mende
  * 
  */
 public final class MACKNetworkHandler implements ICommunicationHandler {
 	/** the development logger */
 	private static final Logger log = Logger.getLogger(MACKNetworkHandler.class
 			.getName());
 	/** The jabber identifier of the mack server */
 	private String MACK_SERVER_IDENTIFIER;
	/** The default resource identifier */
	private String RESOURCE;
 
 	/** The port on which to connect to to the XMPP_SERVER */
 	private int XMPP_PORT;
 	/** The xmpp server to connect to */
 	private String XMPP_SERVER;
 	/**
 	 * The time in minutes that has to elapse between two jabber id
 	 * registrations. (XMPP server specific)
 	 */
 	private int REGISTRATION_DELAY;
 
 	/** The password for all accounts */
 	private String XMPP_PASSWORD;
 
 	/** Flag which handles the interrupt before registering a new component */
 	private boolean registeredLastTime = false;
 
 	/**
 	 * the component map holds all components with their identifier which should
 	 * also be used in the MACK framework.
 	 */
 	private Map<ICommunicationComponent, Chat> components = new HashMap<ICommunicationComponent, Chat>();
 
 	/** A list of jabber identifiers which are free to use by the simulator */
 	private static ArrayList<XmppIdentifier> usableJabberIdentifiers = new ArrayList<XmppIdentifier>();
 
 	/**
 	 * Creates a new network handler with the default configuration which fits
 	 * to MATe office simulation
 	 */
 	public MACKNetworkHandler() {
 		setupDefaults();
 		setupUsableJabberIdentifiers();
		RESOURCE = "CASi-"+System.currentTimeMillis();
 	}
 
 	/**
 	 * Sets the default configuration
 	 */
 	private void setupDefaults() {
 		XMPP_PORT = 5222;
 		REGISTRATION_DELAY = 10;
 		XMPP_SERVER = "macjabber.de";
 		XMPP_PASSWORD = "ao8Thim2iengeehoeyae4aequigaeV";
 		MACK_SERVER_IDENTIFIER = "mate_server_1@macjabber.de";
		RESOURCE = "CASi-"+System.currentTimeMillis();
 	}
 
 	/**
 	 * Constructor for a new MACKNetworkHandler which uses a xml configuration
 	 * file
 	 * 
 	 * @param path
 	 *            the path to the configuration file
 	 */
 	@SuppressWarnings("unchecked")
 	public MACKNetworkHandler(String path) {
 		log.info("Loading setup from config file!");
 		XMLDecoder dec = null;
 		try {
			RESOURCE = "CASi-"+System.currentTimeMillis();
 			dec = new XMLDecoder(new FileInputStream(path));
 			XMPP_SERVER = (String) dec.readObject();
 			REGISTRATION_DELAY = (Integer) dec.readObject();
 			XMPP_PASSWORD = (String) dec.readObject();
 			XMPP_PORT = (Integer) dec.readObject();
 			MACK_SERVER_IDENTIFIER = (String) dec.readObject();
 			usableJabberIdentifiers = (ArrayList<XmppIdentifier>) dec
 					.readObject();
 		} catch (IOException e) {
 			log.severe("Can't deserialize the config: " + e.fillInStackTrace());
 			log.severe("Terminating execution!");
 			System.exit(0);
 		} finally {
 			if (dec != null)
 				dec.close();
 		}
 	}
 
 	/**
 	 * Sets up the list of jabber identifier which could be used for the
 	 * communication
 	 */
 	private static void setupUsableJabberIdentifiers() {
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_tim", "doorlight",
 				"CASi_doorlight_0"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_tim",
 				"doorsensor", "CASi_doorsensor_0"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_tim", "daa",
 				"CASi_daa_0"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_tim", "cubus",
 				"CASi_cubus_0"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_tim", "mike",
 				"CASi_mike_0"));
 
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy",
 				"doorlight", "CASi_doorlight_1"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy",
 				"doorlight", "CASi_doorlight_2"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy",
 				"doorsensor", "CASi_doorsensor_1"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy",
 				"doorsensor", "CASi_doorsensor_2"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy", "daa",
 				"CASi_daa_1"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy",
 				"cubus", "CASi_cubus_1"));
 		usableJabberIdentifiers.add(new XmppIdentifier("casi_crazy_guy",
 				"mike", "CASi_mike_1"));
 
 	}
 
 	@Override
 	public synchronized boolean send(ICommunicationComponent sender,
 			Object message) {
 		Chat chat = components.get(sender);
 		if (chat == null) {
 			log.warning(sender + " is not registered");
 			return false;
 		}
 
 		try {
 			if (CASi.VERBOSE) {
 				CASi.SIM_LOG.info("Sending: " + message);
 			}
 			chat.sendMessage((String) message);
 		} catch (XMPPException e) {
 			CASi.SIM_LOG.severe("Can't send to mack server: "
 					+ e.fillInStackTrace());
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public synchronized void register(ICommunicationComponent comp) {
 		if (comp instanceof AbstractInteractionComponent) {
 			AbstractInteractionComponent component = ((AbstractInteractionComponent) comp);
 			XmppIdentifier result = null;
 			for (XmppIdentifier id : usableJabberIdentifiers) {
 				if (id.getComponentOwner().equals(
 						component.getAgent().getIdentifier())
 						&& id.getComponentType().equals(component.getType())) {
 					result = id;
 					break;
 				}
 			}
 			if (result != null) {
 				usableJabberIdentifiers.remove(result);
 				setupXmppConnection(result, comp);
 			} else {
 				log.warning("Can't find a jabber identifier for the component "
 						+ comp + " for " + component.getAgent());
 			}
 		} else {
 			log.warning("Component isn't an interaction component. Don't know what to do with it. Ignoring...");
 		}
 	}
 
 	/**
 	 * Creates a new xmpp connection for the provided component and identifier
 	 * 
 	 * @param identifier
 	 *            the identifier
 	 * @param comp
 	 *            the component
 	 */
 	private synchronized void setupXmppConnection(XmppIdentifier identifier,
 			final ICommunicationComponent comp) {
 		ConnectionConfiguration config = new ConnectionConfiguration(
 				XMPP_SERVER, XMPP_PORT);
 		Connection connection = new XMPPConnection(config);
 		try {
 			if (registeredLastTime && REGISTRATION_DELAY > 0) {
 				CASi.SIM_LOG.info("Sleeping for " + REGISTRATION_DELAY
 						+ " minutes. Need to wait for " + XMPP_SERVER);
 				try {
 					// macjabber.de allows new account creation only every x
 					// minutes
 					Thread.sleep(REGISTRATION_DELAY * 60000 + 10);
 				} catch (InterruptedException e1) {
 					log.warning("Can't sleep");
 				}
 			}
 			connection.connect();
 			boolean createdAccount = false;
 			try {
				connection.login(identifier.getId(), XMPP_PASSWORD, RESOURCE);
 			} catch (XMPPException e) {
 				log.info(identifier.getId()
 						+ " seems not to be registered. Trying to register now");
 				createdAccount = checkAndRegister(identifier.getId(),
 						connection);
 				if (createdAccount) {
 					registeredLastTime = true;
 					log.info("Account for " + identifier.getId()
 							+ " was registered successfull. Logging in...");
					connection.login(identifier.getId(), XMPP_PASSWORD, RESOURCE);
 					
 				}
 			}
 			createChat(identifier, comp, connection);
 		} catch (XMPPException e1) {
 			log.severe("Can't connect: " + e1.fillInStackTrace());
 			return;
 		}
 
 	}
 
 	/**
 	 * Creates a new chat
 	 * 
 	 * @param identifier
 	 *            the identifier to use
 	 * @param comp
 	 *            the component that should use the chat for communication
 	 * @param connection
 	 *            the connection to use for chat creation
 	 */
 	private void createChat(XmppIdentifier identifier,
 			final ICommunicationComponent comp, Connection connection) {
 		// Creating chat with server:
 		if (connection.isAuthenticated()) {
 			ChatManager chatmanager = connection.getChatManager();
 			Chat chat = chatmanager.createChat(MACK_SERVER_IDENTIFIER,
 					new MessageListener() {
 						public void processMessage(Chat chat, Message message) {
 							if (message.getType().equals(Message.Type.normal)
 									|| message.getType().equals(
 											Message.Type.chat)) {
 								if (CASi.VERBOSE) {
 									CASi.SIM_LOG.info("Receiving: "
 											+ message.getBody());
 								}
 								comp.receive(message.getBody());
 							} else {
 								CASi.SIM_LOG.warning("Message of type "
 										+ message.getType()
 										+ " received. I didn't pass it!");
 							}
 						}
 					});
 			// Only listen to messages from the server:
 //			createPacketListener(comp, connection);
 			components.put(comp, chat);
 			CASi.SIM_LOG.config(identifier.getId()
 					+ ": Component is connected now. Chat with server was initialized");
 		} else {
 			log.warning(comp + "is not authenticated");
 			connection.disconnect();
 		}
 	}
 
 	/**
 	 * Creates and sets a packet listener that observes the connection
 	 * @param comp the component to which packages should be forwarded
 	 * @param connection the connection to observe
 	 */
 	@SuppressWarnings("unused")
 	private void createPacketListener(final ICommunicationComponent comp,
 			Connection connection) {
 		PacketFilter filter = new AndFilter(new PacketTypeFilter(
 				Message.class), new FromContainsFilter(
 				MACK_SERVER_IDENTIFIER));
 
 		PacketCollector myCollector = connection
 				.createPacketCollector(filter);
 		PacketListener myListener = new PacketListener() {
 			public void processPacket(Packet packet) {
 				if (packet instanceof Message) {
 					Message message = ((Message) packet);
 					if (message.getType().equals(Message.Type.normal)) {
 						if (CASi.VERBOSE) {
 							CASi.SIM_LOG.info("Receiving: "
 									+ message.getBody());
 						}
 						comp.receive(message.getBody());
 					}
 				} else {
 					log.warning("The packet isn't a message: "
 							+ packet.toXML());
 				}
 			}
 		};
 
 		connection.addPacketListener(myListener, filter);
 	}
 
 	/**
 	 * Checks if an account is registered and tries to register, if not.
 	 * 
 	 * @param id
 	 *            the xmpp user identifier
 	 * @param connection
 	 *            the connection to use
 	 * @return {@code true} if the id was registered successful or {@code false}
 	 *         otherwise.
 	 */
 	private boolean checkAndRegister(String id, Connection connection) {
 		if (!connection.isAuthenticated()) {
 			AccountManager acManager = connection.getAccountManager();
 			try {
 				acManager.createAccount(id, XMPP_PASSWORD);
 			} catch (XMPPException e) {
 				log.severe("The registration failed! "
 						+ e.getLocalizedMessage());
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Creates the xml representation of the configuration and stores it in
 	 * {@code sims/dev_office_java/network.conf.xml}
 	 * 
 	 * @deprecated because this method was only for initial generation and
 	 *             debugging. No need to use it in productive mode.
 	 */
 	@Deprecated
 	public void serializeSettings() {
 		log.info("Serializing network handler settings");
 		String filename = "sims/dev_office_java/network.conf.xml";
 		XMLEncoder enc = null;
 		try {
 			enc = new XMLEncoder(new FileOutputStream(filename));
 			enc.writeObject(XMPP_SERVER);
 			enc.writeObject(REGISTRATION_DELAY);
 			enc.writeObject(XMPP_PASSWORD);
 			enc.writeObject(XMPP_PORT);
 			enc.writeObject(MACK_SERVER_IDENTIFIER);
 			enc.writeObject(usableJabberIdentifiers);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (enc != null)
 				enc.close();
 		}
 	}
 
 }
