 package actions;
 
 import static org.apache.commons.lang3.StringUtils.isNotBlank;
 import play.mvc.Action;
 import play.mvc.Http.Context;
 import play.mvc.Result;
 
 public class Authentication extends Action.Simple {
 
     @Override
     public Result call( Context context ) throws Throwable {
        if ( isNotBlank( context.request().cookies().get( "oauthtoken" ).value() ) ) {
             return delegate.call( context );
         }
         return redirect( "/" );
     }
 
 }
