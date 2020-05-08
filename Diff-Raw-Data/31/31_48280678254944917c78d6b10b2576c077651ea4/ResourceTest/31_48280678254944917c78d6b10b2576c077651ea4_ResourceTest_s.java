 package rip;
 
 import com.google.mockwebserver.MockResponse;
 import com.google.mockwebserver.MockWebServer;
 import com.google.mockwebserver.RecordedRequest;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotEquals;
 
 public class ResourceTest {
 
     MockWebServer server;
     Resource resource;
 
     @Before
     public void setUp() throws IOException {
         server = new MockWebServer();
         server.play();
         String serverUrl = server.getUrl("/").toExternalForm();
         resource = new RestClient().open(serverUrl);
     }
 
     @After
     public void tearDown() throws IOException {
         server.shutdown();
     }
 
     @Test
     public void getsResource() throws InterruptedException, IOException {
         server.enqueue(new MockResponse().setBody("foo"));
 
         assertEquals("foo", resource.get());
         RecordedRequest request = server.takeRequest();
         assertEquals("/", request.getPath());
         assertEquals("application/json", request.getHeader("Accept"));
 
     }
 
     @Test
     public void putsResource() throws InterruptedException {
         server.enqueue(new MockResponse());
 
         resource.put("foo");
 
         RecordedRequest request = server.takeRequest();
         assertEquals("PUT", request.getMethod());
         assertEquals("foo", new String(request.getBody()));
         assertEquals("application/json", request.getHeader("Content-Type"));
     }
 
     @Test
     public void postsResource() throws InterruptedException {
         server.enqueue(new MockResponse().setBody("foo"));
 
         assertEquals("foo", resource.post("bar"));
 
         RecordedRequest request = server.takeRequest();
         assertEquals("POST", request.getMethod());
         assertEquals("bar", new String(request.getBody()));
         assertEquals("application/json", request.getHeader("Content-Type"));
         assertEquals("application/json", request.getHeader("Accept"));
     }
 
     @Test
     public void deletesResource() throws InterruptedException {
         server.enqueue(new MockResponse());
 
         resource.delete();
 
         RecordedRequest request = server.takeRequest();
         assertEquals("DELETE", request.getMethod());
     }
 
     @Test
     public void handlesRelativePaths() throws IOException, InterruptedException {
         server.enqueue(new MockResponse());
         server.enqueue(new MockResponse());
 
         resource.path("bar").get();
        resource.path("foo").put("");
 
         assertEquals("/bar", server.takeRequest().getPath());
        assertEquals("/foo", server.takeRequest().getPath());
     }
 
     @Test
     public void equalsWorksAsExpected() {
         Resource resource = new RestClient().open("http://foo.com/bar");
         assertEquals(resource, new RestClient().open("http://foo.com/bar"));
         assertEquals(resource, new RestClient().open("http://foo.com").path("bar"));
         assertNotEquals(resource, new RestClient().open("http://bar.com"));
     }
 
     @Test
     public void hashCodeIsCompatibleWithEquals() {
         int hash1 = new RestClient().open("http://foo.com/bar").hashCode();
         int hash2 = new RestClient().open("http://foo.com").path("bar").hashCode();
         assertEquals(hash1, hash2);
     }
 }
