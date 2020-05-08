 /*
  * Copyright 2012 Matthew Precious
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.mattprecious.smsfix.library;
 
 import java.text.DecimalFormat;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.text.InputType;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.TimePicker;
 
 import com.mattprecious.smsfix.library.util.SmsMmsDbHelper;
 import com.mattprecious.smsfix.library.util.TimeHelper;
 
 public class FixOld extends FragmentActivity {
     private final Uri SMS_URI = SmsMmsDbHelper.getSmsUri();
     private final Uri MMS_URI = SmsMmsDbHelper.getMmsUri();
     
     private final int DIALOG_ID_START_DATE_PICKER = 1;
     private final int DIALOG_ID_START_TIME_PICKER = 2;
     private final int DIALOG_ID_END_DATE_PICKER = 3;
     private final int DIALOG_ID_END_TIME_PICKER = 4;
     private final int DIALOG_ID_OFFSET_PICKER = 5;
     private final int DIALOG_ID_CONFIRM = 6;
     
     private enum Type { SMS, MMS };
 
     private Button startDateButton;
     private Button startTimeButton;
     private Button endDateButton;
     private Button endTimeButton;
     private Button signButton;
     private Button offsetButton;
     private Button typeButton;
     private Button goButton;
 
     private Calendar startCalendar;
     private Calendar endCalendar;
     private long offset;
     private Type type = Type.SMS;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.fix_old);
 
         startCalendar = Calendar.getInstance();
         startCalendar.set(Calendar.SECOND, 0);
         startCalendar.set(Calendar.MILLISECOND, 0);
 
         endCalendar = (Calendar) startCalendar.clone();
         offset = TimeHelper.getOffset(this);
 
         startDateButton = (Button) findViewById(R.id.start_date_button);
         startTimeButton = (Button) findViewById(R.id.start_time_button);
         endDateButton = (Button) findViewById(R.id.end_date_button);
         endTimeButton = (Button) findViewById(R.id.end_time_button);
         signButton = (Button) findViewById(R.id.sign_button);
         offsetButton = (Button) findViewById(R.id.offset_button);
         typeButton = (Button) findViewById(R.id.type_button);
         goButton = (Button) findViewById(R.id.go_button);
         
         if (MMS_URI == null) {
             typeButton.setVisibility(View.GONE);
         }
 
         startDateButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_ID_START_DATE_PICKER);
             }
         });
 
         startTimeButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_ID_START_TIME_PICKER);
             }
         });
 
         endDateButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_ID_END_DATE_PICKER);
             }
         });
 
         endTimeButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_ID_END_TIME_PICKER);
             }
         });
 
         signButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 offset *= -1;
                 updateButtons();
             }
         });
 
         offsetButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_ID_OFFSET_PICKER);
             }
         });
         
         typeButton.setOnClickListener(new OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 if (type == Type.SMS) {
                     type = Type.MMS;
                 } else {
                     type = Type.SMS;
                 }
                 
                 updateButtons();
             }
         });
 
         goButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 showDialog(DIALOG_ID_CONFIRM);
             }
         });
 
         updateButtons();
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Dialog dialog;
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         switch (id) {
             case DIALOG_ID_START_DATE_PICKER:
                 dialog = new DatePickerDialog(this, new OnDateSetListener() {
 
                     @Override
                     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                         startCalendar.set(year, monthOfYear, dayOfMonth);
 
                         updateButtons();
                     }
                 }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                         startCalendar.get(Calendar.DAY_OF_MONTH));
                 break;
             case DIALOG_ID_START_TIME_PICKER:
                 dialog = new TimePickerDialog(this, new OnTimeSetListener() {
 
                     @Override
                     public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                         startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                         startCalendar.set(Calendar.MINUTE, minute);
 
                         updateButtons();
                     }
                 }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE),
                         DateFormat.is24HourFormat(this));
                 break;
             case DIALOG_ID_END_DATE_PICKER:
                 dialog = new DatePickerDialog(this, new OnDateSetListener() {
 
                     @Override
                     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                         endCalendar.set(year, monthOfYear, dayOfMonth);
 
                         updateButtons();
                     }
                 }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar
                         .get(Calendar.DAY_OF_MONTH));
                 break;
             case DIALOG_ID_END_TIME_PICKER:
                 dialog = new TimePickerDialog(this, new OnTimeSetListener() {
 
                     @Override
                     public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                         endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                         endCalendar.set(Calendar.MINUTE, minute);
 
                         updateButtons();
                     }
                 }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE),
                         DateFormat.is24HourFormat(this));
                 break;
             case DIALOG_ID_OFFSET_PICKER:
                 builder.setTitle(R.string.offset_hours);
 
                 final EditText editText = new EditText(this);
 
                 DecimalFormat df = new DecimalFormat("#.###");
                 editText.setText(df.format(Math.abs(offset) / 3600000f));
                 editText.setInputType(InputType.TYPE_CLASS_NUMBER
                         | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 
                 builder.setView(editText);
 
                 builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         offset = (long) (Double.parseDouble(editText.getText().toString()) * 3600000);
                         updateButtons();
                     }
                 });
 
                 builder.setNegativeButton(android.R.string.cancel,
                         new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                             }
                         });
 
                 dialog = builder.create();
                 break;
             case DIALOG_ID_CONFIRM:
                 builder.setTitle(R.string.fix_old_confirm_title);
                 builder.setMessage(R.string.fix_old_confirm_message);
                 builder.setPositiveButton(android.R.string.ok,
                         new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 fixMessages();
 
                             }
                         });
 
                 builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
 
                     }
                 });
 
                 dialog = builder.create();
                 break;
             default:
                 dialog = null;
         }
 
         return dialog;
     }
 
     private void updateButtons() {
         startDateButton.setText(DateFormat.getLongDateFormat(this).format(startCalendar.getTime()));
         startTimeButton.setText(DateFormat.getTimeFormat(this).format(startCalendar.getTime()));
 
         endDateButton.setText(DateFormat.getLongDateFormat(this).format(endCalendar.getTime()));
         endTimeButton.setText(DateFormat.getTimeFormat(this).format(endCalendar.getTime()));
 
         signButton.setText(offset >= 0 ? R.string.fix_old_add : R.string.fix_old_subtract);
 
         DecimalFormat df = new DecimalFormat("#.###");
         offsetButton.setText(getString(R.string.fix_old_offset_hours,
                 df.format(Math.abs(offset) / 3600000.0)));
 
         if (!startCalendar.before(endCalendar)) {
             goButton.setEnabled(false);
             goButton.setText(R.string.fix_old_invalid_dates);
         } else if (offset == 0) {
             goButton.setEnabled(false);
             goButton.setText(R.string.fix_old_invalid_offset);
         } else {
             goButton.setEnabled(true);
             goButton.setText(R.string.fix_old_go);
         }
         
         if (type == Type.SMS) {
             typeButton.setText("SMS");
         } else {
             typeButton.setText("MMS");
         }
     }
 
     private void fixMessages() {
         Uri uri = (type == Type.SMS) ? SMS_URI : MMS_URI;
         if (uri != null) {
             new FixMessagesTask(this, uri).execute(startCalendar, endCalendar);
         }
     }
     
 //    private void fixThreads() {
 //        new FixThreadsTask(this).execute();
 //    }
 
     private class FixMessagesTask extends AsyncTask<Calendar, Integer, Integer> {
         Activity activity;
         ProgressDialog progressDialog;
         Uri uri;
         long realOffset;
 
         public FixMessagesTask(Activity activity, Uri uri) {
             this.activity = activity;
             this.uri = uri;
 
             if (uri == null) {
                 cancel(true);
             }
             
             if (uri == MMS_URI) {
                 realOffset = offset / 1000;
             } else {
                 realOffset = offset;
             }
         }
 
         @Override
         protected void onPreExecute() {
             progressDialog = new ProgressDialog(activity);
             progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             progressDialog.setTitle(R.string.fix_old_fixing);
             progressDialog.setIndeterminate(false);
             progressDialog.setCancelable(false);
             progressDialog.setCanceledOnTouchOutside(false);
             progressDialog.setMax(0);
             progressDialog.show();
         }
 
         @Override
         protected Integer doInBackground(Calendar... params) {
            long startTime = params[0].getTimeInMillis();
            long endTime = params[1].getTimeInMillis();
            
            if (uri == MMS_URI) {
                startTime /= 1000;
                endTime /= 1000;
            }
            
            String startTimeStr = String.valueOf(startTime);
            String endTimeStr = String.valueOf(endTime);
            
             String typeColumn = SmsMmsDbHelper.getTypeColumnName(uri);
             String[] columns = { "_id", "date" };
             String condition = typeColumn + " = ? AND date >= ? AND date <= ?";
            String[] args = { "1", startTimeStr, endTimeStr };
 
             Cursor c = getContentResolver().query(uri, columns, condition, args, "date ASC");
 
             // TODO: should probably check if c is null and handle it better...
 
             int totalCount = c.getCount();
             progressDialog.setMax(totalCount);
 
             int numCompleted = 0;
             if (c.moveToFirst()) {
                 do {
                     long id = c.getLong(c.getColumnIndexOrThrow("_id"));
                     long longdate = c.getLong(c.getColumnIndexOrThrow("date"));
 
                     longdate += realOffset;
 
                     // update the message with the new time stamp
                     ContentValues values = new ContentValues();
                     values.put("date", longdate);
                     int result = getContentResolver().update(uri, values, "_id = " + id, null);
 
                     numCompleted += result;
 
                     progressDialog.incrementProgressBy(1);
                 } while (c.moveToNext());
             }
 
             c.close();
 
             return numCompleted;
         }
 
         @Override
         protected void onPostExecute(Integer result) {
             progressDialog.dismiss();
 
             AlertDialog.Builder builder = new AlertDialog.Builder(activity);
             builder.setTitle(R.string.fix_old_complete_title);
             builder.setMessage(getString(R.string.fix_old_complete_message, result));
             builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     dialog.cancel();
 
                 }
             });
 
             builder.create().show();
         }
     }
 
     // /////////////////////////////////////////////////////////////////////////
     // This doesn't work... there isn't a 'threads' URI that allows writing:( //
     // /////////////////////////////////////////////////////////////////////////
 //    private class FixThreadsTask extends AsyncTask<Void, Void, Void> {
 //        Activity activity;
 //        ProgressDialog progressDialog;
 //
 //        public FixThreadsTask(Activity activity) {
 //            this.activity = activity;
 //        }
 //
 //        @Override
 //        protected void onPreExecute() {
 //            progressDialog = new ProgressDialog(activity);
 //            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 //            progressDialog.setTitle("Fixing threads...");
 //            progressDialog.setIndeterminate(false);
 //            progressDialog.setCancelable(false);
 //            progressDialog.setCanceledOnTouchOutside(false);
 //            progressDialog.setMax(0);
 //            progressDialog.show();
 //        }
 //
 //        @Override
 //        protected Void doInBackground(Void... params) {
 //            // TODO: change this call
 //            Uri threadUri = Uri.parse("content://mms-sms/conversations?simple=true");
 //            String[] threadColumns = { "_id" };
 //
 //            Cursor threadCursor = getContentResolver().query(threadUri, threadColumns, null, null,
 //                    null);
 //
 //            // TODO: should probably check if cursor is null and handle it
 //            // better...
 //
 //            int totalCount = threadCursor.getCount();
 //            progressDialog.setMax(totalCount);
 //
 //            if (threadCursor.moveToFirst()) {
 //                do {
 //                    String threadId = threadCursor.getString(threadCursor
 //                            .getColumnIndexOrThrow("_id"));
 //                    System.out.println("Updating thread: " + threadId);
 //
 //                    Uri smsUri = SmsDbHelper.getEditingUri();
 //                    String[] smsColumns = { "date" };
 //                    String[] args = { threadId };
 //
 //                    // find the newest message
 //                    Cursor smsCursor = getContentResolver().query(smsUri, smsColumns,
 //                            "thread_id = ?", args, "date DESC");
 //
 //                    // TODO: should probably check if cursor is null and handle
 //                    // it better...
 //
 //                    if (smsCursor.moveToFirst()) {
 //                        ContentValues values = new ContentValues();
 //                        values.put("date",
 //                                smsCursor.getString(smsCursor.getColumnIndexOrThrow("date")));
 //                        int result = getContentResolver().update(threadUri, values,
 //                                "_id = " + threadId, null);
 //                    }
 //
 //                    smsCursor.close();
 //
 //                    progressDialog.incrementProgressBy(1);
 //                } while (threadCursor.moveToNext());
 //            }
 //
 //            threadCursor.close();
 //
 //            return null;
 //        }
 //
 //        @Override
 //        protected void onPostExecute(Void result) {
 //            progressDialog.dismiss();
 //        }
 //    }
 }
