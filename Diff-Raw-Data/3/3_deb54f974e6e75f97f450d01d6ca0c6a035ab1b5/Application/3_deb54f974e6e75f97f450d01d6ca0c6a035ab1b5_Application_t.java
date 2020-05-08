 package controllers;
 
 import javax.persistence.TypedQuery;
 
 import models.ImageEntity;
 import play.Logger;
 import play.db.jpa.JPA;
 import play.modules.facebook.FbGraph;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Http.Response;
 import play.mvc.Scope.Session;
 
 import com.restfb.FacebookClient;
 import com.restfb.exception.FacebookOAuthException;
 import com.restfb.types.User;
 
 import domain.JsonResponse;
 import domain.LikeRepository;
 import domain.LoginManager;
 import domain.SocialApplication;
 
 public class Application extends Controller {
 
 	static SocialApplication APP = SocialApplication.FACEBOOK;
 
 	public static void index()
 	{
 		try {
 			FacebookClient fbClient = FbGraph.getFacebookClient();
 			User profile = fbClient.fetchObject("me", com.restfb.types.User.class);
 			Logger.info("profile=%s", profile.getName());
 			String uid = profile.getId() + " ";
 			String name = profile.getFirstName();
 			Session.current().put("username", uid);
 			user(profile.getName(), profile.getId());
 
 		} catch (Exception ex) {
 			// not logged in, show button
 			login();
 		}
 	}
 
 	public static void user(String name, String id)
 	{
 		render(name, id);
 	}
 
 	public static void login()
 	{
 		if ( "check is login".equals("login") ) {
 
 		} else {
 			render();
 		}
 	}
 
 	public static void miniLogin(String siteUrl, String imageUrl)
 	{
		render(siteUrl,imageUrl);
 	}
 
 	public static void count(String siteUrl, String imageUrl)
 	{
 
 		boolean wasLiked = false;
 		if ( LoginManager.isLoggedInSoft(APP, Session.current()) ) {
 			String uToken = LoginManager.loggedUserToken(APP, Session.current());
 			ImageEntity ie = ImageEntity.find("siteUrl = ? and imageUrl = ? and userToken = ?", siteUrl, imageUrl, uToken)
 					.first();
 			wasLiked = ie != null;
 		}
 
 		long value = ImageEntity.count("siteUrl = ? and imageUrl = ?", siteUrl, imageUrl);
 
 		String result = JsonResponse.getCount(value, wasLiked);
 
 		renderJSON(result);
 	}
 
 	public static void like(String siteUrl, String imageUrl)
 	{
 
 		Session s = Session.current();
 
 		if ( LoginManager.isLoggedInMandatory(APP, s) ) {
 			
 			String uToken = LoginManager.loggedUserToken(APP, Session.current());
 			ImageEntity ie = ImageEntity.find("siteUrl = ? and imageUrl = ? and userToken = ?", siteUrl, imageUrl, uToken)
 					.first();
 			
 			if( ie == null){	// new like from this user 
 
 				ImageEntity ieNew = new ImageEntity();
 
 				ieNew.setSiteUrl(siteUrl);
 				ieNew.setImageUrl(imageUrl);
 				ieNew.setUserToken(LoginManager.loggedUserToken(APP, s));
 
 				ieNew.save();
 
 			} else {	// unlike 
 				ie.delete();
 			}
 
 			
 			long value = ImageEntity.count("siteUrl = ? and imageUrl = ?", siteUrl, imageUrl);
 
 			String countResult = JsonResponse.getCount(value, true);
 
 			renderJSON(countResult);
 				
 		} else {
 			Response.current().status = Http.StatusCode.FORBIDDEN;
 		}
 
 	}
 
 	public static void facebookLogout()
 	{
 		Session.current().remove("username");
 		FbGraph.destroySession();
 		index();
 	}
 
 	public static void users()
 	{
 		render();
 	}
 
 	public static void reset()
 	{
 		Logger.info("Like repository is being cleared");
 		LikeRepository.reset();
 	}
 }
