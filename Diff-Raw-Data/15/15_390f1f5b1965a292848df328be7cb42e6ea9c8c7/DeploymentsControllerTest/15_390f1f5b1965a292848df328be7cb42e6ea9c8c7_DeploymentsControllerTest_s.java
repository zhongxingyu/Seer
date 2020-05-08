 package test.rest.component;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.cloudifysource.dsl.internal.CloudifyMessageKeys;
 import org.cloudifysource.dsl.rest.request.SetApplicationAttributesRequest;
 import org.cloudifysource.dsl.rest.response.DeleteServiceInstanceAttributeResponse;
 import org.cloudifysource.dsl.rest.response.Response;
 import org.cloudifysource.dsl.rest.response.ServiceDetails;
 import org.openspaces.admin.application.Application;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 import framework.utils.ApplicationInstaller;
 import framework.utils.AssertUtils;
 import framework.utils.rest.CloudifyRestClient;
 import framework.utils.rest.RestClientException;
 
 public class DeploymentsControllerTest extends AbstractLocalCloudTest {
 
 	private CloudifyRestClient client;
 
 	@BeforeClass
 	public void installTravel() throws IOException, InterruptedException {
 		ApplicationInstaller installer = new ApplicationInstaller(restUrl, "helloworld");
 		installer.recipePath("helloworld");
 		installer.install();
 		client = new CloudifyRestClient(restUrl + "/" + PlatformVersion.getVersion() + "/deployments");
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testGoodGetServiceDetails() throws Exception {
 		Application app = admin.getApplications().waitFor("helloworld", OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		AssertUtils.assertTrue("Failed to discover application helloworld" , app != null);
 		ServiceDetails details = client.getServiceDetails("helloworld", "tomcat");
 		AssertUtils.assertEquals("helloworld", details.getApplicationName());
 		AssertUtils.assertEquals("tomcat", details.getName());
 		AssertUtils.assertEquals(1, details.getNumberOfInstances());
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testBadGetServiceDetails() throws Exception {
 		try {
 			client.getServiceDetails("helloworldBad", "tomcat");
 			AssertUtils.assertFail("getServiceDetails request should have thrown an exception due to a wrong application name");
 		} catch (RestClientException e) {
 			AssertUtils.assertEquals(CloudifyMessageKeys.APPLICATION_WAIT_TIMEOUT.getName(), e.getMessageId());
 			AssertUtils.assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatus());
 		}
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testSetApplicationAttributes() throws Exception {
 		SetApplicationAttributesRequest request = new SetApplicationAttributesRequest();
 		Map<String, Object> attributes = new HashMap<String, Object>();
 		attributes.put("A", "ValueA");
 		request.setAttributes(attributes);
 		Response<Void> response = client.setApplicationAttributes("helloworld", request);
 		AssertUtils.assertEquals(CloudifyMessageKeys.OPERATION_SUCCESSFULL.getName(), response.getMessageId());
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testDeleteServiceInstanceAttribute() throws Exception {
 		DeleteServiceInstanceAttributeResponse response = client.deleteServiceInstanceAttribute("helloworld", "tomcat", 1, "Key");
 		AssertUtils.assertEquals(response.getAttributeName(), "Key");
 		AssertUtils.assertEquals(response.getAttributeLastValue(), null);
 	}
 }
