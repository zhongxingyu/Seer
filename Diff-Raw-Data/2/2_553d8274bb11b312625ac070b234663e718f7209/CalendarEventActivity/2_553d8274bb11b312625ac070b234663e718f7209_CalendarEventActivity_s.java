 package coupling.app;
 
 import java.util.Calendar;
 
 import com.nit.coupling.R;
 
import coupling.app.com.Constants;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.TimePickerDialog;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 
 public class CalendarEventActivity extends Activity implements OnClickListener{
 
 	EditText title, discription;
 	Button fromDatebt, fromHourbt, toDatebt, toHourbt;
 	
 	DatePickerDialog fromDatePicker, toDatePicker;
 	TimePickerDialog fromHourPicker, toHourPicker;
 	
 	int fromMonth, fromYear, fromDay, toMonth, toYear, toDay;
 	int fromHour, fromMinute, toHour, toMinute;
 	String titleTXT = "", discreptionTXT = "";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	
 		setContentView(R.layout.event_layout);
 		
 		initGUI();
 		
 		SharedPreferences settings = getSharedPreferences(Constants.CALENDER_PREFS, 0);
 		long eventId = settings.getLong(Constants.EVENT_ID, 0);
 		
 		Calendar calendar = Calendar.getInstance();
 		
 		if (eventId == 0) {
 			fromYear = toYear = calendar.get(Calendar.YEAR);
 			fromMonth = toMonth = calendar.get(Calendar.MONTH);
 			fromDay = toDay = calendar.get(Calendar.DAY_OF_MONTH);
 			
 			fromHour = toHour = calendar.get(Calendar.HOUR_OF_DAY);
 			fromMinute = toMinute = calendar.get(Calendar.MINUTE);
 		} else {
 
 		}
 		
 		setAllViewFildes();
 		
 	}
 	
 	
 	private void initGUI(){
 		title = (EditText)findViewById(R.id.event_title);
 		discription = (EditText)findViewById(R.id.event_description);
 		
 		fromDatebt = (Button)findViewById(R.id.from_date);
 		fromHourbt = (Button)findViewById(R.id.from_time);
 		toDatebt = (Button)findViewById(R.id.to_date);
 		toHourbt = (Button)findViewById(R.id.to_time);
 		
 		fromDatebt.setOnClickListener(this);
 		fromHourbt.setOnClickListener(this);
 		toDatebt.setOnClickListener(this);
 		toHourbt.setOnClickListener(this);
 		
 		fromDatePicker = new DatePickerDialog(this, fromDateSetListener, fromYear, fromMonth, fromDay);
 		toDatePicker = new DatePickerDialog(this, toDateSetListener, toYear, toMonth, toDay);
 		
 		fromHourPicker = new TimePickerDialog(this, fromHourTimeLstener, fromHour, fromMinute, true);
 		toHourPicker = new TimePickerDialog(this, toHourTimeLstener, toHour, toMinute, true);
 	}
 	
 	
 	OnTimeSetListener fromHourTimeLstener = new OnTimeSetListener() {
 		
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			fromHourbt.setText(TimeToText(hourOfDay, minute));
 		}
 	};
 	
 	OnTimeSetListener toHourTimeLstener = new OnTimeSetListener() {
 		
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			toHourbt.setText(TimeToText(hourOfDay, minute));
 		}
 	};
 
 	OnDateSetListener fromDateSetListener = new OnDateSetListener() {
 		
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			fromDatebt.setText(DateTotext(year, monthOfYear, dayOfMonth));
 		}
 	};
 	
 	OnDateSetListener toDateSetListener = new OnDateSetListener() {
 		
 		@Override
 		public void onDateSet(DatePicker view, int year, int monthOfYear,
 				int dayOfMonth) {
 			toDatebt.setText(DateTotext(year, monthOfYear, dayOfMonth));
 		}
 	};
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == fromDatebt.getId()) {
 			fromDatePicker.show();
 		} else if (v.getId() == fromHourbt.getId()) {
 			fromHourPicker.show();
 		} else if (v.getId() == toDatebt.getId()) {
 			toDatePicker.show();
 		} else if (v.getId() == toHourbt.getId()) {
 			toHourPicker.show();
 		}		
 	}
 	
 	private String DateTotext(int year, int month, int day){
 		return String.format("%02d", day) + "/" + String.format("%02d", month + 1) + "/" + year;
 	}
 	
 	private String TimeToText(int hour, int minute){
 		return String.format("%02d", hour) + ":" + String.format("%02d", minute);
 	}
 	
 	private void setAllViewFildes(){
 		fromDatebt.setText(DateTotext(fromYear, fromMonth, fromDay));
 		fromHourbt.setText(TimeToText(fromHour, fromMinute));
 		
 		toDatebt.setText(DateTotext(toYear, toMonth, toDay));
 		toHourbt.setText(TimeToText(toHour, toMinute));
 		
 		title.setText(titleTXT);
 		discription.setText(discreptionTXT);
 	}
 
 }
