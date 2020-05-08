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
 package org.logicprobe.LogicMail.model;
 
 import java.util.Hashtable;
 
 import org.logicprobe.LogicMail.mail.AbstractMailStore;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 
 import j2meunit.framework.Test;
 import j2meunit.framework.TestCase;
 import j2meunit.framework.TestMethod;
 import j2meunit.framework.TestSuite;
 
 public class AccountNodeTest extends TestCase {
     private AccountNode instance = null;
     private TestMailStore mailStore = null;
     private AccountNodeEvent accountNodeEvent = null;
     private Hashtable mailboxStatusChangedEvents = new Hashtable();
     private MailboxNodeListener mailboxNodeListener = new MailboxNodeListener() {
 		public void mailboxStatusChanged(MailboxNodeEvent e) {
 			mailboxStatusChangedEvents.put(e.getSource(), e);
 		}
     };
     
     /** Creates a new instance of AccountNodeTest */
     public AccountNodeTest() {
     }
     
     public AccountNodeTest(String testName, TestMethod testMethod) {
         super(testName, testMethod);
     }
 
     public void setUp() {
     	mailStore = new TestMailStore();
     	mailStore.rootFolder = new FolderTreeItem("", "", "");
     	mailStore.rootFolder.addChild(new FolderTreeItem("INBOX", "INBOX", "."));
    	mailStore.rootFolder.children()[0].addChild(new FolderTreeItem("One", "INBOX.One", "."));
    	mailStore.rootFolder.children()[0].addChild(new FolderTreeItem("Two", "INBOX.Two", "."));
     	
         instance = new AccountNode(mailStore, false);
     	instance.addAccountNodeListener(new AccountNodeListener() {
     		public void accountStatusChanged(AccountNodeEvent e) {
     			accountNodeEvent = e;
     		}
         });
     }
     
     public void tearDown() {
         instance = null;
         mailStore = null;
         accountNodeEvent = null;
     }
     
     public void testRefreshMailboxes() {
     	
     	// Test simple building of the initial tree
     	instance.refreshMailboxes();
     	assertNotNull("accountNodeEvent", accountNodeEvent);
     	assertEquals(AccountNodeEvent.TYPE_MAILBOX_TREE, accountNodeEvent.getType());
     	MailboxNode rootMailbox = instance.getRootMailbox();
     	assertNotNull("rootMailbox", rootMailbox);
     	
     	MailboxNode[] childMailboxes = rootMailbox.getMailboxes();
     	assertNotNull("childMailboxes", childMailboxes);
     	assertEquals(1, childMailboxes.length);
     	assertNotNull("childMailboxes[0]", childMailboxes[0]);
     	assertEquals("INBOX", childMailboxes[0].toString());
     	MailboxNode inboxNode = childMailboxes[0];
     	
     	childMailboxes = childMailboxes[0].getMailboxes();
     	assertNotNull("childMailboxes2", childMailboxes);
     	assertEquals(2, childMailboxes.length);
     	assertNotNull("childMailboxes2[0]", childMailboxes[0]);
    	assertEquals("One", childMailboxes[0].toString());
     	MailboxNode oneNode = childMailboxes[0];
     	assertNotNull("childMailboxes2[1]", childMailboxes[1]);
    	assertEquals("Two", childMailboxes[1].toString());
     	MailboxNode twoNode = childMailboxes[1];
     	
     	// Add another child node
    	mailStore.rootFolder.children()[0].addChild(new FolderTreeItem("Three", "INBOX.Three", "."));
     	
     	// Now make sure another refresh will return the same
     	// nodes for the mailboxes that have not changed.
     	accountNodeEvent = null;
     	instance.refreshMailboxes();
     	assertNotNull("accountNodeEvent", accountNodeEvent);
     	assertEquals(AccountNodeEvent.TYPE_MAILBOX_TREE, accountNodeEvent.getType());
     	rootMailbox = instance.getRootMailbox();
     	assertNotNull("rootMailbox", rootMailbox);
     	
     	childMailboxes = rootMailbox.getMailboxes();
     	assertNotNull("childMailboxes", childMailboxes);
     	assertEquals(1, childMailboxes.length);
     	assertNotNull("childMailboxes[0]", childMailboxes[0]);
     	assertEquals(inboxNode, childMailboxes[0]);
     	
     	childMailboxes = childMailboxes[0].getMailboxes();
     	assertNotNull("childMailboxes2", childMailboxes);
     	assertEquals(3, childMailboxes.length);
     	assertNotNull("childMailboxes2[0]", childMailboxes[0]);
     	assertEquals(oneNode, childMailboxes[0]);
     	assertNotNull("childMailboxes2[1]", childMailboxes[1]);
     	assertEquals(twoNode, childMailboxes[1]);
     	assertNotNull("childMailboxes2[2]", childMailboxes[2]);
    	assertEquals("Three", childMailboxes[2].toString());
     }
     
     public void testRefreshMailboxStatus() {
     	// Make sure we have mailboxes to check
     	instance.refreshMailboxes();
     	assertNotNull("accountNodeEvent", accountNodeEvent);
     	assertEquals(AccountNodeEvent.TYPE_MAILBOX_TREE, accountNodeEvent.getType());
     	MailboxNode rootMailbox = instance.getRootMailbox();
     	assertNotNull("rootMailbox", rootMailbox);
     	
     	// We'll assume the mailbox tree is valid, since we already
     	// have a test to verify that.  So, let's register some listeners...
     	MailboxNode[] childMailboxes = rootMailbox.getMailboxes();
     	MailboxNode inboxNode = childMailboxes[0];
     	childMailboxes[0].addMailboxNodeListener(mailboxNodeListener);
     	childMailboxes = childMailboxes[0].getMailboxes();
     	childMailboxes[0].addMailboxNodeListener(mailboxNodeListener);
     	MailboxNode oneNode = childMailboxes[0];
     	childMailboxes[1].addMailboxNodeListener(mailboxNodeListener);
     	MailboxNode twoNode = childMailboxes[1];
 
     	// Now we'll create a mirror of the current FolderTreeItem
     	// structure with updated status counts
     	mailStore.statusUpdatedRootFolder = new FolderTreeItem("", "", "");
     	mailStore.statusUpdatedRootFolder.addChild(new FolderTreeItem("INBOX", "INBOX", "."));
    	mailStore.statusUpdatedRootFolder.children()[0].addChild(new FolderTreeItem("One", "INBOX.One", "."));
    	mailStore.statusUpdatedRootFolder.children()[0].addChild(new FolderTreeItem("Two", "INBOX.Two", "."));
     	mailStore.statusUpdatedRootFolder.children()[0].setUnseenCount(5);
     	mailStore.statusUpdatedRootFolder.children()[0].setMsgCount(10);
     	mailStore.statusUpdatedRootFolder.children()[0].children()[0].setUnseenCount(6);
     	mailStore.statusUpdatedRootFolder.children()[0].children()[0].setMsgCount(11);
     	mailStore.statusUpdatedRootFolder.children()[0].children()[1].setUnseenCount(7);
     	mailStore.statusUpdatedRootFolder.children()[0].children()[1].setMsgCount(12);
     	
     	// Refresh status
     	instance.refreshMailboxStatus();
     	
     	// Check for the necessary events and changes
     	assertTrue(mailboxStatusChangedEvents.containsKey(inboxNode));
     	assertEquals(5, inboxNode.getUnseenMessageCount());
     	//assertEquals(10, inboxNode.getMessageCount());
 
     	assertTrue(mailboxStatusChangedEvents.containsKey(oneNode));
     	assertEquals(6, oneNode.getUnseenMessageCount());
     	//assertEquals(11, oneNode.getMessageCount());
     	
     	assertTrue(mailboxStatusChangedEvents.containsKey(twoNode));
     	assertEquals(7, twoNode.getUnseenMessageCount());
     	//assertEquals(12, twoNode.getMessageCount());
     	
     	// Note: getMessageCount() assertions are commented out because
     	// they are now based on the actual MailboxNode contents.  When
     	// this test was written, they were based on the IMAP folder
     	// status results.
     }
     
     public Test suite() {
         TestSuite suite = new TestSuite("AccountNode");
 
         suite.addTest(new AccountNodeTest("refreshMailboxes", new TestMethod()
         { public void run(TestCase tc) {((AccountNodeTest)tc).testRefreshMailboxes(); } }));
         suite.addTest(new AccountNodeTest("refreshMailboxStatus", new TestMethod()
         { public void run(TestCase tc) {((AccountNodeTest)tc).testRefreshMailboxStatus(); } }));
 
         return suite;
     }
     
     private class TestMailStore extends AbstractMailStore {
     	public FolderTreeItem rootFolder = null;
     	public FolderTreeItem statusUpdatedRootFolder = null;
     	public boolean folderTreeRequested = false;
     	public boolean folderStatusRequested = false;
     	public FolderTreeItem folderStatusRequestedItem = null;
     	
 		public boolean hasFlags() {
 			return false;
 		}
 
 		public boolean hasFolders() {
 			return true;
 		}
 
 		public boolean hasMessageParts() {
 			return false;
 		}
 
 		public boolean hasAppend() {
 			return true;
 		}
 		
 		public boolean hasCopy() {
 			return false;
 		}
 		
 		public boolean hasUndelete() {
 			return false;
 		}
 
 		public boolean isLocal() {
 			return false;
 		}
 
 		public void requestFolderTree() {
 			folderTreeRequested = true;
 			fireFolderTreeUpdated(rootFolder);
 		}
 		
 		public void requestFolderStatus(FolderTreeItem[] folders) {
 			folderStatusRequested = true;
 			folderStatusRequestedItem = folders[0];
 			fireFolderStatusChanged(statusUpdatedRootFolder);
 		}
 		
 		public void requestMessage(MessageToken messageToken) { }
 		public void requestMessageParts(MessageToken messageToken, MimeMessagePart[] messageParts) { }
 		public void requestMessageAnswered(MessageToken messageToken, MessageFlags messageFlags) { }
 		public void requestMessageDelete(MessageToken messageToken, MessageFlags messageFlags) { }
 		public void requestMessageUndelete(MessageToken messageToken, MessageFlags messageFlags) { }
 		public void shutdown(boolean wait) { }
 		public void requestFolderMessagesRange(FolderTreeItem folder, int firstIndex, int lastIndex) { }
 		public void requestFolderMessagesRecent(FolderTreeItem folder) { }
 		public void requestFolderMessagesSet(FolderTreeItem folder, int[] indices) { }
 		public void requestMessageAppend(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags) { }
 		public void requestMessageCopy(MessageToken messageToken, FolderTreeItem destinationFolder) { }
     }
 }
