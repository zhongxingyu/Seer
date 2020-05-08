 /*
  * Copyright (c) 2006-2011 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  ******************************************************************************/
 
 package net.link.util.saml;
 
 import be.fedict.trust.MemoryCertificateRepository;
 import be.fedict.trust.TrustValidator;
 import com.google.common.base.Charsets;
 import com.lyndir.lhunath.opal.system.logging.Logger;
 import java.io.*;
 import java.security.*;
 import java.security.cert.*;
 import java.security.cert.X509Certificate;
 import java.util.*;
 import java.util.zip.Deflater;
 import java.util.zip.DeflaterOutputStream;
 import javax.xml.namespace.QName;
 import net.link.util.common.CertificateChain;
 import net.link.util.common.DomUtils;
 import net.link.util.config.KeyProvider;
 import net.link.util.exception.ValidationFailedException;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.joda.time.DateTime;
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.common.SAMLObject;
 import org.opensaml.common.SignableSAMLObject;
 import org.opensaml.security.SAMLSignatureProfileValidator;
 import org.opensaml.xml.*;
 import org.opensaml.xml.io.*;
 import org.opensaml.xml.schema.XSAny;
 import org.opensaml.xml.schema.impl.XSAnyBuilder;
 import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
 import org.opensaml.xml.security.x509.BasicX509Credential;
 import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
 import org.opensaml.xml.signature.*;
 import org.opensaml.xml.signature.Signature;
 import org.opensaml.xml.signature.SignatureException;
 import org.opensaml.xml.signature.Signer;
 import org.opensaml.xml.signature.impl.SignatureBuilder;
 import org.opensaml.xml.util.Base64;
 import org.opensaml.xml.validation.ValidationException;
 import org.w3c.dom.Element;
 
 
 /**
  * <i>09 08, 2011</i>
  *
  * @author lhunath
  */
 public class SamlUtils {
 
    public static final Logger logger                   = Logger.get( SamlUtils.class );
     public static final MarshallerFactory   marshallerFactory        = Configuration.getMarshallerFactory();
     public static final UnmarshallerFactory unmarshallerFactory      = Configuration.getUnmarshallerFactory();
    public static final QName               XML_SCHEMA_INSTANCE_TYPE = new QName( "http://www.w3.org/2001/XMLSchema-instance", "type",
            "xsi" );
    public static final QName               XML_SCHEMA_INSTANCE_NIL  = new QName( "http://www.w3.org/2001/XMLSchema-instance", "nil",
                    "xsi" );
 
     static {
         /*
          * Next is because Sun loves to endorse crippled versions of Xerces.
          */
         System.setProperty( "javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema",
                 "org.apache.xerces.jaxp.validation.XMLSchemaFactory" );
         try {
             DefaultBootstrap.bootstrap();
         }
         catch (ConfigurationException e) {
             throw new RuntimeException( "could not bootstrap the OpenSAML library", e );
         }
     }
 
     /**
      * Generates an XML {@link XSAny} object filled with specified value.
      *
      * @param attributeValue attribute value to convert
      *
     * @param attributeElement
      * @return converted {@link XMLObject}
      */
     public static XMLObject toAttributeValue(final QName attributeElement, Object attributeValue) {
 
         logger.dbg( "converting value %s to XML", attributeValue );
 
         XSAnyBuilder anyBuilder = (XSAnyBuilder) Configuration.getBuilderFactory().getBuilder( XSAny.TYPE_NAME );
         XSAny anyValue = anyBuilder.buildObject( attributeElement, XSAny.TYPE_NAME );
 
         if (attributeValue == null) {
             anyValue.getUnknownAttributes().put( XML_SCHEMA_INSTANCE_NIL, "true" );
             return anyValue;
         }
 
         String xsType, xsValue = String.valueOf( attributeValue );
         if (Boolean.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:boolean";
         else if (Integer.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:integer";
         else if (Long.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:long";
         else if (Short.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:short";
         else if (Byte.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:byte";
         else if (Float.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:float";
         else if (Double.class.isAssignableFrom( attributeValue.getClass() ))
             xsType = "xs:double";
         else if (Date.class.isAssignableFrom( attributeValue.getClass() )) {
             xsType = "xs:dateTime";
             xsValue = new DateTime( ((Date) attributeValue).getTime() ).toString();
         } else
             xsType = "xs:string";
 
         anyValue.getUnknownAttributes().put( XML_SCHEMA_INSTANCE_TYPE, xsType );
         anyValue.setTextContent( xsValue );
        logger.dbg( "converting value %s of type %s to XML: xsType = %s, xsValue = %s", attributeValue, attributeValue.getClass(), xsType,
                xsValue );
 
         return anyValue;
     }
 
     /**
      * Returns the value inside specified {@link XMLObject}.
      *
      * @param attributeValue {@link XMLObject} to be converted.
      *
      * @return converted java object.
      */
     public static Serializable toJavaObject(XMLObject attributeValue) {
 
         if (Boolean.valueOf( attributeValue.getDOM().getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "nil" ) ))
             return null;
 
         String xsType = attributeValue.getDOM().getAttributeNS( "http://www.w3.org/2001/XMLSchema-instance", "type" );
         String xsValue = attributeValue.getDOM().getTextContent();
 
         if ("xs:boolean".equals( xsType ))
             return Boolean.valueOf( xsValue );
        else if ("xs:integer".equals( xsType ))
             return Integer.valueOf( xsValue );
         else if ("xs:long".equals( xsType ))
             return Long.valueOf( xsValue );
         else if ("xs:short".equals( xsType ))
             return Short.valueOf( xsValue );
         else if ("xs:byte".equals( xsType ))
             return Byte.valueOf( xsValue );
         else if ("xs:float".equals( xsType ))
             return Float.valueOf( xsValue );
         else if ("xs:double".equals( xsType ))
             return Double.valueOf( xsValue );
         else if ("xs:dateTime".equals( xsType ))
             return new DateTime( xsValue ).toDate();
         else if ("xs:string".equals( xsType ))
             return xsValue;
 
        throw new IllegalArgumentException(
                "XML Type (xsi:type=" + xsType + ") of attribute value (text=" + xsValue + ") not understood." );
     }
 
     public static <T extends XMLObject> T buildXMLObject(QName objectQName) {
 
         @SuppressWarnings("unchecked")
         XMLObjectBuilder<T> builder = Configuration.getBuilderFactory().getBuilder( objectQName );
         if (builder == null)
             throw new RuntimeException( "Unable to retrieve builder for object QName " + objectQName );
 
         return builder.buildObject( objectQName.getNamespaceURI(), objectQName.getLocalPart(), objectQName.getPrefix() );
     }
 
     public static Element marshall(XMLObject samlObject) {
 
         Marshaller marshaller = marshallerFactory.getMarshaller( samlObject );
 
         try {
             return marshaller.marshall( samlObject );
         }
         catch (MarshallingException e) {
             throw new RuntimeException( "While marshaling: " + samlObject, e );
         }
     }
 
     public static <X extends XMLObject> X unmarshall(Element xmlElement) {
 
         Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller( xmlElement );
 
         try {
             //noinspection unchecked
             return (X) unmarshaller.unmarshall( xmlElement );
         }
         catch (UnmarshallingException e) {
             throw new RuntimeException( "While unmarshaling: " + xmlElement, e );
         }
     }
 
     public static String deflateAndBase64Encode(SAMLObject message)
             throws IOException {
 
         logger.dbg( "Deflating and Base64 encoding SAML message" );
         String messageStr = DomUtils.domToString( marshall( message ) );
 
         ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
         Deflater deflater = new Deflater( Deflater.DEFLATED, true );
         DeflaterOutputStream deflaterStream = new DeflaterOutputStream( bytesOut, deflater );
         try {
             deflaterStream.write( messageStr.getBytes( Charsets.UTF_8 ) );
             deflaterStream.finish();
         }
         finally {
             deflaterStream.close();
         }
 
         return Base64.encodeBytes( bytesOut.toByteArray(), Base64.DONT_BREAK_LINES );
     }
 
     public static Element sign(SignableSAMLObject samlObject, KeyProvider keyProvider) {
 
         return sign( samlObject, keyProvider.getIdentityKeyPair(), keyProvider.getIdentityCertificateChain() );
     }
 
     public static Element sign(SignableSAMLObject samlObject, KeyPair signerKeyPair, @Nullable CertificateChain certificateChain) {
 
         XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
         SignatureBuilder signatureBuilder = (SignatureBuilder) builderFactory.getBuilder( Signature.DEFAULT_ELEMENT_NAME );
         Signature signature = signatureBuilder.buildObject();
         signature.setCanonicalizationAlgorithm( SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS );
 
         String algorithm = signerKeyPair.getPrivate().getAlgorithm();
         if ("RSA".equals( algorithm ))
             signature.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_RSA );
         else if ("DSA".equals( algorithm ))
             signature.setSignatureAlgorithm( SignatureConstants.ALGO_ID_SIGNATURE_DSA );
 
         BasicX509Credential signingCredential = new BasicX509Credential();
         signingCredential.setPrivateKey( signerKeyPair.getPrivate() );
 
         if (null != certificateChain) {
             // enable adding the cert.chain as KeyInfo
             ((X509KeyInfoGeneratorFactory) Configuration.getGlobalSecurityConfiguration()
                                                         .getKeyInfoGeneratorManager()
                                                         .getDefaultManager()
                                                         .getFactory( signingCredential )).setEmitEntityCertificateChain( true );
             signingCredential.setEntityCertificateChain( certificateChain.getOrderedCertificateChain() );
 
             // add certificate chain as keyinfo
             signature.setKeyInfo( getKeyInfo( certificateChain ) );
         } else {
             signingCredential.setPublicKey( signerKeyPair.getPublic() );
 
             // add public key as keyinfo
             signature.setKeyInfo( getKeyInfo( signerKeyPair.getPublic() ) );
         }
 
         signature.setSigningCredential( signingCredential );
         samlObject.setSignature( signature );
 
         // Marshall so it has an XML representation.
         Element samlElement = marshall( samlObject );
 
         // Sign after marshaling so we can add a signature to the XML representation.
         try {
             Signer.signObject( signature );
         }
         catch (SignatureException e) {
             throw new RuntimeException( e );
         }
         return samlElement;
     }
 
     public static KeyInfo getKeyInfo(CertificateChain certificateChain) {
 
         try {
             KeyInfo keyInfo = buildXMLObject( KeyInfo.DEFAULT_ELEMENT_NAME );
             for (java.security.cert.X509Certificate certificate : certificateChain)
                 KeyInfoHelper.addCertificate( keyInfo, certificate );
 
             return keyInfo;
         }
         catch (CertificateEncodingException e) {
             throw new RuntimeException( e );
         }
     }
 
     public static KeyInfo getKeyInfo(PublicKey publicKey) {
 
         KeyInfo keyInfo = buildXMLObject( KeyInfo.DEFAULT_ELEMENT_NAME );
         KeyInfoHelper.addPublicKey( keyInfo, publicKey );
 
         return keyInfo;
     }
 
     @NotNull
     public static CertificateChain validatePostSignature(Signature signature, Collection<X509Certificate> trustedCertificates)
             throws ValidationFailedException {
 
         logger.dbg( "validate[HTTP POST], Signature:\n%s", DomUtils.domToString( signature.getDOM(), true ) );
 
         try {
             CertificateChain certificateChain = new CertificateChain( KeyInfoHelper.getCertificates( signature.getKeyInfo() ) );
             List<PublicKey> publicKeys = KeyInfoHelper.getPublicKeys( signature.getKeyInfo() );
             BasicX509Credential credential = new BasicX509Credential();
             if (!certificateChain.isEmpty())
                 credential.setPublicKey( certificateChain.getIdentityCertificate().getPublicKey() );
             else if (!publicKeys.isEmpty() && publicKeys.size() == 1)
                 credential.setPublicKey( publicKeys.get( 0 ) );
             else
                 throw new ValidationFailedException( "Failed to validate XML Signature, no suitable KeyInfo found..." );
 
             // Validate the profile.
             SAMLSignatureProfileValidator pv = new SAMLSignatureProfileValidator();
             pv.validate( signature );
 
             // Validate the signature.
             SignatureValidator sigValidator = new SignatureValidator( credential );
             sigValidator.validate( signature );
 
             // Validate the certificate chain.
             if (trustedCertificates != null && !trustedCertificates.isEmpty())
                 // Allow no trusted certificates: Beware, the chain has not been validated.
                 // The application should validate it manually, eg. using XKMS.
                 validateCertificateChain( certificateChain, trustedCertificates );
 
             return certificateChain;
         }
         catch (CertificateException e) {
             throw new ValidationFailedException( e );
         }
         catch (KeyException e) {
             throw new ValidationFailedException( e );
         }
         catch (ValidationException e) {
             throw new ValidationFailedException( e );
         }
     }
 
     public static void validateCertificateChain(CertificateChain certificateChain, Collection<X509Certificate> trustedCertificates)
             throws ValidationFailedException {
 
         try {
             MemoryCertificateRepository certificateRepository = new MemoryCertificateRepository();
             for (X509Certificate trustedCertificate : trustedCertificates)
                 certificateRepository.addTrustPoint( trustedCertificate );
 
             TrustValidator trustValidator = new TrustValidator( certificateRepository );
             trustValidator.isTrusted( certificateChain.getOrderedCertificateChain() );
         }
         catch (CertPathValidatorException e) {
             throw new ValidationFailedException(
                    "Certificate chain did not validate against trusted certificates.\nChain:\n" + certificateChain
                    + "\nTrusted Certificates:\n" + trustedCertificates, e );
         }
     }
 }
