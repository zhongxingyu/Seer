 /**
  * The screen for when users attempt to post a solution
  * 
  * @author Cody Andrews, Justin Robb 05/14/2013
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.runtime.PostSolutionsTask;
 
 import android.os.Bundle;
 import android.app.Dialog;
 
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.TextAppearanceSpan;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.annotation.SuppressLint;
 import android.content.Intent;
 
 public class PostSolutionActivity extends AbstractPostingActivity {
 	/** The question being answered **/
 	private Question question;
 	
 	
 	@SuppressLint("NewApi")
 	@Override
 	public synchronized void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_post_solution);
 		setBehindContentView(R.layout.activity_menu);
 		getActionBar().setHomeButtonEnabled(true);
 		buildSlideMenu();
 
 		// Get intent
 		Intent intent = getIntent();
 		question = (Question) intent.getSerializableExtra(
 				QuestionActivity.EXTRA_MESSAGE);
 		this.setTitle(question.getTitle());
 		//setup question view
 		//build text
 		String questionBody = question.getText();
 		String questionDate = question.getDateCreated().toString();
 				
 		int pos = 0;
 		SpannableStringBuilder sb = new SpannableStringBuilder();
 		// body
 		sb.append(questionBody);
 		sb.setSpan(new  TextAppearanceSpan(
 				this, R.style.question_appearance), pos, 
 				sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 		sb.append('\n');
 		pos += questionBody.length() + 1;
 		// date
 		sb.append('\n');
 		sb.append(questionDate);
 		sb.setSpan(new  TextAppearanceSpan(
 				this, R.style.question_date_appearance), pos, 
 				sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 				
 		// done
 		TextView tv = (TextView) findViewById(R.id.question_view);
 		tv.setText(sb);
 	}
 	
 
 	
 	/** Called when the user clicks the post button */
 	public void sendSolution(View view) {
 		sendSolution();
 	}
 
 	/**
 	* Attempts to post a solution to the database.
 	*/
 	private synchronized void sendSolution() {
 		if (question == null)
 			throw new IllegalStateException();
 	
 		EditText editText = (EditText) findViewById(R.id.edit_solution);
 		String message = editText.getText().toString();  
 		if (message.trim().equals("")){
 			// Fail due to bad solution
 			displayMessage(0);
 			return;
 		}
 		switchToLoad();
 		Solution solution = new Solution(question.getQuestionId(), message);
 		new PostSolutionsTask(this, solution).execute();
 	}
 	
 	/**
 	* Pops up a window for the user to interact with the 
     * results of posting their solution.
     * 
     * @param status The state of the solution, which should 
     * 		 be passed as one of the following:
     *              1 if the user is finished on this page
     *              0 if the solution was not valid upon trying to post
     *              -1 to indicate network error
     *              Any other number to indicate an internal error
     *                      
     */
 	public void displayMessage(int status){
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
 			text.setText(getString(R.string.badInputDialog_solution));
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
 					sendSolution(v);
 				}
 			});
 			dialogButton = (Button) dialog.findViewById(R.id.button_cancel);
 			// if CANCEL button is clicked
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
 	 * Shows loading text
 	 */
 	public void switchToLoad(){
 		View loadingText = findViewById(R.id.layout_loading);
 		View main = findViewById(R.id.post_solution_main_view);
		Button send = (Button)findViewById(R.id.send_solution);
 		main.setVisibility(View.GONE);
 		loadingText.setVisibility(View.VISIBLE);
 		send.setEnabled(false);
 	}
 	
 	/**
 	 * Hides loading text
 	 */
 	public void switchFromLoad(){
 		View loadingText = findViewById(R.id.layout_loading);
 		View main = findViewById(R.id.post_solution_main_view);
		Button send = (Button)findViewById(R.id.send_solution);
 		send.setEnabled(true);
 		main.setVisibility(View.VISIBLE);
 		loadingText.setVisibility(View.GONE);
 	}
 }
