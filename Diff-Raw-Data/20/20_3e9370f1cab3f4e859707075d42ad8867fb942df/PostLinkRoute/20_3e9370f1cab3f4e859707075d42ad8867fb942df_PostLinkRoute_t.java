 package earth.xor.rest.routes;
 
 import org.xorrr.util.EnvironmentVars;
 
 import earth.xor.db.DatastoreFacade;
import earth.xor.db.LinkFields;
 import earth.xor.rest.transformation.Transformator;
 import spark.Request;
 import spark.Response;
 import spark.Route;
 
 public class PostLinkRoute extends Route{
 
     private DatastoreFacade facade;
     private Transformator transformator;
 
    public PostLinkRoute(DatastoreFacade facade, Transformator transformator) {
        super(LinkFields.LINKS_NAME);
         this.facade = facade;
         this.transformator = transformator;
     }
 
     @Override
     public Object handle(Request request, Response response) {
         String  pw = request.queryParams("pw");
         if (!pw.equals(System.getenv(EnvironmentVars.PW)))
             halt(401, "Authentication failed");
         facade.addLink(transformator.toLink(request.body()));
 
         return request.body();
     }
 }
