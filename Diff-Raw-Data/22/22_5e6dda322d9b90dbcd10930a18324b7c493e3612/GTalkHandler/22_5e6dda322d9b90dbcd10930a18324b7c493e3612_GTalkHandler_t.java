 /**
  * Copyright (c) 2009 Aurora Software Technology Studio. All rights reserved.
  */
 package com.corona.logging;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.LogRecord;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 
 /**
  * <p>This handler is used to send logging message to an XMPP client. </p>
  *
  * @author $Author$
  * @version $Id$
  */
 public class GTalkHandler extends Handler implements ChatManagerListener, MessageListener {
 
 	/**
 	 * the XMPP connection
 	 */
 	private XMPPConnection connection = null;
 
 	/**
 	 * the user name
 	 */
 	private String username;
 	
 	/**
 	 * the password
 	 */
 	private String password;
 	
 	/**
 	 * the last time try to connect GTalk
 	 */
 	private Date lastTriedTime = new Date(0);
 	
 	/**
 	 * all connected chats
 	 */
 	private Set<Chat> clients = new HashSet<Chat>();
 	
 	/**
 	 * load all properties for this handler that are defined in properties
 	 */
 	public GTalkHandler() {
 		
 		if (this.getProperty("username") != null) {
 			this.username = this.getProperty("username");
 		}
 		if (this.getProperty("password") != null) {
 			this.password = this.getProperty("password");
 		}
 		if (this.getProperty("level") != null) {
 			try {
 				this.setLevel(Level.parse(this.getProperty("level")));
 			} catch (Throwable e) {
 				this.setLevel(Level.SEVERE);
 			}
 		}
 	}
 
 	/**
 	 * @param name the property name
 	 * @return the property value or <code>null</code> if does not exist
 	 */
 	private String getProperty(final String name) {
 		return LogManager.getLogManager().getProperty(GTalkHandler.class.getName() + "." + name);
 	}
 	
 	/**
 	 * @return whether needs to connect GTalk service or not 
 	 */
 	private boolean needConnectGTalkService() {
 		return (this.connection == null) && (new Date().getTime() - this.lastTriedTime.getTime()) > 60000L;
 	}
 
 	/**
 	 * @return the current opened XMPP connection
 	 */
 	private XMPPConnection getConnection() {
 		
 		if (needConnectGTalkService()) {
 			
 			ConnectionConfiguration config = new ConnectionConfiguration(
 					"talk.google.com", 5222, "chengyousoft.com"
 			);
 			config.setSASLAuthenticationEnabled(false);
 			
 			this.connection = new XMPPConnection(config);
 			try {
 				this.connection.connect();
 				this.connection.login(this.username, this.password);
 			} catch (XMPPException e) {
 				this.connection = null;
 			}
 			
			this.lastTriedTime = new Date();
 			this.connection.getChatManager().addChatListener(this);
 		}
 		
 		return this.connection;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see org.jivesoftware.smack.ChatManagerListener#chatCreated(org.jivesoftware.smack.Chat, boolean)
 	 */
 	@Override
 	public void chatCreated(final Chat chat, final boolean createdLocally) {
 		
 		if (!createdLocally) {
 			chat.addMessageListener(this);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.jivesoftware.smack.MessageListener#processMessage(
 	 * 	org.jivesoftware.smack.Chat, org.jivesoftware.smack.packet.Message
 	 * )
 	 */
 	@Override
 	public void processMessage(final Chat chat, final Message message) {
 		
 		if (message.getType().equals(Message.Type.error)) {
 			
 			this.clients.remove(chat);
 		} else if ("HELLO".equals(message.getBody())) {
 
 			// CHECKSTYLE:OFF
 			try {
 				chat.sendMessage("Welcome, " + chat.getParticipant());
 				this.clients.add(chat);
 			} catch (XMPPException e) {
 				// do nothing
 			}
 			// CHECKSTYLE:ON
 		} else if ("BYE".equals(message.getBody())) {
 
 			// CHECKSTYLE:OFF
 			try {
 				chat.sendMessage("Bye, " + chat.getParticipant());
 				this.clients.remove(chat);
 			} catch (XMPPException e) {
 				// do nothing
 			}
 			// CHECKSTYLE:ON
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
 	 */
 	@Override
 	public void publish(final LogRecord record) {
 		
 		// If logging level is smaller than desired level, just return
 		if (record.getLevel().intValue() < this.getLevel().intValue()) {
 			return;
 		}
 		
 		// if does not connect GTalk or no client requires logging, just return
 		if ((this.getConnection() == null) || (this.clients.size() == 0)) {
 			return;
 		}
 		
 		// create logging message
 		StringBuilder builder = new StringBuilder();
 		builder.append(new Date(record.getMillis())).append(" ");
 		builder.append(record.getLevel()).append(" ");
 		builder.append(record.getLoggerName()).append(" - ");
 		builder.append(record.getMessage());
 		
 		//String message = builder.toString();
 		Message message = new Message();
 		message.setBody(builder.toString());
 		
 		// send logging message to client, and store all clients if send fail 
 		List<Chat> closedClients = new ArrayList<Chat>();
 		for (Chat chat : this.clients) {
 			
 			try {
 				chat.sendMessage(message);
 			} catch (Throwable e) {
 				closedClients.add(chat);
 			}
 		}
 		
 		// remove all clients if can not send message to
 		for (Chat chat : closedClients) {
 			this.clients.remove(chat);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.util.logging.Handler#flush()
 	 */
 	@Override
 	public void flush() {
 		// do nothing
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.util.logging.Handler#close()
 	 */
 	@Override
 	public void close() {
 		
 		if (this.connection != null) {
 			this.connection.disconnect();
 		}
 	}
 }
