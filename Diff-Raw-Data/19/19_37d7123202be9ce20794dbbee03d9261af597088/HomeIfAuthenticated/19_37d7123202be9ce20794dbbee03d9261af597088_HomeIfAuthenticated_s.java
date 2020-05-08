 package actions;
 
import org.apache.commons.lang3.StringUtils;

 import play.mvc.Action;
 import play.mvc.Http.Context;
 import play.mvc.Result;
 
 public class HomeIfAuthenticated extends Action.Simple {
 
     @Override
     public Result call( Context context ) throws Throwable {
        if (StringUtils.isBlank( context.request().cookies().get( "oauthtoken" ).value() ) ) {
             return delegate.call( context );
         }
         return redirect( "/home" );
     }
 
 }
