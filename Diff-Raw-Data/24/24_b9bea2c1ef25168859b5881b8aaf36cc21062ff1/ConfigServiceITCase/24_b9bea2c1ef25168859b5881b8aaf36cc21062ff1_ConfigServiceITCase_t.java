 // <<<<<<< HEAD
 package eu.dm2e.ws.services.data;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.junit.matchers.JUnitMatchers.*;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Arrays;
 
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import eu.dm2e.ws.DM2E_MediaType;
 import eu.dm2e.ws.ErrorMsg;
 import eu.dm2e.ws.NS;
 import eu.dm2e.ws.OmnomTestCase;
 import eu.dm2e.ws.OmnomTestResources;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.jena.GrafeoImpl;
 import eu.dm2e.ws.grafeo.junit.GrafeoAssert;
 import eu.dm2e.ws.services.Client;
 
 public class ConfigServiceITCase extends OmnomTestCase {
 
 	private Client client;
 	private WebTarget webTarget;
 
 	@Before
 	public void setUp() throws Exception {
 		client = new Client();
 		webTarget = client.getJerseyClient().target(URI_BASE).path("config");
 	}
 
 	public void testGetConfig() {
 		URI configURI = null;
 		{
 			Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
 			Response resp = webTarget.request().post(
 					Entity.entity(gOut.getCanonicalNTriples(),
 							DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 			assertThat(resp.getStatus(), is(201));
 			configURI = resp.getLocation();
 		}
 		assertNotNull(configURI);
 		// n-triple
 		{
 			Response resp = client.target(configURI)
 					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES).get();
 			final String respStr = resp.readEntity(String.class);
 			log.warn(respStr);
 			assertThat(resp.getStatus(), is(200));
 			assertThat(resp.getMediaType().toString(), is(DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 		}
 		// turtle
 		{
 			Response resp = client.target(configURI).request(DM2E_MediaType.TEXT_TURTLE).get();
 			final String respStr = resp.readEntity(String.class);
 			log.warn(respStr);
 			assertThat(resp.getStatus(), is(200));
 			assertThat(resp.getMediaType().toString(), is(DM2E_MediaType.TEXT_TURTLE));
 		}
 		// json
 		{
 			Response resp = client.target(configURI).request(MediaType.APPLICATION_JSON).get();
 			final String respStr = resp.readEntity(String.class);
 			log.warn(respStr);
 			assertThat(resp.getStatus(), is(200));
 			assertThat(resp.getMediaType(), is(MediaType.APPLICATION_JSON_TYPE));
 		}
 	}
 
 	@Test
 	public void testGetConfigList() {
 		URI configURI = null;
 		{
 			for (OmnomTestResources x : Arrays.asList(
 				OmnomTestResources.DEMO_SERVICE_WORKING,
 				OmnomTestResources.DEMO_SERVICE_ILLEGAL_PARAMETER
 			)) {
 				Grafeo gOut = new GrafeoImpl(configFile.get(x));
 				Response resp = webTarget.request().post(
 						Entity.entity(gOut.getCanonicalNTriples(),
 								DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 				assertThat(resp.getStatus(), is(201));
 				configURI = resp.getLocation();
 			}
 		}
 		assertNotNull(configURI);
 		// n-triple
 		{
 			Response resp = webTarget.path("list")
 					.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES).get();
 			final String respStr = resp.readEntity(String.class);
 			log.warn(respStr);
 			assertThat(resp.getStatus(), is(200));
 			assertThat(resp.getMediaType().toString(), is(DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 		}
 		// turtle
 		{
 			Response resp = webTarget.path("list").request(DM2E_MediaType.TEXT_TURTLE).get();
 			final String respStr = resp.readEntity(String.class);
 			log.warn(respStr);
 			assertThat(resp.getStatus(), is(200));
 			assertThat(resp.getMediaType().toString(), is(DM2E_MediaType.TEXT_TURTLE));
 		}
 		// json
 		{
 			Response resp = webTarget.path("list").request(MediaType.APPLICATION_JSON).get();
 			final String respStr = resp.readEntity(String.class);
 			log.warn(respStr);
 			assertThat(resp.getStatus(), is(200));
 			assertThat(resp.getMediaType(), is(MediaType.APPLICATION_JSON_TYPE));
 		}
 	}
 
 	@Test
 	public void testPostBadSyntax() {
		Response resp = webTarget
				.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES)
				.post(Entity.entity("FOO", DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 		assertEquals(400, resp.getStatus());
 		String respStr = resp.readEntity(String.class);
 		assertThat(respStr, containsString(ErrorMsg.BAD_RDF.toString()));
 	}
 
 	@Test
 	public void testPostNoBlank() {
		Response resp = webTarget
				.request()
				.post(Entity.entity(configString .get(OmnomTestResources.DEMO_SERVICE_NO_TOP_BLANK),
						DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 		assertEquals(400, resp.getStatus());
 		String respStr = resp.readEntity(String.class);
 		assertThat(respStr, containsString(ErrorMsg.NO_TOP_BLANK_NODE.toString()));
 	}
 
 	@Test
 	public void testPostGoodSyntax() {
 		Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
 		{
			Response resp = webTarget.request().post(Entity.entity(gOut.getNTriples(), DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 			assertThat(resp.getMediaType(), is(DM2E_MediaType.APPLICATION_RDF_TRIPLES_TYPE));
 			assertEquals(201, resp.getStatus());
 			assertNotNull(resp.getLocation());
 			File respFile = resp.readEntity(File.class);
 			Grafeo gIn = new GrafeoImpl(respFile);
 			assertEquals("Contains no blank nodes", 0, gIn.listAnonStatements(null, null, null).size());
 			log.info(gIn.getCanonicalNTriples());
 		}
 		{
 			Response resp = webTarget
 					.request(MediaType.APPLICATION_JSON)
					.post(Entity.entity(gOut.getNTriples(), DM2E_MediaType.APPLICATION_RDF_TRIPLES));
 			assertEquals(201, resp.getStatus());
 			assertThat(resp.getMediaType(), is(MediaType.APPLICATION_JSON_TYPE));
 			final String respStr = resp.readEntity(String.class);
 			log.debug("Response body: " + respStr);
 		}
 	}
 
 	@Test
 	public void testPut() {
 		Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
 		Response respPOST = webTarget
 				.request().post(gOut.getNTriplesEntity());
 		assertEquals(201, respPOST.getStatus());
 		assertNotNull(respPOST.getLocation());
 
 		Response respGET1 = client
 				.target(respPOST.getLocation())
 				.request()
 				.get();
 //		final String respStr = respGET1.readEntity(String.class);
 //		log.debug(respStr);
 		assertThat("For the default request media type, return N-TRIPLE",
 				respGET1.getMediaType(),
 				is(DM2E_MediaType.APPLICATION_RDF_TRIPLES_TYPE));
 		assertEquals(200, respGET1.getStatus());
 		GrafeoImpl gGET1 = new GrafeoImpl(respGET1.readEntity(InputStream.class));
 		GrafeoAssert.graphContainsGraph(gGET1, gOut);
 
 		Response respPUT = client
 				.target(respPOST.getLocation())
 				.request()
 				.put(gOut.getNTriplesEntity());
 		assertEquals(201, respPUT.getStatus());
 		assertNotNull(respPUT.getLocation());
 		assertEquals(respPOST.getLocation(), respPUT.getLocation());
 
 		Response respGET2 = client
 				.target(respPUT.getLocation())
 				.request()
 				.get();
 		assertEquals(200, respGET2.getStatus());
 		GrafeoImpl gGET2 = new GrafeoImpl(respGET2.readEntity(InputStream.class));
 		assertEquals(gGET1.getCanonicalNTriples(), gGET2.getCanonicalNTriples());
 		// assertTrue(gIn.isGraphEquivalent(gOut));
 
 	}
 
 	@Test
 	// @Ignore("This is *not* a valid webserviceConfig because it omits a reference to the Webservice.")
 	public void testData() {
 		Response response = webTarget
 				.request()
				.post(Entity.entity(
 						"[] <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://onto.dm2e.eu/omnom/WebserviceConfig> ;" +
 //		 NOTE: dc:creator isn't in a WebserviceConfigPojo so it will *not* be stored
 //							" <http://purl.org/dc/terms/creator> <http://localhost/kai>;" +
 							" <http://onto.dm2e.eu/omnom/webservice> <http://localhost:9998/service/xslt>."
						, DM2E_MediaType.APPLICATION_RDF_TRIPLES));
		assertEquals(201, response.getStatus());
 		Grafeo gPosted = new GrafeoImpl();
 		final String confRespStr = response.readEntity(String.class);
 		log.info(confRespStr);
 		gPosted.readHeuristically(confRespStr);
 		log.info(gPosted.getTerseTurtle());
 		Grafeo gBuilt = new GrafeoImpl();
 		final String confUri = response.getLocation().toString();
 		gBuilt.addTriple(confUri,
 				NS.OMNOM.PROP_WEBSERVICE,
 				"http://localhost:9998/service/xslt");
 		// NOTE: dc:creator isn't in a WebserviceConfigPojo so it will *not* be stored
 //		gBuilt.addTriple(response.getLocation().toString(),
 //				"http://purl.org/dc/terms/creator",
 //				"http://localhost/kai");
 		gBuilt.addTriple(confUri,
 				NS.RDF.PROP_TYPE,
 				NS.OMNOM.CLASS_WEBSERVICE_CONFIG);
 		GrafeoAssert.graphContainsGraph(gPosted, gBuilt);
 
 		gBuilt = new GrafeoImpl();
 		gBuilt.addTriple(confUri,
 				"http://doesnotexist.org/bla",
 				gPosted.resource("http://localhost/kai"));
 		gBuilt.addTriple(confUri,
 				"rdf:type",
 				gPosted.resource("omnom:WebServiceConfig"));
 		assertFalse(gPosted.isGraphEquivalent(gBuilt));
 	}
 
 	@Test
 	public void testValidate() {
 		log.info("Validating Valid ...");
 		{
 			Grafeo gOut = new GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_WORKING));
 			Response respPOST = client.getConfigWebTarget()
 					.request().post(gOut.getNTriplesEntity());
 			assertEquals(201, respPOST.getStatus());
 			URI uri = respPOST.getLocation();
 			assertNotNull(uri);
 			String validateUri = uri.toString() + "/validate";
 			Response resp = client.target(validateUri).request().get();
 			assertEquals(200, resp.getStatus());
 		}
 		log.info("Validating Invalid ...");
 		{
 			Grafeo gOut = new GrafeoImpl(
 					configFile.get(OmnomTestResources.DEMO_SERVICE_NO_TOP_BLANK));
 			Response respPOST = client.getConfigWebTarget()
 					.request().post(gOut.getNTriplesEntity());
 			assertEquals(400, respPOST.getStatus());
 			String respStr = respPOST.readEntity(String.class);
 			assertThat(respStr, containsString(ErrorMsg.NO_TOP_BLANK_NODE.toString()));
 		}
 		// log.info("Validating Invalid ...");
 		// {
 		// Grafeo gOut = new
 		// GrafeoImpl(configFile.get(OmnomTestResources.DEMO_SERVICE_NO_TOP_BLANK));
 		// Response respPOST = client.getConfigWebTarget()
 		// .request().post(Response.class, gOut.getCanonicalNTriples());
 		// assertEquals(400, respPOST.getStatus());
 		// String respStr = respPOST.readEntity(String.class);
 		// assertThat(respStr,
 		// containsString(ErrorMsg.NO_TOP_BLANK_NODE.toString()));
 		// }
 	}
 
 }
