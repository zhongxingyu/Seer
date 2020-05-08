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
 
 import java.util.List;
 
 import com.huskysoft.interviewannihilator.R;
 import com.huskysoft.interviewannihilator.model.Question;
 import com.huskysoft.interviewannihilator.model.Solution;
 import com.huskysoft.interviewannihilator.runtime.FetchSolutionsTask;
 import com.huskysoft.interviewannihilator.runtime.VoteSolutionTask;
 import com.huskysoft.interviewannihilator.util.UIConstants;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.app.ActionBar.LayoutParams;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.style.TextAppearanceSpan;
 import android.util.TypedValue;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.content.Context;
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
 
 		//build text
 		String questionBody = question.getText();
 		String questionDate = question.getDateCreated().toString();
 		String questionDiff = question.getDifficulty().toString();
 		String questionCat = question.getCategory().toString();
 		
 		int pos = 0;
 		SpannableStringBuilder sb = new SpannableStringBuilder();
 		// body
 		sb.append(questionBody);
 		sb.setSpan(new  TextAppearanceSpan(
 				this, R.style.question_appearance), pos, 
 				sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 		sb.append('\n');
 		pos += questionBody.length() + 1;
 		// descriptors
 				sb.append('\n');
 				sb.append(questionCat);
 				sb.append("\t\t\t");
 				sb.append(questionDiff);
 				sb.setSpan(new  TextAppearanceSpan(
 						this, R.style.question_descriptors_appearance),
 						pos, sb.length(), 
 						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 				sb.append('\n');
 				pos += questionDiff.length() + questionCat.length() + 4;
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
 				
 		// Initialize values
 		solutionsLoaded = false;
 		showSolutionsPressed = false;
 		
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
 	public synchronized void addSolutionList(List<Solution> solutions){
 		if(solutions == null || solutions.size() <= 0){
 			TextView t = new TextView(this);
 			t.setText("There doesn't seem to be any solutions");
 			
 			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
 			llp.setMargins(40, 10, 40, 10);
 			llp.gravity = 1; // Horizontal Center
 			t.setLayoutParams(llp);
 			
 			solutionsLayout.addView(t);
 		} else {
 			for(int i = 0; i < solutions.size(); i++){
 				Solution solution = solutions.get(i);
 				if(solution != null && solution.getText() != null){
 					addSolution(solution);
 				}
 			}
 		}
 			
 		solutionsLoaded = true;
 		if(showSolutionsPressed){
 			revealSolutions();
 		}
 	}
 	
 	/**
 	 * Adds a single solution to the UI. This includes the solution TextView
 	 * and upvote/downvote arrows.
 	 * 
 	 * @param solution solution that will populate the text view
 	 * @param llp layout parameters
 	 */
 	private void addSolution(Solution solution){
 		// get layout from xml
 		LayoutInflater li = (LayoutInflater)
 				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View solutionView = li.inflate(R.layout.solution_view, solutionsLayout, false);
 		
 		// build text
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
 		
 		
 		
 		// get the solution TextView from solution_view.xml
 		TextView solnView = (TextView)
 				solutionView.findViewById(R.id.solution_text);
 		solnView.setText(sb);
 		solnView.setId(solution.getSolutionId());
 		
 		// get the score TextView from solution_view.xml
 		TextView scoreView = (TextView)
 				solutionView.findViewById(R.id.score_text);
 		int score = solution.getLikes() - solution.getDislikes();
 		scoreView.setText(Integer.toString(score));
 		
 		// store the context and solution so that when a button is pressed,
 		// we can access it
 		solutionView.setTag(R.id.TAG_CONTEXT_ID, this);
 		solutionView.setTag(R.id.TAG_SOLUTION_VIEW_ID, solution);
 		
 		// yes I know this is redundant but it is difficult to code around
 		// get upvote button
 		Button upvoteButton = (Button)
 				solutionView.findViewById(R.id.button_upvote);
 		upvoteButton.setTag(solutionView);
 		upvoteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v){
 				View solutionView = (View) v.getTag();
 				QuestionActivity context = (QuestionActivity)
 						solutionView.getTag(R.id.TAG_CONTEXT_ID);
 				Solution solution = (Solution)
 						solutionView.getTag(R.id.TAG_SOLUTION_VIEW_ID);
 				new VoteSolutionTask(context,
 						solutionView, solution, true).execute();
 			}
 		});
 		
 		// get downvote button
 		Button downvoteButton = (Button)
 				solutionView.findViewById(R.id.button_downvote);
 		downvoteButton.setTag(solutionView);
 		downvoteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v){
 				View solutionView = (View) v.getTag();
 				QuestionActivity context = (QuestionActivity)
 						solutionView.getTag(R.id.TAG_CONTEXT_ID);
 				Solution solution = (Solution)
 						solutionView.getTag(R.id.TAG_SOLUTION_VIEW_ID);
 				new VoteSolutionTask(context,
 						solutionView, solution, false).execute();
 			}
 		});
 		
		/*
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		int verticalMargin = UIConstants.DEFAULT_VERTICAL_MARGIN;
		llp.setMargins(0, verticalMargin, 0, verticalMargin);
		solutionView.setLayoutParams(llp);
		*/
		
 		solutionsLayout.addView(solutionView);
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
 		solutionsLayout.setVisibility(View.VISIBLE);
 		
 		// Add post solution button to end of list
 		Button post = new Button(this);
 		post.setText(R.string.button_post_solution);
 		float dp = TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 1, 
 				getResources().getDisplayMetrics());
 		int height = (int) (40 * dp);
 		int margin = (int) (16 * dp);
 		LinearLayout.LayoutParams lp = 
 				new LinearLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, height, 1f);
 		lp.gravity = Gravity.CENTER_HORIZONTAL;
 		lp.setMargins(0, margin, 0, 0);
 		post.setLayoutParams(lp);
 		post.setPadding(margin, 0, margin, 0);
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
