 package org.fabric3.tests.messaging;
 
import java.util.concurrent.atomic.AtomicInteger;

 import org.oasisopen.sca.annotation.Scope;
 
 import org.fabric3.api.annotation.Consumer;
 
 /**
  *
  */
 @Scope("COMPOSITE")
 public class TestConsumerImpl implements TestConsumer {
     private long start = -1;
    private AtomicInteger count = new AtomicInteger();
 
     @Consumer
     public void publish(Message message) {
         if (Message.Type.START == message.getType()) {
             start = System.currentTimeMillis();
            System.out.println(count.getAndIncrement());
         } else if (Message.Type.END == message.getType()) {
             System.out.println("Invoked: " + message.getSequence() + " [" + (System.currentTimeMillis() - start) + "]");
            count.set(0);
        }  else {
            System.out.println(count.getAndIncrement());
         }
 
     }
 
 }
