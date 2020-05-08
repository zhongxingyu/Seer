 package src;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Util {
	public static final Logger logger = Logger.getLogger(Util.class .getName());
 	
 	public static URI toURI(String url) {
 		String urlStr = url;
 		URI uri = null;
 		URL newURL = null;
 		
 		try {
 			newURL = new URL(urlStr);
 			uri = new URI(newURL.getProtocol(), newURL.getUserInfo(), 
 						  newURL.getHost(), newURL.getPort(), newURL.getPath(), 
 						  newURL.getQuery(),newURL.getRef());
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			logger.log(Level.SEVERE,e.getMessage(),e);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			logger.log(Level.SEVERE,e.getMessage(),e);
 		}
 		
 		return uri;
 	}
 	
 	public static void printMessage(String msg, String level, Logger logger) {
 		//System.out.println(msg);
 		if (level == "info")
 			logger.info(msg);
 		else if (level == "warn")
 			logger.warning(msg);
 		else 
 			logger.severe(msg);
 	}
 }
