 package name.pehl.taoki.rest.paging;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.junit.Test;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.ServerResource;
 
 /**
  * @author $Author: harald.pehl $
  * @version $Date: 2010-09-09 17:57:49 +0200 (Do, 09. Sep 2010) $ $Revision: 135
  *          $
  */
 public class NumbersQueryResourceIntegrationTest extends NumbersIntegrationTest
 {
     @Override
     protected Class<? extends ServerResource> getResourceClass()
     {
         return NumbersQueryResource.class;
     }
 
 
     @Test
     public void testPaging() throws IOException, ResourceException, JSONException
     {
        ClientResource resource = new ClientResource(BASE_URL + "/numbers?offset=5&pageSsize=19");
         assertJson(resource);
     }
 }
