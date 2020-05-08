 package com.pwr.zpi;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 
 import com.pwr.zpi.database.entity.WorkoutAction;
 import com.pwr.zpi.database.entity.WorkoutActionAdvanced;
 import com.pwr.zpi.database.entity.WorkoutActionSimple;
 import com.pwr.zpi.utils.TimeFormatter;
 import com.pwr.zpi.views.CustomPicker;
 
 public class NewWorkoutActionActivity extends Activity implements OnClickListener, TextWatcher {
 	
 	private static final String TAB_SPEC_1_TAG = "TabSpec1";
 	private static final String TAB_SPEC_2_TAG = "TabSpec2";
 	private static final double MINUTES_IN_HOUR = 60;
 	private static final double SECOND_IN_MINUTE = 60;
 	private static final long MILINSECONDS_IN_HOUR = 3600000;
 	private static final long MILINSECONDS_IN_MINUTE = 60000;
 	private static final long MILINSECONDS_IN_SECOND = 1000;
 	private static final int TAB1 = 1;
 	private static final int TAB2 = 2;
 	private static final int TAB3 = 3;
 	private TabHost tabHost;
 	
 	//Interval Action
 	Button buttonActionSpeedTypeSlow;
 	Button buttonActionSpeedTypeSteady;
 	Button buttonActionSpeedTypeFast;
 	Button buttonActionTypeTime;
 	Button buttonActionTypeDistance;
 	Button buttonActionTypeActual;
 	Button buttonSpeedTypeActual;
 	Button buttonAddInterval;
 	CustomPicker pickerHours;
 	CustomPicker pickerMinutes;
 	CustomPicker pickerSec;
 	CustomPicker pickerKm;
 	CustomPicker pickerM;
 	LinearLayout timeChooser;
 	LinearLayout distanceChooser;
 	//Virtual Partner
 	Button buttonDistanceTime;
 	Button buttonDistancePace;
 	Button buttonTimePace;
 	Button buttonActual;	//contains an instance of one of the 3 other buttons
 	ScrollView scrollViewDistanceTime;
 	ScrollView scrollViewDistancePace;
 	ScrollView scrollViewTimePace;
 	ScrollView scrollViewActual;
 	
 	//VP Action Tab1
 	Button buttonTab1Add;
 	CustomPicker pickerTab1DistanceKm;
 	CustomPicker pickerTab1DistanceM;
 	CustomPicker pickerTab1TimeHours;
 	CustomPicker pickerTab1TimeMin;
 	CustomPicker pickerTab1TimeSec;
 	EditText editTextTab1Pace;
 	double distanceTab1 = 0;
 	long timeTab1 = 0;
 	double paceTab1 = -1;
 	
 	//VP Action Tab2
 	Button buttonTab2Add;
 	CustomPicker pickerTab2DistanceKm;
 	CustomPicker pickerTab2DistanceM;
 	CustomPicker pickerTab2PaceMin;
 	CustomPicker pickerTab2PaceSec;
 	EditText editTextTab2Time;
 	double distanceTab2 = 0;
 	long timeTab2 = 0;
 	double paceTab2 = 0;
 	//VP Action Tab3
 	Button buttonTab3Add;
 	CustomPicker pickerTab3TimeHours;
 	CustomPicker pickerTab3TimeMin;
 	CustomPicker pickerTab3TimeSec;
 	CustomPicker pickerTab3PaceMin;
 	CustomPicker pickerTab3PaceSec;
 	EditText editTextTab3Distance;
 	double distanceTab3 = -1;
 	long timeTab3 = 0;
 	double paceTab3 = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.action_activity);
 		
 		initTabs();
 		initFields();
 		addListeners();
 		setDataIfEdit();
 	}
 	
 	private void initTabs()
 	{
 		tabHost = (TabHost) findViewById(R.id.tabhostActions);
 		tabHost.setup();
 		TabSpec tabSpecs = tabHost.newTabSpec(TAB_SPEC_1_TAG);
 		tabSpecs.setContent(R.id.tab1IntervalAction);
 		tabSpecs.setIndicator(getResources().getString(R.string.interval));
 		tabHost.addTab(tabSpecs);
 		tabSpecs = tabHost.newTabSpec(TAB_SPEC_2_TAG);
 		tabSpecs.setContent(R.id.tab2VirtualPartner);
 		tabSpecs.setIndicator(getResources().getString(R.string.virtual_partner));
 		tabHost.addTab(tabSpecs);
 		
 	}
 	
 	private void initFields()
 	{
 		//interval action
 		buttonActionSpeedTypeSteady = (Button) findViewById(R.id.buttonSteady);
 		buttonActionSpeedTypeFast = (Button) findViewById(R.id.buttonFast);
 		buttonActionSpeedTypeSlow = (Button) findViewById(R.id.buttonSlow);
 		buttonActionTypeDistance = (Button) findViewById(R.id.buttonDistance);
 		buttonActionTypeTime = (Button) findViewById(R.id.buttonTime);
 		buttonSpeedTypeActual = buttonActionSpeedTypeSlow;
 		buttonActionTypeActual = buttonActionTypeTime;
 		buttonActionTypeActual.setSelected(true);
 		buttonSpeedTypeActual.setSelected(true);
 		buttonAddInterval = (Button) findViewById(R.id.buttonAddIntervalWorkout);
 		pickerHours = (CustomPicker) findViewById(R.id.customPickerIntervalHourPicker);
 		pickerMinutes = (CustomPicker) findViewById(R.id.customPickerIntervalMinutePicker);
 		pickerSec = (CustomPicker) findViewById(R.id.customPickerIntervalSecPicker);
 		pickerKm = (CustomPicker) findViewById(R.id.customPickerIntervalKmPicker);
 		pickerM = (CustomPicker) findViewById(R.id.customPickerIntervalMPicker);
 		distanceChooser = (LinearLayout) findViewById(R.id.linearLayoutIntervalDistanceChooser);
 		timeChooser = (LinearLayout) findViewById(R.id.linearLayoutIntervalTimeChooser);
 		//Virtual partner
 		buttonDistanceTime = (Button) findViewById(R.id.tabButtonDistanceTime);
 		buttonDistancePace = (Button) findViewById(R.id.tabButtonDistancePace);
 		buttonTimePace = (Button) findViewById(R.id.tabButtonTimePace);
 		buttonDistanceTime.setSelected(true);
 		buttonActual = buttonDistanceTime;
 		
 		scrollViewDistanceTime = (ScrollView) findViewById(R.id.tab1DistanceTime);;
 		scrollViewDistancePace = (ScrollView) findViewById(R.id.tab2DistancePace);
 		scrollViewTimePace = (ScrollView) findViewById(R.id.tab3TimePace);
 		scrollViewActual = scrollViewDistanceTime;
 		
 		//VP tab1
 		pickerTab1DistanceKm = (CustomPicker) findViewById(R.id.customPickerTab1Km);
 		pickerTab1DistanceM = (CustomPicker) findViewById(R.id.customPickerTab1M);
 		pickerTab1TimeHours = (CustomPicker) findViewById(R.id.customPickerTab1TimeHour);
 		pickerTab1TimeMin = (CustomPicker) findViewById(R.id.customPickerTab1TimeMin);
 		pickerTab1TimeSec = (CustomPicker) findViewById(R.id.customPickerTab1TimeSec);
 		buttonTab1Add = (Button) findViewById(R.id.buttonTab1Add);
 		buttonTab1Add.setEnabled(false);
 		editTextTab1Pace = (EditText) findViewById(R.id.editTextTab1Pace);
 		//VP tab2
 		pickerTab2DistanceKm = (CustomPicker) findViewById(R.id.customPickerTab2Km);
 		pickerTab2DistanceM = (CustomPicker) findViewById(R.id.customPickerTab2M);
 		pickerTab2PaceMin = (CustomPicker) findViewById(R.id.customPickerTab2PaceMin);
 		pickerTab2PaceSec = (CustomPicker) findViewById(R.id.customPickerTab2PaceSec);
 		buttonTab2Add = (Button) findViewById(R.id.buttonTab2Add);
 		editTextTab2Time = (EditText) findViewById(R.id.editTextTab2Time);
 		//VP tab3
 		pickerTab3TimeHours = (CustomPicker) findViewById(R.id.customPickerTab3TimeHour);
 		pickerTab3TimeMin = (CustomPicker) findViewById(R.id.customPickerTab3TimeMin);
 		pickerTab3TimeSec = (CustomPicker) findViewById(R.id.customPickerTab3TimeSec);
 		pickerTab3PaceMin = (CustomPicker) findViewById(R.id.customPickerTab3PaceMin);
 		pickerTab3PaceSec = (CustomPicker) findViewById(R.id.customPickerTab3PaceSec);
 		buttonTab3Add = (Button) findViewById(R.id.buttonTab3Add);
 		buttonTab3Add.setEnabled(false);
 		editTextTab3Distance = (EditText) findViewById(R.id.editTextTab3Distance);
 	}
 	
 	private void addListeners()
 	{
 		buttonDistanceTime.setOnClickListener(this);
 		buttonDistancePace.setOnClickListener(this);
 		buttonTimePace.setOnClickListener(this);
 		buttonTab1Add.setOnClickListener(this);
 		buttonTab2Add.setOnClickListener(this);
 		buttonTab3Add.setOnClickListener(this);
 		buttonActionSpeedTypeSlow.setOnClickListener(this);
 		buttonActionSpeedTypeSteady.setOnClickListener(this);
 		buttonActionSpeedTypeFast.setOnClickListener(this);
 		buttonActionTypeDistance.setOnClickListener(this);
 		buttonActionTypeTime.setOnClickListener(this);
 		buttonAddInterval.setOnClickListener(this);
 		pickerTab1DistanceKm.getDisplayEditText().addTextChangedListener(this);
 		pickerTab1DistanceM.getDisplayEditText().addTextChangedListener(this);
 		pickerTab1TimeHours.getDisplayEditText().addTextChangedListener(this);
 		pickerTab1TimeMin.getDisplayEditText().addTextChangedListener(this);
 		pickerTab1TimeSec.getDisplayEditText().addTextChangedListener(this);
 		pickerTab2DistanceKm.getDisplayEditText().addTextChangedListener(this);
 		pickerTab2DistanceM.getDisplayEditText().addTextChangedListener(this);
 		pickerTab2PaceMin.getDisplayEditText().addTextChangedListener(this);
 		pickerTab2PaceSec.getDisplayEditText().addTextChangedListener(this);
 		pickerTab3PaceMin.getDisplayEditText().addTextChangedListener(this);
 		pickerTab3PaceSec.getDisplayEditText().addTextChangedListener(this);
 		pickerTab3TimeHours.getDisplayEditText().addTextChangedListener(this);
 		pickerTab3TimeMin.getDisplayEditText().addTextChangedListener(this);
 		pickerTab3TimeSec.getDisplayEditText().addTextChangedListener(this);
 		
 	}
 	
 	@Override
 	public void onClick(View v) {
 		//if tab has changed
 		if (buttonActual != v && buttonActionTypeActual != v && buttonSpeedTypeActual != v)
 		{
 			switch (v.getId())
 			{
 				case R.id.tabButtonDistanceTime:
 					setCurrentVirtualPartnerTab(TAB1);
 					break;
 				case R.id.tabButtonDistancePace:
 					setCurrentVirtualPartnerTab(TAB2);
 					break;
 				case R.id.tabButtonTimePace:
 					setCurrentVirtualPartnerTab(TAB3);
 					break;
 				case R.id.buttonTab1Add:
 					WorkoutActionAdvanced action = new WorkoutActionAdvanced(timeTab1, distanceTab1 * 1000);
 					Intent returnIntent = new Intent();
 					returnIntent.putExtra(WorkoutAction.TAG, action);
 					setResult(RESULT_OK, returnIntent);
 					finish();
 					break;
 				case R.id.buttonTab2Add:
 					action = new WorkoutActionAdvanced(distanceTab2 * 1000, paceTab2);
 					returnIntent = new Intent();
 					returnIntent.putExtra(WorkoutAction.TAG, action);
 					setResult(RESULT_OK, returnIntent);
 					finish();
 					break;
 				case R.id.buttonTab3Add:
 					action = new WorkoutActionAdvanced(paceTab3, timeTab3);
 					returnIntent = new Intent();
 					returnIntent.putExtra(WorkoutAction.TAG, action);
 					setResult(RESULT_OK, returnIntent);
 					finish();
 					break;
 				case R.id.buttonSlow:
 					buttonSpeedTypeActual.setSelected(false);
 					buttonActionSpeedTypeSlow.setSelected(true);
 					buttonSpeedTypeActual = buttonActionSpeedTypeSlow;
 					break;
 				case R.id.buttonSteady:
 					buttonSpeedTypeActual.setSelected(false);
 					buttonActionSpeedTypeSteady.setSelected(true);
 					buttonSpeedTypeActual = buttonActionSpeedTypeSteady;
 					break;
 				case R.id.buttonFast:
 					buttonSpeedTypeActual.setSelected(false);
 					buttonActionSpeedTypeFast.setSelected(true);
 					buttonSpeedTypeActual = buttonActionSpeedTypeFast;
 					break;
 				case R.id.buttonDistance:
 					setIntervalValueType(WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE);
 					break;
 				case R.id.buttonTime:
 					setIntervalValueType(WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME);
 					break;
 				case R.id.buttonAddIntervalWorkout:
 					int speedType = 0;
 					int valueType = 0;
 					double value = 0;
 					switch (buttonActionTypeActual.getId())
 					{
 						case R.id.buttonDistance:
 							valueType = WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE;
 							value = pickerKm.getValue() * 1000 + (pickerM.getValue());
 							break;
 						case R.id.buttonTime:
 							valueType = WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME;
 							value = pickerHours.getValue() * MILINSECONDS_IN_HOUR + pickerMinutes.getValue()
 								* MILINSECONDS_IN_MINUTE + pickerSec.getValue() * MILINSECONDS_IN_SECOND;
 							
 							break;
 					}
 					
 					switch (buttonSpeedTypeActual.getId())
 					{
 						case R.id.buttonSlow:
 							speedType = WorkoutAction.ACTION_SIMPLE_SPEED_SLOW;
 							break;
 						case R.id.buttonSteady:
 							speedType = WorkoutAction.ACTION_SIMPLE_SPEED_STEADY;
 							break;
 						case R.id.buttonFast:
 							speedType = WorkoutAction.ACTION_SIMPLE_SPEED_FAST;
 							break;
 					}
 					
 					WorkoutActionSimple actionSimple = new WorkoutActionSimple(speedType, valueType, value);
 					returnIntent = new Intent();
 					returnIntent.putExtra(WorkoutAction.TAG, actionSimple);
 					setResult(RESULT_OK, returnIntent);
 					finish();
 					break;
 			}
 			
 		}
 	}
 	
 	@Override
 	public void afterTextChanged(Editable s) {
 		
 		switch (buttonActual.getId())
 		{
 			case R.id.tabButtonDistanceTime:
 				double seconds = pickerTab1TimeSec.getValue();
 				double distanceInKm = pickerTab1DistanceKm.getValue() + (double) (pickerTab1DistanceM.getValue())
 					/ 1000;
 				distanceTab1 = distanceInKm;
 				double timeInMinutes = pickerTab1TimeHours.getValue() * MINUTES_IN_HOUR
 					+ pickerTab1TimeMin.getValue() +
 					seconds / SECOND_IN_MINUTE;
 				timeTab1 = (long) (timeInMinutes * MILINSECONDS_IN_MINUTE);
 				
 				if (distanceInKm != 0)
 				{
 					buttonTab1Add.setEnabled(true);
 					double pace = timeInMinutes / distanceInKm;
 					//paceTab1 = pace;
 					editTextTab1Pace.setText(TimeFormatter.formatTimeMMSSorHHMMSS(pace) + " min/km");
 					
 				}
 				else {
 					buttonTab1Add.setEnabled(false);
 					editTextTab1Pace.setText(getResources().getString(R.string.dashes) + " min/km");
 				}
 				break;
 			case R.id.tabButtonDistancePace:
 				distanceInKm = pickerTab2DistanceKm.getValue() + (double) (pickerTab2DistanceM.getValue()) / 1000;
 				distanceTab2 = distanceInKm;
 				long paceInMilisecondsPerKm = pickerTab2PaceMin.getValue() * MILINSECONDS_IN_MINUTE
 					+ pickerTab2PaceSec.getValue() * MILINSECONDS_IN_SECOND;
				paceTab2 = (double) paceInMilisecondsPerKm / MILINSECONDS_IN_MINUTE;
 				long timeInMiliseconds = (long) (paceInMilisecondsPerKm * distanceInKm);
 				//timeTab2 = timeInMiliseconds;
 				editTextTab2Time.setText(TimeFormatter.formatTimeHHMMSS(timeInMiliseconds));
 				break;
 			case R.id.tabButtonTimePace:
 				seconds = pickerTab3PaceSec.getValue();
 				double paceInMinutesPerKm = pickerTab3PaceMin.getValue() + seconds
 					/ SECOND_IN_MINUTE;
 				paceTab3 = paceInMinutesPerKm;
 				seconds = pickerTab3TimeSec.getValue();
 				timeInMinutes = pickerTab3TimeHours.getValue() * MINUTES_IN_HOUR
 					+ pickerTab3TimeMin.getValue() +
 					(seconds) / SECOND_IN_MINUTE;
 				timeTab3 = (long) (timeInMinutes * MILINSECONDS_IN_MINUTE);
 				if (paceInMinutesPerKm != 0)
 				{
 					buttonTab3Add.setEnabled(true);
 					double distance = timeInMinutes / paceInMinutesPerKm;
 					//distanceTab3 = distance;
 					editTextTab3Distance.setText(String.format("%.3fkm", distance));
 				}
 				else {
 					buttonTab3Add.setEnabled(false);
 					editTextTab3Distance.setText(getResources().getString(R.string.dashes2));
 				}
 				break;
 		}
 	}
 	
 	private void setIntervalValueType(int type)
 	{
 		switch (type)
 		{
 			case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE:
 				buttonActionTypeActual.setSelected(false);
 				buttonActionTypeDistance.setSelected(true);
 				buttonActionTypeActual = buttonActionTypeDistance;
 				distanceChooser.setVisibility(View.VISIBLE);
 				timeChooser.setVisibility(View.GONE);
 				break;
 			case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME:
 				buttonActionTypeActual.setSelected(false);
 				buttonActionTypeTime.setSelected(true);
 				buttonActionTypeActual = buttonActionTypeTime;
 				distanceChooser.setVisibility(View.GONE);
 				timeChooser.setVisibility(View.VISIBLE);
 				break;
 		}
 	}
 	
 	//set privious data if we are editing view
 	private void setDataIfEdit()
 	{
 		Intent intent = getIntent();
 		if (intent.hasExtra(WorkoutAction.TAG))
 		{
 			WorkoutAction workoutAction = intent.getParcelableExtra(WorkoutAction.TAG);
 			setTab(workoutAction);
 		}
 	}
 	
 	private void setTab(WorkoutAction action)
 	{
 		if (action.isSimple())
 		{
 			setIntervalTab((WorkoutActionSimple) action);
 		}
 		else if (action.isAdvanced())
 		{
 			setVirtualPartnerTab((WorkoutActionAdvanced) action);
 		}
 		
 	}
 	
 	private void setVirtualPartnerTab(WorkoutActionAdvanced actionAdvanced)
 	{
 		tabHost.setCurrentTab(1);
 		switch (actionAdvanced.getType())
 		{
 		
 			case WorkoutAction.ACTION_ADVANCED_TYPE_TIME_DISTANCE:
 				setCurrentVirtualPartnerTab(TAB1);
 				setDistance(actionAdvanced.getDistance(), pickerTab1DistanceKm, pickerTab1DistanceM);
 				setTime(actionAdvanced.getTime(), pickerTab1TimeHours, pickerTab1TimeMin, pickerTab1TimeSec);
 				editTextTab1Pace.setText(TimeFormatter.formatTimeMMSSorHHMMSS(actionAdvanced.getPace()));
 				break;
 			case WorkoutAction.ACTION_ADVANCED_TYPE_DISTANCE_PACE:
 				setCurrentVirtualPartnerTab(TAB2);
 				setDistance(actionAdvanced.getDistance(), pickerTab2DistanceKm, pickerTab2DistanceM);
 				setPace(actionAdvanced.getPace(), pickerTab2PaceMin, pickerTab2PaceSec);
 				editTextTab2Time.setText(TimeFormatter.formatTimeHHMMSS(actionAdvanced.getTime()));
 				break;
 			case WorkoutAction.ACTION_ADVANCED_TYPE_PACE_TIME:
 				setCurrentVirtualPartnerTab(TAB3);
 				setTime(actionAdvanced.getTime(), pickerTab3TimeHours, pickerTab3TimeMin, pickerTab3TimeSec);
				setPace(actionAdvanced.getPace(), pickerTab3PaceMin, pickerTab3PaceSec);
 				editTextTab3Distance.setText(String.format("%.3f", actionAdvanced.getDistance() / 1000));
 				break;
 		
 		}
 		
 	}
 	
 	private void setCurrentVirtualPartnerTab(int tab)
 	{
 		switch (tab) {
 			case TAB1:
 				scrollViewActual.setVisibility(View.GONE);
 				buttonDistanceTime.setSelected(true);
 				buttonActual.setSelected(false);
 				scrollViewDistanceTime.setVisibility(View.VISIBLE);
 				buttonActual = buttonDistanceTime;
 				scrollViewActual = scrollViewDistanceTime;
 				break;
 			case TAB2:
 				scrollViewActual.setVisibility(View.GONE);
 				buttonActual.setSelected(false);
 				buttonDistancePace.setSelected(true);
 				scrollViewDistancePace.setVisibility(View.VISIBLE);
 				buttonActual = buttonDistancePace;
 				scrollViewActual = scrollViewDistancePace;
 				break;
 			case TAB3:
 				scrollViewActual.setVisibility(View.GONE);
 				buttonActual.setSelected(false);
 				buttonTimePace.setSelected(true);
 				scrollViewTimePace.setVisibility(View.VISIBLE);
 				buttonActual = buttonTimePace;
 				scrollViewActual = scrollViewTimePace;
 				break;
 		}
 	}
 	
 	private void setIntervalTab(WorkoutActionSimple actionSimple)
 	{
 		tabHost.setCurrentTab(0);
 		buttonSpeedTypeActual.setSelected(false);
 		switch (actionSimple.getSpeedType())
 		{
 			case WorkoutAction.ACTION_SIMPLE_SPEED_SLOW:
 				buttonSpeedTypeActual = buttonActionSpeedTypeSlow;
 				break;
 			case WorkoutAction.ACTION_SIMPLE_SPEED_STEADY:
 				buttonSpeedTypeActual = buttonActionSpeedTypeSteady;
 				break;
 			case WorkoutAction.ACTION_SIMPLE_SPEED_FAST:
 				buttonSpeedTypeActual = buttonActionSpeedTypeFast;
 				break;
 		}
 		buttonSpeedTypeActual.setSelected(true);
 		switch (actionSimple.getValueType())
 		{
 			case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE:
 				setIntervalValueType(WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_DISTANCE);
 				double distance = actionSimple.getValue();
 				setDistance(distance, pickerKm, pickerM);
 				break;
 			case WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME:
 				setIntervalValueType(WorkoutAction.ACTION_SIMPLE_VALUE_TYPE_TIME);
 				long time = (long) actionSimple.getValue();
 				setTime(time, pickerHours, pickerMinutes, pickerSec);
 				break;
 		}
 		
 	}
 	
 	private void setTime(long time, CustomPicker pickerHour, CustomPicker pickerMinute, CustomPicker pickerSecond)
 	{
 		int hours = (int) (time / MILINSECONDS_IN_HOUR);
 		int min = (int) ((time % MILINSECONDS_IN_HOUR) / MILINSECONDS_IN_MINUTE);
		int sec = (int) Math.round(((double) (time % MILINSECONDS_IN_MINUTE) / MILINSECONDS_IN_SECOND));
 		pickerHour.setValue(hours);
 		pickerMinute.setValue(min);
 		pickerSecond.setValue(sec);
 	}
 	
 	private void setDistance(double distance, CustomPicker pickerKm, CustomPicker pickerM)
 	{
 		int km = (int) (distance / 1000);
		int m = (int) Math.round(distance % 1000);
 		pickerKm.setValue(km);
 		pickerM.setValue(m);
 	}
 	
 	private void setPace(double pace, CustomPicker pickerMinute, CustomPicker pickerSecond)
 	{
 		
 		int min = (int) (pace);
		int sec = (int) Math.round(((pace - (int) pace) * 60));
 		pickerMinute.setValue(min);
 		pickerSecond.setValue(sec);
 		
 	}
 	
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 }
