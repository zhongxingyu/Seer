 /*-
  * Copyright (c) 2009, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.model;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.rim.device.api.system.EventLogger;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.mail.MessageToken;
 
 /**
  * Front-end for reading and writing messages from local file storage.
  */
 public class MailFileManager {
 	private static MailFileManager instance;
 	private MailSettings mailSettings;
 	private String cacheUrl;
 	
 	private static String CACHE_PREFIX = "cache/";
 	private static String MSG_SUFFIX = ".msg";
 	private static String MSG_FILTER = "*.msg";
 
 	/**
 	 * Instantiates a new mail file manager.
 	 */
 	private MailFileManager() {
 		mailSettings = MailSettings.getInstance();
 		
 		// Register a listener for configuration changes
 		mailSettings.addMailSettingsListener(new MailSettingsListener() {
 			public void mailSettingsSaved(MailSettingsEvent e) {
 				refreshConfiguration();
 			}
 		});
 		
 		refreshConfiguration();
 	}
 	
 	/**
 	 * Gets the single instance of MailFileManager.
 	 * 
 	 * @return single instance of MailFileManager
 	 */
 	public static synchronized MailFileManager getInstance() {
 		if(instance == null) {
 			instance = new MailFileManager();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Refreshes the configuration based on any system configuration changes.
 	 */
 	void refreshConfiguration() {
		String newCacheUrl = mailSettings.getGlobalConfig().getLocalDataLocation() + CACHE_PREFIX;
 		if(!newCacheUrl.equals(cacheUrl)) {
 			FileConnection fileConnection;
 			try {
 				fileConnection = (FileConnection)Connector.open(newCacheUrl);
 				if(!fileConnection.exists()) {
 					fileConnection.mkdir();
 				}
 				fileConnection.close();
 				cacheUrl = newCacheUrl;
 			} catch (IOException e) {
 				EventLogger.logEvent(AppInfo.GUID,
 		                ("Unable to open cache: " + newCacheUrl
 	                		+ "\r\n" + e.getMessage()).getBytes(),
 		                EventLogger.ERROR);
 				cacheUrl = null;
 			}
 		}
 	}
 	
 	/**
 	 * Write a message node to local storage.
 	 * If data already exists matching the file path convention for the provided
 	 * message, it will be overwritten.
 	 * 
 	 * @param messageNode the message node
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	public void writeMessage(MessageNode messageNode) throws IOException {
 		// Create a file connection for the message
 		FileConnection fileConnection = getMailboxFileConnection(messageNode.getParent());
 		String url = fileConnection.getURL();
 		fileConnection.close();
 		fileConnection = (FileConnection)Connector.open(
 				url
 				+ messageNode.getMessageToken().getMessageUid()
 				+ MSG_SUFFIX);
 		if(fileConnection.exists()) {
 			fileConnection.truncate(0);
 		}
 		else {
 			fileConnection.create();
 		}
 		
 		// Create the writer
 		DataOutputStream output = fileConnection.openDataOutputStream();
 		MessageNodeWriter writer = new MessageNodeWriter(output);
 		
 		// Write the node
 		writer.write(messageNode);
 		
 		// Close and cleanup
 		output.close();
 		fileConnection.close();
 	}
 	
 	/**
 	 * Read message tokens for all the messages contained within the local
 	 * storage location backing the provided mailbox node.
 	 * 
 	 * @param mailboxNode the mailbox node
 	 * 
 	 * @return the message tokens
 	 * 
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
 	public MessageToken[] readMessageTokens(MailboxNode mailboxNode) throws IOException {
 		//TODO: Implement an indexing mechanism to avoid listing the whole directory
 		//TODO: Provide support for partial listing requests
 		
 		Vector messageTokenList = new Vector();
 		
 		FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
 		String mailboxUrl = fileConnection.getURL();
 		Enumeration e = fileConnection.list(MSG_FILTER, false);
 		while(e.hasMoreElements()) {
 			String fileUrl = mailboxUrl + e.nextElement();
 			MessageToken messageToken = readMessageToken(fileUrl);
 			if(messageToken != null) {
 				messageTokenList.addElement(messageToken);
 			}
 		}
 		fileConnection.close();
 		
 		MessageToken[] result = new MessageToken[messageTokenList.size()];
 		messageTokenList.copyInto(result);
 		return result;
 	}
 	
 	public MessageNode[] readMessageNodes(MailboxNode mailboxNode) throws IOException {
 		Vector messageNodeList = new Vector();
 		
 		FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
 		String mailboxUrl = fileConnection.getURL();
 		Enumeration e = fileConnection.list(MSG_FILTER, false);
 		while(e.hasMoreElements()) {
 			String fileUrl = mailboxUrl + e.nextElement();
 			MessageNode messageNode = readMessageNode(fileUrl);
 			if(messageNode != null) {
 				messageNodeList.addElement(messageNode);
 			}
 		}
 		fileConnection.close();
 		
 		MessageNode[] result = new MessageNode[messageNodeList.size()];
 		messageNodeList.copyInto(result);
 		return result;
 	}
 	
 	private FileConnection getMailboxFileConnection(MailboxNode mailboxNode) throws IOException {
 		// Build the account and mailbox UID strings
 		String accountUid = Long.toString(
 				mailboxNode.getParentAccount().getAccountConfig().getUniqueId(), 16).toLowerCase();
 		String mailboxUid = Long.toString(
 				mailboxNode.getUniqueId(), 16).toLowerCase();
 		StringBuffer buf = new StringBuffer(cacheUrl);
 		
 		// Open the account directory, creating if necessary
 		buf.append(accountUid); buf.append('/');
 		FileConnection fileConnection = (FileConnection)Connector.open(buf.toString());
 		if(!fileConnection.exists()) {
 			fileConnection.mkdir();
 		}
 		fileConnection.close();
 		
 		// Traverse to the mailbox directory, creating if necessary
 		buf.append(mailboxUid); buf.append('/');
 		fileConnection = (FileConnection)Connector.open(buf.toString());
 		if(!fileConnection.exists()) {
 			fileConnection.mkdir();
 		}
 		
 		return fileConnection;
 	}
 	
 	private MessageToken readMessageToken(String fileUrl) {
 		MessageToken result;
 		FileConnection fileConnection = null;
 		try {
 			fileConnection = (FileConnection)Connector.open(fileUrl);
 			DataInputStream input = fileConnection.openDataInputStream();
 			MessageNodeReader reader = new MessageNodeReader(input);
 			result = reader.read();
 			input.close();
 			fileConnection.close();
 		} catch (Exception e) {
 			EventLogger.logEvent(AppInfo.GUID,
 	                ("Unable to read token: " + fileUrl
                 		+ "\r\n" + e.getMessage()).getBytes(),
 	                EventLogger.ERROR);
 			result = null;
 		} finally {
 			if(fileConnection != null) {
 				try { fileConnection.close(); } catch (Exception e) { }
 			}
 		}
 		return result;
 	}
 	
 	private MessageNode readMessageNode(String fileUrl) {
 		MessageNode result;
 		FileConnection fileConnection = null;
 		try {
 			fileConnection = (FileConnection)Connector.open(fileUrl);
 			DataInputStream input = fileConnection.openDataInputStream();
 			MessageNodeReader reader = new MessageNodeReader(input);
 			result = reader.readMessageNode();
 			result.setCached(true);
 			input.close();
 			fileConnection.close();
 		} catch (Exception e) {
 			EventLogger.logEvent(AppInfo.GUID,
 	                ("Unable to read token: " + fileUrl
                 		+ "\r\n" + e.getMessage()).getBytes(),
 	                EventLogger.ERROR);
 			result = null;
 		} finally {
 			if(fileConnection != null) {
 				try { fileConnection.close(); } catch (Exception e) { }
 			}
 		}
 		return result;
 	}
 }
