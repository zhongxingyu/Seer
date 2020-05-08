 package lt.vumifps.undzenastest;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ExamConfigActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
 
     private static final int DEFAULT_NUM_QUESTIONS = 20;
     private static final String EXAM_QUESTIONS_COUNT_KEY = "exam_questions_count";
 
     private SharedPreferencesHelper sharedPreferencesHelper;
 
     private Quiz filteredQuiz;
     private CheckBox shouldRandomizeCheckBox;
     private EditText questionsCountEditText;
     private CheckBox showAllCheckBox;
     private String oldQuestionsCountValue;
     private CheckBox askWorstCheckBox;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.exam_config);
 
         sharedPreferencesHelper = new SharedPreferencesHelper(this);
 
         Intent intent = getIntent();
         String quizJsonString= intent.getStringExtra(MainActivity.QUIZ_JSON_KEY);
 
         try {
             filteredQuiz = new Quiz(new JSONObject(quizJsonString), -1);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         shouldRandomizeCheckBox = (CheckBox) findViewById(R.id.randomizeCheckBox);
         askWorstCheckBox = (CheckBox) findViewById(R.id.askWorstCheckBox);
         showAllCheckBox = (CheckBox) findViewById(R.id.showAllCheckBox);
         showAllCheckBox.setOnCheckedChangeListener(this);
         questionsCountEditText = (EditText) findViewById(R.id.questionsCountEditText);
         questionsCountEditText.setText(String.valueOf(DEFAULT_NUM_QUESTIONS));
 
         String savedQuestionsCount = sharedPreferencesHelper.loadString(EXAM_QUESTIONS_COUNT_KEY);
         if (savedQuestionsCount.length() > 0) {
             questionsCountEditText.setText(savedQuestionsCount);
         }
 
         Button startExamButton = (Button) findViewById(R.id.startButton);
         startExamButton.setOnClickListener(this);
 
     }
 
     @Override
     public void onClick(View view) {
 
         boolean shouldRandomize = shouldRandomizeCheckBox.isChecked();
         boolean shouldAskWorst = askWorstCheckBox.isChecked() && askWorstCheckBox.isEnabled();
 
         if (shouldAskWorst) {
             StatsManager statsManager = new StatsManager(this);
             filteredQuiz.moveWorstAnsweredQuestionsToBeginning(statsManager);
         }
 
         if (shouldRandomize && !shouldAskWorst) {
             filteredQuiz.shuffleQuestions();
         }
 
         if (!showAllCheckBox.isChecked()) {
             int questionsCount = DEFAULT_NUM_QUESTIONS;
 
             if (questionsCountEditText.getText() != null) {
                 String questionsCountString = questionsCountEditText.getText().toString();
 
                 sharedPreferencesHelper.saveString(EXAM_QUESTIONS_COUNT_KEY, questionsCountString);
 
                 try {
                     questionsCount = Integer.parseInt(questionsCountString);
                 } catch (NumberFormatException e) {
                     // We should do something here
                 }
             }
 
             if ((questionsCount < filteredQuiz.getQuestions().size()) && (questionsCount > 0)) {
                 filteredQuiz.truncateTo(questionsCount);
             }
         }
 
         if (shouldRandomize && shouldAskWorst) {
             filteredQuiz.shuffleQuestions();
         }
 
 
         Intent intent = new Intent(this, MainActivity.class);
 
         intent.putExtra(MainActivity.JSON_RES_ID_KEY, -1);
         intent.putExtra(MainActivity.QUIZ_JSON_KEY, filteredQuiz.toJson().toString());
         intent.putExtra(MainActivity.SHOULD_RANDOMIZE_KEY, shouldRandomize);
         startActivity(intent);
        this.finish();
     }
 
     @Override
     public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
 
         if (compoundButton == showAllCheckBox) {
             if (checked) {
                 if (questionsCountEditText.getText() != null) {
                     oldQuestionsCountValue = questionsCountEditText.getText().toString();
                 }
 
                 questionsCountEditText.setText(String.valueOf(filteredQuiz.getQuestions().size()));
                 questionsCountEditText.setEnabled(false);
 
                 askWorstCheckBox.setEnabled(false);
             } else {
                 questionsCountEditText.setEnabled(true);
                 questionsCountEditText.setText(oldQuestionsCountValue);
 
                 askWorstCheckBox.setEnabled(true);
             }
         }
     }
 }
