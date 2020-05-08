 package a2;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.ProtocolException;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.RedirectStrategy;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.HttpClientContext;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.BasicAuthCache;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectStrategy;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.protocol.HttpContext;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 public class GithubRequest implements Closeable{
 	private CloseableHttpClient httpclient = null;
     private HttpEntity entity1 = null;
 	private CloseableHttpResponse response1 = null;
 	private InputStream content= null;
 	private long wait = 0;
 	public int status = 200;
 	
 	public GithubRequest(String url) throws IllegalStateException, IOException{
 		while(true){
 		Logger httplogger = Logger.getLogger("org.apache.http.wire");
 		httplogger.setLevel(Level.DEBUG);
 		HttpHost targetHost = new HttpHost("api.github.com", 443, "https");
		UsernamePasswordCredentials cred = new UsernamePasswordCredentials("logangilmour","!snTH3q77X");
 		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
 		credentialsProvider.setCredentials(new AuthScope("api.github.com",443), cred);
 		
 		
 		
 		
 		AuthCache authCache = new BasicAuthCache();
         // Generate BASIC scheme object and add it to the local
         // auth cache
         BasicScheme basicAuth = new BasicScheme();
         authCache.put(targetHost, basicAuth);
         
         HttpClientContext localContext = HttpClientContext.create();
 		localContext.setCredentialsProvider(credentialsProvider);
 		localContext.setAuthCache(authCache);
 		
 		httpclient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).setRedirectStrategy(new DefaultRedirectStrategy(){
 
 			@Override
 			public HttpUriRequest getRedirect(HttpRequest arg0,
 					HttpResponse arg1, HttpContext arg2)
 					throws ProtocolException {
 				// TODO Auto-generated method stub
 				HttpUriRequest req = super.getRedirect(arg0, arg1, arg2);
 				System.out.println("Rate limit: "+arg1.getFirstHeader("X-RateLimit-Limit"));
 				System.out.println("Remaining: "+arg1.getFirstHeader("X-RateLimit-Remaining"));
 				
 				return req;
 			}
 			
 		}).build();
 		
 
 		
 		HttpGet httpGet = new HttpGet(url);
 	
 		response1 = httpclient.execute(httpGet,localContext);
 		entity1 = response1.getEntity();
 		// The underlying HTTP connection is still held by the response object
 		// to allow the response content to be streamed directly from the network socket.
 		// In order to ensure correct deallocation of system resources
 		// the user MUST either fully consume the response content  or abort request
 		// execution by calling CloseableHttpResponse#close().
 		status = response1.getStatusLine().getStatusCode();
 		long reset = -1;
 		long remaining = -1;
 		long until = 0;
 		try{
 			remaining = Long.parseLong(response1.getFirstHeader("X-RateLimit-Remaining").getValue());
 			reset = Long.parseLong(response1.getFirstHeader("X-RateLimit-Reset").getValue());
 			until = reset-System.currentTimeMillis()/1000;
 			System.out.println("Minutes until next reset: "+until/60);
 		}catch(NumberFormatException e){
 			
 		}catch(NullPointerException e){
 			
 		}
 		if(status==403 && remaining== 0){
 			try {
 				Thread.sleep(1000*(until+1));
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace(System.err);
 				
 				break;
 			}
 		}else{
 			break;
 		}
 		}
 		this.content = entity1.getContent();
 	}
 	
 	public InputStream getInputStream(){
 		return content;
 	}
 
 	@Override
 	public void close() throws IOException {
 		IOUtils.closeQuietly(response1);
 		IOUtils.closeQuietly(httpclient);
 	}
 }
