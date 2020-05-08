 package no.steria.swhrs;
 
 import com.gargoylesoftware.htmlunit.HttpMethod;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebRequestSettings;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.util.NameValuePair;
 import com.sun.deploy.net.HttpResponse;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import javax.security.auth.login.FailedLoginException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.when;
 
 public class AuthorizationFilterTest {
     private static WebClient client;
     private static JettyServer jettyServer;
 
     @BeforeClass
     public static void setUp() throws Exception {
         client = new WebClient();
         client.setThrowExceptionOnFailingStatusCode(false);
         client.setThrowExceptionOnScriptError(false);
         jettyServer = new JettyServer();
         jettyServer.setPort(8888);
         jettyServer.startServer();
     }
 
     @Test
     public void testShallDenyProtectedResource() throws Exception {
         HtmlPage page = client.getPage(new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/hours"), HttpMethod.GET));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
     }
 
 
     @Test
     public void testAllowUnprotectedResource() throws Exception {
         HtmlPage page = client.getPage(new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/"), HttpMethod.GET));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
     }
 
     @Test
     public void testAllowAfterLogin() throws Exception {
         login();
 
         HtmlPage page = client.getPage(new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/hours"), HttpMethod.GET));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_OK);
 
         logout();
 
         page = client.getPage(new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/hours"), HttpMethod.GET));
         assertThat(page.getWebResponse().getStatusCode()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
     }
 
     private void logout() throws IOException {
         client.getPage(new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/logout"), HttpMethod.POST));
     }
 
     private void login() throws IOException {
         WebRequestSettings settings = new WebRequestSettings(new URL("http://localhost:8888/swhrs-app/login"), HttpMethod.POST);
         settings.setRequestParameters(Arrays.asList(
                 new NameValuePair("username", "matb"),
                new NameValuePair("password", Password.fromPlaintext("salt", "password").toString())
         ));
         client.getPage(settings);
     }
 }
