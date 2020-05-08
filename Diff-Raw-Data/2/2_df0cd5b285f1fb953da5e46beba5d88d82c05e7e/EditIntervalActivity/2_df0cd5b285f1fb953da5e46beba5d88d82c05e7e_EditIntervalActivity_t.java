 /**
  * 
  */
 package com.ctrlb.talkinterval.activity;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.ctrlb.talkinterval.R;
 import com.ctrlb.talkinterval.activity.IntervalListActivity;
 import com.ctrlb.talkinterval.model.Interval;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.ContextThemeWrapper;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class EditIntervalActivity extends SherlockFragmentActivity implements OnClickListener {
 
     private Interval mInterval;
     private AlertDialog mvalidateDialog;
 
     // form
     EditText mEdtTxtIntervalName;
     EditText mEdtTxtMinutes;
     EditText mEdtTxtSeconds;
     ToggleButton mTbtnHalfway;
     ToggleButton mTbtnMinutes;
     ToggleButton mTbtnCountdown;
     ToggleButton mTbtnName;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.edit_interval_activity);
 
 	ActionBar ab = getSupportActionBar();
 	ab.setDisplayHomeAsUpEnabled(true);
 
 	// bind onClickListeners to Buttons
 	((Button) findViewById(R.id.btn_save)).setOnClickListener(this);
 	((Button) findViewById(R.id.btn_cancel)).setOnClickListener(this);
 
 	// Get the Interval Id passed from the calling Activity
 	Bundle extras = getIntent().getExtras();
 
 	if (extras != null) {
 	    mInterval = extras.getParcelable(IntervalListActivity.SELECTED_INTERVAL_ID);
 
 	} else {
 	    mInterval = new Interval();
 	}
 
 	mEdtTxtIntervalName = (EditText) findViewById(R.id.edtTxt_interval_name);
 	mEdtTxtMinutes = (EditText) findViewById(R.id.edtTxt_minutes);
 	mEdtTxtSeconds = (EditText) findViewById(R.id.edtTxt_seconds);
 	mTbtnHalfway = (ToggleButton) findViewById(R.id.tbtn_halfway);
 	mTbtnMinutes = (ToggleButton) findViewById(R.id.tbtn_minutes);
 	mTbtnCountdown = (ToggleButton) findViewById(R.id.tbtn_countdown);
 	mTbtnName = (ToggleButton) findViewById(R.id.tbtn_name);
 
 	mEdtTxtIntervalName.addTextChangedListener(new TextWatcher() {
 
 	    @Override
 	    public void onTextChanged(CharSequence s, int start, int before, int count) {
 	    }
 
 	    @Override
 	    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 	    }
 
 	    @Override
 	    public void afterTextChanged(Editable s) {
 	    }
 	});
 
 	mEdtTxtMinutes.setOnTouchListener(new OnTouchListener() {
 	    @Override
 	    public boolean onTouch(View v, MotionEvent event) {
 
 		if (event.getAction() == MotionEvent.ACTION_UP)
 		    timeTouchHelper((EditText) v, 99);
 
 		return true;
 	    }
 	});
 
 	mEdtTxtMinutes.setOnFocusChangeListener(new OnFocusChangeListener() {
 	    @Override
 	    public void onFocusChange(View v, boolean hasFocus) {
 		if (hasFocus) {
 		} else {
 		    timeFormatHelper((EditText) v, 99);
 		}
 	    }
 	});
 
 	mEdtTxtSeconds.setOnTouchListener(new OnTouchListener() {
 	    @Override
 	    public boolean onTouch(View v, MotionEvent event) {
 
 		if (event.getAction() == MotionEvent.ACTION_UP)
 		    timeTouchHelper((EditText) v, 59);
 
 		return true;
 	    }
 	});
 
 	mEdtTxtSeconds.setOnFocusChangeListener(new OnFocusChangeListener() {
 	    @Override
 	    public void onFocusChange(View v, boolean hasFocus) {
 		if (hasFocus) {
 		} else {
 		    timeFormatHelper((EditText) v, 59);
 		}
 	    }
 	});
 
 	bindData();
 
     }
 
     private String validate() {
 
 	StringBuilder msg = new StringBuilder();
 
 	String name = mEdtTxtIntervalName.getText().toString();
 	int mins = Integer.parseInt(mEdtTxtMinutes.getText().toString());
 	int secs = Integer.parseInt(mEdtTxtSeconds.getText().toString());
 
 	if (name.equals("")) {
 	    msg.append("Interval Name cannot be blank.\n");
 	}
 
 	if (mins == 0 && secs == 0) {
 	    msg.append("Seconds & Minutes cannot both be zero.\n");
 	}
 
 	return msg.toString();
     }
 
     void timeTouchHelper(EditText editText, int maxValue) {
 
 	editText.requestFocus();
 
 	timeFormatHelper(editText, maxValue);
 
 	editText.setSelection(0, 2);
 
 	InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 	inputMethodManager.showSoftInput(editText, 0);
 
     }
 
     void timeFormatHelper(EditText editText, int maxValue) {
 
 	String txt = editText.getText().toString();
 
 	if (txt.equals(""))
 	    txt = "0";
 
 	int i = Integer.parseInt(txt);
 
 	if (i > maxValue)
 	    i = maxValue;
 
 	editText.setText(String.format("%02d", i));
 
     }
 
     @Override
     protected void onStart() {
 	super.onStart();
     }
 
     private void bindData() {
 
 	mEdtTxtIntervalName.setText(mInterval.getName());
 	mEdtTxtMinutes.setText(String.format("%02d", mInterval.getMinutes()));
 	mEdtTxtSeconds.setText(String.format("%02d", mInterval.getSeconds()));
 	mTbtnCountdown.setChecked(mInterval.getCountdownAlert());
 	mTbtnHalfway.setChecked(mInterval.getHalfwayAlert());
 	mTbtnMinutes.setChecked(mInterval.getMinutesAlert());
 	mTbtnName.setChecked(mInterval.getNameAlert());
 
 	RadioButton radioBut;
 
 	switch (mInterval.getColor()) {
 	case 2:
 	    radioBut = (RadioButton) findViewById(R.id.radio_color2);
 	    break;
 	case 3:
 	    radioBut = (RadioButton) findViewById(R.id.radio_color3);
 	    break;
 	case 4:
 	    radioBut = (RadioButton) findViewById(R.id.radio_color4);
 	    break;
 	default:
 	    radioBut = (RadioButton) findViewById(R.id.radio_color1);
 	}
 
 	radioBut.setChecked(true);
 	mEdtTxtIntervalName.requestFocus();
     }
 
     @Override
     protected void onStop() {
 	super.onStop();
     }
 
     @Override
     protected void onDestroy() {
 	super.onDestroy();
     }
 
     @Override
     public void onBackPressed() {
 	Intent in = new Intent();
 	setResult(RESULT_CANCELED, in);
 	super.onBackPressed();
     }
 
     @Override
     public void onClick(View v) {
 	switch (v.getId()) {
 	case R.id.btn_save:
 
	    timeFormatHelper((EditText) mEdtTxtMinutes, 99);
	    timeFormatHelper((EditText) mEdtTxtSeconds, 59);
 	    String msg = validate();
 
 	    if (msg.equals("")) {
 
 		String name = mEdtTxtIntervalName.getText().toString();
 		int mins = Integer.parseInt(mEdtTxtMinutes.getText().toString());
 		int secs = Integer.parseInt(mEdtTxtSeconds.getText().toString());
 		boolean alertHalf = mTbtnHalfway.isChecked();
 		boolean alertMins = mTbtnMinutes.isChecked();
 		boolean alertName = mTbtnName.isChecked();
 		boolean countdown = mTbtnCountdown.isChecked();
 
 		RadioButton radioBut1 = (RadioButton) findViewById(R.id.radio_color1);
 		RadioButton radioBut2 = (RadioButton) findViewById(R.id.radio_color2);
 		RadioButton radioBut3 = (RadioButton) findViewById(R.id.radio_color3);
 		RadioButton radioBut4 = (RadioButton) findViewById(R.id.radio_color4);
 
 		if (radioBut1.isChecked())
 		    mInterval.setColor(1);
 		if (radioBut2.isChecked())
 		    mInterval.setColor(2);
 		if (radioBut3.isChecked())
 		    mInterval.setColor(3);
 		if (radioBut4.isChecked())
 		    mInterval.setColor(4);
 
 		mInterval.setName(name);
 		mInterval.setMinutes(mins);
 		mInterval.setSeconds(secs);
 		mInterval.setHalfwayAlert(alertHalf);
 		mInterval.setMinutesAlert(alertMins);
 		mInterval.setNameAlert(alertName);
 		mInterval.setCountdownAlert(countdown);
 
 		Intent in = new Intent();
 		in.putExtra("INTERVAL_DATA", mInterval);
 		setResult(RESULT_OK, in);
 		finish();
 	    } else {
 		validateDialog(msg);
 	    }
 	    break;
 	case R.id.btn_cancel:
 	    // TODO alert users about un saved changes
 	    finish();
 	    break;
 	}
     }
 
     @Override
     public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
 
 	switch (item.getItemId()) {
 	case android.R.id.home:
 	    Intent i = new Intent(this, IntervalListActivity.class);
 	    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
 	    startActivity(i);
 	    return true;
 	}
 
 	return true;
     }
 
     private void validateDialog(String msg) {
 
 	if (mvalidateDialog == null) {
 
 	    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,
 		    R.style.TalkingIntervalDialog));
 
 	    mvalidateDialog = builder.create();
 
 	    LayoutInflater mInflater = LayoutInflater.from(this);
 	    View v = mInflater.inflate(R.layout.validate_dialog, null, false);
 
 	    Button btn = (Button) v.findViewById(R.id.btn_ok);
 
 	    btn.setOnClickListener(new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 		    // TODO Auto-generated method stub
 		    mvalidateDialog.dismiss();
 		}
 	    });
 
 	    mvalidateDialog.setView(v, 0, 0, 0, 0);
 
 	    TextView tv = (TextView) v.findViewById(R.id.tv_message);
 	    tv.setText(msg);
 
 	} else {
 	    TextView tv = (TextView) mvalidateDialog.findViewById(R.id.tv_message);
 	    tv.setText(msg);
 	}
 
 	mvalidateDialog.show();
 
     }
 
 }
