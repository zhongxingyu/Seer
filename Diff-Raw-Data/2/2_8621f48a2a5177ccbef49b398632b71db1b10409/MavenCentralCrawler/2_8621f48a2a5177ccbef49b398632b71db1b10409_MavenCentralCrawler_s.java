 package ie.dcu.collir24;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.MBeanRegistrationException;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.GzipDecompressingEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 
 public class MavenCentralCrawler {
 	protected static final String MAVEN_REPO_BASE = "http://repo1.maven.org/maven2/";
 	private HttpClient httpClient;
	private final ExecutorService exec = Executors.newFixedThreadPool(1);
 
 	public MavenCentralCrawler() {
 		File downloadDir = new File("maven2");
 		if (!downloadDir.exists()) {
 			downloadDir.mkdir();
 		}
 		System.setProperty("download.dir", downloadDir.getAbsolutePath()
 				+ File.separator);
 		final HttpParams httpParams = new BasicHttpParams();
 	    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
 		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
 		cm.setDefaultMaxPerRoute(6);// increase from the default of 2
 		httpClient = gzipClient(new DefaultHttpClient(cm));
 	}
 
 	private static DefaultHttpClient gzipClient(
 			final DefaultHttpClient httpClient) {
 		httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
 
 			public void process(final HttpRequest request,
 					final HttpContext context) throws HttpException,
 					IOException {
 				if (!request.containsHeader("Accept-Encoding")) {
 					request.addHeader("Accept-Encoding", "gzip");
 				}
 			}
 
 		});
 
 		httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
 
 			public void process(final HttpResponse response,
 					final HttpContext context) throws HttpException,
 					IOException {
 				HttpEntity entity = response.getEntity();
 				if (entity != null) {
 					Header ceheader = entity.getContentEncoding();
 					if (ceheader != null) {
 						HeaderElement[] codecs = ceheader.getElements();
 						for (int i = 0; i < codecs.length; i++) {
 							if (codecs[i].getName().equalsIgnoreCase("gzip")) {
 								response.setEntity(new GzipDecompressingEntity(
 										response.getEntity()));
 								return;
 							}
 						}
 					}
 				}
 			}
 
 		});
 		return httpClient;
 	}
 
 	/**
 	 * @param args
 	 * @throws NullPointerException
 	 * @throws MalformedObjectNameException
 	 * @throws NotCompliantMBeanException
 	 * @throws MBeanRegistrationException
 	 * @throws InstanceAlreadyExistsException
 	 */
 	public static void main(String[] args) {
 		MavenCentralCrawler crawler = new MavenCentralCrawler();
 		crawler.crawlCentral();
 	}
 
 	private List<String> initPaths() {
 		List<String> paths = new ArrayList<String>(2378);
 		BufferedReader r = null;
 		try {
 			r = new BufferedReader(new InputStreamReader(
 					MavenCentralCrawler.class
 							.getResourceAsStream("MavenRoots.txt")));
 			while (r.ready()) {
 				paths.add(r.readLine());
 			}
 			return paths;
 		} catch (IOException e) {
 			throw new RuntimeException(
 					"Couldn't initialize list of Maven Root Paths.", e);
 		} finally {
 			if (r != null) {
 				try {
 					r.close();
 				} catch (IOException e) {
 					throw new RuntimeException(
 							"Couldn't initialize list of Maven Root Paths.", e);
 				}
 			}
 		}
 	}
 
 	private void crawlCentral() {
 		List<String> paths = initPaths();
 		for (String path : paths) {
 			exec.submit(new LinkTask(MAVEN_REPO_BASE + path, httpClient, exec));
 		}
 	}
 
 	public int getQueueSize() {
 		return ((ThreadPoolExecutor) exec).getQueue().size();
 	}
 
 	public void shutdownExecAndCleanup() {
 		try {
 			exec.shutdown();
 			exec.awaitTermination(30, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			Thread.interrupted();
 		} finally {
 			exec.shutdownNow();
 			httpClient.getConnectionManager().shutdown();
 		}
 	}
 }
