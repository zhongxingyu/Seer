 package com.epam.memegen;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.zip.CRC32;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 
 @SuppressWarnings("serial")
 public class UploadServlet extends HttpServlet {
 
   public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     ServletFileUpload u = new ServletFileUpload();
     u.setFileSizeMax((1 << 20) - 10000); // 1MiB - 10kB
 
     String topText = null;
     String centerText = null;
     String bottomText = null;
 
     String fileName = null;
     CRC32 crc32 = new CRC32();
     Blob blob = null;
 
     try {
       FileItemIterator iterator = u.getItemIterator(req);
       while (iterator.hasNext()) {
         FileItemStream item = iterator.next();
         InputStream is = item.openStream();
         String fieldName = item.getFieldName();
 
         if (!item.isFormField()) {
           fileName = item.getName();
           if (is.available() > 0) {
             byte[] bytes = IOUtils.toByteArray(is);
             blob = new Blob(bytes);
             crc32.update(bytes);
           }
         } else if (fieldName.equals("topText")) {
           topText = IOUtils.toString(is, "UTF-8");
         } else if (fieldName.equals("centerText")) {
           centerText = IOUtils.toString(is, "UTF-8");
         } else if (fieldName.equals("bottomText")) {
           bottomText = IOUtils.toString(is, "UTF-8");
         }
       }
 
       if (blob == null) {
         resp.sendError(400, "No file content");
         return;
       }
 
       DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       Entity entity = new Entity("Meme");
       entity.setUnindexedProperty("blob", blob);
       entity.setProperty("fileName", fileName);
      entity.setProperty("date", new Date());
       if (topText != null) entity.setProperty("topText", topText);
       if (centerText != null) entity.setProperty("centerText", centerText);
       if (bottomText != null) entity.setProperty("bottomText", bottomText);
 
       datastore.put(entity);
 
       resp.sendRedirect("/");
 
     } catch (FileUploadException e) {
       throw new IOException(e);
     }
   }
 }
