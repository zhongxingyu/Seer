 package pl.psnc.dl.wf4ever.resources;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Properties;
 
 import junit.framework.Assert;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.W4ETest;
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.searchserver.SearchServer;
 import pl.psnc.dl.wf4ever.searchserver.solr.SolrSearchServer;
 
 public class SearchServerTest extends W4ETest {
 
     protected String urlConnectionString;
     protected static final String CONNECTION_PROPERTIES_FILENAME = "connection.properties";
     protected String getAllQuery = "*:*";
     protected String cleaningString = "update?commit=true -d  '<delete><query>*:*</query></delete>'";
 
 
     @Override
     public void setUp()
             throws Exception {
         super.setUp();
         SolrSearchServer.get().clearIndex();
         createUser(userIdSafe, username);
         accessToken = createAccessToken(userId);
         try (InputStream is = Thing.class.getClassLoader().getResourceAsStream(CONNECTION_PROPERTIES_FILENAME)) {
             Properties props = new Properties();
             props.load(is);
             urlConnectionString = props.getProperty("solrServer");
 
         } catch (Exception e) {
             //do nth, it's just a test
         }
     }
 
 
     @Override
     public void tearDown()
             throws Exception {
         SolrSearchServer.get().clearIndex();
         deleteAccessToken(accessToken);
         deleteUser(userIdSafe);
         super.tearDown();
     }
 
 
     @Test
     public void testUpdateIndexAttributes()
             throws IOException, SolrServerException {
         URI ro1uri = createRO(accessToken);
         URI ro2uri = createRO(accessToken);
         SearchServer searchServer = SolrSearchServer.get();
         QueryResponse response = searchServer.query("*:*");
         Assert.assertEquals(2, response.getResults().size());
 
     }
 
 
     @Test
     public void testSearchByAnnotation() {
         URI searchRO = createRO("search-ro/", accessToken);
         InputStream is = getClass().getClassLoader().getResourceAsStream("resources/searchROAnnotationBody.ttl");
         URI annotationUri = addAnnotation(is, searchRO, accessToken).getLocation();
     }
 
 
     @Test
     public void testDeleteIndexAttributes()
             throws SolrServerException {
         URI ro1uri = createRO(accessToken);
         SearchServer searchServer = SolrSearchServer.get();
         QueryResponse response = searchServer.query("*:*");
         Assert.assertEquals(1, response.getResults().size());
         deleteROs();
         response = searchServer.query("*:*");
         Assert.assertEquals(0, response.getResults().size());
     }
 
 }
