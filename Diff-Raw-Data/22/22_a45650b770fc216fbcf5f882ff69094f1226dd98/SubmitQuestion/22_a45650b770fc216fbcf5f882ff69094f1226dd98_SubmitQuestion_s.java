 package epfl.sweng.tasks;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONException;
 
 import epfl.sweng.editquestions.EditQuestionActivity;
 import epfl.sweng.globals.Globals;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.quizquestions.QuizQuestion;
 
 
 import android.os.AsyncTask;
 
 /**
  */
 public class SubmitQuestion extends AsyncTask<Object, Void, QuizQuestion> {
     
 	private EditQuestionActivity mEditQuestionActivity;
 	
 	/**
 	 */
 	public SubmitQuestion(EditQuestionActivity editQuestionActivity) {
 		mEditQuestionActivity = editQuestionActivity;
 	}
 	
 	/**
 	 */
 	@Override
 	protected QuizQuestion doInBackground(Object... args) {
 		// TODO Auto-generated method stub
     	try {
     		String url;
     		QuizQuestion question = (QuizQuestion) args[0];
     		if (args.length == 1) {
     			url = Globals.SUBMIT_QUESITON_URL;
     		} else {
     			url = (String) args[1];
     		}
     		
 
     		HttpPost post = new HttpPost(url);
     		post.setEntity(new StringEntity(question.getJSONString()));
     		post.setHeader("Content-type", "application/json");
     		ResponseHandler<String> handler = new BasicResponseHandler();
     		String response = SwengHttpClientFactory.getInstance().execute(post, handler);
     		System.out.println("Response from server: " + response);
     		return new QuizQuestion(response);
 		} catch (JSONException e) {
 			System.out.println(e.getMessage());
 			cancel(true);
 		} catch (ClientProtocolException e) {
 			System.out.println(e.getMessage());
 			System.out.println(e.toString());
 			cancel(true);
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			cancel(true);
 		}
     	
 		return null;
 	}
 	
 	
 	/**
 	 * Calls back the displayQuestion Method of the EditQuestionActivity once 
 	 * the background process loading the random message completed
 	 * @param QuizQuestion question The random question to be displayed as received from the server.
 	 */
 	@Override
 	protected void onPostExecute(QuizQuestion question) {
 	}
 
 	@Override
 	protected void onCancelled() {
 		mEditQuestionActivity.displaySubmitError();
 	}
 	
 }
