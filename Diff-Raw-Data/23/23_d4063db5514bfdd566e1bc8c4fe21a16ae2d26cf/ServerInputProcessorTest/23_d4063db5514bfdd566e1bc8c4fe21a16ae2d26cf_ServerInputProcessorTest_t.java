 package server;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import message.servertoclient.SMessageImpls.NormalAction;
 import message.servertoclient.SMessageImpls.NormalAction.ActionType;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import client.ClientInputProcessor;
 import client.MainListener;
 import client.MainListenerMock;
 
 import testutil.SocketMock;
 
 public class ServerInputProcessorTest {
     
     @Test
     public void testGetId() throws InterruptedException {
         ServerInputProcessor sip = new ServerInputProcessor();
         ByteArrayInputStream is = new ByteArrayInputStream(
                 "handle: james\ngetid/james".getBytes());
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         List<Thread> threads = sip.addClient(new SocketMock(is, os));
         for (Thread t : threads) {
             t.join();
         }
         Assert.assertTrue(os.toString().startsWith("claimed: james\nid: "));
     }
    @Test
    public void testNormalActionAddUser() throws InterruptedException {
        ServerInputProcessor sip = new ServerInputProcessor();
        ByteArrayInputStream is = new ByteArrayInputStream(
                "handle: james\ngetid/james\nconv/12/james/add: james".getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        List<Thread> threads = sip.addClient(new SocketMock(is, os));
        for (Thread t : threads) {
            t.join();
        }
        System.out.println(os.toString());
        //Assert.assertTrue(os.toString().startsWith("claimed: james\nid: "));
    }
     
     @Test
    public void testNormalActionAdsdUser() throws IOException {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         MainListener ml = new MainListenerMock(os);
         //ClientInputProcessor cip = new ClientInputProcessor(ml);
         //cip.visit(new NormalAction(12, "james", ActionType.ADD_USER, Arrays.asList("john", "james"), Arrays.asList("john", "james"), ""));
        //Assert.assertEquals(os.toString(), 
        //		"ml/makeConversationListener/12\ncl/addUsers/12/john,james\ncl/addUsers/12/john,james\n");
     }
    
 }
