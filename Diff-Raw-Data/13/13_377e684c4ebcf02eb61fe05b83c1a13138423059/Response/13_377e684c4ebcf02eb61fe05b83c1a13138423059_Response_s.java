 package de.uxnr.proxy;
 
 import java.util.List;
 import java.util.Map;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 
 @SuppressWarnings("restriction")
 public class Response extends Request {
 	protected Headers responseHeaders;
 
 	protected Response(HttpExchange httpExchange) {
 		super(httpExchange);
 		this.responseHeaders = httpExchange.getResponseHeaders();
 	}
	
 	protected void populate(Map<String, List<String>> headers) {
		this.responseHeaders.putAll(headers);
 	}
 }
