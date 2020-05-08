 /**
  * @author William Speirs <bill.speirs@gmail.com>
  */
 package com.bittrust.http.server;
 
 import java.io.IOException;
 import java.net.Socket;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponseFactory;
 import org.apache.http.impl.DefaultConnectionReuseStrategy;
 import org.apache.http.impl.DefaultHttpResponseFactory;
 import org.apache.http.impl.DefaultHttpServerConnection;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.BasicHttpProcessor;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpProcessor;
 import org.apache.http.protocol.HttpRequestHandlerResolver;
 import org.apache.http.protocol.HttpService;
 
 /**
  * @class RequestWorker
  * 
  * Process a request from the server.
  */
 public class RequestWorker extends Thread{
 	private DefaultHttpServerConnection connection;
 	private HttpParams params;
 	private HttpRequestHandlerResolver resolver;
 	private HttpProcessor processor;
 	private HttpResponseFactory responseFactory;
 	private HttpContext context;
 	
 	/**
 	 * Setup the worker.
 	 * @param socket The socket to read/write from/to
 	 * @param resolver The resolvers for the URLs
 	 */
 	public RequestWorker(Socket socket, HttpRequestHandlerResolver resolver) {
 		this.resolver = resolver;
 		this.params = new BasicHttpParams();
 		this.connection = new DefaultHttpServerConnection();
 		this.processor = new BasicHttpProcessor();
 		this.responseFactory = new DefaultHttpResponseFactory();
 		this.context = new BasicHttpContext();
 		
 		try { connection.bind(socket, params); }
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Simply handle the request as described by the proper handler.
 	 */
 	public void run() {
 		HttpService httpService = new HttpService(this.processor,
 												  new DefaultConnectionReuseStrategy(),
 												  this.responseFactory,
 												  this.resolver,
 												  this.params);
 
 		while(connection.isOpen()) {
 			try {
 				// set the remote address in the context so we can audit it later
				context.setAttribute("REMOTE_ADDR", connection.getRemoteAddress());
 				
 				httpService.handleRequest(connection, context);
 			} catch (IOException e) {
 				//e.printStackTrace();
 			} catch (HttpException e) {
 				System.err.println("HTTP EXCEPTION: " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
