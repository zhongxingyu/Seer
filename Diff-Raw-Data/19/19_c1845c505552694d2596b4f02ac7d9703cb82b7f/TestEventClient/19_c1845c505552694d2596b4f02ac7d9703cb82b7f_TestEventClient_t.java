 package org.fabric3.tests.messaging;
 
 import java.util.UUID;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.oasisopen.sca.annotation.Scope;
 
 import org.fabric3.api.annotation.Producer;
 import org.fabric3.api.annotation.Resource;
 import org.fabric3.api.annotation.management.Management;
 import org.fabric3.api.annotation.management.ManagementOperation;
 
 /**
  *
  */
 @Management
 @Scope("COMPOSITE")
 public class TestEventClient {
 
     private String uuid = UUID.randomUUID().toString();
     private AtomicLong sequence = new AtomicLong();
     public int clients = 20;
     private int number = 1000;
 
     @Resource
     ExecutorService executorService;
 
     @Producer
     protected TestConsumer producer;
 
     private AtomicBoolean stop = new AtomicBoolean(true);
 
     @ManagementOperation
     public void setClients(int clients) {
         this.clients = clients;
     }
 
     @ManagementOperation
     public int getClients() {
         return clients;
     }
 
     @ManagementOperation
     public void setNumber(int number) {
         this.number = number;
     }
 
     @ManagementOperation
     public int getNumber() {
         return number;
     }
 
     @ManagementOperation
     public String start() {
         if (!stop.getAndSet(false)) {
             return uuid;
         }
         executorService.execute(new Multiplexer());
         return uuid;
     }
 
     @ManagementOperation
     public void stop() {
         stop.set(true);
     }
 
     private abstract class AbstractRunner implements Runnable {
         private CountDownLatch latch;
 
         private AbstractRunner(CountDownLatch latch) {
             this.latch = latch;
         }
 
         public void run() {
             long start = System.currentTimeMillis();
             invoke(Message.Type.START);
             int i = 0;
             while (!stop.get() && i < number) {
                 try {
                     i++;
                     invoke(Message.Type.CONTINUE);
                 } catch (RejectedExecutionException e) {
                     e.printStackTrace();
                 }
             }
             invoke(Message.Type.END);
            invoke(Message.Type.END);
            invoke(Message.Type.END);
             latch.countDown();
             System.out.println("Runner time: " + (System.currentTimeMillis() - start));
         }
 
         protected abstract void invoke(Message.Type type);
 
     }
 
     private class ProducerRunner extends AbstractRunner {
 
         private ProducerRunner(CountDownLatch latch) {
             super(latch);
         }
 
         @Override
         protected void invoke(Message.Type type) {
             long number = sequence.incrementAndGet();
             producer.publish(new Message(uuid, number, type));
            System.out.println("end sent----------------");
         }
     }
 
 
     private class Multiplexer implements Runnable {
         public void run() {
             CountDownLatch latch = new CountDownLatch(clients);
             long start = System.currentTimeMillis();
             for (int i = 0; i <= clients - 1; i++) {
                 executorService.execute(new ProducerRunner(latch));
             }
             try {
                 latch.await();
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
             }
             System.out.println("Total time:" + (System.currentTimeMillis() - start));
 
         }
     }
 
 }
