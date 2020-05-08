 package se.chalmers.dat255.sleepfighter;
 
 import se.chalmers.dat255.sleepfighter.debug.Debug;
 import android.content.Context;
import android.content.SharedPreferences;
 import android.preference.DialogPreference;
import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TimePicker;
 
 /**
  * A DialogPreference with containing a TimePicker.
  * 
  * @author Hassel
  *
  */
 public class TimepickerPreference extends DialogPreference {
 	
 	private int hour;
 	private int minute;
 	private TimePicker tp;
 	
 	public TimepickerPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	@Override
 	protected View onCreateDialogView() {
 
 		Debug.d("created time picker preference view");
 		tp = new TimePicker(getContext());
 		tp.setIs24HourView(true);
 	
 		tp.setCurrentHour(hour);
 		tp.setCurrentMinute(minute);
 		
 		return(tp);
 	}
 	
 	@Override
 	protected void onDialogClosed(boolean positiveResult) {
 		super.onDialogClosed(positiveResult);
 		
 		if (positiveResult) {
 			hour = tp.getCurrentHour();
 			minute = tp.getCurrentMinute();
 		}
 		
 		String time = (hour < 10 ? "0" : "") + hour + ":"
 				+ (minute < 10 ? "0" : "") + minute;
 		
 		
 		if (positiveResult && callChangeListener(time)) {
 			Debug.d("persist time");
             persistString(time);
         }
 	}
 	
 	public void setHour(int hour) {
 		this.hour = hour;
 	}
 	
 	/**
 	 * @return the picked hour.
 	 */
 	public int getHour() {
 		return hour;
 	}
 	
 	public void setMinute(int minute) {
 		this.minute = minute;
 	}
 	
 	/**
 	 * @return the picked minute.
 	 */
 	public int getMinute() {
 		return minute;
 	}
 }
