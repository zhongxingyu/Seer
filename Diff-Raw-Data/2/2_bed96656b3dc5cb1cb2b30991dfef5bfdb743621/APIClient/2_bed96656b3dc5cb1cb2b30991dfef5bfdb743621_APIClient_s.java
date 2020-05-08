 package ru.omsu.avangard.omsk.services.network;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.params.ConnManagerPNames;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import ru.omsu.avangard.omsk.services.protocol.RequestBuildingException;
 import ru.omsu.avangard.omsk.services.protocol.Requests.Request;
 import ru.omsu.avangard.omsk.services.protocol.Responses.Response;
 import android.util.Log;
 
 public class APIClient {
 
 	private static final String TAG = APIClient.class.getSimpleName();
 
 	public Response execute(Request request, Class<? extends Response> responseClass) throws APIClientException {
 		try {
 			Log.v(TAG, "------- starting exectute");
 			Log.v(TAG, ": request class " + request.getClass().getSimpleName());
 			Log.v(TAG, ": request data " + request);
 			Log.v(TAG, ": response class " + responseClass.getSimpleName());
 
 			HttpRequestBase httpRequest = request.toHttpRequest();
 
 			Log.v(TAG, ": will send " + httpRequest.getMethod() + " http request to URI " + httpRequest.getURI());
 			if (httpRequest.getMethod().equalsIgnoreCase("POST")) {
 				HttpEntity entity = ((HttpPost) httpRequest).getEntity();
 				if(entity instanceof UrlEncodedFormEntity){
 					Log.v(TAG, ": post data > " + EntityUtils.toString(entity) + " <");
 				}
 			}
 			final HttpResponse httpResponse = mHttpClient.execute(httpRequest);
 			int responseCode = httpResponse.getStatusLine().getStatusCode();
 
 			Log.v(TAG, ": HTTP response code = " + responseCode);
 			if (responseCode == HttpStatus.SC_OK) {
				String responseData = EntityUtils.toString(httpResponse.getEntity());
 				Log.v(TAG, ": HTTP response data: ");
 				Log.v(TAG, responseData);
 				try {
 					final Response response = responseClass.newInstance();
 					response.fromXML(responseData);
 					Log.v(TAG, ": OK. parsed response = " + response);
 					return response;
 				} catch (Exception e) {
 					Log.v(TAG, ": ERROR. while parsing response", e);
 					throw new APIClientException(APIClientException.ERROR_PROTOCOL, "error while parsing response", e);
 				}
 			} else {
 				Log.v(TAG, ": ERROR. Bad status of response " + responseCode);
 				throw new APIClientException(APIClientException.ERROR_NETWORK_STATUS, "bad status of response " + responseCode);
 			}
 		} catch (RequestBuildingException e) {
 			Log.v(TAG, ": INTERNAL ERROR. ", e);
 			throw new APIClientException(APIClientException.ERROR_INNER, "request building error", e);
 		} catch (UnsupportedEncodingException e) {
 			Log.v(TAG, ": INTERNAL ERROR. ", e);
 			throw new APIClientException(APIClientException.ERROR_INNER, "unsupported encoding", e);
 		} catch (IOException e) {
 			Log.v(TAG, ": NETWORK ERROR. ", e);
 			throw new APIClientException(APIClientException.ERROR_NETWORK, "io error", e);
 		} catch (Exception e) {
 			Log.v(TAG, ": INTERNAL ERROR. ", e);
 			throw new APIClientException(APIClientException.ERROR_INNER, "request building error", e);
 		}
 	}
 
 	private static GZIPHttpClient mHttpClient;
 
 	private static final int WAIT_FOR_CONNECTION_TIMEOUT = 10 * 1000;
 	private static final int SOCKET_TIMEOUT = 100 * 1000;
 	private static final int SOCKET_BUFFER_SIZE = 1024;
 	private static final int MAX_TOTAL_CONNECTIONS = 4;
 
 	public APIClient() {
 		initClient();
 	}
 
 	protected void initClient() {
 		HttpParams httpParams = new BasicHttpParams();
 		httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1).setParameter(CoreProtocolPNames.USER_AGENT, "Kickster-android/0.0.1").setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true).setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SOCKET_TIMEOUT).setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, SOCKET_BUFFER_SIZE).setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, WAIT_FOR_CONNECTION_TIMEOUT).setIntParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, MAX_TOTAL_CONNECTIONS);
 
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
 		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
 
 		mHttpClient = new GZIPHttpClient(connManager, httpParams);
 	}
 
 }
