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
 
 import org.w3c.dom.*;
 import org.guanxi.common.GuanxiPrincipal;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.Utils;
 import org.guanxi.common.log.Log4JLoggerConfig;
 import org.guanxi.common.log.Log4JLogger;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.common.definitions.Shibboleth;
 import org.guanxi.common.definitions.EduPerson;
 import org.guanxi.xal.saml_1_0.protocol.*;
 import org.guanxi.xal.saml_1_0.assertion.*;
 import org.guanxi.xal.soap.EnvelopeDocument;
 import org.guanxi.xal.soap.Envelope;
 import org.guanxi.xal.soap.Body;
 import org.guanxi.xal.idp.*;
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlOptions;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 import org.springframework.web.context.ServletContextAware;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.servlet.ServletContext;
 import javax.xml.namespace.QName;
 import java.io.*;
 import java.security.cert.X509Certificate;
 import java.security.cert.CertificateExpiredException;
 import java.security.cert.CertificateNotYetValidException;
 import java.security.PublicKey;
 import java.math.BigInteger;
 import java.util.Calendar;
 import java.util.HashMap;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class AttributeAuthority extends HandlerInterceptorAdapter implements ServletContextAware {
   /** The ServletContext, passed to us by Spring as we are ServletContextAware */
   private ServletContext servletContext = null;
   /** Our logger */
   private Logger log = null;
   /** The logger config */
   private Log4JLoggerConfig loggerConfig = null;
   /** The Logging setup to use */
   private Log4JLogger logger = null;
   /** The attributors to use */
   private org.guanxi.idp.farm.attributors.Attributor[] attributor = null;
 
   public void setServletContext(ServletContext servletContext) { this.servletContext = servletContext; }
 
   public void setLog(Logger log) { this.log = log; }
   public Logger getLog() { return log; }
 
   public void setLoggerConfig(Log4JLoggerConfig loggerConfig) { this.loggerConfig = loggerConfig; }
   public Log4JLoggerConfig getLoggerConfig() { return loggerConfig; }
 
   public void setLogger(Log4JLogger logger) { this.logger = logger; }
   public Log4JLogger getLogger() { return logger; }
 
   public void setAttributor(org.guanxi.idp.farm.attributors.Attributor[] attributor) { this.attributor = attributor; }
 
   public void init() throws ServletException {
     try {
       loggerConfig.setClazz(AttributeAuthority.class);
 
       // Sort out the file paths for logging
       loggerConfig.setLogConfigFile(servletContext.getRealPath(loggerConfig.getLogConfigFile()));
       loggerConfig.setLogFile(servletContext.getRealPath(loggerConfig.getLogFile()));
 
       // Get our logger
       log = logger.initLogger(loggerConfig);
     }
     catch(GuanxiException me) {
     }
   }
 
   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
     isRequesterSupported(request);
 
     String nameIdentifier = null;
     RequestType samlRequest = null;
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
       RequestDocument samlRequestDoc = RequestDocument.Factory.parse(soapBody.getDomNode().getFirstChild());
       samlRequest = samlRequestDoc.getRequest();
     }
     catch(XmlException xe) {
       log.error("Can't parse SOAP AttributeQuery", xe);
     }
 
     // The first thing we need to do is find out what Principal is being referred to by the requesting SP
 
     // Get the NameIdentifier of the query...
     nameIdentifier = samlRequest.getAttributeQuery().getSubject().getNameIdentifier().getStringValue();
     // ...and retrieve their SP specific details from the session
     GuanxiPrincipal principal = (GuanxiPrincipal)servletContext.getAttribute(nameIdentifier);
 
     // Pick up the config from the context. The SSO servlet put it there on startup
     IdpDocument.Idp idpConfig = (IdpDocument.Idp)servletContext.getAttribute(Guanxi.CONTEXT_ATTR_IDP_CONFIG);
 
     // Need to work out the identity to use based on the SP's providerId
     String issuer = null;
     ServiceProvider[] spList = idpConfig.getServiceProviderArray();
     for (int c=0; c < spList.length; c++) {
       if (spList[c].getName().equals(principal.getRelyingPartyID())) {
         String identity = spList[c].getIdentity();
         // We've found the <service-provider> node so look for the corresponding <identity> node
         Identity[] ids = idpConfig.getIdentityArray();
         for (int cc=0; cc < ids.length; cc++) {
           if (ids[cc].getName().equals(identity)) {
             issuer = ids[cc].getIssuer();
           }
         }
       }
     }
 
     HashMap namespaces = new HashMap();
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
       attr.getAttributes(principal, attributes);
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
     assertion.setIssuer(issuer);
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
 
     response.setContentType("text/xml");
 
     // SOAP message to hold the SAML Response
     EnvelopeDocument soapResponseDoc = EnvelopeDocument.Factory.newInstance();
     Envelope soapEnvelope = soapResponseDoc.addNewEnvelope();
     Body soapBody = soapEnvelope.addNewBody();
     
     // Add the SAML Response to the SOAP message
     soapBody.getDomNode().appendChild(soapBody.getDomNode().getOwnerDocument().importNode(samlResponse.newDomNode(xmlOptions), true));
 
     // Debug syphoning?
     if (idpConfig.getDebug() != null) {
       if (idpConfig.getDebug().getSypthonAttributeAssertions() != null) {
         if (idpConfig.getDebug().getSypthonAttributeAssertions().equals("yes")) {
           log.info("=======================================================");
           log.info("Response to AttributeQuery by " + principal.getRelyingPartyID());
           log.info("");
           StringWriter sw = new StringWriter();
           soapResponseDoc.save(sw, xmlOptions);
           log.info(sw.toString());
           log.info("");
           log.info("=======================================================");
         }
       }
     }
 
     soapResponseDoc.save(response.getOutputStream(), xmlOptions);
 
     return false;
   }
 
   private boolean isRequesterSupported(HttpServletRequest request) {
     // Get the client's X509 and any cert chain
     X509Certificate[] x509s = (X509Certificate[])servletContext.getAttribute("javax.servlet.request.X509Certificate");
 
     X509Certificate[] x = (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");
     //X509Certificate[] x = (X509Certificate[])request.getAttribute("org.apache.coyote.request.X509Certificate");
     //x = (X509Certificate[])request.getAttribute("X509Certificate");
 
     if (x509s == null)
       return false;
 
     // See if we support a client with this certificate
     X509Certificate x509 = null;
     PublicKey publicKey = null;
     byte[] encodedBytes = null;
     for (int count=0; count > x509s.length; count++) {
       try {
         x509 = x509s[count];
 
         // Reject the cert if it has expired - will throw one of two exceptions
         x509.checkValidity();
 
         publicKey = x509.getPublicKey();
         encodedBytes = publicKey.getEncoded();
       }
       catch(CertificateExpiredException cee) {return false; }
       catch(CertificateNotYetValidException cnyve) {return false; }
     }
 
     return true;
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
 
       if (attribute.getAttributeName().equals(EduPerson.EDUPERSON_SCOPED_AFFILIATION)) {
         String[] parts = attributorAttr.getValue().split(EduPerson.EDUPERSON_SCOPED_AFFILIATION_DELIMITER);
         Attr scopeAttribute = attrValue.getDomNode().getOwnerDocument().createAttribute(EduPerson.EDUPERSON_SCOPED_AFFILIATION_SCOPE_ATTRIBUTE);
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
