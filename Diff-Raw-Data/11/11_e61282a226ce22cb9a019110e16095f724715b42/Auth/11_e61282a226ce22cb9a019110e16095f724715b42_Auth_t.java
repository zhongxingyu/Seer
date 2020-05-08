 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Auth extends Controller {
     
    /**
     * If a user is already logged in, redirect to /
     */
    @Before
    static void checkAuthentification() {
      if(session.get("username") != null) redirect("/");
    }
    
    /**
     * Login
     */
     public static void index() {
         render();
     }
 
 }
