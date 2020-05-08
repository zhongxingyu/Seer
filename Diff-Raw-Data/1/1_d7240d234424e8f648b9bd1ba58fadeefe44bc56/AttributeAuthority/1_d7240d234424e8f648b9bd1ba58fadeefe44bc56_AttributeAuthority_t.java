 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.idp.service.shibboleth;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.math.BigInteger;
 import java.security.cert.X509Certificate;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.GuanxiPrincipal;
 import org.guanxi.common.Utils;
 import org.guanxi.common.entity.EntityFarm;
 import org.guanxi.common.entity.EntityManager;
 import org.guanxi.common.definitions.EduPerson;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.common.definitions.Shibboleth;
 import org.guanxi.common.security.SecUtils;
 import org.guanxi.common.security.SecUtilsConfig;
 import org.guanxi.xal.idp.AttributorAttribute;
 import org.guanxi.xal.idp.IdpDocument;
 import org.guanxi.xal.idp.UserAttributesDocument;
 import org.guanxi.xal.idp.ServiceProvider;
 import org.guanxi.xal.saml_1_0.assertion.AssertionDocument;
 import org.guanxi.xal.saml_1_0.assertion.AssertionType;
 import org.guanxi.xal.saml_1_0.assertion.AttributeStatementDocument;
 import org.guanxi.xal.saml_1_0.assertion.AttributeStatementType;
 import org.guanxi.xal.saml_1_0.assertion.AttributeType;
 import org.guanxi.xal.saml_1_0.assertion.ConditionsDocument;
 import org.guanxi.xal.saml_1_0.assertion.ConditionsType;
 import org.guanxi.xal.saml_1_0.assertion.NameIdentifierType;
 import org.guanxi.xal.saml_1_0.assertion.SubjectType;
 import org.guanxi.xal.saml_1_0.protocol.RequestDocument;
 import org.guanxi.xal.saml_1_0.protocol.RequestType;
 import org.guanxi.xal.saml_1_0.protocol.ResponseDocument;
 import org.guanxi.xal.saml_1_0.protocol.ResponseType;
 import org.guanxi.xal.saml_1_0.protocol.StatusCodeType;
 import org.guanxi.xal.saml_1_0.protocol.StatusDocument;
 import org.guanxi.xal.saml_1_0.protocol.StatusType;
 import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorType;
 import org.guanxi.xal.soap.Body;
 import org.guanxi.xal.soap.Envelope;
 import org.guanxi.xal.soap.EnvelopeDocument;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Text;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class AttributeAuthority extends HandlerInterceptorAdapter implements ServletContextAware {
   /** The ServletContext, passed to us by Spring as we are ServletContextAware */
   private ServletContext servletContext = null;
   /** Our logger */
   private static final Logger logger = Logger.getLogger(AttributeAuthority.class.getName());
   /** The attributors to use */
   private org.guanxi.idp.farm.attributors.Attributor[] attributor = null;
 
   public void setServletContext(ServletContext servletContext) { this.servletContext = servletContext; }
 
   public void setAttributor(org.guanxi.idp.farm.attributors.Attributor[] attributor) { this.attributor = attributor; }
 
   public void init() throws ServletException {
   }
 
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
     // Load up the config file
     IdpDocument.Idp idpConfig = (IdpDocument.Idp)servletContext.getAttribute(Guanxi.CONTEXT_ATTR_IDP_CONFIG);
     
     String nameIdentifier = null;
     RequestType samlRequest = null;
     RequestDocument samlRequestDoc = null;
     try {
       /* Parse the SOAP message that contains the SAML Request...
        * XMLBeans 2.2.0 has problems parsing from an InputStream though
        */
       InputStream in = request.getInputStream();
       BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
       StringBuffer stringBuffer = new StringBuffer();
       String line = null;
       while ((line = buffer.readLine()) != null) {
         stringBuffer.append(line);
       }
       in.close();
       EnvelopeDocument soapEnvelopeDoc = EnvelopeDocument.Factory.parse(stringBuffer.toString());
 
       Body soapBody = soapEnvelopeDoc.getEnvelope().getBody();
       // ...and extract the SAML Request
       samlRequestDoc = RequestDocument.Factory.parse(soapBody.getDomNode().getFirstChild());
       samlRequest = samlRequestDoc.getRequest();
     }
     catch(XmlException xe) {
       logger.error("Can't parse SOAP AttributeQuery", xe);
     }
 
     // The first thing we need to do is find out what Principal is being referred to by the requesting SP
 
     // Get the SP's providerId from the attribute query
     String spProviderId = samlRequest.getAttributeQuery().getResource();
     
     // Get the NameIdentifier of the query...
     nameIdentifier = samlRequest.getAttributeQuery().getSubject().getNameIdentifier().getStringValue();
     // ...and retrieve their SP specific details from the session
     GuanxiPrincipal principal = (GuanxiPrincipal)servletContext.getAttribute(nameIdentifier);
 
     HashMap<String, String> namespaces = new HashMap<String, String>();
     namespaces.put(Shibboleth.NS_SAML_10_PROTOCOL, Shibboleth.NS_PREFIX_SAML_10_PROTOCOL);
     namespaces.put(Shibboleth.NS_SAML_10_ASSERTION, Shibboleth.NS_PREFIX_SAML_10_ASSERTION);
 
     XmlOptions xmlOptions = new XmlOptions();
     xmlOptions.setSavePrettyPrint();
     xmlOptions.setSavePrettyPrintIndent(2);
     xmlOptions.setUseDefaultNamespace();
     xmlOptions.setSaveAggressiveNamespaces();
     xmlOptions.setSaveSuggestedPrefixes(namespaces);
     xmlOptions.setSaveNamespacesFirst();
 
     // Build a SAML Response to send to the SP
     ResponseDocument samlResponseDoc = ResponseDocument.Factory.newInstance(xmlOptions);
     ResponseType samlResponse = samlResponseDoc.addNewResponse();
     samlResponse.setResponseID(Utils.createNCNameID());
     samlResponse.setMajorVersion(new BigInteger("1"));
     samlResponse.setMinorVersion(new BigInteger("1"));
     samlResponse.setIssueInstant(Calendar.getInstance());
     samlResponse.setInResponseTo(samlRequest.getRequestID());
     Utils.zuluXmlObject(samlResponse, 0);
 
     // Get a SAML Status ready
     StatusDocument statusDoc = StatusDocument.Factory.newInstance();
     StatusType status = statusDoc.addNewStatus();
     StatusCodeType topLevelStatusCode = status.addNewStatusCode();
 
     // From now on, any exceptions will be propagated to the SP using <Status>
 
     // Is this a locally registered SP?
     String spID = null;
     ServiceProvider[] spList = idpConfig.getServiceProviderArray();
     for (int c=0; c < spList.length; c++) {
       if (spList[c].getName().equals(spProviderId)) {
         // We trust locally registered SPs
         spID = spProviderId;
       }
     }
 
     /* Not a locally registered SP, so full validation rules.
      *
      * The client's X509Certificate chain will only be available if the server is configured to ask for it.
      * In Tomcat's case, this means configuring client authentication:
      * clientAuth="want"
      * If you use clientAuth="true" you'll need to put the AA on a different port from the SSO as the SSO
      * doesn't use certificates as it's accessed by a browser. The AA is only accessed by a machine (SP).
      */
     if (spID == null) {
       EntityFarm farm = (EntityFarm)servletContext.getAttribute(Guanxi.CONTEXT_ATTR_IDP_ENTITY_FARM);
       EntityManager manager = farm.getEntityManagerForID(spProviderId);
 
       if (manager != null) {
         if (manager.getMetadata(spProviderId) != null) {
           if (manager.getTrustEngine() != null) {
             if (!manager.getTrustEngine().trustEntity(manager.getMetadata(spProviderId),
                                                      (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate"))) {
               logger.error("Failed to trust SP '" + spProviderId);
               topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_ERROR));
               samlResponse.setStatus(status);
               samlResponseDoc.save(response.getOutputStream());
               return false;
             }
           }
           else {
             logger.error("Manager could not find trust engine for SP '" + spProviderId);
             topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_ERROR));
             samlResponse.setStatus(status);
             samlResponseDoc.save(response.getOutputStream());
             return false;
           }
         }
         else {
           logger.error("Manager could not find metadata for SP '" + spProviderId);
           topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_ERROR));
           samlResponse.setStatus(status);
           samlResponseDoc.save(response.getOutputStream());
           return false;
         }
       }
       else {
         logger.error("Could not find manager for SP '" + spProviderId);
         topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_ERROR));
         samlResponse.setStatus(status);
         samlResponseDoc.save(response.getOutputStream());
         return false;
       }
     }
 
     // Did we get the principal from the request?
     if (principal == null) {
       // If not, there's nothing we can do about attributes.
       topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_ERROR));
       samlResponse.setStatus(status);
       samlResponseDoc.save(response.getOutputStream());
       return false;
     }
 
     // Get their attributes
     UserAttributesDocument attributesDoc = UserAttributesDocument.Factory.newInstance();
     UserAttributesDocument.UserAttributes attributes = attributesDoc.addNewUserAttributes();
     for (org.guanxi.idp.farm.attributors.Attributor attr : attributor) {
       attr.getAttributes(principal, spProviderId, attributes);
     }
 
     // Set the Status for the SAML Response
     topLevelStatusCode.setValue(new QName("", Shibboleth.SAMLP_SUCCESS));
     samlResponse.setStatus(status);
 
     // Get a new Assertion ready for the AttributeStatement nodes
     AssertionDocument assertionDoc = AssertionDocument.Factory.newInstance();
     AssertionType assertion = assertionDoc.addNewAssertion();
     assertion.setAssertionID(Utils.createNCNameID());
     assertion.setMajorVersion(new BigInteger("1"));
     assertion.setMinorVersion(new BigInteger("1"));
     assertion.setIssuer(principal.getIssuerFor(spProviderId));
     assertion.setIssueInstant(Calendar.getInstance());
     Utils.zuluXmlObject(assertion, 0);
 
     // Conditions for the assertions
     ConditionsDocument conditionsDoc = ConditionsDocument.Factory.newInstance();
     ConditionsType conditions = conditionsDoc.addNewConditions();
     conditions.setNotBefore(Calendar.getInstance());
     conditions.setNotOnOrAfter(Calendar.getInstance());
     Utils.zuluXmlObject(conditions, 5);
 
     assertion.setConditions(conditions);
 
     // Add the attributes if there are any
     AttributeStatementDocument attrStatementDoc = addAttributesFromFarm(attributesDoc);
 
     // If a user has no attributes we shouldn't add an Assertion or Subject
     if (attrStatementDoc != null) {
       SubjectType subject = attrStatementDoc.getAttributeStatement().addNewSubject();
       NameIdentifierType nameID = subject.addNewNameIdentifier();
       nameID.setFormat("urn:mace:shibboleth:1.0:nameIdentifier");
       nameID.setNameQualifier(samlRequest.getAttributeQuery().getSubject().getNameIdentifier().getNameQualifier());
       nameID.setStringValue(samlRequest.getAttributeQuery().getSubject().getNameIdentifier().getStringValue());
 
       assertion.setAttributeStatementArray(new AttributeStatementType[] {attrStatementDoc.getAttributeStatement()});
       samlResponse.setAssertionArray(new AssertionType[] {assertion});
     }
 
     // Get the config ready for signing
     SecUtilsConfig secUtilsConfig = new SecUtilsConfig();
     secUtilsConfig.setKeystoreFile(principal.getSigningCredsFor(spProviderId).getKeystoreFile());
     secUtilsConfig.setKeystorePass(principal.getSigningCredsFor(spProviderId).getKeystorePassword());
     secUtilsConfig.setKeystoreType(principal.getSigningCredsFor(spProviderId).getKeystoreType());
     secUtilsConfig.setPrivateKeyAlias(principal.getSigningCredsFor(spProviderId).getPrivateKeyAlias());
     secUtilsConfig.setPrivateKeyPass(principal.getSigningCredsFor(spProviderId).getPrivateKeyPassword());
     secUtilsConfig.setCertificateAlias(principal.getSigningCredsFor(spProviderId).getCertificateAlias());
     secUtilsConfig.setKeyType(principal.getSigningCredsFor(spProviderId).getKeyType());
 
     response.setContentType("text/xml");
 
     // SOAP message to hold the SAML Response
     EnvelopeDocument soapResponseDoc = EnvelopeDocument.Factory.newInstance();
     Envelope soapEnvelope = soapResponseDoc.addNewEnvelope();
     Body soapBody = soapEnvelope.addNewBody();
 
     // Do we need to sign the assertion?
     boolean samlAddedToResponse = false;
     EntityDescriptorType sp = (EntityDescriptorType)servletContext.getAttribute(request.getParameter(samlRequest.getAttributeQuery().getResource()));
     if (sp != null) {
       if (sp.getSPSSODescriptorArray(0) != null) {
         if (sp.getSPSSODescriptorArray(0).getWantAssertionsSigned()) {
           // Break out to DOM land to get the SAML Response signed...
           Document signedDoc = null;
           try {
             // Add a signed assertion to the response
             samlAddedToResponse = true;
             // Need to use newDomNode to preserve namespace information
             signedDoc = SecUtils.getInstance().sign(secUtilsConfig, (Document)samlResponseDoc.newDomNode(xmlOptions), "");
             // Add the SAML Response to the SOAP message
             soapBody.getDomNode().appendChild(soapBody.getDomNode().getOwnerDocument().importNode(signedDoc.getDocumentElement(), true));
           }
           catch(GuanxiException ge) {
             logger.error(ge);
           }
         } // if (sp.getSPSSODescriptorArray(0).getWantAssertionsSigned())
       } // if (sp.getSPSSODescriptorArray(0) != null)
     }
 
     if (!samlAddedToResponse) {
       // Add the unsigned SAML Response to the SOAP message
       soapBody.getDomNode().appendChild(soapBody.getDomNode().getOwnerDocument().importNode(samlResponse.newDomNode(xmlOptions), true));
     }
 
     // Debug syphoning?
     if (idpConfig.getDebug() != null) {
       if (idpConfig.getDebug().getSypthonAttributeAssertions() != null) {
         if (idpConfig.getDebug().getSypthonAttributeAssertions().equals("yes")) {
           logger.info("=======================================================");
           logger.info("Response to AttributeQuery by " + spProviderId);
           logger.info("");
           StringWriter sw = new StringWriter();
           soapResponseDoc.save(sw, xmlOptions);
           logger.info(sw.toString());
           logger.info("");
           logger.info("=======================================================");
         }
       }
     }
 
     soapResponseDoc.save(response.getOutputStream(), xmlOptions);
 
     return false;
   }
 
   private AttributeStatementDocument addAttributesFromFarm(UserAttributesDocument guanxiAttrFarmOutput) {
     AttributeStatementDocument attrStatementDoc = AttributeStatementDocument.Factory.newInstance();
     AttributeStatementType attrStatement = attrStatementDoc.addNewAttributeStatement();
 
     boolean hasAttrs = false;
     for (int c=0; c < guanxiAttrFarmOutput.getUserAttributes().getAttributeArray().length; c++) {
       hasAttrs = true;
 
       AttributorAttribute attributorAttr = guanxiAttrFarmOutput.getUserAttributes().getAttributeArray(c);
 
       // Has the attribute already been processed? i.e. does it have multiple values?
       AttributeType attribute = null;
       AttributeType[] existingAttrs = attrStatement.getAttributeArray();
       if (existingAttrs != null) {
         for (int cc=0; cc < existingAttrs.length; cc++) {
           if (existingAttrs[cc].getAttributeName().equals(attributorAttr.getName())) {
             attribute = existingAttrs[cc];
           }
         }
       }
 
       // New attribute, not yet processed
       if (attribute == null) {
         attribute = attrStatement.addNewAttribute();
         attribute.setAttributeName(attributorAttr.getName());
         attribute.setAttributeNamespace(Shibboleth.NS_ATTRIBUTES);
       }
 
       XmlObject attrValue = attribute.addNewAttributeValue();
 
       // Deal with scoped eduPerson attributes
       if  ((attribute.getAttributeName().equals(EduPerson.EDUPERSON_SCOPED_AFFILIATION)) ||
            (attribute.getAttributeName().equals(EduPerson.EDUPERSON_TARGETED_ID))) {
         // Check if the scope is present...
         if (!attributorAttr.getValue().contains(EduPerson.EDUPERSON_SCOPED_DELIMITER)) {
           // ...if not, add the error scope
           logger.error(attribute.getAttributeName() + " has no scope, adding " + EduPerson.EDUPERSON_NO_SCOPE_DEFINED);
           attributorAttr.setValue(attributorAttr.getValue() + EduPerson.EDUPERSON_SCOPED_DELIMITER + EduPerson.EDUPERSON_NO_SCOPE_DEFINED);
         }
         String[] parts = attributorAttr.getValue().split(EduPerson.EDUPERSON_SCOPED_DELIMITER);
         Attr scopeAttribute = attrValue.getDomNode().getOwnerDocument().createAttribute(EduPerson.EDUPERSON_SCOPE_ATTRIBUTE);
         scopeAttribute.setValue(parts[1]);
         attrValue.getDomNode().getAttributes().setNamedItem(scopeAttribute);
         Text valueNode = attrValue.getDomNode().getOwnerDocument().createTextNode(parts[0]);
         attrValue.getDomNode().appendChild(valueNode);
       }
       else {
         Text valueNode = attrValue.getDomNode().getOwnerDocument().createTextNode(attributorAttr.getValue());
         attrValue.getDomNode().appendChild(valueNode);
       }
       
     }
 
     if (hasAttrs)
       return attrStatementDoc;
     else
       return null;
   }
 }
