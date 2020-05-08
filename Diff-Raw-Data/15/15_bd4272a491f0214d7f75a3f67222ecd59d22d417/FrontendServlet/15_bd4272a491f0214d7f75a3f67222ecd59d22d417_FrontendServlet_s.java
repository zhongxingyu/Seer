 package uk.ac.cam.cl.dtg.teaching;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Arrays;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.UriBuilder;
 
 import org.jboss.resteasy.client.ClientRequestFactory;
 import org.jboss.resteasy.client.ClientResponseFailure;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi;
 import uk.ac.cam.cl.dtg.teaching.api.DashboardApi.Settings;
 
 import com.google.common.collect.ImmutableMap;
 
 @WebServlet(//
 name = "frontend", //
 urlPatterns = { "/*" },//
 loadOnStartup = 1//
 )
 public class FrontendServlet extends HttpServlet {
 
 	private static Logger log = LoggerFactory.getLogger(FrontendServlet.class);
 
 	private static final long serialVersionUID = -7875184838824615593L;
 	private String dashboardUrl;
 	private String apiKey;
 
 	private String cssNamespace = null;
 	private String[] cssFiles;
 	private String[] jsFiles;
 
 	private void raiseInitError(String message) throws ServletException {
 		log.error(message);
 		throw new ServletException(message);
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 
 		// Load dashboard API URL from servlet context.
 		dashboardUrl = config.getServletContext().getInitParameter(
 				"dashboardUrl");
 
 		if (dashboardUrl == null) {
 			raiseInitError("Missing dashboardUrl from context parameters");
 		}
 
 		URI dashboardUri = UriBuilder.fromUri(dashboardUrl).build();
 		if (dashboardUri.getHost() == null) {
 			raiseInitError("Couldn't find a host when parsing dashboardUrl "
 					+ dashboardUrl);
 		}
 
 		cssNamespace = config.getServletContext().getInitParameter(
 				"cssNamespace");
 
 		// Load global API key from servlet context for accessing dashboard.
 		apiKey = config.getServletContext().getInitParameter("apiKey");
 
 		if (apiKey == null) {
 			raiseInitError("Missing API key from context parameters.");
 		}
 
 		// CSS files to include
 		String cssFilesStr = config.getServletContext().getInitParameter(
 				"cssFiles");
 
 		cssFiles = (cssFilesStr == null) ? new String[] {} : cssFilesStr
 				.split(",");
 
 		for (int i = 0; i < cssFiles.length; i++) {
 			cssFiles[i] = cssFiles[i].trim();
 		}
 
 		// JS files to include
 		String jsFilesStr = config.getServletContext().getInitParameter(
 				"jsFiles");
 
 		jsFiles = (jsFilesStr == null) ? new String[] {} : jsFilesStr
 				.split(",");
 
 		for (int i = 0; i < jsFiles.length; i++) {
 			jsFiles[i] = jsFiles[i].trim();
 		}
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException, ServletException {
 
 		// Handle logout
 		String expectedLink = req.getContextPath() + "/logout";
 		String actualLink = req.getRequestURI().toString();
 		if (expectedLink != null && actualLink != null
 				&& expectedLink.equals(actualLink)) {
 			log.debug("Logging user out");
 			req.getSession().invalidate();
 
 			// Destroy all cookies
 			Cookie[] cookies = req.getCookies();
 			if (cookies != null) {
 				for (Cookie cookie : cookies) {
 					cookie.setValue(null);
 					cookie.setPath("/");
 					cookie.setMaxAge(0);
 
 					resp.addCookie(cookie);
 				}
 			}
 
 			resp.sendRedirect("http://www.cam.ac.uk");
 			return;
 		}
 
 		URI dashboardUri = UriBuilder.fromUri(dashboardUrl).build();
 		ClientRequestFactory crf = new ClientRequestFactory(dashboardUri);
 		String userId = (String) req.getSession().getAttribute(
 				"RavenRemoteUser");
 
 		Settings settings;
 		try {
 			settings = crf.createProxy(DashboardApi.class).getSettings(userId,
 					apiKey);
 		} catch (ClientResponseFailure e) {
			throw new ServletException("Couldn't load Settings from "
					+ dashboardUri);
 		}
 
 		// Load extra javascript/CSS as required.
 		req.setAttribute("model", ImmutableMap.of("cssNamespace", cssNamespace,
 				"cssFiles", Arrays.asList(cssFiles), "jsFiles",
 				Arrays.asList(jsFiles), "contextPath", req.getContextPath(),
 				"settings", settings));
 
 		// Direct all requests to the main frontend template via Silken.
 		getServletContext().getRequestDispatcher("/soy/frontend.main").forward(
 				req, resp);
 	}
 
 }
