 /**
  * 
  */
 package org.purl.wf4ever.wf2ro.rest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.net.URI;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.MediaType;
 
 import org.junit.After;
 import org.junit.Test;
 import org.purl.wf4ever.rosrs.client.common.ROSRService;
 import org.purl.wf4ever.wf2ro.rest.Job.Status;
 import org.scribe.model.Token;
 
 import uk.org.taverna.scufl2.translator.t2flow.T2FlowReader;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.test.framework.JerseyTest;
 
 /**
  * @author piotrekhol
  * 
  */
 public class RestApiTest
 	extends JerseyTest
 {
 
 	private static final URI WF_URI = URI.create("http://www.myexperiment.org/workflows/2648/download?version=1");
 
 	private static final URI TAVERNA_FORMAT = URI.create(T2FlowReader.APPLICATION_VND_TAVERNA_T2FLOW_XML);
 
 	private static final URI RO_URI = URI.create("http://sandbox.wf4ever-project.org/rosrs5/ROs/"
 			+ UUID.randomUUID().toString() + "/");
 
 	private static final URI RO2_URI = URI.create("http://sandbox.wf4ever-project.org/rosrs5/ROs/"
 			+ UUID.randomUUID().toString() + "/");
 
 	private static final Token TOKEN = new Token("47d5423c-b507-4e1c-8", null);
 
 	private static final long MAX_JOB_TIME_S = 120;
 
 	private WebResource webResource;
 
 
 	@After
 	public void tearDown()
 	{
 		ROSRService.deleteResearchObject(RO_URI, TOKEN);
 		ROSRService.deleteResearchObject(RO2_URI, TOKEN);
 	}
 
 
 	public RestApiTest()
 	{
 		super("org.purl.wf4ever.wf2ro.rest");
 	}
 
 
 	@Test
 	public void test()
 		throws InterruptedException
 	{
 		if (resource().getURI().getHost().equals("localhost")) {
 			webResource = resource();
 		}
 		else {
			webResource = resource().path("wf2ro/");
 		}
 
 		Form f = new Form();
 		f.add("resource", WF_URI);
 		f.add("format", TAVERNA_FORMAT);
 		f.add("ro", RO_URI);
 		f.add("token", TOKEN.getToken());
 
 		JobConfig config = new JobConfig(WF_URI, TAVERNA_FORMAT, RO2_URI, TOKEN.getToken());
 
 		ClientResponse response = webResource.path("jobs").post(ClientResponse.class, f);
 		assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
 		URI jobURI = response.getLocation();
 		response.close();
 
 		ClientResponse response2 = webResource.path("jobs").type(MediaType.APPLICATION_JSON_TYPE)
 				.post(ClientResponse.class, config);
 		assertEquals(HttpServletResponse.SC_CREATED, response2.getStatus());
 		URI job2URI = response2.getLocation();
 		response2.close();
 
 		JobStatus status = null;
 
 		status = webResource.uri(job2URI).get(JobStatus.class);
 		assertTrue(status.getStatus() == Status.RUNNING || status.getStatus() == Status.DONE);
 		assertEquals(WF_URI, status.getResource());
 		assertEquals(TAVERNA_FORMAT, status.getFormat());
 		assertEquals(RO2_URI, status.getRo());
 
 		response2 = webResource.uri(job2URI).delete(ClientResponse.class);
 		assertEquals(HttpServletResponse.SC_NO_CONTENT, response2.getStatus());
 		response2.close();
 		response2 = webResource.uri(job2URI).get(ClientResponse.class);
 		assertEquals(HttpServletResponse.SC_GONE, response2.getStatus());
 		response2.close();
 
 		for (int i = 0; i < MAX_JOB_TIME_S; i++) {
 			System.out.print(".");
 			status = webResource.uri(jobURI).get(JobStatus.class);
 			assertTrue(status.getStatus() == Status.RUNNING || status.getStatus() == Status.DONE);
 			assertEquals(WF_URI, status.getResource());
 			assertEquals(TAVERNA_FORMAT, status.getFormat());
 			assertEquals(RO_URI, status.getRo());
 			if (status.getStatus() == Status.DONE) {
 				System.out.println();
 				break;
 			}
 			Thread.sleep(1000);
 		}
 		System.out.println(webResource.uri(jobURI).get(String.class));
 		if (status.getStatus() == Status.RUNNING) {
 			fail("The job hasn't finished on time");
 		}
 		assertNotNull(status.getAdded());
 		assertEquals(2, status.getAdded().size());
 	}
 }
