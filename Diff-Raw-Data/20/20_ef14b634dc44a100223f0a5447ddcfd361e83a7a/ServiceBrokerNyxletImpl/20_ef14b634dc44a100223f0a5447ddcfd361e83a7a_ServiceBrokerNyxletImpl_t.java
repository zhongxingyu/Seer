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
 package org.cyclades.engine.nyxlet.templates.xstroma;
 
 import java.io.OutputStream;
 import org.json.JSONObject;
 import org.w3c.dom.Node;
 import org.cyclades.engine.ResponseCodeEnum;
 import org.cyclades.engine.exception.AuthException;
 import org.cyclades.engine.exception.CycladesException;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.api.RawMessageProducer;
 import org.cyclades.engine.nyxlet.templates.xstroma.message.api.StreamingMessageProducer;
 import org.cyclades.engine.nyxlet.templates.xstroma.target.ConsumerTarget;
 import org.cyclades.engine.nyxlet.templates.xstroma.target.ProducerTarget;
 import org.cyclades.engine.MetaTypeEnum;
 import org.cyclades.engine.stroma.STROMAResponseWriter;
 import org.cyclades.engine.stroma.xstroma.XSTROMAResponseWriter;
 import org.cyclades.engine.util.JSON;
 import org.cyclades.engine.util.MapHelper;
 import org.cyclades.engine.adapter.HttpServletRequestAdapter;
 import org.cyclades.engine.adapter.HttpServletResponseAdapter;
 import org.cyclades.xml.comparitor.XMLComparitor;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.List;
 import java.util.ArrayList;
 import org.cyclades.engine.util.GenericXMLObject;
 import org.cyclades.engine.NyxletSession;
 import com.google.common.io.ByteStreams;
 
 public class ServiceBrokerNyxletImpl extends XSTROMANyxlet {
 
     public ServiceBrokerNyxletImpl() throws Exception {
         super();
     }
 
     /**
      * For use by JUnit only
      *
      * @param dummy This can be any value
      * @throws Exception
      */
     public ServiceBrokerNyxletImpl(int dummy) throws Exception {
         setName("ServiceBrokerNyxlet-JUNIT");
     }
 
     @Override
     public byte[] process(NyxletSession nyxletSession) throws CycladesException {
         final String eLabel = "ServiceBrokerNyxlet.process: ";
         try {
             nyxletSession.setRawResponseRequested(false);
             nyxletSession.setResponseContentType(nyxletSession.getDataContentType());
             // Is this an actual message format sent to this service? (HTTP if we are at this point)
             // If so, extract and process via protocol agnostic message entry point which all MessageConsumers
             // will call. FYI: Consider this an HTTP MessageConsumer implementation....also as an alternate protocol
             // into this service via HTTP.
             String temp = nyxletSession.getParameter(XSTROMA_MESSAGE);
             if (temp != null) {
                 processXSTROMAMessagePayload(
                         nyxletSession.getOutputStream(),
                         (temp.isEmpty()) ? new String(ByteStreams.toByteArray(nyxletSession.getInputStream())) : temp
                 );
                 return null;
             }
             temp = nyxletSession.getParameter(TARGET);
             if (temp != null && !producerTargetMap.containsKey(temp)) throw new Exception("Target not found: " + temp);
             if (temp == null || producerTargetMap.get(temp).isLocal()) {
                 if (temp != null) {
                     ProducerTarget theTargetQueue = producerTargetMap.get(temp);
                     if (theTargetQueue != null && !theTargetQueue.auth(nyxletSession)) {
                         throw new AuthException(eLabel + "User authorization invalid ", ResponseCodeEnum.AUTHORIZATION_FAILED.getCode(), "User authorization invalid ", null);
                     }
                 }
                 processXSTROMARequest(nyxletSession);
             } else {
                 if (producerTargetMap.get(temp).getMessageProducer() instanceof RawMessageProducer) {
                     sendRawMessage(nyxletSession, temp);
                 } else {
                     sendXSTROMAMessage(nyxletSession, temp);
                 }
             }
             return null;
         } catch (Exception e) {
             logStackTrace(e);
             try {
                 nyxletSession.raiseOrchestrationFault(eLabel + e);
                 new STROMAResponseWriter(this.getName(), nyxletSession, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                         eLabel + e);
                 return null;
             } catch (Exception ex) {
                 throw new CycladesException(eLabel + ex);
             }
         }
     }
 
     private void sendRawMessage(NyxletSession nyxletSession, String target) throws Exception {
         final String eLabel = "ServiceBrokerNyxlet.sendRawMessage: ";
         try {
             String metaString = nyxletSession.getParameter(DATA_PARAMETER);
             byte[] incomingMessage  = (metaString != null && !metaString.isEmpty()) ?
                     metaString.getBytes() : (nyxletSession.getInputStream() != null) ? ByteStreams.toByteArray(nyxletSession.getInputStream()) : null;
             if (incomingMessage == null) {
                 throw new Exception("Error, parameter \"data\" not detected");
             }/* else if (nyxletSession.getActionString() != null) {
                 throw new Exception("Error, parameter \"action\" not allowed by this service.");
             }*/
             ProducerTarget producerTarget = validateAndGetProducerTarget(nyxletSession, target);
             Map<String, List<String>> requestParameters = nyxletSession.getParameterMap();
             if (producerTarget.forwardAuthData()) requestParameters.putAll(producerTarget.getAuthDataMap(nyxletSession));
             engageProducerTarget(nyxletSession, producerTarget, incomingMessage, requestParameters);
         } catch (Exception e) {
             logStackTrace(e);
             // Let's make sure these fields are set to null so we don't misrepresent this response
             // with cached data from above, as this is an upper layer error, specific to the
             // request broker.
             nyxletSession.setActionObject(null);
             nyxletSession.setTransactionDataObject(null);
             nyxletSession.raiseOrchestrationFault(eLabel + e);
             new STROMAResponseWriter(this.getName(), nyxletSession, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                     eLabel + e);
         }
     }
 
     private void engageProducerTarget (NyxletSession nyxletSession, ProducerTarget producerTarget, byte[] messagePayload, Map<String, List<String>> requestParameters) throws Exception {
         final String eLabel = "ServiceBrokerNyxlet.engageProducerTarget(Raw Message): ";
         try {
             byte[] messageResponse = ((RawMessageProducer)producerTarget.getMessageProducer()).sendMessage(messagePayload, requestParameters);
             if (messageResponse == null) {
                 new XSTROMAResponseWriter(this.getName(), nyxletSession, this).writeResponse("");
             } else {
                 nyxletSession.setResponseContentType("application/octet-stream");
                 nyxletSession.getOutputStream().write(messageResponse);
             }
         } catch (Exception e) {
            throw new Exception(eLabel + e);
         }
     }
 
     @SuppressWarnings("unchecked")
     private void sendXSTROMAMessage(NyxletSession nyxletSession, String target) throws Exception {
         final String eLabel = "ServiceBrokerNyxlet.sendXSTROMAMessage: ";
         try {
             nyxletSession.setResponseContentType(nyxletSession.getDataContentType());
             String metaString = nyxletSession.getParameter(DATA_PARAMETER);
             if (metaString == null && nyxletSession.getInputStream() != null) metaString = new String(ByteStreams.toByteArray(nyxletSession.getInputStream()));
             if (metaString == null || metaString.isEmpty()) {
                 throw new Exception("Error, parameter \"data\" not detected");
             }/* else if (nyxletSession.getActionString() != null) {
                 throw new Exception("Error, parameter \"action\" not allowed by this service.");
             }*/
             ProducerTarget producerTarget = validateAndGetProducerTarget(nyxletSession, target);
             StringBuilder messagePayload = new StringBuilder();
             Map<String, List<String>> requestParameters = nyxletSession.getParameterMap();
             if (!allowXSTROMAMessageTargets) requestParameters.remove(TARGET);
             if (producerTarget.forwardAuthData()) requestParameters.putAll(producerTarget.getAuthDataMap(nyxletSession));
             if (nyxletSession.getMetaTypeEnum().equals(MetaTypeEnum.JSON)) {
                 // Object at index 0 is the base-parameters (minus the data), object at index 1 is the data
                 requestParameters.remove(NyxletSession.DATA_PARAMETER);
                 messagePayload.append("{\"").append(BASE_PARAMETERS).append("\":");
                 messagePayload.append(MapHelper.parameterMapToJSON(requestParameters));
                 messagePayload.append(",\"").append(DATA_PARAMETER).append("\":");
                 messagePayload.append(metaString);
                 messagePayload.append("}");
             } else {
                 messagePayload.append("<x-stroma>");
                 messagePayload.append(metaString);
                 messagePayload.append("<").append(BASE_PARAMETERS).append(">");
                 requestParameters.remove(NyxletSession.DATA_PARAMETER);
                 messagePayload.append(MapHelper.parameterMapToXML(requestParameters, "parameters"));
                 messagePayload.append("</").append(BASE_PARAMETERS).append("></x-stroma>");
             }
             engageProducerTarget(nyxletSession, producerTarget, messagePayload.toString(), requestParameters);
         } catch (Exception e) {
             logStackTrace(e);
             // Let's make sure these fields are set to null so we don't misrepresent this response
             // with cached data from above, as this is an upper layer error, specific to the
             // request broker.
             nyxletSession.setActionObject(null);
             nyxletSession.setTransactionDataObject(null);
             nyxletSession.raiseOrchestrationFault(eLabel + e);
             new STROMAResponseWriter(this.getName(), nyxletSession, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                     eLabel + e);
         }
     }
 
     private ProducerTarget validateAndGetProducerTarget(NyxletSession nyxletSession, String target) throws Exception {
         ProducerTarget producerTarget = producerTargetMap.get(target);
         if (producerTarget == null) throw new Exception("Target not found: " + target);
         if (!producerTarget.auth(nyxletSession)) {
             throw new AuthException("Target authorization invalid " + target, ResponseCodeEnum.AUTHORIZATION_FAILED.getCode(), "Target authorization invalid " + target, target);
         }
         return producerTarget;
     }
 
     private void engageProducerTarget(NyxletSession nyxletSession, ProducerTarget producerTarget, String messagePayload, Map<String, List<String>> requestParameters) throws Exception {
         final String eLabel = "ServiceBrokerNyxlet.engageProducerTarget: ";
         try {
             if (producerTarget.getMessageProducer() instanceof StreamingMessageProducer) {
                 // For larger payloads...like the "HTTPMessageProducers"
                 ((StreamingMessageProducer)producerTarget.getMessageProducer()).sendMessage(messagePayload, requestParameters, nyxletSession.getOutputStream());
             } else {
                 String messageResponse = producerTarget.getMessageProducer().sendMessage(messagePayload, requestParameters);
                 if (messageResponse == null) {
                     new XSTROMAResponseWriter(this.getName(), nyxletSession, this).writeResponse("");
                 } else {
                     nyxletSession.getOutputStream().write(messageResponse.getBytes());
                 }
             }
         } catch (Exception e) {
            throw new Exception(eLabel + e);
         }
     }
 
     /**
      * Process a X-STROMA message targeted for this Engine/ServiceRequestBroker. This method is called by MessageConsumers (serviceConsumerTargets)
      * to process incoming messages and should be called by any message consumers within the onMessage method
      * implementation.
      *
      * @param out           The OutputStream to write the response
      * @param messageString The String representation of the incoming request (STROMA)
      * @return              The NyxletSession object created by calling this method (may be ignored)
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     public NyxletSession processXSTROMAMessagePayload(OutputStream out, String messageString) throws Exception {
         final String eLabel = "ServiceBrokerNyxlet.processXSTROMAMessagePayload: ";
         NyxletSession nyxletSession = null;
         try {
             HttpServletRequestAdapter request = null;
             Map<String, List<String>> requestParameters;
             HttpServletResponseAdapter response = new HttpServletResponseAdapter(out);
             if (messageString.charAt(0) == '<') {
                 GenericXMLObject xmlObject = new GenericXMLObject(messageString);
                 requestParameters = MapHelper.parameterMapFromMetaObject(((Node)XMLComparitor.getMatchingChildNodes(xmlObject.getRootElement(), BASE_PARAMETERS).firstElement()).getChildNodes());
                 requestParameters.put(NyxletSession.DATA_PARAMETER, new ArrayList<String>(Arrays.asList("")));
                 request = new HttpServletRequestAdapter(new HashMap(), MapHelper.arrayParameterMapFromParameterMap(requestParameters), new HashMap(), null);
                 nyxletSession = new NyxletSession(request, response, request.getInputStream(), response.getOutputStream());
                 nyxletSession.setDataObject(((Node)XMLComparitor.getMatchingChildNodes(xmlObject.getRootElement(), DATA_PARAMETER).firstElement()));
             } else {
                 JSONObject jsonObject = new JSONObject(messageString);
                 requestParameters = MapHelper.parameterMapFromMetaObject(jsonObject.getJSONArray(BASE_PARAMETERS));
                 requestParameters.put(NyxletSession.DATA_PARAMETER, new ArrayList<String>(Arrays.asList("")));
                 request = new HttpServletRequestAdapter(new HashMap(), MapHelper.arrayParameterMapFromParameterMap(requestParameters), new HashMap(), null);
                 nyxletSession = new NyxletSession(request, response, request.getInputStream(), response.getOutputStream());
                 nyxletSession.setDataObject(jsonObject.getJSONObject(DATA_PARAMETER));
             }
             ProducerTarget producerTarget = null;
             if (requestParameters.containsKey(TARGET)) {
                 if (!allowXSTROMAMessageTargets) throw new Exception("This broker has not been configured to allow targets embedded in X-STROMA messages");
                 producerTarget = validateAndGetProducerTarget(nyxletSession, requestParameters.get(TARGET).get(0));
             }
             if (producerTarget == null || producerTarget.isLocal()) {
                 processXSTROMARequest(nyxletSession);
             } else {
                 engageProducerTarget(nyxletSession, producerTarget, messageString, requestParameters);
             }
         } catch (Exception e) {
             // Log warn to avoid excessive log noise, as this can be a frequent user error
             logWarn(eLabel + e + " Possibly a malformed X-STROMA request:[" + messageString + "]");
             // If the NyxletSession is null, populate it just enough to do its thing. We want to avoid anyone calling
             // this method to process exceptions unless there is no other way.
             if (nyxletSession == null) {
                 nyxletSession = new NyxletSession(new HttpServletRequestAdapter(new HashMap(), new HashMap(), new HashMap(), null),
                         new HttpServletResponseAdapter(out), null, out);
             }
             nyxletSession.raiseOrchestrationFault(eLabel + e);
             new STROMAResponseWriter(this.getName(), nyxletSession, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                     eLabel + e);
         }
         return nyxletSession;
     }
 
     @SuppressWarnings("unchecked")
     public NyxletSession processXSTROMAMessagePayloads(OutputStream out, List<String> messageList, MetaTypeEnum metaTypeEnum) throws Exception {
         final String eLabel = "ServiceBrokerNyxlet.processXSTROMAMessagePayloads: ";
         NyxletSession nyxletSession = null;
         try {
             Map<String, List<String>>requestParameters;
             HttpServletRequestAdapter request = new HttpServletRequestAdapter(new HashMap(), null, new HashMap(), null);
             HttpServletResponseAdapter response = new HttpServletResponseAdapter(out);
             nyxletSession = new NyxletSession(request, response, request.getInputStream(), response.getOutputStream());
             int currentMessageCount = 0;
             int messageTotal = messageList.size() - 1;
             for (String messageString : messageList) {
                 if (metaTypeEnum.equals(MetaTypeEnum.XML)) {
                     GenericXMLObject xmlObject = new GenericXMLObject(messageString);
                     requestParameters = MapHelper.parameterMapFromMetaObject(((Node)XMLComparitor.getMatchingChildNodes(xmlObject.getRootElement(), BASE_PARAMETERS).firstElement()).getChildNodes());
                     requestParameters.put(NyxletSession.DATA_PARAMETER, new ArrayList<String>(Arrays.asList("")));
                     requestParameters.put(MERGE_COUNT, new ArrayList<String>(Arrays.asList(String.valueOf(currentMessageCount++))));
                     requestParameters.put(MERGE_TOTAL, new ArrayList<String>(Arrays.asList(String.valueOf(messageTotal))));
                     request.setParameterMap(MapHelper.arrayParameterMapFromParameterMap(requestParameters));
                     nyxletSession.setDataObject(((Node)XMLComparitor.getMatchingChildNodes(xmlObject.getRootElement(), DATA_PARAMETER).firstElement()));
                 } else {
                     JSONObject jsonObject = new JSONObject(messageString);
                     requestParameters = MapHelper.parameterMapFromMetaObject(jsonObject.getJSONArray(BASE_PARAMETERS));
                     requestParameters.put(NyxletSession.DATA_PARAMETER, new ArrayList<String>(Arrays.asList("")));
                     requestParameters.put(MERGE_COUNT, new ArrayList<String>(Arrays.asList(String.valueOf(currentMessageCount++))));
                     requestParameters.put(MERGE_TOTAL, new ArrayList<String>(Arrays.asList(String.valueOf(messageTotal))));
                     request.setParameterMap(MapHelper.arrayParameterMapFromParameterMap(requestParameters));
                     nyxletSession.setDataObject(jsonObject.getJSONObject(DATA_PARAMETER));
                 }
                 ProducerTarget producerTarget = null;
                 // XXX - Not sure if we should allow producer targets here, most likely not, but it may work just fine if we use a reserved parameter to
                 // trigger the correct behavior...i.e. merge-count and merge-total (base 0) TBD, let's give it a shot.
                 if (requestParameters.containsKey(TARGET)) {
                     if (!allowXSTROMAMessageTargets) throw new Exception("This broker has not been configured to allow targets embedded in X-STROMA messages");
                     producerTarget = validateAndGetProducerTarget(nyxletSession, requestParameters.get(TARGET).get(0));
                 }
                 if (producerTarget == null || producerTarget.isLocal()) {
                     processXSTROMARequest(nyxletSession);
                 } else {
                     engageProducerTarget(nyxletSession, producerTarget, messageString, requestParameters);
                 }
             }
         } catch (Exception e) {
             // Log debug to avoid excessive log noise, as this can be a frequent user error
             logDebug(eLabel + e);
             // If the NyxletSession is null, populate it just enough to do its thing. We want to avoid anyone calling
             // this method to process exceptions unless there is no other way.
             if (nyxletSession == null) {
                 nyxletSession = new NyxletSession(new HttpServletRequestAdapter(new HashMap(), new HashMap(), new HashMap(), null),
                         new HttpServletResponseAdapter(out), null, out);
             }
             nyxletSession.raiseOrchestrationFault(eLabel + e);
             new STROMAResponseWriter(this.getName(), nyxletSession, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                     eLabel + e);
         }
         return nyxletSession;
     }
 
     @Override
     public void init() throws CycladesException {
         final String eLabel = "ServiceBrokerNyxlet.init: ";
         try {
             super.init();
             // Initialize service producers
             producerTargetMap = ProducerTarget.loadTargets(JSON.resolveLinkedJSONObjects(getExternalProperties().getProperty(PRODUCER_TARGETS, "[]")), 
                     JSON.resolveLinkedJSONObjects(getExternalProperties().getProperty(PRODUCER_TARGET_ALIASES, "[]")), this);
             for (Map.Entry<String, ProducerTarget> entry : producerTargetMap.entrySet()) logInfo("Service Producer Target Loaded: " + entry.getKey().toString() + " " + entry.getValue());
             // Initialize service consumers
             consumerTargetList = ConsumerTarget.loadTargets(JSON.resolveLinkedJSONObjects(getExternalProperties().getProperty(CONSUMER_TARGETS, "[]")), this);
             for (ConsumerTarget consumerTarget : consumerTargetList) logInfo("Service Consumer Target Loaded: " + consumerTarget.toString());
             // Are we going to allow targets embedded in X-STROMA messages
             String propertyVal = getExternalProperties().getProperty("allowXSTROMAMessageTargets");
             if (propertyVal != null) allowXSTROMAMessageTargets = propertyVal.equalsIgnoreCase("true");
         } catch (Exception e) {
             logStackTrace(e);
             throw new CycladesException(eLabel + e);
         }
     }
 
     @Override
     public void destroy() throws CycladesException {
         final String eLabel = "ServiceBrokerNyxlet.destroy: ";
         for (Map.Entry<String, ProducerTarget> entry : producerTargetMap.entrySet()) {
             logInfo(eLabel + "Destroying producer target: " + entry.getKey() + " " + entry.getValue());
             try {entry.getValue().destroy();} catch (Exception e) {logError(eLabel + e);}
         }
         for (ConsumerTarget consumerTarget : consumerTargetList) {
             logInfo(eLabel + "Destroying consumer target: " + consumerTarget);
             try {consumerTarget.destroy();} catch (Exception e) {logError(eLabel + e);}
         }
         super.destroy();
     }
 
     @Override
     public boolean isHealthy () throws CycladesException {
         boolean failed = false;
         for (Map.Entry<String, ProducerTarget> entry : producerTargetMap.entrySet()) {
             try {
                 if (!entry.getValue().isHealthy()) failed = true;
             } catch (Exception e) {
                 logStackTrace(e);
                 failed = true;
             }
         }
         for (ConsumerTarget consumerTarget : consumerTargetList) {
             try {
                 if (!consumerTarget.isHealthy()) failed = true;
             } catch (Exception e) {
                 logStackTrace(e);
                 failed = true;
             }
         }
         return (!failed);
     }
 
     protected Map<String, ProducerTarget> producerTargetMap = new HashMap<String, ProducerTarget>();
     protected List<ConsumerTarget> consumerTargetList = new ArrayList<ConsumerTarget>();
     private boolean allowXSTROMAMessageTargets = false;
     // Configuration keys
     public final static String PRODUCER_TARGETS         = "serviceProducerTargets";
     public final static String PRODUCER_TARGET_ALIASES  = "serviceProducerTargetAliases";
     public final static String CONSUMER_TARGETS         = "serviceConsumerTargets";
     // Attribute names
     public final static String TARGET           = "target";
     public final static String XSTROMA_MESSAGE  = "x-stroma-message";
     public final static String BASE_PARAMETERS  = "parameters";
     public final static String DATA_PARAMETER   = "data";
 
 }
