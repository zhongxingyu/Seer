 /*
  * This file is in the public domain, furnished "as is", without technical
  * support, and with no warranty, express or implied, as to its usefulness for
  *	any purpose.
  */
 package org.openexchangerates.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 /**
  * A client library for accessing resources via HTTP.
  * 
  * @author kgilmer
  *
  */
 public class RestClient {
 	private static final String HEADER_CONTENT_TYPE = "Content-Type";
 	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
 	
 	private static final int COPY_BUFFER_SIZE = 1024 * 4;
 	private static final int RANDOM_CHAR_COUNT = 15;
 	private static final String HEADER_TYPE = HEADER_CONTENT_TYPE;
 	private static final String HEADER_PARA = "Content-Disposition: form-data";
 	private static final String MULTIPART_FORM_DATA_CONTENT_TYPE = "multipart/form-data";
 	private static final String FILE_NAME = "filename";
 	private static final String LINE_ENDING = "\r\n";
 	private static final String BOUNDARY = "boundary=";
 	private static final String PARA_NAME = "name";	
 	
 	/**
 	 * HTTP methods supported by REST client.
 	 *
 	 */
 	public enum HttpMethod {
 		GET, POST, PUT, DELETE, HEAD
 	}
 	
 	/**
 	 * implement to provide an HttpURLConnection w/ some properties pre-set see
 	 * BasicAuthenticationConnectionProvider for an example.
 	 * 
 	 * This is passed into HttpRequest on creation to provide it w/ the connection
 	 * 
 	 * @author bballantine
 	 * 
 	 */
 	public interface ConnectionProvider {
 		/**
 		 * @param urlStr url that is used to source the connection
 		 * @return an appropriate instance of HttpURLConnection
 		 * @throws IOException on I/O error.
 		 */
 		HttpURLConnection getConnection(String urlStr) throws IOException;
 	}
 	
 	/**
 	 * A caching interface clients may set to enable caching of GETs.
 	 * Client implementation must handle caching logic such as expiring entries and managing resources.
 	 */
 	public interface HttpGETCache {
 		
 		/**
 		 * @param key cache key
 		 * @return content as InputStream or null if content doesn't exist.
 		 */
 		HttpGETCacheEntry get(String key);
 		
 		void put(String key, HttpGETCacheEntry entry);		
 	}
 	
 	/**
 	 * Represents all information that should be cached by HttpGETCache implementation.
 	 *
 	 */
 	public interface HttpGETCacheEntry {
 		/**
 		 * @return input stream of entry
 		 */
 		byte [] getContent();
 		/**
 		 * @return headers of entry
 		 */ 
 		Map<String, List<String>> getHeaders();
 		
 		/**
 		 * @return response code of entry
 		 */
 		int getResponseCode();
 	}
 
 	/**
 	 * Implementors can configure the http connection before every call is made.
 	 * Useful for setting headers that always need to be present in every WS
 	 * call to a given server.
 	 * 
 	 */
 	public interface ConnectionInitializer {
 		/**
 		 * @param connection
 		 *            HttpURLConnection
 		 */
 		void initialize(HttpURLConnection connection);
 	}
 	
 	/**
 	 * Utility interface for building URLs from String segments.
 	 */
 	public interface URLBuilder extends Cloneable {
 		/**
 		 * Append a segment to the URL.  Will handle leading and trailing slashes, and schemes.
 		 * 
 		 * @param segment to be appended
 		 * @return instance of builder
 		 */
 		URLBuilder append(String ... segment);
 		
 		/**
 		 * @param value if true, scheme is set to https, otherwise http.
 		 * @return instance of builder
 		 */
 		URLBuilder setHttps(boolean value);		
 		
 		/**
 		 * @return URL as a String with scheme
 		 */
 		@Override
 		String toString();
 		
 		/**
 		 * @return A new instance of URLBuilder with same path and scheme as parent.
 		 */
 		URLBuilder copy();
 		
 		/**
 		 * @param segments new segment to append to new copy of URLBuilder
 		 * @return A new instance of URLBuilder with same path and scheme as parent, with segment appended.
 		 */
 		URLBuilder copy(String ... segments);
 
 		/**
 		 * Add a query string parameter to the URL.
 		 * 
 		 * @param key key name.  Same key name can be used multiple times.
 		 * @param value value of parameter.
 		 * @return URLBuilder
 		 */
 		URLBuilder addParameter(String key, String value);
 
 		/**
 		 * 
 		 * @param value if true, scheme will be present in toString(), otherwise will be omitted.
 		 * @return URLBuilder
 		 */
 		URLBuilder emitScheme(boolean value);
 
 		/**
 		 * @param value if true, domain is emitted in toString(), false means it will not be emitted.
 		 * @return URLBuilder
 		 */
 		URLBuilder emitDomain(boolean value);
 	}
 	
 	/**
 	 * 
 	 *
 	 * @param <T>
 	 */
 	public interface ResponseDeserializer<T>  {			
 		/**
 		 * Deserialize the input.
 		 * @param input input stream of response
 		 * @param responseCode HTTP response from server
 		 * @param headers HTTP response headers
 		 * @return deserialized representation of response
 		 * @throws IOException on I/O error
 		 */
 		T deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException;
 	}
 	
 	/**
 	 * A HTTPResponseDeserializer that returns the entire response as a String.
 	 */
 	public static final ResponseDeserializer<String> STRING_DESERIALIZER = new ResponseDeserializer<String>() {
 
 		@Override
 		public String deserialize(InputStream input, int responseCode, Map<String, 
 				List<String>> headers) throws IOException {			
 			if (input != null)
 				return new String(readStream(input));
 			
 			return null;
 		}
 	};
 	
 	/**
 	 * A HTTPResponseDeserializer that returns true if the response from the server was not an error.
 	 */
 	public static final ResponseDeserializer<Integer> HTTP_CODE_DESERIALIZER = new ResponseDeserializer<Integer>() {
 
 		@Override
 		public Integer deserialize(InputStream input, int responseCode, Map<String, 
 				List<String>> headers) throws IOException {			
 			return responseCode;
 		}
 	};
 	
 	/**
 	 * A HTTPResponseDeserializer that simply returns the internal inputstream.
 	 * Useful for clients that wish to handle the response input stream manually.
 	 */
 	public static final ResponseDeserializer<InputStream> INPUTSTREAM_DESERIALIZER = new ResponseDeserializer<InputStream>() {
 
 		@Override
 		public InputStream deserialize(InputStream input, int responseCode, Map<String, 
 				List<String>> headers) throws IOException {			
 			return input;
 		}
 	};
 	
 	/**
 	 *
 	 */
 	public static final ErrorHandler THROW_ALL_ERRORS = new ErrorHandler() {
 
 		@Override
 		public void handleError(int code, String message) throws IOException {
 			throw new IOException("HTTP Error " + code + " was returned from the server: " + message);			
 		}
 		
 	};
 	
 	/**
 	 *
 	 */
 	public static final ErrorHandler THROW_5XX_ERRORS = new ErrorHandler() {
 
 		@Override
 		public void handleError(int code, String message) throws IOException {
 			if (code > 499 && code < 600)
 				throw new IOException("HTTP Error " + code + " was returned from the server: " + message);			
 		}
 		
 	};
 	/**
 	 * Time format for debug messages.
 	 */
 	private static final String DEBUG_TIME_FORMAT = "H:mm:ss:SSS";
 	
 	/**
 	 * The response from the server for a given request.
 	 *
 	 * @param <T>
 	 */
 	public interface Response<T> {
 	    /**
 	     * Cancel the request.
 	     * 
 	     * @param mayInterruptIfRunning
 	     * @return
 	     */
 	    public abstract boolean cancel(boolean mayInterruptIfRunning);
 
 	    /**
 	     * @return true if the request has been canceled
 	     */
 	    public abstract boolean isCancelled();
 
 	    /**
 	     * @return true if the request has been completed
 	     */
 	    public abstract boolean isDone();
 
 	    /**
 	     * @return the content (body) of the response.
 	     * 
 	     * @throws IOException
 	     */
 	    public abstract T getContent() throws IOException;	   
 	    
 		/**
 		 * @return The HttpURLConnection associated with the request.
 		 */
 		HttpURLConnection getConnection();
 		/**
 		 * @return The HTTP method that was used in the call.
 		 */
 		HttpMethod getRequestMethod();
 		/**
 		 * @return The URL that was used in the call.
 		 */
 		String getRequestUrl();
 		/**
 		 * @return The HTTP Response code from the server.
 		 * @throws IOException on I/O error.
 		 */
 		int getCode() throws IOException;		
 		
 		/**
 		 * @return true if error code or an exception is raised, false otherwise.
 		 */
 		boolean isError();
 
 		/**
 		 * @return error message or null if failure to get message from server.
 		 */
 		public abstract String getErrorMessage();				
 	}
 	
 	/**
 	 * The ErrorHander does something based on an HTTP or I/O error.
 	 *
 	 */
 	public interface ErrorHandler {
 		/**
 		 * @param code the HTTP code of the error
 		 * @param human-readable error message
 		 * @throws IOException on I/O error
 		 */
 		void handleError(int code, String message) throws IOException;
 	}
 	
 	/**
 	 * Used to specify a file to upload in a multipart POST.
 	 *
 	 */
 	public static class FormFile extends File {
 		private static final long serialVersionUID = 2957338960806476533L;
 		private final String mimeType;
 
 		/**
 		 * @param pathname
 		 */
 		public FormFile(String pathname, String mimeType) {
 			super(pathname);
 			this.mimeType = mimeType;					
 		}
 		
 		/**
 		 * @return Mime type of file.
 		 */
 		public String getMimeType() {
 			return mimeType;
 		}
 	}
 	
 	/**
 	 * Simple cache implementation using a HashMap backend.
 	 */
 	public static class HashMapCache implements RestClient.HttpGETCache {
 		protected final int CONTENT_INDEX = 0;
 		protected final int HEADERS_INDEX = 1;
 		protected final int CODE_INDEX = 2;
 		protected final Map<String, Object[]> cache = new HashMap<String, Object[]>();
 
 		@Override
 		public HttpGETCacheEntry get(final String key) {		
 			if (cache.containsKey(key)) {
 				return new RestClient.HttpGETCacheEntry() {
 					
 					@Override
 					public int getResponseCode() {
 						return ((Integer) cache.get(key)[CODE_INDEX]).intValue();
 					}
 										
 					@Override
 					public Map<String, List<String>> getHeaders() {						
 						return (Map<String, List<String>>) cache.get(key)[HEADERS_INDEX];
 					}
 
 					@Override
 					public byte[] getContent() {
 						return (byte []) cache.get(key)[CONTENT_INDEX];
 					}
 				};
 			}
 			
 			return null;
 		}
 		
 		/**
 		 * Clear the map
 		 */
 		public void clear() {
 			cache.clear();
 		}
 
 		@Override
 		public void put(String key, HttpGETCacheEntry entry) {
 			if (entry != null) {
 				Object [] ov = new Object[3];			
 				ov[CONTENT_INDEX] = entry.getContent();
 				ov[HEADERS_INDEX] = entry.getHeaders();
 				ov[CODE_INDEX] = entry.getResponseCode();
 				
 				cache.put(key, ov);						
 			} else {
 				cache.remove(key);
 			}
 		}
 	}
 	
 	/**
 	 * Used to specify a file to upload in a multipart POST.
 	 *
 	 */
 	public static class FormInputStream extends InputStream {
 		private static final long serialVersionUID = 2957338960806476533L;
 		private final String mimeType;
 		private final InputStream parent;
 		private final String name;
 
 		/**
 		 * @param parent Input stream of file
 		 * @param name name of file
 		 * @param mimeType mimetype for content of file
 		 */
 		public FormInputStream(InputStream parent, String name, String mimeType) {
 
 			this.parent = parent;
 			this.name = name;
 			this.mimeType = mimeType;					
 		}
 		
 		/**
 		 * @return Mime type of file.
 		 */
 		public String getMimeType() {
 			return mimeType;
 		}
 
 		@Override
 		public int read() throws IOException {			
 			return parent.read();
 		}
 		
 		@Override
 		public int read(byte[] b) throws IOException {		
 			return parent.read(b);
 		}
 		
 		@Override
 		public int read(byte[] b, int off, int len) throws IOException {
 			return parent.read(b, off, len);
 		}
 
 		/**
 		 * @return name of content
 		 */
 		public String getName() {
 			return name;
 		}
 	}
 	
 	/**
 	 * A ConnectionIntializer for timeouts.
 	 *
 	 */
 	public static class TimeoutConnectionInitializer implements ConnectionInitializer {
 
 		private final int connectTimeout;
 		private final int readTimeout;
 
 		/**
 		 * @param connectTimeout
 		 * @param readTimeout
 		 */
 		public TimeoutConnectionInitializer(int connectTimeout, int readTimeout) {
 			this.connectTimeout = connectTimeout;
 			this.readTimeout = readTimeout;
 			
 		}
 		
 		@Override
 		public void initialize(HttpURLConnection connection) {
 			connection.setConnectTimeout(connectTimeout);
 			connection.setReadTimeout(readTimeout);
 		}
 	}
 	
 	/**
 	 * A Connection Initializer that sends Basic Authentication header in the request.
 	 *
 	 */
 	public static class BasicAuthConnectionInitializer implements ConnectionInitializer {
 
 		private final String field;
 
 		/**
 		 * @param username
 		 * @param password
 		 */
 		public BasicAuthConnectionInitializer(String username, String password) {
 			String userpass = username + ":" + password;
 			field = "Basic " + encodeBytes(userpass.getBytes()); 
 		}
 		
 		@Override
 		public void initialize(HttpURLConnection connection) {
 			connection.addRequestProperty("Authorization", field);
 		}		
 	}
 
 	private static Random RNG;
 
 	private ConnectionProvider connectionProvider;
 
 	private final List<ConnectionInitializer> connectionInitializers;
 	
 	private HttpGETCache contentCache;
 	
 	private ErrorHandler errorHandler;
 	private PrintWriter debugStream;
 	private SimpleDateFormat debugTimeFormat;
 		
 	/**
 	 * Default constructor.
 	 */
 	public RestClient() {
 		this.connectionProvider = new DefaultConnectionProvider();
 		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
 		this.errorHandler = null;
 		this.contentCache = null;
 	}
 	
 	/**
 	 * @param connectionProvider ConnectionProvider
 	 */
 	public RestClient(ConnectionProvider connectionProvider) {
 		this.connectionProvider = connectionProvider;
 		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
 		this.errorHandler = null;
 		this.contentCache = null;
 	}
 	
 	/**
 	 * @param initializer ConnectionInitializer
 	 */
 	public RestClient(ConnectionInitializer initializer) {
 		this();
 		connectionInitializers.add(initializer);
 	}
 		
 	/**
 	 * Creates a ReSTClient with basic authentication credentials.
 	 * @param username
 	 * @param password
 	 */
 	public RestClient(String username, String password) {
 		this();
 		connectionInitializers.add(new BasicAuthConnectionInitializer(username, password));
 	}
 	
 	/**
 	 * @param connectionProvider ConnectionProvider
 	 * @param initializer ConnectionInitializer
 	 */
 	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer) {
 		this(connectionProvider);
 		connectionInitializers.add(initializer);
 	}
 	
 	/**
 	 * @param initializer ConnectionInitializer
 	 * @param deserializer ResponseDeserializer<T>
 	 */
 	public RestClient(ConnectionInitializer initializer, ResponseDeserializer<?> deserializer) {
 		this(initializer);
 	}
 	
 	/**
 	 * @param connectionProvider ConnectionProvider
 	 * @param initializer ConnectionInitializer
 	 * @param deserializer ResponseDeserializer<T>
 	 */
 	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer, ResponseDeserializer<?> deserializer) {
 		this(connectionProvider, initializer);
 	}
 	
 	/**
 	 * @param connectionProvider ConnectionProvider
 	 * @param initializer ConnectionInitializer
 	 * @param deserializer ResponseDeserializer<T>
 	 * @param errorHandler ErrorHandler
 	 */
 	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer, 
 			ResponseDeserializer<?> deserializer, ErrorHandler errorHandler) {
 		this.connectionProvider = connectionProvider;
 		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
 		this.errorHandler = errorHandler;
 		this.contentCache = null;
 		connectionInitializers.add(initializer);		
 	}
 	
 	/**
 	 * @param connectionProvider ConnectionProvider
 	 * @param initializer ConnectionInitializer
 	 * @param deserializer ResponseDeserializer<T>
 	 * @param errorHandler ErrorHandler
 	 * @param debugStream OutputStream to pass debug messages to.  If null, no debug output.
 	 */
 	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer, 
 			ResponseDeserializer<?> deserializer, ErrorHandler errorHandler, PrintWriter debugStream) {
 		this.connectionProvider = connectionProvider;
 		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
 		this.errorHandler = errorHandler;
 		this.debugStream = debugStream;
 		this.contentCache = null;
 		connectionInitializers.add(initializer);		
 	}
 	
 	// Public methods
 	/**
 	 * Set a content cache for the client.  Will be used to cache GET methods only.
 	 * @param cache
 	 */
 	public void setCache(HttpGETCache cache) {
 		this.contentCache = cache;
 	}
 	
 	/**
 	 * @return current ContentCache or null if not previously set.
 	 */
 	public HttpGETCache getCache() {
 		return contentCache;
 	}
 	
 	/**
 	 * @return ErrorHandler
 	 */
 	public ErrorHandler getErrorHandler() {
 		return errorHandler;
 	}
 	
 	/**
 	 * Sets an error handler for the client.  If no error handler is set, HTTP (application level) errors will be ignored 
 	 * by the client.
 	 * 
 	 * Creating a custom ErrorHandler let's the client handle specific errors from the server in an application specific way.
 	 * 
 	 * See also: THROW_ALL_ERRORS, THROW_5XX_ERRORS
 	 * 
 	 * @param handler ErrorHandler
 	 */
 	public void setErrorHandler(ErrorHandler handler) {
 		this.errorHandler = handler;
 	}
 	
 	/**
 	 * Sets a debug OutputStream for the client.  If null is passed, no debug output
 	 * will be generated.
 	 * 
 	 * @param debugStream OutputStream
 	 */
 	public void setDebugWriter(PrintWriter writer) {
 		this.debugStream = writer;
 		
 		if (writer != null && debugTimeFormat == null) {
 			debugTimeFormat = new SimpleDateFormat(DEBUG_TIME_FORMAT);
 		}
 	}
 	
 	/**
 	 * @param provider ConnectionProvider
 	 */
 	public void setConnectionProvider(ConnectionProvider provider) {
 		this.connectionProvider = provider;
 	}
 	
 	/**
 	 * @return ConnectionProvider
 	 */
 	public ConnectionProvider getConnectionProvider() {
 		return connectionProvider;
 	}
 	
 	/**
 	 * @param initializer
 	 */
 	public ConnectionInitializer addConnectionInitializer(ConnectionInitializer initializer) {
 		if (!connectionInitializers.contains(initializer))
 			connectionInitializers.add(initializer);
 		
 		return initializer;
 	}
 	
 	/**
 	 * @param initializer ConnectionInitializer
 	 * @return ConnectionInitializer
 	 */
 	public boolean removeConnectionInitializer(ConnectionInitializer initializer) {
 		return connectionInitializers.remove(initializer);
 	}
 	
 	/**
 	 * This is the primary call in RestClient.  All other HTTP method calls call this method with some specific parameters.
 	 * For flexibility this method is exposed to clients but should not be used in a majority of cases.  See callGet(), 
 	 * callPost() etc. for the simplest usage.  This call is asynchronous, the Response works like a Future.
 	 * 
 	 * This method handles errors based on how the client is configured.  If no Deserializer or ErrorHander is specified
 	 * the response content will produce null.  The response class will contain the HTTP error information.
 	 * 
 	 * @param method HTTP method.  Cannot be null.
 	 * @param url url of server.  Cannot be null.
 	 * @param deserializer class to deserialize the response body.  If null then response is deserialized to a String.
 	 * @param content Optional content to pass to server, can be null.
 	 * @param headers HTTP headers that should be appended to the call.  These are in addition to any headers set 
 	 * 			in any ConnectionInitializers associated with client.
 	 * @param <T> type to deserialize to
 	 * @return deserialized response
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> call(final HttpMethod method, final String url, final ResponseDeserializer<T> deserializer, 
 			InputStream content, Map<String, String> headers) throws IOException {
 		
 		validateArguments(method, url);		
 		
		String httpUrl = url.toLowerCase();
 		if (!httpUrl.startsWith("http://") && !httpUrl.startsWith("https://"))
 			httpUrl = "http://" + url;
 		
 		StringBuilder debugBuffer = null;
 		if (debugStream != null)
 			debugBuffer = debugStart(httpUrl, method.toString());
 				
 		final HttpURLConnection connection;
 		
 		HttpGETCacheEntry cacheEntry = null;
 		if (method == HttpMethod.GET && contentCache != null && (cacheEntry = contentCache.get(url)) != null) {
 			connection = new CachedConnectionProvider(cacheEntry.getContent(), cacheEntry.getHeaders(), cacheEntry.getResponseCode());
 			
 			if (debugStream != null)
 				debugMid(debugBuffer, "[CACHE HIT]");
 		} else {
 			connection = connectionProvider.getConnection(httpUrl);
 			connection.setRequestMethod(method.toString());
 		}
 		
 		for (ConnectionInitializer initializer : connectionInitializers)
 			initializer.initialize(connection);
 		
 		if (headers != null && headers.size() > 0)
 			for (Map.Entry<String, String> entry : headers.entrySet())
 				connection.addRequestProperty(entry.getKey(), entry.getValue());
 
 		ByteArrayOutputStream baos;
 		switch(method) {
 		case GET:			
 			connection.setDoInput(true);
 			connection.setDoOutput(false);
 			break;
 		case POST:
 			if (content != null) {
 				connection.setDoOutput(true);	
 				baos = new ByteArrayOutputStream();
 				copy(content, baos);					
 				writeRequestBody(connection, baos.toByteArray());	
 				baos.close();			
 			
 				if (debugStream != null)
 					debugMid(debugBuffer, new String(baos.toByteArray()));
 			}
 			break;
 		case PUT:
 			if (content != null) {
 				connection.setDoOutput(true);
 				baos = new ByteArrayOutputStream();
 				copy(content, baos);
 				writeRequestBody(connection, baos.toByteArray());
 				baos.close();
 						
 				if (debugStream != null)
 					debugMid(debugBuffer, new String(baos.toByteArray()));
 			}
 			break;
 		case DELETE:
 			connection.setDoInput(true);			
 			break;
 		case HEAD:
 			connection.setDoInput(true);
 			connection.setDoOutput(false);
 			break;
 		default:
 			throw new RuntimeException("Unhandled HTTP method.");
 		}	
 		
 		if (debugStream != null) 
 			debugEnd(debugBuffer);
 		
 		return new Response<T>() {
 
 			private boolean done;
 			private boolean cancelled;
 			private StringBuilder responseBuffer;
 
 			@Override
 			public int getCode() throws IOException {	
 				int code = connection.getResponseCode();
 				
 				if (debugStream != null) {					
 					responseBuffer = debugStart(code, connection.getResponseMessage());
 				}
 					
 				return code;
 			}
 			
 			@Override
 			public String getRequestUrl() {
 				return url;
 			}
 			
 			@Override
 			public HttpMethod getRequestMethod() {
 				return method;			
 			}
 			
 			@Override
 			public HttpURLConnection getConnection() {
 				return connection;			
 			}
 
 			@Override
 			public boolean isError() {
 				try {
 					int code = getCode();
 					return code >= HttpURLConnection.HTTP_BAD_REQUEST && code < HttpURLConnection.HTTP_VERSION;
 				} catch (IOException e) {
 					e.printStackTrace();
 					return true;
 				}		
 			}
 
 			@Override
 			public boolean cancel(boolean flag) {
 				connection.disconnect();
 				cancelled = true;
 				
 				if (responseBuffer != null) {
 					debugMid(responseBuffer, "[CANCELLED]");
 					debugEnd(responseBuffer);
 				}
 				
 				return cancelled;
 			}
 
 			@Override
 			public boolean isCancelled() {			
 				return cancelled;				
 			}
 
 			@Override
 			public boolean isDone() {
 				return done;				
 			}
 
 			@Override
 			public T getContent() throws IOException {									
 				if (isError()) {
 					String serverMessage = getErrorMessage();
 					
 					if (responseBuffer != null) {
 						debugMid(responseBuffer, serverMessage);						
 						debugEnd(responseBuffer);
 					}
 					
 					if (errorHandler != null) 
 						errorHandler.handleError(getCode(), serverMessage);
 						
 					if (deserializer != null)
 						return deserializer.deserialize(connection.getErrorStream(), connection.getResponseCode(), 
 								connection.getHeaderFields());
 					
 					return null;
 				}
 				
 				InputStream inputStream = connection.getInputStream();
 				final int responseCode = connection.getResponseCode();
 				final Map<String, List<String>> headerFields = connection.getHeaderFields();
 				HttpGETCacheEntry entry = null;				
 				if (contentCache != null) {
 					final byte[] buf = readStream(connection.getInputStream());
 				
 					
 					entry = new HttpGETCacheEntry() {
 						
 						@Override
 						public int getResponseCode() {
 							return responseCode;
 						}											
 						
 						@Override
 						public Map<String, List<String>> getHeaders() {
 							return headerFields;
 						}
 
 						@Override
 						public byte[] getContent() {							
 							return buf;
 						}
 					};
 					contentCache.put(url, entry);
 					
 					inputStream = new ByteArrayInputStream(buf);
 					
 					if (responseBuffer != null) 
 						debugMid(responseBuffer, "[CACHED RESPONSE]");
 				} else if (responseBuffer != null) {
 					debugMid(responseBuffer, "[NOT CACHING, INVALID RESPONSE]");					
 				}
 				
 				if (entry != null)
 				
 				if (deserializer == null) {
 					// If no deserializer is specified, use String.
 					T response = (T) RestClient.STRING_DESERIALIZER.deserialize(inputStream, 0, null);
 					done = true;
 					
 					if (responseBuffer != null) {
 						debugMid(responseBuffer, response.toString());
 						debugEnd(responseBuffer);
 					}
 					
 					return response;
 				}
 				
 				T response = deserializer.deserialize(inputStream, responseCode, headerFields);
 				
 				done = true;
 				
 				if (responseBuffer != null) {				
 					debugEnd(responseBuffer);
 				}
 				
 				return response;				
 			}
 
 			@Override
 			public String getErrorMessage() {
 				try {
 					String errorMessage = connection.getResponseMessage();
 					byte[] serverMessage = readStream(connection.getErrorStream());
 					if (serverMessage != null && serverMessage.length > 0) {
 						if (connection.getContentEncoding() != null)
 							errorMessage = new String(serverMessage, connection.getContentEncoding());
 						else 
 							errorMessage = new String(serverMessage, "UTF-8");
 					}
 					
 					return errorMessage;
 				} catch (IOException e) {
 					return null;
 				}
 			}
 			
 		};				
 	}
 	
 	private StringBuilder debugStart(String httpUrl, String httpMethod) {
 		StringBuilder debugBuffer = new StringBuilder();
 		debugBuffer.append(debugTimeFormat.format(new Date(System.currentTimeMillis())));
 		debugBuffer.append(' ');
 		debugBuffer.append(httpMethod.subSequence(0, 3));
 		debugBuffer.append(' ');
 		debugBuffer.append(httpUrl);
 		debugBuffer.append(' ');
 		
 		return debugBuffer;
 	}
 	
 	private StringBuilder debugStart(int responseCode, String responseMessage) {
 		StringBuilder debugBuffer = new StringBuilder();
 		debugBuffer.append(debugTimeFormat.format(new Date(System.currentTimeMillis())));
 		debugBuffer.append(' ');
 		debugBuffer.append("<-- ");
 		debugBuffer.append(responseCode);
 		debugBuffer.append(" ");
 		debugBuffer.append(responseMessage);
 		
 		debugBuffer.append(' ');
 		
 		return debugBuffer;
 	}
 	
 	private void debugMid(StringBuilder debugBuffer, String element) {		
 		debugBuffer.append(element);
 		debugBuffer.append(' ');
 	}
 	
 	private void debugEnd(StringBuilder debugBuffer) {		
 		debugStream.println(debugBuffer.toString());	
 		debugStream.flush();
 	}
 
 	/**
 	 * Execute GET method and return body as a string.  This call blocks
 	 * until the response content is deserialized into a String.
 	 * 
 	 * @param url of server.  If not String, toString() will be called.
 	 * @return body as a String
 	 * @throws IOException on I/O error
 	 */
 	public String callGet(Object url) throws IOException {		
 		return callGetContent(url.toString(), STRING_DESERIALIZER);
 	}
 	
 	
 	/**
 	 * Execute GET method and return body deserizalized.  This call
 	 * blocks until the response body content is deserialized.
 	 * 
 	 * @param <T> Deserialize response to type
 	 * @param url of server.  If not String, toString() will be called.
 	 * @param deserializer ResponseDeserializer
 	 * @return T deserialized object
 	 * @throws IOException on I/O error
 	 */
 	public <T> T callGetContent(Object url, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.GET, url.toString(), deserializer, null, null).getContent();
 	}
 	
 	/**
 	 * Execute GET method and deserialize response.
 	 * @param <T> Deserialize response to type
 	 * 
 	 * @param url of server  If not String, toString() will be called.
 	 * @param headers Map of headers to pass to server.
 	 * @param deserializer class that can deserialize content into desired type.
 	 * @return type specified by deserializer
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callGet(Object url, Map<String, String> headers, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.GET, url.toString(), deserializer, null, headers);
 	}
 	
 	/**
 	 * Execute GET method and deserialize response.
 	 * @param <T> Deserialize response to type
 	 * 
 	 * @param url of server.  If not String, toString() will be called.
 	 * @param deserializer class that can deserialize content into desired type.
 	 * @return type specified by deserializer
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callGet(Object url, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.GET, url.toString(), deserializer, null, null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param url url of server.  If not String, toString() will be called.
 	 * @param body body of post as an input stream
 	 * @return a response to the request
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callPost(Object url, InputStream body) throws IOException {
 		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, body, null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param url url of server.  If not String, toString() will be called.
 	 * @param body body of post as a String
 	 * @return a response to the request
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callPost(Object url, String body) throws IOException {
 		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, new ByteArrayInputStream(body.getBytes()), null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param url url of server
 	 * @param formData Form data as strings.  
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callPost(Object url, Map<String, String> formData) throws IOException {
 		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, 
 				new ByteArrayInputStream(propertyString(formData).getBytes()), 
 				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * @param <T> Deserialize response to type
 	 * 
 	 * @param url url of server
 	 * @param body body of post as an input stream
 	 * @param deserializer 
 	 * @return a response to the request
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPost(Object url, InputStream body, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.POST, url.toString(), deserializer, body, null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * @param <T> Deserialize response to type
 	 * 
 	 * @param url url of server.   If not String, toString() will be called.
 	 * @param formData Form data as strings.  
 	 * @param deserializer 
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPost(Object url, Map<String, String> formData, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.POST, url.toString(), deserializer, 
 				new ByteArrayInputStream(propertyString(formData).getBytes()), 
 				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param <T> type to deserialize to
 	 * @param url of server
 	 * @param body of post
 	 * @param headers additional headers for request
 	 * @param deserializer deserializer
 	 * @return response
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPost(URLBuilder url, InputStream body, Map<String, String> headers, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.POST, url.toString(), deserializer, body, headers);
 	}
 	
 	/**
 	 * Send a multipart POST to the server.  Convenience method for post(url, createMultipartPostBody(content)).
 	 * 
 	 * @param url url of server.  If not String, toString() will be called.
 	 * @param content See createMultipartPostBody() for details on this parameter.
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callPostMultipart(Object url, Map<String, Object> content) throws IOException {
 		String boundary = createMultipartBoundary();
 		String contentType = MULTIPART_FORM_DATA_CONTENT_TYPE + "; " + BOUNDARY + boundary;
 						
 		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, 
 				createMultipartPostBody(boundary, content), toMap(HEADER_CONTENT_TYPE, contentType));
 	}
 	
 	/**
 	 * Send a multipart POST to the server.  Convenience method for post(url, createMultipartPostBody(content)).
 	 * 
 	 * @param url url of server.  If not String, toString() will be called.
 	 * @param content See createMultipartPostBody() for details on this parameter.
 	 * @param deserializer class that can deserialize content into desired type.
 	 * @param <T> Type to be deserialized to.
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPostMultipart(Object url, Map<String, Object> content, 
 			ResponseDeserializer<T> deserializer) throws IOException {
 		
 		String boundary = createMultipartBoundary();
 		String contentType = MULTIPART_FORM_DATA_CONTENT_TYPE + "; " + BOUNDARY + boundary;
 					
 		return call(HttpMethod.POST, url.toString(), deserializer, 
 				createMultipartPostBody(boundary, content), toMap(HEADER_CONTENT_TYPE, contentType));
 	}
 	
 	/**
 	 * Call PUT method on a server.
 	 * 
 	 * @param url url of server
 	 * @param content See createMultipartPostBody() for details on this parameter.
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callPut(Object url, InputStream content) throws IOException {
 		return call(HttpMethod.PUT, url.toString(), HTTP_CODE_DESERIALIZER, content, null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param url url of server
 	 * @param formData Form data as strings.  
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callPut(Object url, Map<String, String> formData) throws IOException {
 		return call(HttpMethod.PUT, url.toString(), HTTP_CODE_DESERIALIZER, 
 				new ByteArrayInputStream(propertyString(formData).getBytes()), 
 				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
 	}
 	
 	/**
 	 * Call PUT method on a server.
 	 * 
 	 * @param url url of server
 	 * @param content See createMultipartPostBody() for details on this parameter.
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPut(Object url, InputStream content, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.PUT, url.toString(), deserializer, content, null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param url url of server
 	 * @param formData Form data as strings.  
 	 * @return a response from the POST
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPut(Object url, Map<String, String> formData, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.PUT, url.toString(), deserializer, 
 				new ByteArrayInputStream(propertyString(formData).getBytes()), 
 				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
 	}
 
 	
 	/**
 	 * Call DELETE method on a server.
 	 * 
 	 * @param url of server.  If not String, toString() will be called.
 	 * @return HTTP response from server
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callDelete(Object url) throws IOException {
 		return call(HttpMethod.DELETE, url.toString(), HTTP_CODE_DESERIALIZER, null, null);
 	}
 	
 	/**
 	 * Call DELETE method on a server.
 	 * 
 	 * @param url of server.  If not String, toString() will be called.
 	 * @return HTTP response from server
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callDelete(Object url, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.DELETE, url.toString(), deserializer, null, null);
 	}
 	
 	/**
 	 * Call HEAD method on a server.
 	 * 
 	 * @param url of server.  If not String, toString() will be called.
 	 * @return HTTP Response from server
 	 * @throws IOException on I/O error
 	 */
 	public Response<Integer> callHead(Object url) throws IOException {
 		return call(HttpMethod.HEAD, url.toString(), HTTP_CODE_DESERIALIZER, null, null);
 	}
 
 	
 	// Public static methods
 	
 	/**
 	 * Create a buffer for a multi-part POST body, and return an input stream to the buffer.
 	 * @param boundary String use use to signify end of part.
 	 * 
 	 * @param content A map of <String, Object>  The values can either be of type String, RestClient.FormInputStream, or 
 	 * type RestClient.FormFile.  Other types will cause an IllegalArgumentException.
 	 * @return an input stream of buffer of POST body.
 	 * @throws IOException on I/O error.
 	 */
 	public static InputStream createMultipartPostBody(String boundary, Map<String, Object> content) throws IOException {		
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();	
 		byte[] header = getPartHeader(boundary);
 		
 		for (Map.Entry<String, Object> entry : content.entrySet()) {
 			baos.write(header);
 			baos.write(entry.getKey().getBytes());
 			baos.write('"');
 			
 			if (entry.getValue() instanceof String) {
 				baos.write(LINE_ENDING.getBytes());
 				baos.write(LINE_ENDING.getBytes());
 				baos.write(((String) entry.getValue()).getBytes());
 			} else if (entry.getValue() instanceof FormFile) {
 				FormFile ffile = (FormFile) entry.getValue();
 				baos.write("; ".getBytes());
 				baos.write(FILE_NAME.getBytes());
 				baos.write("=\"".getBytes());
 				baos.write(ffile.getName().getBytes());
 				baos.write('"');
 				baos.write(LINE_ENDING.getBytes());				
 				baos.write(HEADER_TYPE.getBytes());
 				baos.write(": ".getBytes());
 				baos.write(ffile.getMimeType().getBytes());
 				baos.write(';');
 				baos.write(LINE_ENDING.getBytes());
 				baos.write(LINE_ENDING.getBytes());
 				baos.write(readStream(new FileInputStream(ffile)));
 			} else if (entry.getValue() instanceof FormInputStream) {
 				FormInputStream ffile = (FormInputStream) entry.getValue();
 				baos.write("; ".getBytes());
 				baos.write(FILE_NAME.getBytes());
 				baos.write("=\"".getBytes());
 				baos.write(ffile.getName().getBytes());
 				baos.write('"');
 				baos.write(LINE_ENDING.getBytes());				
 				baos.write(HEADER_TYPE.getBytes());
 				baos.write(": ".getBytes());
 				baos.write(ffile.getMimeType().getBytes());
 				baos.write(';');
 				baos.write(LINE_ENDING.getBytes());
 				baos.write(LINE_ENDING.getBytes());
 				baos.write(readStream(ffile));
 			} else if (entry.getValue() == null) {
 				throw new IllegalArgumentException("Content value is null.");
 			} else {
 				throw new IllegalArgumentException("Unhandled type: " + entry.getValue().getClass().getName());
 			}
 			
 			baos.write(LINE_ENDING.getBytes());
 		}
 		
 		return new ByteArrayInputStream(baos.toByteArray());
 	}
 	
 	/**
 	 * Build a URL with the URLBuilder utility interface.  This interface
 	 * will clean extra/missing path segment terminators and handle schemes.
 	 * 
 	 * @param segment set of segments that compose the url.
 	 * @return an instance of URLBuilder with complete url.
 	 */
 	public URLBuilder buildURL(String ... segment) {
 		URLBuilderImpl builder = new URLBuilderImpl();
 		
 		if (segment != null)
 			if (segment.length == 0)
 				return builder;
 			else if (segment.length == 1)
 				return builder.append(segment[0]);
 			else
 				for (String seg : segment)
 					builder.append(seg);
 				
 		return builder;
 	}
 	
 	/**
 	 * @param boundary String that represents form part boundary.
 	 * @return byte array of String of part header.
 	 */
 	private static byte[] getPartHeader(String boundary) {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("--");
 		sb.append(boundary);
 		sb.append(LINE_ENDING);
 		sb.append(HEADER_PARA);
 		sb.append("; ");
 		sb.append(PARA_NAME);
 		sb.append("=\"");
 		
 		return sb.toString().getBytes();
 	}
 
 	/**
 	 * Turns a map into a key=value property string.
 	 * 
 	 * @param props
 	 *            Map of <String, String> properties
 	 * @return A querystring as String
 	 * @throws IOException
 	 *             on string encoding error
 	 */
 	public static String propertyString(Map<String, String> props) throws IOException {
 		StringBuilder sb = new StringBuilder();
 				
 		for (Iterator<String> i = props.keySet().iterator(); i.hasNext();) {
 			String key = i.next();		
 			sb.append(URLEncoder.encode(key, "UTF-8"));
 			sb.append("=");
 			sb.append(URLEncoder.encode(props.get(key), "UTF-8"));
 
 			if (i.hasNext()) {
 				sb.append("&");
 			}
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Given a variable number of <String, String> pairs, construct a Map and
 	 * return it with values loaded.
 	 * 
 	 * @param elements
 	 *            name1, value1, name2, value2...
 	 * @return a Map and return it with values loaded.
 	 */
 	public static Map<String, String> toMap(String... elements) {
 		if (elements.length % 2 != 0) {
 			throw new IllegalStateException("Input parameters must be even.");
 		}
 
 		Iterator<String> i = Arrays.asList(elements).iterator();
 		Map<String, String> m = new HashMap<String, String>();
 
 		while (i.hasNext()) {
 			m.put(i.next().toString(), i.next());
 		}
 
 		return m;
 	}
 	
 // Private methods
 	
 	/**
 	 * Create an array of bytesfrom the complete contents of an InputStream.
 	 * 
 	 * @param in
 	 *            InputStream to turn into a byte array
 	 * @return byte array (byte[]) w/ contents of input stream, or null if inputstream is null.
 	 * @throws IOException
 	 *             on I/O error
 	 */
 	public static byte[] readStream(InputStream in) throws IOException {
 		if (in == null)
 			return null;
 		
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		int read = 0;
 		byte[] buff = new byte[COPY_BUFFER_SIZE];
 
 		while ((read = in.read(buff)) > 0) {
 			os.write(buff, 0, read);
 		}
 		os.close();
 
 		return os.toByteArray();
 	}
 	
 	/**
 	 * Create multipart form boundary.
 	 * 
 	 * @return boiundry as a String
 	 */
 	private static String createMultipartBoundary() {
 		if (RNG == null)
 			RNG = new Random();
 		
 		StringBuilder buf = new StringBuilder(42);
 		buf.append("---------------------------");
 
 		for (int i = 0; i < RANDOM_CHAR_COUNT; i++) {
 			if (RNG.nextBoolean())
 				buf.append((char) (RNG.nextInt(25) + 65));
 			else
 				buf.append((char) (RNG.nextInt(25) + 98));
 		}
 		
 		return buf.toString();
 	}
 
 	
 	/**
 	 * Create a byte array from the contents of an input stream.
 	 * 
 	 * @param inputStream
 	 *            InputStream to read from
 	 * @param outputStream
 	 * 			  OutputStream to write to
 	 * @return number of bytes copied
 	 * @throws IOException
 	 *             on I/O error
 	 */
 	private static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
 		
 		int read = 0;
 		long size = 0;
 		byte[] buff = new byte[COPY_BUFFER_SIZE];
 
 		while ((read = inputStream.read(buff)) > 0) {
 			outputStream.write(buff, 0, read);
 			size += read;
 		}
 		
 		outputStream.flush();
 		
 		return size;
 	}	
 	
 	/**
 	 * Throws an IllegalArgumentException if any input parameters are null.
 	 * 
 	 * @param args list of parameters.
 	 */
 	private static void validateArguments(Object ... args) {
 		for (int i = 0; i < args.length; ++i)
 			if (args[i] == null)
 				throw new IllegalArgumentException("An input parameter is null.");
 	}
 
 	/**
 	 * The default connection provider returns a HttpUrlConnection.
 	 */
 	private class DefaultConnectionProvider implements ConnectionProvider {
 
 		/* (non-Javadoc)
 		 * @see com.buglabs.util.http.RestClient.ConnectionProvider#getConnection(java.lang.String)
 		 */
 		@Override
 		public HttpURLConnection getConnection(String urlStr) throws IOException {
 			URL url = new URL(urlStr);
 			return (HttpURLConnection) url.openConnection();
 		}
 
 	}
 	
 	/**
 	 * Write the content to the request body.
 	 * @param connection associated with request
 	 * @param content content of request.  If null, no body is sent and HTTP header Content-Length is set to zero.
 	 * @throws IOException on I/O error.
 	 */
 	private void writeRequestBody(HttpURLConnection connection, byte[] content) throws IOException {
 		if (content != null) {
 			connection.setRequestProperty("Content-Length", Long.toString(content.length));
 			OutputStream outputStream = connection.getOutputStream();
 			outputStream.write(content);			
 			outputStream.close();			
 		} else {
 			connection.setRequestProperty("Content-Length", Long.toString(0));
 		}
 	}
 	
 	/**
 	 * URLBuilder Implementation for safely composing URLs.
 	 */
 	private final class URLBuilderImpl implements URLBuilder {
 		private final List<String> segments;
 		private boolean httpsScheme;
 		private List<Map.Entry<String, String>> parameters;
 		private boolean emitScheme = true;
 		private boolean emitDomain = true;
 		
 		/**
 		 * 
 		 */
 		public URLBuilderImpl() {
 			segments = new ArrayList<String>();
 			httpsScheme = false;
 		}
 		
 		/**
 		 * @param segments list of in-order segments that are used to build the url.
 		 * @param httpsScheme true if HTTPS should be used, false otherwise.
 		 * @param parameters Params of url
 		 */
 		private URLBuilderImpl(List<String> segments, boolean httpsScheme, List<Map.Entry<String, String>> parameters) {
 			this.segments = segments;
 			this.httpsScheme = httpsScheme;
 			this.parameters = parameters;
 		}
 		
 		@Override
 		public URLBuilder append(String ... sgmnts) {
 			validateArguments((Object []) sgmnts);
 			
 			if (sgmnts.length == 1)
 				appendSingle(sgmnts[0]);
 			else
 				for (String segment : sgmnts) {
 					appendSingle(segment);
 				}
 			
 			return this;
 		}
 		
 		/**
 		 * Append a single segment.
 		 * @param segment segment to append.
 		 * @return instance of URLBuilder with the new segment attached.
 		 */
 		private URLBuilder appendSingle(String segment) {
 			segment = segment.trim();
 			
 			if (segment.length() == 0)
 				return this;
 			else if (segment.indexOf('/', 1) > -1) {
 				for (String nseg : segment.split("/")) 
 					this.append(nseg);
 			} else if (segment.length() > 0) {
 				if (segment.toUpperCase().startsWith("HTTP:"))					
 						return this;
 				else if (segment.toUpperCase().startsWith("HTTPS:")) {
 					httpsScheme = true;
 					return this;
 				}
 				
 				segments.add(stripIllegalChars(segment));
 			}
 			
 			return this;
 		}
 		
 		@Override
 		public String toString() {
 			StringBuilder sb = new StringBuilder();
 			
 			if (emitScheme) {
 				if (httpsScheme)
 					sb.append("https://");
 				else 
 					sb.append("http://");
 			}			
 			
 			int count = 0;
 			for (Iterator<String> i = segments.iterator(); i.hasNext();) {
 				count++;
 				if (!emitDomain && count == 1) {
 					i.next();
 					sb.append('/');
 					continue;
 				}
 				
 				sb.append(i.next());
 				
 				if (i.hasNext())
 					sb.append('/');
 			}
 			
 			try {
 				if (parameters != null) {
 					sb.append('?');
 					for (Map.Entry<String, String> parameter : parameters) {
 						sb.append(URLEncoder.encode(parameter.getKey(), "UTF-8"));
 						sb.append('=');
 						sb.append(URLEncoder.encode(parameter.getValue(), "UTF-8"));
 						
 						if (parameters.indexOf(parameter) < (parameters.size() - 1))
 							sb.append('&');
 					}
 				}
 			} catch (UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 				
 			return sb.toString();
 		}
 
 		/**
 		 * Remove characters that should be removed from a segment before appending.
 		 * @param segment input segment
 		 * @return segment string without invalid characters.
 		 */
 		private String stripIllegalChars(String segment) {
 			return segment.replaceAll("/", "");			
 		}
 
 		@Override
 		public URLBuilder setHttps(boolean value) {
 			httpsScheme = value;
 			return this;
 		}
 		
 		@Override
 		protected Object clone() throws CloneNotSupportedException {			
 			return new URLBuilderImpl(new ArrayList<String>(segments), httpsScheme, parameters);
 		}
 
 		@Override
 		public URLBuilder copy() {
 			try {
 				return (URLBuilder) this.clone();
 			} catch (CloneNotSupportedException e) {
 				throw new RuntimeException("Invalid state", e);
 			}
 		}
 
 		@Override
 		public URLBuilder copy(String ... segments) {	
 			validateArguments((Object []) segments);
 			
 			return this.copy().append(segments);
 		}
 
 		@Override
 		public URLBuilder addParameter(final String key, final String value) {
 			validateArguments(key, value);
 			
 			if (parameters == null) {
 				parameters = new ArrayList<Map.Entry<String,String>>();
 			}
 			
 			parameters.add(new Map.Entry<String, String>() {
 				
 				@Override
 				public String setValue(String arg0) {
 					//Unimplemented
 					return null;
 				}
 				
 				@Override
 				public String getValue() {
 					return value;
 				}
 				
 				@Override
 				public String getKey() {					
 					return key;
 				}
 			});
 			
 			return this;
 		}
 
 		@Override
 		public URLBuilder emitScheme(boolean value) {
 			this.emitScheme = value;
 			return this;
 		}
 
 		@Override
 		public URLBuilder emitDomain(boolean value) {
 			this.emitDomain = value;
 			return this;
 		}
 	}
 	
 	/**
 	 * Mimics a HttpUrlConnection provider.  Acts as proxy between ContentCache and the request.
 	 *
 	 */
 	private final class CachedConnectionProvider extends HttpURLConnection {
 
 		private final byte[] content;
 		private final Map<String, List<String>> headers;
 		private final int responseCode;
 
 		public CachedConnectionProvider(byte[] content, Map<String, List<String>> headerFields, int responseCode) {
 			super(null);
 			this.content = content;			
 			this.headers = headerFields;
 			this.responseCode = responseCode;
 		}
 
 		@Override
 		public void setRequestMethod(String method) throws ProtocolException {
 			if (!method.equals("GET"))
 				throw new ProtocolException(CachedConnectionProvider.class.getName() + " only supports GET.");
 		}
 		
 		@Override
 		public void setRequestProperty(String key, String value) {			
 		}
 		
 		@Override
 		public void addRequestProperty(String key, String value) {			
 		}
 		
 		@Override
 		public void setDoInput(boolean doinput) {			
 		}
 		
 		@Override
 		public void setDoOutput(boolean dooutput) {			
 		}
 		
 		@Override
 		public int getResponseCode() throws IOException {		
 			return responseCode;
 		}
 		
 		@Override
 		public InputStream getInputStream() throws IOException {
 			return new ByteArrayInputStream(content);
 		}		
 		
 		@Override
 		public Map<String, List<String>> getHeaderFields() {			
 			return headers;
 		}
 		
 		@Override
 		public String getHeaderField(int n) {		
 			throw new RuntimeException("Unimplemented.");
 		}
 		
 		@Override
 		public String getHeaderField(String name) {	
 			if (headers.containsKey(name))
 				return headers.get(name).iterator().next();
 			
 			return null;
 		}
 		
 		@Override
 		public long getHeaderFieldDate(String name, long Default) {
 			throw new RuntimeException("Unimplemented.");
 		}
 		
 		@Override
 		public int getHeaderFieldInt(String name, int Default) {
 			throw new RuntimeException("Unimplemented.");
 		}
 		
 		@Override
 		public String getHeaderFieldKey(int n) {
 			throw new RuntimeException("Unimplemented.");			
 		}
 		
 		public Map<String, List<String>> getHeaders() {
 			return headers;
 		}
 		
 		@Override
 		public void disconnect() {		
 		}
 
 		@Override
 		public boolean usingProxy() {		
 			return false;
 		}
 
 		@Override
 		public void connect() throws IOException {			
 		}
 		
 	}
 	
 	/*
 	 * Base64 Encoding extracted from http://iharder.net/base64
 	 */
 	
 	/*
 	 * Constants for Base64 encoder.
 	 */
     private final static int BASE_64_ENCODE = 1;
     private final static int BASE_64_GZIP = 2;
     private final static int BASE_64_MAX_LINE_LENGTH = 76;
     private final static int BASE_64_DO_BREAK_LINES = 8;
     private final static byte BASE_64_NEW_LINE = (byte)'\n';
     private final static int BASE_64_URL_SAFE = 16;
     private final static int BASE_64_ORDERED = 32;   
     private final static byte BASE_64_EQUALS_SIGN = (byte)'=';
     private final static byte BASE_64_WHITE_SPACE_ENC = -5; 
     private final static byte BASE_64_EQUALS_SIGN_ENC = -1; 
     private final static byte[] BASE_64_STANDARD_ALPHABET = {
         (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
         (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
         (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
         (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
         (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
         (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
         (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
         (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
         (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
         (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
     };
     private final static byte[] BASE_64_URL_SAFE_ALPHABET = {
         (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
         (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
         (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', 
         (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
         (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
         (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
         (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', 
         (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
         (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', 
         (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'-', (byte)'_'
       };
     private final static byte[] BASE_64_ORDERED_ALPHABET = {
         (byte)'-',
         (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4',
         (byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9',
         (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
         (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
         (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
         (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
         (byte)'_',
         (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
         (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
         (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
         (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z'
       };
     private final static byte[] BASE_64_STANDARD_DECODABET = {
         -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
         -5,-5,                                      // Whitespace: Tab and Linefeed
         -9,-9,                                      // Decimal 11 - 12
         -5,                                         // Whitespace: Carriage Return
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
         -9,-9,-9,-9,-9,                             // Decimal 27 - 31
         -5,                                         // Whitespace: Space
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
         62,                                         // Plus sign at decimal 43
         -9,-9,-9,                                   // Decimal 44 - 46
         63,                                         // Slash at decimal 47
         52,53,54,55,56,57,58,59,60,61,              // Numbers zero through nine
         -9,-9,-9,                                   // Decimal 58 - 60
         -1,                                         // Equals sign at decimal 61
         -9,-9,-9,                                      // Decimal 62 - 64
         0,1,2,3,4,5,6,7,8,9,10,11,12,13,            // Letters 'A' through 'N'
         14,15,16,17,18,19,20,21,22,23,24,25,        // Letters 'O' through 'Z'
         -9,-9,-9,-9,-9,-9,                          // Decimal 91 - 96
         26,27,28,29,30,31,32,33,34,35,36,37,38,     // Letters 'a' through 'm'
         39,40,41,42,43,44,45,46,47,48,49,50,51,     // Letters 'n' through 'z'
         -9,-9,-9,-9,-9                              // Decimal 123 - 127
         ,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,       // Decimal 128 - 139
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 140 - 152
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 153 - 165
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 166 - 178
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 179 - 191
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 192 - 204
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 205 - 217
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 218 - 230
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 231 - 243
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9         // Decimal 244 - 255 
     };
     private final static byte[] BASE_64_URL_SAFE_DECODABET = {
         -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
         -5,-5,                                      // Whitespace: Tab and Linefeed
         -9,-9,                                      // Decimal 11 - 12
         -5,                                         // Whitespace: Carriage Return
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
         -9,-9,-9,-9,-9,                             // Decimal 27 - 31
         -5,                                         // Whitespace: Space
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
         -9,                                         // Plus sign at decimal 43
         -9,                                         // Decimal 44
         62,                                         // Minus sign at decimal 45
         -9,                                         // Decimal 46
         -9,                                         // Slash at decimal 47
         52,53,54,55,56,57,58,59,60,61,              // Numbers zero through nine
         -9,-9,-9,                                   // Decimal 58 - 60
         -1,                                         // Equals sign at decimal 61
         -9,-9,-9,                                   // Decimal 62 - 64
         0,1,2,3,4,5,6,7,8,9,10,11,12,13,            // Letters 'A' through 'N'
         14,15,16,17,18,19,20,21,22,23,24,25,        // Letters 'O' through 'Z'
         -9,-9,-9,-9,                                // Decimal 91 - 94
         63,                                         // Underscore at decimal 95
         -9,                                         // Decimal 96
         26,27,28,29,30,31,32,33,34,35,36,37,38,     // Letters 'a' through 'm'
         39,40,41,42,43,44,45,46,47,48,49,50,51,     // Letters 'n' through 'z'
         -9,-9,-9,-9,-9                              // Decimal 123 - 127
         ,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 128 - 139
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 140 - 152
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 153 - 165
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 166 - 178
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 179 - 191
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 192 - 204
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 205 - 217
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 218 - 230
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 231 - 243
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9         // Decimal 244 - 255 
       };
     private final static byte[] BASE_64_ORDERED_DECODABET = {
         -9,-9,-9,-9,-9,-9,-9,-9,-9,                 // Decimal  0 -  8
         -5,-5,                                      // Whitespace: Tab and Linefeed
         -9,-9,                                      // Decimal 11 - 12
         -5,                                         // Whitespace: Carriage Return
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 14 - 26
         -9,-9,-9,-9,-9,                             // Decimal 27 - 31
         -5,                                         // Whitespace: Space
         -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,              // Decimal 33 - 42
         -9,                                         // Plus sign at decimal 43
         -9,                                         // Decimal 44
         0,                                          // Minus sign at decimal 45
         -9,                                         // Decimal 46
         -9,                                         // Slash at decimal 47
         1,2,3,4,5,6,7,8,9,10,                       // Numbers zero through nine
         -9,-9,-9,                                   // Decimal 58 - 60
         -1,                                         // Equals sign at decimal 61
         -9,-9,-9,                                   // Decimal 62 - 64
         11,12,13,14,15,16,17,18,19,20,21,22,23,     // Letters 'A' through 'M'
         24,25,26,27,28,29,30,31,32,33,34,35,36,     // Letters 'N' through 'Z'
         -9,-9,-9,-9,                                // Decimal 91 - 94
         37,                                         // Underscore at decimal 95
         -9,                                         // Decimal 96
         38,39,40,41,42,43,44,45,46,47,48,49,50,     // Letters 'a' through 'm'
         51,52,53,54,55,56,57,58,59,60,61,62,63,     // Letters 'n' through 'z'
         -9,-9,-9,-9,-9                                 // Decimal 123 - 127
          ,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 128 - 139
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 140 - 152
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 153 - 165
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 166 - 178
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 179 - 191
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 192 - 204
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 205 - 217
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 218 - 230
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,     // Decimal 231 - 243
           -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9         // Decimal 244 - 255 
       };
 	 /**
      * Encodes a byte array into Base64 notation.
      * Does not GZip-compress data.
      *  
      * @param source The data to convert
      * @return The data in Base64-encoded form
      * @throws NullPointerException if source array is null
      * @since 1.4
      */
     public static String encodeBytes( byte[] source ) {
         // Since we're not going to have the GZIP encoding turned on,
         // we're not going to have an java.io.IOException thrown, so
         // we should not force the user to have to catch it.
         String encoded = null;
         try {
             encoded = encodeBytes(source, 0, source.length, 0);
         } catch (java.io.IOException ex) {
             assert false : ex.getMessage();
         }   // end catch
         assert encoded != null;
         return encoded;
     }   // end encodeBytes
     
     public static String encodeBytes( byte[] source, int off, int len, int options ) throws java.io.IOException {
         byte[] encoded = encodeBytesToBytes( source, off, len, options );
 
         // Return value according to relevant encoding.
         try {
             return new String( encoded, "US-ASCII" );
         }   // end try
         catch (java.io.UnsupportedEncodingException uue) {
             return new String( encoded );
         }   // end catch
         
     }   // end encodeBytes
     
     public static byte[] encodeBytesToBytes( byte[] source, int off, int len, int options ) throws java.io.IOException {
 
         if( source == null ){
             throw new NullPointerException( "Cannot serialize a null array." );
         }   // end if: null
 
         if( off < 0 ){
             throw new IllegalArgumentException( "Cannot have negative offset: " + off );
         }   // end if: off < 0
 
         if( len < 0 ){
             throw new IllegalArgumentException( "Cannot have length offset: " + len );
         }   // end if: len < 0
 
         if( off + len > source.length  ){
             throw new IllegalArgumentException(
             String.format( "Cannot have offset of %d and length of %d with array of length %d", off,len,source.length));
         }   // end if: off < 0
 
 
 
         // Compress?
         if( (options & BASE_64_GZIP) != 0 ) {
             java.io.ByteArrayOutputStream  baos  = null;
             java.util.zip.GZIPOutputStream gzos  = null;
             Base64OutputStream            b64os = null;
 
             try {
                 // GZip -> Base64 -> ByteArray
                 baos = new java.io.ByteArrayOutputStream();
                 b64os = new Base64OutputStream( baos, BASE_64_ENCODE | options );
                 gzos  = new java.util.zip.GZIPOutputStream( b64os );
 
                 gzos.write( source, off, len );
                 gzos.close();
             }   // end try
             catch( java.io.IOException e ) {
                 // Catch it and then throw it immediately so that
                 // the finally{} block is called for cleanup.
                 throw e;
             }   // end catch
             finally {
                 try{ gzos.close();  } catch( Exception e ){}
                 try{ b64os.close(); } catch( Exception e ){}
                 try{ baos.close();  } catch( Exception e ){}
             }   // end finally
 
             return baos.toByteArray();
         }   // end if: compress
 
         // Else, don't compress. Better not to use streams at all then.
         else {
             boolean breakLines = (options & BASE_64_DO_BREAK_LINES) != 0;
 
             //int    len43   = len * 4 / 3;
             //byte[] outBuff = new byte[   ( len43 )                      // Main 4:3
             //                           + ( (len % 3) > 0 ? 4 : 0 )      // Account for padding
             //                           + (breakLines ? ( len43 / MAX_LINE_LENGTH ) : 0) ]; // New lines
             // Try to determine more precisely how big the array needs to be.
             // If we get it right, we don't have to do an array copy, and
             // we save a bunch of memory.
             int encLen = ( len / 3 ) * 4 + ( len % 3 > 0 ? 4 : 0 ); // Bytes needed for actual encoding
             if( breakLines ){
                 encLen += encLen / BASE_64_MAX_LINE_LENGTH; // Plus extra newline characters
             }
             byte[] outBuff = new byte[ encLen ];
 
 
             int d = 0;
             int e = 0;
             int len2 = len - 2;
             int lineLength = 0;
             for( ; d < len2; d+=3, e+=4 ) {
                 encode3to4( source, d+off, 3, outBuff, e, options );
 
                 lineLength += 4;
                 if( breakLines && lineLength >= BASE_64_MAX_LINE_LENGTH )
                 {
                     outBuff[e+4] = BASE_64_NEW_LINE;
                     e++;
                     lineLength = 0;
                 }   // end if: end of line
             }   // en dfor: each piece of array
 
             if( d < len ) {
                 encode3to4( source, d+off, len - d, outBuff, e, options );
                 e += 4;
             }   // end if: some padding needed
 
 
             // Only resize array if we didn't guess it right.
             if( e <= outBuff.length - 1 ){
                 // If breaking lines and the last byte falls right at
                 // the line length (76 bytes per line), there will be
                 // one extra byte, and the array will need to be resized.
                 // Not too bad of an estimate on array size, I'd say.
                 byte[] finalOut = new byte[e];
                 System.arraycopy(outBuff,0, finalOut,0,e);
                 //System.err.println("Having to resize array from " + outBuff.length + " to " + e );
                 return finalOut;
             } else {
                 //System.err.println("No need to resize array.");
                 return outBuff;
             }
         
         }   // end else: don't compress
 
     }   // end encodeBytesToBytes
     
     private static byte[] encode3to4( 
     	    byte[] source, int srcOffset, int numSigBytes,
     	    byte[] destination, int destOffset, int options ) {
     	        
     		byte[] ALPHABET = getAlphabet( options ); 
     		
     	        //           1         2         3  
     	        // 01234567890123456789012345678901 Bit position
     	        // --------000000001111111122222222 Array position from threeBytes
     	        // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
     	        //          >>18  >>12  >> 6  >> 0  Right shift necessary
     	        //                0x3f  0x3f  0x3f  Additional AND
     	        
     	        // Create buffer with zero-padding if there are only one or two
     	        // significant bytes passed in the array.
     	        // We have to shift left 24 in order to flush out the 1's that appear
     	        // when Java treats a value as negative that is cast from a byte to an int.
     	        int inBuff =   ( numSigBytes > 0 ? ((source[ srcOffset     ] << 24) >>>  8) : 0 )
     	                     | ( numSigBytes > 1 ? ((source[ srcOffset + 1 ] << 24) >>> 16) : 0 )
     	                     | ( numSigBytes > 2 ? ((source[ srcOffset + 2 ] << 24) >>> 24) : 0 );
 
     	        switch( numSigBytes )
     	        {
     	            case 3:
     	                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
     	                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
     	                destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
     	                destination[ destOffset + 3 ] = ALPHABET[ (inBuff       ) & 0x3f ];
     	                return destination;
     	                
     	            case 2:
     	                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
     	                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
     	                destination[ destOffset + 2 ] = ALPHABET[ (inBuff >>>  6) & 0x3f ];
     	                destination[ destOffset + 3 ] = BASE_64_EQUALS_SIGN;
     	                return destination;
     	                
     	            case 1:
     	                destination[ destOffset     ] = ALPHABET[ (inBuff >>> 18)        ];
     	                destination[ destOffset + 1 ] = ALPHABET[ (inBuff >>> 12) & 0x3f ];
     	                destination[ destOffset + 2 ] = BASE_64_EQUALS_SIGN;
     	                destination[ destOffset + 3 ] = BASE_64_EQUALS_SIGN;
     	                return destination;
     	                
     	            default:
     	                return destination;
     	        }   // end switch
     	    }   // end encode3to4
     
     private final static byte[] getAlphabet( int options ) {
         if ((options & BASE_64_URL_SAFE) == BASE_64_URL_SAFE) {
             return BASE_64_URL_SAFE_ALPHABET;
         } else if ((options & BASE_64_ORDERED) == BASE_64_ORDERED) {
             return BASE_64_ORDERED_ALPHABET;
         } else {
             return BASE_64_STANDARD_ALPHABET;
         }
     }	// end getAlphabet
     
 public static class Base64OutputStream extends java.io.FilterOutputStream {
         
         private final boolean encode;
         private int     position;
         private byte[]  buffer;
         private final int     bufferLength;
         private int     lineLength;
         private final boolean breakLines;
         private final byte[]  b4;         // Scratch used in a few places
         private boolean suspendEncoding;
         private final int     options;    // Record for later
         private final byte[]  decodabet;  // Local copies to avoid extra method calls
         
         /**
          * Constructs a {@link Base64.OutputStream} in ENCODE mode.
          *
          * @param out the <tt>java.io.OutputStream</tt> to which data will be written.
          * @since 1.3
          */
         public Base64OutputStream( java.io.OutputStream out ) {
             this( out, BASE_64_ENCODE );
         }   // end constructor
         
         
         /**
          * Constructs a {@link Base64.OutputStream} in
          * either ENCODE or DECODE mode.
          * <p>
          * Valid options:<pre>
          *   ENCODE or DECODE: Encode or Decode as data is read.
          *   DO_BREAK_LINES: don't break lines at 76 characters
          *     (only meaningful when encoding)</i>
          * </pre>
          * <p>
          * Example: <code>new Base64.OutputStream( out, Base64.ENCODE )</code>
          *
          * @param out the <tt>java.io.OutputStream</tt> to which data will be written.
          * @param options Specified options.
          * @see Base64#ENCODE
          * @see Base64#DECODE
          * @see Base64#DO_BREAK_LINES
          * @since 1.3
          */
         public Base64OutputStream( java.io.OutputStream out, int options ) {
             super( out );
             this.breakLines   = (options & BASE_64_DO_BREAK_LINES) != 0;
             this.encode       = (options & BASE_64_ENCODE) != 0;
             this.bufferLength = encode ? 3 : 4;
             this.buffer       = new byte[ bufferLength ];
             this.position     = 0;
             this.lineLength   = 0;
             this.suspendEncoding = false;
             this.b4           = new byte[4];
             this.options      = options;
             this.decodabet    = getDecodabet(options);
         }   // end constructor
         
         
         /**
          * Writes the byte to the output stream after
          * converting to/from Base64 notation.
          * When encoding, bytes are buffered three
          * at a time before the output stream actually
          * gets a write() call.
          * When decoding, bytes are buffered four
          * at a time.
          *
          * @param theByte the byte to write
          * @since 1.3
          */
         @Override
         public void write(int theByte) 
         throws java.io.IOException {
             // Encoding suspended?
             if( suspendEncoding ) {
                 this.out.write( theByte );
                 return;
             }   // end if: supsended
             
             // Encode?
             if( encode ) {
                 buffer[ position++ ] = (byte)theByte;
                 if( position >= bufferLength ) { // Enough to encode.
                 
                     this.out.write( encode3to4( b4, buffer, bufferLength, options ) );
 
                     lineLength += 4;
                     if( breakLines && lineLength >= BASE_64_MAX_LINE_LENGTH ) {
                         this.out.write( BASE_64_NEW_LINE );
                         lineLength = 0;
                     }   // end if: end of line
 
                     position = 0;
                 }   // end if: enough to output
             }   // end if: encoding
 
             // Else, Decoding
             else {
                 // Meaningful Base64 character?
                 if( decodabet[ theByte & 0x7f ] > BASE_64_WHITE_SPACE_ENC ) {
                     buffer[ position++ ] = (byte)theByte;
                     if( position >= bufferLength ) { // Enough to output.
                     
                         int len = decode4to3( buffer, 0, b4, 0, options );
                         out.write( b4, 0, len );
                         position = 0;
                     }   // end if: enough to output
                 }   // end if: meaningful base64 character
                 else if( decodabet[ theByte & 0x7f ] != BASE_64_WHITE_SPACE_ENC ) {
                     throw new java.io.IOException( "Invalid character in Base64 data." );
                 }   // end else: not white space either
             }   // end else: decoding
         }   // end write
         
         
         
         /**
          * Calls {@link #write(int)} repeatedly until <var>len</var> 
          * bytes are written.
          *
          * @param theBytes array from which to read bytes
          * @param off offset for array
          * @param len max number of bytes to read into array
          * @since 1.3
          */
         @Override
         public void write( byte[] theBytes, int off, int len ) 
         throws java.io.IOException {
             // Encoding suspended?
             if( suspendEncoding ) {
                 this.out.write( theBytes, off, len );
                 return;
             }   // end if: supsended
             
             for( int i = 0; i < len; i++ ) {
                 write( theBytes[ off + i ] );
             }   // end for: each byte written
             
         }   // end write
         
         
         
         /**
          * Method added by PHIL. [Thanks, PHIL. -Rob]
          * This pads the buffer without closing the stream.
          * @throws java.io.IOException  if there's an error.
          */
         public void flushBase64() throws java.io.IOException  {
             if( position > 0 ) {
                 if( encode ) {
                     out.write( encode3to4( b4, buffer, position, options ) );
                     position = 0;
                 }   // end if: encoding
                 else {
                     throw new java.io.IOException( "Base64 input not properly padded." );
                 }   // end else: decoding
             }   // end if: buffer partially full
 
         }   // end flush
 
         
         /** 
          * Flushes and closes (I think, in the superclass) the stream. 
          *
          * @since 1.3
          */
         @Override
         public void close() throws java.io.IOException {
             // 1. Ensure that pending characters are written
             flushBase64();
 
             // 2. Actually close the stream
             // Base class both flushes and closes.
             super.close();
             
             buffer = null;
             out    = null;
         }   // end close
         
         
         
         /**
          * Suspends encoding of the stream.
          * May be helpful if you need to embed a piece of
          * base64-encoded data in a stream.
          *
          * @throws java.io.IOException  if there's an error flushing
          * @since 1.5.1
          */
         public void suspendEncoding() throws java.io.IOException  {
             flushBase64();
             this.suspendEncoding = true;
         }   // end suspendEncoding
         
         
         /**
          * Resumes encoding of the stream.
          * May be helpful if you need to embed a piece of
          * base64-encoded data in a stream.
          *
          * @since 1.5.1
          */
         public void resumeEncoding() {
             this.suspendEncoding = false;
         }   // end resumeEncoding
         
         
         
     }   // end inner class OutputStream
 
 	private final static byte[] getDecodabet( int options ) {
 	    if( (options & BASE_64_URL_SAFE) == BASE_64_URL_SAFE) {
 	        return BASE_64_URL_SAFE_DECODABET;
 	    } else if ((options & BASE_64_ORDERED) == BASE_64_ORDERED) {
 	        return BASE_64_ORDERED_DECODABET;
 	    } else {
 	        return BASE_64_STANDARD_DECODABET;
 	    }
 	}	// end getAlphabet
 	
 	private static byte[] encode3to4( byte[] b4, byte[] threeBytes, int numSigBytes, int options ) {
         encode3to4( threeBytes, 0, numSigBytes, b4, 0, options );
         return b4;
     }   // end encode3to4
 	
 	private static int decode4to3( 
 		    byte[] source, int srcOffset, 
 		    byte[] destination, int destOffset, int options ) {
 		        
 		        // Lots of error checking and exception throwing
 		        if( source == null ){
 		            throw new NullPointerException( "Source array was null." );
 		        }   // end if
 		        if( destination == null ){
 		            throw new NullPointerException( "Destination array was null." );
 		        }   // end if
 		        if( srcOffset < 0 || srcOffset + 3 >= source.length ){
 		            throw new IllegalArgumentException( String.format(
 		            "Source array with length %d cannot have offset of %d and still process four bytes.", source.length, srcOffset ) );
 		        }   // end if
 		        if( destOffset < 0 || destOffset +2 >= destination.length ){
 		            throw new IllegalArgumentException( String.format(
 		            "Destination array with length %d cannot have offset of %d and still store three bytes.", destination.length, destOffset ) );
 		        }   // end if
 		        
 		        
 		        byte[] DECODABET = getDecodabet( options ); 
 			
 		        // Example: Dk==
 		        if( source[ srcOffset + 2] == BASE_64_EQUALS_SIGN ) {
 		            // Two ways to do the same thing. Don't know which way I like best.
 		          //int outBuff =   ( ( DECODABET[ source[ srcOffset    ] ] << 24 ) >>>  6 )
 		          //              | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
 		            int outBuff =   ( ( DECODABET[ source[ srcOffset    ] ] & 0xFF ) << 18 )
 		                          | ( ( DECODABET[ source[ srcOffset + 1] ] & 0xFF ) << 12 );
 		            
 		            destination[ destOffset ] = (byte)( outBuff >>> 16 );
 		            return 1;
 		        }
 		        
 		        // Example: DkL=
 		        else if( source[ srcOffset + 3 ] == BASE_64_EQUALS_SIGN ) {
 		            // Two ways to do the same thing. Don't know which way I like best.
 		          //int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
 		          //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
 		          //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
 		            int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] & 0xFF ) << 18 )
 		                          | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
 		                          | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6 );
 		            
 		            destination[ destOffset     ] = (byte)( outBuff >>> 16 );
 		            destination[ destOffset + 1 ] = (byte)( outBuff >>>  8 );
 		            return 2;
 		        }
 		        
 		        // Example: DkLE
 		        else {
 		            // Two ways to do the same thing. Don't know which way I like best.
 		          //int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
 		          //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
 		          //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
 		          //              | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
 		            int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] & 0xFF ) << 18 )
 		                          | ( ( DECODABET[ source[ srcOffset + 1 ] ] & 0xFF ) << 12 )
 		                          | ( ( DECODABET[ source[ srcOffset + 2 ] ] & 0xFF ) <<  6)
 		                          | ( ( DECODABET[ source[ srcOffset + 3 ] ] & 0xFF )      );
 
 		            
 		            destination[ destOffset     ] = (byte)( outBuff >> 16 );
 		            destination[ destOffset + 1 ] = (byte)( outBuff >>  8 );
 		            destination[ destOffset + 2 ] = (byte)( outBuff       );
 
 		            return 3;
 		        }
 		    }   // end decodeToBytes
 }
