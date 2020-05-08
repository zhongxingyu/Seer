 package controllers.minifymod;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import play.Play;
 import play.mvc.After;
 import play.mvc.Controller;
 import play.mvc.Finally;
 
 public class MinifyAndGzipResponse extends Controller {
 	
 	private static boolean moduleEnabled = true;
 	private static boolean minifyEnabled = true;
 	private static boolean gzipEnabled = true;
 
 	static {
 		moduleEnabled = !"false".equals(Play.configuration.getProperty("minifymod.moduleEnabled"));
 		minifyEnabled = !"false".equals(Play.configuration.getProperty("minifymod.minifyEnabled"));
 		gzipEnabled = !"false".equals(Play.configuration.getProperty("minifymod.gzipEnabled"));
 		if(Play.mode.isDev() && moduleEnabled) {
 			moduleEnabled = !"true".equals(Play.configuration.getProperty("minifymod.moduleDisabledOnDev"));
 		}
 	}
 	
 	/**
 	 * this is triggered after rendering is done. It takes the rendered template from response.out,
 	 * creates a gzipped stream for response.out  (if supported by the client), minifies the content
 	 *  and writes the template-string back to response.out
 	 */
	@Finally
 	static void compress() throws IOException {
 		if(moduleEnabled && response != null) {
 			// get rendered content
 			String content = response.out.toString();
			if("".equals(content)) {
				return; // fix stange chars on suspended requests
			}
 			// minify
 			if(minifyEnabled && !isExcluded() && response.contentType != null) {
 				// select compression method by contentType
 				if (response.contentType.contains("text/html")) {	// could be "text/html; charset=utf-8"
 					content = Compression.compressHTML(content);
 				} else if (response.contentType.contains("text/xml")) {
 					content = Compression.compressXML(content);
 				} else if (response.contentType.contains("text/css")) {
 					content = Compression.compressCSS(content);
 				} else if (response.contentType.contains("text/javascript")) {
 					content = Compression.compressJS(content);
 				}
 			}
 
 			// gzip only if enabled, supported and not excluded
 			if(gzipEnabled && isGzipSupported() && !isExcluded()) {
 				final ByteArrayOutputStream gzip = Compression.getGzipStream(content);
 				// set response header
 				response.setHeader("Content-Encoding", "gzip");
 				response.setHeader("Content-Length", gzip.size() + "");
 				response.out = gzip;
 			} else {
 				response.out = new ByteArrayOutputStream(content.length());
 				response.out.write(content.getBytes());
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
