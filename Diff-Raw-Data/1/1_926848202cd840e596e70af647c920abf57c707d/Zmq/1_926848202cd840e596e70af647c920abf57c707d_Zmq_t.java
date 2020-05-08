 /*
     Copyright (c) 2007-2009 FastMQ Inc.
 
     This file is part of 0MQ.
 
     0MQ is free software; you can redistribute it and/or modify it under
     the terms of the Lesser GNU General Public License as published by
     the Free Software Foundation; either version 3 of the License, or
     (at your option) any later version.
 
     0MQ is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     Lesser GNU General Public License for more details.
 
     You should have received a copy of the Lesser GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package org.zmq;
 
 public class Zmq
 {
     static
     {
         System.loadLibrary ("jzmq");
     }
 
     /**  Specifies that the object will be visible only in this session. */
     public static final int SCOPE_LOCAL = 1;
 
     /**  Specifies that the object will be visible only within this process. */
     public static final int SCOPE_PROCESS = 2;
 
     /**  Specifies that the object will be visible all over the network. */
     public static final int SCOPE_GLOBAL = 3;
      
     /**  Specifies data notifications to be received. */
     public static final int MESSAGE_DATA = 1;
 
     /**  Specifies gap notifications to be received. */
     public static final int MESSAGE_GAP = 2;
 
     /**  Specifies data distribution style. */
     public static final int STYLE_DATA_DISTRIBUTION = 1;	
 
     /**  Specifies load balancing style. */
     public static final int STYLE_LOAD_BALANCING = 2;
 
     /**  Specifies that there's no high watermark for the queue. It can consume
      *   as much memory as needed. 
      */
     public static final int NO_LIMIT = -1;
 
     /**  Specified that disk-offload feature should not be used. All the queue
      *   data are to be held in the memory. 
      */
     public static final int NO_SWAP = 0;
 
     /**  
      *  Initalises Zmq object. 
      * 
      *  @param host is a name or an IP address of the box where zmq_server is 
      *  	  running. 
      */
     public Zmq (String host)
     {
         construct (host);
     }
      
     /**  Specifies which notifications should be received. */
     public native void mask (int notifications);
 
     
     /**
      * Create an exchange. 
      * 
      * @param name the name of the exchange to be created.
      * @param scope the scope of the exchange. 
      * @param location if the scope is global, you have to supply the name
      *        or the IP address of the network interface to be used by 
      *        the exchange. Optionally you can specify the port to use 
      *        this way "eth0:5555".
      * @return the exchange id.
      */
     public native int createExchange (String name, int scope,
         String location, int style);
 
     /**
      * Create a queue.
      * 
      * @param name the name of the queue to be created.
      * @param scope the scope of the queue.
      * @param location if the scope is global, you have to supply the name
      *        or the IP address of the network interface to be used by 
      *        the queue. Optionally you can specify the port to use 
      *        this way "eth0:5555".
      * @return the queue id.
      */    
     public native int createQueue (String name, int scope, String location,
         long hwm, long lwm, long swap);
 
     /**
      * Bind the exchange to the queue.
      *   
      * @param exchangeName the exchange name.
      * @param queueName the queue name.
      */
     public native void bind (String exchangeName, String queueName,
         String exchangeOptions, String queueOptions);
 
     /**
      * Send a binary message to the specified exchange.
      * 
      * @param exchange identifies the exchange to be sent the binary data.
      * @param message binary message to be sent to the exchange.
      * @param block if it is set to true and there are no queues bound to the 
      * 	  exchange, execution will be blocked until a binding is created. 
      * @return true if message was successfully enqueued, false if it was not 
      * 	  send because fo pipe limits.
      */    
     public native boolean send (int exchange, byte [] message, boolean block);
 
     /**  
      *   Ad-hoc structure used to return multiple values from the
      *   'receive' method.
      */
     public static class InboundData
     {
         /** 
          *  ID of the queue the message was received from. In case of
          *  non-blocking receive, value of 0 indicates that no message was
          *  received.
          */
         public int queue;
 
         /**  
 	   *   Either MESSAGE_DATA in case it is an actual message or
          *   a notification type (like MESSAGE_GAP).
          */
         public int type;
 
         /**  Actual message payload. */
         public byte [] message;
     }
 
     /**
      * Receive the next message.
      * 
      * @param block By default (when the block parameter is set to true) 
      *	  if no message is immediately available, this method waits for 
      *	  the next message to arrive. If block is false, the method 
      *        returns immediately even if there is no message available.	
      * @return class containing the binary message retrieved from the queue.
      */
     public native InboundData receive (boolean block);
 
     /**  Initialises 0MQ infrastructure. */
     protected native void construct (String host);
 
     /**  Deallocates resources associated with the object. */
     protected native void finalize ();
 
     /**  Getter method for context. */
     protected long getContext ()
     {
         return context;
     }
     
     /**  The context.*/	
     private long context;
 }
