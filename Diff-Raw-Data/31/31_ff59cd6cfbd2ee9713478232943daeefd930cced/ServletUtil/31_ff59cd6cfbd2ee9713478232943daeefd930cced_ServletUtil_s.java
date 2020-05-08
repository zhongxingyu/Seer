 package yugi.servlet;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
import java.net.URISyntaxException;
 import java.nio.CharBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import org.apache.http.client.utils.URIBuilder;

 import yugi.Config;
 import yugi.Config.HtmlParam;
 import yugi.Config.Mode;
 import yugi.Config.Servlet;
import yugi.service.UserUtil;
 import yugi.Screen;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class ServletUtil {
 	
 	private static UserService userService = UserServiceFactory.getUserService();
 
 	/**
 	 * Writes a standard web page back to the client.
 	 * @param req The request.
 	 * @param resp The response.
 	 * @param screen The screen to write.
 	 * @throws FileNotFoundException Thrown when any of the paths are wrong.
 	 * @throws IOException Thrown if any of the files can't be read.
 	 */
 	public static void writeScreen(
 			HttpServletRequest req, HttpServletResponse resp, Screen screen)
 	throws FileNotFoundException, IOException {
 		writeScreen(req, resp, screen, new HashMap<HtmlParam, String>());
 	}
 	
 	/**
 	 * Writes a standard web page back to the client.
 	 * @param req The request.
 	 * @param resp The response.
 	 * @param screen The screen to write.
 	 * @param paramMap The map of values that will replace things in the HTML.
 	 * @throws FileNotFoundException Thrown when any of the paths are wrong.
 	 * @throws IOException Thrown if any of the files can't be read.
 	 */
 	public static void writeScreen(HttpServletRequest req, HttpServletResponse resp,
 			Screen screen, Map<HtmlParam, String> paramMap)
 	throws FileNotFoundException, IOException {
 		
 		boolean rawMode = Config.isRawMode(req);
 		
 		// Get the HTML path.
 		String htmlPath = screen.getHtmlPath();
 		if (rawMode == true) {
 			htmlPath = screen.getDevHtmlPath();
 		}
 		
 		// Read in the HTML.
 		FileReader reader = new FileReader(htmlPath);
 	    CharBuffer buffer = CharBuffer.allocate(16384);
 	    reader.read(buffer);
 	    reader.close();
 		String html = new String(buffer.array());
 
 		// Maybe replace the JS file path.
 		if (rawMode != true) {
 			// Raw mode doesn't have a JS file param - it's specified in the HTML.
 			html = Config.replaceParam(html, HtmlParam.JS_FILE_PATH, screen.getJsPath());
 		}
 
 		// All CSS is parameterized the same way.
 		html = Config.replaceParam(html, HtmlParam.CSS_FILE_PATH, screen.getCssPath());
 		
		// Figure out the continue URL with the mode parameter attached.
		String thisUrl = req.getRequestURI();
		
		try {
			URIBuilder builder = new URIBuilder(thisUrl);
			Mode mode = Config.getMode(req);
			if (mode != null) {
				builder.setParameter(
						Config.UrlParameter.MODE.name().toLowerCase(),
						mode.toString().toLowerCase());
			}
			thisUrl = builder.build().toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
 		}
 
 		// Figure out the sign/in out URL.
 		User user = userService.getCurrentUser();
 		boolean signedOut = user == null;
 		String signInOutUrl = signedOut ?
				userService.createLoginURL(thisUrl) :
 				userService.createLogoutURL(createUrl(req, "/"));
 
 		// Figure out the deck manager URL.
 		String deckManagerUrl = createUrl(req, Servlet.DECK_MANAGER.getPath());
 		if (signedOut) {
 			deckManagerUrl = userService.createLoginURL(deckManagerUrl);
 		}
 
 		// Replace basic, shared parameters.
 		paramMap.put(HtmlParam.SIGN_IN_OUT_URL, signInOutUrl);
 		paramMap.put(HtmlParam.DECK_MANAGER_URL, deckManagerUrl);
 		paramMap.put(HtmlParam.USER_JSON, UserUtil.getCurrentUserAsJson());
 
 		// Replace specified parameters
 		for (Entry<HtmlParam, String> entry : paramMap.entrySet()) {
 			html = Config.replaceParam(html, entry.getKey(), entry.getValue());
 		}
 		
 	    resp.setContentType("text/html");
 	    resp.getWriter().write(html);
 	}
 	
 	/**
 	 * Writes the login screen to the response.
 	 * @param resp The HTTP response.
 	 * @param userService The user service.
 	 * @throws IOException Thrown if writing the response fails.
 	 */
 	public static void writeLoginScreen(
 			HttpServletRequest req,
 			HttpServletResponse res,
 			UserService userService)
 	throws IOException {
 		
 		// TODO Create an actual HTML file for this authentication screen.
 		
 		// Write out a link so they can authenticate themselves.  The link will
 		// forward the user to the current URL by default.
 		res.getWriter().println("<p>Please <a href=\"" +
 				userService.createLoginURL(req.getRequestURI()) +
 				"\">sign in</a> in order to use this part of Yu-Gi-Oh! Online.</p>");
 	}
 	
 	/**
 	 * Creates a URL.
 	 * @param req The request from which to extract mode information.
 	 * @param servlet The servlet destination for the URL.
 	 * @return The new URL.
 	 */
 	public static String createUrl(HttpServletRequest req, String path) {
 		return createUrl(req, path, null);
 	}
 	
 	/**
 	 * Creates a URL.
 	 * @param req The request from which to extract mode information.
 	 * @param servlet The servlet destination for the URL.
 	 * @param params The parameters that should be set in the URL.
 	 * @return The new URL.
 	 */
 	public static String createUrl(HttpServletRequest req, Config.Servlet servlet,
 			Map<Config.UrlParameter, String> params) {
 		return createUrl(req, servlet.getPath(), params);
 	}
 	
 	/**
 	 * Creates a URL.
 	 * @param req The request from which to extract mode information.
 	 * @param path The destination.
 	 * @param params The parameters that should be set in the URL.
 	 * @return The new URL.
 	 */
 	public static String createUrl(HttpServletRequest req, String path,
 			Map<Config.UrlParameter, String> params) {
 
 		// Create a map if there isn't one.
 		if (params == null) {
 			params = new HashMap<Config.UrlParameter, String>();
 		}
 		
 		// Keep the mode parameter.
 		Mode mode = Config.getMode(req);
 		if (mode != null) {
 			params.put(Config.UrlParameter.MODE, mode.name().toLowerCase());
 		}
 		
 		// The base URL is the path to the servlet.
 		String url = path;
 		
 		// Add all the URL parameters.
 		boolean first = true;
 		for (Entry<Config.UrlParameter, String> entry : params.entrySet()) {
 			if (first == true) {
 				url += "?";
 				first = false;
 			} else {
 				url += "&";
 			}
 			url += entry.getKey().name().toLowerCase() + "=" + entry.getValue();
 		}
 		
 		return url;
 	}
 }
