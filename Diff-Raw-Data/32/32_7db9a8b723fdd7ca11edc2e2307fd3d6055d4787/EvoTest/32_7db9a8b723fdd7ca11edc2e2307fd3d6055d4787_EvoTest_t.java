 /**
  * 
  */
 package pl.psnc.dl.wf4ever;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.MediaType;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang3.StringUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import pl.psnc.dl.wf4ever.evo.EvoType;
 import pl.psnc.dl.wf4ever.evo.Job.State;
 import pl.psnc.dl.wf4ever.evo.JobStatus;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.test.framework.JerseyTest;
 import com.sun.jersey.test.framework.WebAppDescriptor;
 
 /**
  * @author piotrhol
  * 
  */
 public class EvoTest extends JerseyTest {
 
     private final String clientName = "ROSRS testing app written in Ruby";
 
     private final String clientRedirectionURI = "OOB"; // will not be used
 
     private String clientId;
 
     private final String adminCreds = StringUtils.trim(Base64.encodeBase64String("wfadmin:wfadmin!!!".getBytes()));
 
     private final String userId = UUID.randomUUID().toString();
 
     private final String userIdUrlSafe = StringUtils.trim(Base64.encodeBase64URLSafeString(userId.getBytes()));
 
     private WebResource webResource;
 
     private String accessToken;
 
    private URI ro;
 
     private final String username = "John Doe";
 
 
     public EvoTest() {
         super(new WebAppDescriptor.Builder("pl.psnc.dl.wf4ever").servletClass(TestServletContainer.class).build());
     }
 
 
     @Override
     @Before
     public final void setUp()
             throws Exception {
         super.setUp();
         client().setFollowRedirects(true);
         if (resource().getURI().getHost().equals("localhost")) {
             webResource = resource();
         } else {
             webResource = resource().path("rodl/");
         }
         createClient();
         createUsers();
         createAccessTokens();
         createROs();
     }
 
 
     @Override
     @After
     public void tearDown()
             throws Exception {
         super.tearDown();
         deleteROs();
         deleteAccessTokens();
         deleteUsers();
         deleteClient();
     }
 
 
     @Test
     public final void test()
             throws URISyntaxException {
         JobStatus status = new JobStatus();
        status.setCopyfrom(ro);
         status.setType(EvoType.SNAPSHOT);
         status.setFinalize(false);
         URI copyJob = createCopyJob(status);
         do {
             status = getCopyJobStatus(copyJob, status);
         } while (status.getState() == State.RUNNING);
         checkFinishedCopyJob(copyJob);
 
         JobStatus status2 = new JobStatus();
         status2.setTarget(status.getTarget());
         URI finalizeJob = createFinalizeJob(status2);
         do {
             status = getFinalizeJobStatus(finalizeJob, status2);
         } while (status.getState() == State.RUNNING);
         checkFinishedFinalizeJob(finalizeJob, status.getType());
 
        status.setCopyfrom(ro);
         status.setType(EvoType.SNAPSHOT);
         status.setFinalize(false);
         URI copyAndFinalizeJob = createCopyJob(status);
         do {
             status = getCopyJobStatus(copyAndFinalizeJob, status);
         } while (status.getState() == State.RUNNING);
         checkFinishedFinalizeJob(copyAndFinalizeJob, status.getType());
     }
 
 
     private URI createCopyJob(JobStatus status) {
         ClientResponse response = webResource.path("evo/copy/").header("Authorization", "Bearer " + accessToken)
                 .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, status);
         assertEquals(response.getEntity(String.class), HttpServletResponse.SC_CREATED, response.getStatus());
         return response.getLocation();
     }
 
 
     private JobStatus getCopyJobStatus(URI copyJob, JobStatus original) {
         JobStatus status = webResource.uri(copyJob).header("Authorization", "Bearer " + accessToken)
                 .get(JobStatus.class);
         Assert.assertEquals(original.getCopyfrom(), status.getCopyfrom());
         Assert.assertEquals(original.getType(), status.getType());
         Assert.assertEquals(original.isFinalize(), status.isFinalize());
         Assert.assertNotNull(status.getState());
         return status;
     }
 
 
     private void checkFinishedCopyJob(URI job) {
         JobStatus status = webResource.uri(job).header("Authorization", "Bearer " + accessToken).get(JobStatus.class);
         Assert.assertEquals(State.DONE, status.getState());
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(status.getTarget().toString());
         //TODO verify correct transient RO
     }
 
 
     private URI createFinalizeJob(JobStatus status) {
         ClientResponse response = webResource.path("evo/finalize/").header("Authorization", "Bearer " + accessToken)
                 .type(MediaType.APPLICATION_JSON).post(ClientResponse.class, status);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         return response.getLocation();
     }
 
 
     private JobStatus getFinalizeJobStatus(URI job, JobStatus original) {
         JobStatus status = webResource.uri(job).header("Authorization", "Bearer " + accessToken).get(JobStatus.class);
         Assert.assertEquals(original.getTarget(), status.getTarget());
         Assert.assertNotNull(status.getState());
         return status;
     }
 
 
     private void checkFinishedFinalizeJob(URI job, EvoType type) {
         JobStatus status = webResource.uri(job).header("Authorization", "Bearer " + accessToken).get(JobStatus.class);
         Assert.assertEquals(State.DONE, status.getState());
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(status.getTarget().toString());
         //TODO verify correct finalized RO
     }
 
 
     private void createClient() {
         ClientResponse response = webResource.path("clients/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientName + "\r\n" + clientRedirectionURI);
         URI clientURI = response.getLocation();
         clientId = clientURI.resolve(".").relativize(clientURI).toString();
         response.close();
     }
 
 
     private void createUsers() {
         webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds)
                 .put(ClientResponse.class, username);
     }
 
 
     private void createAccessTokens() {
         ClientResponse response = webResource.path("accesstokens/").header("Authorization", "Bearer " + adminCreds)
                 .post(ClientResponse.class, clientId + "\r\n" + userId);
         URI accessTokenURI = response.getLocation();
         accessToken = accessTokenURI.resolve(".").relativize(accessTokenURI).toString();
         response.close();
     }
 
 
     private void createROs() {
         ClientResponse response = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken)
                .header("Slug", UUID.randomUUID().toString()).post(ClientResponse.class);
        ro = response.getLocation();
     }
 
 
     private void deleteROs() {
        String[] ros = webResource.path("ROs/").header("Authorization", "Bearer " + accessToken).get(String.class)
                .split("\r\n");
        for (String ro : ros) {
            webResource.uri(URI.create(ro)).header("Authorization", "Bearer " + accessToken).delete();
         }
     }
 
 
     private void deleteAccessTokens() {
         webResource.path("accesstokens/" + accessToken).header("Authorization", "Bearer " + adminCreds).delete();
     }
 
 
     private void deleteUsers() {
         webResource.path("users/" + userIdUrlSafe).header("Authorization", "Bearer " + adminCreds).delete();
     }
 
 
     private void deleteClient() {
         webResource.path("clients/" + clientId).header("Authorization", "Bearer " + adminCreds).delete();
     }
 }
