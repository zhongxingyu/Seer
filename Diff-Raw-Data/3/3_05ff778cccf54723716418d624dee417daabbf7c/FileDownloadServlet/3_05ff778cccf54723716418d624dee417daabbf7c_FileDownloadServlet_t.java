 package org.iplantc.de.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.iplantc.de.shared.services.ServiceCallWrapper;
 import org.iplantc.irodsfile.util.LargeIOUtils;
 
 /**
  * A servlet for downloading a file.
  */
 @SuppressWarnings("serial")
 public class FileDownloadServlet extends HttpServlet {
     private static final String[] HEADER_FIELDS_TO_COPY = {"Content-Disposition"}; //$NON-NLS-1$
 
     // private static final Logger logger = Logger.getLogger(FileDownloadServlet.class);
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
         DEServiceInputStream fileContents = null;
         try {
             String address = buildRequestAddress(request);
             ServiceCallWrapper wrapper = new ServiceCallWrapper(address);
             DEServiceDispatcher dispatcher = createServiceDispatcher(request);
             fileContents = dispatcher.getServiceStream(wrapper);
             copyHeaderFields(response, fileContents);
             copyFileContents(response, fileContents);
         } catch (Exception e) {
             throw new ServletException(e.getMessage(), e);
         } finally {
             if (fileContents != null) {
                 try {
                     fileContents.close();
                 } catch (IOException ignore) {
                 }
             }
         }
     }
 
     /**
      * Copies the file contents from the given input stream to the output stream controlled by the given
      * response object.
      * 
      * @param response the HTTP servlet response object.
      * @param fileContents the input stream used to retrieve the file contents.
      * @throws IOException if an I/O error occurs.
      */
     private void copyFileContents(HttpServletResponse response, InputStream fileContents)
             throws IOException {
         OutputStream out = null;
         try {
             out = response.getOutputStream();
             LargeIOUtils.copy(fileContents, out);
         } finally {
             fileContents.close();
             if (out != null) {
                 out.close();
             }
         }
     }
 
     /**
      * Copies the content type along with any other HTTP header fields that are supposed to be copied
      * from the original HTTP response to our HTTP servlet response.
      * 
      * @param response our HTTP servlet response.
      * @param fileContents the file contents along with the HTTP headers and content type.
      */
     private void copyHeaderFields(HttpServletResponse response, DEServiceInputStream fileContents) {
         String contentType = fileContents.getContentType();
         response.setContentType(contentType == null ? "" : contentType); //$NON-NLS-1$
 
         for (String fieldName : HEADER_FIELDS_TO_COPY) {
             response.setHeader(fieldName, fileContents.getHeaderField(fieldName));
         }
     }
 
     /**
      * Creates the service dispatcher that will be used to fetch the file contents.
      * 
      * @param request our HTTP servlet request.
      * @return the service dispatcher.
      */
     private DEServiceDispatcher createServiceDispatcher(HttpServletRequest request) {
         DEServiceDispatcher dispatcher = new DEServiceDispatcher();
         try {
             dispatcher.init(getServletConfig());
         } catch (ServletException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         dispatcher.setContext(getServletContext());
         dispatcher.setRequest(request);
         return dispatcher;
     }
 
     /**
      * Builds the URL used to fetch the file contents.
      * 
      * @param request out HTTP servlet request.
      * @return the URL.
      * @throws UnsupportedEncodingException
      */
     private String buildRequestAddress(HttpServletRequest request) throws UnsupportedEncodingException {
         String user = URLEncoder.encode(request.getParameter("user"), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
         String path = URLEncoder.encode(request.getParameter("path"), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
 
         String attachment = request.getParameter("attachment"); //$NON-NLS-1$
         if (attachment == null) {
             attachment = "1"; //$NON-NLS-1$
         }
         attachment = URLEncoder.encode(attachment, "UTF-8"); //$NON-NLS-1$
 
         String downloadUrl = request.getParameter("url"); //$NON-NLS-1$
         if (downloadUrl == null) {
             downloadUrl = DiscoveryEnvironmentProperties.getDownloadFileServiceBaseUrl();
         } else {
            downloadUrl = DiscoveryEnvironmentProperties.getDataMgmtServiceBaseUrl() + downloadUrl;
         }
 
         String address = String.format("%s?user=%s&path=%s&attachment=%s", downloadUrl, user, path, //$NON-NLS-1$
                 attachment);
 
         return address;
     }
 }
