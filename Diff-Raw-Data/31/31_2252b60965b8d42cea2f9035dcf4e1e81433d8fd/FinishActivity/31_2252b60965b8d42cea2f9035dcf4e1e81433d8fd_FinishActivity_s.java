 package edu.uci.ics.android;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.TextView;
 
 public class FinishActivity extends Activity {
 
 	private TextView correct;
 	private TextView incorrect;
 	private TextView totalQuestions;
 	private TextView timePerQuestion;
 	private TextView totalTime;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.finish);
 		
 		this.correct = (TextView)this.findViewById(R.id.correctText);
 		this.incorrect = (TextView)this.findViewById(R.id.incorrectText);
 		this.totalQuestions = (TextView)this.findViewById(R.id.totalQuestions);
 		this.timePerQuestion = (TextView)this.findViewById(R.id.timePerQuestion);
 		this.totalTime = (TextView)this.findViewById(R.id.totalTime);
 		
 		this.correct.setText("Number correct: " + getIntent().getExtras().getInt("numCorrect"));
 		this.incorrect.setText("Number incorrect: " + getIntent().getExtras().getInt("totalQuestions"));
 		this.totalQuestions.setText("Total questions: " + getIntent().getExtras().getInt("totalQuestions"));
		this.timePerQuestion.setText("Total Time: " + (getIntent().getExtras().getLong("totalTime") / 1000) + " seconds");
 		this.totalTime.setText("Total time: " + (getIntent().getExtras().getLong("totalTime") / 1000) + " seconds");
 	}
 }
