 package uk.co.tfd.sm.milton.spase;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.tfd.sm.milton.MiltonSecurityManager;
 import uk.co.tfd.sm.milton.SessionTrackerFilter;
 
 import com.bradmcevoy.http.AuthenticationHandler;
 import com.bradmcevoy.http.AuthenticationService;
 import com.bradmcevoy.http.Filter;
 import com.bradmcevoy.http.HttpManager;
 import com.bradmcevoy.http.Request;
 import com.bradmcevoy.http.ResourceFactory;
 import com.bradmcevoy.http.Response;
 import com.bradmcevoy.http.SecurityManager;
 import com.bradmcevoy.http.ServletRequest;
 import com.bradmcevoy.http.ServletResponse;
 import com.bradmcevoy.http.http11.Http11ResponseHandler;
 import com.bradmcevoy.http.http11.auth.PreAuthenticationFilter;
 import com.bradmcevoy.http.http11.auth.SecurityManagerBasicAuthHandler;
import com.google.common.collect.Lists;
 
 @Component(immediate = true, metatype = true)
 @Service(value = Servlet.class)
 @Properties(value = { @Property(name = "alias", value = "/dav") })
 public class SparseMiltonServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -7451452381693049651L;
 	private static final Logger LOGGER = LoggerFactory.getLogger(SparseMiltonServlet.class);
 	private HttpManager httpManager;
 
 	@Reference
 	private Repository repository;
 
 	@Activate
 	public void activate(Map<String, Object> properties) {
 		String basePath = (String) properties.get("alias");
 		if ( basePath == null ) {
 			basePath = "/dav";
 		}
 		ResourceFactory resourceFactory = new SparseMiltonContentResourceFactory(basePath);
 		SessionTrackerFilter sessionTrackerFilter = new SessionTrackerFilter();
 		SecurityManager securityManager = new MiltonSecurityManager(repository, sessionTrackerFilter, basePath);
		List<AuthenticationHandler> authHandlers = Lists.immutableList(
 				(AuthenticationHandler) new SecurityManagerBasicAuthHandler(securityManager));
 		AuthenticationService authenticationService = new AuthenticationService(authHandlers);
 		httpManager = new HttpManager(resourceFactory, authenticationService);
 		Http11ResponseHandler responseHandler = httpManager
 				.getResponseHandler();
 
 		Filter authenticationFilter = new PreAuthenticationFilter(
 				responseHandler, authHandlers);
 		httpManager.addFilter(0, authenticationFilter);
 		httpManager.addFilter(1, sessionTrackerFilter);
 
 	}
 
 	@Deactivate
 	public void deactivate(Map<String, Object> properties) {
 	}
 
 	@Override
 	protected void service(HttpServletRequest servletRequest,
 			HttpServletResponse servletResponse) throws ServletException,
 			IOException {
 		String litmusTestId = null;
 		try {
 			Request request = new ServletRequest(servletRequest);
 			Response response = new ServletResponse(servletResponse);
 			Map<String,String> headers = request.getHeaders();
 			if ( headers.containsKey("X-Litmus") ) {
 				litmusTestId = headers.get("X-Litmus");
 				LOGGER.info("+++++++++++++Litmus Test Start {} ",litmusTestId);
 			}
 			httpManager.process(request, response);
 		} finally {
 			if ( litmusTestId != null ) {
 				LOGGER.info("-------------Litmus Test End {} ",litmusTestId);
 			}
 			servletResponse.getOutputStream().flush();
 			servletResponse.flushBuffer();
 		}
 	}
 
 }
