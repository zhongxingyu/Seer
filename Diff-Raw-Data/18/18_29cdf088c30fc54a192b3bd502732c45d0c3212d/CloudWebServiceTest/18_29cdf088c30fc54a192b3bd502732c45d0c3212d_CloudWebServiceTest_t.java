  package n3phele;
 
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.UriBuilder;
 
 import n3phele.client.CacheManager;
 import n3phele.service.model.Cloud;
 import n3phele.service.model.core.BaseEntity;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.Entity;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.rest.impl.CloudResource;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.representation.Form;
 
 public class CloudWebServiceTest  {
 	public CloudWebServiceTest() throws Exception {
 
 	}
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		client = Client.create();
 		client.addFilter(new HTTPBasicAuthFilter("test-user@gmail.com", "testit!"));
 		//client.addFilter(new HTTPBasicAuthFilter("root", "n3phelepoa"));
 		webResource = client.resource(UriBuilder.fromUri(getServiceAddress()).path(CloudResource.class).build());
 	}
 
 	private Client client;
 	private WebResource webResource;
 		
 	public String getServiceAddress()
 	{
 		return new CacheManager(null).ServiceAddress;
 	}	
 	
 	public Map<String,Double> readFile(String fileName){
 		Map<String, Double> map = new HashMap<String,Double>();
 		try{
 			  FileInputStream fstream = new FileInputStream(fileName);
 			  DataInputStream in = new DataInputStream(fstream);
 			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			  
 			  String strLine;
 			  while ((strLine = br.readLine()) != null)   {	 
 				 
 				  String[] values = strLine.split(" ");
 				  System.out.println (values[0]+" "+values[1]);
 				  map.put(values[0],Double.parseDouble(values[1]));
 			  }
 			  in.close();
 		}catch (Exception e){
 			  System.err.println("Error: " + e.getMessage());
 		}
 		
 		return map;
 	}
 
 	@Test
 	public void addEC2CloudTest() {
 		URI cloud = null;
 		Collection<BaseEntity> response = webResource.queryParam("summary", "false").get(new GenericType<Collection<BaseEntity>>() {});
 		if(response.getTotal() > 0) {
 			for(Entity c : response.getElements()) {
 				if(c.getName().equals("EC2 - USWest2")) {
 					cloud = c.getUri();
 				}
 			}
 		}
 
 		if(cloud == null) {
 
 			
 			
 			String myName = "EC2";
 			String description = "Amazon Elastic Compute Service";
 			URI location = URI.create("https://ec2.amazonaws.com");
 			URI factory =  URI.create("https://ec2factory.appspot.com/resources/virtualServer");
 			String factoryId = "fred";
 			String mySecret = "3hyebbehg56yeh5";
 			Form form = new Form();
 			form.add("name", myName);
 			form.add("description", description);
 			form.add("location", location);
 			form.add("factory", factory);
 			form.add("factoryId", factoryId);
 			form.add("secret", mySecret);
 			form.add("isPublic", true);
 			form.add("costDriverName","instanceType");
 
 			ClientResponse result = webResource.post(ClientResponse.class, form);
 			cloud = result.getLocation();
 			
 			Assert.assertEquals(201, result.getStatus());  
 		}
 
 	}
 	
	static String UBUNTU_1204_64BITS = "ami-a73264ce";
	
 	static TypedParameter EC2Defaults[] = {
 		new TypedParameter("instanceType", "specifies virtual machine size. Valid Values: t1.micro | m1.small | m1.large | m1.xlarge | m2.xlarge | m2.2xlarge | m2.4xlarge | c1.medium | c1.xlarge", n3phele.service.model.core.ParameterType.String, "", "t1.micro"),
		new TypedParameter("imageId", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", UBUNTU_1204_64BITS),
 		new TypedParameter("securityGroups", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", "n3phele-default"),
		new TypedParameter("userData", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "", "#!/bin/bash\necho n3phele agent injection... \nset -x\n apt-get update;  apt-get install -y openjdk-6-jre-headless \n wget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ubuntu ~/agent ~/sandbox https://region-a.geo-1.objects.hpcloudsvc.com:443/v1/AUTH_dc700102-734c-4a97-afc8-50530e87a171/n3phele-agent/n3phele-agent.tgz' ubuntu\n")		
 	};
 	
 	@Test
 	public void testInitCloudDefaults() throws Exception {
 		Cloud cloud = webResource.path("byName").queryParam("id","EC2").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 		for(TypedParameter t : EC2Defaults) {
 			Form form = new Form();
 			form.add("key", t.getName());
 			form.add("defaultValue", t.getDefaultValue());
			form.add("type", t.getType().toString());			
 	
 			ClientResponse result = webResource.uri(cloud.getUri()).path("inputParameter").post(ClientResponse.class, form);
 			Assert.assertEquals(200, result.getStatus());  
 		}
 
 	}
 	
 	@Test
 	public void testAddCloudCosts(){
 		Cloud cloud = webResource.path("byName").queryParam("id","EC2").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
  		Map<String,Double> map = readFile("EC2_us_west_2.txt");
 		
 		for (Entry<String,Double> entryPrice : map.entrySet())
 		{						
 			Form form = new Form();
 			form.add("key", entryPrice.getKey());
 			form.add("value",entryPrice.getValue().toString());
 			
 			ClientResponse result = webResource.uri(cloud.getUri()).path("costMap").post(ClientResponse.class, form);
 			Assert.assertEquals(200, result.getStatus());  
 		}
 	}
 
 	
 	 //@Test
 	  public void testCloudDelete() throws Exception {
 
 		Cloud cloud = webResource.path("byName").queryParam("id","EC2").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	  	ClientResponse response = webResource.uri(cloud.getUri()).delete(ClientResponse.class);
 
 	  	Assert.assertEquals(204, response.getStatus()); 
 	  	 
 	  }
 	  
 		@Test
 		public void addHPCloudTest() {
 			URI cloud = null;
 			
 			String cloudName = "HPZone1";
 			
 			Collection<BaseEntity> response = webResource.queryParam("summary", "false").get(new GenericType<Collection<BaseEntity>>() {});
 			if(response.getTotal() > 0) {
 				for(Entity c : response.getElements()) {
 					if(c.getName().equals(cloudName)) {
 						cloud = c.getUri();
 					}
 				}
 			}
 
 			if(cloud == null) {
 
 				String myName = cloudName;
 				String description = "HP Cloud";
 				URI location = URI.create("https://az-1.region-a.geo-1.compute.hpcloudsvc.com/v1.1/12365734013392");
 				URI factory =  URI.create("https://nova-factory.appspot.com/resources/virtualServer");
 				//URI factory =  URI.create("http://127.0.0.1:8889/resources/virtualServer");
 				String factoryId = "user";
 				String mySecret = "password";
 				
 				//Prices for Linux machines
 				String costDriverName = "flavorRef";
 				
 				Form form = new Form();
 				form.add("name", myName);
 				form.add("description", description);
 				form.add("location", location);
 				form.add("factory", factory);
 				form.add("factoryId", factoryId);
 				form.add("secret", mySecret);
 				form.add("isPublic", true);
 				form.add("costDriverName", costDriverName);
 
 				ClientResponse result = webResource.post(ClientResponse.class, form);
 				cloud = result.getLocation();
 
 				Assert.assertEquals(201, result.getStatus());  
 			}
 
 		}
 	  
 	  static TypedParameter HPCloudDefaults[] = {
 		  	new TypedParameter("flavorRef", "Specifies the virtual machine size. Valid Values: 100 (standard.xsmall), 101 (standard.small), 102 (standard.medium), 103 (standard.large), 104 (standard.xlarge), 105 (standard.2xlarge)", ParameterType.String,"", "100"),
 		  	new TypedParameter("imageRef", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", "75845"),
 		  	new TypedParameter("locationId", "Unique ID of hpcloud zone. Valid Values: az-1.region-a.geo-1 | az-2.region-a.geo-1 | az-3.region-a.geo-1", ParameterType.String, null, "az-1.region-a.geo-1"),
 		  	new TypedParameter("security_groups", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", "n3phele-default"),
 		  	new TypedParameter("user_data", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "", "#!/bin/bash\necho n3phele agent injection... \nset -x\n apt-get update;  apt-get install -y openjdk-6-jre-headless \n wget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ubuntu ~/agent ~/sandbox https://region-a.geo-1.objects.hpcloudsvc.com:443/v1/AUTH_dc700102-734c-4a97-afc8-50530e87a171/n3phele-agent/n3phele-agent.tgz' ubuntu\n")	,	
 	};
 	  		
 		@Test
 		public void testInitHPCloudDefaults() throws Exception {
 			Cloud cloud = webResource.path("byName").queryParam("id","HPZone1").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 			for(TypedParameter t : HPCloudDefaults) {
 				Form form = new Form();
 			  	form.add("key", t.getName());
 			  	form.add("defaultValue", t.getDefaultValue());
 			  	form.add("type", t.getType().toString());
 			  		
 			  	ClientResponse result = webResource.uri(cloud.getUri()).path("inputParameter").post(ClientResponse.class, form);
 			  	Assert.assertEquals(200, result.getStatus());  
 			  	}
 			  		
 		}
 		
 		@Test
 		public void testAddCloudCostsHP(){
 			Cloud cloud = webResource.path("byName").queryParam("id","HPZone1").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	 		Map<String,Double> map = readFile("HP.txt");
 			
 			for (Entry<String,Double> entryPrice : map.entrySet())
 			{						
 				Form form = new Form();
 				form.add("key", entryPrice.getKey());
 				form.add("value",entryPrice.getValue().toString());
 				
 				ClientResponse result = webResource.uri(cloud.getUri()).path("costMap").post(ClientResponse.class, form);
 				Assert.assertEquals(200, result.getStatus());  
 			}
 		}		
 		
 	  			  
 	  
 	  //@Test
 	  public void testHPCloudDelete() throws Exception {
 
 		Cloud cloud = webResource.path("byName").queryParam("id","HPZone1").accept(MediaType.APPLICATION_JSON_TYPE).get(Cloud.class);
 	  	ClientResponse response = webResource.uri(cloud.getUri()).delete(ClientResponse.class);
 
 	  	Assert.assertEquals(204, response.getStatus()); 
 	  	 
 	  }
 
 }
