 package epfl.sweng.quizzes;
 
 
 
 import epfl.sweng.R;
 import epfl.sweng.authentication.SessionManager;
 import epfl.sweng.entry.MainActivity;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.tasks.IQuizReceivedCallback;
 import epfl.sweng.tasks.LoadQuiz;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 /**
  * Class allowing to take a quiz and hand in the answers to the server
  * @author dchriste
  *
  */
 public class ShowQuizActivity extends Activity {
 	
 	// TODO A la création de mQuiz, mChoices doit être initialisé avec :
 	// { "choices": [ null, null, ..., null] }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_quiz);
         
         if (!SessionManager.getInstance().isAuthenticated()) {
         	finish();
         	Intent mainActivityIntent = new Intent(this, MainActivity.class);
         	startActivity(mainActivityIntent);
         }
         
         Intent startingIntent = getIntent();
         int quizId = startingIntent.getIntExtra("id", -1);
         
         new LoadQuiz(new IQuizReceivedCallback() {
 			
 			@Override
 			public void onSuccess(Quiz quiz) {
 				displayQuestion(quiz.getQuestions().get(0));
 			}
 			
 			@Override
 			public void onError() {
 				displayError();
 			}
 
 		}, quizId).execute();
     }
 
     public void displayScoreAlertDialog(double score) {
     	String displayedText = new String(getText(R.string.quiz_score_alert_dialog_text).toString() + " " + score);
     	AlertDialog.Builder alert=new AlertDialog.Builder(this);
     	alert.setMessage(displayedText);
     	alert.setTitle(R.string.quiz_score_alert_dialog_title);
     	
     	alert.setPositiveButton(R.string.quiz_score_alert_dialog_button_text, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 // Nothing special for the time being... //Dana
             }
         });
     	Log.i("ALERT_DIALOG", displayedText);
     	AlertDialog alertBox = alert.create();
     	alertBox.show();
     }
     
     /**
      * Display a question on the screen
      * @param question The question to be displayed
      */
     public void displayQuestion(final QuizQuestion question) {
     	
     	TextView questionTxt = (TextView) findViewById(R.id.quiz_question);
     	questionTxt.setText(question.getQuestion());
     	
     	LinearLayout answersContainer = (LinearLayout) findViewById(R.id.quiz_answers_container);
     	for (int i=0; i < question.getAnswers().length; i++) {
     		TextView answerTextView = new TextView(getApplicationContext());
     		answerTextView.setText(question.getAnswers()[i]);
     		answersContainer.addView(answerTextView, i);
     	}
     }
     
     /**
      * Handles the "Previous question" Button 
      * @param previousButton
      */
     public void clickedPreviousQuestion(View previousButton) {
     	
     }
     
     /**
      * Handles the "Next question" Button 
      * @param nextButton
      */
     public void clickedNextQuestion(View nextButton) {
     	
     }
     
     /**
      * Handles the "Hand in quiz" Button 
      * @param handInButton
      */
     public void clickedHandInQuiz(View handInButton) {
     	
     }
     
     public void displayError() {
     	
     }
 }
