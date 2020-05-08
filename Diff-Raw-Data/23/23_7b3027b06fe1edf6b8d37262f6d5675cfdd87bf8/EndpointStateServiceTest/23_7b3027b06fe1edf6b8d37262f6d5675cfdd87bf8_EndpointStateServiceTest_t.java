 /**
  * 
  */
 package org.easysoa.registry.integration;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.DiscoveryService;
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.rest.AbstractRestApiTest;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.nuxeo.ecm.core.test.annotations.Granularity;
 import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
 import org.nuxeo.runtime.test.runner.Deploy;
 import com.google.inject.Inject;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 /**
  * Test for Endpoint state service
  * 
  * @author jguillemotte
  *
  */
 @Deploy("org.easysoa.registry.rest.server")
 @RepositoryConfig(cleanup = Granularity.CLASS)
 public class EndpointStateServiceTest extends AbstractRestApiTest {
 
     private static Logger logger = Logger.getLogger(EndpointStateServiceTest.class);
 
     private EndpointStateServiceHelper endpointStateService = new EndpointStateServiceHelper(this);
     
     @Inject
     DocumentService documentService;
     
     @Inject
     DiscoveryService discoveryService;        
     
     /**
      * Init the tests
      * @throws Exception
      */
     @Before
     public void init() throws Exception {
         
     }
 
     /**
      * Test the getSlaOrOlaIndicators REST operation
      */
     @Test
     @Ignore
     public void getSlaOrOlaIndicatorsTest(){
         logTestName(logger);
         
         // Run first test request
         Client client = createAuthenticatedHTTPClient();
         WebResource discoveryRequest = client.resource(endpointStateService.getRootURL()).path("/getSlaOrOlaIndicators").queryParam("search", "test").queryParam("subProjectId", "test");
         //ServiceInformations serviceInformations = discoveryRequest.get(ServiceInformations.class);        
         
         
     }    
     
     /**
      * Test the updateSlaOlaIndicators REST operation
      */
     @Test
     @Ignore
     public void updateSlaOlaIndicatorsTest(){
         logTestName(logger);
         
         // Run first test request
         Client client = createAuthenticatedHTTPClient();
         WebResource discoveryRequest = client.resource(endpointStateService.getRootURL()).path("/getSlaOrOlaIndicators").queryParam("search", "test").queryParam("subProjectId", "test");
         //ServiceInformations serviceInformations = discoveryRequest.post(ServiceInformations.class);        
     }
     
 }
