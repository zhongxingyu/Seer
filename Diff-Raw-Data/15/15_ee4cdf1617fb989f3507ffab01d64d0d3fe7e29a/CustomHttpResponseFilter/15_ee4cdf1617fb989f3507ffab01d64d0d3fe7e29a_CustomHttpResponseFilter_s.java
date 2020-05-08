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
		return 4096;
 	}
 }
