 package pl.psnc.dl.wf4ever.rosrs;
 
 import java.io.InputStream;
 import java.net.URI;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.dlibra.DigitalLibraryException;
 import pl.psnc.dl.wf4ever.dlibra.NotFoundException;
 
 import com.sun.jersey.core.header.ContentDisposition;
 
 /**
  * A REST API resource corresponding to a zipped RO.
  * 
  * @author Piotr Hołubowicz
  * 
  */
 @Path("zippedROs/{ro_id}/")
 public class ZippedResearchObjectResource {
 
     /** URI info. */
     @Context
     UriInfo uriInfo;
 
 
     /**
      * Returns zip archive with contents of RO version.
      * 
      * @param researchObjectId
      *            RO identifier - defined by the user
      * @return 200 OK
      * @throws DigitalLibraryException
      *             could not get the RO frol dLibra
      * @throws NotFoundException
      *             could not get the RO frol dLibra
      */
     @GET
     @Produces({ "application/zip", "multipart/related" })
     public Response getZippedRO(@PathParam("ro_id") String researchObjectId)
             throws DigitalLibraryException, NotFoundException {
        URI uri = uriInfo.getAbsolutePath().resolve("../../ROs/" + researchObjectId);
         ResearchObject researchObject = ResearchObject.findByUri(uri);
         if (researchObject == null) {
             researchObject = new ResearchObject(uri);
         }
         InputStream body = ROSRService.DL.get().getZippedResearchObject(researchObject);
         //TODO add all named graphs from SMS that start with the base URI
         ContentDisposition cd = ContentDisposition.type("application/zip").fileName(researchObjectId + ".zip").build();
         return ResearchObjectResource.addLinkHeaders(Response.ok(body), uriInfo, researchObjectId)
                 .header("Content-disposition", cd).build();
     }
 }
