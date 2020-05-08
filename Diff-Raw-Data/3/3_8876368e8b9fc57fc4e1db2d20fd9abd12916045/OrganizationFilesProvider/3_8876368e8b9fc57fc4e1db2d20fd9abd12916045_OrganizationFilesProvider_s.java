 package net.cyklotron.ngo.bazy.files;
 
 import java.io.InputStream;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.bind.JAXBElement;
 
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.web.LedgeServletContextListener;
 import org.picocontainer.PicoContainer;
 
 import net.cyklotron.cms.files.FilesException;
 import net.cyklotron.cms.files.FilesListener;
 import net.cyklotron.cms.rest.FilesProvider;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.ngo.bazy.BazyngoService;
 
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
 
 import net.cyklotron.cms.rest.CmsFile;
 
 @Path("/org_files")
 public class OrganizationFilesProvider extends FilesProvider
 {
     private static final String baseDirName = FilesListener.SITE_PUBLIC_ROOT_DIR + "/" + BazyngoService.BAZYNGO_SITE_FILES_ROOT_DIR;
     private static final String orgPrefix ="/org";
     private static final String filePrefix = "/file";
 
     @GET
     @Path(orgPrefix + "/{orgId: [a-zA-Z0-9_]+}/{ftype}/{fname}")
     @Produces("application/json")
     public CmsFile getFile(@PathParam("orgId") String orgId, 
             @PathParam("ftype") String ftype,
             @PathParam("fname") String fname) {
         CmsFile file = getCmsFile(buildPath(orgId, ftype, fname));
         file.setOrganizationFileType(ftype);
         return file;
                        
     }
     
     @GET
     @Path(filePrefix + "/{fid}")
     @Produces("application/json")
     public CmsFile getFileById(@PathParam("fid") long fid) {
         CmsFile file = getCmsFileById(fid);
         String ftype = file.getFileResource().getParent().getName();
         file.setOrganizationFileType(ftype);
         return file;
 
     }    
 
     @GET
     @Path(orgPrefix +"/{orgId: [a-zA-Z0-9_]+}/{ftype}")
     @Produces("application/json")
     public List<CmsFile> getFiles(@PathParam("orgId") String orgId, 
             @PathParam("ftype") String ftype) {
         return getCmsFiles(buildPath(orgId, ftype, null));
     }
 
     @POST
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Path(orgPrefix + "/{orgId: [a-zA-Z0-9_]+}/{ftype}")
     public Response createFile(@PathParam("orgId") String orgId, 
             @PathParam("ftype") String ftype,
             @FormDataParam("fname") String fname,
             @FormDataParam("file") InputStream uploadedInputStream,
             @FormDataParam("file") FormDataContentDisposition fileDetail
             ) {
         if(fname == null) {
             fname = fileDetail.getFileName();
         }
         try
         {
             return createCmsFile(buildPath(orgId, ftype, fname), uploadedInputStream, fileDetail);
         }
         catch(FilesException e)
         {
             e.printStackTrace();
             return errorResponse(Response.Status.BAD_REQUEST, e.getMessage());
         }
     }
 
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Path(filePrefix + "/{fid}")
     public Response modifyFile(@PathParam("fid") long fid,
             @FormDataParam("file") InputStream uploadedInputStream,
             @FormDataParam("file") FormDataContentDisposition fileDetail) {
         return modifyCmsFile(fid, uploadedInputStream, fileDetail);
     }        
     
     @DELETE
     @Produces("application/json")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path(orgPrefix + "/{orgId}/{ftype}/{fname}")
     public Response deleteFile(@PathParam("orgId") String orgId, 
             @PathParam("ftype") String ftype,
             @PathParam("fname") String fname) {
         return deleteCmsFileByPath(buildPath(orgId, ftype, fname));                    
     }    
   
     @DELETE
     @Produces("application/json")
     @Path(filePrefix + "/{fid}")
     public Response deleteFile(@PathParam("fid") long fid) {
         return deleteCmsFileById(fid);                    
     }    
   
     @Override
     public SiteResource getSite() {
         final PicoContainer container = (PicoContainer)getContext().getAttribute(LedgeServletContextListener.CONTAINER_CONTEXT_KEY);
         final BazyngoService bazyngo = (BazyngoService)container.getComponentInstance(BazyngoService.class);        
 
         return bazyngo.getSite();
     }
     
     private String buildPath(String orgId, String ftype, String fname) {
         if(fname == null) {
             return baseDirName + "/" + buildOrgIdPath(orgId) + "/" + ftype;
         }
         return baseDirName + "/" + buildOrgIdPath(orgId) + "/" + ftype + "/" + fname;
     }
     
     private String buildOrgIdPath(String orgId) {
         return orgId;
     }
     
 }
