 package com.edumet.models.payroll;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.Serializable;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 
 public class W2Model implements Serializable {
 
     private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
     private static Logger log = Logger.getLogger(W2Model.class);
 
     public W2Model() {
         super();
     }
 
     private String year;
     private byte[] stream;
 
     public void setYear(String year) {
         this.year = year;
     }
 
     public String getYear() {
         return year;
     }
 
     public void setStream(byte[] stream) {
         this.stream = stream;
     }
 
     public byte[] getStream() {
         return stream;
     }
 
     public String onClick() {
 
         try {
             downloadPDF();
         } catch (IOException ioe) {
             log.error(ioe, ioe);
         }
         
         return "";
 
     }
     // Actions ------------------------------------------------------------------------------------
 
     private void downloadPDF() throws IOException {
 
         // Prepare.
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         HttpServletResponse response = (HttpServletResponse)externalContext.getResponse();
 
 
         BufferedInputStream input = null;
         BufferedOutputStream output = null;
 
         try {
             // Open file.
             input = new BufferedInputStream(new ByteArrayInputStream(stream, 0, stream.length));
 
             // Init servlet response.
             response.reset();
             response.setHeader("Content-Type", "application/pdf");
             response.setHeader("Content-Length", String.valueOf(stream.length));
            response.setHeader("Content-Disposition", "inline;filename=\"" + year + "\"");
             output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);
 
             // Write file contents to response.
             byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
             int length;
             while ((length = input.read(buffer)) > 0) {
                 output.write(buffer, 0, length);
             }
 
             // Finalize task.
             output.flush();
         } finally {
             // Gently close streams.
             close(output);
             close(input);
         }
 
         // Inform JSF that it doesn't need to handle response.
         // This is very important, otherwise you will get the following exception in the logs:
         // java.lang.IllegalStateException: Cannot forward after response has been committed.
         facesContext.responseComplete();
     }
 
     // Helpers (can be refactored to public utility class) ----------------------------------------
 
     private static void close(Closeable resource) {
         if (resource != null) {
             try {
                 resource.close();
             } catch (IOException e) {
                 // Do your thing with the exception. Print it, log it or mail it. It may be useful to
                 // know that this will generally only be thrown when the client aborted the download.
                 log.error(e, e);
             }
         }
     }
 }
