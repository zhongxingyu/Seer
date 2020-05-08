 package com.jozeflang.android.germanirregularverbs.main;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.InflateException;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.jozeflang.android.germanirregularverbs.db.VerbDTO.Perfect;
 import com.jozeflang.android.germanirregularverbs.util.Utils;
 import com.jozeflang.android.germanirregularverbs.validator.AnswerValidator;
 
 /** 
  * Entry point activity
  * @author Jozef Lang (developer@jozeflang.com)
  */
 public class EntryPointActivity extends Activity {
 	
 	private GermanIrregularVerbsApplication application;
 	
 	private EditText perfectAuxTE;
 	private EditText perfectInputTE;
 	private EditText preteriteInputTE;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		application = ((GermanIrregularVerbsApplication) getApplication());
 		
 		// Initialize UI components
 		setContentView(R.layout.main_layout);
 		initScreenElements(savedInstanceState);
 		
 		// Display currently active question
 		displayQuestion(application.getQuestion());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		try {
 			inflater.inflate(R.layout.main_menu, menu);
 		} catch (InflateException e) {
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		switch (item.getItemId()) {
 			case R.id.mainmenu_about:
 				AboutDialog aboutDialog = new AboutDialog(this);
 				aboutDialog.show();
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void initScreenElements(Bundle savedInstanceState) {
 		perfectAuxTE = ((EditText) findViewById(R.id.perfectAuxVerbInputTE));
 		perfectInputTE = ((EditText) findViewById(R.id.perfectInputTE));
 		preteriteInputTE = ((EditText) findViewById(R.id.preteriteInputTE));
 		initButtonsCallback(savedInstanceState);
 	}
 	
 	private void initButtonsCallback(Bundle savedInstanceState) {
 		Button nextBtn = (Button) findViewById(R.id.nextBtn);
 		Button skipBtn = (Button) findViewById(R.id.skipBtn);
 		Button helpBtn = (Button) findViewById(R.id.helpBtn);
 		
 		// Next button
 		nextBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Question activeQuestion = application.getQuestion();
 				if (!AnswerValidator.validate(activeQuestion.getVerb(), getAnswer(activeQuestion), activeQuestion.getAnswerType())) {
 					if (perfectAuxTE.hasFocus()) {
 						perfectAuxTE.setError(getString(R.string.incorrect_answer));
 					} else if (perfectInputTE.hasFocus()) {
 						perfectInputTE.setError(getString(R.string.incorrect_answer));
 					} else {
 						preteriteInputTE.setError(getString(R.string.incorrect_answer));
 					}
 				} else {
 					displayNewQuestion();
 				}
 			}
 		});
 		
 		// Skip button
 		skipBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				displayNewQuestion();
 			}
 		});
 		
 		// Help button
 		helpBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				displayCorrectAnswer(application.getQuestion());
 			}
 		});
 		
 	}
 	
 	private void displayNewQuestion() {
 		displayQuestion(application.getQuestion(true));
 	}
 	
 	private void displayQuestion(Question question) {
 		TextView display = (TextView) findViewById(R.id.displayTW);
 		TextView answerType = (TextView) findViewById(R.id.answerTypeTW);
 		display.setText(question.getVerb().getPresent());
 		answerType.setText(question.getAnswerType().getStringId());
 		
 		switch (question.getAnswerType()) {
 			case PERFECT:
 				 // Hide/show layout for a perfect question
 				 findViewById(R.id.perfectLayout).setVisibility(View.VISIBLE);
 				 findViewById(R.id.preteriteInputTE).setVisibility(View.GONE);
 				 perfectAuxTE.setText("");
 				 perfectInputTE.setText("");
 				break;
 			case PRETERITE:
 				// Hide/show layout for a perfect question
 				findViewById(R.id.perfectLayout).setVisibility(View.GONE);
 				findViewById(R.id.preteriteInputTE).setVisibility(View.VISIBLE);
 				preteriteInputTE.setText("");
 				break;
 		}
 		
 		// Hide errors
 		perfectAuxTE.setError(null);
 		perfectInputTE.setError(null);
 		preteriteInputTE.setError(null);
 		
 	}
 	
 	private Answer getAnswer(Question question) {
 		switch (question.getAnswerType()) {
 			case PERFECT:
 				return new Answer(perfectAuxTE.getText().toString(), perfectInputTE.getText().toString());
 			case PRETERITE:
 				return new Answer(null, preteriteInputTE.getText().toString());
 		}
 		return new Answer(null, null);
 	}
 	
 	private void displayCorrectAnswer(Question question) {
 		switch (question.getAnswerType()) {
 		case PRETERITE:
 			preteriteInputTE.setText(Utils.escapeGermanCharacters(question.getVerb().getPreterites().iterator().next().getPreterite()));
 			break;
 		case PERFECT:
 			Perfect perfect = question.getVerb().getPerfects().iterator().next();
 			perfectAuxTE.setText(Utils.escapeGermanCharacters(perfect.getAuxVerb()));
 			perfectInputTE.setText(Utils.escapeGermanCharacters(perfect.getPerfect()));
 			break;
 		}
 	}
 
 }
