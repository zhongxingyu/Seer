 package de.uxnr.proxy;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 
 @SuppressWarnings("restriction")
 public class HostResponse {
 	private HttpExchange httpExchange;
 	private HttpURLConnection connection;
 	private HostHandler hostHandler;
 	
 	protected HostResponse(HttpExchange httpExchange, HttpURLConnection connection, HostHandler hostHandler) {
 		this.httpExchange = httpExchange;
 		this.connection = connection;
 		this.hostHandler = hostHandler;
 	}
 	
 	public void process() throws IOException {
 		String requestMethod = this.httpExchange.getRequestMethod();
 		URI requestURI = this.httpExchange.getRequestURI();
 		Headers requestHeaders = this.httpExchange.getRequestHeaders();
 		Headers responseHeaders = this.httpExchange.getResponseHeaders();
 		
 		Map<String, List<String>> remoteHeaders = this.connection.getHeaderFields();
 		
 		for (String header : remoteHeaders.keySet()) {
 			if (header != null) {
 				responseHeaders.set(header, remoteHeaders.get(header).get(0));
 			}
 		}
 		
 		int responseCode = this.connection.getResponseCode();
 		long contentLength = this.connection.getContentLength();
 		
 		if (contentLength < 0)
 			contentLength = 0;
 		else if (contentLength == 0)
 			contentLength = -1;
 		
 		this.httpExchange.sendResponseHeaders(responseCode, contentLength);
 		
 		InputStream remoteInput = this.connection.getInputStream();
 		OutputStream localOutput = this.httpExchange.getResponseBody();
 		ByteArrayOutputStream bufferOutput = new ByteArrayOutputStream();
 		
 		int size = Math.max(Math.min(remoteInput.available(), 65536), 1024);
 		int length = -1;
 		
 		byte[] data = new byte[size];
 		while ((length = remoteInput.read(data)) != -1) {
 			localOutput.write(data, 0, length);
 			bufferOutput.write(data, 0, length);
 		}
 		
 		remoteInput.close();
 		localOutput.close();
 		bufferOutput.close();
 		
 		if (this.hostHandler != null) {
 			ByteArrayInputStream body = new ByteArrayInputStream(bufferOutput.toByteArray());
 			Map<String, String> headers = new HashMap<String, String>();
 			for (String header : requestHeaders.keySet()) {
 				if (header != null) {
 					headers.put(header.toLowerCase(), requestHeaders.getFirst(header));
 				}
 			}
 			Map<String, String> response = new HashMap<String, String>();
 			for (String header : remoteHeaders.keySet()) {
 				if (header != null) {
					response.put(header.toLowerCase(), remoteHeaders.get(header).get(0));
 				}
 			}
 			this.hostHandler.handleResponse(requestMethod, requestURI, headers, response, body);
 		}
 	}
 }
