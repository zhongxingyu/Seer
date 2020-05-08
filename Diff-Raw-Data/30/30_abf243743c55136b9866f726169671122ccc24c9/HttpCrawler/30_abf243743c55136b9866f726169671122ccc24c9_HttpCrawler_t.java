 package org.paxle.crawler.http.impl;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.NoRouteToHostException;
 import java.net.UnknownHostException;
 import java.util.Date;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.util.DateParseException;
 import org.apache.commons.httpclient.util.DateUtil;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.doc.CrawlerDocument;
 import org.paxle.core.doc.ICrawlerDocument;
 import org.paxle.crawler.CrawlerTools;
 import org.paxle.crawler.ISubCrawler;
 import org.paxle.crawler.http.IHttpCrawler;
 
 /**
  * TODO: configure the connection pool properly
  * TODO: set redirection follow, etc....
  * 
  */
 public class HttpCrawler implements IHttpCrawler {
 	private static final String HTTPHEADER_ETAG = "ETag";
 	private static final String HTTPHEADER_LAST_MODIFIED = "Last-Modified";
 	private static final String HTTPHEADER_DATE = "Date";
 	private static final String HTTPHEADER_CONTENT_LANGUAGE = "Content-Language";
 	private static final String HTTPHEADER_CONTENT_TYPE = "Content-Type";
 
 	/**
 	 * The protocol supported by this crawler
 	 */
 	public static final String[] PROTOCOLS = new String[]{"http"};
 	
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
 	
	public HttpCrawler(int maxConnectionsPerHost, int connectionTimeout, int socketTimeout) {
		this.connectionManager = new MultiThreadedHttpConnectionManager();
		
		this.connectionManager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
		
		// configuring timeouts
		this.connectionManager.getParams().setConnectionTimeout(connectionTimeout);
		this.connectionManager.getParams().setSoTimeout(socketTimeout);
		
		this.httpClient = new HttpClient(connectionManager); 
	}
	
 	/**
 	 * @see ISubCrawler#getProtocols()
 	 */
 	public String[] getProtocols() {
 		return HttpCrawler.PROTOCOLS;
 	}	
 	
 	public ICrawlerDocument request(String requestUrl) {
 		if (requestUrl == null) throw new NullPointerException("URL was null");
 		this.logger.info(String.format("Crawling URL '%s' ...",requestUrl));
 		
 		CrawlerDocument doc = new CrawlerDocument();
 		
 		// FIXME: we need to re-write the location on crawler-redirection
 		doc.setLocation(requestUrl);
 		
 		HttpMethod method = null;
 		try {
 			// generate the request method
 			method = new GetMethod(requestUrl);
 	        method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
 
 			// TODO: set some additional http headers
 			//method.setRequestHeader("User-Agent","xxx");
 			//method.setRequestHeader("Accept-Encoding","gzip");
 
 			// send the request to the server
 			int statusCode = httpClient.executeMethod(method);
 			
 			// check the response status code
 			if (statusCode != HttpStatus.SC_OK) {
 				if (statusCode == HttpStatus.SC_NOT_FOUND) {
 					doc.setStatus(ICrawlerDocument.Status.NOT_FOUND);
 				} else {
 					doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, String.format("Server returned: %s", method.getStatusLine()));
 				}
 				
 				this.logger.warn(String.format("Crawling of URL '%s' failed. Server returned: %s", requestUrl, method.getStatusLine()));
 				return doc;
 			}
 			
 			// getting the mimetype and charset
 			String contentMimeType = null;
 			Header contentTypeHeader = method.getResponseHeader(HTTPHEADER_CONTENT_TYPE);
 			if (contentTypeHeader != null) {
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
 					} else {
 						contentCharset = null;
 					}
 				}	
 				
 				doc.setMimeType(contentMimeType);
 				doc.setCharset(contentCharset);
 			}
 			
 			// getting the document languages
 			Header contentLanguageHeader = method.getResponseHeader(HTTPHEADER_CONTENT_LANGUAGE);
 			if (contentLanguageHeader != null) {
 				String contentLanguage = contentLanguageHeader.getValue();
 				String[] languages = contentLanguage.split(",");
 				doc.setLanguages(languages);
 			}
 			
 			// crawling Date
 			Date crawlingDate = null;			
 			try {
 				Header crawlingDateHeader = method.getResponseHeader(HTTPHEADER_DATE);
 				if (crawlingDateHeader != null) {
 					String dateStr = crawlingDateHeader.getValue();
 					crawlingDate = DateUtil.parseDate(dateStr);
 				}
 			} catch (DateParseException e) {
 				crawlingDate = new Date();
 			}
 			doc.setCrawlerDate(crawlingDate);
 
 			// last mod date
 			Date lastModDate = null;			
 			try {
 				Header lastModDateHeader = method.getResponseHeader(HTTPHEADER_LAST_MODIFIED);
 				if (lastModDateHeader != null) {
 					String dateStr = lastModDateHeader.getValue();
 					lastModDate = DateUtil.parseDate(dateStr);
 				}
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
 			
 			// getting the response body			
 			InputStream respBody = method.getResponseBodyAsStream();
 			// TODO: add gzip/deflate support
 			
 			// copy the content to file
 			CrawlerTools.saveInto(doc, respBody);
 			respBody.close();
 			
 			doc.setStatus(ICrawlerDocument.Status.OK);
 			this.logger.info(String.format("Crawling of URL '%s' finished.", requestUrl));
 		} catch (NoRouteToHostException e) {
 			this.logger.error(String.format("Error crawling %s: %s", requestUrl, e.getMessage()));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());
 		} catch (UnknownHostException e) {
 			this.logger.error(String.format("Error crawling %s: Unknown host.", requestUrl));
 			doc.setStatus(ICrawlerDocument.Status.NOT_FOUND, e.getMessage());	
 		} catch (ConnectException e) {
 			this.logger.error(String.format("Error crawling %s: Unable to connect to host.", requestUrl));
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
 			
 			this.logger.error(String.format("Error crawling %s: %s", requestUrl, errorMsg));
 			doc.setStatus(ICrawlerDocument.Status.UNKNOWN_FAILURE, errorMsg);
 			e.printStackTrace();
 		} finally {
 			if (method != null) method.releaseConnection();
 		}
 		
 		return doc;
 	}
 }
