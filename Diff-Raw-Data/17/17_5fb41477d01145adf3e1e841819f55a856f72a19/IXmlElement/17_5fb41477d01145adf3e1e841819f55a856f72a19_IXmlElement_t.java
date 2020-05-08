 package org.ubimix.model.xml;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.ubimix.model.IHasValueMap;
 import org.ubimix.model.IValueFactory;
 import org.ubimix.model.conversion.Converter;
 
 /**
  * @author kotelnikov
  */
 public interface IXmlElement extends IXmlNode, Iterable<IXmlNode>, IHasValueMap {
 
     /**
      * Creates and returns {@link IXmlElement} instance wrapping the specified
      * java value.
      */
     public static final IValueFactory<IXmlElement> FACTORY = new IValueFactory<IXmlElement>() {

        private IXmlFactory fXmlFactory = XmlFactory.getInstance();
 
         @Override
         public IXmlElement newValue(Object object) {
             IXmlElement result = null;
             if (!(object instanceof IXmlElement)) {
                 if (object instanceof IHasValueMap) {
                     IHasValueMap m = (IHasValueMap) object;
                     result = Converter.convertJsonToXml(m, fXmlFactory);
                 } else if (object instanceof Map<?, ?>) {
                    // FIXME : remove this code
                     Map<Object, Object> map = (Map<Object, Object>) object;
                     result = new XmlElement(fXmlFactory, null, map);
                 } else {
                     result = Converter.convertJavaToXml(object, fXmlFactory);
                 }
 
             } else {
                 result = (IXmlElement) object;
             }
             return result;
         }
     };
 
     String NS = "xmlns";
 
     String NS_PREFIX = "xmlns:";
 
     void addChild(IXmlNode node);
 
     boolean addChild(IXmlNode node, int pos);
 
     void addChildren(Iterable<? extends IXmlNode> children);
 
     IXmlElement addText(String str);
 
     String getAttribute(String key);
 
     Set<String> getAttributeNames();
 
     Map<String, String> getAttributes();
 
     IXmlNode getChild(int pos);
 
     int getChildCount();
 
     int getChildPosition(IXmlNode node);
 
     List<IXmlNode> getChildren();
 
     /**
      * Returns a map with prefixes and the corresponding namespaces defined
      * directly in this element. The default namespace corresponds to an empty
      * prefix ("").
      * 
      * @return a map with prefixes and the corresponding namespaces
      */
     Map<String, String> getDeclaredNamespaces();
 
     String getName();
 
     IXmlElement removeAttribute(String key);
 
     boolean removeChild(int pos);
 
     boolean removeChild(IXmlNode child);
 
     void removeChildren();
 
     IXmlElement setAttribute(String key, String value);
 
     IXmlElement setAttributes(Map<String, String> attributes);
 
     IXmlElement setChildren(Iterable<IXmlNode> children);
 
     IXmlElement setChildren(IXmlNode... children);
 
     IXmlElement setName(String tagName);
 
     IXmlElement setNamespace(String nsPrefix, String nsUrl);
 
     IXmlElement setNamespaces(Map<String, String> attributes);
 
     IXmlElement setText(String str);
 
 }
