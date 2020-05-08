 package app.xzone.storyline.component;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import android.app.DatePickerDialog;
 import android.app.TimePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import app.xzone.storyline.util.TimeUtil;
 
 
 public class DateTimePicker {
 	
 	// Component for handle date picker
 	public static void showDatePicker(final Context context,
 			final View resourceTarget) {
 		DateTime dt = new DateTime();
 		DatePickerDialog dp = null;
 
 		dp = new DatePickerDialog(context, new OnDateSetListener() {
 
 
 			@Override
 			public void onDateSet(android.widget.DatePicker view, int year,
 					int monthOfYear, int dayOfMonth) {
 				DateTimeFormatter fmt = DateTimeFormat
						.forPattern("E MMM dd, yyyy");
 
 				TextView dateText = (TextView) resourceTarget;
 				dateText.setText((new DateTime(year, monthOfYear+1,
 						dayOfMonth, 0, 0, 0, 0)).toString(fmt));
 				
 			}
 		}, dt.getYear(), dt.getMonthOfYear()-1, dt.getDayOfMonth());
 		dp.show();
 
 	}
 	
 	
 	// Component for handle time picker
 	public static void showTimePicker(final Context context,
 			final View resourceTarget) {
 		DateTime dt = new DateTime();
 		TimePickerDialog tp = null;
 
 		tp = new TimePickerDialog(context, new OnTimeSetListener() {
 
 			@Override
 			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 				TextView timeText = (TextView) resourceTarget;
 				timeText.setText(hourOfDay + ":" + minute
 						+ TimeUtil.timeArea(hourOfDay));
 
 			}
 		}, dt.getHourOfDay(), dt.getMinuteOfHour(), true);
 		tp.show();
 	}
 
 
 }
