 
 package com.iinteractive.Bullpen;
 
 import org.zeromq.ZMQ;
 import java.util.logging.*;
 import java.util.UUID;
 
 public class Worker extends Core {
 
     private ZMQ.Socket coordinator;
     private ZMQ.Socket publisher;
 
     private Producer producer;
 
     public Worker ( String coordinator_addr,
                     String publisher_addr,
                     Producer _producer ) {
 
         initializeContext();
 
         logger.log(Level.INFO, "welcome to worker ...");
 
         initializeCoordinator( coordinator_addr );
         logger.log(Level.INFO, "coordinator connected to " + coordinator_addr);
 
         initializePublisher( publisher_addr );
         logger.log(Level.INFO, "publisher bound to " + publisher_addr);
 
         producer = _producer;
     }
 
     private String generateSubscriberKey () {
         return UUID.randomUUID().toString();
     }
 
     private void initializeCoordinator ( String address ) {
         coordinator = initializeSocket( ZMQ.REP, address );
     }
 
     private void initializePublisher ( String address ) {
         publisher = initializeSocket( ZMQ.PUB, address );
     }
 
     private void publishMessage ( String subscriber_key, String message ) {
         String pub_msg = subscriber_key + " " + message;
         logger.log(Level.INFO, "=> publishing message " + pub_msg);
         sendMessage( publisher, pub_msg );
     }
 
     public void run () {
 
         while (!Thread.currentThread().isInterrupted()) {
 
             String msg = receiveMessage( coordinator );
             logger.log(Level.INFO, "<= got value=(" + msg + ") on coodinator ...");
 
             String subscriber_key = generateSubscriberKey();
             logger.log(Level.INFO, "=> sending subscriber-key=(" + subscriber_key + ")");
 
             sendMessage( coordinator, subscriber_key );
 
             try {
                 producer.initialize( msg );
                 while ( producer.hasNext() ) {
                     publishMessage( subscriber_key, producer.next() );
                 }
             } catch ( Exception e ) {
                publishMessage(
                    subscriber_key,
                    producer.formatError( e )
                );
             } finally {
                 producer.reset();
             }
 
             logger.log(Level.INFO, "publishing completed, sending empty value to subscriber ...");
             sendMessage( publisher, (subscriber_key + " ") );
 
         }
 
         coordinator.close();
         publisher.close();
         context.term();
     }
 }
