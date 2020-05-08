 package com.macdems.planactivator;
 
import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.ptashek.widgets.datetimepicker.DateTimePicker;
 
 public class PlanEditActivity extends Activity {
 
 	private EditText mEditName;
 	private EditText mEditUSSD;
 	private EditText mEditPeriod;
 	private EditText mEditDelay;
 	private Spinner mSpinnerPeriodUnit;
 	private Spinner mSpinnerDelayUnit;
 	private Spinner mSpinnerDelaySign;
 	private TextView mTextLastUsed;
 	private TextView mTextValidUntil;
 	private CheckBox mCheckboxActive;
 	@SuppressWarnings("unused")	private LinearLayout mLayoutDelay;
 	private Button mButtonChangeValidity;
 	private Button mButtonDone;
 	
 	private DatabaseAdapter mDbHelper;
 	private Long mRowId;
 	private Date mValidUntil;
 	private boolean mValidDateChanged;
 	
 	public static final long[] Units = new long[] {86400, 3600, 60, 1};
 	//												days   hours minutes seconds
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.packet_editor);
         setTitle(R.string.edit_plan);
    
         mValidUntil = null;
         mValidDateChanged = false;
         
         ArrayAdapter<CharSequence> unitsAdapter = ArrayAdapter.createFromResource(
                 this, R.array.time_units, android.R.layout.simple_spinner_item);
         unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         mSpinnerPeriodUnit = (Spinner) findViewById(R.id.spinnerPeriodUnit);
         mSpinnerPeriodUnit.setAdapter(unitsAdapter);
         mSpinnerPeriodUnit.setSelection(0);
        
         mSpinnerDelayUnit = (Spinner) findViewById(R.id.spinnerDelayUnit);
         mSpinnerDelayUnit.setAdapter(unitsAdapter);
         mSpinnerDelayUnit.setSelection(3);
               
         ArrayAdapter<CharSequence> signAdapter = ArrayAdapter.createFromResource(
                 this, R.array.earlier_later, android.R.layout.simple_spinner_item);
         signAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         mSpinnerDelaySign = (Spinner) findViewById(R.id.spinnerDelaySign);
         mSpinnerDelaySign.setAdapter(signAdapter);
 
         mTextLastUsed = (TextView) findViewById(R.id.textLastUsed);
         mTextValidUntil = (TextView) findViewById(R.id.textValidUntil);
         
         mEditName = (EditText) findViewById(R.id.editName);
         mEditUSSD = (EditText) findViewById(R.id.editUSSD);
         mEditPeriod = (EditText) findViewById(R.id.editPeriod);
         mEditDelay = (EditText) findViewById(R.id.editDelay);
         mEditName = (EditText) findViewById(R.id.editName);
         
         mTextLastUsed = (TextView) findViewById(R.id.textLastUsed);
         mTextValidUntil = (TextView) findViewById(R.id.textValidUntil);
         
         mCheckboxActive = (CheckBox) findViewById(R.id.checkActive);
         
         mLayoutDelay = (LinearLayout) findViewById(R.id.layoutDelay);
         
         mButtonDone = (Button) findViewById(R.id.buttonDone);
         mButtonDone.setEnabled(false);
 
         mButtonChangeValidity = (Button) findViewById(R.id.buttonChangeValidity);
         
         mDbHelper = new DatabaseAdapter(this);
         mDbHelper.open();
 
         mEditName.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable arg0) {
 				mButtonDone.setEnabled(arg0.length() != 0);
 			}
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
         
         mEditUSSD.addTextChangedListener(new TextWatcher() {
 			public void afterTextChanged(Editable arg0) {
 				String str = arg0.toString();
 				mButtonDone.setEnabled(arg0.length() != 0 && str.startsWith("*") && str.endsWith("#"));
 			}
 			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
 			public void onTextChanged(CharSequence s, int start, int before, int count) {}
         });
         
 
         if (savedInstanceState != null) {
         	mRowId = (Long) savedInstanceState.getSerializable(DatabaseAdapter.KEY_ROWID);        
         } else {
         	mRowId = null;
         }
         
         if (mRowId == null) {
             Bundle extras = getIntent().getExtras();
             mRowId = (extras != null) ? extras.getLong(DatabaseAdapter.KEY_ROWID)
                                     : null;
         }
 
     	mEditDelay.setEnabled(false);
     	mSpinnerDelayUnit.setEnabled(false);
     	mSpinnerDelaySign.setEnabled(false);
         
         loadData();
         
         mCheckboxActive.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 	        	mEditDelay.setEnabled(isChecked);
 	        	mSpinnerDelayUnit.setEnabled(isChecked);
 	        	mSpinnerDelaySign.setEnabled(isChecked);
 			}
 		});
         
         mButtonChangeValidity.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDateTimeDialog();
 			}
         	
         });
         
         mButtonDone.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if (saveData()) finish();
 			}
 		});
 	}
 
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	   	boolean result = super.onCreateOptionsMenu(menu);
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.editplan, menu);
         return result;        
  	}
 
 
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	
     	case R.id.activateplan2:
        		Intent activateIntent = new Intent(this, ActivateEvent.class);
        		activateIntent.putExtra("id", mRowId);
        		sendBroadcast(activateIntent);
        		break;
     	
     	case R.id.deleteplan2:	// Start an activity for creating a new packet
     		AlertDialog dialog = new AlertDialog.Builder(this).create();
     		dialog.setMessage(getString(R.string.delete_plan));
     		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes), new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
     				dialog.dismiss();
     	    		mDbHelper.deletePlan(mRowId);
     	    		finish();
     			}}
     		);
     		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no), new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int which) {
     				dialog.dismiss();
     			}}
     		);		 
     		dialog.show();
     		break;
     	}
     	
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	
 	@Override
 	public void onBackPressed() {
 		AlertDialog dialog = new AlertDialog.Builder(this).create();
 		dialog.setMessage(getString(R.string.no_save_confirm));
 
 		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				finish();
 			}}
 		);
 
 		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(android.R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}}
 		);		
 		
 		dialog.show();
 	}
 
 
 	
 	/**
 	 * Sets the value in specified view and unit based on the value modulo unit
 	 * @param value to analyze
 	 * @param textView to set
 	 * @param unitView to set proper unit
 	 */
 	private void setValueWithUnit(Long value, EditText textView, Spinner unitView) {
 		if (value == null) return;
 		if (value == 0) {
         	textView.setText("0");
 			return;
 		}
 		int i = 0;
 		for (long unit : Units) {
 			if (value % unit == 0) {
 				Long newValue = value / unit;
 	        	textView.setText(newValue.toString());
 	        	unitView.setSelection(i);
 	        	return;
 			}
 			i++;
 		}
     	textView.setText(value.toString());
     	unitView.setSelection(i);
 	}
 	
 	
 	
 	/**
 	 * Load data for the specified packet from the database
 	 */
 	private void loadData() {
 		if (mRowId != null) {
 	        Cursor cursor = mDbHelper.fetchPlan(mRowId);
 	        startManagingCursor(cursor);
 	        
 	        mEditName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_NAME)));
 	        
 	        mEditUSSD.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_USSD)));
 	        
 	        Long period = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_PERIOD));
 	        setValueWithUnit(period, mEditPeriod, mSpinnerPeriodUnit);
         	
 	        boolean active = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_AUTO)) != 0;
 	        mCheckboxActive.setChecked(active);
         	mEditDelay.setEnabled(active);
         	mSpinnerDelayUnit.setEnabled(active);
         	mSpinnerDelaySign.setEnabled(active);
         	
         	Long delay = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_DELAY));
 	        if (delay != null) {
 	        	if (delay < 0) {
 		        	mSpinnerDelaySign.setSelection(0);
 		        	delay = -delay;
 		        } else {
 		        	mSpinnerDelaySign.setSelection(1);	        	
 		        }
 	        }
 	        setValueWithUnit(delay, mEditDelay, mSpinnerDelayUnit);
 	        
 	        Long last = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_LAST));
 	        if (last != null && last != 0) {
 	        	mTextLastUsed.setText(DateFormat.getDateFormat(this).format(last) + " " + DateFormat.getTimeFormat(this).format(last));
 	        }
 	        
 	        Long valid = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_VALIDUNTIL));
 	        if (valid != null && valid != 0) {
 	        	mValidUntil = new Date(valid);
 	        	mTextValidUntil.setText(DateFormat.getDateFormat(this).format(valid) + " " + DateFormat.getTimeFormat(this).format(valid));
 	        }
 
 		}
 	}
 	
 	
 	
     /**
      * Save data to the database
      * @return true if data saved, false if name or USSD are empty
      */
 	private boolean saveData() {
         String name = mEditName.getText().toString();
         String ussd = mEditUSSD.getText().toString();
 
         if (name.equals("") || ussd.equals("") ||
             !ussd.startsWith("*") || !ussd.endsWith("#")) return false;
         
         Long period;
         try {
         	period = Long.parseLong(mEditPeriod.getText().toString())
         		* Units[(int) mSpinnerPeriodUnit.getSelectedItemId()];
         } catch (NumberFormatException e) {
         	period = null;
         }
         
         Boolean active = mCheckboxActive.isChecked();
         
         Long delay;
         try {
         	delay = Long.parseLong(mEditDelay.getText().toString())
     			* Units[(int) mSpinnerDelayUnit.getSelectedItemId()];
             if (mSpinnerDelaySign.getSelectedItemId() == 0)
             	delay = -delay;
         } catch (NumberFormatException e) {
         	delay = null;
         }
 
         if (mRowId == null) {
             long id = mDbHelper.createPlan(name, ussd, period, active, delay);
             if (id > 0) {
                 mRowId = id;
             }
         } else {
             mDbHelper.updatePlan(mRowId, name, ussd, period, active, delay);
             if (!active) ActivateEvent.cancelAlarm(this, mRowId);
         }
 
         if (mValidDateChanged)
         	mDbHelper.updateValidityDate(mRowId, mValidUntil);
 
         return true;
     }
 
 	
 	
 	/**
 	 * Show date-time dialog to manually change validity time
 	 */
 	void showDateTimeDialog() {
 
 		final Context context = this;
 		
 		// Create the dialog
 		final Dialog dateTimeDialog = new Dialog(this);
 		// Inflate the root layout
 		final RelativeLayout dateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.datetime_dialog, null);
 		// Grab widget instance
 		final DateTimePicker dateTimePicker = (DateTimePicker) dateTimeDialogView.findViewById(R.id.DateTimePicker);
 		// Check is system is set to use 24h time (this doesn't seem to work as expected though)
 		final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
 		final boolean is24h = (timeS == null || !timeS.equals("12"));
 		
 		// Update demo TextViews when the "OK" button is clicked 
 		((Button) dateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mValidDateChanged = true;
 				mValidUntil = dateTimePicker.getCalendar().getTime();
 	        	mTextValidUntil.setText(DateFormat.getDateFormat(context).format(mValidUntil) + " " +
 	        			            DateFormat.getTimeFormat(context).format(mValidUntil));
 				dateTimeDialog.dismiss();
 			}
 		});
 
 		((Button) dateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				dateTimeDialog.cancel();
 			}
 		});
 
 		((Button) dateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				dateTimePicker.reset();
 			}
 		});
 	
 		if (mValidUntil != null) {
 			dateTimePicker.updateDate(mValidUntil.getYear()+1900, mValidUntil.getMonth(), mValidUntil.getDate());
 			dateTimePicker.updateTime(mValidUntil.getHours(), mValidUntil.getMinutes());
 		}
 		
 		dateTimePicker.setIs24HourView(is24h);
 		dateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dateTimeDialog.setContentView(dateTimeDialogView);
 		dateTimeDialog.show();
 	}
 }
