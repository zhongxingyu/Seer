 /**
  * Asynchronous thread designed to upvote or downvote a solution
  * 
  * @author Cody Andrews, 05/30/2013
  */
 
 package com.huskysoft.interviewannihilator.runtime;
 
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 import com.huskysoft.interviewannihilator.ui.QuestionActivity;
 
 public class VoteSolutionTask extends AsyncTask<Void, Void, Boolean>{
 
 	private QuestionActivity context;
 	
 	/**
 	 * Solution view as defined in solution_view.xml
 	 */
 	private View solutionView;
 	private Solution solution;
 	private boolean isUpvote;
 	private Exception exception;
 
 	/**
 	 * @param solutionView Solution view as defined in solution_view.xml
 	 * 			solutionView must have .setTag(Solution) set in order for
 	 * 			this to work.
 	 * @param isUpvote true upvotes, false downvotes
 	 */
 	public VoteSolutionTask(QuestionActivity context,
 			View solutionView, Solution solution, boolean isUpvote){
 		this.context = context;
 		this.solutionView = solutionView;
 		this.solution = solution;
 		this.isUpvote = isUpvote;
 	}
 
 
 	/**
 	 * This function does all network calls
 	 */
 	@Override
 	protected Boolean doInBackground(Void... params) {
 		Boolean success = false;
 		QuestionService questionService = QuestionService.getInstance();
 		try {
 			int solutionId = solution.getSolutionId();
 			if(isUpvote){
 				success = questionService.upvoteSolution(solutionId);
			}
			else{
 				success = questionService.downvoteSolution(solutionId);
 			}
 		} catch (Exception e){
 			Log.e("VoteSolutionTask", "" + e.getMessage());
 			exception = e;
 			this.cancel(true);
 		}
		if(!success)
 			this.cancel(true);
 		return success;
 	}
 
 	/**
 	 * An error has occurred
 	 */
 	@Override
 	protected void onCancelled(){
 		if(exception != null){
 			// network error
 			Toast.makeText(context.getApplicationContext(), 
 					R.string.toast_network_error, Toast.LENGTH_LONG).show();	
		}
		else{
 			// user has already voted
 			Toast.makeText(context.getApplicationContext(), 
 					R.string.toast_already_voted, Toast.LENGTH_LONG).show();
 		}
 	}
 
 	/**
 	 * This event fires when doInBackground() is complete. It will increment or
 	 * decrement the score TextView
 	 */
 	@Override
 	protected void onPostExecute(Boolean success){
 		if(success){
 			TextView scoreView = (TextView)
 					solutionView.findViewById(R.id.score_text);
 			
 			int currentScore =
 					Integer.parseInt(scoreView.getText().toString());
			if(isUpvote)
 				currentScore++;
			else
 				currentScore--;
 			scoreView.setText(currentScore + "");
 		}
 	}
 }
