 package net.qldarch.web.service;
 
 import com.fasterxml.jackson.annotation.JsonAnyGetter;
 import com.fasterxml.jackson.annotation.JsonAnySetter;
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 import org.openrdf.model.Statement;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.StatementImpl;
 import org.openrdf.model.impl.URIImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.base.Predicates.notNull;
 

 @JsonIgnoreProperties({"ontology", "properties", "type"})
 public class RdfDescription {
     public static Logger logger = LoggerFactory.getLogger(RdfDescription.class);
 
     public static final URI RDF_TYPE = URI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
     private URI uri;
     private QldarchOntology ontology;
 
     private Multimap<URI, Object> properties;
 
     public RdfDescription() {
         this.properties = HashMultimap.create();
         this.ontology = null;
     }
 
     public List<URI> getType() {
         Collection<Object> typeObj = properties.get(RDF_TYPE);
         return ImmutableList.copyOf(filter(transform(typeObj, toURI), notNull()));
     }
 
     @JsonIgnore
     public void setURI(URI uri) {
         this.uri = uri;
     }
 
     @JsonProperty(value="uri")
     public URI getURI() {
         return this.uri;
     }
 
     @JsonProperty(value="uri")
     public void setUri(String uri) {
         try {
             this.uri = new URI(uri);
         } catch (URISyntaxException eu) {
             throw new IllegalArgumentException("Invalid uri", eu);
         }
     }
 
     @JsonAnySetter
     public void addProperty(String name, Object value) throws MetadataRepositoryException {
         URI nameURI = KnownPrefixes.resolve(name);
         properties.put(nameURI, value);
     }
 
     @JsonAnyGetter
     public Map<URI, Object> getProperties() {
         Map<URI, Object> result = Maps.newHashMap();
         for (URI key : properties.keySet()) {
             Collection<Object> entry = properties.get(key);
             switch (entry.size()) {
                 case 0:
                     continue;
                 case 1:
                     result.put(key, entry.toArray()[0]);
                     break;
                 default:
                     result.put(key, new ArrayList<Object>(entry));
             }
         }
 
         return result;
     }
 
     public Collection<Object> getValues(URI name) {
         return properties.get(name);
     }
 
     public void replaceProperty(URI name, Object value) {
         replaceProperty(name, Collections.singleton(value));
     }
 
     public void replaceProperty(URI name, Iterable<? extends Object> values) {
         properties.replaceValues(name, values);
     }
 
     private static Function<Object,URI> toURI = new Function<Object,URI>() {
         public URI apply(Object o) {
             if (!(o instanceof String)) {
                 return null;
             } else {
                 try {
                     return new URI((String)o);
                 } catch (URISyntaxException eu) {
                     logger.debug("Error in uri: {}", o, eu);
                     return null;
                 }
             }
         }
     };
 
     public Iterable<Statement> asStatements()
             throws MetadataRepositoryException {
         if (this.uri == null) {
             throw new MetadataRepositoryException("Generating statements for unidentifed subject");
         }
         return filter(transform(properties.entries(), toStatements()), notNull());
     }
 
     private Function<Map.Entry<URI,Object>,Statement> toStatements() 
             throws MetadataRepositoryException {
         final URIImpl subjectURI = new URIImpl(uri.toString());
 
         return new Function<Map.Entry<URI,Object>,Statement>() {
             public Statement apply(Map.Entry<URI,Object> entry) {
                 try {
                     URIImpl predicateURI = new URIImpl(entry.getKey().toString());
                     Object object = entry.getValue();
                     String objectString = object.toString();
                     Value objectValue = getOntology().convertObject(entry.getKey(), entry.getValue());
 
                     return new StatementImpl(subjectURI, predicateURI, objectValue);
                 } catch (MetadataRepositoryException em) {
                     return null;
                 }
             }
         };
     }
 
     public synchronized QldarchOntology getOntology() {
         if (this.ontology == null) {
             this.ontology = QldarchOntology.instance();
         }
         return this.ontology;
     }
 
     public synchronized void setOntology(QldarchOntology ontology) {
         this.ontology = ontology;
     }
 }
