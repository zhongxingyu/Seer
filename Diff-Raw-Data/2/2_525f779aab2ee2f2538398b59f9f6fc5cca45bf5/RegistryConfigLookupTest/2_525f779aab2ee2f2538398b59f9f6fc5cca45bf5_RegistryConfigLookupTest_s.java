 package org.mule.galaxy.mule1;
 
 
 import java.io.InputStream;
 import java.util.List;
 
 import org.apache.abdera.i18n.text.UrlEncoding;
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.protocol.client.AbderaClient;
 import org.apache.abdera.protocol.client.ClientResponse;
 import org.apache.abdera.protocol.client.RequestOptions;
 import org.apache.axiom.om.util.Base64;
 import org.mule.galaxy.test.AbstractAtomTest;
 import org.mule.galaxy.util.IOUtils;
 
 public class RegistryConfigLookupTest extends AbstractAtomTest {
 
     public void testMuleLookup() throws Exception {
         AbderaClient client = new AbderaClient(abdera);
 
         String url = "http://localhost:9002/api/registry";
         
 //        // POST a Mule configuration
 //        RequestOptions opts = new RequestOptions();
 //        opts.setContentType("application/xml; charset=utf-8");
 //        opts.setSlug("hello-config.xml");
 //        opts.setHeader("X-Artifact-Version", "0.1");
 //        opts.setHeader("X-Workspace", "Default Workspace");
 //        opts.setAuthorization("Basic " + Base64.encode("admin:admin".getBytes()));
 //        ClientResponse res = client.post(url, getClass().getResourceAsStream("/mule/hello-config.xml"), opts);
 //        assertEquals(201, res.getStatus());
 //        
         // TODO: this query language will improve in the future, so don't read too much into it yet
        String search = UrlEncoding.encode("select artifact where mule2.service = 'GreeterUMO'");
         url = url + "?q=" + search;
         
         // GET a Feed with Mule Configurations which match the criteria
         RequestOptions defaultOpts = client.getDefaultRequestOptions();
         defaultOpts.setAuthorization("Basic " + Base64.encode("admin:admin".getBytes()));
         ClientResponse res = client.get(url, defaultOpts);
         assertEquals(200, res.getStatus());
         
         Document<Feed> feedDoc = res.getDocument();
         // prettyPrint(feedDoc);
         List<Entry> entries = feedDoc.getRoot().getEntries();
         assertEquals(1, entries.size());
         Entry entry = entries.get(0);
         
         // GET the actual mule configuration
         String muleConfigUrlLink = entry.getContentSrc().toString();
         System.out.println(muleConfigUrlLink);
         res = client.get(muleConfigUrlLink, defaultOpts);
         assertEquals(200, res.getStatus());
         
         // Use this as your handle to the mule configuration
         InputStream is = res.getInputStream();
         IOUtils.copy(is, System.out);
     }
     
     protected String getWebappDirectory() {
         return "../../web/src/main/webapp";
     }
 }
