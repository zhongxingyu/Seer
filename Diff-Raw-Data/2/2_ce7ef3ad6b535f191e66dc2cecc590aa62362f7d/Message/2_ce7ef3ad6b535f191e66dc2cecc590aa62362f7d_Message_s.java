 /*******************************************************************************
  * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *    Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
  *    may be used to endorse or promote products derived from this software without
  *    specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package org.cyclades.client;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import org.cyclades.client.message.CompletableMessageProcessor;
 import org.cyclades.client.message.ConsumingWorker;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.api.RawMessageProducer;
 import org.cyclades.engine.nyxlet.templates.xstroma.target.ProducerTarget;
 import org.cyclades.engine.stroma.xstroma.XSTROMABrokerRequest;
 import org.json.JSONObject;
 
 public class Message {
     
     /**
      * Consumes into the specified CompletableMessageProcessor until its done method returns true or the maxWait
      * is reached.
      * 
      * This method blocks.
      * 
      * @param consumerClass The class of the desired consumer
      * @param consumerJSON  The JSONObject descriptor for the queue connections
      * @param cmp           The CompletableMessageProcessor to use
      * @param maxWait       The maximum number of milliseconds to wait for completion (a negative number waits forever for 
      *  completion)
      * @return true if the CompletableMessageProcessor completed successfully
      * @throws InterruptedException
      * @throws ExecutionException
      */
     public static Boolean consume (String consumerClass, JSONObject consumerJSON, CompletableMessageProcessor cmp, 
             long maxWait) throws InterruptedException, ExecutionException {
         ExecutorService executorService = null;
         try {
             executorService = Executors.newSingleThreadExecutor();
             Boolean returnBool = consume(executorService, consumerClass, consumerJSON, cmp, maxWait).get();
             return returnBool;
         } finally {
             executorService.shutdown();
         }
     }
     
     /**
      * Consumes into the specified CompletableMessageProcessor until its done method returns true or the maxWait
      * is reached.
      * 
      * This method returns immediately.
      * 
      * @param executorService   The ExecutorService to use for this consumer job
      * @param consumerClass     The class of the desired consumer
      * @param consumerJSON      The JSONObject descriptor for the queue connections
      * @param cmp               The CompletableMessageProcessor to use
      * @param maxWait           The maximum number of milliseconds to wait for completion (a negative number waits forever for 
      *  completion)
      * @return The Future object of this consumer job
      */
     public static Future<Boolean> consume (ExecutorService executorService, String consumerClass, JSONObject consumerJSON, 
             CompletableMessageProcessor cmp, long maxWait) {
         return executorService.submit(new ConsumingWorker(consumerClass, consumerJSON, cmp, maxWait));
     }
      
     /**
      * Send a XSTROMABrokerRequest message. This is simply a helper method, ProducerTarget can be used directly as an alternative.
      * 
      * @param producerClass     The class of the producer
      * @param producerJSON      The producer connection JSON descriptor
      * @param xstromaRequest    The message to send
     * @param @param replyTo           The replyto queue, null means do not reply, or that it already exists in the messageAttributes
      *  passed in.
      * @param messageAttributes Message attributes, this can be used for utilizing the same attributes for many messages without
      *  re creating the Map for each message, i.e. you can do this by creating a Map, adding the "replyto" attribute to that
      *  map and passing that in for each call to this method. A new Map will be created if this is null.
      * @throws Exception
      */
     public static void produce (String producerClass,JSONObject producerJSON, XSTROMABrokerRequest xstromaRequest, 
             String replyTo, Map<String, List<String>> messageAttributes) throws Exception {
         produce(producerClass, producerJSON, xstromaRequest.toXSTROMAMessage(), replyTo, messageAttributes);
     }
     
     /**
      * Send a XSTROMABrokerRequest message. This is simply a helper method, ProducerTarget can be used directly as an alternative.
      * 
      * @param pt                The ProducerTarget to use for sending the message
      * @param xstromaRequest    The message to send
      * @param replyTo           The replyto queue, null means do not reply, or that it already exists in the messageAttributes
      *  passed in.
      * @param messageAttributes Message attributes, this can be used for utilizing the same attributes for many messages without
      *  re creating the Map for each message, i.e. you can do this by creating a Map, adding the "replyto" attribute to that
      *  map and passing that in for each call to this method. A new Map will be created if this is null.
      * @throws Exception
      */
     public static void produce (ProducerTarget pt, XSTROMABrokerRequest xstromaRequest, String replyTo, 
             Map<String, List<String>> messageAttributes) throws Exception {
         produce(pt, xstromaRequest.toXSTROMAMessage(), replyTo, messageAttributes);
     }
      
     /**
      * Send a String message. This is simply a helper method, ProducerTarget can be used directly as an alternative.
      * 
      * @param producerClass The class of the producer
      * @param producerJSON  The producer connection JSON descriptor
      * @param message       The message to send
      * @param replyTo           The replyto queue, null means do not reply, or that it already exists in the messageAttributes
      *  passed in.
      * @param messageAttributes Message attributes, this can be used for utilizing the same attributes for many messages without
      *  re creating the Map for each message, i.e. you can do this by creating a Map, adding the "replyto" attribute to that
      *  map and passing that in for each call to this method. A new Map will be created if this is null.
      * @throws Exception
      */
     public static void produce (String producerClass, JSONObject producerJSON, String message, String replyTo, 
             Map<String, List<String>> messageAttributes) throws Exception {
         ProducerTarget pt = null;
         try {
             pt = new ProducerTarget(producerClass, producerJSON);
             produce(pt, message, replyTo, messageAttributes);
         } finally {
             try { pt.destroy(); } catch (Exception e) {};
         }
     }
     
     /**
      * Send a String message. This is simply a helper method, ProducerTarget can be used directly as an alternative.
      * 
      * @param pt                The ProducerTarget to use for sending the message
      * @param message           The message to send
      * @param replyTo           The replyto queue, null means do not reply, or that it already exists in the messageAttributes
      *  passed in.
      * @param messageAttributes Message attributes, this can be used for utilizing the same attributes for many messages without
      *  re creating the Map for each message, i.e. you can do this by creating a Map, adding the "replyto" attribute to that
      *  map and passing that in for each call to this method. A new Map will be created if this is null.
      * @throws Exception
      */
     public static void produce (ProducerTarget pt, String message, String replyTo, 
             Map<String, List<String>> messageAttributes) throws Exception {
         if (messageAttributes == null) messageAttributes = new HashMap<String, List<String>>();
         if (replyTo != null) messageAttributes.put("replyto", new ArrayList<String>(Arrays.asList(replyTo)));
         pt.getMessageProducer().sendMessage(message, messageAttributes);
     }
     
     /**
      * Send a byte[] message. This is simply a helper method, ProducerTarget can be used directly as an alternative.
      * 
      * @param producerClass     The class of the producer
      * @param producerJSON      The producer connection JSON descriptor
      * @param message           The message to send
      * @param replyTo           The replyto queue, null means do not reply, or that it already exists in the messageAttributes
      *  passed in.
      * @param messageAttributes Message attributes, this can be used for utilizing the same attributes for many messages without
      *  re creating the Map for each message, i.e. you can do this by creating a Map, adding the "replyto" attribute to that
      *  map and passing that in for each call to this method. A new Map will be created if this is null.
      * @throws Exception
      */
     public static void produce (String producerClass, JSONObject producerJSON, byte[] message, String replyTo, 
             Map<String, List<String>> messageAttributes) throws Exception {
         ProducerTarget pt = null;
         try {
             pt = new ProducerTarget(producerClass, producerJSON);
             produce(pt, message, replyTo, messageAttributes);
         } finally {
             try { pt.destroy(); } catch (Exception e) {};
         }
     }
     
     /**
      * Send a byte[] message. This is simply a helper method, ProducerTarget can be used directly as an alternative.
      * 
      * @param pt                The ProducerTarget to use for sending the message
      * @param message           The message to send
      * @param replyTo           The replyto queue, null means do not reply, or that it already exists in the messageAttributes
      *  passed in.
      * @param messageAttributes Message attributes, this can be used for utilizing the same attributes for many messages without
      *  re creating the Map for each message, i.e. you can do this by creating a Map, adding the "replyto" attribute to that
      *  map and passing that in for each call to this method. A new Map will be created if this is null.
      * @throws Exception
      */
     public static void produce (ProducerTarget pt, byte[] message, String replyTo, 
             Map<String, List<String>> messageAttributes) throws Exception {
         if (messageAttributes == null) messageAttributes = new HashMap<String, List<String>>();
         if (replyTo != null) messageAttributes.put("replyto", new ArrayList<String>(Arrays.asList(replyTo)));
         ((RawMessageProducer)pt.getMessageProducer()).sendMessage(message, messageAttributes);
     }
     
 }
