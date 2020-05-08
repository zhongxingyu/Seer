 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	System.out.println(String.format("Got a request for index.html. System Property PLAY_TEST=%s",Play.configuration.get("PLAY_TEST")));
    	render();
     }
 
 }
