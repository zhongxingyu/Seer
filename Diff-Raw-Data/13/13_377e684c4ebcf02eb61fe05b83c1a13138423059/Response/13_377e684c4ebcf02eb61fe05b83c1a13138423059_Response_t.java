 package de.uxnr.proxy;
 
 import java.util.List;
 import java.util.Map;
import java.util.Map.Entry;
 
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
		// The implementation of putAll would not convert the strings into the correct case
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			String header = entry.getKey();
			for (String prop : entry.getValue()) {
				this.responseHeaders.add(header, prop);
			}
		}
 	}
 }
