 package controllers;
 
 
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Router;
 import play.mvc.results.Redirect;
 import utils.BigBrotherHelper;
 
 public class FourSquareController extends Controller {
	private static String CLIENT_ID="3HEIFZIGIX0WUJCJWPDZP1QPGQUIVVIOLNZ4ASRBYCUO3XN4";
 
 
 	public static void callBack4SQAuth(){
 		String code = params.get("code");
 		String token = BigBrotherHelper.retrieveToken(code);
 		System.out.println(token);
 		Application.index();
 		//4square login
 	}
 	
 	public static void austh4SQ(){
 		String redirect = Router.getFullUrl("Application.callBack4SQAuth");
		String url = "https://foursquare.com/oauth2/authenticate?client_id="+CLIENT_ID+
											"&response_type=code&redirect_uri="+redirect;	
		Logger.info("url " + url);
 		throw new Redirect(url);
 	}
 }
