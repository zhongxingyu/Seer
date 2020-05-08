 package net.transformatorhuis.cgi.conversion;
 
 import org.apache.log4j.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 import org.w3c.dom.Comment;
 import org.w3c.dom.Node;
 
 import java.util.Stack;
 import java.util.EmptyStackException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerConfigurationException;
 
 import javax.xml.transform.dom.DOMSource;
 
 import javax.xml.transform.stream.StreamResult;
 
 /**
  * Holds all information about status rib xml document
  * together with the document itself. 
  * Implements Singleton pattern.
  */
 public final class RibDocument implements RibOutput {
 
     /**
      * Logger.
      */
     private static Logger logger = Logger.getLogger(RibDocument.class);
 
     /**
      * AbstractRib XML document.
      */
     private static RibDocument instance;
 
     /**
      * All classes make use of the same (thus static) document 
      * Rib2Xml starts with empty document which get filled via 
      * the RibFactory. 
      */
     private static Document ribDoc;
     
     /**
      * Vector to keep track of current active xml element. 
      */
     private Stack parentNodes;
 
     /**
      * Root element.
      */
     private Element root;
 
     /**
      * Rib class.
      */
     private Class ribClazz;
 
     /**
      * Rib document constructor.
      */
     private RibDocument() {
 
         /* Create empty xml document */
         DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory
         .newInstance();
 
         try {
             DocumentBuilder docBuilder = docBuilderFac.newDocumentBuilder();
             ribDoc = docBuilder.newDocument();
         } catch (ParserConfigurationException ex) {
             // No exception, empty document is created
             logger.info(ex.toString());
         } 
 
         root = ribDoc.createElement("rib");
 
         parentNodes = new Stack();
 
         // Add first (root) element of xml doc
         parentNodes.push(root);
         logger.debug("Stack size (constructor): " + parentNodes.size());
 
    };
 
     /**
      * Instantiate the Ribdocument or get the already existing one.
      * RibDocument starts element calculation from within
      * @return new instance
      */
     public static RibDocument newInstance() {
 
         if (instance == null) {
             instance = new RibDocument();
             logger.debug("Created new RibDocument");
         }
 
         return instance;
 
     }
 
     /**
      * @param rib rib
      * @see net.transformatorhuis.cgi.conversion.RibOutput#doJob(net.transformatorhuis.cgi.conversion.AbstractRib)
      */
     public void doJob(AbstractRib rib) {
 
         if (rib.isEndElement()) {
             try {
                 parentNodes.pop();
                 logger.debug("Stack size: " + parentNodes.size());
                 logger.debug("--> End element");
             } catch (EmptyStackException ese) {
                 logger.error("Wrong rib structure: " + ese.toString());
             }
         } else {
             // Here the AbstractRib element get called to calculate the xml by giving it
             // to the xml document, needed to create nodes.
             logger.debug("Stack size: " + parentNodes.size());
             Node parentNode = (Node) parentNodes.pop();
             Node currentNode = rib.createXML(this);
             parentNode.appendChild(currentNode);
             parentNodes.push(parentNode);
             if (rib.hasChildElements()) {
                 parentNodes.push(currentNode);
             }
         }
 
     }
 
     /**
      * @param name name
      * @return name
      */
     public Element requestElement(String name) {
 
         return ribDoc.createElement(name);
 
     }
 
     /**
      * @param data data
      * @return text node
      */
     public Text requestTextNode(String data) {
 
         return ribDoc.createTextNode(data);
 
     }
 
     /**
      * @param data data
      * @return comment
      */
     public Comment requestComment(String data) {
 
         return ribDoc.createComment(data);
 
     }
 
     /**
      * Convert to file.
      */
     public void toFile() {
 
         ribDoc.appendChild(root);
 
         try {
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
 
             DOMSource source = new DOMSource(ribDoc);
             StreamResult result = new StreamResult(System.out);
             transformer.setOutputProperty("indent", "yes");
             transformer.transform(source, result);
 
         } catch (TransformerConfigurationException tce) {
             logger.debug(tce.toString());
         } catch (TransformerException te) {
             logger.debug(te.toString());
         }
 
     }
 
 }
