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
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 import javax.servlet.http.HttpServletResponse;
 import org.cyclades.engine.MetaTypeEnum;
 import org.cyclades.engine.NyxletSession;
 import org.cyclades.engine.ResponseCodeEnum;
 import org.cyclades.engine.api.Nyxlet;
 import org.cyclades.engine.exception.CycladesException;
 import org.cyclades.engine.nyxlet.NyxletRepository;
 import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
 import org.cyclades.engine.stroma.STROMAResponseWriter;
 import org.cyclades.engine.stroma.xstroma.XSTROMAResponseWriter;
 import org.cyclades.engine.util.SendMail;
 import org.cyclades.xml.comparitor.XMLComparitor;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.w3c.dom.Node;
 
 public class XSTROMANyxlet extends STROMANyxlet {
 
     public XSTROMANyxlet () throws Exception {
         super();
     }
 
     @Override
     public void loadActionHandlers (Set<Entry<Object, Object>> actionHandlerEntrySet) throws Exception {
         // NO-OP
     }
 
     @Override
     public byte[] process (NyxletSession sessionDelegate) throws CycladesException {
         final String eLabel = "XSTROMANyxlet.process: ";
         try {
             this.processXSTROMARequest(sessionDelegate);
             return null;
         } catch (Exception e) {
             this.logError(eLabel + e);
             try {
                 return this.handleError(ResponseCodeEnum.GENERAL_ERROR.getCode(), e);
             } catch (Exception ex) {
                 throw new CycladesException(eLabel + ex);
             }
         }
     }
 
     protected void processXSTROMARequest (NyxletSession sessionDelegate) throws Exception {
         final String eLabel = "XSTROMANyxlet.processXSTROMARequest: ";
         sessionDelegate.setRawResponseRequested(false);
         OutputStream liveOutputStream = sessionDelegate.getOutputStream();
         OutputStream workingOutputStream = null;
         XSTROMAResponseWriter xstromaResponseWriter = new XSTROMAResponseWriter(getName(), sessionDelegate, this);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         boolean async = false;
         String notificationList = null;
         String transData = sessionDelegate.getTransactionDataString();
         int statusCode = HttpServletResponse.SC_OK;
         int mergeCount = -1;
         int mergeTotal = -1;
         boolean moreToMerge = false;
         try {
             String mergeCountString = sessionDelegate.getParameter(MERGE_COUNT);
             String mergeTotalString = sessionDelegate.getParameter(MERGE_TOTAL);
             if (mergeCountString != null && mergeTotalString != null) {
                 mergeCount = Integer.parseInt(mergeCountString);
                 mergeTotal = Integer.parseInt(mergeTotalString);
                 if (mergeCount == 0) {
                     if (mergeTotal > 0) {
                         moreToMerge = true;
                         xstromaResponseWriter.setOmitSuffix(true);
                     }
                 } else if (mergeCount >= mergeTotal) {
                     xstromaResponseWriter.setOmitPrefix(true);
                 } else if (mergeCount > 0) {
                     moreToMerge = true;
                     xstromaResponseWriter.setOmitPrefix(true);
                     xstromaResponseWriter.setOmitSuffix(true);
                 }
             }
             sessionDelegate.setResponseContentType(sessionDelegate.getDataContentType());
             if (sessionDelegate.getDataObject() == null) {
                 throw new Exception("Error, no data detected");
             } /*else if (sessionDelegate.getActionString() != null) {
                 throw new Exception("Error, parameter \"action\" not allowed in this nyxlet.");
             }*/
             Object metaObject = sessionDelegate.getDataObject();
             String chainingMode = sessionDelegate.getParameter(CHAINING_MODE);
             boolean chainServiceRequests = (chainingMode != null && chainingMode.equalsIgnoreCase("true"));
             sessionDelegate.setOrchestrationTypeEnum((chainServiceRequests) ? OrchestrationTypeEnum.CHAINED_ORCHESTRATION : OrchestrationTypeEnum.COMPOSED_ORCHESTRATION);
             boolean respectOrchestrationFault = false;
             String respestOrchestrationFaultString =
                 sessionDelegate.getParameter(RESPECT_ORCHESTRATION_FAULT_ATTRIBUTE);
             if ((respestOrchestrationFaultString != null && respestOrchestrationFaultString.equalsIgnoreCase("true")) || chainServiceRequests) {
                 respectOrchestrationFault = true;
             }
             async = (sessionDelegate.getParameter(ASYNC) != null &&
                      sessionDelegate.getParameter(ASYNC).equalsIgnoreCase("true"));
             if (!async && !chainServiceRequests) liveOutputStream = xstromaResponseWriter.getOutputStream();
             if (async) {
                 // Output does not matter
                 liveOutputStream.close();
                 liveOutputStream = new OutputStream (){ @Override public void write ( int b ){}};
                 notificationList = sessionDelegate.getParameter(NOTIFICATION_LIST);
             }
             workingOutputStream = (chainServiceRequests) ? baos : liveOutputStream;
             String serviceName;
             if (sessionDelegate.getMetaTypeEnum().equals(MetaTypeEnum.JSON)) {
                 JSONArray requests = ((JSONObject)metaObject).getJSONArray(REQUESTS_ATTRIBUTE);
                 JSONObject requestMeta;
                 for (int i = 0; i < requests.length(); i++) {
                     serviceName = requests.getJSONObject(i).getString(SERVICE_ATTRIBUTE);
                     requestMeta = requests.getJSONObject(i).getJSONObject(META_ATTRIBUTE);
                     if (chainServiceRequests) {
                         sessionDelegate.setChainsForward(true);
                         sessionDelegate.setPortData(baos.toByteArray());
                         baos.reset();
                         if ((i == requests.length() - 1) && !moreToMerge) {
                             sessionDelegate.setChainsForward(false);
                             workingOutputStream = liveOutputStream;
                         }
                     }
                     if ((i < requests.length() - 1) || moreToMerge) {
                         sessionDelegate.setIsLast(false);
                     } else {
                         sessionDelegate.setIsLast(true);
                     }
                     if (this.dispatchNyxletRequest(serviceName, requestMeta, sessionDelegate.setOutputStream(workingOutputStream)) && respectOrchestrationFault) {
                         if (chainServiceRequests) {
                             statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                             sessionDelegate.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                         }
                         break;
                     }
                     if (sessionDelegate.getResponseMetaTypeEnum().equals(MetaTypeEnum.JSON) && !chainServiceRequests && ((i < requests.length() - 1) || moreToMerge)) workingOutputStream.write(",".getBytes());
                 }
             } else if (sessionDelegate.getMetaTypeEnum().equals(MetaTypeEnum.XML)) {
                 Vector<Node> requestsElements = XMLComparitor.getMatchingChildNodes((Node)metaObject, REQUESTS_ATTRIBUTE);
                 if (requestsElements.size() < 1) throw new Exception("No \"requests\" element found");
                 Vector<Node> requestElements = XMLComparitor.getMatchingChildNodes(requestsElements.firstElement(), REQUEST_ATTRIBUTE);
                 Node requestElement;
                 final String malformedRequestError = "Malformed service request...field missing: ";
                 for (int i = 0; i < requestElements.size(); i++) {
                     requestElement = requestElements.get(i);
                     serviceName = XMLComparitor.getAttribute(requestElement, SERVICE_ATTRIBUTE);
                     if (serviceName == null || serviceName.isEmpty()) {
                         throw new Exception(malformedRequestError + SERVICE_ATTRIBUTE);
                     }
                     if (chainServiceRequests) {
                         sessionDelegate.setChainsForward(true);
                         sessionDelegate.setPortData(baos.toByteArray());
                         baos.reset();
                         if ((i == requestElements.size() - 1) && !moreToMerge) {
                             sessionDelegate.setChainsForward(false);
                             workingOutputStream = liveOutputStream;
                         }
                     }
                     if ((i < requestElements.size() - 1) || moreToMerge) {
                         sessionDelegate.setIsLast(false);
                     } else {
                         sessionDelegate.setIsLast(true);
                     }
                     if (this.dispatchNyxletRequest(serviceName, requestElement, sessionDelegate.setOutputStream(workingOutputStream)) && respectOrchestrationFault) {
                         if (chainServiceRequests) {
                             statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                             sessionDelegate.setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                         }
                         break;
                     }
                     if (sessionDelegate.getResponseMetaTypeEnum().equals(MetaTypeEnum.JSON) && !chainServiceRequests && ((i < requestElements.size() - 1) || moreToMerge)) workingOutputStream.write(",".getBytes());
                 }
             } else {
                 throw new Exception("Undefined meta type: " + metaObject.getClass().getName());
             }
             if (!async && !chainServiceRequests) {
                 xstromaResponseWriter.writeOrchestrationFault(sessionDelegate.orchestrationFaultRaised() && respectOrchestrationFault);
                 xstromaResponseWriter.done();
             }
         } catch (Exception e) {
             logError(eLabel + e);
             sessionDelegate.setActionObject(null);
             sessionDelegate.setTransactionDataObject(null);
             sessionDelegate.setUserLoggingLevel(null);
             sessionDelegate.raiseOrchestrationFault(eLabel + e);
             new STROMAResponseWriter(this.getName(), sessionDelegate, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                     eLabel + e);
         } finally {
             try {
                 baos.close();
             } catch (Exception e) {}
             try {
                 sessionDelegate.setOutputStream(liveOutputStream);
                 // XXX - redundant close: liveOutputStream.close();
             } catch (Exception e) {}
             try {
                if (async && notificationList != null) {
                     StringBuilder sb = new StringBuilder(NyxletSession.TRANSACTION_DATA_PARAMETER);
                     sb.append(":[").append(transData).append("] statuscode:[").append(statusCode).append("]");
                     SendMail.sendMessage(notificationList, emailFrom, emailSMTPHost, emailSubject, sb.toString());
                 }
             } catch (Exception e) {
                 logError(eLabel + e);
             }
         }
     }
 
     private boolean dispatchNyxletRequest(String requestName, Object requestMeta, NyxletSession nyxletSession) throws Exception {
         final String eLabel = "XSTROMANyxlet.dispatchNxletRequest: ";
         try {
             // Clear items that we don't want to carry over from a previous Nyxlet's session, then let "process" populate them
             nyxletSession.setActionObject(null);
             nyxletSession.setTransactionDataObject(null);
             nyxletSession.setUserLoggingLevel(null);
             nyxletSession.clearOrchestrationFault();
             nyxletSession.setDurationRequested(null);
             nyxletSession.resetDuration();
             nyxletSession.setServiceAgentRequested(null);
             nyxletSession.setDataObject(requestMeta);
             Nyxlet mod = NyxletRepository.getStaticInstance().getNyxlet(requestName);
             if (mod == null) {
                 StringBuilder sb = new StringBuilder(eLabel);
                 sb.append("Nyxlet not found: ");
                 sb.append(requestName);
                 throw new CycladesException(sb.toString(), ResponseCodeEnum.SERVICE_NOT_FOUND.getCode(), "Nyxlet not found: ", requestName);
             }
             mod.process(nyxletSession);
         } catch (Exception e) {
             logError(eLabel + e);
             nyxletSession.raiseOrchestrationFault(eLabel + e);
             new STROMAResponseWriter(this.getName(), nyxletSession, this).writeErrorResponse((e instanceof CycladesException) ? ((CycladesException)e).getCode() : ResponseCodeEnum.GENERAL_ERROR.getCode(),
                     eLabel + e);
         }
         return nyxletSession.orchestrationFaultRaised();
     }
 
     @Override
     public void init () throws CycladesException {
         final String eLabel = "XSTROMANyxlet.init: ";
         try {
             super.init();
             emailSMTPHost = (getExternalProperties().containsKey(EMAIL_SMTP_HOST)) ? getExternalProperties().getProperty(EMAIL_SMTP_HOST) : "localhost";
            emailFrom = (getExternalProperties().containsKey(EMAIL_FROM)) ? getExternalProperties().getProperty(EMAIL_FROM) : "async@" + java.net.InetAddress.getLocalHost().getHostName();
             emailSubject = (getExternalProperties().containsKey(EMAIL_SUBJECT)) ?
                    getExternalProperties().getProperty(EMAIL_SUBJECT) : "Asynchronous X-STROMA Response: " + java.net.InetAddress.getLocalHost().getHostName();
         } catch (Exception e) {
             throw new CycladesException(eLabel + e);
         }
     }
 
     @Override
     public void destroy () throws CycladesException {
         super.destroy();
     }
 
     private String emailSMTPHost;
     private String emailFrom;
     private String emailSubject;
     private final static String EMAIL_SMTP_HOST                             = "emailSMTPHost";
     private final static String EMAIL_FROM                                  = "emailFrom";
     private final static String EMAIL_SUBJECT                               = "emailSubject";
     // Attribute names
     private final static String SERVICE_ATTRIBUTE                           = "service";
     private final static String META_ATTRIBUTE                              = "data";
     private final static String REQUESTS_ATTRIBUTE                          = "requests";
     private final static String REQUEST_ATTRIBUTE                           = "request";
     public final static String RESPECT_ORCHESTRATION_FAULT_ATTRIBUTE        = "enable-orchestration-fault";
     private final static String CHAINING_MODE                               = "chain";
     private final static String ASYNC                                       = "asynchronous";
     private final static String NOTIFICATION_LIST                           = "notify";
    public final static String MERGE_COUNT                                 = "merge-count";
    public final static String MERGE_TOTAL                                 = "merge-total";
 
 }
