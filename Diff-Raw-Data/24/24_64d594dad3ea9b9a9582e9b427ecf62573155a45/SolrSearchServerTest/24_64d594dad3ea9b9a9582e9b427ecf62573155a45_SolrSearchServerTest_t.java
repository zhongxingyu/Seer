 package pl.psnc.dl.wf4ever.searchserver.solr;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
import org.junit.Ignore;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.model.BaseTest;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.searchserver.SearchServer;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
@Ignore
 public class SolrSearchServerTest extends BaseTest {
 
     private static final String CONNECTION_PROPERTIES = "connection.properties";
 
 
     @Test
     public void testGetSolrServerInstance()
             throws SolrServerException, IOException {
         SearchServer server = SolrSearchServer.get();
         Assert.assertNotNull(server);
     }
 
 
     @Test
     public void testConnectionProperties()
             throws IOException {
         SearchServer server = SolrSearchServer.get();
         InputStream is = Thing.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES);
         Properties props = new Properties();
         props.load(is);
         String connectionUrl = props.getProperty("solrServer");
         Assert.assertEquals(connectionUrl, server.getConnectionUrl());
     }
 
 
     @Test
     public void testSaveAndQueryROAttributes()
             throws SolrServerException {
         SearchServer server = SolrSearchServer.get();
         Multimap<URI, Object> map = ArrayListMultimap.create();
         map.put(URI.create("attribute"), "attributeValue");
         server.saveROAttributes(researchObject.getUri(), map);
         QueryResponse asnwer = server.query("property_attribute:attributeValue");
         Assert.assertTrue(0 < asnwer.getResults().toArray().length);
         asnwer = server.query("uri:" + "\"" + researchObject.getUri().toString() + "\"");
         Assert.assertEquals(1, asnwer.getResults().toArray().length);
         server.deleteROAttributes(researchObject.getUri());
 
         //version2
         URI sourceUri = researchObject.getUri().resolve("new-indexes-resource");
         server.saveROAttributes(researchObject.getUri(), map, sourceUri);
         asnwer = server.query("property_attribute:attributeValue");
         Assert.assertTrue(0 < asnwer.getResults().toArray().length);
         asnwer = server.query("uri:" + "\"" + sourceUri.toString() + "\"");
         Assert.assertEquals(1, asnwer.getResults().toArray().length);
         server.deleteROAttributes(sourceUri);
 
     }
 
 
     @Test
     public void testDeleteROAttributes()
             throws SolrServerException {
         SearchServer server = SolrSearchServer.get();
         Multimap<URI, Object> map = ArrayListMultimap.create();
         server.saveROAttributes(researchObject.getUri(), map);
         QueryResponse asnwer = server.query("uri:" + "\"" + researchObject.getUri().toString() + "\"");
         Assert.assertEquals(1, asnwer.getResults().toArray().length);
         server.deleteROAttributes(researchObject.getUri());
         asnwer = server.query("uri:" + "\"" + researchObject.getUri().toString() + "\"");
         Assert.assertEquals(0, asnwer.getResults().toArray().length);
     }
 
 
     @Test
     public void testSaveROWithNoAttributes()
             throws SolrServerException {
         SearchServer server = SolrSearchServer.get();
         Multimap<URI, Object> map = ArrayListMultimap.create();
         server.saveROAttributes(researchObject.getUri(), map);
         QueryResponse asnwer = server.query("uri:" + "\"" + researchObject.getUri().toString() + "\"");
         Assert.assertEquals(1, asnwer.getResults().toArray().length);
         server.deleteROAttributes(researchObject.getUri());
     }
 
 
     @Test
     public void testSaveROWithNullAttributes()
             throws SolrServerException {
         SearchServer server = SolrSearchServer.get();
         server.saveROAttributes(researchObject.getUri(), null);
         QueryResponse asnwer = server.query("uri:" + "\"" + researchObject.getUri().toString() + "\"");
         Assert.assertEquals(1, asnwer.getResults().toArray().length);
         server.deleteROAttributes(researchObject.getUri());
     }
 
 
     @Test
     public void testClearIndex()
             throws SolrServerException, IOException {
         SearchServer server = SolrSearchServer.get();
         server.saveROAttributes(researchObject.getUri(), null);
         QueryResponse asnwer = server.query("uri:" + "\"" + researchObject.getUri().toString() + "\"");
         Assert.assertEquals(1, asnwer.getResults().toArray().length);
         server.clearIndex();
         asnwer = server.query("*:*");
         Assert.assertEquals(0, asnwer.getResults().toArray().length);
     }
 }
