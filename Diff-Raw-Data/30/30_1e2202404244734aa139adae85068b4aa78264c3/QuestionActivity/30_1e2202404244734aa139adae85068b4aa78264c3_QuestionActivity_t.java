 package com.education.flashmath;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.education.flashmath.fragment.ArithmeticQuestionAnswerFragment;
 import com.education.flashmath.fragment.ArithmeticQuestionFragment;
 import com.education.flashmath.fragment.FractionQuestionAnswerFragment;
 import com.education.flashmath.fragment.FractionQuestionFragment;
 import com.education.flashmath.fragment.QuestionFragment;
 import com.education.flashmath.models.ArithmeticQuestion;
 import com.education.flashmath.models.FractionQuestion;
 import com.education.flashmath.models.Question;
 import com.education.flashmath.network.FlashMathClient;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class QuestionActivity extends Activity {
 
 	private static final String NEXT_QUESTION_BTN_TITLE = "Next";
 	private static final String VERIFY_ANSWER_BTN_TITLE = "Answer";
 	private static final String END_QUIZ_BTN_TITLE = "Finish";
 	public static final String QUESTIONS_ANSWERED_INTENT_KEY = "QUESTIONS_ANSWERED";
 	public QuestionFragment qf;
 	public Fragment qaf;
 	private int currentQuestionIndex;
 	private ArrayList<Question> questionList;
 	private TextView tvQuestionProgress;
 	private Button btnVerifyAndNextQuestion;
 	private String subject;
 	private String backgroundColor;
	private boolean questionDisplayed;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_question);
 		
 		btnVerifyAndNextQuestion = (Button) findViewById(R.id.btnVerifyAndNextQuestion);
 		btnVerifyAndNextQuestion.setVisibility(View.VISIBLE);
 		subject = getIntent().getStringExtra("subject");
 		btnVerifyAndNextQuestion.setBackground(getButton());
 		Button btnClear = (Button) findViewById(R.id.btnClear);
 		btnClear.setBackground(getButton());
 		backgroundColor = getIntent().getStringExtra(SubjectActivity.SUBJECT_BACKGROUND_INTENT_KEY);
 		tvQuestionProgress = (TextView) findViewById(R.id.tvQuestionProgress);
 		tvQuestionProgress.setTextColor(getColor());
 		ActionBar ab = getActionBar();
 		ab.setIcon(getBarIcon());
 		String subjectTitle = Character.toUpperCase(subject.charAt(0))+subject.substring(1);
 		ab.setTitle(subjectTitle + " Questions");
 		
 		if (subject.equalsIgnoreCase("Fractions")) {
 			qf = new FractionQuestionFragment();
 		} else {
 			qf = new ArithmeticQuestionFragment();
 		}
 		setupServerQuestions();
 		qf.setBackgroundColor(backgroundColor);
 		
 		
 		getFragmentManager().beginTransaction().add(R.id.fragmentForQuestion, qf).commit();
		questionDisplayed = true;
 	}
 	
 	private Drawable getBarIcon() {
 		if(subject.equals("addition")){
 			return getResources().getDrawable(R.drawable.ic_action_plus);
 		} else if(subject.equals("subtraction")){
 			return getResources().getDrawable(R.drawable.ic_action_minus);
 		} else if(subject.equals("multiplication")){
 			return getResources().getDrawable(R.drawable.ic_action_times);
 		} else if(subject.equals("fractions")){
 			return getResources().getDrawable(R.drawable.ic_action_fraction);
 		} else {
 			return getResources().getDrawable(R.drawable.ic_action_divide);
 		}
 	}
 
 	private void setupServerQuestions() {
 		currentQuestionIndex = 0;
 		FlashMathClient client = FlashMathClient.getClient(this);
 		client.getQuestions(subject, new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject response) {
 				JSONArray jsonResults = null;
 				try {
 					jsonResults = response.getJSONArray("questions");
 					
 					questionList = new ArrayList<Question>();
 					
 					if (subject.equalsIgnoreCase("Fractions")) {
 						questionList.addAll(FractionQuestion.fromJSONArray(jsonResults, subject));
 					} else {
 						questionList.addAll(ArithmeticQuestion.fromJSONArray(jsonResults, subject));
 					}
 					
 					Log.d("DEBUG", questionList.toString());
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				
 				qf.setQuestion(questionList.get(currentQuestionIndex));
 				
 				if (subject.equalsIgnoreCase("Fractions")) {
 					((FractionQuestionFragment) qf).setupFractionQuestion();
 					qaf = new FractionQuestionAnswerFragment();
 					((FractionQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 				} else {
 					((ArithmeticQuestionFragment) qf).setupArithmeticQuestion();
 					qaf = new ArithmeticQuestionAnswerFragment();
 					((ArithmeticQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 				}
 				
 				tvQuestionProgress.setText((currentQuestionIndex + 1) + "/" + questionList.size());
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.question, menu);
 		return true;
 	}
 
 	public int getColor(){
 		int color = 0;
 		if(subject.equals("addition")){
 			color = Color.parseColor("#7979FF");
 		} else if(subject.equals("subtraction")){
 			color = Color.parseColor("#D79BFA");
 		} else if(subject.equals("multiplication")){
 			color = Color.parseColor("#66b266");
 		} else if(subject.equals("fractions")){
 			color = Color.parseColor("#FA96D2");
 		} else if(subject.equals("division")){
 			color = Color.parseColor("#44B4D5");
 		}
 		return color;
 	}
 
 	public void onClearQuestionAnswer(View v) {
 		qf.clearAnswerFields();
 	}
 	
 	public void onVerifyAndNextQuestionPressed(View v) {
 		if (btnVerifyAndNextQuestion.getText().toString().equals(VERIFY_ANSWER_BTN_TITLE)) {
 			onVerifyQuestionAnswer(v);
			questionDisplayed = false;
 		} else if (btnVerifyAndNextQuestion.getText().toString().equals(NEXT_QUESTION_BTN_TITLE)){
 			onNextQuestion(v);
			questionDisplayed = true;
 		} else {
 			qf.saveUserAnswer();
 			finalizeQuiz();
			questionDisplayed = false;
 		}
 	}
 	
 	public void onVerifyQuestionAnswer(View v) {
 		qf.saveUserAnswer();
 		
 		if (subject.equalsIgnoreCase("Fractions")) {
 			((FractionQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 
 			// Create and commit a new fragment transaction that adds the fragment for the back of
 			// the card, uses custom animations, and is part of the fragment manager's back stack.
 			getFragmentManager().beginTransaction()
 			// Replace the default fragment animations with animator resources representing
 			// rotations when switching to the back of the card, as well as animator
 			// resources representing rotations when flipping back to the front (e.g. when
 			// the system Back button is pressed).
 			.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
 								 R.animator.card_flip_left_in, R.animator.card_flip_left_out)
 			// Replace any fragments currently in the container view with a fragment
 			// representing the next page (indicated by the just-incremented currentPage
 			// variable).
 			.replace(R.id.fragmentForQuestion, (FractionQuestionAnswerFragment) qaf)
 			// Add this transaction to the back stack, allowing users to press Back
 			// to get to the front of the card.
 			.addToBackStack(null)
 			// Commit the transaction.
 			.commit();
 		} else {
 			((ArithmeticQuestionAnswerFragment) qaf).setQuestion(questionList.get(currentQuestionIndex));
 
 			// Create and commit a new fragment transaction that adds the fragment for the back of
 			// the card, uses custom animations, and is part of the fragment manager's back stack.
 			getFragmentManager().beginTransaction()
 			// Replace the default fragment animations with animator resources representing
 			// rotations when switching to the back of the card, as well as animator
 			// resources representing rotations when flipping back to the front (e.g. when
 			// the system Back button is pressed).
 			.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
 								 R.animator.card_flip_left_in, R.animator.card_flip_left_out)
 			// Replace any fragments currently in the container view with a fragment
 			// representing the next page (indicated by the just-incremented currentPage
 			// variable).
 			.replace(R.id.fragmentForQuestion, (ArithmeticQuestionAnswerFragment) qaf)
 			// Add this transaction to the back stack, allowing users to press Back
 			// to get to the front of the card.
 			.addToBackStack(null)
 			// Commit the transaction.
 			.commit();
 		}
 		
 		
 		
 		//we reached end of questions, remove the Next Question button and 
 		//let user use End Quiz button
 		if(currentQuestionIndex+1 >= this.questionList.size()) {
 			btnVerifyAndNextQuestion.setText(END_QUIZ_BTN_TITLE);
 		} else {
 			btnVerifyAndNextQuestion.setText(NEXT_QUESTION_BTN_TITLE);
 		}
 	}
 
 	public void onNextQuestion(View v) {
 		if (currentQuestionIndex < this.questionList.size()) {
 			btnVerifyAndNextQuestion.setText(VERIFY_ANSWER_BTN_TITLE);
 			//if user forgot to press save button and just presses next question
 			qf.saveUserAnswer();
 
 			Question q = qf.getQuestion();
 			this.questionList.remove(currentQuestionIndex);
 			this.questionList.add(currentQuestionIndex, q);
 			currentQuestionIndex++;
 
 			if(currentQuestionIndex < this.questionList.size()) {
 	            getFragmentManager().popBackStack();
 	            getFragmentManager().beginTransaction().remove(qf).commit();
 	            if (this.subject.equalsIgnoreCase("Fractions")) {
 	            	qf = new FractionQuestionFragment();
 	            } else {
 	            	qf = new ArithmeticQuestionFragment();
 				}
 	            
 	            qf.setBackgroundColor(backgroundColor);
 	            qf.setQuestion(this.questionList.get(currentQuestionIndex));
 	            getFragmentManager().beginTransaction().add(R.id.fragmentForQuestion, qf).commit();
 	            tvQuestionProgress.setText((currentQuestionIndex+1)+"/"+this.questionList.size());
 			} else {
 				finalizeQuiz();
 			}
 		}
 		else {
 			finalizeQuiz();
 		}
 	}
 
 	public void onEndQuiz(View v) {
 		qf.saveUserAnswer();
 		finalizeQuiz();
 	}
 	
 	private void finalizeQuiz() {
 		Intent i = new Intent(this, ResultActivity.class);
 		i.putExtra(QUESTIONS_ANSWERED_INTENT_KEY, this.questionList);
 		i.putExtra("subject", subject);
 		i.putExtra(SubjectActivity.SUBJECT_BACKGROUND_INTENT_KEY, backgroundColor);
 		startActivity(i);
 	}
 	
 	public Drawable getButton(){
 		if(subject.equals("addition")){
 			return getResources().getDrawable(R.drawable.btn_blue2);
 		} else if(subject.equals("subtraction")){
 			return getResources().getDrawable(R.drawable.btn_purple2);
 		} else if(subject.equals("multiplication")){
 			return getResources().getDrawable(R.drawable.btn_green2);
 		} else if(subject.equals("fractions")){
 			return getResources().getDrawable(R.drawable.btn_pink2);
 		} else {
 			return getResources().getDrawable(R.drawable.btn_yellow2);
 		}
 	}
 
	@Override
	public void onBackPressed() {
		//allow to go back only on the first question AND when the answer is not displayed yet
		//this is to prevent user from changing the question answers. On the first question
		//the user can go back to the main menu (if he did not answer the question yet).
		if (currentQuestionIndex == 0 && questionDisplayed) {
			super.onBackPressed();
		}
	}
 }
