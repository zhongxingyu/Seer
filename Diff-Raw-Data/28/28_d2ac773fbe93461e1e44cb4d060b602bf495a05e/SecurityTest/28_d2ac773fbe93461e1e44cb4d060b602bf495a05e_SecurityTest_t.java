 package security;
 
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import test.Tests;
 
 import com.sun.enterprise.security.auth.login.FileLoginModule;
 
 @RunWith(Arquillian.class)
 public class SecurityTest {
 
 	private static final String PATH = "src/test/resources/security";
 
 	@ArquillianResource
 	private URL deploymentUrl;
 
 	@Deployment(testable = false)
 	public static WebArchive createDeployment() {
 		return Tests.createDeployment().addClasses(SecurityServlet.class, FileLoginModule.class)
 				.addAsWebInfResource(Tests.createFileAsset(PATH + "/web.xml"), "web.xml");
 	}
 
 	@Test
 	public void loginSucessfull() throws ClientProtocolException, IOException, URISyntaxException {
 		URIBuilder uriBuilder = new URIBuilder(deploymentUrl + "/login");
 		uriBuilder.setParameter("username", "demoiselle");
 		uriBuilder.setParameter("password", "changeit");
 
 		HttpGet httpGet = new HttpGet(uriBuilder.build());
		HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
 
 		int status = httpResponse.getStatusLine().getStatusCode();
		assertEquals(SC_OK, status);
 	}
 
 	@Test
	public void loginFailed() throws ClientProtocolException, IOException, URISyntaxException {
		URIBuilder uriBuilder = new URIBuilder(deploymentUrl + "/login");
		uriBuilder.setParameter("username", "invalid");
		uriBuilder.setParameter("password", "invalid");

		HttpGet get = new HttpGet(uriBuilder.build());
		HttpResponse response = HttpClientBuilder.create().build().execute(get);
 
 		int status = response.getStatusLine().getStatusCode();
		assertEquals(SC_FORBIDDEN, status);
 	}
 }
