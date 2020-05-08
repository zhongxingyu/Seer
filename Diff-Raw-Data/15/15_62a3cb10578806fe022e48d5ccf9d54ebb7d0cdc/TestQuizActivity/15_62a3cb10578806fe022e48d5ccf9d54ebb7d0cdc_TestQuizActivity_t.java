 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.ObjectAnimator;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.AnimationUtils;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.Interpolator;
 import android.widget.ArrayAdapter;
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
  * This class defines an Activity used for Quiz-functionality. 
  *
  * @author johan
  */
 public class TestQuizActivity extends MuldvarpActivity{
     
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
         //Set Layout from XML-file
         setContentView(R.layout.quiz_activity_main);
         
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
         //Change content view with animatino
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
             ft.add(R.id.QuizQuestionFragmentHolder, questionFragments.get(currentQuestionNumber)).commit();
         }
         
         Button nextQuestionButton = (Button) findViewById(R.id.QuizNextButton);
         nextQuestionButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {                
                 if (currentQuestionNumber < (quiz.getQuestions().size() -1)) {
                     currentQuestionNumber++;
                     FragmentTransaction ft = getFragmentManager().beginTransaction();
                     ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                             R.anim.fragment_slide_left_exit,
                             R.anim.fragment_slide_right_enter,
                             R.anim.fragment_slide_right_exit);
                     ft.replace(R.id.QuizQuestionFragmentHolder, questionFragments.get(currentQuestionNumber));
                     ft.addToBackStack(null);
                     ft.commit();
                 } else if (currentQuestionNumber >= quiz.getQuestions().size()-1){
                     currentQuestionNumber = quiz.getQuestions().size();
                     prepAnswer();
                 }
             }
         });
         
         Button prevQuestionButton = (Button) findViewById(R.id.QuizPreviousButton);
         prevQuestionButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 if (currentQuestionNumber > 0) {
                     currentQuestionNumber--;
                     onBackPressed();
                 } else {
                     currentQuestionNumber = 0;                    
                 }                
             }
         });
     }   
     
     public void prepAnswer(){
         setContentView(R.layout.activity_quiz_question_ver);
         answerView = (ListView) findViewById(R.id.list_answer);
         resultView = (ListView) findViewById(R.id.list_results);
         final ArrayAdapter<String> adapterAns = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, LIST_ANS);
         final ArrayAdapter<String> adapterRes = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, LIST_RES);
         answerView.setAdapter(adapterAns);
         resultView.setAdapter(adapterRes);
         resultView.setRotationY(-90f);
         Button starter = (Button) findViewById(R.id.revealAnswerButton);
         starter.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 flipit();
             }
         });
     }
     
     private void fillQuestionFragmentList(){
        //Only fill question fragment list if it hasn't been filled already        
        if(questionFragments.isEmpty()){
            questionFragments = new ArrayList<QuizQuestionFragment>();            
             for (int i = 0; i < quiz.getQuestions().size(); i++) {
                 Question tempQuestion = quiz.getQuestions().get(i);
                 questionFragments.add(new QuizQuestionFragment(tempQuestion));
             }            
         }        
     }
     
             
     ListView answerView; //ListView holding answers supplied by user
     ListView resultView;//ListView holding actual answers
     private Interpolator accelerator = new AccelerateInterpolator();
     private Interpolator decelerator = new DecelerateInterpolator();
     private void flipit() {
         final ListView visibleList;
         final ListView invisibleList;
         if (answerView.getVisibility() == View.GONE) {
             visibleList = resultView;
             invisibleList = answerView;
         } else {
             invisibleList = resultView;
             visibleList = answerView;
         }
         ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visibleList, "rotationY", 0f, 90f);
         visToInvis.setDuration(500);
         visToInvis.setInterpolator(accelerator);
         final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisibleList, "rotationY",
                 -90f, 0f);
         invisToVis.setDuration(500);
         invisToVis.setInterpolator(decelerator);
         visToInvis.addListener(new AnimatorListenerAdapter() {
             @Override
             public void onAnimationEnd(Animator anim) {
                 visibleList.setVisibility(View.GONE);
                 invisToVis.start();
                 invisibleList.setVisibility(View.VISIBLE);
             }
         });
         visToInvis.start();
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
     
     
     /**
      * Below is test stuff
      */
     private static final String[] LIST_ANS = new String[] {
             "Answer 1",
             "Answer 2",
             "Answer 3",
             "Answer 4",
             "Answer 5",
             "Answer 6"
     };
     private static final String[] LIST_RES = new String[] {
             "Result 1",
             "Result 2",
             "Result 3",
             "Result 4",
             "Result 5",
             "Result 6"
     };
     
 }
