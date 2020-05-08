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
 
 package org.guanxi.sp.engine.service.shibboleth;
 
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.math.BigInteger;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.Calendar;
 import java.net.URLEncoder;
 import java.net.URL;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.guanxi.common.Bag;
 import org.guanxi.common.EntityConnection;
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.Utils;
 import org.guanxi.common.definitions.EduPerson;
 import org.guanxi.common.metadata.Metadata;
 import org.guanxi.common.entity.EntityManager;
 import org.guanxi.common.definitions.Shibboleth;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.xal.saml_1_0.assertion.*;
 import org.guanxi.xal.saml_1_0.protocol.*;
 import org.guanxi.xal.soap.Body;
 import org.guanxi.xal.soap.Envelope;
 import org.guanxi.xal.soap.EnvelopeDocument;
 import org.guanxi.xal.soap.Header;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.context.MessageSource;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * This is a thread that can be used to perform the ACS process in the background.
  * This is associated with a session and will set the ModelAndView to return upon
  * completion. Using a thread like this allows the 
  * 
  * @author matthew
  *
  */
 public class AuthConsumerServiceThread implements Runnable {
   /**
    * This is the logger that this will use. This is set to use the
    * AuthConsumerService logger because this thread is a way to allow
    * feedback on that process while it is being executed.
    */
   private static final Logger logger = Logger.getLogger(AuthConsumerService.class.getName());
   
   /**
    * These are the fixed states to indicate the current progress.
    * These should be coupled with a progress indicator and text.
    */
   private static final ModelAndView preparingAARequest, readingAAResponse, preparingGuardRequest, readingGuardResponse;
   /**
    * This is the key that is used to store the Integer that indicates
    * the approximate progress of this thread in the ModelAndView.
    */
   public static final String progressPercentKey = "percent";
   /**
    * This is the key that is used to store the String that describes
    * the current operation being performed in the ModelAndView.
    */
   public static final String progressTextKey    = "text";
   /** The localised messages to use */
   private MessageSource messages = null;
 
   /**
    * This initialises the different model and view objects used to display
    * the progress of the AuthConsumerServiceThread
    */
   static {
     preparingAARequest    = new ModelAndView();
     readingAAResponse     = new ModelAndView();
     preparingGuardRequest = new ModelAndView();
     readingGuardResponse  = new ModelAndView();
     
     preparingAARequest.addObject(progressPercentKey, new Integer(0));
     readingAAResponse.addObject(progressPercentKey, new Integer(25));
     preparingGuardRequest.addObject(progressPercentKey, new Integer(50));
     readingGuardResponse.addObject(progressPercentKey, new Integer(75));
   }
   
   /**
    * This is the object that spawned this thread and is used to reference
    * various variables that have been set in it.
    */
   private final AuthConsumerService parent;
   /**
    * This is the session according to the guard.
    * This is used in some of the communication between
    * the engine and the guard, and this is NOT the
    * session to which this thread is tied.
    */
   private final String guardSession;
   /**
    * This is the URL of the attribute consumer service
    * on the Guard.
    */
   private final String acsURL;
   /**
    * This is the URL of the Attribute Authority on the IdP.
    */
   private final String aaURL;
   /**
    * This is the URL of the Podder on the Guard.
    */
   private final String podderURL;
   /**
    * This is the entityID of the guard and is used in communications
    * with the IdP.
    */
   private final String entityID;
   /**
    * This is the keystore used to identify the client in the 
    * communication with the AA and Guard.
    */
   private final String keystoreFile;
   /**
    * This is the password associated with the keystore file.
    */
   private final String keystorePassword;
   /**
    * This is the truststore that is used to validate the server
    * in the communication with the AA and Guard.
    */
   private final String truststoreFile;
   /**
    * This is the password associated with the truststore file.
    */
   private final String truststorePassword;
   /**
    * This is the IdP's provider Id.
    */
   private final String idpProviderId;
   /**
    * This is the IdP's name identifier.
    */
   private final String idpNameIdentifier;
   /**
    * This holds the SAML response coming from an IdP
    */
   private final ResponseType samlResponse;
   /**
    * This is a very important variable which is used to
    * communicate state. This must always be accessed through
    * the methods, even within this class. This is because
    * those methods are synchronised and as such not using them
    * risks race conditions.
    * 
    * This is accessed by different threads - it is set by the
    * one associated with this object, and is read by the one
    * associated with the HttpRequest.
    */
   private volatile ModelAndView status;
   /**
    * This indicates if the thread has come to an end and can be
    * discarded. Since the status is always a valid object the nullness
    * of it cannot be tested for to determine this state. Also, assumptions
    * about the content of 'in progress' status objects should not be 
    * used to replace this as the structure of them may change.
    */
   private volatile boolean completed;
   /**
    * The entity manager for the IdP
    */
   private EntityManager manager = null;
   
   /**
    * This creates an AuthConsumerServiceThread that can be used
    * to retrieve the attributes from the AA URL and then pass them
    * to the Guard.
    * 
    * @param parent              This is the AuthConsumerService object that has spawned this object.
    * @param guardSession        This is the Guard Session string that has been passed to the Engine.
    * @param acsURL              This is the URL of the Attribute Consumer Service on the Guard.
    * @param aaURL               This is the URL of the Attribute Authority on the IdP.
    * @param podderURL           This is the URL of the Podder Service on the Guard.
    * @param entityID            This is the entityID of the Guard that will be used when talking to the IdP.
    * @param keystoreFile        This is the location of the KeyStore which will be used to authenticate the client in secure communications.
    * @param keystorePassword    This is the password for the KeyStore file.
    * @param truststoreFile      This is the TrustStore file which will be used to authenticate the server in secure communications.
    * @param truststorePassword  This is the password for the TrustStore file.
    * @param idpProviderId       This is the providerId for the IdP that provides the Attributes.
    * @param idpNameIdentifier   This is the name identifier that the IdP requires.
    * @param samlResponse        This is the initial SAML response from the IdP that confirmed that the user had logged in.
    * @param messages            This is the source of localised messages the thread must display
    * @param request             This is the request this thread is associated with
    * @param manager             This is the entity manager for the AA
    */
   public AuthConsumerServiceThread(AuthConsumerService parent, String guardSession, String acsURL, String aaURL, 
                                    String podderURL, String entityID, String keystoreFile, String keystorePassword,
                                    String truststoreFile, String truststorePassword, String idpProviderId, 
                                    String idpNameIdentifier, ResponseType samlResponse,
                                    MessageSource messages, HttpServletRequest request,
                                    EntityManager manager) {
     this.parent             = parent;
     this.guardSession       = guardSession;
     this.acsURL             = acsURL;
     this.aaURL              = aaURL;
     this.podderURL          = podderURL;
     this.entityID           = entityID;
     this.keystoreFile       = keystoreFile;
     this.keystorePassword   = keystorePassword;
     this.truststoreFile     = truststoreFile;
     this.truststorePassword = truststorePassword;
     this.idpProviderId      = idpProviderId;
     this.idpNameIdentifier  = idpNameIdentifier;
     this.samlResponse       = samlResponse;
     this.messages           = messages;
     this.manager            = manager;
 
     preparingAARequest.addObject(progressTextKey, messages.getMessage("engine.acs.preparing.aa.request", null, request.getLocale()));
     readingAAResponse.addObject(progressTextKey, messages.getMessage("engine.acs.comm.with.aa", null, request.getLocale()));
     preparingGuardRequest.addObject(progressTextKey, messages.getMessage("engine.acs.preparing.guard.request", null, request.getLocale()));
     readingGuardResponse.addObject(progressTextKey, messages.getMessage("engine.acs.comm.with.guard", null, request.getLocale()));
   }
   
   /**
    * This sets the ModelAndView, and should be called solely by
    * this class, hence the access limit. The status object should
    * only ever be set by this method to preserve the synchronised
    * status correctly.
    * 
    * @param status the current status of this thread
    */
   private synchronized void setStatus(ModelAndView status) {
     this.status = status;
   }
   /**
    * This gets the ModelAndView that can be used to display the current
    * status of the process. 
    * 
    * @return the current status of this thread
    */
   public synchronized ModelAndView getStatus() {
     return status;
   }
   /**
    * This sets the completed flag. When this has been set to true this thread
    * has concluded and should be discarded, and the ModelAndView returned by
    * {@link #getStatus()} will be the final result.
    * 
    * @param completed if this thread has finished and can be discarded
    */
   private synchronized void setCompleted(boolean completed) {
     this.completed = completed;
   }
   /**
    * This reads the completed flag. When this has been set to true this thread
    * has concluded and should be discarded, and the ModelAndView returned by
    * {@link #getStatus()} will be the final result.
    * 
    * @return if this thread has finished and can be discarded
    */
   public synchronized boolean isCompleted() {
     return completed;
   }
   
 
   /**
    * This prepares the request to the IdP for the attributes.
    * 
    * @param idpProviderId     This is the providerId for the IdP that provides the Attributes.
    * @param idpNameIdentifier This is the name identifier that the IdP requires.
    * @param entityID          The entityID of the guard to use when communicating with the Attribute Authority
    * @return                  An EnvelopeDocument containing the SOAP request
    */
   private EnvelopeDocument prepareAARequest(String idpProviderId, String idpNameIdentifier, String entityID) {
     RequestDocument    samlRequestDoc;
     RequestType        samlRequest;
     AttributeQueryType attrQuery;
     SubjectType        subject;
     NameIdentifierType nameID;
     EnvelopeDocument   soapEnvelopeDoc;
     Envelope           soapEnvelope;
     Body               soapBody;
 
     // Build a SAML Request to get attributes from the IdP
     samlRequestDoc = RequestDocument.Factory.newInstance();
     samlRequest    = samlRequestDoc.addNewRequest();
     samlRequest.setRequestID(Utils.createNCNameID());
     samlRequest.setMajorVersion(new BigInteger("1"));
     samlRequest.setMinorVersion(new BigInteger("1"));
     samlRequest.setIssueInstant(Calendar.getInstance());
     Utils.zuluXmlObject(samlRequest, 0);
 
     // Add an attribute query to the SAML request
     attrQuery = samlRequest.addNewAttributeQuery();
     attrQuery.setResource(entityID);
     subject   = attrQuery.addNewSubject();
     nameID    = subject.addNewNameIdentifier();
     nameID.setFormat(Shibboleth.NS_NAME_IDENTIFIER);
     nameID.setNameQualifier(idpProviderId);
     nameID.setStringValue(idpNameIdentifier);
 
     // Put the SAML request and attribute query in a SOAP message
     soapEnvelopeDoc = EnvelopeDocument.Factory.newInstance();
     soapEnvelope    = soapEnvelopeDoc.addNewEnvelope();
     soapBody        = soapEnvelope.addNewBody();
 
     soapBody.getDomNode().appendChild(soapBody.getDomNode().getOwnerDocument().importNode(samlRequest.getDomNode(), true));
     
     return soapEnvelopeDoc;
   }
   
   /**
    * This opens an AA connection to the indicated IdP, sends the SOAP request, and then reads the result.
    * 
    * @param aaURL                     The URL to connect to
    * @param entityID                  The entity ID of the guard to use (used to load the correct certificate from the keystore)
    * @param keystoreFile              The location of the keystore file for the client certificates
    * @param keystorePassword          The password for the keystore file
    * @param truststoreFile            The location of the truststore file to use to verify the server certificates
    * @param truststorePassword        The password for the truststore file
    * @param soapRequest               The soap request to write to the Attribute Authority
    * @return                          The response from the Attribute Authority
    * @throws GuanxiException          If there is a problem creating the connection or setting the attributes for the connection
    * @throws IOException              If there is a problem reading from or writing to the connection
    * @throws CertificateException     If there is a problem creating the truststore.
    * @throws NoSuchAlgorithmException If there is a problem creating the truststore.
    * @throws KeyStoreException        If there is a problem creating the truststore.
    */
   private String processAAConnection(String aaURL, String entityID, String keystoreFile, String keystorePassword, String truststoreFile, 
 		  							                 String truststorePassword, EnvelopeDocument soapRequest) throws GuanxiException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
     EntityConnection connection = new EntityConnection(aaURL, entityID,
                                                        keystoreFile, keystorePassword,
                                                        truststoreFile, truststorePassword,
                                                        EntityConnection.PROBING_ON);
 
     connection.setDoOutput(true);
     connection.setRequestProperty("Content-type", "text/xml");
     connection.connect();
 
     // Do the trust
     X509Certificate x509 = connection.getServerCertificate();
     if (x509 != null) {
       Metadata idpMetadata = manager.getMetadata(idpProviderId);
       idpMetadata.setHostName(new URL(aaURL).getHost());
       if (!manager.getTrustEngine().trustEntity(idpMetadata, x509)) {
         throw new GuanxiException("Trust failed");
       }
     }
     else {
       throw new GuanxiException("No X509 from connection");
     }
 
     soapRequest.save(connection.getOutputStream());
     return new String(Utils.read(connection.getInputStream()));
   }
   
   /**
    * This prepares the response to the guard regarding the AA process.
    * 
    * @param samlResponse  Used to generate the Guard request, this has been collected before this thread starts
    * @param guardSession  The string indicating the guard session to use
    * @param aaURL         The Attribute Authority URL which was just used to get the attributes
    * @param aaResponse    The response from talking to the Attribute Authority
    * @return              An EnvelopeDocument that must be sent to the Guard
    * @throws XmlException If there is a problem parsing the aaResponse
    */
   private EnvelopeDocument prepareGuardRequest(ResponseType samlResponse, String guardSession, String aaURL, String aaResponse) throws XmlException {
     EnvelopeDocument soapEnvelopeDoc;
     Envelope         soapEnvelope;
     
     soapEnvelopeDoc = EnvelopeDocument.Factory.parse(aaResponse);
 
     soapEnvelope = soapEnvelopeDoc.getEnvelope();
 
     // Before we send the SAML Response from the AA to the Guard, add the Guanxi SOAP header
     Header soapHeader = soapEnvelope.addNewHeader();
     Element gx = soapHeader.getDomNode().getOwnerDocument().createElementNS("urn:guanxi:sp", "GuanxiGuardSessionID");
     Node gxNode = soapHeader.getDomNode().appendChild(gx);
     org.w3c.dom.Text gxTextNode = soapHeader.getDomNode().getOwnerDocument().createTextNode(guardSession);
     gxNode.appendChild(gxTextNode);
   
     // Add the SAML Response from the IdP to the SOAP headers
     Header authHeader = soapEnvelope.addNewHeader();
     Element auth = authHeader.getDomNode().getOwnerDocument().createElementNS("urn:guanxi:sp", "AuthnFromIdP");
     auth.setAttribute("aa", aaURL);
     Node authNode = authHeader.getDomNode().appendChild(auth);
     authNode.appendChild(authNode.getOwnerDocument().importNode(samlResponse.getDomNode(), true));
 
     return soapEnvelopeDoc;
   }
   
   /**
    * This opens the connection to the guard, sends the SOAP request, and reads the response.
    * 
    * @param acsURL              The URL of the Guard Attribute Consumer Service
    * @param entityID            The entity ID of the Guard
    * @param keystoreFile        The location of the keystore to use to identify the engine to the guard
    * @param keystorePassword    The password for the keystore
    * @param truststoreFile      The location of the truststore to use to verify the guard
    * @param truststorePassword  The password for the truststore
    * @param soapRequest         The request that will be sent to the Guard
    * @param guardSession        The Guard's session ID
    * @return                    A string containing the response from the guard
    * @throws GuanxiException    If there is a problem creating the EntityConnection or setting the attributes on it
    * @throws IOException        If there is a problem using the EntityConnection to read or write data
    */
   private String processGuardConnection(String acsURL, String entityID, String keystoreFile, String keystorePassword,
                                         String truststoreFile, String truststorePassword,
                                         EnvelopeDocument soapRequest, String guardSession) throws GuanxiException, IOException {
     ResponseDocument responseDoc = unmarshallSAML(soapRequest);
     Bag bag = getBag(responseDoc, guardSession);
     
     // Initialise the connection to the Guard's attribute consumer service
     EntityConnection connection = new EntityConnection(acsURL, entityID, keystoreFile, keystorePassword,
                                                        truststoreFile, truststorePassword,
                                                        EntityConnection.PROBING_OFF);
     connection.setDoOutput(true);
     connection.connect();
 
     // Send the data to the Guard in an explicit POST variable
     String json = URLEncoder.encode(Guanxi.REQUEST_PARAMETER_SAML_ATTRIBUTES, "UTF-8") + "=" + URLEncoder.encode(bag.toJSON(), "UTF-8");
 
     OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
     wr.write(json);
     wr.flush();
     wr.close();
 
     // ...and read the response from the Guard
     return new String(Utils.read(connection.getInputStream()));
   }
 
   /**
    * This chunky method actually performs the meat of the AA conversation. The conversation can
    * be split into 4 parts, each of which is relayed to the user through an update to the progress
    * bar:
    * 
    * 1) Preparing the request to the Attribute Authority
    * 2) Send the request to and read the response of the Attribute Authority
    * 3) Preparing the request to the Guard which contains the meat of the response from the Attribute Authority
    * 4) Sending the request to the Guard and confirming that it was accepted
    * 
    * Once this has been completed the user is redirected to the Guard Podder URL which will set the
    * cookie associated with the Guard that indicates that the user has logged in.
    */
   @SuppressWarnings("unchecked")
   public void run() {
     ModelAndView mAndV;
     String       aaResponse, guardResponse;
     EnvelopeDocument aaSoapRequest, guardSoapRequest;
     
     mAndV = new ModelAndView();
     
     // done getting configuration information, lets make the connection to the AA
 
     setStatus(preparingAARequest);
     aaSoapRequest = prepareAARequest(idpProviderId, idpNameIdentifier, entityID);
     logger.debug("Request to AA:\n" + aaSoapRequest);
 
     setStatus(readingAAResponse);
     try {
       aaResponse = processAAConnection(aaURL, entityID, keystoreFile, keystorePassword, truststoreFile, truststorePassword, aaSoapRequest); // no close, so no finally
       logger.debug("Response from AA:\n" + aaResponse);
     }
     catch (Exception e) {
       logger.error("AA connection error", e);
       mAndV.setViewName(parent.getErrorView());
       mAndV.getModel().put(parent.getErrorViewDisplayVar(), e.getMessage());
       mAndV.getModel().put(parent.getErrorViewSimpleVar(), 
                            "There was a problem connecting to the Attribute Authority. " + 
                            "Check that the Attribute Authority Server Certificate is "   +
                            "correct and that the Attribute Authority accepts the client" +
                            "certificate of this Service Provider.");
       
       setStatus(mAndV);
       setCompleted(true);
       return;
     }
     
     // done with the connection to the AA, lets talk to the Guard
     
     setStatus(preparingGuardRequest);
     try {
       guardSoapRequest = prepareGuardRequest(samlResponse, guardSession, aaURL, aaResponse);
     }
     catch (XmlException e) { // this is caused by parsing the AA response, and so is a problem with the attribute authority not the guard
       logger.error("AA SAML Response parse error", e);
       logger.error("SOAP response:");
       logger.error("------------------------------------");
       logger.error(aaResponse);
       logger.error("------------------------------------");
       mAndV.setViewName(parent.getErrorView());
       mAndV.getModel().put(parent.getErrorViewDisplayVar(), e.getMessage());
       mAndV.getModel().put(parent.getErrorViewSimpleVar(), 
                            "There was a problem parsing the response from the Attribute " +
                            "Authority. Check that the Attribute Authority URL is correct.");
       
       setStatus(mAndV);
       setCompleted(true);
       return;
     }
     
     setStatus(readingGuardResponse);
     try {
       guardResponse = processGuardConnection(acsURL, entityID, keystoreFile, keystorePassword, truststoreFile, truststorePassword, guardSoapRequest, guardSession);
     }
     catch (Exception e) {
       logger.error("Guard ACS connection error", e);
       mAndV.setViewName(parent.getErrorView());
       mAndV.getModel().put(parent.getErrorViewDisplayVar(), e.getMessage());
       mAndV.getModel().put(parent.getErrorViewSimpleVar(), 
                            "There was a problem communicating with the Guard. Check " +
                            "that the Guard is running.");
       
       setStatus(mAndV);
       setCompleted(true);
       return;
     }
     
     // Done talking to the guard. Redirect to the Podder
     mAndV.setViewName(parent.getPodderView());
     mAndV.getModel().put("podderURL", podderURL + "?id=" + guardSession);
     
     setStatus(mAndV);
     setCompleted(true);
   }
 
   /**
    * Extracts the SAML Response from a SOAP message
    *
    * @param soapDoc The SOAP message containing the SAML Response
    * @return ResponseDocument
    * @throws GuanxiException if an error occurs
    */
   private ResponseDocument unmarshallSAML(EnvelopeDocument soapDoc) throws GuanxiException {
     // Rake through the SOAP to find the SAML Response...
     NodeList nodes = soapDoc.getEnvelope().getBody().getDomNode().getChildNodes();
     Node samlResponseNode = null;
     for (int c=0; c < nodes.getLength(); c++) {
       samlResponseNode = nodes.item(c);
       if (samlResponseNode.getLocalName() != null) {
         if (samlResponseNode.getLocalName().equals("Response"))
           break;
       }
     }
     // ...and parse it
     try {
       return ResponseDocument.Factory.parse(samlResponseNode);
     }
     catch(XmlException xe) {
       throw new GuanxiException("can't parse response: " + xe.getMessage());
     }
   }
 
   
 
   private Bag getBag(ResponseDocument responseDocument, String guardSession) throws GuanxiException {
     Bag bag = new Bag();
     bag.setSessionID(guardSession);
     bag.setSamlResponse(Utils.base64(responseDocument.toString().getBytes()));
     
     // Grab the Assertions, if there are any...
     AssertionType[] assertions = responseDocument.getResponse().getAssertionArray();
     if (assertions.length > 0) {
       // ...to get the AttributeStatement...
       AttributeStatementType[] attrStatements = assertions[0].getAttributeStatementArray();
       // ...and the corresponding attributes...
       AttributeType[] attributes = attrStatements[0].getAttributeArray();
       // ...adding them as convenience objects to the Bag
       for (int c=0; c < attributes.length; c++) {
         XmlObject[] obj = attributes[c].getAttributeValueArray();
         for (int cc=0; cc < obj.length; cc++) {
           if ((attributes[c].getAttributeName().equals(EduPerson.EDUPERSON_SCOPED_AFFILIATION)) ||
               (attributes[c].getAttributeName().equals(EduPerson.EDUPERSON_TARGETED_ID))) {
             String attrValue = obj[cc].getDomNode().getFirstChild().getNodeValue();
             if (obj[cc].getDomNode().getAttributes().getNamedItem(EduPerson.EDUPERSON_SCOPE_ATTRIBUTE) != null) {
               attrValue += EduPerson.EDUPERSON_SCOPED_DELIMITER;
               attrValue += obj[cc].getDomNode().getAttributes().getNamedItem(EduPerson.EDUPERSON_SCOPE_ATTRIBUTE).getNodeValue();
             }
             bag.addAttribute(attributes[c].getAttributeName(), attrValue);
           }
           else {
             if (obj[cc].getDomNode().getFirstChild() != null) {
               if (obj[cc].getDomNode().getFirstChild().getNodeValue() != null) {
                 bag.addAttribute(attributes[c].getAttributeName(), obj[cc].getDomNode().getFirstChild().getNodeValue());
               }
               else {
                 bag.addAttribute(attributes[c].getAttributeName(), "");
               }
             }
           }
         }
       }
     }
 
     return bag;
   }
 }
