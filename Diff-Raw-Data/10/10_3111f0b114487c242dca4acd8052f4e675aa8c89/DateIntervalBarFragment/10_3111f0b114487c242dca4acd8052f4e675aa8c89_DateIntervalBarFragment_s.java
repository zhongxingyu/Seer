 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.fragments;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.alexjf.tmm.R;
 import net.alexjf.tmm.utils.PreferenceManager;
 
 import android.app.Activity;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.Spinner;
 
 public class DateIntervalBarFragment extends Fragment 
     implements OnDateSetListener {
     private static String KEY_CURRENTSTARTDATE = "startDate";
     private static String KEY_CURRENTENDDATE = "endDate";
     private static String KEY_SPINNERSELECTION = "spinnerSelection";
     private static String KEY_STARTDATEEDIT = "startTimeEdit";
 
     private static String PREFKEY_DEFAULTTIMEINTERVAL = "pref_key_default_timeinterval";
 
     private static String TAG_DATEPICKER = "datePicker";
 
     private String[] dateIntervalTypes;
 
     private OnDateIntervalChangedListener listener;
 
     private Calendar startDate;
     private Calendar endDate;
     private static DateFormat dateTimeFormat = DateFormat.getDateTimeInstance();
     private static DateFormat dateFormat = DateFormat.getDateInstance();
     private boolean startDateBeingEdited;
     private boolean allTime;
 
     private DatePickerFragment datePicker;
 
     private Spinner dateIntervalSpinner;
     private Button startDateButton;
     private Button endDateButton;
     private View customSelector;
 
     public interface OnDateIntervalChangedListener {
         /**
          * Receives notification of date interval change on DateIntervalBar.
          * 
          * New date interval between startDate and endDate. If these are null,
          * no limit established on start/end.
          *
          * @param startDate Starting date of interval.
          * @param endDate Ending date of interval.
          */
         public void onDateIntervalChanged(Date startDate, Date endDate);
     }
 
     public DateIntervalBarFragment() {
         allTime = false;
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             final Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_dateinterval_bar, container, false);
 
         dateIntervalTypes = getResources().getStringArray(R.array.date_intervals);
 
         dateIntervalSpinner = (Spinner) v.findViewById(R.id.interval_spinner);
         startDateButton = (Button) v.findViewById(R.id.start_button);
         endDateButton = (Button) v.findViewById(R.id.end_button);
         customSelector = v.findViewById(R.id.custom_selector);
 
         int selectedSpinnerPos = 0;
 
         if (savedInstanceState != null) {
             try {
                 startDate.setTime(dateTimeFormat.parse(
                     savedInstanceState.getString(KEY_CURRENTSTARTDATE)));
             } catch (ParseException e) {
                 Log.e("TMM", "Error parsing saved start date", e);
             }
             try {
                 endDate.setTime(dateTimeFormat.parse(
                     savedInstanceState.getString(KEY_CURRENTENDDATE)));
             } catch (ParseException e) {
                 Log.e("TMM", "Error parsing saved end date", e);
             }
 
             selectedSpinnerPos = savedInstanceState.getInt(
                         KEY_SPINNERSELECTION);
             startDateBeingEdited = 
                 savedInstanceState.getByte(KEY_STARTDATEEDIT) == 1;
         } else {
             PreferenceManager prefManager = PreferenceManager.getInstance();
             String prefDateInterval = prefManager.readUserStringPreference(
                     PREFKEY_DEFAULTTIMEINTERVAL, null);
             if (prefDateInterval != null) { 
                 selectedSpinnerPos = Arrays.asList(dateIntervalTypes).indexOf(
                         prefDateInterval);
             }
         }
 
         dateIntervalSpinner.setSelection(selectedSpinnerPos, false);
         final int positionToSelect = selectedSpinnerPos;
 
         // Hack to ignore initial onItemSelecteds due to layout and measure
         dateIntervalSpinner.post(new Runnable() {
             public void run() {
                 dateIntervalSpinner.setOnItemSelectedListener(
                     new OnItemSelectedListener() {
                         public void onItemSelected(AdapterView<?> parent, View view, 
                             int position, long id) {
                             setDateInterval(position);
                             notifyDatesChanged();
                         };
 
                         public void onNothingSelected(AdapterView<?> parent) {
                         };
                     });
 
                 setDateInterval(positionToSelect);
                 notifyDatesChanged();
             }
         });
 
         datePicker = (DatePickerFragment) 
             getFragmentManager().findFragmentByTag(TAG_DATEPICKER);
 
         if (datePicker == null) {
             datePicker = new DatePickerFragment();
         }
 
         datePicker.setListener(this);
 
         startDateButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 try {
                     datePicker.setDate(dateFormat.parse(
                             startDateButton.getText().toString()));
                 } catch (ParseException e) {
                 }
                 startDateBeingEdited = true;
                 datePicker.show(getFragmentManager(), TAG_DATEPICKER);
             }
         });
 
         endDateButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 try {
                     datePicker.setDate(dateFormat.parse(
                             endDateButton.getText().toString()));
                 } catch (ParseException e) {
                 }
                 startDateBeingEdited = false;
                 datePicker.show(getFragmentManager(), TAG_DATEPICKER);
             }
         });
 
         return v;
     }
 
     @Override
     public void onDestroyView() {
         // Hack to ignore initial onItemSelecteds due to layout and measure
         dateIntervalSpinner.setOnItemSelectedListener(null);
         super.onDestroyView();
     };
 
     @Override
     public void onDateSet(DatePicker view, int year, int month, int day) {
         Calendar calendar;
 
         if (startDateBeingEdited) {
             calendar = startDate;
         } else {
             calendar = endDate;
         }
 
         calendar.set(year, month, day);
         updateDateButtons();
         notifyDatesChanged();
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             listener = (OnDateIntervalChangedListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + 
                     " must implement OnDateIntervalChangedListener");
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString(KEY_CURRENTSTARTDATE, 
                 dateTimeFormat.format(startDate.getTime()));
         outState.putString(KEY_CURRENTENDDATE, 
                 dateTimeFormat.format(endDate.getTime()));
         outState.putInt(KEY_SPINNERSELECTION,
                 dateIntervalSpinner.getSelectedItemPosition());
         outState.putByte(KEY_STARTDATEEDIT,
                 (byte) (startDateBeingEdited ? 1 : 0));
         super.onSaveInstanceState(outState);
     }
 
     public boolean isAllTime() {
         return allTime;
     }
 
     public Date getStartDate() {
         return startDate.getTime();
     }
 
     public Date getEndDate() {
         return endDate.getTime();
     }
 
     public void setStartDate(Date date) {
         startDate.setTime(date);
         updateDateButtons();
         notifyDatesChanged();
     }
 
     public void setEndDate(Date date) {
         endDate.setTime(date);
         updateDateButtons();
         notifyDatesChanged();
     }
 
     private void notifyDatesChanged() {
         if (listener == null) {
             return;
         }
 
         if (allTime) {
             listener.onDateIntervalChanged(null, null);
         } else {
             listener.onDateIntervalChanged(startDate.getTime(), endDate.getTime());
         }
     }
 
     private void resetDates() {
        Date today = new Date();
        startDate.setTime(today);
         startDate.set(Calendar.HOUR_OF_DAY, 0);
         startDate.set(Calendar.MINUTE, 0);
         startDate.set(Calendar.SECOND, 0);
        endDate.setTime(today);
         endDate.set(Calendar.HOUR_OF_DAY, 23);
         endDate.set(Calendar.MINUTE, 59);
         endDate.set(Calendar.SECOND, 59);
     }
 
     private void setDateInterval(int position) {
         // If Custom
         if (position == 6) {
             allTime = false;
             customSelector.setVisibility(View.VISIBLE);
             updateDateButtons();
         } 
         // If all time
         else if (position == 5) {
             allTime = true;
             customSelector.setVisibility(View.GONE);
         }
         else {
             allTime = false;
             customSelector.setVisibility(View.GONE);
             resetDates();
             switch (position) {
                 // Yesterday
                 case 1:
                     startDate.add(Calendar.DAY_OF_MONTH, -1);
                     endDate.add(Calendar.DAY_OF_MONTH, -1);
                     break;
                 // This month
                 case 2:
                     startDate.set(Calendar.DAY_OF_MONTH, 1);
                     endDate.set(Calendar.DAY_OF_MONTH, 
                         endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                     break;
                 // Last month
                 case 3:
                     startDate.add(Calendar.MONTH, -1);
                     endDate.add(Calendar.MONTH, -1);
                     startDate.set(Calendar.DAY_OF_MONTH, 1);
                     endDate.set(Calendar.DAY_OF_MONTH, 
                         endDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                     break;
                 // This year
                 case 4:
                     startDate.set(Calendar.DAY_OF_YEAR, 1);
                     endDate.set(Calendar.DAY_OF_YEAR, 
                         endDate.getActualMaximum(Calendar.DAY_OF_YEAR));
                     break;
                 case 0:
                 default:
                     break;
             }
         }
     }
 
     private void updateDateButtons() {
         String startDateString = dateFormat.format(startDate.getTime());
         String endDateString = dateFormat.format(endDate.getTime());
 
         startDateButton.setText(startDateString);
         endDateButton.setText(endDateString);
     }
 }
