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
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import se.umu.cs.umume.PersonBean;
 import se.umu.cs.umume.util.LDAPUtils;
 
 @Path("/search/{searchString}")
 public class SearchResource {
     private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);
     private static @Context UriInfo uriInfo;
     private static @PathParam("searchString") String searchString;
 
     // The Java method will process HTTP GET requests
     @GET
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public List<PersonBean> getUserXML() {
         try {
             if (searchString.length() < 3) {
                 throw new WebApplicationException(Response.Status.BAD_REQUEST);
             }
             List<PersonBean> result = LDAPUtils.toPersonBeans(LDAPUtils.searchPerson(searchString));
             for (PersonBean pb : result) {
                 pb.setResourceRef(UriBuilder.fromUri(uriInfo.getBaseUri()).path(UsersResource.class).build(pb.getUid()));
             }
             return result;
         } catch (NamingException e) {
            logger.warn("Search Exception: {} for search '{}'", e.getMessage(), searchString);
             Response r = Response.status(Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build();
             throw new WebApplicationException(r);
         }
     }
 }
