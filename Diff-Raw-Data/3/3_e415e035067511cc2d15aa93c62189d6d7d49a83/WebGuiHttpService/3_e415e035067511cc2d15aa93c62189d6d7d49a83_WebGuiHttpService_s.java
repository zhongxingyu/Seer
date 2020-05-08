 package il.technion.ewolf.server;
 
 import java.io.IOException;
 
 import org.apache.http.ConnectionReuseStrategy;
 import org.apache.http.Header;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseFactory;
 import org.apache.http.HttpStatus;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpExpectationVerifier;
 import org.apache.http.protocol.HttpProcessor;
 import org.apache.http.protocol.HttpRequestHandlerResolver;
 import org.apache.http.protocol.HttpService;
 
 import com.google.inject.Inject;
 
 public class WebGuiHttpService extends HttpService {
 	HttpSessionStore sessionStore;
 
 	@Inject
 	public WebGuiHttpService(HttpProcessor processor,
 			ConnectionReuseStrategy connStrategy,
 			HttpResponseFactory responseFactory,
 			HttpRequestHandlerResolver handlerResolver, HttpParams params,
 			HttpSessionStore sessionStore) {
 		super(processor, connStrategy, responseFactory, handlerResolver, params);
 		this.sessionStore = sessionStore;
 	}
 
 	@Inject
 	public WebGuiHttpService(HttpProcessor processor,
 			ConnectionReuseStrategy connStrategy,
 			HttpResponseFactory responseFactory,
 			HttpRequestHandlerResolver handlerResolver,
 			HttpExpectationVerifier expectationVerifier, HttpParams params,
 			HttpSessionStore sessionStore) {
 		super(processor, connStrategy, responseFactory, handlerResolver,
 				expectationVerifier, params);
 		this.sessionStore = sessionStore;
 	}
 
 	@Override
 	protected void doService(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		boolean authorized = false;
 		if (req.containsHeader("Cookie")) {
 			Header[] headers = req.getHeaders("Cookie");
 			for (Header h : headers) {
 				String cookie = h.getValue();
				if (sessionStore.isValid(cookie)) {
 					authorized = true;
 					context.setAttribute("authorized", true);
 					break;
 				}
 			}
 		}
 		
 		if (!authorized) {
 			String uri = req.getRequestLine().getUri();
 			if (uri.equals("/sfs") || uri.equals("/sfsupload")) {
 				res.setStatusCode(HttpStatus.SC_FORBIDDEN);
 				return;
 			} else {
 				context.setAttribute("authorized", false);
 			}
 		}
 		super.doService(req, res, context);
 	}
 }
