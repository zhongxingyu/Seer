 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.app.AlertDialog;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ListView;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.domain.Question;
 import no.hials.muldvarp.v2.domain.Quiz;
 import no.hials.muldvarp.v2.fragments.QuizQuestionFragment;
 
 /**
  * This class defines an Activity used for Quiz-functionality. 
  *
  * @author johan
  */
 public class QuizActivity extends MuldvarpActivity{
     
     //Global Variables
     View mainQuizView;
     View holderQuizView;
     ListView listView;
     Quiz quiz;
     List<Question> questions = new ArrayList<Question>();
     int currentQuestionNumber;
     //Fragments
     ArrayList<QuizQuestionFragment> questionFragments;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);                
         //See if the Activity was started with an Intent that included a Domain object
         if(getIntent().hasExtra("Domain")) {
             domain = (Domain) getIntent().getExtras().get("Domain");
             activityName = domain.getName();
             quiz = (Quiz) domain;
             setupQuiz();
             
         }        
     }
     
     public void setupQuiz(){
         //Change content view with animation
         LayoutInflater inflator = getLayoutInflater();
         holderQuizView =  inflator.inflate(R.layout.activity_quiz_question_holder, null, false);
         holderQuizView.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));        
         setContentView(holderQuizView);
         //Get fragments        
         currentQuestionNumber = 0;
         if (!quiz.getQuestions().isEmpty()) {
             questionFragments = new ArrayList<QuizQuestionFragment>();
             fillQuestionFragmentList();                        
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             ft.replace(R.id.QuizQuestionFragmentHolder, questionFragments.get(currentQuestionNumber)).commit();
         }
         
         Button backToMainQuizButton = (Button) findViewById(R.id.backToMainQuizActivityButton);
         backToMainQuizButton.setText(R.string.quizBackToMainQuizButtonText);
          backToMainQuizButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 showReturnDialog(); 
             }
         });
         final Button prevQuestionButton = (Button) findViewById(R.id.quizPreviousButton);
         prevQuestionButton.setEnabled(false);
         prevQuestionButton.setText(R.string.quizPreviousButtonText);
         prevQuestionButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (currentQuestionNumber > 0) {
                     Button nextQuestionButton = (Button) findViewById(R.id.quizNextButton);
                     nextQuestionButton.setText(R.string.quizNextButtonText);
                     onBackPressed();
                     if (currentQuestionNumber == 0){
                     prevQuestionButton.setEnabled(false);
                     }     
                 }            
             }
         });
         final Button nextQuestionButton = (Button) findViewById(R.id.quizNextButton);
         nextQuestionButton.setText(R.string.quizNextButtonText);
         nextQuestionButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {                
                 if (currentQuestionNumber < (quiz.getQuestions().size() -1)) {
                     currentQuestionNumber++;
                     prevQuestionButton.setEnabled(true);
                     FragmentTransaction ft = getFragmentManager().beginTransaction();
 //                    ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
 //                            R.anim.fragment_slide_left_exit,
 //                            R.anim.fragment_slide_right_enter,
 //                            R.anim.fragment_slide_right_exit);
                     ft.replace(R.id.QuizQuestionFragmentHolder, questionFragments.get(currentQuestionNumber));
                     ft.addToBackStack(null);
                     ft.commit();
                     if (currentQuestionNumber >= quiz.getQuestions().size()-1){
                         nextQuestionButton.setText(R.string.quizGoToResultsButtonText);
                     }
                 } else if (currentQuestionNumber >= quiz.getQuestions().size()-1){
                     
                     if(checkQuestionsForEmptyAnswers()){
                         showWarningDialog();
                     } else {
                         Intent quizResultsIntent = new Intent(getApplicationContext(), QuizResultActivity.class);
                         quizResultsIntent.putExtra("Quiz", quiz);
                         startActivity(quizResultsIntent);
                         finish(); //end the quizactivity 
                     }
                 }
             }
         }); 
     }
     
     /**
      * This method runs through all the questions in the quiz and returns true if there are
      * unanswered questions.
      * 
      * It uses the checkForEmptyAnswers() method in the QuizQuestionFragment.
      * @return 
      */
     public boolean checkQuestionsForEmptyAnswers(){
         for (int i = 0; i < quiz.getQuestions().size(); i++) {
             QuizQuestionFragment tempFrag = questionFragments.get(i);
             if(!tempFrag.checkForEmptyAnswers()){
                 return true;
             } 
         }  
         return false;
     }
 
     @Override
     public void onBackPressed() {
         currentQuestionNumber--;
         super.onBackPressed();
     }
     
     private void fillQuestionFragmentList(){
         //Only fill question fragment list if it hasn't been filled already        
         if(questionFragments.isEmpty()){
             questionFragments = new ArrayList<QuizQuestionFragment>();            
             for (int i = 0; i < quiz.getQuestions().size(); i++) {
                 Question tempQuestion = quiz.getQuestions().get(i);
                 QuizQuestionFragment tempFrag = new QuizQuestionFragment(tempQuestion);
                 tempFrag.setQuestionAmount(quiz.getQuestions().size());
                 tempFrag.setQuestionNo(i+1);                        
                 questionFragments.add(tempFrag);
             }            
         }        
     }    
     
     /**
      * Void method containing functionality to construct a dialog.
      */
     public void showReturnDialog(){
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.quizResultBackToQuizText).setTitle(R.string.quizResultBackToQuizPrompt);        
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //DO NOTHING
            }
         });
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish(); //end the quizactivity 
            }
         });
         AlertDialog dialog = builder.create();
         dialog.show();
     }
     
     /**
      * Void method containing functionality to construct a dialog.
      */
     public void showWarningDialog(){
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.quizUnfilledAnswersWarningText).setTitle(R.string.warning);        
         
         builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               onBackPressed();
            }
         });
         AlertDialog dialog = builder.create();
         dialog.show();
     }
 }
