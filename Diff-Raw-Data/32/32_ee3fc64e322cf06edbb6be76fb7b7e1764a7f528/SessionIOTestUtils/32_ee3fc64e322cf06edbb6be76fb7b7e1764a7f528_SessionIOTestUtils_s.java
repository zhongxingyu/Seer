 package net.dovemq.transport.session;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Random;
 import java.util.UUID;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
import org.jboss.netty.buffer.ChannelBuffer;

 import net.dovemq.transport.common.CAMQPTestTask;
 import net.dovemq.transport.connection.CAMQPConnectionFactory;
 import net.dovemq.transport.connection.CAMQPConnectionManager;
 import net.dovemq.transport.frame.CAMQPMessagePayload;
 import net.dovemq.transport.link.CAMQPLinkSenderInterface;
 import net.dovemq.transport.protocol.CAMQPEncoder;
 import net.dovemq.transport.protocol.data.CAMQPControlAttach;
 import net.dovemq.transport.protocol.data.CAMQPControlTransfer;
 
 class FileHeader implements Serializable
 {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     public String getFileName()
     {
         return fileName;
     }
     public long getFileSize()
     {
         return fileSize;
     }
     public FileHeader(String fileName, long fileSize)
     {
         super();
         this.fileName = fileName;
         this.fileSize = fileSize;
     }
     private final String fileName;
     private final long fileSize;
 }
 
 class MockLinkSender implements CAMQPLinkSenderInterface
 {
     @Override
     public void sendMessage(String deliveryTag, CAMQPMessagePayload message)
     {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void messageSent(CAMQPControlTransfer transferFrame)
     {
         // TODO Auto-generated method stub
         
     }
 }
 
 class SessionCreator extends CAMQPTestTask implements Runnable
 {
     public SessionCreator(String brokerContainerId, SessionCommand localSessionCommand, CountDownLatch startSignal, CountDownLatch doneSignal)
     {
         super(startSignal, doneSignal);
         this.brokerContainerId = brokerContainerId;
         this.localSessionCommand = localSessionCommand;
     }
     
     @Override
     public void run()
     {
         waitForReady();
      
         localSessionCommand.sessionCreate(brokerContainerId);
         System.out.println("created session in thread id: " + Thread.currentThread().getId());        
         done();
     }   
     
     private final String brokerContainerId;
     private final SessionCommand localSessionCommand;
 }
 
 class SessionSender extends CAMQPTestTask implements Runnable
 {
     public SessionSender(CAMQPSession session, CountDownLatch startSignal, CountDownLatch doneSignal)
     {
         super(startSignal, doneSignal);
         this.session = session;
     }
     
     @Override
     public void run()
     {
         waitForReady();
         
         try
         {
             String sourceName = System.getenv("DOVEMQ_TEST_DIR") + "/testfile.tar";
             String targetName = String.format("%s/%s-%d.txt", System.getenv("DOVEMQ_TEST_DIR"), "foo2", Thread.currentThread().getId());
             //String targetName = System.getenv("DOVEMQ_TEST_DIR") + "/foo2.txt";
             SessionIOTestUtils.transmitFile(session, sourceName, targetName);
         }
         catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         done();
     }
     private final CAMQPSession session;
 }
 
 public class SessionIOTestUtils
 {   
     static void createSessions(int numThreads, String brokerContainerId, SessionCommand sessionCommand) throws InterruptedException
     {
         ExecutorService executor = Executors.newFixedThreadPool(numThreads);
         CountDownLatch startSignal = new CountDownLatch(1);
         CountDownLatch doneSignal = new CountDownLatch(numThreads);        
         
         for (int i = 0; i < numThreads; i++)
         {
             SessionCreator sessionCreator = new SessionCreator(brokerContainerId, sessionCommand, startSignal, doneSignal);
             executor.submit(sessionCreator);
         }
         
         startSignal.countDown();
         doneSignal.await();
         Thread.sleep(2000);
         executor.shutdown();
     }
     
     static void sendTransferFrames(int numThreads, String brokerContainerId, SessionCommand sessionCommand) throws InterruptedException
     {
         ExecutorService executor = Executors.newFixedThreadPool(numThreads);
         CountDownLatch startSignal = new CountDownLatch(1);
         CountDownLatch doneSignal = new CountDownLatch(numThreads);        
         
         Collection<Integer> attachedChannels = sessionCommand.getChannelId(brokerContainerId);
         
         for (int channelId : attachedChannels)
         {
             CAMQPSession session = CAMQPSessionManager.getSession(brokerContainerId, channelId);
             SessionSender sessionSender = new SessionSender(session, startSignal, doneSignal);
             executor.submit(sessionSender);
         }
         
         startSignal.countDown();
         doneSignal.await();
         Thread.sleep(2000);
         executor.shutdown();
     }
     
     static void closeSessions(int numThreads, String brokerContainerId, SessionCommand sessionCommand) throws InterruptedException
     {
         Collection<Integer> attachedChannels = sessionCommand.getChannelId(brokerContainerId);
         
         for (int channelId : attachedChannels)
         {
             sessionCommand.sessionClose(brokerContainerId, channelId);
             System.out.println("closed session id: " + channelId);
             Thread.sleep(2000);
         }
     }
     
     static byte[] marshalFileHeader(String source, String target) throws IOException
     {
         ObjectOutputStream oos = null;
         try
         {
             File fileHandle = new File(source);
             FileHeader fileHeader = new FileHeader(target, fileHandle.length());
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             oos = new ObjectOutputStream(bos);
             oos.writeObject(fileHeader);
             return bos.toByteArray();
         }
         finally
         {
             if (oos != null)
                 oos.close();
         }
     }
     
     static FileHeader unmarshalFileHeader(byte[] buf) throws IOException, ClassNotFoundException
     {
         ByteArrayInputStream bis = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bis);
         FileHeader fileHeader = (FileHeader) ois.readObject();
         ois.close();
         return fileHeader;
     }
     
     protected static void transmitFile(CAMQPSession session, String source, String target) throws InterruptedException, IOException
     {
         int bufsizes[] = new int[] {64, 128, 256, 512, 1024, 2048};
         BufferedInputStream inputStream =
             new BufferedInputStream(new FileInputStream(source));
         Random randomGenerator = new Random();
         boolean firstTime = true;
         long deliveryId = 0;
         long linkHandle = 1;
         
         ChannelBuffer attachedBuffer = createAttachedFrame(linkHandle);
         session.sendLinkControlFrame(attachedBuffer);
         
         MockLinkSender linkSender = new MockLinkSender();
         while (inputStream.available() > 0)
         {
             int bufsizeIndex = 0;
             byte[] inbuf = null;
             int bytesRead = 0;            
             if (firstTime)
             {           
                 firstTime = false;
                 inbuf = marshalFileHeader(source, target);
                 bytesRead = inbuf.length;
             }
             else
             {
                 bufsizeIndex = randomGenerator.nextInt(6);
                 inbuf = new byte[bufsizes[bufsizeIndex]];
                 bytesRead = inputStream.read(inbuf);
             }
             
             if (bytesRead < inbuf.length)
                 inbuf = Arrays.copyOf(inbuf, bytesRead);
             
             deliveryId = session.getNextDeliveryId();
             CAMQPControlTransfer transferFrame = createTransferFrame(linkHandle, deliveryId);
             session.sendTransfer(transferFrame, new CAMQPMessagePayload(inbuf), linkSender);
             
             int randomInt = randomGenerator.nextInt(20);
             Thread.sleep(randomInt);
         }
         System.out.println("Delivery ID of last message: " + deliveryId + " source: " + source + " target: " + target);
         inputStream.close();
         Thread.sleep(5000);
     }
     
     private static CAMQPControlTransfer createTransferFrame(long linkHandle, long deliveryId)
     {
         CAMQPControlTransfer transfer = new CAMQPControlTransfer();
         transfer.setDeliveryId(deliveryId);
         transfer.setDeliveryTag(UUID.randomUUID().toString().getBytes());
         transfer.setHandle(linkHandle);
         transfer.setMessageFormat(1L);
         transfer.setMore(false);
         return transfer;
     }
     
     private static ChannelBuffer createAttachedFrame(long linkHandle)
     {
         CAMQPControlAttach attachControl = new CAMQPControlAttach();
         attachControl.setHandle(linkHandle);
         attachControl.setName("TestLink");
         attachControl.setMaxMessageSize(BigInteger.valueOf(16384L));
 
         CAMQPEncoder encoder = CAMQPEncoder.createCAMQPEncoder();
         CAMQPControlAttach.encode(encoder, attachControl);
         return encoder.getEncodedBuffer();
     }
     
     public static void cleanup()
     {
         CAMQPSessionManager.shutdown();
         CAMQPConnectionManager.shutdown();
         CAMQPConnectionFactory.shutdown();
     }
 }
