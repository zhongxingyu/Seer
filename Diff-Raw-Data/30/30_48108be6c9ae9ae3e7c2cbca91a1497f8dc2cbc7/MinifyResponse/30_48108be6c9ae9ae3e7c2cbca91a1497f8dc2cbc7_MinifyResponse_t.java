 package controllers.minifymod;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import play.Play;
 import play.mvc.After;
 import play.mvc.Controller;
 import play.mvc.Finally;
 
 public class MinifyResponse extends Controller {
 	
 	private static boolean moduleEnabled = true;
 	private static boolean minifyEnabled = true;
 
 	static {
 		moduleEnabled = !"false".equals(Play.configuration.getProperty("minifymod.moduleEnabled"));
 		minifyEnabled = !"false".equals(Play.configuration.getProperty("minifymod.minifyEnabled"));
 		if(Play.mode.isDev() && moduleEnabled) {
 			moduleEnabled = !"true".equals(Play.configuration.getProperty("minifymod.moduleDisabledOnDev"));
 		}
 	}
 	
 	/**
 	 * this is triggered after rendering is done. It takes the rendered template from response.out,
 	 * minifies it and writes it back to respone.out
 	 */
	@Finally
 	static void compress() throws IOException {
 		if(moduleEnabled && minifyEnabled && !isExcluded() && response != null && response.contentType != null) {
 			// get rendered content
 			String content = response.out.toString();
			if("".equals(content)) {
				return; // fix stange chars on suspended requests
			}
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
 			response.out = new ByteArrayOutputStream(content.length());
 			response.out.write(content.getBytes());
 		}
 	}
 
 	/**
 	 * tells if the current action is an excluded action. Delegates to Compression.class
 	 * just here for easy overriding
 	 */
 	public static boolean isExcluded() {
 		return Compression.isExcludedAction(request);
 	}
 
 }
