 package xmlkit.jaxp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import xmlkit.XmlElement;
 import xmlkit.XmlNode;
 
 public class JAXPElement extends JAXPNode 
   implements XmlElement {
   
   protected JAXPElement(org.w3c.dom.Document dom) {
     super(dom);
   }
   
   public JAXPElement(org.w3c.dom.Element element) {
     super(element);
   }
   
 
   @Override
   public String getAttributeText(String name) {
     return ((Element)getUnderlyingNode()).getAttribute(name);
   }
 
   @Override
   public void setAttributeText(String name, String text) {
     ((Element)getUnderlyingNode()).setAttribute(name, text);
   }
 
   @Override
   public List<XmlNode> getChildNodes() {
     final NodeList nl = getUnderlyingNode().getChildNodes();
     List<XmlNode> list = new ArrayList<XmlNode>(10);
     for(int i=0; i < nl.getLength(); i++) {
       org.w3c.dom.Node n = nl.item(i);
       list.add((XmlNode)JAXPNode.wrap(n));
     }
     
     return list;
   }
 
   @Override
   public boolean hasChildNodes() {
     return getUnderlyingNode().hasChildNodes();
   }
 
   @Override
   public List<XmlElement> getChildElements() {
     final NodeList nl = getUnderlyingNode().getChildNodes();
     List<XmlElement> list = new ArrayList<XmlElement>(10);
     for(int i=0; i < nl.getLength(); i++) {
       org.w3c.dom.Node n = nl.item(i);
       if(n instanceof org.w3c.dom.Element) {
         list.add((XmlElement)JAXPNode.wrap(n));
       }
     }
     
     return list;
   }
 
   @Override
   public List<XmlNode> getAttributes() {
     NamedNodeMap map = getUnderlyingNode().getAttributes();
     return adaptNamedNodeMap(map);
   }
 
   @Override
   public void removeAttribute(String name) {
     ((Element)getUnderlyingNode()).removeAttribute(name);
   }
 
   @Override
   public XmlNode replaceChild(XmlNode old, XmlNode n) {
     
     if(old instanceof JAXPNode && n instanceof JAXPNode) {
       Node nn = ((JAXPNode)n).getUnderlyingNode();
       Node oold = ((JAXPNode)old).getUnderlyingNode();
       return wrap(getUnderlyingNode().replaceChild(nn, oold));
     }
     
     throw new IllegalArgumentException("old and new nodes must be instances of JAXPNode");
   }
 
   @Override
   public void appendText(String text) {
     Text t = getUnderlyingNode().getOwnerDocument().createTextNode(text);
     getUnderlyingNode().appendChild(t);
   }
 
   @Override
   public XmlNode appendChild(XmlNode n) {
     
     if(n instanceof JAXPNode) {
       Node nn = ((JAXPNode) n).getUnderlyingNode();
       return wrap(getUnderlyingNode().appendChild(nn));
     }
   
     throw new IllegalArgumentException("argument must be instance of JAXPNode");
   }
 
   @Override
   public XmlNode removeChild(XmlNode n) {
     
     if(n instanceof JAXPNode) {
       Node nn = ((JAXPNode) n).getUnderlyingNode();
       return wrap(getUnderlyingNode().removeChild(nn));
     }
     
     throw new IllegalArgumentException("argument must be instance of JAXPNode");
   }
 
   @Override
   public XmlElement appendElement(String name) {
     org.w3c.dom.Element e;
     e=getUnderlyingNode().getOwnerDocument().createElement(name);
     
     return (XmlElement)wrap(getUnderlyingNode().appendChild(e));
   }
 }
