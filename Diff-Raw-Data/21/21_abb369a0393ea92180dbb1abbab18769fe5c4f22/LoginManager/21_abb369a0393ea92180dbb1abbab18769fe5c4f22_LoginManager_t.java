 package domain;
 
 import java.awt.geom.CubicCurve2D;
 
 import play.mvc.Scope.Session;
 
 public class LoginManager {
 	
 	public static boolean isLoggedIn(SocialApplication app, Session currentSession, boolean forceLogon){
 		if( forceLogon ){
 			return checkAndForceLogin(app, currentSession);
 		} else {
 			return gentleCheckIfUserIsLogged(app, currentSession);
 		}
 	}
 	
 	private static boolean checkAndForceLogin(SocialApplication app, Session currentSession){
		return app.login(currentSession);
 	}
 	
 	private static boolean gentleCheckIfUserIsLogged(SocialApplication app, Session currentSession){
 		return app.loggedIn(currentSession);
 	}
 	
 	public static String userId(SocialApplication app, Session currentSession){
 		return currentSession.get( app.sessionIdKey() );
 	}
 	
 }
