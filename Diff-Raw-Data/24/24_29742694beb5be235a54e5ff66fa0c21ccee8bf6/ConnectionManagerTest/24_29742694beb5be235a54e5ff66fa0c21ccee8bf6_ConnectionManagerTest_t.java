 package ch9k.network;
 
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import ch9k.eventpool.TypeEventFilter;
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.Socket;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class ConnectionManagerTest {
     private TestListener testListener;
     private ConnectionManager connectionManager;
 
     private class TestListener implements EventListener {
         public int received = 0;
 
         @Override
         public void handleEvent(Event ev) {
             received++;
         }
     }
 
     @Before
     public void setUp() throws IOException {
         EventPool pool = new EventPool();
         testListener = new TestListener();
         pool.addListener(testListener, new TypeEventFilter(TestNetworkEvent.class));
 
         connectionManager = new ConnectionManager(pool);
     }
 
     @Test
     public void testSendEvent() throws IOException, InterruptedException {
         DirectResponseServer echoServer = new DirectResponseServer();
         echoServer.start();
 
         // number of events to send
         int n = 3;
         for (int i = 0; i < n; i++) {
             connectionManager.sendEvent(new TestNetworkEvent());
         }
 
         // we should sleep +- 10 ms per event, to make sure they're send
         Thread.sleep(150*n);
         assertEquals(n, testListener.received);
 
         echoServer.stop();
         Thread.sleep(50); // wait so server has shutdown for sure
     }
 
     /**
      * @throws IOException
      * @throws InterruptedException
      */
     @Test
     public void testDisconnect() throws IOException, InterruptedException {
         connectionManager.readyForIncomingConnections();
         Thread.sleep(50);
 
         Connection conn = new Connection(InetAddress.getLocalHost(), new EventPool());
         assertTrue(conn.hasConnection());
 
         // close it up
         connectionManager.disconnect();
         Thread.sleep(200);
         assertFalse(conn.hasConnection());
 
         // we shouldn't be able to connect any more
         boolean exceptionThrown = false;
         try {
             conn = new Connection(InetAddress.getLocalHost(), new EventPool());
         } catch(ConnectException ex) {
             exceptionThrown = true;
         }
         assertTrue(exceptionThrown);
     }
     
     @Test(expected=ConnectException.class)
     public void testShouldRaiseConnectException() throws IOException {
         Socket s = new Socket(InetAddress.getLocalHost(), Connection.DEFAULT_PORT);
     }
     
     @Test
     public void testShouldNotRaiseConnectException() throws IOException, InterruptedException {
         connectionManager.readyForIncomingConnections();
         // creating a serversocket takes some time, lets wait a bit
         Thread.sleep(50);
         Socket s = new Socket(InetAddress.getLocalHost(), Connection.DEFAULT_PORT);
         s.close();
     }
 }
