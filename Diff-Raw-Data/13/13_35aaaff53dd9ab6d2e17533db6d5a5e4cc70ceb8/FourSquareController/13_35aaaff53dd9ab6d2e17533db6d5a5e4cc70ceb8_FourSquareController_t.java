 package controllers;
 
 
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Router;
 import play.mvc.results.Redirect;
 import utils.BigBrotherHelper;
 
 public class FourSquareController extends Controller {
 
 
 	public static void callBack4SQAuth(){
 		String code = params.get("code");
 		String token = BigBrotherHelper.retrieveToken(code);
 		System.out.println(token);
 		Application.index();
 		//4square login
 	}
 	
 	public static void austh4SQ(){
 		String redirect = Router.getFullUrl("Application.callBack4SQAuth");
		String url = "https://foursquare.com/oauth2/authenticate?client_id=" + BigBrotherHelper.CLIENT_ID +
											"&response_type=code&redirect_uri="+redirect;
 		throw new Redirect(url);
 	}
 }
