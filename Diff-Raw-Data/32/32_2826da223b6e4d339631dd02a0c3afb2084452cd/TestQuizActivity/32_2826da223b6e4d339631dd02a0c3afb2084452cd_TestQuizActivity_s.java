 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import java.util.ArrayList;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.v2.domain.Domain;
 import no.hials.muldvarp.v2.domain.Question;
 import no.hials.muldvarp.v2.domain.Quiz;
 import no.hials.muldvarp.v2.fragments.MuldvarpFragment;
 import no.hials.muldvarp.v2.fragments.QuizQuestionFragment;
 
 /**
 * This class defines an Activity used for Quiz-functionality. Should
 * encapsulate a fragment, but NYI
  *
  * @author johan
  */
 public class TestQuizActivity extends MuldvarpActivity{
     
     //Global Variables
     ListView listView;
     Quiz quiz;
     List<Question> questions = new ArrayList<Question>();
     int currentQuestionNumber;
     //Fragments
     ArrayList<QuizQuestionFragment> questionFragments;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //Set Layout from XML-file
         setContentView(R.layout.quiz_activity_main);
        currentQuestionNumber = 0;
         
         //See if the Activity was started with an Intent that included a Domain object
         if(getIntent().hasExtra("Domain")) {
             domain = (Domain) getIntent().getExtras().get("Domain");
             activityName = domain.getName();
             quiz = (Quiz) domain;
             
             TextView quizName = (TextView) findViewById(R.id.QuizNameText);
             quizName.setText(quiz.getName());
             
             
             if(quiz.getDescription() != null){
                 TextView quizDescription = (TextView) findViewById(R.id.QuizNameDescription);
                 quizDescription.setText(quiz.getDescription());
             } 
             
             TextView questionNumber = (TextView) findViewById(R.id.QuestionAmountNo);
             questionNumber.setText(String.valueOf(quiz.getQuestions().size()));
             if (quiz.getQuestions().size() > 0) {
                 setOnClickListeners();
             } else {
                 Button startQuizButton = (Button) findViewById(R.id.StartQuizButton);
                 startQuizButton.setText("Denne Quiz'en har ingen spørsmål.");
                 startQuizButton.setClickable(false);
             }
         }        
     }
     
     private void setOnClickListeners(){
         
         Button startQuizButton = (Button) findViewById(R.id.StartQuizButton);
         startQuizButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startQuiz();
             }
         });        
     }
     
     public void startQuiz(){
         //Change content view
         setContentView(R.layout.activity_quiz_question_holder);
        //Get fragments
         if (!quiz.getQuestions().isEmpty()) {
             fillFragmentList();                        
             FragmentTransaction ft = getFragmentManager().beginTransaction();
             ft.add(R.id.QuizQuestionFragmentHolder, questionFragments.get(currentQuestionNumber)).commit();
         }
         
         Button nextQuestionButton = (Button) findViewById(R.id.QuizNextButton);
         nextQuestionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentQuestionNumber++;
                if (currentQuestionNumber < quiz.getQuestions().size()) {
                     FragmentTransaction ft = getFragmentManager().beginTransaction();
                     ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                             R.anim.fragment_slide_left_exit,
                             R.anim.fragment_slide_right_enter,
                             R.anim.fragment_slide_right_exit);
                     ft.replace(R.id.QuizQuestionFragmentHolder, questionFragments.get(currentQuestionNumber));
                     ft.addToBackStack(null);
                     ft.commit();
                 }
             }
         });
         
         Button prevQuestionButton = (Button) findViewById(R.id.QuizPreviousButton);
         prevQuestionButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (currentQuestionNumber > 0) {
                     currentQuestionNumber--;
                 } else {
                     currentQuestionNumber = 0;
                 }
                 onBackPressed();
             }
         });
         
         
     }
     
     public void addFragmentToStack() {
 
         // Instantiate a new fragment.
         MuldvarpFragment newFragment = new QuizQuestionFragment(quiz.getQuestions().get(currentQuestionNumber));        
                 
         // Add the fragment to the activity, pushing this transaction
         // on to the back stack.
         FragmentTransaction ft = getFragmentManager().beginTransaction();
         ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                 R.anim.fragment_slide_left_exit,
                 R.anim.fragment_slide_right_enter,
                 R.anim.fragment_slide_right_exit);
         ft.replace(R.id.QuizQuestionFragmentHolder, newFragment);
         ft.addToBackStack(null);
         ft.commit();
     }
     
     private void fillFragmentList(){
         questionFragments = new ArrayList<QuizQuestionFragment>();
         for (int i = 0; i < quiz.getQuestions().size(); i++) {
             Question tempQuestion = quiz.getQuestions().get(i);
             questionFragments.add(new QuizQuestionFragment(tempQuestion));
         }
     }
     
     private void updateButtons() {
         if (currentQuestionNumber >= questionFragments.size()) {
             Button nextQuestionButton = (Button) findViewById(R.id.QuizNextButton);
             nextQuestionButton.setClickable(false);
         } else {
             Button nextQuestionButton = (Button) findViewById(R.id.QuizNextButton);
             nextQuestionButton.setClickable(true);
         }
 //        
 //        if (currentQuestionNumber <= 0) {
 //            Button prevQuestionButton = (Button) findViewById(R.id.QuizPreviousButton);
 //            prevQuestionButton.setClickable(false);
 //        } else if (currentQuestionNumber <= 0){
 //            Button prevQuestionButton = (Button) findViewById(R.id.QuizPreviousButton);
 //            prevQuestionButton.setClickable(true);
 //        }
     }
     
 }
