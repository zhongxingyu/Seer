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
 
 package org.apache.rahas;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.util.Base64;
 import org.apache.axis2.addressing.AddressingConstants;
 import org.apache.axis2.context.MessageContext;
 import org.apache.ws.security.WSConstants;
 import org.apache.ws.security.WSSecurityEngineResult;
 import org.apache.ws.security.handler.WSHandlerConstants;
 import org.apache.ws.security.handler.WSHandlerResult;
 import org.opensaml.SAMLAssertion;
 
 import javax.xml.namespace.QName;
 
 import java.security.Principal;
 import java.security.cert.X509Certificate;
 import java.util.Vector;
 
 /**
  * Common data items on WS-Trust request messages
  */
 public class RahasData {
 
     private MessageContext inMessageContext;
 
     private OMElement rstElement;
 
     private int version = -1;
 
     private String wstNs;
 
     private String requestType;
 
     private String tokenType;
 
     private int keysize = -1;
 
     private String computedKeyAlgo;
 
     private String keyType;
 
     private String appliesToAddress;
     
     private OMElement appliesToEpr;
 
     private Principal principal;
 
     private X509Certificate clientCert;
 
     private byte[] ephmeralKey;
 
     private byte[] requestEntropy;
 
     private byte[] responseEntropy;
 
     private String addressingNs;
 
     private String soapNs;
     
     private OMElement claimElem;
     
     private String  claimDialect;
     
     private SAMLAssertion assertion;
     /**
      * Create a new RahasData instance and populate it with the information from
      * the request.
      *
      * @throws TrustException <code>RequestSecurityToken</code> element is invalid.
      */
     public RahasData(MessageContext inMessageContext) throws TrustException {
 
         this.inMessageContext = inMessageContext;
 
         //Check for an authenticated Principal
         this.processWSS4JSecurityResults();
 
         // Find out the incoming addressing version
         this.addressingNs = (String) this.inMessageContext
                 .getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
 
         this.rstElement = this.inMessageContext.getEnvelope().getBody()
                 .getFirstElement();
 
         this.soapNs = this.inMessageContext.getEnvelope().getNamespace()
                 .getNamespaceURI();
 
         this.wstNs = this.rstElement.getNamespace().getNamespaceURI();
 
         int ver = TrustUtil.getWSTVersion(this.wstNs);
 
         if (ver == -1) {
             throw new TrustException(TrustException.INVALID_REQUEST);
         } else {
             this.version = ver;
         }
 
         this.processRequestType();
 
         this.processTokenType();
 
         this.processKeyType();
 
         this.processKeySize();
 
         this.processAppliesTo();
 
         this.processEntropy();
         
         this.processClaims();
 
     }
 
     /**
      * Processes the authenticated user information from the WSS4J security
      * results.
      *
      * @throws TrustException
      */
     private void processWSS4JSecurityResults() throws TrustException {
 
         /*
          * User can be identifier using a UsernameToken or a certificate - If a
          * certificate is found then we use that to - identify the user and -
          * encrypt the response (if required) - If a UsernameToken is found then
          * we will not be encrypting the response
          */
 
         Vector results;
         if ((results = (Vector) this.inMessageContext
                 .getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
             throw new TrustException(TrustException.REQUEST_FAILED);
         } else {
 
             for (int i = 0; i < results.size(); i++) {
                 WSHandlerResult rResult = (WSHandlerResult) results.get(i);
                 Vector wsSecEngineResults = rResult.getResults();
 
                 for (int j = 0; j < wsSecEngineResults.size(); j++) {
                     WSSecurityEngineResult wser = (WSSecurityEngineResult) wsSecEngineResults
                             .get(j);
                     Object principalObject = wser.get(WSSecurityEngineResult.TAG_PRINCIPAL);
                     int act = ((Integer)wser.get(WSSecurityEngineResult.TAG_ACTION)).
                             intValue();
                     if (act == WSConstants.SIGN && principalObject != null) {
                         this.clientCert = (X509Certificate) wser
                                 .get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
                         this.principal = (Principal)principalObject;
                     } else if (act == WSConstants.UT && principalObject != null) {
                         this.principal = (Principal)principalObject;
                     } else if (act == WSConstants.BST) {
                         final X509Certificate[] certificates = 
                             (X509Certificate[]) wser
                                 .get(WSSecurityEngineResult.TAG_X509_CERTIFICATES);
                         this.clientCert = certificates[0];
                         this.principal = this.clientCert.getSubjectDN();
                     } else if (act == WSConstants.ST_UNSIGNED) {
                         this.assertion = (SAMLAssertion) wser
                                 .get(WSSecurityEngineResult.TAG_SAML_ASSERTION);
                         
                     }
                 }
             }
             // If the principal or a SAML assertion is missing
             if (this.principal == null && this.assertion == null) {
                 throw new TrustException(TrustException.REQUEST_FAILED);
             }
         }
     }
 
     private void processAppliesTo() throws TrustException {
 
         OMElement appliesToElem = this.rstElement
                 .getFirstChildWithName(new QName(RahasConstants.WSP_NS,
                                                  RahasConstants.IssuanceBindingLocalNames.
                                                          APPLIES_TO));
 
         if (appliesToElem != null) {
             OMElement eprElem = appliesToElem.getFirstElement();
             this.appliesToEpr = eprElem;
             
             // If there were no addressing headers
             // The find the addressing version using the EPR element
             if (this.addressingNs == null) {
                 this.addressingNs = eprElem.getNamespace()
                         .getNamespaceURI();
             }
 
             if (eprElem != null) {
                 
                 //Of the epr is a web service then try to get the addr
                 
                 OMElement addrElem = eprElem
                         .getFirstChildWithName(new QName(
                                 this.addressingNs,
                                 AddressingConstants.EPR_ADDRESS));
                 if (addrElem != null && addrElem.getText() != null
                     && !"".equals(addrElem.getText().trim())) {
                     this.appliesToAddress = addrElem.getText().trim();
                 } 
             } else {
                 throw new TrustException("invalidAppliesToElem");
             }
         }
     }
 
     private void processRequestType() throws TrustException {
         OMElement reqTypeElem = this.rstElement
                 .getFirstChildWithName(new QName(this.wstNs,
                                                  RahasConstants.LocalNames.REQUEST_TYPE));
 
         if (reqTypeElem == null ||
             reqTypeElem.getText() == null ||
             reqTypeElem.getText().trim().length() == 0) {
             throw new TrustException(TrustException.INVALID_REQUEST);
         } else {
             this.requestType = reqTypeElem.getText().trim();
         }
     }
 
     private void processTokenType() {
         OMElement tokTypeElem = this.rstElement
                 .getFirstChildWithName(new QName(this.wstNs,
                                                  RahasConstants.LocalNames.TOKEN_TYPE));
 
         if (tokTypeElem != null && tokTypeElem.getText() != null
             && !"".equals(tokTypeElem.getText().trim())) {
             this.tokenType = tokTypeElem.getText().trim();
         }
     }
 
     /**
      * Find the value of the KeyType element of the RST
      */
     private void processKeyType() {
         OMElement keyTypeElem = this.rstElement
                 .getFirstChildWithName(new QName(this.wstNs,
                                                  RahasConstants.IssuanceBindingLocalNames.KEY_TYPE));
         if (keyTypeElem != null) {
             String text = keyTypeElem.getText();
             if (text != null && !"".equals(text.trim())) {
                 this.keyType = text.trim();
             }
         }
     }
 
     /**
      * Finds the KeySize and creates an empty ephmeral key.
      *
      * @throws TrustException
      */
     private void processKeySize() throws TrustException {
         OMElement keySizeElem =
                 this.rstElement
                         .getFirstChildWithName(new QName(this.wstNs,
                                                          RahasConstants.IssuanceBindingLocalNames.
                                                                  KEY_SIZE));
         if (keySizeElem != null) {
             String text = keySizeElem.getText();
             if (text != null && !"".equals(text.trim())) {
                 try {
                     //Set key size
                     this.keysize = Integer.parseInt(text.trim());
 
                     //Create an empty array to hold the key
                    this.ephmeralKey = new byte[this.keysize];
                 } catch (NumberFormatException e) {
                     throw new TrustException(TrustException.INVALID_REQUEST,
                                              new String[]{"invalid wst:Keysize value"}, e);
                 }
             }
         }
         this.keysize = -1;
     }
     
     /**
      * Processes a claims.
      *
      */
     private void processClaims() throws TrustException{
         	claimElem = this.rstElement
         			.getFirstChildWithName(new QName(this.wstNs,
         					RahasConstants.IssuanceBindingLocalNames.CLAIMS));
         	
         	if(claimElem != null){
         		claimDialect = claimElem.getAttributeValue(new QName(this.wstNs,
         					RahasConstants.ATTR_CLAIMS_DIALECT));
         	}
     	
     }
 
     /**
      * Process wst:Entropy element in the request.
      */
     private void processEntropy() throws TrustException {
         OMElement entropyElem = this.rstElement
                 .getFirstChildWithName(new QName(this.wstNs,
                                                  RahasConstants.IssuanceBindingLocalNames.ENTROPY));
 
         if (entropyElem != null) {
             OMElement binSecElem = entropyElem.getFirstElement();
             if (binSecElem != null && binSecElem.getText() != null
                 && !"".equals(binSecElem.getText())) {
                 this.requestEntropy = Base64.decode(binSecElem.getText());
             } else {
                 throw new TrustException("malformedEntropyElement",
                                          new String[]{entropyElem.toString()});
             }
 
         }
     }
 
     /**
      * @return Returns the appliesToAddress.
      */
     public String getAppliesToAddress() {
         return appliesToAddress;
     }
 
     /**
      * @return Returns the clientCert.
      */
     public X509Certificate getClientCert() {
         return clientCert;
     }
 
     /**
      * @return Returns the computedKeyAlgo.
      */
     public String getComputedKeyAlgo() {
         return computedKeyAlgo;
     }
 
     /**
      * @return Returns the ephmeralKey.
      */
     public byte[] getEphmeralKey() {
         return ephmeralKey;
     }
 
     /**
      * @return Returns the inMessageContext.
      */
     public MessageContext getInMessageContext() {
         return inMessageContext;
     }
 
     /**
      * @return Returns the keysize.
      */
     public int getKeysize() {
         return keysize;
     }
 
     /**
      * @return Returns the keyType.
      */
     public String getKeyType() {
         return keyType;
     }
 
     /**
      * @return Returns the principal.
      */
     public Principal getPrincipal() {
         return principal;
     }
 
     /**
      * @return Returns the requestEntropy.
      */
     public byte[] getRequestEntropy() {
         return requestEntropy;
     }
 
     /**
      * @return Returns the requestType.
      */
     public String getRequestType() {
         return requestType;
     }
 
     /**
      * @return Returns the responseEntropy.
      */
     public byte[] getResponseEntropy() {
         return responseEntropy;
     }
 
     /**
      * @return Returns the rstElement.
      */
     public OMElement getRstElement() {
         return rstElement;
     }
 
     /**
      * @return Returns the tokenType.
      */
     public String getTokenType() {
         return tokenType;
     }
 
     /**
      * @return Returns the version.
      */
     public int getVersion() {
         return version;
     }
 
     /**
      * @return Returns the addressingNs.
      */
     public String getAddressingNs() {
         return addressingNs;
     }
 
     /**
      * @return Returns the wstNs.
      */
     public String getWstNs() {
         return wstNs;
     }
 
     /**
      * @return Returns the soapNs.
      */
     public String getSoapNs() {
         return soapNs;
     }
 
     /**
      * @param responseEntropy The responseEntropy to set.
      */
     public void setResponseEntropy(byte[] responseEntropy) {
         this.responseEntropy = responseEntropy;
     }
 
     /**
      * @param ephmeralKey The ephmeralKey to set.
      */
     public void setEphmeralKey(byte[] ephmeralKey) {
         this.ephmeralKey = ephmeralKey;
     }
 
 	public String getClaimDialect() {
 		return claimDialect;
 	}
 
 	public OMElement getClaimElem() {
 		return claimElem;
 	}
 
     public OMElement getAppliesToEpr() {
         return appliesToEpr;
     }
 
 
 }
