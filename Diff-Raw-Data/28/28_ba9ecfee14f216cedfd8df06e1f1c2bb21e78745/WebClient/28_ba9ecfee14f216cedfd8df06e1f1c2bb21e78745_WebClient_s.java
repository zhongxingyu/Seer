 package org.tsaikd.java.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.HttpHost;
 import org.apache.http.client.config.RequestConfig;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.conn.ssl.TrustStrategy;
 import org.apache.http.cookie.Cookie;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.apache.http.protocol.HttpContext;
 
 public class WebClient {
 
 	static Log log = LogFactory.getLog(WebClient.class);
 
 	static class WebRetryHandler extends DefaultHttpRequestRetryHandler {
 		@Override
 		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
 			boolean doRetry = super.retryRequest(exception, executionCount, context);
 			if (doRetry) {
 				try {
 					Thread.sleep(3000);
 				} catch (InterruptedException e) {
 					return false;
 				}
 			}
 			return doRetry;
 		}
 	}
 
 	static class WebSSLSocketFactory extends SSLSocketFactory {
		public WebSSLSocketFactory() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
			super(new TrustStrategy() {
				@Override
				public boolean isTrusted(X509Certificate[] arg0, String arg1)
						throws CertificateException {
					return true;
				}}, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 		}
 	}
 
 	static class WebCookieStore extends BasicCookieStore {
 
 		private static final long serialVersionUID = 1L;
 		private File cookieFile;
 
 		public WebCookieStore() {}
 
 		public WebCookieStore(File cookieFile) {
 			this.cookieFile = cookieFile;
 			try {
 				loadCookie();
 			} catch (ClassNotFoundException | IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public WebCookieStore loadCookie(File cookieFile) throws IOException, ClassNotFoundException {
 			if (cookieFile != null && cookieFile.exists()) {
 				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cookieFile));
 				@SuppressWarnings("unchecked")
 				List<Cookie> cookies = (List<Cookie>) ois.readObject();
 				for (Cookie c : cookies) {
 					addCookie(c);
 				}
 				ois.close();
 			}
 			return this;
 		}
 
 		public WebCookieStore loadCookie() throws IOException, ClassNotFoundException {
 			return loadCookie(cookieFile);
 		}
 
 		public WebCookieStore saveCookie(File cookieFile) throws IOException {
 			if (cookieFile != null) {
 				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cookieFile));
 				oos.writeObject(getCookies());
 				oos.close();
 			}
 			return this;
 		}
 
 		public WebCookieStore saveCookie() throws IOException {
 			return saveCookie(cookieFile);
 		}
 
 		public synchronized void addCookie(final Cookie cookie) {
 			super.addCookie(cookie);
 			try {
 				saveCookie();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	protected static HttpHost defaultProxy;
 	public static HttpHost getDefaultProxy() {
 		if (defaultProxy == null) {
 			String proxyHost;
 			int proxyPort;
 
 			proxyHost = ConfigUtils.get("proxyHost", null);
 			if (proxyHost != null) {
 				proxyPort = ConfigUtils.getInt("proxyPort", 0);
 				if (proxyPort != 0) {
 					defaultProxy = new HttpHost(proxyHost, proxyPort);
 					return defaultProxy;
 				}
 			}
 
 			proxyHost = ConfigUtils.get("http.proxyHost", null);
 			if (proxyHost != null) {
 				proxyPort = ConfigUtils.getInt("http.proxyPort", 0);
 				if (proxyPort != 0) {
 					defaultProxy = new HttpHost(proxyHost, proxyPort);
 					return defaultProxy;
 				}
 			}
 		}
 		return defaultProxy;
 	}
 
 	public static CloseableHttpClient newHttpClient(File cookieFile, boolean noProxy) {
 		HttpClientBuilder builder = HttpClientBuilder.create();
 
 		// retry
 		builder.setRetryHandler(new WebRetryHandler());
 
 		// https
 		try {
 			builder.setSSLSocketFactory(new WebSSLSocketFactory());
		} catch (KeyManagementException | UnrecoverableKeyException
				| NoSuchAlgorithmException | KeyStoreException e) {
 			e.printStackTrace();
 		}
 
 		// cookie
 		builder.setDefaultCookieStore(new WebCookieStore(cookieFile));
 
 		// proxy
 		if (!noProxy) {
 			HttpHost proxy = getDefaultProxy();
 			if (proxy == null) {
 				builder.useSystemProperties();
 			} else {
 				builder.setProxy(proxy);
 			}
 		}
 
 		// maybe should enable
 		if (!WebClient.class.getSimpleName().equals("WebClient")) {
 			builder.setDefaultRequestConfig(RequestConfig.custom().setExpectContinueEnabled(false).build());
 		}
 
 		return builder.build();
 	}
 
 	public static CloseableHttpClient newHttpClient() {
 		return newHttpClient(null, false);
 	}
 
 }
