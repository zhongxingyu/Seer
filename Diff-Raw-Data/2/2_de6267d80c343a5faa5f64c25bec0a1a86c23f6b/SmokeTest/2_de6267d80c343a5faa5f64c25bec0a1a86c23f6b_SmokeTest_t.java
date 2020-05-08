 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class SmokeTest {
 
     private static DefaultHttpClient client;
 
     @BeforeClass
     public static void setupClass() {
         client = new DefaultHttpClient();
     }
 
     @Test
     public void should_display_front_page() throws IOException {
         HttpResponse response = client.execute(new HttpGet("http://localhost:9080/smidig2011/index.jsp"));
 
         String content = EntityUtils.toString(response.getEntity());
 
        assertThat(content, containsString("shoop shoop"));
     }
 }
