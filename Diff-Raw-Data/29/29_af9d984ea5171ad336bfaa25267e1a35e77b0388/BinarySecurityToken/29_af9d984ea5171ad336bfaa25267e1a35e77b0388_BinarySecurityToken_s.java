 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: BinarySecurityToken.java,v 1.1 2007-03-23 00:01:58 mallas Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.wss.security;
 
 import java.util.ResourceBundle;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.xml.XMLUtils;
 import com.sun.identity.shared.encode.Base64;
 import com.sun.identity.saml.common.SAMLUtils;
 import com.iplanet.am.util.Locale;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import java.security.cert.X509Certificate;
 import java.security.cert.CertificateEncodingException;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.CertPath;
 
 /**
  * This class <code>BinarySecurityToken</code> represents an X509
  * token that can be inserted into web services security header
  * for message level security.
  *
  * <p>This class implements <code>SecurityToken</code> and can be
  * created through security token factory. 
  */
 public class BinarySecurityToken implements SecurityToken {
 
     private String[] certAlias = null;
     private String valueType = null;
     private String encodingType = null;
     private String id = null;
     private String xmlString = null;
     private String value = null;
 
     private static final String BINARY_SECURITY_TOKEN = "BinarySecurityToken";
     private static final String ENCODING_TYPE = "EncodingType";
     private static final String VALUE_TYPE = "ValueType";
     private static final String ID = "Id";
     private static Debug debug = WSSUtils.debug;
     private static ResourceBundle bundle = WSSUtils.bundle;
 
     /**
      * Default constructor
      */
     private BinarySecurityToken () {}
 
     /**
      * Constructor
      * @param tokenSpec the <code>X509TokenSpec</code> for generating
      *        binary security token.
      */
     public BinarySecurityToken(X509TokenSpec tokenSpec)
                throws SecurityException {
 
         if(tokenSpec == null) {
            throw new SecurityException(
                  bundle.getString("invalidTokenSpec"));
         }
        
         this.valueType = tokenSpec.getValueType();
         this.encodingType = tokenSpec.getEncodingType();
         this.certAlias = tokenSpec.getSubjectCertAlias();
 
         if(valueType == null || encodingType == null ||
                  certAlias == null || certAlias.length == 0) {
            debug.error("BinarySecurityToken.constructor: invalid token spec");
            throw new SecurityException(
                  bundle.getString("invalidTokenSpec"));
         }
 
         byte[] data = null;
 
         try {
             if(PKIPATH.equals(valueType)) {
                List certs = AMTokenProvider.getX509Certificates(certAlias);
 
                CertificateFactory factory = 
                          CertificateFactory.getInstance("X.509");
                CertPath path = factory.generateCertPath(certs);
                data = path.getEncoded();
 
             } else if(X509V3.equals(valueType)) {
                X509Certificate certificate = 
                      AMTokenProvider.getX509Certificate(certAlias[0]); 
                data = certificate.getEncoded();
             } else {
                debug.error("BinarySecurityToken.constructor: unsupported" +
                "value type. " + valueType);
                throw new SecurityException(
                         bundle.getString("invalidTokenSpec"));
             }
             this.value = Base64.encode(data);
 
         } catch (CertificateEncodingException cee) {
             debug.error("BinarySecurityToken.constructor:: Certificate " +
             "Encoding Exception", cee); 
             throw new SecurityException(
                    bundle.getString("invalidCertificate"));
 
         } catch (CertificateException ce) {
             debug.error("BinarySecurityToken.constructor:: Certificate " +
             "Exception", ce); 
             throw new SecurityException(
                    bundle.getString("invalidCertificate"));
         }
 
         this.id = SAMLUtils.generateID();
     }
 
     public BinarySecurityToken(X509Certificate cert, 
             String valueType, String encodingType) throws SecurityException {
 
         byte data[];
         try {
             data = cert.getEncoded();
         } catch (CertificateEncodingException ce) {
             debug.error("BinarySecurityToken. Invalid Certifcate", ce);
             throw new SecurityException(
                   bundle.getString("invalidCertificate"));
         }
         value = Base64.encode(data);
         this.valueType = valueType;
         this.encodingType = encodingType; 
              
     }
 
     /**
      * Constructor
      * @param token Binary Security Token Element
      * @exception SecurityException if token Element is not a valid binary 
      *     security token 
      */
     public BinarySecurityToken(Element token) 
         throws SecurityException {
 
         if (token == null) {
             debug.error("BinarySecurityToken: null input token");
             throw new IllegalArgumentException(
                     bundle.getString("nullInputParameter")) ;
         }
 
         String elementName = token.getLocalName();
         if (elementName == null)  {
             debug.error("BinarySecurityToken: local name missing");
             throw new SecurityException(bundle.getString("nullInput")) ;
         }
 	if (!(elementName.equals(BINARY_SECURITY_TOKEN)))  {
             debug.error("BinarySecurityToken: invalid binary token");
 	    throw new SecurityException(bundle.getString("invalidElement") + 
                 ":" + elementName) ;   
 	}
         NamedNodeMap nm = token.getAttributes();
         if (nm == null) {
             debug.error("BinarySecurityToken: missing token attrs in element");
             throw new SecurityException(bundle.getString("missingAttribute"));
         }
 
         int len = nm.getLength();
         for (int i = 0; i < len; i++) {
             Attr attr = (Attr) nm.item(i);
             String localName = attr.getLocalName();
             if (localName == null) {
                 continue;
             }
 
             // check Id/EncodingType/ValueType attribute
             if (localName.equals(ID)) {
                 this.id = attr.getValue();
             } else if (localName.equals(ENCODING_TYPE)) {
                 // no namespace match done here
                 encodingType =  trimPrefix(attr.getValue());
             } else if (localName.equals(VALUE_TYPE)) {
                 // no namespace match done here
                 valueType = trimPrefix(attr.getValue());
             }
         }
  
 	if (id == null || id.length() == 0) {
             debug.error("BinarySecurityToken: ID missing");
 	    throw new SecurityException(
                   bundle.getString("missingAttribute") + " : " + ID);
 	}
 
         if (encodingType == null) {
             debug.error("BinarySecurityToken: encoding type missing");
             throw new SecurityException(
                   bundle.getString("missingAttribute") + " : " + ENCODING_TYPE);
         }
 
         if (valueType == null) {
             debug.error("BinarySecurityToken: valueType missing");
             throw new SecurityException(
                 bundle.getString("missingAttribute") + " : " + VALUE_TYPE);
         }
 
        try {
            this.value = token.getFirstChild().getNodeValue().trim();
         } catch (Exception e) {
             debug.error("BinarySecurityToken: unable to get value", e);
             this.value = null;
         }
 
        if (value == null) {
             debug.error("BinarySecurityToken: value missing");
             throw new SecurityException(bundle.getString("missingValue"));
         }
  
         // save the original string for toString()
         xmlString = XMLUtils.print(token);
 
     }
 
     /**
      * trim prefix and get the value, e.g, for wsse:X509v3 will return X509v3 
      */
     private String trimPrefix(String val) {
         int pos = val.indexOf(":");
         if (pos == -1) {
             return val;
         } else if (pos == val.length()) {
             return "";
         } else {
             return val.substring(pos+1);
         } 
     }
 
     /**
      * Gets encoding type for the token.
      *
      * @return encoding type for the token. 
      */
     public String getEncodingType() {
         return encodingType;
     }
 
     /**
      * Gets value type for the token.
      *
      * @return value type for the token. 
      */
     public String getValueType() {
         return valueType;
     }
 
     /**
      * Gets id attribute for the tokens.
      *
      * @return id attribute for the token.
      */
     public java.lang.String getId() {
         return id;
     }
 
     /**
      * Gets value of the token.
      *
      * @return value of the token.
      */
     public java.lang.String getTokenValue() { 
         return value;
     }
 
     /**
      * Returns a String representation of the token 
      * @return A string containing the valid XML for this element
      */
     public java.lang.String toString() {
         if (xmlString == null) {
             StringBuffer sb = new StringBuffer(300);
             sb.append("<").append(WSSConstants.WSSE_TAG).append(":")
               .append(BINARY_SECURITY_TOKEN).append(" ")
               .append(WSSConstants.TAG_XML_WSSE).append("=\"")
               .append(WSSConstants.WSSE_NS).append("\" ") 
               .append(WSSConstants.TAG_XML_WSU).append("=\"")
               .append(WSSConstants.WSU_NS).append("\" ")
               .append(WSSConstants.WSU_ID).append("=\"").append(id)
               .append("\" ").append(VALUE_TYPE).append("=\"")
               .append(valueType).append("\" ")
               .append(ENCODING_TYPE).append("=\"")
               .append(encodingType).append("\">\n")
               .append(value.toString()).append("\n").append("</")
               .append(WSSConstants.WSSE_TAG).append(":")
               .append(BINARY_SECURITY_TOKEN).append(">\n");
 
               xmlString = sb.toString();
         }
         return xmlString;
     }
 
     /**
      * Returns the token type.
      * @return String the token type.
      */
     public String getTokenType() {
         return SecurityToken.WSS_X509_TOKEN;
     }
 
     /**
      * Returns the <code>DOM</code> Element of the binary security
      * token.
      * @return Element the DOM document element of binary security token.
      * @exception SecurityException if the document element can not be
      *            created.
      */
     public Element toDocumentElement() throws SecurityException {
         Document document = XMLUtils.toDOMDocument(
                 toString(), WSSUtils.debug);
         if(document == null) {
            throw new SecurityException(
                  WSSUtils.bundle.getString("cannotConvertToDocument"));
         }
         return document.getDocumentElement();
     }
     
     /**
      * The <code>X509V3</code> value type indicates that
      * the value name given corresponds to a X509 Certificate
      */
     public static final String X509V3 = WSSConstants.WSSE_X509_NS + "#X509v3";
 
     /**
      * The <code>PKCS7</code> value type indicates
      * that the value name given corresponds to a
      * PKCS7 object
      */
     public static final String PKCS7 = WSSConstants.WSSE_X509_NS + "#PKCS7";
 
     /**
      * The <code>PKIPATH</code> value type indicates
      * that the value name given corresponds to a
      * PKI Path object
      */
     public static final String PKIPATH = WSSConstants.WSSE_X509_NS + "#PKIPath";
 
     /** 
      * The <code>BASE64BINARY</code> encoding type indicates that
      * the encoding name given corresponds to base64 encoding of a binary value 
      */
     public static final String BASE64BINARY =  
             WSSConstants.WSSE_MSG_SEC + "#Base64Binary";
         
     /**
      * The <code>HEXBINARY</code> encoding type indicates that
      * the encoding name given corresponds to Hex encoding of
      * a binary value 
      */
     public static final String HEXBINARY =  
          WSSConstants.WSSE_MSG_SEC + "#HexBinary";
         
 }
