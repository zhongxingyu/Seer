 package com.HuskySoft.metrobike.ui;
 
 import java.io.Serializable;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.TimePicker;
 
 import com.HuskySoft.metrobike.R;
 import com.HuskySoft.metrobike.backend.DirectionsRequest;
 import com.HuskySoft.metrobike.backend.DirectionsStatus;
 import com.HuskySoft.metrobike.backend.TravelMode;
 import com.HuskySoft.metrobike.ui.utility.HistoryAdapter;
 import com.HuskySoft.metrobike.ui.utility.HistoryItem;
 
 /**
  * An activity that receives and handle's users' requests for routes.
  * 
  * @author Shuo Wang
  */
 
 public class SearchActivity extends Activity {
 
     /**
      * Minimum two digit number.
      */
     private static final int MIN_TWO_DIGIT_NUMBER = 10;
     
     /**
      * A Fragment that contains a date picker to let user select a date of
      * departure/arrival.
      */
     
     public static class DatePickerFragment extends DialogFragment implements
             DatePickerDialog.OnDateSetListener {
         
         /**
          * Minimum two digit number.
          */
         private static final int MIN_TWO_DIGIT_NUMBER = 10;
         
         @Override
         public final Dialog onCreateDialog(final Bundle savedInstanceState) {
             // Use the current date as the default date in the picker
             final Calendar calendar = Calendar.getInstance();
             int year = calendar.get(Calendar.YEAR);
             int month = calendar.get(Calendar.MONTH);
             int day = calendar.get(Calendar.DAY_OF_MONTH);
 
             // Create a new instance of DatePickerDialog and return it
             DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year, month, day);
             // Set the name of the dialog box
             dpd.setTitle(R.string.dialogbox_date);
             return dpd;
         }
 
         /**
          * Update the date EditText widget after an user picks a date.
          * @param view
          *            The DatePicker view whose date is set by user 
          * @param year 
          *            The year of the date picked
          * @param month
          *            The month of the date picked (1-12 as Jan. to Dec.)
          * @param day
          *            The day of month of the date picked
          */
         public final void onDateSet(final DatePicker view, 
                                     final int year, 
                                     final int month, 
                                     final int day) {
             EditText dateEditText = (EditText) getActivity().findViewById(R.id.editTextDate);
 
             // Formatting string be displayed
             String monthString = "";
 
             // System uses month from 0 to 11 to represent January to December
             int monthUIStyle = month + 1;
             if (monthUIStyle < MIN_TWO_DIGIT_NUMBER) {
                 monthString += "0";
             }
             monthString += monthUIStyle;
 
             String dayString = "";
             if (day < MIN_TWO_DIGIT_NUMBER) {
                 dayString += "0";
             }
             dayString += day;
 
             // Update the date EditText widget
             dateEditText.setText(monthString + "/" + dayString + "/" + year);
         }
     }
 
     /**
      * A Fragment that contains a time picker to let user select a time (hh:mm)
      * of departure/arrival.
      */
     public static class TimePickerFragment extends DialogFragment implements
             TimePickerDialog.OnTimeSetListener {
         
         /**
          * Minimum two digit number.
          */
         private static final int MIN_TWO_DIGIT_NUMBER = 10;
         
         @Override
         public final Dialog onCreateDialog(final Bundle savedInstanceState) {
             // Use the current time as the default values for the picker
             final Calendar calendar = Calendar.getInstance();
             int hour = calendar.get(Calendar.HOUR_OF_DAY);
             int minute = calendar.get(Calendar.MINUTE);
 
             // Create a new instance of TimePickerDialog and return it
             TimePickerDialog tpd = new TimePickerDialog(getActivity(), this, hour, minute, true);
             // Set the name of the dialog box
             tpd.setTitle(R.string.dialogbox_time);
             return tpd;
         }
 
         /**
          * Update the date EditText widget after an user picks a date.
          * @param view
          *            The TimePicker view whose date is set by user 
          * @param hourOfDay
          *            The hour of a day of the time picked
          * @param minute
          *            The minute of the time picked
          */
         public final void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
             EditText timeEditText = (EditText) getActivity().findViewById(R.id.editTextTime);
 
             // Formatting string be displayed
             String hourString = "";
             if (hourOfDay < MIN_TWO_DIGIT_NUMBER) {
                 hourString += "0";
             }
             hourString += hourOfDay;
 
             String minuteString = "";
             if (minute < MIN_TWO_DIGIT_NUMBER) {
                 minuteString += "0";
             }
             minuteString += minute;
 
             // Update the time EditText widget
             timeEditText.setText(hourString + ":" + minuteString);
         }
     }
 
     /**
      * An inner class that generates a request for routes and allows the backend
      * request to run on its own thread.
      * 
      * @author dutchscout, Shuo Wang(modification)
      */
     private class DirThread implements Runnable {
         
         /**
          * Fourth digit in a date/time.
          */
         private static final int DIGIT_FOURTH = 3;
         
         /**
          * Sixth digit in a date/time.
          */
         private static final int DIGIT_SIXTH = 5;
         
         /**
          * Seventh digit in a date/time.
          */
         private static final int DIGIT_SEVENTH = 6;
         
         /**
          * Eleventh digit in a date/time.
          */
         private static final int DIGIT_ELEVENTH = 10;
         
         /**
          * 1 second = 1000 milliseconds.
          */
         private static final int SEC_TO_MILLISEC = 1000;
         
         /**
          * {@inheritDoc}
          */
         @Override
         public void run() {
 
             // Set up time for direction request
 
             int month, dayOfMonth, year, hourOfDay, minute, second = 0;
 
             Time timeDeparture = new Time();
             if (leaveNowButton.isChecked()) {
                 hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                 minute = calendar.get(Calendar.MINUTE);
                 month = calendar.get(Calendar.MONTH);
                 dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                 year = calendar.get(Calendar.YEAR);
             } else {
                 String dateString = dateEditText.getText().toString();
                 String timeString = timeEditText.getText().toString();
 
                 // System uses month from 0 to 11 to represent January to
                 // December
                 month = Integer.parseInt(dateString.substring(0, 2)) - 1;
 
                 dayOfMonth = Integer.parseInt(dateString.substring(DIGIT_FOURTH, DIGIT_SIXTH));
                 year = Integer.parseInt(dateString.substring(DIGIT_SEVENTH, DIGIT_ELEVENTH));
                 hourOfDay = Integer.parseInt(timeString.substring(0, 2));
                 minute = Integer.parseInt(timeString.substring(DIGIT_FOURTH, DIGIT_SIXTH));
             }
 
             timeDeparture.set(second, minute, hourOfDay, dayOfMonth, month, year);
             long timeDepartureToSend = timeDeparture.toMillis(false) / SEC_TO_MILLISEC;
 
             // Generate a direction request
             DirectionsRequest dReq = (new DirectionsRequest())
                     .setStartAddress(startFromEditText.getText().toString())
                     .setEndAddress(toEditText.getText().toString()).setTravelMode(TravelMode.MIXED)
                     .setDepartureTime(timeDepartureToSend);
 
             DirectionsStatus retVal = dReq.doRequest();
 
             // If an error happens to the direction request
             // display an AlertDialog to let user to (re)start
             // a new request
             if (retVal.isError()) {
                 // Generates and displays error message
                 final CharSequence errorMessage = "Error getting directions: "
                         + dReq.getErrorMessages();
 
                 // Must call runOnUiThread if want to display a Toast or a
                 // Dialog within a thread
                 runOnUiThread(new Runnable() {
                     public void run() {
                         AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                         builder.setMessage(errorMessage).setTitle("Error")
                                 .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                     public void onClick(final DialogInterface dialog, 
                                                         final int id) {
                                         // Do nothing here currently since we
                                         // only need to
                                         // stay in SearchActvity page
                                     }
                                 });
                         // Create the AlertDialog object and return it
                         builder.create().show();
                     }
                 });
 
                 // Closes the searching dialog and stay in the search activity
                 pd.dismiss();
                 return;
             }
 
             // If no errors, send the result to ResultsActivity
             Intent intent = new Intent(SearchActivity.this, ResultsActivity.class);
             intent.putExtra("List of Routes", (Serializable) dReq.getSolutions());
             intent.putExtra("Current Route Index", 0);
 
             startActivity(intent);
 
             // Closes the searching dialog
             pd.dismiss();
         }
     }
 
     /**
      * The calendar visible within this SearchActivity as a source of time Note:
      * Calendar subclass instance is set to the current date and time in the
      * default Timezone, could be changed as users live in different timezones.
      */
     private final Calendar calendar = Calendar.getInstance();
 
     /**
      * "Leave Now" radio button.
      */
     private RadioButton leaveNowButton;
 
     /**
      * "Depart At" radio button.
      */
     private RadioButton departAtButton;
 
     /**
      * "Start from" EditText for starting address.
      */
     private EditText startFromEditText;
 
     /**
      * "To" EditText for destination address.
      */
     private EditText toEditText;
 
     /**
      * EditText for user to pick a date.
      */
     private EditText dateEditText;
 
     /**
      * EditText for user to pick a time.
      */
     private EditText timeEditText;
 
     /**
      * "Find" to start searching.
      */
     private Button findButton;
 
     /**
      * Reverse button for user to switch starting and destination address.
      */
     private ImageButton reverseButton;
 
     /**
      * A listView for showing typing history.
      */
     private ListView historyListView;
 
     /**
      * Keeps an array of history entries.
      */
     private HistoryItem[] historyItemData;
 
     /**
      * A progress dialog indicating the searching status of this activity.
      */
     private ProgressDialog pd;
 
     /**
      * {@inheritDoc}
      * 
      * @see android.app.Activity#onCreate(android.os.Bundle)
      */
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);
 
         establishViewsAndOtherNecessaryComponents();
         setInitialText();
         setListeners();
         setHistorySection();
     }
 
     /**
      * Show the menu bar when the setting button is clicked.
      * 
      * @param menu
      *            The options menu in which you place your items.
      * @return true if the menu to be displayed.
      * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
      */
     @Override
     public final boolean onCreateOptionsMenu(final Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_menu, menu);
         return true;
     }
 
     /**
      * This method will be called when user click buttons in the setting menu.
      * 
      * @param item
      *            the menu item that user will click
      * @return true if user select an item
      */
     @Override
     public final boolean onOptionsItemSelected(final MenuItem item) {
         switch (item.getItemId()) {
         case R.id.action_settings:
             // user click the setting button, start the settings activity
             Intent intent = new Intent(this, SettingsActivity.class);
             startActivity(intent);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Sets up default text to be displayed on the UI of this activity.
      */
     private void setInitialText() {
         int hour = calendar.get(Calendar.HOUR_OF_DAY);
         String hourString = "";
         if (hour < MIN_TWO_DIGIT_NUMBER) {
             hourString += "0";
         }
         hourString += hour;
 
         int minute = calendar.get(Calendar.MINUTE);
         String minuteString = "";
         if (minute < MIN_TWO_DIGIT_NUMBER) {
             minuteString += "0";
         }
         minuteString += minute;
 
         int year = calendar.get(Calendar.YEAR);
 
         int month = calendar.get(Calendar.MONTH);
         // System uses month from 0 to 11 to represent January to December
         month++;
 
         String monthString = "";
         if (month < MIN_TWO_DIGIT_NUMBER) {
             monthString += "0";
         }
         monthString += month;
 
         int day = calendar.get(Calendar.DAY_OF_MONTH);
         String dayString = "";
         if (day < MIN_TWO_DIGIT_NUMBER) {
             dayString += "0";
         }
         dayString += day;
 
         dateEditText.setText(monthString + "/" + dayString + "/" + year);
         timeEditText.setText(hourString + ":" + minuteString);
     }
 
     /**
      * Find and establish all UI components from xml to this activity.
      */
     private void establishViewsAndOtherNecessaryComponents() {
         departAtButton = (RadioButton) findViewById(R.id.radioButtonDepartAt);
         leaveNowButton = (RadioButton) findViewById(R.id.radioButtonLeaveNow);
         startFromEditText = (EditText) findViewById(R.id.editTextStartFrom);
         toEditText = (EditText) findViewById(R.id.editTextTo);
         dateEditText = (EditText) findViewById(R.id.editTextDate);
         timeEditText = (EditText) findViewById(R.id.editTextTime);
         findButton = (Button) findViewById(R.id.buttonFind);
         reverseButton = (ImageButton) findViewById(R.id.imageButtonReverse);
         historyListView = (ListView) findViewById(R.id.listViewHistory);
     }
 
     /**
      * Attach all listeners to corresponding UI widgets.
      */
     private void setListeners() {
         reverseButton.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 String temp = startFromEditText.getText().toString();
                 startFromEditText.setText(toEditText.getText().toString());
                 toEditText.setText(temp);
             }
         });
 
         leaveNowButton.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 dateEditText.setEnabled(false);
                 timeEditText.setEnabled(false);
             }
         });
 
         departAtButton.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 dateEditText.setEnabled(true);
                 timeEditText.setEnabled(true);
                 dateEditText.requestFocus();
             }
         });
 
         dateEditText.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 DialogFragment dpf = new DatePickerFragment();
                 dpf.show(getFragmentManager(), "datePicker");
             }
         });
 
         // Prohibit user from directly typing a date (instead, users should pick
         // a date)
         dateEditText.setKeyListener(null);
 
         timeEditText.setOnClickListener(new OnClickListener() {
             public void onClick(final View v) {
                 DialogFragment tpf = new TimePickerFragment();
                 tpf.show(getFragmentManager(), "timePicker");
             }
         });
 
         // Prohibit user from directly typing a time (instead, users should pick
         // a time)
         timeEditText.setKeyListener(null);
 
         findButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(final View v) {
                 pd = ProgressDialog.show(v.getContext(), "Searching", "Searching for routes...");
 
                 Thread dirThread = new Thread(new DirThread());
                 dirThread.start();
             }
         });
     }
 
     /**
      * Fill in the history section. TODO: currently hard-coded, creating a live
      * version in next phases. Since the data is dummy, I keep all the numbers
      * even if they are marked as magic numbers.
      */
     private void setHistorySection() {
         historyItemData = new HistoryItem[] {
                 new HistoryItem(1, "Paul G. Allen Center for Computer Science & Engineering (CSE)",
                         "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(2, "Guggenheim Hall (GUG)", "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(3, "Schmitz Hall (SMZ)", "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(4, "85 Pike St, Seattle, Washington", "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(5, "Mount Rainier National Park, Washington 98304",
                         "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(6, "7201 East Green Lake Dr N, Seattle, WA",
                         "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(7, "601 N 59th St, Seattle, WA", "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(8, "400 Broad St, Seattle, WA", "4311 11th Ave NE, Seattle, WA"),
                 new HistoryItem(9, "2623 NE University Village St #7, Seattle, WA",
                         "4311 11th Ave NE, Seattle, WA"), };
 
         HistoryAdapter adapter = new HistoryAdapter(this, R.layout.listview_history_item_row,
                 historyItemData);
 
        // Add title row to the history list
         View titleRow = (View) getLayoutInflater().inflate(R.layout.listview_title_row, null);
        
        // set false to make titleRow not clickable
        // NOTE: The index of titleRow is still 0 even false
        historyListView.addHeaderView(titleRow, null, false);
         
         historyListView.setAdapter(adapter);
 
         historyListView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(final AdapterView<?> parent, final View view,
                     final int position, final long id) {
                 // The first row (with index of 0) is the title row
                if (position < 1) {
                    return;
                }
                 startFromEditText.setText(historyItemData[position - 1].getFrom());
                 toEditText.setText(historyItemData[position - 1].getTo());
             }
         });
     }
 
 }
