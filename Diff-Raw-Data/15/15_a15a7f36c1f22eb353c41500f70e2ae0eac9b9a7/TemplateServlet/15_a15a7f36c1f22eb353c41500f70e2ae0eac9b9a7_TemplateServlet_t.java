 package edu.gatech.oad.rocket.findmythings.server;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP;
 import edu.gatech.oad.rocket.findmythings.server.util.HTTP.Status;
 import edu.gatech.oad.rocket.findmythings.server.util.MimeTypes;
 import edu.gatech.oad.rocket.findmythings.server.web.PageGenerator;
 
 public abstract class TemplateServlet extends BaseServlet {
 
 	private static final long serialVersionUID = 8526927539799303725L;
 	
     static final Logger LOGGER = Logger.getLogger(TemplateServlet.class.getName());
 
 	public TemplateServlet() {
 		// TODO Auto-generated constructor stub
 	}
     
     protected static int indexOfExtension(String filename) {
         if (filename == null) {
             return -1;
         }
         int extensionPos = filename.lastIndexOf(".");
         int lastUnixPos = filename.lastIndexOf('/');
         int lastWindowsPos = filename.lastIndexOf('\\');
         return (Math.max(lastUnixPos, lastWindowsPos) > extensionPos ? -1 : extensionPos);
     }
 
     protected static String removeExtension(String filename) {
         if (filename == null) {
             return null;
         }
         int index = indexOfExtension(filename);
         if (index == -1) {
             return filename;
         } else {
             return filename.substring(0, index);
         }
     }
     
     protected static String replaceExtensionWith(String filename, String extension) {
    	return removeExtension(filename) + "." + extension;
     }
     
     protected static String getDefaultTemplateURI(HttpServletRequest request) {
    	String URI = request.getRequestURI();
    	if (URI.endsWith("/")) URI = URI.substring(0, URI.length() - 1);
    	if (URI.length() == 0) URI = "index.html";
    	return replaceExtensionWith(URI, "ftl");
     }
     
     protected String createDocument(String templateName, Map<String,Object> userArgs) throws IOException {
     	return generator.createPage(templateName, userArgs);
     }
     
     protected String createDocument(String templateName, Object... args) throws IOException {
     	return createDocument(templateName, PageGenerator.map(args));
     }
     
     protected void writeDocument(HttpServletResponse response, String templateName, Map<String,Object> args) throws IOException {
     	HTTP.write(response, MimeTypes.HTML, Status.OK, createDocument(templateName, args));
     }
 
     protected void writeDocument(HttpServletResponse response, String templateName, Object... args) throws IOException {
     	writeDocument(response, templateName, PageGenerator.map(args));
     }
 
 }
