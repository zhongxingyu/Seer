 /**
 * $Id: SAXDOM.java,v 1.2 2003/01/25 20:59:46 tufte Exp $
  *
  */
 
 package niagara.ndom;
 
 import org.w3c.dom.*;
 import niagara.utils.XMLUtils;
import niagara.utils.PEException;
 
 
 /**
  * <code>SAXDOM</code> wraps the read-only "DOM as SAX events" implementation.
  * @author <a href="mailto:vpapad@cse.ogi.edu">Vassilis Papadimos</a>
  */
 class SAXDOM implements DOMImplementation {
 
     public Document newDocument() {
        throw new PEException("Can't create arbitrary documents with SAXDOM.");
     }
 
     public DOMParser newParser() {
         return new SAXDOMParser();
     }
 
     public Node importNode(Document d, Node n) {
         // SAXDOM objects are read-only.
	throw new PEException("SAXDOM objects are read-only");
     }
 }
