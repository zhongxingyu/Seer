 package com.jeffthefate.dmbquiz.activity;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import android.app.NotificationManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.TransitionDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.facebook.Request;
 import com.facebook.Request.GraphUserCallback;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 import com.jeffthefate.dmbquiz.ApplicationEx;
 import com.jeffthefate.dmbquiz.Constants;
 import com.jeffthefate.dmbquiz.DatabaseHelper;
 import com.jeffthefate.dmbquiz.R;
 import com.jeffthefate.dmbquiz.VersionedActionBar;
 import com.jeffthefate.dmbquiz.fragment.FragmentBase;
 import com.jeffthefate.dmbquiz.fragment.FragmentBase.OnButtonListener;
 import com.jeffthefate.dmbquiz.fragment.FragmentInfo;
 import com.jeffthefate.dmbquiz.fragment.FragmentLeaders;
 import com.jeffthefate.dmbquiz.fragment.FragmentLoad;
 import com.jeffthefate.dmbquiz.fragment.FragmentLogin;
 import com.jeffthefate.dmbquiz.fragment.FragmentNameDialog;
 import com.jeffthefate.dmbquiz.fragment.FragmentQuiz;
 import com.jeffthefate.dmbquiz.fragment.FragmentScoreDialog;
 import com.jeffthefate.dmbquiz.fragment.FragmentSplash;
 import com.parse.GetCallback;
 import com.parse.LogInCallback;
 import com.parse.Parse;
 import com.parse.ParseAnonymousUtils;
 import com.parse.ParseException;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseTwitterUtils;
 import com.parse.ParseUser;
 import com.parse.PushService;
 import com.parse.RequestPasswordResetCallback;
 import com.parse.SaveCallback;
 import com.parse.SignUpCallback;
 
 public class ActivityMain extends FragmentActivity implements OnButtonListener {
     
     private ParseUser user;
     private String userId;
     
     private ImageView background;
     private TextView noConnection;
     
     FragmentManager fMan;
     private FragmentBase currFrag;
     
     private int rawIndex = -1;
     private Field[] fields;
     private ArrayList<Integer> fieldsList;
     private String currentBackground = null;
     
     private boolean loggedIn = false;
     private boolean isLogging = false;
     private boolean inLoad = false;
     private boolean inStats = false;
     private boolean inInfo = false;
     
     private String questionId;
     private String question;
     private String correctAnswer;
     private String questionCategory;
     private String questionScore;
     private boolean questionHint = false;
     private boolean questionSkip = false;
     private String nextQuestionId;
     private String nextQuestion;
     private String nextCorrectAnswer;
     private String nextQuestionCategory;
     private String nextQuestionScore;
     private boolean nextQuestionHint = false;
     private boolean nextQuestionSkip = false;
     private String thirdQuestionId;
     private String thirdQuestion;
     private String thirdCorrectAnswer;
     private String thirdQuestionCategory;
     private String thirdQuestionScore;
     private boolean thirdQuestionHint = false;
     private boolean thirdQuestionSkip = false;
     
     private boolean newQuestion = false;
     
     private GetStatsTask getStatsTask;
     private GetScoreTask getScoreTask;
     private GetNextQuestionsTask getNextQuestionsTask;
     private GetStageTask getStageTask;
     
     private Bundle leadersBundle;
     private ArrayList<String> rankList = new ArrayList<String>();
     private ArrayList<String> userList = new ArrayList<String>();
     private ArrayList<String> scoreList = new ArrayList<String>();
     private ArrayList<String> userIdList = new ArrayList<String>();
     
     private String displayName;
     private int currScore = -1;
     private int tempScore = 0;
     
     private UserTask userTask;
     
     private ArrayList<String> correctAnswers;
     private ArrayList<String> tempAnswers;
     
     private boolean networkProblem = false;
     private boolean facebookLogin = false;
     private boolean newUser = false;
     
     public interface UiCallback {
         public void showNetworkProblem();
         public void showLoading(String message);
         public void showNoMoreQuestions();
         public void resumeQuestion();
         public void updateScoreText();
         public void resetHint();
         public void disableButton(boolean isRetry);
         public void enableButton(boolean isRetry);
         public void setDisplayName(String displayName);
     }
     
     private NotificationManager nManager;
     
     private ConnectionReceiver connReceiver;
     
     private Resources res;
     
     private Drawable backgroundDrawable;
     private Drawable tempDrawable;
     private Drawable[] arrayDrawable = new Drawable[2];
     private TransitionDrawable transitionDrawable;
     private BitmapDrawable oldBitmapDrawable = null;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         /*
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                 .detectAll()
                 .penaltyDialog()
                 .penaltyLog()
                 .permitDiskReads()
                 .permitDiskWrites()
                 .build());
         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                 .detectAll()
                 .penaltyLog()
                 .build());
         */
         super.onCreate(savedInstanceState);
         res = getResources();
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
             requestWindowFeature(Window.FEATURE_NO_TITLE);
         else
             VersionedActionBar.newInstance().create(this).setDisplayHome();
         setContentView(R.layout.main);
         fields = R.drawable.class.getFields();
         fieldsList = new ArrayList<Integer>();
         for (Field field : fields) {
             if (field.getName().contains("splash")) {
                 try {
                     fieldsList.add(field.getInt(null));
                 } catch (IllegalArgumentException e1) {e1.printStackTrace();
                 } catch (IllegalAccessException e1) {e1.printStackTrace();}
             }
         }
         background = (ImageView) findViewById(R.id.Background);
         noConnection = (TextView) findViewById(R.id.NoConnection);
         /*
         TEST
         Parse.initialize(this, "6pJz1oVHAwZ7tfOuvHfQCRz6AVKZzg1itFVfzx2q",
                 "2ocGkdBygVyNStd8gFQQgrDyxxZJCXt3K1GbRpMD");
         */
         Parse.initialize(this, "ImI8mt1EM3NhZNRqYZOyQpNSwlfsswW73mHsZV3R",
                 "hpTbnpuJ34zAFLnpOAXjH583rZGiYQVBWWvuXsTo");
         PushService.subscribe(this, "", ActivityMain.class);
         PushService.setDefaultPushCallback(this, ActivityMain.class);
         fMan = getSupportFragmentManager();
         if (savedInstanceState != null) {
             loggedIn = savedInstanceState.getBoolean("loggedIn");
             isLogging = savedInstanceState.getBoolean("isLogging");
             inLoad = savedInstanceState.getBoolean("inLoad");
             inStats = savedInstanceState.getBoolean("inStats");
             inInfo = savedInstanceState.getBoolean("inInfo");
             currentBackground = savedInstanceState.getString(
                     "currentBackground");
             userId = savedInstanceState.getString("userId");
             questionId = savedInstanceState.getString("questionId");
             question = savedInstanceState.getString("question");
             correctAnswer = savedInstanceState.getString("correctAnswer");
             questionScore = savedInstanceState.getString("questionScore");
             questionCategory = savedInstanceState.getString("questionCategory");
             questionHint = savedInstanceState.getBoolean("questionHint");
             questionSkip = savedInstanceState.getBoolean("questionSkip");
             nextQuestionId = savedInstanceState.getString("nextQuestionId");
             nextQuestion = savedInstanceState.getString("nextQuestion");
             nextCorrectAnswer = savedInstanceState.getString(
                     "nextCorrectAnswer");
             nextQuestionScore = savedInstanceState.getString(
                     "nextQuestionScore");
             nextQuestionCategory = savedInstanceState.getString(
                     "nextQuestionCategory");
             nextQuestionHint = savedInstanceState.getBoolean(
                     "nextQuestionHint");
             nextQuestionSkip = savedInstanceState.getBoolean(
                     "nextQuestionSkip");
             thirdQuestionId = savedInstanceState.getString("thirdQuestionId");
             thirdQuestion = savedInstanceState.getString("thirdQuestion");
             thirdCorrectAnswer = savedInstanceState.getString(
                     "thirdCorrectAnswer");
             thirdQuestionScore = savedInstanceState.getString(
                     "thirdQuestionScore");
             thirdQuestionCategory = savedInstanceState.getString(
                     "thirdQuestionCategory");
             thirdQuestionHint = savedInstanceState.getBoolean(
                     "thirdQuestionHint");
             thirdQuestionSkip = savedInstanceState.getBoolean(
                     "thirdQuestionSkip");
             newQuestion = savedInstanceState.getBoolean("newQuestion");
             displayName = savedInstanceState.getString("displayName");
             currScore = savedInstanceState.getInt("currScore");
             correctAnswers = savedInstanceState.getStringArrayList(
                     "correctAnswers");
             newUser = savedInstanceState.getBoolean("newUser");
             networkProblem = savedInstanceState.getBoolean("networkProblem");
             ApplicationEx.dbHelper.setUserValue(networkProblem ? 1 : 0,
                     DatabaseHelper.COL_NETWORK_PROBLEM, userId);
         }
         else {
             userId = ApplicationEx.dbHelper.getCurrUser();
             if (userId != null)
                 getPersistedData(userId);
         }
         if (userId == null) {
             userId = ApplicationEx.dbHelper.getCurrUser();
             if (userId != null)
                 getUserData(userId);
         }
         if (currentBackground == null && userId != null)
             currentBackground =
                     ApplicationEx.dbHelper.getCurrBackground(userId);
         if (currentBackground != null) {
             int resourceId = res.getIdentifier(currentBackground, "drawable",
                     getPackageName());
             background.setImageResource(resourceId);
         }
         nManager = (NotificationManager) getSystemService(
                 Context.NOTIFICATION_SERVICE);
         connReceiver = new ConnectionReceiver();
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putBoolean("loggedIn", loggedIn);
         outState.putBoolean("isLogging", isLogging);
         outState.putBoolean("inLoad", inLoad);
         outState.putBoolean("inStats", inStats);
         outState.putBoolean("inInfo", inInfo);
         outState.putString("currentBackgrond", currentBackground);
         outState.putString("userId", userId);
         outState.putString("questionId", questionId);
         outState.putString("question", question);
         outState.putString("correctAnswer", correctAnswer);
         outState.putString("questionCategory", questionCategory);
         outState.putString("questionScore", questionScore);
         outState.putBoolean("questionHint", questionHint);
         outState.putBoolean("questionSkip", questionSkip);
         outState.putString("nextQuestionId", nextQuestionId);
         outState.putString("nextQuestion", nextQuestion);
         outState.putString("nextCorrectAnswer", nextCorrectAnswer);
         outState.putString("nextQuestionCategory", nextQuestionCategory);
         outState.putString("nextQuestionScore", nextQuestionScore);
         outState.putBoolean("nextQuestionHint", nextQuestionHint);
         outState.putBoolean("nextQuestionSkip", nextQuestionSkip);
         outState.putString("thirdQuestionId", thirdQuestionId);
         outState.putString("thirdQuestion", thirdQuestion);
         outState.putString("thirdCorrectAnswer", thirdCorrectAnswer);
         outState.putString("thirdQuestionCategory", thirdQuestionCategory);
         outState.putString("thirdQuestionScore", thirdQuestionScore);
         outState.putBoolean("thirdQuestionHint", thirdQuestionHint);
         outState.putBoolean("thirdQuestionSkip", thirdQuestionSkip);
         outState.putBoolean("newQuestion", newQuestion);
         outState.putString("displayName", displayName);
         outState.putInt("currScore", currScore);
         outState.putStringArrayList("correctAnswers", correctAnswers);
         outState.putBoolean("newUser", newUser);
         outState.putBoolean("networkProblem", networkProblem);
         super.onSaveInstanceState(outState);
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (getScoreTask != null)
             getScoreTask.cancel(true);
         /*
         if (correctAnswers == null) {
             if (tempAnswers != null)
                 tempAnswers.clear();
             else
                 tempAnswers = new ArrayList<String>();
             getScore(userId, false, false);
         }
         */
         if (user == null)
             user = ParseUser.getCurrentUser();
         if (userId == null && user != null) {
             userId = user.getObjectId();
             if (userId != null)
                 getUserData(userId);
         }
         if (!isLogging) {
             if (userId == null) {
                 if (!loggedIn)
                     logOut(false);
                 else
                     checkUser();
             }
             else {
                 if (correctAnswers == null) {
                     isLogging = true;
                     showLogin();
                     if (tempAnswers != null)
                         tempAnswers.clear();
                     else
                         tempAnswers = new ArrayList<String>();
                     getScore(false, true, userId, false);
                 }
                 else
                     showLoggedInFragment();
             }
         }
         else {
             if (userId != null)
                 setupUser(newUser);
             else if (!facebookLogin)
                 checkUser();
         }
         if (inInfo)
             onInfoPressed();
         ApplicationEx.setActive();
         nManager.cancel(Constants.NOTIFICATION_NEW_QUESTIONS);
         registerReceiver(connReceiver,
                 new IntentFilter(Constants.ACTION_CONNECTION));
     }
     
     @Override
     public void onBackPressed() {
         if (!inStats && !inLoad && !isLogging && !inInfo)
             moveTaskToBack(true);
         else {
             if (inLoad) {
                 if (getStatsTask != null)
                     getStatsTask.cancel(true);
                 showQuiz();
                 inLoad = false;
                 ApplicationEx.dbHelper.setUserValue(inLoad ? 1 : 0,
                         DatabaseHelper.COL_IN_LOAD, userId);
             }
             else if (inStats) {
                 showQuiz();
                 inStats = false;
                 ApplicationEx.dbHelper.setUserValue(inStats ? 1 : 0,
                         DatabaseHelper.COL_IN_STATS, userId);
             }
             else if (inInfo) {
                 showSplash();
                 inInfo = false;
                 ApplicationEx.dbHelper.setUserValue(inInfo ? 1 : 0,
                         DatabaseHelper.COL_IN_INFO, userId);
             }
             else if (isLogging) {
                 if (userTask != null)
                     userTask.cancel(true);
                 if (facebookTask != null)
                     facebookTask.cancel(true);
                 if (getScoreTask != null)
                     getScoreTask.cancel(true);
                 logOut(true);
             }
         }
     }
     
     @Override
     public void onPause() {
         try {
             unregisterReceiver(connReceiver);
         } catch (IllegalArgumentException e) {}
         if (getStageTask != null)
             getStageTask.cancel(true);
         if (getNextQuestionsTask != null)
             getNextQuestionsTask.cancel(true);
         if (userTask != null)
             userTask.cancel(true);
         if (facebookTask != null)
             facebookTask.cancel(true);
         if (getStatsTask != null)
             getStatsTask.cancel(true);
         if (!isLogging && userId != null) {
             if (getScoreTask != null)
                 getScoreTask.cancel(true);
             if (tempAnswers != null)
                 tempAnswers.clear();
             else
                 tempAnswers = new ArrayList<String>();
             getScore(false, false, userId, false);
         }
         ApplicationEx.setInactive();
         super.onPause();
     }
 
     @Override
     public void setBackground(String name, boolean showNew) {
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             new SetBackgroundTask(name, showNew).execute();
         else
             new SetBackgroundTask(name, showNew).executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class SetBackgroundTask extends AsyncTask<Void, Void, Void> {
         private String name;
         private boolean showNew;
         private int resourceId;
         
         private SetBackgroundTask(String name, boolean showNew) {
             this.name = name;
             this.showNew = showNew;
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             if (name == null)
                 name = "splash8";
             resourceId = res.getIdentifier(name, "drawable", getPackageName());
             if (showNew) {
                 rawIndex = fieldsList.indexOf(resourceId);
                 if (rawIndex < 0)
                     rawIndex = fieldsList.indexOf(R.drawable.splash8);
                 rawIndex++;
                 if (rawIndex >= fieldsList.size())
                     rawIndex = 0;
                 int currentId = fieldsList.get(rawIndex);
                 currentBackground = res.getResourceEntryName(currentId);
                 ApplicationEx.dbHelper.setCurrBackground(userId, currentBackground);
                 if (currentId != resourceId) {
                     try {
                         tempDrawable = res.getDrawable(currentId);
                         backgroundDrawable = background.getDrawable();
                         oldBitmapDrawable = null;
                         if (backgroundDrawable instanceof TransitionDrawable) {
                             transitionDrawable = (TransitionDrawable) backgroundDrawable;
                             oldBitmapDrawable = (BitmapDrawable)(
                                     transitionDrawable.getDrawable(1));
                         }
                         else if (backgroundDrawable instanceof BitmapDrawable)
                             oldBitmapDrawable = (BitmapDrawable) backgroundDrawable;
                         if (backgroundDrawable != null) {
                             arrayDrawable[0] = oldBitmapDrawable;
                             arrayDrawable[1] = tempDrawable;
                         }
                     } catch (Resources.NotFoundException e) {
                         setBackground(currentBackground, showNew);
                     }
                 }
                 else
                     setBackground(currentBackground, showNew);
             }
             ApplicationEx.dbHelper.setCurrBackground(userId, currentBackground);
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             if (showNew) {
                 if (backgroundDrawable != null) {
                     transitionDrawable = new TransitionDrawable(arrayDrawable);
                     transitionDrawable.setCrossFadeEnabled(true);
                     background.setImageDrawable(transitionDrawable);
                     transitionDrawable.startTransition(500);
                 }
                 else
                     background.setImageDrawable(tempDrawable);
             }
             else {
                 if (fieldsList.indexOf(resourceId) >= 0)
                     background.setImageResource(resourceId);
                 else
                     background.setImageResource(R.drawable.splash8);
             }
         }
     }
 
     private void checkUser() {
         noConnection.setVisibility(View.INVISIBLE);
         user = ParseUser.getCurrentUser();
         if (user == null)
             userId = null;
         else {
             userId = user.getObjectId();
             if (userId != null)
                 getUserData(userId);
         }
         if (!loggedIn && !isLogging && (user == null || userId == null))
             logOut(true);
         else
             setupUser(newUser);
     }
     
     @Override
     public void setupUser(boolean newUser) {
         if (userId == null && !isLogging) {
             showSplash();
             return;
         }
         if (userId != null)
             getUserData(userId);
         if (fMan.findFragmentByTag("fLogin") == null)
             showLogin();
         if (userTask != null)
             userTask.cancel(true);
         userTask = new UserTask(newUser);
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
             userTask.execute();
         else
             userTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class UserTask extends AsyncTask<Void, Void, Void> {
         private boolean newUser;
         
         private UserTask(boolean newUser) {
             this.newUser = newUser;
         }
         
         @Override
         protected void onPreExecute() {
             if (newUser && currFrag != null)
                 currFrag.showLoading("Creating account...");
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             user = ParseUser.getCurrentUser();
             if (user == null) {
                 publishProgress();
                 return null;
             }
             if (userId == null) {
                 publishProgress();
                 return null;
             }
             if (getScoreTask != null)
                 getScoreTask.cancel(true);
             if (ApplicationEx.dbHelper.isAnonUser(userId)) {
                 if (correctAnswers != null)
                     correctAnswers.clear();
                 else
                     correctAnswers = new ArrayList<String>();
                 getNextQuestions(false);
             }
             else {
                 if (tempAnswers != null)
                     tempAnswers.clear();
                 else
                     tempAnswers = new ArrayList<String>();
                 publishProgress();
             }
             if (displayName == null && !isCancelled()) {
                 displayName = user.getString("displayName");
                 ApplicationEx.dbHelper.setUserValue(displayName, 
                         DatabaseHelper.COL_DISPLAY_NAME, userId);
             }
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             if (userId == null || user == null) {
                 logOut(true);
                 ApplicationEx.mToast.setText("Login failed, try again");
                 ApplicationEx.mToast.show();
             }
             else
                 getScore(true, false, userId, newUser);
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             if (userId != null)
                 setBackground(ApplicationEx.dbHelper.getCurrBackground(userId),
                         false);
         }
     }
     
     private void getScore(boolean show, boolean restore, String userId,
             boolean newUser) {
         if (getScoreTask != null)
             getScoreTask.cancel(true);
         getScoreTask = new GetScoreTask(show, restore, userId, newUser);
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             getScoreTask.execute();
         else
             getScoreTask.executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     public class GetScoreTask extends AsyncTask<Void, Void, Void> {
         private boolean show;
         private boolean restore;
         private String userId;
         private boolean newUser;
         private List<ParseObject> scoreList;
         private List<ParseObject> deleteList;
         private Number number;
         
         private GetScoreTask(boolean show, boolean restore, String userId,
                 boolean newUser) {
             this.show = show;
             this.restore = restore;
             this.userId = userId;
             this.newUser = newUser;
         }
         
         @Override
         protected void onPreExecute() {
             if (!newUser && currFrag != null)
                 currFrag.showLoading("Calculating score...");
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             int questionScore = -1;
             ParseQuery correctQuery = new ParseQuery("CorrectAnswers");
             correctQuery.whereEqualTo("userId", userId);
             correctQuery.setLimit(1000);
             ParseQuery query = new ParseQuery("Question");
             query.setLimit(1000);
             ParseQuery deleteQuery = new ParseQuery("CorrectAnswers");
             deleteQuery.whereEqualTo("userId", userId);
             do {
                 correctQuery.whereEqualTo("hint", true);
                 correctQuery.whereNotContainedIn("questionId", tempAnswers);
                 query.whereMatchesKeyInQuery("objectId", "questionId",
                         correctQuery);
                 try {
                     scoreList = query.find();
                     if (scoreList.size() == 0)
                         break;
                     for (ParseObject score : scoreList) {
                         if (isCancelled())
                             return null;
                         if (tempAnswers.contains(score.getObjectId()) &&
                                 userId != null) {
                             deleteQuery.whereEqualTo("questionId",
                                     score.getObjectId());
                             deleteList = deleteQuery.find();
                             for (int i = 1; i < deleteList.size(); i++) {
                                 try {
                                     deleteList.get(i).deleteEventually();
                                 } catch (RuntimeException exception) {}
                             }
                         }
                         else {
                             number = score.getNumber("score");
                             questionScore = number == null ? 1000 :
                                     number.intValue();
                             tempScore += questionScore / 2;
                             tempAnswers.add(score.getObjectId());
                         }
                     }
                 } catch (ParseException e) {
                     Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                     if (userTask != null)
                         userTask.cancel(true);
                     if (getScoreTask != null)
                         getScoreTask.cancel(true);
                     if (show && currFrag != null)
                         currFrag.showNetworkProblem();
                 }
             } while (!isCancelled());
             do {
                 correctQuery.whereEqualTo("hint", false);
                 correctQuery.whereNotContainedIn("questionId", tempAnswers);
                 query.whereMatchesKeyInQuery("objectId", "questionId",
                         correctQuery);
                 try {
                     scoreList = query.find();
                     if (scoreList.size() == 0)
                         break;
                     for (ParseObject score : scoreList) {
                         if (isCancelled())
                             return null;
                         if (tempAnswers.contains(score.getObjectId()) &&
                                 userId != null) {
                             deleteQuery.whereEqualTo("questionId",
                                     score.getObjectId());
                             deleteList = deleteQuery.find();
                             for (int i = 1; i < deleteList.size(); i++) {
                                 try {
                                     deleteList.get(i).deleteEventually();
                                 } catch (RuntimeException exception) {}
                             }
                         }
                         else {
                             number = score.getNumber("score");
                             questionScore = number == null ? 1000 :
                                     number.intValue();
                             tempScore += questionScore;
                             tempAnswers.add(score.getObjectId());
                         }
                     }
                 } catch (ParseException e) {
                     Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                     if (userTask != null)
                         userTask.cancel(true);
                     if (getScoreTask != null)
                         getScoreTask.cancel(true);
                     if (show && currFrag != null)
                         currFrag.showNetworkProblem();
                 }
             } while (!isCancelled());
             if (!isCancelled()) {
                 currScore = tempScore;
                 tempScore = 0;
                 saveUserScore(currScore);
                 correctAnswers = new ArrayList<String>(tempAnswers);
                 ApplicationEx.setStringArrayPref(
                         getString(R.string.correct_key), correctAnswers);
                 publishProgress();
             }
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         protected void onProgressUpdate(Void... nothing) {
             if (show) {
                 if (questionId != null) {
                     try {
                         goToQuiz();
                     } catch (IllegalStateException exception) {}
                 }
                 else
                     getNextQuestions(false);
             }
             if (restore)
                 showLoggedInFragment();
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
         }
     }
     
     private void showLoggedInFragment() {
         fetchDisplayName();
         if (user == null)
             user = ParseUser.getCurrentUser();
         if (displayName == null) {
             displayName = user.getString("displayName");
             ApplicationEx.dbHelper.setUserValue(displayName, 
                     DatabaseHelper.COL_DISPLAY_NAME, userId);
         }
         if (inStats)
             showLeaders();
         else if (inLoad)
             onStatsPressed();
         else {
             isLogging = false;
             ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                     DatabaseHelper.COL_LOGGING, userId);
             loggedIn = true;
             ApplicationEx.dbHelper.setUserValue(loggedIn ? 1 : 0,
                     DatabaseHelper.COL_LOGGED_IN, userId);
             showQuiz();
         }
     }
     
     private void fetchDisplayName() {
         if (userId == null)
             return;
         ParseQuery query = ParseUser.getQuery();
         query.whereEqualTo("objectId", userId);
         query.getInBackground("displayName", new GetCallback() {
             @Override
             public void done(ParseObject user, ParseException e) {
                 if (user != null)
                     displayName = user.getString("displayName");
             }
         });
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode,
             Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
     }
     
     @Override
     public void onInfoPressed() {
         try {
             FragmentInfo fInfo = new FragmentInfo();
             fMan.beginTransaction().replace(android.R.id.content, fInfo,
                     "fInfo").commitAllowingStateLoss();
             fMan.executePendingTransactions();
             currFrag = fInfo;
             inInfo = true;
             ApplicationEx.dbHelper.setUserValue(inInfo ? 1 : 0,
                     DatabaseHelper.COL_IN_INFO, userId);
         } catch (IllegalStateException e) {}
     }
     
     @Override
     public void onStatsPressed() {
         showLoad();
         if (getStatsTask != null)
             getStatsTask.cancel(true);
         getStatsTask = new GetStatsTask(
                 ApplicationEx.dbHelper.getScore(userId));
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             getStatsTask.execute();
         else
             getStatsTask.executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
         inLoad = true;
         ApplicationEx.dbHelper.setUserValue(inLoad ? 1 : 0,
                 DatabaseHelper.COL_IN_LOAD, userId);
     }
     
     @Override
     public void onLoginPressed(int loginType, String user, String pass) {
         isLogging = true;
         showLogin();
         switch(loginType) {
         case FragmentBase.LOGIN_FACEBOOK:
             facebookLogin();
             break;
         case FragmentBase.LOGIN_TWITTER:
             twitterLogin();
             break;
         case FragmentBase.LOGIN_ANON:
             anonymousLogin();
             break;
         case FragmentBase.LOGIN_EMAIL:
             emailLogin(user, pass);
             break;
         case FragmentBase.SIGNUP_EMAIL:
             emailSignup(user, pass);
             break;
         }
         ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                 DatabaseHelper.COL_LOGGING, userId);
     }
     
     private void goToQuiz() {
         isLogging = false;
         ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                 DatabaseHelper.COL_LOGGING, userId);
         showQuiz();
         loggedIn = true;
         ApplicationEx.dbHelper.setUserValue(loggedIn ? 1 : 0,
                 DatabaseHelper.COL_LOGGED_IN, userId);
     }
     
     private void facebookLogin() {
         facebookLogin = true;
         ParseFacebookUtils.logIn(this, new LogInCallback() {
             @Override
             public void done(ParseUser user, ParseException err) {
                 if (user == null) {
                     if (err != null) {
                         if (err.getCode() == ParseException.CONNECTION_FAILED) {
                             ApplicationEx.mToast.setText("Network error");
                             ApplicationEx.mToast.show();
                         }
                         else {
                             ApplicationEx.mToast.setText(
                                     "Login failed, try again");
                             ApplicationEx.mToast.show();
                         }
                         logOut(false);
                     }
                     else
                         logOut(false);
                 } else {
                     newUser = user.isNew();
                     userId = user.getObjectId();
                     if (!ApplicationEx.dbHelper.hasUser(userId))
                         ApplicationEx.dbHelper.addUser(user, "Facebook");
                     else
                         ApplicationEx.dbHelper.setOffset(1, user.getObjectId());
                     facebookTask = new FacebookTask(user); 
                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                         facebookTask.execute();
                     else
                         facebookTask.executeOnExecutor(
                                 AsyncTask.THREAD_POOL_EXECUTOR);
                     facebookLogin = false;
                 }
             }
         });
     }
     
     private FacebookTask facebookTask;
     
     private class FacebookTask extends AsyncTask<Void, Void, Void> {
         ParseUser user;
         
         private FacebookTask(ParseUser user) {
             this.user = user;
         }
         
         @Override
         protected Void doInBackground(Void... params) {
             if (user.getString("displayName") == null) {
                 Session session = ParseFacebookUtils.getSession();
             	if (session.getState().isOpened()) {
             		Request.executeMeRequestAsync(session,
             				new GraphUserCallback() {
 						@Override
 						public void onCompleted(GraphUser graphUser,
 								Response response) {
 							if (user != null) {
 								displayName = graphUser.getFirstName() + " "
 										+ graphUser.getLastName().substring(
 												0, 1) + ".";
 				                user.put("displayName", displayName);
 				                try {
 				                    user.saveEventually();
 				                }
 				                catch (RuntimeException e) {}
 				                ApplicationEx.dbHelper.setUserValue(displayName, 
 			                        DatabaseHelper.COL_DISPLAY_NAME, userId);
 							}
 						}
             		});
             	}
             }
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             if (isLogging)
                 setupUser(newUser);
         }
     }
     
     private void twitterLogin() {
         ParseTwitterUtils.logIn(this, new LogInCallback() {
             @Override
             public void done(ParseUser user, ParseException err) {
                 if (user == null) {
                     if (err != null) {
                         if (err.getCode() == ParseException.CONNECTION_FAILED) {
                             ApplicationEx.mToast.setText("Network error");
                             ApplicationEx.mToast.show();
                         }
                         else {
                             ApplicationEx.mToast.setText(
                                     "Login failed, try again");
                             ApplicationEx.mToast.show();
                         }
                         logOut(false);
                     }
                     else
                         logOut(false);
                 } else {
                     newUser = user.isNew();
                     userId = user.getObjectId();
                     if (user.getString("displayName") == null) {
                         user.put("displayName", "@" +
                                 ParseTwitterUtils.getTwitter().getScreenName());
                         try {
                             user.saveEventually();
                         }
                         catch (RuntimeException e) {}
                     }
                     if (!ApplicationEx.dbHelper.hasUser(user.getObjectId()))
                         ApplicationEx.dbHelper.addUser(user, "Twitter");
                     else
                         ApplicationEx.dbHelper.setOffset(1, user.getObjectId());
                     if (isLogging)
                         setupUser(newUser);
                 }
             }
         });
     }
     
     private void anonymousLogin() {
         ParseAnonymousUtils.logIn(new LogInCallback() {
             @Override
             public void done(final ParseUser user, ParseException e) {
                 if (e != null) {
                     if (e.getCode() == ParseException.CONNECTION_FAILED) {
                         ApplicationEx.mToast.setText("Network error");
                         ApplicationEx.mToast.show();
                     }
                     else {
                         ApplicationEx.mToast.setText(
                                 "Login failed, try again");
                         ApplicationEx.mToast.show();
                     }
                     logOut(false);
                 }
                 else {
                     newUser = user.isNew();
                     userId = user.getObjectId();
                     try {
                         user.saveInBackground(new SaveCallback() {
                             @Override
                             public void done(ParseException e) {
                                 if (e == null) {
                                     if (!ApplicationEx.dbHelper.hasUser(
                                                 user.getObjectId()))
                                         ApplicationEx.dbHelper.addUser(user,
                                                 "Anonymous");
                                     else
                                         ApplicationEx.dbHelper.setOffset(1,
                                                 user.getObjectId());
                                     if (isLogging)
                                         setupUser(newUser);
                                 }
                             }
                         });
                     }
                     catch (RuntimeException exception) {}
                 }
             }
         });
     }
     
     private void emailSignup(String username, String password) {
         final ParseUser user = new ParseUser();
         user.setUsername(username);
         user.setPassword(password);
         int atIndex = username.indexOf("@");
         int numReplace = atIndex - 2;
         if (numReplace < 1)
             numReplace = 1;
         user.put("displayName", username.substring(0, username.indexOf("@")+1));
         user.signUpInBackground(new SignUpCallback() {
             @Override
             public void done(ParseException err) {
                 if (err == null) {
                     newUser = user.isNew();
                     userId = user.getObjectId();
                     if (!ApplicationEx.dbHelper.hasUser(userId))
                         ApplicationEx.dbHelper.addUser(user, "Email");
                     else
                         ApplicationEx.dbHelper.setOffset(1, user.getObjectId());
                     if (isLogging)
                         setupUser(newUser);
                 }
                 else {
                     if (err.getCode() == ParseException.CONNECTION_FAILED) {
                         ApplicationEx.mToast.setText("Network error");
                         ApplicationEx.mToast.show();
                     }
                     else if (err.getCode() != 202) {
                         ApplicationEx.mToast.setText(
                                 "Sign up failed: " + err.getCode());
                         ApplicationEx.mToast.show();
                     }
                     logOut(false);
                 }
             }
         });
     }
     
     private void emailLogin(final String username, String password) {
         ParseUser.logInInBackground(username, password, new LogInCallback() {
             @Override
             public void done(ParseUser user, ParseException err) {
                 if (user == null) {
                     if (err != null) {
                         Log.e(Constants.LOG_TAG, "Login failed: " +
                                 err.getCode());
                         if (err.getCode() == ParseException.CONNECTION_FAILED) {
                             ApplicationEx.mToast.setText("Network error");
                             ApplicationEx.mToast.show();
                         }
                         else if (err.getCode() ==
                                 ParseException.OBJECT_NOT_FOUND) {
                             ApplicationEx.mToast.setText("Invalid password");
                             ApplicationEx.mToast.show();
                         }
                         logOut(false);
                     }
                     else
                         logOut(false);
                 } else {
                     newUser = user.isNew();
                     userId = user.getObjectId();
                     if (user.getString("displayName") == null) {
                         user.put("displayName", username.substring(0,
                                          username.indexOf("@")+1));
                         try {
                             user.saveEventually();
                         }
                         catch (RuntimeException e) {}
                     }
                     if (!ApplicationEx.dbHelper.hasUser(userId))
                         ApplicationEx.dbHelper.addUser(user, "Email");
                     else
                         ApplicationEx.dbHelper.setOffset(1, userId);
                     if (isLogging)
                         setupUser(newUser);
                 }
             }
         });
     }
     
     @Override
     public void resetPassword(String username) {
         ParseUser.requestPasswordResetInBackground(username,
                 new RequestPasswordResetCallback() {
             public void done(ParseException e) {
                 if (e == null) {
                     ApplicationEx.mToast.setText("Password reset email has " +
                             "been sent to this address");
                     ApplicationEx.mToast.show();
                 } else {
                     ApplicationEx.mToast.setText("An error occurred, " +
                             "try again");
                     ApplicationEx.mToast.show();
                 }
             }
         });
     }
     
     @Override
     public String getBackground() {
         return currentBackground;
     }
 
     @Override
     public void showScoreDialog() {
         DialogFragment newFragment = new FragmentScoreDialog();
         newFragment.show(getSupportFragmentManager(), "dScore");
     }
     
     @Override
     public void showNameDialog() {
         DialogFragment newFragment = new FragmentNameDialog();
         newFragment.show(getSupportFragmentManager(), "dName");
     }
 
     @Override
     public void logOut(boolean force) {
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
             new LogOutTask(force).execute();
         else
             new LogOutTask(force).executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class LogOutTask extends AsyncTask<Void, Void, Void> {
         boolean force;
         
         private LogOutTask(boolean force) {
             this.force = force;
         }
         
         @Override
         protected void onPreExecute() {
             showLogin();
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             loggedIn = false;
             if (userTask != null)
                 userTask.cancel(true);
             if (facebookTask != null)
                 facebookTask.cancel(true);
             if (getScoreTask != null)
                 getScoreTask.cancel(true);
             if (getNextQuestionsTask != null)
                 getNextQuestionsTask.cancel(true);
             if (getStageTask != null)
                 getStageTask.cancel(true);
             if (getStatsTask != null)
                 getStatsTask.cancel(true);
             if (userId != null) {
                 ApplicationEx.dbHelper.setUserValue(isLogging ? 1 : 0,
                         DatabaseHelper.COL_LOGGING, userId);
                 ApplicationEx.dbHelper.setUserValue(loggedIn ? 1 : 0,
                         DatabaseHelper.COL_LOGGED_IN, userId);
                 ApplicationEx.dbHelper.setOffset(0, userId);
             }
             ParseUser.logOut();
             user = null;
             userId = null;
             displayName = null;
             if (correctAnswers != null) {
                 correctAnswers.clear();
                 correctAnswers = null;
                 ApplicationEx.setStringArrayPref(
                         getString(R.string.correct_key), correctAnswers);
             }
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         protected void onProgressUpdate(Void... nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             if (!getNetworkProblem() || force) {
                 isLogging = false;
                 showSplash();
             }
         }
     }
     
     private void showLeaders() {
         try {
             FragmentLeaders fLeaders = new FragmentLeaders();
             fMan.beginTransaction().replace(android.R.id.content, fLeaders,
                     "fLeaders").commitAllowingStateLoss();
             fMan.executePendingTransactions();
             currFrag = fLeaders;
         } catch (IllegalStateException e) {}
     }
     
     private void showLogin() {
         try {
             FragmentLogin fLogin = new FragmentLogin();
             fMan.beginTransaction().replace(android.R.id.content, fLogin,
                     "fLogin").commitAllowingStateLoss();
             fMan.executePendingTransactions();
             currFrag = fLogin;
         } catch (IllegalStateException e) {}
     }
     
     private void showSplash() {
         try {
             FragmentSplash fSplash = new FragmentSplash();
             fMan.beginTransaction().replace(android.R.id.content, fSplash,
                     "fSplash").commitAllowingStateLoss();
             fMan.executePendingTransactions();
             currFrag = fSplash;
         } catch (IllegalStateException e) {}
     }
     
     private void showQuiz() {
         newUser = false;
         try {
             FragmentQuiz fQuiz = new FragmentQuiz();
             fMan.beginTransaction().replace(android.R.id.content, fQuiz,
                     "fQuiz").commitAllowingStateLoss();
             fMan.executePendingTransactions();
             currFrag = fQuiz;
         } catch (IllegalStateException e) {}
     }
     
     private void showLoad() {
         try {
             FragmentLoad fLoad = new FragmentLoad();
             fMan.beginTransaction().replace(android.R.id.content, fLoad,
                     "fLoad").commitAllowingStateLoss();
             fMan.executePendingTransactions();
             currFrag = fLoad;
         } catch (IllegalStateException e) {}
     }
     
     private class GetStatsTask extends AsyncTask<Void, Void, Void> {
         int currScore;
         ParseException error;
         int hints;
         
         private GetStatsTask(int currScore) {
             this.currScore = currScore;
         }
         
         @Override
         protected void onPreExecute() {
             currFrag.showLoading("Downloading standings...");
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             leadersBundle = new Bundle();
             leadersBundle.putString("userId", userId);
             leadersBundle.putString("userName", displayName);
             leadersBundle.putString("userScore", Integer.toString(currScore));
             leadersBundle.putString("userAnswers",
                     Integer.toString(correctAnswers.size()));
             if (isCancelled())
                 return null;
             ApplicationEx.dbHelper.clearLeaders();
             ArrayList<String> devList = new ArrayList<String>();
             devList.add("unPF5wRxnK");
             devList.add("LuzjEBVnC8");
             devList.add("8aLb2I0fQA");
             devList.add("krEPKBuzFN");
             devList.add("k5VoRhL5BQ");
             devList.add("9LvKnpSEqu");
             if (isCancelled())
                 return null;
             ParseQuery hintsQuery = new ParseQuery("CorrectAnswers");
             hintsQuery.whereEqualTo("userId", userId)
                       .whereEqualTo("hint", true);
             try {
                 hints = hintsQuery.count();
                 leadersBundle.putString("userHints", Integer.toString(
                         hints));
             } catch (ParseException e) {
                 if (e.getCode() == 101)
                     leadersBundle.putString("userHints", Integer.toString(
                             hints));
                 else {
                     error = e;
                     publishProgress();
                 }
             }
             if (isCancelled())
                 return null;
             ParseQuery leadersQuery = ParseUser.getQuery();
             leadersQuery.whereExists("displayName").whereExists("score")
                     .whereNotContainedIn("objectId", devList)
                     .orderByDescending("score").setLimit(50);
             try {
                 getLeaders(leadersQuery.find());
             } catch (ParseException e) {
                 error = e;
                 publishProgress();
             }
             if (isCancelled())
                 return null;
             ParseQuery questionQuery = new ParseQuery("Question");
             questionQuery.orderByDescending("createdAt");
             try {
                 ParseObject question = questionQuery.getFirst();
                 Date questionDate = question.getCreatedAt();
                 Calendar cal = new GregorianCalendar(
                         TimeZone.getTimeZone("GMT"));
                 cal.setTime(questionDate);
                 cal.setTimeZone(TimeZone.getDefault());
                 String lastQuestionDate = DateFormat.getDateFormat(
                         ApplicationEx.getApp()).format(cal.getTime());
                 leadersBundle.putString("lastQuestion",
                         lastQuestionDate);
             } catch (ParseException e) {
                 error = e;
                 publishProgress();
             }
             if (!isCancelled()) {
                 inStats = true;
                 ApplicationEx.dbHelper.setUserValue(inStats ? 1 : 0,
                         DatabaseHelper.COL_IN_STATS, userId);
                 inLoad = false;
                 ApplicationEx.dbHelper.setUserValue(inLoad ? 1 : 0,
                         DatabaseHelper.COL_IN_LOAD, userId);
             }
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             if (error != null)
                 Log.e(Constants.LOG_TAG, "Error getting stats: " +
                         error.getMessage());
             currFrag.showNetworkProblem();
             if (getStatsTask != null)
                 getStatsTask.cancel(true);
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             showLeaders();
         }
         
         private void getLeaders(List<ParseObject> leaders) {
             rankList = new ArrayList<String>();
             userList = new ArrayList<String>();
             scoreList = new ArrayList<String>();
             userIdList = new ArrayList<String>();
             Number tempNum = 0;
             int tempInt = 0;
             int limit = 0;
             for (ParseObject leader : leaders) {
                 if (isCancelled())
                     return;
                 if (limit >= 50)
                     break;
                 userIdList.add(leader.getObjectId());
                 if (limit+1 < 10)
                     rankList.add("0" + Integer.toString(limit+1));
                 else
                     rankList.add(Integer.toString(limit+1));
                 userList.add(leader.getString("displayName"));
                 tempNum = leader.getNumber("score");
                 tempInt = tempNum == null ? 0 : tempNum.intValue();
                 scoreList.add(Integer.toString(tempInt));
                 if (leader.getObjectId().equals(userId))
                     leadersBundle.putString("userScore",
                             Integer.toString(tempInt));
                 limit++;
                 ApplicationEx.dbHelper.addLeader(userId,
                         rankList.get(rankList.size()-1),
                         userList.get(userList.size()-1),
                         scoreList.get(scoreList.size()-1),
                         userIdList.get(userIdList.size()-1));
             }
             leadersBundle.putStringArrayList("rank", rankList);
             leadersBundle.putStringArrayList("user", userList);
             leadersBundle.putStringArrayList("score", scoreList);
             leadersBundle.putStringArrayList("userIdList",
                     userIdList);
         }
     }
 
     @Override
     public Bundle getLeadersState() {
         return leadersBundle;
     }
     
     @Override
     public void saveUserScore(final int currTemp) {
         try {
             user.put("score", currTemp);
             user.saveEventually();
         }
         catch (RuntimeException e) {}
         ApplicationEx.dbHelper.setScore(currTemp, userId);
     }
     
     @Override
     public void getNextQuestions(boolean force) {
         if (getNextQuestionsTask != null)
             getNextQuestionsTask.cancel(true);
         getNextQuestionsTask = new GetNextQuestionsTask(force);
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             getNextQuestionsTask.execute();
         else
             getNextQuestionsTask.executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class GetNextQuestionsTask extends AsyncTask<Void, Void, Void> {
         int count = -1;
         List<ParseObject> questionList;
         ParseException error;
         boolean questionNull = false;
         ArrayList<String> stageList;
         boolean force;
         
         private GetNextQuestionsTask(boolean force) {
             this.force = force;
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             if (((nextQuestionId == null && thirdQuestionId != null) || 
                     correctAnswers != null &&
                         correctAnswers.contains(nextQuestionId)) &&
                             !isCancelled() && !force) {
                 updateIds();
                 getNextQuestions(force);
             }
             else if (!isCancelled()) {
                 if (!force)
                     updateIds();
                 publishProgress();
                 if (tempAnswers == null)
                     tempAnswers = new ArrayList<String>();
                 else
                     tempAnswers.clear();
                 tempAnswers.addAll(correctAnswers);
                 if (questionId != null)
                     tempAnswers.add(questionId);
                 if (nextQuestionId != null)
                     tempAnswers.add(nextQuestionId);
                 if (thirdQuestionId != null)
                     tempAnswers.add(thirdQuestionId);
                 ParseQuery query = new ParseQuery("Question");
                 query.whereNotContainedIn("objectId", tempAnswers);
                 try {
                     count = query.count();
                     if (count > 0 && !isCancelled()) {
                         stageList = new ArrayList<String>();
                         int skip = (int) (Math.random()*count);
                         query = new ParseQuery("Question");
                         query.whereNotContainedIn("objectId", correctAnswers);
                         if (questionId == null)
                             query.setLimit(3);
                         else {
                             if (nextQuestionId == null) {
                                 if (thirdQuestionId == null)
                                     query.setLimit(2);
                                 else
                                     query.setLimit(1);
                             }
                             else
                                 query.setLimit(1);
                         }
                         if (query.getLimit() > (count-skip))
                             skip = count-query.getLimit();
                         query.setSkip(skip);
                         // TODO set and fetch hint/skip values from new class in Parse
                         questionList = query.find();
                         if (!questionList.isEmpty() && !isCancelled()) {
                             Number score;
                             ParseObject followQuestion = null;
                             if (questionList.size() == 3) {
                                 ParseObject currQuestion = questionList.get(0);
                                 questionId = currQuestion.getObjectId();
                                 stageList.add(questionId);
                                 question = currQuestion.getString("question");
                                 correctAnswer = currQuestion.getString("answer");
                                 questionCategory = currQuestion.getString(
                                         "category");
                                 score = currQuestion.getNumber("score");
                                 questionScore = score == null ? "1011" :
                                         Integer.toString(score.intValue());
                                 currQuestion = questionList.get(1);
                                 nextQuestionId = currQuestion.getObjectId();
                                 stageList.add(nextQuestionId);
                                 nextQuestion = currQuestion.getString(
                                         "question");
                                 nextCorrectAnswer = currQuestion.getString(
                                         "answer");
                                 nextQuestionCategory = currQuestion.getString(
                                         "category");
                                 score = currQuestion.getNumber("score");
                                 nextQuestionScore = score == null ? "1011" :
                                         Integer.toString(score.intValue());
                                 followQuestion = questionList.get(2);
                             }
                             else if (questionList.size() == 2 &&
                                     !isCancelled()) {
                                 ParseObject currQuestion = questionList.get(0);
                                 if (questionId == null) {
                                     questionId = currQuestion.getObjectId();
                                     stageList.add(questionId);
                                     question = currQuestion.getString(
                                             "question");
                                     correctAnswer = currQuestion.getString(
                                             "answer");
                                     questionCategory = currQuestion.getString(
                                             "category");
                                     score = currQuestion.getNumber("score");
                                     questionScore = score == null ? "1011" :
                                     Integer.toString(score.intValue());
                                 }
                                 else {
                                     nextQuestionId = currQuestion.getObjectId();
                                     stageList.add(nextQuestionId);
                                     nextQuestion = currQuestion.getString(
                                             "question");
                                     nextCorrectAnswer = currQuestion.getString(
                                             "answer");
                                     nextQuestionCategory = 
                                             currQuestion.getString("category");
                                     score = currQuestion.getNumber("score");
                                     nextQuestionScore = score == null ? "1011" :
                                             Integer.toString(score.intValue());
                                 }
                                 followQuestion = questionList.get(1);
                             }
                             else {
                                 followQuestion = questionList.get(0);
                             }
                             if (questionId == null) {
                                 questionId = followQuestion.getObjectId();
                                 stageList.add(questionId);
                                 question = followQuestion.getString(
                                         "question");
                                 correctAnswer = followQuestion.getString(
                                         "answer");
                                 questionCategory = followQuestion.getString(
                                         "category");
                                 score = followQuestion.getNumber("score");
                                 questionScore = score == null ? "1011" :
                                         Integer.toString(score.intValue());
                             }
                             else if (nextQuestionId == null) {
                                 nextQuestionId = followQuestion.getObjectId();
                                 stageList.add(nextQuestionId);
                                 nextQuestion = followQuestion.getString(
                                         "question");
                                 nextCorrectAnswer = followQuestion.getString(
                                         "answer");
                                 nextQuestionCategory = followQuestion.getString(
                                         "category");
                                 score = followQuestion.getNumber("score");
                                 nextQuestionScore = score == null ? "1011" :
                                         Integer.toString(score.intValue());
                             }
                             else {
                                 thirdQuestionId = followQuestion.getObjectId();
                                 stageList.add(thirdQuestionId);
                                 thirdQuestion = followQuestion.getString(
                                         "question");
                                 thirdCorrectAnswer = followQuestion.getString(
                                         "answer");
                                 thirdQuestionCategory = followQuestion.getString(
                                         "category");
                                 score = followQuestion.getNumber("score");
                                 thirdQuestionScore = score == null ? "1011" :
                                         Integer.toString(score.intValue());
                             }
                             getStage(userId, stageList);
                             if (!isCancelled())
                                 ApplicationEx.dbHelper.setQuestions(userId,
                                         questionId, question, correctAnswer,
                                         questionCategory, questionScore,
                                         questionHint, questionSkip,
                                         nextQuestionId, nextQuestion,
                                         nextCorrectAnswer, nextQuestionCategory,
                                         nextQuestionScore, nextQuestionHint,
                                         nextQuestionSkip, thirdQuestionId,
                                         thirdQuestion, thirdCorrectAnswer,
                                         thirdQuestionCategory,
                                         thirdQuestionScore, thirdQuestionHint,
                                         thirdQuestionSkip);
                         }
                     }
                     else if (!isCancelled()) {
                         ApplicationEx.dbHelper.setQuestions(userId, questionId,
                                 question, correctAnswer, questionCategory,
                                 questionScore, questionHint, questionSkip,
                                 nextQuestionId, nextQuestion, nextCorrectAnswer,
                                 nextQuestionCategory, nextQuestionScore,
                                 nextQuestionHint, nextQuestionSkip,
                                 thirdQuestionId, thirdQuestion,
                                 thirdCorrectAnswer, thirdQuestionCategory,
                                 thirdQuestionScore, thirdQuestionHint,
                                 thirdQuestionSkip);
                     }
                 } catch (ParseException e) {
                     error = e;
                 }
             }
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         protected void onProgressUpdate(Void... nothing) {
             if (questionId != null && !isCancelled() && !force) {
                 if (!loggedIn) {
                     try {
                         goToQuiz();
                     } catch (IllegalStateException exception) {}
                 }
                 else
                     currFrag.resumeQuestion();
             }
             else if (questionId == null)
                 questionNull = true;
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             if (error != null && !isCancelled()) {
                 Log.e(Constants.LOG_TAG, "Error: " + error.getMessage());
                 if (userTask != null)
                     userTask.cancel(true);
                 currFrag.showNetworkProblem();
             }
             else if (!isCancelled() && questionNull) {
                 if (!loggedIn) {
                     try {
                         goToQuiz();
                     } catch (IllegalStateException exception) {}
                 }
                 else {
                     if (questionId == null)
                         currFrag.showNoMoreQuestions();
                     else
                         currFrag.resumeQuestion();
                 }
             }
         }
         
         private void updateIds() {
             questionId = nextQuestionId;
             nextQuestionId = thirdQuestionId;
             thirdQuestionId = null;
             question = nextQuestion != null ? nextQuestion.trim() :
                     nextQuestion;
             nextQuestion = thirdQuestion != null ?
                     thirdQuestion.trim() : thirdQuestion;
             thirdQuestion = null;
             correctAnswer = nextCorrectAnswer != null ?
                     nextCorrectAnswer.trim() : nextCorrectAnswer;
             nextCorrectAnswer = thirdCorrectAnswer != null ?
                     thirdCorrectAnswer.trim() : thirdCorrectAnswer;
             thirdCorrectAnswer = null;
             questionCategory = nextQuestionCategory != null ?
                     nextQuestionCategory.trim() :
                         nextQuestionCategory;
             nextQuestionCategory = thirdQuestionCategory != null ?
                     thirdQuestionCategory.trim() :
                         thirdQuestionCategory;
             thirdQuestionCategory = null;
             questionScore = nextQuestionScore;
             nextQuestionScore = thirdQuestionScore;
             thirdQuestionScore = null;
             questionHint = nextQuestionHint;
             nextQuestionHint = thirdQuestionHint;
             thirdQuestionHint = false;
         }
     }
     
     private void getStage(String userId, ArrayList<String> questionIds) {
         if (getStageTask != null)
             getStageTask.cancel(true);
         getStageTask = new GetStageTask(userId, questionIds);
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             getStageTask.execute();
         else
             getStageTask.executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class GetStageTask extends AsyncTask<Void, Void, Void> {
         private String userId;
         ArrayList<String> questionIds;
         
         private GetStageTask(String userId, ArrayList<String> questionIds) {
             this.userId = userId;
             this.questionIds = questionIds;
         }
         @Override
         protected Void doInBackground(Void... nothing) {
             ParseQuery query = new ParseQuery("Stage");
             query.whereEqualTo("userId", userId);
             query.whereContainedIn("questionId", questionIds);
             query.setLimit(questionIds.size());
             try {
                 List<ParseObject> questionList = query.find();
                 if (!questionList.isEmpty()) {
                     for (int i = 0; i < questionList.size(); i++) {
                         if (questionList.get(i).getString("questionId")
                                 .equals(questionId)) {
                             questionHint = questionList.get(i).getBoolean("hint");
                             questionSkip = questionList.get(i).getBoolean("skip");
                             publishProgress();
                         }
                         else if (questionList.get(i).getString("questionId")
                                 .equals(nextQuestionId)) {
                             nextQuestionHint = questionList.get(i)
                                     .getBoolean("hint");
                             nextQuestionSkip = questionList.get(i)
                                     .getBoolean("skip");
                             if (!questionIds.contains(questionId))
                                 publishProgress();
                         }
                         else if (questionList.get(i).getString("questionId")
                                 .equals(thirdQuestionId)) {
                             thirdQuestionHint = questionList.get(i)
                                     .getBoolean("hint");
                             thirdQuestionSkip = questionList.get(i)
                                     .getBoolean("skip");
                             if (!questionIds.contains(questionId) &&
                                     !questionIds.contains(nextQuestionId))
                                 publishProgress();
                         }
                     }
                 }
                 else {
                     for (int i = 0; i < questionIds.size(); i++) {
                         if (questionIds.get(i).equals(questionId)) {
                             questionHint = false;
                             questionSkip = false;
                             publishProgress();
                         }
                         else if (questionIds.get(i).equals(nextQuestionId)) {
                             nextQuestionHint = false;
                             nextQuestionSkip = false;
                             if (!questionIds.contains(questionId))
                                 publishProgress();
                         }
                         else if (questionIds.get(i).equals(thirdQuestionId)) {
                             thirdQuestionHint = false;
                             thirdQuestionSkip = false;
                             if (!questionIds.contains(questionId) &&
                                     !questionIds.contains(nextQuestionId))
                                 publishProgress();
                         }
                     }
                 }
             } catch (ParseException e) {
                 Log.e(Constants.LOG_TAG, "Error: " + e.getMessage());
                 if (currFrag != null)
                     currFrag.showNetworkProblem();
             }
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             if (!loggedIn) {
                 try {
                     goToQuiz();
                 } catch (IllegalStateException exception) {}
             }
             else
                 currFrag.resumeQuestion();
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             
         }
     }
     
     private class BackgroundTask extends AsyncTask<Void, Void, Void> {
         private String questionId;
         
         private BackgroundTask(String questionId) {
             this.questionId = questionId;
         }
         @Override
         protected Void doInBackground(Void... nothing) {
             if (questionId != null && correctAnswers.contains(questionId)) {
                 if (correctAnswers.size() % 20 == 0) {
                     if (currentBackground == null) {
                         if (userId != null) {
                             currentBackground =
                                 ApplicationEx.dbHelper.getCurrBackground(userId);
                             if (currentBackground == null)
                                 currentBackground = "splash8";
                         }
                         else
                             currentBackground = "splash8";
                         
                     }
                     publishProgress();
                 }
             }
             return null;
         }
         
         protected void onProgressUpdate(Void... nothing) {
             setBackground(currentBackground, true);
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
             
         }
     }
     
     @Override
     public void next() {
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             new BackgroundTask(questionId).execute();
         else
             new BackgroundTask(questionId).executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
         newQuestion = false;
         ApplicationEx.dbHelper.setUserValue(newQuestion ? 1 : 0,
                 DatabaseHelper.COL_NEW_QUESTION, userId);
         getNextQuestions(false);
     }
 
     @Override
     public String getQuestionId() {
         return questionId;
     }
 
     @Override
     public String getQuestion() {
         return question;
     }
 
     @Override
     public String getCorrectAnswer() {
         return correctAnswer;
     }
 
     @Override
     public String getQuestionScore() {
         return questionScore;
     }
 
     @Override
     public String getQuestionCategory() {
         return questionCategory;
     }
     
     @Override
     public boolean getQuestionHint() {
         return questionHint;
     }
     
     @Override
     public boolean getQuestionSkip() {
         return questionSkip;
     }
 
     @Override
     public String getNextQuestionId() {
         return nextQuestionId;
     }
 
     @Override
     public String getNextQuestion() {
         return nextQuestion;
     }
 
     @Override
     public String getNextCorrectAnswer() {
         return nextCorrectAnswer;
     }
 
     @Override
     public String getNextQuestionScore() {
         return nextQuestionScore;
     }
 
     @Override
     public String getNextQuestionCategory() {
         return nextQuestionCategory;
     }
     
     @Override
     public boolean getNextQuestionHint() {
         return nextQuestionHint;
     }
     
     @Override
     public boolean getNextQuestionSkip() {
         return nextQuestionSkip;
     }
     
     @Override
     public String getThirdQuestionId() {
         return thirdQuestionId;
     }
 
     @Override
     public String getThirdQuestion() {
         return thirdQuestion;
     }
 
     @Override
     public String getThirdCorrectAnswer() {
         return thirdCorrectAnswer;
     }
 
     @Override
     public String getThirdQuestionScore() {
         return thirdQuestionScore;
     }
 
     @Override
     public String getThirdQuestionCategory() {
         return thirdQuestionCategory;
     }
     
     @Override
     public boolean getThirdQuestionHint() {
         return thirdQuestionHint;
     }
     
     @Override
     public boolean getThirdQuestionSkip() {
         return thirdQuestionSkip;
     }
 
     @Override
     public void setQuestionId(String questionId) {
         this.questionId = questionId;
     }
 
     @Override
     public void setQuestion(String question) {
         this.question = question;
     }
 
     @Override
     public void setCorrectAnswer(String correctAnswer) {
         this.correctAnswer = correctAnswer;
     }
 
     @Override
     public void setQuestionScore(String questionScore) {
         this.questionScore = questionScore;
     }
 
     @Override
     public void setQuestionCategory(String questionCategory) {
         this.questionCategory = questionCategory;
     }
     
     @Override
     public void setQuestionHint(boolean questionHint) {
         this.questionHint = questionHint;
     }
 
     @Override
     public void setNextQuestionId(String nextQuestionId) {
         this.nextQuestionId = nextQuestionId;
     }
 
     @Override
     public void setNextQuestion(String nextQuestion) {
         this.nextQuestion = nextQuestion;
     }
 
     @Override
     public void setNextCorrectAnswer(String nextCorrectAnswer) {
         this.nextCorrectAnswer = nextCorrectAnswer;
     }
 
     @Override
     public void setNextQuestionScore(String nextQuestionScore) {
         this.nextQuestionScore = nextQuestionScore;
     }
 
     @Override
     public void setNextQuestionCategory(String nextQuestionCategory) {
         this.nextQuestionCategory = nextQuestionCategory;
     }
     
     @Override
     public void setNextQuestionHint(boolean nextQuestionHint) {
         this.nextQuestionHint = nextQuestionHint;
     }
     
     @Override
     public void setThirdQuestionId(String thirdQuestionId) {
         this.thirdQuestionId = thirdQuestionId;
     }
 
     @Override
     public void setThirdQuestion(String thirdQuestion) {
         this.thirdQuestion = thirdQuestion;
     }
 
     @Override
     public void setThirdCorrectAnswer(String thirdCorrectAnswer) {
         this.thirdCorrectAnswer = thirdCorrectAnswer;
     }
 
     @Override
     public void setThirdQuestionScore(String thirdQuestionScore) {
         this.thirdQuestionScore = thirdQuestionScore;
     }
 
     @Override
     public void setThirdQuestionCategory(String thirdQuestionCategory) {
         this.thirdQuestionCategory = thirdQuestionCategory;
     }
     
     @Override
     public void setThirdQuestionHint(boolean thirdQuestionHint) {
         this.thirdQuestionHint = thirdQuestionHint;
     }
 
     @Override
     public String getUserId() {
         return userId;
     }
 
     @Override
     public String getDisplayName() {
         return displayName;
     }
 
     @Override
     public boolean isNewQuestion() {
         return newQuestion;
     }
 
     @Override
     public void setIsNewQuestion(boolean isNewQuestion) {
         newQuestion = isNewQuestion;
         ApplicationEx.dbHelper.setUserValue(newQuestion ? 1 : 0,
                 DatabaseHelper.COL_NEW_QUESTION, userId);
     }
 
     @Override
     public int getCurrentScore() {
         return currScore;
     }
 
     @Override
     public void addCurrentScore(int addValue) {
         currScore += addValue;
         saveUserScore(currScore);
     }
 
     @Override
     public void shareScreenshot() {
         String path = takeScreenshot();
         Intent share = new Intent(Intent.ACTION_SEND);
         share.setType("image/jpeg");
 
         share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
         
         PackageManager packageManager = getPackageManager();
         List<ResolveInfo> activities =
                 packageManager.queryIntentActivities(share, 0);
         boolean isIntentSafe = activities.size() > 0;
         if (isIntentSafe)
             startActivity(Intent.createChooser(share, "Share screenshot with"));
     }
     
     @Override
     public void setDisplayName(String displayName) {
         currFrag.setDisplayName(displayName);
         this.displayName = displayName;
         if (user != null) {
             user.put("displayName", displayName);
             try {
                 user.saveEventually();
             }
             catch (RuntimeException e) {}
         }
         if (userId != null)
             ApplicationEx.dbHelper.setUserValue(displayName, 
                     DatabaseHelper.COL_DISPLAY_NAME, userId);
     }
     
     private String takeScreenshot() {
         Calendar cal = Calendar.getInstance(TimeZone.getDefault(),
                 Locale.getDefault());
         int calendarMonth = cal.get(Calendar.MONTH)+1;
         int calendarDate = cal.get(Calendar.DATE);
         int calendarHour = cal.get(Calendar.HOUR_OF_DAY);
         int calendarMinute = cal.get(Calendar.MINUTE);
         int calendarSecond = cal.get(Calendar.SECOND);
         String fileName = cal.get(Calendar.YEAR) + "_" + (calendarMonth < 10 ?
                 ("0" + calendarMonth) : calendarMonth) + "_" +
                 (calendarDate < 10 ? ("0" + calendarDate) : calendarDate) +
                 "_" + (calendarHour < 10 ? ("0" + calendarHour) : calendarHour)
                 + "_" + (calendarMinute < 10 ? ("0" + calendarMinute) :
                 calendarMinute) + "_" + (calendarSecond < 10 ?
                 ("0" + calendarSecond) : calendarSecond) + ".jpg";
         String path = ApplicationEx.cacheLocation + Constants.SCREENS_LOCATION +
                 fileName;
         
         Bitmap bitmap;
         View v1 = background.getRootView();
         v1.setDrawingCacheEnabled(true);
         bitmap = Bitmap.createBitmap(v1.getDrawingCache());
         v1.setDrawingCacheEnabled(false);
 
         OutputStream fout = null;
         File imageFile = new File(path);
 
         try {
             fout = new FileOutputStream(imageFile);
             bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
             fout.flush();
             fout.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return path;
     }
     
     private void getPersistedData(String userId) {
         loggedIn = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_LOGGED_IN, userId) == 1 ? true : false;
         isLogging = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_LOGGING, userId) == 1 ? true : false;
         inLoad = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_IN_LOAD, userId) == 1 ? true : false;
         inStats = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_IN_STATS, userId) == 1 ? true : false;
         inInfo = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_IN_INFO, userId) == 1 ? true : false;
         getUserData(userId);
         networkProblem = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_NETWORK_PROBLEM, userId) == 1 ? true : false;
     }
     
     private void getUserData(String userId) {
         currentBackground = ApplicationEx.dbHelper.getUserStringValue(
                 DatabaseHelper.COL_CURR_BACKGROUND, userId);
         questionId = ApplicationEx.dbHelper.getCurrQuestionId(userId);
         question = ApplicationEx.dbHelper.getCurrQuestionQuestion(userId);
         correctAnswer = ApplicationEx.dbHelper.getCurrQuestionAnswer(userId);
         questionScore = ApplicationEx.dbHelper.getCurrQuestionScore(userId);
         questionCategory =
                 ApplicationEx.dbHelper.getCurrQuestionCategory(userId);
         questionHint = ApplicationEx.dbHelper.getCurrQuestionHint(userId);
         questionSkip = ApplicationEx.dbHelper.getCurrQuestionSkip(userId);
         nextQuestionId = ApplicationEx.dbHelper.getNextQuestionId(userId);
         nextQuestion = ApplicationEx.dbHelper.getNextQuestionQuestion(userId);
         nextCorrectAnswer =
                 ApplicationEx.dbHelper.getNextQuestionAnswer(userId);
         nextQuestionScore = ApplicationEx.dbHelper.getNextQuestionScore(userId);
         nextQuestionCategory =
                 ApplicationEx.dbHelper.getNextQuestionCategory(userId);
         nextQuestionHint = ApplicationEx.dbHelper.getNextQuestionHint(userId);
         nextQuestionSkip = ApplicationEx.dbHelper.getNextQuestionSkip(userId);
         thirdQuestionId = ApplicationEx.dbHelper.getThirdQuestionId(userId);
         thirdQuestion = ApplicationEx.dbHelper.getThirdQuestionQuestion(userId);
         thirdCorrectAnswer =
                 ApplicationEx.dbHelper.getThirdQuestionAnswer(userId);
         thirdQuestionScore = ApplicationEx.dbHelper.getThirdQuestionScore(
                 userId);
         thirdQuestionCategory =
                 ApplicationEx.dbHelper.getThirdQuestionCategory(userId);
         thirdQuestionHint = ApplicationEx.dbHelper.getThirdQuestionHint(userId);
         thirdQuestionSkip = ApplicationEx.dbHelper.getThirdQuestionSkip(userId);
         newQuestion = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_NEW_QUESTION, userId) == 1 ? true : false;
         displayName = ApplicationEx.dbHelper.getUserStringValue(
                 DatabaseHelper.COL_DISPLAY_NAME, userId);
         currScore = ApplicationEx.dbHelper.getUserIntValue(
                 DatabaseHelper.COL_SCORE, userId);
         correctAnswers = ApplicationEx.getStringArrayPref(
                 getString(R.string.correct_key));
     }
     
     private class ConnectionReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equalsIgnoreCase(
                     Constants.ACTION_CONNECTION)) {
                 if (intent.hasExtra("hasConnection")) {
                     if (!intent.getBooleanExtra("hasConnection", false))
                         currFrag.showNetworkProblem();
                 }
             }
         }
     }
 
     @Override
     public boolean getNetworkProblem() {
         return networkProblem;
     }
 
     @Override
     public void setNetworkProblem(boolean networkProblem) {
         this.networkProblem = networkProblem;
     }
 
     @Override
     public void addCorrectAnswer(String correctId) {
         if (correctAnswers == null) {
             correctAnswers = new ArrayList<String>();
             correctAnswers.add(correctId);
         }
         else
             correctAnswers.add(correctId);
         ApplicationEx.setStringArrayPref(
                 getString(R.string.correct_key), correctAnswers);
     }
     
     @Override
     public boolean isCorrectAnswer(String correctId) {
         if (correctAnswers == null)
             return false;
         else {
             if (correctAnswers.contains(correctId))
                 return true;
             else
                 return false;
         }
     }
 
     @Override
     public void setUserName(String userName) {
         if (leadersBundle != null && userName != null)
             leadersBundle.putString("userName", userName);
     }
     
     @Override
     public boolean isNewUser() {
         return newUser;
     }
     
 }
