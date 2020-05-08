 package controllers;
 
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class DemotivatorsController extends Controller{
 
 	@Before
	static void interception(){
 		String domain = request.domain;
 		if (domain.startsWith("www.")){
 			request.domain = domain.substring(4);
			redirect(request.url);
 		}
 	}
 }
