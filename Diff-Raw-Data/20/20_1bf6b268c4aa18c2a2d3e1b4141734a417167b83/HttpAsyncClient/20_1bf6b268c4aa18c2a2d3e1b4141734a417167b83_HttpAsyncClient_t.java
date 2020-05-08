 package org.getcomposer.httpclient;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.nio.reactor.IOReactorException;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 /**
  * Used as an adapter for HttpAsyncClient until it becomes available
  * as a stable version in eclipse orbit updatesite. 
  * 
  * @see http://hc.apache.org/httpcomponents-asyncclient-dev/httpasyncclient/apidocs/overview-summary.html
  * @author Robert Gruendler <r.gruendler@gmail.com>
  */
 public class HttpAsyncClient implements AsyncClientInterface {
 
 	private HttpClient client;
 	private HttpParams params;
 	private Log log = LogFactory.getLog(HttpAsyncClient.class);
	private GetThread thread;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.getcomposer.packages.AsyncClientInterface#start()
 	 */
 	@Override
 	public void start() throws IOReactorException {
 		client = new DefaultHttpClient();
 		params = new BasicHttpParams();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.getcomposer.packages.AsyncClientInterface#getParams()
 	 */
 	@Override
 	public HttpParams getParams() {
 		return params;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.getcomposer.packages.AsyncClientInterface#execute(org.apache.http
 	 * .client.methods.HttpGet, org.apache.http.concurrent.FutureCallback)
 	 */
 	@Override
 	public void execute(HttpGet httpGet, FutureCallback<HttpResponse> futureCallback) {
 
 		try {
 			if (httpGet == null) {
 				throw new RuntimeException("Cannot issue GET request with HttpGet object");
 			}
 			log.info("Executing GET request to " + httpGet.getURI().toString());
			thread = new GetThread(client, httpGet, futureCallback);
 			thread.start();
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			futureCallback.failed(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.getcomposer.packages.AsyncClientInterface#shutdown()
 	 */
 	@Override
 	public void shutdown() throws InterruptedException {
 		if(client != null) {
 			client.getConnectionManager().shutdown();
 		}
 	}
 
 	static class GetThread extends Thread {
 
 		private final HttpClient httpClient;
 		private final HttpContext context;
 		private final HttpGet httpget;
 		private FutureCallback<HttpResponse> callback;
 
 		public GetThread(HttpClient httpClient, HttpGet httpget, FutureCallback<HttpResponse> futureCallback) {
 			this.httpClient = httpClient;
 			this.callback = futureCallback;
 			this.context = new BasicHttpContext();
 			this.httpget = httpget;
 		}
 		
 		@Override
 		public void interrupt() {
 			super.interrupt();
 			if (callback != null) {
 				callback.cancelled();
 			}
 		}
 
 		@Override
 		public void run() {
 			try {
 				HttpResponse response = httpClient.execute(httpget, context);
 				if (callback != null) {
 					callback.completed(response);
 				}
 			} catch (Exception e) {
 				if (callback != null) {
 					callback.failed(e);
 				}
 				httpget.abort();
 			}
 		}
 	}
 
 	@Override
 	public HttpClient getBackend() {
 		return client;
 	}
 }
