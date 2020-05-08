 package networking;
 
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 
 /**
 * Custom HTTP filter.
 */
 public class CustomHttpResponseFilter implements HttpFilter {
 	public boolean filterResponses(final HttpRequest httpRequest) {
 		return true;
 	}
 
 	public HttpResponse filterResponse(final HttpRequest httpRequest, final HttpResponse response) {
 		return response;
 	}
 
 	public int getMaxResponseSize() {
		//Netty uses 1048576
		return 1024 * 200;
 	}
 }
