 package voyager.quickstart;
 
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
 
 /**
  * Integration test requires a running Voyager instance
  */
 public abstract class IntegrationBase {
 
   protected String baseURL;
   protected SolrServer solr;
   protected DefaultHttpClient httpclient;
   
   @Before
   public void initClient()
   {
     baseURL = System.getProperty("voyager.url");
     if(baseURL==null) {
       baseURL = "http://localhost:7777/";
     }
     if(!baseURL.endsWith("/")) {
       baseURL += "/";
     }
     solr = new HttpSolrServer(baseURL+"solr/v0");
 
     httpclient = new DefaultHttpClient();
     httpclient.getCredentialsProvider().setCredentials(
         AuthScope.ANY,
         // Add the default credentials
         new UsernamePasswordCredentials("admin", "admin"));
   }
   
   @After
   public void shutdownClient() {
     httpclient.getConnectionManager().shutdown();
     httpclient = null;
   }
 }
