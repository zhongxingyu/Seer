 /*-
  * Copyright (c) 2008, Derek Konigsberg
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
 
 package org.logicprobe.LogicMail.mail;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Vector;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.TextContent;
 import org.logicprobe.LogicMail.message.TextPart;
 
 import j2meunit.framework.Test;
 import j2meunit.framework.TestCase;
 import j2meunit.framework.TestMethod;
 import j2meunit.framework.TestSuite;
 
 /**
  * Unit test for NetworkMailStore.
  * This is technically a partial component test, since it also exercises
  * IncomingMailConnectionHandler to some extent.  However, it does use
  * a fake implementation of IncomingMailClient.
  */
 public class NetworkMailStoreTest extends TestCase {
 	private NetworkMailStore instance;
 	
 	private AccountConfig fakeAccountConfig;
 	private FakeIncomingMailClient fakeIncomingMailClient;
 
 	private FolderEvent eventFolderTreeUpdated;
 	private Vector eventFolderMessagesAvailable = new Vector();
 	private FolderEvent eventFolderStatusChanged;
 	private MessageEvent eventMessageAvailable;
 	private MessageEvent eventMessageDeleted;
 	private MessageEvent eventMessageUndeleted;
 	
     public NetworkMailStoreTest() {
     }
 	
     public NetworkMailStoreTest(String testName, TestMethod testMethod) {
         super(testName, testMethod);
     }
     
     public void setUp() {
     	fakeAccountConfig = new AccountConfig() { };
     	fakeIncomingMailClient = new FakeIncomingMailClient();
     	MailClientFactory.setIncomingMailClient(fakeAccountConfig, fakeIncomingMailClient);
     	instance = new NetworkMailStore(fakeAccountConfig);
 
     	instance.addMailStoreListener(new MailStoreListener() {
 			public void folderTreeUpdated(FolderEvent e) {
 				eventFolderTreeUpdated = e;
 			}
     	});
     	
     	instance.addFolderListener(new FolderListener() {
 			public void folderMessagesAvailable(FolderMessagesEvent e) {
 				eventFolderMessagesAvailable.addElement(e);
 			}
 			public void folderStatusChanged(FolderEvent e) {
 				eventFolderStatusChanged = e;
 			}
     	});
     	
     	instance.addMessageListener(new MessageListener() {
 			public void messageAvailable(MessageEvent e) {
 				eventMessageAvailable = e;
 			}
 			public void messageDeleted(MessageEvent e) {
 				eventMessageDeleted = e;
 			}
 			public void messageUndeleted(MessageEvent e) {
 				eventMessageUndeleted = e;
 			}
 			public void messageFlagsChanged(MessageEvent e) {
 			}
     	});
     }
     
     public void tearDown() {
     	instance.shutdown(true);
     	instance = null;
     	fakeAccountConfig = null;
     	fakeIncomingMailClient = null;
     }
 
     public void testProperties() {
     	assertTrue(!instance.isLocal());
     }
 
     /**
      * Tests the mail store shutdown process
      */
     public void testShutdown() {
     	// Make a fake request, then call for shutdown
     	fakeIncomingMailClient.folderTree = new FolderTreeItem("INBOX", "INBOX", ".");
     	instance.requestFolderTree();
     	instance.shutdown(true);
     	
     	// Assert that both open and close were really called, and that
     	// the request was processed.
     	assertTrue("open", fakeIncomingMailClient.openCalled);
     	assertNotNull("request", eventFolderTreeUpdated);
     	assertTrue("close", fakeIncomingMailClient.closeCalled);
 	}
     
     /**
      * Tests the mail store shutdown/restart process
      */
     public void testRestart() {
     	// Make a fake request, then call for shutdown
     	fakeIncomingMailClient.folderTree = new FolderTreeItem("INBOX", "INBOX", ".");
     	instance.requestFolderTree();
     	instance.shutdown(true);
     	
     	// Assert that both open and close were really called, and that
     	// the request was processed.
     	assertTrue("open", fakeIncomingMailClient.openCalled);
     	assertNotNull("request", eventFolderTreeUpdated);
     	assertTrue("close", fakeIncomingMailClient.closeCalled);
     	
     	// Reset the sense flags
     	fakeIncomingMailClient.openCalled = false;
     	eventFolderTreeUpdated = null;
     	fakeIncomingMailClient.closeCalled = false;
     	
     	// Restart the thread and try again
     	instance.restart();
     	instance.requestFolderTree();
     	instance.shutdown(true);
     	
     	assertTrue("restart open", fakeIncomingMailClient.openCalled);
     	assertNotNull("restart request", eventFolderTreeUpdated);
     	assertTrue("restart close", fakeIncomingMailClient.closeCalled);
 	}
     
     public void testRequestFolderTree() {
     	fakeIncomingMailClient.folderTree = new FolderTreeItem("INBOX", "INBOX", ".");
     	instance.requestFolderTree();
     	instance.shutdown(true);
     	
     	assertNotNull(eventFolderTreeUpdated);
     	assertEquals(fakeIncomingMailClient.folderTree, eventFolderTreeUpdated.getFolder());
     }
     
     public void testRequestFolderStatus() {
     	fakeIncomingMailClient.refreshedMsgCount = 42;
     	FolderTreeItem folder = new FolderTreeItem("INBOX", "INBOX", ".");
     	folder.setMsgCount(0);
     	instance.requestFolderStatus(new FolderTreeItem[] { folder });
     	instance.shutdown(true);
     	
     	assertNotNull(eventFolderStatusChanged);
     	assertEquals("INBOX", eventFolderStatusChanged.getFolder().getName());
     	assertEquals(42, eventFolderStatusChanged.getFolder().getMsgCount());
     }
     
     public void testRequestFolderMessages() {
     	fakeIncomingMailClient.folderMessages = new FolderMessage[] {
     		new FolderMessage(new FakeMessageToken(1), new MessageEnvelope(), 42, 52),
     		new FolderMessage(new FakeMessageToken(2), new MessageEnvelope(), 43, 53),
     	};
     	FolderTreeItem folder = new FolderTreeItem("INBOX", "INBOX", ".");
     	instance.requestFolderMessagesRange(folder, 42, 43);
     	instance.shutdown(true);
     	
     	// Cannot assume the number of events that will be fired,
     	// but only the number of folder messages contained within
     	// all of them put together.
     	
     	int eventCount = eventFolderMessagesAvailable.size();
     	assertTrue(eventCount > 0);
     	
     	Vector folderMessagesAvailable = new Vector();
     	for(int i=0; i<eventCount; i++) {
     	    // Check general event properties
     	    FolderMessagesEvent event = (FolderMessagesEvent)eventFolderMessagesAvailable.elementAt(i);
             assertNotNull(event);
             assertEquals("INBOX", event.getFolder().getName());
             
             FolderMessage[] messages = event.getMessages();
             if(i < eventCount - 1) {
                 assertNotNull(messages);
 
                 // Collect folder messages within the event
                 for(int j=0; j<messages.length; j++) {
                     folderMessagesAvailable.addElement(messages[j]);
                 }
             }
     	}
     	
     	// Assert the folder messages
     	assertEquals(2, folderMessagesAvailable.size());
 
     	FolderMessage folderMessage1 = (FolderMessage)folderMessagesAvailable.elementAt(0);
         assertEquals(52, folderMessage1.getUid());
         
         FolderMessage folderMessage2 = (FolderMessage)folderMessagesAvailable.elementAt(1);
         assertEquals(53, folderMessage2.getUid());
     }
     
     public void testRequestMessage() {
     	TextPart part = new TextPart("plain", "", "", "", "", "", -1);
     	TextContent content = new TextContent(part, "Hello World");
     	fakeIncomingMailClient.message = new Message(part);
     	fakeIncomingMailClient.message.putContent(part, content);
     	FakeMessageToken messageToken = new FakeMessageToken(1);
     	instance.requestMessage(messageToken);
     	instance.shutdown(true);
     	
     	assertNotNull(eventMessageAvailable);
     	assertEquals(MessageEvent.TYPE_FULLY_LOADED, eventMessageAvailable.getType());
     	assertNotNull(eventMessageAvailable.getMessageStructure());
     	assertNotNull(eventMessageAvailable.getMessageContent());
     	assertEquals(fakeIncomingMailClient.message.getStructure(), eventMessageAvailable.getMessageStructure());
     	assertEquals(fakeIncomingMailClient.message.getContent(part), eventMessageAvailable.getMessageContent()[0]);
     	assertNotNull(eventMessageAvailable.getMessageToken());
     	assertEquals(fakeIncomingMailClient.messageToken, eventMessageAvailable.getMessageToken());
     }
     
     public void testRequestMessageDelete() {
     	TextPart part = new TextPart("plain", "", "", "", "", "", -1);
     	TextContent content = new TextContent(part, "Hello World");
     	fakeIncomingMailClient.message = new Message(part);
     	fakeIncomingMailClient.message.putContent(part, content);
     	FakeMessageToken messageToken = new FakeMessageToken(1);
     	MessageFlags messageFlags = new MessageFlags();
     	messageFlags.setDeleted(false);
     	instance.requestMessageDelete(messageToken, messageFlags);
     	instance.shutdown(true);
     	
     	assertNotNull(eventMessageDeleted);
     	assertEquals(MessageEvent.TYPE_FLAGS_CHANGED, eventMessageDeleted.getType());
     	assertNotNull(eventMessageDeleted.getMessageToken());
     	assertEquals(fakeIncomingMailClient.messageToken, eventMessageDeleted.getMessageToken());
     	assertTrue(eventMessageDeleted.getMessageFlags().isDeleted());
     }
     
     public void testRequestMessageUndelete() {
     	TextPart part = new TextPart("plain", "", "", "", "", "", -1);
     	TextContent content = new TextContent(part, "Hello World");
     	fakeIncomingMailClient.message = new Message(part);
     	fakeIncomingMailClient.message.putContent(part, content);
     	FakeMessageToken messageToken = new FakeMessageToken(1);
     	MessageFlags messageFlags = new MessageFlags();
     	messageFlags.setDeleted(true);
     	instance.requestMessageUndelete(messageToken, messageFlags);
     	instance.shutdown(true);
     	
     	assertNotNull(eventMessageUndeleted);
     	assertEquals(MessageEvent.TYPE_FLAGS_CHANGED, eventMessageUndeleted.getType());
     	assertNotNull(eventMessageUndeleted.getMessageToken());
     	assertEquals(fakeIncomingMailClient.messageToken, eventMessageUndeleted.getMessageToken());
     	assertTrue(!eventMessageUndeleted.getMessageFlags().isDeleted());
     }
     
     public void testRequestBatch() {
     	TextPart part = new TextPart("plain", "", "", "", "", "", -1);
     	TextContent content = new TextContent(part, "Hello World");
     	fakeIncomingMailClient.message = new Message(part);
     	fakeIncomingMailClient.message.putContent(part, content);
     	FolderTreeItem folder = new FolderTreeItem("INBOX", "INBOX", ".");
     	fakeIncomingMailClient.folderTree = folder;
     	FakeMessageToken messageToken1 = new FakeMessageToken(1);
     	FakeMessageToken messageToken2 = new FakeMessageToken(2);
     	fakeIncomingMailClient.folderMessages = new FolderMessage[] {
         		new FolderMessage(messageToken1, new MessageEnvelope(), 42, 52),
         		new FolderMessage(messageToken2, new MessageEnvelope(), 43, 53),
         };
 
     	// Do a whole batch of non-conflicting requests to make
     	// sure the queue is working correctly.
     	instance.requestFolderTree();
     	instance.requestFolderStatus(new FolderTreeItem[] { folder });
     	instance.requestFolderMessagesRange(folder, 42, 43);
     	instance.requestMessage(messageToken1);
     	instance.shutdown(true);
     	
     	// We know the requests work individually, so lets just
     	// make sure they all went through the system.
     	assertNotNull("requestFolderTree", eventFolderTreeUpdated);
     	assertNotNull("requestFolderStatus", eventFolderStatusChanged);
     	assertNotNull("requestFolderMessages", eventFolderMessagesAvailable);
     	assertNotNull("requestMessage", eventMessageAvailable);
     }
     
     public Test suite() {
         TestSuite suite = new TestSuite("NetworkMailStore");
 
         suite.addTest(new NetworkMailStoreTest("properties", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testProperties(); } }));
         suite.addTest(new NetworkMailStoreTest("shutdown", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testShutdown(); } }));
         suite.addTest(new NetworkMailStoreTest("restart", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRestart(); } }));
         suite.addTest(new NetworkMailStoreTest("requestFolderTree", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestFolderTree(); } }));
         suite.addTest(new NetworkMailStoreTest("requestFolderStatus", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestFolderStatus(); } }));
         suite.addTest(new NetworkMailStoreTest("requestFolderMessages", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestFolderMessages(); } }));
         suite.addTest(new NetworkMailStoreTest("requestMessage", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestMessage(); } }));
         suite.addTest(new NetworkMailStoreTest("requestMessageDelete", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestMessageDelete(); } }));
         suite.addTest(new NetworkMailStoreTest("requestMessageUndelete", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestMessageUndelete(); } }));
         suite.addTest(new NetworkMailStoreTest("request batch", new TestMethod()
         { public void run(TestCase tc) {((NetworkMailStoreTest)tc).testRequestBatch(); } }));
 
         return suite;
     }
 
 	private class FakeIncomingMailClient implements IncomingMailClient {
 		public boolean openCalled = false;
 		public boolean closeCalled = false;
 		public FolderTreeItem activeFolder;
 		public FolderTreeItem inboxFolder;
 		public FolderMessage[] folderMessages;
 		public FolderTreeItem folderTree;
 		public Message message;
 		public MessageToken messageToken;
 		public int refreshedMsgCount;
 		
 		public boolean isConnected() { return true; }
 		public boolean open() throws IOException, MailException { openCalled = true; return true; }
 		public void close() throws IOException, MailException { closeCalled = true; }
 		public String getPassword() { return fakeAccountConfig.getServerPass(); }
 		public String getUsername() { return fakeAccountConfig.getServerUser(); }
 		public void setPassword(String password) { fakeAccountConfig.setServerPass(password); }
 		public void setUsername(String username) { fakeAccountConfig.setServerUser(username); }
 		public boolean hasFolders() { return true; }
 		public boolean hasUndelete() { return true; }
 		public boolean hasAppend() { return false; }
 		public boolean hasCopy() { return false; }
 		public AccountConfig getAcctConfig() { return fakeAccountConfig; }
 		public FolderTreeItem getInboxFolder() { return inboxFolder; }
 		public FolderTreeItem getActiveFolder() { return activeFolder; }
 		public void getFolderMessages(int firstIndex, int lastIndex, FolderMessageCallback callback, MailProgressHandler progressHandler)
 				throws IOException, MailException {
 		    for(int i=0; i<folderMessages.length; i++) {
 		        callback.folderMessageUpdate(folderMessages[i]);
 		    }
 		    callback.folderMessageUpdate(null);
 	    }
 		public void getFolderMessages(MessageToken[] messageTokens, FolderMessageCallback callback, MailProgressHandler progressHandler)
 				throws IOException, MailException { }
 		public void getNewFolderMessages(boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler)
 				throws IOException,	MailException { }
 		public FolderTreeItem getFolderTree(MailProgressHandler progressHandler) throws IOException, MailException { return this.folderTree; }
 		public Message getMessage(MessageToken messageToken, MailProgressHandler progressHandler)
 				throws IOException, MailException { this.messageToken = messageToken; return this.message; }
 		public void refreshFolderStatus(FolderTreeItem[] folders, MailProgressHandler progressHandler)
 				throws IOException, MailException { folders[0].setMsgCount(refreshedMsgCount); }
 		public void setActiveFolder(FolderTreeItem folderItem)
 		throws IOException, MailException { this.activeFolder = folderItem; }
 		public void setActiveFolder(MessageToken messageToken)
 				throws IOException, MailException { this.messageToken = messageToken; }
 		public void deleteMessage(MessageToken messageToken, MessageFlags messageFlags)
 		throws IOException, MailException { messageFlags.setDeleted(true); this.messageToken = messageToken;  }
 		public void undeleteMessage(MessageToken messageToken, MessageFlags messageFlags)
 				throws IOException, MailException { messageFlags.setDeleted(false); this.messageToken = messageToken;  }
 		public void appendMessage(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags)
 				throws IOException, MailException { }
 		public void copyMessage(MessageToken messageToken, FolderTreeItem destinationFolder)
 				throws IOException, MailException { }
 		public ConnectionConfig getConnectionConfig() { return null; }
 		public boolean noop() throws IOException, MailException { return false; }
 		public boolean hasIdle() { return false; }
 		public void idleModeBegin() throws IOException, MailException { }
 		public void idleModeEnd() throws IOException, MailException { }
 		public boolean idleModePoll() throws IOException, MailException { return false; }
        public void expungeActiveFolder() throws IOException, MailException { }
        public boolean hasExpunge() { return false; }
 	};
 	
 	private class FakeMessageToken implements MessageToken {
 		private long uniqueId;
 		public FakeMessageToken(long uniqueId) { this.uniqueId = uniqueId; }
 		public long getUniqueId() { return uniqueId; }
 		public void deserialize(DataInput input) throws IOException { }
 		public void serialize(DataOutput output) throws IOException { }
 		public boolean containedWithin(FolderTreeItem folderTreeItem) { return true; }
 		public String getMessageUid() {	return null; }
         public void updateToken(MessageToken messageToken) { }
         public boolean isLoadable() { return true; }
 	}
 }
