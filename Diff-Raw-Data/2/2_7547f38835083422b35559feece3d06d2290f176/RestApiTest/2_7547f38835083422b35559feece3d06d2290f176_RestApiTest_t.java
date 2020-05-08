 package uk.ac.jorum.integration;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.junit.matchers.JUnitMatchers.*;
 
 public class RestApiTest extends RestApiBaseTest {
 
   @Test
     public void emptyCommunitiesList() throws Exception {
       loadFixture("emptyDatabase");
       String result = makeRequest("/communities");
      assertThat(result, containsString("\"communities_collection\": [\n\n]}"));
     }
 }
