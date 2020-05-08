 package earth.xor.rest.routes;
 
 import org.xorrr.util.EnvironmentVars;
import org.xorrr.util.HttpResponseErrorMessages;
 
 import spark.Request;
 import spark.Response;
 import spark.Route;
 import earth.xor.db.DatastoreFacade;
 import earth.xor.model.LinkFields;
 import earth.xor.rest.transformation.JSONTransformator;
 
 public class PostLinkRoute extends Route{
 
     private DatastoreFacade facade;
     private JSONTransformator transformator;
 
     public PostLinkRoute(DatastoreFacade facade, JSONTransformator transformator) {
         super(LinkFields.LINKS_NAME);
         this.facade = facade;
         this.transformator = transformator;
     }
 
     @Override
     public Object handle(Request request, Response response) {
         checkPassword(request);
         facade.addLink(transformator.jsonToLink(request.body()));
 
         return request.body();
     }
 
     private void checkPassword(Request request) {
         String pw = request.queryParams("pw");
         if (pw == null)
            halt(400, HttpResponseErrorMessages.MISSING_PW);
         else if (passwardIncorrect(pw))
            halt(401, HttpResponseErrorMessages.AUTH_FAIL);
     }
 
     private boolean passwardIncorrect(String pw) {
         return !pw.equals(System.getenv(EnvironmentVars.PW));
     }
 }
