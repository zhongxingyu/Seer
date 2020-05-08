 /*
  * This file is part of experimaestro.
  * Copyright (c) 2012 B. Piwowarski <benjamin@bpiwowar.net>
  *
  * experimaestro is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * experimaestro is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with experimaestro.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package sf.net.experimaestro.utils;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.FunctionObject;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.NativeObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.UniqueTag;
 import org.mozilla.javascript.Wrapper;
 import org.mozilla.javascript.xml.XMLObject;
 import org.mozilla.javascript.xmlimpl.XMLLibImpl;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import sf.net.experimaestro.exceptions.ExperimaestroRuntimeException;
 import sf.net.experimaestro.manager.Manager;
 import sf.net.experimaestro.manager.QName;
import sf.net.experimaestro.manager.js.JSBaseObject;
 import sf.net.experimaestro.manager.js.JSNamespaceBinder;
 import sf.net.experimaestro.manager.js.JSNode;
 import sf.net.experimaestro.manager.js.JSNodeList;
 import sf.net.experimaestro.utils.log.Logger;
 
 import javax.xml.namespace.NamespaceContext;
 
 import static java.lang.String.format;
 
 public class JSUtils {
     final static private Logger LOGGER = Logger.getLogger();
 
     /**
      * Get an object from a scriptable
      *
      * @param object
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> T get(Scriptable scope, String name, NativeObject object, boolean allowNull) {
         final Object _value = object.get(name, scope);
         if (_value == UniqueTag.NOT_FOUND)
             if (allowNull) return null;
             else throw new RuntimeException(format("Could not find property '%s'",
                     name));
         return (T) unwrap(_value);
     }
 
     public static <T> T get(Scriptable scope, String name, NativeObject object) {
         return get(scope, name, object, false);
     }
 
 
     /**
      * Unwrap a JavaScript object (if necessary)
      *
      * @param object
      * @return
      */
     public static Object unwrap(Object object) {
         if (object == null)
             return null;
 
         if (object instanceof Wrapper)
             object = ((Wrapper) object).unwrap();
 
         if (object == Scriptable.NOT_FOUND)
             return null;
         return object;
     }
 
     /**
      * Get an object from a scriptable
      *
      * @param object
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <T> T get(Scriptable scope, String name, NativeObject object,
                             T defaultValue) {
         final Object _value = object.get(name, scope);
         if (_value == UniqueTag.NOT_FOUND)
             return defaultValue;
         return (T) unwrap(_value);
     }
 
     /**
      * Transforms a DOM node to a E4X scriptable object
      *
      * @param node
      * @param cx
      * @param scope
      * @return
      */
     public static Object domToE4X(Node node, Context cx, Scriptable scope) {
         if (node == null) {
             LOGGER.info("XML is null");
             return Context.getUndefinedValue();
         }
         if (node instanceof Document)
             node = ((Document) node).getDocumentElement();
 
         if (node instanceof DocumentFragment) {
 
             final Document document = node.getOwnerDocument();
             Element root = document.createElement("root");
             document.appendChild(root);
 
             Scriptable xml = cx.newObject(scope, "XML", new Node[]{root});
 
             final Scriptable list = (Scriptable) xml.get("*", xml);
             int count = 0;
             for (Node child : XMLUtils.children(node)) {
                 list.put(count++, list, cx.newObject(scope, "XML", new Node[]{child}));
             }
             return list;
         }
 
         LOGGER.debug("XML is of type %s [%s]; %s", node.getClass(),
                 XMLUtils.toStringObject(node),
                 node.getUserData("org.mozilla.javascript.xmlimpl.XmlNode"));
         return cx.newObject(scope, "XML", new Node[]{node});
     }
 
     public static Document toDocument(Scriptable jsScope, Object returned) {
         Object object = toDOM(jsScope, returned);
         return Manager.wrap(object);
     }
 
     public static Object get(Scriptable scope, String name) {
         Scriptable _scope = scope;
         while (scope != null) {
             if (scope.has(name, _scope))
                 return scope.get(name, _scope);
             scope = scope.getParentScope();
         }
 
         return Scriptable.NOT_FOUND;
     }
 
 
     static public class OptionalDocument {
         Document document;
 
         Document get() {
             if (document == null)
                 document = XMLUtils.newDocument();
             return document;
         }
 
         public boolean has() {
             return document != null;
         }
 
         /**
          * Clone and adopt node if not already owned
          * @param node
          * @return
          */
         public Node cloneAndAdopt(Node node) {
             if (node.getOwnerDocument() != get())
                 return get().adoptNode(node.cloneNode(true));
             return node;
         }
     }
 
     /**
      * Transform objects into an XML node or a NodeLsit
      *
      * @param object
      * @return a {@linkplain Node} or a {@linkplain NodeList}
      */
     public static Object toDOM(Scriptable scope, Object object) {
         return toDOM(scope, object, new OptionalDocument());
     }
 
     public static Object toDOM(Scriptable scope, Object object, OptionalDocument document) {
        // Unwrap if needed (if this is not a JSBaseObject)
        if (object instanceof Wrapper && !(object instanceof JSBaseObject))
             object = ((Wrapper) object).unwrap();
 
         // It is already a DOM node
         if (object instanceof Node)
             return object;
 
         if (object instanceof XMLObject) {
             final XMLObject xmlObject = (XMLObject) object;
             String className = xmlObject.getClassName();
 
             if (className.equals("XMLList")) {
                 LOGGER.debug("Transforming from XMLList [%s]", object);
                 final Object[] ids = xmlObject.getIds();
                 if (ids.length == 1)
                     return toDOM(scope, xmlObject.get((Integer) ids[0], xmlObject), document);
 
                 Document doc = XMLUtils.newDocument();
                 DocumentFragment fragment = doc.createDocumentFragment();
 
                 for (int i = 0; i < ids.length; i++) {
                     Node node = (Node) toDOM(scope, xmlObject.get((Integer) ids[i], xmlObject), document);
                     if (node instanceof Document)
                         node = ((Document) node).getDocumentElement();
                     fragment.appendChild(doc.adoptNode(node));
                 }
 
                 return fragment;
             }
 
             // XML node
             if (className.equals("XML")) {
                 // FIXME: this strips all whitespaces!
                 Node node = XMLLibImpl.toDomNode(object);
                 LOGGER.debug("Got node from JavaScript [%s / %s] from [%s]",
                         node.getClass(), XMLUtils.toStringObject(node),
                         object.toString());
 
                 if (node instanceof Document)
                     node = ((Document) node).getDocumentElement();
 
                 node = document.get().adoptNode(node.cloneNode(true));
                 return node;
             }
 
 
             throw new RuntimeException(format(
                     "Not implemented: convert %s to XML", className));
 
         }
 
         if (object instanceof NativeArray) {
             NativeArray array = (NativeArray) object;
             ArrayNodeList list = new ArrayNodeList();
             for (Object x : array) {
                 Object o = toDOM(scope, x, document);
                 if (o instanceof Node)
                     list.add(document.cloneAndAdopt((Node) o));
                 else {
                     for (Node node : XMLUtils.iterable((NodeList) o)) {
                         list.add(document.cloneAndAdopt(node));
                     }
                 }
             }
             return list;
         }
 
         if (object instanceof NativeObject) {
             // JSON case: each key of the JS object is an XML element
             NativeObject json = (NativeObject) object;
             ArrayNodeList list = new ArrayNodeList();
 
             for (Object _id : json.getIds()) {
 
                 String jsQName = JSUtils.toString(_id);
 
                 if (jsQName.length() == 0) {
                     final Object seq = toDOM(scope, json.get(jsQName, json), document);
                     for (Node node : XMLUtils.iterable(seq)) {
                         if (node instanceof Document)
                             node = ((Document) node).getDocumentElement();
                         list.add(document.cloneAndAdopt(node));
                     }
                 } else if (jsQName.charAt(0) == '@') {
                     final QName qname = QName.parse(jsQName.substring(1), null, new JSNamespaceBinder(scope));
                     Attr attribute = document.get().createAttributeNS(qname.getNamespaceURI(), qname.getLocalPart());
                     StringBuilder sb = new StringBuilder();
                     for (Node node : XMLUtils.iterable(toDOM(scope, json.get(jsQName, json), document))) {
                         sb.append(node.getTextContent());
                     }
 
                     attribute.setTextContent(sb.toString());
                     list.add(attribute);
                 } else {
                     final QName qname = QName.parse(jsQName, null, new JSNamespaceBinder(scope));
                     Element element = qname.hasNamespace() ?
                             document.get().createElementNS(qname.getNamespaceURI(), qname.getLocalPart())
                             : document.get().createElement(qname.getLocalPart());
 
                     list.add(element);
 
                     final Object seq = toDOM(scope, json.get(jsQName, json), document);
                     for (Node node : XMLUtils.iterable(seq)) {
                         if (node instanceof Document)
                             node = ((Document) node).getDocumentElement();
                         node = document.get().adoptNode(node.cloneNode(true));
                         if (node.getNodeType() == Node.ATTRIBUTE_NODE)
                             element.setAttributeNodeNS((Attr) node);
                         else
                             element.appendChild(node);
                     }
                 }
             }
 
             return list;
         }
 
         if (object instanceof Double) {
             // Wrap a double
             final Double x = (Double) object;
             if (x.longValue() == x.doubleValue())
                 return document.get().createTextNode(Long.toString(x.longValue()));
             return document.get().createTextNode(Double.toString(x));
         }
 
         if (object instanceof Integer) {
             return document.get().createTextNode(Integer.toString((Integer) object));
         }
 
         if (object instanceof CharSequence) {
             return document.get().createTextNode(object.toString());
         }
 
 
         if (object instanceof UniqueTag)
             throw new ExperimaestroRuntimeException("Undefined cannot be converted to XML", object.getClass());
 
         if (object instanceof JSNode) {
             Node node = ((JSNode) object).getNode();
             if (document.has()) {
                 if (node instanceof Document)
                     node = ((Document) node).getDocumentElement();
                 return document.get().adoptNode(node);
             }
             return node;
         }
 
         if (object instanceof JSNodeList) {
             return ((JSNodeList) object).getList();
         }
 
         if (object instanceof Scriptable) {
             ((Scriptable) object).getDefaultValue(String.class);
         }
 
         // By default, convert to string
         return document.get().createTextNode(object.toString());
     }
 
     /**
      * Returns true if the object is XML
      *
      * @param input
      * @return
      */
     public static boolean isXML(Object input) {
         return input instanceof XMLObject;
     }
 
     /**
      * Converts a JavaScript object into an XML document
      *
      * @param srcObject The javascript object to convert
      * @param wrapName  If the object is not already a document and has more than one
      *                  element child (or zero), use this to wrap the elements
      * @return
      */
     public static Document toDocument(Scriptable scope, Object srcObject, QName wrapName) {
         final Object object = toDOM(scope, srcObject);
 
         if (object instanceof Document)
             return (Document) object;
 
         Document document = XMLUtils.newDocument();
 
         // Add a new root element if needed
         NodeList childNodes;
 
         if (!(object instanceof Node)) {
             childNodes = (NodeList) object;
         } else {
             final Node dom = (Node) object;
             if (dom.getNodeType() == Node.ELEMENT_NODE) {
                 childNodes = new NodeList() {
                     @Override
                     public Node item(int index) {
                         if (index == 0)
                             return dom;
                         throw new IndexOutOfBoundsException(Integer.toString(index) + " out of bounds");
                     }
 
                     @Override
                     public int getLength() {
                         return 1;
                     }
                 };
             } else
                 childNodes = dom.getChildNodes();
         }
 
         int elementCount = 0;
         for (int i = 0; i < childNodes.getLength(); i++)
             if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE)
                 elementCount++;
 
         Node root = document;
         if (elementCount != 1) {
             root = document.createElementNS(wrapName.getNamespaceURI(),
                     wrapName.getLocalPart());
             document.appendChild(root);
         }
 
         // Copy back in the DOM
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node node = childNodes.item(i);
             node = node.cloneNode(true);
             document.adoptNode(node);
             root.appendChild(node);
         }
 
         return document;
     }
 
     /**
      * Add a new javascript function to the scope
      *
      * @param aClass    The class where the function should be searched
      * @param scope     The scope where the function should be defined
      * @param fname     The function name
      * @param prototype The prototype or null. If null, uses the standard Context, Scriptablem Object[], Function prototype
      *                  used by Rhino JS
      * @throws NoSuchMethodException If
      */
     public static void addFunction(Class<?> aClass, Scriptable scope, final String fname,
                                    Class<?>[] prototype) throws NoSuchMethodException {
         final FunctionObject f = new FunctionObject(fname,
                 aClass.getMethod("js_" + fname, prototype), scope);
         ScriptableObject.putProperty(scope, fname, f);
     }
 
     public static String toString(Object object) {
         return Context.toString(unwrap(object));
     }
 
     public static int getInteger(Object object) {
         return (Integer) unwrap(object);
     }
 
     public static void addFunction(Scriptable scope, FunctionDefinition definition) throws NoSuchMethodException {
         addFunction(definition.clazz, scope, definition.name, definition.arguments);
     }
 
     /**
      * Convert a property into a boolean
      *
      * @param scope  The JS scope
      * @param object The JS object
      * @param name   The name of the property
      * @return <tt>false</tt> if the property does not exist.
      */
     public static boolean toBoolean(Scriptable scope, Scriptable object, String name) {
         if (!object.has(name, scope)) return false;
         Object value = object.get(name, scope);
         if (value instanceof Boolean)
             return (Boolean) value;
         return Boolean.parseBoolean(JSUtils.toString(value));
     }
 
     public static NamespaceContext getNamespaceContext(final Scriptable scope) {
         return new JSNamespaceContext(scope);
     }
 
     /**
      * Defines a JavaScript function by refering a class, a name and its parameters
      */
     static public class FunctionDefinition {
         Class<?> clazz;
         String name;
         Class<?>[] arguments;
 
         public FunctionDefinition(Class<?> clazz, String name, Class<?>... arguments) {
             this.clazz = clazz;
             this.name = name;
             if (arguments == null)
                 this.arguments = new Class[]{Context.class, Scriptable.class, Object[].class, Function.class};
             else
                 this.arguments = arguments;
         }
 
         public Class<?> getClazz() {
             return clazz;
         }
 
         public String getName() {
             return name;
         }
 
         public Class<?>[] getArguments() {
             return arguments;
         }
     }
 
 }
