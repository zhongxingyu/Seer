 /**
  * The UI for posting a question.
  * 
  * @author Justin Robb, 05/18/2013
  * 
  */
 package com.huskysoft.interviewannihilator.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Category;
 import com.huskysoft.interviewannihilator.model.Difficulty;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.runtime.PostQuestionsTask;
 
 import android.os.Bundle;
 import android.app.Dialog;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PostQuestionActivity extends AbstractPostingActivity {
 	
 	/**The currently selected difficulty (radio buttons)*/
 	Difficulty difficulty;
 	
 	@Override
 	public synchronized void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_post_question);
 		setBehindContentView(R.layout.activity_menu);
 		getActionBar().setHomeButtonEnabled(true);
 		buildSlideMenu();
 		
 		// VALIDATE
 		assert(isUserInfoLoaded());
 		
 		
 		difficulty = Difficulty.EASY;
 		
 		// fill category spinner
 		List<String> spinnerArray =  new ArrayList<String>();
 		spinnerArray.add("<SELECT>");
 		for (Category c : Category.values()){
 			spinnerArray.add(c.toString());
 		}
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 				this, android.R.layout.simple_spinner_item, spinnerArray);
 		adapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		findViewById(R.id.edit_solution_q);
 		Spinner spinner = (Spinner) 
 				findViewById(R.id.category_spinner_question);
 
 		spinner.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.post_question, menu);
 		return true;
 	}
 	
 	/**
 	 * Sends a question to the database.
 	 * 
 	 * @param v The view for the button for sending a question
 	 */
 	public void sendQuestion(View v) {
 		// get all necessary fields
 		String categoryStr = ((Spinner) findViewById(
 				R.id.category_spinner_question)).getSelectedItem().toString();
 		Category category = null;
 		if (!categoryStr.equals("<SELECT>")) {
 			category = Category.valueOf(categoryStr);
 		}
 		String solutionText = ((EditText) findViewById(
 				R.id.edit_solution_q)).getText().toString();
 		String questionText = ((EditText) findViewById(
 				R.id.edit_question)).getText().toString();
 		String titleText = ((EditText) findViewById(
 				R.id.edit_question_title)).getText().toString();
 		
 		
 		// check fields for correctness
 		if (titleText.trim().equals("")){
 			displayMessage(0, getString(R.string.badInputDialog_title));
 		} else if (questionText.trim().equals("")){
 			displayMessage(0, getString(R.string.badInputDialog_question));
 		} else if (solutionText.trim().equals("")){
 			displayMessage(0, getString(R.string.badInputDialog_solution));
 		} else if (categoryStr.equals("<SELECT>")) {
 			displayMessage(0, getString(R.string.badInputDialog_category));
 		} else {
 			// all fields are correct, try and send it!
 			switchToLoad();
 			Question q = new Question(questionText, 
 					titleText, category, difficulty);
 			Solution s = new Solution(q.getQuestionId(), solutionText);
 			new PostQuestionsTask(this, q, s).execute();
 		}
 	}
 	
 	/**
 	* Pops up a window for the user to interact with the 
     * results of posting their solution.
     * 
     * @param status The state of the solution, which should 
     * 		 be passed as one of the following:
     *              1 if the user is finished on this page
     *              0 if the solution was not valid upon trying to post
     *              -1 for network error
     *              any other number for internal error
     *              
     * @param message The string to display to the user, 
     * 				 telling them what was invalid.
     * 				only needed when status == 0     
     */
 	public Dialog displayMessage(int status, String message){
 		// custom dialog
 		final Dialog dialog = new Dialog(this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		TextView text;
 		if (status == 1 || status == 0){
 			dialog.setContentView(R.layout.alertdialogcustom);
 			text = (TextView) dialog.findViewById(R.id.dialog_text_alert);
 		}else{
 			dialog.setContentView(R.layout.retrydialogcustom);
 			text = (TextView) dialog.findViewById(R.id.dialog_text);
 		}
 
 		// set the custom dialog components - text, buttons
 		if (status == 1){
 			text.setText(getString(R.string.successDialog_title));
 			Button dialogButton = (Button) 
 					dialog.findViewById(R.id.dialogButtonOK);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Toast.makeText(getApplicationContext(), 
 							R.string.toast_return, Toast.LENGTH_LONG).show();
 					finish();   //It would look really cool for the solutions
 								//to update b4 the user returns
 				}
 			});
 		}else if (status == 0){
			if (message == null) {
				message = "";
			}
 			text.setText(message);
 			Button dialogButton = (Button) 
 					dialog.findViewById(R.id.dialogButtonOK);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Toast.makeText(getApplicationContext(), 
 							R.string.toast_return, Toast.LENGTH_LONG).show();
 					dialog.dismiss();
 				}
 			});
 		}else{
 			if (status == -1)
 				text.setText(getString(R.string.retryDialog_title));
 			else
 				text.setText(getString(R.string.internalError_title));
 			Button dialogButton = (Button) 
 					dialog.findViewById(R.id.button_retry);
 			// if button is clicked, send the solution
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Toast.makeText(getApplicationContext(), 
 							R.string.toast_retry, Toast.LENGTH_LONG).show();
 					dialog.dismiss();
 					sendQuestion(v);
 				}
 			});
 			dialogButton = (Button) dialog.findViewById(R.id.button_cancel);
 			// if button is clicked, close the custom dialog
 			dialogButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Toast.makeText(getApplicationContext(), 
 							R.string.toast_return, Toast.LENGTH_LONG).show();
 					dialog.dismiss();
 				}
 			});
 		}
 		dialog.show();
 		return dialog;
 	}
 	/**
 	 * Manages the radio buttons 
 	 * 
 	 * @param v The radio button that was clicked
 	 */
 	public void updateRadio(View v){
 		RadioButton easy = (RadioButton) findViewById(R.id.difficulty_easy);
 		RadioButton med = (RadioButton) findViewById(R.id.difficulty_medium);
 		RadioButton hard = (RadioButton) findViewById(R.id.difficulty_hard);
 		
 		if (v.equals(easy)){
 			med.setChecked(false);
 			hard.setChecked(false);
 			difficulty = Difficulty.EASY;
 		} else if (v.equals(med)){
 			easy.setChecked(false);
 			hard.setChecked(false);
 			difficulty = Difficulty.MEDIUM;
 		} else {
 			easy.setChecked(false);
 			med.setChecked(false);
 			difficulty = Difficulty.HARD;
 		}
 	}
 	
 	/**
 	 * Shows loading text
 	 */
 	public void switchToLoad(){
 		View loadingText = findViewById(R.id.layout_loading);
 		View main = findViewById(R.id.post_question_main_view);
 		Button send = (Button) findViewById(R.id.send_question);
 		main.setVisibility(View.GONE);
 		loadingText.setVisibility(View.VISIBLE);
 		send.setEnabled(false);
 	}
 	
 	/**
 	 * Hides loading text
 	 */
 	public void switchFromLoad(){
 		View loadingText = findViewById(R.id.layout_loading);
 		View main = findViewById(R.id.post_question_main_view);
 		Button send = (Button) findViewById(R.id.send_question);
 		send.setEnabled(true);
 		main.setVisibility(View.VISIBLE);
 		loadingText.setVisibility(View.GONE);
 	}
 }
