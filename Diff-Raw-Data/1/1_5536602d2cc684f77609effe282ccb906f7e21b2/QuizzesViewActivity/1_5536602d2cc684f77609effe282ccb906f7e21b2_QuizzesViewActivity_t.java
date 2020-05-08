 package com.huskysoft.eduki;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.huskysoft.eduki.data.Quiz;
 import com.huskysoft.eduki.data.QuizContent;
 import com.huskysoft.eduki.data.QuizContent.Problem;
 import com.huskysoft.eduki.data.QuizQuery;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 /**
  * @author Rafael Vertido Class QuizzesViewActivity allows users to take 
  * multiple choice quizzes
  */
 @SuppressLint("DefaultLocale")
 public class QuizzesViewActivity extends Activity implements TaskComplete {
 
     /**
      * private global variables that are initialized after requests to database are
      * completed 
      * 
      * questions - Questions that will be asked 
      * choices - Choices associated with each question 
      * answers - Answers associated with each question 
      * quiz - Quiz chosen by user 
      * quizContent - Structure that holds details of the quiz
      */
     private List<RadioGroup> answersRadioGroup;
     private List<String> questions;
     private List<String> answers;
     private Quiz quiz;
     private QuizContent quizContent;
     private int course_id;
 
     /**
      * Static variables to keep track of quiz state 
      * questionsAnswered - Total number answered 
      * questionsCorrect  - Number answered correctly
      */
     private static int questionsAnswered = 0;
     private static int questionsCorrect = 0;
     private static int questionsCreated = 0;
 
     /** Constant required to match ASCII 'a' to index 0 */
     private static final int ASCII_NUM = 97;
     
     /** Save 'this' for access to nested classes */
     private final Context context = this;
 
     /** Quiz answer choices, used for dynamic generation of quiz content */
     private final String[] choices = {"A", "B", "C", "D"};
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             String quiz_title = extras.getString("quiz_title");
             int quiz_id = extras.getInt("quiz_id");
             course_id = extras.getInt("course_id");
             quiz = new Quiz(quiz_id, course_id, quiz_title);
             QuizQuery.getAllQuestions(this, quiz_id, 0);
         }
         setContentView(R.layout.loading_screen);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void taskComplete(String data, int id) {
         // Get the list of questions, and set the title
         quizContent = QuizQuery.parseQuestionsList(data);
         List<Problem> problemList = quizContent.getProblems();
 
         // Check if there are questions for this quiz, if so then begin the
         // quiz. Otherwise then display a message that there is no content.
         if (problemList.size() == 0) {
             setContentView(R.layout.activity_no_list_found);
             TextView contentView = (TextView) findViewById(R.id.noListText);
             contentView.setText("No questions found for this quiz.");
         } else {
             // We are ready to display quiz content, initialize required fields
             initializeLists();
 
             // Process the input data
             Log.w("Eduki", "Eduki: parsing quiz content");
             parseQuizContent(problemList);
             
             // Set to the proper view and set the correct title
             Log.w("Eduki", "Eduki: Setting content view");
             setContentView(R.layout.activity_quizzesview);
             ((TextView) findViewById(R.id.title)).setText(quiz.getTitle());
             
             // Display quiz content and attach submit listener
             Log.w("Eduki", "Eduki: Updating quiz");
             updateQuiz();
             Log.w("Eduki", "Eduki: Setting up listener");
             setupSubmitListener();
         }
     }
 
     @Override
     public void onBackPressed() {
         resetData();
         super.onBackPressed();
     }
     
     /**
      * Parse the problemList as retrieved as a List of Problem objects.
      * Populates the questions and answers lists required for displaying the quiz
      * the quiz content to the user dynamically.
      * 
      * @param problemList List of problems to parse
      */
     public void parseQuizContent(List<Problem> problemList) {
         for (Problem p : problemList) {
             questions.add(p.getQuestion());
             answers.add(p.getAnswer());
         }
     }
 
     /**
      * Updates the state of the quiz. Manages what is displayed to the user,
      * shows a new problem if there are any unanswered, a message noting there
      * are no problems associated with the quiz if there are none, or displays
      * the quiz results if the user has completed the quiz.
      */
     private void updateQuiz() {
         LinearLayout questionsLayout = (LinearLayout) this.findViewById(R.id.quizScrollViewLayout);
        questionsLayout.setPadding(0, 0, 0, 15);
         
         for (String question: questions) {
             // Create necessary layout components to display a question
             LinearLayout questionLayout = new LinearLayout(this);
             TextView questionTextView = new TextView(this);
             TextView questionNumber = new TextView(this);
             
             // Set text content
             questionTextView.setText(question);
             questionTextView.setPadding(10, 5, 0, 0);
             questionNumber.setText((questionsCreated + 1) + ".");
             questionNumber.setPadding(0, 5, 0, 0);
             
             // Nest and style the views
             questionLayout.setOrientation(LinearLayout.HORIZONTAL);
             questionLayout.addView(questionNumber); 
             questionLayout.addView(questionTextView);
             questionsLayout.addView(questionLayout);
             
             generateAnswers(questionsLayout);
             questionsCreated++;
         }
     }
 
     /**
      * Dynamically generate the proper choices for the given question that the user 
      * is currently answering.
      * 
      * @param questionNumber Question # that the user is at
      */
     private void generateAnswers(LinearLayout questionsLayout) {
         // Create necessary layouts and set orientation of radio group to horizontal
         LinearLayout answersLayout = new LinearLayout(this);        
         RadioGroup currentAnswersGroup = new RadioGroup(this);
         currentAnswersGroup.setOrientation(RadioGroup.HORIZONTAL);
         currentAnswersGroup.setPadding(18, 0, 0, 0); // Shift right to align with question
         
         // Get the choices and display them as radio button choices
         RadioButton[] rb = new RadioButton[choices.length];
         for (int i = 0; i < rb.length; i++) {
             rb[i] = new RadioButton(this);
             rb[i].setText(choices[i]);
             rb[i].setId(i);
             rb[i].setTextColor(Color.parseColor(getResources().getString(R.color.content)));
             currentAnswersGroup.addView(rb[i]);
         }
         
         // Add to the collection of answer groups
         answersRadioGroup.add(currentAnswersGroup);
         
         // Put together the radio buttons and add to the ScrollView
         answersLayout.addView(currentAnswersGroup);
         questionsLayout.addView(answersLayout);
     }
     
     private void setupSubmitListener() {
         // Set a listener for the submit button
         Button submitButton = (Button) findViewById(R.id.submitButton);
         submitButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 for (int i = 0; i < answersRadioGroup.size(); i ++) {
                     // Get the user's selected answer
                     int answerButtonId = answersRadioGroup.get(i).getCheckedRadioButtonId();
                     View radioButton = answersRadioGroup.get(i).findViewById(answerButtonId);
                     int answerIndex = answersRadioGroup.get(i).indexOfChild(radioButton);
 
                     // Check if any of the questions were not answered, if so prompt the user
                     // And also exit out of the loop and refresh current grading state
                     if (answerIndex < 0) { 
                         showMessageForIncompleteQuiz();
                         resetData();
                         return;
                     }
 
                     // Get the answer from database data
                     char correctAnswer = answers.get(questionsAnswered).toLowerCase().charAt(0);
                     int correctAnswerIndex = ((int) correctAnswer) - ASCII_NUM;
 
                     // Check if the user got it correct
                     if (answerIndex == correctAnswerIndex) {
                         questionsCorrect++;
                     } 
                     questionsAnswered++;
                 }
                 displayQuizResults(); // Finished grading, show score
             }
         });
     }
     
     private void showMessageForIncompleteQuiz() {
         // Show a message that the user is required to select an answer before submitting
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
         alertDialogBuilder.setTitle(quiz.getTitle());
         alertDialogBuilder
             .setMessage(R.string.noAnswerChosen)
             .setCancelable(false)
             .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int id) {
                     // Do nothing, remove message
                 }
             });
         AlertDialog alertDialog = alertDialogBuilder.create();
         alertDialog.show();
     }
     
     private void displayQuizResults() {
         Intent i = new Intent(context, QuizzesResultsActivity.class);
         i.putExtra("questionsCorrect", questionsCorrect);
         i.putExtra("questionsAnswered", questionsAnswered);
         i.putExtra("quiz_title", quiz.getTitle());
         i.putExtra("quiz_id", quiz.getId());
         i.putExtra("course_id", course_id);
         resetData();
         startActivity(i);
     }
     
     private void resetData() {
         questionsAnswered = 0;
         questionsCorrect = 0;
         questionsCreated = 0;
     }
     
     private void initializeLists() {
         questions = new ArrayList<String>();
         answers = new ArrayList<String>();
         answersRadioGroup = new ArrayList<RadioGroup>();   
     }
 }
