 package hu.xea.nova.ws.rest.bamboo;
 
import static org.junit.Assert.*;
import hu.xea.nova.ws.rest.bamboo.api.BambooServer;
 import hu.xea.nova.ws.rest.bamboo.api.Projects;
 
 import org.junit.Before;
 import org.junit.Test;
import org.mockito.Mockito;
 
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
