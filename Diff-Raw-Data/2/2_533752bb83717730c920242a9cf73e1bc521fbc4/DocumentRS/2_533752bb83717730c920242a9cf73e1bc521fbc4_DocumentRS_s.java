 package vio.service.rest.doc;
 
 import vio.service.rest.AbstractRS;
 import javax.ejb.EJB;
 import javax.ws.rs.GET;
 
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.core.Response;
 import vio.model.doc.Document;
 import vio.service.doc.DocumentFacade;
 
 /**
  * @author moroz
  */
 @Path("/doc")
 public class DocumentRS extends AbstractRS {
 
     @EJB
     DocumentFacade facade;
 
     @GET
     @Path("{id:\\d+}")
     @Produces(MediaType.APPLICATION_JSON)
     public Document getDocumentByID(@PathParam("id") int id) {
        return facade.get(Integer.valueOf(id));
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response getDocumentList(@HeaderParam("X-Range") String headerRange) {
         int[] range = getRangeFromHeader(headerRange);
         return Response.ok()
                 .entity(facade.listByRange(range))
                 .header("X-Content-Range", buildContentRangeHeaderValue(range)).build();
 
     }
 }
