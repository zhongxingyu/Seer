 package org.getcomposer.packages;
 
 import java.io.InputStream;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class Downloader extends AbstractDownloader {
 	
 	private HttpGet httpGet;
 	
 	public Downloader() {
 		super();
 	}
 	
 	public Downloader(String url) {
 		super(url);
 	}
 
 	public InputStream download() {
 		HttpClient client = new DefaultHttpClient();
 		InputStream content = null;
 
 		try {
 
 			//if (url.startsWith("https://packagist.org") || url.startsWith("https://getcomposer.org")) {
 			if (url.startsWith("https://")) {
 				registerSSLContext(client);
 			}
 	
 			httpGet = new HttpGet(url);
 			HttpResponse response = null;
 	
 			if (httpGet.isAborted()) {
				httpGet.reset();
 				
 				if (httpGet.isAborted()) {
 					return null;
 				}
 			}
 	
 			response = client.execute(httpGet);
 	
 			if (httpGet.isAborted()) {
 				for (DownloadListenerInterface listener : listeners) {
 					listener.aborted(url);
 				}
 				return null;
 			}
 	
 			HttpEntity entity = response.getEntity();
 			content = entity.getContent();
 	
 			for (DownloadListenerInterface listener : listeners) {
 				listener.dataReceived(content, url);
 			}
 	
 		} catch (Exception e) {
 			for (DownloadListenerInterface listener : listeners) {
 				listener.errorOccured(e);
 			}
 		}
 		return content;
 	}
 	
 	private void registerSSLContext(HttpClient client) throws Exception {	
 		X509TrustManager tm = new ComposerTrustManager();
 		SSLContext ctx = SSLContext.getInstance("TLS");
 		ctx.init(null, new TrustManager[]{tm}, null);
 		SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 		ClientConnectionManager ccm = client.getConnectionManager();
 		SchemeRegistry sr = ccm.getSchemeRegistry();
 		sr.register(new Scheme("https", 443, ssf));
 	}
 
 	public void abort() {
 		if (httpGet != null) {
 			httpGet.abort();
 		}
 	}
 }
