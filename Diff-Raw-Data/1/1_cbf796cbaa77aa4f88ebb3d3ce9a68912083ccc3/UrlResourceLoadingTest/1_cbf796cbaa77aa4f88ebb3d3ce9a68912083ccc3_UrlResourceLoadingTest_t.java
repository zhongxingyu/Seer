 package org.jboss.test.faces.staging.suite;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.net.URL;
 
 import org.jboss.test.faces.ApplicationServer;
 import org.junit.Test;
 
 public class UrlResourceLoadingTest {
 
 	@Test
 	public void testAddFromJar() throws Exception {
 		URL resource = this.getClass().getClassLoader().getResource("java/util/Set.class");
 		assertNotNull(resource);
 		ApplicationServer server = ApplicationServer.createApplicationServer();
 		server.addResourcesFromDirectory("/", resource);
 		try {
 	        server.init();
 	        assertNotNull(server.getContext().getResource("/Map.class"));
 	        assertNotNull(server.getContext().getResource("/concurrent/atomic/AtomicBoolean.class"));
 		} finally {
 	        server.destroy();
 		}
 	}
 
 	@Test
 	public void testAddFromFile() throws Exception {
 		URL resource = this.getClass().getClassLoader().getResource("resource.txt");
 		assertNotNull(resource);
 		ApplicationServer server = ApplicationServer.createApplicationServer();
 		server.addResourcesFromDirectory("/", resource);
         try {
             server.init();
             assertNotNull(server.getContext().getResource("/foo.txt"));
             assertNotNull(server.getContext().getResource("/baz/bar.txt"));
         } finally {
             server.destroy();
         }
 	}
 
 	
 	@Test
 	public void testGetDirectory()  throws Exception {
 		URL resource = this.getClass().getClassLoader().getResource("java/util/Set.class");
 		assertNotNull(resource);
 		ApplicationServer server = ApplicationServer.createApplicationServer();
 		server.addResourcesFromDirectory("/WEB-INF/classes/java/util/", resource);
 		try {
     		server.init();
     		assertNotNull(server.getContext().getResource("/WEB-INF/classes/java/util/Map.class"));
 		} finally {
 			server.destroy();
 		}
 		
 	}
 
 }
