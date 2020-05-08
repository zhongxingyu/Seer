 package nu.staldal.zt;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.xml.transform.Result;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import nu.staldal.xmltree.Element;
 import nu.staldal.xmltree.Node;
 import nu.staldal.lsp.wrapper.BooleanArrayCollection;
 import nu.staldal.lsp.wrapper.DoubleArrayCollection;
 import nu.staldal.lsp.wrapper.FloatArrayCollection;
 import nu.staldal.lsp.wrapper.IntArrayCollection;
 import nu.staldal.lsp.wrapper.LongArrayCollection;
 import nu.staldal.lsp.wrapper.ReadonlyBeanMap;
 import nu.staldal.lsp.wrapper.ResourceBundleTuple;
 import nu.staldal.lsp.wrapper.ShortArrayCollection;
 import nu.staldal.util.Environment;
 
 /**
  * Element with ZeroTemplate commands.
  *
  * @author Mikael Stldal
  */
 public class ZtElement extends Element {
 
     private static final long serialVersionUID = -8783416221544659625L;
     
     private final List<ZtAttr> attrs;
     private final String string;
     private final boolean stringIsLiteral;
     private final String map;
     private final String list;    
     private final boolean listIsOddEven;
     
     public ZtElement(Element element, List<ZtAttr> attrs, String string,
             boolean stringIsLiteral, String map, String list, boolean listIsOddEven) {
         super(element);
         this.attrs = attrs;
         this.string = string;
         this.stringIsLiteral = stringIsLiteral;
         this.map = map;
         this.list = list;
         this.listIsOddEven = listIsOddEven;
     }
 
     public List<ZtAttr> getAttrs() {
         return attrs;
     }
 
     public String getString() {
         return string;
     }
 
     public boolean isStringIsLiteral() {
         return stringIsLiteral;
     }
 
     public String getMap() {
         return map;
     }
 
     public String getList() {
         return list;
     }
     
     public boolean isListIsOffEven() {
         return listIsOddEven;
     }
 
     protected String getString(String key, Environment<String,Object> env) 
             throws SAXException {
         if (!env.containsKey(key)) {
             throw new SAXException("Parameter " + key + " not defined");
         }
         Object value = env.lookup(key);
         if (value == null) {
             return null;
         } else if (value instanceof String) {
             return (String)value;            
         } else if (value instanceof CharSequence) {            
             return value.toString(); // TODO handle CharSequence directly?
         } else if (value instanceof char[]) {
             return new String((char[])value);
         } else if (value instanceof Enum) {
             return value.toString();    
         } else {
             throw new SAXException("Parameter " + key + " must be string, but is: " + value.getClass().getName());                
         }        
     }
     
     protected String getStringNotNull(String key, Environment<String,Object> env) 
             throws SAXException {
         String value = getString(key, env);
         if (value == null) {
             throw new SAXException("Parameter " + key + " is null");                
         }
         return value;
     }
     
     /**
      * Unbound = false
      */
     protected boolean getBooleanNotNull(String key, Environment<String,Object> env) 
             throws SAXException {
         if (!env.containsKey(key)) {
             return false;
         }
         Object value = env.lookup(key);
         if (value == null) {
             throw new SAXException("Parameter " + key + " is null");
         } else if (value instanceof Boolean) {
             return (Boolean)value;
         } else if (value instanceof CharSequence) {
             return ((CharSequence)value).length() > 0;
         } else if (value instanceof char[]) {
             return ((char[])value).length > 0;
         } else if (value instanceof Collection<?>) {
             return ((Collection<?>)value).size() > 0;
         } else if (value instanceof int[]) {
             return ((int[])value).length > 0;
         } else if (value instanceof short[]) {
             return ((short[])value).length > 0;
         } else if (value instanceof long[]) {
             return ((long[])value).length > 0;
         } else if (value instanceof float[]) {
             return ((float[])value).length > 0;
         } else if (value instanceof double[]) {
             return ((double[])value).length > 0;
         } else if (value instanceof boolean[]) {
             return ((boolean[])value).length > 0;
         } else if (value instanceof Object[]) {
             return ((Object[])value).length > 0;
         } else {
             throw new SAXException("Parameter " + key + " must be boolean, but is: " + value.getClass().getName());                
         }        
     }
     
     protected Map<String,Object> getMapNotNull(String key, Environment<String,Object> env) 
             throws SAXException {
         if (!env.containsKey(key)) {
             throw new SAXException("Parameter " + key + " not defined");
         }
         Object value = env.lookup(key);
         if (value == null) {
             throw new SAXException("Parameter " + key + " is null");
         } else if (value instanceof Map) {
             return (Map<String,Object>)value;
         } else if (value instanceof ResourceBundle) {
             return new ResourceBundleTuple((ResourceBundle)value);        
         } else if (value instanceof CharSequence
                    || value.getClass().isArray()
                     || value instanceof Enum
                     || value instanceof Boolean) {
             throw new SAXException("Parameter " + key + " must be map, but is: " + value.getClass().getName());                            
         } else {
             return new ReadonlyBeanMap(value);
         }        
     }
     
    protected Collection<?> getListNotNull(String key, Environment<String,Object> env)
             throws SAXException {
         if (!env.containsKey(key)) {
             throw new SAXException("Parameter " + key + " not defined");
         }
         Object value = env.lookup(key);
         if (value == null) {
             throw new SAXException("Parameter " + key + " is null");
        } else if (value instanceof Collection) {            
            return (Collection<?>)value;
         } else if (value instanceof int[]) {
             return new IntArrayCollection((int[])value);
         } else if (value instanceof short[]) {
             return new ShortArrayCollection((short[])value);
         } else if (value instanceof long[]) {
             return new LongArrayCollection((long[])value);
         } else if (value instanceof float[]) {
             return new FloatArrayCollection((float[])value);
         } else if (value instanceof double[]) {
             return new DoubleArrayCollection((double[])value);
         } else if (value instanceof boolean[]) {
             return new BooleanArrayCollection((boolean[])value);
         } else if (value instanceof Object[]) {
             Object[] arr = (Object[])value;
             if (arr.length == 0)
                 return Collections.emptyList();
             else
                 return Arrays.asList(arr);
         } else {
             throw new SAXException("Parameter " + key + " must be list, but is: " + value.getClass().getName());                
         }        
     }
     
     public void toSAX(ContentHandler sax, Environment<String, Object> env) 
             throws SAXException {
         if (map != null) {
             Map<String,Object> value = getMapNotNull(map, env);
             env.pushFrame(value);
         }
         
         if (list != null) {
            Collection<?> value = getListNotNull(list, env);
             int index = 1;
             for (Object item : value) {
                 if (item instanceof CharSequence
                         || item instanceof Enum
                         || item instanceof Boolean
                        || item instanceof Collection<?>
                         || item.getClass().isArray()) {
                     env.pushFrame();                    
                     env.bind("", item);
                 } else if (item instanceof Map) {
                     env.pushFrame((Map<String,Object>)item);
                 } else if (item instanceof ResourceBundle) {
                     env.pushFrame(new ResourceBundleTuple((ResourceBundle)item));
                 } else {
                     env.pushFrame(new ReadonlyBeanMap(item));                    
                 }
                 String extraClass = null;
                 if (listIsOddEven) {
                     extraClass = (index % 2 == 0) ? "even" : "odd";
                 }
                 _toSAX(sax, env, extraClass);                
                 env.popFrame();
                 index++;
             }
         } else {
             _toSAX(sax, env, null);
         }
         
         if (map != null) {
             env.popFrame();
         }
     }
     
     private void _toSAX(ContentHandler sax, Environment<String, Object> env, String extraClass) 
             throws SAXException {
         if (extraClass != null || !attrs.isEmpty()) {
             Map<String,String> attrCopy = new HashMap<String,String>(getAttributes());
             
             if (extraClass != null) {
                 String cls = attrCopy.get("class");
                 if (cls != null) {
                     attrCopy.put("class", cls+' '+extraClass);
                 } else {
                     attrCopy.put("class", extraClass);
                 }
             }
             
             for (ZtAttr ztAttr : attrs) {
                 String value = getString(ztAttr.getString(), env);
                 if (value == null) {
                     attrCopy.remove(ztAttr.getAttributeName());
                 } else {
                     attrCopy.put(ztAttr.getAttributeName(), value);
                 }
             }
             
             outputStartElement(sax, attrCopy);                        
         } else {
             outputStartElement(sax);            
         }
         
         if (string != null) {
             String value = getStringNotNull(string, env);
             if (stringIsLiteral) {
                 sax.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
             }
             sax.characters(value.toCharArray(), 0, value.length());
             if (stringIsLiteral) {
                 sax.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
             }
         } else {
             for (Node node : this) {
                 if (node instanceof ZtElement) {
                     ((ZtElement)node).toSAX(sax, env);
                 } else {
                     node.toSAX(sax);
                 }
             }
         }
         
         outputEndElement(sax);
     }
     
 }
