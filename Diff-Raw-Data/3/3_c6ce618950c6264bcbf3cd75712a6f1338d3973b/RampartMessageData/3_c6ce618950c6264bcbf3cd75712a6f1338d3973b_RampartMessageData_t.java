 /*
  * Copyright 2004,2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.rampart;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.soap.SOAPEnvelope;
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.util.PolicyUtil;
 import org.apache.axis2.engine.AxisConfiguration;
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.context.OperationContext;
 import org.apache.axis2.description.AxisService;
 import org.apache.axis2.description.Parameter;
 import org.apache.axis2.wsdl.WSDLConstants;
 import org.apache.neethi.Policy;
 import org.apache.neethi.PolicyEngine;
 import org.apache.neethi.PolicyComponent;
 import org.apache.rahas.RahasConstants;
 import org.apache.rahas.SimpleTokenStore;
 import org.apache.rahas.TokenStorage;
 import org.apache.rahas.TrustException;
 import org.apache.rahas.TrustUtil;
 import org.apache.rampart.handler.WSSHandlerConstants;
 import org.apache.rampart.policy.RampartPolicyBuilder;
 import org.apache.rampart.policy.RampartPolicyData;
 import org.apache.rampart.policy.model.RampartConfig;
 import org.apache.rampart.util.Axis2Util;
 import org.apache.rampart.util.RampartUtil;
 import org.apache.ws.secpolicy.WSSPolicyException;
 import org.apache.ws.security.SOAPConstants;
 import org.apache.ws.security.WSConstants;
 import org.apache.ws.security.WSSConfig;
 import org.apache.ws.security.WSSecurityEngineResult;
 import org.apache.ws.security.WSSecurityException;
 import org.apache.ws.security.conversation.ConversationConstants;
 import org.apache.ws.security.handler.WSHandlerConstants;
 import org.apache.ws.security.handler.WSHandlerResult;
 import org.apache.ws.security.message.WSSecHeader;
 import org.apache.ws.security.message.token.SecurityContextToken;
 import org.apache.ws.security.util.Loader;
 import org.apache.ws.security.util.WSSecurityUtil;
 import org.opensaml.SAMLAssertion;
 import org.w3c.dom.Document;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 import java.util.ArrayList;
 
 public class RampartMessageData {
     
     /**
      * Axis2 parameter name to be used in the client's axis2 xml
      */
     public final static String KEY_RAMPART_POLICY = "rampartPolicy";
     
     public final static String KEY_RAMPART_IN_POLICY = "rampartInPolicy";
         
     public final static String KEY_RAMPART_OUT_POLICY = "rampartOutPolicy";
     
     /**
      * Key to hold the populated RampartPolicyData object
      */
     public final static String RAMPART_POLICY_DATA = "rampartPolicyData";
     
     public final static String RAMPART_STS_POLICY = "rampartStsPolicy";
     
     /**
      * Key to hold the custom issued token identifier
      */
     public final static String KEY_CUSTOM_ISSUED_TOKEN = "customIssuedToken";
     
     /**
      * Key to hold username which was used to authenticate
      */
     public final static String USERNAME = "username";
     
     /**
      * Key to hold the WS-Trust version
      */
     public final static String KEY_WST_VERSION = "wstVersion";
     
     public final static String PARAM_CLIENT_SIDE = "CLIENT_SIDE";
 
     /**
      * Key to hold the WS-SecConv version
      */
     public final static String KEY_WSSC_VERSION = "wscVersion";
 
     public static final String KEY_SCT_ISSUER_POLICY = "sct-issuer-policy";
     
     public final static String CANCEL_REQUEST = "cancelrequest";
     
     public final static String SCT_ID = "sctID";
 
     public final static String X509_CERT ="X509Certificate";
     
     private MessageContext msgContext = null;
 
     private RampartPolicyData policyData = null;
 
     private WSSecHeader secHeader = null;
 
     private WSSConfig config = null;
     
     private int timeToLive = 300;
     
     private int timestampMaxSkew = 0;
     
     private String timestampId;
     
     private Document document;
 
     private TokenStorage tokenStorage;
     
     /**
      * WS-Trust version to use.
      * 
      * Possible values:
      * RahasConstants.VERSION_05_02
      * RahasConstants.VERSION_05_12
      */
     
     private int wstVersion = RahasConstants.VERSION_05_02;
     
     private int secConvVersion = ConversationConstants.DEFAULT_VERSION;
     
     /*
      * IssuedTokens or SecurityContextTokens can be used
      * as the encryption token, signature token
      */
     private String issuedEncryptionTokenId;
     
     private String issuedSignatureTokenId;
     
     /**
      * The service policy extracted from the message context.
      * If policy is specified in the RampartConfig <b>this</b> will take precedence
      */
     private Policy servicePolicy;
 
     private boolean isInitiator;
     
     private boolean sender;
     
     private ClassLoader customClassLoader;
     
     private SOAPConstants soapConstants;
 
     public RampartMessageData(MessageContext msgCtx, boolean sender) throws RampartException {
         
         this.msgContext = msgCtx;
         
         try {
 
             //Extract known properties from the msgCtx
             
             if(msgCtx.getProperty(KEY_WST_VERSION) != null) {
                 this.wstVersion = TrustUtil.getWSTVersion((String)msgCtx.getProperty(KEY_WST_VERSION));
             }
             
             if(msgCtx.getProperty(KEY_WSSC_VERSION) != null) {
                 this.secConvVersion = TrustUtil.getWSTVersion((String)msgCtx.getProperty(KEY_WSSC_VERSION));
             }
             
             // First obtain the axis service as we have to do a null check, there can be situations 
             // where Axis Service is null
             AxisService axisService = msgCtx.getAxisService();            
                     
             if(axisService != null && axisService.getParameter(PARAM_CLIENT_SIDE) != null) {
                 this.isInitiator = true;
             } else {
                 this.isInitiator = !msgCtx.isServerSide();
                 //TODO if Axis Service is null at this point, do we have to create a dummy one ??    
                 if(this.isInitiator && axisService != null ) {
                     Parameter clientSideParam = new Parameter();
                     clientSideParam.setName(PARAM_CLIENT_SIDE);
                     clientSideParam.setLocked(true);
                     msgCtx.getAxisService().addParameter(clientSideParam);
                 }
             }
             
             if(msgCtx.getProperty(KEY_RAMPART_POLICY) != null) {
                 this.servicePolicy = (Policy)msgCtx.getProperty(KEY_RAMPART_POLICY);
             }
             
             
             // Checking which flow we are in
             int flow = msgCtx.getFLOW();
             
             // If we are IN flow or IN_FAULT flow and the KEY_RAMPART_IN_POLICY is set , we set the
             // merge that policy to the KEY_RAMPART_POLICY if it is present. Else we set 
             // KEY_RAMPART_IN_POLICY as the service policy
             if ( (flow == MessageContext.IN_FLOW || flow == MessageContext.IN_FAULT_FLOW ) 
                     &&  msgCtx.getProperty(KEY_RAMPART_IN_POLICY) != null) {
                 if ( this.servicePolicy == null ) {
                     this.servicePolicy = (Policy)msgCtx.getProperty(KEY_RAMPART_IN_POLICY);
                 } else {
                     this.servicePolicy = this.servicePolicy.merge((Policy)msgCtx
                             .getProperty(KEY_RAMPART_IN_POLICY));
                 }
                 
             // If we are OUT flow or OUT_FAULT flow and the KEY_RAMPART_OUT_POLICY is set , we set 
             // the merge that policy to the KEY_RAMPART_POLICY if it is present. Else we set 
             // KEY_RAMPART_OUT_POLICY as the service policy    
             } else if ( (flow == MessageContext.OUT_FLOW || flow == MessageContext.OUT_FAULT_FLOW ) 
                     &&  msgCtx.getProperty(KEY_RAMPART_OUT_POLICY) != null) {
                 if (this.servicePolicy == null) {
                     this.servicePolicy = (Policy)msgCtx.getProperty(KEY_RAMPART_OUT_POLICY);
                 } else {
                     this.servicePolicy = this.servicePolicy.merge((Policy)msgCtx
                             .getProperty(KEY_RAMPART_OUT_POLICY));
                 }
             }
             
             /*
              * Init policy:
              * When creating the RampartMessageData instance we 
              * extract the service policy is set in the msgCtx.
              * If it is missing then try to obtain from the configuration files.
              */
 
             if (this.servicePolicy == null) {
                 try {
                     this.servicePolicy = msgCtx.getEffectivePolicy();
                 } catch (NullPointerException e) {
                     //TODO remove this once AXIS2-4114 is fixed
                     if (axisService != null) {
                         List<PolicyComponent> policyList = new ArrayList<PolicyComponent>();
                         policyList.addAll(axisService.getPolicySubject().getAttachedPolicyComponents());
                         AxisConfiguration axisConfiguration = axisService.getAxisConfiguration();
                         policyList.addAll(axisConfiguration.getPolicySubject().getAttachedPolicyComponents());
                         this.servicePolicy = PolicyUtil.getMergedPolicy(policyList, axisService);
                     }
                 }
             }
 
             if(this.servicePolicy == null) {
                 Parameter param = msgCtx.getParameter(RampartMessageData.KEY_RAMPART_POLICY);
                 if(param != null) {
                     OMElement policyElem = param.getParameterElement().getFirstElement();
                     this.servicePolicy = PolicyEngine.getPolicy(policyElem);
                 }
             }
             
             if(this.servicePolicy != null){
                 List it = (List)this.servicePolicy.getAlternatives().next();
 
                 //Process policy and build policy data
                 this.policyData = RampartPolicyBuilder.build(it);
             }
             
             
             if(this.policyData != null) {
 
                 // Get the SOAP envelope as document, then create a security
                 // header and insert into the document (Envelope)
                 // WE SHOULD ONLY DO THE CONVERTION IF THERE IS AN APPLICABLE POLICY
                 this.document = Axis2Util.getDocumentFromSOAPEnvelope(msgCtx.getEnvelope(), true);
                 msgCtx.setEnvelope((SOAPEnvelope)this.document.getDocumentElement());
 
                 this.soapConstants = WSSecurityUtil.getSOAPConstants(this.document.getDocumentElement());
                                 
                 // Update the Rampart Config if RampartConfigCallbackHandler is present in the
                 // RampartConfig
                 
                 RampartConfigCallbackHandler rampartConfigCallbackHandler = RampartUtil
                         .getRampartConfigCallbackHandler(msgCtx, policyData);
                 
                 if (rampartConfigCallbackHandler != null) {
                     rampartConfigCallbackHandler.update(policyData.getRampartConfig());
                 }
                 
                 //Check for RST and RSTR for an SCT
                 if((WSSHandlerConstants.RST_ACTON_SCT.equals(msgContext.getWSAAction())
                         || WSSHandlerConstants.RSTR_ACTON_SCT.equals(msgContext.getWSAAction())) &&
                         this.policyData.getIssuerPolicy() != null) {
                     
                     this.servicePolicy = this.policyData.getIssuerPolicy();
                     
                     RampartConfig rampartConfig = policyData.getRampartConfig();
                     if(rampartConfig != null) {
                         /*
                          * Copy crypto info into the new issuer policy 
                          */
                         RampartConfig rc = new RampartConfig();
                         rc.setEncrCryptoConfig(rampartConfig.getEncrCryptoConfig());
                         rc.setSigCryptoConfig(rampartConfig.getSigCryptoConfig());
                         rc.setDecCryptoConfig(rampartConfig.getDecCryptoConfig());
                         rc.setUser(rampartConfig.getUser());
                         rc.setUserCertAlias(rc.getUserCertAlias());
                         rc.setEncryptionUser(rampartConfig.getEncryptionUser());
                         rc.setPwCbClass(rampartConfig.getPwCbClass());
                         rc.setSSLConfig(rampartConfig.getSSLConfig());
                         
                         this.servicePolicy.addAssertion(rc);
                     }
     
                     List it = (List)this.servicePolicy.getAlternatives().next();
     
                     //Process policy and build policy data
                     this.policyData = RampartPolicyBuilder.build(it);
                 }
             }
             
             
             this.sender = sender;
             
             OperationContext opCtx = this.msgContext.getOperationContext();
             
             if(!this.isInitiator && this.sender) {
                 //Get hold of the incoming msg ctx
                 MessageContext inMsgCtx;
                 if (opCtx != null
                         && (inMsgCtx = opCtx
                                 .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE)) != null
                                 && msgContext.getProperty(WSHandlerConstants.RECV_RESULTS) == null) {
                     msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, 
                             inMsgCtx.getProperty(WSHandlerConstants.RECV_RESULTS));
                     
                     //If someone set the sct_id externally use it at the receiver
                     msgContext.setProperty(SCT_ID, inMsgCtx.getProperty(SCT_ID));
                 }
             }
             
             if(this.isInitiator && !this.sender) {
                 MessageContext outMsgCtx;
                 if (opCtx != null
                         && (outMsgCtx = opCtx
                                 .getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE)) != null) {
                     
                     //If someone set the sct_id externally use it at the receiver
                     msgContext.setProperty(SCT_ID, outMsgCtx.getProperty(SCT_ID));
                 }
             }
             
            // Check whether RampartConfig is present 
            if (this.policyData != null && this.policyData.getRampartConfig() != null) {
                
                boolean timestampPrecisionInMilliseconds = Boolean.valueOf(this.policyData
                        .getRampartConfig().getTimestampPrecisionInMilliseconds()).booleanValue();
                
                // This is not the default behavior, we clone the default WSSConfig to prevent this 
                // affecting globally 
                if (timestampPrecisionInMilliseconds == WSSConfig.getDefaultWSConfig()
                                                            .isPrecisionInMilliSeconds()) {
                    this.config = WSSConfig.getDefaultWSConfig();                
                } else {
                    this.config = RampartUtil.getWSSConfigInstance();
                    this.config.setPrecisionInMilliSeconds(timestampPrecisionInMilliseconds);               
                }
            } else {
                this.config = WSSConfig.getDefaultWSConfig();
            }
             
            // To handle scenarios where password type is not set by default.
             this.config.setHandleCustomPasswordTypes(true);
 
             if (axisService != null) { 
                 this.customClassLoader = axisService.getClassLoader(); 
             } 
             
             if(this.sender && this.policyData != null) {
                 this.secHeader = new WSSecHeader();
                 secHeader.insertSecurityHeader(this.document);
             }
             
         } catch (TrustException e) {
             throw new RampartException("errorInExtractingMsgProps", e);
         } catch (AxisFault e) {
             throw new RampartException("errorInExtractingMsgProps", e);
         } catch (WSSPolicyException e) {
             throw new RampartException("errorInExtractingMsgProps", e);
         } catch (WSSecurityException e) {
             throw new RampartException("errorInExtractingMsgProps", e);
         }
         
     }
 
     /**
      * @return Returns the document.
      */
     public Document getDocument() {
         return document;
     }
 
     /**
      * @param document The document to set.
      * @deprecated document is derived from MessageContext passed in constructor
      */
     public void setDocument(Document document) {
         this.document = document;
     }
 
     /**
      * @return Returns the timeToLive.
      */
     public int getTimeToLive() {
         return timeToLive;
     }
 
     /**
      * @param timeToLive The timeToLive to set.
      */
     public void setTimeToLive(int timeToLive) {
         this.timeToLive = timeToLive;
     }
 
     /**
      * @return Returns the timestampMaxSkew.
      */
     public int getTimestampMaxSkew() {
         return timestampMaxSkew;
     }
 
     /**
      * @param timestampMaxSkew The timestampMaxSkew to set.
      */
     public void setTimestampMaxSkew(int timestampMaxSkew) {
         this.timestampMaxSkew = timestampMaxSkew;
     }
 
     /**
      * @return Returns the config.
      */
     public WSSConfig getConfig() {
         return config;
     }
 
     /**
      * @param config
      *            The config to set.
      */
     public void setConfig(WSSConfig config) {
         this.config = config;
     }
 
     /**
      * @return Returns the msgContext.
      */
     public MessageContext getMsgContext() {
         return msgContext;
     }
 
     /**
      * @param msgContext The msgContext to set.
      * @deprecated MessageContext is set in constructor
      */
     public void setMsgContext(MessageContext msgContext) {
         this.msgContext = msgContext;
     }
 
     /**
      * @return Returns the policyData.
      */
     public RampartPolicyData getPolicyData() {
         return policyData;
     }
 
     /**
      * @param policyData The policyData to set.
      * @deprecated Policy data determined within constructor
      */
     public void setPolicyData(RampartPolicyData policyData) throws RampartException {
         this.policyData = policyData;
         
         try {
             //if client side then check whether sig conf enabled 
             //and get hold of the stored signature values
             if(this.isInitiator && !this.sender && policyData.isSignatureConfirmation()) {
                 OperationContext opCtx = msgContext.getOperationContext();
                 MessageContext outMsgCtx = opCtx
                         .getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                 msgContext.setProperty(WSHandlerConstants.SEND_SIGV, outMsgCtx
                         .getProperty(WSHandlerConstants.SEND_SIGV));
             }
         } catch (AxisFault e) {
             throw new RampartException("errorGettingSignatureValuesForSigconf", e);
         }
     }
 
     /**
      * @return Returns the secHeader.
      */
     public WSSecHeader getSecHeader() {
         return secHeader;
     }
 
     /**
      * @param secHeader
      *            The secHeader to set.
      */
     public void setSecHeader(WSSecHeader secHeader) {
         this.secHeader = secHeader;
     }
 
     /**
      * @return Returns the issuedEncryptionTokenId.
      */
     public String getIssuedEncryptionTokenId() {
         return issuedEncryptionTokenId;
     }
 
     /**
      * @param issuedEncryptionTokenId The issuedEncryptionTokenId to set.
      */
     public void setIssuedEncryptionTokenId(String issuedEncryptionTokenId) {
         this.issuedEncryptionTokenId = issuedEncryptionTokenId;
     }
 
     /**
      * @return Returns the issuedSignatureTokenId.
      */
     public String getIssuedSignatureTokenId() {
         if(this.isInitiator) {
             return issuedSignatureTokenId;
         } else {
             //Pick the first SAML token
             //TODO : This is a hack , MUST FIX
             //get the sec context id from the req msg ctx
             Vector results = (Vector)this.msgContext.getProperty(WSHandlerConstants.RECV_RESULTS);
             for (int i = 0; i < results.size(); i++) {
                 WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                 Vector wsSecEngineResults = rResult.getResults();
 
                 for (int j = 0; j < wsSecEngineResults.size(); j++) {
                     WSSecurityEngineResult wser = (WSSecurityEngineResult) wsSecEngineResults
                             .get(j);
                     final Integer actInt = 
                         (Integer)wser.get(WSSecurityEngineResult.TAG_ACTION);
                     if(WSConstants.ST_UNSIGNED == actInt.intValue()) {
                         final SAMLAssertion assertion = 
                             ((SAMLAssertion) wser
                                 .get(WSSecurityEngineResult.TAG_SAML_ASSERTION));
                         return assertion.getId();
                     }
 
                 }
             }
             return null;
         }
     }
 
     /**
      * @param issuedSignatureTokenId The issuedSignatureTokenId to set.
      */
     public void setIssuedSignatureTokenId(String issuedSignatureTokenId) {
         this.issuedSignatureTokenId = issuedSignatureTokenId;
     }
 
     /**
      * @return Returns the secConvTokenId.
      */
     public String getSecConvTokenId() {
         String id = null;
         
         if(this.isInitiator) {
             String contextIdentifierKey = RampartUtil.getContextIdentifierKey(this.msgContext);
             id = (String) RampartUtil.getContextMap(this.msgContext).get(contextIdentifierKey);
         } else {
             //get the sec context id from the req msg ctx
             Vector results = (Vector)this.msgContext.getProperty(WSHandlerConstants.RECV_RESULTS);
             for (int i = 0; i < results.size(); i++) {
                 WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                 Vector wsSecEngineResults = rResult.getResults();
 
                 for (int j = 0; j < wsSecEngineResults.size(); j++) {
                     WSSecurityEngineResult wser = (WSSecurityEngineResult) wsSecEngineResults
                             .get(j);
                     final Integer actInt = 
                         (Integer)wser.get(WSSecurityEngineResult.TAG_ACTION);
                     if(WSConstants.SCT == actInt.intValue()) {
                         final SecurityContextToken sct = 
                             ((SecurityContextToken) wser
                                 .get(WSSecurityEngineResult.TAG_SECURITY_CONTEXT_TOKEN));
                         id = sct.getID();
                     }
 
                 }
             }
         }
 
         if(id == null || id.length() == 0) {
             //If we can't find the sec conv token id up to this point then
             //check if someone has specified which one to use
             id = (String)this.msgContext.getProperty(SCT_ID);
         }
         
         return id;
     }
 
     /**
      * @param secConvTokenId The secConvTokenId to set.
      */
     public void setSecConvTokenId(String secConvTokenId) {
         String contextIdentifierKey = RampartUtil.getContextIdentifierKey(this.msgContext);
         RampartUtil.getContextMap(this.msgContext).put(
                                                     contextIdentifierKey,
                                                     secConvTokenId);
     }
 
 
 
     /**
      * @return Returns the tokenStorage.
      */
     public TokenStorage getTokenStorage() throws RampartException {
 
         if(this.tokenStorage != null) {
             return this.tokenStorage;
         }
 
         TokenStorage storage = (TokenStorage) this.msgContext.getConfigurationContext().getProperty(
                         TokenStorage.TOKEN_STORAGE_KEY);
 
         if (storage != null) {
             this.tokenStorage = storage;
         } else {
             if (this.policyData.getRampartConfig() != null &&
                     this.policyData.getRampartConfig().getTokenStoreClass() != null) {
                 Class stClass = null;
                 String storageClass = this.policyData.getRampartConfig()
                         .getTokenStoreClass();
                 try {
                    stClass = Loader.loadClass(this.customClassLoader, storageClass);
                 } catch (ClassNotFoundException e) {
                     throw new RampartException(
                             "WSHandler: cannot load token storage class: "
                                     + storageClass, e);
                 }
                 try {
                     this.tokenStorage = (TokenStorage) stClass.newInstance();
                 } catch (java.lang.Exception e) {
                     throw new RampartException(
                             "Cannot create instance of token storage: "
                                     + storageClass, e);
                 }
             } else {
                 this.tokenStorage = new SimpleTokenStore();
                 
             }
             
             //Set the storage instance
             this.msgContext.getConfigurationContext().setProperty(
                     TokenStorage.TOKEN_STORAGE_KEY, this.tokenStorage);
         }
         
         
         return tokenStorage;
     }
 
     /**
      * @param tokenStorage The tokenStorage to set.
      */
     public void setTokenStorage(TokenStorage tokenStorage) {
         this.tokenStorage = tokenStorage;
     }
 
     /**
      * @return Returns the wstVersion.
      */
     public int getWstVersion() {
         return wstVersion;
     }
 
     /**
      * @param wstVersion The wstVersion to set.
      * @deprecated This is defined by the class.
      */
     public void setWstVersion(int wstVersion) {
         this.wstVersion = wstVersion;
     }
 
     /**
      * @return Returns the secConvVersion.
      */
     public int getSecConvVersion() {
         return secConvVersion;
     }
 
     /**
      * @return Returns the servicePolicy.
      */
     public Policy getServicePolicy() {
         return servicePolicy;
     }
 
     /**
      * @param servicePolicy The servicePolicy to set.
      * @deprecated servicePolicy determined in constructor
      */
     public void setServicePolicy(Policy servicePolicy) {
         this.servicePolicy = servicePolicy;
     }
     
     /**
      * @return Returns the timestampId.
      */
     public String getTimestampId() {
         return timestampId;
     }
 
     /**
      * @param timestampId The timestampId to set.
      */
     public void setTimestampId(String timestampId) {
         this.timestampId = timestampId;
     }
 
     /**
      * @return Returns the Initiator value
      */
     public boolean isInitiator() {
         return isInitiator;
     }
 
     /**
      * Returns the custom class loader if we are using one
      * @return Returns the custom class loader if we are using one
      */
     public ClassLoader getCustomClassLoader() {
         return customClassLoader;
     }
 
     /**
      * Returns an <code>org.apache.ws.security.SOAPConstants</code> instance 
      * with soap version information of this request. 
      * @return Returns soap version information of this request
      */
     public SOAPConstants getSoapConstants() {
         return soapConstants;
     }
 }
