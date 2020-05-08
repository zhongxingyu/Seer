 package xmlkit.jaxp;
 
 import static java.util.Collections.unmodifiableList;
 import static javax.xml.xpath.XPathConstants.BOOLEAN;
 import static xmlkit.Xml.evalXPath;
 import static xmlkit.Xml.evalXPathAsNumber;
 import static xmlkit.Xml.evalXPathAsString;
 import static xmlkit.Xml.serialize;
 
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.util.AbstractList;
 import java.util.List;
 
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import xmlkit.Xml;
 import xmlkit.XmlDocument;
 import xmlkit.XmlElement;
 import xmlkit.XmlNode;
 
 public class JAXPNode implements XmlNode {
 
   private final Node node;
 
   public JAXPNode(org.w3c.dom.Node node) {
     this.node = node;
   }
 
   public Node getUnderlyingNode() {
     return node;
   }
 
   public static XmlNode wrap(org.w3c.dom.Node n) {
 
     if (n == null)
       return null;
 
     if (n instanceof org.w3c.dom.Document) {
       return new JAXPDocument((org.w3c.dom.Document) n);
     }
 
     if (n instanceof org.w3c.dom.Element) {
       return new JAXPElement((org.w3c.dom.Element) n);
     }
 
     return new JAXPNode(n);
   }
 
   @Override
   public boolean isSame(XmlNode node) {
 
     if (node instanceof JAXPNode) {
       return ((JAXPNode) node).getUnderlyingNode().isSameNode(
           getUnderlyingNode());
     }
 
     return false;
   }
 
   @Override
   public XmlDocument getDocument() {
     Node nn = getUnderlyingNode();
     Document d = nn instanceof Document ? (Document) nn : nn.getOwnerDocument();
     return (XmlDocument) JAXPNode.wrap(d);
   }
 
   @Override
   public XmlElement getRootElement() {
 
     Node nn = getUnderlyingNode();
     Document d = nn instanceof Document ? (Document) nn : nn.getOwnerDocument();
     Element el = d.getDocumentElement();
     return (XmlElement) JAXPNode.wrap(el);
   }
 
   @Override
   public XmlNode getNext() {
     return JAXPNode.wrap(getUnderlyingNode().getNextSibling());
   }
 
   @Override
   public XmlNode getPrevious() {
     return JAXPNode.wrap(getUnderlyingNode().getPreviousSibling());
   }
 
   @Override
   public String getText() {
     return getUnderlyingNode().getTextContent();
   }
 
   @Override
   public void setText(String text) {
     getUnderlyingNode().setTextContent(text);
   }
 
   @Override
   public XmlNode getParent() {
    return JAXPNode.wrap(getUnderlyingNode().getParentNode());
   }
 
   protected List<XmlNode> adaptNamedNodeMap(final NamedNodeMap m) {
     return unmodifiableList(new AbstractList<XmlNode>() {
       @Override
       public XmlNode get(int index) {
         return JAXPNode.wrap(m.item(index));
       }
 
       @Override
       public int size() {
         return m.getLength();
       }
     });
   }
 
   protected List<XmlNode> adaptNodeList(final org.w3c.dom.NodeList nl) {
     return unmodifiableList(new AbstractList<XmlNode>() {
       @Override
       public XmlNode get(int index) {
         return JAXPNode.wrap(nl.item(index));
       }
 
       @Override
       public int size() {
         return nl.getLength();
       }
     });
   }
 
   @Override
   public List<XmlNode> query(String xPath) {
 
     try {
 
       Object result = Xml.evalXPath(xPath, getUnderlyingNode(),
           XPathConstants.NODESET);
 
       if (result instanceof org.w3c.dom.NodeList) {
         return adaptNodeList((org.w3c.dom.NodeList) result);
       }
     } catch (XPathExpressionException e) {
       throw new RuntimeException(e);
     }
 
     return null;
   }
 
   @Override
   public boolean queryBool(String xPath) {
     try {
       Boolean r;
       r = (Boolean) evalXPath(xPath, getUnderlyingNode(), BOOLEAN);
       if (r != null) {
         return r.booleanValue();
       }
     } catch (XPathExpressionException e) {
       throw new RuntimeException(e);
     }
 
     return false;
   }
 
   @Override
   public String queryText(String xPath) {
     try {
       return evalXPathAsString(xPath, getUnderlyingNode());
     } catch (XPathExpressionException e) {
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public Number queryNumber(String xPath) {
     try {
       return evalXPathAsNumber(xPath, getUnderlyingNode());
     } catch (XPathExpressionException e) {
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public XmlElement queryElement(String xPath) {
     try {
 
       Object o = evalXPath(xPath, getUnderlyingNode(), XPathConstants.NODE);
       if (o instanceof org.w3c.dom.Element) {
         return (XmlElement) JAXPNode.wrap((org.w3c.dom.Element) o);
       }
     } catch (XPathExpressionException e) {
       throw new RuntimeException(e);
     }
 
     return null;
   }
 
   @Override
   public XmlNode queryNode(String xPath) {
     try {
       Object o = evalXPath(xPath, getUnderlyingNode(), XPathConstants.NODE);
       if (o instanceof org.w3c.dom.Node) {
         return (XmlNode) JAXPNode.wrap((org.w3c.dom.Node) o);
       }
     } catch (XPathExpressionException e) {
       throw new RuntimeException(e);
     }
 
     return null;
   }
 
   @Override
   public String getNamespaceUri() {
     return getUnderlyingNode().getNamespaceURI();
   }
 
   @Override
   public String getNamespacePrefix() {
     return getUnderlyingNode().getPrefix();
   }
 
   @Override
   public String toXml() {
     return toXml(false);
   }
 
   @Override
   public String toXml(boolean xmlDecl) {
     StringWriter sw = new StringWriter();
     serialize(new DOMSource(getUnderlyingNode()), new StreamResult(sw), xmlDecl);
     return sw.toString();
   }
 
   @Override
   public OutputStream toXml(OutputStream os, boolean xmlDecl) {
     serialize(new DOMSource(getUnderlyingNode()), new StreamResult(os), xmlDecl);
     return os;
   }
 
   @Override
   public String getName() {
     return getUnderlyingNode().getNodeName();
   }
 
   @Override
   public boolean isAttribute() {
     return getUnderlyingNode().getNodeType() == Node.ATTRIBUTE_NODE;
   }
 
   @Override
   public boolean isText() {
     return getUnderlyingNode().getNodeType() == Node.TEXT_NODE;
   }
 
   @Override
   public JAXPNode copy() {
     return (JAXPNode) JAXPNode.wrap(getUnderlyingNode().cloneNode(true));
   }
   
   @Override
   public int hashCode() {
     return getUnderlyingNode().hashCode();
   }
 }
