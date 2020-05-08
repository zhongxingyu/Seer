 package com.jeffthefate.dmbquiz_dev.fragment;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.jeffthefate.dmbquiz_dev.ApplicationEx;
 import com.jeffthefate.dmbquiz_dev.Constants;
 import com.jeffthefate.dmbquiz_dev.DatabaseHelper;
 import com.jeffthefate.dmbquiz_dev.R;
 import com.jeffthefate.dmbquiz_dev.activity.ActivityMain;
 import com.parse.CountCallback;
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 public class FragmentQuiz extends FragmentBase {
     
     private TextView scoreText;
     private TextView questionText;
     private EditText answerText;
     private TextView answerPlace;
     private Button answerButton;
     private TextView logoutButton;
     private TextView userText;
     private RelativeLayout skipButton;
     private TextView skipText;
     private TextView skipTime;
     private RelativeLayout hintButton;
     private TextView hintText;
     private TextView hintTime;
     private ImageView answerImage;
     private TextView retryText;
     private Button retryButton;
     private long skipWaitTick;
     private long skipTick = 17000;
     private long hintTick = 15000;
     
     private SkipTimer skipTimer;
     private HintTimer hintTimer;
     private WrongTimer wrongTimer;
     
     private HintTask hintTask;
     
     private String savedAnswer;
     private String savedHint;
     
     private boolean hintPressed = false;
     private boolean skipPressed = false;
     
     private boolean isCorrect = false;
     
     private String answerTextHint = null;
     
     public FragmentQuiz() {
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (!sharedPrefs.contains(getString(R.string.sound_key)))
             sharedPrefs.edit().putBoolean(getString(R.string.sound_key), true)
                     .commit();
         if (!sharedPrefs.contains(getString(R.string.notification_key)))
             sharedPrefs.edit().putBoolean(getString(R.string.notification_key),
                     true).commit();
         setHasOptionsMenu(true);
         if (savedInstanceState != null) {
             savedAnswer = savedInstanceState.getString("answer");
             ApplicationEx.dbHelper.setUserValue(savedAnswer,
                     DatabaseHelper.COL_ANSWER, mCallback.getUserId());
             savedHint = savedInstanceState.getString("hint");
             ApplicationEx.dbHelper.setUserValue(savedHint,
                     DatabaseHelper.COL_HINT, mCallback.getUserId());
             skipTick = savedInstanceState.getLong("skipTick");
             ApplicationEx.dbHelper.setUserValue((int) skipTick,
                     DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
             hintTick = savedInstanceState.getLong("hintTick");
             ApplicationEx.dbHelper.setUserValue((int) hintTick,
                     DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
             hintPressed = savedInstanceState.getBoolean("hintPressed");
             ApplicationEx.dbHelper.setUserValue(hintPressed ? 1 : 0,
                     DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
             skipPressed = savedInstanceState.getBoolean("skipPressed");
             ApplicationEx.dbHelper.setUserValue(skipPressed ? 1 : 0,
                     DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
             isCorrect = savedInstanceState.getBoolean("isCorrect");
             ApplicationEx.dbHelper.setUserValue(isCorrect ? 1 : 0,
                     DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
         }
         else {
             if (mCallback.getUserId() != null) {
                 savedAnswer = ApplicationEx.dbHelper.getUserStringValue(
                         DatabaseHelper.COL_ANSWER, mCallback.getUserId());
                 if (savedAnswer != null && savedAnswer.equals(""))
                     savedAnswer = null;
                 savedHint = ApplicationEx.dbHelper.getUserStringValue(
                         DatabaseHelper.COL_HINT, mCallback.getUserId());
                 skipTick = ApplicationEx.dbHelper.getUserIntValue(
                         DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
                 hintTick = ApplicationEx.dbHelper.getUserIntValue(
                         DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                 hintPressed = ApplicationEx.dbHelper.getUserIntValue(
                         DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId())
                             == 1 ? true : false;
                 skipPressed = ApplicationEx.dbHelper.getUserIntValue(
                         DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId())
                             == 1 ? true : false;
                 isCorrect = ApplicationEx.dbHelper.getUserIntValue(
                         DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId())
                             == 1 ? true : false;
             }
         }
         if (hintTick < 0)
             hintTick = 15000;
         if (skipTick < 0)
             skipTick = 17000;
         ApplicationEx.dbHelper.setUserValue((int) hintTick,
                 DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
         ApplicationEx.dbHelper.setUserValue((int) skipTick,
                 DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
     }
     
     private class WrongTimer extends CountDownTimer {
         public WrongTimer(long millisInFuture, long countDownInterval) {
             super(millisInFuture, countDownInterval);
         }
         
         @Override
         public void onTick(long millisUntilFinished) {}
         
         @Override
         public void onFinish() {
             answerImage.setVisibility(View.INVISIBLE);
             questionText.setTextColor(Color.WHITE);
         }
     }
     
     private class SkipTimer extends CountDownTimer {
         public SkipTimer(long millisInFuture, long countDownInterval) {
             super(millisInFuture, countDownInterval);
             skipTime.setText("");
             skipText.setTextColor(Color.BLACK);
             skipText.setBackgroundResource(R.drawable.button);
             skipText.setVisibility(View.INVISIBLE);
             skipTime.setVisibility(View.VISIBLE);
         }
 
         @Override
         public void onTick(long millisUntilFinished) {
             skipTick = millisUntilFinished;
             ApplicationEx.dbHelper.setUserValue((int) skipTick,
                     DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
             skipTime.setText(Long.toString((millisUntilFinished/1000)-1));
             skipText.setVisibility(View.INVISIBLE);
             skipTime.setVisibility(View.VISIBLE);
             if (millisUntilFinished < 2000) {
                 skipText.setVisibility(View.VISIBLE);
                 skipText.setTextColor(Color.BLACK);
                 skipText.setBackgroundResource(R.drawable.button);
                 skipTime.setVisibility(View.INVISIBLE);
                 skipButton.setEnabled(true);
             }
         }
         
         @Override
         public void onFinish() {
             skipTick = 0;
             ApplicationEx.dbHelper.setUserValue((int) skipTick,
                     DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
         }
     }
     
     private class HintTimer extends CountDownTimer {
         public HintTimer(long millisInFuture, long countDownInterval) {
             super(millisInFuture, countDownInterval);
             long text = (millisInFuture/1000)+1;
             if (text > 15)
                 text = 15;
             hintTime.setText(Long.toString(text));
             hintText.setVisibility(View.INVISIBLE);
             hintTime.setVisibility(View.VISIBLE);
             skipTime.setText("");
             skipText.setTextColor(Color.BLACK);
             skipText.setBackgroundResource(R.drawable.button);
             skipText.setVisibility(View.INVISIBLE);
             skipTime.setVisibility(View.VISIBLE);
         }
 
         @Override
         public void onTick(long millisUntilFinished) {
             hintTick = millisUntilFinished;
             ApplicationEx.dbHelper.setUserValue((int) hintTick,
                     DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
             hintTime.setText(Long.toString((millisUntilFinished/1000)+1));
             hintText.setVisibility(View.INVISIBLE);
             hintTime.setVisibility(View.VISIBLE);
         }
         
         @Override
         public void onFinish() {
             if (!isCorrect) {
                 hintButton.setEnabled(true);
                 if (skipTimer != null)
                     skipTimer.cancel();
                 skipTimer = new SkipTimer(skipTick, 500);
                 skipTimer.start();
                 skipText.setVisibility(View.INVISIBLE);
                 skipTime.setVisibility(View.VISIBLE);
             }
             hintText.setVisibility(View.VISIBLE);
             hintText.setTextColor(Color.BLACK);
             hintText.setBackgroundResource(R.drawable.button);
             hintTime.setVisibility(View.INVISIBLE);
             hintTick = 0;
             ApplicationEx.dbHelper.setUserValue((int) hintTick,
                     DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
         }
     }
     
     private class NextTask extends AsyncTask<Void, Void, Void> {        
         @Override
         protected Void doInBackground(Void... nothing) {
             publishProgress();
             savedAnswer = null;
             ApplicationEx.dbHelper.setUserValue("",
                     DatabaseHelper.COL_ANSWER, mCallback.getUserId());
             skipTick = 17000;
             ApplicationEx.dbHelper.setUserValue((int) skipTick,
                     DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
             hintTick = 15000;
             ApplicationEx.dbHelper.setUserValue((int) hintTick,
                     DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
             hintPressed = false;
             ApplicationEx.dbHelper.setUserValue(hintPressed ? 1 : 0,
                     DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
             skipPressed = false;
             ApplicationEx.dbHelper.setUserValue(skipPressed ? 1 : 0,
                     DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
             savedHint = "";
             ApplicationEx.dbHelper.setUserValue("",
                     DatabaseHelper.COL_HINT, mCallback.getUserId());
             isCorrect = false;
             ApplicationEx.dbHelper.setUserValue(isCorrect ? 1 : 0,
                     DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             answerButton.setBackgroundResource(R.drawable.button_disabled);
             answerButton.setTextColor(res.getColor(R.color.light_gray));
             answerButton.setText("LOADING");
             answerButton.setEnabled(false);
             answerText.setText("");
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             mCallback.next();
         }
     }
     
     @Override
     public void disableButton(boolean isRetry) {
         if (!isRetry) {
             answerButton.setBackgroundResource(R.drawable.button_disabled);
             answerButton.setTextColor(res.getColor(R.color.light_gray));
             answerButton.setEnabled(false);
         }
         else {
             retryButton.setBackgroundResource(R.drawable.button_disabled);
             retryButton.setTextColor(res.getColor(R.color.light_gray));
             retryButton.setEnabled(false);
         }
     }
     
     @Override
     public void enableButton(boolean isRetry) {
         if (!isRetry) {
             answerButton.setBackgroundResource(R.drawable.button);
             answerButton.setTextColor(Color.BLACK);
             answerButton.setEnabled(true);
         }
         else {
             retryButton.setBackgroundResource(R.drawable.button);
             retryButton.setTextColor(Color.BLACK);
             retryButton.setEnabled(true);
         }
     }
     
     private void indicateHint() {
         hintTask = new HintTask();
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
             hintTask.execute();
         else
             hintTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class HintTask extends AsyncTask<Void, Void, Void> {
         
         @Override
         protected Void doInBackground(Void... nothing) {
             StringBuilder sb = new StringBuilder(mCallback.getCorrectAnswer()
                     .replaceAll("\\s+", " "));
             int textSize = mCallback.getCorrectAnswer().replaceAll("\\s+", " ")
                     .length();
             int replaceSize = textSize / 2;
             int currIndex = -1;
             int replacements = 0;
             while (replacements < replaceSize) {
                 currIndex = (int) (Math.random()*textSize);
                 if (sb.substring(currIndex, currIndex+1)
                         .matches("[0-9a-zA-Z]") && 
                     !sb.substring(currIndex, currIndex+1).matches("[*]")) {
                     sb.replace(currIndex, currIndex+1, "*");
                     replacements++;
                 }
             }
             ApplicationEx.dbHelper.setQuestionHint(mCallback.getQuestionId(),
                     true, mCallback.getUserId(),
                     ApplicationEx.dbHelper.getQuestionSkip(
                             mCallback.getQuestionId(), mCallback.getUserId()));
             hintPressed = true;
             ApplicationEx.dbHelper.setUserValue(hintPressed ? 1 : 0,
                     DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
             if (mCallback.getQuestionScore() != null)
                 answerTextHint = Integer.toString(((int)((int)(
                         Integer.parseInt(
                                 mCallback.getQuestionScore())*0.99))/2)) +
                                 " points";
             else
                 answerTextHint = "";
             savedHint = sb.toString();
             ApplicationEx.dbHelper.setUserValue(savedHint,
                     DatabaseHelper.COL_HINT, mCallback.getUserId());
             publishProgress();
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             answerPlace.setText(savedHint, TextView.BufferType.NORMAL);
             answerPlace.setVisibility(View.VISIBLE);
             hintButton.setEnabled(false);
             answerText.setHint(answerTextHint);
             hintText.setTextColor(res.getColor(R.color.light_gray));
             hintText.setBackgroundResource(R.drawable.button_disabled);
         }
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.question, container, false);
         scoreText = (TextView) v.findViewById(R.id.ScoreText);
         scoreText.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mCallback != null)
                     mCallback.onStatsPressed();
             }
         });
         questionText = (TextView) v.findViewById(R.id.QuestionText);
         questionText.setMovementMethod(new ScrollingMovementMethod());
         answerText = (EditText) v.findViewById(R.id.QuestionAnswer);
         answerText.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event) {
                 if (mCallback != null && !mCallback.getNetworkProblem() &&
                         !mCallback.isNewQuestion() &&
                         (actionId == EditorInfo.IME_ACTION_DONE ||
                         event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                     String entry = null;
                     entry = v.getEditableText().toString();
                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                         new VerifyTask().execute(entry);
                     else
                         new VerifyTask().executeOnExecutor(
                                 AsyncTask.THREAD_POOL_EXECUTOR, entry);
                     InputMethodManager imm = 
                         (InputMethodManager) getActivity().getSystemService(
                                 Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                     return true;
                 }
                 else
                     return false;
             } 
         });
         answerPlace = (TextView) v.findViewById(R.id.AnswerText);
         answerButton = (Button) v.findViewById(R.id.QuestionButton);
         answerButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mCallback.isNewQuestion()) {
                     if (Build.VERSION.SDK_INT <
                             Build.VERSION_CODES.HONEYCOMB)
                         new NextTask().execute();
                     else
                         new NextTask().executeOnExecutor(
                                 AsyncTask.THREAD_POOL_EXECUTOR);
                 }
                 else {
                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                         new VerifyTask().execute(
                                 answerText.getEditableText().toString());
                     else
                         new VerifyTask().executeOnExecutor(
                                 AsyncTask.THREAD_POOL_EXECUTOR,
                                     answerText.getEditableText().toString());
                 }
             }
         });
         skipButton = (RelativeLayout) v.findViewById(R.id.Skip);
         skipButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 skipButton.setEnabled(false);
                 if (ApplicationEx.dbHelper.hasAnswer(mCallback.getQuestionId(),
                         mCallback.getUserId()))
                     ApplicationEx.dbHelper.deleteAnswer(
                             mCallback.getQuestionId(), mCallback.getUserId());
                 ApplicationEx.dbHelper.setQuestionSkip(
                         mCallback.getQuestionId(),
                         ApplicationEx.dbHelper.getQuestionHint(
                                 mCallback.getQuestionId(),
                                     mCallback.getUserId()),
                                     mCallback.getUserId(), true);
                 playAudio("skip");
                 savedHint = mCallback.getCorrectAnswer();
                 ApplicationEx.dbHelper.setUserValue(savedHint,
                         DatabaseHelper.COL_HINT, mCallback.getUserId());
                 answerPlace.setText(savedHint);
                 hintButton.setEnabled(false);
                 hintText.setTextColor(res.getColor(R.color.light_gray));
                 hintText.setBackgroundResource(R.drawable.button_disabled);
                 mCallback.setIsNewQuestion(true);
                 answerButton.setBackgroundResource(R.drawable.button);
                 answerButton.setTextColor(Color.BLACK);
                 answerButton.setText("NEXT");
                 answerButton.setEnabled(true);
                 skipText.setTextColor(res.getColor(R.color.light_gray));
                 skipText.setBackgroundResource(R.drawable.button_disabled);
                 skipPressed = true;
                 ApplicationEx.dbHelper.setUserValue(skipPressed ? 1 : 0,
                         DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
                 if (hintTimer != null)
                     hintTimer.cancel();
                 if (skipTimer != null)
                     skipTimer.cancel();
                 hintTick = 0;
                 ApplicationEx.dbHelper.setUserValue((int) hintTick,
                         DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                 skipTick = 0;
                 ApplicationEx.dbHelper.setUserValue((int) skipTick,
                         DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
                 saveQuestionScore(true);
             } 
         });
         skipText = (TextView) v.findViewById(R.id.SkipText);
         skipTime = (TextView) v.findViewById(R.id.SkipTime);
         if (skipTick > 0)
             skipTime.setText(Long.toString((skipTick/1000)+1));
         hintButton = (RelativeLayout) v.findViewById(R.id.Hint);
         hintButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 hintTick = 0;
                 ApplicationEx.dbHelper.setUserValue((int) hintTick,
                         DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                 playAudio("hint");
                 indicateHint();
             } 
         });
         hintText = (TextView) v.findViewById(R.id.HintText);
         hintTime = (TextView) v.findViewById(R.id.HintTime);
         if (hintTick > 0)
             hintTime.setText(Long.toString((hintTick/1000)+1));
         answerImage = (ImageView) v.findViewById(R.id.AnswerImage);
         answerImage.bringToFront();
         retryText = (TextView) v.findViewById(R.id.RetryText);
         retryButton = (Button) v.findViewById(R.id.RetryButton);
         retryButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 disableButton(true);
                 if (mCallback != null) {
                     if (ApplicationEx.getConnection()) {
                         mCallback.setNetworkProblem(false);
                         if (mCallback.getAnswerIds() != null) {
                             if (mCallback.getQuestionId() != null)
                                 resumeQuestion();
                             else
                                 mCallback.nextQuestion();
                         }
                     }
                     else {
                         Toast.makeText(ApplicationEx.getApp(), "No connection",
                                 Toast.LENGTH_LONG).show();
                         showNetworkProblem();
                     }
                 }
             }
         });
         return v;
     }
     
     @Override
     public void resetHint() {
         savedHint = "";
         ApplicationEx.dbHelper.setUserValue("",
                 DatabaseHelper.COL_HINT, mCallback.getUserId());
     }
     
     @Override
     public void resumeQuestion() {
         if (mCallback != null && mCallback.hasAnswerId(
                mCallback.getQuestionId()) && !mCallback.isNewQuestion()) {
             mCallback.nextQuestion();
             return;
         }
         Log.d(Constants.LOG_TAG, mCallback.getCorrectAnswer());
         answerText.setVisibility(View.VISIBLE);
         if (mCallback.getQuestion() != null) {
             questionText.setText(mCallback.getQuestion());
             questionText.setVisibility(View.VISIBLE);
         }
         if (savedAnswer != null)
             answerText.setText(savedAnswer);
         int tempScore = (int)(Integer.parseInt(mCallback.getQuestionScore())
                 *0.99);
         if (tempScore < 100)
             tempScore = 100;
         if (!hintPressed)
             answerText.setHint(Integer.toString(tempScore) + " points");
         else
             answerText.setHint(Integer.toString(tempScore/2) + " points");
         if (!isCorrect) {
             if (!hintPressed)
                 savedHint = "";
         }
         if (savedHint == null || savedHint.equals("")) {
             ArrayList<String> answerStrings = new ArrayList();
             int lastSpace = -1;
             String answer = (mCallback.getCorrectAnswer()
                     .replaceAll("\\s+", " ")).replaceAll("[0-9a-zA-Z#&]", "*");
             if (answer.length() > 36 && answer.lastIndexOf(" ") > -1) {
                 lastSpace = answer.lastIndexOf(" ");
                 if (lastSpace > 36)
                     lastSpace = answer.substring(0, lastSpace-1)
                             .lastIndexOf(" ");
                 answerStrings.add(answer.substring(0, lastSpace-1));
                 answerStrings.add(answer.substring(lastSpace+2,
                         answer.length()));
                 answer = answerStrings.get(0) + "\n" + answerStrings.get(1);
             }
             savedHint = answer;
             ApplicationEx.dbHelper.setUserValue(savedHint,
                     DatabaseHelper.COL_HINT, mCallback.getUserId());
         }
         answerPlace.setText(savedHint);
         answerPlace.setVisibility(View.VISIBLE);
         answerButton.setVisibility(View.VISIBLE);
         answerButton.setBackgroundResource(R.drawable.button);
         answerButton.setTextColor(Color.BLACK);
         retryText.setVisibility(View.INVISIBLE);
         retryButton.setVisibility(View.INVISIBLE);
         if (mCallback.isNewQuestion()) {
             answerButton.setText("NEXT");
             answerButton.setEnabled(true);
             if (ApplicationEx.dbHelper.hasCorrectAnswer(
                     mCallback.getQuestionId(), mCallback.getUserId())) {
                 answerImage.setImageResource(R.drawable.correct);
                 questionText.setTextColor(Color.GREEN);
                 answerImage.setVisibility(View.VISIBLE);
             }
             else {
                 questionText.setTextColor(Color.WHITE);
                 answerImage.setVisibility(View.INVISIBLE);
             }
             hintText.setTextColor(res.getColor(R.color.light_gray));
             hintText.setBackgroundResource(R.drawable.button_disabled);
             skipText.setTextColor(res.getColor(R.color.light_gray));
             skipText.setBackgroundResource(R.drawable.button_disabled);
             hintTick = 0;
             ApplicationEx.dbHelper.setUserValue((int) hintTick,
                     DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
             skipTick = 0;
             ApplicationEx.dbHelper.setUserValue((int) skipTick,
                     DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
             skipPressed = true;
             ApplicationEx.dbHelper.setUserValue(skipPressed ? 1 : 0,
                     DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
             hintPressed = true;
             ApplicationEx.dbHelper.setUserValue(hintPressed ? 1 : 0,
                     DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
         }
         else {
             answerButton.setText("ENTER");
             answerButton.setEnabled(true);
             questionText.setTextColor(Color.WHITE);
             answerImage.setVisibility(View.INVISIBLE);
         }
         hintButton.setEnabled(false);
         hintButton.setVisibility(View.VISIBLE);
         if (hintPressed) {
             hintText.setTextColor(res.getColor(R.color.light_gray));
             hintText.setBackgroundResource(R.drawable.button_disabled);
             hintText.setVisibility(View.VISIBLE);
             hintTime.setVisibility(View.INVISIBLE);
         }
         else {
             if (hintTick > 0) {
                 if (hintTimer != null)
                     hintTimer.cancel();
                 hintTimer = new HintTimer(hintTick, 500);
                 hintTimer.start();
             }
             else {
                 hintText.setBackgroundResource(R.drawable.button);
                 hintText.setTextColor(Color.BLACK);
                 hintText.setVisibility(View.VISIBLE);
                 hintTime.setVisibility(View.INVISIBLE);
                 hintButton.setEnabled(true);
             }
         }
         if (skipTimer != null)
             skipTimer.cancel();
         skipButton.setEnabled(false);
         skipButton.setVisibility(View.VISIBLE);
         if (skipPressed) {
             skipText.setTextColor(res.getColor(R.color.light_gray));
             skipText.setBackgroundResource(R.drawable.button_disabled);
             skipText.setVisibility(View.VISIBLE);
             skipTime.setVisibility(View.INVISIBLE);
         }
         else {
             if (skipTick > 0) {
                 if (hintTick == 0) {
                     skipTimer = new SkipTimer(skipTick, 500);
                     skipTimer.start();
                 }
             }
             else {
                 skipText.setBackgroundResource(R.drawable.button);
                 skipText.setTextColor(Color.BLACK);
                 skipText.setVisibility(View.VISIBLE);
                 skipTime.setVisibility(View.INVISIBLE);
                 skipButton.setEnabled(true);
             }
         }
         updateTimerButtons();
         updateScoreText();
     }
     
     @Override
     public void onResume() {
         super.onResume();
         loggingOut = false;
         if (mCallback != null) {
             if (mCallback.getNetworkProblem())
                 showNetworkProblem();
             else {
                 if (ApplicationEx.getConnection()) {
                     mCallback.saveUserScore(mCallback.getCurrentScore());
                     if (mCallback.getAnswerIds() != null) {
                         if (mCallback.getQuestionsLeft() <= 0 &&
                                 mCallback.getAnswerIds().size() > 0)
                             showNoMoreQuestions();
                         else {
                             if (mCallback.getQuestionId() != null)
                                 resumeQuestion();
                             else
                                 mCallback.nextQuestion();
                         }
                     }
                 }
                 else {
                     Toast.makeText(ApplicationEx.getApp(), "No connection",
                             Toast.LENGTH_LONG).show();
                     showNetworkProblem();
                 }
             }
         }
     }
     
     @Override
     public void onPause() {
         if (skipTimer != null)
             skipTimer.cancel();
         if (hintTimer != null)
             hintTimer.cancel();
         if (hintTask != null)
             hintTask.cancel(true);
         savedAnswer = answerText.getText() == null ? "" :
                 answerText.getText().toString();
         ApplicationEx.dbHelper.setUserValue(savedAnswer,
                 DatabaseHelper.COL_ANSWER, mCallback.getUserId());
         savedHint = answerPlace.getText() == null ? "" :
                 answerPlace.getText().toString();
         ApplicationEx.dbHelper.setUserValue(savedHint,
                 DatabaseHelper.COL_HINT, mCallback.getUserId());
         super.onPause();
     }
     
     private class VerifyTask extends AsyncTask<String, Void, Void> {
         private void saveAnswer(final String userId,
                 final String questionId) {
             ParseQuery query = new ParseQuery("CorrectAnswers");
             query.whereEqualTo("userId", userId);
             query.whereEqualTo("questionId", questionId);
             query.getFirstInBackground(new GetCallback() {
                 @Override
                 public void done(ParseObject answer, ParseException e) {
                     if (answer == null) {
                         ParseObject correctAnswer =
                                 new ParseObject("CorrectAnswers");
                         correctAnswer.put("questionId", questionId);
                         correctAnswer.put("userId", userId);
                         correctAnswer.put("hint",
                                 ApplicationEx.dbHelper.getQuestionHint(
                                         questionId, userId));
                         try {
                             correctAnswer.saveEventually();
                         } catch (RuntimeException exception) {}
                     }
                     if (e != null && e.getCode() != 101) {
                         Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                         showNetworkProblem();
                     }
                 }
             });
         }
         
         @Override
         protected Void doInBackground(String... entry) {
             String trimmed = entry[0].trim();
             if (trimmed.equalsIgnoreCase(mCallback.getCorrectAnswer())) {
                 isCorrect = true;
                 playAudio("correct");
                 publishProgress();
                 if (hintTimer != null)
                     hintTimer.cancel();
                 if (skipTimer != null)
                     skipTimer.cancel();
                 hintTick = 0;
                 ApplicationEx.dbHelper.setUserValue((int) hintTick,
                         DatabaseHelper.COL_HINT_TICK, mCallback.getUserId());
                 skipTick = 0;
                 ApplicationEx.dbHelper.setUserValue((int) skipTick,
                         DatabaseHelper.COL_SKIP_TICK, mCallback.getUserId());
                 mCallback.setIsNewQuestion(true);
                 hintPressed = true;
                 ApplicationEx.dbHelper.setUserValue(hintPressed ? 1 : 0,
                         DatabaseHelper.COL_HINT_PRESSED, mCallback.getUserId());
                 skipPressed = true;
                 ApplicationEx.dbHelper.setUserValue(skipPressed ? 1 : 0,
                         DatabaseHelper.COL_SKIP_PRESSED, mCallback.getUserId());
                 ApplicationEx.dbHelper.setUserValue(isCorrect ? 1 : 0,
                         DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
                 if (wrongTimer != null)
                     wrongTimer.cancel();
                 String qId = mCallback.getQuestionId();
                 if (mCallback.getAnswerIds() != null &&
                         !mCallback.getAnswerIds().contains(qId)) {
                     ApplicationEx.dbHelper.markAnswerCorrect(qId,
                             mCallback.getUserId(), true,
                             ApplicationEx.dbHelper.getQuestionHint(qId,
                                     mCallback.getUserId()));
                     mCallback.addAnswerId(qId);
                     int tempScore = -1;
                     try {
                         tempScore = (int)(Integer.parseInt(
                                 mCallback.getQuestionScore())*0.99);
                     } catch (NumberFormatException e) {
                         Log.e(Constants.LOG_TAG, "Bad question score: " +
                                 mCallback.getQuestionScore() + " : " +
                                 mCallback.getQuestionId());
                     }
                     if (tempScore > -1) {
                         if (tempScore < 100)
                             tempScore = 100;
                         if (ApplicationEx.dbHelper.getQuestionHint(qId,
                                 mCallback.getUserId()))
                             tempScore = tempScore/2;
                         mCallback.addCurrentScore(tempScore);
                     }
                     mCallback.saveUserScore(mCallback.getCurrentScore());
                     saveAnswer(mCallback.getUserId(), qId);
                 }
             }
             else {
                 isCorrect = false;
                 playAudio("wrong");
                 publishProgress();
                 ApplicationEx.dbHelper.setUserValue(isCorrect ? 1 : 0,
                         DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
             }
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             if (isCorrect) {
                 savedHint = mCallback.getCorrectAnswer();
                 ApplicationEx.dbHelper.setUserValue(savedHint,
                         DatabaseHelper.COL_HINT, mCallback.getUserId());
                 answerPlace.setText(savedHint);
                 hintText.setVisibility(View.VISIBLE);
                 hintTime.setVisibility(View.INVISIBLE);
                 skipText.setVisibility(View.VISIBLE);
                 skipTime.setVisibility(View.INVISIBLE);
                 hintButton.setEnabled(false);
                 skipButton.setEnabled(false);
                 answerImage.setImageResource(R.drawable.correct);
                 answerImage.setVisibility(View.VISIBLE);
                 questionText.setTextColor(Color.GREEN);
                 answerText.setText("");
                 answerButton.setText("NEXT");
                 answerButton.setEnabled(true);
                 hintText.setTextColor(res.getColor(R.color.light_gray));
                 hintText.setBackgroundResource(R.drawable.button_disabled);
                 skipText.setTextColor(res.getColor(R.color.light_gray));
                 skipText.setBackgroundResource(R.drawable.button_disabled);
             }
             else {
                 answerImage.setImageResource(R.drawable.wrong);
                 answerImage.setVisibility(View.VISIBLE);
                 questionText.setTextColor(Color.RED);
                 if (wrongTimer == null)
                     wrongTimer = new WrongTimer(2000, 1000);
                 wrongTimer.start();
             }
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             if (isCorrect) {
                 scoreText.setText(
                         Integer.toString(mCallback.getCurrentScore()));
                 if (mCallback.getDisplayName() != null)
                     scoreText.setVisibility(View.VISIBLE);
                 saveQuestionScore(false);
             }
             isCorrect = false;
             ApplicationEx.dbHelper.setUserValue(isCorrect ? 1 : 0,
                     DatabaseHelper.COL_IS_CORRECT, mCallback.getUserId());
         }
     }
 
     private void saveQuestionScore(final boolean isSkip) {
         ParseQuery query = new ParseQuery("Question");
         query.getInBackground(mCallback.getQuestionId(), new GetCallback() {
             @Override
             public void done(ParseObject question, ParseException e) {
                 if (e == null) {
                     String currScore;
                     Number number = question.getNumber("score");
                     if (number != null) {
                         int score = number.intValue();
                         if (isSkip) {
                             currScore = Integer.toString((int)(score/0.99));
                             if (Integer.parseInt(currScore) > 1000)
                                 currScore = "1000";
                         }
                         else {
                             currScore = Integer.toString((int)(score*0.99));
                             if (Integer.parseInt(currScore) < 100)
                                 currScore = "100";
                         }
                     }
                     else
                         currScore = "1000";
                     question.put("score", Integer.parseInt(currScore));
                     try {
                         question.saveEventually();
                     } catch (RuntimeException err) {}
                 }
                 else {
                     Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                     showNetworkProblem();
                 }
             }
         });
     }
     
     private boolean loggingOut = false;
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString("answer", savedAnswer);
         outState.putString("hint", savedHint);
         outState.putLong("skipTick", skipTick);
         outState.putLong("hintTick", hintTick);
         outState.putBoolean("hintPressed", hintPressed);
         outState.putBoolean("skipPressed", skipPressed);
         outState.putBoolean("isCorrect", isCorrect);
         if (!loggingOut)
             super.onSaveInstanceState(outState);
         else
             super.onSaveInstanceState(null);
     }
     
     @Override
     public void showNoMoreQuestions() {
         answerImage.setVisibility(View.INVISIBLE);
         questionText.setVisibility(View.VISIBLE);
         questionText.setTextColor(Color.WHITE);
         questionText.setText("Congratulations!\nYou've answered them all!");
         answerText.setVisibility(View.INVISIBLE);
         answerPlace.setVisibility(View.INVISIBLE);
         answerButton.setVisibility(View.INVISIBLE);
         if (hintTimer != null)
             hintTimer.cancel();
         if (skipTimer != null)
             skipTimer.cancel();
         hintButton.setVisibility(View.INVISIBLE);
         skipButton.setVisibility(View.INVISIBLE);
         scoreText.setVisibility(View.VISIBLE);
         updateScoreText();
     }
     
     @Override
     public void showNetworkProblem() {
         enableButton(true);
         if (mCallback != null)
             mCallback.setNetworkProblem(true);
         answerImage.setVisibility(View.INVISIBLE);
         questionText.setVisibility(View.INVISIBLE);
         retryText.setVisibility(View.VISIBLE);
         answerText.setVisibility(View.INVISIBLE);
         answerPlace.setVisibility(View.INVISIBLE);
         answerButton.setVisibility(View.INVISIBLE);
         if (hintTimer != null)
             hintTimer.cancel();
         if (skipTimer != null)
             skipTimer.cancel();
         hintButton.setVisibility(View.INVISIBLE);
         skipButton.setVisibility(View.INVISIBLE);
         retryButton.setVisibility(View.VISIBLE);
         scoreText.setVisibility(View.INVISIBLE);
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.menu_quiz, menu);
     }
     
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         if (mCallback.getUserId() != null) {
             if (ApplicationEx.dbHelper.hasUser(mCallback.getUserId()) &&
                     !ApplicationEx.dbHelper.isAnonUser(mCallback.getUserId())) {
                 if (mCallback.getDisplayName() != null)
                     menu.findItem(R.id.LogoutMenu).setTitle("Logout (" +
                             mCallback.getDisplayName() + ")");
                 else
                     menu.findItem(R.id.LogoutMenu).setTitle("Logout");
                 menu.findItem(R.id.LeadersMenu).setVisible(true)
                         .setEnabled(true);
                 menu.findItem(R.id.NameMenu).setVisible(true)
                         .setEnabled(true);
             }
             else {
                 menu.findItem(R.id.LogoutMenu).setTitle("Logout");
                 menu.findItem(R.id.LeadersMenu).setVisible(false)
                         .setEnabled(false);
                 menu.findItem(R.id.NameMenu).setVisible(false)
                         .setEnabled(false);
             }
         }
         else {
             menu.findItem(R.id.LogoutMenu).setTitle("Logout");
             menu.findItem(R.id.LeadersMenu).setVisible(true).setEnabled(true);
             menu.findItem(R.id.NameMenu).setVisible(true).setEnabled(true);
         }
         menu.findItem(R.id.SoundMenu).setChecked(sharedPrefs.getBoolean(
                 getString(R.string.sound_key), true));
         menu.findItem(R.id.Notifications).setChecked(sharedPrefs.getBoolean(
                 getString(R.string.notification_key), true));
         super.onPrepareOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         case R.id.SwitchBackground:
             if (mCallback != null) {
                 String newBackground = mCallback.setBackground(
                         mCallback.getBackground(), true);
                 ApplicationEx.dbHelper.setCurrBackground(mCallback.getUserId(),
                         newBackground);
             }
             break;
         case R.id.LeadersMenu:
             if (mCallback != null)
                 mCallback.onStatsPressed();
             break;
         case R.id.SoundMenu:
             item.setChecked(!item.isChecked());
             sharedPrefs.edit().putBoolean(getString(R.string.sound_key),
                     !sharedPrefs.getBoolean(getString(R.string.sound_key),
                             true))
                 .commit();
             break;
         case R.id.Notifications:
             item.setChecked(!item.isChecked());
             sharedPrefs.edit().putBoolean(getString(R.string.notification_key),
                     !sharedPrefs.getBoolean(
                             getString(R.string.notification_key), true))
                 .commit();
             break;
         case R.id.ReportMenu:
             ApplicationEx.reportQuestion(mCallback.getQuestionId(),
                     mCallback.getQuestion(), mCallback.getCorrectAnswer(),
                     mCallback.getQuestionScore());
             break;
         case R.id.NameMenu:
             if (mCallback != null)
                 mCallback.showNameDialog();
             break;
         case R.id.LogoutMenu:
             if (mCallback != null) {
                 ApplicationEx.dbHelper.setOffset(0, mCallback.getUserId());
                 loggingOut = true;
                 mCallback.setQuestionId(null);
                 mCallback.setQuestion(null);
                 mCallback.setCorrectAnswer(null);
                 mCallback.setQuestionCategory(null);
                 mCallback.setQuestionScore(null);
                 mCallback.setNextQuestionId(null);
                 mCallback.setNextQuestion(null);
                 mCallback.setNextCorrectAnswer(null);
                 mCallback.setNextQuestionCategory(null);
                 mCallback.setNextQuestionScore(null);
                 mCallback.setThirdQuestionId(null);
                 mCallback.setThirdQuestion(null);
                 mCallback.setThirdCorrectAnswer(null);
                 mCallback.setThirdQuestionCategory(null);
                 mCallback.setThirdQuestionScore(null);
                 mCallback.logOut(true);
             }
             break;
         case R.id.ScreenMenu:
             if (mCallback != null)
                 mCallback.shareScreenshot();
             break;
         case R.id.ExitMenu:
             getActivity().moveTaskToBack(true);
             break;
         case R.id.FollowMenu:
             startActivity(getOpenTwitterIntent());
             break;
         case R.id.LikeMenu:
             startActivity(getOpenFacebookIntent());
             break;
         default:
             super.onOptionsItemSelected(item);
             break;
         }
         return true;
     }
     
     @Override
     public void updateTimerButtons() {
         if (mCallback != null && mCallback.getQuestionsLeft() < 2 &&
                 mCallback.getQuestionsLeft() > -1) {
             skipButton.setVisibility(View.INVISIBLE);
             if (mCallback.getQuestionsLeft() > 0)
                 hintButton.setVisibility(View.VISIBLE);
             else
                 hintButton.setVisibility(View.INVISIBLE);
         }
         else
             skipButton.setVisibility(View.VISIBLE);
     }
     
     @Override
     public void updateScoreText() {
         if (mCallback != null && mCallback.getCurrentScore() > -1 &&
                 mCallback.getDisplayName() != null) {
             scoreText.setText(Integer.toString(mCallback.getCurrentScore()));
             scoreText.setVisibility(View.VISIBLE);
         }
         else
             scoreText.setVisibility(View.INVISIBLE);
     }
     
 }
