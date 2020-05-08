 package com.zenika.dorm.maven.ws.resource;
 
 import com.google.inject.Inject;
 import com.zenika.dorm.core.model.DormRequest;
 import com.zenika.dorm.core.model.builder.DormRequestBuilder;
 import com.zenika.dorm.core.processor.Processor;
 import com.zenika.dorm.maven.model.impl.MavenMetadataExtension;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.File;
 
 @Path("maven")
 @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 public class MavenResource {
 
     private static final Logger LOG = LoggerFactory.getLogger(MavenResource.class);
 
     @Inject
     private Processor processor;
 
     public MavenResource() {
         LOG.info("Init maven resource as web service");
     }
 
     @GET
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Path("{groupId:.*}/{artifactID}/{version}/{fileName}")
     public Response get(@PathParam("groupId") String groupId,
                         @PathParam("artifactID") String artifactId,
                         @PathParam("version") String version,
                         @PathParam("fileName") String fileName) {
 
         LOG.info("Call to maven web service : GET");
 
         // TODO : fix metadata
         if (fileName.equals("maven-metadata.xml")) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }

         DormRequest request = new DormRequestBuilder(version, MavenMetadataExtension.EXTENSION_NAME)
                 .filename(fileName)
                 .property(MavenMetadataExtension.METADATA_GROUPID, groupId)
                 .property(MavenMetadataExtension.METADATA_ARTIFACTID, artifactId)
                 .property(MavenMetadataExtension.METADATA_VERSION, version)
                 .build();

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("{groupId:.*}/{artifactID}/{versions}/{fileName}.sha1")
    public Response getSha1(@PathParam("groupId") String path, @PathParam("artifactID") String artifactId,
                            @PathParam("versions") String version, @PathParam("fileName") String fileName) {
        LOG.info("Get SHA1 ");
        LOG.info("GroupId : " + path);
        LOG.info("ArtifactId : " + artifactId);
        LOG.info("Version : " + version);
        LOG.info("FileName : " + fileName);

         try {
             File file = processor.get(request).getResource().getFile();
             return Response.status(Response.Status.OK).entity(file).build();
         } catch (NullPointerException e) {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
     }
 
     @PUT
     @Consumes(MediaType.APPLICATION_OCTET_STREAM)
     @Path("{groupId:.*}/{artifactId}/{version}/{fileName}")
     public Response put(File file,
                         @PathParam("groupId") String groupId,
                         @PathParam("artifactId") String artifactId,
                         @PathParam("version") String version,
                         @PathParam("fileName") String fileName) {
 
         LOG.info("Call to maven web service : PUT");
 
         DormRequest request = new DormRequestBuilder(version, MavenMetadataExtension.EXTENSION_NAME)
                 .file(file)
                 .filename(fileName)
                 .property(MavenMetadataExtension.METADATA_GROUPID, groupId)
                 .property(MavenMetadataExtension.METADATA_ARTIFACTID, artifactId)
                 .property(MavenMetadataExtension.METADATA_VERSION, version)
                 .build();
 
         LOG.info("PUT request to the maven web service : " + request);
 
         if (processor.push(request)) {
             return Response.status(Response.Status.OK).build();
         }
 
         return Response.status(Response.Status.NOT_FOUND).build();
     }
 }
