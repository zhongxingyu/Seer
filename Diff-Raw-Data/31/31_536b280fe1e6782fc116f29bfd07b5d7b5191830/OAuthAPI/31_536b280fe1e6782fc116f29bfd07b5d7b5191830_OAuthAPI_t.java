 package org.internetrt.driver;
 
 
 import org.internetrt.*;
 import org.internetrt.core.security.*;
 import org.internetrt.core.signalsystem.SignalResponse;
 
 import play.mvc.Http.Request;
 import play.mvc.Result;
 import play.mvc.Controller;
 
 public class OAuthAPI extends Controller {
 	
 	/**
	 * The API for Web Server Flow is moved to package userinterface
 	 * @param appID
 	 * @param response_type
 	 * @param redirect_uri
 	 * @return
 	 */
//	public static Result authorize(){
//		String userID = session(CONSTS.SESSIONUID());
//		String appID = request().queryString().get("appID")[0];
//		String redirect_uri = request().queryString().get("redirect_uri")[0];
//		System.out.print(redirect_uri);
//		String code= org.internetrt.SiteInternetRuntime.getAuthcodeForServerFlow(appID,userID,redirect_uri);
//		System.out.print(code);
//		return redirect(redirect_uri+"?code="+code);
//	}
 	/**
 	 * The API for global workflow type auth
 	 * @param appSecret
 	 * @param workflowID
 	 * @return
 	 */
 	public static Result authorizebyworkflow(){
 		String appID = request().queryString().get("appID")[0];
 		String appSecret = request().queryString().get("appSecret")[0];
 		String workflowID = request().queryString().get("workflowID")[0];
 		String code = SiteInternetRuntime.getAuthcodeForActionFlow(appID, appSecret,workflowID);
 		
 		return ok("{code:"+code+"}");
 	}
 	
 	public static Result token(){
 		String authtoken = request().queryString().get("authtoken")[0];
 		String appID = request().queryString().get("appID")[0];
 		String appSecret = request().queryString().get("appSecret")[0];
 		AccessToken accesstoken = SiteInternetRuntime.getAccessTokenByAuthtoken(appID, authtoken, appSecret);
 		return ok("{access_token:\""+accesstoken.value()+
 				"\",expires_in: \""+accesstoken.expire()+"\""+
 				",refresh_token=\""+accesstoken.refresh()+"\"}");
 	}
 	
 	
 }
