 package controllers;
 
 import play.*;
import play.data.validation.Valid;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Application extends Controller {
 
     public static void index() {
     	
     	List<Client> clients = Client.findAll();
    	Order order = new Order(null,null);
        render(clients, order);
     }
     
     public static void stats(){
     	
     }
     public static void saveClient(String firtsname, String lastname){
     	
     	
     }
    public static void saveOrder(String clientId, Date orderDate, Order order){
    	 	
    	Client client = Client.findById(clientId);
    	
     	index();
     }
 
 }
