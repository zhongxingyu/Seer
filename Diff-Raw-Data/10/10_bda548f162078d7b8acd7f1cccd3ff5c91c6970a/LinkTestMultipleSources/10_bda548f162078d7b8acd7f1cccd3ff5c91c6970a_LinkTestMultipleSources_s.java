 package net.dovemq.transport.link;
 
 import java.io.IOException;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.management.MalformedObjectNameException;
 
 import net.dovemq.transport.common.JMXProxyWrapper;
 import net.dovemq.transport.endpoint.CAMQPEndpointManager;
 import net.dovemq.transport.endpoint.CAMQPSourceInterface;
 import net.dovemq.transport.frame.CAMQPMessagePayload;
 import net.dovemq.transport.session.SessionCommand;
 
 public class LinkTestMultipleSources
 {
     private static final String source = "src";
     private static final String target = "target";
     private static String brokerContainerId ;
     private static int NUM_THREADS = 5;
     private static LinkCommandMBean mbeanProxy;
     
     private static final CountDownLatch startSignal = new CountDownLatch(1);
    private static final CountDownLatch doneSignal = new CountDownLatch(NUM_THREADS);
     
     private static class LinkSourceDriver implements Runnable
     {
         public LinkSourceDriver(int numMessagesToSend)
         {
             super();
             this.numMessagesToSend = numMessagesToSend;
         }
 
         private final int numMessagesToSend;
         
         @Override
         public void run()
         {
             try
             {
                 startSignal.await();
             }
             catch (InterruptedException e1)
             {
                 // TODO Auto-generated catch block
                 e1.printStackTrace();
             }
             String localSource = String.format("%s%d", source, Thread.currentThread().getId());
             String localTarget = String.format("%s%d", target, Thread.currentThread().getId());
             CAMQPSourceInterface sender = CAMQPEndpointManager.createSource(brokerContainerId, localSource, localTarget);
             mbeanProxy.attachSharedTarget(localSource,  localTarget);
             
             Random randomGenerator = new Random();
             for (int i = 0; i < numMessagesToSend; i++)
             {
                 CAMQPMessagePayload message = LinkTestUtils.createMessagePayload(randomGenerator);
                 sender.sendMessage(message);
             }
             doneSignal.countDown();
         }
     }
     
     public static void main(String[] args) throws InterruptedException, IOException, MalformedObjectNameException
     {
         /*
          * Read args
          */
         String publisherName = args[0];
         String brokerIp = args[1];
         String jmxPort = args[2];
         
         JMXProxyWrapper jmxWrapper = new JMXProxyWrapper(brokerIp, jmxPort);
         
         NUM_THREADS = Integer.parseInt(args[3]);
         int numMessagesToSend = Integer.parseInt(args[4]);
           
         brokerContainerId = String.format("broker@%s", brokerIp);
         CAMQPLinkManager.initialize(false, publisherName);
         
         SessionCommand localSessionCommand = new SessionCommand();
         localSessionCommand.sessionCreate(brokerContainerId);
         
         mbeanProxy = jmxWrapper.getLinkBean();
         
         ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
 
         int numMessagesExpected = numMessagesToSend * NUM_THREADS;
         
         LinkSourceDriver[] senders = new LinkSourceDriver[NUM_THREADS];
         for (int i = 0; i < NUM_THREADS; i++)
         {
             LinkSourceDriver sender = new LinkSourceDriver(numMessagesToSend);
             senders[i] = sender;
             executor.submit(sender);
         }
         
         startSignal.countDown();
         
         doneSignal.await();
         Thread.sleep(2000);
         
         while (true)
         {
             Thread.sleep(1000);
             long numMessagesReceivedAtRemote = mbeanProxy.getNumMessagesReceivedAtTargetReceiver();
             System.out.println(numMessagesReceivedAtRemote);
             if (numMessagesReceivedAtRemote == numMessagesExpected)
                 break;
         }
         
        System.out.println("Got all messages: sleeping for 1 min");
        Thread.sleep(60000);
         executor.shutdown();
         CAMQPLinkManager.shutdown();
         System.out.println("Shutdown linkManager");
         mbeanProxy.reset();
         jmxWrapper.cleanup();
         System.out.println("cleanup JMX");
     }
 }
