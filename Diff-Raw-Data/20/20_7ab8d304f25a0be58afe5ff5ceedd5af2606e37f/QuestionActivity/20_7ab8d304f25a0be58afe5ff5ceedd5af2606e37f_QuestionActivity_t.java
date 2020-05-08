 package com.sambuo.stackoverlook;
 
 import com.sambuo.stackoverlook.entities.Question;
 import com.sambuo.stackoverlook.repositories.StackOverflowRepository;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.widget.TextView;
 
 public class QuestionActivity extends Activity {
 	
 	private final StackOverflowRepository repository = StackOverflowRepository.getInstance();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.question);
 		
 		Intent intent = getIntent();
 		if (!intent.hasExtra(AnswersActivity.EXTRA_QUESTION_ID)) {
 			Log.e(QuestionActivity.class.toString(), "QuestionId was not given to QuestionActivity");
 		}
 		
 		long questionId = intent.getLongExtra(AnswersActivity.EXTRA_QUESTION_ID, 0);
 		Question question = this.repository.getQuestionFromQuestionId(questionId);
 		android.util.Log.v("test", String.format("%d", questionId));
 		
 		TextView title = (TextView) findViewById(R.id.title);
		TextView questionBody = (TextView) findViewById(R.id.questionBody);
		TextView score = (TextView) findViewById(R.id.score);
		TextView answerBody = (TextView) findViewById(R.id.answerBody);
 		
		title.setText(Html.fromHtml(question.getTitle()));
		questionBody.setText(Html.fromHtml(question.getBody()));
		score.setText(String.format("%d", question.getScore()));
		answerBody.setText("Answer");
 	}
 }
