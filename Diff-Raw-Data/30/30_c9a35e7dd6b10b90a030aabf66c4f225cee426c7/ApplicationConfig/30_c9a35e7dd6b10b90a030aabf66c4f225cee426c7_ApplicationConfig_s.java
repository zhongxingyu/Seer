 package cz.muni.fi.pa165.rest;
 
import cz.muni.fi.pa165.library.dtos.CustomerTO;
import cz.muni.fi.pa165.library.dtos.ImpressionTO;
 import java.util.Set;
 import javax.ws.rs.core.Application;
 
@javax.ws.rs.ApplicationPath("rest")
 public class ApplicationConfig extends Application {
 
     @Override
     public Set<Class<?>> getClasses() {
         Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
         addRestResourceClasses(resources);
         return resources;
     }
 
     private void addRestResourceClasses(Set<Class<?>> resources) {
         resources.add(cz.muni.fi.pa165.rest.CustomerRest.class);
         resources.add(cz.muni.fi.pa165.rest.ImpressionRest.class);
     }
 }
