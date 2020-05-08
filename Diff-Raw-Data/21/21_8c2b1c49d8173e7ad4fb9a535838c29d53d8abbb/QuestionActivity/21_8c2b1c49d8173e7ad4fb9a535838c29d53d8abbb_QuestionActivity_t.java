 package com.flashmath.activity;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.education.flashmath.R;
 import com.flashmath.fragment.ArithmeticQuestionAnswerFragment;
 import com.flashmath.fragment.ArithmeticQuestionFragment;
 import com.flashmath.fragment.FractionQuestionAnswerFragment;
 import com.flashmath.fragment.FractionQuestionFragment;
 import com.flashmath.fragment.QuestionFragment;
 import com.flashmath.models.ArithmeticQuestion;
 import com.flashmath.models.FractionQuestion;
 import com.flashmath.models.Question;
 import com.flashmath.network.FlashMathClient;
 import com.flashmath.util.ColorUtil;
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
 		btnVerifyAndNextQuestion.setBackground(ColorUtil.getButtonStyle(subject, this));
 		Button btnClear = (Button) findViewById(R.id.btnClear);
 		btnClear.setBackground(ColorUtil.getButtonStyle(subject, this));
 		backgroundColor = ColorUtil.identifySubjectColor(subject);
 		tvQuestionProgress = (TextView) findViewById(R.id.tvQuestionProgress);
		TextView tvQuestionSlash = (TextView) findViewById(R.id.tvQuestionSlash);
		TextView tvQuestionTotal = (TextView) findViewById(R.id.tvQuestionTotal);
 		tvQuestionProgress.setTextColor(ColorUtil.subjectColorInt(subject));
		tvQuestionSlash.setTextColor(ColorUtil.subjectColorInt(subject));
		tvQuestionTotal.setTextColor(ColorUtil.subjectColorInt(subject));
 		ActionBar ab = getActionBar();
 		ab.setIcon(ColorUtil.getBarIcon(subject, this));
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
 				
				tvQuestionProgress.setText(String.valueOf(currentQuestionIndex + 1));
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.question, menu);
 		return true;
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
	            tvQuestionProgress.setText(String.valueOf(currentQuestionIndex + 1));
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
 		startActivity(i);
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
