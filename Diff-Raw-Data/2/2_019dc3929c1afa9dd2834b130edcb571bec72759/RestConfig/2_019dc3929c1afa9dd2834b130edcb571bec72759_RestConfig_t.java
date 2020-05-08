 package fi.muni.pa165.rest;
 
 import java.util.HashSet;
 import java.util.Set;
 import javax.ws.rs.core.Application;
 
 /**
  *
  * @author Oliver Pentek
  */
 @javax.ws.rs.ApplicationPath("webresources")
 public final class RestConfig extends Application {
     @Override
     public Set<Class<?>> getClasses() {
         final Set<Class<?>> resources = new HashSet<>();
         addRestResourceClasses(resources);
         return resources;
     }
 
     private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(fi.muni.pa165.rest.CustomersResource.class);
         resources.add(fi.muni.pa165.rest.DogsResource.class);
     }
 }
