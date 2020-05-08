 package experiments.resources;
 
 import java.util.Collection;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;
 
 import experiments.MockupData;
 import experiments.exceptions.NotSupportedException;
 import experiments.resources.dtos.SubTest;
 
 /**
  * @author zlatka
  * 
  */
@Path("/test/{id}/subtest")
 public class SubtestResource extends ComplexRestResource<SubTest> {
 
     @Override
     public Collection<SubTest> getResources(Long baseResourceId) throws NotSupportedException {
         return new MockupData().findSubTestsForTest(baseResourceId);
     }
 
     @Override
     public SubTest getResource(Long baseResourceId, Long resourceId) throws NotSupportedException {
         return new MockupData().findSubTest(resourceId, baseResourceId);
     }
 
     @GET
     @Path("hey")
     public Response justSayingHey() {
         return Response.ok("Hey hey!").build();
     }
 
 }
