 package com.atlassian.pdkinstall;
 
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.File;
 import java.util.List;
 
 import com.atlassian.plugin.PluginController;
 import com.atlassian.plugin.PluginArtifactFactory;
 import com.atlassian.plugin.DefaultPluginArtifactFactory;
 import com.atlassian.plugin.PluginAccessor;
 
 /**
  * Created by IntelliJ IDEA.
  * User: mrdon
  * Date: Dec 7, 2008
  * Time: 2:43:52 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PdkInstallFilter implements Filter {
 
     private final PluginController pluginController;
     private final FileItemFactory factory;
     private final PluginAccessor pluginAccessor;
 
     private final PluginArtifactFactory pluginArtifactFactory;
    private static final Log log = LogFactory.getLog(FakePdkFilter.class);
 
     public PdkInstallFilter(PluginController pluginController, PluginAccessor pluginAccessor) {
         this.pluginController = pluginController;
         this.pluginAccessor = pluginAccessor;
         factory = new DiskFileItemFactory();
         pluginArtifactFactory = new DefaultPluginArtifactFactory();
     }
 
     public void init(FilterConfig filterConfig) throws ServletException {
 
     }
 
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
         HttpServletRequest req = (HttpServletRequest) servletRequest;
         HttpServletResponse res = (HttpServletResponse) servletResponse;
 
         if (!req.getMethod().equalsIgnoreCase("post"))
         {
             res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requires post");
             return;
         }
 
         // Check that we have a file upload request
         boolean isMultipart = ServletFileUpload.isMultipartContent(req);
         if (isMultipart)
         {
             // Create a new file upload handler
             ServletFileUpload upload = new ServletFileUpload(factory);
 
             // Parse the request
             File tmp = null;
             try {
                 List<FileItem> items = upload.parseRequest(req);
                 for (FileItem item : items)
                 {
                     if (!item.isFormField() && item.getFieldName().startsWith("file_"))
                     {
                         tmp = File.createTempFile("plugindev-", item.getName());
                         item.write(tmp);
                     }
                 }
             } catch (FileUploadException e) {
                 log.warn(e, e);
                 res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to process file upload");
             } catch (Exception e) {
                 log.warn(e, e);
                 res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to process file upload");
             }
 
             if (tmp != null)
             {
                 String key = pluginController.installPlugin(pluginArtifactFactory.create(tmp.toURI()));
                 if (!pluginAccessor.isPluginEnabled(key))
                 {
                     pluginController.enablePlugin(key);
                 }
                 tmp.delete();
                 res.setStatus(HttpServletResponse.SC_OK);
                 servletResponse.setContentType("text/plain");
                 servletResponse.getWriter().println("Installed plugin "+key);
                 servletResponse.getWriter().close();
                 return;
             }
         }
         res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing plugin file");
     }
 
     public void destroy() {
     }
 }
