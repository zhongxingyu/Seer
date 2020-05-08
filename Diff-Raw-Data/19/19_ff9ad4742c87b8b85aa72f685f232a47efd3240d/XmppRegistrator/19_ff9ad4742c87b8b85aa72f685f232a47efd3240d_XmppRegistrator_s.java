 /*  	CASi Context Awareness Simulation Software
  *   Copyright (C) 2012 2012  Moritz Bürger, Marvin Frick, Tobias Mende
  *
  *  This program is free software. It is licensed under the
  *  GNU Lesser General Public License with one clarification.
  *  
  *  You should have received a copy of the 
  *  GNU Lesser General Public License along with this program. 
  *  See the LICENSE.txt file in this projects root folder or visit
  *  <http://www.gnu.org/licenses/lgpl.html> for more details.
  */
 package de.uniluebeck.imis.casi.utils;
 
 import java.beans.XMLDecoder;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jivesoftware.smack.AccountManager;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 
import de.uniluebeck.imis.casi.communication.mack.MACKNetworkHandler;
 import de.uniluebeck.imis.casi.communication.mack.XmppIdentifier;
 
 /**
  * This single application can be used to register xmpp identifier which are
  * provided in a network config file that can also be used to configure the
 * {@link MACKNetworkHandler}
  * 
  * @author Tobias Mende
  * 
  */
 public class XmppRegistrator {
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
 	
 	/** A list of jabber identifiers which are free to use by the simulator */
 	private ArrayList<XmppIdentifier> usableJabberIdentifiers = new ArrayList<XmppIdentifier>();
 	/** Counts the registrations */
 	private int registrationCounter = 0;
 	
 	private ArrayList<XmppIdentifier> identifierToRegister = new ArrayList<XmppIdentifier>();
 	
 	/**
 	 * The main method
 	 * @param args the path to the config file
 	 */
 	public static void main(String[] args) {
 		if(args.length  != 1) {
 			System.err.println("Wrong amount of parameter. Can't execute");
 			System.exit(0);
 		}
 		XmppRegistrator registrator = new XmppRegistrator(args[0]);
 		registrator.perform();
 	}
 	
 	/**
 	 * Constructor for a XmppRegistrator
 	 * @param path the path to the config file
 	 */
 	@SuppressWarnings("unchecked")
 	public XmppRegistrator(String path) {
 		XMLDecoder dec = null;
 		try {
 			dec = new XMLDecoder(new FileInputStream(path));
 			XMPP_SERVER = (String) dec.readObject();
 			REGISTRATION_DELAY = (Integer) dec.readObject();
 			XMPP_PASSWORD = (String) dec.readObject();
 			XMPP_PORT = (Integer) dec.readObject();
 			dec.readObject();
 			usableJabberIdentifiers = (ArrayList<XmppIdentifier>) dec
 					.readObject();
 		} catch (IOException e) {
 			System.err.println("Can't read xml specification!");
 		} finally {
 			if (dec != null)
 				dec.close();
 		}
 		
 	}
 	
 	/**
 	 * Performs the registration process.
 	 */
 	public void perform() {
		System.out.println("Checking and registering "+usableJabberIdentifiers.size()+" identfiers");
 		System.out.println("======================================================================");
 		System.out.println("Checking identifiers:");
 		System.out.println("----------------------------------------------------------------------");
 		for(XmppIdentifier identifier : usableJabberIdentifiers) {
 			check(identifier);
 		}
 		System.out.println("----------------------------------------------------------------------");
 		System.out.println("Need to register "+identifierToRegister.size()+" new identifiers");
 		int minutes = (identifierToRegister.size()-1)*REGISTRATION_DELAY;
 		if(minutes >= 60) {
 			int remainingMinutes = minutes%60;
 			System.out.println("Needs "+(minutes-remainingMinutes)/60+" hours and "+remainingMinutes+" minutes");
 		} else if (minutes > 0) {
 			System.out.println("Needs "+minutes+" minutes!");
 		}
 		System.out.println("----------------------------------------------------------------------");
 		for(XmppIdentifier identifier : identifierToRegister) {
 			if(register(identifier.getId())) {
 				System.out.println(identifier.getId()+" was registered successful.");
 				registrationCounter++;
 			} else {
 				System.err.println(identifier.getId()+" can't be registered.");
 			}
 		}
 		System.out.println("----------------------------------------------------------------------");
 		System.out.println("Done! "+registrationCounter+" identifiers were registered successful");
 		System.out.println("Job completed");
 		System.out.println("======================================================================");
 	}
 	
 	private void check(XmppIdentifier identifier) {
 		ConnectionConfiguration config = new ConnectionConfiguration(
 				XMPP_SERVER, XMPP_PORT);
 		Connection connection = new XMPPConnection(config);
 		try {
 			connection.connect();
 			try {
 				connection.login(identifier.getId(), XMPP_PASSWORD);
 			} catch (XMPPException e) {
 				System.out.println(identifier.getId()
 						+ " seems not to be registered. ");
 				identifierToRegister.add(identifier);
 			}
 		} catch (XMPPException e) {
 			System.err.println("Can't connect "+identifier.getId());
 		} finally {
 			connection.disconnect();
 		}
 	}
 	
 	/**
 	 * Registers a new account
 	 * @param id the identifier
 	 * @param connection the connection to the server
 	 * @return {@code true} if the registration was completed successful, {@code false} otherwise.
 	 */
 	private boolean register(String id) {
 		ConnectionConfiguration config = new ConnectionConfiguration(
 				XMPP_SERVER, XMPP_PORT);
 		Connection connection = new XMPPConnection(config);
 		if (registeredLastTime && REGISTRATION_DELAY > 0) {
 			System.out.println("Sleeping for " + REGISTRATION_DELAY
 					+ " minutes. Need to wait for " + XMPP_SERVER);
 			try {
 				// macjabber.de allows new account creation only every x
 				// minutes
 				Thread.sleep(REGISTRATION_DELAY * 60000 + 10);
 			} catch (InterruptedException e1) {
 				System.err.println("Can't sleep");
 			}
 		}
 		try {
 			connection.connect();
 		} catch (XMPPException e) {
 			e.printStackTrace();
 			return false;
 		}
 		if (!connection.isAuthenticated()) {
 			AccountManager acManager = connection.getAccountManager();
 			try {
 				acManager.createAccount(id, XMPP_PASSWORD);
 			} catch (XMPPException e) {
 				e.printStackTrace();
 				return false;
 			} finally {
 				registeredLastTime = true;
 			}
 		}
		connection.disconnect();
 		return true;
 	}
 }
