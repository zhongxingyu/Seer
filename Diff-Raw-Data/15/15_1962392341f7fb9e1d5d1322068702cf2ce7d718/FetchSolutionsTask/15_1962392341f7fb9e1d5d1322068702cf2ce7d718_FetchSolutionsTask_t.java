 package com.huskysoft.interviewannihilator.runtime;
 
import java.io.IOException;
 import java.util.List;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 import com.huskysoft.interviewannihilator.ui.SolutionActivity;
 import com.huskysoft.interviewannihilator.util.PaginatedSolutions;
 
 /**
  * 
  * This AsyncTask is used to populate SolutionActivity
  * 
  * @author Cody Andrews
  *
  */
 public class FetchSolutionsTask extends AsyncTask<Void, Void, Void>{
 	
 	private QuestionService questionService;
 	private SolutionActivity context;
 	private List<Solution> solutionList;
 	private Question question;
 	
 	/**
 	 * 
 	 * @param context reference to SolutionActivity
 	 * @param question question to find solutions for
 	 */
 	public FetchSolutionsTask(Activity context, Question question){
 		this.context = (SolutionActivity) context;
 		this.question = question;
 	}
 	
 	/**
 	 * This is the main function of the AsyncTask thread. This will populate
 	 * solutionList with Solutions so that they may be displayed afterwards.
 	 */
 	@Override
 	protected Void doInBackground(Void... params) {
 		questionService = QuestionService.getInstance();
 		
 		try {
 			PaginatedSolutions paginatedSolutions =
 					questionService.getSolutions(question.getQuestionId(), 10, 0);
 			
 			solutionList = paginatedSolutions.getSolutions();
 		} catch (NetworkException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
		} catch (IOException e) {
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
 		context.displaySolutions(solutionList);
 	}
 }
