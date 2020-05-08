 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa165.jtravelagency.rest;
 
 import java.util.Set;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.Application;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
 
 @javax.ws.rs.ApplicationPath("webresources")
 public class ApplicationConfig extends Application {
 
       private static final String API_URL = "http://localhost:8081/client";
    private WebTarget webTarget;
    
    public ApplicationConfig(){
        Client client = ClientBuilder.newClient();
        client.register(new HttpBasicAuthFilter("rest", "rest"));
        webTarget = client.target(API_URL);
    }
    
     @Override
     public Set<Class<?>> getClasses() {
         Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
         addRestResourceClasses(resources);
         return resources;
     }
 
     private void addRestResourceClasses(Set<Class<?>> resources) {
         resources.add(cz.muni.fi.pa165.jtravelagency.rest.CustomerFacade.class);
         resources.add(cz.muni.fi.pa165.jtravelagency.rest.TripFacade.class);
     }
 }
