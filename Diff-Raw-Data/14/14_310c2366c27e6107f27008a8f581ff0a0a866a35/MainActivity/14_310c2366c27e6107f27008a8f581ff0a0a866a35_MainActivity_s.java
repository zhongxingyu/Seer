 package lt.vumifps.undzenastest;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Collections;
 import java.util.LinkedList;
 
 public class MainActivity extends Activity implements View.OnClickListener {
 
     public static final String SHOULD_RANDOMIZE_KEY = "should_randomize";
     public static final String QUIZ_JSON_KEY = "quiz_json";
     private boolean shouldRandomize;
 
     private enum QuestionState {Unanswered, Correct, Wrong}
 
     public static final String JSON_RES_ID_KEY = "json_res_id";
     Quiz quiz, answeredWrong;
 
     LinearLayout answerLayout;
     ScrollView mainScrollView;
     ProgressBar progressBar;
     private TextView progressTextView, scoreTextView;
 
     int currentIndex;
     int numberOfQuestions;
     int correctCount = 0;
 
     QuestionState questionState = QuestionState.Unanswered;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_main);
 
         answeredWrong = new Quiz(getString(R.string.wrongly_answered));
 
         this.questionState = QuestionState.Unanswered;
 
         Intent intent = getIntent();
         int testNumber = intent.getIntExtra(MainActivity.JSON_RES_ID_KEY, -1);
         shouldRandomize = intent.getBooleanExtra(MainActivity.SHOULD_RANDOMIZE_KEY, false);
         String quizJsonString= intent.getStringExtra(MainActivity.QUIZ_JSON_KEY);
 
         try {
             JSONObject quizJsonObject = new JSONObject(quizJsonString);
             quiz = new Quiz(quizJsonObject, testNumber);
 
             currentIndex = 0;
             numberOfQuestions = quiz.getNumberOfquestions();
 
             progressBar = (ProgressBar) findViewById(R.id.progressBar);
             progressBar.setMax(numberOfQuestions);
             progressTextView = (TextView) findViewById(R.id.progressTextView);
             progressTextView.setText(getProgressText(numberOfQuestions, currentIndex+1));
 
             scoreTextView = (TextView) findViewById(R.id.scoreTextView);
             scoreTextView.setText(getcurrentScoreText(correctCount));
 
             answerLayout = (LinearLayout) this.findViewById(R.id.answersLinearLayout);
             mainScrollView = (ScrollView) this.findViewById(R.id.mainScrollView);
             Button homeButton = (Button) this.findViewById(R.id.homeButton);
             homeButton.setOnClickListener(this);
 
             Button nextButton = (Button) this.findViewById(R.id.nextButton);
             nextButton.setOnClickListener(this);
             if (shouldRandomize) {
                 quiz.shuffleQuestions();
             }
 
             showQuestion(quiz.getQuestion(0));
 
 
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
     private void showQuestion(Question question) {
 
         this.questionState = QuestionState.Unanswered;
 
         TextView questionsTextView = (TextView) this.findViewById(R.id.questionTextView);
         questionsTextView.setText(question.getQuestion());
 
         LinkedList<Answer> answers = question.getAnswers();
         Collections.shuffle(answers);

         answerLayout.removeAllViews();
        mainScrollView.fullScroll(View.FOCUS_UP);
 
         for (Answer answer : answers) {
             AnswerTextView answerTextView = getAnswerTextView(answer);
             answerLayout.addView(answerTextView);
         }
 
     }
 
     private AnswerTextView getAnswerTextView(Answer answer) {
 
         AnswerTextView answerTextView = new AnswerTextView(this);
         answerTextView.setAnswer(answer);
 
         LinearLayout.LayoutParams linearLayoutParams =
                 new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                         LinearLayout.LayoutParams.WRAP_CONTENT);
 
         linearLayoutParams.setMargins(0, 10, 0, 10);
         answerTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
 
         answerTextView.setLayoutParams(linearLayoutParams);
 
         answerTextView.setOnClickListener(this);
 
         return answerTextView;
     }
 
 
 
     @Override
     public void onClick(View view) {
 
         if (view instanceof AnswerTextView){
             AnswerTextView answerTextView = (AnswerTextView) view;
             if (this.questionState == QuestionState.Unanswered){
                 if (answerTextView.isCorrect()){
                     this.questionState = QuestionState.Correct;
                     correctCount++;
                     scoreTextView.setText(getcurrentScoreText(correctCount));
                 } else {
                     this.questionState = QuestionState.Wrong;
                     answeredWrong.addQuestion(answerTextView.getAnswer().getParent());
                 }
             }
         }
 
         switch (view.getId()){
             case R.id.nextButton:
 
                 if (this.questionState == QuestionState.Unanswered) {
                     answeredWrong.addQuestion(quiz.getQuestion(currentIndex));
                 }
 
                 this.showNextQuestion();
                 progressBar.incrementProgressBy(1);
 
                 break;
 
 
             case R.id.homeButton:
                 this.finish();
                 Intent homeIntent = new Intent(this, StartingActivity.class);
                 startActivity(homeIntent);
                 break;
         }
     }
 
     private void showNextQuestion() {
         currentIndex++;
 
         if (currentIndex+1 > quiz.getNumberOfquestions()) {
             if (shouldRandomize) {
                 quiz.shuffleQuestions();
             }
 
             currentIndex = 0;
             //Toast.makeText(this, "Fsio, rodau iš naujo random tvarka", Toast.LENGTH_LONG).show();
             progressBar.setProgress(0);
             Intent resultsIntent = new Intent(this, ResultsActivity.class);
             String results = correctCount + "/" + numberOfQuestions;
             resultsIntent.putExtra(ResultsActivity.RESULTS_KEY, results);
             resultsIntent.putExtra(ResultsActivity.WRONGLY_ANSWERED_QUESTIONS_JSON_KEY, this.answeredWrong.toJson().toString());
             resultsIntent.putExtra(ResultsActivity.WAS_RANDOMIZED_KEY, this.shouldRandomize);
             startActivity(resultsIntent);
             finish();
         } else {
             showQuestion(quiz.getQuestion(currentIndex));
 
             progressTextView.setText(getProgressText(numberOfQuestions, currentIndex+1));
         }
     }
 
     private String getProgressText(int total, int current) {
         return current + " / " + total;
     }
 
     private String getcurrentScoreText(int current) {
         return "[" + current + "]";
     }
 }
