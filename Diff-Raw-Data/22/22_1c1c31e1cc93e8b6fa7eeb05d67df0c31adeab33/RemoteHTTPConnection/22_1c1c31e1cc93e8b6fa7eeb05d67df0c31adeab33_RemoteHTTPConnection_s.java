 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2012 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.storage;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthProtocolState;
 import org.apache.http.auth.AuthScheme;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.AuthState;
 import org.apache.http.auth.Credentials;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.client.protocol.RequestAuthCache;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.ExecutionContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.wwscc.util.CancelException;
 import org.wwscc.util.MonitorProgressStream;
 import org.wwscc.util.Prefs;
 
 /**
  * Wraps Apache HTTPClient to provide access /dbserve URL methods like download, upload, sqlmap, etc.
  */
 public class RemoteHTTPConnection
 {
 	private static final Logger log = Logger.getLogger(RemoteHTTPConnection.class.getCanonicalName());
 
 	DefaultHttpClient httpclient;
 	HttpContext context;
 	String hostname;
 
 	public RemoteHTTPConnection() throws IOException
 	{
 		this(Prefs.getHomeServer());
 	}
 
 	public RemoteHTTPConnection(String remote) throws IOException
 	{
 		hostname = remote;
 		
 		httpclient = new DefaultHttpClient();
 		HttpProtocolParams.setUserAgent(httpclient.getParams(), "Scorekeeper/2.0");
 		httpclient.removeRequestInterceptorByClass(RequestAuthCache.class);
 		httpclient.addRequestInterceptor(new URLBasedAuthCache(), 7);
 			
 		CredentialsProvider creds = httpclient.getCredentialsProvider();
 		for (Map.Entry<String,String> entry : Prefs.getPasswords().entrySet())
 			creds.setCredentials(produceScope(entry.getKey()), produceCredentials(entry.getValue()));
 		
 		context = new BasicHttpContext();
 	}
 	
 	
 	/**
 	 * Worker function to actual perform the HTTP request/response and take car of error or authentication return values.
 	 * @param request the request
 	 * @param database the database name in question
 	 * @return a response object if things complete successfully.
 	 * @throws IOException
 	 * @throws ClientProtocolException
 	 */
 	public HttpResponse execute(HttpUriRequest request, String database) throws IOException, ClientProtocolException 
 	{
 		while (true)
 		{
 			HttpResponse response = httpclient.execute(request, context);
 			if (response.getStatusLine().getStatusCode() == 401)  // try and get new credentials from user cause something failed
 			{
 				String s = JOptionPane.showInputDialog("Password not accepted for " + database + ", please enter the proper password: ");
 				if (s == null) 
 					throw new CancelException("No input for password dialog");
 				
 				Prefs.setPasswordFor(database, s);
 				httpclient.getCredentialsProvider().setCredentials(produceScope(database), produceCredentials(s));
 				EntityUtils.consume(response.getEntity());
 				continue;
 			}
 			else if (response.getStatusLine().getStatusCode() != 200)
 			{
 				String error = EntityUtils.toString(response.getEntity());
 				if (error.contains("<body")) {
 					error = error.substring(error.indexOf("<body")+6, error.length()-14);
 				}
 
 				if (error.length() > 600) {
 					error = "oversized error from backend";
 				}
 
 				throw new IOException(error);
 			}
 			
 			return response;
 		}
 		
 	}
 
 	/**
 	 * Call to sqlmap to perform operations on the remote database
 	 * @param dbname the database name
 	 * @param data the encoded request data
 	 * @return the encoded response data if 200 response
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	public byte[] performSQL(String dbname, byte[] data) throws IOException, URISyntaxException
 	{
         ByteArrayEntity entity = new ByteArrayEntity(data);
         HttpPost sqlmap = new HttpPost(URIs.sqlmap(hostname, dbname));
         sqlmap.setEntity(new CountingEntity("Remote SQL", entity));		        
         return EntityUtils.toByteArray(new CountingEntity("SQL Results", execute(sqlmap, dbname).getEntity()));
 	}
 
 	
 	/**
 	 * Get the list of available databases on the server.  The return value map contains keys based
 	 * on the status of the database, currently "locked" or "unlocked".
 	 * @return a map of status to lists of names
 	 * @throws IOException
 	 * @throws URISyntaxException 
 	 */
 	public Map<String, List<String>> getAvailableForCheckout() throws IOException, URISyntaxException
 	{
 		Map<String, List<String>> ret = new HashMap<String, List<String>>();
 		ret.put("unlocked", new ArrayList<String>());
 		ret.put("locked", new ArrayList<String>());
 		
 		HttpGet available = new HttpGet(URIs.available(hostname));
 		HttpEntity resEntity = new CountingEntity("Download", execute(available, "error").getEntity());
 		String data = EntityUtils.toString(resEntity);
 		for (String db : data.split("\n"))
 		{
 			String[] parts = db.split("\\s+");
 			if (parts.length < 3)
 			{
 				log.log(Level.INFO, "Invalid data from available: {0}", db);
 				continue;
 			}
 			
 			String name = parts[0];
 			boolean locked = parts[1].trim().equals("1");
 			boolean archived = parts[2].trim().equals("1");
 			if (archived) continue;
 			ret.get(locked ? "locked" : "unlocked").add(name);
 		}
 
 		for (List<String> list : ret.values())
 			Collections.sort(list);
 		return ret;
 	}
 
 	/**
 	 * Download a database to a location file, optionally locked it server side
 	 * @param dst the location file to write to, we determine name of remote database from the this name
 	 * @param lockServerSide true if server side should be locked.
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	public void downloadDatabase(File dst, boolean lockServerSide) throws IOException, URISyntaxException
 	{
 		String name = dst.getName();
 		this.downloadDatabase(dst, name.substring(0, name.indexOf('.')), lockServerSide);
 	}
 
 	/**
 	 * Same a counterpart but we specify the remote name rather than determine it from the file name.
 	 * @param dst the location file to write to
 	 * @param remotename the remote database name
 	 * @param lockServerSide true if server side should be locked
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	public void downloadDatabase(File dst, String remotename, boolean lockServerSide) throws IOException, URISyntaxException
 	{	
 		HttpGet download = new HttpGet((lockServerSide) ? URIs.download(hostname, remotename) : URIs.copy(hostname, remotename));
 		HttpEntity resEntity = new CountingEntity("Download", execute(download, remotename).getEntity());
 		FileOutputStream output = new FileOutputStream(dst);
 		resEntity.writeTo(output);
 	}
 
 	/**
 	 * Upload a database from the provided file to the remote server
 	 * @param f the location file to read from, we determine name of remote database from the this name
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	public void uploadDatabase(File f) throws IOException, URISyntaxException
 	{
 		String dbname = f.getName().substring(0, f.getName().indexOf('.'));
		
        MultipartEntity entity = new MultipartEntity();
        entity.addPart("db", new FileBody(f, dbname, ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
        HttpPost upload = new HttpPost(URIs.download(hostname, dbname));
        upload.setEntity(new CountingEntity("Upload", entity));			
         EntityUtils.consume(execute(upload, dbname).getEntity());
 	}
 
 
 	/**
 	 * Utility so that everyone uses the same authscope for determining authentication paramters.
 	 * @param seriesname the series name to get the authscope for
 	 * @return a new AuthScope object that should match the realm/type for the database from the server
 	 */
 	protected static AuthScope produceScope(String seriesname) {
 		return new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, seriesname, "digest");
 	}
 	
 	protected static Credentials produceCredentials(String password) {
 		return new UsernamePasswordCredentials("admin", password);
 	}
 	
 	
 	/**
 	 * Single location for specification of URLs
 	 */
 	static class URIs {
 		public static URI upload(String host, String db) throws URISyntaxException { return base(host, db, "upload"); }
 		public static URI download(String host, String db) throws URISyntaxException { return base(host, db, "download"); }
 		public static URI copy(String host, String db) throws URISyntaxException { return base(host, db, "copy"); }
 		public static URI sqlmap(String host, String db) throws URISyntaxException { return base(host, db, "sqlmap"); }
 		public static URI available(String host) throws URISyntaxException { return new URI("http", host, "/dbserve/available", null); }
 		private static URI base(String host, String db, String action) throws URISyntaxException { return new URI("http", host, String.format("/dbserve/%s/%s", db, action), null); } 
 	}
 
 	/**
 	 * Wrap an HttpEntity so that we can attach a progress bar watcher to it
 	 */
 	static class CountingEntity extends HttpEntityWrapper
 	{
 		String title;
 		public CountingEntity(String monitorTitle, HttpEntity wrapped) { 
 			super(wrapped);
 			title = monitorTitle;
 		}
 		@Override
 		public void writeTo(final OutputStream out) throws IOException {
 			if (out instanceof MonitorProgressStream)
 				wrappedEntity.writeTo(out);
 			else
 				wrappedEntity.writeTo(new MonitorProgressStream(title, out, wrappedEntity.getContentLength()));
 		}
 	}
 	
 	/**
 	 * We replace the authcaching portion of HttpClient as it retrieves the password for ANY realm, not the one we want.
 	 * Note, we forgo alot of the error checking as its already been down a hundred times by the other interceptors. 
 	 */
 	static class URLBasedAuthCache implements HttpRequestInterceptor 
 	{
 	    public URLBasedAuthCache() {
 	        super();
 	    }
 
 	    public void process(final HttpRequest request, final HttpContext reqcontext) throws HttpException, IOException 
 	    {
 	        AuthCache authCache = (AuthCache) reqcontext.getAttribute(ClientContext.AUTH_CACHE);
 	        AuthState targetState = (AuthState) reqcontext.getAttribute(ClientContext.TARGET_AUTH_STATE);
 	        CredentialsProvider credsProvider = (CredentialsProvider) reqcontext.getAttribute(ClientContext.CREDS_PROVIDER);
 	        HttpHost target = (HttpHost) reqcontext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
 	        
 	        if (target != null && targetState != null && authCache != null && targetState.getState() == AuthProtocolState.UNCHALLENGED) {
 	            AuthScheme authScheme = authCache.get(target);
 	            if (authScheme != null) {
 	                //doPreemptiveAuth
 	            	String[] bits = request.getRequestLine().getUri().split("/");  // magic time, we know the URL scheme  (/{controller}/{seriesname}/...)
 	    	        Credentials creds = credsProvider.getCredentials(produceScope(bits[2]));
 	    	        if (creds != null) {
 	    	        	targetState.setState(AuthProtocolState.SUCCESS);
 	    	        	targetState.update(authScheme, creds);
 	    	        } 
 	            }
 	        }
 	    }
 	}
 }
