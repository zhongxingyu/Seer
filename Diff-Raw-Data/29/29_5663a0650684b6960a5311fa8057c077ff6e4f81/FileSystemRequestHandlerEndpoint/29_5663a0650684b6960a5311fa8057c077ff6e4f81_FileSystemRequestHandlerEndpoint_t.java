 package com.example.fsexplorer.api.rs;
 
 import com.example.fsexplorer.api.FileSystemRequestHandler;
 import com.example.fsexplorer.api.NodeList;
 import com.example.fsexplorer.fs.FileSystemResolver;
 import com.example.fsexplorer.template.TemplateFactory;
import org.apache.commons.codec.binary.Base64;
 import org.springframework.stereotype.Service;
 
import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
import java.nio.charset.Charset;
 
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
        if (!Base64.isBase64(parentRoot)) {
            return Response.serverError().build();
        }

        String realPath = new String(Base64.decodeBase64(parentRoot.getBytes()));
        NodeList nodeList = fileSystemResolver.getAllChildren(realPath);
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
