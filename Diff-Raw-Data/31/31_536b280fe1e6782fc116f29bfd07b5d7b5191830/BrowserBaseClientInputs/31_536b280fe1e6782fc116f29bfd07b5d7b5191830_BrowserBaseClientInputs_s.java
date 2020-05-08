 package org.internetrt.driver.userinterface;
 
 import org.internetrt.CONSTS;
 import org.internetrt.SiteUserInterface;
 
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class BrowserBaseClientInputs extends Controller{
 
 	
 	public static Result register(){
 		String username = request().queryString().get("username")[0];
 		String password = request().queryString().get("password")[0];
 		return ok(SiteUserInterface.register(username,password));
 	}
 	public static Result login(){
 		String username = request().queryString().get("username")[0];
 		String password = request().queryString().get("password")[0];
 		
 		try{
			System.out.println("111111111111111111111111111111111");
 			String uid = SiteUserInterface.login(username,password);
			System.out.println("2222222222222222222222222222222222");
 			session().put(CONSTS.SESSIONUID(), uid);
			System.out.println("33333333333333333333333333333333333");
 			return ok(uid);
 		}catch(Exception e){
 			return ok(e.getMessage());
 		}	
 	}
 	
 	public static Result installRootApp(){
 		String uid = session().get(CONSTS.SESSIONUID());
 		String xml = request().queryString().get("xml")[0];
 		
 		Boolean success =  SiteUserInterface.installRootApp(uid,xml);
 		
 		return ok(success.toString());
 	}
 	
 }
