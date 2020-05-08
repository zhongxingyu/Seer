 package n3phele;
 
 import java.net.URI;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import n3phele.service.model.Account;
 import n3phele.service.model.Cloud;
 import n3phele.service.rest.impl.AccountResource;
 import n3phele.service.rest.impl.CloudResource;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.representation.Form;
 
 public class AccountWebServiceTest  {
 	public AccountWebServiceTest() throws Exception {
 
 	}
 
 	String serverUri;
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		client = Client.create();
 		client.addFilter(new HTTPBasicAuthFilter("test-user@gmail.com", "testit!"));
 		serverUri = getServiceAddress();
 		webResource = client.resource(UriBuilder.fromUri(serverUri).path(AccountResource.class).build());
 	}
 	
 	public String getServiceAddress()
 	{
		return "https://n3phele-dev4.appspot.com/resources";		
 	}
 
 	private Client client;
 	private WebResource webResource;
 
 	@Test
 	public void addEC2AccountCloudTest() {
 		URI account = null;
 		Cloud cloud = webResource.uri(UriBuilder.fromUri(serverUri).path(CloudResource.class).build()).path("byName").queryParam("id","EC2").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 
 		if(account == null) {
 
 			Form form = new Form();
 			String myAccountName = "test-account";
 	    	String accountDescription = "this is a test description.";
 	    	String accountId = "account-Id";
 	    	String accountSecret = "biggle";
 			
 			form = new Form();
 			form.add("name", myAccountName);
 			form.add("description", accountDescription);
 			form.add("cloud", cloud.getUri());
 			form.add("factoryId", accountId);
 			form.add("secret", accountSecret);
 	    	
 	    	
 	    	
 	    	ClientResponse accountResponse = webResource.post(ClientResponse.class, form);
 	    	account = accountResponse.getLocation();
 	 
 
 	    	Assert.assertTrue(accountResponse.getStatus()==200 || accountResponse.getStatus()==201);     
 	    	
 	    	n3phele.service.model.Account accountResult = webResource.uri(account).get(Account.class);
 	    	Assert.assertEquals(myAccountName, accountResult.getName());
 	    	Assert.assertEquals(accountDescription, accountResult.getDescription());
 	    	Assert.assertEquals(cloud.getUri(), accountResult.getCloud());
 	    	Assert.assertNull(accountResult.getCredential()); 
 		}
 		
 
 
 	}
 	@Test
 	public void addHPAccountCloudTest(){
 		URI account = null;
 		Cloud cloud = webResource.uri(UriBuilder.fromUri(serverUri).path(CloudResource.class).build()).path("byName").queryParam("id","HPZone1").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 		if(account == null) {
 			
 			Form form = new Form();
 			String myAccountName = "HPTest-account";
 	    	String accountDescription = "this is a test description.";
 	    	String accountId = "HPAccount-Id";
 	    	String accountSecret = "biggle";
 			
 			form = new Form();
 			form.add("name", myAccountName);
 			form.add("description", accountDescription);
 			form.add("cloud", cloud.getUri());
 			form.add("factoryId", accountId);
 			form.add("secret", accountSecret);
 			
 			ClientResponse accountResponse = webResource.post(ClientResponse.class, form);
 	    	account = accountResponse.getLocation();
 	    	
 	    	Assert.assertTrue(accountResponse.getStatus()==200 || accountResponse.getStatus()==201);     
 	    	
 	    	n3phele.service.model.Account accountResult = webResource.uri(account).get(Account.class);
 	    	Assert.assertEquals(myAccountName, accountResult.getName());
 	    	Assert.assertEquals(accountDescription, accountResult.getDescription());
 	    	Assert.assertEquals(cloud.getUri(), accountResult.getCloud());
 	    	Assert.assertNull(accountResult.getCredential()); 
 		}
 	}
 	
 	//@Test
 	public void testHPAccountDelete(){
 		Cloud cloud = webResource.path("byName").queryParam("id","HPTest-account").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	  	ClientResponse response = webResource.uri(cloud.getUri()).delete(ClientResponse.class);
 
 	  	Assert.assertEquals(204, response.getStatus()); 
 	}
 	
 	
 	// @Test
 	  public void testAccountDelete() throws Exception {
 
 		Cloud cloud = webResource.path("byName").queryParam("id","test-account").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	  	ClientResponse response = webResource.uri(cloud.getUri()).delete(ClientResponse.class);
 
 	  	Assert.assertEquals(204, response.getStatus()); 
 	  	 
 	  }
 
 }
