 package n3phele.factory.test.integration;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.FileNotFoundException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 
 import javax.ws.rs.core.UriBuilder;
 
 import n3phele.security.EncryptedAWSCredentials;
 import n3phele.service.model.core.ExecutionFactoryAssimilateRequest;
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.NameValue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.representation.Form;
 
 public class VirtualServerResourceTest {
 	private String location;
 
 	private Client client;
 
 	private WebResource webResource;
 
 	private TestResource testResource;
 
 	@Before
 	public void setUp() throws Exception {
 		location = "https://ec2.amazonaws.com";
 
 		client = Client.create();
 		client.setConnectTimeout(60000);
 		client.setReadTimeout(60000);
 
 		try {
 			/*
 			 * Create this file, contains sensitive data, must not be pushed to
 			 * repo (delete after running tests) Get testAccessId and
 			 * testAccessKey from EC2 Console -> Security Credentials -> Access
 			 * keys. You can create a new pair of id/key since the key can only
 			 * be seen once
 			 */
 			testResource = new TestResource("n3phele.factory.test.integration.credentials");
 		} catch (FileNotFoundException e) {
 			throw new FileNotFoundException("The necessary file with test credentials was not found. Manually create the file and put real credentials there so integration tests can reach the cloud. See tests for necessary variables.");
 		}
 
 		client.addFilter(new HTTPBasicAuthFilter(testResource.get("factoryUser", ""), testResource.get("factorySecret", "")));
 
 		// Run tests on GAE application, change address
 		// String serverAddress = testResource.get("testServerAddress",
 		// "http://ec2factory2.appspot.com/");
 
 		// Run tests locally
 		String serverAddress = testResource.get("testServerAddress", "http://localhost:8888");
 
 		webResource = client.resource(UriBuilder.fromUri(serverAddress + "/resources/virtualServer").build());
 	}
 
 	@Test
 	public void testAccountTest() throws UnsupportedEncodingException, NoSuchAlgorithmException {
 		WebResource resource = webResource.path("/accountTest");
 
 		String accessId = EncryptedAWSCredentials.encryptX(testResource.get("testAccessId", ""), testResource.get("factorySecret", ""));
 		String secret = EncryptedAWSCredentials.encryptX(testResource.get("testAccessKey", ""), testResource.get("factorySecret", ""));
 
 		Form form = new Form();
 		form.add("fix", true);
 		form.add("id", accessId);
 		form.add("secret", secret);
 		form.add("key", "mykey");
 		form.add("location", location);
 		form.add("email", "test@cpca.pucrs.br");
 		form.add("firstName", "User");
 		form.add("lastName", "LastName");
 		form.add("securityGroup", "default");
 
 		ClientResponse result = resource.post(ClientResponse.class, form);
 
 		assertEquals(200, result.getStatus());
 	}
 
 	@Test
 	public void testCreateVM() throws UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException {
 		WebResource resource = webResource.path("/");
 
 		String accessId = EncryptedAWSCredentials.encryptX(testResource.get("testAccessId", ""), testResource.get("factorySecret", ""));
 		String secret = EncryptedAWSCredentials.encryptX(testResource.get("testAccessKey", ""), testResource.get("factorySecret", ""));
 
 		ExecutionFactoryCreateRequest request = new ExecutionFactoryCreateRequest();
 
 		request.accessKey = accessId;
 		request.encryptedSecret = secret;
 		request.location = new URI(location);
 		request.description = "description";
 		request.name = "vm_name";
 		request.owner = new URI("http://localhost/");
 		// Change idempotencyKey, must be unique value
		request.idempotencyKey = "idempotencyKey12345";
 		ArrayList<NameValue> parameters = new ArrayList<NameValue>();
 		parameters.add(new NameValue("nodeCount", "1"));
 		parameters.add(new NameValue("imageId", "ami-bba18dd2"));
 		parameters.add(new NameValue("instanceType", "t1.micro"));
 		parameters.add(new NameValue("security_groups", "default_lis"));
 		parameters.add(new NameValue("key_name", "lis_key"));
 		parameters.add(new NameValue("user_data", ""));
 		request.parameters = parameters;
 
 		ClientResponse result = resource.post(ClientResponse.class, request);
 
 		assertEquals(201, result.getStatus());
 	}
 
 	@Test
 	public void testAssimilateVM() throws UnsupportedEncodingException, NoSuchAlgorithmException, URISyntaxException {
 		WebResource resource = webResource.path("/assimilate");
 
 		String accessId = EncryptedAWSCredentials.encryptX(testResource.get("testAccessId", ""), testResource.get("factorySecret", ""));
 		String secret = EncryptedAWSCredentials.encryptX(testResource.get("testAccessKey", ""), testResource.get("factorySecret", ""));
 
 		ExecutionFactoryAssimilateRequest request = new ExecutionFactoryAssimilateRequest();
 
 		request.accessKey = accessId;
 		request.encryptedSecret = secret;
 		request.location = new URI(location);
 		request.description = "description";
 		request.name = "";
 		// Change idempotencyKey, must be unique value
		request.idempotencyKey = "idempotency-key0";
 		request.owner = new URI("http://localhost/");
 		request.locationId = "ec2.amazonaws.com";
 		
 		// Fill with public IP of existing amazon instance
		request.ipaddress = "54.204.239.127";
 
 		ClientResponse result = resource.post(ClientResponse.class, request);
 		assertEquals(201, result.getStatus());
 	}
 }
