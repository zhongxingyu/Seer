 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.data.validation.*;
 
 import java.util.*;
 
 import models.*;
 import models.user.*;
 
 //@With(Secure.class)
 public class Account extends Controller {
 	public static void create() {
 		render();
 	}
 
	public static void createAccount(@Required @Email String email, 
 									 @Required String password) {
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			create();
 		}
 
 		if (UserLogin.exists(email)) {
 			System.out.println(email + " exists!");
 		} else {
 			System.out.println(email + " does not exist!");
 		}
 	}
 }
