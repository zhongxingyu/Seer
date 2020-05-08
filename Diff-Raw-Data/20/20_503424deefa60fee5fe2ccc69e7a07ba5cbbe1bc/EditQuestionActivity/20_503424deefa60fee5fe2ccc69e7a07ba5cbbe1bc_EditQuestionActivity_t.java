 package epfl.sweng.editquestions;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import epfl.sweng.R;
 import epfl.sweng.quizquestions.QuizQuestion;
 
 /**
  * Activity enabling the user to edit a question
  *
  */
 public class EditQuestionActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_edit_question);
         manageAnswers();
     }
 
     public QuizQuestion createQuestionFromUser() {
     	QuizQuestion question = new QuizQuestion();
     	// TODO Implement a listener that will hydrate the question and check its validity
     	return question;
     }
     
     public void manageAnswers() {
     	final LinearLayout answersContainer = (LinearLayout) findViewById(R.id.edit_answers_container);
     	
     	// TODO Remove questions, mark them as correct or not
     	
     	// Add new answers on clicking '+' button
     	final Button newQuestionButton = (Button) findViewById(R.id.edit_button_new_answer);
     	newQuestionButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				int nbAnswers = answersContainer.getChildCount()-1;
 				View newAnswer = getLayoutInflater().inflate(R.layout.edit_new_answer, null);
 				answersContainer.addView(newAnswer, answersContainer.getChildCount()-1);
				nbAnswers++;
				if (nbAnswers>=10) { // TODO Change this '10', need to access QuizQuestion.MAX_NUMBER_OF_ANSWERS 
					newQuestionButton.setEnabled(false);
				}
 			}
 		});
     }
     
     
     public void displaySubmitError() {
     	
     }
 }
