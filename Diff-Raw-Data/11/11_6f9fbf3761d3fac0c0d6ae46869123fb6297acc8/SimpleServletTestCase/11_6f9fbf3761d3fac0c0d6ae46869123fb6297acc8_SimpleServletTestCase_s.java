 package io.undertow.integration.test.basic;
 
import static org.junit.Assert.assertEquals;

 import java.net.URL;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * @author Stuart Douglas
  */
 @RunWith(Arquillian.class)
 @RunAsClient
 public class SimpleServletTestCase {
 
     @ArquillianResource
     private URL url;
 
     @Deployment
     public static WebArchive deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class,"SimpleServlet.war");
         archive.addClass(SimpleServlet.class);
         return archive;
     }
 
     private String performCall(URL url, String urlPattern) throws Exception {
         DefaultHttpClient client = new DefaultHttpClient();
        return client.execute(new HttpGet(url + urlPattern)).getStatusLine().getReasonPhrase();
         //return HttpRequest.get(url.toExternalForm() + urlPattern, 1000, SECONDS);
     }
 
 
     @Test
     public void test() throws Exception {
         String result = performCall(url, "SimpleServlet");
         assertEquals("OK", result);
     }
 
 }
