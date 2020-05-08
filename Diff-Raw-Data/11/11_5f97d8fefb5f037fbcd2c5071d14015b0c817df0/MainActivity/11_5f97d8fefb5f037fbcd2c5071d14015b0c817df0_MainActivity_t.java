 /**
  * Main UI for the application. Displays a list of questions.
  * 
  * @author Cody Andrews, Phillip Leland
  * 
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import java.util.List;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.*;
 import com.huskysoft.interviewannihilator.runtime.*;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	/**
 	 * Used to pass the String question to the child activity.
 	 * Will pass a Question object.
 	 */
 	public final static String EXTRA_MESSAGE =
 			"com.huskysoft.interviewannihilator.QUESTION";
 		
 	/** Layout element that holds the questions */
 	private LinearLayout questionll;
 	
 	
 	/**
 	 * Method that populates the app when the MainActivity is created.
 	 * Initializes the questions and questionll fields. Also calls
 	 * the displayQuestions function.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		questionll = (LinearLayout) findViewById(R.id.question_layout);		
 
 		loadQuestions();
 	}
 	
 	public void loadQuestions(){
 		// Display loading text
 		LinearLayout loadingText =
 				(LinearLayout) findViewById(R.id.loading_text_layout);
 		loadingText.setVisibility(View.VISIBLE);
 		
 		// Populate questions list. This makes a network call.
 		new FetchQuestionsTask(this).execute();
 	}
 	
 	/**
 	 * Displays a formatted list of questions
 	 * 
 	 * @param questions
 	 */
 	@SuppressLint("NewApi")
 	public void displayQuestions(List<Question> questions) {		
 		
 		// Dismiss loading text
 		LinearLayout loadingText =
 				(LinearLayout) findViewById(R.id.loading_text_layout);
 		loadingText.setVisibility(View.GONE);
 		
 		
 		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.75f);
 		
 		//TODO: Move to XML or constants file - haven't yet figured out how
 		llp.setMargins(40, 10, 40, 10);
 		llp.gravity = 1;  // Horizontal Center
 		
 		
 		if(questions == null || questions.size() <= 0){
 			TextView t = new TextView(this);
 			
 			t.setText("There doesn't seem to be any questions.");
 			// special look?
 			t.setLayoutParams(llp);
 			questionll.addView(t);
		}else{
 			for(int i = 0; i < questions.size(); i++){
 				Question question = questions.get(i);
 				if(question != null && question.getText() != null){
 					
 					String questionText = question.getTitle();
 					
 					TextView t = new TextView(this);
 					
 					t.setLayoutParams(llp);
 					t.setId(question.getQuestionId());
 					t.setTag(question);
 					t.setText(questionText);	
 					
 					
 					// to make it work on older versions use this instead of setBackground() 
 					t.setBackgroundDrawable(getResources().getDrawable( R.drawable.listitem));
 					
 					t.setOnClickListener(new View.OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							openQuestion(v);
 						}
 					});
 			
 					questionll.addView(t);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Pops up a dialog menu with "Retry" and "Cancel" options when a network
 	 * operation fails.
 	 */
 	public void onNetworkError(){	
 		// Stop loadingDialog
 		LinearLayout loadingText =
 				(LinearLayout) findViewById(R.id.loading_text_layout);
 		loadingText.setVisibility(View.GONE);
 		
 		// Create a dialog
 		new AlertDialog.Builder(this).setTitle(R.string.retryDialog_title)
 		.setPositiveButton(R.string.retryDialog_retry,
 		new DialogInterface.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				loadQuestions();
 			}
 		})
 		.setNegativeButton(R.string.retryDialog_cancel,
 		new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				finish();
 			}
 		})
 		.create().show();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/**
 	 * Function used as the onClickHandler of the Question tiles
 	 * on the main menu of the application.
 	 * 
 	 * @param view The TextView that holds the selected question.
 	 */
 	public void openQuestion(View view){
 		Intent intent = new Intent(this, QuestionActivity.class);
 		intent.putExtra(EXTRA_MESSAGE, (Question) view.getTag());
 		startActivity(intent);
 	}
 }
