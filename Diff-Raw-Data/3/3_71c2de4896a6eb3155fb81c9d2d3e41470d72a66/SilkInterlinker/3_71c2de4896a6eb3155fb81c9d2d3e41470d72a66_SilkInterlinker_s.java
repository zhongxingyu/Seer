 /**
  *
  */
 package eu.fusepool.enhancer.linking;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import org.apache.clerezza.rdf.core.TripleCollection;
 import org.apache.clerezza.rdf.core.UriRef;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.ConfigurationPolicy;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.component.ComponentContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.fusepool.datalifecycle.Interlinker;
 import eu.fusepool.java.silk.client.SilkClient;
 import eu.fusepool.java.silk.client.SilkException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Set;
 import java.util.UUID;
 import org.apache.clerezza.rdf.core.MGraph;
 import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
 import org.apache.clerezza.rdf.core.serializedform.Parser;
 import org.apache.clerezza.rdf.core.serializedform.Serializer;
 import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
 import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.stanbol.commons.indexedgraph.IndexedMGraph;
 import org.osgi.framework.ServiceReference;
 
 /**
  * @author giorgio
  *
  */
 @Component(immediate = true, metatype = true, configurationFactory = true, // allow
         // multiple
         // instances
         policy = ConfigurationPolicy.OPTIONAL)
 // create a default instance with the default configuration
 @Service(value=Interlinker.class)
 @Properties(value = {
     @Property(name = Constants.SERVICE_RANKING, 
     		intValue = SilkInterlinker.DEFAULT_SERVICE_RANKING),
     @Property(name = SilkInterlinker.SPARQL_ENDPOINT_LABEL,
     		value = SilkInterlinker.DEFAULT_SPARQL_ENDPOINT, 
     		description = "SPARQL endpoint of the target (master) repository")})
 public class SilkInterlinker implements Interlinker {
 
     private static Logger logger = LoggerFactory.getLogger(SilkInterlinker.class);
 
     @Reference
     private Parser parser;
     
     @Reference
     private Serializer serializer;
     
     @Reference
     private SilkClient silk;
     /**
      * Default value for the {@link Constants#SERVICE_RANKING} used by this
      * engine. This is a negative value to allow easy replacement by this engine
      * depending to a remote service with one that does not have this
      * requirement
      */
     public static final int DEFAULT_SERVICE_RANKING = 101;
 
    public static final String DEFAULT_SPARQL_ENDPOINT = "http://localhost:8080/sparql";
 
     // Labels for the component configuration panel
     public static final String SPARQL_ENDPOINT_LABEL = "Endpoint";
 
     protected ComponentContext componentContext;
     protected BundleContext bundleContext;
 
     String sparqlEndpoint = this.DEFAULT_SPARQL_ENDPOINT;
 
     String sparqlGraph = "";
 
     private final static boolean isDebug = true;
 
     @Activate
     protected void activate(ComponentContext ce) throws IOException,
             ConfigurationException {
         this.componentContext = ce;
         this.bundleContext = ce.getBundleContext();
 
         @SuppressWarnings("rawtypes")
         Dictionary dict = ce.getProperties();
         Object o = dict.get(SilkInterlinker.SPARQL_ENDPOINT_LABEL);
         if (o != null && !"".equals(o.toString())) {
             sparqlEndpoint = (String) o;
         }
 
         logger.info("The Silk Linking Service is being activated");
 
     }
 
     @Deactivate
     protected void deactivate(ComponentContext context) {
         logger.info("The Silk Linking Service is being deactivated");
     }
 
     public TripleCollection interlink(TripleCollection dataToInterlink,
             UriRef targetGraphRef) {
         SilkJob job = new SilkJob(bundleContext, sparqlEndpoint, targetGraphRef);
         return job.executeJob(dataToInterlink);
     }
 
     public class SilkJob {
 
         private static final String CI_METADATA_TAG = "[CI_METADATA_FILE]";
         private static final String OUTPUT_TMP_TAG = "[OUTPUT_TMP_FILE_PATH]";
         private static final String SPARQL_ENDPOINT_01_TAG = "[SPARQL_ENDPOINT_01]";
         private static final String SPARQL_GRAPH_01_TAG = "[GRAPH_PARAMETER]";
 
         // sparql endpoint
         String sparqlEndpoint;
         String sparqlGraph;
 
         private static final boolean holdDataFiles = true;
         // private boolean stick2RDF_XML = false ;
 
         protected BundleContext bundleContext;
 
         private String jobId;
         
         
         File rdfData; // source RDF data
         File outputData;
 
         String config;
 
         public SilkJob(BundleContext ctx, String sparqlEndpoint, UriRef targetGraphRef) {
             bundleContext = ctx;
             this.sparqlEndpoint = sparqlEndpoint;
             this.sparqlGraph = targetGraphRef.getUnicodeString();
             logger.info("Silk job started");
         }
 
         @SuppressWarnings("unused")
         public MGraph executeJob(TripleCollection dataToInterlink) {
 
             jobId = UUID.randomUUID().toString();
             try {
                 TripleCollection inputGraph = null;
                 createTempFiles();
 
                 OutputStream rdfOS = new FileOutputStream(rdfData);
                 serializer.serialize(rdfOS, dataToInterlink, SupportedFormat.RDF_XML);
                 rdfOS.close();
                 buildConfig();
 
                 silk.executeStream(IOUtils.toInputStream(config, "UTF-8"), null, 1, true);
 
 			    // This graph will contain the results of the duplicate detection
                 // i.e. owl:sameAs statements
                 MGraph owlSameAsStatements = new SimpleMGraph();
                 InputStream is = new FileInputStream(outputData);
                 Set<String> formats = parser.getSupportedFormats();
                 parser.parse(owlSameAsStatements, is, SupportedFormat.N_TRIPLE);
                 is.close();
                 
                 logger.info(owlSameAsStatements.size() + " triples extracted by job: " + jobId);
                 
                 return owlSameAsStatements;
 
             } catch (IOException e) {
                 throw new RuntimeException("Exception creating the silk files", e);
             } catch (SilkException e) {
                 throw new RuntimeException(e);
             } finally {
                 cleanUpFiles();
             }
         }
 
         private void createTempFiles() {
             String ifName = jobId + "-in.rdf";
             String ofName = jobId + "-out.rdf";
             rdfData = bundleContext.getDataFile(ifName);
             outputData = bundleContext.getDataFile(ofName);
         }
 
         /**
          * builds the configuration for the silk job
          *
          * @throws IOException
          */
         private void buildConfig() throws IOException {
             InputStream cfgIs = this.getClass().getResourceAsStream("silk-config-patents-agents.xml");
             String roughConfig = IOUtils.toString(cfgIs, "UTF-8");
             roughConfig = StringUtils.replace(roughConfig, SPARQL_ENDPOINT_01_TAG, sparqlEndpoint);
             
             if (sparqlGraph != null && !"".equals(sparqlGraph)) {
                 
                 roughConfig = StringUtils.replace(roughConfig, SPARQL_GRAPH_01_TAG, sparqlGraph);
                 
             } else {
                 roughConfig = StringUtils.replace(roughConfig, SPARQL_GRAPH_01_TAG, "");
             }
             
             roughConfig = StringUtils.replace(roughConfig, CI_METADATA_TAG, rdfData.getAbsolutePath());
             config = StringUtils.replace(roughConfig, OUTPUT_TMP_TAG, outputData.getAbsolutePath());
             
             logger.info("configuration built for the job:" + jobId+"\n"+config) ;
             //System.out.println("configuration built for the job:" + jobId + "\n" + config);
         }
 
         /**
          * Removes temporary files
          */
         private void cleanUpFiles() {
             if (holdDataFiles) {
                 return;
             }
             if (rdfData != null) {
                 rdfData.delete();
             }
             if (outputData != null) {
                 outputData.delete();
             }
         }
 
         /**
          * @return the sparqlEndpoint
          */
         public String getSparqlEndpoint() {
             return sparqlEndpoint;
         }
 
         /**
          * @param sparqlEndpoint the sparqlEndpoint to set
          */
         public void setSparqlEndpoint(String sparqlEndpoint) {
             this.sparqlEndpoint = sparqlEndpoint;
         }
 
         /**
          * @return the sparqlGraph
          */
         public String getSparqlGraph() {
             return sparqlGraph;
         }
 
         /**
          * @param sparqlGraph the sparqlGraph to set
          */
         public void setSparqlGraph(String sparqlGraph) {
             this.sparqlGraph = sparqlGraph;
         }
     }
 }
