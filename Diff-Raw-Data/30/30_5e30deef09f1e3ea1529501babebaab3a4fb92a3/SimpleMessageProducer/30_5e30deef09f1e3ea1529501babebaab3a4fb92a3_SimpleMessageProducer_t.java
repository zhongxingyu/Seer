 
 package com.iinteractive.Bullpen.test;
 
 import java.util.HashMap;
 import java.util.logging.*;
 import com.iinteractive.Bullpen.*;
 
 public class SimpleMessageProducer implements Producer {
 
     private int num_messages = 100;
     private int message_counter = 0;
     private Logger logger = Logger.getAnonymousLogger();
 
     public SimpleMessageProducer () {}
     public SimpleMessageProducer ( int _num_messages ) {
         this();
         num_messages = _num_messages;
     }
 
     public void configure ( HashMap config ) {
         if ( config.containsKey("num_messages") ) {
             num_messages = ((Long) config.get("num_messages")).intValue();
         }
     }
 
     public void initialize ( String request ) {
         logger.log(Level.INFO, "got request=(" + request + ")");
     }
 
     public void reset () {
         message_counter = 0;
     }
 
     public boolean hasNext () {
         return message_counter < num_messages;
     }
 
     public String next () {
         message_counter++;
         return "[" + message_counter + "]";
     }

    public String formatError ( Exception e ) {
        return e.toString();
    }
 }
