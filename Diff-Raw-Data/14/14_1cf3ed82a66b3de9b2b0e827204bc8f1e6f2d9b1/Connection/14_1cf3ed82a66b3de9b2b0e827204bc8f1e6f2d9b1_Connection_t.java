 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.patuck.stegmpp.xmpp;
 
 import java.io.IOException;
 import java.io.PrintWriter;
import java.net.ConnectException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.patuck.stegmpp.StegMPP;
 import net.patuck.stegmpp.ui.Style;
 
 /**
  *
  * @author reshad
  */
 public class Connection
 {
 	private String username;
 	private String password;
 	private String server;
 	private int port;
 	
 	private Socket socket;
 	
 	
 	/**
 	 * Default constructor makes a connection with the default values.
 	 */
 	public Connection()
 	{
		username = "username";
 		password = "password";
		server = "localhost";
 		port = 5222;
 	}
 	
 	
 	/**
 	 * Getter method to get the username.
 	 * @return the username.
 	 */
 	public String getUsername()
 	{
 		return username;
 	}
 	
 	
 	/**
 	 * Setter method for username.
 	 * @param username the username to set.
 	 */
 	public void setUsername(String username)
 	{
 		this.username = username;
 	}
 	
 	
 	/**
 	 * Getter method to get the password.
 	 * @return the password.
 	 */
 	public String getPassword()
 	{
 		return password;
 	}
 	
 	
 	/**
 	 * Setter method for the password.
 	 * @param password the password to set.
 	 */
 	public void setPassword(String password)
 	{
 		this.password = password;
 	}
 	
 	
 	/**
 	 * Setter method to for the server.
 	 * @param server the server.
 	 */
 	public void setServer(String server)
 	{
 		this.server = server;
 	}
 	
 	
 	/**
 	 * Getter method to get the server name.
 	 * @return the server name.
 	 */
 	public String getServer()
 	{
 		return server;
 	}
 	
 	
 	/**
 	 * Setter method to set the port.
 	 * @param port the port number.
 	 */
 	public void setPort(int port)
 	{
 		this.port = port;
 	}
 	
 	
 	/**
 	 * Getter method to get the port.
 	 * @return the port.
 	 */
 	public int getPort()
 	{
 		return port;
 	}
 	
 	
 	/**
 	 * The connect method is used to connect to the XMPP server.
 	 */
 	public PrintWriter connect()
 	{
 		StegMPP.getUI().print("[System] ",Style.SYSTEM);
 		StegMPP.getUI().println("Connecting to XMPP Server");
 		
 		
 		
 		try
 		{
 			socket = new Socket(server, port);
 			Receiver receiver = new Receiver(socket.getInputStream(), false);
 			Thread inThread = new Thread(receiver);
 			inThread.start();
 			PrintWriter pw=new PrintWriter(socket.getOutputStream());
 			Session.pw = pw;
 			
 			pw.println("<?xml version='1.0' ?>");
 			pw.flush();
 			pw.println("<stream:stream to='" + server + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
 			pw.flush();
 			
 			
 			StegMPP.getUI().print("[System] ",Style.SYSTEM);
 			Authentication.setPrintWriter(pw);
 			if(Authentication.sendAuth("PLAIN", username, username))
 			{
 				StegMPP.getUI().println("Authentication: Successful");
 			}
 			else
 			{
 				StegMPP.getUI().println("Authentication failed. please check connection setting and try again");
 				return null;
 			}
 			
 			
 			pw.println("<stream:stream to='" + server + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>");
 			pw.flush();
 			
 			
 			IQ.setPrintWriter(pw);
 			IQ.sendIQ("set", null, "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>");
 			IQ.sendIQ("set", null, "<session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>");
 			IQ.sendIQ("get", server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
 			IQ.sendIQ("get", null, "<vCard xmlns='vcard-temp'/>");
 			IQ.sendIQ("get", null, "<query xmlns='jabber:iq:roster'/>");
 			IQ.sendIQ("get", server, "<query xmlns='http://jabber.org/protocol/disco#items' node='http://jabber.org/protocol/commands'/>");
 		//	IQ.sendIQ("get", null, "<blocklist xmlns='urn:xmpp:blocking'/>");
 		//	iq.sendIQ("get", "proxy.eu.jabber.org", "<query xmlns='http://jabber.org/protocol/bytestreams'/>");
 			IQ.sendIQ("get", "conference." + server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
 		//	IQ.sendIQ("get", "irc." + server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
 			IQ.sendIQ("get", "pubsub." + server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
 		//	IQ.sendIQ("get", "vjud." + server, "<query xmlns='http://jabber.org/protocol/disco#info'/>");
 
 			
 			pw.println("<presence><priority>1</priority><show>chat</show></presence>");
 			pw.flush();
 			
 //			pw.println("<presence><priority>1</priority><c xmlns='http://jabber.org/protocol/caps' node='http://pidgin.im/' hash='sha-1' ver='AcN1/PEN8nq7AHD+9jpxMV4U6YM=' ext='voice-v1 camera-v1 video-v1'/><x xmlns='vcard-temp:x:update'/></presence>");
 //			pw.flush();
 //			pw.println("");
 //			pw.flush();
 //			pw.println("");
 //			pw.flush();
 			//whole bunch of iq to query server status
 			StegMPP.getUI().enableMessaging();
 			return pw;
 			
 		}
		catch (UnknownHostException | ConnectException ex)
 		{
			StegMPP.getUI().print("[System] ",Style.SYSTEM);
			StegMPP.getUI().println("Error: wrong hostname, please check the server.");
 		}
 		catch (IOException ex)
 		{
 			Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		return null;
 		
 	}
 	
 	public void disconnect()
 	{
 		Session.pw.println("</stream:stream>");
 		Session.pw.flush();
 		StegMPP.getUI().print("[System] ",Style.SYSTEM);
 		StegMPP.getUI().println("Disconnected.");
 	}
 }
