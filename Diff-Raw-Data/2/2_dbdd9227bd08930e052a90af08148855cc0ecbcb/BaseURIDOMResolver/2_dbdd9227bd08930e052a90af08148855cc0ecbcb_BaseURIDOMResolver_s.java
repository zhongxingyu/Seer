 package org.xbrlapi.xmlbase;
 
 /**
  * This interface declares the functionality expected
  * of classes that provide the 
  * <a href="http://www.w3.org/TR/xmlbase/">XML Base</a>
  * functionality required by XBRL API.
  * 
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 
 import java.net.URI;
 
 import org.w3c.dom.Element;
 
 public interface BaseURIDOMResolver extends BaseURIResolver {
 
     /**
     * @param elt The XML DOM element that the Base URI is being
      * obtained for.
      * @return the Base URI or null if none is defined.
      * The Base URI is determined with reference to xml:base
      * attribute values and the Document URI as specified
      * in the <a href="http://www.w3.org/TR/xmlbase/">XML Base 1.0 specification</a>.
      * @throws XMLBaseException if the base URI is defined but is not absolute 
      * or is opaque.
      */
     public URI getBaseURI(Element element) throws XMLBaseException;
 
     /**
      * @return The document URI or null if none is defined.
      */
     public URI getDocumentURI();
         
 }
