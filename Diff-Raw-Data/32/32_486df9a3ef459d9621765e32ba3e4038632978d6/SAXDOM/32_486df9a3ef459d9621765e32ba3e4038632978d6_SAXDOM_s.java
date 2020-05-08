 /**
 * $Id: SAXDOM.java,v 1.1 2002/03/26 22:07:41 vpapad Exp $
  *
  */
 
 package niagara.ndom;
 
 import org.w3c.dom.*;
 import niagara.utils.XMLUtils;
 
 
 /**
  * <code>SAXDOM</code> wraps the read-only "DOM as SAX events" implementation.
  * @author <a href="mailto:vpapad@cse.ogi.edu">Vassilis Papadimos</a>
  */
 class SAXDOM implements DOMImplementation {
 
     public Document newDocument() {
        // We can't create arbitrary documents with SAXDOM.
        return null;
     }
 
     public DOMParser newParser() {
         return new SAXDOMParser();
     }
 
     public Node importNode(Document d, Node n) {
         // SAXDOM objects are read-only.
        return null; 
     }
 }
