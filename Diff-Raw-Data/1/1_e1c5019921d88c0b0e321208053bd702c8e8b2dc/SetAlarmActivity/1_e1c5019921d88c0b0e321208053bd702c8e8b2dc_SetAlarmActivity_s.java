 package com.purpleparrots.beiroute;
 
 //import java.util.Calendar;
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 public class SetAlarmActivity extends Activity {
 	EditText nameField;
 	
 	/*private TextView mTimeDisplay;
     private Button mPickTime;
     private int mHour;
     private int mMinute;
 
     private TextView mDateDisplay;
     private Button mPickDate;
     private int mYear;
     private int mMonth;
     private int mDay;*/
     
     TimePicker timePicker;
     DatePicker datePicker;
     TextView topText, advanceText;
     CheckBox repeatCheckBox;
     
     static final int REPEAT_DIALOG_ID = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.makealarm);
         
         topText = (TextView)findViewById(R.id.set_alarm_top_text);
         nameField = (EditText)findViewById(R.id.alarm_set_name_field);
         timePicker = (TimePicker)findViewById(R.id.timePicker);
         datePicker = (DatePicker)findViewById(R.id.datePicker);
         advanceText = (TextView)findViewById(R.id.textView2);
         
         String routeName = Control.getRouteName();
         topText.setText("Schedule route " + routeName + ", arriving at:");
         
         repeatCheckBox = (CheckBox)findViewById(R.id.repeat_check_box);
         repeatCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
         	{
         	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
         	    {
         	        if (isChecked)
         	        {
         	            Log.d("Dan's Log","Entered repeat checkbox logic");
         	            showDialog(REPEAT_DIALOG_ID);
         	        }
 
         	    }
         	});
         
     }
     
    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog = new Dialog(SetAlarmActivity.this);
 
 	   switch (id) {
 	   /*case TIME_DIALOG_ID:
 		   	return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false);
 	   case DATE_DIALOG_ID:
    			return new DatePickerDialog(this,
    					mDateSetListener,
    					mYear, mMonth, mDay);*/
 	   case REPEAT_DIALOG_ID:
 		   //LayoutInflater factory = LayoutInflater.from(this);
 		   //final View dialogView = factory.inflate(R.layout.repeatdialog, null);
            dialog.setContentView(R.layout.repeatdialog);
            dialog.setTitle("This is my custom dialog box");
            dialog.setCancelable(true);
            RadioGroup rg = (RadioGroup)dialog.findViewById(R.id.radioGroup1);
            rg.check(0);
            Button doneButton = (Button)dialog.findViewById(R.id.done_with_repeat);
            Button cancelButton = (Button)dialog.findViewById(R.id.cancel_repeat);
            
            doneButton.setOnClickListener(new View.OnClickListener()
            {
         	   public void onClick(View v) {
         		   Log.d("Dan's Log", "Got into done button's onClick");
         		   dialog.cancel(); //we want it to do something real in the final implementation
         	   }
            });
            
            cancelButton.setOnClickListener(new View.OnClickListener()
            {
         	   public void onClick(View v) {
         		   Log.d("Dan's Log", "Got into cancel button's onClick");
         		   dialog.cancel();
         	   }
            });
            
            return dialog;
 	   }
 	   return null;
    }
     
     public void onClick(View route) {
     	Log.d("Dan's Log", "Pushed set alarm button");
     	
         String name = nameField.getText().toString();
         int hour = timePicker.getCurrentHour();
         int minute = timePicker.getCurrentMinute();
         int day = datePicker.getDayOfMonth();
         int month = datePicker.getMonth();
         int year = datePicker.getYear();
         Log.d("Dan's Log", "Saving alarm with data: " + name + ", " + hour + ", "+ minute + ", "
         		+ month + ", " + day + ", " + (year));
         Control.saveAlarm(name, year, month, day, hour, minute, this);
         startActivity(new Intent(this, MainActivity.class));
     }
 }
