 package com.andresaraujo.resources;
 
 import com.yammer.dropwizard.jersey.caching.CacheControl;
 import com.yammer.metrics.annotation.Timed;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Dummy test
  * User: andres
  * Date: 8/22/13 - 5:48 PM
  */
 @Path("/dummy")
 @Produces("application/json; charset=UTF-8")
 @Consumes(MediaType.APPLICATION_JSON)
 public class DummyResource {
 
     @GET
     @Timed
     @CacheControl(noCache = true)
     public List<Map> findAll() {
         List<Map> result = new ArrayList<Map>();
 
        String []awesomeThings={"HTML5 Boilerplate", "AngularJs", "Karma", "Java", "Dropwizard", "Maven/Grunt/Bower"};

        Map<String,Object> dummy;

        int i=1;
        for(String s : awesomeThings){
            dummy = new HashMap();
            dummy.put("id", i++);
            dummy.put("name", s);
            result.add(dummy);
        }
 
         return result;
 
     }
 
 
 }
