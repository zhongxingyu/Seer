 /**
  * 
  */
 package org.webreformatter.commons.xml;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.InputSource;
 
 /**
  * This is an utility class simplifying access and manipulations with XML nodes.
  * 
  * @author kotelnikov
  */
 public class XmlWrapper {
 
     /**
      * A composite implementation of the {@link NamespaceContext} interface
      * "merging" namespaces from all registered {@link NamespaceContext}
      * instances.
      * 
      * @author kotelnikov
      */
     public static class CompositeNamespaceContext implements NamespaceContext {
 
         /**
          * The internal list of {@link NamespaceContext} instances used to
          * retrieve real namespaces and prefixes.
          */
         private List<NamespaceContext> fContexts = new ArrayList<NamespaceContext>();
 
         /**
          * This constructor adds the given {@link NamespaceContext} instances to
          * the internal list.
          * 
          * @param contexts an array of namespace contexts to add in the internal
          *        list
          */
         public CompositeNamespaceContext(NamespaceContext... contexts) {
             addContexts(contexts);
         }
 
         /**
          * This method adds the given {@link NamespaceContext} instance to the
          * internal list.
          * 
          * @param context the context to add in the internal list
          */
         public void addContext(NamespaceContext context) {
             synchronized (fContexts) {
                 if (!fContexts.contains(context)) {
                     fContexts.add(context);
                 }
             }
         }
 
         /**
          * This method adds the given {@link NamespaceContext} instances to the
          * internal list.
          * 
          * @param contexts an array of namespace contexts to add in the internal
          *        list
          */
         public void addContexts(NamespaceContext... contexts) {
             synchronized (fContexts) {
                 for (NamespaceContext context : contexts) {
                     fContexts.add(context);
                 }
             }
         }
 
         /**
          * Copies all contexts from the given composite in this object
          * 
          * @param namespaceContext
          */
         public void addContextsFrom(CompositeNamespaceContext namespaceContext) {
             synchronized (fContexts) {
                 if (namespaceContext != null) {
                     fContexts.addAll(namespaceContext.fContexts);
                 }
             }
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
          */
         public String getNamespaceURI(String prefix) {
             String result = null;
             synchronized (fContexts) {
                 for (NamespaceContext context : fContexts) {
                     result = context.getNamespaceURI(prefix);
                     if (result != null) {
                         break;
                     }
                 }
             }
             return result;
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
          */
         public String getPrefix(String namespaceURI) {
             String result = null;
             synchronized (fContexts) {
                 for (NamespaceContext context : fContexts) {
                     result = context.getPrefix(namespaceURI);
                     if (result != null) {
                         break;
                     }
                 }
             }
             return result;
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
          */
         public Iterator<String> getPrefixes(String namespaceURI) {
             Set<String> prefixes = new LinkedHashSet<String>();
             synchronized (fContexts) {
                 for (NamespaceContext context : fContexts) {
                     @SuppressWarnings("unchecked")
                     Iterator<String> iterator = context
                         .getPrefixes(namespaceURI);
                     if (iterator != null) {
                         while (iterator.hasNext()) {
                             String prefix = iterator.next();
                             prefixes.add(prefix);
                         }
                     }
                 }
             }
             return prefixes.iterator();
         }
 
         /**
          * Removes the given context from the internal list of contexts.
          * 
          * @param context the context to remove from the list
          */
         public void removeContext(NamespaceContext context) {
             synchronized (fContexts) {
                 fContexts.remove(context);
             }
         }
 
     }
 
     /**
      * This {@link NamespaceContext} implementation uses an XML element to
      * retrieve namespaces and the corresponding prefixes. The
      * {@link Node#lookupNamespaceURI(String)} and
      * {@link Node#lookupPrefix(String)} methods are used to implement this
      * class.
      * 
      * @author kotelnikov
      */
     public static class ElementBasedNamespaceContext
         implements
         NamespaceContext {
 
         /**
          * The internal element used as a source of namespaces and the
          * corresponding prefixes.
          */
         private Node fElement;
 
         /**
          * This constructor initializes the internal element used as a source of
          * namespaces and prefixes
          * 
          * @param element the element to set
          */
         public ElementBasedNamespaceContext(Node element) {
             fElement = element;
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
          */
         public String getNamespaceURI(String prefix) {
             String namespaceUri = null;
             if (prefix == null || XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
                 String elementPrefix = fElement.getPrefix();
                 if (elementPrefix == null) {
                     namespaceUri = fElement.getNamespaceURI();
                 }
             }
             if (namespaceUri == null) {
                 namespaceUri = fElement.lookupNamespaceURI(prefix);
             }
             return namespaceUri;
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
          */
         public String getPrefix(String namespaceURI) {
             if (fElement.isDefaultNamespace(namespaceURI)) {
                 return XMLConstants.DEFAULT_NS_PREFIX;
             }
             String prefix = fElement.lookupPrefix(namespaceURI);
             return prefix;
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
          */
         public Iterator<String> getPrefixes(String namespaceURI) {
             Set<String> prefixes = new HashSet<String>();
             Node e = fElement;
             while (e != null) {
                 String prefix = e.lookupPrefix(namespaceURI);
                 if (prefix != null) {
                     prefixes.add(prefix);
                 }
                 Node parent = e.getParentNode();
                 e = ((parent instanceof Node) ? parent : null);
             }
             return prefixes.iterator();
         }
 
     }
 
     /**
      * This implementation of the {@link NamespaceContext} explicitly registers
      * namespaces and the corresponding prefixes.
      * 
      * @author kotelnikov
      */
     public static class SimpleNamespaceContext implements NamespaceContext {
 
         /**
          * This dictionary maps namespaces to the corresponding prefixes.
          */
         private Map<String, String> fNamespaceToPrefix = new HashMap<String, String>();
 
         /**
          * This dictionary maps prefixes to the corresponding full namespace
          * URIs.
          */
         private Map<String, String> fPrefixToNamespace = new HashMap<String, String>();
 
         /**
          * This constructor initializes internal dictionaries with prefixes and
          * the corresponding namespace URIs using the given map.
          * 
          * @param namespaceMapping this dictionary maps namespace prefixes to
          *        the corresponding full namespace URIs.
          */
         public SimpleNamespaceContext(Map<String, String> namespaceMapping) {
             setNamespaceMapping(namespaceMapping);
         }
 
         /**
          * This constructor initializes internal dictionaries with prefixes and
          * the corresponding namespace URIs using the specified list of prefixes
          * and URIs.
          * 
          * <pre>
          * Example:
          * SimpleNamespaceContext context = new SimpleNamespaceContext(
          *      "html", "http://www.w3.org/1999/xhtml",
          *      "atom", "http://www.w3.org/2005/Atom");
          * </pre>
          * 
          * @param namespaceMapping this array contains prefixes in odd positions
          *        and the corresponding namespace URIs in even positions.
          */
         public SimpleNamespaceContext(String... namespaceMapping) {
             setNamespaceMapping(namespaceMapping);
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
          */
         public String getNamespaceURI(String prefix) {
             return fPrefixToNamespace.get(prefix);
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
          */
         public String getPrefix(String namespaceURI) {
             return fNamespaceToPrefix.get(namespaceURI);
         }
 
         /**
          * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
          */
         public Iterator<?> getPrefixes(String namespaceURI) {
             List<String> list = new ArrayList<String>(
                 fPrefixToNamespace.keySet());
             return list.iterator();
         }
 
         /**
          * This is an internal utility method used to removes values from the
          * direct and inverted map by the key specified for the direct map.
          * 
          * @param first the direct map
          * @param second the dictionary containing key/value pairs inverted
          *        relatively to the first map
          * @param key the key for the direct map; this key is used to remove the
          *        value from the first map and the removed value is used to
          *        remove values from the inverted map
          */
         private void remove(
             Map<String, String> first,
             Map<String, String> second,
             String key) {
             String value = first.remove(key);
             if (value != null) {
                 second.remove(value);
             }
         }
 
         /**
          * Sets a new map defining prefixes and the corresponding namespaces.
          * This map is used for building XPath expressions - all namespace
          * prefixes referenced in XPath expression will be resolved using this
          * map.
          * 
          * @param mapping this map contains prefixes and the corresponding
          *        namespaces
          */
         public void setNamespaceMapping(Map<String, String> mapping) {
             fPrefixToNamespace.clear();
             fNamespaceToPrefix.clear();
             fPrefixToNamespace.putAll(mapping);
             for (Map.Entry<String, String> entry : fPrefixToNamespace
                 .entrySet()) {
                 String prefix = entry.getKey();
                 String namespace = entry.getValue();
                 fNamespaceToPrefix.put(namespace, prefix);
             }
         }
 
         /**
          * Sets a new map defining prefixes and the corresponding namespaces.
          * This array contains prefixes on the pair places and the corresponding
          * namespace URIs on the impair places. This mapping is used for
          * building XPath expressions - all namespace prefixes referenced in
          * XPath expression will be resolved using these values.
          * 
          * @param mapping this array contains prefixes and the corresponding
          *        namespace URIs
          */
         public void setNamespaceMapping(String... mapping) {
             fPrefixToNamespace.clear();
             fNamespaceToPrefix.clear();
             for (int i = 0; i < mapping.length;) {
                 String prefix = mapping[i++];
                 String namespaceUri = i < mapping.length ? mapping[i++] : "";
                 fPrefixToNamespace.put(prefix, namespaceUri);
                 fNamespaceToPrefix.put(namespaceUri, prefix);
             }
         }
 
         /**
          * Defines a new mapping between the specified prefix and the
          * corresponding namespace URI.
          * 
          * @param prefix the prefix to add
          * @param namespace the namespace URI corresponding to the prefix
          */
         public void setNamespacePrefix(String prefix, String namespace) {
             remove(fPrefixToNamespace, fNamespaceToPrefix, prefix);
             remove(fNamespaceToPrefix, fPrefixToNamespace, namespace);
             fPrefixToNamespace.put(prefix, namespace);
             fNamespaceToPrefix.put(namespace, prefix);
         }
     }
 
     /**
      * A basic implementation of the {@link URIResolver} interface using a base
      * URL to resolve references.
      * 
      * @author kotelnikov
      */
     public static class URLBasedURIResolver implements URIResolver {
 
         /**
          * The base URL used to resolve all other URIs.
          */
         private URL fBaseURL;
 
         /**
          * This constructor initializes internal fields; it sets the specified
          * base URL.
          * 
          * @param baseURL the base URL used to resolve all references.
          */
         public URLBasedURIResolver(URL baseURL) {
             fBaseURL = baseURL;
         }
 
         /**
          * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
          *      java.lang.String)
          */
         public Source resolve(String href, String base)
             throws TransformerException {
             try {
                 URL url = new URL(fBaseURL, href);
                 InputStream input = url.openStream();
                 return new StreamSource(input);
             } catch (Exception e) {
                 return null;
             }
         }
     }
 
     /**
      * Instances of this type are used to provide configurations to
      * {@link XmlWrapper} objects. They provide access to namespaces (used to
      * build XPath expressions) and to a {@link URIResolver} instance (used to
      * perform XSL transformations). Note that this object is immutable and
      * should be created using a builder instance
      * {@link XmlWrapper.XmlContext.Builder}.
      * <p>
      * Example of usage of this class:
      * </p>
      * 
      * <pre>
      * URIResolver myUriResolver = ...
      * NamespaceContext namespaceContext = ... 
      * XmlContext context = XmlContext.
      *      builder().
      *      setNamespaceContext(namespaceContext).
      *      setURIResolver(myUriResolver).
      *      build();
      * XmlWrapper wrapper = new XmlWrapper(element, context);
      * ...
      * </pre>
      * 
      * @author kotelnikov
      */
     public static class XmlContext {
 
         /**
          * Instances of this type are used to build/configure
          * {@link XmlWrapper.XmlContext} objects.
          * 
          * @author kotelnikov
          */
         public static class Builder extends XmlContext {
 
             /**
              * The default constructor.
              */
             public Builder() {
             }
 
             /**
              * This constructor initializes the namespace context associated
              * with this builder.
              * 
              * @param namespaceContext the namespace context to set.
              */
             public Builder(NamespaceContext namespaceContext) {
                 setNamespaceContext(namespaceContext);
             }
 
             /**
              * This method builds and sets a new namespace context using the
              * given array of namespace prefixes and namespace URIs.
              * 
              * @param namespaces an array of prefixes and the corresponding
              *        namespace URIs.
              */
             public Builder(String... namespaces) {
                 setNamespaceContext(new SimpleNamespaceContext(namespaces));
             }
 
             /**
              * Creates and returns an immutable {@link XmlWrapper.XmlContext}
              * instance.
              * 
              * @return a newly created {@link XmlWrapper.XmlContext} instance
              */
             public XmlContext build() {
                 return new XmlContext(this);
             }
 
             @Override
             protected XmlContext getXmlContext() {
                 return build();
             }
 
             /**
              * Sets a new {@link NamespaceContext} instance.
              * 
              * @param namespaceContext a new namespace context to set
              * @return reference to this object
              */
             public Builder setNamespaceContext(NamespaceContext namespaceContext) {
                 CompositeNamespaceContext context = getNamespaceContext();
                 context.addContext(namespaceContext);
                 return this;
             }
 
             /**
              * Sets a new {@link URIResolver} instance.
              * 
              * @param resolver an {@link URIResolver} instance to set
              * @return reference to this object
              */
             public Builder setURIResolver(URIResolver resolver) {
                 fResolver = resolver;
                 return this;
             }
 
         }
 
         /**
          * Returns a new XML context({@link XmlWrapper.XmlContext}) object.
          * 
          * @param namespaces a namespace context
          * @return a newly created {@link XmlWrapper.XmlContext.Builder}
          *         instance used to configure all internal fields.
          */
         public static XmlContext build(NamespaceContext namespaces) {
             return builder(namespaces).build();
         }
 
         /**
          * Returns a new XML context ({@link XmlWrapper.XmlContext}) object.
          * 
          * @param namespaces an array of initial prefixes and the corresponding
          *        namespace URLs; prefixes are in the odd positions of this
          *        array and the corresponding namespaces are in even positions
          * @return a newly created {@link XmlWrapper.XmlContext.Builder}
          *         instance used to configure all internal fields.
          */
         public static XmlContext build(String... namespaces) {
             return builder(namespaces).build();
         }
 
         /**
          * Returns a new builder used to configure and create new
          * {@link XmlWrapper.XmlContext} objects.
          * 
          * @param namespaces a namespace context
          * @return a newly created {@link XmlWrapper.XmlContext.Builder}
          *         instance used to configure all internal fields.
          */
         public static XmlContext.Builder builder(NamespaceContext namespaces) {
             return new Builder(namespaces);
         }
 
         /**
          * Returns a new builder used to configure and create new
          * {@link XmlWrapper.XmlContext} objects.
          * 
          * @param namespaces an array of initial prefixes and the corresponding
          *        namespace URLs; prefixes are in the odd positions of this
          *        array and the corresponding namespaces are in even positions
          * @return a newly created {@link XmlWrapper.XmlContext.Builder}
          *         instance used to configure all internal fields.
          */
         public static XmlContext.Builder builder(String... namespaces) {
             return new Builder(namespaces);
         }
 
         /**
          * Namespace context; this value is used by the {@link XmlWrapper} to
          * compile XPath expressions.
          */
         protected CompositeNamespaceContext fNamespaceContext1;
 
         /**
          * This URI resolver is used by all methods in {@link XmlWrapper}
          * dealing with XSL transformations.
          */
         protected URIResolver fResolver;
 
         private Map<String, XPathExpression> fXPathCache = new HashMap<String, XPathExpression>();
 
         /**
          * The default constructor. Normally it will be never called directly;
          * only by subclasses and by the {@link XmlWrapper.XmlContext.Builder}.
          */
         protected XmlContext() {
         }
 
         /**
          * This constructor copies all internal fields from the given object; by
          * default it should be a {@link XmlWrapper.XmlContext.Builder}
          * instance.
          * 
          * @param context the context used as a source of all fields
          */
         public XmlContext(XmlContext context) {
             CompositeNamespaceContext namespaceContext = getNamespaceContext();
             namespaceContext
                 .addContextsFrom(context.getNamespaceContext(false));
             fResolver = context.fResolver;
         }
 
         /**
          * Returns the namespace context handled by this object.
          * 
          * @return the namespace context handled by this object
          */
         public CompositeNamespaceContext getNamespaceContext() {
             return getNamespaceContext(true);
         }
 
         /**
          * Returns the namespace context handled by this object.
          * 
          * @return the namespace context handled by this object
          */
         protected CompositeNamespaceContext getNamespaceContext(boolean create) {
             if (fNamespaceContext1 == null && create) {
                 fNamespaceContext1 = new CompositeNamespaceContext();
             }
             return fNamespaceContext1;
         }
 
         /**
          * Returns an {@link URIResolver} instance managed by this object.
          * 
          * @return an {@link URIResolver} instance managed by this object
          */
         public URIResolver getURIResolver() {
             return fResolver;
         }
 
         /**
          * Returns a reference to an {@link XmlWrapper.XmlContext} instance used
          * to build {@link XmlWrapper} objects. This value is used by the
          * {@link #wrap(Node, Class)} method.
          * 
          * @return a reference to an {@link XmlWrapper.XmlContext} instance used
          *         to build {@link XmlWrapper} objects
          */
         protected XmlContext getXmlContext() {
             return this;
         }
 
         /**
          * This method parses the given XPath expression and returns a compiled
          * {@link XPathExpression} object.
          * 
          * @param xpath an XPath expression to compile
          * @return a compiled XPath expression (a {@link XPathExpression}
          *         instance).
          * @throws XmlException
          */
         protected XPathExpression getXpath(String xpath) throws XmlException {
             synchronized (fXPathCache) {
                 try {
                     XPathExpression expr = fXPathCache.get(xpath);
                     if (expr == null) {
                         XPath compiledXpath = XPATH_FACTORY.newXPath();
                         NamespaceContext namespaceContext = getNamespaceContext();
                         if (namespaceContext != null) {
                             compiledXpath.setNamespaceContext(namespaceContext);
                         }
                         expr = compiledXpath.compile(xpath);
                         fXPathCache.put(xpath, expr);
                     }
                     return expr;
                 } catch (Throwable t) {
                     throw handleError("Can not parse an XPath expression", t);
                 }
             }
         }
 
         /**
          * Creates and returns a new XML document with the specified root
          * element.
          * 
          * @param elementName a qualified name of the root element for a new
          *        document
          * @return a newly created wrapper with the specified root element
          * @throws XmlException
          */
         public XmlWrapper newXML(String elementName) throws XmlException {
             return newXML(elementName, XmlWrapper.class);
         }
 
         /**
          * Creates a new XML document with the specified root element and
          * returns a wrapper of the required type for this document.
          * 
          * @param elementName a qualified name of the root element for a new
          *        document
          * @param type the type of the wrapper to use for the newly created
          *        document
          * @return a newly created wrapper with the specified root element
          * @throws XmlException
          */
         public <T extends XmlWrapper> T newXML(String elementName, Class<T> type)
             throws XmlException {
             Document doc = newDocument();
             NamespaceContext namespaceContext = getNamespaceContext();
             Element element = newElement(namespaceContext, doc, elementName);
             doc.appendChild(element);
             T result = wrap(element, type);
             return result;
         }
 
         /**
          * Parses the given stream and returns an {@link XmlWrapper} instance of
          * the specified class managing the resulting parsed document.
          * 
          * @param <T> the type of the wrapper
          * @param type the class defining the wrapper
          * @param reader the reader providing access to the serialized XML
          *        document
          * @return an {@link XmlWrapper} instance managing the resulting XML
          *         document
          * @throws XmlException
          * @throws IOException
          */
         public <T extends XmlWrapper> T readXML(Class<T> type, Reader reader)
             throws XmlException,
             IOException {
             Document doc = XmlWrapper.readXML(reader);
             T wrapper = wrap(doc, type);
             wrapper.toString();
             return wrap(doc, type);
         }
 
         /**
          * Parses the given serialized XML document and returns an
          * {@link XmlWrapper} instance managing the resulting DOM object.
          * 
          * @param <T> the type of the wrapper
          * @param type the class defining the wrapper
          * @param xml the reader providing access to the serialized XML document
          * @return an {@link XmlWrapper} instance managing the resulting XML
          *         document
          * @throws XmlException
          * @throws IOException
          */
         public <T extends XmlWrapper> T readXML(Class<T> type, String xml)
             throws XmlException,
             IOException {
             StringReader reader = new StringReader(xml);
             return readXML(type, reader);
         }
 
         /**
          * Parses the given stream and returns an {@link XmlWrapper} instance
          * managing the resulting parsed document.
          * 
          * @param reader the reader providing access to the serialized XML
          *        document
          * @return an {@link XmlWrapper} instance managing the resulting XML
          *         document
          * @throws XmlException
          * @throws IOException
          */
         public XmlWrapper readXML(Reader reader)
             throws XmlException,
             IOException {
             return readXML(XmlWrapper.class, reader);
         }
 
         /**
          * Parses the given serialized XML document and returns an
          * {@link XmlWrapper} instance managing the resulting DOM object.
          * 
          * @param xml the reader providing access to the serialized XML document
          * @return an {@link XmlWrapper} instance managing the resulting XML
          *         document
          * @throws XmlException
          * @throws IOException
          */
         public XmlWrapper readXML(String xml) throws XmlException, IOException {
             return readXML(XmlWrapper.class, xml);
         }
 
         /**
          * Creates and returns a new {@link XmlWrapper} instance.
          * 
          * @param doc the document to wrap
          * @return a newly created {@link XmlWrapper} instance
          * @throws XmlException
          */
         public XmlWrapper wrap(Document doc) throws XmlException {
             return wrap(doc, XmlWrapper.class);
         }
 
         /**
          * Creates and returns a new {@link XmlWrapper} instance wrapping the
          * specified XML element.
          * 
          * @param node the node to wrap
          * @return a newly created {@link XmlWrapper} instance
          * @throws XmlException
          */
         public XmlWrapper wrap(Node node) throws XmlException {
             return wrap(node, XmlWrapper.class);
         }
 
         /**
          * Creates and returns a new instance of the specified type wrapping the
          * given XML node.
          * 
          * @param <T> the type of the wrapper; should be a sub-class of the
          *        {@link XmlWrapper} type
          * @param node the node to wrap
          * @param type the type of the wrapper
          * @return a newly created instance of the specified type
          * @throws XmlException
          */
         public <T extends XmlWrapper> T wrap(Node node, Class<T> type)
             throws XmlException {
             try {
                 Constructor<T> constructor = type.getConstructor(
                     Node.class,
                     XmlContext.class);
                 XmlContext context = getXmlContext();
                 T instance = constructor.newInstance(node, context);
                 return instance;
             } catch (Throwable t) {
                 throw handleError("Can not instantiate an XML wrapper", t);
             }
         }
 
     }
 
     /**
      * The logger instance used to report all errors produced by this class
      */
     private static Logger log = Logger.getLogger(XmlWrapper.class.getName());
 
     /**
      * This instance is used to create new templates for XSL transformations.
      */
     private static TransformerFactory TRANSFORMER_FACTORY = TransformerFactory
         .newInstance();
 
     /**
      * This factory is used to compile XPath expressions.
      */
     private static XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
 
     /**
      * Appends the given node to the target element.
      * 
      * @param target the target element where the given node should be appended
      * @param node the node to append
      */
     protected static void append(Node target, Node node) {
         Document targetDoc = getDocument(target);
         targetDoc.adoptNode(node);
         target.appendChild(node);
     }
 
     /**
      * Appends all children of the specified node to the given container.
      * 
      * @param container the container where all sub-nodes should be added
      * @param node the node whose children should be added to the container
      * @param copy if this flag is <code>true</code> then node copies will be
      *        added to the specified container and the original nodes stay
      *        intact
      * @throws XmlException
      */
     public static void appendChildren(
         XmlWrapper container,
         XmlWrapper node,
         boolean copy) throws XmlException {
        Node rootElement = node.getRootNode();
        if (rootElement == null) {
            return;
        }
        Node childNode = rootElement.getFirstChild();
        if (childNode == null) {
             return;
         }
         List<Node> list = new ArrayList<Node>();
         while (childNode != null) {
             if (copy) {
                Node nodeClone = childNode.cloneNode(true);
                list.add(nodeClone);
             } else {
                 list.add(childNode);
             }
             childNode = childNode.getNextSibling();
         }
         Node parent = container.getRoot();
         for (Node n : list) {
             append(parent, n);
         }
     }
 
     /**
      * Applies an XSL transformation to an XML document. The original XML the
      * document and document defining the XSL transformation are provided in the
      * form of streams.
      * 
      * @param xmlStream stream providing access to the original serialized
      *        document
      * @param xslStream stream providing access to the XSL transformation
      *        document
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @param xslStream the stream providing access to the serialized XSL
      *        document
      * @param out the output where the resulting document is written
      * @throws XmlException
      * @throws IOException
      */
     public static void applyXSL(
         InputStream xmlStream,
         InputStream xslStream,
         URIResolver resolver,
         Writer output) throws XmlException, IOException {
         StreamSource xmlSource = new StreamSource(xmlStream);
         StreamSource xslSource = new StreamSource(xslStream);
         applyXSL(xmlSource, xslSource, resolver, output);
     }
 
     /**
      * Applies the specified transformation to the document, creates a new
      * document and returns it.
      * 
      * @param xml the XML document to transform
      * @param transformer the XSL transformer
      * @return the result of the transformation
      * @throws XmlException
      */
     public static Document applyXSL(Node xml, Transformer transformer)
         throws XmlException {
         try {
             DOMSource xmlSource = new DOMSource(xml);
             Document resultDoc = newDocument();
             DOMResult result = new DOMResult(resultDoc);
             transformer.transform(xmlSource, result);
             return resultDoc;
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation to the current document",
                 t);
         }
     }
 
     /**
      * Applies an XSL transformation to the given document. The first parameter
      * of this method should be an {@link Node} or a {@link Document}. The
      * document defining the XSL transformation is provided in the form of a
      * stream.
      * 
      * @param doc the document to transform; should be the {@link Node} or
      *        {@link Document} instance
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @param xslStream the stream providing access to the serialized XSL
      *        document
      * @param out the output where the resulting document is written
      * @throws XmlException
      * @throws IOException
      */
     public static void applyXSL(
         Node doc,
         URIResolver resolver,
         InputStream xslStream,
         OutputStream out) throws XmlException, IOException {
         DOMSource xmlSource = new DOMSource(doc);
         StreamSource xslSource = new StreamSource(xslStream);
         OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
         applyXSL(xmlSource, xslSource, resolver, writer);
         writer.flush();
     }
 
     /**
      * Applies an XSL transformation to the given document. The first parameter
      * of this method should be an {@link Node} or a {@link Document}. The
      * document defining the XSL transformation is provided in the form of a
      * stream.
      * 
      * @param doc the document to transform; should be the {@link Node} or
      *        {@link Document} instance
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @param xslStream the stream providing access to the serialized XSL
      *        document
      * @param writer the output where the resulting document is written
      * @throws XmlException
      * @throws IOException
      */
     public static void applyXSL(
         Node doc,
         URIResolver resolver,
         Reader xslStream,
         Writer writer) throws XmlException, IOException {
         DOMSource xmlSource = new DOMSource(doc);
         StreamSource xslSource = new StreamSource(xslStream);
         applyXSL(xmlSource, xslSource, resolver, writer);
         writer.flush();
     }
 
     /**
      * Applies an XSL transformation to an XML document. The original XML the
      * document and document defining the XSL transformation are provided in the
      * form of streams.
      * 
      * @param xmlStream stream providing access to the original serialized
      *        document
      * @param xslStream stream providing access to the XSL transformation
      *        document
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @param xslStream the stream providing access to the serialized XSL
      *        document
      * @param output the output where the resulting document is written
      * @throws XmlException
      * @throws IOException
      */
     public static void applyXSL(
         Reader xmlStream,
         Reader xslStream,
         URIResolver resolver,
         Writer output) throws XmlException, IOException {
         StreamSource xmlSource = new StreamSource(xmlStream);
         StreamSource xslSource = new StreamSource(xslStream);
         applyXSL(xmlSource, xslSource, resolver, output);
     }
 
     /**
      * Applies an XSL transformation to an XML document and returns the
      * resulting XML document.
      * 
      * @param xmlSource the original serialized document
      * @param xslStream XSL transformation document
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @return the resulting XML document (the result of the transformation)
      * @throws XmlException
      * @throws IOException
      */
     public static Document applyXSL(
         Source xmlSource,
         Source xslSource,
         URIResolver resolver) throws XmlException {
         try {
             Transformer transformer = getXslTransformer(xslSource, resolver);
             Document resultDoc = newDocument();
             DOMResult result = new DOMResult(resultDoc);
             transformer.transform(xmlSource, result);
             return resultDoc;
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation to the current document",
                 t);
         }
     }
 
     /**
      * Applies an XSL transformation to an XML document and writes the resulting
      * XML document in the given stream.
      * 
      * @param xmlSource the original document
      * @param xslStream XSL transformation document
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @param output the output where the resulting document is written
      * @throws XmlException
      * @throws IOException
      */
     public static void applyXSL(
         Source xmlSource,
         Source xslSource,
         URIResolver resolver,
         Writer output) throws XmlException, IOException {
         try {
             Transformer transformer = getXslTransformer(xslSource, resolver);
             StreamResult result = new StreamResult(output);
             transformer.transform(xmlSource, result);
         } catch (Throwable t) {
             throw handleError("Can not format an XML document.", t);
         }
     }
 
     /**
      * Applies an XSL transformation to an XML document and returns the
      * resulting document serialized as a string.
      * 
      * @param xml the original document in the form of a string
      * @param xslStream XSL transformation document in the form of a string
      * @param resolver an {@link URIResolver} instance used to resolve entities
      *        found in the document
      * @return the resulting document serialized as a string
      * @throws XmlException
      */
     public static String applyXSL(String xml, String xsl, URIResolver resolver)
         throws XmlException {
         try {
             StringWriter writer = new StringWriter();
             StringReader xmlReader = new StringReader(xml);
             StringReader xslReader = new StringReader(xsl);
             applyXSL(xmlReader, xslReader, resolver, writer);
             return writer.toString();
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation for this document",
                 t);
         }
     }
 
     /**
      * Copies the given document and returns it to the caller.
      * 
      * @param document the document to copy
      * @return a new document
      * @throws Exception
      */
     public static Document copyDocument(Document document) throws Exception {
         Node n = document.getDocumentElement().cloneNode(true);
         Document result = newDocument();
         result.adoptNode(n);
         result.appendChild(n);
         return result;
     }
 
     /**
      * Copies the specified node and append the resulting copy to the given
      * element.
      * 
      * @param target the target element where a newly created copy should be
      *        appended.
      * @param node the node to copy
      * @return a newly created copy of the given node.
      */
     public static Node copyNode(Element target, Node node) {
         Node n = node.cloneNode(true);
         Document targetDoc = target.getOwnerDocument();
         targetDoc.adoptNode(n);
         target.appendChild(n);
         return node;
     }
 
     /**
      * Copies the specified node and adds it to the target element. The created
      * deep copy is adopted by the document of the target element.
      * 
      * @param target the target node where a newly created clone should be
      *        appended
      * @param node the node to copy
      * @return the newly created copy of the specified node
      */
     public static Node copyNode(Node target, Node node) {
         Node n = node.cloneNode(true);
         append(target, n);
         return node;
     }
 
     /**
      * Returns the document corresponding to the specified node.
      * 
      * @param target the node
      * @return the document corresponding to the specified node
      */
     public static Document getDocument(Node target) {
         return target instanceof Document ? (Document) target : target
             .getOwnerDocument();
     }
 
     /**
      * Creates and returns an new document builder factory. This methnod tries
      * to configure the namespace support for the builder. If the underlying
      * parser does not support namespaces then this method returns a simple
      * DocumentBuilder object.
      * 
      * @return a new document builder
      * @throws ParserConfigurationException
      */
     private static DocumentBuilder getDocumentBuilder() throws XmlException {
         try {
             DocumentBuilderFactory factory = DocumentBuilderFactory
                 .newInstance();
             factory.setNamespaceAware(true); // never forget this!
             try {
                 factory.setFeature(
                     "http://xml.org/sax/features/namespaces",
                     true);
             } catch (Throwable t) {
                 // Just skip it...
             }
             DocumentBuilder builder = factory.newDocumentBuilder();
             return builder;
         } catch (Throwable t) {
             throw handleError("Can not an XML document builder", t);
         }
     }
 
     //
     /**
      * Returns a localized qualified name object corresponding to the specified
      * element name. The given element name can use namespace prefixes defined
      * in the user-defined {@link NamespaceContext} while the same namespace in
      * the current node could be assiciated with a different namespace prefix.
      * So this method translates user-defined namespace prefix to the namespace
      * prefix specifiec for the current element context.
      * <p>
      * Example:<br />
      * User wants to create a new element "atom:content". The "atom" prefix is
      * associated with this namespace: "http://www.w3.org/2005/Atom". But the
      * element managed by the current wrapper uses another namespace prefix for
      * the same namespace - "a". So the user-defined element name "atom:content"
      * will be translated to the "a:element" qualified name.
      * </p>
      * 
      * @param namespaceContext the namespace context used to translate prefixes
      *        to namespace URIs and vice versa
      * @param elementName the full element name
      * @return a qualified name object corresponding to the specified element
      *         name
      */
     protected static QName getQualifiedName(
         NamespaceContext namespaceContext,
         String elementName) {
         String[] array = splitQName(elementName);
         String prefix = array[0];
         if (prefix == null) {
             prefix = XMLConstants.DEFAULT_NS_PREFIX;
         }
         String namespace = namespaceContext.getNamespaceURI(prefix);
         if (namespace == null) {
             throw new IllegalArgumentException(
                 "Can not find a namespace by the element prefix.");
         }
         String resultPrefix = namespaceContext.getPrefix(namespace);
         String localPart = array[1];
         QName qName = new QName(namespace, localPart, resultPrefix);
         return qName;
     }
 
     /**
      * Creates and returns a newly created XSL transformer instance; the
      * returned instance contains the given {@link URIResolver} used to load
      * external XML documents from XSL templates.
      * 
      * @param xslSource the XSL document
      * @param resolver the {@link URIResolver} instance used to load external
      *        documents
      * @return a newly created configured XSL transformer
      * @throws XmlException
      */
     public static Transformer getXslTransformer(
         Source xslSource,
         URIResolver resolver) throws XmlException {
         try {
             Templates t = TRANSFORMER_FACTORY.newTemplates(xslSource);
             Transformer transformer = t.newTransformer();
             if (resolver != null) {
                 transformer.setURIResolver(resolver);
             }
             return transformer;
         } catch (Throwable t) {
             throw handleError("Can not initialize an XSL transformer.", t);
         }
     }
 
     /**
      * Logs the specified error and wraps it in a {@link XmlException}; if the
      * given exception is already an {@link XmlException} instance then this
      * method returns it "as is", without adding it in the log.
      * 
      * @param msg the message associated with this error; it will be added to
      *        the log
      * @param e the error to wrap
      * @return an {@link XmlException} instance wrapping the specified exception
      */
     public static XmlException handleError(String msg, Throwable e) {
         if (e instanceof XmlException) {
             return (XmlException) e;
         }
         log.log(Level.FINE, msg, e);
         return new XmlException(msg, e);
     }
 
     /**
      * Creates and returns a new empty DOM document.
      * 
      * @return a newly created DOM document
      * @throws ParserConfigurationException
      */
     public static Document newDocument() throws XmlException {
         DocumentBuilder builder = getDocumentBuilder();
         return builder.newDocument();
     }
 
     /**
      * Creates and returns a new element with the specified name.
      * 
      * @param namespaceContext the namespace context used prefixes to namespace
      *        URIs and vice versa
      * @param doc the document used as a factory of the element
      * @param elementName the qualified name of the element to create
      * @return a newly created element
      */
     protected static Element newElement(
         NamespaceContext namespaceContext,
         Document doc,
         String elementName) {
         QName qName = getQualifiedName(namespaceContext, elementName);
         String qualifiedName = serializeQualifiedName(qName);
         Element element = doc.createElementNS(
             qName.getNamespaceURI(),
             qualifiedName);
         return element;
     }
 
     /**
      * Parses the given input stream and returns the corresponding desirialized
      * XML document.
      * 
      * @param input the input stream containing the serialized XML document
      * @return the deserialized DOM document
      * @throws XmlException
      * @throws IOException
      */
     public static Document readXML(InputStream input)
         throws XmlException,
         IOException {
         return readXML(new InputStreamReader(input, "UTF-8"));
     }
 
     /**
      * Parses the given input stream and returns the corresponding desirialized
      * XML document.
      * 
      * @param reader the reader containing the serialized XML document
      * @return the deserialized DOM document
      * @throws XmlException
      */
     public static Document readXML(Reader reader)
         throws XmlException,
         IOException {
         try {
             DocumentBuilder builder = getDocumentBuilder();
             InputSource source = new InputSource(reader);
             Document doc = builder.parse(source);
             return doc;
         } catch (IOException e) {
             throw e;
         } catch (Throwable t) {
             throw handleError("Can not read an XML document", t);
         } finally {
             reader.close();
         }
     }
 
     /**
      * Parses the given string-serialized XML document and returns the
      * corresponding {@link Document} instance.
      * 
      * @param xml the string-serialized XML document
      * @return the de-serialized DOM document
      * @throws XmlException
      */
     public static Document readXML(String xml) throws XmlException, IOException {
         StringReader reader = new StringReader(xml);
         return readXML(reader);
     }
 
     /**
      * Returns a string-serialized representation of the given qualified name.
      * 
      * @param qName the qualified name to serialize
      * @return a string representation of the qualified name
      */
     private static String serializeQualifiedName(QName qName) {
         String prefix = qName.getPrefix();
         String name = qName.getLocalPart();
         if (prefix != null && !"".equals(prefix)) {
             name = prefix + ":" + name;
         }
         return name;
     }
 
     /**
      * Serializes the given XML node in the specified output stream.
      * 
      * @param node the XML node to serialize
      * @param writer the output stream where the content is written
      * @throws XmlException
      */
     public static void serializeXML(Node doc, Writer writer, boolean indent)
         throws XmlException {
         boolean omitxmldeclaration = true;
         try {
             Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
             if (omitxmldeclaration) {
                 transformer.setOutputProperty(
                     OutputKeys.OMIT_XML_DECLARATION,
                     "yes");
             }
             transformer.setOutputProperty(OutputKeys.METHOD, "xml");
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             if (indent) {
                 transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                 try {
                     transformer.setOutputProperty(
                         "{http://xml.apache.org/xslt}indent-amount",
                         "4");
                 } catch (Exception e) {
                     // Just do nothing. The serializer does not recognize this
                     // Apache-specific feature.
                 }
             }
             Source input = new DOMSource(doc);
             Result output = new StreamResult(writer);
             transformer.transform(input, output);
         } catch (Throwable e) {
             throw handleError(
                 "Can not serialize an XLM document or an XML element",
                 e);
         }
     }
 
     /**
      * Serializes the given XML node in the specified output stream.
      * 
      * @param node the XML node to serialize
      * @param writer the output stream where the document should be serialized
      * @param includeNode if this parameter is <code>true</code> then the tag
      *        markup corresponding to this element is serialized as well;
      *        otherwise only children of this node are serialized.
      * @throws XmlException
      */
     public static boolean serializeXML(
         Node node,
         Writer writer,
         boolean includeNode,
         boolean indent) throws XmlException {
         if (node == null) {
             return false;
         }
         try {
             short type = node.getNodeType();
             boolean result = true;
             switch (type) {
                 case Node.DOCUMENT_NODE: {
                     Node element = ((Document) node).getDocumentElement();
                     serializeXML(element, writer, includeNode, indent);
                     break;
                 }
                 case Node.ATTRIBUTE_NODE: {
                     String text = ((Attr) node).getValue();
                     writer.write(text);
                     break;
                 }
                 case Node.TEXT_NODE: {
                     String text = ((Text) node).getData();
                     writer.write(text);
                     break;
                 }
                 case Node.ELEMENT_NODE: {
                     Node element = node;
                     if (includeNode) {
                         serializeXML(element, writer, indent);
                     } else {
                         Node child = element.getFirstChild();
                         while (child != null) {
                             serializeXML(child, writer, true, indent);
                             child = child.getNextSibling();
                         }
                     }
                     break;
                 }
                 default: {
                     result = false;
                     break;
                 }
             }
             return result;
         } catch (Throwable t) {
             throw handleError("Can not serialize an XML node", t);
         }
     }
 
     /**
      * This utility method is used to split a fully qualified name to prefix an
      * a local name; the returned array contains two values - a name prefix and
      * a local name
      * 
      * @param qName a qualified element (or attributed) name to split
      * @return an array with two elements - name prefix and local name
      */
     public static String[] splitQName(String qName) {
         String[] result = { null, qName };
         int idx = qName.indexOf(':');
         if (idx > 0) {
             result[0] = qName.substring(0, idx);
             result[1] = qName.substring(idx + 1);
         }
         return result;
     }
 
     /**
      * The context object containing parameters used by this wrapper. It
      * contains {@link NamespaceContext} and {@link URIResolver} instances used
      * to compile XPath expressions and to implement document importing for XSL
      * transformations.
      */
     protected XmlContext fContext;
 
     /**
      * The composite namespace context. This context should be never accessed
      * directly - only using the {@link #getNamespaceContext()} method. This
      * field is calculated based on the {@link NamespaceContext} instance
      * provided by the internal context (see the {@link #fContext} field) and by
      * a namespace context created from the internal XML node (see
      * {@link ElementBasedNamespaceContext} class).
      */
     private NamespaceContext fNamespaceContext;
 
     /**
      * The wrapped node
      */
     protected Node fRoot;
 
     /**
      * This constructor initializes the root node and creates a new
      * {@link XmlWrapper.XmlContext} instance using the user-defined namespaces.
      * These namespaces are used to compile XPath expressions.
      * 
      * @param node the node to wrap
      * @param namespaces an array containing prefixes (odd positions in the
      *        array) and the corresponding namespace URIs (even positions in the
      *        array).
      */
     public XmlWrapper(Node node, String... namespaces) {
         this(node, XmlContext.builder(namespaces).build());
     }
 
     /**
      * This constructor saves the wrapped node and the given XML context in the
      * internal fields.
      * 
      * @param node the wrapped node
      * @param context the XML context
      */
     public XmlWrapper(Node node, XmlContext context) {
         fRoot = node;
         setXmlContext(context);
     }
 
     /**
      * Creates this wrapper using parameters from an another wrapper (this is a
      * "copy constructor").
      * 
      * @param wrapper the wrapper to copy
      */
     public XmlWrapper(XmlWrapper wrapper) {
         this(wrapper.getRootNode(), wrapper.getXmlContext());
     }
 
     /**
      * This method appends the given XML node to the internal managed node. The
      * appended element is wrapped in the object of the specified type and
      * returned.
      * 
      * @param xml the XML to append to this document
      * @param type the class defining the type of the wrapper to apply to the
      *        copy
      * @return a wrapper instance of the specified type managing the appended
      *         node
      * @throws XmlException
      */
     public XmlWrapper append(XmlWrapper xml) throws XmlException {
         return append(xml, XmlWrapper.class);
     }
 
     /**
      * This method appends the given XML node to the internal managed node. The
      * appended element is wrapped in the object of the specified type and
      * returned.
      * 
      * @param <T> the type of the wrapper
      * @param xml the XML to append to this document
      * @param type the class defining the type of the wrapper to apply to the
      *        copy
      * @return a wrapper instance of the specified type managing the appended
      *         node
      * @throws XmlException
      */
     public <T extends XmlWrapper> T append(XmlWrapper xml, Class<T> type)
         throws XmlException {
         Node root = getRootNode();
         Node child = xml.getRootNode();
         Document doc = root.getOwnerDocument();
         child = doc.adoptNode(child);
         root.appendChild(child);
         T result = fContext.wrap(child, type);
         return result;
     }
 
     /**
      * This method creates a new copy of the given XML and appends this copy to
      * the internal managed node. The resulting created node is wrapped in a
      * {@link XmlWrapper} object and returned.
      * 
      * @param xml the XML to append to this document
      * @return a wrapper instance managing newly created and appended copy
      * @throws XmlException
      */
     public XmlWrapper appendCopy(XmlWrapper xml) throws XmlException {
         return appendCopy(xml, XmlWrapper.class);
     }
 
     /**
      * This method creates a new copy of the given XML and appends this copy to
      * the internal managed node. The resulting created node is wrapped in the
      * object of the specified type and returned.
      * 
      * @param <T> the type of the wrapper
      * @param xml the XML to append to this document
      * @param type the class defining the type of the wrapper to apply to the
      *        copy
      * @return a wrapper instance of the specified type managing newly created
      *         and appended copy
      * @throws XmlException
      */
     public <T extends XmlWrapper> T appendCopy(XmlWrapper xml, Class<T> type)
         throws XmlException {
         T copy = xml.createCopy(type);
         append(getRoot(), copy.getRoot());
         return copy;
     }
 
     /**
      * This method appends a new element with the specified name to the internal
      * node. The resulting created node is wrapped in a {@link XmlWrapper}
      * instance.
      * 
      * @param elementName a qualified name of the element to add
      * @return a wrapper instance of the specified type managing newly created
      *         and appended copy
      * @throws XmlException
      */
     public XmlWrapper appendElement(String elementName) throws XmlException {
         return appendElement(elementName, XmlWrapper.class);
     }
 
     /**
      * This method appends a new element with the specified name to the internal
      * node. The resulting created node is wrapped in the object of the
      * specified type and returned.
      * 
      * @param <T> the type of the wrapper to apply to the created element
      * @param elementName a qualified name of the element to add
      * @param type the class defining the type of the wrapper to apply to the
      *        copy
      * @return a wrapper instance of the specified type managing newly created
      *         and appended copy
      * @throws XmlException
      */
     public <T extends XmlWrapper> T appendElement(
         String elementName,
         Class<T> type) throws XmlException {
         Document doc = getDocument();
         NamespaceContext namespaceContext = getNamespaceContext();
         Element element = newElement(namespaceContext, doc, elementName);
         Element root = getRootElement();
         if (root == null) {
             doc.appendChild(element);
         } else {
             root.appendChild(element);
         }
         T result = fContext.wrap(element, type);
         return result;
     }
 
     /**
      * Appends the specified text to the current element (adds a new text node).
      * 
      * @param content the text to append
      * @return reference to this element
      */
     public XmlWrapper appendText(String content) {
         Element element = getRootElement();
         Document doc = element.getOwnerDocument();
         Text node = doc.createTextNode(content);
         element.appendChild(node);
         return this;
     }
 
     /**
      * Applies the specified XSL transformation to the wrapped node and returns
      * a resulting XML document.
      * 
      * @param xsl the XSL document to apply to this node
      * @return the resulting XML document
      * @throws XmlException
      */
     public Document applyXSL(Node xsl) throws XmlException {
         try {
             Node e = getRootNode();
             DOMSource xmlSource = new DOMSource(e);
             DOMSource xslSource = new DOMSource(xsl);
             URIResolver resolver = fContext.getURIResolver();
             Document resultDoc = applyXSL(xmlSource, xslSource, resolver);
             return resultDoc;
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation to the current document",
                 t);
         }
     }
 
     /**
      * Applies the specified XSL document to the wrapped node. The result is
      * serialized in the given output stream.
      * 
      * @param xsl the XSL document to apply to this node
      * @param writer the output stream where the result of the XSL
      *        transformation is serialized
      * @throws XmlException
      */
     public void applyXSL(Node xsl, Writer writer) throws XmlException {
         try {
             Node e = getRootNode();
             DOMSource xmlSource = new DOMSource(e);
             DOMSource xslSource = new DOMSource(xsl);
             URIResolver resolver = fContext.getURIResolver();
             applyXSL(xmlSource, xslSource, resolver, writer);
             writer.flush();
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation for this document.",
                 t);
         }
     }
 
     /**
      * Applies an XSL transformation managed by the given wrapper instance and
      * returns a new wrapper with the resulting document.
      * 
      * @param xsl the object wrapping the XSL document to apply
      * @return a wrapper with the resulting XML document
      * @throws XmlException
      */
     public XmlWrapper applyXSL(XmlWrapper xsl) throws XmlException {
         return applyXSL(xsl, XmlWrapper.class);
     }
 
     /**
      * Applies an XSL transformation managed by the given wrapper instance and
      * returns a new wrapper of the specified type with the resulting document.
      * 
      * @param xsl the object wrapping the XSL document to apply
      * @param type the type of the wrapper to apply for the resulting document
      * @return a wrapper with the resulting XML document
      * @throws XmlException
      */
     public <T extends XmlWrapper> T applyXSL(XmlWrapper xsl, Class<T> type)
         throws XmlException {
         try {
             Node e = getRootNode();
             DOMSource xmlSource = new DOMSource(e);
             DOMSource xslSource = new DOMSource(xsl.getDocument());
             URIResolver resolver = fContext.getURIResolver();
             Document resultDoc = applyXSL(xmlSource, xslSource, resolver);
             return fContext.wrap(resultDoc, type);
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation to the current document",
                 t);
         }
     }
 
     /**
      * Applies the specified XSL document to the wrapped node. The result is
      * serialized in the given output stream.
      * 
      * @param xsl the XSL document to apply to this node
      * @param writer the output stream where the result of the XSL
      *        transformation is serialized
      * @throws XmlException
      */
     public void applyXSL(XmlWrapper xsl, OutputStream out) throws XmlException {
         try {
             Writer writer = new OutputStreamWriter(out, "UTF-8");
             applyXSL(xsl, writer);
         } catch (Throwable t) {
             throw handleError(
                 "Can not apply an XSL transformation for the current document",
                 t);
         }
     }
 
     /**
      * Applies the specified XSL document to the wrapped node. The result is
      * serialized in the given output stream.
      * 
      * @param xsl the XSL document to apply to this node
      * @param writer the output stream where the result of the XSL
      *        transformation is serialized
      * @throws XmlException
      */
     public void applyXSL(XmlWrapper xsl, Writer writer) throws XmlException {
         applyXSL(xsl.getRootElement(), writer);
     }
 
     /**
      * This method creates and returns a new copy of this XML.
      * 
      * @return a new wrapper instance containing a copy of this node
      * @throws XmlException
      */
     public XmlWrapper createCopy() throws XmlException {
         return createCopy(XmlWrapper.class);
     }
 
     /**
      * This method creates and returns a new copy of this XML wrapped in the
      * object of the specified type.
      * 
      * @param <T> the type of the wrapper to return
      * @param type the class defining the type of the wrapper to apply to the
      *        copy
      * @return a wrapper instance of the specified type managing newly created
      *         copy
      * @throws XmlException
      */
     public <T extends XmlWrapper> T createCopy(Class<T> type)
         throws XmlException {
         Node copy = createNodeCopy();
         T result = fContext.wrap(copy, type);
         return result;
     }
 
     /**
      * Returns a new copy of the underlying XML node.
      * 
      * @return a new copy of the underlying XML node.
      */
     protected Node createNodeCopy() {
         Node root = getRoot();
         Node copy = root.cloneNode(true);
         return copy;
     }
 
     /**
      * Evaluates the given XPath expression and returns an XmlWrapper managing
      * the result.
      * 
      * @param xpath the XPath expression
      * @return an XmlWrapper object managing the result of the XPath expression
      * @throws XmlException
      */
     public XmlWrapper eval(String xpath) throws XmlException {
         return eval(xpath, XmlWrapper.class);
     }
 
     /**
      * Evaluates the given XPath expression and returns an XmlWrapper of the
      * specified type with the result of this expression.
      * 
      * @param xpath the XPath expression to apply to the wrapped element
      * @param type the type of the wrapper used for the results of XPath
      *        expressions.
      * @return an XmlWrapper object managing the result of the XPath expression
      * @throws XmlException
      */
     public <T extends XmlWrapper> T eval(String xpath, Class<T> type)
         throws XmlException {
         Node node = evalNode(xpath);
         return node != null ? fContext.wrap(node, type) : null;
     }
 
     /**
      * Evaluates the given XPath expression and returns an XML element
      * corresponding to it.
      * 
      * @param xpath the XPath expression to evaluate
      * @return the resulting XML element corresponding to the given XPath
      *         expression
      * @throws XmlException
      */
     public Element evalElement(String xpath) throws XmlException {
         Node e = evalNode(xpath);
         return (Element) (e instanceof Element ? e : null);
     }
 
     /**
      * Returns a list of wrapper objects corresponding to all XML nodes matching
      * to the given XPath expression.
      * 
      * @param <T> the type of the wrapper objects
      * @param xpath the XPath expression to evaluate
      * @param type the class defining the type of wrapper objects
      * @return a list of {@link XmlWrapper} instances wrapping all resulting XML
      *         nodes
      * @throws XmlException
      */
     public List<XmlWrapper> evalList(String xpath) throws XmlException {
         return evalList(xpath, XmlWrapper.class);
     }
 
     /**
      * Returns a list of wrapper objects corresponding to all XML nodes matching
      * to the given XPath expression.
      * 
      * @param <T> the type of the wrapper objects
      * @param xpath the XPath expression to evaluate
      * @param type the class defining the type of wrapper objects
      * @return a list of {@link XmlWrapper} instances wrapping all resulting XML
      *         nodes
      * @throws XmlException
      */
     public <T extends XmlWrapper> List<T> evalList(String xpath, Class<T> type)
         throws XmlException {
         try {
             List<T> result = new ArrayList<T>();
             Element node = getRootElement();
             XPathExpression expr = fContext.getXpath(xpath);
             NodeList nodes = (NodeList) expr.evaluate(
                 node,
                 XPathConstants.NODESET);
             for (int i = 0; i < nodes.getLength(); i++) {
                 Node n = nodes.item(i);
                 T item = fContext.wrap(n, type);
                 result.add(item);
             }
             return result;
         } catch (Throwable t) {
             throw handleError("Can not evaluate the given XPath expression "
                 + "to a list of nodes. XPath: '"
                 + xpath
                 + "'.", t);
         }
     }
 
     /**
      * Evaluates the given XPath expression and returns a list of XML nodes
      * corresponding to it.
      * 
      * @param xpath the XPath expression to evaluate
      * @return a list of XML nodes corresponding to the XPath expression
      * @throws XmlException
      */
     public List<Node> evalListNode(String xpath) throws XmlException {
         try {
             Element node = getRootElement();
             XPathExpression expr = fContext.getXpath(xpath);
             NodeList nodes = (NodeList) expr.evaluate(
                 node,
                 XPathConstants.NODESET);
             List<Node> result = new ArrayList<Node>();
             for (int i = 0; i < nodes.getLength(); i++) {
                 Node n = nodes.item(i);
                 result.add(n);
             }
             return result;
         } catch (Throwable t) {
             throw handleError("Can not evaluate the given XPath expression "
                 + "to a list of nodes. XPath: '"
                 + xpath
                 + "'.", t);
         }
     }
 
     /**
      * Evaluates the given XPath expression and returns the resulting XML node
      * object.
      * 
      * @param xpath the XPath expression to evaluate
      * @return an XML node corresponding to the given XPath expression
      * @throws XmlException
      */
     public Node evalNode(String xpath) throws XmlException {
         try {
             XPathExpression expr = fContext.getXpath(xpath);
             Element node = getRootElement();
             Node resultNode = (Node) expr.evaluate(node, XPathConstants.NODE);
             return resultNode;
         } catch (Throwable t) {
             throw handleError(
                 "Can not evaluate the specified XPath expression. XPath: '"
                     + xpath
                     + "'.",
                 t);
         }
     }
 
     /**
      * Evaluates the given XPath expression and returns a string-serialized
      * result.
      * 
      * @param xpath the XPath expression to evaluate
      * @return the result of the given XPath expression in the form of a string
      * @throws XmlException
      */
     public String evalStr(String xpath) throws XmlException {
         try {
             XPathExpression expr = fContext.getXpath(xpath);
             Element root = getRootElement();
             Node resultNode = (Node) expr.evaluate(root, XPathConstants.NODE);
             String result = null;
             if (resultNode != null) {
                 StringWriter writer = new StringWriter();
                 serializeXML(resultNode, writer, false, false);
                 result = writer.toString();
             }
             return result;
         } catch (Throwable t) {
             throw handleError("Can not evaluate to a string. XPath: '"
                 + xpath
                 + "'.", t);
         }
     }
 
     /**
      * Returns the attribute value corresponding to the specified name.
      * 
      * @param attrName the name of the attribute
      * @return an attribute value correspoding to the specified attribute name
      */
     public String getAttribute(String attrName) {
         Element e = getRootElement();
         return e.hasAttribute(attrName) ? e.getAttribute(attrName) : null;
     }
 
     /**
      * Returns the root document of the wrapped node
      * 
      * @return the root document of the wrapped node
      */
     public Document getDocument() {
         return getDocument(fRoot);
     }
 
     /**
      * Returns the first XML element of this node.
      * 
      * @return the first XML element
      * @throws XmlException
      */
     public XmlWrapper getFirstElement() throws XmlException {
         return getFirstElement(XmlWrapper.class);
     }
 
     /**
      * Returns the first XML element of this node.
      * 
      * @return the first XML element
      * @throws XmlException
      */
     public <T extends XmlWrapper> T getFirstElement(Class<T> type)
         throws XmlException {
         Element element = getRootElement();
         if (element == null) {
             return null;
         }
         Node start = element.getFirstChild();
         return getNextElementWrapper(type, start);
     }
 
     /**
      * Returns the local name of the node
      * 
      * @return the local name of the node
      */
     public String getLocalName() {
         return getRoot().getLocalName();
     }
 
     /**
      * Returns the namespace context used by this wrapper. This method builds a
      * {@link CompositeNamespaceContext} instance taking into account namespaces
      * defined in the {@link XmlContext} as well as namespaces available in the
      * scope of the wrapped XML node.
      * 
      * @return the namespace context used by this wrapper
      */
     protected NamespaceContext getNamespaceContext() {
         if (fNamespaceContext == null) {
             CompositeNamespaceContext userDefinedContext = fContext != null
                 ? fContext.getNamespaceContext(false)
                 : null;
             NamespaceContext elementNamespaceContext = null;
             Node root = getRootNode();
             if (root != null) {
                 elementNamespaceContext = new ElementBasedNamespaceContext(root);
             }
             if (userDefinedContext != null && elementNamespaceContext != null) {
                 fNamespaceContext = new CompositeNamespaceContext(
                     elementNamespaceContext,
                     userDefinedContext);
             } else {
                 fNamespaceContext = userDefinedContext != null
                     ? userDefinedContext
                     : elementNamespaceContext;
             }
         }
         return fNamespaceContext;
     }
 
     /**
      * Returns the next element sibling for this node.
      * 
      * @return the next element sibling for this node.
      * @throws XmlException
      */
     public XmlWrapper getNext() throws XmlException {
         return getNext(XmlWrapper.class);
     }
 
     /**
      * Returns the next element sibling for this node.
      * 
      * @return the next element sibling for this node.
      * @throws XmlException
      */
     public <T extends XmlWrapper> T getNext(Class<T> type) throws XmlException {
         Element element = getRootElement();
         if (element == null) {
             return null;
         }
         Node start = element.getNextSibling();
         return getNextElementWrapper(type, start);
     }
 
     /**
      * Searches then next element sibling of the specified node and returns a
      * wrapper object of the specified type for the found element.
      * 
      * @param type the type of the wrapper to apply for the found element
      * @param start the start XML node
      * @return a wrapper of the specified for the next element sibling for the
      *         specified node
      * @throws XmlException
      */
     protected <T extends XmlWrapper> T getNextElementWrapper(
         Class<T> type,
         Node start) throws XmlException {
         Element resultElement = null;
         for (Node node = start; node != null; node = node.getNextSibling()) {
             if (node instanceof Element) {
                 resultElement = (Element) node;
                 break;
             }
         }
         T result = null;
         if (resultElement != null) {
             result = getXmlContext().wrap(resultElement, type);
         }
         return result;
     }
 
     /**
      * This method returns a sub-node with the specified name. If there is no
      * such a sub-node then this method creates a new one.
      * 
      * @param elementName a qualified name of the element
      * @return a wrapper instance of the specified type managing founded or
      *         appended node
      * @throws XmlException
      */
     public XmlWrapper getOrCreateElement(String elementName)
         throws XmlException {
         return getOrCreateElement(elementName, XmlWrapper.class);
     }
 
     /**
      * This method returns a sub-node with the specified name. If there is no
      * such a sub-node then this method creates a new one.
      * 
      * @param <T> the type of the wrapper to apply to the resulting element
      * @param elementName a qualified name of the element
      * @param type the class defining the type of the wrapper to apply to the
      *        result node
      * @return a wrapper instance of the specified type managing founded or
      *         appended node
      * @throws XmlException
      */
     public <T extends XmlWrapper> T getOrCreateElement(
         String elementName,
         Class<T> type) throws XmlException {
         T node = eval(elementName, type);
         if (node == null) {
             node = appendElement(elementName, type);
         }
         return node;
     }
 
     /**
      * Returns parent wrapper
      * 
      * @return parent wrapper element
      * @throws XmlException
      */
     public XmlWrapper getParent() throws XmlException {
         return getParent(XmlWrapper.class);
     }
 
     /**
      * Returns the parent of this node.
      * 
      * @return the parent of this node.
      * @throws XmlException
      */
     public <T extends XmlWrapper> T getParent(Class<T> type)
         throws XmlException {
         Element element = getRootElement();
         if (element == null) {
             return null;
         }
         Node parent = element.getParentNode();
         if (parent == null) {
             return null;
         }
         T result = getXmlContext().wrap(parent, type);
         return result;
     }
 
     /**
      * Returns the previous element sibling for this node.
      * 
      * @return the previous element sibling for this node.
      * @throws XmlException
      */
     public XmlWrapper getPrevious() throws XmlException {
         return getPrevious(XmlWrapper.class);
     }
 
     /**
      * Returns the previous element sibling for this node.
      * 
      * @return the previous element sibling for this node.
      * @throws XmlException
      */
     public <T extends XmlWrapper> T getPrevious(Class<T> type)
         throws XmlException {
         Element element = getRootElement();
         if (element == null) {
             return null;
         }
         Node start = element.getPreviousSibling();
         return getPreviousElementWrapper(type, start);
     }
 
     /**
      * Searches then next element sibling of the specified node and returns a
      * wrapper object of the specified type for the found element.
      * 
      * @param type the type of the wrapper to apply for the found element
      * @param start the start XML node
      * @return a wrapper of the specified for the next element sibling for the
      *         specified node
      * @throws XmlException
      */
     protected <T extends XmlWrapper> T getPreviousElementWrapper(
         Class<T> type,
         Node start) throws XmlException {
         Element resultElement = null;
         for (Node node = start; node != null; node = node.getPreviousSibling()) {
             if (node instanceof Element) {
                 resultElement = (Element) node;
                 break;
             }
         }
         T result = null;
         if (resultElement != null) {
             result = getXmlContext().wrap(resultElement, type);
         }
         return result;
     }
 
     /**
      * Returns the wrapped XML node
      * 
      * @return the wrapped XML node
      */
     public Node getRoot() {
         return fRoot;
     }
 
     /**
      * Returns the root XML element; if this object wraps an XML element then
      * this element is returned; if a document is wrapped then the root element
      * of this document is returned; otherwise this method returns
      * <code>null</code>.
      * 
      * @return the root XML element;
      */
     public Element getRootElement() {
         if (fRoot instanceof Document) {
             return ((Document) fRoot).getDocumentElement();
         }
         return (Element) (fRoot instanceof Element ? fRoot : null);
     }
 
     /**
      * Returns the root XML node
      * 
      * @return the root XML node
      */
     public Node getRootNode() {
         return fRoot instanceof Document ? ((Document) fRoot)
             .getDocumentElement() : fRoot;
     }
 
     /**
      * Returns an {@link XmlWrapper.XmlContext} instance associated with this
      * wrapper and used to configure {@link NamespaceContext},
      * {@link URIResolver} and other parameters.
      * 
      * @return an {@link XmlWrapper.XmlContext} instance associated with this
      *         wrapper
      */
     public XmlContext getXmlContext() {
         return fContext;
     }
 
     /**
      * Creates a new {@link Transformer} object using the given XSL document.
      * 
      * @param xslSource an {@link Source} object giving access to XSL
      *        transformation document
      * @return a newly created XSL transformer object
      * @throws XmlException
      */
     public Transformer getXslTransformer(Source xslSource) throws XmlException {
         URIResolver resolver = fContext.getURIResolver();
         return getXslTransformer(xslSource, resolver);
     }
 
     /**
      * Inserts the specified node before an existing node and returns a wrapper
      * for this new child.
      * 
      * @param child an existing child
      * @param newChild a new child node
      * @param type the type of the wrapper to apply
      * @return a wrapper for the new child node
      * @throws XmlException
      */
     public XmlWrapper insertBefore(XmlWrapper child, XmlWrapper newChild)
         throws XmlException {
         return insertBefore(child, newChild, XmlWrapper.class);
     }
 
     /**
      * Inserts the specified node before an existing node and returns a wrapper
      * for this new child.
      * 
      * @param child an existing child
      * @param newChild a new child node
      * @param type the type of the wrapper to apply
      * @return a wrapper for the new child node
      * @throws XmlException
      */
     public <T extends XmlWrapper> T insertBefore(
         XmlWrapper child,
         XmlWrapper newChild,
         Class<T> type) throws XmlException {
         Node root = getRootNode();
         Node childNode = child.getRootNode();
         Node newChildNode = newChild.getRootNode();
         Document doc = root.getOwnerDocument();
         newChildNode = doc.adoptNode(newChildNode);
         root.insertBefore(newChildNode, childNode);
         T result = fContext.wrap(newChildNode, type);
         return result;
     }
 
     /**
      * Creates and returns a new copy of this object; it creates a copy of the
      * underlying XML nodes.
      * 
      * @return a new copy of this wrapper
      * @throws XmlException
      */
     public XmlWrapper newCopy() throws XmlException {
         return newCopy(XmlWrapper.class);
     }
 
     /**
      * Creates and returns a new copy of this object; it creates a copy of the
      * underlying XML nodes.
      * 
      * @param type the type of the resulting wrapper object
      * @return a new copy of this XML object of a specified type
      * @throws XmlException
      */
     public <T extends XmlWrapper> T newCopy(Class<T> type) throws XmlException {
         Node root = getRootElement();
         Document targetDoc = newDocument();
         T result = fContext.wrap(targetDoc, type);
         copyNode(targetDoc, root);
         return result;
     }
 
     /**
      * Removes this node from the parent document.
      */
     public void remove() {
         Element node = getRootElement();
         Node parent = node.getParentNode();
         if (parent == null) {
             parent = node.getOwnerDocument();
         }
         parent.removeChild(node);
     }
 
     /**
      * Removes all XML nodes corresponding to the specified XPath expression.
      * 
      * @param xpath the XPath expression defining nodes to remove
      * @throws XmlException
      */
     public void remove(String xpath) throws XmlException {
         try {
             Element node = getRootElement();
             XPathExpression expr = fContext.getXpath(xpath);
             NodeList nodes = (NodeList) expr.evaluate(
                 node,
                 XPathConstants.NODESET);
             for (int i = nodes.getLength() - 1; i >= 0; i--) {
                 Node n = nodes.item(i);
                 node.removeChild(n);
             }
         } catch (Throwable t) {
             throw handleError(
                 "Can not remove nodes corresponding to the specified "
                     + "XPath expression. "
                     + "XPath: '"
                     + xpath
                     + "'.",
                 t);
         }
     }
 
     /**
      * Removes all children of this node.
      */
     public void removeChildren() {
         Node root = getRoot();
         List<Node> list = new ArrayList<Node>();
         Node child = root.getFirstChild();
         while (child != null) {
             list.add(child);
             child = child.getNextSibling();
         }
         for (Node node : list) {
             root.removeChild(node);
         }
     }
 
     /**
      * Returns <code>true</code> if the specified wrapper contains the same node
      * 
      * @param wrapper the wrapper to check
      * @return <code>true</code> if the specified wrapper has the same XML node
      */
     public boolean sameNode(XmlWrapper wrapper) {
         if (wrapper == null) {
             return false;
         }
         return getRoot().equals(wrapper.getRoot());
     }
 
     /**
      * Serializes all text nodes contained in this wrapper (all tags are
      * ignored).
      * 
      * @param writer the writer where this node should be serialized
      * @throws XmlException
      */
     public void serializeText(final StringWriter writer) throws XmlException {
         XmlAcceptor.accept(getRootNode(), new XmlAcceptor.XmlVisitor() {
             @Override
             public void visit(Text node) {
                 String str = node.getData();
                 writer.write(str);
             }
         });
     }
 
     /**
      * Serializes the wrapped XML node in the given output stream.
      * 
      * @param includeNode if this flag is <code>true</code> then the tag
      *        corresponding to this node will be included in the serialized
      *        value
      * @return a string-serialized representation of the wrapped XML node
      * @throws XmlException
      */
     public void serializeXML(OutputStream output, boolean includeNode)
         throws XmlException {
         try {
             Writer writer = new OutputStreamWriter(output, "UTF-8");
             serializeXML(writer, includeNode);
         } catch (Throwable t) {
             throw handleError(
                 "Can not serialize this document in the specified stream",
                 t);
         }
     }
 
     /**
      * Serializes the wrapped XML node in the given output stream.
      * 
      * @param includeNode if this flag is <code>true</code> then the tag
      *        corresponding to this node will be included in the serialized
      *        value
      * @return a string-serialized representation of the wrapped XML node
      * @throws XmlException
      */
     public void serializeXML(Writer writer, boolean includeNode)
         throws XmlException {
         serializeXML(writer, includeNode, false);
     }
 
     /**
      * Serializes the wrapped XML node in the given output stream.
      * 
      * @param includeNode if this flag is <code>true</code> then the tag
      *        corresponding to this node will be included in the serialized
      *        value
      * @param indent the indentation (
      * @return a string-serialized representation of the wrapped XML node
      * @throws XmlException
      */
     public void serializeXML(Writer writer, boolean includeNode, boolean indent)
         throws XmlException {
         try {
             try {
                 Element root = getRootElement();
                 serializeXML(root, writer, includeNode, indent);
             } finally {
                 writer.flush();
             }
         } catch (Throwable t) {
             throw handleError(
                 "Can not serialize this document in the specified stream",
                 t);
         }
     }
 
     /**
      * Adds the specified attribute and the corresponding value to this element.
      * 
      * @param attrName the qualified name of the attribute
      * @param value the attribute value
      * @return reference to this object
      */
     public XmlWrapper setAttribute(String attrName, String value) {
         Element element = getRootElement();
         NamespaceContext namespaceContext = getNamespaceContext();
         String qualifiedName = attrName;
         if (attrName.indexOf(':') > 0) {
             QName qName = getQualifiedName(namespaceContext, attrName);
             qualifiedName = serializeQualifiedName(qName);
         }
         element.setAttribute(qualifiedName, value);
         return this;
     }
 
     /**
      * Sets a new {@link XmlContext} object given access to namespaces (via
      * {@link NamespaceContext}) and external entities (via {@link URIResolver}
      * ).
      * 
      * @param context an XML context to set
      */
     public void setXmlContext(XmlContext context) {
         fContext = context;
     }
 
     /**
      * Returns the underlying node wrapped in a XML adapter of the specified
      * type
      * 
      * @param type the type of the adapter to apply for the underlying node
      * @return the underlying node wrapped in a XML adapter of the specified
      *         type
      * @throws XmlException
      */
     public <T extends XmlWrapper> T to(Class<T> type) throws XmlException {
         return getXmlContext().wrap(getRoot(), type);
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return toString(true);
     }
 
     /**
      * Returns a string representation of this XML node; if the specified
      * <code>indent</code> parameter is <code>true</code> then the resulting
      * value will be "pretty-printed" (with indentations).
      * 
      * @param indent if this parameter is <code>true</code> then the resulting
      *        serialized XML is printed with indentations
      * @return string representation of this node
      */
     public String toString(boolean indent) {
         return toString(true, indent);
     }
 
     /**
      * Returns a string representation of this XML node; if the specified
      * <code>indent</code> parameter is <code>true</code> then the resulting
      * value will be "pretty-printed" (with indentations).
      * 
      * @param includeNode if this parameter is <code>true</code> then the tag
      *        itself is included in the serialized output; otherwise only child
      *        nodes are serialized
      * @param indent if this parameter is <code>true</code> then the resulting
      *        serialized XML is printed with indentations
      * @return string representation of this node
      */
     public String toString(boolean includeNode, boolean indent) {
         try {
             Element root = getRootElement();
             StringWriter writer = new StringWriter();
             serializeXML(root, writer, includeNode, indent);
             String result = writer.toString();
             return result;
         } catch (Throwable e) {
             handleError("Can not serialize to string", e);
             return null;
         }
     }
 
     /**
      * Serializes and returns only "visible" text nodes managed by this wrapper.
      * 
      * @return a string representation of this node
      */
     public String toText() throws XmlException {
         StringWriter writer = new StringWriter();
         serializeText(writer);
         String result = writer.toString();
         return result;
     }
 
 }
