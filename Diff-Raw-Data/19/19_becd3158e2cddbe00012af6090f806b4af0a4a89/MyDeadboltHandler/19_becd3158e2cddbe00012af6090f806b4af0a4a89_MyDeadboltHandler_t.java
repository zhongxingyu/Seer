 package security;
 
import models.SessionToken;
 import models.User;
import play.mvc.Action;
 import play.mvc.Http;
 import play.mvc.Result;
 import be.objectify.deadbolt.java.AbstractDeadboltHandler;
 import be.objectify.deadbolt.java.DynamicResourceHandler;
 import be.objectify.deadbolt.core.models.Subject;
 
 import com.feth.play.module.pa.PlayAuthenticate;
 import com.feth.play.module.pa.user.AuthUserIdentity;
 
 public class MyDeadboltHandler extends AbstractDeadboltHandler {
 
 	@Override
 	public Result beforeAuthCheck(final Http.Context context) {

        // Check token stuff ....
        if (context.request().cookie("PLAY_SESSION") == null &&
                    context.request().getHeader("token") != null) {
            SessionToken st = SessionToken.findByToken(context.request().getHeader("token"));
            if (st != null && !st.expired()) {
                context.session().put(PlayAuthenticate.ORIGINAL_URL, context.request().uri());
                context.session().put(PlayAuthenticate.USER_KEY, st.getUserId());
                context.session().put(PlayAuthenticate.PROVIDER_KEY, st.getProviderId());
                context.session().put(PlayAuthenticate.EXPIRES_KEY, Long.toString(st.getExpires().getTime()));
                context.session().put(PlayAuthenticate.SESSION_ID_KEY, st.getToken());
            }
        }


 		if (PlayAuthenticate.isLoggedIn(context.session())) {
 			// user is logged in
 			return null;
 		} else {
 			// user is not logged in
 
 			// call this if you want to redirect your visitor to the page that
 			// was requested before sending him to the login page
 			// if you don't call this, the user will get redirected to the page
 			// defined by your resolver
              /*
 			final String originalUrl = PlayAuthenticate
 					.storeOriginalUrl(context);
 
 			context.flash().put("error",
 					"You need to log in first, to view '" + originalUrl + "'");
 			return redirect(PlayAuthenticate.getResolver().login());
 
             return forbidden("You need to log in first, to view '" + originalUrl + "'");
             */
             return forbidden("You need to log in first, to view this page");
         }
 	}
 
 	@Override
 	public Subject getSubject(final Http.Context context) {
 		final AuthUserIdentity u = PlayAuthenticate.getUser(context);
 		// Caching might be a good idea here
 		return User.findByAuthUserIdentity(u);
 	}
 
 	@Override
 	public DynamicResourceHandler getDynamicResourceHandler(
 			final Http.Context context) {
 		return null;
 	}
 
 	@Override
 	public Result onAuthFailure(final Http.Context context,
 			final String content) {
 		// if the user has a cookie with a valid user and the local user has
 		// been deactivated/deleted in between, it is possible that this gets
 		// shown. You might want to consider to sign the user out in this case.
 		return forbidden("Forbidden");
 	}
 }
