 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
		System.out.println(request.params.all().toString() );
 		System.out.println(request.params.toString());
         render();
     }
 
 }
