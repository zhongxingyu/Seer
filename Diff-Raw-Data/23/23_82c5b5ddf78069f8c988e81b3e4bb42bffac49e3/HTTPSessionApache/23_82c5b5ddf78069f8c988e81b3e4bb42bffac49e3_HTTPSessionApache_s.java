 /**
  * 
  */
 package com.saplo.api.client;
 
 import java.io.IOException;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NoHttpResponseException;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.saplo.api.client.TransportRegistry.SessionFactory;
 import com.saplo.api.client.entity.JSONRPCRequestObject;
 import com.saplo.api.client.entity.JSONRPCResponseObject;
 
 /**
  * @author progre55
  *
  */
 public class HTTPSessionApache implements Session {
 
 //	private static Log logger = LogFactory.getLog(HTTPSessionApache.class);
 	protected URI uri;
 	protected volatile String params;
 	protected HttpHost proxy = new HttpHost("localhost");
 	protected ClientProxy clientProxy = null;
	protected UsernamePasswordCredentials proxyCredentials = null;
	protected AuthScope proxyScope = null;
 
 	public HTTPSessionApache(URI uri, String params) {
 		this.uri = uri;
 		this.params = params;
 	}
 	
 	public HTTPSessionApache(URI uri, String params, ClientProxy clientProxy) {
 		this(uri, params);
 		this.setProxy(clientProxy);
 	}
 
 	public JSONRPCResponseObject sendAndReceive(JSONRPCRequestObject message)
 	throws JSONException, SaploClientException {
 
 		HttpPost httpost = new HttpPost(uri+"?"+params);
 
 		ByteArrayEntity ent = new ByteArrayEntity(message.toString().getBytes(Charset.forName("UTF-8")));
 		ent.setContentEncoding(HTTP.UTF_8);
 		ent.setContentType("application/json");
 		httpost.setEntity(ent);
 				
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 		try {
 			if(clientProxy != null) {
				if(clientProxy.isSecure())
					httpClient.getCredentialsProvider().setCredentials(proxyScope, proxyCredentials);
 				
 				httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 			}
 			
 			HttpResponse response = httpClient.execute(httpost);
 			HttpEntity entity = response.getEntity();
 			int statusCode = response.getStatusLine().getStatusCode();
 
 			if (statusCode != HttpStatus.SC_OK)
 				// probably the API is down..
 				throw new SaploClientException(ResponseCodes.MSG_API_DOWN_EXCEPTION, ResponseCodes.CODE_API_DOWN_EXCEPTION, statusCode);
 
 			String got = "";
 			if (entity != null) {
 				byte[] bytes = EntityUtils.toByteArray(entity);
 				got = new String(bytes, Charset.forName("UTF-8"));
 			}
 
 			JSONTokener tokener = new JSONTokener(got);
 			Object rawResponseMessage = tokener.nextValue();
 			JSONObject responseMessage = (JSONObject) rawResponseMessage;
 			if (responseMessage == null)
 				throw new ClientError("Invalid response type - " + rawResponseMessage);
 			return new JSONRPCResponseObject(responseMessage);
 
 		} catch (ClientProtocolException e) {
 			throw new ClientError(e);
 		} catch (NoHttpResponseException nr) {
 			// TODO what code to send here? 404? for now, just send 777 
 			// cause 404 is thrown when response.getStatusLine().getStatusCode() == 404 above
 			throw new SaploClientException(ResponseCodes.MSG_API_DOWN_EXCEPTION, ResponseCodes.CODE_API_DOWN_EXCEPTION, 777);
 		} catch (IOException e) {
 			throw new ClientError(e);
 		} finally {
 			httpClient.getConnectionManager().shutdown();
 		}
 	}
 
 	public void setParams(String params) {
 		this.params = params;
 	}
 
 	/**
 	 * Set a proxy of type ClientProxy to use for this transport connections
 	 * 
 	 * @param proxy
 	 */
 	public void setProxy(ClientProxy clientProxy) {
 		this.clientProxy = clientProxy;
 		if(clientProxy.isSecure()) {
			this.proxyScope = new AuthScope(clientProxy.getHost(), clientProxy.getPort());
			this.proxyCredentials = new UsernamePasswordCredentials(
					clientProxy.getUsername(), clientProxy.getPassword());
 		}
 	}
 	
 	/**
 	 * Close all the clients and clear the pool.
 	 */
 	public synchronized void close() {
 	}
 
 	static class SessionFactoryImpl implements SessionFactory {
 		volatile HashMap<URI, Session> sessionMap = new HashMap<URI, Session>();
 
 		public Session newSession(URI uri, String params, ClientProxy proxy) {
 			Session session = sessionMap.get(uri);
 			if (session == null) {
 				synchronized (sessionMap) {
 					session = sessionMap.get(uri);
 					if(session == null) {
 						if(proxy != null)
 							session = new HTTPSessionApache(uri, params, proxy);
 						else
 							session = new HTTPSessionApache(uri, params);
 						sessionMap.put(uri, session);
 					}
 				}
 			}
 			return session;
 		}
 	}
 
 	/**
 	 * Register this transport in 'registry'
 	 */
 	public static void register(TransportRegistry registry) {
 		registry.registerTransport("http", new SessionFactoryImpl());
 	}
 
 	/**
 	 * De-register this transport from the 'registry'
 	 */
 	public static void deregister(TransportRegistry registry) {
 		registry.deregisterTransport("http");
 	}
 
 }
