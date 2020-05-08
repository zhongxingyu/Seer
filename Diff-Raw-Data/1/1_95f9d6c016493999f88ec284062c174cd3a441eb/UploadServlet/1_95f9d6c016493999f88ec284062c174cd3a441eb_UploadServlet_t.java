 package eu.liveandgov.wp1.server;
 
 import org.apache.commons.fileupload.*;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 
 import static java.lang.System.currentTimeMillis;
 import static org.apache.commons.io.IOUtils.copy;
 
 /**
  * Implementation of a sensor data UploadServlet based on {@link org.apache.commons.fileupload}
  * see <a href="http://commons.apache.org/proper/commons-fileupload/">website.</a>
  * <p/>
  * Listens to POST request with a form/multipart field named 'upfile' and writes it to the file system.
  * <p/>
  * Test with
  * <pre>{@code echo "FILE CONTENTS" | curl localhost:8080/server/upload -F "upfile=@-"}</pre>
  * or
  * <pre>{@code cat test-upload-data.txt | curl localhost:8080/server/upload -F "upfile=@-"}</pre>
  *
  * User: hartmann
  * Date: 10/19/13
  */
 public class UploadServlet extends HttpServlet {
     static final String OUT_DIR = "/srv/liveandgov/UploadServletRawFiles/";
     private static final String FIELD_NAME_UPFILE = "upfile";
     private static final Logger LOG = Logger.getLogger(UploadServlet.class);
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         PrintWriter writer = resp.getWriter();
         writer.write(
                 "<html>" +
                 "<h1>Upload Servlet</h1>" +
                 "<form action=\"\" enctype=\"multipart/form-data\" method=\"post\">" +
                 "Upload File: <input type=\"file\" name=\"upfile\" size=\"40\"/><br/>" +
                 "<input type=\"submit\" value=\"Submit\">" +
                 "</form>" +
                 "</html>"
                 );
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         LOG.info("Incoming POST request from " + req.getRemoteAddr());
 
         // Retrieve Upfile
         InputStream fileStream = getFormFieldStream(req, FIELD_NAME_UPFILE);
 
         if (fileStream == null) {
             LOG.error("Field not found: " + FIELD_NAME_UPFILE);
             resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
 
         // Write to file system
         String fileName = generateFileName(req);
         int bytesWritten = 0;
 
         File outFile = new File(OUT_DIR, fileName);
         try {
             bytesWritten = writeStreamToFile(fileStream, outFile);
         } catch (IOException e) {
             LOG.error("Error writing output file.");
             resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
 
         // Success
         LOG.info("Received file " + fileName + " of length " + bytesWritten);
         resp.getWriter().write(
                 // Output will be parsed by test script.
                 "Status:Success.\n" +
                 "Destination:"+ outFile.getAbsolutePath() + "\n" +
                 "Bytes written:" + bytesWritten
         );
         resp.setStatus(HttpServletResponse.SC_ACCEPTED);
     }
 
     /**
      *  Writes contents of inputStream to a file.
      *  Uses Apache IOUtils for copy.
      *  @see <a href="http://stackoverflow.com/questions/43157/easy-way-to-write-contents-of-a-java-inputstream-to-an-outputstream/43168#43168">StackOverflow</a>
      *  for a discussion.
      *
      * @param inputStream   stream to read
      * @param file          file to write
      * @throws IOException  if writing of file fails
      * @return the number of bytes copied, or -1 if > Integer.MAX_VALUE
      */
     private int writeStreamToFile(InputStream inputStream, File file) throws IOException {
         assert(inputStream != null): "inputStream must be non-null";
 
         OutputStream outputStream = new FileOutputStream(file);
         return IOUtils.copy(inputStream, outputStream);
     }
 
     /**
      * Generate a "unique" file name for the given request.
      *
      * @param req       HttpRequest
      * @return fileName
      */
     private String generateFileName(HttpServletRequest req) {
         return req.getHeader("ID") + "_" + currentTimeMillis();
     }
 
     /**
      * Get contents of fieldName as InputStream from POST request
      *
      * @see <a href="http://commons.apache.org/proper/commons-fileupload/streaming.html">the docs.</a>
      *
      * @param req               HttpRequest Containing a Form Request
      * @param fieldName         name of field to isolate
      * @return formFieldStream
      */
     private InputStream getFormFieldStream(HttpServletRequest req, String fieldName) {
         try {
             // Check that we have a file upload request
             boolean isMultipart = ServletFileUpload.isMultipartContent(req);
             if (!isMultipart) {
                 LOG.error("Received non-multipart POST request");
                 return null;
             }
 
             // Create a new file upload handler
             ServletFileUpload upload = new ServletFileUpload();
 
             // Parse the request
             FileItemIterator iter = upload.getItemIterator(req);
 
             while (iter.hasNext()) {
                 FileItemStream item = iter.next();
                 String name = item.getFieldName();
 
                 if (!name.equals(FIELD_NAME_UPFILE)) {
                     LOG.info("Found unknown field " + name);
                     continue;
                 }
                 return item.openStream();
             }
         } catch (FileUploadException e) {
             LOG.error("Error parsing the upfile", e);
         } catch (IOException e) {
             LOG.error("Network error while parsing file.", e);
         }
         return null;
     }
 }
