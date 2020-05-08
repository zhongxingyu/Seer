 package com.dgsd.android.ShiftTracker.Fragment;
 
import android.R;
import android.app.DatePickerDialog;
 import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.text.format.Time;
import android.widget.DatePicker;
import android.widget.TimePicker;
 import com.actionbarsherlock.app.SherlockDialogFragment;
import com.dgsd.android.ShiftTracker.Util.Api;
import com.dgsd.android.ShiftTracker.Util.TimeUtils;
 
 public class TimePickerFragment extends SherlockDialogFragment implements TimePickerDialog.OnTimeSetListener {
     private static final String KEY_TIME = "_time";
 
     private long mTime = -1;
 
     private OnTimeSelectedListener mOnTimeSelectedListener;
 
     public static TimePickerFragment newInstance(long time) {
         TimePickerFragment frag = new TimePickerFragment();
 
         Bundle args = new Bundle();
         args.putLong(KEY_TIME, time);
         frag.setArguments(args);
 
         return frag;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         final Bundle args = getArguments();
         if(args != null) {
             mTime = args.getLong(KEY_TIME, mTime);
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final Time time = new Time();
 
         if(mTime == -1)
             time.setToNow();
         else
             time.set(mTime);
 
         return new TimePickerDialog(getActivity(), this, time.hour, time.minute, DateFormat.is24HourFormat(getActivity()));
     }
 
     public void setOnTimeSelectedListener(OnTimeSelectedListener listener) {
         this.mOnTimeSelectedListener = listener;
     }
 
     @Override
     public void onTimeSet(TimePicker timePicker, int hour, int minute) {
         if(mOnTimeSelectedListener != null) {
             Time time = new Time();
             time.hour = hour;
             time.minute = minute;
             time.second = 0;
             time.normalize(true);
 
             mOnTimeSelectedListener.onTimeSelected(time.toMillis(true));
         }
     }
 
     public static interface OnTimeSelectedListener {
         public void onTimeSelected(long time);
     }
 
 }
