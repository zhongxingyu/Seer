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
 import com.huskysoft.interviewannihilator.model.NetworkException;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.service.QuestionService;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Dialog;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PostQuestionActivity extends Activity {
 	
 	/**The currently selected difficulty (radio buttons)*/
 	Difficulty diff;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_post_question);
 		diff = Difficulty.EASY;
 		
 		// fill category spinner
 		List<String> spinnerArray =  new ArrayList<String>();
 		for (Category c : Category.values()){
 			spinnerArray.add(c.toString());
 		}
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
 				this, android.R.layout.simple_spinner_item, spinnerArray);
 		adapter.setDropDownViewResource(
 				android.R.layout.simple_spinner_dropdown_item);
 		EditText solutionText = (EditText) findViewById(R.id.edit_solution_q);
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
 		String category = ((Spinner) findViewById(
 				R.id.category_spinner_question)).getSelectedItem().toString();
 		Category c = Category.valueOf(category);
 		String solutionText = ((EditText) findViewById(
 				R.id.edit_solution_q)).getText().toString();
 		String questionText = ((EditText) findViewById(
 				R.id.edit_question)).getText().toString();
 		String titleText = ((EditText) findViewById(
 				R.id.edit_question_title)).getText().toString();
 		
 		// chack fields for correctness
 		if (titleText.trim().equals("")){
 			displayMessage(0, getString(R.string.badInputDialog_title));
 		} else if (questionText.trim().equals("")){
 			displayMessage(0, getString(R.string.badInputDialog_question));
 		} else if (solutionText.trim().equals("")){
 			displayMessage(0, getString(R.string.badInputDialog_solution));
 		} else {
 			// all fields are correct, try and send it!
 			Question q = new Question(questionText, 
 					titleText, Category.COMPSCI, diff);
 			QuestionService qs = QuestionService.getInstance();
 			Solution s = new Solution(q.getQuestionId(), solutionText);
 			try {
 				qs.postQuestion(q);
 				qs.postSolution(s);
				displayMessage(1, getString(R.string.successDialog_title_q));
 			} catch (NetworkException e) {
 				Log.w("Network error", e.getMessage());
 				displayMessage(-1, getString(R.string.retryDialog_title));
 			} catch (Exception e) {
 				Log.e("Internal Error", e.getMessage());
 				displayMessage(-1, getString(R.string.internalError_title));
 			}
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
     *              Any other number to indicate an error
     *              
     * @param message The string to display to the user, 
     * 				 telling them what was invalid          
     */
 	private void displayMessage(int status, String message){
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
 			text.setText(message);
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
 			text.setText(message);
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
 	}
 	
 	/**
 	 * Manages the radio buttons 
 	 * 
 	 * @param v The radio button that was clicked
 	 */
 	public void updateRadio(View v){
 		RadioButton clicked = (RadioButton) v;
 		RadioButton easy = (RadioButton) findViewById(R.id.difficulty_easy);
 		RadioButton med = (RadioButton) findViewById(R.id.difficulty_medium);
 		RadioButton hard = (RadioButton) findViewById(R.id.difficulty_hard);
 		
 		if (v.equals(easy)){
 			med.setChecked(false);
 			hard.setChecked(false);
 			diff = Difficulty.EASY;
 		} else if (v.equals(med)){
 			easy.setChecked(false);
 			hard.setChecked(false);
 			diff = Difficulty.MEDIUM;
 		} else {
 			easy.setChecked(false);
 			med.setChecked(false);
 			diff = Difficulty.HARD;
 		}
 	}
 }
