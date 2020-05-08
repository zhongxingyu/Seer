 package mctech.android.glucosemeter;
 
 import java.util.Calendar;
 import java.util.TimeZone;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.NumberPicker;
 import android.widget.TimePicker;
 
 
 
 
 
 
 public class TimePreference extends DialogPreference {
     private int lastHour=0;
     private int lastMinute=0;
     private TimePicker picker=null;
     private static String default_key = "checkbox_dialog";
     private boolean is_am = false;
     
     private NumberPicker.OnValueChangeListener am_pm_listener=null;
     private NumberPicker.OnValueChangeListener null_am_pm_listener=null;
     private TimePicker.OnTimeChangedListener time_changed_listener = null;
     private TimePicker.OnTimeChangedListener null_time_changed_listener = null;
     private PendingIntent open_bloodmeter =null;
     
     public static int getHour(String time) {
         String[] pieces=time.split(":");
 
         return(Integer.parseInt(pieces[0]));
     }
 
     public static int getMinute(String time) {
         String[] pieces=time.split(":");
 
         return(Integer.parseInt(pieces[1]));
     }
 
     public TimePreference(Context ctxt) {
         this(ctxt, null);
     }
 
     public TimePreference(Context ctxt, AttributeSet attrs) {
         this(ctxt, attrs, 0);
     }
 
     public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
         super(ctxt, attrs, defStyle);
 
         setPositiveButtonText("Set");
         setNegativeButtonText("Cancel");
         Intent alarm_receiver = new Intent(ctxt, AlarmReceiver.class);
         alarm_receiver.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         open_bloodmeter = PendingIntent.getBroadcast(ctxt,
                 0, alarm_receiver, PendingIntent.FLAG_CANCEL_CURRENT);
         
     }
 
     @Override
     protected View onCreateDialogView() {
         picker=new TimePicker(getContext());
     	ViewGroup vg = (ViewGroup)picker.getChildAt(0);
    	NumberPicker am_pm = (NumberPicker)vg.getChildAt(1);
     	
         if (is_am) {
         	am_pm_listener = new NumberPicker.OnValueChangeListener() {
         			
         		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
         			picker.setOnValueChangedListener(null_am_pm_listener);
         			picker.setValue(oldVal);
         			picker.setOnValueChangedListener(am_pm_listener);
         		}
         	};
         	time_changed_listener = new TimePicker.OnTimeChangedListener() {
         		
         		public void  onTimeChanged(TimePicker picker, int hourOfDay, int minute) {
         			if (hourOfDay > 11) {
         				hourOfDay = hourOfDay - 12;
         			}
         			picker.setOnTimeChangedListener(null_time_changed_listener);
         			picker.setCurrentHour(hourOfDay);
         			picker.setOnTimeChangedListener(time_changed_listener);
         		}
         	};
         }
         else {
         	am_pm_listener = new NumberPicker.OnValueChangeListener() {
         			
         		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
         			picker.setOnValueChangedListener(null_am_pm_listener);
         			picker.setValue(oldVal);
         			picker.setOnValueChangedListener(am_pm_listener);
         		}
         	};   	
         	
         	time_changed_listener = new TimePicker.OnTimeChangedListener() {
         		
         		public void  onTimeChanged(TimePicker picker, int hourOfDay, int minute) {
         			if (hourOfDay < 12) {
         				hourOfDay = 12 + hourOfDay;
         			}
         			picker.setOnTimeChangedListener(null_time_changed_listener);
         			picker.setCurrentHour(hourOfDay);
         			picker.setOnTimeChangedListener(time_changed_listener);
         		}
         	};
         }
         
         am_pm.setOnValueChangedListener(am_pm_listener);
         picker.setOnTimeChangedListener(time_changed_listener);
         
         return(picker);
     }
 
     public void show() {
     	onClick();
     }
     
     @Override
     protected void showDialog(Bundle state) {
     	super.showDialog(state);
     	
     }
     
     @Override
     protected void onBindDialogView(View v) {
         super.onBindDialogView(v);
 
         picker.setCurrentHour(lastHour);
         picker.setCurrentMinute(lastMinute);
     }
 
     @Override
     protected void onDialogClosed(boolean positiveResult) {
         super.onDialogClosed(positiveResult);
 
         if (positiveResult) {
             lastHour=picker.getCurrentHour();
             lastMinute=picker.getCurrentMinute();
 
             String time=String.valueOf(lastHour)+":"+String.valueOf(lastMinute);
 
             if (callChangeListener(time)) {
                 persistString(time);
             }
             setRecurringAlarm();
         }
         
         setKey(default_key);
     }
 
     @Override
     protected Object onGetDefaultValue(TypedArray a, int index) {
         return(a.getString(index));
     }
 
     @Override
     protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
         String time=null;
         
         if (restoreValue) {
             if (defaultValue==null) {
                 time=getPersistedString("00:00");
             }
             else {
                 time=getPersistedString(defaultValue.toString());
             }
         }
         else {
             time=defaultValue.toString();
         }
 
         lastHour=getHour(time);
         if (is_am && lastHour > 11) {
         	lastHour = lastHour -12;
         }
         else if ((is_am == false) && lastHour < 12) {
         	lastHour += 12;
         }
         lastMinute=getMinute(time);
     }    
 
     public void set_am(Boolean is_am) {
     	this.is_am = is_am;
     }
     
     private void setRecurringAlarm() {
 
         Calendar updateTime = Calendar.getInstance();
         updateTime.setTimeZone(TimeZone.getDefault());
         updateTime.set(Calendar.HOUR_OF_DAY, lastHour);
         updateTime.set(Calendar.MINUTE, lastMinute);
        
        Context context = ((Activity)getContext());
        AlarmManager alarms = (AlarmManager)context.getSystemService(
                 Context.ALARM_SERVICE);
        alarms.setRepeating(AlarmManager.RTC_WAKEUP,
                 updateTime.getTimeInMillis(),
                 AlarmManager.INTERVAL_DAY, open_bloodmeter);
 
     }
 
     public void cancelAlarm() {
     	AlarmManager alarms = (AlarmManager)((Activity)getContext()).getSystemService(
                 Context.ALARM_SERVICE);
 
     	alarms.cancel(open_bloodmeter); 
     }
     
 }
