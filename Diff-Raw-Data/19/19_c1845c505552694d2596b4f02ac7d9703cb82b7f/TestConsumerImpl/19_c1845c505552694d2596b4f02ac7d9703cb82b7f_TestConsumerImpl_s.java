 package org.fabric3.tests.messaging;
 
 import org.oasisopen.sca.annotation.Scope;
 
 import org.fabric3.api.annotation.Consumer;
 
 /**
  *
  */
 @Scope("COMPOSITE")
 public class TestConsumerImpl implements TestConsumer {
     private long start = -1;
 
     @Consumer
     public void publish(Message message) {
         if (Message.Type.START == message.getType()) {
             start = System.currentTimeMillis();
         } else if (Message.Type.END == message.getType()) {
             System.out.println("Invoked: " + message.getSequence() + " [" + (System.currentTimeMillis() - start) + "]");
         }
 
     }
 
 }
