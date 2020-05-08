 /**
  * 
  */
 package com.japtar.chessclock.gui;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.TypedArray;
 import android.preference.DialogPreference;
 import android.preference.Preference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TimePicker;
 
 import com.japtar.chessclock.models.TimeModel;
 
 /**
  * GUI element that opens a timer dialog
  * @author japtar10101
  */
 public class TimerPreference extends DialogPreference implements
 		TimePicker.OnTimeChangedListener {
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** This preference's time */
 	private final TimeModel mTime = new TimeModel();
 	/** The default value */
	private int mDefaultValue = 0;;
 	
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * @see Preference#Preference(Context, AttributeSet)
 	 */
 	public TimerPreference(final Context context, final AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	/**
 	 * @see Preference#Preference(Context, AttributeSet, int)
 	 */
 	public TimerPreference(final Context context, final AttributeSet attrs,
 			final int defStyle) {
 		super(context, attrs, defStyle);
 	}
 	
 	/* ===========================================================
 	 * Overrides
 	 * =========================================================== */
 	/**
 	 * Updates internal time variables
 	 * @see android.widget.TimePicker.OnTimeChangedListener#onTimeChanged(android.widget.TimePicker, int, int)
 	 */
 	@Override
 	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		mTime.setTime(TimeModel.intToByte(hourOfDay),
				TimeModel.intToByte(minute));
 	}
 	
 	/**
 	 * Updates {@link mModel}, if user clicked "OK"
 	 * @see android.preference.DialogPreference#onDialogClosed(boolean)
 	 */
 	@Override
 	public void onDialogClosed(boolean positiveResult) {
 		// If we clicked OK...
 		if(positiveResult) {
 			// Save the values
             this.saveValues();
 		}
 	}
 	
 	@Override
 	protected View onCreateDialogView() {
 		// Create a new TimePicker
 		final TimePicker timer = new TimePicker(this.getContext());
 		timer.setIs24HourView(true);
 		
 		// Recall the stored values
     	this.recallValues();
 		
 		// Update view
		timer.setCurrentHour((int) mTime.getMinutes());
		timer.setCurrentMinute((int) mTime.getSeconds());
 		
 		// Return view
 		timer.setOnTimeChangedListener(this);
 		return timer;
 	}
 	
 	/**
 	 * Returns a default integer
 	 * @see android.preference.Preference#onGetDefaultValue(android.content.res.TypedArray, int)
 	 */
 	@Override
     protected Object onGetDefaultValue(TypedArray a, int index) {
         // This preference type's value type is Integer, so we read the default
         // value from the attributes as an Integer.
         return a.getInteger(index, 0);
     }
 
     /**
      * Sets the initial value.
      * @see android.preference.Preference#onSetInitialValue(boolean, java.lang.Object)
      */
     @Override
     protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
         // Check if we should recall the values instead
         if(restoreValue && this.shouldPersist()) {
         	// If so, recall or save the values
         	this.recallValues();
         } else if(defaultValue instanceof Integer) {
         	// Set state based on default value
         	mDefaultValue = (Integer) defaultValue;
     	}
     }
     
     /* ===========================================================
 	 * Private/Protected Methods
 	 * =========================================================== */
     /**
      * Recalls the stored minutes and seconds
      */
     private void recallValues() {
         // Restore state
     	mTime.recallTime(this.getSharedPreferences(),
     			this.getKey(), mDefaultValue);
     }
     
     /**
      * Saves the minutes and seconds
      */
     private void saveValues() {
 		// Grab the editor
 		final SharedPreferences toSave = this.getSharedPreferences();
 		final SharedPreferences.Editor editor = toSave.edit();
 		
 		// Write in the preferences
 		mTime.saveTime(editor, this.getKey());
 		editor.commit();
     }
 }
