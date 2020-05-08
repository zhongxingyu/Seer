 package no.henning.restful.http;
 
 import java.io.IOException;
 
 import no.henning.restful.http.callback.HttpRestClientResponseCallback;
 import no.henning.restful.http.status.HttpRestResponse;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.util.EntityUtils;
 
 import android.os.AsyncTask;
import android.util.Log;
 
 public class HttpRestClient extends AsyncTask<HttpUriRequest, Void, HttpRestResponse> {
 
 	private DefaultHttpClient client;
 
 	private HttpRestClientResponseCallback callback;
 
 	public HttpRestClient(HttpRestClientResponseCallback callback) {
 		this.callback = callback;
		BasicHttpParams params = new BasicHttpParams();
 		SchemeRegistry schemeRegistry = new SchemeRegistry();
		//schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
 		final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
 		schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
 		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
 		client = new DefaultHttpClient(cm, params);
 	}
 
 	@Override
 	protected HttpRestResponse doInBackground(HttpUriRequest... requests) {
 		try {
 			HttpResponse response = client.execute(requests[0]);
 
 			String responseAsString = response.getEntity() == null ? "{}" : EntityUtils.toString(response.getEntity());
 
 			StatusLine responseStatusLine = response.getStatusLine();
 
 			return new HttpRestResponse(responseStatusLine.getStatusCode(), responseStatusLine.getReasonPhrase(),
 					responseAsString);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(HttpRestResponse response) {
 		if (callback != null)
 			callback.onDone(response);
 	}
 
 	public static void request(HttpUriRequest request, HttpRestClientResponseCallback callback) {
 		new HttpRestClient(callback).execute(request);
 	}
 }
