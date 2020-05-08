 package org.n52.wps.server;
 
 import gov.usgs.cida.gdp.wps.util.MIMEUtil;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.n52.wps.server.database.DatabaseFactory;
 import org.n52.wps.server.database.IDatabase;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RetrieveResultServlet extends HttpServlet {
 
     private final static Logger LOGGER = LoggerFactory.getLogger(RetrieveResultServlet.class);
     
     private static final long serialVersionUID = -268198171054599696L;
     
     // This is required for URL generation for response documents.
     public final static String SERVLET_PATH = "RetrieveResultServlet";
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         // id of result to retrieve.
         String id = request.getParameter("id");
         
         // return result as attachment (instructs browser to offer user "Save" dialog
         // if document is XML the default is false, otherwise the default is true.
         String attachment = request.getParameter("attachment");
 
         if (StringUtils.isEmpty(id)) {
             errorResponse("id parameter missing", response);
         } else {
             
             IDatabase db = DatabaseFactory.getDatabase();
             String mimeType = db.getMimeTypeForStoreResponse(id);
                 
             InputStream is = null;
             try {
                 db.lookupResponse(id);
 
                 if (mimeType == null || is == null) {
                     errorResponse("id parameter of " + id + " is unknown to server", response);
                 } else {
                     String suffix = MIMEUtil.getSuffixFromMIMEType(mimeType).toLowerCase();
                     boolean useAttachment = (StringUtils.isEmpty(attachment) && !"xml".equals(suffix)) || Boolean.parseBoolean(attachment);
                     if (useAttachment) {
                         response.addHeader("Content-Disposition", "attachment; filename=\"wps-result." + suffix + "\"");
                     }
                     // set Content-Type before getting outputstream, this allows
                     // for output stream 
                     response.setContentType(mimeType);
                     OutputStream os = response.getOutputStream();
                     long bytes = IOUtils.copyLarge(is, os);
                     os.flush();
                     LOGGER.info("{} bytes written in response to id {}", bytes, id);
                 }
             } finally {
                 IOUtils.closeQuietly(is);
             }
         }
         response.flushBuffer();
     }
 
     protected void errorResponse(String error, HttpServletResponse response) throws IOException {
         response.setContentType("text/html");
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         PrintWriter writer = response.getWriter();
         writer.write("<html><title>Error</title><body>" + error + "</body></html>");
         writer.flush();
         LOGGER.warn("Error processing response: " + error);
     }
 }
