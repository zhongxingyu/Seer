 package fmin362.resources;
 
 import fmin362.model.Category;
 import fmin362.values.Menu;
 import java.util.List;
 import javax.naming.InitialContext;
 import javax.persistence.EntityManager;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 @Path( "/menu" )
 public class MenuResource
 {
 
     @GET
    @Produces( MediaType.APPLICATION_JSON )
     public Menu menu()
         throws Exception
     {
         EntityManager em = (EntityManager) new InitialContext().lookup( "java:comp/env/persistence/EntityManager" );
         List<Category> categories = em.createQuery( "select c from Category c" ).getResultList();
         Menu menu = new Menu( categories );
         return menu;
     }
 
 }
