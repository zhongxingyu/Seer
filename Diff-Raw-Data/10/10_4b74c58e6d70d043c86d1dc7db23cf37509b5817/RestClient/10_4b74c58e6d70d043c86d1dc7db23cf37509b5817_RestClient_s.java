 /*
  * This file is in the public domain, furnished "as is", without technical
  * support, and with no warranty, express or implied, as to its usefulness for
  *	any purpose.
  */
 package org.touge.restclient;
 
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
 
import org.touge.restclient.Base64;

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
			if (code > 0)
				throw new IOException("HTTP Error " + code + " was returned from the server: " + message);
			else 
				throw new IOException("A non-HTTP error was returned from the server.");
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
 			field = "Basic " + Base64.encodeBytes(userpass.getBytes()); 
 		}
 		
 		@Override
 		public void initialize(HttpURLConnection connection) {
 			connection.addRequestProperty("Authorization", field);
 		}		
 	}
 
 	private static Random RNG;
 
 	private ConnectionProvider connectionProvider;
 
 	private final List<ConnectionInitializer> connectionInitializers;
 	
 	private ErrorHandler errorHandler;
 	private PrintWriter debugStream;
 	//private StringBuilder debugBuffer;
 	private SimpleDateFormat debugTimeFormat;
 		
 	/**
 	 * Default constructor.
 	 */
 	public RestClient() {
 		this.connectionProvider = new DefaultConnectionProvider();
 		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
 		this.errorHandler = null;
 	}
 	
 	/**
 	 * @param connectionProvider ConnectionProvider
 	 */
 	public RestClient(ConnectionProvider connectionProvider) {
 		this.connectionProvider = connectionProvider;
 		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
 		this.errorHandler = null;
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
 		connectionInitializers.add(initializer);		
 	}
 	
 	// Public methods
 	
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
 		
 		String httpUrl = url;
 		if (!url.toLowerCase().startsWith("http://"))
 			httpUrl = "http://" + url;
 		
 		StringBuilder debugBuffer = null;
 		if (debugStream != null)
 			debugBuffer = debugStart(httpUrl, method.toString());
 				
 		final HttpURLConnection connection = connectionProvider.getConnection(httpUrl);
 		connection.setRequestMethod(method.toString());
 		
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
 				
 				if (deserializer == null) {
 					// If no deserializer is specified, use String.
 					T response = (T) RestClient.STRING_DESERIALIZER.deserialize(connection.getInputStream(), 0, null);
 					done = true;
 					
 					if (responseBuffer != null) {
 						debugMid(responseBuffer, response.toString());
 						debugEnd(responseBuffer);
 					}
 					
 					return response;
 				}
 				
 				T response = deserializer.deserialize(connection.getInputStream(), 
 						connection.getResponseCode(), connection.getHeaderFields());
 				
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
 	 * 
 	 * @param url of server  If not String, toString() will be called.
 	 * @param deserializer class that can deserialize content into desired type.
 	 * @return type specified by deserializer
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callGet(Object url, Map<String, String> headers, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.GET, url.toString(), deserializer, null, headers);
 	}
 	
 	/**
 	 * Execute GET method and deserialize response.
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
 	 * 
 	 * @param url url of server
 	 * @param body body of post as an input stream
 	 * @return a response to the request
 	 * @throws IOException on I/O error
 	 */
 	public <T> Response<T> callPost(Object url, InputStream body, ResponseDeserializer<T> deserializer) throws IOException {
 		return call(HttpMethod.POST, url.toString(), deserializer, body, null);
 	}
 	
 	/**
 	 * Send a POST to the server.
 	 * 
 	 * @param url url of server.   If not String, toString() will be called.
 	 * @param formData Form data as strings.  
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
 		 */
 		private URLBuilderImpl(List<String> segments, boolean httpsScheme) {
 			this.segments = segments;
 			this.httpsScheme = httpsScheme;
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
 			return new URLBuilderImpl(new ArrayList<String>(segments), httpsScheme);
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
 }
