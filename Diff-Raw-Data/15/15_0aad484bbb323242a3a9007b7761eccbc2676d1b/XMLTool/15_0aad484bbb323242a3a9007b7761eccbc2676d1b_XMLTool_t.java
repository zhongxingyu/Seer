package util;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 
 /**
  * This utility creates an XML document.
  * 
  * It must handle creation/addition, addition to point, removal of Elements and Contents from Maps,
  * Trees.
  * It must provide the user feedback of what has been changed in the file (getters)
  * It must be able to inform the user about its content.
  * 
  * The element operations consist of creating element trees.
  * The use of attributes is avoided because they are more difficult to read
  * and to maintain. (Reference: w3schools.com)
  * 
  * TODO: Create an easy way to store multiple values in an Element?
  * 
  * @WARNING: This code is not yet fully implemented.
  * 
  * @author Yoshida
  * 
  */
 public class XMLTool {
     
     private static final String XML_PARAM_YES = "yes";
     private static final String RUNTIME_EXP_MESSAGE =
             "Could not create a new instance of a Document.";
     private Document myDoc;
     
     /**
      * The constructor of this XML file builder automatically creates a buffered
      * document with the destination of the argument path, ready to receive elements. *
      */
     public XMLTool () {
         makeDoc();
     }
     
     /**
      * Creates a new document.
      */
     public void makeDoc () {
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try {
             myDoc = dbFactory.newDocumentBuilder().newDocument();
         }
         catch (ParserConfigurationException e) {
             throw new RuntimeException(RUNTIME_EXP_MESSAGE, e);
         }
     }
     
     /**
      * Sets a new document from an XML file.
      * This is an XML reader.
      * 
      * @param path The path with the filename of an XML formatted file.
      */
     public void setDoc (String path) {
         File file = new File(path);
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         try {
             myDoc = dbFactory.newDocumentBuilder().parse(file);
             
         }
         catch (IOException e) {
             throw new RuntimeException("Could not open file.", e);
         }
         catch (ParserConfigurationException e) {
             throw new RuntimeException(RUNTIME_EXP_MESSAGE, e);
         }
         catch (SAXException e) {
             throw new RuntimeException("XML document is corrupted.", e);
         }
     }
     
     /**
      * Gets the document being written in this XML Builder.
      * 
      * @return A document with an XML format.
      */
     public Document getDoc () {
         return myDoc;
     }
     
     /*
      * Creating new elements
      */
     
     /**
      * Creates the root of this document.
      * 
      * @param tag This parameter is the tag of the root
      * @return the root
      */
     public Element makeRoot (String tag) {
         Element root = myDoc.createElement(tag);
         myDoc.appendChild(root);
         return root;
     }
     
     /**
      * Creates a new empty element in the Doc.
      * 
      * @param tag The tag of the element
      * @return The recently created element.
      */
     public Element makeElement (String tag) {
         Element element = myDoc.createElement(tag);
         return element;
     }
     
     /**
      * Creates a new element in the Doc with a content.
      * 
      * @param tag The tag of the element
      * @param content The content of the element.
      * @return The recently created element.
      */
     public Element makeElement (String tag, String content) {
         Element element = makeElement(tag);
         element.setTextContent(content);
         return element;
     }
     
     /**
      * Creates a new elements tree from a Map.
      * 
      * @param parentTag The tag of the parent
      * @param elementsMap The keys of this map will be converted into tags and the values of the map
      *        into its values
      * @return the parent element with its respective tag.
      */
     public Element makeElementsFromMap (String parentTag, Map<String, String> elementsMap) {
         Element parent = makeElement(parentTag);
         return addChildrenFromMap(parent, elementsMap);
     }
     
     /*
      * Adding elements to parent element
      */
     
     /**
      * Adds a element child to an specific parent element
      * 
      * @param parent The parent element to which this method is going to add a child
      * @param child The element to be added as a child
      * @return The recently modified parent.
      */
     public Element addChildElement (Element parent, Element child) {
         parent.appendChild(child);
         return parent;
     }
     
     /**
      * Creates a new child element from a tag and adds it to an specific parent element
      * 
      * @param parent The parent element to which this method is going to add a child
      * @param tag The tag of the child node
      * @return The recently modified parent.
      */
     public Element addChildTag (Element parent, String tag) {
         Element child = makeElement(tag);
         return addChildElement(parent, child);
     }
     
     /**
      * Creates a new child element from a tag, adds a value to it and adds it to a parent element
      * 
      * @param parent The parent element to which this method is going to add a child
      * @param tag The tag of the child
      * @param value The value of the child
      * @return The recently modified parent.
      */
     public Element addChild (Element parent, String tag, String value) {
         Element child = makeElement(tag, value);
         return addChildElement(parent, child);
     }
     
     /**
      * Creates children and appends/adds them to a particular
      * parent element (passed in as a parameter)
      * 
      * @param parent The parent element to which this method is going to add children
      * @param elementsMap The keys of this map will be converted into tags and the values of the map
      *        into its values
      * @return The parent element.
      */
     public Element addChildrenFromMap (Element parent, Map<String, String> elementsMap) {
         for (String name : elementsMap.keySet()) {
             String value = elementsMap.get(name);
             addChild(parent, name, value);
         }
         return parent;
     }
     
     /*
      * Getters
      */
     /**
      * This method returns the first element in the document with an specific tag.
      * Be careful with this method! If you have many instances of the same tag, use
      * getElementsListByTagName, which will return a list of elements.
      * 
      * @param tag The name of the tag to match on. The special value "*" matches all tags. For XML,
      *        the tag parameter is case-sensitive, otherwise it depends on the case-sensitivity
      *        of the mark up language in use.
      * @return The FIRST element with the tag.
      */
     public Element getElementFromTag (String tag) {
         return (Element) myDoc.getElementsByTagName(tag).item(0);
     }
     
     /**
      * Returns a list of elements with the tag.
      * 
      * @param tag The name of the tag to match on. The special value "*" matches all tags. For XML,
      *        the tag parameter is case-sensitive, otherwise it depends on the case-sensitivity
      *        of the mark up language in use.
      * @return A list of elements with the tag.
      */
     public List<Element> getElementListByTagName (String tag) {
         List<Element> nodeList = new ArrayList<Element>();
         NodeList nodes = myDoc.getElementsByTagName(tag);
         for (int i = 0; i < nodes.getLength(); i++) {
             nodeList.add((Element) myDoc.getElementsByTagName(tag).item(i));
         }
         return nodeList;
     }
     
     /**
      * This method return the tag name of an element.
      * 
      * @param element The element containing a tag
      * @return the tag of the element as a string
      */
     public String getTagName (Element element) {
         return element.getTagName();
     }
     
     /**
      * This method returns the value of an element as a String.
      * 
      * @param element The element from which the content is being extracted.
      * @return A string stores in the element.
      */
     public String getContent (Element element) {
         return element.getTextContent();
     }
     
     /**
      * Gets the content of the FIRST element associated with the referent tag.
      * If the tag refers to a non-leaf node, it concatenates all the values from its children.
      * 
      * @param tag A string with the tag of the element.
      * @return the content(value) of the element.
      */
     public String getContentFromTag (String tag) {
         return getContent(getElementFromTag(tag));
     }
     
     /**
      * Creates a map with the tag (as a map key) and the content (as a map value) of all the
      * children elements of a particular node.
      * If the parent element does not contain children, this method returns an empty map.
      * 
      * @param parent The parent element node.
      * @return a map with the tag (as a map key) and the content (as a map value) of all the
      *         children elements of a particular node.
      */
     public Map<String, String> getMapFromParentElement (Element parent) {
         Map<String, String> map = new HashMap<String, String>();
         NodeList nodes = parent.getChildNodes();
         for (int i = 0; i < nodes.getLength(); i++) {
             if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                 Element node = (Element) nodes.item(i);
                 map.put(node.getTagName(), node.getTextContent());
             }
         }
         return map;
     }
     
     /**
      * Searches for all parent elements with the same tag and then generates a list containing maps
      * of their respective children.
      * Creates maps with the tag (as a map key) and the content (as a map value) of all the
      * children elements of a particular node.
      * The list preserves the top-down numerical order in which the tag is found in the XML
      * document.
      * 
      * @param parentsTag The tag of the parent elements to be searched.
      * @return a list of maps. The map contains the tag of the children elements as a key and the
      *         children content as a value. Map<key, value> = Map<childTag, childValue> =
      *         Map<String, String>;
      */
     public List<Map<String, String>> getMapListFromTag (String parentsTag) {
         List<Element> nodeList = getElementListByTagName(parentsTag);
         List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>(nodeList.size());
         for (int i = 0; i < nodeList.size(); i++) {
             listOfMaps.add(getMapFromParentElement(nodeList.get(i)));
         }
         return listOfMaps;
     }
     
     /*
      * Writing to file, will be replaced by the secretary program
      */
     /**
      * Translates a document into an XML formatted String.
      * 
      * @param doc document with its elements.
      * @return String with XML formatting
      * @throws TransformerException This exception is thrown when a Document object cannot be
      *         converted into a String
      */
     public String translateToXMLString (Document doc) throws TransformerException {
         DOMSource source = new DOMSource(doc);
         
         TransformerFactory factory = TransformerFactory.newInstance();
         Transformer transformer = factory.newTransformer();
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, XML_PARAM_YES);
         transformer.setOutputProperty(OutputKeys.INDENT, XML_PARAM_YES);
         
         StringWriter sw = new StringWriter();
         StreamResult result = new StreamResult(sw);
         transformer.transform(source, result);
         String xmlString = sw.toString();
         return xmlString;
     }
     
     /**
      * This <code>FileWriter</code> creates the document in an specific location,
      * form an String.
      * 
      * @param path The destination URL with the <filename>.XML
      */
     public void writeFile (String path) {
         FileWriter writer = null;
         try {
             writer = new FileWriter(path);
             writer.write(translateToXMLString(myDoc));
             writer.close();
         }
         catch (TransformerException e) {
             try {
                 writer.close();
             }
             catch (IOException e1) {
                 throw new RuntimeException("The writer could not be closed", e);
             }
             throw new RuntimeException("The document could not be converted into a string", e);
         }
         catch (IOException e) {
             throw new RuntimeException("File could not be written.", e);
         }
     }
 }
