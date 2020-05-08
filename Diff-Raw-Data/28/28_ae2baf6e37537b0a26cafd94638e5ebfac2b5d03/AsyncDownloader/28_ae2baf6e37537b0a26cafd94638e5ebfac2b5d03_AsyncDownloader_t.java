 package org.getcomposer.packages;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.concurrent.FutureCallback;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
 import org.apache.http.nio.client.HttpAsyncClient;
 import org.apache.http.nio.conn.ClientAsyncConnectionManager;
 import org.apache.http.nio.conn.scheme.AsyncScheme;
 import org.apache.http.nio.conn.scheme.AsyncSchemeRegistry;
 import org.apache.http.nio.conn.ssl.SSLLayeringStrategy;
 import org.apache.http.nio.reactor.IOReactorException;
 
 public class AsyncDownloader extends AbstractDownloader {
 	
 	private List<HttpGet> httpGets;
 	private HttpAsyncClient client;
 	private int lastSlot;
 	
 	public AsyncDownloader() {
 		super();
 	}
 
 	public AsyncDownloader(String url) {
 		super(url);
 	}
 	
 	protected void init() {
 		super.init();
 		httpGets = new ArrayList<HttpGet>();
 		
 		// start http async client
 		try {
 			client = new DefaultHttpAsyncClient();
 			client.start();
 			HttpClientParams.setRedirecting(client.getParams(), false);
 		} catch (IOReactorException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Starts the async download. The returned number is the internal slot for this download transfer,
 	 * which can be used as parameter in abort to stop this specific transfer.
 	 * 
 	 * @return slot
 	 */
 	public int download() {
 		try {
 			//if (url.startsWith("https://packagist.org") || url.startsWith("https://getcomposer.org")) {
 			if (url.startsWith("https://")) {
 				registerSSLContext(client);
 			}
 			
 			final HttpGet httpGet = new HttpGet(url);
 			
 			
 			if (httpGet.isAborted()) {
				httpGet.abort();
 			}
 			
 			client.execute(httpGet, new FutureCallback<HttpResponse>() {
 				
 				@Override
 				public void failed(Exception e) {
 					for (DownloadListenerInterface listener : listeners) {
 						listener.errorOccured(e);
 					}
 				}
 
 				@Override
 				public void completed(HttpResponse response) {
 					for (DownloadListenerInterface listener : listeners) {
 						try {
 							listener.dataReceived(response.getEntity().getContent(), httpGet.getURI().toString());
 						} catch (Exception e) {
 							listener.errorOccured(e);
 						}
 					}
 				}
 
 				@Override
 				public void cancelled() {
 					for (DownloadListenerInterface listener : listeners) {
 						listener.aborted(httpGet.getURI().toString());
 					}
 				}
 			});
 			
 			httpGets.add(httpGet);
 //			client.shutdown();
 			
 			lastSlot = httpGets.size() - 1;
 						
 			return lastSlot;
 			
 		} catch (Exception e) {
 			for (DownloadListenerInterface listener : listeners) {
 				listener.errorOccured(e);
 			}
 		}
 		
 		return -1;
 	}
 
 	private void registerSSLContext(HttpAsyncClient client) throws Exception {	
 		X509TrustManager tm = new ComposerTrustManager();
 		SSLContext ctx = SSLContext.getInstance("TLS");
 		ctx.init(null,  new TrustManager[]{tm}, null);
 		SSLLayeringStrategy sls = new SSLLayeringStrategy(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 		ClientAsyncConnectionManager ccm = client.getConnectionManager();
 		AsyncSchemeRegistry sr = ccm.getSchemeRegistry();
 		sr.register(new AsyncScheme("https", 443, sls));
 	}
 	
 	/**
 	 * Aborts the last transfer
 	 */
 	public void abort() {
 		abort(lastSlot);
 	}
 
 	/**
 	 * Aborts a transfer at the given slot
 	 * 
 	 * @param slot
 	 */
 	public void abort(int slot) {
 		try {
 			httpGets.get(slot).abort();
 		} catch(Exception e) {
 //			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Shuts down the download client
 	 */
 	public void shutdown() {
 		try {
 			client.shutdown();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }
