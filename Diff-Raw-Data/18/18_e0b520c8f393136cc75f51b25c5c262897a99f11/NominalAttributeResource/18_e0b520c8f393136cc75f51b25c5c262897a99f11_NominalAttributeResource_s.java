 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package qa.qcri.aidr.predictui.api;
 
 import java.util.List;
 import qa.qcri.aidr.predictui.util.ResponseWrapper;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import qa.qcri.aidr.predictui.dto.CrisisAttributesDTO;
 import qa.qcri.aidr.predictui.entities.NominalAttribute;
 import qa.qcri.aidr.predictui.facade.NominalAttributeFacade;
 
 /**
  * REST Web Service
  *
  * @author Imran
  */
 @Path("/attribute")
 @Stateless
 public class NominalAttributeResource {
 
     @Context
     private UriInfo context;
     @EJB
     private NominalAttributeFacade attributeLocalEJB;
 
     public NominalAttributeResource() {
     }
 
     
     
     @GET
     @Produces("application/json")
     @Path("/all")
     public Response getAllNominalAttributes() {
         List<NominalAttribute> attributeList = attributeLocalEJB.getAllAttributes();
         ResponseWrapper response = new ResponseWrapper();
        response.setMessage("SUCCESS");
         response.setNominalAttributes(attributeList);
         return Response.ok(response).build();
     }
     
     @GET
     @Produces("application/json")
     @Path("/crisis/all")
     public Response getAllNominalAttributesWithCrisis(@QueryParam("exceptCrisis") int crisisID) {
         List<CrisisAttributesDTO> attributeList = attributeLocalEJB.getAllAttributesExceptCrisis(crisisID);
         ResponseWrapper response = new ResponseWrapper();
        response.setMessage("SUCCESS");
         response.setCrisisAttributes(attributeList);
         return Response.ok(response).build();
     }
     
     @GET
     @Produces("application/json")
     @Path("{attributeID}")
     public Response getAttributesnLabelByAttrID(@PathParam("attributeID") int attributeID) {
         NominalAttribute attribute = attributeLocalEJB.getAttributeByID(attributeID);
         ResponseWrapper response = new ResponseWrapper();
         if (attribute != null){
             return Response.ok(attribute).build();
         }
         response.setMessage("no attribute found with the given id.");
         return Response.ok(response).build();
     }
     
     @POST
     @Consumes("application/json")
     @Produces(MediaType.APPLICATION_JSON)
     public Response addAttribute(NominalAttribute attribute){
         NominalAttribute attrib =  attributeLocalEJB.addAttribute(attribute);
         return Response.ok(attrib).build();
     }
 
     
 }
