 /*
  * Copyright (C) 2007 The Android Open Source Project
  * Copyright (C) 2009 Andago Ingenieria
  * Author: Alvaro del Castillo, adelcastillo@andago.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sta.andswtch.gui.timepicker;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import sta.andswtch.R;
 
 /**
  * A dialog that prompts the user for the time of day using a {@link TimePicker}.
  */
 public class TimePickerDialog extends AlertDialog implements OnClickListener, 
         TimePicker.OnTimeChangedListener {
 
     /**
      * The callback interface used to indicate the user is done filling in
      * the time (they clicked on the 'Set' button).
      */
     public interface OnTimeSetListener {
 
         /**
          * @param view The view associated with this listener.
          * @param hour The hour that was set.
          * @param minute The minute that was set.
          * @param second The second that was set.
          * @param cancel If the set is cancel
          */
         void onTimeSet(TimePicker view, int hour, int minute, int second, boolean cancel);
     }
 
     private static final String HOUR = "hour";
     private static final String MINUTE = "minute";
     private static final String SECOND = "second";
     
     private final TimePicker mTimePicker;
     private final OnTimeSetListener mCallback;
     
     private int mInitialHour;
     private int mInitialMinute;
     private int mInitialSecond;
 
     private Resources resources;
 
     /**
      * @param context Parent.
      * @param callBack How parent is notified.
      * @param hour The initial hour.
      * @param minute The initial minute.
      * @param second The initial second.
      */
     public TimePickerDialog(Context context,
             OnTimeSetListener callBack,
             int hour, int minute, int second) {
 //        this(context, R.style.Theme_Dialog_Alert,
         this(context, 0, callBack, hour, minute, second);
     }
 
     /**
      * @param context Parent.
      * @param theme the theme to apply to this dialog
      * @param callBack How parent is notified.
      * @param hour The initial hour.
      * @param minute The initial minute.
      * @param second The initial second.
      */
     public TimePickerDialog(Context context,
             int theme,
             OnTimeSetListener callBack,
             int hour, int minute, int second) {
         // super(context, theme);
         super(context);
         resources = context.getResources();
 
         mCallback = callBack;
         mInitialHour = hour;
         mInitialMinute = minute;
         mInitialSecond = second;
 
         updateTitle(mInitialHour, mInitialMinute, mInitialSecond);
         
         setButton(BUTTON_POSITIVE, context.getText(R.string.tp_countdown_start), this);
         setButton(BUTTON_NEGATIVE, context.getText(R.string.tp_cancel_txt), this);
         setIcon(R.drawable.ic_dialog_time);
 
         LayoutInflater inflater = 
                 (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View view = inflater.inflate(R.layout.time_picker_dialog, null);
         setView(view);
         mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
 
         // initialize state
         mTimePicker.setCurrentHour(mInitialHour);
         mTimePicker.setCurrentMinute(mInitialMinute);
         mTimePicker.setCurrentSecond(mInitialSecond);
         mTimePicker.setOnTimeChangedListener(this);
     }
 
     public void onClick(DialogInterface dialog, int which) {
         boolean cancel = false;
         Log.i(this.getClass().getName(), new Integer(which).toString());
         if (which == BUTTON_NEGATIVE) {cancel=true;}
         if (mCallback != null) {
             mTimePicker.clearFocus();
             mCallback.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(), 
                     mTimePicker.getCurrentMinute(), mTimePicker.getCurrentSecond(),cancel);
         }
     }
 
     public void onTimeChanged(TimePicker view, int hour, int minute, int second) {
         // FIXME: Enable them only the first time
         if (hour > 0 || minute > 0 || second > 0) { 
             getButton(BUTTON_POSITIVE).setEnabled(true);
             getButton(BUTTON_NEGATIVE).setEnabled(true);
         } else {
             getButton(BUTTON_POSITIVE).setEnabled(false);
            getButton(BUTTON_NEGATIVE).setEnabled(false);
         }
         updateTitle(hour, minute, second);
     }
     
     public void updateTime(int hour, int minute, int second) {
         mTimePicker.setCurrentHour(hour);
         mTimePicker.setCurrentMinute(minute);
         mTimePicker.setCurrentSecond(second);
     }
 
     private void updateTitle(int hour, int minute, int second) {
         String title = resources.getString(R.string.tp_title_text);
         setTitle(title+" "+
                  Util.pad(hour)+":"+Util.pad(minute)+":"+Util.pad(second));
     }
     
     @Override
     public Bundle onSaveInstanceState() {
         Bundle state = super.onSaveInstanceState();
         state.putInt(HOUR, mTimePicker.getCurrentHour());
         state.putInt(MINUTE, mTimePicker.getCurrentMinute());
         state.putInt(SECOND, mTimePicker.getCurrentSecond());
         return state;
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         int hour = savedInstanceState.getInt(HOUR);
         int minute = savedInstanceState.getInt(MINUTE);
         int second = savedInstanceState.getInt(SECOND);
         mTimePicker.setCurrentHour(hour);
         mTimePicker.setCurrentMinute(minute);
         mTimePicker.setCurrentSecond(second);
         mTimePicker.setOnTimeChangedListener(this);
         updateTitle(hour, minute, second);
     }
 }
