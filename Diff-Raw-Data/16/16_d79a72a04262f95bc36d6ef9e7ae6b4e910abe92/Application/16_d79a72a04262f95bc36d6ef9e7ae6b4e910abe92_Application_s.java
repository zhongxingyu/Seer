 package controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import models.ImageEntity;
 import models.Pair;
 import models.UserEntity;
 import play.Logger;
 import play.Play;
 import play.modules.facebook.FbGraph;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.mvc.Http.Response;
 import play.mvc.Scope.Session;
 
 import com.restfb.BinaryAttachment;
 import com.restfb.FacebookClient;
 import com.restfb.Parameter;
 import com.restfb.types.FacebookType;
 
 import domain.JsonResponse;
 import domain.LoginManager;
 import domain.SocialApplication;
 
 public class Application extends Controller {
 
 	static SocialApplication APP = SocialApplication.FACEBOOK;
 
     public static void index()
     {
         if( LoginManager.isLoggedIn(APP, session.current(), false) ){
             String uid = LoginManager.userId(APP, session.current() );
             UserEntity user = UserEntity.findById(uid);
             if(user==null){
                 Session.current().remove( APP.sessionIdKey());
             }
            Logger.info("Redirect to user page");
            user ( uid );
         }
 
             Logger.info("render index with welcome page");
             render();
 
     }
 
 	public static void user(String uid)
 	{
 		UserEntity user = UserEntity.findById(uid);
 		if ( user != null ) {
 			List<ImageEntity> likeSet = ImageEntity.find("userToken = ?", uid).fetch();
 			Collections.sort(likeSet);
 
 			int size = 10;
 			if ( size > likeSet.size() ) {
 				size = likeSet.size();
 			}
 			List<ImageEntity> cutList = likeSet.subList(0, size);
 
 			List<Pair> imageWithCounterSet = new ArrayList<Pair>(cutList.size());
 
 			for ( ImageEntity ie : cutList ) {
 				imageWithCounterSet.add(new Pair(ie, getLikeCount(ie)));
 			}
 
 			render(user, imageWithCounterSet);
 
 		} else {
 			index();
 		}
 	}
 
 	private static long getLikeCount(ImageEntity ie)
 	{
 		return ImageEntity.count("siteUrl = ? and imageUrl = ?", ie.getSiteUrl(), ie.getImageUrl());
 	}
 
 	public static void login()
 	{
 		render();
 	}
 
     public static void successful(){
         if( LoginManager.isLoggedIn(APP, session.current(), true) ){
             String uid=LoginManager.userId(APP,session.current());
             user(uid);
         }
         index();
     }
 
 
 	public static void miniLogin(String siteUrl, String imageUrl)
 	{
 		render(siteUrl,imageUrl);
 	}
 
 	public static void count(String siteUrl, String imageUrl)
 	{
 		boolean wasLiked = false;
 		if ( LoginManager.isLoggedIn(APP, Session.current(), true) ) {
 			String uToken = LoginManager.userId(APP, Session.current());
 			ImageEntity ie = ImageEntity.find("siteUrl = ? and imageUrl = ? and userToken = ?", siteUrl, imageUrl, uToken)
 					.first();
 			wasLiked = ie != null;
 		}
 
 		long value = ImageEntity.count("siteUrl = ? and imageUrl = ?", siteUrl, imageUrl);
 
 		String result = JsonResponse.getCount(value, wasLiked);
 
 		renderJSON(result);
 	}
 
 	static boolean hasComment()
 	{
 		String comment = request.params.get("comment");
 		return comment != null && comment.length() > 0;
 	}
 
 	static String getComment()
 	{
 		String comment = request.params.get("comment");
 		if ( comment == null || comment.length() == 0 )
 			return "";
 		return comment;
 	}
 
 	public static void like(String siteUrl, String imageUrl)
 	{
 		boolean wasLiked = false;
 		
 		if ( LoginManager.isLoggedIn(APP, Session.current(), false) ) {
 			
 			if ( hasComment() ) {
 				String comment = getComment();
 				String photoId = session.get( asSesstionKey(imageUrl) );
 
 				FacebookClient fbClient = FbGraph.getFacebookClient();
 				fbClient.publish(photoId + "/comments", FacebookType.class, Parameter.with("message", comment));
 
 				wasLiked = true;
 				
 			} else {
 				String uToken = LoginManager.userId(APP, Session.current());
 				ImageEntity ie = ImageEntity.find("siteUrl = ? and imageUrl = ? and userToken = ?", siteUrl, imageUrl, uToken)
 						.first();
 
 				if ( ie == null ) { // new like from this user
 
 					ImageEntity ieNew = ImageEntity.valueOf(siteUrl, imageUrl, LoginManager.userId(APP, Session.current()));
 
 					String photoId = postImageToApplication(APP, ieNew);
 					if ( photoId != null ){
 						
 						ieNew.save();	// store entity as far as an image is posted on APP
 						session.put( asSesstionKey(imageUrl), photoId);
 						wasLiked = true;
 						
					} 
 
 				} else { // unlike
 					ie.delete();
 					wasLiked = false;
 				}
 				
 			}
 			
 		} else {
 			Response.current().status = Http.StatusCode.FORBIDDEN;
 		}
 		
 		long value = ImageEntity.count("siteUrl = ? and imageUrl = ?", siteUrl, imageUrl);
 		String countResult = JsonResponse.getCount(value, wasLiked);
 
 		renderJSON(countResult);
 
 	}
 
 	private static String asSesstionKey(String imageUrl)
 	{
 		return "url_" + imageUrl.hashCode();
 	}
 
 	private static String postImageToApplication(SocialApplication app, ImageEntity ieNew)
 	{
 
		FacebookClient fbClient = FbGraph.getFacebookClient();

 		String url = ieNew.getImageUrl();
 		if ( url.startsWith("//") ) {
 			url = "http:" + url;
 		}
 		String domain = null;
 		try {
 			URL siteUrl = new URL(ieNew.getSiteUrl());
 			domain = siteUrl.getHost();
 		} catch (MalformedURLException e) {
 			Logger.error(e, "Cannot parse url");
 		}
 		
 		try{
 			FacebookType publishPhotoResponse = fbClient.publish("me/feed", FacebookType.class,
 				Parameter.with("message", "Shared photo from " + ((domain == null) ? ieNew.getSiteUrl() : domain)),
 				Parameter.with("picture", url), Parameter.with("link", ieNew.getSiteUrl()));
 			return publishPhotoResponse.getId();
 		} catch(Exception e){
 			Logger.error(e, "Cannot post image in APP");
 		}
 		return null;
 	}
 
 	public static void facebookLogout()
 	{
 		Session.current().remove(APP.sessionIdKey());
 		FbGraph.destroySession();
 		index();
 	}
 
     public static void logout(){
         Session.current().remove(APP.sessionIdKey());
         FbGraph.destroySession();
         render();
     }
 
 	public static void users()
 	{
 		render();
 	}
 
     public static void script(){
         response.setContentTypeIfNotSet("application/x-javascript");
 
         String baseUrl = request.getBase();
         render(baseUrl);
     }
 
     public static void download(){
         render();
     }
 
 }
