 package com.easytag.web.webservices;
 
 import com.easytag.core.entity.jpa.EasyTagFile;
 import com.easytag.core.entity.jpa.Photo;
 import com.easytag.core.entity.jpa.User;
 import com.easytag.core.managers.FileManagerLocal;
 import com.easytag.core.managers.PhotoManagerLocal;
 import com.easytag.core.managers.UserManagerLocal;
 import com.easytag.json.utils.JsonError;
 import com.easytag.json.utils.JsonResponse;
 import com.easytag.json.utils.ResponseConstants;
 import com.easytag.json.utils.SimpleResponseWrapper;
 import com.easytag.utils.StringUtils;
 import com.easytag.web.utils.SessionUtils;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.imageio.ImageIO;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import org.glassfish.jersey.media.multipart.FormDataBodyPart;
 import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
 import org.glassfish.jersey.media.multipart.FormDataParam;
 
 /**
  *
  * @author danshin
  */
 @Stateless
 @Path("file")
 public class FileResource {
 
     private static final List<String> ALLOWED_FILE_EXTENSIONS =
             Arrays.asList(".jpg", "jpeg", ".gif", ".png", ".bmp");
     private static final String UPLOAD_DIRECTORY = "C:/uploads";
     private static final int BUFFER_SIZE = 10*1024; // 10KB
     
     @Inject
     private UserManagerLocal um;
     @Inject
     private PhotoManagerLocal pm;
     @Inject
     private FileManagerLocal fm;
 
     @POST
     @Path("upload")
     @Consumes(MediaType.MULTIPART_FORM_DATA)
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response uploadFile(
             @Context HttpServletRequest request,
             @FormDataParam("file") InputStream uploadedInputStream,
             @FormDataParam("file") FormDataContentDisposition fileDetail,
             @FormDataParam("file") FormDataBodyPart body,
             @FormDataParam("album_id") Long albumId) {
         JsonError error = null;
         try {
             Long userId = SessionUtils.getUserId(SessionUtils.getSession(request, true));
             if (userId == null) {
                 error = new JsonError("User is not logged in.", ResponseConstants.NOT_AUTHORIZED_CODE);
                 throw new IllegalArgumentException("Unauthorised operation.");
             }
            if (albumId == null) {
                error = new JsonError("Album was not specified.", ResponseConstants.NOT_AUTHORIZED_CODE);
                throw new IllegalArgumentException("albumID == null.");
            }
 
            final String fileName = StringUtils.decode(fileDetail.getFileName(), "ISO-8859-1");
             if (!isSupportedFormat(fileName)) {
                 error = new JsonError("File '" + fileName + "' is not supported.", ResponseConstants.ACCESS_DENIED_CODE);
                 throw new RuntimeException("Unsupported format of the file: " + fileDetail.getFileName());
             }
 
             String uploadedFileName = saveUploadedFile(userId, albumId, uploadedInputStream, fileName);
             if (uploadedFileName == null) {
                 throw new RuntimeException("Couldn't upload file.");
             }
 
             EasyTagFile file = fm.addFile(userId, fileName, uploadedFileName, body.getMediaType().toString());
             if (file == null || file.getId() == null) {
                 throw new IllegalStateException("Unable to create file record.");
             }
             BufferedImage bimg = ImageIO.read(new File(uploadedFileName));
             Long width = new Long(bimg.getWidth());
             Long height = new Long(bimg.getHeight());
             Photo photo = pm.addPhoto(userId, albumId, fileName, null, null, file.getId(), height, width);
             if (photo == null || photo.getId() == null) {
                 throw new IllegalStateException("Unable to create a database record for the foto.");
             }
             
             pm.generatePreview(photo);
             return Response.ok(SimpleResponseWrapper.getJsonResponse(new JsonResponse<Photo>(ResponseConstants.OK, null, photo))).build();
         } catch (Exception ex) {
             ex.printStackTrace();
 
             if (error == null) {
                 error = new JsonError(ex.getMessage(), ResponseConstants.NORMAL_ERROR_CODE);
             }
             return Response.ok(SimpleResponseWrapper.getJsonResponse(new JsonResponse(ResponseConstants.OK, error, null))).build();
         }
     }
     
     @GET
     @Path("download")
     public Response download(@Context HttpServletRequest request, @QueryParam("id") long id, @QueryParam("inline") String inline) {
         try {
             Long userId = SessionUtils.getUserId(SessionUtils.getSession(request, true));
             if (userId == null) {
                 return Response.status(Status.FORBIDDEN).build();
             }
             
             User u = um.getUserById(userId);
             if (u == null) {
                 return Response.status(Status.FORBIDDEN).build();
             }
             
             return download(u.getEmail(), u.getPassword(), id, inline);
         } catch (Exception ex) {
             ex.printStackTrace();
             
             return Response.status(Status.NOT_FOUND).build();
         }
     }
     
     @GET
     @Path("client")
     public Response download(@QueryParam("email") String email, @QueryParam("password") String password, @QueryParam("id") long id, @QueryParam("inline") String inline) {
         try {
             try {
                 um.checkAuthorisationData(email, password);
             } catch (Exception ex) {
                 return Response.status(Status.FORBIDDEN).build();
             }
             User user = um.getUserByEmail(email);
             EasyTagFile file = fm.findFileById(id);
             if (file == null)
                 return Response.status(Response.Status.NOT_FOUND).build();
             String attachment = StringUtils.isTrue(inline) ? "inline" : "attachment";
             return Response.ok(new File(file.getCurrentPath()), file.getContentType())
                     .header("Content-Disposition", attachment + "; filename=\"" + file.getOriginalName() + "\"")
                     .header("Content-Length", file.getFileSize())
                     .build();
         } catch (Exception ex) {
             ex.printStackTrace();
             return Response.serverError().build();
         }
     }
 
     private boolean isSupportedFormat(String name) {
         if (StringUtils.isEmpty(name)) {
             return false;
         }
         String lowerName = name.toLowerCase();
         for (String ext : ALLOWED_FILE_EXTENSIONS) {
             if (lowerName.endsWith(ext)) {
                 return true;
             }
         }
         return false;
     }
 
     private String getValidFileName(String name) {
         return StringUtils.getValidString(name);
     }
 
     /**
      * Saves input stream to upload directory.
      *
      * @param is - input stream of uploaded file.
      * @return full name of target file
      */
     private String saveUploadedFile(Long userId, Long albumId, InputStream is, String originalName) {
         try {
             File uploadDir = new File(UPLOAD_DIRECTORY + "/user" + userId +"/album" + albumId) ;
             uploadDir.mkdirs();
             File targetFile = File.createTempFile("upload", originalName.substring(originalName.lastIndexOf('.')), uploadDir);
             OutputStream os = new FileOutputStream(targetFile);
             try {
                 byte[] buffer = new byte[BUFFER_SIZE];
                 int read = 0;
                 while ((read = is.read(buffer)) >= 0) {
                     os.write(buffer, 0, read);
                 }
                 return targetFile.getAbsolutePath();
             } finally {
                 try {
                     os.close();
                 } catch (IOException ex) {
                     ex.printStackTrace();
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
             return null;
         }
 
     }
 }
 
