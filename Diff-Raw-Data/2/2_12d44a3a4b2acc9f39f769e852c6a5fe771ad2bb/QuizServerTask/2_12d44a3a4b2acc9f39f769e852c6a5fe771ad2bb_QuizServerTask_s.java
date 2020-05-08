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
 
 import epfl.sweng.globals.Globals;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 
 
 import android.os.AsyncTask;
 
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
 	 * @param IQuizServerCallback callback interface defining the methods to be called
 	 * for the outcomes of success (onSuccess) or error (onError)
 	 */
 	public QuizServerTask(IQuizServerCallback callback) {
 		mCallback = callback;
 	}
 	
 	/**
 	 * Calls back the onSuccess method of the interface defined when creating the task
 	 * @param QuizQuestion question the question returned by the server
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
 	 */
 	final protected QuizQuestion handleQuizServerRequest(HttpUriRequest request) {
 		try {
 			if (Globals.LOG_QUESTIONSERVER_REQUESTS) {				
 				System.out.println("==== Sweng QuizQuestion Server Request ====");
 				System.out.println(request.getRequestLine());
 				for (Header header : request.getAllHeaders()) {
 					System.out.println(header.toString());
 				}
 				
 				if (request instanceof HttpPost) {
 					System.out.println(EntityUtils.toString(((HttpPost) request).getEntity()));
 				}
 			}
 			
 			ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			
 			HttpResponse response = SwengHttpClientFactory.getInstance().execute(request);
 			String body = responseHandler.handleResponse(response);
 			
			if (Globals.LOG_QUESTIONSERVER_REQUESTS){
 				System.out.println("==== Sweng QuizQuestion Server Response ====");
 				System.out.println(response.getStatusLine().getStatusCode() + " "
 						+ response.getStatusLine().getReasonPhrase());
 				for (Header header : response.getAllHeaders()) {
 					System.out.println(header.toString());
 				}
 				System.out.println(body);
 			}
 			
 			return new QuizQuestion(body);
     	} catch (JSONException e) {
     		cancel(true);
     	} catch (ClientProtocolException e) {
     		cancel(true);
     	} catch (IOException e) {
     		cancel(true);
     	}
 		return null;
 	}
 }
