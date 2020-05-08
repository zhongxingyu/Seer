 package com.alexwlsnr.cycletimecalc;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.DatePicker;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import java.util.Calendar;
 
 import static com.alexwlsnr.cycletimecalc.CycleTimeUtils.getCycleTime;
 
 public class MainActivity extends FragmentActivity {
 
     private int startHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
     private int endHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
     private Boolean calculateEndDate = false;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         System.setProperty("org.joda.time.DateTimeZone.Provider",
                 "com.alexwlsnr.cycletimecalc.FastDateTimeZoneProvider");
         setContentView(R.layout.activity_main);
     }
 
 
     @Override
     protected void onStart()
     {
         super.onStart();
         configureSpinners();
     }
 
 
 
 
     private void configureSpinners()
     {
         String[] hoursArray = getResources().getStringArray(R.array.hours_array);
 
         Spinner startHourSpinner = (Spinner) findViewById(R.id.startHourSpinner);
     // Create an ArrayAdapter using the string array and a default spinner layout
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                 R.array.hours_array, android.R.layout.simple_spinner_item);
     // Specify the layout to use when the list of choices appears
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     // Apply the adapter to the spinner
         startHourSpinner.setAdapter(adapter);
 
         Spinner endHourSpinner = (Spinner) findViewById(R.id.endHourSpinner);
         // Apply the adapter to the spinner
         endHourSpinner.setAdapter(adapter);
 
         setSpinnerValue(startHourSpinner, startHour);
         setSpinnerValue(endHourSpinner, endHour);
 
     }
 
 
     private void setSpinnerValue(Spinner spinnerToSet, int value)
     {
         int spinnerItemCount = spinnerToSet.getAdapter().getCount();
         int spinnerMatchedItemId = -1;
         for(int i = 0; i < spinnerItemCount; i++)
         {
             if(Integer.toString(value).equals((String)spinnerToSet.getAdapter().getItem(i)))
             {
                 spinnerMatchedItemId = i;
             }
 
         }
 
         if(spinnerMatchedItemId != -1)
         {
             spinnerToSet.setSelection(spinnerMatchedItemId);
         }
         else if (value < Integer.parseInt((String)spinnerToSet.getAdapter().getItem(0)))
         {
             spinnerToSet.setSelection(0);
         }
         else if (value > Integer.parseInt((String)spinnerToSet.getAdapter().getItem(spinnerItemCount - 1)))
         {
             spinnerToSet.setSelection(0);
         }
 
 
     }
     public void resetUi(View v)
     {
         configureSpinners();
         Calendar currentDate = Calendar.getInstance();
         DatePicker startDatePicker = (DatePicker) findViewById(R.id.startDatePicker);
         DatePicker endDatePicker = (DatePicker) findViewById(R.id.endDatePicker);
         startDatePicker.getCalendarView().setDate(currentDate.getTimeInMillis());
         endDatePicker.getCalendarView().setDate(currentDate.getTimeInMillis());
     }
 
 
     public void calculate(View v)
     {
         Spinner startHourSpinner = (Spinner) findViewById(R.id.startHourSpinner);
         int startHour = Integer.parseInt((String)startHourSpinner.getSelectedItem());
         DatePicker startDatePicker = (DatePicker) findViewById(R.id.startDatePicker);
         DateTime startDate = new DateTime(startDatePicker.getYear(), startDatePicker.getMonth() + 1, startDatePicker.getDayOfMonth(), startHour, 0);
         DateTime endDate;
         if (calculateEndDate)
         {
             Spinner endHourSpinner = (Spinner) findViewById(R.id.endHourSpinner);
             int endHour = Integer.parseInt((String)endHourSpinner.getSelectedItem());
             DatePicker endDatePicker = (DatePicker) findViewById(R.id.endDatePicker);
             endDate = new DateTime(endDatePicker.getYear(), endDatePicker.getMonth() + 1, endDatePicker.getDayOfMonth(), endHour, 0);
         }
         else
         {
             Calendar current = Calendar.getInstance();
            endDate = new DateTime(current.get(Calendar.YEAR), current.get(Calendar.MONTH) + 1,
                    current.get(Calendar.DAY_OF_MONTH), current.get(Calendar.HOUR_OF_DAY), 0);
         }
 
 
         int cycleTime = getCycleTime(startDate, endDate);
         TextView resultsArea = (TextView) findViewById(R.id.cycleTimeTextView);
 
         resultsArea.setText(Integer.toString(cycleTime));
     }
 
     public void scanQr(View v)
     {
         try {
 
             Intent intent = new Intent("com.google.zxing.client.android.SCAN");
             intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
 
             startActivityForResult(intent, 0);
 
         } catch (Exception e) {
 
             Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
             Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
             startActivity(marketIntent);
 
         }
 
     }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == 0) {
 
             if (resultCode == RESULT_OK) {
                 String contents = data.getStringExtra("SCAN_RESULT");
                 String[] parts =  contents.split(";");
                 String date = parts[parts.length-1];
                 DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH");
                 DateTime startDate = dateStringFormat.parseDateTime(date);
                 DatePicker startDatePicker = (DatePicker) findViewById(R.id.startDatePicker);
                 startDatePicker.getCalendarView().setDate(startDate.getMillis());
                 startHour = startDate.hourOfDay().get();
 //                Spinner startHourSpinner = (Spinner) findViewById(R.id.startHourSpinner);
 //                setSpinnerValue(startHourSpinner, startDate.hourOfDay().get());
             }
             if(resultCode == RESULT_CANCELED){
                 //handle cancel
 
             }
         }
     }
 
     public void onEndDateEnabledToggleClicked(View view) {
 
         boolean on = ((ToggleButton) view).isChecked();
         RelativeLayout tl = (RelativeLayout)findViewById(R.id.endDateSection);
         tl.setVisibility(on ? View.VISIBLE: View.GONE);
         calculateEndDate = !on;
 
     }
 
 
 }
