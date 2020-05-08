 package io.rampant.orchard.security;
 
 import be.objectify.deadbolt.core.models.Subject;
 import be.objectify.deadbolt.java.DeadboltHandler;
 import be.objectify.deadbolt.java.DynamicResourceHandler;
 import io.rampant.orchard.Global;
 import io.rampant.orchard.mongo.dao.UserDAO;
 import play.Configuration;
 import play.Play;
 import play.libs.F;
 import play.mvc.Http;
 import play.mvc.SimpleResult;
 
 import static play.mvc.Results.forbidden;
 
 /**
  * @author jonathan
  */
 public class OrchardDeadboltHandler implements DeadboltHandler {
 
 	@Override
 	public F.Promise<SimpleResult> beforeAuthCheck(Http.Context context) {
 		return null;
 	}
 
 	@Override
 	public Subject getSubject(Http.Context context) {
 		Configuration conf = Play.application().configuration();
 		/**
 		 * First, check to see if we have an "admin" session.
 		 */
 		Object isAdmin = context.session().get(conf.getString("auth.admin.sessionkey"));
 		if( null != isAdmin ) {


 		}
 
 		Http.Cookie c = context.request().cookie(conf.getString("auth.cookie.name"));
 		// No Cookie, no authenticated user.
 		if( null != c ) {
 			String appToken = c.value();
 
 			/**
 			 * This is a touch hacky. We can't use proper Dependency Injection here,
 			 * because this will get executed outside any context we have control
 			 * over. But we can get the global object we defined, which is hanging
 			 * on to the Guice injector. So we'll just grab our injector and get our
 			 * instance manually.
 			 */
			UserDAO dao = Global.getInjector().getInstance(UserDAO.class);
 			return dao.findByToken(appToken);
 		}
 		return null;
 	}
 
 	@Override
 	public F.Promise<SimpleResult> onAuthFailure(Http.Context ctx, String content) {
 		return F.Promise.promise(new F.Function0<SimpleResult>() {
 			@Override
 			public SimpleResult apply() throws Throwable {
 				return forbidden("You do not have access to this resource.");
 			}
 		});
 	}
 
 	@Override
 	public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
 		return null;
 	}
 }
