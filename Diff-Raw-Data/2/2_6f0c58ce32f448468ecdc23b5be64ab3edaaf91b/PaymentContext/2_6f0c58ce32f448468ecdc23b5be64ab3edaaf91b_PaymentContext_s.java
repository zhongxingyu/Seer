 package net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext;
 
 import javax.xml.namespace.QName;
 import org.opensaml.common.xml.SAMLConstants;
 import org.opensaml.saml2.core.AttributeStatement;
 
 
 public interface PaymentContext extends AttributeStatement {
 
     /**
      * Element local name.
      */
    String DEFAULT_ELEMENT_LOCAL_NAME = "PaymentContextDO";
 
     /**
      * Default element name.
      */
     QName DEFAULT_ELEMENT_NAME = new QName( SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
 
     /**
      * Local name of the XSI type.
      */
     String TYPE_LOCAL_NAME = "PaymentContextType";
 
     /**
      * QName of the XSI type.
      */
     QName TYPE_NAME = new QName( SAMLConstants.SAML20_NS, TYPE_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
 }
