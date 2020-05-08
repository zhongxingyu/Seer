 package org.openforis.collect.android.dialogs;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.openforis.collect.android.R;
 import org.openforis.collect.android.fields.DateField;
 import org.openforis.collect.android.management.ApplicationManager;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnKeyListener;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.widget.DatePicker;
 
 public class DateSetDialog extends FragmentActivity {
 	private String path;
 	//Subclass which creates DatePicker
 	public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
 		public DateField activity_edittext;
 		public DatePickerDialog datePicker;
 		private String pathToParentScreen;
 		public DatePickerFragment(DateField date_field, String path) {
 		    activity_edittext = date_field;
 		    pathToParentScreen = path;
 		}	    
 		
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			// Use the current date as the default date in the picker
 			final Calendar c = Calendar.getInstance();
 			int year = c.get(Calendar.YEAR);
 			int month = c.get(Calendar.MONTH);
 			int day = c.get(Calendar.DAY_OF_MONTH);
 			// Create a new instance of DatePickerDialog and return it
 			this.datePicker = new DatePickerDialog(getActivity(), this, year, month, day);
 			
 			//Add listener for button "Cancel"
 			this.datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int which) {
 			       if (which == DialogInterface.BUTTON_NEGATIVE) {
 			          //Finish activity
 			    	   finish();
 			       }
 			    }
 			});
 			
 			this.datePicker.setOnKeyListener(new OnKeyListener(){
 				@Override
 				public boolean onKey( DialogInterface dialog , int keyCode , KeyEvent event ){
 					// disable search button action
 					if (keyCode == KeyEvent.KEYCODE_BACK){
 					    //Finish activity
 					    finish();						
 						return true;
 					}
 					return false;
 				}
 			});
 			
 			return this.datePicker;
 		}
 		
 		public void onDateSet(DatePicker view, int year, int month, int day) {
 			//Set date to the clicked DateField 
 			Calendar cal = new GregorianCalendar(year, month, day);
 			//String strDate = DateFormat.getDateFormat(getActivity()).format(cal.getTime());
 			//String strDate = (String) DateFormat.format("yyyy"+getResources().getString(R.string.dateSeparator)+"MM"+getResources().getString(R.string.dateSeparator)+"dd", cal);
 //			String strDate = cal.get(Calendar.YEAR)+getResources().getString(R.string.dateSeparator)+(cal.get(Calendar.MONTH)+1)+getResources().getString(R.string.dateSeparator)+cal.get(Calendar.DAY_OF_MONTH);
 			//!!! BECAUSE IN FORM SCREEN FORMAT IS "mm-dd-yyy" 
			String strDate = cal.get(Calendar.YEAR) + getResources().getString(R.string.dateSeparator) + (cal.get(Calendar.MONTH)+1) + getResources().getString(R.string.dateSeparator)+cal.get(Calendar.DAY_OF_MONTH);
 			//activity_edittext.txtBox.setText(strDate);
 			activity_edittext.setValue(0, strDate, this.pathToParentScreen, false);
 		    //Finish activity
 		    finish();
 		}		
 	}
 	
 	//Main class
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    getWindow().requestFeature(Window.FEATURE_OPTIONS_PANEL);
 	    Bundle extras = getIntent().getExtras(); 
 	    this.path = extras.getString("dateFieldPath");
 	    if (extras != null) {
 	    	  int widget_id = extras.getInt("datefield_id");
 	    	  View v = ApplicationManager.getUIElement(widget_id);
 	    	  if (v != null){
 	    		  showDatePickerDialog((DateField)v);
 	    	  }
 	    }
 	}	
 	
 	public void showDatePickerDialog(DateField dateField) {
 	    DialogFragment newFragment = new DatePickerFragment(dateField, this.path);
 	    newFragment.setCancelable(false);
 //	    newFragment.getFragmentManager().popBackStack();
 	    newFragment.show(getSupportFragmentManager(), "datePicker");
 	}		
 }
