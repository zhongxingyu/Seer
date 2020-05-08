 package realtalk.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 /**
  * Utility class used for making POST or GET requests to a given Url.
  * 
  * @author Colin Kho
  *
  */
 public final class HttpUtility {
	
	private static final int CONNECTION_TIMEOUT = 30000;
	
 	/**
 	 * This method makes a Get Request given a list of params and retrieves the response
 	 * from the server. It returns the response as an inputStream.
 	 * 
 	 * @param stUrl     URL to retrieve from.
 	 * @param rgparams  List of params.
 	 * @return          InputStream containing the response.
 	 * @throws IOException Error with reading stream.
 	 * @throws UnsupportedOperationException Unable to retrieve from URL
 	 * @throws org.apache.http.client.ClientProtocolException Clients URL or Params are wrong.
 	 */
 	public static InputStream sendGetRequest(String stUrl, List<NameValuePair> rgparams) 
 			throws IOException, UnsupportedOperationException, ClientProtocolException {
        DefaultHttpClient httpclient = defaulthttpclientCreateClient();
         String stParam = URLEncodedUtils.format(rgparams, "utf-8");
         stUrl += "?" + stParam;
         HttpGet httpget = new HttpGet(stUrl);
 
         HttpResponse httpresponse = httpclient.execute(httpget);
         HttpEntity httpentity = httpresponse.getEntity();
         return httpentity.getContent();
 	}
 	
 	/**
 	 * This method makes a Post request to the server given a list of params and returns
 	 * the response as an InputStream object.
 	 * 
 	 * @param stUrl    URL of object
 	 * @param rgparams List of params
 	 * @return         InputStream that contains server's response
 	 * @throws IOException Error with reading stream.
 	 * @throws UnsupportedOperationException Unable to retrieve from URL
 	 * @throws org.apache.http.client.ClientProtocolException Clients URL or Params are wrong.
 	 */
 	public static InputStream sendPostRequest(String stUrl, List<NameValuePair> rgparams) 
 			throws IOException, UnsupportedOperationException, ClientProtocolException {
        DefaultHttpClient httpclient = defaulthttpclientCreateClient();
         HttpPost httppost = new HttpPost(stUrl);
         httppost.setEntity(new UrlEncodedFormEntity(rgparams, "UTF-8"));
 
         HttpResponse httpresponse = httpclient.execute(httppost);
         HttpEntity httpentity = httpresponse.getEntity();
         return httpentity.getContent();
 	}
 	
	/**
	 * Creates a default http client with a connection timeout paramter.
	 * @return default http client
	 */
	private static DefaultHttpClient defaulthttpclientCreateClient() {
		final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		return new DefaultHttpClient(httpParams);
	}

 	/*
 	 * Private Constructor that throws an error if it is used. Also this disallows the public constructor.
 	 */
 	private HttpUtility() {
 	    throw new UnsupportedOperationException("Http Utility class should never be constructed");
 	}
 }
