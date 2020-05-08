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
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.rim.device.api.collection.util.BigVector;
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.util.Comparator;
 import net.rim.device.api.util.InvertedOrderComparator;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.util.StringParser;
 import org.logicprobe.LogicMail.util.FilenameEncoder;
 
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
 	private static String LOCAL_ACCOUNT_UID = "local";
     private static String LOCAL_OUTBOX_UID = "Outbox";
 	
 	/**
 	 * Instantiates a new mail file manager.
 	 */
     private MailFileManager() {
         mailSettings = MailSettings.getInstance();
 
         // Register a listener for configuration changes
         mailSettings.addMailSettingsListener(new MailSettingsListener() {
             public void mailSettingsSaved(MailSettingsEvent e) {
                 if((e.getGlobalChange() & GlobalConfig.CHANGE_TYPE_DATA) != 0) {
                     refreshConfiguration();
                 }
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
 	synchronized void refreshConfiguration() {
 		String localDataLocation = mailSettings.getGlobalConfig().getLocalDataLocation();
 		String newCacheUrl = localDataLocation + CACHE_PREFIX;
 		if(!newCacheUrl.equals(cacheUrl)) {
 			FileConnection fileConnection;
 			try {
 				fileConnection = (FileConnection)Connector.open(localDataLocation);
 				if(!fileConnection.exists()) {
 					fileConnection.mkdir();
 				}
 				fileConnection.close();
 				
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
 	public synchronized void writeMessage(MessageNode messageNode) throws IOException {
 		if(cacheUrl == null) { return; }
 		
 		// Create a file connection for the message
 		FileConnection fileConnection = getMailboxFileConnection(messageNode.getParent());
 		String url = fileConnection.getURL();
 		fileConnection.close();
 		fileConnection = (FileConnection)Connector.open(
 				getMessageFileUrl(url, messageNode.getMessageToken()));
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
 	public synchronized MessageToken[] readMessageTokens(MailboxNode mailboxNode) throws IOException {
 		if(cacheUrl == null) { return new MessageToken[0]; }
 		
 		//TODO: Implement an indexing mechanism to avoid listing the whole directory
 		//TODO: Provide support for partial listing requests
 		
 		Vector messageTokenList = new Vector();
 		
         String[] fileUrls = getMessageFiles(mailboxNode);
         for(int i=0; i<fileUrls.length; i++) {
             MessageToken messageToken = readMessageToken(fileUrls[i]);
             if(messageToken != null) {
                 messageTokenList.addElement(messageToken);
             }
         }
         
 		MessageToken[] result = new MessageToken[messageTokenList.size()];
 		messageTokenList.copyInto(result);
 		return result;
 	}
 	
 	public synchronized MessageNode[] readMessageNodes(MailboxNode mailboxNode) throws IOException {
 	    return readMessageNodes(mailboxNode, false);
 	}
 	
 	public synchronized MessageNode[] readMessageNodes(MailboxNode mailboxNode, boolean loadContent) throws IOException {
 		if(cacheUrl == null) { return new MessageNode[0]; }
 
 		Vector messageNodeList = new Vector();
 		
         String[] fileUrls = getMessageFiles(mailboxNode);
         for(int i=0; i<fileUrls.length; i++) {
             MessageNode messageNode = readMessageNode(fileUrls[i]);
             if(messageNode != null) {
                 if(loadContent) {
                     MimeMessageContent[] content = readMessageContent(mailboxNode, messageNode.getMessageToken());
                     messageNode.putMessageContent(content);
                 }
                 messageNodeList.addElement(messageNode);
             }
         }
 		
 		MessageNode[] result = new MessageNode[messageNodeList.size()];
 		messageNodeList.copyInto(result);
 		return result;
 	}
 	
 	public synchronized void readMessageNodes(MailboxNode mailboxNode, MessageNodeCallback callback) throws IOException {
 	    readMessageNodes(mailboxNode, false, callback);
 	}
 	
     public synchronized void readMessageNodes(MailboxNode mailboxNode, boolean loadContent, MessageNodeCallback callback) throws IOException {
         if(cacheUrl == null) { callback.messageNodeUpdated(null); }
         
         String[] fileUrls = getMessageFiles(mailboxNode);
         Vector fileUrlsToLoad = new Vector(fileUrls.length);
         for(int i=0; i<fileUrls.length; i++) {
             String messageUid = getMessageUidFromFileUrl(fileUrls[i]);
             if(messageUid != null) {
                 if(callback.messageNodeAvailable(messageUid)) {
                     fileUrlsToLoad.addElement(fileUrls[i]);
                 }
             }
         }
         
         int size = fileUrlsToLoad.size();
         for(int i=0; i<size; i++) {
             MessageNode messageNode = readMessageNode((String)fileUrlsToLoad.elementAt(i));
             if(messageNode != null) {
                 if(loadContent) {
                     MimeMessageContent[] content = readMessageContent(mailboxNode, messageNode.getMessageToken());
                     messageNode.putMessageContent(content);
                 }
                 callback.messageNodeUpdated(messageNode);
             }
         }
         
         callback.messageNodeUpdated(null);
     }
 
     private String[] getMessageFiles(MailboxNode mailboxNode) throws IOException {
         BigVector fileVector = new BigVector();
         Comparator comparator;
         if(mailSettings.getGlobalConfig().getDispOrder()) {
             comparator = new MailFileComparator();
         }
         else {
             comparator = new InvertedOrderComparator(new MailFileComparator());
         }
         
         FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
         String mailboxUrl = fileConnection.getURL();
         Enumeration e = fileConnection.list(MSG_FILTER, false);
         while(e.hasMoreElements()) {
             fileVector.insertElement(comparator, e.nextElement());
         }
         fileConnection.close();
 
         int size = fileVector.size();
         String[] result = new String[size];
         for(int i=0; i<size; i++) {
             result[i] = mailboxUrl + fileVector.elementAt(i);
         }
         return result;
     }
 
     public synchronized boolean messageNodeExists(MailboxNode mailboxNode, MessageToken messageToken) {
         if(cacheUrl == null) { return false; }
         
         FileConnection fileConnection = null;
         try {
             fileConnection = getMailboxFileConnection(mailboxNode);
             if(!fileConnection.exists()) { return false; }
             
             String mailboxUrl = fileConnection.getURL();
             fileConnection.close();
             String fileUrl = getMessageFileUrl(mailboxUrl, messageToken);
             fileConnection = (FileConnection)Connector.open(fileUrl);
             if(fileConnection.exists() && fileConnection.canRead()) {
                 return true;
             }
             fileConnection.close();
         } catch (IOException e) {
             return false;
         } finally {
             if(fileConnection != null) {
                 try { fileConnection.close(); } catch (Exception e) { }
             }
         }
         return false;
     }
     
 	public synchronized MessageNode readMessageNode(MailboxNode mailboxNode, MessageToken messageToken, boolean loadContent) throws IOException {
 		if(cacheUrl == null) { return null; }
 
 		FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
 		if(!fileConnection.exists()) { return null; }
 		
 		String mailboxUrl = fileConnection.getURL();
 		fileConnection.close();
 		String fileUrl = getMessageFileUrl(mailboxUrl, messageToken);
 		MessageNode messageNode = readMessageNode(fileUrl);
 		if(messageNode == null) { return null; }
 		
 		if(loadContent) {
 			MimeMessageContent[] content = readMessageContent(mailboxNode, messageToken);
 			messageNode.putMessageContent(content);
 		}
 		return messageNode;
 	}
 	
 	public synchronized MimeMessageContent[] readMessageContent(MailboxNode mailboxNode, MessageToken messageToken) throws IOException {
 		if(cacheUrl == null) { return null; }
 		
 		FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
 		if(!fileConnection.exists()) { return null; }
 		
 		String mailboxUrl = fileConnection.getURL();
 		fileConnection.close();
 		String fileUrl = getMessageFileUrl(mailboxUrl, messageToken);
 		MimeMessageContent[] messageContent = readMessageContent(fileUrl, messageToken);
 		
 		return messageContent;
 	}
 	
 	private FileConnection getMailboxFileConnection(MailboxNode mailboxNode) throws IOException {
 		// Build the account and mailbox UID strings
 	    String accountUid;
 	    String mailboxUid;
 	    if(mailboxNode instanceof OutboxMailboxNode) {
 	        accountUid = LOCAL_ACCOUNT_UID;
 	        mailboxUid = LOCAL_OUTBOX_UID;
 	    }
 	    else {
     		accountUid = StringParser.toHexString(
     				((NetworkAccountNode)mailboxNode.getParentAccount()).getUniqueId()).toLowerCase();
     		mailboxUid = StringParser.toHexString(
     				mailboxNode.getUniqueId()).toLowerCase();
 	    }
 	    
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
 			if(fileConnection.exists()) {
     			DataInputStream input = fileConnection.openDataInputStream();
     			MessageNodeReader reader = new MessageNodeReader(input);
     			result = reader.readMessageNode();
     			result.setCached(true);
     			input.close();
 			}
 			else {
 			    result = null;
 			}
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
 	
 	private MimeMessageContent[] readMessageContent(String fileUrl, MessageToken messageToken) {
 		FileConnection fileConnection = null;
 		Vector content = new Vector();
 		try {
 			fileConnection = (FileConnection)Connector.open(fileUrl);
			DataInputStream input = fileConnection.openDataInputStream();
			MessageNodeReader reader = new MessageNodeReader(input);
			MessageToken tempToken = reader.read();
			if(messageToken.equals(tempToken)) {
				while(reader.isContentAvailable()) {
					content.addElement(reader.getNextContent());
				}
 			}
			input.close();
 			fileConnection.close();
 		} catch (Exception e) {
 			EventLogger.logEvent(AppInfo.GUID,
 	                ("Unable to read content: " + fileUrl
                 		+ "\r\n" + e.getMessage()).getBytes(),
 	                EventLogger.ERROR);
 		} finally {
 			if(fileConnection != null) {
 				try { fileConnection.close(); } catch (Exception e) { }
 			}
 		}
 		MimeMessageContent[] result = new MimeMessageContent[content.size()];
 		content.copyInto(result);
 		return result;
 	}
 	
 	public synchronized void removeStaleMessageNodes(MailboxNode mailboxNode, String[] uidsToRetain) throws IOException {
 	    if(cacheUrl == null) { return; }
 	    
 	    Hashtable retentionSet = new Hashtable();
 	    for(int i=0; i<uidsToRetain.length; i++) {
 	        retentionSet.put(FilenameEncoder.encode(uidsToRetain[i]), Boolean.TRUE);
 	    }
 	    
         String[] fileUrls = getMessageFiles(mailboxNode);
         Vector fileUrlsToDelete = new Vector();
         for(int i=0; i<fileUrls.length; i++) {
             int p = fileUrls[i].lastIndexOf('/');
             int q = fileUrls[i].lastIndexOf('.');
             if(p != -1 && q != -1 && p < q) {
                 String messageUid = fileUrls[i].substring(p + 1, q);
                 if(!retentionSet.containsKey(messageUid)) {
                     fileUrlsToDelete.addElement(fileUrls[i]);
                 }
             }
         }
         
         int size = fileUrlsToDelete.size();
         for(int i=0; i<size; i++) {
             try {
                 FileConnection fc = (FileConnection)Connector.open((String)fileUrlsToDelete.elementAt(i));
                 if(fc.exists() && fc.canRead()) {
                     fc.delete();
                 }
             } catch (IOException exp) {
                 if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
                     EventLogger.logEvent(AppInfo.GUID,
                             ("Error deleting message from cache: " + exp.toString()).getBytes(),
                             EventLogger.DEBUG_INFO);
                 }
             }
         }
 	}
 	
 	public synchronized void removeMessageNodes(MailboxNode mailboxNode, MessageToken[] messageTokens) throws IOException {
         if(cacheUrl == null) { return; }
 
         FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
         String mailboxUrl = fileConnection.getURL();
         fileConnection.close();
         
         for(int i=0; i<messageTokens.length; i++) {
             try {
                 FileConnection mailFileConnection =
                     (FileConnection)Connector.open(getMessageFileUrl(mailboxUrl, messageTokens[i]));
                 if(mailFileConnection.exists() && !mailFileConnection.isDirectory() && mailFileConnection.canRead()) {
                     mailFileConnection.delete();
                 }
                 mailFileConnection.close();
             } catch (IOException exp) {
                 if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
                     EventLogger.logEvent(AppInfo.GUID,
                             ("Error deleting message from cache: " + exp.toString()).getBytes(),
                             EventLogger.DEBUG_INFO);
                 }
             }
         }
 	}
 
     public synchronized void removeMessageNode(MailboxNode mailboxNode, MessageToken messageToken) throws IOException {
         if(cacheUrl == null) { return; }
 
         FileConnection fileConnection = getMailboxFileConnection(mailboxNode);
         String mailboxUrl = fileConnection.getURL();
         fileConnection.close();
         
         try {
             FileConnection mailFileConnection =
                 (FileConnection)Connector.open(getMessageFileUrl(mailboxUrl, messageToken));
             if(mailFileConnection.exists() && !mailFileConnection.isDirectory() && mailFileConnection.canRead()) {
                 mailFileConnection.delete();
             }
             mailFileConnection.close();
         } catch (IOException exp) {
             if (EventLogger.getMinimumLevel() >= EventLogger.DEBUG_INFO) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("Error deleting message from cache: " + exp.toString()).getBytes(),
                         EventLogger.DEBUG_INFO);
             }
         }
     }
 
     private static String getMessageFileUrl(String mailboxUrl, MessageToken messageToken) {
         return mailboxUrl + FilenameEncoder.encode(messageToken.getMessageUid()) + MSG_SUFFIX;
     }
     
     private static String getMessageUidFromFileUrl(String fileUrl) {
         int p = fileUrl.lastIndexOf('/');
         int q = fileUrl.lastIndexOf('.');
         if(p != -1 && q != -1 && p < q) {
             return FilenameEncoder.decode(fileUrl.substring(p + 1, q));
         }
         else {
             return null;
         }
     }
 }
