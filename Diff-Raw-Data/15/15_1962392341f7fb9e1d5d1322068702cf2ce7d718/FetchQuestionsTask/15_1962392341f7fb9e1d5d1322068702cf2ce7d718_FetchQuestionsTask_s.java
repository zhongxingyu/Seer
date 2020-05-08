 package com.huskysoft.interviewannihilator.runtime;
 
 import java.util.List;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 import com.huskysoft.interviewannihilator.ui.MainActivity;
 import com.huskysoft.interviewannihilator.util.PaginatedQuestions;
 
 /**
  * 
  * This AsyncTask is used to populate the MainActivity list of questions.
  * To use it, call new FetchQuestionsTask(this).execute();
  * 
  * @author Cody Andrews
  *
  */
 public class FetchQuestionsTask extends AsyncTask<Void, Void, Void>{
 	
 	private QuestionService questionService;
 	private MainActivity context;
 	private List<Question> questionList;
 	
 	/**
 	 * 
 	 * @param context reference to MainActivity
 	 */
 	public FetchQuestionsTask(Activity context){
 		this.context = (MainActivity) context;
 	}
 	
 	
 	/**
 	 * This is the main function of the AsyncTask thread. This will populate
 	 * questionList with Questions so that they may be displayed afterwards.
 	 */
 	@Override
 	protected Void doInBackground(Void... params) {
 		questionService = QuestionService.getInstance();
 		
 		try {
 			PaginatedQuestions currentQuestions =
 					questionService.getQuestions(null, null, 20, 0);
 			questionList = currentQuestions.getQuestions();
 		} catch (NetworkException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 		return null;
 	}
 	
 	/**
 	 * This event fires when doInBackground() is complete, and it will populate
 	 * the MainActivity question area.
 	 */
 	@Override
 	protected void onPostExecute(Void result){
 		context.displayQuestions(questionList);
 	}
 }
