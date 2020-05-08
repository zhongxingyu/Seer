 package eu.dm2e.ws.grafeo.jena;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.io.IOUtils;
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
 
 import eu.dm2e.ws.grafeo.GLiteral;
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.GStatement;
 import eu.dm2e.ws.grafeo.GValue;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.SkolemizationMethod;
 import eu.dm2e.ws.grafeo.annotations.Namespaces;
 import eu.dm2e.ws.grafeo.annotations.RDFId;
 import eu.dm2e.ws.grafeo.gom.ObjectMapper;
 
 public class GrafeoImpl extends JenaImpl implements Grafeo {
 	
     private static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
     private Logger log = Logger.getLogger(getClass().getName());
     protected Model model;
     private Map<String, String> namespaces = new HashMap<>();
     private Map<String, String> namespacesUsed = new HashMap<>();;
     protected ObjectMapper objectMapper;
 
     public static String SPARQL_CONSTRUCT_EVERYTHING = "CONSTRUCT { ?s ?p ?o } WHERE { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } }";
 
     public GrafeoImpl() {
         this(ModelFactory.createDefaultModel());
     }
 
     public GrafeoImpl(String uri) {
         this(ModelFactory.createDefaultModel());
         this.load(uri);
     }
     
     public GrafeoImpl(String uriOrStr, boolean interpretAsContent) {
         this(ModelFactory.createDefaultModel());
     	if (interpretAsContent) {
     		this.readHeuristically(uriOrStr);
     	} else {
 	        this.load(uriOrStr);
     	}
     }
 
     public GrafeoImpl(InputStream input, String lang) {
         this(ModelFactory.createDefaultModel());
         this.model.read(input, null, lang);
     }
 
     public GrafeoImpl(InputStream input) {
         this(ModelFactory.createDefaultModel());
         try {
 			this.readHeuristically(input);
 		} catch (IOException e) {
 			log.severe("Could not read from input stream.");
 //			throw(e);
 		}
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
         Resource typeObjectResource = typeObjectGResource.getJenaResource();
         Property typeProperty = model.createProperty(typePropertyGResource.getUri());
         while (it.hasNext()) {
             Resource res = it.next();
             if (! res.isAnon()) {
             	continue;
             }
             log.info("Checking resource: " + res);
 //            if (model.listStatements(null, null, res).hasNext())
 //            	continue;
             if (model.listStatements(res, typeProperty, typeObjectResource).hasNext()) {
             	return new GResourceImpl(this, res);
             }
         }
         return null;
     }
 
     @Override
     public Set<GResource> findByClass(String uri) {
         ResIterator it = model.listSubjects();
         GResourceImpl typeObjectGResource = new GResourceImpl(this, uri);
         GResourceImpl typePropertyGResource = new GResourceImpl(this, "rdf:type");
         Resource typeObjectResource = typeObjectGResource.getJenaResource();
         Property typeProperty = model.createProperty(typePropertyGResource.getUri());
         Set<GResource> result = new HashSet<>();
         while (it.hasNext()) {
             Resource res = it.next();
 //                if (model.listStatements(null, null, res).hasNext())
 //                    continue;
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
 
     public GrafeoImpl(URI uri) {
         this(ModelFactory.createDefaultModel());
         this.load(uri.toString());
     }
 
 	@Override
     public void readHeuristically(String contentStr) {
         try {
             this.model.read(IOUtils.toInputStream(contentStr), null, "N3");
         } catch (Throwable t) {
             try {
                 this.model.read(IOUtils.toInputStream(contentStr), null, "RDF/XML");
             } catch (Throwable t2) {
                 // TODO Throw proper exception that is converted to a proper HTTP response in DataService
                 throw new RuntimeException("Could not parse input either as N3 or RDF/XML: " + contentStr + "\n" + t + "\n" + t2);
             }
         }
     }
 
     @Override
     public void readHeuristically(InputStream input) throws IOException {
     	String contentStr;
 		try {
 			contentStr = IOUtils.toString(input);
 		} catch (IOException e) {
 			throw(e);
 		}
         readHeuristically(contentStr);
     }
 
     @Override
     public void readHeuristically(File file) {
         FileInputStream fis;
         try {
             fis = new FileInputStream(file);
 	        readHeuristically(fis);
         } catch (IOException e) {
             throw new RuntimeException("File not found:  " + file.getAbsolutePath(), e);
         }
     }
     
     @Override
     public void load(String uri) {
     	this.load(uri, 0);
     }
 
     @Override
     public void load(String uri, int expansionSteps) {
         log.fine("Load data from URI: " + uri);
         uri = expand(uri);
         int count = 3;
         boolean success = false;
         // Workaround, if just published content is not yet ready (and against other web problems)
         while (count > 0 && !success) {
             try {
                 // NOTE: read(String uri) does content-negotiation - sort of
                 this.model.read(uri);
                 log.info("Content read.");
                 success = true;
             } catch (Throwable t) {
                     log.severe("Could not parse URI content: " + t.getMessage());
                     throw new RuntimeException("Could not parse uri content: " + uri + " : " + t.getMessage());
             }
             count--;
             try {
                 Thread.sleep(500);
             } catch (InterruptedException e) {
                 throw new RuntimeException("An exception occurred: " + e, e);
             }
         }
         if (!success) throw new RuntimeException("After 3 tries I still couldn't make sense from this URI: " + uri);
         // Expand the graph by recursively loading additional resources
         Set<GResource> resourceCache = new HashSet<GResource>();
        log.info("Expansions to go: " + expansionSteps);
         for ( ; expansionSteps > 0 ; expansionSteps--) {
         	log.info("Expansion No. " + expansionSteps);
         	log.info("Before expansion: "+ this.size());
         	for (GResource gres : this.listURIResources()) {
         		if (resourceCache.contains(gres)){
         			continue;
         		}
 				try {
 					log.info("Trying to load resource " + gres.getUri());
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
                 log.warning("Could not parse URI content: " + t2.getMessage());
                 throw new RuntimeException("Could not parse URI content: " + uri + "\n"
                 		+"N3 Parser croaked: " + t.getMessage()
                 		+"\nRDFXML parser croaked: " + t2.getMessage());
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
             for (GResource gres : this.listURIResources()) {
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
         String expanded = model.expandPrefix(uri);
    	if (uri.indexOf(":") > 0) {
	    	String ns = uri.substring(0, uri.indexOf(":"));
	        if (! namespacesUsed.keySet().contains(ns) 
	        		&& 
	    		(expanded.length() > uri.length())) {
	        	this.namespacesUsed.put(ns, namespaces.get(ns));
	        }
    	}
         return expanded;
     }
     
     @Override
     public String shorten(String uri) {
         return model.shortForm(uri);
     }
 	@Override
 	public GStatement addTriple(GStatement stmt) {
 		// TODO this might break on blank nodes
 		GStatementImpl stmtImpl = new GStatementImpl(this, stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
 		this.model.add(stmtImpl.getStatement());
 		return stmtImpl;
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
     	SparqlConstruct sparco = new SparqlConstruct.Builder()
     		.construct("?s ?p ?o")
     		.where("?s ?p ?o")
     		.graph(graph)
     		.endpoint(endpoint)
     		.build();
 //        StringBuilder sb = new StringBuilder(
 //                "CONSTRUCT {?s ?p ?o}  WHERE { GRAPH <");
 //        sb.append(graph);
 //        sb.append("> {");
 //        sb.append("?s ?p ?o");
 //        sb.append("} . }");
         Query query = QueryFactory.create(sparco.toString());
         log.info("CONSTRUCT Query: " + sparco.toString());
 
         long sizeBefore = size();
         long stmtsAdded = 0;
 
         boolean success = false;
         int count = 5;
         // Workaround to avoid the empty graph directly after a publish and other bad things that happen in the web.
         while (count>0 && !success) {
             QueryExecution exec = QueryExecutionFactory.createServiceRequest(
                     endpoint, query);
             exec.execConstruct(model);
             stmtsAdded = size() - sizeBefore;
             if (stmtsAdded > 0) {
                 success = true;
 
             } else {
                 count--;
                 log.info("No statements found, I try again... Count: " + count);
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
             }
             exec.close();
         }
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
         	for (GResource gres : this.listURIResources()) {
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
         log.info("Write to endpoint: " + endpoint + " / Graph: " + graph);
         SparqlUpdate sparul = new SparqlUpdate.Builder()
         	.insert(this.getNTriples())
         	.graph(graph)
         	.endpoint(endpoint)
         	.build();
         log.info("writeToEndpoint Query: " + sparul.toString());
         sparul.execute();
 
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
         return StringUtils.join(lines, "\n");
     }
     
 //    public String getUnskolemnizedToSingleResourcePredicateSortedNTriples() {
 //    	GrafeoImpl thisCopy = new GrafeoImpl();
 //    	thisCopy.unskolemize();
 //    	thisCopy.readHeuristically(this.getNTriples());
 //    	String pSortedNT = thisCopy.getPredicateSortedNTriples();
 //    	return pSortedNT.replaceAll("<_[^>]+>", "<blank>");
 //    }
     
     @Override
     public String getPredicateSortedNTriples() {
         StringWriter sw = new StringWriter();
         model.write(sw, "N-TRIPLE");
         String[] lines = sw.toString().split("\n");
         List<String> linesPOSlist = new ArrayList<>();
         List<String> linesSPOlist = new ArrayList<>();
         for (String line : lines) {
         	String linePSO = line.replaceAll("^([^\\s*]+)\\s+(.*)$", "$2 $1");
         	linesPOSlist.add(linePSO);
         }
         Collections.sort(linesPOSlist);
         for (String line : linesPOSlist) {
         	String lineSPO = line.replaceAll("(.*)\\s([^\\s]+)$", "$2 $1");
         	linesSPOlist.add(lineSPO);
         }
         return StringUtils.join(linesSPOlist, "\n");
     }
     
     @Override
     public List<String> diffUnskolemizedNTriples(Grafeo that) {
     	
     	List<Grafeo> grafeos = Arrays.asList(this.copy(), that.copy());
     	List<String> grafeosNT = new ArrayList<>();
     	for (int i = 0; i <= 1; i++) {
     		Grafeo g = grafeos.get(i);
     		g.unskolemize();
     		String asNT = g.getPredicateSortedNTriples();
     		asNT += "\n";
 	    	asNT = asNT.replaceAll("<_[^>]+>", "_");
 	    	asNT = asNT.replaceAll("_[^\\s]+", "_");
 	    	grafeosNT.add(asNT);
     	}
     	
     	// sort by grafeo size
     	if (this.size() > that.size()) {
     		String swap = grafeosNT.get(0);
     		grafeosNT.set(0, grafeosNT.get(1));
     		grafeosNT.set(1, swap);
     	}
 //    	log.severe("SIZERRR: " + this.size() + "" + that.size());
     	
     	// replace all the statements from the smaller one in the larger one and the smaller one
     	for (String line : grafeosNT.get(0).split("\n")) {
     		line += "\n";
     		grafeosNT.set(0, StringUtils.replaceOnce(grafeosNT.get(0), line, ""));
     		grafeosNT.set(1, StringUtils.replaceOnce(grafeosNT.get(1), line, ""));
     	}
     	// replace all the statements from the larger one in the smaller one
     	for (String line : grafeosNT.get(1).split("\n")) {
     		line += "\n";
     		grafeosNT.set(0, StringUtils.replaceOnce(grafeosNT.get(0), line, ""));
     	}
     	return grafeosNT;
     }
     
     @Override
     public String getTurtle() {
         StringWriter sw = new StringWriter();
         model.write(sw, "TURTLE");
         return sw.toString();
     }
     
     @Override
     public String getTerseTurtle() {
         StringWriter sw = new StringWriter();
         model.write(sw, "TURTLE");
         StringBuilder sb = new StringBuilder();
         for (String line: sw.toString().split(LINE_SEPARATOR)) {
         	if (! line.matches("\\s*@prefix.*")) {
 	        	sb.append(line);
 	        	sb.append(LINE_SEPARATOR);
         	}
         }
         return sb.toString();
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
     public boolean containsTriple(String s, String p, String o) {
     	List<String> spo = Arrays.asList(s, p, o);
     	for (int i = 0; i <= 2; i++) {
     		if (spo.get(i) == null) spo.set(i, "?var" + i);
     		else spo.set(i, spo.get(i).startsWith("?") 
 	    			? spo.get(i) 
 					: String.format("<%s>", expand(spo.get(i))));
     	}
         return new SparqlAsk.Builder()
         	.ask(String.format("%s %s %s", spo.toArray()))
         	.grafeo(this)
         	.build()
         	.execute();
     }
 
     @Override
     public boolean containsTriple(String s, String p, GLiteral o) {
     	List<String> sp = Arrays.asList(s,p);
     	for (int i = 0; i <= 1; i++) {
     		if (sp.get(i) == null) sp.set(i, "?var" + i);
     		else sp.set(i, sp.get(i).startsWith("?") 
 	    			? sp.get(i) 
 					: String.format("<%s>", expand(sp.get(i))));
     	}
     	String oStr = (o == null) ? "?o" : o.getTypedValue();
         return new SparqlAsk.Builder()
         	.ask(String.format("%s %s %s", sp.get(0), sp.get(1), oStr))
         	.grafeo(this)
         	.build()
         	.execute();
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
         ResultSet iter = new SparqlSelect.Builder()
         	.where(String.format("%s %s ?o", s, p))
         	.select("?o")
         	.grafeo(this)
         	.limit(1)
         	.build()
         	.execute();
         if (!iter.hasNext()) return null;
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
 		new SparqlUpdate.Builder()
 			.graph(graph)
 			.endpoint(endpoint)
 			.delete("?s ?p ?o.")
 			.build()
 			.execute();
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
     public Set<GResource> listResources() {
     	Set<GResource> resSet = new HashSet<>();
     	NodeIterator iterObj = this.getModel().listObjects();
     	while (iterObj.hasNext()) {
     		RDFNode node = iterObj.next();
     		if (! node.isLiteral()) resSet.add(this.resource(node.asResource()));
     	}
     	ResIterator iterSub = this.getModel().listSubjects();
     	while (iterSub.hasNext()) {
 			resSet.add(this.resource(iterSub.next()));
     	}
     	return resSet;
     }
     
     /**
      * Create a new GResouce from a Jena Resource
      * 
      * @param jenaResource
      * @return New GResource
      */
     private GResource resource(Resource jenaResource) {
     	return new GResourceImpl(this, jenaResource);
 	}
 
 	@Override
     public Set<GResource> listURIResources() {
     	Set<GResource> resSet = new HashSet<>();
     	NodeIterator iterObj = this.getModel().listObjects();
     	while (iterObj.hasNext()) {
     		RDFNode node = iterObj.next();
     		if (node.isURIResource()) resSet.add(this.resource(node.asResource().getURI()));
     	}
     	ResIterator iterSub = this.getModel().listSubjects();
     	while (iterSub.hasNext()) {
    		RDFNode node = iterSub.next();
     		if (node.isURIResource()) resSet.add(this.resource(node.asResource()));
     	}
     	return resSet;
     }
     @Override
     public Set<GResource> listAnonResources() {
     	Set<GResource> resSet = new HashSet<GResource>();
     	NodeIterator iterObj = this.getModel().listObjects();
     	while (iterObj.hasNext()) {
     		RDFNode node = iterObj.next();
     		if (node.isAnon()) {
     			resSet.add(this.resource(node.asResource()));
     		}
     	}
     	ResIterator iterSub = this.getModel().listSubjects();
     	while (iterSub.hasNext()) {
    		RDFNode node = iterSub.next();
     		if (node.isURIResource()) resSet.add(this.resource(node.asResource()));
     	}
     	return resSet;
     }
     
     @Override
     public void skolemizeSequential(String subject, String predicate, String template) {
     	this.skolemize(subject, predicate, template, SkolemizationMethod.SEQUENTIAL_ID);
     }
     
     @Override
     public void skolemizeUUID(String subject, String predicate, String template) {
     	this.skolemize(subject, predicate, template, SkolemizationMethod.RANDOM_UUID);
     }
     
     @Override
     public void unskolemize() {
//    	long i = 0;
     	for (GResource res : this.listResources()) {
//    		res.rename(this.resource("_blank_" + (i++) +"_"));
    		res.rename(this.createBlank());
     	}
     	log.info(this.getTerseTurtle());
     }
 
 	@Override
 	public void skolemize(String subject, String predicate, String template, SkolemizationMethod method) {
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
 			if (method.equals(SkolemizationMethod.RANDOM_UUID)) {
 				randomId = UUID.randomUUID().toString();
 			}
 			else if (method.equals(SkolemizationMethod.SEQUENTIAL_ID)) {
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
 	public Set<GStatement> listStatements(GResource s, String p, GValue o) {
 		Resource sS = null;
 		Property pP = null;
 		RDFNode oO = null;
 		if (s != null) {
 			sS = ((GResourceImpl) s.resource()).getJenaResource();
 		}
 		if (p != null) {
 			pP = this.model.getProperty(expand(p));
 		}
 		if (o != null) {
 			oO = ((GResourceImpl) o.resource()).getJenaResource();
 		}
 		StmtIterator iter = this.model.listStatements(sS, pP, oO);
 		Set<GStatement> matchingStmts = new HashSet<>();
 		while (iter.hasNext()) {
 			Statement jenaStmt = iter.next();
 			GStatementImpl stmt = new GStatementImpl(this, jenaStmt);
 			matchingStmts.add(stmt);
 		}
 		return matchingStmts;
 	}
 	
 	@Override
 	public Set<GStatement> listAnonStatements(String s, String p, GResource o) {
 		Resource sS = null;
 		Property pP = null;
 		RDFNode oO = null;
 		if (s != null)
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
 			oO = this.model.getResource(expand(o));
 		StmtIterator iter = this.model.listStatements(sS, pP, oO);
 		Set<GStatement> matchingStmts = new HashSet<>();
 		while (iter.hasNext()) {
 			GStatementImpl stmt = new GStatementImpl(this, iter.next());
 			matchingStmts.add(stmt);
 		}
 		return matchingStmts;
 	}
 	
 	@Override 
 	public Grafeo copy() {
 		Grafeo newGrafeo = new GrafeoImpl();
 		newGrafeo.readHeuristically(this.getNTriples());
 		return newGrafeo;
 	}
 
     @Override
     public boolean isGraphEquivalent(Grafeo g) {
         GrafeoImpl gi = (GrafeoImpl) g;
         return getModel().isIsomorphicWith(gi.getModel());
     }
     @Override
     public boolean isStructuralGraphEquivalent(Grafeo g) {
         GrafeoImpl that = (GrafeoImpl) g;
         List<Grafeo> toCompare = Arrays.asList(this.copy(), that.copy());
         for (Grafeo gN : toCompare) gN.unskolemize();
         return toCompare.get(0).isGraphEquivalent(toCompare.get(1));
     }
     
     @Override
 	public void visualizeWithGraphviz(String outname) throws Exception {
     	if (null == outname) {
     		outname = "output.svg";
     	}
 		BufferedWriter out;
 		String cmd = "./bin/visualize-turtle.sh " + outname  +" \n";
 		Process p;
 		try {
 			p = Runtime.getRuntime().exec(cmd);
 			out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
 			out.write(this.getTurtle());
 			out.write("\n");
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			throw new RuntimeException("Could not execute command for visualizing: " + e);
 		}
 	}
 
     @Override
 	public Map<String, String> getNamespaces() { return namespaces; }
     
     // TODO    getConciseBoundingDescription()
     
     @Override
     public String stringifyResourcePattern(String subject, String predicate, String object) {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<");
     	sb.append(subject == null ? "?s" : this.expand(subject));
     	sb.append("> <");
     	sb.append(predicate == null ? "?p" : this.expand(predicate));
     	sb.append("> <");
     	sb.append(object == null ? "?o" : this.expand(object));
     	sb.append(">.");
     	return sb.toString();
     }
     @Override
     public String stringifyLiteralPattern(String subject, String predicate, String object) {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<");
     	sb.append(subject == null ? "?s" : this.expand(subject));
     	sb.append("> <");
     	sb.append(predicate == null ? "?p" : this.expand(predicate));
     	sb.append("> ");
     	sb.append(object == null ? "?o" : this.literal(object).getTypedValue());
     	sb.append(". ");
 		return sb.toString();
     }
     @Override
     public String stringifyLiteralPattern(String subject, String predicate, GLiteral object) {
     	StringBuilder sb = new StringBuilder();
     	sb.append("<");
     	sb.append(subject == null ? "?s" : this.expand(subject));
     	sb.append("> <");
     	sb.append(predicate == null ? "?p" : this.expand(predicate));
     	sb.append("> ");
     	sb.append(object == null ? "?o" : object.getTypedValue());
     	sb.append(". ");
 		return sb.toString();
     }
 
 	@Override
 	public void removeTriple(GStatement stmt) {
 		GStatementImpl stmtImpl = new GStatementImpl(this, stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
 		this.model.remove(stmtImpl.getStatement());
 	}
 
 	@Override
 	public GStatement addTriple(GResource subject, String predicate, GValue object) {
 		// TODO Auto-generated method stub
         GResourceImpl p = new GResourceImpl(this, predicate);
         GStatementImpl statement;
         statement = new GStatementImpl(this, subject, p, object);
         model.add(statement.getStatement());
         return statement;
 	}
 
 	@Override
 	public Map<String, String> getNamespacesUsed() { return namespacesUsed; }
 
 }
