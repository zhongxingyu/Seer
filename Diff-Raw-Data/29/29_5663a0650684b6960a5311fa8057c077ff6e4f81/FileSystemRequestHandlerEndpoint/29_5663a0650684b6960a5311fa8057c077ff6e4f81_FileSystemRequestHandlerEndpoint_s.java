 package com.example.fsexplorer.api.rs;
 
 import com.example.fsexplorer.api.FileSystemRequestHandler;
 import com.example.fsexplorer.api.NodeList;
 import com.example.fsexplorer.fs.FileSystemResolver;
 import com.example.fsexplorer.template.TemplateFactory;
 import org.springframework.stereotype.Service;
 
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 
 /**
  * @author Andrey Tarashevsky
  *         Date: 02.02.13
  */
 @Service("fsExplorerWS")
 public class FileSystemRequestHandlerEndpoint implements FileSystemRequestHandler {
 
     private final FileSystemResolver fileSystemResolver;
 
     public FileSystemRequestHandlerEndpoint(FileSystemResolver fileSystemResolver) {
         this.fileSystemResolver = fileSystemResolver;
     }
 
     @GET
     @Path("/children/{path:.*}")
     @Produces("application/json")
     @Override
     public NodeList getAllChildrenPaths(@PathParam("path") final String parentRoot) {
         return fileSystemResolver.getAllChildren(parentRoot);
     }
 
     @GET
     @Path("/rendered/children/{path:.*}")
     @Override
     public Response getAllChildrenRendered(@PathParam("path") final String parentRoot) {
        NodeList nodeList = fileSystemResolver.getAllChildren(parentRoot);
         try {
             String html = TemplateFactory.prepareNodeListTemplate(nodeList);
             return Response.ok().type(MediaType.TEXT_HTML_TYPE).entity(html).build();
         } catch (IOException e) {
             return Response.serverError().build();
         }
     }
 
     @GET
     @Path("/template")
     @Produces("text/plain")
     public Response getTemplate() {
         return Response.ok().entity(TemplateFactory.getDefaultTemplate()).build();
     }
 
 
 }
