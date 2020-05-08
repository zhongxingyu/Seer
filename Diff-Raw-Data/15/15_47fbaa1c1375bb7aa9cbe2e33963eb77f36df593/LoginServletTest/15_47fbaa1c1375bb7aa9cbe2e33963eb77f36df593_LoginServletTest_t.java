 package no.steria.swhrs;
 
 import com.gargoylesoftware.htmlunit.*;
 import com.gargoylesoftware.htmlunit.util.NameValuePair;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import javax.servlet.http.HttpServletResponse;
 import java.net.URL;
 import java.util.Arrays;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 public class LoginServletTest {
 
     private static WebClient client;
     private static JettyServer jettyServer;
 
     @BeforeClass
     public static void setUp() throws Exception {
         client = new WebClient();
         jettyServer = new JettyServer();
         jettyServer.setPort(8888);
         jettyServer.startServer();
     }
 
     @Test
     public void testSuccessfulLoginResponse() throws Exception {
         WebRequestSettings settings = new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/login"), HttpMethod.POST);
         settings.setRequestParameters(Arrays.asList(
                 new NameValuePair("username", "matb"),
                new NameValuePair("password", Password.fromPlaintext("salt", "password").toString())
         ));
         assertThat(client.getPage(settings).getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
     }
 
     @Test(expected = FailingHttpStatusCodeException.class)
     public void testUnsuccessfulLoginResponse() throws Exception {
         WebRequestSettings settings = new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/login"), HttpMethod.POST);
         settings.setRequestParameters(Arrays.asList(
                 new NameValuePair("username", "matb"),
                new NameValuePair("password", Password.fromPlaintext("salt", "passwort").toString())
         ));
         WebResponse webResponse = client.getPage(settings).getWebResponse();
     }
 }
