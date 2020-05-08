 package com.appcay.cocorahs;
 
 import android.app.*;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.*;
 import com.placed.client.android.PlacedAgent;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 
 public class CoCoRaHS extends Activity
 {
 
     Context mContext = null;
     ProgressDialog progressDialog = null;
     CoCoComm comm = new CoCoComm();
     static PlacedAgent placedAgent = null;
     Menu cocoMenu = null;
     String new_snow_inches = "";
     String new_snow_melted_core = "";
     String total_snow_inches = "";
     String total_snow_melted_core = "";
     Integer flood_index = 0;
     String flood_text = "No flooding occurred";
     String notes = "";
     String rain = "0.00";
     int[] keycodes = new int[5];
 
     static final int TIME_DIALOG_ID = 0;
     static final int DATE_DIALOG_ID = 1;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.main);
         mContext = this;
         placedAgent = PlacedAgent.getInstance(mContext, "c6ff9337c4f9");
         handleButtons();
         placedAgent.logPageView("Login Screen");
 
         // hide the soft keyboard
         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
         imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
     }
 
     @Override
     public void onBackPressed() {
         new AlertDialog.Builder(this)
                 .setTitle("Really Exit?")
                 .setMessage("Are you sure you want to exit?")
                 .setNegativeButton(android.R.string.no, null)
                 .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface arg0, int arg1) {
                         CoCoRaHS.super.onBackPressed();
                     }
                 }).create().show();
     }
 
     @Override
     public void onStart() {
         super.onStart();
         placedAgent.logStartSession();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         placedAgent.logEndSession();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         cocoMenu = menu;
         getMenuInflater().inflate(R.menu.option_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId()) {
             case R.id.history:
                 if(comm.isLoggedIn()) {
                     showHistory();
                 }
                 else {
                     showOKAlertMsg("Whoops!", "You must log in before viewing history.", false);
                 }
                 return true;
             case R.id.report:
                 if(comm.isLoggedIn()) {
                     comm.fetchStationId();
                     showReport();
                 }
                 else {
                     showOKAlertMsg("Whoops!", "You must log in before submitting reports.", false);
                 }
                 return true;
             case R.id.about:
                 showAbout();
                 return true;
             case R.id.login:
                 showLogin();
                 return true;
         }
         return false;
     }
 
 
     // the callback received when the user "sets" the time in the dialog
     private TimePickerDialog.OnTimeSetListener mTimeSetListener =
             new TimePickerDialog.OnTimeSetListener() {
                 public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                     EditText etObTime = (EditText) findViewById(R.id.etObTime);
                     Calendar mCalendar = Calendar.getInstance();
                     mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                     mCalendar.set(Calendar.MINUTE, minute);
                     SimpleDateFormat mSDF = new SimpleDateFormat("hh:mm a");
                     String time = mSDF.format(mCalendar.getTime());
                     etObTime.setText("" + time);
                 }
             };
     private DatePickerDialog.OnDateSetListener mDateSetListener =
             new DatePickerDialog.OnDateSetListener() {
                 @Override
                 public void onDateSet(DatePicker view,
                                       int year, int monthOfYear,
                                       int dayOfMonth) {
                     EditText etObDate = (EditText) findViewById(R.id.etObDate);
                     etObDate.setText("" + (monthOfYear+1) + "/" +
                             dayOfMonth + "/" + year);
                 }
             };
 
     private void handleButtons() {
         EditText etObDate = (EditText) findViewById(R.id.etObDate);
         if(etObDate != null) {
             etObDate.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     showDialog(DATE_DIALOG_ID);
                 }
             });
             etObDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                 public void onFocusChange(View v, boolean hasFocus) {
                     if (hasFocus) showDialog(DATE_DIALOG_ID);
                 }
             });
         }
 
         EditText etObTime = (EditText) findViewById(R.id.etObTime);
         if(etObTime != null) {
             etObTime.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     showDialog(TIME_DIALOG_ID);
                 }
             });
             etObTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                 public void onFocusChange(View v, boolean hasFocus) {
                     if(hasFocus) showDialog(TIME_DIALOG_ID);
                 }
             });
         }
 
         TextView tvSnow = (TextView) findViewById(R.id.tvSnow);
         if(tvSnow != null) {
             tvSnow.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     EditText etNotes = (EditText) findViewById(R.id.etNotes);
                     if(etNotes != null) {
                         notes = etNotes.getText().toString().trim();
                     }
                     EditText etRain = (EditText) findViewById(R.id.etObRain);
                     if(etRain != null) {
                         rain = etRain.getText().toString().trim();
                     }
                     setContentView(R.layout.report_detail);
                     placedAgent.logPageView("Snow Detail");
                     Spinner spnFlood = (Spinner) findViewById(R.id.spnFlood);
                     ArrayAdapter<CharSequence> floodlevelsAdapter = null;
                     floodlevelsAdapter = ArrayAdapter.createFromResource(mContext, R.array.flooding , android.R.layout.simple_spinner_item);
                     floodlevelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                     spnFlood.setAdapter(floodlevelsAdapter);
                     EditText etNewSnow = (EditText) findViewById(R.id.etNewAccum);
                     EditText etNewCore = (EditText) findViewById(R.id.etNewCore);
                     EditText etTotalSnow = (EditText) findViewById(R.id.etTotalAccum);
                     EditText etTotalCore = (EditText) findViewById(R.id.etTotalCore);
                     if(etNewSnow != null && etNewCore != null && etTotalCore != null && etTotalSnow != null) {
                         spnFlood.setSelection(flood_index);
                         etNewSnow.setText(new_snow_inches);
                         etNewCore.setText(new_snow_melted_core);
                         etTotalSnow.setText(total_snow_inches);
                         etTotalCore.setText(total_snow_melted_core);
                     }
                     handleButtons();
                 }
             });
         }
 
         Button btnSave = (Button) findViewById(R.id.btnSave);
         if(btnSave != null) {
             btnSave.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     EditText etNewSnow = (EditText) findViewById(R.id.etNewAccum);
                     EditText etNewCore = (EditText) findViewById(R.id.etNewCore);
                     EditText etTotalSnow = (EditText) findViewById(R.id.etTotalAccum);
                     EditText etTotalCore = (EditText) findViewById(R.id.etTotalCore);
                     Spinner  spnFlood  = (Spinner)  findViewById(R.id.spnFlood);
                     if(etNewSnow != null && etNewCore != null && etTotalCore != null && etTotalSnow != null) {
                         new_snow_inches = etNewSnow.getText().toString().trim();
                         new_snow_melted_core = etNewCore.getText().toString().trim();
                         total_snow_inches = etTotalSnow.getText().toString().trim();
                         total_snow_melted_core = etTotalCore.getText().toString().trim();
                         flood_index = spnFlood.getSelectedItemPosition();
                         flood_text = spnFlood.getSelectedItem().toString();
                         if(flood_index < 0) flood_index = 0;
                         if(total_snow_melted_core.equals("")) total_snow_melted_core = "NA";
                         if(total_snow_inches.equals("")) total_snow_inches = "NA";
                         if(new_snow_melted_core.equals("")) new_snow_melted_core = "NA";
                         if(new_snow_inches.equals("")) new_snow_inches = "NA";
                     }
 
                     showReport();
                     EditText etNotes = (EditText) findViewById(R.id.etNotes);
                     if(etNotes != null) {
                         etNotes.setText(notes);
                     }
                     EditText etRain = (EditText) findViewById(R.id.etObRain);
                     if(etRain != null) {
                         etRain.setText(rain);
                     }
                     handleButtons();
                 }
             });
         }
 
         CheckBox cbSaveLogin = (CheckBox) findViewById(R.id.cbSaveLogin);
         if(cbSaveLogin != null) {
             String username = getPreference("username", mContext);
             String password = getPreference("password", mContext);
             EditText etUser = (EditText) findViewById(R.id.etUsername);
             if(etUser != null) {
                 etUser.setText(username);
             }
             EditText etPass = (EditText) findViewById(R.id.etPassword);
             if(etPass != null) {
                 etPass.setText(password);
             }
             if(username.length() > 0 && password.length() > 0) {
                 cbSaveLogin.setChecked(true);
                 try { etPass.requestFocus(); } catch (Exception e) {
                   // ignore
                 }
             }
             else {
                 cbSaveLogin.setChecked(false);
             }
 
             // make the enter key in password auto-click submit.
             etPass.setOnKeyListener(new View.OnKeyListener() {
                 public boolean onKey(View v, int keyCode, KeyEvent event) {
                     // If the event is a key-down event on the "enter" button
                     if ((event != null) && (v != null) && (event.getAction() == KeyEvent.ACTION_DOWN) &&
                             (keyCode == KeyEvent.KEYCODE_ENTER)) {
                         // Perform action on key press
                         Button btnLogin = (Button) findViewById(R.id.btnLogin);
                         btnLogin.performClick();
                         return true;
                     }
                     return false;
                 }
             });
         }
 
         // make the enter key in etObRain hide keyboard.
         EditText etObRain = (EditText) findViewById(R.id.etObRain);
         if(etObRain != null) {
             etObRain.setOnKeyListener(new View.OnKeyListener() {
                 public boolean onKey(View v, int keyCode, KeyEvent event) {
                     // If the event is a key-down event on the "enter" button
                     if ((event != null) && (v != null) && (event.getAction() == KeyEvent.ACTION_DOWN) &&
                             (keyCode == KeyEvent.KEYCODE_ENTER)) {
                         // hide the soft keyboard
                         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                         imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                         return true;
                     }
                     return false;
                 }
             });
         }
 
         // make the enter key in etNotes hide keyboard.
         EditText etNotes = (EditText) findViewById(R.id.etNotes);
         if(etNotes != null) {
             etNotes.setOnKeyListener(new View.OnKeyListener() {
                 public boolean onKey(View v, int keyCode, KeyEvent event) {
                     // If the event is a key-down event on the "enter" button
                     if ((event != null) && (v != null) && (event.getAction() == KeyEvent.ACTION_DOWN) &&
                             (keyCode == KeyEvent.KEYCODE_ENTER)) {
                         // hide the soft keyboard
                         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                         imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                         return true;
                     }
                     return false;
                 }
             });
         }
 
         WebView wvSignUp = (WebView) findViewById(R.id.wvSignUp);
         if(wvSignUp != null) {
             wvSignUp.loadDataWithBaseURL(null, this.getText(R.string.signup).toString(), "text/html", "UTF-8", null);
             wvSignUp.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
         }
 
         Button btnSubmitPrecip = (Button) findViewById(R.id.btnSubmit);
         if(btnSubmitPrecip != null) {
             btnSubmitPrecip.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     EditText etObTime = (EditText) findViewById(R.id.etObTime);
                     EditText etObDate = (EditText) findViewById(R.id.etObDate);
                     EditText etObRain = (EditText) findViewById(R.id.etObRain);
                     EditText etNotes = (EditText) findViewById(R.id.etNotes);
                     if(etObTime != null && etObDate != null && etObRain != null && etNotes != null) {
                         progressDialog = ProgressDialog.show(CoCoRaHS.this, "", "Submitting...", true);
                         new PrecipTask().execute("http://www.cocorahs.org/Admin/MyDataEntry/DailyPrecipReport.aspx",
                                 etObDate.getText().toString().trim(), etObTime.getText().toString().trim(),
                                 etObRain.getText().toString().trim(),
                                 flood_text, etNotes.getText().toString().trim(),
                                 new_snow_inches, new_snow_melted_core,
                                 total_snow_inches, total_snow_melted_core);
                     }
                 }
             });
         }
 
         Button btnLogin = (Button) findViewById(R.id.btnLogin);
         if(btnLogin != null) {
             btnLogin.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     String username = "", password = "";
                     EditText etUser = (EditText) findViewById(R.id.etUsername);
                     if(etUser != null) {
                         username = etUser.getText().toString().toLowerCase().trim();
                     }
                     EditText etPass = (EditText) findViewById(R.id.etPassword);
                     if(etPass != null) {
                         password = etPass.getText().toString().trim();
                     }
                     CheckBox cbSaveLogin = (CheckBox) findViewById(R.id.cbSaveLogin);
                     if(cbSaveLogin != null) {
                         if(cbSaveLogin.isChecked()) {
                             LOG("Saving user/pass for future login...");
                             savePreference("username", username, mContext);
                             savePreference("password", password, mContext);
                         }
                         else {
                             LOG("Clearing saved user/pass...");
                             savePreference("username", "", mContext);
                             savePreference("password", "", mContext);
                         }
                     }
                     progressDialog = ProgressDialog.show(CoCoRaHS.this, "", "Authenticating...", true);
                     new LoginTask().execute("http://www.cocorahs.org/Login.aspx", username, password);
                 }
             });
         }
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             return true;
         } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         keycodes[0] = keycodes[1];
         keycodes[1] = keycodes[2];
         keycodes[2] = keycodes[3];
         keycodes[3] = keycodes[4];
         keycodes[4] = keyCode;
 
         // volume UP UP DOWN DOWN UP will send me logs
         if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             if ((keycodes[0] == KeyEvent.KEYCODE_VOLUME_UP)
                     && (keycodes[1] == KeyEvent.KEYCODE_VOLUME_UP)
                     && (keycodes[2] == KeyEvent.KEYCODE_VOLUME_DOWN)
                     && (keycodes[3] == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                 LOG("Getting Logs...");
                 Logs l = new Logs();
                 ArrayList<String> loglines = l.getLogs();
                 Toast.makeText(mContext, "Gathering logs...",
                         Toast.LENGTH_LONG).show();
                 Iterator<String> iterator = loglines.iterator();
                 String emailtext = "SecuRemote Logs\n";
                 while(iterator.hasNext()) {
                     emailtext = emailtext + iterator.next().toString() + "\n";
                 }
                 String sub = "CoCoRaHS Debug Logs from Android phone";
                 l.sendLogs(mContext,"support@appcay.com", sub, emailtext);
             }
         }
         if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
             return true;
         } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
             return true;
         }
         return super.onKeyUp(keyCode, event);
     }
 
     protected Dialog onCreateDialog(int id) {
         Dialog dialog = null;
         Calendar calendar = Calendar.getInstance();
         switch (id) {
             case TIME_DIALOG_ID:
                 int hour = calendar.get(Calendar.HOUR_OF_DAY);
                 int min = calendar.get(Calendar.MINUTE);
                 return new TimePickerDialog(this,
                         mTimeSetListener, hour, min, false);
             case DATE_DIALOG_ID:
                 int year       = calendar.get(Calendar.YEAR);
                 int monthOfYear   = calendar.get(Calendar.MONTH);
                 int dayOfMonth     = calendar.get(Calendar.DAY_OF_MONTH);
                 return new DatePickerDialog(this,mDateSetListener, year, monthOfYear, dayOfMonth);
             default:
                 dialog = null;
         }
         return dialog;
     }
 
     public static String savePreference(String key, String value, Context c) {
         String value2 = value.replaceAll("[^\\d\\w\\.@-]", "").replaceAll("\000", "");
         SharedPreferences mySharedPrefs = c.getSharedPreferences("CoCoRaHS-Prefs",
                 Activity.MODE_WORLD_WRITEABLE);
         SharedPreferences.Editor editor = mySharedPrefs.edit();
         editor.putString(key, value2);
         editor.commit();
         return value2;  // in case we want to chain it to another function
     }
 
     public static String getPreference(String key, Context c) {
         SharedPreferences mySharedPrefs = c.getSharedPreferences("CoCoRaHS-Prefs",
                 Activity.MODE_WORLD_WRITEABLE);
         return mySharedPrefs.getString(key, "");
     }
 
     public static void LOG(String msg) {
         if(msg != null) {
             Log.v("CoCoRaHS", msg);
         }
     }
 
     void showOKAlertMsg(String title, String msg, final Boolean xfinish) {
         LOG(title + ": " + msg);
         AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         dialogBuilder.setNeutralButton("OK",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                         if(xfinish) { finish(); }
                     }
                 });
 
         dialogBuilder.setTitle(title);
         dialogBuilder.setMessage(msg);
         try {
             dialogBuilder.show();
         } catch (Exception e) {
             LOG("OOPS: I can't show a showOKAlertMsg dialog: " + e.getMessage());
         }
     }
 
 
     //TODO: need to reload the precip report form to get right viewstate
 
     private void showReport() {
         setContentView(R.layout.report);
         placedAgent.logPageView("Precip Report");
         handleButtons();
         TextView tvId = (TextView) findViewById(R.id.txtStationId);
         if(tvId != null) {
             tvId.setText(comm.getStationId());
         }
         TextView tvName = (TextView) findViewById(R.id.txtStationName);
         if(tvName != null) {
             tvName.setText(comm.getStationName());
         }
 
         EditText etObTime = (EditText) findViewById(R.id.etObTime);
         EditText etObDate = (EditText) findViewById(R.id.etObDate);
         if(etObTime != null && etObDate != null) {
             etObDate.setText(comm.getObservedDate());
             etObTime.setText(comm.getObservedTime() + " " + comm.getObservedAmPm());
         }
 
         EditText etDate = (EditText) findViewById(R.id.etObDate);
         if(etDate != null) {
             etDate.setText(comm.getObservedDate());
         }
         EditText etTime = (EditText) findViewById(R.id.etObTime);
         if(etTime != null) {
             etTime.setText(comm.getObservedTime() + " " + comm.getObservedAmPm());
         }
 
         // hide the soft keyboard
         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
         imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
     }
 
     private void showLogin() {
         setContentView(R.layout.main);
         placedAgent.logPageView("Login Screen");
         comm = new CoCoComm();
         handleButtons();
     }
 
     private void showAbout() {
         setContentView(R.layout.about);
         WebView wvAbout = (WebView) findViewById(R.id.wvAbout);
         if(wvAbout != null) {
             wvAbout.loadDataWithBaseURL(null, this.getText(R.string.about).toString(), "text/html", "UTF-8", null);
         }
         TextView tvSubtitle = (TextView) findViewById(R.id.txtSubtitle);
         if(tvSubtitle != null) {
             PackageManager pm = getPackageManager();
             PackageInfo packageInfo = null;
             String myVersion = "0.0.0";
             try {
                 packageInfo = pm.getPackageInfo(this.getPackageName(), 0);
                 myVersion = packageInfo.versionName;
             } catch (PackageManager.NameNotFoundException e) {
             }
             tvSubtitle.append("\n\nVersion: " + myVersion);
         }
     }
 
     private void showHistory() {
         setContentView(R.layout.history);
         TextView txtStationId = (TextView) findViewById(R.id.txtStationId);
         TextView txtStationName = (TextView) findViewById(R.id.txtStationName);
         if(txtStationId != null && txtStationName != null) {
             txtStationId.setText(comm.getStationId());
             txtStationName.setText(comm.getStationName());
         }
         progressDialog = ProgressDialog.show(CoCoRaHS.this, "", "Fetching History...", true);
         new HistoryTask().execute("");
     }
 
     private void addTableRow(String date, String time, String rain, String editLink, Boolean isHeader) {
         final TableLayout table = (TableLayout) findViewById(R.id.history_table);
         final TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.table_row_item, null);
 
         TextView tv1 = (TextView) tr.findViewById(R.id.cell_date);
         tv1.setText(date);
         TextView tv2 = (TextView) tr.findViewById(R.id.cell_time);
         tv2.setText(time);
         TextView tv3 = (TextView) tr.findViewById(R.id.cell_rain);
         tv3.setText(rain);
         TextView tv4 = (TextView) tr.findViewById(R.id.cell_editLink);
         tv4.setText(editLink);
         if(isHeader) {
             tv1.setTypeface(null, Typeface.BOLD);
             tv2.setTypeface(null, Typeface.BOLD);
             tv3.setTypeface(null, Typeface.BOLD);
             tv4.setTypeface(null, Typeface.BOLD);
             tr.setFocusableInTouchMode(false);
         }
         tr.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 TextView tv1 = (TextView) v.findViewById(R.id.cell_date);
                 TextView tv2 = (TextView) v.findViewById(R.id.cell_time);
                 TextView tv3 = (TextView) v.findViewById(R.id.cell_rain);
                 TextView tv4 = (TextView) v.findViewById(R.id.cell_editLink);
                 CoCoRaHS.LOG("Clicked row: " + tv1.getText() + " " + tv2.getText() +
                     " " + tv3.getText() + " " + tv4.getText());
             }
         });
         table.addView(tr);
 
         // Draw separator
         TextView tv5 = new TextView(this);
         tv5.setBackgroundColor(Color.parseColor("#80808080"));
         tv5.setHeight(2);
         table.addView(tv5);
         registerForContextMenu(tr);
     }
 
     class LoginTask extends AsyncTask<String, Void, Boolean> {
         private Exception exception;
 
         protected Boolean doInBackground(String... args) {
             try {
                 String url = args[0];
                 String username = args[1];
                 String password = args[2];
                 comm.clearStation();
                 if(comm.postLoginData(url, username, password)) {
                     LOG("Login OK.");
                     LOG("Station ID: " + comm.getStationId());
                     LOG("Station Name: " + comm.getStationName());
                     return true;
                 }
                 else {
                     LOG("Login Failed.");
                     return false;
                 }
             } catch (Exception e) {
                 this.exception = e;
                 return null;
             }
         }
 
         protected void onPostExecute(Boolean result) {
             try {
                 progressDialog.dismiss();
             } catch (Exception e) {}
             if(result) {
                 showReport();
             }
             else {
                 showOKAlertMsg("Whoops!", "Login Failed.  Check your username and password then try again.", false);
             }
         }
     }
 
     class PrecipTask extends AsyncTask<String, Void, Boolean> {
         private Exception exception;
 
         protected Boolean doInBackground(String... args) {
             try {
                 String url = args[0];
                 String date = args[1];
                 String time = args[2];
                 String rain = args[3];
                 //String loc = args[4];
                 String flooding = args[4];
                 String notes = args[5];
                 String new_snow = args[6];
                 String new_snow_core = args[7];
                 String total_snow = args[8];
                 String total_snow_core = args[9];
                 //comm.clearStation();
                 if(comm.postPrecipReport(url,date,time,rain,flooding,notes,new_snow,new_snow_core,total_snow,total_snow_core)) {
                     LOG("Precip Report Sent OK.");
                     return true;
                 }
                 else {
                     LOG("Failed to send precip report.");
                     return false;
                 }
             } catch (Exception e) {
                 this.exception = e;
                 return null;
             }
         }
 
         protected void onPostExecute(Boolean result) {
             try {
                 progressDialog.dismiss();
             } catch (Exception e) {}
             if(result == null) {
                  showOKAlertMsg("Whoops!", "An error occurred: Make sure all fields are entered correctly.  Exception: " + exception.getMessage(), false);
             }
             else if(result) {
                 showOKAlertMsg("Success!", "Thank you for submitting your daily precipation report.", false);
                 showHistory();
             }
             else {
                 if(! comm.getReportOkReason().equals("")) {
                     showOKAlertMsg("Whoops!", "Failed to send the precipitation report to CoCoRaHS: " + comm.getReportOkReason(), false);
                 }
                 else {
                     showOKAlertMsg("Whoops!", "Failed to send the precipitation report to CoCoRaHS: Try again.", false);
                 }
             }
         }
     }
 
     class HistoryTask extends AsyncTask<String, Void, Boolean> {
         private Exception exception;
         ArrayList<CoCoRecord> history = null;
 
         protected Boolean doInBackground(String... args) {
             comm.clearReportOkReason();
             try {
                 history = comm.getPrecipHistory(7);
             } catch (Exception e) {
                 this.exception = e;
                 return null;
             }
             return true;
         }
 
         protected void onPostExecute(Boolean result) {
             try {
                 progressDialog.dismiss();
             } catch (Exception e) {}
             if(result == null) {
                 showOKAlertMsg("Whoops!", "An error occurred: " + exception.getMessage(), false);
             }
             else if(result) {
                 addTableRow("DATE", "TIME", "PRECIP", "", true);
                 for(CoCoRecord item : history) {
                     addTableRow(item.getDate(), item.getTime(), "" + item.getTotalPrecip() + "\"", item.getEditLink(), false);
                 }
             }
             else {
                 if(! comm.getReportOkReason().equals("")) {
                     showOKAlertMsg("Whoops!", "Failed to fetch CoCoRaHS history: " + comm.getReportOkReason(), false);
                 }
                 else {
                     showOKAlertMsg("Whoops!", "Failed to fetch CoCoRaHS history: Try again.", false);
                 }
             }
         }
     }
 
 }
