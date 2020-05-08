 package org.kie.tests.drools.wb.jboss.test;
 
import static org.junit.Assert.*;
import static org.kie.tests.drools.wb.jboss.TestConstants.*;

 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.UUID;

 import javax.ws.rs.core.MediaType;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.container.test.api.RunAsClient;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.arquillian.test.api.ArquillianResource;
 import org.jboss.resteasy.client.ClientRequest;
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.shrinkwrap.api.Archive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kie.tests.drools.wb.jboss.base.DroolsWbWarJbossDeploy;
 import org.kie.workbench.common.services.shared.rest.CreateOrCloneRepositoryRequest;
 import org.kie.workbench.common.services.shared.rest.CreateProjectRequest;
 import org.kie.workbench.common.services.shared.rest.Entity;
import org.kie.workbench.common.services.shared.rest.JobRequest.Status;
import org.kie.workbench.common.services.shared.rest.BuildConfig;
import org.kie.workbench.common.services.shared.rest.CompileProjectRequest;
import org.kie.workbench.common.services.shared.rest.CreateOrganizationalUnitRequest;
 import org.kie.workbench.common.services.shared.rest.JobResult;
 import org.kie.workbench.common.services.shared.rest.OrganizationalUnit;
 import org.kie.workbench.common.services.shared.rest.RepositoryRequest;
 import org.kie.workbench.common.services.shared.rest.RepositoryResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @RunAsClient
 @RunWith(Arquillian.class)
 public class DroolsWbRestJbossIntegrationTest extends DroolsWbWarJbossDeploy {
 
     private static Logger logger = LoggerFactory.getLogger(DroolsWbRestJbossIntegrationTest.class);
     
     @ArquillianResource
     URL deploymentUrl;
     
     @Deployment(testable = false, name="drools-wb")
     public static Archive<?> createWar() {
        return createWarWithTestDeploymentLoader("test");
     }
     
     @Test
     public void manipulatingRepositories() throws Exception { 
         // rest/repositories GET
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         String urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/repositories").toExternalForm();
         ClientRequest restRequest = createRequest(requestFactory, urlString);
         ClientResponse<?> responseObj = checkResponse(restRequest.get());
         Collection<RepositoryResponse> repoResponses = responseObj.getEntity(Collection.class);
         assertTrue( repoResponses.size() > 0 );
         String ufPlaygroundUrl = null;
         Iterator<?> iter = repoResponses.iterator();
         while( iter.hasNext() ) { 
             Map<String, String> repoRespMap = (Map<String, String>) iter.next();
             if( "uf-playground".equals(repoRespMap.get("name")) ) { 
                 ufPlaygroundUrl = repoRespMap.get("gitURL");
             }
         }
         assertEquals( "UF-Playground Git URL", "git://uf-playground", ufPlaygroundUrl );
         
         // rest/repositories POST
         urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/repositories").toExternalForm();
         restRequest = createRequest(requestFactory, urlString);
         RepositoryRequest newRepo = new RepositoryRequest();
         String repoName = UUID.randomUUID().toString();
         newRepo.setName(repoName);
         newRepo.setDescription("repo for testing rest services");
         newRepo.setRequestType("new");
         addToRequestBody(restRequest, newRepo);
         responseObj = checkResponse(restRequest.post());
         
         CreateOrCloneRepositoryRequest createJobRequest = responseObj.getEntity(CreateOrCloneRepositoryRequest.class);
         logger.debug("]] " + convertObjectToJsonString(createJobRequest));
         assertNotNull( "create repo job request", createJobRequest);
        assertEquals( "job request status", Status.ACCEPTED, createJobRequest.getStatus() );
         String jobId = createJobRequest.getJobId();
         
         // rest/jobs/{jobId} GET
         waitForJobToComplete(jobId, createJobRequest.getStatus(), requestFactory);
         
         // rest/repositories/{repoName}/projects POST
         urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/repositories/" + repoName + "/projects").toExternalForm();
         restRequest = createRequest(requestFactory, urlString);
         Entity project = new Entity();
         project.setDescription("test project");
         String testProjectName = "test-project";
         project.setName(testProjectName);
         addToRequestBody(restRequest, project);
         responseObj = checkResponse(restRequest.post());
         CreateProjectRequest createProjectRequest = responseObj.getEntity(CreateProjectRequest.class);
         logger.debug("]] " + convertObjectToJsonString(createProjectRequest));
         
         // rest/jobs/{jobId} GET
         waitForJobToComplete(jobId, createProjectRequest.getStatus(), requestFactory);
     }
     
     @Test
     public void mavenOperations() throws Exception { 
         // rest/repositories GET
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         String urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/repositories").toExternalForm();
         ClientRequest restRequest = createRequest(requestFactory, urlString);
         ClientResponse<?> responseObj = checkResponse(restRequest.get());
         Collection<Map<String, String>> repoResponses = responseObj.getEntity(Collection.class);
         assertTrue( repoResponses.size() > 0 );
         String repoName = repoResponses.iterator().next().get("name");
        
         // rest/repositories/{repoName}/projects POST
         urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/repositories/" + repoName + "/projects").toExternalForm();
         restRequest = createRequest(requestFactory, urlString);
         Entity project = new Entity();
         project.setDescription("test project");
         String projectName = UUID.randomUUID().toString();
         project.setName(projectName);
         addToRequestBody(restRequest, project);
         responseObj = checkResponse(restRequest.post());
         CreateProjectRequest createProjectRequest = responseObj.getEntity(CreateProjectRequest.class);
         logger.debug("]] " + convertObjectToJsonString(createProjectRequest));
         
         // rest/jobs/{jobId} GET
         waitForJobToComplete(createProjectRequest.getJobId(), createProjectRequest.getStatus(), requestFactory);
 
         // rest/repositories/{repoName}/projects POST
         String mavenOperBase = "rest/repositories/" + repoName + "/projects/" + projectName + "/maven/";
         urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + mavenOperBase + "compile").toExternalForm();
         restRequest = createRequest(requestFactory, urlString);
         responseObj = checkResponse(restRequest.post());
         CompileProjectRequest compileRequest = responseObj.getEntity(CompileProjectRequest.class);
         logger.debug("]] " + convertObjectToJsonString(compileRequest));
         
         // rest/jobs/{jobId} GET
         waitForJobToComplete(createProjectRequest.getJobId(), createProjectRequest.getStatus(), requestFactory);
         
         
     }
     
    private void waitForJobToComplete(String jobId, Status jobStatus, ClientRequestFactory requestFactory) throws Exception { 
        while( jobStatus.equals(Status.ACCEPTED) ) { 
             String urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/jobs/" + jobId).toExternalForm();
             ClientRequest restRequest = createRequest(requestFactory, urlString);
             ClientResponse<?> responseObj = checkResponse(restRequest.get());
             JobResult jobResult = responseObj.getEntity(JobResult.class);
             logger.debug( "]] " + convertObjectToJsonString(jobResult) );
             assertEquals( jobResult.getJobId(), jobId );
             jobStatus = jobResult.getStatus();
         }
     }
     
     @Test
     public void manipulatingOUs() throws Exception { 
         // rest/organizaionalunits GET
         ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
         String urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/organizationalunits").toExternalForm();
         ClientRequest restRequest = createRequest(requestFactory, urlString);
         ClientResponse<?> responseObj = checkResponse(restRequest.get());
         Collection<OrganizationalUnit> orgUnits = responseObj.getEntity(Collection.class);
         int origUnitsSize = orgUnits.size();
         
         // rest/organizaionalunits POST
         urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/organizationalunits").toExternalForm();
         restRequest = createRequest(requestFactory, urlString);
         OrganizationalUnit orgUnit = new OrganizationalUnit();
         orgUnit.setDescription("Test OU");
         orgUnit.setName(UUID.randomUUID().toString());
         orgUnit.setOwner(this.getClass().getSimpleName());
         addToRequestBody(restRequest, orgUnit);
         responseObj = checkResponse(restRequest.post());
         CreateOrganizationalUnitRequest createOURequest = responseObj.getEntity(CreateOrganizationalUnitRequest.class);
 
         // rest/jobs/{jobId}
         waitForJobToComplete(createOURequest.getJobId(), createOURequest.getStatus(), requestFactory);
         
         // rest/organizaionalunits GET
         urlString = new URL(deploymentUrl,  deploymentUrl.getPath() + "rest/organizationalunits").toExternalForm();
         restRequest = createRequest(requestFactory, urlString);
         responseObj = checkResponse(restRequest.get());
         orgUnits = responseObj.getEntity(Collection.class);
         assertEquals( "Exepcted an OU to be added.", origUnitsSize + 1, orgUnits.size());
     }
     
     private ClientResponse<?> checkResponse(ClientResponse<?> responseObj) throws Exception {
         logger.debug("<< Response received");
         responseObj.resetStream();
         int status = responseObj.getStatus(); 
         if( status != 200 ) { 
             logger.warn("Response with exception:\n" + responseObj.getEntity(String.class));
             assertEquals( "Status OK", 200, status);
         } 
         return responseObj;
     }
     
     private ClientRequest createRequest(ClientRequestFactory requestFactory, String urlString) { 
         ClientRequest restRequest = requestFactory.createRequest(urlString);
         restRequest.accept(MediaType.APPLICATION_JSON_TYPE);
         logger.debug( ">> " + urlString);
         return restRequest;
     }
     
     private void addToRequestBody(ClientRequest restRequest, Object obj) throws Exception { 
         String body = convertObjectToJsonString(obj);
         logger.debug( "]] " + body );
         restRequest.body(MediaType.APPLICATION_JSON_TYPE, body);
     }
     
     private static ObjectMapper mapper = new ObjectMapper();
     static {
         mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);
     }
         
     public static String convertObjectToJsonString(Object object) throws JsonGenerationException, JsonMappingException, IOException {
         return mapper.writeValueAsString(object);
     }
         
     public static Object convertJsonStringToObject(String jsonStr, Class<?> type) throws JsonParseException, JsonMappingException, IOException {
         return mapper.readValue(jsonStr, type);
     }
 
 }
