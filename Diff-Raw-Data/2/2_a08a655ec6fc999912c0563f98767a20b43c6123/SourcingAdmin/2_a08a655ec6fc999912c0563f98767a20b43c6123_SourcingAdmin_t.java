 /*.
 * Copyright 2013 Fusepool Project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package eu.fusepool.datalifecycle;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.AccessController;
 import java.security.AllPermission;
 import java.security.Permission;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 
 import javax.ws.rs.FormParam;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import org.apache.clerezza.jaxrs.utils.RedirectUtil;
 import org.apache.clerezza.jaxrs.utils.TrailingSlash;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.NonLiteral;
 import org.apache.clerezza.rdf.core.Resource;
 import org.apache.clerezza.rdf.core.Triple;
 import org.apache.clerezza.rdf.core.TripleCollection;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
 import org.apache.clerezza.rdf.core.access.LockableMGraph;
 import org.apache.clerezza.rdf.core.access.TcManager;
 import org.apache.clerezza.rdf.core.access.security.TcAccessController;
 import org.apache.clerezza.rdf.core.access.security.TcPermission;
 import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
 import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
 import org.apache.clerezza.rdf.core.impl.TripleImpl;
 import org.apache.clerezza.rdf.core.serializedform.Parser;
 import org.apache.clerezza.rdf.core.serializedform.Serializer;
 import org.apache.clerezza.rdf.ontologies.DCTERMS;
 import org.apache.clerezza.rdf.ontologies.OWL;
 import org.apache.clerezza.rdf.ontologies.RDF;
 import org.apache.clerezza.rdf.ontologies.RDFS;
 import org.apache.clerezza.rdf.utils.GraphNode;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.ConfigurationPolicy;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.ReferenceCardinality;
 import org.apache.felix.scr.annotations.ReferencePolicy;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.stanbol.commons.indexedgraph.IndexedMGraph;
 import org.apache.stanbol.commons.web.viewable.RdfViewable;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.apache.clerezza.rdf.utils.smushing.SameAsSmusher;
 
 
 /**
  * This is the controller class of the fusepool data life cycle component. The main functionalities provided are
  * 1) XML2RDF transformation 
  * 2) Indexing and Information Extraction
  * 3) Reconciliation/Interlinking
  * 4) Smushing
  */
 @Component(immediate = true, metatype = true,
 policy = ConfigurationPolicy.OPTIONAL)
 @Properties( value={
         @Property(name = "javax.ws.rs", boolValue = true),
         @Property(name=Constants.SERVICE_RANKING,intValue=SourcingAdmin.DEFAULT_SERVICE_RANKING)
         })
 
 @Service(Object.class)
 @Path("sourcing")
 public class SourcingAdmin {
     
     // Service property attributes
     public static final int DEFAULT_SERVICE_RANKING = 101;
     
     // Base URI property attributes. This property is used to canonicalize URIs of type urn:x-temp.
     // The value of the property is updated at service activation from the service configuration panel.
    public static final String BASE_URI_DESCRIPTION = "Base http URI to be used when publishing data ( e.g. http://mydomain.com )";
     public static final String BASE_URI_LABEL= "Base URI";
     public static final String DEFAULT_BASE_URI = "http://localhost:8080"; 
     @Property(label=BASE_URI_LABEL, value=DEFAULT_BASE_URI, description=BASE_URI_DESCRIPTION)
     public static final String BASE_URI = "baseUri";
     
     // base uri updated at service activation from the service property in the osgi console
     private String baseUri;
     
     // Scheme of non-http URI used
     public static final String URN_SCHEME = "urn:x-temp:";
     
     
     /**
      * Using slf4j for normal logging
      */
     private static final Logger log = LoggerFactory.getLogger(SourcingAdmin.class);
     
     BundleContext bundleCtx = null;
 
     @Reference
     private Parser parser;
 
     @Reference
     private Serializer serializer;
 
     /**
      * This service allows accessing and creating persistent triple collections
      */
     @Reference
     private TcManager tcManager;
 
     @Reference
     private Interlinker interlinker;
     
     // Stores bindings to different implementations of RdfDigester
     @Reference(cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
             policy = ReferencePolicy.DYNAMIC,
             referenceInterface=eu.fusepool.datalifecycle.RdfDigester.class)
     private final Map<String,RdfDigester> digesters = new HashMap<String,RdfDigester>();;
     
     
     /**
      * This is the name of the graph in which we "log" the requests
      */
     //private UriRef REQUEST_LOG_GRAPH_NAME = new UriRef("http://example.org/resource-resolver-log.graph");
     /**
      * Name of the data life cycle graph. It is used as a register of other
      * graphs to manage their life cycle
      */
     public static final UriRef DATA_LIFECYCLE_GRAPH_REFERENCE = new UriRef("urn:x-localinstance:/dlc/meta.graph");
     
     /**
      * Register graph referencing graphs for life cycle monitoring;
      */
     public static final String CONTENT_GRAPH_NAME = "urn:x-localinstance:/content.graph";
 
     private UriRef CONTENT_GRAPH_REF = new UriRef(CONTENT_GRAPH_NAME);
 
     // Operation codes
     private final int RDFIZE = 1;
     private final int ADD_TRIPLES_OPERATION = 2;
     private final int TEXT_EXTRACTION = 3;
     private final int RECONCILE_GRAPH_OPERATION = 4;
     private final int SMUSH_GRAPH_OPERATION = 5;
     private final int PUBLISH_DATA = 6;
     
     
     // RDFizer
     private final String PUBMED_RDFIZER = "pubmed";
     private final String PATENT_RDFIZER = "patent";
     
     /**
      * For each rdf triple collection uploaded 5 graphs are created.
      * 1) a source graph to store the rdf data
      * 2) an enhancements graph to store the text extracted for indexing and the
      *   entities extracted from the text by NLP engines in the default enhancement chain
      * 3) a graph to store the result of the interlinking task 
      * 4) a graph to store the smushed graph
      * 5) a graph to store the published graph i.e. the smushed graph in a coherent state with data in the content graph
      * The name convention for these graphs is
      *   GRAPH_URN_PREFIX + timestamp + SUFFIX
      *   where SUFFIX can be one of SOURCE_GRAPH_URN_SUFFIX, ENHANCE_GRAPH_URN_SUFFIX,
      *   INTERLINK_GRAPH_URN_SUFFIX, SMUSH_GRAPH_URN_SUFFIX, PUBLISH_GRAPH_URN_SUFFIX
      */
     // base graph uri
     public static final String GRAPH_URN_PREFIX = "urn:x-localinstance:/dlc/";
     // graph suffix
     public static final String SOURCE_GRAPH_URN_SUFFIX = "/rdf.graph";
     // enhancements graph suffix
     public static final String ENHANCE_GRAPH_URN_SUFFIX = "/enhance.graph";
     // interlink graph suffix
     public static final String INTERLINK_GRAPH_URN_SUFFIX = "/interlink.graph";
     // smushed graph suffix
     public static final String SMUSH_GRAPH_URN_SUFFIX = "/smush.graph";
     // published graph suffix
     public static final String PUBLISH_GRAPH_URN_SUFFIX = "/publish.graph";
     //mesage to show when base URI is invalid
     private final String INVALID_BASE_URI_ALERT = "A valid base URI has not been set. It can be set in the framework configuration panel (eu.fusepool.datalifecycle.SourcingAdmin)";
     
     private UriRef pipeRef = null;
    
     
     // Validity of base Uri (enables interlinking, smushing and publishing tasks)
     private boolean isValidBaseUri = false;
     @SuppressWarnings("unchecked")
     @Activate
     protected void activate(ComponentContext context) {
 
         log.info("The Sourcing Admin Service is being activated");
         // Creates the data lifecycle graph if it doesn't exists. This graph contains references to graphs and linksets 
         
         // Get the value of the base uri from the service property set in the Felix console
         Dictionary<String,Object> dict = context.getProperties() ;
         Object baseUriObj = dict.get(BASE_URI) ;
         baseUri = baseUriObj.toString();
         if( (!"".equals(baseUri)) && ( baseUri.startsWith("http://")) ){
             if(baseUri.endsWith("/")){
                 baseUri = baseUri.substring(0, baseUri.length() - 1);
             }
             isValidBaseUri = true;
             log.info("Base URI: {}", baseUri);
         } else { 
             isValidBaseUri = false;
         }
         
         try {
             createDlcGraph();
             log.info("Created Data Lifecycle Register Graph. This graph will reference all graphs during their lifecycle");
         } catch (EntityAlreadyExistsException ex) {
             log.info("Data Lifecycle Graph already exists.");
         }
         
     }
 
     @Deactivate
     protected void deactivate(ComponentContext context) {
         log.info("The Sourcing Admin Service is being deactivated"); 
     }
     
     /**
      * Bind digester used by this component
      * @param digester
      */
     protected void bindDigesters(RdfDigester digester){
         
         log.info("Binding digester " + digester.getName());
         if( ! digesters.containsKey(digester.getName()) ) {
             digesters.put(digester.getName(), digester);
             log.info("Digester " + digester.getName() + " bound");
             
         }
         else {
             log.info("Digester " + digester.getName() + " already bound.");
         }
         
         
     }
     
     protected void unbindDigesters(RdfDigester digester) {
         if( digesters.containsKey(digester.getName()) ) {
             digesters.remove(digester.getName());
             log.info("Digester " + digester.getName() + " unbound.");
         }
     }
     
     
 
     /**
      * This method return an RdfViewable, this is an RDF serviceUri with
      * associated presentational information.
      */
     @GET
     public RdfViewable serviceEntry(@Context final UriInfo uriInfo,
             @QueryParam("url") final UriRef url,
             @HeaderParam("user-agent") String userAgent) throws Exception {
         //this makes sure we are nt invoked with a trailing slash which would affect
         //relative resolution of links (e.g. css)
         TrailingSlash.enforcePresent(uriInfo);
 
         final String resourcePath = uriInfo.getAbsolutePath().toString();
         if (url != null) {
             String query = url.toString();
             log.info(query);
         }
 
         //The URI at which this service was accessed, this will be the 
         //central serviceUri in the response
         final UriRef serviceUri = new UriRef(resourcePath);
         //the in memory graph to which the triples for the response are added
         final MGraph responseGraph = new IndexedMGraph();
         Lock rl = getDlcGraph().getLock().readLock();
         rl.lock();
         try {
             responseGraph.addAll(getDlcGraph());
         }
         finally {
             rl.unlock();
         }
         
         Iterator<String> digestersNames = digesters.keySet().iterator();
         while(digestersNames.hasNext()){
             String digesterName = digestersNames.next(); 
             responseGraph.add(new TripleImpl(DATA_LIFECYCLE_GRAPH_REFERENCE, Ontology.service, new UriRef("urn:x-temp:/" + digesterName)));            
             responseGraph.add(new TripleImpl(new UriRef("urn:x-temp:/" + digesterName), RDFS.label, new PlainLiteralImpl(digesterName)));
         }
         
         //This GraphNode represents the service within our result graph
         final GraphNode node = new GraphNode(DATA_LIFECYCLE_GRAPH_REFERENCE, responseGraph);
         
         // Adds information about base uri configuration
         if( ! isValidBaseUri ){
             responseGraph.add(new TripleImpl(DATA_LIFECYCLE_GRAPH_REFERENCE, RDFS.comment, new PlainLiteralImpl(INVALID_BASE_URI_ALERT)));
         }
         
         //What we return is the GraphNode we created with a template path
         return new RdfViewable("SourcingAdmin", node, SourcingAdmin.class);
     }
     
     private void setPipeRef(UriRef pipeRef) {
         this.pipeRef = pipeRef;
     }
     
     private LockableMGraph getSourceGraph() {
         return tcManager.getMGraph(new UriRef(pipeRef.getUnicodeString() + SOURCE_GRAPH_URN_SUFFIX));
     }
     private LockableMGraph getEnhanceGraph() {
         return tcManager.getMGraph(new UriRef(pipeRef.getUnicodeString() + ENHANCE_GRAPH_URN_SUFFIX));
     }
     private LockableMGraph getInterlinkGraph() {
         return tcManager.getMGraph(new UriRef(pipeRef.getUnicodeString() + INTERLINK_GRAPH_URN_SUFFIX));
     }
     private LockableMGraph getSmushGraph() {
         return tcManager.getMGraph(new UriRef(pipeRef.getUnicodeString() + SMUSH_GRAPH_URN_SUFFIX));
     }
     private LockableMGraph getPublishGraph() {
         return tcManager.getMGraph(new UriRef(pipeRef.getUnicodeString() + PUBLISH_GRAPH_URN_SUFFIX));
     }
     private LockableMGraph getContentGraph() {
         return tcManager.getMGraph( CONTENT_GRAPH_REF );
     }
 
     /**
      * Creates a new pipe with tasks and product graphs and adds its uri and a label to the data life cycle graph. 
      * A graph will contain the RDF data uploaded or sent by a transformation task 
      * that have to be processed (text extraction, NLP processing, reconciliation, smushing).
      * The following graphs are created to store the results of the processing tasks
      * enhance.graph
      * interlink.graph
      * smush.graph
      * These graphs will be empty at the beginning. 
      * @param uriInfo
      * @param graphName
      * @return
      * @throws Exception
      */
     @POST
     @Path("create_pipe")
     @Produces("text/plain")
     public Response createPipeRequest(@Context final UriInfo uriInfo,            
             @FormParam("pipe_label") final String pipeLabel) throws Exception {
         
         AccessController.checkPermission(new AllPermission());
         
         // use dataset label as name after validation
         String datasetName = getValidDatasetName(pipeLabel); 
         
         if (datasetName != null && initializePipe(datasetName)) {
             return RedirectUtil.createSeeOtherResponse("./", uriInfo);
         } 
         else {
             return Response.status(Response.Status.BAD_REQUEST).entity("Cannot create graph " + pipeLabel).build();
         }
 
     }
     
     /**
      * Check whether a label can be used as a dataset name. To be a valid name a label must be: 
      * 1) not null and at least one character long
      * 2) without white spaces 
      * 3) unique (no two dataset can have the same name) 
      * @return String
      */
     private String getValidDatasetName(String label) {
         String newDatasetName = null;
         //check validity 
         if(label == null || "".equals(label)){
             return null;
         }
         
         // replace white space if present
         newDatasetName = label.replace(' ', '-');
         
         //check uniqueness of name
         Lock rl = getDlcGraph().getLock().readLock();
         rl.lock();
         try {
           Iterator<Triple> idatasets = getDlcGraph().filter(null, RDF.type, Ontology.Pipe);          
           while(idatasets.hasNext()) {
               GraphNode datasetNode = new GraphNode((UriRef)idatasets.next().getSubject(), getDlcGraph());
               String datasetName = datasetNode.getLiterals(RDFS.label).next().getLexicalForm();
               if(newDatasetName.equals(datasetName)) {
                   return null;
               }
           }
         }
         finally {
             rl.unlock();
         }
         
         return newDatasetName;
     }
         
     /**
      * Initialize the dataset creating the graphs for each task in the pipe line
      */
     private boolean initializePipe(String datasetName) {
         
         boolean result = false;
         
         try {
             // create a pipe 
             UriRef pipeRef = new UriRef(GRAPH_URN_PREFIX + datasetName);
             getDlcGraph().add(new TripleImpl(pipeRef, RDF.type, Ontology.Pipe));
             if(datasetName != null & ! "".equals(datasetName)) {
                 getDlcGraph().add(new TripleImpl(pipeRef, RDFS.label, new PlainLiteralImpl(datasetName)));
             }
             getDlcGraph().add(new TripleImpl(DATA_LIFECYCLE_GRAPH_REFERENCE, Ontology.pipe, pipeRef));
             
             // create tasks
             //rdf task
             UriRef rdfTaskRef = new UriRef(GRAPH_URN_PREFIX + datasetName + "/rdf");
             getDlcGraph().add(new TripleImpl(pipeRef, Ontology.creates, rdfTaskRef));
             getDlcGraph().add(new TripleImpl(rdfTaskRef, RDF.type, Ontology.RdfTask));
             // enhance task
             UriRef enhanceTaskRef = new UriRef(GRAPH_URN_PREFIX + datasetName + "/enhance");
             getDlcGraph().add(new TripleImpl(pipeRef, Ontology.creates, enhanceTaskRef));
             getDlcGraph().add(new TripleImpl(enhanceTaskRef, RDF.type, Ontology.EnhanceTask));
             // interlink task
             UriRef interlinkTaskRef = new UriRef(GRAPH_URN_PREFIX + datasetName + "/interlink");
             getDlcGraph().add(new TripleImpl(pipeRef, Ontology.creates, interlinkTaskRef));
             getDlcGraph().add(new TripleImpl(interlinkTaskRef, RDF.type, Ontology.InterlinkTask));
             // smush task
             UriRef smushTaskRef = new UriRef(GRAPH_URN_PREFIX + datasetName + "/smush");
             getDlcGraph().add(new TripleImpl(pipeRef, Ontology.creates, smushTaskRef));
             getDlcGraph().add(new TripleImpl(smushTaskRef, RDF.type, Ontology.SmushTask));
             // publish task
             UriRef publishTaskRef = new UriRef(GRAPH_URN_PREFIX + datasetName + "/publish");
             getDlcGraph().add(new TripleImpl(pipeRef, Ontology.creates, publishTaskRef));
             getDlcGraph().add(new TripleImpl(smushTaskRef, RDF.type, Ontology.PublishTask));
             
             // create the source graph for the dataset (result of transformation in RDF)
             String sourceGraphName = GRAPH_URN_PREFIX + datasetName + SOURCE_GRAPH_URN_SUFFIX;
             UriRef sourceGraphRef = new UriRef(sourceGraphName);
             tcManager.createMGraph(sourceGraphRef);
             //GraphNode dlcGraphNode = new GraphNode(DATA_LIFECYCLE_GRAPH_REFERENCE, getDlcGraph());
             //dlcGraphNode.addProperty(DCTERMS.hasPart, graphRef);
             getDlcGraph().add(new TripleImpl(rdfTaskRef, Ontology.deliverable, sourceGraphRef));
             getDlcGraph().add(new TripleImpl(sourceGraphRef, RDF.type, Ontology.voidDataset));
             
             
             
             // create the graph to store text and enhancements
             String enhancementsGraphName = GRAPH_URN_PREFIX + datasetName + ENHANCE_GRAPH_URN_SUFFIX;
             UriRef enhancementsGraphRef = new UriRef(enhancementsGraphName);
             tcManager.createMGraph(enhancementsGraphRef);
             getDlcGraph().add(new TripleImpl(enhanceTaskRef, Ontology.deliverable, enhancementsGraphRef));
             getDlcGraph().add(new TripleImpl(enhancementsGraphRef, RDFS.label, new PlainLiteralImpl("Contains a sioc:content property with text " +
                     "for indexing and references to entities found in the text by NLP enhancement engines")));
             
             // create the graph to store the result of the interlinking task
             String interlinkGraphName = GRAPH_URN_PREFIX + datasetName + INTERLINK_GRAPH_URN_SUFFIX;
             UriRef interlinkGraphRef = new UriRef(interlinkGraphName);
             tcManager.createMGraph(interlinkGraphRef);
             getDlcGraph().add(new TripleImpl(interlinkTaskRef, Ontology.deliverable, interlinkGraphRef));
             getDlcGraph().add(new TripleImpl(interlinkGraphRef, RDF.type, Ontology.voidLinkset));
             getDlcGraph().add(new TripleImpl(interlinkGraphRef,Ontology.voidSubjectsTarget, sourceGraphRef));
             getDlcGraph().add(new TripleImpl(interlinkGraphRef,Ontology.voidLinkPredicate, OWL.sameAs));
             getDlcGraph().add(new TripleImpl(interlinkGraphRef, RDFS.label, new PlainLiteralImpl("Contains equivalence links")));
             
             // create the graph to store the result of the smushing task
             String smushGraphName = GRAPH_URN_PREFIX + datasetName + SMUSH_GRAPH_URN_SUFFIX;
             UriRef smushGraphRef = new UriRef(smushGraphName);
             tcManager.createMGraph(smushGraphRef);
             getDlcGraph().add(new TripleImpl(smushTaskRef, Ontology.deliverable, smushGraphRef));
             
             // create the graph to store the result of the publishing task
             String publishGraphName = GRAPH_URN_PREFIX + datasetName + PUBLISH_GRAPH_URN_SUFFIX;
             UriRef publishGraphRef = new UriRef(publishGraphName);
             tcManager.createMGraph(publishGraphRef);
             getDlcGraph().add(new TripleImpl(publishTaskRef, Ontology.deliverable, publishGraphRef));
             
             setPipeRef(pipeRef);
             
             result = true;
  
             
         } 
         catch (UnsupportedOperationException uoe) {
             log.error("Error while creating a graph");
         }
         
         return result;
         
     }
     
     
 
     /**
      * Applies one of the following operations to a graph: - add triples
      * (operation code: 1) - remove all triples (operation code: 2) - delete
      * graph (operation code: 3) - reconcile (operation code: 4) - smush
      * (operation code: 5)
      */
     @POST
     @Path("operate")
     @Produces("text/plain")
     public String operateOnGraphCommand(@Context final UriInfo uriInfo,
             @FormParam("pipe") final UriRef pipeRef,
             @FormParam("operation_code") final int operationCode,
             @FormParam("data_url") final URL dataUrl,
             @FormParam("rdfizer") final String rdfizer,
             @FormParam("rdfdigester") final String rdfdigester,
             @HeaderParam("Content-Type") String mediaType) throws Exception {
         AccessController.checkPermission(new AllPermission());
 
         // validate arguments and handle all the connection exceptions
         return operateOnPipe(pipeRef, operationCode, dataUrl, rdfizer, rdfdigester, mediaType);
 
     }
 
     private String operateOnPipe(UriRef pipeRef, 
             int operationCode, 
             URL dataUrl, 
             String rdfizer,
             String rdfdigester,
             String mediaType) throws Exception {
         AccessController.checkPermission(new AllPermission());
         String message = "";
         if (pipeExists(pipeRef)) {
             
             setPipeRef(pipeRef);
             
             switch (operationCode) {
                 case ADD_TRIPLES_OPERATION:
                     message = addTriples(pipeRef, dataUrl, mediaType);
                     break;                       
                 case RECONCILE_GRAPH_OPERATION:
                     message = reconcile(pipeRef);
                     break;
                 case SMUSH_GRAPH_OPERATION:
                     message = smush(pipeRef);
                     break;            
                 case TEXT_EXTRACTION:
                     message = extractTextFromRdf(pipeRef, rdfdigester);
                     break;                
                 case RDFIZE:
                     message = transformXml(dataUrl, rdfizer);
                     break;     
                 case PUBLISH_DATA:
                     message = publishData(pipeRef);
                     break;
             }
         } else {
             message = "The pipe does not exist.";
         }
 
         return message;
 
     }
     /**
      * Transforms Patent or PubMed XML data into RDF
      * @param dataUrl
      * @param rdfizer
      * @return
      */
     private String transformXml(URL dataUrl, String rdfizer) {
         String message = "";
         
         if(PUBMED_RDFIZER.equals(rdfizer)){
             message = transformPubMedXml(dataUrl);
         }
         else if (PATENT_RDFIZER.equals(rdfizer)) {
             message = transformPatentXml(dataUrl);
         }
         
         return message;
         
     }
     
     private String transformPubMedXml(URL dataUrl) {
         String message = "PubMed XML->RDF transformation to be implemented.";
         
         return message;
     }
     
     private String transformPatentXml(URL dataUrl) {
         String message = "Marec Patent XML->RDF transformation to be implemented";
         
         return message;
     }
     
     /**
      * Load RDF data into an existing graph from a URL (schemes: "file://" or "http://"). 
      * The arguments to be passed are: 
      * 1) graph in which the RDF data must be stored
      * 2) url of the dataset 
      * After the upload the input graph is sent to a digester to extract text for indexing and 
      * adding entities found by NLP components (in the default chain) as subject
      */
     private String addTriples(UriRef pipeRef, URL dataUrl, String mediaType) throws Exception {
         AccessController.checkPermission(new AllPermission());
         String message = "";
         
         // look up the pipe's rdf graph to which add the data
         UriRef graphRef = new UriRef(pipeRef.getUnicodeString() + SOURCE_GRAPH_URN_SUFFIX);
         
         // add the triples of the temporary graph into the graph selected by the user
         if (isValidUrl(dataUrl)) {
             
             MGraph updatedGraph = addTriplesCommand(graphRef, dataUrl, mediaType);
 
             message = "Added " + updatedGraph.size() + " triples to " + graphRef.getUnicodeString() + "\n";
 
         } else {
             message = "The URL of the data is not a valid one.\n";
         }
        
         log.info(message);
         return message;
         
     }
 
     private MGraph addTriplesCommand(UriRef graphRef, URL dataUrl, String mediaType) throws Exception {
         AccessController.checkPermission(new AllPermission());
         MGraph graph = null;
         URLConnection connection = dataUrl.openConnection();
         connection.addRequestProperty("Accept", "application/rdf+xml; q=.9, text/turte;q=1");
 
         // create a temporary graph to store the data        
         SimpleMGraph tempGraph = new SimpleMGraph();
 
         InputStream data = connection.getInputStream();
         if (data != null) {
             if (mediaType.equals("application/x-www-form-urlencoded")) {
                 mediaType = getContentTypeFromUrl(dataUrl);
             }
             parser.parse(tempGraph, data, mediaType);
 
             // add the triples of the temporary graph into the graph selected by the user
             if (graphExists(graphRef)) {
                 graph = tcManager.getMGraph(graphRef);
 
                 graph.addAll(tempGraph);
                 
             } 
         }
         
         return tempGraph;
     }
     
     
 
     /**
      * Removes all the triples from the graph
      *
      */
     /*
     private String emptyGraph(UriRef graphRef) {
         // removes all the triples from the graph
         MGraph graph = tcManager.getMGraph(graphRef);
         graph.clear();
         return "Graph " + graphRef.getUnicodeString() + " is now empty.";
     }
     */
 
     /**
      * Deletes a graph, the reference to it in the DLC graph and deletes all the
      * derived graphs linked to it by the dcterms:source property.
      *
      * @param graphRef
      * @return
      */
     private String deleteGraph(UriRef graphRef) {
         tcManager.deleteTripleCollection(graphRef);
         GraphNode dlcGraphNode = new GraphNode(DATA_LIFECYCLE_GRAPH_REFERENCE, getDlcGraph());
         //remove the relation with the data lifecycle graph and all the information (triples) about the deleted graph (label).
         dlcGraphNode.deleteProperty(DCTERMS.hasPart, graphRef);
 
         return "Graph " + graphRef.getUnicodeString() + " has been deleted.";
     }
 
     /**
      * Reconciles a source graph against itself and against the content graph. The result of the reconciliation is an equivalence set 
      * stored in the interlink graph of the pipe.  
      * @param sourceGraphRef the URI of the referenced graph, i.e. the graph for which the reconciliation should be performed.
      * @return String 
      * @throws Exception 
      */
     private String reconcile(UriRef pipeRef) throws Exception {
         String message = "";
         
         // Identifier of the link rules within the Silk config file
         String linkSpecId = "agents";
         
         UriRef sourceGraphRef = new UriRef(pipeRef.getUnicodeString() + SOURCE_GRAPH_URN_SUFFIX);
         
         if (graphExists(sourceGraphRef) && getSourceGraph().size() > 0) {            
             
             // size of interlink graph before reconciliations
             int interlinkGraphInitSize = getInterlinkGraph().size();
             
             // reconcile the source graph against itself 
             reconcileCommand(pipeRef, sourceGraphRef, sourceGraphRef, linkSpecId);
             
             // size of interlink graph after reconciliation of source graph against itself 
             int interlinkSourceGraphSize = getInterlinkGraph().size();
             
             // new interlinks within source graph
             int numSourceInterlinks = interlinkSourceGraphSize - interlinkGraphInitSize; 
 
             if (numSourceInterlinks > 0) {
 
                 message = "A reconciliation task has been done on " + sourceGraphRef.getUnicodeString() + "\n"
                         + numSourceInterlinks + " owl:sameAs statements have been created.";
             } 
             else {
                 message = "A reconciliation task has been done on " + sourceGraphRef.getUnicodeString()
                         + ". No equivalent entities have been found.\n";
             }
             
             // reconcile the source graph against the content graph 
             if(getContentGraph().size() > 0) {
                 
                 reconcileCommand(pipeRef, sourceGraphRef, CONTENT_GRAPH_REF, linkSpecId);
                 
                 // size of interlink graph after reconciliation of source graph against content graph 
                 int interlinkContentGraphSize = getInterlinkGraph().size();
                 
                 // new interlinks with content graph
                 int numContentInterlinks = interlinkContentGraphSize - interlinkSourceGraphSize;      
     
                 if (numContentInterlinks > 0) {
     
                     message += "A reconciliation task has been done between " + sourceGraphRef.getUnicodeString() + " and " + CONTENT_GRAPH_NAME + "\n"
                             + numContentInterlinks + " owl:sameAs statements have been created.";
                 } 
                 else {
                     message += "A reconciliation task has been done between " + sourceGraphRef.getUnicodeString() + " and " + CONTENT_GRAPH_NAME + "\n"
                             + ". No equivalent entities have been found.\n";
                 }
             }
             
         } 
         else {
             message = "The source graph does not exist or is empty.";
         }
         
         log.info(message);
         return message;
 
     }
     
     /**
      * Reconciles a source graph with a target graph. The result of the reconciliation is an equivalence set 
      * stored in the interlink graph of the pipe. The graph used as source is the source rdf graph. 
      * @throws Exception 
      */
     private void reconcileCommand(UriRef pipeRef, UriRef sourceGraphRef, UriRef targetGraphRef, String linkSpecId) throws Exception {
         
         TripleCollection owlSameAs = null;
         
         // get the pipe's interlink graph to store the result of the reconciliation task        
         UriRef interlinkGraphRef = new UriRef(pipeRef.getUnicodeString() + INTERLINK_GRAPH_URN_SUFFIX);
         
         if (graphExists(sourceGraphRef)) {
             
             // Get the source graph from the triple store
             LockableMGraph sourceGrah = tcManager.getMGraph(sourceGraphRef);
             // Copy the graph
             MGraph copySourceGraph = new SimpleMGraph();
             Lock rl = sourceGrah.getLock().readLock();
             rl.lock();
             try {
                 copySourceGraph.addAll(sourceGrah);
             }
             finally {
                 rl.unlock();
             }
             
             
             // reconcile the source graph with the target graph 
             owlSameAs =  interlinker.interlink(copySourceGraph, targetGraphRef, linkSpecId);
 
             if (owlSameAs.size() > 0) {
 
                 LockableMGraph sameAsGraph = tcManager.getMGraph(interlinkGraphRef);
                 sameAsGraph.addAll(owlSameAs);
 
                 // log the result (the equivalence set should be serialized and stored)
                 Lock l = sameAsGraph.getLock().readLock();
                 l.lock();
                 try {
                     Iterator<Triple> isameas = owlSameAs.iterator();
                     while (isameas.hasNext()) {
                         Triple t = isameas.next();
                         NonLiteral s = t.getSubject();
                         UriRef p = t.getPredicate();
                         Resource o = t.getObject();
                         log.info(s.toString() + p.getUnicodeString() + o.toString() + " .\n");
                     }
                 }
                 finally {
                     l.unlock();
                 }
                 
                 // add a reference of the equivalence set to the source graph 
                 getDlcGraph().add(new TripleImpl(interlinkGraphRef, Ontology.voidSubjectsTarget, sourceGraphRef));
                 // add a reference of the equivalence set to the target graph                
                 getDlcGraph().add(new TripleImpl(interlinkGraphRef, Ontology.voidObjectsTarget, targetGraphRef));
                
             }         
         }
 
     }
 
     /**
      * Smush the enhanced graph using the interlinking graph. More precisely collates URIs coming 
      * from different equivalent resources in a single one chosen among them. The triples in the
      * source graph are copied in the smush graph that is then smushed using the interlinking 
      * graph. 
      * @param graphToSmushRef
      * @return
      */
     private String smush(UriRef pipeRef) {
         String message = "Smushing task.\n";
         // As the smush.graph must be published it has to contain the sioc.content property and all the subject
         // extracted during the extraction phase that are stored in the enhance.graph with all the triples from 
         // the rdf
         UriRef enhanceGraphRef = new UriRef(pipeRef.getUnicodeString() + ENHANCE_GRAPH_URN_SUFFIX);
         
         if(getInterlinkGraph().size() > 0 & getEnhanceGraph().size() > 0) {
                 
             LockableMGraph smushedGraph = smushCommand(enhanceGraphRef, getInterlinkGraph());
         
             message = "Smushing of " + enhanceGraphRef.getUnicodeString()
                     + " with equivalence set completed. "
                     + "Smushed graph size = " + smushedGraph.size() + "\n";
         }
         else {
             message = "No equivalence links available for " + enhanceGraphRef.getUnicodeString() + "\n"
                     + "or the enhancement graph is empty.\n"
                     + "The smushing task is applied to the enhancement graph using the equivalence set in the interlinking graph.";
         }
                 
         return message;
     }
     
     private LockableMGraph smushCommand(UriRef enhanceGraphRef, LockableMGraph equivalenceSet) {
         
         if(getSmushGraph().size() > 0) {
             getSmushGraph().clear();
         }        
         
         Lock erl = getEnhanceGraph().getLock().readLock();
         erl.lock();
         try {
             // add triples from enhance graph to smush graph
             getSmushGraph().addAll(getEnhanceGraph());
             log.info("Copied " + getEnhanceGraph().size() + " triples from the enhancement graph into the smush graph.");
             SimpleMGraph tempEquivalenceSet = new SimpleMGraph();
             tempEquivalenceSet.addAll(equivalenceSet);
             
             // smush and canonicalize equivalent uris
             SameAsSmusher smusher = new CanonicalizingSameAsSmusher();
             log.info("Smush task started.");
             smusher.smush(getSmushGraph(), tempEquivalenceSet, true);
             log.info("Smush task completed.");
         }
         finally {
             erl.unlock();
         }
             
         // Remove from smush graph equivalences between temporary uri (urn:x-temp) and http uri that are added by the clerezza smusher.
         // These equivalences must be removed as only equivalences between known entities (http uri) must be maintained and then published
         MGraph equivToRemove = new SimpleMGraph();
         Lock srl = getSmushGraph().getLock().readLock();
         srl.lock();
         try {            
             Iterator<Triple> isameas = getSmushGraph().filter(null, OWL.sameAs, null);
             while (isameas.hasNext()) {
                 Triple sameas = isameas.next();
                 NonLiteral subject = sameas.getSubject();                
                 Resource object = sameas.getObject();
                 if( subject.toString().startsWith("<" + URN_SCHEME) || object.toString().startsWith("<" + URN_SCHEME)) {
                     equivToRemove.add(sameas);
                 }
             }
         }
         finally {
           srl.unlock();    
         }
         
         getSmushGraph().removeAll(equivToRemove);
         
         return getSmushGraph();
 
     }
     
     
     /**
      * Extract text from dcterms:title and dcterms:abstract fields in the source graph and adds a sioc:content
      * property with that text in the enhance graph. The text is used by the ECS for indexing. The keywords
      * will be related to a patent (resource of type pmo:PatentPublication) so that the patent will be retrieved anytime 
      * the keyword is searched. The extractor also takes all the entities extracted by NLP enhancement engines. These entities
      * and a rdfs:label if available, are added to the patent resource using dcterms:subject property. 
      * @param pipeRef
      * @return
      */
     private String extractTextFromRdf(UriRef pipeRef, String selectedDigester){
         
         String message = "";
         UriRef enhanceGraphRef = new UriRef(pipeRef.getUnicodeString() + ENHANCE_GRAPH_URN_SUFFIX);
         MGraph enhanceGraph = tcManager.getMGraph(enhanceGraphRef);
         UriRef sourceGraphRef = new UriRef(pipeRef.getUnicodeString() + SOURCE_GRAPH_URN_SUFFIX);
         LockableMGraph sourceGraph = tcManager.getMGraph(sourceGraphRef);
         
         SimpleMGraph tempGraph = new SimpleMGraph();
         Lock rl = sourceGraph.getLock().readLock();
         rl.lock();
         try {
             tempGraph.addAll(sourceGraph);
         }
         finally {
             rl.unlock();
         }
         
         enhanceGraph.addAll(tempGraph);
         
         RdfDigester digester = digesters.get(selectedDigester);
         digester.extractText(enhanceGraph);
         message += "Extracted text from " + enhanceGraphRef.getUnicodeString() + " by " + selectedDigester + " digester";
         
         
         return message;
     }
     
     
        
     /**
      * Moves data from smush.grah to content.graph. The triples (facts) in the two graphs must be coherent, i.e. the same. 
      * Before publishing the current smushed data must be compared with the last published data. New triples 
      * in the smushed graph not in the published graph must be added while triples in the published graph absent
      * in the smushed graph must be removed.  The algorithm is as follows
      * 1) make all URIs in smush.graph http dereferencable (uri canonicalization)
      * 2) find triples in smush.graph not in publish.graph (new triples)
      * 3) find triples in publish.graph not in smush.graph (old triples)
      * 4) add new triples to content.graph 
      * 5) remove old triples from content.graph
      * 6) delete all triples in publish.graph
      * 7) copy triples from smush.graph to publish.graph
      */
     private String publishData(UriRef pipeRef) {
         String message = "";
         
         // add these triples to the content.graph 
         MGraph triplesToAdd = new SimpleMGraph();
         // remove these triples from the content.graph
         MGraph triplesToRemove = new SimpleMGraph();
         
         // make all URIs in smush graph dereferencable
         canonicalizeResources(getSmushGraph());
         
         // triples to add to the content.graph
         Lock ls = getSmushGraph().getLock().readLock();
         ls.lock();
         try {
             
             Iterator<Triple> ismush = getSmushGraph().iterator();            
             while (ismush.hasNext()) {
                 Triple smushTriple = ismush.next();                
                 if( ! getPublishGraph().contains(smushTriple) ) {
                     triplesToAdd.add(smushTriple);
                 }
                 
             }
         }
         finally {
             ls.unlock();
         }
         
         // triples to remove from the content.graph
         Lock lp = getPublishGraph().getLock().readLock();
         lp.lock();
         try {
             Iterator<Triple> ipublish = getPublishGraph().iterator();            
             while (ipublish.hasNext()) {
                 Triple publishTriple = ipublish.next();
                 if( ! getSmushGraph().contains(publishTriple) ) {
                     triplesToRemove.add(publishTriple);
                 }
                 
             }
         }
         finally {
             lp.unlock();
         }
         
         if(triplesToRemove.size() > 0) {
             getContentGraph().removeAll(triplesToRemove);
             log.info("Removed " + triplesToRemove.size() + " triples from " + CONTENT_GRAPH_REF.getUnicodeString());
         }
         else {
             log.info("No triples to remove from " + CONTENT_GRAPH_REF.getUnicodeString());
         }
         if(triplesToAdd.size() > 0) {
             getContentGraph().addAll(triplesToAdd);
             log.info("Added " + triplesToAdd.size() + " triples to " + CONTENT_GRAPH_REF.getUnicodeString());
         }
         else {
             log.info("No triples to add to " + CONTENT_GRAPH_REF.getUnicodeString());
         }
         
         getPublishGraph().clear();
         
         Lock rl = getSmushGraph().getLock().readLock();
         rl.lock();
         try {
             getPublishGraph().addAll( getSmushGraph() );
         }
         finally {
             rl.unlock();
         }
         
         message = "Copied " + triplesToAdd.size() + " triples from " + pipeRef.getUnicodeString() + " to content-graph";
         
         return message;
     }
     
     /**
      * All the resources in the smush graph must be http dereferencable when published. 
      * All the triples in the smush graph are copied into a temporary graph. For each triple the subject and the object
      * that have a non-http URI are changed in http uri and an equivalence link is added in the interlinking graph for each
      * resource (subject and object) that has been changed.
      */
     private void canonicalizeResources(LockableMGraph graph) {
         
         MGraph graphCopy = new SimpleMGraph();
         // graph containing the same triple with the http URI for each subject and object
         MGraph canonicGraph = new SimpleMGraph();
         Lock rl = graph.getLock().readLock();
         rl.lock();
         try {
             graphCopy.addAll(graph);
         }
         finally {
             rl.unlock();
         }
         
         Iterator<Triple> ismushTriples = graphCopy.iterator();  
         while (ismushTriples.hasNext()) {
             Triple triple = ismushTriples.next();
             UriRef subject = (UriRef) triple.getSubject();
             Resource object = triple.getObject();
             // generate an http URI for both subject and object and add an equivalence link into the interlinking graph
             if( subject.getUnicodeString().startsWith(URN_SCHEME) ) {
                 subject = generateNewHttpUri(Collections.singleton(subject));
             }
             if( object.toString().startsWith("<" + URN_SCHEME) ) {
                 object = generateNewHttpUri(Collections.singleton((UriRef)object));
             }            
             
             // add the triple with the http uris to the canonic graph
             canonicGraph.add(new TripleImpl(subject, triple.getPredicate(), object));
         }
         
         Lock wl = graph.getLock().writeLock();
         wl.lock();
         try {
             graph.clear();
             graph.addAll(canonicGraph);
         }
         finally {
             wl.unlock();
         }
         
         
         
     }
     
     /**
      * Validate URL
      * A valid URL must start with file:/// or http://
      */
     private boolean isValidUrl(URL url) {
         boolean isValidUrl = false;
         if(url != null) {
             if( url.toString().startsWith("http://") || url.toString().startsWith("file:/")) {
                 isValidUrl = true;
             }
         }
         
         return isValidUrl;
     }
 
     /**
      * Extracts the content type from the file extension
      *
      * @param url
      * @return
      */
     private String getContentTypeFromUrl(URL url) {
         String contentType = null;
         if (url.getFile().endsWith("ttl")) {
             contentType = "text/turtle";
         } else if (url.getFile().endsWith("nt")) {
             contentType = "text/turtle";
         } else {
             contentType = "application/rdf+xml";
         }
         return contentType;
     }
 
     /**
      * Returns the data life cycle graph containing all the monitored graphs. It
      * creates it if doesn't exit yet.
      *
      * @return
      */
     private LockableMGraph getDlcGraph() {
         return tcManager.getMGraph(DATA_LIFECYCLE_GRAPH_REFERENCE);
     }
 
     /**
      * Checks if a graph exists and returns a boolean value.
      * true if graph exist
      * false if graph does not exist
      * @param graph_ref
      * @return
      */
     private boolean graphExists(UriRef graph_ref) {
         Set<UriRef> graphs = tcManager.listMGraphs();
         Iterator<UriRef> igraphs = graphs.iterator();
         while (igraphs.hasNext()) {
             UriRef graphRef = igraphs.next();
             if (graph_ref.toString().equals(graphRef.toString())) {
                 return true;
             }
         }
 
         return false;
 
     }
     
     /**
      * Checks whether a pipe exists
      */
     private boolean pipeExists(UriRef pipeRef) {
         boolean result = false;
         
         if (pipeRef != null) {
             GraphNode pipeNode = new GraphNode(pipeRef, getDlcGraph());
             if(pipeNode != null) {
                 result = true;
             }
         }
         
         return result;
         
     }
 
     /**
      * Creates the data lifecycle graph. Must be called at the bundle
      * activation if the graph doesn't exists yet.
      */
     private MGraph createDlcGraph() {
         MGraph dlcGraph = tcManager.createMGraph(DATA_LIFECYCLE_GRAPH_REFERENCE);
         TcAccessController tca = new TcAccessController(tcManager);
         tca.setRequiredReadPermissions(DATA_LIFECYCLE_GRAPH_REFERENCE,
                 Collections.singleton((Permission) new TcPermission(
                                 "urn:x-localinstance:/content.graph", "read")));
         return dlcGraph;
     }
     
     /**
      * An inline class to canonicalize URI from urn to http scheme. A http URI is chosen 
      * among the equivalent ones.if no one http URI is available a new one is created. 
      */
     private class CanonicalizingSameAsSmusher extends SameAsSmusher {
         @Override
         protected UriRef getPreferedIri(Set<UriRef> uriRefs) {
             Set<UriRef> httpUri = new HashSet<UriRef>();
             for (UriRef uriRef : uriRefs) {
                 if (uriRef.getUnicodeString().startsWith("http")) {
                     httpUri.add(uriRef);
                 }
             }
             if (httpUri.size() == 1) {
                 return httpUri.iterator().next();
             }
             // There is no http URI in the set of equivalent resource. The entity was unknown. 
             // A new representation of the entity with http URI will be created. 
             if (httpUri.size() == 0) {
                 return generateNewHttpUri(uriRefs);
             }
             if (httpUri.size() > 1) {
                 return chooseBest(httpUri);
             }
             throw new Error("Negative size set.");
         }
         
 
     }
     
     /**
      * Generates a new http URI that will be used as the canonical one in place 
      * of a set of equivalent non-http URIs. An owl:sameAs statement is added to
      * the interlinking graph stating that the canonical http URI is equivalent 
      * to one of the non-http URI in the set of equivalent URIs. 
      * @param uriRefs
      * @return
      */
     private UriRef generateNewHttpUri(Set<UriRef> uriRefs) {
         UriRef bestNonHttp = chooseBest(uriRefs);
         String nonHttpString = bestNonHttp.getUnicodeString();
         if (!nonHttpString.startsWith(URN_SCHEME)) {
             throw new RuntimeException("Sorry we current assume all non-http "
                     + "URIs to be canonicalized to be urn:x-temp, cannot handle: "+nonHttpString);
         }
         String httpUriString = nonHttpString.replaceFirst(URN_SCHEME, baseUri);
         UriRef httpUriRef = new UriRef(httpUriString);
         // add an owl:sameAs statement in the interlinking graph 
         getInterlinkGraph().add(new TripleImpl(bestNonHttp, OWL.sameAs, httpUriRef));
         return httpUriRef;
     }
 
     private UriRef chooseBest(Set<UriRef> httpUri) {
         Iterator<UriRef> iter = httpUri.iterator();
         UriRef best = iter.next();
         while (iter.hasNext()) {
             UriRef next = iter.next();
             if (next.getUnicodeString().compareTo(best.getUnicodeString()) < 0) {
                 best = next;
             }
         }
         return best;
     }
 
 }
