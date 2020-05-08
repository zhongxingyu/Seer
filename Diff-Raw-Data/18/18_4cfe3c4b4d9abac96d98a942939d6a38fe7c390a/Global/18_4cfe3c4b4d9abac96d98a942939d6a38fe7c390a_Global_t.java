import info.seltenheim.play2.usertracking.actions.TrackAsAction;
 
 import java.lang.reflect.Method;
 
 import play.GlobalSettings;
 import play.mvc.Action;
 import play.mvc.Http.Context;
 import play.mvc.Http.Request;
import play.mvc.Result;
 
 public class Global extends GlobalSettings {
 
     @SuppressWarnings("rawtypes")
     @Override
     public Action onRequest(Request request, Method method) {
         final String action = method.getName();
         final String controller = method.getDeclaringClass().getSimpleName();
 
         return new Action.Simple() {
 
             @Override
             public Result call(Context ctx) throws Throwable {
                 final Result result = delegate.call(ctx);
                TrackAsAction.call(ctx, controller, action);
                 return result;
             }
         };
     }
 }
