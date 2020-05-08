 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package net.link.util.ws.pkix.wssecurity;
 
 import java.security.PrivateKey;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPMessage;
 import javax.xml.soap.SOAPPart;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.handler.Handler;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.handler.soap.SOAPHandler;
 import javax.xml.ws.handler.soap.SOAPMessageContext;
 import net.link.util.ws.pkix.ClientCrypto;
 import net.link.util.ws.pkix.ServerCrypto;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ws.security.*;
 import org.apache.ws.security.components.crypto.Crypto;
 import org.apache.ws.security.message.WSSecHeader;
 import org.apache.ws.security.message.WSSecSignature;
 import org.apache.ws.security.message.WSSecTimestamp;
 import org.apache.ws.security.message.token.Timestamp;
 import org.apache.ws.security.util.WSSecurityUtil;
 import org.joda.time.DateTime;
 import org.joda.time.Instant;
 import org.w3c.dom.Document;
 
 
 /**
  * JAX-WS SOAP Handler that provides the client-side WS-Security. This handler will add the WS-Security SOAP header element as required by
  * the SafeOnline web service authentication module. Per default this handler will sign the Body element of the SOAP envelope. You can make
  * this handler to sign additional XML elements via the {@link #addToBeSignedId(String, SOAPMessageContext)} method.
  *
  * If no certificate or private key is specified, this handler will not sign the outbound message but will be able to process signed inbound
  * messages.
  *
  * @author fcorneli
  */
 public class WSSecurityClientHandler implements SOAPHandler<SOAPMessageContext> {
 
     private static final Log LOG = LogFactory.getLog( WSSecurityClientHandler.class );
 
     public static final String TO_BE_SIGNED_IDS_SET = WSSecurityClientHandler.class + ".tbs";
 
     public static final long DEFAULT_MAX_TIMESTAMP_OFFSET = 1000 * 60 * 5L;
 
     private final X509Certificate certificate;
 
     private final PrivateKey privateKey;
 
     private final X509Certificate serverCertificate;
 
     private final long maxTimestampOffset;
 
     /**
      * Main constructor.
      *
      * @param certificate        the client X509 certificate.
      * @param privateKey         the private key corresponding with the client X509 certificate.
      * @param serverCertificate  the server X509 certificate
      * @param maxTimestampOffset maximum offset of the WS-Security timestamp ( in ms ), if <code>null</code> defaults to 5 minutes
      */
     public WSSecurityClientHandler(X509Certificate certificate, PrivateKey privateKey, X509Certificate serverCertificate,
                                    Long maxTimestampOffset) {
 
         this.certificate = certificate;
         this.privateKey = privateKey;
         this.serverCertificate = serverCertificate;
 
         if (null != maxTimestampOffset)
             this.maxTimestampOffset = maxTimestampOffset;
         else
             this.maxTimestampOffset = DEFAULT_MAX_TIMESTAMP_OFFSET;
     }
 
     public Set<QName> getHeaders() {
 
         Set<QName> headers = new HashSet<QName>();
         headers.add( new QName( "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security" ) );
         return headers;
     }
 
     public void close(MessageContext messageContext) {
 
         // empty
     }
 
     public boolean handleFault(SOAPMessageContext soapMessageContext) {
 
         return true;
     }
 
     @SuppressWarnings("unchecked")
     public boolean handleMessage(SOAPMessageContext soapMessageContext) {
 
         SOAPMessage soapMessage = soapMessageContext.getMessage();
         SOAPPart soapPart = soapMessage.getSOAPPart();
 
         Boolean outboundProperty = (Boolean) soapMessageContext.get( MessageContext.MESSAGE_OUTBOUND_PROPERTY );
         if (false == outboundProperty.booleanValue()) {
             /*
              * Validate incoming WS-Security header
              */
             handleInboundDocument( soapPart );
             return true;
         }
 
         Set<String> tbsIds = (Set<String>) soapMessageContext.get( TO_BE_SIGNED_IDS_SET );
 
         handleOutBoundDocument( soapPart, tbsIds );
 
         return true;
     }
 
     private void handleInboundDocument(SOAPPart document) {
 
         LOG.debug( "WS-Security header validation" );
         WSSecurityEngine securityEngine = WSSecurityEngine.getInstance();
         Crypto crypto = new ServerCrypto();
 
         Vector<WSSecurityEngineResult> wsSecurityEngineResults;
         try {
             @SuppressWarnings("unchecked")
             Vector<WSSecurityEngineResult> checkedWsSecurityEngineResults = securityEngine.processSecurityHeader( document, null, null,
                                                                                                                   crypto );
             wsSecurityEngineResults = checkedWsSecurityEngineResults;
         }
         catch (WSSecurityException e) {
             throw SOAPUtils.createSOAPFaultException( "The signature or decryption was invalid", "FailedCheck", e );
         }
         LOG.debug( "results: " + wsSecurityEngineResults );
         if (null == wsSecurityEngineResults && null != serverCertificate) {
             LOG.debug( "No WS-Security header" );
             throw SOAPUtils.createSOAPFaultException( "No signature found.", "FailedCheck" );
         } else if (null == wsSecurityEngineResults) {
             LOG.debug( "No WS-Security header but no server certificate specified, ignoring..." );
             return;
         }
 
         Timestamp timestamp = null;
         X509Certificate signingCertificate = null;
         Set<String> signedElements = null;
         for (WSSecurityEngineResult result : wsSecurityEngineResults) {
             @SuppressWarnings("unchecked")
             Set<String> resultSignedElements = (Set<String>) result.get( WSSecurityEngineResult.TAG_SIGNED_ELEMENT_IDS );
             if (null != resultSignedElements)
                 signedElements = resultSignedElements;
 
             if (null != result.get( WSSecurityEngineResult.TAG_X509_CERTIFICATE ))
                 signingCertificate = (X509Certificate) result.get( WSSecurityEngineResult.TAG_X509_CERTIFICATE );
 
             Timestamp resultTimestamp = (Timestamp) result.get( WSSecurityEngineResult.TAG_TIMESTAMP );
             if (null != resultTimestamp)
                 timestamp = resultTimestamp;
         }
 
         if (null == signedElements)
             throw SOAPUtils.createSOAPFaultException( "The signature or decryption was invalid", "FailedCheck" );
         LOG.debug( "signed elements: " + signedElements );
 
         /*
          * Validate certificate
          */
         if (null == signingCertificate)
             throw SOAPUtils.createSOAPFaultException( "missing Certificate in WS-Security header", "InvalidSecurity" );
         if (null != serverCertificate && !serverCertificate.equals( signingCertificate ))
             throw SOAPUtils.createSOAPFaultException( "The signing certificate does not match the specified server certificate",
                                                       "FailedCheck" );
 
         /*
          * Check timestamp.
          */
         if (null == timestamp)
             throw SOAPUtils.createSOAPFaultException( "missing Timestamp in WS-Security header", "InvalidSecurity" );
         String timestampId = timestamp.getID();
         if (!signedElements.contains( timestampId ))
             throw SOAPUtils.createSOAPFaultException( "Timestamp not signed", "FailedCheck" );
         Calendar created = timestamp.getCreated();
         DateTime createdDateTime = new DateTime( created );
         Instant createdInstant = createdDateTime.toInstant();
         Instant nowInstant = new DateTime().toInstant();
         long offset = Math.abs( createdInstant.getMillis() - nowInstant.getMillis() );
         if (offset > maxTimestampOffset) {
             LOG.debug( "timestamp offset: " + offset );
             LOG.debug( "maximum allowed offset: " + maxTimestampOffset );
             throw SOAPUtils.createSOAPFaultException( "WS-Security Created Timestamp offset exceeded", "FailedCheck" );
         }
     }
 
     /**
      * @param tbsIds the optional set of XML Id's to be signed.
      */
     private void handleOutBoundDocument(Document document, Set<String> tbsIds) {
 
         if (null == certificate || null == privateKey) {
             LOG.debug( "no certificate specified, will NOT sign message" );
             return;
         }
 
         LOG.debug( "adding WS-Security SOAP header" );
         WSSecSignature wsSecSignature = new WSSecSignature();
         wsSecSignature.setKeyIdentifierType( WSConstants.BST_DIRECT_REFERENCE );
         Crypto crypto = new ClientCrypto( certificate, privateKey );
         WSSecHeader wsSecHeader = new WSSecHeader();
         wsSecHeader.insertSecurityHeader( document );
         try {
             wsSecSignature.prepare( document, crypto, wsSecHeader );
 
             SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants( document.getDocumentElement() );
 
             Vector<WSEncryptionPart> wsEncryptionParts = new Vector<WSEncryptionPart>();
             WSEncryptionPart wsEncryptionPart = new WSEncryptionPart( soapConstants.getBodyQName().getLocalPart(),
                                                                       soapConstants.getEnvelopeURI(), "Content" );
             wsEncryptionParts.add( wsEncryptionPart );
 
             WSSecTimestamp wsSecTimeStamp = new WSSecTimestamp();
             wsSecTimeStamp.setTimeToLive( 0 );
             /*
              * If ttl is zero then there will be no Expires element within the Timestamp. Eventually we want to let the service itself
              * decide how long the message validity period is.
              */
             wsSecTimeStamp.prepare( document );
             wsSecTimeStamp.prependToHeader( wsSecHeader );
             wsEncryptionParts.add( new WSEncryptionPart( wsSecTimeStamp.getId() ) );
 
             if (null != tbsIds)
                 for (String tbsId : tbsIds)
                     wsEncryptionParts.add( new WSEncryptionPart( tbsId ) );
 
             wsSecSignature.addReferencesToSign( wsEncryptionParts, wsSecHeader );
 
             wsSecSignature.prependToHeader( wsSecHeader );
 
             wsSecSignature.prependBSTElementToHeader( wsSecHeader );
 
             wsSecSignature.computeSignature();
         }
         catch (WSSecurityException e) {
             throw new RuntimeException( "WSS4J error: " + e.getMessage(), e );
         }
     }
 
     /**
      * Adds a new WS-Security client handler to the handler chain of the given JAX-WS port.
      */
     public static void addNewHandler(Object port, X509Certificate certificate, PrivateKey privateKey, X509Certificate serverCertificate,
                                      Long maxTimestampOffset) {
 
         @SuppressWarnings("unchecked")
         List<Handler> handlerChain = ((BindingProvider) port).getBinding().getHandlerChain();
         handlerChain.add( new WSSecurityClientHandler( certificate, privateKey, serverCertificate, maxTimestampOffset ) );
     }
 
     /**
      * Add an XML Id that needs to be included in the WS-Security signature digest.
      */
     public static void addToBeSignedId(String id, SOAPMessageContext context) {
 
         @SuppressWarnings("unchecked")
         Set<String> toBeSignedIds = (Set<String>) context.get( TO_BE_SIGNED_IDS_SET );
         if (null == toBeSignedIds)
             context.put( TO_BE_SIGNED_IDS_SET, toBeSignedIds = new TreeSet<String>() );
 
         toBeSignedIds.add( id );
     }
 }
