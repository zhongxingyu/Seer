 package de.lbe.sandbox.metrics;
 
 import static de.asideas.lib.commons.test.hamcrest.Matchers.mapWithSize;
 
 import java.net.URL;
 import java.util.Map;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import de.asideas.lib.commons.arquillian.AbstractJUnit4ArquillianTest;
 import de.asideas.lib.commons.cdi.Startup;
 import de.asideas.lib.commons.shrinkwrap.ShrinkWrapUtils;
 import de.asideas.lib.commons.test.restclient.RestClient;
 import de.lbe.sandbox.metrics.webapp.Hello;
 import de.lbe.sandbox.metrics.webapp.HelloWorldResource;
 
 /**
  * @author lars.beuster
  */
 public class RestTest extends AbstractJUnit4ArquillianTest {
 
 	@ArquillianResource
 	private URL contextPathURL;
 
 	private RestClient restClient;
 
 	/**
 	 * 
 	 */
 	@Before
 	public void setUp() {
 		this.restClient = RestClient.create(this.contextPathURL);
 	}
 
 	/**
 	 * 
 	 */
 	@After
 	public void tearDown() {
 		this.restClient.close();
 	}
 
 	/**
 	 * 
 	 */
 	@Deployment(testable = false)
 	public static WebArchive deployment() {
 		WebArchive war = ShrinkWrapUtils.prepareCdiWar("sandbox.war");
 		ShrinkWrapUtils.addDirectory(war, "src/main/webapp");
 		ShrinkWrapUtils.addPackagesWithTestClasses(war, HelloWorldResource.class.getPackage().getName());
 
		ShrinkWrapUtils.addArchiveOfClassAsLib(war, Startup.class, "asideas-cdi.jar");
 		ShrinkWrapUtils.addArchiveOfClassAsLib(war, MetricNameUtils.class, "metric.jar");
 		return war;
 	}
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testHello() throws Exception {
 
 		final int n = 10;
 		for (int i = 0; i < n; i++) {
 			Hello hello = this.restClient.path("rest/hello").acceptJSON().get().assertIsStatusOk().assertIsJSON().getEntity(Hello.class);
 			assertNotNull(hello);
 			assertEquals("HALLO", hello.getMessage());
 			assertEquals(i + 1, hello.getCounter());
 		}
 	}
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testRestHealthChecks() throws Exception {
 
 		Map healthChecks = this.restClient.path("rest/healthchecks").acceptJSON().get().assertIsStatusOk().assertIsJSON().getEntity(Map.class);
 		Map myCheck = (Map) healthChecks.get("myHealthCheck");
 		assertNotNull(myCheck);
 	}
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testServletHealthChecks() throws Exception {
 		Map<?, ?> checks = this.restClient.path("admin/metrics").path("healthcheck").acceptJSON().get().assertIsStatusOk().assertIsJSON().getEntity(Map.class);
 		assertThat(checks, mapWithSize(2));
 	}
 
 	/**
 	 * 
 	 */
 	@Test
 	public void testAdminServlet() throws Exception {
 		String html = this.restClient.path("admin/metrics").acceptJSON().get().assertIsStatusOk().assertIsHTML().getEntity(String.class);
 		assertNotNull(html);
 	}
 }
