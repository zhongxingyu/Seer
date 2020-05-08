 package pl.psnc.dl.wf4ever.searchserver.solr;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrInputDocument;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.evo.EvoType;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.searchserver.SearchServer;
 import pl.psnc.dl.wf4ever.vocabulary.ROEVO;
 
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 /**
  * Solr implementation of search/idex server represented by SearchServer interface.
  * 
  * @author pejot
  * 
  */
 public class SolrSearchServer implements SearchServer {
 
     /** logger. */
     private static final Logger LOGGER = Logger.getLogger(SearchServer.class);
 
     /** the only instance. */
     private static SearchServer instance;
 
     /** Solr instance. */
     private HttpSolrServer server;
 
     /** Server url. */
     private static String url;
 
     /** connection properties file name. */
     private static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties";
 
 
     /**
      * Constructor, opens connection to the solr server.
      * 
      * @param url
      *            solr URL
      */
     private SolrSearchServer(String url) {
         this.url = url;
         server = new HttpSolrServer(url);
     }
 
 
     /**
      * Constructor, opens connection to the solr server. Read the solr url from connection.propertie file.
      */
     private SolrSearchServer() {
         try (InputStream is = Thing.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES_FILENAME)) {
             Properties props = new Properties();
             props.load(is);
             url = props.getProperty("solrServer");
 
         } catch (Exception e) {
             LOGGER.error("Trple store location can not be loaded from the properties file", e);
         }
 
         server = new HttpSolrServer(url);
     }
 
 
     /**
      * Get the only instance.
      * 
      * @return a search server singleton instance
      */
     public static SearchServer get() {
         if (instance == null) {
             instance = new SolrSearchServer();
         }
         return instance;
     }
 
 
     public static String getUrl() {
         return url;
     }
 
 
     public static void setUrl(String url) {
         SolrSearchServer.url = url;
     }
 
 
     @Override
     public void saveROAttributes(URI ro, Multimap<URI, Object> attributes) {
         saveROAttributes(ro, attributes, ro);
     }
 
 
     @Override
     public void deleteROAttributes(URI ro) {
         deleteROAttributes(ro, ro);
     }
 
 
     @Override
     public void saveRO(ResearchObject ro, Multimap<URI, Object> roAttributes) {
         SolrInputDocument document = new SolrInputDocument();
         document.addField("uri", ro.getUri().toString());
         document.addField("ro_uri", ro.getUri().toString());
         if (ro.getCreator() != null) {
            document.addField("creator", ro.getCreator().getUri().toString());
         }
         document.addField("created", ro.getCreated().toDate());
         Model model = ModelFactory.createDefaultModel();
         model.read(ro.getEvoInfo().getGraphAsInputStream(RDFFormat.RDFXML), null);
         Resource r = model.getResource(ro.getUri().toString());
         if (r.hasProperty(RDF.type, ROEVO.LiveRO)) {
             document.addField("evo_type", EvoType.LIVE);
         } else if (r.hasProperty(RDF.type, ROEVO.ArchivedRO)) {
             document.addField("evo_type", EvoType.ARCHIVE);
         } else if (r.hasProperty(RDF.type, ROEVO.SnapshotRO)) {
             document.addField("evo_type", EvoType.SNAPSHOT);
         }
         if (ro.getAggregatedResources() != null) {
             document.addField("resources_size", ro.getAggregatedResources().size());
         }
         if (ro.getAnnotations() != null) {
             document.addField("annotations_size", ro.getAnnotations().size());
         }
         if (roAttributes != null) {
             for (Map.Entry<URI, Object> entry : roAttributes.entries()) {
                 document.addField("property_" + entry.getKey().toString(), entry.getValue());
             }
         }
 
         Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
         docs.add(document);
         try {
             server.add(docs);
             server.commit();
         } catch (SolrServerException | IOException e) {
             LOGGER.error("Could not add a document to the Solr server", e);
         }
     }
 
 
     @Override
     public void saveROAttributes(URI ro, Multimap<URI, Object> attributes, URI source) {
         SolrInputDocument document = new SolrInputDocument();
         document.addField("uri", source.toString());
         document.addField("ro_uri", ro.toString());
         if (attributes != null) {
             for (Map.Entry<URI, Object> entry : attributes.entries()) {
                 document.addField("property_" + entry.getKey().toString(), entry.getValue());
             }
         }
         Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
         docs.add(document);
         try {
             server.add(docs);
             server.commit();
         } catch (SolrServerException | IOException e) {
             LOGGER.error("Could not add a document to the Solr server", e);
         }
     }
 
 
     @Override
     public void deleteROAttributes(URI ro, URI source) {
         try {
             server.deleteById(source.toString());
             server.commit();
         } catch (SolrServerException | IOException e) {
             LOGGER.error("Could not delete a document from the Solr server", e);
         }
     }
 
 
     @Override
     public String getConnectionUrl() {
         return getUrl();
     }
 
 
     @Override
     public QueryResponse query(SolrQuery query)
             throws SolrServerException {
         return server.query(query);
     }
 
 
     @Override
     public QueryResponse query(String query)
             throws SolrServerException {
         return server.query(new SolrQuery(query));
 
     }
 
 
     @Override
     public void clearIndex()
             throws IOException, SolrServerException {
         server.deleteByQuery("*:*");
         server.commit();
     }
 
 }
