 package clear4j.msg;
 
 import clear4j.msg.queue.Message;
 import clear4j.msg.queue.MessageListener;
 import clear4j.msg.queue.Receiver;
 import junit.framework.Assert;
 import org.junit.Test;
 
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * User: alexb
  * Date: 24/05/12
  * Time: 09:23
  */
 public class MessengerTest {
 
     private static final Logger LOG = Logger.getLogger(MessengerTest.class.getName());
 
 //    @Test   //TODO not thread safe
 //    public void testRemoteReceiver() throws Exception {
 //
 //    	String remoteQueue = "remote queue";
 //        String remoteMessage = "remote test";
 //
 //        final String[] received = new String[1];
 //
 //        Messenger.Receiver<String> receiver = Messenger.register(new Receiver<String>(){
 //            @Override
 //            public void onMessage(Message<String> message) {
 //                received[0] = message.getPayload();
 //            }
 //        }).on("localhost", 9876).to(remoteQueue);
 //
 //        Thread.sleep(3000);   //have to wait for registration //todo
 //
 //    	Messenger.send(remoteMessage).on("localhost", 9876).to(remoteQueue);
 //
 //    	Messenger.waitFor(receiver.getQueue(), remoteMessage);
 //
 //        Assert.assertEquals(remoteMessage, received[0]);
 //
 //    	Messenger.unregister(receiver);  //TODO remote unregistering when it turns out it's required..
 //    }
 //
 //    @Test
 //    public void testRemoteAdapter() throws Exception {
 //
 //        String localQueue = "local queue";
 //        String localMessage = "local test";
 //        String remoteQueue = "remote queue";
 //        String remoteMessage = "remote test";
 //
 //        //Message<String> remote = Messenger.send(remoteMessage).on("localhost", 9876).toAndWait(remoteQueue); //TODO fix this!
 //        //Assert.assertEquals(remoteMessage, remote.getPayload());
 //        Assert.fail("needs fixing");
 //
 //        Message<String> local = Messenger.send(localMessage).toAndWait(localQueue);
 //        Assert.assertEquals(localMessage, local.getPayload());
 //
 //    }
 
 
     @Test
     public void testMessaging() throws Exception {
 
         int NUM = 1000;
 
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, "starting testMessaging test");
         }
 
         Random random = new Random();
 
         final List<String> sentMessages = new CopyOnWriteArrayList<String>();
         final List<String> receivedMessages = new CopyOnWriteArrayList<String>();
 
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, "creating receiver");
         }
 
         Receiver<String> receiver = Messenger.register("test", new MessageListener<String>() {
             private int count;
 
             @Override
             public void onMessage(Message<String> message) {
                 receivedMessages.add(message.getPayload());
                 if (LOG.isLoggable(Level.INFO)) {
                     LOG.log(Level.INFO, String.format("messages received: %s", ++count));
                 }
             }
         });
        
//        Messenger.unregister(receiver);
 
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, "starting loop");
         }
 
         for (int i = 0; i < NUM; i++) {
 
             String sent = "test-" + random.nextInt(1000);
 
             if (LOG.isLoggable(Level.INFO)) {
                 LOG.log(Level.INFO, String.format("sending [%s]", sent));
             }
 
             sentMessages.add(sent);
             Messenger.send("test", sent);
         }
 
         if (LOG.isLoggable(Level.INFO)) {
             LOG.log(Level.INFO, "waiting for all messages");
         }
 
         Messenger.wait("test");
 
         for (String sent : sentMessages) {
             Assert.assertTrue(String.format("%s not in received messages!", sent), receivedMessages.contains(sent));
         }
 
         Messenger.unregister(receiver);
     }
 
 }
