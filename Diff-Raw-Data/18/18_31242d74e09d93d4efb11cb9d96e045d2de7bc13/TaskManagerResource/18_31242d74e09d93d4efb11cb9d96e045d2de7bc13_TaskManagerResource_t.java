 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.awesomedat076.task_manager_backend;
 
import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 /**
  *
  * @author Adam
  */
 
 @Path("items")
 public class TaskManagerResource {
    
     @GET
    @Path("/items")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<ItemProxy> getListItems(@QueryParam("listId") int listId) {
         //****************************************************************REMOVE IN FINAL ************************DEBUGG! 
         listId= 1337; 
         Logger.getAnonymousLogger().log(Level.INFO, "getListItems for list{0}", listId);
         
         AppController ap = new AppController();
        LinkedList<Item> items;
        List<ItemProxy> proxyList = new LinkedList();
         
        items = (LinkedList<Item>) ap.getItemList(listId);
        for(Item i : items) {
            proxyList.add(new ItemProxy(i));
        }
        return proxyList;
     }
     
     
 }
