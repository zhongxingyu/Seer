 package controllers;
 
 import play.mvc.Controller;
 
 /**
  *
  * @author pierregaste
  */
 public class Carte extends Controller {
     public static void index(){
        redirect("carte/show");
     }
     
     public static void show(){
         render();
     }
 }
