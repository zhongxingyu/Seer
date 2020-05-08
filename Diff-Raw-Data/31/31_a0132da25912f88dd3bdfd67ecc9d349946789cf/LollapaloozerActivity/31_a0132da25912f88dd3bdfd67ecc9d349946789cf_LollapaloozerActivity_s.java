 package com.lollapaloozer.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.lollapaloozer.LollapaloozerServiceUtils;
 import com.lollapaloozer.LollapaloozerStorageManager;
 import com.lollapaloozer.R;
 import com.lollapaloozer.auth.client.AuthChooseAccountActivity;
 import com.lollapaloozer.data.CustomSetListAdapter;
 import com.lollapaloozer.data.JSONArrayHashMap;
 import com.lollapaloozer.data.JSONArraySortMap;
 import com.lollapaloozer.data.LoginData;
 import com.lollapaloozer.util.LollapaloozerHelper;
 import com.ratethisfest.shared.Constants;
 import com.ratethisfest.shared.FieldVerifier;
 import com.ratethisfest.shared.HttpConstants;
 
 public class LollapaloozerActivity extends Activity implements View.OnClickListener,
     OnItemSelectedListener, OnItemClickListener, OnCheckedChangeListener {
 
   private static final int DIALOG_RATE = 1;
   private static final int DIALOG_GETEMAIL = 2;
   private static final int DIALOG_NETWORK_ERROR = 3;
   private static final int DIALOG_FIRST_USE = 4;
 
   private static final int THREAD_UPDATE_UI = 1;
   private static final int THREAD_SUBMIT_RATING = 2;
 
   private static final int SORT_TIME = 1;
   private static final int SORT_ARTIST = 2;
 
   // Local Storage
   private static final String DATA_SETS = "DATA_SETS";
   private static final String DATA_RATINGS = "DATA_RATINGS";
   private static final String DATA_FIRST_USE = "DATA_FIRST_USE";
   private static final String DATA_LOGIN_INFO = "DATA_LOGIN_INFO";
 
   // Database
   public static final String QUERY_RATINGS__SET_ID = "set_id";
   public static final String QUERY_RATINGS__WEEK = "weekend";
   public static final String QUERY_SETS__SET_ID = "id";
   public static final String QUERY_SETS__DAY = "day";
   public static final String QUERY_SETS__TIME_ONE = "time_one";
   public static final String QUERY_SETS__STAGE_ONE = "stage_one";
   public static final String QUERY_RATINGS__RATING = "score";
   public static final String QUERY_RATINGS__NOTES = "notes";
 
   private static final int REFRESH_INTERVAL__SECONDS = 15;
 
   private int _yearToQuery;
   private String _dayToExamine;
   private int _sortMode;
   private long _lastRefresh = 0;
 
   private String _timeFieldName;
   private String _stageFieldName;
 
   private Dialog _rateDialog;
   private Dialog _getEmailDialog;
   private Dialog _networkErrorDialog;
   private Dialog _firstUseDialog;
 
   private CustomSetListAdapter _setListAdapter;
   private JSONObject _lastItemSelected;
   // private CustomPair<String, String> _lastRatings = new CustomPair<String,
   // String>(
   // null, null);
 
   private JSONObject _lastRatings;
 
   private HashMap<Integer, Integer> _ratingSelectedIdToValue = new HashMap<Integer, Integer>();
   private HashMap<String, Integer> _ratingSelectedScoreToId = new HashMap<String, Integer>();
 
   private LollapaloozerStorageManager _storageManager;
   private int _ratingSelectedScore;
 
   private JSONArrayHashMap _myRatings_JAHM = new JSONArrayHashMap(QUERY_RATINGS__SET_ID,
       QUERY_RATINGS__WEEK);
   private boolean _networkErrors;
 
   private Handler _networkIOHandler;
   private LoginData _loginData;
 
   /** Called by Android Framework when the activity is first created. */
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
 
     LollapaloozerHelper.debug(this, "LollapaloozerActivity Launched");
 
     _ratingSelectedIdToValue.put(R.id.radio_button_score1, 1);
     _ratingSelectedIdToValue.put(R.id.radio_button_score2, 2);
     _ratingSelectedIdToValue.put(R.id.radio_button_score3, 3);
     _ratingSelectedIdToValue.put(R.id.radio_button_score4, 4);
     _ratingSelectedIdToValue.put(R.id.radio_button_score5, 5);
 
     _ratingSelectedScoreToId.put("1", R.id.radio_button_score1);
     _ratingSelectedScoreToId.put("2", R.id.radio_button_score2);
     _ratingSelectedScoreToId.put("3", R.id.radio_button_score3);
     _ratingSelectedScoreToId.put("4", R.id.radio_button_score4);
     _ratingSelectedScoreToId.put("5", R.id.radio_button_score5);
 
     _storageManager = new LollapaloozerStorageManager(this);
     _storageManager.load();
 
     // initJAHM(); //Only initialized once
 
     setContentView(R.layout.sets_list);
     _setListAdapter = new CustomSetListAdapter(this, QUERY_SETS__TIME_ONE, QUERY_SETS__STAGE_ONE,
         _myRatings_JAHM);
     _setListAdapter.setData(new JSONArray());
 
     ListView viewSetsList = (ListView) findViewById(R.id.viewSetsList);
     viewSetsList.setAdapter(_setListAdapter);
     viewSetsList.setOnItemClickListener(this);
 
     Button buttonSearchSets = (Button) this.findViewById(R.id.buttonChangeToSearchSets);
     buttonSearchSets.setOnClickListener(this);
 
     Spinner spinnerSortType = (Spinner) findViewById(R.id.spinner_sort_by);
     LollapaloozerHelper.populateSpinnerWithArray(spinnerSortType, R.array.search_types);
     spinnerSortType.setOnItemSelectedListener(this);
 
     _yearToQuery = LollapaloozerHelper.whatYearIsToday();
     _dayToExamine = LollapaloozerHelper.whatDayIsToday();
     _sortMode = SORT_TIME;
 
     // Above here is stuff to be done once
 
     _networkIOHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
 
         super.handleMessage(msg);
 
         if (msg.what == THREAD_UPDATE_UI) {
           System.out.println("Executing update UI thread callback ");
           redrawUI();
         }
 
         if (msg.what == THREAD_SUBMIT_RATING) {
           System.out.println("Executing submit rating thread callback ");
           String lastNote = "";
 
           try {
             if (_lastRatings.has(QUERY_RATINGS__NOTES)) {
               lastNote = _lastRatings.getString(QUERY_RATINGS__NOTES);
             }
             doSubmitRating(_lastRatings.get(QUERY_RATINGS__RATING) + "", lastNote);
           } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
           }
         }
       }
 
     };
 
     processExtraData();
 
   }
 
   protected void redrawUI() {
     try {
       setView_reSort();
 
     } catch (JSONException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
     ListView viewSetsList = (ListView) findViewById(R.id.viewSetsList);
     viewSetsList.invalidateViews();
 
     LollapaloozerHelper.debug(this, "Data Refresh is complete");
     _lastRefresh = System.currentTimeMillis();
 
     if (!_networkErrors) {
       _storageManager.save(); // TODO only if both sets and ratings were
       // retrieved
 
     } else {
       showDialog(DIALOG_NETWORK_ERROR);
     }
   }
 
   /** Called by Android Framework when activity (re)gains foreground status */
   public void onResume() {
     super.onResume();
 
     String firstUse = _storageManager.getString("DATA_FIRST_USE");
     // TODO: We'll reenable this if we have something significant to say in the
     // beginning
     // if (firstUse == null || firstUse.equals("true")) {
     // showDialog(DIALOG_FIRST_USE);
     // } else {
     _showClickToRate();
     // }
 
     if ((System.currentTimeMillis() - _lastRefresh) / 1000 > REFRESH_INTERVAL__SECONDS) {
       refreshData(); // TODO multi-thread this
     }
 
   }
 
   private void _showClickToRate() {
     Toast clickToRate = Toast.makeText(this, "Tap any set to rate it!", 20);
     clickToRate.show();
   }
 
   private void _obtainLoginDataFromStorage() {
     _loginData = (LoginData) _storageManager.getObject(DATA_LOGIN_INFO);
   }
 
   // Only called once, so commented out.
   /*
    * private void initJAHM() { LollapaloozerHelper.debug(this, "initJAHM()");
    * _myRatings_JAHM = new JSONArrayHashMap(QUERY_RATINGS__SET_ID,
    * QUERY_RATINGS__WEEK); }
    */
 
   private void refreshData() {
     // Below here is stuff to be done each refresh
     _networkErrors = false;
 
     if (!_dayToExamine.equals("Friday") && !_dayToExamine.equals("Saturday")
         && !_dayToExamine.equals("Sunday")) {
       _dayToExamine = "Friday";
     }
 
     _timeFieldName = QUERY_SETS__TIME_ONE;
     _stageFieldName = QUERY_SETS__STAGE_ONE;
 
     TextView titleView = (TextView) this.findViewById(R.id.text_set_list_title);
     titleView.setText(_dayToExamine + " - Lollapalooza " + _yearToQuery);
     // +" "+ weekString);
     _setListAdapter.setTimeFieldName(_timeFieldName);
     _setListAdapter.setStageFieldName(_stageFieldName);
 
     _obtainLoginDataFromStorage();
 
     launchGetDataThread(); // TODO multithread this
   }
 
   private void launchGetDataThread() {
     LollapaloozerHelper.debug(this, "Launching getData thread");
 
     new Thread() {
       public void run() {
         try {
           doNetworkOperations();
         } catch (JSONException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
         _networkIOHandler.sendEmptyMessage(THREAD_UPDATE_UI);
       }
     }.start();
     LollapaloozerHelper.debug(this, "getData thread launched");
   }
 
   private void launchSubmitRatingThread() {
     LollapaloozerHelper.debug(this, "Launching Rating thread");
 
     new Thread() {
       public void run() {
         try {
           doNetworkOperations();
         } catch (JSONException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
         Message msgToSend = Message.obtain(_networkIOHandler, THREAD_SUBMIT_RATING);
         msgToSend.sendToTarget();
       }
     }.start();
     LollapaloozerHelper.debug(this, "Rating thread launched");
 
   }
 
   public void doNetworkOperations() throws JSONException {
 
     LollapaloozerHelper.debug(this, "rebuildJAHM()");
     if (_isLoggedIn()) { // Get my ratings
 
       JSONArray myRatings = null;
       try {
         // TODO: year can remain hardcoded for now (to force users to
         // update app
         // in future)
 
         List<NameValuePair> params = new ArrayList<NameValuePair>();
         params.add(new BasicNameValuePair(HttpConstants.PARAM_YEAR, "2012"));
         params.add(new BasicNameValuePair(HttpConstants.PARAM_YEAR, "2012"));
         params.add(new BasicNameValuePair(HttpConstants.PARAM_DAY, _dayToExamine));
         params
             .add(new BasicNameValuePair(HttpConstants.PARAM_AUTH_TYPE, _loginData.loginType + ""));
         params
             .add(new BasicNameValuePair(HttpConstants.PARAM_AUTH_ID, _loginData.accountIdentifier));
         params.add(new BasicNameValuePair(HttpConstants.PARAM_AUTH_TOKEN, _loginData.accountToken));
         if (_loginData.emailAddress != null) {
           params.add(new BasicNameValuePair(HttpConstants.PARAM_EMAIL, _loginData.emailAddress));
         }
         myRatings = LollapaloozerServiceUtils.getRatings(params, this);
 
         _storageManager.putJSONArray(DATA_RATINGS, myRatings);
       } catch (Exception e1) {
         _networkErrors = true;
         LollapaloozerHelper.debug(this,
             "Exception getting Ratings data, loading from storage if available");
         try {
           myRatings = _storageManager.getJSONArray(DATA_RATINGS);
         } catch (JSONException e) {
           e.printStackTrace();
           LollapaloozerHelper.debug(this, "JSONException loading ratings from storage");
         }
       }
 
       try {
         // TODO this may not be correct. JAHM should only be initialized
         // once.
         // _myRatings_JAHM = new JSONArrayHashMap(myRatings,
         // QUERY_RATINGS__SET_ID, QUERY_RATINGS__WEEK);
 
         if (myRatings == null) {
           LollapaloozerHelper.debug(this, "Had to initialize ratings data JSONArray");
           myRatings = new JSONArray();
         }
 
         _myRatings_JAHM.rebuildDataWith(myRatings);
 
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         // Could not get my ratings :(
         e.printStackTrace();
       }
     } else {
       // TODO This may not be correct. initJAHM() may have already been
       // called
       // on startup and
       // TODO the reference to the JAHM may have been passed to the
       // adapter
       // already
       // initJAHM(); //Commented in hope of addressing crash issue
 
       // Need to wipe out ratings if email was just deleted
       _myRatings_JAHM.wipeData();
     }
 
     // New strategy does not re-instantiate this object, this line should
     // not be
     // needed
     // _setListAdapter.setNewJAHM(_myRatings_JAHM);
 
     JSONArray setData = null;
     try {
       // TODO: pass proper values (year can remain hard-coded for now)
       List<NameValuePair> params = new ArrayList<NameValuePair>();
       params.add(new BasicNameValuePair(HttpConstants.PARAM_YEAR, "2012"));
       params.add(new BasicNameValuePair(HttpConstants.PARAM_DAY, _dayToExamine));
       setData = LollapaloozerServiceUtils.getSets(params, this);
 
       _storageManager.putJSONArray(DATA_SETS, setData);
     } catch (Exception e) {
       _networkErrors = true;
       LollapaloozerHelper.debug(this,
           "Exception getting Set data, loading from storage if available");
       setData = _storageManager.getJSONArray(DATA_SETS);
     }
 
     if (setData == null) {
       LollapaloozerHelper.debug(this, "Had to initialize set data JSONArray");
       setData = new JSONArray();
     }
     _setListAdapter.setData(setData);
   }
 
   public void doSubmitRating(String scoreSelectedValue, String notes) {
     // submit rating
     // If Exception is thrown, do not store rating locally
     try {
       String set_id = _lastItemSelected.get(QUERY_SETS__SET_ID) + "";
 
       List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
       nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_SET_ID, set_id));
       nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_SCORE, scoreSelectedValue));
       nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_NOTES, notes));
       nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_AUTH_TYPE, _loginData.loginType
           + ""));
       nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_AUTH_ID,
           _loginData.accountIdentifier));
       nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_AUTH_TOKEN,
           _loginData.accountToken));
       if (_loginData.emailAddress != null) {
         nameValuePairs.add(new BasicNameValuePair(HttpConstants.PARAM_EMAIL,
             _loginData.emailAddress));
       }
 
       LollapaloozerServiceUtils.addRating(nameValuePairs, this);
 
       // Need this in order to make the new rating appear in real time
 
       try {
         // CRITICAL that the keys are listed in this order
         _myRatings_JAHM.addValues(QUERY_RATINGS__SET_ID, QUERY_RATINGS__WEEK, _lastRatings);
 
         // Don't need since we are refreshing
         // ListView viewSetsList = (ListView)
         // findViewById(R.id.viewSetsList);
         // viewSetsList.invalidateViews();
 
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
 
       refreshData(); // If this is removed, uncomment ListView and
       // invalidate
       // above ^^^
 
     } catch (Exception e1) {
       LollapaloozerHelper.debug(this, "Error submitting rating");
       e1.printStackTrace();
       showDialog(DIALOG_NETWORK_ERROR);
     }
   }
 
   // An item was selected from the list of sets
   @Override
   public void onItemSelected(AdapterView<?> parent, View arg1, int arg2, long arg3) {
 
     // TODO Auto-generated method stub
     LollapaloozerHelper.debug(this, "Search Type Spinner: Selected -> " + parent.getSelectedItem()
         + "(" + arg2 + ")");
     ListView viewSetsList = (ListView) findViewById(R.id.viewSetsList);
 
     try {
       if (parent.getSelectedItem().toString().toLowerCase().equals("time")) {
         _sortMode = SORT_TIME;
 
       } else if (parent.getSelectedItem().toString().toLowerCase().equals("artist")) {
         _sortMode = SORT_ARTIST;
       }
 
       setView_reSort();
       viewSetsList.invalidateViews();
     } catch (JSONException e) {
       LollapaloozerHelper.debug(this, "JSONException re-sorting data");
       e.printStackTrace();
     }
   }
 
   private void setView_reSort() throws JSONException {
     if (_sortMode == SORT_TIME) {
       _setListAdapter.sortByField(_timeFieldName, JSONArraySortMap.VALUE_INTEGER);
     } else if (_sortMode == SORT_ARTIST) {
       _setListAdapter.sortByField("artist", JSONArraySortMap.VALUE_STRING);
     } else {
       LollapaloozerHelper.debug(this, "Unexpected sort mode: " + _sortMode);
       (new Exception()).printStackTrace();
     }
   }
 
   @Override
   public void onNothingSelected(AdapterView<?> arg0) {
     LollapaloozerHelper.debug(this, "Search Type Spinner: Nothing Selected");
     Spinner spinnerSortType = (Spinner) findViewById(R.id.spinner_sort_by);
     spinnerSortType.setSelection(0);
   }
 
   // Any button in any view or dialog was clicked
   @Override
   public void onClick(View viewClicked) {
 
     // OK clicked on first use dialog
     if (viewClicked.getId() == R.id.button_firstuse_ok) {
       _storageManager.putString(DATA_FIRST_USE, false + "");
       _storageManager.save();
       _firstUseDialog.dismiss();
       _showClickToRate(); // display 'tap set to rate it' toast
     }
 
     // "OK" clicked to submit email address
     if (viewClicked.getId() == R.id.button_provideEmail) {
       EditText emailField = (EditText) _getEmailDialog.findViewById(R.id.textField_enterEmail);
       String email = emailField.getText().toString();
 
       LollapaloozerHelper.debug(this, "User provided email address: " + email);
 
       // if
       // (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
       if (!FieldVerifier.isValidEmail(email)) {
         Toast invalidEmail = Toast.makeText(this, "Please enter your real email address.", 25);
         invalidEmail.show();
 
       } else { // Email is valid. Save email and email ratings
 
         _loginData.emailAddress = email;
         _storageManager.save();
         _getEmailDialog.dismiss();
         try {
           System.out.println("Requesting ratings email.");
           String result = LollapaloozerServiceUtils.emailMyRatings(this, _loginData.emailAddress);
 
         } catch (Exception e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
       }
     }
 
     if (viewClicked.getId() == R.id.button_declineEmail) {
       _getEmailDialog.dismiss();
 
     }
 
     if (viewClicked.getId() == R.id.buttonChangeToSearchSets) {
       System.out.println("Button: Switch Day");
       Intent intent = new Intent();
       intent.setClass(this, ActivitySetsSearch.class);
       startActivity(intent);
     }
 
     // Submit rating for a set
     if (viewClicked.getId() == R.id.button_rate_okgo) { // Selections
       // incomplete
       RadioGroup scoreGroup = (RadioGroup) _rateDialog.findViewById(R.id.radio_pick_score);
       int scoreSelectedId = scoreGroup.getCheckedRadioButtonId();
 
       if (scoreSelectedId == -1) {
         Toast selectEverything = Toast.makeText(this, "Please select a rating for this Set", 25);
         selectEverything.show();
 
       } else { // Selections are valid
 
         // _ratingSelectedScore = _ratingSelectedIdToValue.get(scoreSelectedId);
 
         // LollapaloozerHelper.debug(this, "Score[" + _ratingSelectedScore +
         // "] ScoreId[" + scoreSelectedId + "]");
 
         // scoreGroup.clearCheck();
 
         String submittedRating = _ratingSelectedIdToValue.get(scoreGroup.getCheckedRadioButtonId())
             .toString();
 
         EditText noteWidget = (EditText) _rateDialog.findViewById(R.id.editText_commentsForSet);
         String submittedNote = noteWidget.getText().toString();
 
         try {
           if (_lastRatings == null) {
             JSONObject newObj = new JSONObject();
             newObj.put(QUERY_RATINGS__SET_ID, _lastItemSelected.get(QUERY_SETS__SET_ID));
             newObj.put(QUERY_RATINGS__WEEK, "1"); // TODO leave in
             // hard-coded for
             // now
             // newObj.put(QUERY_RATINGS__RATING, submittedRating);
             // newObj.put(QUERY_RATINGS__NOTES, notes);
             _lastRatings = newObj;
           }
 
           _lastRatings.put(QUERY_RATINGS__RATING, submittedRating);
           _lastRatings.put(QUERY_RATINGS__NOTES, submittedNote);
           launchSubmitRatingThread();
           _rateDialog.dismiss();
         } catch (JSONException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
 
       }
     } // End rating dialog submitted
 
     if (viewClicked.getId() == R.id.button_rate_cancel) {
       // Dialog dialog = (Dialog) viewClicked.getParent();
       RadioGroup scoreGroup = (RadioGroup) _rateDialog.findViewById(R.id.radio_pick_score);
       // scoreGroup.clearCheck();
       _rateDialog.dismiss();
     }
 
     if (viewClicked.getId() == R.id.button_network_error_ok) {
       _networkErrorDialog.dismiss();
     }
 
   }
 
   // An item in the ListView of sets is clicked
   @Override
   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
     try {// TODO Hard coded strings means you are going to hell
       JSONObject obj = (JSONObject) _setListAdapter.getItem(position);
       _lastItemSelected = obj;
 
       String setId = _lastItemSelected.getString(QUERY_SETS__SET_ID);
       _lastRatings = _myRatings_JAHM.getJSONObject(setId, "1");
 
       LollapaloozerHelper.debug(this, "You Clicked On: " + obj + " previous ratings "
           + _lastRatings);
     } catch (JSONException e) {
       LollapaloozerHelper.debug(this, "JSONException retrieving user's last rating");
       e.printStackTrace();
     }
 
     if (!_isLoggedIn()) {
       _beginSigninProcess();
     } else {
       showDialog(DIALOG_RATE);
     }
   }
 
   private boolean _isLoggedIn() {
     if (_loginData == null) {
       return false;
     } else {
       return true;
     }
   }
 
   private void _beginSigninProcess() {
     Toast featureRequiresSignin = Toast.makeText(this, Constants.MSG_SIGNIN_REQUIRED, 25);
     featureRequiresSignin.show();
 
     // This shows the 'enter email' dialog, no longer needed
     // showDialog(DIALOG_GETEMAIL);
 
     Intent lollapaloozerAuthIntent = new Intent(this, AuthChooseAccountActivity.class);
     // no extras needed here
     // lollapaloozerAuthIntent.putExtra(Constants.INTENT_EXTRA_TWITTER_AUTHURL,
     // "String");
     startActivityForResult(lollapaloozerAuthIntent, Constants.INTENT_REQ_CHOOSE_LOGIN_TYPE);
 
   }
 
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     System.out.println("LollapaloozerActivity.onActivityResult req=" + requestCode + " result="
         + resultCode);
 
     if (resultCode == Activity.RESULT_OK) { // Login success
       Bundle results = data.getExtras();
       _loginData = new LoginData();
       _loginData.timeLoginIssued = System.currentTimeMillis();
       _loginData.loginType = results.getInt(Constants.INTENT_EXTRA_LOGIN_TYPE);
       _loginData.accountIdentifier = results.getString(Constants.INTENT_EXTRA_ACCOUNT_IDENTIFIER);
       _loginData.accountToken = results.getString(Constants.INTENT_EXTRA_LOGIN_TOKEN);
 
       if (_loginData.loginType == Constants.LOGIN_TYPE_GOOGLE
           || _loginData.loginType == Constants.LOGIN_TYPE_FACEBOOK) {
         _loginData.emailAddress = _loginData.accountIdentifier;
       } else {
         _loginData.emailAddress = null;
       }
 
       System.out.println("Saving login data timeIssued=" + _loginData.timeLoginIssued
           + " loginType=" + _loginData.loginType + " accountIdentifier="
           + _loginData.accountIdentifier + " accountToken=" + _loginData.accountToken
           + " emailAddress=" + _loginData.emailAddress);
 
       _storageManager.putObject(DATA_LOGIN_INFO, _loginData);
       _storageManager.save();
     }
   }
 
   // Dialog handling, called before any dialog is shown
   @Override
   protected void onPrepareDialog(int id, Dialog dialog) {
 
     // Always call through to super implementation
     super.onPrepareDialog(id, dialog);
 
     if (id == DIALOG_GETEMAIL) {
       EditText emailField = (EditText) _getEmailDialog.findViewById(R.id.textField_enterEmail);
       if (_loginData.emailAddress == null) {
         emailField.setText("");
       } else {
         emailField.setText(_loginData.emailAddress);
         emailField.selectAll();
         emailField.requestFocus();
       }
     }
 
     if (id == DIALOG_RATE) {
 
       // _rateDialog.setTitle("Rate this Set!");
       try {
         TextView subtitleText = (TextView) _rateDialog.findViewById(R.id.text_rateBand_subtitle);
         subtitleText.setText(_lastItemSelected.getString("artist"));
 
       } catch (JSONException e) {
         LollapaloozerHelper.debug(this, "JSONException assigning Artist name to Rating dialog");
         e.printStackTrace();
       }
 
       // This checks the appropriate radio button based on the score set
       // in _lastRatings
       // _lastRatings is set by onItemClick not to be confused with
       // onClick
 
       RadioGroup scoreGroup = (RadioGroup) _rateDialog.findViewById(R.id.radio_pick_score);
       EditText noteWidget = (EditText) _rateDialog.findViewById(R.id.editText_commentsForSet);
       if (_lastRatings == null) {
 
         scoreGroup.clearCheck();
         noteWidget.setText("");
 
         return; // Abandon if there is no previous data
       }
       try {
         int selectedItemScore = _lastRatings.getInt(QUERY_RATINGS__RATING);
         int buttonIdToCheck;
         buttonIdToCheck = _ratingSelectedScoreToId.get(selectedItemScore + "");
         scoreGroup.check(buttonIdToCheck);
         // RadioButton buttonToCheck = (RadioButton) _rateDialog
         // .findViewById(buttonIdToCheck);
         // buttonToCheck.setChecked(true);
 
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
 
       try {
         // Ratings might not include notes
         if (_lastRatings.has(QUERY_RATINGS__NOTES)) {
           String selectedItemNote = _lastRatings.getString(QUERY_RATINGS__NOTES);
           noteWidget = (EditText) _rateDialog.findViewById(R.id.editText_commentsForSet);
           noteWidget.setText(selectedItemNote);
         } else {
           noteWidget.setText("");
         }
       } catch (JSONException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
 
     } // end if rating dialog
 
   }
 
   // Dialog handling, called once the first time this activity displays
   // (a/each
   // type of)? dialog
   @Override
   protected Dialog onCreateDialog(int id) {
     if (id == DIALOG_FIRST_USE) {
       _firstUseDialog = new Dialog(this);
       _firstUseDialog.setContentView(R.layout.first_use_dialog);
       _firstUseDialog.setTitle(Constants.DIALOG_TITLE_FIRST_USE);
 
       Button buttonOK = (Button) _firstUseDialog.findViewById(R.id.button_firstuse_ok);
       buttonOK.setOnClickListener(this);
       return _firstUseDialog;
     }
 
     if (id == DIALOG_GETEMAIL) {
       _getEmailDialog = new Dialog(this);
       _getEmailDialog.setContentView(R.layout.get_email_address);
       _getEmailDialog.setTitle(Constants.DIALOG_TITLE_GET_EMAIL);
 
       Button buttonOK = (Button) _getEmailDialog.findViewById(R.id.button_provideEmail);
       buttonOK.setOnClickListener(this);
 
       Button buttonCancel = (Button) _getEmailDialog.findViewById(R.id.button_declineEmail);
       buttonCancel.setOnClickListener(this);
 
       return _getEmailDialog;
     }
 
     if (id == DIALOG_RATE) {
       _rateDialog = new Dialog(this);
       _rateDialog.setContentView(R.layout.dialog_rate_set);
       _rateDialog.setTitle("Rate This Set!");
 
       Button buttonOK = (Button) _rateDialog.findViewById(R.id.button_rate_okgo);
       buttonOK.setOnClickListener(this);
 
       Button buttonCancel = (Button) _rateDialog.findViewById(R.id.button_rate_cancel);
       buttonCancel.setOnClickListener(this);
       return _rateDialog;
     }
 
     if (id == DIALOG_NETWORK_ERROR) {
       _networkErrorDialog = new Dialog(this);
       _networkErrorDialog.setContentView(R.layout.dialog_network_error);
       _networkErrorDialog.setTitle("Network Error");
 
       Button buttonOK = (Button) _networkErrorDialog.findViewById(R.id.button_network_error_ok);
       buttonOK.setOnClickListener(this);
       return _networkErrorDialog;
 
     }
 
     return super.onCreateDialog(id);
   }
 
   protected void onNewIntent(Intent intent) {
     super.onNewIntent(intent);
     setIntent(intent);// must store the new intent unless getIntent() will
     // return the old one
     processExtraData();
   }
 
   /**
    * TODO: potentially make year a searchable field here
    */
   private void processExtraData() {
     Intent intent = getIntent();
     Bundle bundle = intent.getExtras();
 
     if (bundle == null) {
       return;
     }
 
     String day = intent.getExtras().getString("DAY");
 
     LollapaloozerHelper.debug(this, "Searching day[" + day + "]");
     _dayToExamine = day;
     refreshData();
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.menu_global_options, menu);
     return true;
   }
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
     switch (item.getItemId()) {
     case R.id.menu_item_email_me:
       LollapaloozerHelper.debug(this, "Menu button 'email me' pressed");
 
       if (!_isLoggedIn()) {
         Toast.makeText(this, "Try rating at least one set first", 15).show();
       } else {
         try {
 
           // Toast.makeText(this, "This feature coming soon!", 15).show();
           showDialog(DIALOG_GETEMAIL);
 
         } catch (Exception e) {
           LollapaloozerHelper.debug(this, "Error requesting ratings email");
           e.printStackTrace();
         }
       }
       return true;
 
     case R.id.menu_item_delete_email:
       LollapaloozerHelper.debug(this, "Menu button 'delete email' pressed");
       _loginData = null;
       _storageManager.putObject(DATA_LOGIN_INFO, _loginData);
 
       _storageManager.save();
       refreshData();
       return true;
 
     default:
       return super.onOptionsItemSelected(item);
     }
   }
 
   @Override
   public void onCheckedChanged(RadioGroup clickedGroup, int checkedId) {
     // This is only useful when we want to act based on a user changing
     // radio selection i.e. week1/week2.
     RadioGroup scoreGroup = (RadioGroup) _rateDialog.findViewById(R.id.radio_pick_score);
 
     // Formerly used to select week, now always true
     // if (clickedGroup == _rateDialog.findViewById(R.id.radio_pick_score))
     // {
 
   }
 
 }
