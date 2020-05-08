 package controllers.minifymod;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import play.Play;
 import play.mvc.Controller;
 import play.mvc.Finally;
 
 public class GzipResponse extends Controller {
 	
 	private static boolean moduleEnabled = true;
 	private static boolean gzipEnabled = true;
 
 	static {
 		moduleEnabled = !"false".equals(Play.configuration.getProperty("minifymod.moduleEnabled"));
 		gzipEnabled = !"false".equals(Play.configuration.getProperty("minifymod.gzipEnabled"));
 		if(Play.mode.isDev() && moduleEnabled) {
 			moduleEnabled = !"true".equals(Play.configuration.getProperty("minifymod.moduleDisabledOnDev"));
 		}
 	}
 	
 	/**
 	 * this is triggered after rendering is done. It takes the rendered template from response.out,
 	 * creates a gzipped stream for response.out  (if supported by the client) and writes the template-
 	 * string back to response.out
 	 */
	@Finally
 	static void compress() throws IOException {
 		if(moduleEnabled) {
 			// gzip response if enabled, supported and not excluded
 			if(gzipEnabled && isGzipSupported() && !isExcluded() && response != null) {
 				// get rendered content
 				String content = response.out.toString();
				if("".equals(content)) {
					return; // fix stange chars on suspended requests
				}
 				// change to a gzipped stream
 				final ByteArrayOutputStream gzip = Compression.getGzipStream(content);
 				// set response header
 				response.setHeader("Content-Encoding", "gzip");
 				response.setHeader("Content-Length", gzip.size() + "");
 				response.out = gzip;
 			}
 		}
 	}
 
 	/**
 	 * tells if the client supports gzip. Delegates to Compression.class
 	 * just here for easy overriding
 	 */
 	public static boolean isGzipSupported() {
 		return Compression.isGzipSupported(request);
 	}
 	
 	/**
 	 * tells if the current action is an excluded action. Delegates to Compression.class
 	 * just here for easy overriding
 	 */
 	public static boolean isExcluded() {
 		return Compression.isExcludedAction(request);
 	}
 	
 }
