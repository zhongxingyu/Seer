 /*
  * The screen for when users attempt to post a solution
  * 
  * @author Cody Andrews, 05/14/2013
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Dialog;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.Build;
 
 public class PostSolutionActivity extends Activity {
 	/** The question being answered **/
 	private Question question;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_post_solution);
 		
 		// Get intent
 		Intent intent = getIntent();
 		question = (Question) intent.getSerializableExtra(
 				QuestionActivity.EXTRA_MESSAGE);
 		
 		//setup question view
 		TextView tv = (TextView) findViewById(R.id.question_view);
 		tv.setText(question.getText());
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.post_solution, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/** Called when the user clicks the post button */
 	public void sendSolution(View view) {
 		sendSolution();
 	}
 
 	/**
 	* Attempts to post a solution to the database.
 	*/
 	private void sendSolution() {
 		if (question == null)
 			throw new IllegalStateException();
 	
 		EditText editText = (EditText) findViewById(R.id.edit_solution);
 		String message = editText.getText().toString();
 		int outcome = 1;   
 		if (message.trim().equals("")){
 			// Fail due to bad solution
 			outcome = 0;
 		}
 		Solution solution = new Solution(question.getQuestionId(), message);
 		QuestionService qs = QuestionService.getInstance();
 		try{
 			qs.postSolution(solution);
 		} catch (NetworkException e){
 			// Retry or cancel
 			// Complains that i need to log the error, 
 			// not sure how to do that
 			outcome= -1;
		} catch (Exception e){
			// probably should do something useful here
			finish();
		}
 		displayMessage(outcome);
 	}
 	
 	/**
 	* Pops up a window for the user to interact with the 
     * results of posting their solution.
     * 
     * @param status The state of the solution, which should 
     * 		 be passed as one of the following:
     *              1 if the solution was successfully posted
     *              0 if the solution was not valid upon trying to post
     *              Any other number to indicate a network error
     */
 	private void displayMessage(int status){
 		// custom dialog
 		final Dialog dialog = new Dialog(this);
 		if (status == 1 || status == 0){
 			dialog.setContentView(R.layout.alertdialogcustom);
 		}else{
 			dialog.setContentView(R.layout.retrydialogcustom);
 		}
 
 		// set the custom dialog components - text, buttons
 		TextView text = (TextView) dialog.findViewById(R.id.dialog_text);
 		if (status == 1){
 			text.setText(R.string.successDialog_title);
 			Button dialogButton = (Button) 
 					dialog.findViewById(R.id.dialogButtonOK);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					finish();   //It would look really cool for the solutions
 								//to update b4 the user returns
 				}
 			});
 		}else if (status == 0){
 			text.setText(R.string.badInputDialog_title);
 			Button dialogButton = (Button) 
 					dialog.findViewById(R.id.dialogButtonOK);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			});
 		}else{
 			text.setText(R.string.retryDialog_title);
 			Button dialogButton = (Button) 
 					dialog.findViewById(R.id.button_retry);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					sendSolution();
 				}
 			});
 			dialogButton = (Button) dialog.findViewById(R.id.button_cancel);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			});
 		}
 		dialog.show();
 	}
 }
