 /**
  * 
  */
 package ch.zhaw.mdp.lhb.citr.com.rest;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.util.Base64;
 import android.util.Log;
 import ch.zhaw.mdp.lhb.citr.R;
 import ch.zhaw.mdp.lhb.citr.activities.CitrBaseActivity;
 import ch.zhaw.mdp.lhb.citr.exceptions.CitrCommunicationException;
 import ch.zhaw.mdp.lhb.citr.exceptions.CitrExceptionTypeEnum;
 import ch.zhaw.mdp.lhb.citr.util.PropertyHelper;
 
 /**
  * @author Daniel Brun
  * 
  *         Background-Task to do the REST-Requests
  */
 public class RESTBackgroundTask extends AsyncTask<String, Integer, String> {
 
 	public static final String TAG = "RESTBackgroundTask";
 
 	/**
 	 * HTTP POST
 	 */
 	public static final int HTTP_POST_TASK = 1;
 
 	/**
 	 * HTTP GET
 	 */
 	public static final int HTTP_GET_TASK = 2;
 
 	/**
 	 * Connection timeout in ms
 	 */
 	private static final int CONN_TIMEOUT = 3000;
 
 	/**
 	 * Socket timeout in ms
 	 */
 	private static final int SOCKET_TIMEOUT = 5000;
 
 	private CitrBaseActivity activity;
 
 	private ProgressDialog uiProgressDialog = null;
 
 	private List<NameValuePair> parameters;
 	private int httpRequestType;
 
 	/**
 	 * Creates a new instance of this class.
 	 * 
 	 * @param aContext
 	 *            The underlying context.
 	 * @param anActivity
 	 *            The underlying activity
 	 * @param aHttpRequestType
 	 *            The HTTP-Type
 	 */
 	public RESTBackgroundTask(CitrBaseActivity anActivity) {
 		if (anActivity == null) {
 			throw new NullPointerException(
 					"The argument anActivity must not be null!");
 		}
 
 		parameters = new ArrayList<NameValuePair>();
 
 		activity = anActivity;
 		httpRequestType = HTTP_GET_TASK;
 	}
 
 	/**
 	 * Adds a new parameter to the request.
 	 * 
 	 * @param aName
 	 *            The name of the parameter.
 	 * @param aValue
 	 *            The value of the parameter.
 	 */
 	public void addParameter(String aName, String aValue) {
 		parameters.add(new BasicNameValuePair(aName, aValue));
 	}
 
 	/**
 	 * Displays a 'Working-Dialog'
 	 */
 	private void showWorkingDialog() {
 		uiProgressDialog = new ProgressDialog(activity);
 		uiProgressDialog
 				.setMessage(activity.getString(R.string.conn_data_load));
 		uiProgressDialog.setProgressDrawable(activity.getWallpaper());
 		uiProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		uiProgressDialog.setCancelable(false);
 		uiProgressDialog.show();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.os.AsyncTask#onPreExecute()
 	 */
 	@Override
 	protected void onPreExecute() {
 		// Hide Keyboard
 		activity.cleanScreen();
 		showWorkingDialog();
 
 		ConnectivityManager connMgr = (ConnectivityManager) activity
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
 
 		if (networkInfo == null || !networkInfo.isConnected()) {
 			Log.w(TAG, "No Network connection available");
 			throw new CitrCommunicationException(
 					"No Network connection available",
 					CitrExceptionTypeEnum.CONNECTION_NOT_AVAILABLE);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.os.AsyncTask#doInBackground(Params[])
 	 */
 	@Override
 	protected String doInBackground(String... someArguments) {
 		if (someArguments == null || someArguments.length != 1) {
 			throw new IllegalArgumentException(
 					"Exactly one argument must be set!");
 		}
 
 		String requestUrl = someArguments[0];
 		String result = "";
 
 		HttpResponse reqRes = performRequest(requestUrl);
 
 		if (reqRes != null && reqRes.getEntity() != null) {
 			try {
 				result = readInputStream(reqRes.getEntity().getContent());
 			} catch (IllegalStateException e) {
 				Log.e(TAG, e.getLocalizedMessage(), e);
 				throw new CitrCommunicationException(
 						"An error occurred during HTTP-Result-Processing!", e,
 						CitrExceptionTypeEnum.CONNECTION_RESPONSE_ERROR);
 			} catch (IOException e) {
 				Log.e(TAG, e.getLocalizedMessage(), e);
 				throw new CitrCommunicationException(
 						"An error occurred during HTTP-Result-Processing!", e,
 						CitrExceptionTypeEnum.CONNECTION_RESPONSE_ERROR);
 			}
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.os.AsyncTask#onCancelled()
 	 */
 	@Override
 	protected void onCancelled() {
 		parameters.clear();
 		uiProgressDialog.dismiss();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 	 */
 	@Override
 	protected void onPostExecute(String aResult) {
 		parameters.clear();
 		uiProgressDialog.dismiss();
 	}
 
 	/**
 	 * Performs the HTTP-Request
 	 * 
 	 * @param aRequestUrl
 	 *            the Request URL.
 	 * @return the HTTPResponse.
 	 */
 	private HttpResponse performRequest(String aRequestUrl) {
 		HttpResponse response = null;
 
 		// Create HTTP-Parameters
 		HttpParams httpParameter = new BasicHttpParams();
 
 		HttpConnectionParams.setConnectionTimeout(httpParameter, CONN_TIMEOUT);
 		HttpConnectionParams.setSoTimeout(httpParameter, SOCKET_TIMEOUT);
 		httpParameter.setParameter(HTTP.CONTENT_TYPE, "application/json");
 
 		// Create HTTP-Client
 		HttpClient httpClient = new DefaultHttpClient(httpParameter);
 
 		// TODO: Get frome somewhere else...
 		// http://stackoverflow.com/questions/1925486/android-storing-username-and-password
 		String authString = new String(Base64.encode(
 				("user1" + ":" + new String(Hex.encodeHex(DigestUtils
 						.sha512("strongpassword1")))).getBytes(),
 				Base64.NO_WRAP));
 
 		HttpRequestBase httpRequest = null;
 		StringBuffer url = new StringBuffer();
 		url.append(PropertyHelper.get("rest.url"));
 		url.append(aRequestUrl);
 
 		try {
 			switch (httpRequestType) {
 
 			// Create POST-Request
 			case HTTP_POST_TASK:
 				httpRequest = new HttpPost(url.toString());
 
 				// Add parameters
                /*
                //TODO set correct string entity and content-type
                StringEntity params =new StringEntity("details={\"name\":\"myname\",\"age\":\"20\"} ");
                ((HttpPost) httpRequest).setEntity(parameters.get(0));
                httpRequest.setHeader("Content-Type", "application/json");
                */

                ((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(parameters));
 				break;
 
 			// Create GET-Request
 			case HTTP_GET_TASK:
 				url.append("?");
 				url.append(URLEncodedUtils.format(parameters, "utf-8"));
 
 				httpRequest = new HttpGet(url.toString());
 				break;
 			}
 
 			httpRequest.addHeader("Authorization", "Basic " + authString);
 
 			Log.d(TAG, "Calling: " + url.toString() + ", HTTP-Method: "
 					+ httpRequestType + " 1:POST, 2:GET");
 
 			response = httpClient.execute(httpRequest);
 
 		} catch (UnsupportedEncodingException e) {
 			Log.e(TAG, "Invalid encoding for HTTP-request!", e);
 			throw new CitrCommunicationException(
 					"Invalid encoding for HTTP-request!", e,
 					CitrExceptionTypeEnum.CONNECTION_REQUEST_ERROR);
 		} catch (ClientProtocolException e) {
 			Log.e(TAG, "Client exception occurred on performing HTTP-request!", e);
 			throw new CitrCommunicationException(
 					"Client exception occurred on performing HTTP-request!", e,
 					CitrExceptionTypeEnum.CONNECTION_REQUEST_ERROR);
 		} catch (IOException e) {
 			Log.e(TAG, "IO-exception during HTTP-request!", e);
 			throw new CitrCommunicationException(
 					"IO-exception during HTTP-request!", e,
 					CitrExceptionTypeEnum.CONNECTION_REQUEST_ERROR);
 		}
 
 		return response;
 	}
 
 	/**
 	 * Reads the given input stream to a string.
 	 * 
 	 * @param anInputStream
 	 *            The input stream to read
 	 * @return
 	 */
 	private String readInputStream(InputStream anInputStream) {
 		StringBuilder data = new StringBuilder();
 
 		BufferedReader buffReader = null;
 		try {
 			buffReader = new BufferedReader(new InputStreamReader(
 					anInputStream,"UTF-8"));
 			String line = null;
 			while ((line = buffReader.readLine()) != null) {
 				data.append(line);
 			}
 		} catch (UnsupportedEncodingException e) {
 			Log.e(TAG, "Response couldn't be encoded!", e);
 			throw new CitrCommunicationException(
 					"Response couldn't be encoded!", e,
 					CitrExceptionTypeEnum.CONNECTION_RESPONSE_ERROR);
 		} catch (IOException e) {
 			Log.e(TAG, "IO-exception during HTTP-respone parsing!", e);
 			throw new CitrCommunicationException(
 					"IO-exception during HTTP-respone parsing!", e,
 					CitrExceptionTypeEnum.CONNECTION_RESPONSE_ERROR);
 		} finally {
 			try {
 				buffReader.close();
 			} catch (IOException e) {
 				Log.e(TAG, "IO-exception during HTTP-respone parsing! On closing stream.", e);
 				throw new CitrCommunicationException(
 						"IO-exception during HTTP-respone parsing! On closing stream.",
 						e, CitrExceptionTypeEnum.CONNECTION_RESPONSE_ERROR);
 			}
 		}
 
 		return data.toString();
 	}
 
 	/**
 	 * @param aHttpRequestType
 	 *            the httpRequestType to set
 	 */
 	public void setHttpRequestType(int aHttpRequestType) {
 		httpRequestType = aHttpRequestType;
 	}
 }
