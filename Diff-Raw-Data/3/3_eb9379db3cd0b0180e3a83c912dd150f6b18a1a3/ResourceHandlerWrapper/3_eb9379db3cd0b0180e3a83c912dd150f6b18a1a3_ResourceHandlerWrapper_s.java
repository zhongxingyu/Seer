 package ee.ut.cs.veebirakendus2013.kurivaim.jettytest.servlets;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.util.resource.Resource;
 
 public class ResourceHandlerWrapper extends ResourceHandler {
 	protected void doResponseHeaders(HttpServletResponse response, Resource resource, String mimeType) {
 		if ( mimeType != null && mimeType.equals("text/html") ) {
 			mimeType = "text/html; charset=UTF-8";
 		}
 		
 		super.doResponseHeaders(response, resource, mimeType);
     }
 }
