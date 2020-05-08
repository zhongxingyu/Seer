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
 
 import java.util.Vector;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.TextContent;
 import org.logicprobe.LogicMail.message.TextPart;
 
 import com.hammingweight.hammock.DefaultArgumentMatcher;
 import com.hammingweight.hammock.Hammock;
 import com.hammingweight.hammock.IArgumentMatcher;
 
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
     private Hammock hammock;
 	private NetworkMailStore instance;
 	
 	private AccountConfig fakeAccountConfig;
 	private MockIncomingMailClient mockIncomingMailClient;
 	private IncomingMailClientListener clientListener;
     private FolderTreeItem inboxFolder;
 
 	private FolderEvent eventFolderTreeUpdated;
 	private Vector eventFolderMessagesAvailable = new Vector();
 	private FolderEvent eventFolderStatusChanged;
 	private MessageEvent eventMessageAvailable;
 	private MessageEvent eventMessageFlagsChanged;
 	
     public NetworkMailStoreTest() {
     }
 	
     public NetworkMailStoreTest(String testName, TestMethod testMethod) {
         super(testName, testMethod);
     }
     
     public void setUp() {
         hammock = new Hammock();
     	fakeAccountConfig = new AccountConfig() { };
     	inboxFolder = new FolderTreeItem("INBOX", "INBOX", ".");
     	createMockMailClient();
     	MailClientFactory.setIncomingMailClient(fakeAccountConfig, mockIncomingMailClient);
     	instance = new NetworkMailStore(fakeAccountConfig);
 
     	instance.addMailStoreListener(new MailStoreListener() {
 			public void folderTreeUpdated(FolderEvent e) {
 				eventFolderTreeUpdated = e;
 			}
             public void refreshRequired(MailStoreEvent e) { }
     	});
     	
     	instance.addFolderListener(new FolderListener() {
 			public void folderMessagesAvailable(FolderMessagesEvent e) {
 				eventFolderMessagesAvailable.addElement(e);
 			}
 			public void folderStatusChanged(FolderEvent e) {
 				eventFolderStatusChanged = e;
 			}
             public void folderExpunged(FolderExpungedEvent e) { }
             public void folderMessageIndexMapAvailable(FolderMessageIndexMapEvent e) { }
             public void folderRefreshRequired(FolderEvent e) { }
     	});
     	
     	instance.addMessageListener(new MessageListener() {
 			public void messageAvailable(MessageEvent e) {
 				eventMessageAvailable = e;
 			}
 			public void messageFlagsChanged(MessageEvent e) {
 			    eventMessageFlagsChanged = e;
 			}
     	});
     }
     
     private void createMockMailClient() {
         mockIncomingMailClient = new MockIncomingMailClient(hammock);
         
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_IS_CONNECTED).setReturnValue(Boolean.TRUE);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_IS_LOGIN_REQUIRED).setReturnValue(Boolean.TRUE);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_GET_USERNAME).setReturnValue(fakeAccountConfig.getServerUser());
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_SET_USERNAME_$_STRING, new Object[] { fakeAccountConfig.getServerUser() });
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_GET_PASSWORD).setReturnValue(fakeAccountConfig.getServerPass());
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_SET_PASSWORD_$_STRING, new Object[] { fakeAccountConfig.getServerPass() });
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_HAS_FOLDERS).setReturnValue(Boolean.TRUE);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_HAS_UNDELETE).setReturnValue(Boolean.TRUE);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_HAS_LOCKED_FOLDERS).setReturnValue(Boolean.TRUE);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_HAS_IDLE).setReturnValue(Boolean.FALSE);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_GET_ACCT_CONFIG).setReturnValue(fakeAccountConfig);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_GET_CONNECTION_CONFIG).setReturnValue(fakeAccountConfig);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_SET_LISTENER_$_INCOMINGMAILCLIENTLISTENER, new Object[1])
         .setArgumentMatcher(0, new IArgumentMatcher() {
             public boolean areArgumentsEqual(Object argumentExpected, Object argumentActual) {
                 NetworkMailStoreTest.this.clientListener = (IncomingMailClientListener)argumentActual;
                 return true;
             }
         });
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_GET_INBOX_FOLDER).setReturnValue(inboxFolder);
         hammock.setStubExpectation(MockIncomingMailClient.MTHD_GET_ACTIVE_FOLDER).setReturnValue(inboxFolder);
     }
     
     public void tearDown() {
     	instance.shutdown(true);
     	instance = null;
     	fakeAccountConfig = null;
     	mockIncomingMailClient = null;
     }
 
     public void testProperties() {
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
         instance.shutdown(true);
     	assertTrue(!instance.isLocal());
         hammock.verify();
     }
 
     /**
      * Tests the mail store shutdown process
      */
     public void testShutdown() {
     	// Make a fake request, then call for shutdown
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_TREE_$_MAILPROGRESSHANDLER,
                 new Object[] { null }).ignoreArgument(0).setReturnValue(new FolderTreeItem("INBOX", "INBOX", "."));
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
         
     	instance.processRequest(instance.createFolderTreeRequest());
     	instance.shutdown(true);
     	
     	// Assert that both open and close were really called, and that
     	// the request was processed.
     	assertNotNull("request", eventFolderTreeUpdated);
         hammock.verify();
 	}
     
     /**
      * Tests the mail store shutdown/restart process
      */
     public void testRestart() {
     	// Make a fake request, then call for shutdown
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_TREE_$_MAILPROGRESSHANDLER,
                 new Object[] { null }).ignoreArgument(0).setReturnValue(new FolderTreeItem("INBOX", "INBOX", "."));
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
         
         instance.processRequest(instance.createFolderTreeRequest());
     	instance.shutdown(true);
     	
     	// Assert that both open and close were really called, and that
     	// the request was processed.
     	assertNotNull("request", eventFolderTreeUpdated);
     	
     	// Reset the sense flags and add more expectations
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_TREE_$_MAILPROGRESSHANDLER,
                 new Object[] { null }).ignoreArgument(0).setReturnValue(new FolderTreeItem("INBOX", "INBOX", "."));
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	eventFolderTreeUpdated = null;
     	
     	// Restart the thread and try again
     	instance.restart();
     	instance.processRequest(instance.createFolderTreeRequest());
     	instance.shutdown(true);
     	
     	assertNotNull("restart request", eventFolderTreeUpdated);
         hammock.verify();
 	}
     
     public void testRequestFolderTree() {
         FolderTreeItem testFolderTree = new FolderTreeItem("INBOX", "INBOX", ".");
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_TREE_$_MAILPROGRESSHANDLER,
                 new Object[] { null }).ignoreArgument(0).setReturnValue(testFolderTree);
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
         
     	TestCallback callback = new TestCallback();
     	instance.processRequest(instance.createFolderTreeRequest().setRequestCallback(callback));
     	instance.shutdown(true);
 
     	assertTrue(callback.completed);
     	assertTrue(!callback.failed);
     	assertNull(callback.exception);
         
     	assertNotNull(eventFolderTreeUpdated);
     	assertEquals(testFolderTree, eventFolderTreeUpdated.getFolder());
         hammock.verify();
     }
     
     public void testRequestFolderStatus() {
         inboxFolder.setMsgCount(0);
         FolderTreeItem[] folderArray = new FolderTreeItem[] { inboxFolder };
     	
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_REFRESH_FOLDER_STATUS_$_ARRAY_FOLDERTREEITEM_MAILPROGRESSHANDLER,
                 new Object[] { folderArray, null }).setArgumentMatcher(0, new DefaultArgumentMatcher() {
                     public boolean areArgumentsEqual(Object argumentExpected, Object argumentActual) {
                         if(!super.areArgumentsEqual(argumentExpected, argumentActual)) { return false; }
                         
                         ((FolderTreeItem[])argumentActual)[0].setMsgCount(42);
                         return true;
                     }
                 }).ignoreArgument(1);
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	
         TestCallback callback = new TestCallback();
         instance.processRequest(instance.createFolderStatusRequest(folderArray).setRequestCallback(callback));
     	instance.shutdown(true);
     	
         assertTrue(callback.completed);
         assertTrue(!callback.failed);
         assertNull(callback.exception);
         
     	assertNotNull(eventFolderStatusChanged);
     	assertEquals("INBOX", eventFolderStatusChanged.getFolder().getName());
     	assertEquals(42, eventFolderStatusChanged.getFolder().getMsgCount());
         hammock.verify();
     }
     
     public void testRequestFolderMessages() {
         final FolderMessage[] folderMessageArray = new FolderMessage[] {
     		new FolderMessage(new FakeMessageToken(1), new MessageEnvelope(), 42, 52, -1),
     		new FolderMessage(new FakeMessageToken(2), new MessageEnvelope(), 43, 53, -1),
     	};
     	FakeMessageToken token = new FakeMessageToken(5);
     	
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
     	hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_MESSAGES_$_MESSAGETOKEN_INT_FOLDERMESSAGECALLBACK_MAILPROGRESSHANDLER,
     	        new Object[] { token, new Integer(5), null, null })
     	        .setArgumentMatcher(2, new IArgumentMatcher() {
                     public boolean areArgumentsEqual(Object argumentExpected, Object argumentActual) {
                         if(!(argumentActual instanceof FolderMessageCallback)) { return false; }
                         FolderMessageCallback messageCallback = (FolderMessageCallback)argumentActual;
                         for(int i=0; i<folderMessageArray.length; i++) {
                             messageCallback.folderMessageUpdate(folderMessageArray[i]);
                         }
                         messageCallback.folderMessageUpdate(null);
                         return true;
                     }
     	            
     	        }).ignoreArgument(3);
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	
     	TestCallback callback = new TestCallback();
     	instance.processRequest(instance.createFolderMessagesRangeRequest(inboxFolder, token, 5).setRequestCallback(callback));
     	instance.shutdown(true);
     	
         assertTrue(callback.completed);
         assertTrue(!callback.failed);
         assertNull(callback.exception);
         
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
         hammock.verify();
     }
     
     public void testRequestMessage() {
     	TextPart part = new TextPart("plain", "", "", "", "", "", -1);
     	TextContent content = new TextContent(part, "Hello World");
     	Message testMessage = new Message(part);
     	testMessage.putContent(part, content);
     	FakeMessageToken messageToken = new FakeMessageToken(1);
     	
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_SET_ACTIVE_FOLDER_$_MESSAGETOKEN_BOOLEAN,
                 new Object[] { messageToken, Boolean.TRUE }).setReturnValue(null);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_MESSAGE_$_MESSAGETOKEN_BOOLEAN_MAILPROGRESSHANDLER,
                 new Object[] { messageToken, Boolean.TRUE, null }).ignoreArgument(2)
                 .setReturnValue(testMessage);
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	
     	TestCallback callback = new TestCallback();
     	instance.processRequest(instance.createMessageRequest(messageToken, true).setRequestCallback(callback));
     	instance.shutdown(true);
     	
         assertTrue(callback.completed);
         assertTrue(!callback.failed);
         assertNull(callback.exception);
     	
     	assertNotNull(eventMessageAvailable);
     	assertEquals(MessageEvent.TYPE_FULLY_LOADED, eventMessageAvailable.getType());
     	assertNotNull(eventMessageAvailable.getMessageStructure());
     	assertNotNull(eventMessageAvailable.getMessageContent());
     	assertEquals(testMessage.getStructure(), eventMessageAvailable.getMessageStructure());
     	assertEquals(testMessage.getContent(part), eventMessageAvailable.getMessageContent()[0]);
     	assertNotNull(eventMessageAvailable.getMessageToken());
     	assertEquals(messageToken, eventMessageAvailable.getMessageToken());
         hammock.verify();
     }
     
     public void testRequestMessageDelete() {
     	FakeMessageToken messageToken = new FakeMessageToken(1);
     	
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_SET_ACTIVE_FOLDER_$_MESSAGETOKEN_BOOLEAN,
                 new Object[] { messageToken, Boolean.TRUE }).setReturnValue(null);
     	hammock.setExpectation(MockIncomingMailClient.MTHD_DELETE_MESSAGE_$_MESSAGETOKEN,
     	        new Object[] { messageToken }).setArgumentMatcher(0, new DefaultArgumentMatcher() {
     	            public boolean areArgumentsEqual(Object argumentExpected, Object argumentActual) {
     	                if(!super.areArgumentsEqual(argumentExpected, argumentActual)) { return false; }
 	                    clientListener.folderMessageFlagsChanged(
 	                            (FakeMessageToken)argumentActual,
 	                            new MessageFlags(MessageFlags.Flag.DELETED | MessageFlags.Flag.SEEN));
 	                    return true;
     	            }
     	        });
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	
     	TestCallback callback = new TestCallback();
     	instance.processRequest(instance.createMessageFlagChangeRequest(messageToken, new MessageFlags(MessageFlags.Flag.DELETED), true).setRequestCallback(callback));
     	instance.shutdown(true);
     	
         assertTrue(callback.completed);
         assertTrue(!callback.failed);
         assertNull(callback.exception);
         
     	assertNotNull(eventMessageFlagsChanged);
     	assertEquals(MessageEvent.TYPE_FLAGS_CHANGED, eventMessageFlagsChanged.getType());
     	assertNotNull(eventMessageFlagsChanged.getMessageToken());
     	assertEquals(messageToken, eventMessageFlagsChanged.getMessageToken());
     	assertTrue(eventMessageFlagsChanged.getMessageFlags().isDeleted());
         hammock.verify();
     }
     
     public void testRequestMessageUndelete() {
         FakeMessageToken messageToken = new FakeMessageToken(1);
         
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_SET_ACTIVE_FOLDER_$_MESSAGETOKEN_BOOLEAN,
                 new Object[] { messageToken, Boolean.TRUE }).setReturnValue(null);
         hammock.setExpectation(MockIncomingMailClient.MTHD_UNDELETE_MESSAGE_$_MESSAGETOKEN,
                 new Object[] { messageToken }).setArgumentMatcher(0, new DefaultArgumentMatcher() {
                     public boolean areArgumentsEqual(Object argumentExpected, Object argumentActual) {
                         if(!super.areArgumentsEqual(argumentExpected, argumentActual)) { return false; }
                         clientListener.folderMessageFlagsChanged(
                                 (FakeMessageToken)argumentActual,
                                 new MessageFlags(MessageFlags.Flag.SEEN));
                         return true;
                     }
                 });
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	
     	TestCallback callback = new TestCallback();
     	instance.processRequest(instance.createMessageFlagChangeRequest(messageToken, new MessageFlags(MessageFlags.Flag.DELETED), false).setRequestCallback(callback));
     	instance.shutdown(true);
     	
         assertTrue(callback.completed);
         assertTrue(!callback.failed);
         assertNull(callback.exception);
         
     	assertNotNull(eventMessageFlagsChanged);
     	assertEquals(MessageEvent.TYPE_FLAGS_CHANGED, eventMessageFlagsChanged.getType());
     	assertNotNull(eventMessageFlagsChanged.getMessageToken());
     	assertEquals(messageToken, eventMessageFlagsChanged.getMessageToken());
     	assertTrue(!eventMessageFlagsChanged.getMessageFlags().isDeleted());
         hammock.verify();
     }
     
     public void testRequestBatch() {
     	TextPart part = new TextPart("plain", "", "", "", "", "", -1);
     	TextContent content = new TextContent(part, "Hello World");
     	Message testMessage = new Message(part);
     	testMessage.putContent(part, content);
     	FolderTreeItem[] folderArray = new FolderTreeItem[] { inboxFolder };
         FakeMessageToken messageToken = new FakeMessageToken(0);
     	FakeMessageToken messageToken1 = new FakeMessageToken(1);
     	
     	// Since we are testing for execution, not processing, these mock
     	// expectations don't care to populate any return data unless
     	// absolutely necessary.
         hammock.setExpectation(MockIncomingMailClient.MTHD_OPEN).setReturnValue(Boolean.TRUE);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_TREE_$_MAILPROGRESSHANDLER,
                 new Object[] { null }).ignoreArgument(0).setReturnValue(inboxFolder);
         hammock.setExpectation(MockIncomingMailClient.MTHD_REFRESH_FOLDER_STATUS_$_ARRAY_FOLDERTREEITEM_MAILPROGRESSHANDLER,
                 new Object[] { folderArray, null }).ignoreArgument(1);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_FOLDER_MESSAGES_$_MESSAGETOKEN_INT_FOLDERMESSAGECALLBACK_MAILPROGRESSHANDLER,
                 new Object[] { messageToken, new Integer(5), null, null })
                 .ignoreArgument(2).ignoreArgument(3);
         hammock.setExpectation(MockIncomingMailClient.MTHD_SET_ACTIVE_FOLDER_$_MESSAGETOKEN_BOOLEAN,
                 new Object[] { messageToken1, Boolean.TRUE }).setReturnValue(null);
         hammock.setExpectation(MockIncomingMailClient.MTHD_GET_MESSAGE_$_MESSAGETOKEN_BOOLEAN_MAILPROGRESSHANDLER,
                 new Object[] { messageToken1, Boolean.TRUE, null }).ignoreArgument(2)
                 .setReturnValue(testMessage);
         hammock.setExpectation(MockIncomingMailClient.MTHD_CLOSE);
     	
     	
     	// Do a whole batch of non-conflicting requests to make
     	// sure the queue is working correctly.
         instance.processRequest(instance.createFolderTreeRequest());
         instance.processRequest(instance.createFolderStatusRequest(folderArray));
         instance.processRequest(instance.createFolderMessagesRangeRequest(inboxFolder, messageToken, 5));
         instance.processRequest(instance.createMessageRequest(messageToken1, true));
     	instance.shutdown(true);
     	
     	// We know the requests work individually, so lets just
     	// make sure they all went through the system.
     	assertNotNull("requestFolderTree", eventFolderTreeUpdated);
     	assertNotNull("requestFolderStatus", eventFolderStatusChanged);
     	assertNotNull("requestFolderMessages", eventFolderMessagesAvailable);
     	assertNotNull("requestMessage", eventMessageAvailable);
         hammock.verify();
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
 	
 	private class TestCallback implements MailStoreRequestCallback {
 	    public boolean completed;
 	    public boolean failed;
 	    public Throwable exception;
 	    
         public void mailStoreRequestComplete(MailStoreRequest request) {
             this.completed = true;
         }
 
         public void mailStoreRequestFailed(MailStoreRequest request, Throwable exception, boolean isFinal) {
             this.failed = true;
             this.exception = exception;
         }
 	}
 }
