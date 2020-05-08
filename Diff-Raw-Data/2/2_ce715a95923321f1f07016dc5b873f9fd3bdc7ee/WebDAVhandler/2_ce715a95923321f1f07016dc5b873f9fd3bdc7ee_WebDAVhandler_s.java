 package de.binaervarianz.sendtowebdav;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 
 import android.text.format.DateFormat;
 import android.util.Log;
 
 public class WebDAVhandler {
 	private final String TAG = this.getClass().getName();
 	
 	private String serverURI;
 	private String user;
 	private String pass;
 	private boolean trustAllSSLCerts;
 	
 	public WebDAVhandler(String serverURI, String user, String pass) {
 		super();
 		this.serverURI = serverURI;
 		this.user = user;
 		this.pass = pass;
 	}
 	
 	/**
 	 * Connects to the previously saved server address and puts a file with the given name and content there.
 	 * 
 	 * @param filename
 	 * @param path
 	 * @param data : file contents
 	 * @return boolean evaluating the http response code
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public void putFile(String filename, String path, String data)
 			throws IllegalArgumentException, ClientProtocolException, IOException, HttpException {
 
 		HttpPut put = new HttpPut(serverURI + "/" + path + filename);
 		try {
 			put.setEntity(new StringEntity(data, "UTF8"));
 		} catch (UnsupportedEncodingException e) {
 			Log.e(TAG, "UnsupportedEncoding: ", e);
 		}
 		put.addHeader("Content-type", "text/plain");
 
 		DefaultHttpClient http = this.prepareHttpClient(user, pass);
 		HttpResponse response = http.execute(put);
 		StatusLine responseStatus = response.getStatusLine();		
 
 		// debug
 		Log.d(TAG, "StatusLine: "
 				+ responseStatus.toString() + ", "
 				+ " URL: " + serverURI);
 		
 		// evaluate the HTTP response status code
 		if (responseStatus.getStatusCode() >= 400)
 			throw new HttpException(responseStatus.toString());
 	}
 	
 	/**
 	 * Connects to the previously saved server address and puts a binary file with the given name and content there.
 	 * 
 	 * @param filename String with filename to be created on the server
 	 * @param path String with the server side path to put the file
 	 * @param filePath String with the client side (Android) path to the source file
 	 * @param type : String with MIME type of the data
 	 * @return boolean evaluating the http response code
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public void putBinFile(String filename, String path, String filePath, String type)
 			throws IllegalArgumentException, ClientProtocolException, IOException, HttpException {		
 		
 		HttpPut put = new HttpPut(serverURI + "/" + path + filename);	
 		put.setEntity(new FileEntity(new File(filePath), type));
 				
 		put.addHeader("Content-type", type);
 
 		DefaultHttpClient http = this.prepareHttpClient(user, pass);
 		Log.d(TAG, "http client created");
 		HttpResponse response = http.execute(put);
 		StatusLine responseStatus = response.getStatusLine();		
 
 		// debug
 		Log.d(TAG, "StatusLine: "
 				+ responseStatus.toString() + ", "
 				+ " URL: " + serverURI);
 		
 		// evaluate the HTTP response status code
 		if (responseStatus.getStatusCode() >= 400)
 			throw new HttpException(responseStatus.toString());
 	}
 	
 	/**
 	 * Tests the connection by sending a GET request (no evaluation of results so far, just throwing exceptions if failing)
 	 */
 	public void testConnection() throws IllegalArgumentException, ClientProtocolException, IOException, HttpException {
 		
 		SimpleDateFormat dateformater = new SimpleDateFormat("yyyyMMddHHmmss");
		String timestamp = dateformater.format(new Date())
 		putFile("ConnectionTest-"+ timestamp +".txt", "", "please delete!");
 		
 		// TODO: try to silently delete the file again; ignore errors/exceptions along the way
 	}
 	
 	/**
 	 * Creates a new Connection Manager needed for http/https communication
 	 * @param params
 	 * @return
 	 */
 	private ClientConnectionManager getConManager (HttpParams params) {
 		SchemeRegistry registry = new SchemeRegistry();
 	    registry.register(new Scheme("http", new PlainSocketFactory(), 80));
 	    registry.register(new Scheme("https", (trustAllSSLCerts ? new TrustAllSocketFactory() : SSLSocketFactory.getSocketFactory()), 443));
 		return new ThreadSafeClientConnManager(params, registry);
 	}
 	
 	/**
 	 * Create a DefaultHttpClient instance preconfigured with username and password
 	 * @param user
 	 * @param pass
 	 * @return
 	 */
 	private DefaultHttpClient prepareHttpClient(String user, String pass) {
 		BasicCredentialsProvider credProvider = new BasicCredentialsProvider();
 		credProvider.setCredentials(AuthScope.ANY,
 				new UsernamePasswordCredentials(user, pass));		
 		
 	    HttpParams params = new BasicHttpParams();
 		DefaultHttpClient http = new DefaultHttpClient(this.getConManager(params), params);
 		http.setCredentialsProvider(credProvider);
 		
 		return http;
 	}
 	
 	public String getServerURI() {
 		return serverURI;
 	}
 
 	public void setServerURI(String serverURI) {
 		this.serverURI = serverURI;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 	public boolean isTrustAllSSLCerts() {
 		return trustAllSSLCerts;
 	}
 
 	public void setTrustAllSSLCerts(boolean trustAllSSLCerts) {
 		this.trustAllSSLCerts = trustAllSSLCerts;
 	}
 }
