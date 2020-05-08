 
 package org.paxle.crawler.http.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.NoRouteToHostException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipInputStream;
 
 import org.apache.commons.httpclient.CircularRedirectException;
 import org.apache.commons.httpclient.ConnectTimeoutException;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.ProxyHost;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.apache.commons.httpclient.util.DateParseException;
 import org.apache.commons.httpclient.util.DateUtil;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.ManagedService;
 import org.paxle.core.doc.CrawlerDocument;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.core.prefs.Properties;
 import org.paxle.crawler.CrawlerContext;
 import org.paxle.crawler.CrawlerTools;
 import org.paxle.crawler.ISubCrawler;
 import org.paxle.crawler.http.IHttpCrawler;
 
 /**
  * TODO: javadoc
  */
 public class HttpCrawler implements IHttpCrawler, ManagedService {
 	/* =========================================================
 	 * Config Properties
 	 * ========================================================= */
 	private static final String PROP_CONNECTION_TIMEOUT = "connectionTimeout";
 	private static final String PROP_SOCKET_TIMEOUT = "socketTimeout";
 	private static final String PROP_MAXCONNECTIONS_PER_HOST = "maxConnectionsPerHost";
 	private static final String PROP_MAXDOWNLOAD_SIZE = "maxDownloadSize";
 	private static final String PROP_ACCEPT_ENCODING = "acceptEncodings";
 	private static final String PROP_TRANSFER_LIMIT = "transferLimit";			// in KB/s
 	
 	private static final String PROP_PROXY_USE = "useProxy";
 	private static final String PROP_PROXY_HOST = "proxyHost";
 	private static final String PROP_PROXY_PORT = "proxyPort";
 	private static final String PROP_PROXY_USER = "proxyUser";
 	private static final String PROP_PROXY_PASSWORD = "proxyPassword";
 	
 	private static final String HTTPHEADER_ETAG = "ETag";
 	private static final String HTTPHEADER_LAST_MODIFIED = "Last-Modified";
 	private static final String HTTPHEADER_DATE = "Date";
 	private static final String HTTPHEADER_CONTENT_LANGUAGE = "Content-Language";
 	private static final String HTTPHEADER_CONTENT_TYPE = "Content-Type";
 	private static final String HTTPHEADER_CONTENT_LENGTH = "Content-Length";
 	private static final String HTTPHEADER_CONTENT_ENCODING = "Content-Encoding";
 	private static final String HTTPHEADER_ACCEPT_ENCODING = "Accept-Encoding";
 	
 	private static final int PREF_NO_ENCODING = 1;
 	
 	/**
 	 * The MIME-type detection of some servers is not that mature, i.e. Apache oftenly tends to
 	 * report 'text/plain' for binary files or 'application/x-tar' for compressed tar-archives,
 	 * which do not help us at all. This set contains MIME-types, known to be reported erroneously
 	 * by servers in general.
 	 * Of course this list would be divisible further by extending it into a per-server map, but
 	 * our means to determine the type (and possibly version) of the servers here are limited, so
 	 * this shall suffice for now.
 	 */
 	private static final HashSet<String> ERRONEOUS_MIME_TYPES = new HashSet<String>(Arrays.asList(
 			"text/plain",
 			"application/x-tar"));
 	
 	/**
 	 * The protocol supported by this crawler
 	 */
 	public static final String[] PROTOCOLS = new String[]{"http","https"};
 	
 	/**
 	 * Connection manager used for http connection pooling
 	 */
 	private MultiThreadedHttpConnectionManager connectionManager = null;
 
 	/**
 	 * http client class
 	 */
 	private HttpClient httpClient = null;
 	
 	/**
 	 * Logger class
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * The maximum size of a file. If set to <code>-1</code>, all files are fetched, otherwise
 	 * (if the server provides the file-size) only files with a size equal to or less than this value.
 	 */
 	private int maxDownloadSize = -1;
 	
 	private boolean acceptEncoding = true;
 	private CrawlerTools.LimitedRateCopier lrc = null;
 	
 	private Properties props = null;
 	private final ConcurrentHashMap<String,Integer> hostSettings;
 	
 	public HttpCrawler(final Properties props) {
 		// init with default configuration
 		this.updated(this.getDefaults());
 		
 		this.props = props;
 		if (props != null) {
 			final Set<Object> keySet = props.keySet();
 			hostSettings = new ConcurrentHashMap<String,Integer>(keySet.size(), 0.75f, 10);
 			for (final Object o : keySet) {
 				final String key = (String)o;
 				hostSettings.put(key, Integer.valueOf(props.getProperty(key)));
 			}
 		} else {
 			hostSettings = new ConcurrentHashMap<String,Integer>(10, 0.75f, 10);
 		}
 	}
 	
 	/**
 	 * for testing purpose only! 
 	 * @param httpClient
 	 */
 	HttpCrawler(HttpClient httpClient) {
 		this.httpClient = httpClient;
 		hostSettings = new ConcurrentHashMap<String,Integer>();
 	}
 	
 	public void saveProperties() {
 		if (props != null) {
 			for (Map.Entry<String,Integer> e : hostSettings.entrySet())
 				props.setProperty(e.getKey(), Integer.toString(e.getValue().intValue()));
 		}
 	}
 	
 	private boolean isHostSettingSet(final String host, final int pref) {
 		final Integer i = hostSettings.get(host);
 		if (i == null)
 			return false;
 		return (i.intValue() & pref) != 0;
 	}
 	
 	private void setHostSetting(final String host, final int pref) {
 		final Integer i = hostSettings.get(host);
 		final int val = (i == null) ? pref : (i.intValue() | pref);
 		hostSettings.put(host, Integer.valueOf(val));
 	}
 	
 	public void cleanup() {
 		// cleanup old settings
 		if (this.connectionManager != null) {
 			this.connectionManager.shutdown();
 			this.connectionManager = null;
 			this.httpClient = null;
 		}
 	}
 	
 	/**
 	 * @return the default configuration of this service
 	 */
 	public Hashtable<String,Object> getDefaults() {
 		Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		
 		defaults.put(PROP_CONNECTION_TIMEOUT, Integer.valueOf(15000));
 		defaults.put(PROP_SOCKET_TIMEOUT, Integer.valueOf(15000));
 		defaults.put(PROP_MAXCONNECTIONS_PER_HOST, Integer.valueOf(10));
 		defaults.put(PROP_MAXDOWNLOAD_SIZE, Integer.valueOf(-1));
 		defaults.put(PROP_ACCEPT_ENCODING, Boolean.TRUE);
 		defaults.put(PROP_TRANSFER_LIMIT, Integer.valueOf(-1));
 		
 		defaults.put(PROP_PROXY_USE, Boolean.FALSE);
 		defaults.put(PROP_PROXY_HOST, "");
 		defaults.put(PROP_PROXY_PORT, Integer.valueOf(3128));		// default squid port
 		defaults.put(PROP_PROXY_USER, "");
 		defaults.put(PROP_PROXY_PASSWORD, "");
 		
 		defaults.put(Constants.SERVICE_PID, IHttpCrawler.class.getName());
 		
 		return defaults;
 	}
 	
 	/**
 	 * @see ManagedService#updated(Dictionary)
 	 */
 	@SuppressWarnings("unchecked")		// we're only implementing an interface
 	public synchronized void updated(Dictionary configuration) {
 		// our caller catches all runtime-exceptions and silently ignores them, leaving us confused behind,
 		// so this try/catch-block exists for debugging purposes
 		try {
 			if ( configuration == null ) {
 				logger.warn("updated configuration is null");
 				/*
 				 * Generate default configuration
 				 */
 				configuration = this.getDefaults();
 			}
 			
 			/*
 			 * Cleanup old config
 			 */
 			this.cleanup();
 			
 			/*
 			 * Init with changed configuration
 			 */
 			this.connectionManager = new MultiThreadedHttpConnectionManager();
 			
 			// configure connections per host
 			this.connectionManager.getParams().setDefaultMaxConnectionsPerHost(((Integer) configuration.get(PROP_MAXCONNECTIONS_PER_HOST)).intValue());
 			
 			// configuring timeouts
 			this.connectionManager.getParams().setConnectionTimeout(((Integer) configuration.get(PROP_CONNECTION_TIMEOUT)).intValue());
 			this.connectionManager.getParams().setSoTimeout(((Integer) configuration.get(PROP_SOCKET_TIMEOUT)).intValue());
 			
 			// set new http client
 			this.httpClient = new HttpClient(connectionManager);
 			
 			this.maxDownloadSize = ((Integer)configuration.get(PROP_MAXDOWNLOAD_SIZE)).intValue();
 			this.acceptEncoding = ((Boolean)configuration.get(PROP_ACCEPT_ENCODING)).booleanValue();
 			int limitKBps = ((Integer)configuration.get(PROP_TRANSFER_LIMIT)).intValue();
 			logger.debug("transfer rate limit: " + limitKBps + " kb/s");
			lrc = (limitKBps > 0) ? new CrawlerTools.LimitedRateCopier(limitKBps) : null;
 			
 			// proxy configuration
 			final boolean useProxy = ((Boolean)configuration.get(PROP_PROXY_USE)).booleanValue();
 			final String host = (String)configuration.get(PROP_PROXY_HOST);
 			final int port = ((Integer)configuration.get(PROP_PROXY_PORT)).intValue();
 			if (useProxy && host.length() > 0) {
 				logger.info("Proxy is enabled");
 				final ProxyHost proxyHost = new ProxyHost(host, port);
 				httpClient.getHostConfiguration().setProxyHost(proxyHost);
 				
 				final String user = (String)configuration.get(PROP_PROXY_HOST);
 				final String pwd = (String)configuration.get(PROP_PROXY_PASSWORD);
 				if (user.length() > 0 && pwd.length() > 0)
 					httpClient.getState().setProxyCredentials(
 							new AuthScope(host, port),
 							new UsernamePasswordCredentials(user, pwd));
 			} else {
 				logger.info("Proxy is disabled");
 				httpClient.getHostConfiguration().setProxyHost(null);
 				httpClient.getState().clearCredentials();
 			}
 		} catch (Throwable e) {
 			logger.error("Internal exception during configuring", e);
 		}
 	}
 	
 	/**
 	 * @see ISubCrawler#getProtocols()
 	 */
 	public String[] getProtocols() {
 		return HttpCrawler.PROTOCOLS;
 	}	
 	
 	/**
 	 * This method is synchronized with {@link #updated(Dictionary)} to avoid
 	 * problems during configuration update.
 	 * 
 	 * @return the {@link HttpClient} to use
 	 */
 	private synchronized HttpClient getHttpClient() {
 		return this.httpClient;
 	}
 	
 	/**
 	 * Initializes the {@link HttpMethod} with common attributes for all requests this crawler
 	 * initiates.
 	 * <p>
 	 * Currently the following attributes (represented as HTTP header values in the final request)
 	 * are set:
 	 * <ul>
 	 *   <li>cookies shall be rejected ({@link CookiePolicy#IGNORE_COOKIES})</li>
 	 *   <li>
 	 *     if enabled, content-transformation using <code>compress</code>, <code>gzip</code> and
 	 *     <code>deflate</code> is supported</li>
 	 *   </li> 
 	 * </ul>
 	 * 
 	 * @param method the method to set the standard attributes on
 	 */
 	private void initRequestMethod(final HttpMethod method) throws URIException {
 		method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
 		if (acceptEncoding && !isHostSettingSet(method.getURI().getHost(), PREF_NO_ENCODING))
 			method.setRequestHeader(HTTPHEADER_ACCEPT_ENCODING, "compress, gzip, identity, deflate");	// see RFC 2616, section 14.3
 		
 		// TODO: set some additional http headers
 		//method.setRequestHeader("User-Agent","xxx");
 	}
 	
 	/**
 	 * This method handles the <code>Content-Type</code> {@link Header} of a HTTP-request.
 	 * If available, the header is used to set the MIME-type of the {@link ICrawlerDocument}
 	 * as well as the character set. The return value determines whether processing of the
 	 * URL shall be continued or not
 	 * <p>
 	 * In case of a positive result, the <code>doc</code>'s MIME-type and - if available -
 	 * charset will be set. Otherwise it's <code>result</code> will be set to
 	 * {@link ICrawlerDocument.Status#UNKNOWN_FAILURE} and a warning message will be logged.
 	 * 
 	 * @see #HTTPHEADER_CONTENT_TYPE
 	 * @param contentTypeHeader the HTTP-{@link Header}'s <code>Content-Type</code> attribute
 	 * @param doc the {@link CrawlerDocument} the resulting MIME-type and charset shall be set in
 	 * @return <code>true</code> if proceeding with the URL may continue or <code>false</code> if
 	 *         it shall be aborted due to an unsupported MIME-type of the requested document
 	 */
 	private boolean handleContentTypeHeader(final Header contentTypeHeader, final CrawlerDocument doc) {
 		if (contentTypeHeader == null)
 			// might be ok, might be not, we don't know yet
 			return true;
 		
 		// separate MIME-type and charset from the content-type specification
 		String contentMimeType = null;
 		String contentCharset = null;
 		contentMimeType = contentTypeHeader.getValue();
 		
 		int idx = contentMimeType.indexOf(";");
 		if (idx != -1) {
 			contentCharset = contentMimeType.substring(idx+1).trim();
 			contentMimeType = contentMimeType.substring(0,idx);
 			
 			if (contentCharset.startsWith("charset=")) {
 				contentCharset = contentCharset.substring("charset=".length()).trim();
 				if (contentCharset.matches("^['\"].*")) {
 					contentCharset = contentCharset.substring(1);
 				}
 				if (contentCharset.matches(".*['\"]$")) {
 					contentCharset = contentCharset.substring(0,contentCharset.length()-1);							
 				}
 				doc.setCharset(contentCharset);
 			} else {
 				contentCharset = null;
 			}
 		}
 		
 		// check against common MIME-types wrongly attributed to files by servers
 		// if this is one of them, we just ignore the MIME-type and let the MimeType-bundle do the job
 		if (!ERRONEOUS_MIME_TYPES.contains(contentMimeType)) {
 			doc.setMimeType(contentMimeType);
 			
 			// check if we support the mimetype
 			final CrawlerContext context = CrawlerContext.getCurrentContext();
 			if (context == null)
 				throw new RuntimeException("Unexpected error. The crawler-context was null.");
 			
 			if (!context.getSupportedMimeTypes().contains(contentMimeType)) {
 				// abort
 				String msg = String.format(
 						"Mimetype '%s' of resource '%s' not supported by any parser installed on the system.",
 						contentMimeType,
 						doc.getLocation()
 				);
 				
 				this.logger.warn(msg);
 				doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
 				return false;
 			}
 		}
 		
 		// continue
 		return true;
 	}
 	
 	/**
 	 * This method handles the <code>Content-Length</code> {@link Header} of a HTTP-request.
 	 * If given, and this {@link HttpCrawler} has a valid {@link #maxDownloadSize} set, both values
 	 * are being compared and if the further exceeds the latter, <code>false</code> is retured,
 	 * <code>true</code> otherwise
 	 * <p>
 	 * No values are set in the {@link CrawlerDocument} in case of a positive result, otherwise
 	 * the document's <code>result</code> is set to {@link ICrawlerDocument.Status#UNKNOWN_FAILURE}
 	 * and a warning message is logged.
 	 * 
 	 * @see #maxDownloadSize
 	 * @param contentTypeLength the HTTP-{@link Header}'s <code>Content-Length</code> attribute
 	 * @param doc the {@link CrawlerDocument} the resulting MIME-type and charset shall be set in
 	 * @return <code>true</code> if proceeding with the URL may continue or <code>false</code> if
 	 *         it shall be aborted due to an exceeded maximum file-size of the document
 	 */
 	private boolean handleContentLengthHeader(final Header contentTypeLength, final CrawlerDocument doc) {
 		if (contentTypeLength == null)
 			// no Content-Length given, continue
 			return true;
 		
 		final int maxDownloadSize = this.maxDownloadSize;
 		if (maxDownloadSize < 0)
 			// no maximum specified, continue
 			return true;
 		
 		// extract the content length in bytes
 		final String lengthString = contentTypeLength.getValue();
 		if (lengthString.length() > 0 && lengthString.matches("\\d+")) {
 			final int contentLength = Integer.parseInt(lengthString);
 			if (contentLength > maxDownloadSize) {
 				// reject the document
 				final String msg = String.format(
 						"Content-length %d of resource '%s' is larger than the max. allowed size of %d.",
 						Integer.valueOf(contentLength),
 						doc.getLocation(),
 						Integer.valueOf(maxDownloadSize));
 				
 				this.logger.warn(msg);
 				doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
 				return false;
 			}
 		}
 		
 		// continue
 		return true;
 	}
 	
 	public ICrawlerDocument request(URI requestUri) {
 		if (requestUri == null) throw new NullPointerException("URL was null");
 		this.logger.debug(String.format("Crawling URL '%s' ...",requestUri));
 		
 		CrawlerDocument doc = new CrawlerDocument();
 		
 		doc.setLocation(requestUri);
 		
 		HttpMethod method = null;
 		try {
 			// first use the HEAD method to determine whether the MIME-type is supported
 			// and to compare the content-length with the maximum allowed download size
 			// (both only if the server provides this information, if not, the file is
 			// fetched)
 			
 			final String uriAsciiString = requestUri.toASCIIString();
 			
 			method = new HeadMethod(uriAsciiString);		// automatically follows redirects
 			initRequestMethod(method);
 			
 			int statusCode = this.getHttpClient().executeMethod(method);
 			
 			final boolean headUnsupported = (statusCode == HttpStatus.SC_METHOD_FAILURE || statusCode == HttpStatus.SC_METHOD_NOT_ALLOWED);
 			if (!headUnsupported) {
 				if (statusCode != HttpStatus.SC_OK) {
 					// RFC 2616 states that the GET and HEAD methods _must_ be supported by any
 					// general purpose servers (which are in fact the ones we are connecting to here)
 					
 					if (statusCode == HttpStatus.SC_NOT_FOUND) {
 						doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
 					} else {
 						doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
 					}
 					
 					this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUri, method.getStatusLine()));
 					return doc;
 				}
 				
 				// getting the mimetype and charset
 				Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
 				if (!handleContentTypeHeader(contentTypeHeader, doc))
 					return doc;
 				
 				// reject the document if content-length is above our limit
 				Header contentTypeLength = method.getResponseHeader(HTTPHEADER_CONTENT_LENGTH);
 				if (!handleContentLengthHeader(contentTypeLength, doc))
 					return doc;
 				
 				if (!requestUri.equals(method.getURI()))
 					; // FIXME: we've been redirected, re-enqueue the new URL and abort processing
 			}
 			
 			// secondly - if everything is alright up to now - proceed with getting the actual
 			// document
 			
 			// generate the GET request method
 			HttpMethod getMethod = new GetMethod(uriAsciiString);		// automatically follows redirects
 			method.releaseConnection();
 			
 			method = getMethod;
 			initRequestMethod(method);
 			
 			// send the request to the server
 			statusCode = this.getHttpClient().executeMethod(method);
 			
 			// check the response status code
 			if (statusCode != HttpStatus.SC_OK) {
 				if (statusCode == HttpStatus.SC_NOT_FOUND) {
 					doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
 				} else {
 					doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
 				}
 				
 				this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUri, method.getStatusLine()));
 				return doc;
 			}
 			
 			if (headUnsupported) {
 				// getting the mimetype and charset
 				Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
 				if (!handleContentTypeHeader(contentTypeHeader, doc))
 					return doc;
 				
 				if (!requestUri.equals(method.getURI()))
 					; // FIXME: we've been redirected, re-enqueue the new URL and abort processing
 			}
 			
 			postProcessHeaders(method, doc);		// externalised into this method to cleanup here a bit
 			
 			// getting the response body
 			InputStream respBody = method.getResponseBodyAsStream();
 			
 			// handle the content-encoding, i.e. decompress the server's response
 			Header contentEncodingHeader = method.getResponseHeader(HTTPHEADER_CONTENT_ENCODING);
 			try {
 				respBody = handleContentEncoding(contentEncodingHeader, respBody);
 				
 				// copy the content to file
 				CrawlerTools.saveInto(doc, respBody, lrc);
 				
 				doc.setStatus(ICrawlerDocument.Status.OK);
 				this.logger.debug(String.format("Crawling of URL '%s' finished.", requestUri));
 			} catch (IOException e) {
 				String msg = e.getMessage();
 				if (msg == null || !msg.equals("Corrupt GZIP trailer"))
 					throw e;
 				
 				setHostSetting(method.getURI().getHost(), PREF_NO_ENCODING);
 				msg = String.format("server sent a corrupt gzip trailer at URL '%s'", requestUri);
 				logger.warn(msg);
 				
 				// FIXME re-enqueue command
 				doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, msg);
 			} finally {
 				respBody.close();
 			}
 		} catch (NoRouteToHostException e) {
 			this.logger.error(String.format("Error crawling %s: %s", requestUri, e.getMessage()));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
 		} catch (UnknownHostException e) {
 			this.logger.error(String.format("Error crawling %s: Unknown host.", requestUri));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());	
 		} catch (ConnectException e) {
 			this.logger.error(String.format("Error crawling %s: Unable to connect to host.", requestUri));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
 		} catch (ConnectTimeoutException e) {
 			this.logger.error(String.format("Error crawling %s: %s.", requestUri, e.getMessage()));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
 		} catch (SocketTimeoutException e) {
 			this.logger.error(String.format("Error crawling %s: Connection timeout.", requestUri));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
 		} catch (CircularRedirectException e) {
 			this.logger.error(String.format("Error crawling %s: %s", requestUri, e.getMessage()));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
 		} catch (Throwable e) {
 			String errorMsg;
 			if (e instanceof HttpException) {
 				errorMsg = "Unrecovered protocol exception: [%s] %s";
 			} else if (e instanceof IOException) {
 				errorMsg = "Transport exceptions: [%s] %s";
 			} else {
 				errorMsg = "Unexpected exception: [%s] %s";
 			}
 			errorMsg = String.format(errorMsg, e.getClass().getName(), e.getMessage());
 			
 			this.logger.error(String.format("Error crawling %s: %s", requestUri, errorMsg));
 			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, errorMsg);
 			e.printStackTrace();
 		} finally {
 			if (method != null) method.releaseConnection();
 		}
 		
 		return doc;
 	}
 	
 	private static void postProcessHeaders(final HttpMethod method, final CrawlerDocument doc) throws IOException {
 		// getting the document languages
 		Header contentLanguageHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LANGUAGE);
 		if (contentLanguageHeader != null) {
 			String contentLanguage = contentLanguageHeader.getValue();
 			String[] languages = contentLanguage.split(",");
 			doc.setLanguages(languages);
 		}
 		
 		// crawling Date
 		Date crawlingDate = null;
 		Header crawlingDateHeader = method.getResponseHeader(HTTPHEADER_DATE);			
 		if (crawlingDateHeader == null) {
 			crawlingDate = new Date();
 		} else try {
 			String dateStr = crawlingDateHeader.getValue();
 			crawlingDate = DateUtil.parseDate(dateStr);
 		} catch (DateParseException e) {
 			crawlingDate = new Date();
 		}
 		doc.setCrawlerDate(crawlingDate);
 		
 		// last mod date
 		Date lastModDate = null;
 		Header lastModDateHeader = method.getResponseHeader(HTTPHEADER_LAST_MODIFIED);
 		if (lastModDateHeader != null) try {
 			String dateStr = lastModDateHeader.getValue();
 			lastModDate = DateUtil.parseDate(dateStr);
 		} catch (DateParseException e) {
 			lastModDate = crawlingDate;
 		}
 		doc.setLastModDate(lastModDate);			
 		
 		// ETAG
 		Header etageHeader = method.getResponseHeader(HTTPHEADER_ETAG);
 		if (etageHeader != null) {
 			String etag = etageHeader.getValue();
 			doc.setEtag(etag);
 		}
 	}
 	
 	private static InputStream handleContentEncoding(final Header contentEncodingHeader, final InputStream responseBody) throws IOException {
 		if (contentEncodingHeader == null)
 			return responseBody;
 		
 		final String contentEncoding = contentEncodingHeader.getValue();
 		InputStream r = responseBody;
 		// apply decompression methods in the order given, see RFC 2616, section 14.11
 		final StringTokenizer st = new StringTokenizer(contentEncoding, ",");
 		while (st.hasMoreTokens()) {
 			String encoding = st.nextToken().trim();
 			// the "identity"-encoding does not need any transformation
 			
 			if (encoding.equals("deflate")) {
 				r = new ZipInputStream(r);
 			} else {
 				// support for the recommendation of the W3C, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.5
 				if (encoding.startsWith("x-"))
 					encoding = encoding.substring("x-".length());
 				if (encoding.equals("gzip") || encoding.equals("compress")) {
 					r = new GZIPInputStream(r);
 				}
 			}
 		}
 		return r;
 	}
 }
