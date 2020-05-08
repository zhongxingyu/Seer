 package net.contextfw.web.application.internal.servlet;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.internal.util.ResourceEntry;
 import net.contextfw.web.application.internal.util.ResourceScanner;
 
 import org.apache.commons.io.IOUtils;
 
 public abstract class ResourceServlet extends HttpServlet {
 
     private static final long serialVersionUID = -1979474932427776224L;
 
     private volatile String content = null;
 
     public void clean() {
         content = null;
     }
     
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         if (clear()) {
             clean();
         }
 
         if (content == null) {
             synchronized (this) {
                 if (content == null) {
                     StringBuilder contentBuilder = new StringBuilder();
                     List<ResourceEntry> files = ResourceScanner.findResources(getRootPaths(), getAcceptor());
                     for (ResourceEntry file : files) {
                         addContent(contentBuilder, file);
                     }
                     content = contentBuilder.toString();
                 }
             }
         }
 
         resp.getWriter().print(content);
         resp.getWriter().close();
     }
 
     private void addContent(StringBuilder contentBuilder, ResourceEntry file) {
         try {
             InputStream stream = file.getInputStream();
             contentBuilder.append(IOUtils.toString(stream));
            if (contentBuilder.charAt(contentBuilder.length() - 1) != '\n') {
            	contentBuilder.append("\n");
            }
             stream.close();
         } catch (FileNotFoundException e) {
             throw new WebApplicationException(e);
         } catch (IOException e) {
             throw new WebApplicationException(e);
         }
     }
 
     public abstract boolean clear();
 
     protected abstract Pattern getAcceptor();
 
     protected abstract List<String> getRootPaths();
 }
