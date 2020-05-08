 package epfl.sweng.tasks;
 
 
 
 import org.apache.http.client.methods.HttpGet;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import epfl.sweng.globals.Globals;
 import epfl.sweng.quizzes.Quiz;
 
 /**
  * QuizServerTask realization that fetches a random Question
  */
 public class LoadQuiz extends QuizServerTask {
 	
 	private int mQuizId;
 	
 	/**
 	 * Constructor
 	 * @param callback interface defining the methods to be called
 	 * for the outcomes of success (onSuccess) or error (onError)
 	 */
 	public LoadQuiz(final IQuizReceivedCallback callback, int quizId) {
 		super(new IQuizServerCallback() {
 			
 			@Override
 			public void onSuccess(JSONTokener response) {
 				Quiz quiz = new Quiz();
 				try {
 					JSONObject quizJSON = (JSONObject) response.nextValue();
 					quiz = new Quiz(quizJSON);
 				} catch (JSONException e) {
 					onError();
 				} 
 				callback.onSuccess(quiz);
 			}
 			
 			@Override
 			public void onError() {
 				callback.onError();
 			}
 		});
 		mQuizId = quizId;
 	}
 	
 	/**
 	 * Method fetching the random question
 	 * @param url (optional) an alternative url for the QuizServer "fetch random question location
 	 */
 	@Override
 	protected JSONTokener doInBackground(Object... urls) {
     	String url = "";
     	
 		if (urls.length == 0) {
 			url = Globals.QUIZ_BY_ID_URL + mQuizId;
 		} else {
 			url = (String) urls[0];
 		}
 		
 		return handleQuizServerRequest(new HttpGet(url));
 	}
 	
 }
