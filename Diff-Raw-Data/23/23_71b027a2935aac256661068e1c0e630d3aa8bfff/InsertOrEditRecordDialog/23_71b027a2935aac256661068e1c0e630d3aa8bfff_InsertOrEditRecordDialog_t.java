 /*
  * Copyright (C) 2013 by Ethan Hall
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package io.ehdev.android.drivingtime.view.dialog;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.*;
 import dagger.ObjectGraph;
 import io.ehdev.android.drivingtime.R;
 import io.ehdev.android.drivingtime.backend.model.Record;
 import io.ehdev.android.drivingtime.backend.model.Task;
 import io.ehdev.android.drivingtime.database.dao.DatabaseHelper;
 import io.ehdev.android.drivingtime.module.ModuleGetters;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Period;
 
 import javax.inject.Inject;
 import java.util.List;
 
 public class InsertOrEditRecordDialog extends DialogFragment {
 
     private static final String TAG = InsertOrEditRecordDialog.class.getSimpleName();
 
     @Inject
    protected DatabaseHelper databaseHelper;
 
     private Record drivingRecord;
     private DateTime dateTimeForEntry;
     private List<Task> drivingTaskList;
 
     public InsertOrEditRecordDialog(Record drivingRecord, List<Task> drivingTaskList) {
 
         this.drivingRecord = drivingRecord;
         dateTimeForEntry = drivingRecord.getStartTime();
         this.drivingTaskList = drivingTaskList;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceDialog){
 
         ObjectGraph objectGraph = ObjectGraph.create(new ModuleGetters(getActivity()));
         objectGraph.inject(this);
 
         AlertDialog.Builder builder = createDialogAddButtons();
         builder.setTitle("Add Driving Time");
         return builder.create();
     }
 
 
     private AlertDialog.Builder createDialogAddButtons() {
         AlertDialog.Builder builder = createDialogBuilder();
         setOKButton(builder);
         setCancelButton(builder);
         return builder;
     }
 
     private AlertDialog.Builder createDialogBuilder() {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         View v = setupView();
         builder.setView(v);
         return builder;
     }
 
     private View setupView() {
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View v = inflater.inflate(R.layout.insert_record_dialog, null);
         setCurrentDateTime(v);
         setTimeViewParameters(v);
         setSpinnerAdapter(v);
         return v;
     }
 
     private void setCancelButton(AlertDialog.Builder builder) {
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 InsertOrEditRecordDialog.this.getDialog().cancel();
             }
         });
     }
 
     private void setOKButton(AlertDialog.Builder builder) {
         builder.setPositiveButton(R.string.addRecord, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 Dialog thisDialog = InsertOrEditRecordDialog.this.getDialog();
                 Task task = (Task) ((Spinner) thisDialog.findViewById(R.id.drivingTypeSpinner)).getSelectedItem();
                 Long durationOfEntry = getDurationOfEntry(thisDialog);
                 drivingRecord.setDrivingTask(task);
                 drivingRecord.setStartTime(dateTimeForEntry);
                 drivingRecord.setDurationOfDriving(new Duration(durationOfEntry));
                 try{
                     databaseHelper.getRecordDao().createOrUpdate(drivingRecord);
                 } catch (Exception e) {
                     Log.i(TAG, e.getMessage());
                 }
 
                 thisDialog.dismiss();
             }
         });
     }
 
     private Long getDurationOfEntry(Dialog thisDialog) {
         TimePicker timePicker = ((TimePicker)thisDialog.findViewById(R.id.durationPicker));
         return (timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute()) * 60 * 1000L;
     }
 
     private void setSpinnerAdapter(View v) {
         ArrayAdapter arrayAdapter =
                 new ArrayAdapter<Task>(
                         getActivity(),
                         android.R.layout.simple_spinner_item,
                         drivingTaskList);
         Spinner spinner = (Spinner) v.findViewById(R.id.drivingTypeSpinner);
         spinner.setAdapter(arrayAdapter);
         spinner.setSelection(getSelectedItem());
     }
 
     private int getSelectedItem() {
         return drivingTaskList.indexOf(drivingRecord.getDrivingTask());
     }
 
     private void setCurrentDateTime(View v) {
         setupDateButtonText(v);
         setupDateButtonDialog(v);
         setupTimeButtonText(v);
         setupTimeButtonDialog(v);
     }
 
     private void setupTimeButtonText(View v) {
         java.text.DateFormat formatter = DateFormat.getTimeFormat(getActivity());
         ((Button)v.findViewById(R.id.setTime)).setText(formatter.format(dateTimeForEntry.toDate()));
     }
 
     private void setupTimeButtonDialog(View v) {
         (v.findViewById(R.id.setTime)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 TimePickerDialog timePickerDialog =
                         new TimePickerDialog(
                                 getActivity(),
                                 new DateTimeSelector(),
                                 dateTimeForEntry.getHourOfDay(),
                                 dateTimeForEntry.getMinuteOfHour(),
                                 DateFormat.is24HourFormat(getActivity()));
                 timePickerDialog.show();
             }
         });
     }
 
     private void setupDateButtonText(View v) {
         java.text.DateFormat formatter = DateFormat.getLongDateFormat(getActivity());
         ((Button)v.findViewById(R.id.setDate)).setText(formatter.format(dateTimeForEntry.toDate()));
     }
 
     private void setupDateButtonDialog(View v) {
         (v.findViewById(R.id.setDate)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 DatePickerDialog datePickerDialog =
                         new DatePickerDialog(
                                 getActivity(),
                                 new DateTimeSelector(),
                                 dateTimeForEntry.getYear(),
                                 dateTimeForEntry.getMonthOfYear(),
                                 dateTimeForEntry.getDayOfMonth());
                 datePickerDialog.show();
             }
         });
     }
 
     private class DateTimeSelector implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
 
         @Override
         public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
             Log.d(TAG, String.format("year: %d, month: %d, day: %d", year, monthOfYear,dayOfMonth));
             dateTimeForEntry = dateTimeForEntry.withYear(year);
             dateTimeForEntry = dateTimeForEntry.withMonthOfYear(monthOfYear);
             dateTimeForEntry = dateTimeForEntry.withDayOfMonth(dayOfMonth);
             setupDateButtonText(InsertOrEditRecordDialog.this.getDialog().findViewById(R.id.setDate));
         }
 
         @Override
         public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
             Log.d(TAG, String.format("hour: %d, min: %d", hourOfDay, minute));
             dateTimeForEntry = dateTimeForEntry.withHourOfDay(hourOfDay);
             dateTimeForEntry = dateTimeForEntry.withMinuteOfHour(minute);
             setupTimeButtonText(InsertOrEditRecordDialog.this.getDialog().findViewById(R.id.setTime));
         }
     }
 
     private void setTimeViewParameters(View view) {
         Period duration = drivingRecord.getDurationOfDriving().toPeriod();
         ((TimePicker)view.findViewById(R.id.durationPicker)).setIs24HourView(true);
         ((TimePicker)view.findViewById(R.id.durationPicker)).setCurrentHour(duration.getHours());
         ((TimePicker)view.findViewById(R.id.durationPicker)).setCurrentMinute(duration.getMinutes());
     }
 }
