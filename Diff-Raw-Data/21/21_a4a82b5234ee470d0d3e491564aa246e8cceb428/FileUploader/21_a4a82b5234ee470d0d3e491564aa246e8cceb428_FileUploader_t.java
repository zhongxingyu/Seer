 /**
  * Copyright
  */
 package scldclng;
 
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Iterator;
 import java.util.List;
 
 public class FileUploader {
 
     private ProgressListener progressListener;
 
     private HttpServletRequest req;
     private String partName;
 
     public FileUploader(final HttpServletRequest req, final String partName) {
         this.req = req;
         this.partName = partName;
     }
 
     public void setProgressListener(final ProgressListener progressListener) {
         this.progressListener = progressListener;
     }
 
     public String upload() throws IOException {
         final ServletInputStream in = req.getInputStream();
 
         final MimeHeader contentType = new MimeHeader("Content-Type: " + req.getHeader("Content-Type"));
         final String boundary = contentType.getParameter("boundary");
         if (boundary == null) {
             throw new RuntimeException("MIME error: boundary not specified");
         }
 
         final Multipart multipart;
         try {
             multipart = new Multipart(in, boundary);
            multipart.setProgressListener(progressListener);
             multipart.parse();
         } catch (ParseException e) {
             throw new RuntimeException("Invalid input", e);
         }
 
         final List<MimePart> parts = multipart.getParts();
         MimeHeader filePartHeader = null;
 
         final Iterator<MimePart> iPart = parts.iterator();
         while (filePartHeader == null && iPart.hasNext()) {
             final MimePart part = iPart.next();
             final Iterator<MimeHeader> iHeaders = part.getHeaders().iterator();
 
             while (filePartHeader == null && iHeaders.hasNext()) {
                 final MimeHeader header = iHeaders.next();
                 if ("Content-Disposition".equalsIgnoreCase(header.getName()) &&
                         partName.equals(header.getParameter("name"))) {
                     filePartHeader = header;
                 }
             }
         }
 
         return filePartHeader == null ? null : filePartHeader.getParameter("filename");
     }
 }
