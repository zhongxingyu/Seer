 package filmeUtils.http;
 
 import urlProtocolHandler.URLProtocolHandler;
 
 public class OSMagnetLinkHandler implements MagnetLinkHandler {
 	
 	private final URLProtocolHandler urlProtocolHandler;
 
 	public OSMagnetLinkHandler() {
 		urlProtocolHandler = new URLProtocolHandler();
 	}
 	
 	public void openURL(final String url) {
		urlProtocolHandler.open(url);
 	}
 
 }
