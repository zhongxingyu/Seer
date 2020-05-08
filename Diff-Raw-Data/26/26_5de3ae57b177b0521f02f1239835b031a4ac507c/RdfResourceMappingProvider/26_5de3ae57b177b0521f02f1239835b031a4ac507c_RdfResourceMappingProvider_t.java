 package uk.ac.bristol.dundry.webresources.providers;
 
 import com.google.common.base.Function;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.ImmutableBiMap;
 import com.google.common.collect.Maps;
 import com.google.common.io.CharStreams;
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import java.io.*;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.util.*;
 import java.util.Map.Entry;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.MessageBodyReader;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Provider;
 import javax.xml.stream.*;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.mapped.MappedNamespaceConvention;
 import org.codehaus.jettison.mapped.MappedXMLStreamReader;
 import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.bristol.dundry.dao.Repository;
 import uk.ac.bristol.dundry.model.ResourceCollection;
 
 /**
  *
  * A Configurable provider that provides a very simple-minded RDF <-> JSON
  * object mapping.
  * 
  * Note we have a limitation of the type system here: We want to write
  * Resources and List&lt;Resources>. I suppose we could always deal with the
  * latter via singleton lists?
  * 
  * @author Damian Steer <d.steer@bris.ac.uk>
  */
 @Provider
 @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 public class RdfResourceMappingProvider 
     implements MessageBodyWriter<Object>, MessageBodyReader<Resource> {
     
     final static Logger log = LoggerFactory.getLogger(RdfResourceMappingProvider.class);
     
     final static XMLOutputFactory OUT_FAC = XMLOutputFactory.newInstance();
     final static XMLInputFactory IN_FAC = XMLInputFactory.newInstance();
     
     private final BiMap<String, Property> keyToProperty;
     private final List<String> asArrays;
     
     // '{"item":[]}' in bytes
     private static final byte[] EMPTY_JSON_RESULT = 
             new byte[]{ 123, 34, 105, 116, 101, 109, 34, 58, 91, 93, 125 };
     
     /**
      * Create a provider (no array mapping)
      * 
      * @param k2p key to property map
      */
     public RdfResourceMappingProvider(Map<String, String> keyToProp) {
         this(keyToProp, Collections.EMPTY_LIST);
     }
     
     /**
      * Create a provider
      * 
      * @param k2p key to property map
      * @param asArrays keys to serialise as arrays in json
      */
     public RdfResourceMappingProvider(Map<String, String> keyToProp, List<String> asArrays) {
         
         this.asArrays = asArrays;
         
         Map<String, String> keyToPropDefaults = new HashMap<>();
         
         // Default label mapping
         keyToPropDefaults.put("label", RDFS.label.getURI());
         
         // Add given mappings in
         keyToPropDefaults.putAll(keyToProp);
         
         // Map string value to a property
         Map<String, Property> k2p = Maps.transformValues(keyToPropDefaults,
                  new Function<String, Property>() {
 
             @Override
             public Property apply(String f) {
                 return ResourceFactory.createProperty(f);
             }
                     
                 });
         
         // Create a bimap from that map (so we can look up either way)
         keyToProperty = ImmutableBiMap.copyOf(k2p);
     }
     
     /**
      * Create a provider
      * 
      * @param vocabs list of vocabularies to use
      * @param asArrays keys to serialise as arrays in json
      */
     public RdfResourceMappingProvider(List<String> vocabs, List<String> asArrays) {
         
         this.asArrays = asArrays;
         Map<String, Property> k2p = new HashMap<>();
         
         // Default label mapping
         k2p.put("label", RDFS.label);
         
         // Load each vocabulary, and then hunt for properties
         for (String vocab: vocabs) {
             Model vocabModel = FileManager.get().loadModel(vocab);
             
             // Look for properties in this model
             ResIterator propIt = vocabModel.listResourcesWithProperty(RDF.type, RDF.Property);
             ResIterator objPropIt = vocabModel.listResourcesWithProperty(RDF.type, OWL.ObjectProperty);
             ResIterator dtPropIt = vocabModel.listResourcesWithProperty(RDF.type, OWL.DatatypeProperty);
             
             // Chain results together
             ExtendedIterator<Resource> allProps = propIt.andThen(objPropIt).andThen(dtPropIt);
             
             while (allProps.hasNext()) {
                 Property prop = allProps.next().as(Property.class);
                 String localName = prop.getLocalName();
                 
                 // If localName (the putative key) is, in plural form, in asArrays
                 // use that instead
                 // (fits existing API, but not great. Ought to use property name)
                 if (asArrays.contains(plural(localName))) localName = plural(localName);
                 
                 // Cases: no local name, can't map.
                 // Value clash (is that possible?) x -> prop and y -> prop
                 // Key clash x -> prop1 and x -> prop2. We create x1, x2 ...
                 if (localName == null) log.error("Property <{}> has no local name", prop);
                 else if (k2p.containsValue(prop)) log.warn("Already have key for <{}>", prop);
                 else if (k2p.containsKey(localName)) {
                     String nextKey = getAnotherKey(k2p, localName);
                     log.warn("Mapping <{}> to '{}'", prop, nextKey);
                     k2p.put(nextKey, prop);
                 } else k2p.put(localName, prop);
             }
         }
         
         keyToProperty = ImmutableBiMap.copyOf(k2p);
     }
     
     // VERY ropey pluraliser!
     private static String plural(String name) {
         if (name.endsWith("s")) return name;
         else return name + "s";
     }
     
     // Find an unused key for a map, based on the param key.
     private static String getAnotherKey(Map<String, ?> map, String key) {
         int i = 1;
         while (map.containsKey(key + i)) i++; // loop until we find one
         return key + i;
     } 
     
     /**
      * Write a resource and properties to a simple XML / JSON format,
      * mapping properties to keys as it goes.
      * Unmappable properties are lost.
      * @param resource Resource to write
      * @param writer The XML / JSON writer
      * @throws XMLStreamException 
      */
     protected void map(Resource resource, XMLStreamWriter writer) throws XMLStreamException {
        int valsWritten = 0; // Track number written to work around bug latter
        
         // If present, write the id of the resource as id
         if (resource.isURIResource()) {
            valsWritten += 1;
             writer.writeStartElement("id");
             // TODO: Not convinced toExternalId should be here. We ought to be passing
             // around relative uris. Hmm. Hmm.
             writer.writeCharacters(Repository.toExternalId(resource.getURI()));
             writer.writeEndElement();
         }
         
         // Step through each mappable property and extract values for resource
         // This ensures properties are grouped together, which is better (?)for 
         for (Entry<Property, String> e: keyToProperty.inverse().entrySet()) {
             if (resource.hasProperty(e.getKey())) {
                 // Get values as a list
                 List<Statement> values = resource.listProperties(e.getKey()).toList();
                 for (Statement s: values) {
                    valsWritten += 1;
                     writer.writeStartElement(e.getValue());
                     if (s.getObject().isResource()) {
                         map(s.getObject().asResource(), writer);
                     }
                     // Otherwise write the value as a string (limited :-()
                     else {
                         writer.writeCharacters(s.getLiteral().getLexicalForm());
                     }
                     writer.writeEndElement();
                 }                
             }
         }
        
        // Work around for ActiveResource bug:
        // If there is only one value in an array hash element it will flatten
        // excessively. For example [ { "a": "1" } { "a": "2" } ] becomes [ "1", "2" ]
        if (valsWritten == 1) {
            writer.writeStartElement("ignore");
            writer.writeCharacters("");
            writer.writeEndElement();
        }
     }
 
     @Override
     public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
         
         return Resource.class.isAssignableFrom(type) ||
                 ResourceCollection.class.isAssignableFrom(type);
     }
     
     @Override
     public long getSize(Object t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
         return -1;
     }
 
     @Override
     public void writeTo(Object t, Class<?> type, Type type1, Annotation[] antns,
         MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) 
             throws IOException, WebApplicationException {
         // We either have a resource, or resource collection
         // Make a singleton resource collection to simplify things
         ResourceCollection resources = (t instanceof ResourceCollection) ?
                 (ResourceCollection) t :
                 new ResourceCollection(Collections.singleton((Resource) t));
         
         // Special case that I can't persuade the JSON writer to deal with
         // (it writes [""] rather than [])
         if (resources.isEmpty() && mt.equals(MediaType.APPLICATION_JSON_TYPE)) {
             out.write(EMPTY_JSON_RESULT);
             return;
         }
         
         try {
             // Get appropriate writer for mediatype, and whether this is an
             // individual or list context
             XMLStreamWriter streamWriter = 
                     getWriterFor(mt, out, (t instanceof ResourceCollection));
             streamWriter.writeStartDocument();
             for (Resource r: resources) {
                 streamWriter.writeStartElement("item");
                 map(r, streamWriter);
                 streamWriter.writeEndElement();
             }
             streamWriter.writeEndDocument();
             streamWriter.flush();
             streamWriter.close();
         } catch (XMLStreamException ex) {
             throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
         }
     }
     
     /**
      * Return an 'XML' writer suitable for the mimetype. May return a fake
      * XML writer ;-)
      * 
      * @param mimeType
      * @param out
      * @param inListContext Whether we are serialising a list of items
      * @return
      * @throws XMLStreamException 
      */
     protected XMLStreamWriter getWriterFor(MediaType mimeType, OutputStream out, boolean inListContext) 
             throws XMLStreamException, UnsupportedEncodingException {
         switch (mimeType.toString()) {
             case "application/json":
                 Writer outW = new OutputStreamWriter(out, "UTF-8");
                 MappedNamespaceConvention con = new MappedNamespaceConvention();
                 MappedXMLStreamWriter writer = new MappedXMLStreamWriter(con, outW);
                 // Copy over items to always serialise as arrays
                 for (String key: asArrays) writer.serializeAsArray(key);
                 // If we are serialising a list <item> should always be an array
                 if (inListContext) writer.serializeAsArray("item");
                 return writer;
             default:
                 return OUT_FAC.createXMLStreamWriter(out);
         }
     }
     
     protected XMLStreamReader getReaderFor(MediaType mimeType, InputStream in) 
             throws XMLStreamException, JSONException, IOException {
         switch (mimeType.toString()) {
             case "application/json":
                 // This seems too complex. IS -> String -> JSONObject -> XSR
                 String jsonIn = CharStreams.toString(new InputStreamReader( in, "UTF-8" ) );
                 JSONObject o = new JSONObject(jsonIn);
                 MappedNamespaceConvention con = new MappedNamespaceConvention();
                 return new MappedXMLStreamReader(o, con);
             default:
                 return IN_FAC.createXMLStreamReader(in);
         }
     }
     
     @Override
     public boolean isReadable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
         return Resource.class.isAssignableFrom(type);
     }
 
     @Override
     public Resource readFrom(Class<Resource> type, Type type1, Annotation[] antns, 
         MediaType mt, MultivaluedMap<String, String> mm, InputStream in)
             throws IOException, WebApplicationException {
         try {
             XMLStreamReader streamReader = getReaderFor(mt, in);
             return parse(streamReader);
         } catch (XMLStreamException | JSONException ex) {
             throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
         }
     }
 
     private Resource parse(XMLStreamReader reader) throws XMLStreamException {
         
         Model m = ModelFactory.createDefaultModel();
         
         // Move to the root object
         reader.nextTag();
         
         return (Resource) parseRDFNode(reader, m);
     }
 
     private RDFNode parseRDFNode(XMLStreamReader reader, Model m) throws XMLStreamException {
         String id = null;
         StringBuilder content = null;
         List<Attribute> properties = null;
         int level = 0;
         while (reader.hasNext()) {
             int et = reader.next();
             
             if (XMLStreamReader.CHARACTERS == et) {
                 if (properties != null)
                     log.warn("Characters found unexpectedly. Mixed content. Continuing...");
                 if (content == null) content = new StringBuilder();
                 content.append(reader.getText());
                 //System.err.println("Chars: " + reader.getText());
             }
             else if (XMLStreamReader.START_ELEMENT == et) {
                 if (content != null)
                     log.warn("Tag found unexpectedly. Mixed content. Continuing...");
                 if ("id".equals(reader.getLocalName())) {
                     // id is special.
                     id = reader.getElementText();
                     
                     // Jettison bug. We should be at end element now
                     if (!reader.isEndElement()) reader.next();
                 }
                 else {
                     String name = reader.getLocalName();
                     if (!keyToProperty.containsKey(name)) {
                         // SKIP...
                         level++;
                     } else {
                         if (properties == null) properties = new LinkedList<>();
                         
                         //System.err.println("START: " + reader.getLocalName());
                         Attribute a = 
                                 new Attribute(
                                     keyToProperty.get(name),
                                     parseRDFNode(reader, m));
                         //System.err.printf("Add property: <%s> <%s>\n", a.predicate, a.value);
                         properties.add(a);
                     }
                 }
             }
             else if (XMLStreamReader.END_ELEMENT == et) {
                 //System.err.println("END: " + level + " " + reader.getLocalName());
                 level--;
                 if (level < 0) break;
             }
         }
         
         /** We treat empty content as missing on the RDF side **/
         if (content != null && content.length() == 0) return null;
         if (id != null && id.isEmpty()) return null;
         
         /** We have content: create a literal **/
         if (content != null) return m.createLiteral(content.toString());
         
         /** Otherwise it's a resource (anon or named) **/
         Resource r = (id == null || id.isEmpty()) ? 
                 m.createResource() : 
                 m.createResource(Repository.toInternalId(id));
         
         // Copy over retrieved properties
         if (properties != null) {
             for (Attribute a: properties) {
                 // If object is not null add property
                 if (a.value != null) r.addProperty(a.predicate, a.value);
             }
         }
         
         return r;
     }
     
     /**
      * A struct for holding partial triples
      */
     static class Attribute {
         final Property predicate;
         final RDFNode value;
         
         public Attribute(Property predicate, RDFNode value) {
             this.predicate = predicate;
             this.value = value;
         }
     }
 }
