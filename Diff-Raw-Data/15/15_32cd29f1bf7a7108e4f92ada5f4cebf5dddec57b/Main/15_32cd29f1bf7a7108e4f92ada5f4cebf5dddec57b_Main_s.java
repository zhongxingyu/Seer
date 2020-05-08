 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.standard.shor.curlies;
 
 import com.standard.shor.curlies.domainmodel.Person;
 import java.net.URI;
 import java.net.URISyntaxException;
 import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
 
 import javax.ws.rs.ApplicationPath;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.core.Application;
 import javax.ws.rs.core.Response;
 
 /**
  *
  * @author sbowen
  */
 @ApplicationPath("/foo/")
 @Path("/")
 @Stateless
 public class Main extends Application {
   
    @PersistenceContext(unitName = "primary", type = PersistenceContextType.TRANSACTION)
    private EntityManager em;
 
     
     @GET
     @Path("/bar/{name}")
     public String someMethod(@PathParam("name") String name) {
         return "Hello " + name;
     }
     
     @POST
     @Path("/lol")
     public String someThing(@FormParam("url") String message) {
         Integer count = message.length();
         return "Message length: " + count.toString();
     }
     
     @GET
     @Path("/redirect")
     public Response redirect() throws URISyntaxException {
     	return Response.temporaryRedirect(new URI("http://www.google.co.uk")).build();
     }
     
     @GET
     @Path("/user/add/{userId}/{name}")
     public Response addUser(@PathParam("userId") long userId, @PathParam("name") String name) {
       Person p = new Person();
       p.setAddress("My House");
       p.setId(userId);
       p.setGender("M");
       
      p = em.merge(p);
       return Response.ok(p).build();
     }
 }
