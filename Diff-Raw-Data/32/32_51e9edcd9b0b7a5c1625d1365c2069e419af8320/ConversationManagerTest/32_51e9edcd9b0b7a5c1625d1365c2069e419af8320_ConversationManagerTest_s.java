 package ch9k.chat;
 
 import ch9k.chat.events.ConversationEvent;
 import ch9k.chat.events.NewConversationEvent;
 import ch9k.core.ChatApplication;
 import ch9k.eventpool.EventPool;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ConversationManagerTest {
 
     private ConversationManager localConversationManager;
 
     @Before
    public void setUp() {
         ChatApplication.getInstance().performTestLogin();
         localConversationManager = new ConversationManager();
     }
 
     /**
      * Test of startConversation method, of class ConversationManager.
      */
     @Test
     public void testStartConversation() {
        Contact contact = new Contact("Javache", null);
         Conversation conversation = new Conversation(contact, true);
         assertEquals(conversation, localConversationManager.startConversation(contact, false));
         assertEquals(conversation, localConversationManager.getConversation(contact));
     }
 
     /**
      * Test of closeConversation method, of class ConversationManager.
      */
     @Test
     public void testCloseConversation() {
        Contact contact = new Contact("Javache", null);
         Conversation conversation = localConversationManager.startConversation(contact, true);
         assertEquals(conversation, localConversationManager.getConversation(contact));
         localConversationManager.closeConversation(contact);
         assertNull(localConversationManager.getConversation(contact));
     }
 
     /**
      * test of clear method, of class ConversationManager.
      */
     @Test
     public void testClear() {
        Contact contact = new Contact("Javache", null);
         Conversation conversation = localConversationManager.startConversation(contact, true);
         assertEquals(conversation, localConversationManager.getConversation(contact));
         localConversationManager.clear();
         assertNull(localConversationManager.getConversation(contact));
     }
 
 
 
     /**
      * Test of getConversation method, of class ConversationManager.
      */
     @Test
     public void testGetConversation() {
        Contact contact = new Contact("Javache", null);
         Conversation conversation = new Conversation(contact, true);
         assertEquals(conversation, localConversationManager.startConversation(contact, false));
         assertEquals(conversation, localConversationManager.getConversation(contact));
     }
 
      /**
      * Test of handleEvent method, of class Conversation.
      * Tests only the local side
      */
     @Test
     public void testLocalHandleEvent() throws UnknownHostException, IOException, InterruptedException {
         EventPool localPool = EventPool.getAppPool();
         // wait for apppool to start up
         Thread.sleep(100);
 
         Contact remoteContact = new Contact("JPanneel", InetAddress.getLocalHost());
         ChatApplication.getInstance().getAccount().getContactList().addContact(remoteContact);
 
         ConversationEvent localCreateEvent = new NewConversationEvent(remoteContact);
 
         localPool.raiseEvent(localCreateEvent);
         // wait while the event gets transmitted
        Thread.sleep(800);
 
         Conversation startedConversation = localConversationManager.getConversation(remoteContact);
         assertNotNull(startedConversation);
 
         // this will raise an CloseConversationEvent
         startedConversation.close();
         Thread.sleep(200);
 
         assertNull(localConversationManager.getConversation(remoteContact));
         
     }
 }
