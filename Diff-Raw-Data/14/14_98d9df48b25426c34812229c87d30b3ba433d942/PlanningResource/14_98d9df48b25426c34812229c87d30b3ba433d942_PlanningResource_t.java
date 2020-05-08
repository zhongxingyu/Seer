 package resources;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import planning.Planning;
 
 import javax.ws.rs.CookieParam;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import static javax.ws.rs.core.Response.Status.FORBIDDEN;
 
 @Path("/")
 @Singleton
 public class PlanningResource extends AbstractResource {
   private final Planning planning;
 
   @Inject
   public PlanningResource(Planning planning) {
     this.planning = planning;
   }
 
   @GET
   public Response redirectToIndex() {
     return seeOther("index.html");
   }
 
   @POST
   @Path("star")
   public void star(@CookieParam("userId") String userId, @FormParam("talkId") String talkId) {
     planning.star(assertAuthenticated(userId), talkId);
   }
 
   @POST
   @Path("unstar")
   public void unstar(@CookieParam("userId") String userId, @FormParam("talkId") String talkId) {
     planning.unstar(assertAuthenticated(userId), talkId);
   }
 
   @GET
   @Path("planning.json")
   @Produces("application/javascript;charset=UTF-8")
   public String planning(@QueryParam("callback") String callback) {
     return jsonp(read("planning.json"), callback); // TODO add small cache duration
   }
 
   @GET
   @Path("stars")
   @Produces("application/javascript;charset=UTF-8")
   public String stars(@CookieParam("userId") String userId, @QueryParam("callback") String callback) {
     return jsonp(planning.stars(assertAuthenticated(userId)), callback);
   }
 
   @GET
   @Path("starsPerTalk")
   @Produces("application/javascript;charset=UTF-8")
   public String starsPerTalk(@QueryParam("callback") String callback) {
     return jsonp(planning.countPerTalk(), callback);
   }
 
   @GET
   @Path("{path : .*\\.html}")
   @Produces("text/html;charset=UTF-8")
   public Response html(@PathParam("path") String path) {
    return ok(templatize(read(path)));
   }
 
   private static String assertAuthenticated(String userId) {
     if (userId == null) {
       throw new WebApplicationException(FORBIDDEN);
     }
     return userId;
   }
 }
