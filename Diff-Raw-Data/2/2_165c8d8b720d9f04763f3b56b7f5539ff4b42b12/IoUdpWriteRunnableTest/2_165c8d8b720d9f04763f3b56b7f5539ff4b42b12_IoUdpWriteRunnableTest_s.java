 package ping.pong.net.connection.io;
 
 import java.net.InetSocketAddress;
 import ping.pong.net.connection.DisconnectState;
 import java.net.SocketException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import java.net.DatagramSocket;
 import ping.pong.net.connection.RunnableEventListener;
 import ping.pong.net.connection.config.ConnectionConfiguration;
 import ping.pong.net.connection.messaging.Envelope;
 import ping.pong.net.connection.messaging.MessageProcessor;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import ping.pong.net.connection.config.ConnectionConfigFactory;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author mfullen
  */
 public class IoUdpWriteRunnableTest
 {
     private static final Logger logger = LoggerFactory.getLogger(IoUdpWriteRunnableTest.class);
 
     public IoUdpWriteRunnableTest()
     {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception
     {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception
     {
     }
 
     @Test
     public void testSendByte() throws SocketException, InterruptedException
     {
        ConnectionConfiguration config = ConnectionConfigFactory.createPPNConfig("localhost", 9007, 9009, false);
         final String message = "hello world";
         DatagramSocket udpSocket = new DatagramSocket(config.getUdpPort());
         ByteMessageProcessorImpl messageProcessorImpl = new ByteMessageProcessorImpl();
         IoUdpReadRunnable<byte[]> ioUdpReadRunnable = new IoUdpReadRunnable<byte[]>(messageProcessorImpl, new RunnableEventListener()
         {
             @Override
             public void onRunnableClosed(DisconnectState disconnectState)
             {
                 logger.debug("Close called {}", disconnectState);
             }
         }, udpSocket);
         Thread thread = new Thread(ioUdpReadRunnable);
         thread.start();
 
 
         DatagramSocket clientUdpSocket = new DatagramSocket();
 
         IoUdpWriteRunnable<byte[]> ioUdpWriteRunnable = new IoUdpWriteRunnable<byte[]>(new InetSocketAddress(config.getIpAddress(), config.getUdpPort()), new RunnableEventListener()
         {
             @Override
             public void onRunnableClosed(DisconnectState disconnectState)
             {
                 logger.debug("Close called {}", disconnectState);
             }
         }, clientUdpSocket);
 
         ioUdpWriteRunnable.enqueueMessage(message.getBytes());
         Thread writethread = new Thread(ioUdpWriteRunnable);
         writethread.start();
 
         writethread.join(20);
         thread.join(50);
 
         assertEquals(messageProcessorImpl.myMessage, message);
         ioUdpReadRunnable.close();
         ioUdpWriteRunnable.close();
     }
 
     @Test
     public void testSendObject() throws SocketException, InterruptedException
     {
         ConnectionConfiguration config = ConnectionConfigFactory.createPPNConfig("localhost", 9007, 9009, false);
         final String message = "hello world2";
         DatagramSocket udpSocket = new DatagramSocket(config.getUdpPort());
         StringMessageProcessor messageProcessorImpl = new StringMessageProcessor();
         IoUdpReadRunnable<String> ioUdpReadRunnable = new IoUdpReadRunnable<String>(messageProcessorImpl, new RunnableEventListener()
         {
             @Override
             public void onRunnableClosed(DisconnectState disconnectState)
             {
                 logger.debug("Close called {}", disconnectState);
             }
         }, udpSocket);
         Thread thread = new Thread(ioUdpReadRunnable);
         thread.start();
 
 
         DatagramSocket clientUdpSocket = new DatagramSocket();
 
         IoUdpWriteRunnable<String> ioUdpWriteRunnable = new IoUdpWriteRunnable<String>(new InetSocketAddress(config.getIpAddress(), config.getUdpPort()), new RunnableEventListener()
         {
             @Override
             public void onRunnableClosed(DisconnectState disconnectState)
             {
                 logger.debug("Close called {}", disconnectState);
             }
         }, clientUdpSocket);
 
         ioUdpWriteRunnable.enqueueMessage(message);
         Thread writethread = new Thread(ioUdpWriteRunnable);
         writethread.start();
 
         writethread.join(50);
         thread.join(100);
 
         assertEquals(messageProcessorImpl.myMessage, message);
         ioUdpReadRunnable.close();
         ioUdpWriteRunnable.close();
     }
 
     /**
      * Test of close method, of class IoUdpWriteRunnable.
      */
     @Test
     public void testClose()
     {
     }
 
     /**
      * Test of isRunning method, of class IoUdpWriteRunnable.
      */
     @Test
     public void testIsRunning()
     {
     }
 
     /**
      * Test of enqueueMessage method, of class IoUdpWriteRunnable.
      */
     @Test
     public void testEnqueueMessage()
     {
     }
 
     /**
      * Test of run method, of class IoUdpWriteRunnable.
      */
     @Test
     public void testRun()
     {
     }
 
     class ByteMessageProcessorImpl implements MessageProcessor<byte[]>
     {
         public ByteMessageProcessorImpl()
         {
         }
         public String myMessage = null;
 
         @Override
         public void enqueueReceivedMessage(byte[] byteMessage)
         {
             // logger.info("byte received: {}", byteMessage);
             String string = new String(byteMessage);
             logger.debug("Message Received {}", string);
             myMessage = string;
         }
 
         @Override
         public void enqueueMessageToWrite(Envelope message)
         {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 
     private class StringMessageProcessor implements MessageProcessor<String>
     {
         public StringMessageProcessor()
         {
         }
         public String myMessage = null;
 
         @Override
         public void enqueueReceivedMessage(String message)
         {
             // logger.info("byte received: {}", byteMessage);
             String string = message;
             logger.debug("Message Received {}", string);
             myMessage = string;
         }
 
         @Override
         public void enqueueMessageToWrite(Envelope<String> message)
         {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 }
