 package eu.dm2e.ws.grafeo.jena;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.AnonId;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.update.UpdateExecutionFactory;
 import com.hp.hpl.jena.update.UpdateFactory;
 import com.hp.hpl.jena.update.UpdateProcessor;
 import com.hp.hpl.jena.update.UpdateRequest;
 
 import eu.dm2e.ws.grafeo.GLiteral;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.GStatement;
 import eu.dm2e.ws.grafeo.GValue;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.SkolemnizationMethod;
 import eu.dm2e.ws.grafeo.annotations.Namespaces;
 import eu.dm2e.ws.grafeo.annotations.RDFId;
 import eu.dm2e.ws.grafeo.gom.ObjectMapper;
 
 
 
 public class GrafeoImpl extends JenaImpl implements Grafeo {
 
     private Logger log = Logger.getLogger(getClass().getName());
     protected Model model;
     protected Map<String, String> namespaces = new HashMap<>();
     protected ObjectMapper objectMapper;
 
     public static String SPARQL_CONSTRUCT_EVERYTHING = "CONSTRUCT { ?s ?p ?o } WHERE { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } }";
 
     public GrafeoImpl() {
         this(ModelFactory.createDefaultModel());
     }
 
     public GrafeoImpl(String uri) {
         this(ModelFactory.createDefaultModel());
         this.load(uri);
     }
 
     public GrafeoImpl(InputStream input, String lang) {
         this(ModelFactory.createDefaultModel());
         this.model.read(input, null, lang);
     }
 
     public GrafeoImpl(InputStream input) {
         this(ModelFactory.createDefaultModel());
         this.readHeuristically(input);
     }
 
     public GrafeoImpl(File file) {
         this(ModelFactory.createDefaultModel());
         this.readHeuristically(file);
     }
 
     /**
      * Creates a model from a given string and a content format. If the content
      * format is null the format is guessed.
      *
      * @param content       the content as string
      * @param contentFormat the format of the content. If null it will be guessed.
      */
     public GrafeoImpl(String content, String contentFormat) {
         this(ModelFactory.createDefaultModel());
         if (null == contentFormat) {
             this.readHeuristically(content);
         } else {
             try {
                 this.model.read(content, null, contentFormat);
             } catch (Throwable t0) {
                 throw new RuntimeException("Could not parse input: " + content
                         + " for given content format " + contentFormat, t0);
             }
         }
     }
 
 
     @Override
     public void setNamespace(String prefix, String namespace) {
         namespaces.put(prefix, namespace);
         model.setNsPrefix(prefix, namespace);
     }
 
     @Override
     public GResourceImpl findTopBlank() {
         ResIterator it = model.listSubjects();
         Resource fallback = null;
         while (it.hasNext()) {
             Resource res = it.next();
             if (res.isAnon()) {
                 fallback = res;
                 if (model.listStatements(null, null, res).hasNext())
                     continue;
                 return new GResourceImpl(this, res);
             }
         }
         return fallback != null ? new GResourceImpl(this, fallback) : null;
     }
     
     @Override
     public GResourceImpl findTopBlank(String uri) {
         ResIterator it = model.listSubjects();
         GResourceImpl typeObjectGResource = new GResourceImpl(this, uri);
         GResourceImpl typePropertyGResource = new GResourceImpl(this, "rdf:type");
         Resource typeObjectResource = typeObjectGResource.getResource();
         Property typeProperty = model.createProperty(typePropertyGResource.getUri());
         while (it.hasNext()) {
             Resource res = it.next();
             if (res.isAnon()) {
                 if (model.listStatements(null, null, res).hasNext())
                     continue;
                 if (model.listStatements(res, typeProperty, typeObjectResource).hasNext()) {
 	                return new GResourceImpl(this, res);
                 }
             }
         }
         return null;
     }
 
     @Override
     public Set<GResource> findByClass(String uri) {
         ResIterator it = model.listSubjects();
         GResourceImpl typeObjectGResource = new GResourceImpl(this, uri);
         GResourceImpl typePropertyGResource = new GResourceImpl(this, "rdf:type");
         Resource typeObjectResource = typeObjectGResource.getResource();
         Property typeProperty = model.createProperty(typePropertyGResource.getUri());
         Set<GResource> result = new HashSet<>();
         while (it.hasNext()) {
             Resource res = it.next();
                 if (model.listStatements(null, null, res).hasNext())
                     continue;
                 if (model.listStatements(res, typeProperty, typeObjectResource).hasNext()) {
                     result.add(new GResourceImpl(this, res));
                 }
 
         }
         return result;
     }
 
 
     public GrafeoImpl(Model model) {
         this.model = model;
         initDefaultNamespaces();
         applyNamespaces(model);
     }
 
    @Override
     public void readHeuristically(String contentStr) {
         InputStream content = new ByteArrayInputStream(contentStr.getBytes());
         readHeuristically(content);
     }
 
     @Override
     public void readHeuristically(InputStream input) {
         try {
             this.model.read(input, null, "N3");
         } catch (Throwable t) {
             try {
                 this.model.read(input, null, "RDF/XML");
             } catch (Throwable t2) {
                 // TODO Throw proper exception that is converted to a proper
                 // HTTP response in DataService
                 throw new RuntimeException("Could not parse input: " + input, t);
             }
         }
     }
 
     @Override
     public void readHeuristically(File file) {
         FileInputStream fis;
         try {
             fis = new FileInputStream(file);
         } catch (FileNotFoundException e) {
             throw new RuntimeException("File not found:  " + file.getAbsolutePath(), e);
         }
         readHeuristically(fis);
     }
     
     @Override
     public void load(String uri) {
     	this.load(uri, 0);
     }
 
     @Override
     public void load(String uri, int expansionSteps) {
         log.fine("Load data from URI: " + uri);
         uri = expand(uri);
         try {
         	// NOTE: read(String uri) does content-negotiation - sort of
             this.model.read(uri);
             log.info("Content read.");
         } catch (Throwable t) {
             try {
                 this.model.read(uri, null, "RDF/XML");
                 log.info("Content read, found RDF/XML.");
             } catch (Throwable t2) {
                 // TODO Throw proper exception that is converted to a proper
                 // HTTP response in DataService
                 log.severe("Could not parse URI content: " + t2.getMessage());
                 t.printStackTrace();
                 t2.printStackTrace();
                 throw new RuntimeException("Could not parse uri content: " + uri, t2);
             }
         }
         // Expand the graph by recursively loading additional resources
         Set<GResource> resourceCache = new HashSet<GResource>();
         for ( ; expansionSteps > 0 ; expansionSteps--) {
         	log.info("Expansion No. " + expansionSteps);
         	log.info("Before expansion: "+ this.size());
         	for (GResource gres : this.listResourceObjects()) {
         		if (resourceCache.contains(gres)){
         			continue;
         		}
 				try {
 					this.load(gres.getUri(), 0);
 				} catch (Throwable t) {
 					log.info("Failed to load resource " + gres.getUri() +".");
 //					t.printStackTrace();
 				}
 				resourceCache.add(gres);
     		}
         	log.info("After expansion: "+ this.size());
         }
 
     }
 
     @Override
     public void loadWithoutContentNegotiation(String uri) {
         log.fine("Load data from URI: " + uri);
         uri = expand(uri);
         try {
             this.model.read(uri, null, "N3");
             log.info("Content read, found N3.");
         } catch (Throwable t) {
             try {
                 this.model.read(uri, null, "RDF/XML");
                 log.info("Content read, found RDF/XML.");
             } catch (Throwable t2) {
                 // TODO Throw proper exception that is converted to a proper
                 // HTTP response in DataService
                 log.severe("Could not parse URI content: " + t2.getMessage());
                 throw new RuntimeException("Could not parse uri content: "
                         + uri, t2);
             }
         }
     }
 
     @Override
     public void loadWithoutContentNegotiation(String uri, int expansionSteps) {
         log.fine("Load data from URI: " + uri);
         uri = expand(uri);
         try {
             this.model.read(uri, null, "N3");
             log.info("Content read, found N3.");
         } catch (Throwable t) {
             try {
                 this.model.read(uri, null, "RDF/XML");
                 log.info("Content read, found RDF/XML.");
             } catch (Throwable t2) {
                 // TODO Throw proper exception that is converted to a proper
                 // HTTP response in DataService
                 log.severe("Could not parse URI content: " + t2.getMessage());
                 throw new RuntimeException("Could not parse uri content: "
                         + uri, t2);
             }
         }
         // Expand the graph by recursively loading additional resources
         Set<GResource> resourceCache = new HashSet<GResource>();
         for ( ; expansionSteps > 0 ; expansionSteps--) {
             log.info("Expansion No. " + expansionSteps);
             log.info("Before expansion: "+ this.size());
             for (GResource gres : this.listResourceObjects()) {
                 if (resourceCache.contains(gres)){
                     continue;
                 }
                 try {
                     this.loadWithoutContentNegotiation(gres.getUri(), 0);
                 } catch (Throwable t) {
                     log.info("Failed to load resource " + gres.getUri() +".");
 //					t.printStackTrace();
                 }
                 resourceCache.add(gres);
             }
             log.info("After expansion: "+ this.size());
         }
     }
 
 
     protected GResource getGResource(Object object) {
         String uri = null;
 
         for (Field field : object.getClass().getDeclaredFields()) {
             log.fine("Field: " + field.getName());
             if (field.isAnnotationPresent(RDFId.class)) {
                 try {
                     Object id = PropertyUtils.getProperty(object, field.getName());
                     if (null == id || "0".equals(id.toString()) ) return new GResourceImpl(this, model.createResource(AnonId.create(object.toString())));
                     uri = field.getAnnotation(RDFId.class).prefix() + id.toString();
                 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
             }
         }
         if (uri==null) {
             return new GResourceImpl(this, model.createResource(AnonId.create(object.toString())));
         } else {
             uri = expand(uri);
             return new GResourceImpl(this, model.createResource(uri));
         }
 
     }
 
     protected void setAnnotatedNamespaces(Object object) {
         String key = null;
         Namespaces annotation = object.getClass().getAnnotation(Namespaces.class);
         if (annotation == null) return;
         for (String s : annotation.value()) {
             if (key == null) {
                 key = s;
             } else {
                 setNamespace(key, s);
                 key = null;
             }
         }
 
     }
 
     @Override
     public void empty() {
         model.removeAll();
     }
 
     @Override
     public GResourceImpl get(String uri) {
         uri = expand(uri);
         return new GResourceImpl(this, uri);
     }
 
     @Override
     public String expand(String uri) {
         return model.expandPrefix(uri);
     }
     
     @Override
     public String shorten(String uri) {
         return model.shortForm(uri);
     }
 
     @Override
     public GStatementImpl addTriple(String subject, String predicate,
                                     String object) {
         GResourceImpl s = new GResourceImpl(this, subject);
         GResourceImpl p = new GResourceImpl(this, predicate);
 
         GStatementImpl statement;
         String objectExp = expand(object);
         try {
 //            URI testUri = 
         	new URI(objectExp);
             GResourceImpl or = new GResourceImpl(this, object);
             statement = new GStatementImpl(this, s, p, or);
         } catch (URISyntaxException e) {
             statement = new GStatementImpl(this, s, p, object);
         }
         model.add(statement.getStatement());
         return statement;
     }
 
     @Override
     public GStatementImpl addTriple(String subject, String predicate,
                                     GValue object) {
         GResourceImpl s = new GResourceImpl(this, subject);
         GResourceImpl p = new GResourceImpl(this, predicate);
         GStatementImpl statement = new GStatementImpl(this, s, p, object);
         model.add(statement.getStatement());
         return statement;
     }
     @Override
     public GStatementImpl addTriple(GResource subject, GResource predicate,
                                     GValue object) {
         GStatementImpl statement = new GStatementImpl(this, subject, predicate, object);
         model.add(statement.getStatement());
         return statement;
     }
 
     @Override
     public GLiteralImpl literal(String literal) {
         return new GLiteralImpl(this, literal);
     }
 
     @Override
     public GLiteralImpl literal(Object value) {
         return new GLiteralImpl(this, value);
     }
 
     @Override
     public GResourceImpl resource(String uri) {
         uri = expand(uri);
         return new GResourceImpl(this, uri);
     }
 
     @Override
     public GResourceImpl createBlank() {
         return new GResourceImpl(this, model.createResource(AnonId.create()));
     }
 
     @Override
     public GResourceImpl createBlank(String id) {
         return new GResourceImpl(this, model.createResource(AnonId.create(id)));
     }
 
     @Override
     public GResourceImpl resource(URI uri) {
         return new GResourceImpl(this, uri.toString());
     }
 
     @Override
     public boolean isEscaped(String input) {
         return input.startsWith("\"") && input.endsWith("\"")
                 && input.length() > 1 || input.startsWith("<")
                 && input.endsWith(">") && input.length() > 1;
     }
 
     @Override
     public String unescapeLiteral(String literal) {
         if (isEscaped(literal)) {
             return literal.substring(1, literal.length() - 1);
         }
         return literal;
     }
 
     @Override
     public String escapeLiteral(String literal) {
         return "\"" + literal + "\"";
     }
 
     @Override
     public String unescapeResource(String uri) {
         if (isEscaped(uri)) {
             return uri.substring(1, uri.length() - 1);
         }
         return uri;
     }
 
     @Override
     public String escapeResource(String uri) {
         if (isEscaped(uri))
             return uri;
         return "<" + uri + ">";
     }
 
     @Override
     public void readFromEndpoint(String endpoint, String graph, int expansionSteps) {
         StringBuilder sb = new StringBuilder(
                 "CONSTRUCT {?s ?p ?o}  WHERE { GRAPH <");
         sb.append(graph);
         sb.append("> {");
         sb.append("?s ?p ?o");
         sb.append("} . }");
         Query query = QueryFactory.create(sb.toString());
         log.finest("Query: " + sb.toString());
         QueryExecution exec = QueryExecutionFactory.createServiceRequest(
                 endpoint, query);
         long sizeBefore = size();
         exec.execConstruct(model);
         long stmtsAdded = size() - sizeBefore;
         if (stmtsAdded == 0) {
         	throw new RuntimeException("Graph contained no statements: " + graph);
         }
         log.info("Added " + stmtsAdded + " statements to the graph.");
         
         // Expand the graph by recursively loading additional resources
         Set<GResource> resourceCache = new HashSet<GResource>();
         for ( ; expansionSteps > 0 ; expansionSteps--) {
         	log.info("SCHMExpansion No. " + expansionSteps);
     		log.info("LIVELIVE");
         	log.info("Before expansion: "+ this.size());
         	for (GResource gres : this.listResourceObjects()) {
         		if (resourceCache.contains(gres)){
         			continue;
         		}
         		try {
         			log.info("Reading graph " + graph + " from endpoint " + endpoint + ".");
         			this.readFromEndpoint(endpoint, graph, 0);
         		} catch (Throwable t) {
         			log.info("Graph not found in endpoint: " + graph);
         			try {
 						this.load(gres.getUri(), 0);
 	        		} catch (Throwable t2) {
 	        			log.warning("URI un-dereferenceable: " + graph);
 	        			log.warning("Continuing because this concerns only nested resources.");
 //	        			throw(t2);
 	        		}
         		}
         		log.severe("LIVE");
 				resourceCache.add(gres);
     		}
         	log.info("After expansion: "+ this.size());
         }
         log.info("Reading from endpoint finished.");
     }
     
     @Override
 	public void readFromEndpoint(String endpoint, String graph) {
     	this.readFromEndpoint(endpoint, graph, 0);
     }
 
     @Override
     public void readFromEndpoint(String endpoint, URI graphURI) {
         readFromEndpoint(endpoint, graphURI.toString());
     }
 
     @Override
     public void readTriplesFromEndpoint(String endpoint, String subject,
                                         String predicate, GValue object) {
 
         if (subject != null)
             subject = escapeResource(expand(subject));
         if (predicate != null)
             predicate = escapeResource(expand(predicate));
 
         StringBuilder sb = new StringBuilder("CONSTRUCT {");
         sb.append(subject != null ? subject : "?s").append(" ");
         sb.append(predicate != null ? predicate : "?p").append(" ");
         sb.append(object != null ? object.toEscapedString() : "?o").append(" ");
         sb.append("}  WHERE { ");
         sb.append(subject != null ? subject : "?s").append(" ");
         sb.append(predicate != null ? predicate : "?p").append(" ");
         sb.append(object != null ? object.toEscapedString() : "?o").append(" ");
         sb.append(" }");
         log.finest("Query: " + sb.toString());
         Query query = QueryFactory.create(sb.toString());
         QueryExecution exec = QueryExecutionFactory.createServiceRequest(
                 endpoint, query);
         exec.execConstruct(model);
     }
 
     @Override
     public void writeToEndpoint(String endpoint, String graph) {
         StringBuilder sb = new StringBuilder("CREATE SILENT GRAPH <");
         sb.append(graph);
         sb.append(">");
         log.finest("Query 1: " + sb.toString());
         UpdateRequest update = UpdateFactory.create(sb.toString());
         sb = new StringBuilder("INSERT DATA { GRAPH <");
         sb.append(graph);
         sb.append("> {");
         sb.append(getNTriples());
         sb.append("}}");
         log.finest("Query 2: " + sb.toString());
         update.add(sb.toString());
         UpdateProcessor exec = UpdateExecutionFactory.createRemoteForm(update,
                 endpoint);
         exec.execute();
 
     }
 
     @Override
     public void writeToEndpoint(String endpoint, URI graphURI) {
         writeToEndpoint(endpoint, graphURI.toString());
     }
 
     @Override
     public GLiteral now() {
         return date(new Date().getTime());
     }
 
     @Override
     public GLiteral date(Long timestamp) {
         Calendar cal = GregorianCalendar.getInstance();
         cal.setTimeInMillis(timestamp);
         Literal value = model.createTypedLiteral(cal);
         return new GLiteralImpl(this, value);
     }
 
     @Override
     public String getNTriples() {
         StringWriter sw = new StringWriter();
         model.write(sw, "N-TRIPLE");
         return sw.toString();
     }
     
     @Override
     public String getCanonicalNTriples() {
         StringWriter sw = new StringWriter();
         model.write(sw, "N-TRIPLE");
         String[] lines = sw.toString().split("\n");
         Arrays.sort(lines);
         return StringUtils.join(lines,"\n");
     }
     
     @Override
     public String getTurtle() {
         StringWriter sw = new StringWriter();
         model.write(sw, "TURTLE");
         return sw.toString();
     }
 
     @Override
     public long size() {
         return model.size();
     }
 
 
     protected void applyNamespaces(Model model) {
         for (String prefix : namespaces.keySet()) {
             model.setNsPrefix(prefix, namespaces.get(prefix));
         }
     }
 
     public Model getModel() {
         return model;
 
     }
     
     @Override
     public void executeSparqlUpdate(String queryString, String endpoint) {
         UpdateRequest update = UpdateFactory.create(queryString);
         UpdateProcessor exec = UpdateExecutionFactory.createRemoteForm(update, endpoint);
         exec.execute();
     }
 
     @Override
     public boolean executeSparqlAsk(String queryString) {
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, model);
         return qe.execAsk();
     }
 
     @Override
     public boolean containsStatementPattern(String s, String p, String o) {
     	if (s == null) {
     		s = "?s";
     	} else  {
 	        s = s.startsWith("?") ? s : "<" + expand(s) + ">";
     	}
     	if (p == null) {
     		p = "?p";
     	} else  {
 	        p = p.startsWith("?") ? p : "<" + expand(p) + ">";
     	}
     	if (o == null) {
     		o = "?o";
     	} else  {
 	        o = o.startsWith("?") ? o : "<" + expand(o) + ">";
     	}
         String queryString = String.format("ASK { %s %s %s }", s, p, o);
         log.info(queryString);
         return executeSparqlAsk(queryString);
     }
 
     @Override
     public boolean containsStatementPattern(String s, String p, GLiteral o) {
     	if (s == null) {
     		s = "?s";
     	} else  {
 	        s = s.startsWith("?") ? s : "<" + expand(s) + ">";
     	}
     	if (p == null) {
     		p = "?p";
     	} else  {
 	        p = p.startsWith("?") ? p : "<" + expand(p) + ">";
     	}
     	String oStr = "?o";
     	if (o != null) {
 	        oStr = o.getTypedValue();
     	}
         String queryString = String.format("ASK { %s %s %s }", s, p, oStr);
         log.info("ASK query: " + queryString);
         return executeSparqlAsk(queryString);
     }
 
     @Override
     public ResultSet executeSparqlSelect(String queryString) {
         log.info("SELECT query: " + queryString);
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, model);
         return qe.execSelect();
     }
 
     @Override
     public GrafeoImpl executeSparqlConstruct(String queryString) {
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, model);
         return new GrafeoImpl(qe.execConstruct());
     }
 
     @Override
     public boolean containsResource(String g) {
         String gUri = expand(g);
         return model.containsResource(model.getResource(gUri));
     }
 
     public boolean containsResource(URI graphURI) {
         return containsResource(graphURI.toString());
     }
 
     public GValueImpl firstMatchingObject(String s, String p) {
         s = s.startsWith("?") ? s : "<" + expand(s) + ">";
         p = p.startsWith("?") ? p : "<" + expand(p) + ">";
         ResultSet iter = this.executeSparqlSelect(String.format("SELECT ?o { %s %s ?o } LIMIT 1", s, p));
         if (!iter.hasNext())
             return null;
 //        return new GValueImpl(this, iter.next().get("?o"));
         RDFNode jenaNode = iter.next().get("?o");
         if (jenaNode.isLiteral()) {
         	return new GLiteralImpl(this, (Literal) jenaNode);
         }
         else if (jenaNode.isURIResource()) {
         	return new GResourceImpl(this, (Resource) jenaNode);
         }
         return null;
         // TODO handle blank nodes
     }
     
 	@Override
 	public void emptyGraph(String endpoint, String graph) {
 		String updateStr = new SparqlUpdate.Builder()
 			.graph(graph)
 			.delete("?s ?p ?o.")
 			.build().toString();
         log.info("Empty Graph query: " + updateStr);
         this.executeSparqlUpdate(updateStr, endpoint);
 	}
 
 
     @Override
     public boolean isEmpty() {
         return model.isEmpty();
     }
 
     protected void initDefaultNamespaces() {
         // TODO: Put this in a config file (kai)
         namespaces.put("foaf", "http://xmlns.com/foaf/0.1/");
         namespaces.put("dct", "http://purl.org/dc/terms/");
         namespaces.put("co", "http://purl.org/co/");
         namespaces.put("dcterms", "http://purl.org/dc/terms/");
         namespaces.put("dc", "http://purl.org/dc/elements/1.1/");
         namespaces.put("skos", "http://www.w3.org/2004/02/skos/core#");
         namespaces.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
         namespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
         namespaces.put("owl", "http://www.w3.org/2002/07/owl#");
         namespaces.put("ogp", "http://ogp.me/ns#");
         namespaces.put("gr", "http://purl.org/goodrelations/v1#");
         namespaces.put("xsd", "http://www.w3.org/2001/XMLSchema#");
         namespaces.put("cc", "http://creativecommons.org/ns#");
         namespaces.put("bibo", "http://purl.org/ontology/bibo/");
         namespaces.put("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
         namespaces.put("sioc", "http://rdfs.org/sioc/ns#");
         namespaces.put("oo", "http://purl.org/openorg/");
         namespaces.put("void", "http://rdfs.org/ns/void#");
         namespaces.put("edm", "http://www.europeana.eu/schemas/edm/");
         namespaces.put("ore", "http://www.openarchives.org/ore/terms/");
         namespaces.put("dm2e", "http://onto.dm2e.eu/omnom/");
         namespaces.put("omnom", "http://onto.dm2e.eu/omnom/");
         namespaces.put("omnom_types", "http://onto.dm2e.eu/omnom-types/");
 
     }
 
     @Override
     public ObjectMapper getObjectMapper() {
         if (objectMapper==null) objectMapper = new ObjectMapper(this);
         return objectMapper;
     }
     
     @Override
     public Set<GResource> listResourceObjects() {
     	Set<GResource> resList = new HashSet<>();
     	NodeIterator iter = this.getModel().listObjects();
     	while (iter.hasNext()) {
     		RDFNode node = iter.next();
     		if (node.isURIResource()) {
     			resList.add(this.resource(node.asResource().getURI()));
     		}
     	}
     	return resList;
     }
     @Override
     public Set<GResource> listBlankObjects() {
     	Set<GResource> resList = new HashSet<GResource>();
     	NodeIterator iter = this.getModel().listObjects();
     	while (iter.hasNext()) {
     		RDFNode node = iter.next();
     		if (node.isAnon()) {
     			GResource gres = new GResourceImpl(this, node.asResource());
     			resList.add(gres);
     		}
     	}
     	return resList;
     }
     
     @Override
     public void skolemnizeSequential(String subject, String predicate, String template) {
     	this.skolemnize(subject, predicate, template, SkolemnizationMethod.SEQUENTIAL_ID);
     }
     
     @Override
     public void skolemnizeUUID(String subject, String predicate, String template) {
     	this.skolemnize(subject, predicate, template, SkolemnizationMethod.RANDOM_UUID);
     }
     
 
 	@Override
 	public void skolemnize(String subject, String predicate, String template, SkolemnizationMethod method) {
 		subject = expand(subject);
 		predicate = expand(predicate);
 		Set<GResource> anonRes = new HashSet<>();
 		for (GStatement stmt : this.listAnonStatements(subject, predicate)) {
 			if (! anonRes.contains(stmt.getResourceValue())) {
 				anonRes.add(stmt.getResourceValue());
 			}
 		}
 		long i = 1;
 		for (GResource gres : anonRes) {
 			String randomId;
 			if (method.equals(SkolemnizationMethod.RANDOM_UUID)) {
 				randomId = UUID.randomUUID().toString();
 			}
 			else if (method.equals(SkolemnizationMethod.SEQUENTIAL_ID)) {
 				randomId = "" + i++;
 			}
 			else {
 				throw new RuntimeException("Unknown SkolemnizationMethod " + method.toString());
 			}
 			gres.rename(subject + "/" + template + "/" + randomId);
 		}
 	}
 	@Override
 	public Set<GStatement> listAnonStatements(String s, String p) {
 		return this.listAnonStatements(s, p, null);
 	}
 	@Override
 	public Set<GStatement> listAnonStatements(String s, String p, GResource o) {
 		Resource sS = null;
 		Property pP = null;
 		RDFNode oO = null;
 		if (s !=null)
 			sS = this.model.getResource(expand(s));
 		if (p != null)
 			pP = this.model.getProperty(expand(p));
 		if (o != null) {
 			// TODO how to get a Jena AnonId for a blank GResource?
 		}
 		StmtIterator iter = this.model.listStatements(sS, pP, oO);
 		Set<GStatement> matchingStmts = new HashSet<>();
 		while (iter.hasNext()) {
 			Statement jenaStmt = iter.next();
 			if (jenaStmt.getObject().isAnon()) {
 				GStatementImpl stmt = new GStatementImpl(this, jenaStmt);
 				matchingStmts.add(stmt);
 			}
 		}
 		return matchingStmts;
 	}
 	
 	@Override
 	public Set<GStatement> listResourceStatements(String s, String p, String o) {
 		Resource sS = null;
 		Property pP = null;
 		RDFNode oO = null;
 		if (s !=null)
 			sS = this.model.getResource(expand(s));
 		if (p != null)
 			pP = this.model.getProperty(expand(p));
 		if (o != null) 
			oO = this.model.getResource(expand(s));
 		StmtIterator iter = this.model.listStatements(sS, pP, oO);
 		Set<GStatement> matchingStmts = new HashSet<>();
 		while (iter.hasNext()) {
 			GStatementImpl stmt = new GStatementImpl(this, iter.next());
 			matchingStmts.add(stmt);
 		}
 		return matchingStmts;
 	}
 
     @Override
     public boolean isGraphEquivalent(Grafeo g) {
         GrafeoImpl gi = (GrafeoImpl) g;
         return getModel().isIsomorphicWith(gi.getModel());
     }
 }
