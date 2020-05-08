 /**
  * 
  * After a question is clicked on MainActivity, this activity is brought up.
  * It display the question clicked, and a hidden list of solutions that pop
  * up when a "Solutions" button is clicked.
  * 
  * @author Cody Andrews, Phillip Leland, Justin Robb 05/01/2013
  * 
  */
 
 package com.huskysoft.interviewannihilator.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.runtime.FetchSolutionsTask;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.app.ActionBar.LayoutParams;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.TextAppearanceSpan;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.content.Intent;
 
 
 /**
  * Activity for viewing a question before reading solutions
  */
 public class QuestionActivity extends AbstractPostingActivity {
 
 	public final static String EXTRA_MESSAGE = 
 			"com.huskysoft.interviewannihilator.QUESTION";
 	
 	/** Layout that the question and solutions will populate */
 	private LinearLayout solutionsLayout;
 	
 	/** The question the user is viewing */
 	private Question question;
 	
 	/** List of TextViews containing solutions */
 	private List<TextView> solutionTextViews;
 	
 	/** true when the solutions have finished loading */
 	private boolean solutionsLoaded;
 	
 	/** true when the user presses the "show solutions button" */
 	private boolean showSolutionsPressed;
 	
 	/** Thread in which solutions are loaded */
 	private FetchSolutionsTask loadingThread;
 	
 	
 	@SuppressLint("NewApi")
 	@Override
 	public synchronized void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_question);	
 		setBehindContentView(R.layout.activity_menu);
 		getActionBar().setHomeButtonEnabled(true);
 		buildSlideMenu();
 
 		// Get intent
 		Intent intent = getIntent();
 		question = (Question) intent.getSerializableExtra(
 				MainActivity.EXTRA_MESSAGE);
 		this.setTitle(question.getTitle());
 		// Grab Linear Layout
 		solutionsLayout =
 				(LinearLayout) findViewById(R.id.question_layout_solutions);
 		
 		// Create TextView that holds Question
 		LinearLayout.LayoutParams llp =  new LinearLayout.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
 		
 		// TODO: Move to XML or constants file - haven't yet figured out how
 		llp.setMargins(40, 10, 40, 10);
 		llp.gravity = 1; // Horizontal Center
 
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
 		TextView textview = (TextView) findViewById(R.id.question_text_view);
 		
 		textview.setBackground(
 				getResources().getDrawable( R.drawable.listitem));
 		textview.setText(sb);
 		textview.setLayoutParams(llp);
 				
 		// Initialize values
 		solutionsLoaded = false;
 		showSolutionsPressed = false;
 		solutionTextViews = new ArrayList<TextView>();
 		
 		//Start loading solutions. This makes a network call.
 		loadSolutions();		
 	}
 	
 	
 	/**
 	 * Appends a list of solutions to a hidden list.
 	 * If the showSolutions button has already been pressed, it will reveal
 	 * the solutions upon completion. If the button has not been pressed,
 	 * solutions will be hidden.
 	 * 
 	 * @param solutions
 	 */
 	public synchronized void addSolutions(List<Solution> solutions){
 		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
 		llp.setMargins(40, 10, 40, 10);
 		llp.gravity = 1; // Horizontal Center
 		
 		if(solutions == null || solutions.size() <= 0){
 			TextView t = new TextView(this);
 			
 			t.setText("There doesn't seem to be any solutions");
 			t.setLayoutParams(llp);
 			solutionsLayout.addView(t);
 		} else {
 			for(int i = 0; i < solutions.size(); i++){
 				Solution solution = solutions.get(i);
 				if(solution != null && solution.getText() != null){
 					//build text
 					String solutionBody = solution.getText();
 					String solutionDate = solution.getDateCreated().toString();
 					
 					int pos = 0;
 					SpannableStringBuilder sb = new SpannableStringBuilder();
 					
 					// body
 					sb.append(solutionBody);
 					sb.setSpan(new  TextAppearanceSpan(
 							this, R.style.solution_appearance), pos, 
 							sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					sb.append('\n');
 					pos += solutionBody.length() + 1;
 					// date
 					sb.append('\n');
 					sb.append(solutionDate);
 					sb.setSpan(new  TextAppearanceSpan(
 							this, R.style.question_date_appearance), pos, 
 							sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					
 					// done
 					TextView t = new TextView(this);
 					
 					t.setText(sb);
 					t.setBackground(getResources().
 							getDrawable(R.drawable.listitem));
 					t.setLayoutParams(llp);
 					t.setId(solution.getSolutionId());
 					//Hide solutions
 					t.setVisibility(View.GONE);
 					
 					solutionTextViews.add(t);
 					solutionsLayout.addView(t);
 				}
 			}
 		}
 			
 		solutionsLoaded = true;
 		if(showSolutionsPressed){
 			revealSolutions();
 		}
 	}	
 
 	
 	/**
 	 * Button handler for the "Solutions" button.
 	 * Attempts to reveal solutions. If it cannot (solutions have not been
 	 * loaded yet), it will set a flag to 
 	 * 
 	 * @param v 
 	 */
 	public synchronized void onShowSolutions(View v){
 		if(!showSolutionsPressed){
 			if(solutionsLoaded){
 				revealSolutions();
 			}else{
 				View loadingText = findViewById(R.id.loading_text_layout);
 				loadingText.setVisibility(View.VISIBLE);
 				showSolutionsPressed = true;
 			}
 		}
 	}
 	
 	/**
 	 * Reveals solutions. Should only be called once solutions are loaded.
 	 */
 	private synchronized void revealSolutions(){
 		// Dismiss loading window
 		View loadingText = findViewById(R.id.loading_text_layout);
 		loadingText.setVisibility(View.GONE);
 		
 		// Dismiss show solutions button
 		Button showSolutions =
 				(Button) findViewById(R.id.question_button_show_solutions);
 		showSolutions.setVisibility(View.GONE);
 		
 		// Reveal hidden solutions
 		for(TextView tv : solutionTextViews){
 			tv.setVisibility(View.VISIBLE);
 		}
 		
 		// Add post solution button to end of list
 		Button post = new Button(this);
 		post.setText(R.string.button_post_solution);
		post.setBackgroundColor(getResources().getColor(R.color.button));
 		post.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v){
 				postSolution(v);
 			}
 		});
 		solutionsLayout.addView(post);
 	}
 	
 	private void loadSolutions(){
 		solutionsLoaded = false;
 		
 		loadingThread = new FetchSolutionsTask(this, question);
 		loadingThread.execute();
 	}
 	
 	/**
 	 * Pops up a dialog menu with "Retry" and "Cancel" options when a network
 	 * operation fails.
 	 */
 	public void onNetworkError(){	
 		// Stop loadingDialog
 		View loadingText = findViewById(R.id.loading_text_layout);
 		loadingText.setVisibility(View.GONE);
 
 		// Create a dialog
 		final Dialog dialog = new Dialog(this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.retrydialogcustom);
 		TextView text = (TextView) dialog.findViewById(R.id.dialog_text);
 		text.setText(R.string.retryDialog_title);
 		Button dialogButton = (Button) dialog.findViewById(R.id.button_retry);
 		// if button is clicked, close the custom dialog
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Try reloading
 				loadSolutions();
 			}
 		});
 		dialogButton = (Button) dialog.findViewById(R.id.button_cancel);
 		// if button is clicked, close the custom dialog
 		dialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// Probably don't want to send them back to questions 
 				// screen here, so just dismiss
 				dialog.dismiss();
 			}
 		});
 		dialog.show();
 	}
 	
 	/** Called when the user clicks the post solution button */
 	public void postSolution(View view) {
 		if (isUserInfoLoaded()){
 			Intent intent = new Intent(this, PostSolutionActivity.class);
 			intent.putExtra(EXTRA_MESSAGE, question);
 			startActivity(intent);
 		} else {
 			// helpful message
 			onValidationIssue();
 		}
 	}
 	
 
 }
