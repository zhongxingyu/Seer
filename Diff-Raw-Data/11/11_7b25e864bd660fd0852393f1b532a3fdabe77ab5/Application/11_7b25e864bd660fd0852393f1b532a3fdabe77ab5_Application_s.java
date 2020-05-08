 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	
     	List<Client> clients = Client.findAll();
        render(clients);
     }
     
     public static void stats(){
     	
     }
     public static void saveClient(String firtsname, String lastname){
     	
     	
     }
    public static void saveOrder(String firtsname, String lastname){
     	index();
     }
 
 }
