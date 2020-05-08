 package com.jason.ocbcapp;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Scanner;
 import java.util.TimeZone;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.util.Pair;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 
 public class AppointmentsFragment extends Fragment {
 
     private static final String APP_TAG = MainActivity.APP_TAG;
 
     public enum Services {
         ACCOUNT_OPENING, CREDIT_CARD, LOAN, OTHERS
     };
 
     ArrayList<CharSequence> dateList;
     static ArrayAdapter<CharSequence> dateAdapter;
     static SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM, yyyy");
     static Calendar chosenDate = Calendar.getInstance();
 
     static Spinner branchesSpinner = null;
     Spinner dateSpinner = null;
     Spinner timeSpinner = null;
 
     ArrayList<String> selectedServices = null;
     ArrayList<CheckBox> checkboxes = null;
 
     Dialog loadingDialog = null;
     AlertDialog successDialog = null;
 
     public AppointmentsFragment() {
 
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         View vi = getView();
         // Setup date spinner
         dateSpinner = (Spinner) vi.findViewById(R.id.dateSpinner);
         timeSpinner = (Spinner) vi.findViewById(R.id.timeSpinner);
 
         dateList = new ArrayList<CharSequence>();
 
         // set the picker's default date to currentDate
         Date currentDate = new Date();
         dateList.add(df.format(currentDate));
         dateAdapter = new ArrayAdapter<CharSequence>(vi.getContext(),
                 android.R.layout.simple_spinner_dropdown_item, dateList);
         dateSpinner.setAdapter(dateAdapter);
 
         if (dateList.size() < 2) {
             dateSpinner.setClickable(false);
             dateSpinner.setOnTouchListener(new View.OnTouchListener() {
 
                 @Override
                 public boolean onTouch(View v, MotionEvent event) {
                     // TODO Auto-generated method stub
                     if (event.getAction() == MotionEvent.ACTION_DOWN) {
                         DialogFragment newFragment = new DatePickerFragment();
                         newFragment.show(getFragmentManager(), "datePicker");
                         return true;
                     } else
                         return false;
                 }
             });
 
         }
 
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
         builder.setMessage(R.string.label_queue_text).setTitle(
                 "Appointment Booked");
         builder.setPositiveButton("Got it!",
                 new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 });
         successDialog = builder.create();
 
         branchesSpinner = (Spinner) vi.findViewById(R.id.branchesSpinner);
 
         CheckBox accCheckBox = (CheckBox) vi.findViewById(R.id.accCheckBox);
         CheckBox ccCheckBox = (CheckBox) vi.findViewById(R.id.ccCheckBox);
         CheckBox loanCheckBox = (CheckBox) vi.findViewById(R.id.loanCheckBox);
         CheckBox othersCheckBox = (CheckBox) vi
                 .findViewById(R.id.othersCheckBox);
 
         checkboxes = new ArrayList<CheckBox>();
         checkboxes.add(accCheckBox);
         checkboxes.add(ccCheckBox);
         checkboxes.add(loanCheckBox);
         checkboxes.add(othersCheckBox);
         selectedServices = new ArrayList<String>();
 
         // init loading screen
         loadingDialog = new Dialog(getActivity());
         loadingDialog.setContentView(R.layout.dialog_loading_screen);
 
         // Setup submit button
         Button submitButton = (Button) vi.findViewById(R.id.submitButton);
         submitButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // setup variables to send
                 String dateString = "" + getSelectedTimestamp();
                 String userToken = CrossCutting.getUserTokenFromPreferences(getActivity());
                 String branchId = "" + getSelectedBranchId();
                 JSONObject jobj = new JSONObject();
                 JSONArray jsonStates = new JSONArray();
                 for (int i = 0; i < checkboxes.size(); i++) {
                     if (checkboxes.get(i).isChecked()) {
                         Services service = Services.values()[i];
                         String serviceStr = getServiceString(service);
                         selectedServices.add(serviceStr);
                         jsonStates.put(serviceStr);
                     }
                 }
                 try {
                     jobj.accumulate("appDate", dateString);
                     jobj.accumulate("branchId", branchId);
                     jobj.accumulate("tokenOfCustomer", userToken);
                     jobj.accumulate("serviceType", jsonStates);
                     Log.d(APP_TAG, "json object: " + jobj.toString());
                     SumbitFormTask task = new SumbitFormTask();
                     String url = "http://cutebalrog.com:8080/OCBC-QM-Server-web/webresources/Queue/addOnlineQueue";
                     task.execute(new Pair<String, JSONObject>(url, jobj));
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
                 // show loading spinner
                 loadingDialog.show();
             }
         });
     }
 
     protected String getServiceString(Services service) {
         switch (service) {
         case ACCOUNT_OPENING:
             return "account_opening";
         case CREDIT_CARD:
             return "credit_card";
         case LOAN:
             return "loan";
         case OTHERS:
             return "others";
         default:
             return "ERROR!";
         }
     }
 
     protected long getSelectedTimestamp() {
         int[] timeSlots = getResources().getIntArray(R.array.start_time_slots);
         int selectedTime = timeSlots[timeSpinner.getSelectedItemPosition()];
         // set hour of day and leave mins, sec and ms blank
         chosenDate.setTimeZone(TimeZone.getTimeZone("GMT"));
         chosenDate.set(Calendar.HOUR_OF_DAY, selectedTime);
         chosenDate.set(Calendar.MINUTE, 0);
         chosenDate.set(Calendar.SECOND, 0);
         chosenDate.set(Calendar.MILLISECOND, 0);
         Log.d(APP_TAG, "cal datetime: " + chosenDate.toString());
         Log.d(APP_TAG, "cal getTimeInMillis datetime: "
                 + chosenDate.getTimeInMillis());
         return chosenDate.getTimeInMillis();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View apptView = inflater.inflate(R.layout.fragment_appt, container,
                 false);
 
         return apptView;
     }
 
     /**
      * @param year
      * @param month
      * @param day
      */
     private void changeDateShownInForm(Calendar chosenDate) {
         dateAdapter.clear();
         dateAdapter.add(df.format(chosenDate.getTime()));
     }
 
     private void getAvailableSlots(Calendar chosenDate) {
         int id = getSelectedBranchId();
         int year = chosenDate.get(Calendar.YEAR);
         int month = chosenDate.get(Calendar.MONTH);
         int dayOfMonth = chosenDate.get(Calendar.DATE);
         String url = String
                 .format("http://cutebalrog.com:8080/OCBC-QM-Server-web/webresources/Branch/GetBranchApptSlot/%d/%d/%d/%d",
                        id, year, month, dayOfMonth);
         Log.d(APP_TAG, "date url = " + url);
         String urls[] = { url };
         loadingDialog.show();
         GetAvailableTimeSlotsTask task = new GetAvailableTimeSlotsTask();
         task.execute(urls);
     }
 
     /*
      * Assumes that position of item in list = branch id.
      */
     private static int getSelectedBranchId() {
         return branchesSpinner.getSelectedItemPosition();
     }
 
     @SuppressLint("ValidFragment")
     public class DatePickerFragment extends DialogFragment implements
             DatePickerDialog.OnDateSetListener {
 
        boolean fired = false;
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the chosen date as the default date in the picker
             // Chosen date defaults to current date if the date is not yet chosen
             // See {@link Calendar.getInstance()}
             int year = chosenDate.get(Calendar.YEAR);
             int month = chosenDate.get(Calendar.MONTH);
             int day = chosenDate.get(Calendar.DAY_OF_MONTH);
 
             // Create a new instance of DatePickerDialog and return it
             return new DatePickerDialog(getActivity(), this, year, month, day);
         }
 
         /*
          * Once user has set the date, we want to change the form's date to the
          * chose date. Next, we should query the server for the time slots that
          * are available.
          */
         @Override
         public void onDateSet(DatePicker view, int year, int month, int day) {
            chosenDate = new GregorianCalendar(year, month, day);
            changeDateShownInForm(chosenDate);
            getAvailableSlots(chosenDate);
         }
     }
 
     class GetAvailableTimeSlotsTask extends AsyncTask<String, String, String> {
 
         private int responseCode = 0;
 
         @Override
         protected String doInBackground(String... urls) {
             String responseString = null;
             InputStream responseStream = null;
             HttpURLConnection urlConnection = null;
             try {
                 URL url = new URL(urls[0]);
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.addRequestProperty("Content-type",
                         "application/json");
                 urlConnection.connect();
 
                 responseStream = new BufferedInputStream(urlConnection
                         .getInputStream());
                 responseCode = urlConnection.getResponseCode();
                 Log.d(APP_TAG, "response code: " + responseCode);
                 responseString = CrossCutting.readStream(responseStream);
             } catch (Exception e) {
                 Log.e(APP_TAG, e.getMessage());
             }
             return responseString;
         }
 
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             // Extract waiting time from json object
             loadingDialog.dismiss();
             Log.d(APP_TAG, "finished get available timeslots task");
             Log.d(APP_TAG, "result: " + result);
             Boolean noResponse = responseCode == HttpURLConnection.HTTP_NO_CONTENT;
             Boolean isSuccessful = responseCode == HttpURLConnection.HTTP_OK;
             if (noResponse) {
                 showBranchIsClosedDialog();
             } else if (isSuccessful) {
                 ArrayList<Integer> availableStartingTimeSlots = new ArrayList<Integer>();
                 extractStartingTimeSlotsFromJson(result,
                         availableStartingTimeSlots);
                 showAvailableSlots(availableStartingTimeSlots);
             }
 
         }
 
         /**
          * @param result
          * @param availableStartingTimeSlots
          */
         private void extractStartingTimeSlotsFromJson(String result,
                 ArrayList<Integer> availableStartingTimeSlots) {
             try {
                 JSONObject jObj = new JSONObject(result);
                 JSONArray branchApptSlots = jObj.optJSONArray("branchApptSlot");
                 Log.d(APP_TAG, "branchApptSlots = "
                         + branchApptSlots.toString());
                 for (int i = 0; i < branchApptSlots.length(); i++) {
                     JSONObject slot = branchApptSlots.optJSONObject(i);
                     int numberOfPfc = slot.optInt("pfc", 0);
                     int time = slot.optInt("time", 0);
                     Boolean slotIsAvailable = numberOfPfc != 0;
                     if (slotIsAvailable)
                         availableStartingTimeSlots.add(time / 100);
                 }
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             Log.d(APP_TAG, "avail slots = "
                     + availableStartingTimeSlots.toString());
         }
     }
 
     class SumbitFormTask extends
             AsyncTask<Pair<String, JSONObject>, String, String> {
 
         @Override
         protected String doInBackground(Pair<String, JSONObject>... pair) {
             String responseString = null;
             InputStream responseStream = null;
             HttpURLConnection urlConnection = null;
             OutputStreamWriter writer = null;
             try {
                 URL url = new URL(pair[0].first);
                 JSONObject data = pair[0].second;
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setDoOutput(true);
                 urlConnection.setRequestProperty("Accept", "application/json");
                 urlConnection.addRequestProperty("Content-type",
                         "application/json");
                 urlConnection.setRequestMethod("POST");
                 urlConnection.connect();
 
                 writer = new OutputStreamWriter(urlConnection.getOutputStream());
                 writer.write(data.toString());
                 writer.close();
                 responseStream = new BufferedInputStream(urlConnection
                         .getInputStream());
                 Log.d(APP_TAG, "response code: "
                         + urlConnection.getResponseCode());
                 responseString = CrossCutting.readStream(responseStream);
             } catch (Exception e) {
                 Log.e(APP_TAG, e.getMessage());
             }
             return responseString;
         }
 
         @Override
         protected void onPostExecute(String result) {
             super.onPostExecute(result);
             // Extract waiting time from json object
             Log.d(APP_TAG, "finished post request task");
             Log.d(APP_TAG, "result: " + result);
             loadingDialog.dismiss();
 
             int queueNumber = Integer.parseInt(result);
             showQueueNumberToUser(queueNumber);
         }
 
         /**
          * @param queueNumber
          */
         private void showQueueNumberToUser(int queueNumber) {
             successDialog.setMessage(getString(R.string.label_queue_text) + " "
                     + queueNumber);
             successDialog.show();
         }
     }
 
     public void showAvailableSlots(ArrayList<Integer> availableStartingTimeSlots) {
         ArrayList<String> availableTimeSlots = new ArrayList<String>();
         SparseArray<String> mapStartTimeToSlots = makeTimeSlotsMap(this.getActivity());
         for (Integer slot : availableStartingTimeSlots) {
             availableTimeSlots.add(mapStartTimeToSlots.get(slot));
         }
         timeSpinner.setAdapter(new ArrayAdapter<String>(this.getActivity(),
                 android.R.layout.simple_spinner_dropdown_item,
                 availableTimeSlots));
     }
 
     public void showBranchIsClosedDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
         Dialog branchClosedDialog = null;
         builder.setMessage(R.string.label_branch_closed_on_day).setTitle(
                 "Branch Closed");
         builder.setPositiveButton("Okay",
                 new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.dismiss();
                     }
                 });
         branchClosedDialog = builder.create();
         branchClosedDialog.show();
     }
 
     public static SparseArray<String> makeTimeSlotsMap(Context context) {
         SparseArray<String> timeSlotMap = new SparseArray<String>();
         int[] startingTimeSlots = context.getResources().getIntArray(R.array.start_time_slots);
         String[] timeSlots = context.getResources().getStringArray(R.array.time_slots);
         for (int i = 0; i < startingTimeSlots.length; i++) {
             timeSlotMap.put(startingTimeSlots[i], timeSlots[i]);
         }
         return timeSlotMap;
     }
 
 }
