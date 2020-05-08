 package edu.upenn.studyspaces;
 
 import java.io.Serializable;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
import java.util.Map;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.PendingIntent;
 import android.app.PendingIntent.CanceledException;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class SearchFragment extends Fragment {
 
     private Button mSearchButton;
     private Button mFindNowButton;
     private TextView mNumberOfPeopleTextView;
     private SeekBar mNumberOfPeopleSlider;
     private CheckBox mPrivateCheckBox;
     private CheckBox mWhiteboardCheckBox;
     private CheckBox mComputerCheckBox;
     private CheckBox mProjectorCheckBox;
 
     private CheckBox mEngiBox;
     private CheckBox mWharBox;
     private CheckBox mLibBox;
     private CheckBox mOthBox;
 
     private Button mPickStartTime;
     private Button mPickEndTime;
     private Button mPickDate;
 
     static final int START_TIME_DIALOG_ID = 0;
     static final int END_TIME_DIALOG_ID = 1;
     static final int DATE_DIALOG_ID = 2;
 
     private Location mLocation;
 
     private SearchOptions mSearchOptions;
 
     private SharedPreferences search;
     static final String SEARCH_PREFERENCES = "searchPreferences";
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     Handler mHandler = new Handler() {
         @Override
         public void handleMessage(Message message) {
             /**
              * Creating a bundle object to pass currently set Time to the
              * fragment
              */
             Bundle bundle = message.getData();
 
             /** Getting the Hour of day from bundle */
             int mHour = bundle.getInt("hour");
 
             /** Getting the Minute of the hour from bundle */
             int mMinute = bundle.getInt("minute");
             int fixedMinute = mMinute;// fixMinute(mMinute);
 
             switch (message.what) {
             case START_TIME_DIALOG_ID:
                 mSearchOptions.setStartHour(mHour);
                 mSearchOptions.setStartMinute(fixedMinute);
                 break;
             case END_TIME_DIALOG_ID:
                 mSearchOptions.setEndHour(mHour);
                 mSearchOptions.setEndMinute(fixedMinute);
                 break;
             case DATE_DIALOG_ID:
                 int dayOfMonth = bundle.getInt("day");
                 int monthOfYear = bundle.getInt("month");
                 int year = bundle.getInt("year");
                 int fixedYear = fixYear(year);
 
                 mSearchOptions.setYear(fixedYear);
                 mSearchOptions.setMonth(monthOfYear);
                 mSearchOptions.setDay(dayOfMonth);
                 break;
             }
 
             updateTimeAndDateDisplays();
         }
     };
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.search,
                 container, false);
         captureViewElements(layout);
 
         return layout;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         checkConnection();
 
         search = this.getActivity().getSharedPreferences(SEARCH_PREFERENCES, 0);
 
         mSearchButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 onSearchButtonClick(v);
 
             }
         });
 
         mFindNowButton.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 onFindNowButtonClick(v);
             }
         });
 
         mSearchOptions = new SearchOptions();
 
         setUpNumberOfPeopleSlider();
 
         int numberOfPeople = search.getInt("numberOfPeople", -1);
         if (numberOfPeople != -1) {
             mNumberOfPeopleSlider.setProgress(numberOfPeople - 1);
         }
 
         resetTimeAndDateData();
 
         setUpCheckBoxes();
         setUpPrivate();
 
         updateTimeAndDateDisplays();
 
         // submit GPS update schedule
         LocationManager locationManager = (LocationManager) this.getActivity()
                 .getSystemService(Context.LOCATION_SERVICE);
         Criteria _criteria = new Criteria();
         // _criteria.setAccuracy(Criteria.ACCURACY_LOW);
         PendingIntent _pIntent = PendingIntent.getBroadcast(this.getActivity()
                 .getApplicationContext(), 0, this.getActivity().getIntent(), 0);
         try {
             locationManager.requestLocationUpdates(5, 20, _criteria, _pIntent);
         } catch (IllegalArgumentException e) {
             Log.e("SearchActivity", "GPS probably turned off", e);
         }
         try {
             _pIntent.send();
         } catch (CanceledException e) {
             Log.e("SearchActivity",
                     "Problem sending GPS location update request", e);
         }
 
         // get current GPS location
         String provider = locationManager.getBestProvider(_criteria, true);
         mLocation = null;
         if (provider != null) {
             mLocation = locationManager.getLastKnownLocation(provider);
         }
 
         // add a click listener to the button
         mPickStartTime.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 DialogFragment startTimeDialog = new TimePickerFragment(
                         mHandler, START_TIME_DIALOG_ID, mSearchOptions
                                 .getStartHour(), mSearchOptions
                                 .getStartMinute());
                 startTimeDialog.show(getFragmentManager(), "startTimePicker");
             }
         });
         mPickEndTime.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 DialogFragment startTimeDialog = new TimePickerFragment(
                         mHandler, END_TIME_DIALOG_ID, mSearchOptions
                                 .getEndHour(), mSearchOptions.getEndMinute());
                 startTimeDialog.show(getFragmentManager(), "endTimePicker");
             }
         });
         mPickDate.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 DialogFragment dateDialog = new DatePickerFragment(mHandler,
                         mSearchOptions.getMonth(), mSearchOptions.getDay(),
                         mSearchOptions.getYear());
                 dateDialog.show(getFragmentManager(), "dateTimePicker");
             }
         });
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         // Access the default SharedPreferences
 
         // The SharedPreferences editor - must use commit() to submit changes
         SharedPreferences.Editor editor = search.edit();
         //
         // Edit the saved preferences
         editor.putBoolean("private", mSearchOptions.getPrivate());
         editor.putBoolean("computer", mSearchOptions.getComputer());
         editor.putBoolean("projector", mSearchOptions.getProjector());
         editor.putBoolean("engineering", mSearchOptions.getEngi());
         editor.putBoolean("library", mSearchOptions.getLib());
         editor.putBoolean("wharton", mSearchOptions.getWhar());
         editor.putBoolean("others", mSearchOptions.getOth());
         editor.putInt("numberOfPeople", mSearchOptions.getNumberOfPeople());
         editor.putBoolean("whiteboard", mSearchOptions.getWhiteboard());
         editor.commit();
     }
 
     private void setUpNumberOfPeopleSlider() {
         mNumberOfPeopleSlider
                 .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                     public void onStopTrackingTouch(SeekBar seekBar) {
                     }
 
                     public void onProgressChanged(SeekBar seekBar,
                             int progress, boolean fromUserTouch) {
                         mSearchOptions.setNumberOfPeople(progress + 1);
                         updateNumberOfPeopleDisplay();
                     }
 
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
                 });
 
         mSearchOptions
                 .setNumberOfPeople(mNumberOfPeopleSlider.getProgress() + 1);
 
         updateNumberOfPeopleDisplay();
     }
 
     private void setUpCheckBoxes() {
 
         mEngiBox.setChecked(search.getBoolean("engineering", true));
         mWharBox.setChecked(search.getBoolean("wharton", true));
         mLibBox.setChecked(search.getBoolean("library", true));
         mOthBox.setChecked(search.getBoolean("others", true));
 
     }
 
     private void setUpPrivate() {
 
         mPrivateCheckBox.setChecked(search.getBoolean("private", false));
         mWhiteboardCheckBox.setChecked(search.getBoolean("whiteboard", false));
         mComputerCheckBox.setChecked(search.getBoolean("computer", false));
         mProjectorCheckBox.setChecked(search.getBoolean("projector", false));
     }
 
     private void updateNumberOfPeopleDisplay() {
         String personPeopleString;
         if (mSearchOptions.getNumberOfPeople() == 1) {
             personPeopleString = " person";
         } else {
             personPeopleString = " people";
         }
         mNumberOfPeopleTextView.setText(Integer.toString(mSearchOptions
                 .getNumberOfPeople()) + personPeopleString);
     }
 
     private void roundCalendar(Calendar c) {
         if (c.get(Calendar.MINUTE) >= 1 && c.get(Calendar.MINUTE) <= 29) {
             c.add(Calendar.MINUTE, 30 - c.get(Calendar.MINUTE));
         }
         if (c.get(Calendar.MINUTE) >= 31 && c.get(Calendar.MINUTE) <= 59) {
             c.add(Calendar.MINUTE, 60 - c.get(Calendar.MINUTE));
         }
     }
 
     // TODO: Not sure if this is needed. Works fine without fixing minutes.
     @SuppressWarnings("unused")
     private int fixMinute(int minute) {
         int fixedMinute = minute;
         if (minute >= 31 && minute <= 45) {
             fixedMinute = 0; // Going up.
         } else if (minute >= 1 && minute <= 15) {
             fixedMinute = 30; // Going down.
         } else if (minute >= 46 && minute <= 59) {
             fixedMinute = 30; // Going up.
         } else if (minute >= 16 && minute <= 29) {
             fixedMinute = 0; // Going down.
         }
         return fixedMinute;
     }
 
     private int fixYear(int year) {
         Calendar c = Calendar.getInstance();
         if (year < c.get(Calendar.YEAR)) {
             year = c.get(Calendar.YEAR);
         }
         return year;
     }
 
     private String convertTo12HourFormat(int hour, int minute) {
         SimpleDateFormat displayFormat = new SimpleDateFormat("hh:mm a");
         SimpleDateFormat parseFormat = new SimpleDateFormat("HH:mm");
         String time = hour + ":" + minute;
         Date date;
         String convertedTime = "";
         try {
             date = parseFormat.parse(time);
             convertedTime = displayFormat.format(date);
         } catch (ParseException e) {
             Log.e("Time", "Couldn't parse time to 12 hour format.");
         }
         return convertedTime;
     }
 
     private void updateStartTimeText() {
 
         String convertedTime = convertTo12HourFormat(
                 mSearchOptions.getStartHour(), mSearchOptions.getStartMinute());
 
         mPickStartTime.setText(convertedTime);
     }
 
     private void updateEndTimeText() {
         String convertedTime = convertTo12HourFormat(
                 mSearchOptions.getEndHour(), mSearchOptions.getEndMinute());
 
         mPickEndTime.setText(convertedTime);
     }
 
     private String convertDateFormat(int month, int day, int year) {
         SimpleDateFormat displayFormat = new SimpleDateFormat(
                 "EEE, MMM d, yyyy");
         SimpleDateFormat parseFormat = new SimpleDateFormat("M-d-y");
 
         String time = month + 1 + "-" + day + "-" + year;
         Date date;
         String convertedTime = "";
         try {
             date = parseFormat.parse(time);
             convertedTime = displayFormat.format(date);
         } catch (ParseException e) {
             Log.e("Time", "Couldn't parse date.");
         }
         return convertedTime;
     }
 
     private void updateDateText() {
         String convertedTime = convertDateFormat(mSearchOptions.getMonth(),
                 mSearchOptions.getDay(), mSearchOptions.getYear());
         mPickDate.setText(convertedTime);
     }
 
     // Updates search options then delivers intent
     public void onSearchButtonClick(View view) {
         if (checkConnection()) {
             putDataInSearchOptionsObject();
             Intent i = new Intent(this.getActivity().getBaseContext(),
                     StudySpaceListActivity.class);
             // Put your searchOption class here
             i.putExtra("SEARCH_OPTIONS", (Serializable) mSearchOptions);
             // ends this activity
             startActivity(i);
         }
     }
 
     private void putDataInSearchOptionsObject() {
         mSearchOptions
                 .setNumberOfPeople(mNumberOfPeopleSlider.getProgress() + 1);
         mSearchOptions.setPrivate(mPrivateCheckBox.isChecked());
         mSearchOptions.setWhiteboard(mWhiteboardCheckBox.isChecked());
         mSearchOptions.setComputer(mComputerCheckBox.isChecked());
         mSearchOptions.setProjector(mProjectorCheckBox.isChecked());
 
         mSearchOptions.setEngi(mEngiBox.isChecked());
         mSearchOptions.setWhar(mWharBox.isChecked());
         mSearchOptions.setLib(mLibBox.isChecked());
         mSearchOptions.setOth(mOthBox.isChecked());
     }
 
     private void resetTimeAndDateData() {
         // Initialize the date and time data based on the current time:
         final Calendar cStartTime = Calendar.getInstance();
         roundCalendar(cStartTime);
         mSearchOptions.setStartHour(cStartTime.get(Calendar.HOUR_OF_DAY));
         mSearchOptions.setStartMinute(cStartTime.get(Calendar.MINUTE));
         final Calendar cEndTime = Calendar.getInstance();
         cEndTime.add(Calendar.HOUR_OF_DAY, 1);
         roundCalendar(cEndTime);
         mSearchOptions.setEndHour(cEndTime.get(Calendar.HOUR_OF_DAY));
         mSearchOptions.setEndMinute(cEndTime.get(Calendar.MINUTE));
         mSearchOptions.setYear(cStartTime.get(Calendar.YEAR));
         mSearchOptions.setMonth(cStartTime.get(Calendar.MONTH));
         mSearchOptions.setDay(cStartTime.get(Calendar.DAY_OF_MONTH));
     }
 
     private void updateTimeAndDateDisplays() {
         updateStartTimeText();
         updateEndTimeText();
         updateDateText();
     }
 
     private void captureViewElements(LinearLayout layout) {
 
         // General:
         mSearchButton = (Button) layout.findViewById(R.id.searchButton);
         mFindNowButton = (Button) layout.findViewById(R.id.findNowButton);
         mNumberOfPeopleTextView = (TextView) layout
                 .findViewById(R.id.numberOfPeopleTextView);
         mNumberOfPeopleSlider = (SeekBar) layout
                 .findViewById(R.id.numberOfPeopleSlider);
         mPrivateCheckBox = (CheckBox) layout.findViewById(R.id.privateCheckBox);
         mWhiteboardCheckBox = (CheckBox) layout
                 .findViewById(R.id.whiteboardCheckBox);
         mComputerCheckBox = (CheckBox) layout
                 .findViewById(R.id.computerCheckBox);
         mProjectorCheckBox = (CheckBox) layout
                 .findViewById(R.id.projectorCheckBox);
 
         mEngiBox = (CheckBox) layout.findViewById(R.id.engibox);
         mWharBox = (CheckBox) layout.findViewById(R.id.whartonbox);
         mLibBox = (CheckBox) layout.findViewById(R.id.libbox);
         mOthBox = (CheckBox) layout.findViewById(R.id.otherbox);
 
         // Time and date:
         mPickStartTime = (Button) layout.findViewById(R.id.pickStartTime);
         mPickEndTime = (Button) layout.findViewById(R.id.pickEndTime);
         mPickDate = (Button) layout.findViewById(R.id.pickDate);
 
     }
 
     public void onFavoritesButtonClick(View view) {
         if (checkConnection()) {
             putDataInSearchOptionsObject();
             Intent i = new Intent(this.getActivity().getBaseContext(),
                     FavoritesFragment.class);
             startActivity(i);
         }
     }
 
     public void onFindNowButtonClick(View view) {
         if (checkConnection()) {
             // reinitialize the date and time to current value
             resetTimeAndDateData();
             mSearchOptions.setNumberOfPeople(mNumberOfPeopleSlider
                     .getProgress());
             mSearchOptions.setPrivate(false);
             mSearchOptions.setWhiteboard(false);
             mSearchOptions.setComputer(false);
             mSearchOptions.setProjector(false);
             mSearchOptions.setEngi(true);
             mSearchOptions.setWhar(true);
             mSearchOptions.setLib(true);
             mSearchOptions.setOth(true);
 
             // get the list of all the study spaces
             ArrayList<StudySpace> studySpaces = ((APIAccessor) getActivity()
                     .getApplication()).getStudySpaces();
             ArrayList<StudySpace> ss_list = new ArrayList<StudySpace>();
            SharedPreferences favorites = this.getActivity().getSharedPreferences(StudySpaceListActivity.FAV_PREFERENCES, 0);
            Map<String, ?> items = favorites.getAll();
            Preferences preferences = new Preferences(); // Change this when bundle is
                                             // implemented.
            for (String s : items.keySet()) {
                // boolean fav = favorites.getBoolean(s, false);
                if (Boolean.parseBoolean(items.get(s).toString())) {
                    preferences.addFavorites(s);
                }
            }
             ss_list.addAll(studySpaces);
 
             // filter them by location
             StudySpaceListAdapter ss_adapter = new StudySpaceListAdapter(
                     getActivity(), R.layout.sslistitem, ss_list, mSearchOptions);
             ss_adapter.setLocation(mLocation);
             ss_adapter.filterSpaces();
             ss_adapter.updateFavorites(preferences);
 
             // call the study space details activity with the first study space
             // of the list (it is the closest)
             Intent i = new Intent(getActivity(), StudySpaceDetails.class);
             i.putExtra("STUDYSPACE", (StudySpace) ss_adapter.getItem(0));
             i.putExtra("PREFERENCES", preferences);
             getActivity().setResult(Activity.RESULT_OK, i);
             // ends this activity
             startActivity(i);
         }
     }
 
     private boolean checkConnection() {
         ConnectivityManager connMgr = (ConnectivityManager) this.getActivity()
                 .getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         boolean connection = true;
         if (networkInfo == null || !networkInfo.isConnected()) {
             Context context = this.getActivity().getApplicationContext();
             Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
             toast.setGravity(Gravity.CENTER, 0, 0);
             toast.setText("No network connection available.");
             toast.show();
             connection = false;
         }
         return connection;
     }
 
     public static class TimePickerFragment extends DialogFragment implements
             TimePickerDialog.OnTimeSetListener {
 
         private Handler mHandler;
         private int mHour;
         private int mMinute;
         int mType;
 
         public TimePickerFragment(Handler handler, int type, int hour,
                 int minute) {
             mHandler = handler;
             mType = type;
             mHour = hour;
             mMinute = minute;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
 
             return new TimePickerDialog(getActivity(), this, mHour, mMinute,
                     DateFormat.is24HourFormat(getActivity()));
         }
 
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
             mHour = hourOfDay;
             mMinute = minute;
 
             Bundle bundle = new Bundle();
             bundle.putInt("hour", mHour);
             bundle.putInt("minute", mMinute);
             Message message = new Message();
             message.setData(bundle);
 
             if (mType == START_TIME_DIALOG_ID)
                 message.what = START_TIME_DIALOG_ID;
 
             else if (mType == END_TIME_DIALOG_ID)
                 message.what = END_TIME_DIALOG_ID;
 
             mHandler.sendMessage(message);
         }
     }
 
     public static class DatePickerFragment extends DialogFragment implements
             DatePickerDialog.OnDateSetListener {
         private Handler mHandler;
 
         int mMonth;
         int mDay;
         int mYear;
 
         public DatePickerFragment(Handler handler, int month, int day, int year) {
             mHandler = handler;
             mMonth = month;
             mDay = day;
             mYear = year;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
 
             return new DatePickerDialog(getActivity(), this, mYear, mMonth,
                     mDay);
         }
 
         @Override
         public void onDateSet(DatePicker view, int year, int monthOfYear,
                 int dayOfMonth) {
             mDay = dayOfMonth;
             mMonth = monthOfYear;
             mYear = year;
 
             /**
              * Creating a bundle object to pass currently set date to the
              * fragment
              */
             Bundle bundle = new Bundle();
 
             bundle.putInt("day", mDay);
             bundle.putInt("month", mMonth);
             bundle.putInt("year", mYear);
 
             Message message = new Message();
             message.setData(bundle);
             message.what = DATE_DIALOG_ID;
             mHandler.sendMessage(message);
 
         }
 
     }
 
 }
