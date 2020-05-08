 package org.codehaus.xfire.soap;
 
 
 /**
  * SOAP constants from the specs.
  *
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Feb 18, 2004
  */
 public class SoapConstants
 {
     /** Document styles. */
 
     /**
      * Constant used to specify a rpc binding style.
      * @deprecated RPC style is not (and probably never will be) supported by XFire.
      */
     public static final String STYLE_RPC = "rpc";
 
     /**
      * Constant used to specify a document binding style.
      */
     public static final String STYLE_DOCUMENT = "document";
 
     /**
      * Constant used to specify a wrapped binding style.
      */
     public static final String STYLE_WRAPPED = "wrapped";
 
     /**
      * Constant used to specify a message binding style.
      */
     public static final String STYLE_MESSAGE = "message";
 
     /**
      * Constant used to specify a literal binding use.
      */
     public static final String USE_LITERAL = "literal";
 
     /**
      * Constant used to specify a encoded binding use.
      * @deprecated Encoded use is not (and probably never will be) supported by XFire.
      */
     public static final String USE_ENCODED = "encoded";
 
     /**
      * XML Schema Namespace.
      */
     public static final String XSD = "http://www.w3.org/2001/XMLSchema";
     public static final String XSD_PREFIX = "xsd";
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
 
 }
