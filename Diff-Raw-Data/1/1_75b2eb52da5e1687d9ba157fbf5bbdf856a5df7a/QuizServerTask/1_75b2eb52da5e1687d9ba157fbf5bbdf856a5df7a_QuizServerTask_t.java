 package epfl.sweng.tasks;
 
 import java.io.IOException;
 
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import epfl.sweng.authentication.SessionManager;
 import epfl.sweng.globals.Globals;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  * AsyncTask for communication between the App and the Sweng Quiz question server
  */
 abstract class QuizServerTask extends AsyncTask<Object, Void, QuizQuestion> {
     
 	/**
 	 * Local Variable holding the callback interface passed through the constructor 
 	 */
 	private IQuizServerCallback mCallback;
 	
 	/**
 	 * Constructor
 	 * @param callback interface defining the methods to be called
 	 * for the outcomes of success (onSuccess) or error (onError)
 	 */
 	public QuizServerTask(IQuizServerCallback callback) {
 		mCallback = callback;
 	}
 	
 	/**
 	 * Calls back the onSuccess method of the interface defined when creating the task
 	 * @param question the question returned by the server
 	 */
 	@Override
 	protected void onPostExecute(QuizQuestion question) {
 		mCallback.onSuccess(question);
 	}
 
 	/**
 	 * Calls back the onError method of the interface defined when creating the task if
 	 * the task has experienced an Error
 	 */
 	@Override
 	protected void onCancelled() {
 		mCallback.onError();
 	}
 	
 	/**
 	 * Handle a HTTP request towards quiz server. Always send session id if the user is authenticated
 	 * @param request the request
 	 * @return the JSONObject as received from the server
 	 */
 	final protected JSONObject handleQuizServerRequest(HttpUriRequest request) {
 		try {
 			if (SessionManager.getInstance().isAuthenticated()) {
 				request.addHeader("Authorization", "Tequila " + SessionManager.getInstance().getSessionId());
 			}
 			
 			if (Globals.LOG_QUIZSERVER_REQUESTS) {				
 				Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, "==== Sweng QuizQuestion Server Request ====");
 				Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, request.getRequestLine().toString());
 				for (Header header : request.getAllHeaders()) {
 					Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, header.toString());
 				}
 				
 				if (request instanceof HttpPost) {
 					Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, 
 							EntityUtils.toString(((HttpPost) request).getEntity()));
 				}
 			}
 			
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			
 			HttpResponse response = SwengHttpClientFactory.getInstance().execute(request);
 			
 			// TODO next line: if unexpected status code: call Log.e instead of Log.i
 			Log.i("SERVER", "Replied with status code " + response.getStatusLine().getStatusCode());
 			String body = responseHandler.handleResponse(response);
 			if (body == null || body.equals("")) {
 				body = "{}";
 			}
 			if (Globals.LOG_QUIZSERVER_REQUESTS) {
 				Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, "==== Sweng QuizQuestion Server Response ====");
 				Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, response.getStatusLine().getStatusCode() + " "
 						+ response.getStatusLine().getReasonPhrase());
 				for (Header header : response.getAllHeaders()) {
 					Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, header.toString());
 				}
 				Log.i(Globals.LOGTAG_QUIZSERVER_COMMUNICATION, body);
 			}
 			
 			return new JSONObject(body);
     	} catch (JSONException e) {
     		// SET HERE THE mExcept ATTRIBUTE TO A SUITABLE EXCEPTION
     		cancel(false);    		
     	} catch (ClientProtocolException e) {
     		// WE'RE NOT DOING ANYTHING HERE.. IS IT NORMAL ? /Dana
     	} catch (IOException e) {
     		// SET HERE THE mExcept ATTRIBUTE TO A SUITABLE EXCEPTION
     		cancel(false);
     	}
 		return null;
 	}
 	
 
 }
