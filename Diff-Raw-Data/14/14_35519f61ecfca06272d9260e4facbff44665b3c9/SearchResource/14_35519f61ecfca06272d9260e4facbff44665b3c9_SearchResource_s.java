 package se.umu.cs.umume.rest.resources;
 
 import java.util.List;
 
 import javax.naming.NamingException;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import se.umu.cs.umume.PersonBean;
 import se.umu.cs.umume.util.LDAPUtils;
 
 @Path("/search/{searchString}")
 public class SearchResource {
     @Context UriInfo uriInfo;
     @PathParam("searchString") String searchString;
 
     // The Java method will process HTTP GET requests
     @GET
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public List<PersonBean> getUserXML() {
         try {
            System.err.println("String: " + searchString);
             if (searchString.length() < 3) {
                 throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             List<PersonBean> result = LDAPUtils.toPersonBeans(LDAPUtils.searchPerson(searchString));
             return result;
         } catch (NamingException e) {
             throw new WebApplicationException(e, 500);
         }
     }
}
