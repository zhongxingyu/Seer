 /*
  */
 package net.linkfluence.jspore;
 
 import com.google.common.collect.ImmutableMap;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Map;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
import net.linkfluence.jspore.middleware.format.Json;
 import org.codehaus.jackson.JsonNode;
 
 /**
  * Test the github spore spec.
  * 
  * @author Nicolas Yzet <nyzet@linkfluence.net>
  */
 public class GitHubTest extends TestCase {
  
     public GitHubTest(String testname) {
         super(testname);
     }
 
     public static Test suite() {
         return new TestSuite(GitHubTest.class);
     }    
     
     public void testLoadGitHubSpec() throws IOException, InvalidSporeSpecException{
         URL url = this.getClass().getResource("/github.json");
         File spec = new File(url.getFile());
         Spore<String> spore = new Spore.Builder<JsonNode>()
                 .addSpecContent(spec)
                 .build();
     }
     
     public void testCallGetInfo() throws FileNotFoundException, IOException, InvalidSporeSpecException, SporeException {
         URL url = this.getClass().getResource("/github.json");
         File spec = new File(url.getFile());
         
         Spore<JsonNode> spore = new Spore.Builder<JsonNode>()
                .addMiddleware(new Json())
                 .addSpecContent(spec)
                 .setDebug(true)
                 .build();
         
         Map<String, String> params = new ImmutableMap.Builder<String, String>()
                 .put("format", "json")
                 .put("username", "nicoo")
                 .build();
         SporeResult<JsonNode> result = spore.call("get_info", params);
         assertNotNull(result.response);
         assertNotNull(result.body);
         JsonNode user = result.body.get("user");
         JsonNode login = user.get("login");
         assertEquals("nicoo", login.getTextValue());
     }
     
    
 }
