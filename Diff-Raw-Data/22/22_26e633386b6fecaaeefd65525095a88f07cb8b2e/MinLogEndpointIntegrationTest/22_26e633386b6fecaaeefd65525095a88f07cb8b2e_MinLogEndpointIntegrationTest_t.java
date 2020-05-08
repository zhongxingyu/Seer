 package dk.nsi.minlog;
 
 import static net.javacrumbs.smock.common.server.CommonSmockServer.withMessage;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
 
 import javax.annotation.Resource;
 
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ws.test.server.MockWebServiceClient;
 
 import dk.nsi.minlog.config.ApplicationRootConfig;
 import dk.nsi.minlog.config.TestConfig;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = {ApplicationRootConfig.class, TestConfig.class})
 public class MinLogEndpointIntegrationTest {
 	@Resource
     private ApplicationContext applicationContext;
 
     private MockWebServiceClient mockClient;
 
     
     @Before
     public void createClient() {
         mockClient = MockWebServiceClient.createClient(applicationContext);
     }
 	
    @Ignore
 	@Test
 	public void allByCpr(){
 		mockClient.sendRequest(withMessage("unboundRequest.xml")).andExpect(noFault());
 	}
 }
