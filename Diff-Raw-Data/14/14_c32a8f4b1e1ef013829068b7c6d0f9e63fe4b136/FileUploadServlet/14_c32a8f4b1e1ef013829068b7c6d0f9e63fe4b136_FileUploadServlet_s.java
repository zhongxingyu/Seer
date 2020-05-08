 package otanga.Controllers;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.io.*;
 import org.apache.commons.io.*;
 import org.apache.commons.fileupload.*;
 import org.apache.commons.fileupload.disk.*;
 import org.apache.commons.fileupload.servlet.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Polys
  * Date: 10/05/13
  * Time: 15:16
  */
 public class FileUploadServlet extends HttpServlet {
 
     @Override
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
         try {
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
            for (FileItem item : items) {
                 if (item.isFormField())
                     continue;
 
                 String fileName = FilenameUtils.getName(item.getName());
                InputStream fileContent = item.getInputStream();
 
                 UUID fileKey = null;
 
                 int fileSize = fileContent.available();
                 if (fileSize > 0)
                 {
                     try{
                         byte[] data = new byte[fileSize];
                         if (fileContent.read(data) != fileSize)
                             throw new IOException();
 
                         fileKey = FileStorage.storeImage(data, item.getContentType());
                     }
                     finally {
                         fileContent.close();
                     }
                 }
 
                 if (fileKey != null)
                 {
                     //TODO: Respond somehow using the fileKey
                     resp.setStatus(200); // Status: OK
                     resp.setContentType("text/plain");
                     resp.getWriter().println("Uploaded file '" + fileName + "' with key: " + fileKey.toString());
 
 
                     //test reading back the file
                     String input = FileStorage.retrieveImage(fileKey);
                     resp.getWriter().println();
                     resp.getWriter().println(input);
 
                 }
                 else
                 {
                     resp.setStatus(400); // Status: Bad Request
                 }
             }
         } catch (FileUploadException e) {
             throw new IOException("Cannot parse multipart request.", e);
         }
     }
 }
