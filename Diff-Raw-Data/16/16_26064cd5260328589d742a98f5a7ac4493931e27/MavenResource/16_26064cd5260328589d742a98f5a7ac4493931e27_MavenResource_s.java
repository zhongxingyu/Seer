 package com.zenika.dorm.maven.ws.resource;
 
 import com.google.inject.Inject;
 import com.zenika.dorm.core.model.DormRequest;
 import com.zenika.dorm.core.model.builder.DormRequestBuilder;
 import com.zenika.dorm.core.processor.Processor;
 import com.zenika.dorm.maven.model.impl.MavenMetadataExtension;
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 @Path("maven")
 @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 public class MavenResource {
 
     private static final Logger LOG = LoggerFactory.getLogger(MavenResource.class);
 
     @Inject
     private Processor processor;
 
     public MavenResource() {
         LOG.info("Init maven resource as web service");
     }
 
 //    @GET
 //    @Path("{path:.*}")
 //    public Response get(@PathParam("path") String path) {
 //        LOG.info("Artifact path : " + path);
 //        String[] differentPath = path.split("/");
 //        String fileName = differentPath[differentPath.length - 1];
 //        String version = differentPath[differentPath.length - 2];
 //        String artifactId = differentPath[differentPath.length - 3];
 //        String groupId = "";
 //        for (int i = 0; i < differentPath.length - 3; i++){
 //            if (i == differentPath.length - 4){
 //                groupId += differentPath[i];
 //            } else {
 //                groupId += differentPath[i] + ".";
 //            }
 //        }
 //
 //        return Response.status(Response.Status.OK).build();
 //    }
 
     @GET
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Path("{groupId:.*}/{artifactID}/{version}/{fileName}")
     public Response get(@PathParam("groupId") String groupId, @PathParam("artifactID") String artifactId,
                         @PathParam("version") String version, @PathParam("fileName") String fileName) {
         LOG.info("***GET***");
         LOG.info("GroupId : " + groupId);
         LOG.info("ArtifactId : " + artifactId);
         LOG.info("Version : " + version);
         LOG.info("FileName : " + fileName);
         File file = null;
         // TODO : Put in processor
         if (!fileName.equals("maven-metadata.xml")) {
             DormRequest request = new DormRequestBuilder(version, MavenMetadataExtension.NAME).filename(fileName)
                     .filename(fileName).property(MavenMetadataExtension.METADATA_GROUPID, groupId)
                     .property(MavenMetadataExtension.METADATA_ARTIFACTID, artifactId)
                     .property(MavenMetadataExtension.METADATA_VERSIONID, version)
                     .property(MavenMetadataExtension.METADATA_TYPE, FilenameUtils.getExtension(fileName)).build();
             file = processor.get(request).getFile().getFile();
             LOG.info("Return file : " + file);
         } else {
             return Response.status(Response.Status.NOT_FOUND).build();
         }
         return Response.status(Response.Status.OK).entity(file).build();
     }
 
     @PUT
     @Consumes(MediaType.APPLICATION_OCTET_STREAM)
     @Path("{groupId:.*}/{artifactId}/{version}/{fileName}")
     public Response put(File file, @PathParam("groupId") String groupId, @PathParam("artifactId") String artifactId,
                         @PathParam("version") String version, @PathParam("fileName") String fileName) {
         LOG.info("***PUT***");
         LOG.info("Absolute path : " + file.getAbsolutePath());
         DormRequest request = new DormRequestBuilder(version, MavenMetadataExtension.NAME).file(file)
                 .filename(fileName).property(MavenMetadataExtension.METADATA_GROUPID, groupId)
                 .property(MavenMetadataExtension.METADATA_ARTIFACTID, artifactId)
                 .property(MavenMetadataExtension.METADATA_VERSIONID, version).build();
         processor.push(request);
         return Response.status(Response.Status.OK).build();
     }
 
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
 
         File sha1File = null;
         try {
             File tmp = new File("/home/erouan/test.xml");
             String sha1 = getHash(tmp, "SHA1");
             sha1File = new File("tmp/" + fileName + ".sha1");
             FileWriter writer = new FileWriter(sha1File);
             writer.write(sha1);
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return Response.status(Response.Status.OK).entity(sha1File).build();
     }
 
     @GET
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     @Path("{groupId:.*}/{artifactID}/{versions}/{fileName}.md5")
     public Response getMd5(@PathParam("groupId") String path, @PathParam("artifactID") String artifactId,
                            @PathParam("versions") String version, @PathParam("fileName") String fileName) {
         LOG.info("Get MD5 ");
         LOG.info("GroupId : " + path);
         LOG.info("ArtifactId : " + artifactId);
         LOG.info("Version : " + version);
         LOG.info("FileName : " + fileName);
 
         File md5File = null;
         try {
             File tmp = new File("/home/erouan/test.xml");
             String md5 = getHash(tmp, "MD5");
             md5File = new File("tmp/" + fileName + ".md5");
             FileWriter writer = new FileWriter(md5File);
             writer.write(md5);
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return Response.status(Response.Status.OK).entity(md5File).build();
     }
 
 //    @POST
 //    @Consumes(MediaType.MULTIPART_FORM_DATA)
 //    public Response push(MultivaluedMap<String, String> params) {
 //        LOG.info("Push maven with values : " + params);
 //        return Response.status(Response.Status.OK).build();
 //    }
 
 //    @POST
 //    @Consumes(MediaType.MULTIPART_FORM_DATA)
 //    @Path("{groupid}/{artifactid}/{version}/{filename}")
 //    public Response createArtifact(@FormDataParam("file") File file, @PathParam("groupid") String groupId,
 //                                   @PathParam("artifactid") String artifactId, @PathParam("version") String version,
 //                                   @PathParam("filename") String filename) {
 //
 //        DormMavenMetadata metadata = new DormMavenMetadata(groupId, artifactId, version);
 //
 //        String extension = DormFileHelper.getExtensionFromFilename(filename);
 //
 //        service.pushArtifact(metadata, file, filename);
 //
 //        return Response.status(Response.Status.OK).build();
 //    }
 //
 //    @GET
 //    @Path("{groupid}/{artifactid}/{version}/{filename}")
 //    public DormArtifact<DormMavenMetadata> getArtifactByMetadata(@PathParam("groupid") String groupId,
 //                                                                 @PathParam("artifactid") String artifactId, @PathParam("version") String version,
 //                                                                 @PathParam("filename") String filename) {
 //
 //        DormMavenMetadata metadata = new DormMavenMetadata(groupId, artifactId, version);
 //
 //
 //
 //        DormArtifact<DormMavenMetadata> artifact = service.getArtifact(metadata, filename);
 //
 //        return null;
 //    }
 //
 //    @DELETE
 //    @Path("{groupid}/{artifactid}/{version}/{filename}")
 //    public Response removeArtifactByMetadata(@PathParam("groupid") String groupId,
 //                                             @PathParam("artifactid") String artifactId, @PathParam("version") String version,
 //                                             @PathParam("filename") String filename) {
 //
 //        DormMavenMetadata metadata = new DormMavenMetadata(groupId, artifactId, version);
 //
 //        service.removeArtifact(metadata, filename);
 //
 //        return Response.status(Response.Status.OK).build();
 //    }
 
     private String getHash(File file, String algo) {
         try {
             MessageDigest md = MessageDigest.getInstance(algo);
             FileInputStream fis = new FileInputStream(file);
             byte[] dataBytes = new byte[1024];
             int nread = 0;
             while ((nread = fis.read(dataBytes)) != -1) {
                 md.update(dataBytes, 0, nread);
             }
             byte[] mdBytes = md.digest();
             StringBuilder sb = new StringBuilder();
             for (int i = 0; i < mdBytes.length; i++) {
                 sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
             }
             fis.close();
             LOG.info("Digest(in hex format) " + algo + " ::" + sb.toString() + "-");
             return sb.toString();
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 }
