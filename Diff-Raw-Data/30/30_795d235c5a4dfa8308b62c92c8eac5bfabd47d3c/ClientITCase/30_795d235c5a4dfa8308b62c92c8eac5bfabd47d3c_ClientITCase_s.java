 package eu.dm2e.ws.tests.integration.services;
 
 import eu.dm2e.grafeo.Grafeo;
 import eu.dm2e.grafeo.jena.GrafeoImpl;
 import eu.dm2e.grafeo.junit.GrafeoAssert;
 import eu.dm2e.ws.Config;
 import eu.dm2e.ws.ConfigProp;
 import eu.dm2e.ws.DM2E_MediaType;
 import eu.dm2e.ws.api.FilePojo;
 import eu.dm2e.ws.services.Client;
 import eu.dm2e.ws.tests.OmnomTestCase;
 import org.apache.commons.io.IOUtils;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import javax.ws.rs.client.Invocation;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.Response;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 import org.joda.time.DateTime;
 import eu.dm2e.logback.LogbackMarkers;
 
 public class ClientITCase extends OmnomTestCase {
 	
 	Client client;
 	private static final String FILE_CONTENTS = "FILE_CONTENTS";
 	private static final String ORIGINAL_NAME = "FOO.BAR";
 	private FilePojo FILE_POJO = new FilePojo();
 	private File TEMP_FILE;
 	private DateTime DUMMY_TIME = DateTime.now();
 	
 	private boolean isCorrectlyPosted(String uri) {
 		Invocation invoc = client.target(uri).request().build("GET");
 //		log.debug("" + invoc.);
 		String respStr = invoc.invoke(String.class);
 		assertThat(respStr, is(FILE_CONTENTS));
 		return respStr.equals(FILE_CONTENTS);
 	}
 
 	private boolean isMetadataKept(String fileUri) {
 		FilePojo fp = new FilePojo();
 		try {
 			fp.loadFromURI(fileUri);
 		} catch (Exception e) {
 			log.error("Could not reload file pojo. " + e);
 			e.printStackTrace();
 			return false;
 		}
 		assertTrue(fp.getGrafeo().size() > 0);
 		fp.setModified(DUMMY_TIME);
		fp.setFileRetrievalURI("http://dummy");
 		log.debug(LogbackMarkers.DATA_DUMP, "METADATA WAS: " + fp.getGrafeo().getTerseTurtle());
 		log.debug(LogbackMarkers.DATA_DUMP, "METADATA EXPECTED: " + FILE_POJO.getGrafeo().getTerseTurtle());
		GrafeoAssert.graphContainsGraphStructurally(fp.getGrafeo(), FILE_POJO.getGrafeo());
 
 		return true;
 //		if (null == fp.getOriginalName()
 //				||
 //				fp.getOriginalName().equals(FILE_POJO.getOriginalName();
 //				) {
 //			throw new ComparisonFailure("Metadata mismatch", FILE_POJO.getTerseTurtle(), fp.getTerseTurtle());
 //		}
 	}
 
 	@Before
 	public void setUp() throws Exception {
 
         client = new Client();
 		FILE_POJO.setOriginalName(ORIGINAL_NAME);
 		FILE_POJO.setModified(DUMMY_TIME);
 		TEMP_FILE = File.createTempFile("omnom_test", "txt");
 		IOUtils.copy(IOUtils.toInputStream(FILE_CONTENTS), new FileOutputStream(TEMP_FILE));
 	}
 
 	@Test
 	public void testGetJerseyClient() {
 		javax.ws.rs.client.Client jc = client.getJerseyClient();
 		assertNotNull(jc);
 	}
 
 	@Test
 	public void testConfigJobFile() {
 		Map<String, WebTarget>uriToWR = new HashMap<>();
 		uriToWR.put(Config.get(ConfigProp.CONFIG_BASEURI), client.getConfigWebTarget());
 		uriToWR.put(Config.get(ConfigProp.FILE_BASEURI), client.getFileWebTarget());
 		uriToWR.put(Config.get(ConfigProp.JOB_BASEURI), client.getJobWebTarget());
 		for (Map.Entry<String, WebTarget> entry : uriToWR.entrySet()) {
 			String uri = entry.getKey();
 			WebTarget wr = entry.getValue();
 			assertNotNull(wr);
 			assertNotNull(uri);
 			Response resp = wr.request(DM2E_MediaType.APPLICATION_RDF_TRIPLES).get();
 			assertEquals(200, resp.getStatus());
 			Grafeo g = new GrafeoImpl(resp.readEntity(InputStream.class));
 			log.info(g.getTerseTurtle());
 			assertTrue(
 					g.containsTriple(uri,
 					"rdf:type",
 					"omnom:Webservice")
 			);
 		}
 	}
 	@Test
 	public void testPublishFileString() {
 		String fileUri = client.publishFile(FILE_CONTENTS);
 		assertTrue(isCorrectlyPosted(fileUri));
 	}
 
 	@Test
 	public void testPublishFileStringGrafeo() {
 		String fileUri = client.publishFile(FILE_CONTENTS, FILE_POJO.getGrafeo());
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileStringFilePojo() {
 		String fileUri = client.publishFile(FILE_CONTENTS, FILE_POJO);
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileInputStream() throws IOException {
 		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS));
 		assertTrue(isCorrectlyPosted(fileUri));
 	}
 
 	@Test
 	public void testPublishFileInputStreamString() throws IOException {
 		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS), FILE_POJO.getNTriples());
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileInputStreamGrafeo() throws IOException {
 		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS), FILE_POJO.getGrafeo());
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileInputStreamFilePojo() throws IOException {
 		String fileUri = client.publishFile(IOUtils.toInputStream(FILE_CONTENTS), FILE_POJO);
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileFile() throws IOException {
 		String fileUri = client.publishFile(TEMP_FILE);
 		assertTrue(isCorrectlyPosted(fileUri));
 	}
 
 	@Test
 	public void testPublishFileFileString() throws IOException {
 		String fileUri = client.publishFile(TEMP_FILE, FILE_POJO.getNTriples());
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileFileGrafeo() throws IOException {
 		String fileUri = client.publishFile(TEMP_FILE, FILE_POJO.getGrafeo());
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileFileFilePojo() throws IOException {
 		String fileUri = client.publishFile(TEMP_FILE, FILE_POJO);
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	@Test
 	public void testPublishFileStringString() {
 		String fileUri = client.publishFile(FILE_CONTENTS, FILE_POJO.getNTriples());
 		assertTrue(isCorrectlyPosted(fileUri));
 		assertTrue(isMetadataKept(fileUri));
 	}
 
 	
 	@Ignore("Not yet implemented")
 	@Test
 	public void testCreateFileFormDataMultiPartStringString() {
 	}
 
 	@Ignore("Not yet implemented")
 	@Test
 	public void testCreateFileFormDataMultiPartFilePojoString() {
 		fail("Not yet implemented");
 	}
 
 	@Ignore("Not yet implemented")
 	@Test
 	public void testCreateFileFormDataMultiPartGrafeoString() {
 		fail("Not yet implemented");
 	}
 
 
 }
