 /* 
  * Copyright (c) 2011 Accenture
  * Licensed under the MIT open source license
  * http://www.opensource.org/licenses/mit-license.php
  */
 package no.uka.findmyapp.android.rest.client;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 
 import no.uka.findmyapp.android.rest.datamodels.core.Credentials;
 import oauth.signpost.OAuthConsumer;
 import oauth.signpost.OAuthProvider;
 import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
 import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
 import oauth.signpost.exception.OAuthCommunicationException;
 import oauth.signpost.exception.OAuthExpectationFailedException;
 import oauth.signpost.exception.OAuthMessageSignerException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.util.Log;
 
 
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class RestMethod.
  */
 public class RestMethod 
 {
 	private final static String debug = "RestMethod";
 	
 	/** Default HTTP status response codes. */
 	private final int HTTP_STATUS_OK = 200;
 	
 	/** The HTTP status not modified. */
 	private final int HTTP_STATUS_NOT_MODIFIED = 304;
 	
 	/** The HTTP status bad request. */
 	private final int HTTP_STATUS_BAD_REQUEST = 400; 
 	
 	/** The HTTP status unauthorized. */
 	private final int HTTP_STATUS_UNAUTHORIZED = 401; 
 	
 	/** The HTTP status forbidden. */
 	private final int HTTP_STATUS_FORBIDDEN = 403; 
 	
 	/** The HTTP status not_ found. */
 	private final int HTTP_STATUS_NOT_FOUND = 404; 
 	
 	/** The HTTP status timeout. */
 	private final int HTTP_STATUS_TIMEOUT = 408;
 	
 	/** The HTTP status internal server error. */
 	private final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500; 
 	
 	/** The UNHANDLED status code. */
 	private final int UNHANDLED_STATUS_CODE = 666; 
 	
 	/** The Constant REQUEST_TOKEN_ENDPOINT_URL. */
 	private static final String REQUEST_TOKEN_ENDPOINT_URL = "http://findmyapp.net/findmyapp/oauth/request_token";
 	
 	/** The Constant ACCESS_TOKEN_ENDPOINT_URL. */
 	private static final String ACCESS_TOKEN_ENDPOINT_URL = "http://findmyapp.net/findmyapp/oauth/access_token";
 	
 	/** The Constant AUTHORIZE_WEBSITE_URL. */
 	private static final String AUTHORIZE_WEBSITE_URL = "http://findmyapp.net/findmyapp/oauth/authorize";
 
 	private static final String sDataFormat = "application/json";
 	
 	private static final String CHARSET = "UTF-8"; 
 	
 	/** Default user-agent set to Mozilla Firefox Windows version. {@link #setRequestHeaders(String, HttpGet)} */
 	private String mUseragent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0"; 
 	
 	
 	/**
 	 * Shared buffer used by {@link #getUrlContent(String)} when reading results
 	 * from an API request.
 	 */
 	private static byte[] mStreamBuffer = new byte[512];
 	
 	/** URL to the REST service server {@link #RestClient(String)},. {@link #RestClient(String)} {@link #RestClient(String, String)} */
 	private URI mUri; 
 	
 	/** Instance HTTP client. */
 	private HttpClient mClient; 
 	
 	/** The provider. */
 	private OAuthProvider mProvider;
 	
 	/** The consumer. */
 	private OAuthConsumer mConsumer;
 	
 	private String mOAuthKey; 
 	
 	private String mOAuthSecret; 
 	
 	/**
 	 * Instantiates a new rest method.
 	 */
 	public RestMethod(Credentials credentials) {
 		mProvider = new CommonsHttpOAuthProvider(
                 REQUEST_TOKEN_ENDPOINT_URL, 
                 ACCESS_TOKEN_ENDPOINT_URL,
                 AUTHORIZE_WEBSITE_URL);
 
         mConsumer = new CommonsHttpOAuthConsumer(credentials.getKey(),
                                              credentials.getSecret());
         
         Log.v(debug, "API key: " + credentials.getKey() + " API-secret: " + credentials.getSecret());
 	}
 	
 	/**
 	 * Instantiates a new rest method.
 	 *
 	 * @param uri the uri
 	 */
 	public RestMethod(URI uri) {
 		mUri = uri; 
 	}
 
 	/**
 	 * Gets the useragent.
 	 *
 	 * @return the useragent
 	 */
 	public String getUseragent() {
 		return mUseragent;
 	}
 
 	/**
 	 * Gets the uri.
 	 *
 	 * @return the uri
 	 */
 	public URI getUri() {
 		return mUri;
 	}
 
 	/**
 	 * Sets the uri.
 	 *
 	 * @param uri the new uri
 	 */
 	public void setUri(URI uri) {
 		mUri = uri;
 	}
 
 	/**
 	 * Sets the useragent.
 	 *
 	 * @param useragent the new useragent
 	 */
 	public void setUseragent(String useragent) {
 		mUseragent = useragent;
 	}
 
 	/**
 	 * Gets the.
 	 *
 	 * @param serviceDataFormat the service data format
 	 * @return the string
 	 * @throws HTTPStatusException 
 	 * @throws Exception the exception
 	 */
 	public String get() throws HTTPStatusException {
 		HttpGet request = new HttpGet(mUri);
 		
 		return executeGet(setRequestHeaders(request, mUseragent));
 	}
 	
 	public String post(String data) throws HTTPStatusException {
 		HttpPost post = new HttpPost(this.mUri);
 		setPostHeaders(post, mUseragent);
 		
 		return executePost(post, data); 
 	}
 	
 	/**
 	 * Execute.
 	 *
 	 * @param request the request
 	 * @return the string
 	 * @throws HTTPStatusException 
 	 * @throws Exception the exception
 	 */
 	private String executeGet(HttpRequestBase request) throws HTTPStatusException {
 			this.mClient = new DefaultHttpClient();
 			try {
 				mConsumer.sign(request);
 			} catch (OAuthMessageSignerException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			} catch (OAuthExpectationFailedException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			} catch (OAuthCommunicationException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			}
 			
 			HttpResponse response;
 			try {
 				response = this.mClient.execute(request);
 			} catch (ClientProtocolException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			} catch (IOException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			}
 
 			// Check if server response is valid
 			StatusLine status = response.getStatusLine();
 			if (status.getStatusCode() != HTTP_STATUS_OK) {
 				this.throwHttpStatusException(status.getStatusCode());
 			} 
 	
 			// Pull content stream from response
 			HttpEntity entity = response.getEntity();
 			InputStream inputStream;
 			try {
 				inputStream = entity.getContent();
 			} catch (IllegalStateException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			} catch (IOException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			}
 	
 			ByteArrayOutputStream content = new ByteArrayOutputStream();
 	
 			// Read response into a buffered stream
 			int readBytes = 0;
 			try {
 				while ((readBytes = inputStream.read(mStreamBuffer)) != -1) {
 					content.write(mStreamBuffer, 0, readBytes);
 				}
 			} catch (IOException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			}
 	
 			// Return result from buffered stream
 			return new String(content.toByteArray());
 	}
 	
 	private String executePost(HttpPost post, String data) throws HTTPStatusException {
 		try{
 			StringEntity entity = new StringEntity(data, CHARSET);
 	    	post.setEntity(entity); 
 	    	
 	    	this.mClient = new DefaultHttpClient();
 			try {
 				mConsumer.sign(post);
 			} catch (OAuthMessageSignerException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			} catch (OAuthExpectationFailedException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			} catch (OAuthCommunicationException e) {
 				Log.e(debug, e.getMessage());
 				return ""; 
 			}
 			
 	    	HttpResponse response = mClient.execute(post);
 
 			StatusLine status = response.getStatusLine();
 			if (status.getStatusCode() != HTTP_STATUS_OK) {
 				this.throwHttpStatusException(status.getStatusCode());
 			} 
 			
             if (response != null) {
     			response.getEntity();
     			InputStream inputStream = entity.getContent();
     	
     			ByteArrayOutputStream content = new ByteArrayOutputStream();
     	
     			// Read response into a buffered stream
     			int readBytes = 0;
     			while ((readBytes = inputStream.read(mStreamBuffer)) != -1) {
     				content.write(mStreamBuffer, 0, readBytes);
     			}
     	
     			// Return result from buffered stream
     			return new String(content.toByteArray());
             }
            else {
            	// Return nothing if successful without response 
            	return "[]"; 
            }
 		}
 		catch (UnsupportedEncodingException e) {
 			Log.e(debug, "Unsupported encoding: " + e.getMessage());
 			return ""; 
 		}
 		catch (IOException e) {
 			Log.e(debug, "IOException: " + e.getMessage());
 			return ""; 
 		}
 		catch (HTTPStatusException e) {
 			throw e; 
 		}
 	}
 	
 	/**
 	 * Sets the request headers.
 	 *
 	 * @param expectedDataFormat the expected data format
 	 * @param request the request
 	 * @return the http request base
 	 */
 	private HttpRequestBase setRequestHeaders(HttpRequestBase request, String useragent) {
 		request.setHeader("Accept", sDataFormat);
 		request.setHeader("Content-type", sDataFormat);
 		request.setHeader("User-Agent", useragent);
 		
 		return request; 
 	}
 	
 	private HttpRequestBase setPostHeaders(HttpRequestBase request, String useragent) {
 		request.setHeader("User-Agent", useragent);
 		
 		return request; 
 	}
 
 	/**
 	 * Throws a HTTPStatusExcetion decided by a response/status code.
 	 *
 	 * @param statusCode the status code
 	 * @throws HTTPStatusException the hTTP status exception
 	 */
 	private void throwHttpStatusException(int statusCode) throws HTTPStatusException{
 		switch (statusCode) {
 			case HTTP_STATUS_BAD_REQUEST:
 				throw new HTTPStatusException(HTTP_STATUS_BAD_REQUEST, "400 Bad Request (HTTP/1.1 - RFC 2616)");
 			case HTTP_STATUS_UNAUTHORIZED:
 				throw new HTTPStatusException(HTTP_STATUS_UNAUTHORIZED, "401 Unauthorized (HTTP/1.0 - RFC 1945)");
 			case HTTP_STATUS_FORBIDDEN:
 				throw new HTTPStatusException(HTTP_STATUS_FORBIDDEN, "401 Unauthorized (HTTP/1.0 - RFC 1945)");
 			case HTTP_STATUS_NOT_FOUND:
 				throw new HTTPStatusException(HTTP_STATUS_NOT_FOUND, "404 Not Found (HTTP/1.0 - RFC 1945)");
 			case HTTP_STATUS_TIMEOUT:
 				throw new HTTPStatusException(HTTP_STATUS_TIMEOUT, "408 Request Timeout (HTTP/1.1 - RFC 2616)");
 			case HTTP_STATUS_INTERNAL_SERVER_ERROR:
 				throw new HTTPStatusException(HTTP_STATUS_INTERNAL_SERVER_ERROR, "500 Server Error (HTTP/1.0 - RFC 1945)");
 			default:
 				throw new HTTPStatusException(UNHANDLED_STATUS_CODE, "Unhandled status code: " + statusCode);
 		}
 	}
 	
 	/**
 	 * The Class HTTPStatusException.
 	 */
 	public static class HTTPStatusException extends Exception {
 		
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 4485462910566178510L;
 		
 		/** The status code. */
 		private int statusCode; 
 		
 		/**
 		 * Instantiates a new hTTP status exception.
 		 *
 		 * @param statusCode the status code
 		 * @param errorMessage the error message
 		 */
 		public HTTPStatusException(int statusCode, String errorMessage) {
 			super(errorMessage);
 			this.statusCode = statusCode; 
 		}
 		
 		/**
 		 * Gets the http status code.
 		 *
 		 * @return HTTP status code
 		 */
 		public int getHttpStatusCode() {
 			return this.statusCode; 
 		}
 	}
 }
 
