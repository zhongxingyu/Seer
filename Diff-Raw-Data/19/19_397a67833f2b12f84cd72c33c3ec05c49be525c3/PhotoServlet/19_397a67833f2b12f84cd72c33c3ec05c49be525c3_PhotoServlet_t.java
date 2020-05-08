 package com.dateengine.controllers;
 
 /*
    Need to put this JAR in WEB-INF/lib
    http://commons.apache.org/fileupload/
  */
 
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.ByteArrayOutputStream;
 import java.util.logging.Logger;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.jdo.PersistenceManager;
 
 import com.dateengine.models.Photo;
 import com.dateengine.models.Profile;
 import com.dateengine.PMF;
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.users.User;
 
 public class PhotoServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(PhotoServlet.class.getName());
 
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       try {
          ServletFileUpload upload = new ServletFileUpload();
          FileItemIterator iterator = upload.getItemIterator(request);
 
          while (iterator.hasNext()) {
             FileItemStream item = iterator.next();
             InputStream stream = item.openStream();
 
             if (item.isFormField()) {
                log.warning("Got a form field: " + item.getFieldName());
             } else {
                log.warning("Got an uploaded file: " + item.getFieldName() +
                        ", name = " + item.getName());
 
                byte[] rawData = getDataFromInputStream(stream);
                stream.close();
 
 
               // TODO: Also looks like we can't change a profile photo once we've set one ...
                PersistenceManager pm = PMF.get().getPersistenceManager();
                try {
                   User currentUser = (User) request.getAttribute("user");
                   Profile profile = (Profile) pm.getObjectById(Profile.class, currentUser.getEmail());
                  Photo photo = profile.getPhoto();

                  if (photo == null) {
                     photo = new Photo();
                  }

                  // TODO: Get the Content-type and set it correctly so we don't just force JPEG for everything
                  photo.setImage(new Blob(rawData));

                  photo.setCreatedAt(new Date());
 
                   profile.setPhoto(photo);
                   pm.makePersistent(profile);
 
                } catch (Exception e) {
                   throw new ServletException(e);
                } finally {
                   pm.close();
                }
                stream.close();
 
             }
          }
       } catch (Exception ex) {
          throw new ServletException(ex);
       }
 
    }
 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       String keyString = request.getParameter("key");
       String size = request.getParameter("size");
       PersistenceManager pm = PMF.get().getPersistenceManager();
       Photo photo = (Photo) pm.getObjectById(Photo.class, keyString);
       Blob imageData;
 
       if (size != null && size.equals("full")) {
          imageData = photo.getImage();
       } else { // We set the default to just the thumbnail
          imageData = photo.getThumbnail();
       }
 
       response.setContentType("image/jpeg");
       response.getOutputStream().write(imageData.getBytes());
       response.getOutputStream().flush();
 
    }
 
    private byte[] getDataFromInputStream(InputStream stream) throws IOException {
       byte[] rawData;
       ByteArrayOutputStream output = new ByteArrayOutputStream();
       int len;
       byte[] buffer = new byte[8192];
 
       try {
          while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
             output.write(buffer, 0, len);
          }
          rawData = output.toByteArray();
       } finally {
          output.close();
       }
       return rawData;
    }
 }
