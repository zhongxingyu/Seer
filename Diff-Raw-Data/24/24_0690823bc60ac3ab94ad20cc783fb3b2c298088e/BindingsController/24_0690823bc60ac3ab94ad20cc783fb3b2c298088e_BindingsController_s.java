 package cz.cuni.mff.peckam.java.origamist.jaxb;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.UnmarshalException;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import cz.cuni.mff.peckam.java.origamist.jaxb.AdditionalTransforms.AdditionalTransform;
 import cz.cuni.mff.peckam.java.origamist.jaxb.AdditionalTransforms.TransformLocation;
 
 /**
  * Process an input XML document by unmarshalling.
  * <p>
  * 
  * BindingsController is not re-entrant (i.e., it does not support simultaneous execution by multiple threads, but it is
  * re-useable.
  * <p>
  * 
  * This package is based on a XML Schema versioning system from http://www.funkypeople.biz/knowledge/JavaXml-v2.zip .
  * 
  * @author Sean Barnett
  * @author Martin Pecka
  * 
  * @param T The type of the unmarhalled object.
  */
 public class BindingsController<T>
 {
 
     /** The bindings manager to fetch the transformations from. */
     protected final BindingsManager bindingsManager;
 
     /** The schema this controller handles. */
     protected final SchemaInfo      schema;
 
     /** The parser that parses the XML file. */
     protected SAXParser             unmarshalParser;
 
     /** The binding found for the actually processed document. */
     protected Bindings<T>           binding              = null;
 
     /**
      * This map holds the additional transforms that are to be applied if working with an input file whose namespace is
      * a key from this map. <code>null</code> key holds transforms to be executed for all input files.
      */
     protected AdditionalTransforms  additionalTransforms = new AdditionalTransforms();
 
     /**
      * Simple constructor.
      * 
      * @param bindingsManager A reference to bindings manager.
      * @param namespace The namespace of the schema this controller handles.
      * 
      * @throws JAXBException If something goes wrong in configuring the controller.
      */
     public BindingsController(BindingsManager bindingsManager, String namespace) throws JAXBException
     {
         this.bindingsManager = bindingsManager;
         schema = bindingsManager.getSchema(namespace);
 
         if (schema == null)
             throw new JAXBException("Unsupported document namespace '" + namespace + "'");
     }
 
     /**
      * Unmarshals the given XML file applying transforms to assure it did correspond to the newest version of its schema
      * before the sole unmarshalling.
      * 
      * @param xmlReader The reader that reads the XML file.
      * 
      * @return The unmarshalled object.
      * 
      * @throws JAXBException If there is a problem with the framework software or environment.
      * @throws UnmarshalException If there is a problem during unmarshaling.
      * @throws IOException If reading of the source XML failed.
      */
     public T unmarshal(Reader xmlReader) throws JAXBException, UnmarshalException, IOException
     {
         try {
 
             unmarshalParser = bindingsManager.createParser();
             unmarshalParser.getXMLReader().setContentHandler(new NamespaceDetectingContentHandler());
 
             try {
                 unmarshalParser.getXMLReader().parse(new InputSource(xmlReader));
             } catch (ParseAbortedException e) {
                 // ignore this exception, it just signalizes that no more parsing is required
             } catch (SAXException e) {
                 if (!(e.getCause() instanceof ParseAbortedException))
                     throw e;
             }
 
             // The parse() method triggered the NamespaceDetectingContentHandler, which detected the schema of the XML
             // file, and set the appropriate unmarshal handler to the reader. After the parse has finished, we can query
             // the result from the Bindings object.
 
             return binding.getResult();
         } catch (ParserConfigurationException e) {
             throw new JAXBException(e.getMessage(), e);
         } catch (SAXException e) {
             throw new JAXBException(e.getMessage(), (e.getException() == null) ? e : e.getException());
         }
 
     }
 
     /**
      * Get the appropriate bindings implementation based upon the relative versions of the XML and Java (both deduced
      * from namespaces).
      * 
      * @param xmlNamespace The namespace (and therefore the version) of the XML to unmarshal.
      * 
      * @return The appropriate binding.
      * 
      * @throws BindingsFrameworkException If there is a problem with the framework software or environment.
      */
     protected Bindings<T> getBindings(String xmlNamespace) throws JAXBException
     {
         boolean equalNamespaces = schema.getNamespace().equals(xmlNamespace);
 
         List<AdditionalTransform> start = new LinkedList<AdditionalTransforms.AdditionalTransform>();
         List<AdditionalTransform> beforeUnmarshaller = new LinkedList<AdditionalTransforms.AdditionalTransform>();
         List<AdditionalTransform> end = new LinkedList<AdditionalTransforms.AdditionalTransform>();
 
         List<AdditionalTransform> transforms = additionalTransforms.getTransforms(xmlNamespace, equalNamespaces, start,
                 beforeUnmarshaller, end);
 
         if (equalNamespaces && transforms.size() == 0) {
             // if two schemas are the same then simple JAXB bindings are ok
             return new SimpleBindings<T>(bindingsManager, schema);
         } else {
             // if XML is an older version than Java then locate a transform (or sequence of transforms) from bindings
             // manager and create a ParserTransformBindings around it
             TransformInfo transform = null;
             if (!equalNamespaces) {
                 transform = bindingsManager.getTransform(xmlNamespace, schema.getNamespace());
             } else { // transforms.size() must be >0 for equal namespaces
                 if (start.size() > 0) {
                     transform = start.get(start.size() - 1).getTransform();
                     transforms.remove(start.remove(start.size() - 1));
                 } else if (beforeUnmarshaller.size() > 0) {
                     transform = beforeUnmarshaller.get(beforeUnmarshaller.size() - 1).getTransform();
                     transforms.remove(beforeUnmarshaller.remove(beforeUnmarshaller.size() - 1));
                 } else {
                     transform = end.get(end.size() - 1).getTransform();
                     transforms.remove(end.remove(end.size() - 1));
                 }
             }
 
             if (transform == null)
                 throw new JAXBException("Unsupported XML namespace '" + xmlNamespace + "'");
 
             return new ParserTransformBindings<T>(bindingsManager, transform, start, beforeUnmarshaller, end);
         }
     }
 
     /**
      * Add an additional transform that is to be applied in addition to the transforms fetched from
      * {@link BindingsManager}
      * 
      * @param transform The additional transform. The fromSchema and toSchema will be ignored.
      * @param location The location of the transform.
      * @param execIfSchemataEqual Whether to run this transform even if the XML's schema is in the newest version.
      * @param namespaces The list of source namespaces for which this transform has to be applied. A <code>null</code>
      *            namespace (or namespaces omitted at all) means this transform has to be executed for all source
      *            namespaces (beware: the setting of execIfSchemataEqual can disable the transform if the source
      *            namespace is equal to the target one).
      */
     public void addAdditionalTransform(TransformInfo transform, TransformLocation location,
             boolean execIfSchemataEqual, String... namespaces)
     {
         additionalTransforms.add(transform, location, execIfSchemataEqual, namespaces);
     }
 
     /**
      * This content handler just reads the first tag, and if it is namespaced, sets the content handler appropriate for
      * the found source schema to the parser's reader. If the first element is not namespaced, this handler throws a
      * {@link org.xml.sax.SAXException}.
      * 
      * @author Martin Pecka
      */
     protected class NamespaceDetectingContentHandler extends DefaultHandler
     {
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
         {
             if (uri.length() == 0)
                 throw new SAXException("Trying to unmarshal XML file without a namespaced root element.");
 
             // based upon target XML version (learned from this.schema) and input XML version get the right bindings
             // implementation
             try {
                 binding = getBindings(uri);
             } catch (JAXBException e) {
                 throw new SAXException(e);
             }
 
             // get the unmarshaller content handler
             ContentHandler handler = binding.getContentHandler();
             if (handler == null)
                 throw new SAXException("Couldn't find a handler for unmarshalling the given XML file.");
 
             // this effectively removes this handler from the reader and substitutes it with the bindings' handler
             unmarshalParser.getXMLReader().setContentHandler(handler);
 
             // simulate the start of the parsing process for the new handler
             // TODO maybe we have to handle startPrefixMapping() or similar methods
 
             handler.startDocument();
             handler.startElement(uri, localName, qName, attributes);
         }
     }
 }
