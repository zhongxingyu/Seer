 package hu.xea.nova.ws.rest.bamboo;
 
 import hu.xea.nova.ws.rest.bamboo.api.Projects;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class BambooTest {
 	
 	private BambooClient client;
 	
 	@Before
 	public void setup() {
 		client = new BambooClient();
 	}
 
 	@Test
 	public void testListProjects() {
 		client.connect(new MockServerFactory());
 		Projects projects = client.listProjects();
 	}
 
 }
