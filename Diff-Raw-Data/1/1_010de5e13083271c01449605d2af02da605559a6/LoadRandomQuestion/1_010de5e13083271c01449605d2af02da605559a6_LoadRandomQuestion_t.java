 package epfl.sweng.tasks;
 
 import java.io.IOException;
 
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import epfl.sweng.R;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.showquestions.Question;
 import epfl.sweng.showquestions.ShowQuestionsActivity;
 
 
 import android.os.AsyncTask;
 
 /**
  * Class used to create a Asynchronous Task that will load a random question and display it on a ShowQuestionsActivity
  *
  */
 public class LoadRandomQuestion extends AsyncTask<Void, Void, Question> {
     
 	private ShowQuestionsActivity mShowQuestionsActivity;
 	
 	/**
 	 * Constructor. Create a LoadRandomQuestion object. The loading process can 
 	 * be launched by invoking the inherited execute() method. 
 	 * @param ShowQuestionsActivity _showQuestionsActivity Reference to the ShowQuestionsActivity
 	 * the question will be displayed in.
 	 */
 	public LoadRandomQuestion(ShowQuestionsActivity showQuestionsActivity) {
 		mShowQuestionsActivity = showQuestionsActivity;
 	}
 	
 	/**
 	 * Method fetching the random question
 	 */
 	@Override
 	protected Question doInBackground(Void... nothing) {
 		// TODO Auto-generated method stub
     	Question question = new Question();
     	try {
     		
     		HttpGet request = new HttpGet(mShowQuestionsActivity.getString(R.string.random_question_url));
     		ResponseHandler<String> response = new BasicResponseHandler();
     		String responseText = SwengHttpClientFactory.getInstance().execute(request, response);
 			JSONObject responseJson = new JSONObject(responseText);
 			
 			JSONArray answersJSON = responseJson.getJSONArray("answers");
 			String[] answers = new String[answersJSON.length()];
 			for (int i=0; i<answersJSON.length(); i++) {
 				answers[i]=answersJSON.getString(i);
 			}
 			
 
 			JSONArray tagsJSON = responseJson.getJSONArray("tags");
 			String[] tags = new String[tagsJSON.length()];
 			for (int i=0; i<tagsJSON.length(); i++) {
 				tags[i]=tagsJSON.getString(i);
 			}
 			
 			question.setQuestion(responseJson.getString("question"));
 			question.setId(responseJson.getInt("id"));
 			question.setSolutionIndex(responseJson.getInt("solutionIndex"));
 
 			question.setAnswers(answers);
 			question.setTags(tags);
 		} catch (JSONException e) {
 			cancel(true);
 		} catch (ClientProtocolException e) {
 			cancel(true);
 		} catch (IOException e) {
 			cancel(true);
 		}
     	
 		return question;
 	}
 	
 	
 	/**
 	 * Calls back the displayQuestion Method of the ShowQuestionsActivity once 
 	 * the background process loading the random message completed
 	 * @param Question question The random question to be displayed as received from the server.
 	 */
 	@Override
 	protected void onPostExecute(Question question) {
 		mShowQuestionsActivity.displayQuestion(question);
 	}
 
 	@Override
 	protected void onCancelled() {
 		mShowQuestionsActivity.displayError();
 	}
 	
 }
