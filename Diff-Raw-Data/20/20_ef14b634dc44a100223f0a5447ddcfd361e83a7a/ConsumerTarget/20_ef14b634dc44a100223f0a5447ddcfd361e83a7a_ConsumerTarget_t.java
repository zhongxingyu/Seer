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
 package org.cyclades.engine.nyxlet.templates.xstroma.target;
 
 import org.json.JSONObject;
 import java.util.List;
 import java.util.ArrayList;
 import org.cyclades.engine.nyxlet.templates.xstroma.ServiceBrokerNyxletImpl;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageConsumer;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.api.MessageProcessor;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.impl.RawMessageProcessor;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.impl.ResponseProcessor;
 import org.cyclades.engine.util.MapHelper;
 
 public class ConsumerTarget {
 
    @SuppressWarnings("unchecked")
     public ConsumerTarget (String theClass, JSONObject initializationData, JSONObject messageProcessorData, JSONObject responseProcessorData, ServiceBrokerNyxletImpl service) throws Exception {
         final String eLabel = "ConsumerTarget.ConsumerTarget: ";
         try {
             this.theClass = theClass;
             messageConsumer = (MessageConsumer)service.getClass().getClassLoader().loadClass(theClass).newInstance();
             messageConsumer.init(MapHelper.mapFromMetaObject(initializationData),
                     (messageProcessorData == null) ? null : new RawMessageProcessor(messageProcessorData),
                     (responseProcessorData == null) ? null : new ResponseProcessor(responseProcessorData), service);
         } catch (Exception e) {
             throw new Exception(eLabel + e);
         }
     }
     
     /**
      * This constructor is for external use of a ConsumerTarget...i.e. client software. Only the
      * MessageProcessor will be utilized for this call.
      * 
      * Clients will need to use the ServiceBrokerNyxletImpl implementation of choice as a dependency for this to
      * build...i.e. the "servicebroker" Nyxlet would be used in order to reuse the Consumer targets of the existing Nyxlet.
      * 
      * @param theClass The String representation of the class to load as the consumer implementation
      * @param initializationData The JSONObject that will be used to initialize the consumer (see "target_init_data" field of
      *  one of the consumer targets in ../WEB-INF/targets/consumers/...)
      * @param messageProcessor The message processor implementation for the consumer to use
      * @throws Exception
      */
    @SuppressWarnings("unchecked")
     public ConsumerTarget (String theClass, JSONObject initializationData, MessageProcessor messageProcessor) throws Exception {
         final String eLabel = "ConsumerTarget.ConsumerTarget: ";
         try {
             this.theClass = theClass;
             messageConsumer = (MessageConsumer)this.getClass().getClassLoader().loadClass(theClass).newInstance();
             messageConsumer.init(MapHelper.mapFromMetaObject(initializationData), messageProcessor, null, null);
         } catch (Exception e) {
             throw new Exception(eLabel + e);
         }
     }
 
     /**
      * Load the service consumer targets from list of JSONObjects (each JSONObject is a service consumer target)
      * 
      * Each JSONObject entry will look like the following in JSON:
      *
      * {"class":"com.foo.blah","target_init_data":{...},"raw_message_processor":{...},"response_processor":{...}}
      *
      * @param consumerJSONObjectTargets JSONObject list of targets
      * @param service The service that will be consuming
      * @return the List of ConsumerTargets created
      * @throws Exception
      */
     public static List<ConsumerTarget> loadTargets (List<JSONObject> consumerJSONObjectTargets, ServiceBrokerNyxletImpl service) throws Exception {
         final String eLabel = "ConsumerTarget.loadTargets: ";
         List<ConsumerTarget> targetList = null;
         try {
             targetList = new ArrayList<ConsumerTarget>();
             for (JSONObject target : consumerJSONObjectTargets) {
                 targetList.add(new ConsumerTarget(target.getString(CLASS), target.getJSONObject(TARGET_INITIALIZATION_DATA),
                         (target.has(RAW_MESSAGE_PROCESSOR_DATA)) ? target.getJSONObject(RAW_MESSAGE_PROCESSOR_DATA) : null,
                         (target.has(RESPONSE_PROCESSOR_DATA)) ? target.getJSONObject(RESPONSE_PROCESSOR_DATA) : null, service));
             }
             return targetList;
         } catch (Exception e) {
             if (targetList != null) {
                 for (ConsumerTarget consumerTarget : targetList) {
                     try { consumerTarget.destroy(); } catch (Exception ex) {}
                 }
             }
             throw new Exception(eLabel + e);
         }
     }
 
     public void destroy () throws Exception {
         final String eLabel = "ConsumerTarget.destroy: ";
         try {
             messageConsumer.destroy();
         } catch (Exception e) {
             throw new Exception(eLabel + e);
         }
     }
 
     public String toString () {
         return theClass;
     }
 
     public boolean isHealthy () throws Exception {
         return messageConsumer.isHealthy();
     }
 
     private final String theClass;
     private MessageConsumer messageConsumer;
     private static final String CLASS                       = "class";
     private static final String TARGET_INITIALIZATION_DATA  = "target_init_data";
     private static final String RAW_MESSAGE_PROCESSOR_DATA  = "raw_message_processor";
     private static final String RESPONSE_PROCESSOR_DATA     = "response_processor";
 
 }
