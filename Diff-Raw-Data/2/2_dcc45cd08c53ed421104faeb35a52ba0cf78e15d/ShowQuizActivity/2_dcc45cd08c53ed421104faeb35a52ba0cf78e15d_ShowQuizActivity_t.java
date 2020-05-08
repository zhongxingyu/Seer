 package epfl.sweng.quizzes;
 
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import epfl.sweng.R;
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.tasks.LoadQuiz;
 import epfl.sweng.tasks.SubmitQuizAnswers;
 import epfl.sweng.tasks.interfaces.IQuizAnswersSubmittedCallback;
 import epfl.sweng.tasks.interfaces.IQuizReceivedCallback;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
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
 
 	private QuizQuestion mQuestionDisplayed;
 	private Quiz mQuiz;
 	private int mQuestionIndex = 0;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_show_quiz);
         
         Intent startingIntent = getIntent();
         int quizId = startingIntent.getIntExtra("id", -1);
         
         new LoadQuiz(new IQuizReceivedCallback() {
 			
 			@Override
 			public void onSuccess(Quiz quiz) {
 				mQuiz = quiz;
 				displayQuestion(quiz.getQuestions().get(0));
 				mQuestionIndex = 0;
 			}
 			
 			@Override
 			public void onError() {
 				displayLoadQuizError();
 			}
 
 		}, quizId).execute();
     }
 
     public void displayScoreAlertDialog(double score) {
    	String displayedText = String.format(getText(R.string.quiz_score_alert_dialog_text).toString(), score);
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
     
     public void displayQuestion(final QuizQuestion question) {
 
     	final ListView listView = (ListView) findViewById(R.id.quiz_listView);
         final TextView questionTxt = (TextView) findViewById(R.id.quiz_question);
 
         mQuestionDisplayed = question;
         questionTxt.setText(question.getQuestion());
                        
         // Instantiating array adapter to populate the listView
         // Using an ArrayList instead of an Array to populate, for future modifications
         // The layout android.R.layout.simple_list_item_single_choice creates radio button for each listview item
         final ArrayList<String> listAnswers = new ArrayList<String>();
         listAnswers.addAll(Arrays.asList(question.getAnswers()));
         
         if (question.getAnswerIndex()!=-1) {
         	listAnswers.set(question.getAnswerIndex(), listAnswers.get(question.getAnswerIndex()) + " \u2724");
         }
 		
         final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
         														android.R.layout.simple_list_item_single_choice,
         														listAnswers);
 
         listView.setAdapter(adapter);
         listView.setEnabled(true);
         
         
         // Implementing the interaction with the user
         listView.setOnItemClickListener(new OnItemClickListener() {
         	@Override
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         		
         		listAnswers.clear();
         		listAnswers.addAll(Arrays.asList(question.getAnswers()));
                 
         		if (mQuestionDisplayed.getAnswerIndex() == position) {
         			mQuestionDisplayed.setAnswerIndex(-1);
         		} else {
         			mQuestionDisplayed.setAnswerIndex(position);
         			listAnswers.set(position, listAnswers.get(position) + " \u2724");
         		}
         		adapter.notifyDataSetChanged();
         	}
         });
 
         
     } 
     
     /**
      * Handles the "Previous question" Button 
      * @param previousButton
      */
     public void clickedPreviousQuestion(View previousButton) {
     	mQuestionIndex = (mQuestionIndex-1 + mQuiz.size()) % mQuiz.size();
 		displayQuestion(mQuiz.getQuestions().get(mQuestionIndex));
     }
     
     /**
      * Handles the "Next question" Button 
      * @param nextButton
      */
     public void clickedNextQuestion(View nextButton) {
     	mQuestionIndex = (mQuestionIndex+1) % mQuiz.size();
     	displayQuestion(mQuiz.getQuestions().get(mQuestionIndex));
     }
     
 	/**
      * Handles the "Hand in quiz" Button 
      * @param handInButton
      */
     public void clickedHandInQuiz(View handInButton) {
     	new SubmitQuizAnswers(new IQuizAnswersSubmittedCallback() {
 			
 			@Override
 			public void onSubmitSuccess(double score) {
 				displayScoreAlertDialog(score);
 			}
 			
 			@Override
 			public void onError() {
 				displaySumitQuizAnswersError();
 			}
 
 		}, mQuiz).execute();
     }
     
 
     private void displaySumitQuizAnswersError() {
     	String displayedText = new String(getText(R.string.quiz_hand_in_error).toString());
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
 	
 	private void displayLoadQuizError() {
         final TextView questionTxt = (TextView) findViewById(R.id.quiz_question);
         questionTxt.setText(getText(R.string.quiz_load_error).toString());
 	}
 
 }
