 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.activity;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.ComponentName;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.DatePicker;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.R;
 import com.morphoss.acal.StaticHelpers;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.acaltime.AcalDuration;
 import com.morphoss.acal.acaltime.AcalRepeatRule;
 import com.morphoss.acal.dataservice.CalendarDataService;
 import com.morphoss.acal.dataservice.DataRequest;
 import com.morphoss.acal.davacal.AcalAlarm;
 import com.morphoss.acal.davacal.AcalEvent;
 import com.morphoss.acal.davacal.SimpleAcalEvent;
 import com.morphoss.acal.davacal.AcalAlarm.ActionType;
 import com.morphoss.acal.davacal.AcalEvent.EVENT_FIELD;
 import com.morphoss.acal.providers.DavCollections;
 import com.morphoss.acal.service.aCalService;
 
 public class EventEdit extends Activity implements OnGestureListener, OnTouchListener, OnClickListener, OnCheckedChangeListener {
 
 	public static final String TAG = "aCal EventEdit";
 	public static final int APPLY = 0;
 	public static final int CANCEL = 1;
 
 	private SimpleAcalEvent sae;
 	private AcalEvent event;
 	private static final int FROM_DATE_DIALOG = 0;
 	private static final int FROM_TIME_DIALOG = 1;
 	private static final int UNTIL_DATE_DIALOG = 2;
 	private static final int UNTIL_TIME_DIALOG = 3;
 	private static final int SELECT_COLLECTION_DIALOG = 4;
 	private static final int ADD_ALARM_DIALOG = 5;
 	private static final int SET_REPEAT_RULE_DIALOG = 6;
 	private static final int WHICH_EVENT_DIALOG = 7;
 
 	private SharedPreferences prefs;
 	boolean prefer24hourFormat = false;
 	
 	private String[] repeatRules;
 	private String[] eventChangeRanges; // See strings.xml R.array.EventChangeAffecting
 		
 	// Must match R.array.RelativeAlarmTimes (strings.xml)
 	private String[] alarmRelativeTimeStrings;
 	private static final AcalDuration[] alarmValues = new AcalDuration[] {
 		new AcalDuration(),
 		new AcalDuration("-PT10M"),
 		new AcalDuration("-PT30M"),
 		new AcalDuration("-PT1H"),
 		new AcalDuration("-PT2H"),
 		new AcalDuration("-PT12H"),
 		new AcalDuration("-P1D")
 	};
 	
 	private String[] repeatRulesValues;
 	
 	private DataRequest dataRequest = null;
 
 	//GUI Components
 	private TextView fromLabel;
 	private TextView untilLabel;
 	private Button fromDate;
 	private Button untilDate;
 	private Button fromTime;
 	private Button untilTime;	
 	private Button applyButton;	
 	private LinearLayout sidebar;
 	private TextView eventName;
 	private TextView titlebar;
 	private TextView locationView;
 	private TextView notesView;
 	private TableLayout alarmsList;
 	private Button repeatsView;
 	private Button alarmsView;
 	private Button collection;
 	private CheckBox allDayEvent;
 	
 	//Active collections for create mode
 	private ContentValues[] activeCollections;
 	private ContentValues currentCollection;	//currently selected collection
 	private String[] collectionsArray;
 
 	private List<AcalAlarm> alarmList;
 	
 	private boolean originalHasOccurrence = false;
 	private String originalOccurence = "";
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.event_edit);
 
 		//Ensure service is actually running
 		startService(new Intent(this, aCalService.class));
 		connectToService();
 
 		// Get time display preference
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefer24hourFormat = prefs.getBoolean(getString(R.string.prefTwelveTwentyfour), false);
 
 		alarmRelativeTimeStrings = getResources().getStringArray(R.array.RelativeAlarmTimes);
 		eventChangeRanges = getResources().getStringArray(R.array.EventChangeAffecting);
 		
 		//Set up buttons
 		this.setupButton(R.id.event_apply_button, APPLY);
 		this.setupButton(R.id.event_cancel_button, CANCEL);
 
 		Bundle b = this.getIntent().getExtras();
 		getEventAction(b);
 		if ( this.event == null ) {
 			Log.d(TAG,"Unable to create AcalEvent object");
 			return;
 		}
 		this.populateLayout();
 	}
 
 	
 	private AcalEvent getEventAction(Bundle b) {
 		int operation = SimpleAcalEvent.EVENT_OPERATION_EDIT;
 		if ( b.containsKey("SimpleAcalEvent") ) {
 			SimpleAcalEvent sae = ((SimpleAcalEvent) b.getParcelable("SimpleAcalEvent"));
 			operation = sae.operation;
 			this.sae = (SimpleAcalEvent) b.getParcelable("SimpleAcalEvent");
 			this.event = sae.getAcalEvent(this);
 		}
 
 		//Get collection data
 		activeCollections = DavCollections.getCollections( getContentResolver(), DavCollections.INCLUDE_EVENTS );
 		int collectionId = -1;
 		if ( activeCollections.length > 0 )
 			collectionId = activeCollections[0].getAsInteger(DavCollections._ID);
 		else {
 			this.finish();	// can't work if no active collections
 			Toast.makeText(this, getString(R.string.errorMustHaveActiveCalendar), Toast.LENGTH_LONG);
 			return null;
 		}
 		
 		if ( operation == SimpleAcalEvent.EVENT_OPERATION_EDIT ) {
 			try {
 				collectionId = (Integer) this.event.getCollectionId();
 				this.event.setAction(AcalEvent.ACTION_MODIFY_ALL);
 				if ( event.isModifyAction() ) {
 					String rr = (String)  this.event.getRepetition();
 					if (rr != null && !rr.equals("") && !rr.equals(AcalRepeatRule.SINGLE_INSTANCE)) {
 						this.originalHasOccurrence = true;
 						this.originalOccurence = rr;
 					}
 					if (this.originalHasOccurrence) {
 						this.event.setAction(AcalEvent.ACTION_MODIFY_SINGLE);
 					}
 					else {
 						this.event.setAction(AcalEvent.ACTION_MODIFY_ALL);
 					}
 				}
 			}
 			catch (Exception e) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "Error getting data from caller: "+e.getMessage());
 			}
 		}
 		else if ( operation == SimpleAcalEvent.EVENT_OPERATION_COPY ) {
 			// Duplicate the event into a new one.
 			try {
 				collectionId = event.getCollectionId();
 			}
 			catch (Exception e) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "Error getting data from caller: "+e.getMessage());
 			}
 			this.event.setAction(AcalEvent.ACTION_CREATE);
 		}
 
 		if ( this.event == null ) {
 			AcalDateTime start; 
 			
 			try {
 				start = (AcalDateTime) b.getParcelable("DATE");
 			} catch (Exception e) {
 				start = new AcalDateTime().applyLocalTimeZone();
 			}
 			AcalDuration duration = new AcalDuration("PT1H");
 			AcalAlarm defaultAlarm = new AcalAlarm( true, "", new AcalDuration("-PT15M"),
 						ActionType.AUDIO, start, AcalDateTime.addDuration(start, duration));
 
 			if ( b.containsKey("ALLDAY") && b.getBoolean("ALLDAY", true) ) {
 				start.setDaySecond(0);
 				duration = new AcalDuration("PT24H");
 				defaultAlarm = new AcalAlarm( true, "", new AcalDuration("-PT12H"),
 							ActionType.AUDIO, start, AcalDateTime.addDuration(start, duration));
 			}
 			else if ( b.containsKey("TIME") ) {
 				start.setDaySecond((b.getInt("TIME") / 1800) * 1800);
 			}
 			else {
 				// Default to "in the next hour"
 				AcalDateTime now = new AcalDateTime().applyLocalTimeZone();
 				start.setHour(now.getHour());
 				start.setSecond(0);
 				start.setMinute(0);
 				start.addSeconds(AcalDateTime.SECONDS_IN_HOUR);
 			}
 
 			Map<EVENT_FIELD,Object> defaults = new HashMap<EVENT_FIELD,Object>(10);
 
 			defaults.put( EVENT_FIELD.startDate, start );
 			defaults.put( EVENT_FIELD.duration, duration );
 			defaults.put( EVENT_FIELD.summary, getString(R.string.NewEventTitle) );
 			defaults.put( EVENT_FIELD.location, "" );
 			defaults.put( EVENT_FIELD.description, "" );
 			
 			ContentValues collectionData = DavCollections.getRow(collectionId, getContentResolver());
 			Integer preferredCollectionId = Integer.parseInt(prefs.getString(getString(R.string.DefaultCollection_PrefKey), "-1"));
 			if ( preferredCollectionId != -1 ) {
 				for( ContentValues aCollection : activeCollections ) {
 					if ( preferredCollectionId == aCollection.getAsInteger(DavCollections._ID) ) {
 						collectionId = preferredCollectionId;
 						collectionData = aCollection;
 						break;
 					}
 				}
 			}
 			defaults.put(EVENT_FIELD.collectionId, collectionId);
 			defaults.put(EVENT_FIELD.colour, Color.parseColor(collectionData.getAsString(DavCollections.COLOUR)));
 
 			List<AcalAlarm> alarmList = new ArrayList<AcalAlarm>();
 			alarmList.add(defaultAlarm);
 			defaults.put(EVENT_FIELD.alarmList, alarmList );
 
 			this.event = new AcalEvent(defaults);
 			this.event.setAction(AcalEvent.ACTION_CREATE);
 
 		}
 		this.collectionsArray = new String[activeCollections.length];
 		int count = 0;
 		for (ContentValues cv : activeCollections) {
 			if (cv.getAsInteger(DavCollections._ID) == collectionId) this.currentCollection = cv;
 			collectionsArray[count++] = cv.getAsString(DavCollections.DISPLAYNAME);
 		}
 		
 		return event;
 	}
 
 	
 	private void setSelectedCollection(String name) {
 		for (ContentValues cv : activeCollections) {
 			if (cv.getAsString(DavCollections.DISPLAYNAME).equals(name)) {
 				this.currentCollection = cv; break;
 			}
 		}
 		this.event.setField(EVENT_FIELD.collectionId, this.currentCollection.getAsInteger(DavCollections._ID));
 		this.collection.setText(this.currentCollection.getAsString(DavCollections.DISPLAYNAME));
 		sidebar.setBackgroundColor(Color.parseColor(this.currentCollection.getAsString(DavCollections.COLOUR)));
 		this.event.setField(EVENT_FIELD.colour, Color.parseColor(currentCollection.getAsString(DavCollections.COLOUR)));
 		this.updateLayout();
 	}
 
 	
 	private void populateLayout() {
 
 		//Event Colour
 		sidebar = (LinearLayout)this.findViewById(R.id.EventEditColourBar);
 
 		//Title
 		this.eventName = (TextView) this.findViewById(R.id.EventName);
 		if ( event == null || event.getAction() == AcalEvent.ACTION_CREATE ) {
 			eventName.setSelectAllOnFocus(true);
 		}
 
 		//Collection
 		this.collection = (Button) this.findViewById(R.id.EventEditCollectionButton);
 		if (this.activeCollections.length < 2) {
 			this.collection.setEnabled(false);
 			this.collection.setHeight(0);
 			this.collection.setPadding(0, 0, 0, 0);
 		}
 		else {
 			//set up click listener for collection dialog
 			setListen(this.collection, SELECT_COLLECTION_DIALOG);
 		}
 		
 		
 		//date/time fields
 		fromLabel = (TextView) this.findViewById(R.id.EventFromLabel);
 		untilLabel = (TextView) this.findViewById(R.id.EventUntilLabel);
 		allDayEvent = (CheckBox) this.findViewById(R.id.EventAllDay);
 		fromDate = (Button) this.findViewById(R.id.EventFromDate);
 		fromTime = (Button) this.findViewById(R.id.EventFromTime);
 		untilDate = (Button) this.findViewById(R.id.EventUntilDate);
 		untilTime = (Button) this.findViewById(R.id.EventUntilTime);
 
 		applyButton = (Button) this.findViewById(R.id.event_apply_button);
 
 		//Title bar
 		titlebar = (TextView)this.findViewById(R.id.EventEditTitle);
 
 		locationView = (TextView) this.findViewById(R.id.EventLocationContent);
 		
 
 		notesView = (TextView) this.findViewById(R.id.EventNotesContent);
 		
 
 		alarmsList = (TableLayout) this.findViewById(R.id.alarms_list_table);
 		alarmsView = (Button) this.findViewById(R.id.EventAlarmsButton);
 		
 		repeatsView = (Button) this.findViewById(R.id.EventRepeatsContent);
 		
 		
 		//Button listeners
 		setListen(fromDate,FROM_DATE_DIALOG);
 		setListen(fromTime,FROM_TIME_DIALOG);
 		setListen(untilDate,UNTIL_DATE_DIALOG);
 		setListen(untilTime,UNTIL_TIME_DIALOG);
 		setListen(alarmsView,ADD_ALARM_DIALOG);
 		setListen(repeatsView,SET_REPEAT_RULE_DIALOG);
 		allDayEvent.setOnCheckedChangeListener(this);
 		if ( this.event.getDuration().getDurationMillis() == 60L*60L*24L*1000L ){
 			allDayEvent.setChecked(true);
 		}
 
 		
 		String title = event.getSummary();
 		eventName.setText(title);
 
 		String location = event.getLocation();
 		locationView.setText(location);
 
 		String description = event.getDescription();
 		notesView.setText(description);
 		
 		updateLayout();
 	}
 
 	
 	private void updateLayout() {
 		AcalDateTime start = event.getStart().applyLocalTimeZone();
 		AcalDateTime end = event.getEnd().applyLocalTimeZone();
 
 		Integer colour = event.getColour();
 		if ( colour == null ) colour = getResources().getColor(android.R.color.black);
 		sidebar.setBackgroundColor(colour);
 		eventName.setTextColor(colour);
 		
 		this.collection.setText(this.currentCollection.getAsString(DavCollections.DISPLAYNAME));
 		
 		this.applyButton.setText((event.isModifyAction() ? getString(R.string.Apply) : getString(R.string.Add)));
 		
 		boolean allDay = allDayEvent.isChecked();
 		
 		fromDate.setText(AcalDateTime.fmtDayMonthYear(start));
 		
 		if (allDay) {
 			fromLabel.setVisibility(View.GONE);
 			untilLabel.setVisibility(View.GONE);
 			untilDate.setText(""); 
 			untilDate.setVisibility(View.GONE);
 			fromTime.setText(""); 
 			fromTime.setVisibility(View.GONE);
 			untilTime.setText(""); 
 			untilTime.setVisibility(View.GONE); 
 
 			titlebar.setText(fromDate.getText());
 		}
 		else {
 			fromLabel.setVisibility(View.VISIBLE);
 			untilLabel.setVisibility(View.VISIBLE);
 			untilDate.setText(AcalDateTime.fmtDayMonthYear(end));
 			untilDate.setVisibility(View.VISIBLE);
 
 			DateFormat formatter = new SimpleDateFormat(prefer24hourFormat?"HH:mm":"hh:mmaa");
 			fromTime.setText(formatter.format(start.toJavaDate()));
 			fromTime.setVisibility(View.VISIBLE);
 			untilTime.setText(formatter.format(end.toJavaDate()));
 			untilTime.setVisibility(View.VISIBLE);
 
 			formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
 			titlebar.setText(StaticHelpers.capitaliseWords(formatter.format(start.toJavaDate())));
 
 		}
 		
 		
 		//Display Alarms
 		alarmList = event.getAlarms();
 		this.alarmsList.removeAllViews();
 		for (AcalAlarm alarm : alarmList) {
 			this.alarmsList.addView(this.getAlarmItem(alarm, alarmsList));
 		}
 		
 		//set repeat options
 		int dow = start.getWeekDay();
 		int weekNum = start.getMonthWeek();
 		String dowStr = "";
 		String dowLongString = "";
 		String everyDowString = "";
 		switch (dow) {
 			case 0:
 				dowStr="MO";
 				dowLongString = getString(R.string.Monday);
 				everyDowString = getString(R.string.EveryMonday);
 				break;
 			case 1:
 				dowStr="TU";
 				dowLongString = getString(R.string.Tuesday);
 				everyDowString = getString(R.string.EveryTuesday);
 				break;
 			case 2:
 				dowStr="WE";
 				dowLongString = getString(R.string.Wednesday);
 				everyDowString = getString(R.string.EveryWednesday);
 				break;
 			case 3:
 				dowStr="TH";
 				dowLongString = getString(R.string.Thursday);
 				everyDowString = getString(R.string.EveryThursday);
 				break;
 			case 4:
 				dowStr="FR";
 				dowLongString = getString(R.string.Friday); 
 				everyDowString = getString(R.string.EveryFriday);
 				break;
 			case 5:
 				dowStr="SA";
 				dowLongString = getString(R.string.Saturday); 
 				everyDowString = getString(R.string.EverySaturday);
 				break;
 			case 6:
 				dowStr="SU";
 				dowLongString = getString(R.string.Sunday); 	
 				everyDowString = getString(R.string.EverySunday);
 				break;
 		}
 		String dailyRepeatName = getString(R.string.EveryWeekday);
 		String dailyRepeatRule = "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR;COUNT=260";
 		if (start.get(AcalDateTime.DAY_OF_WEEK) == AcalDateTime.SATURDAY || start.get(AcalDateTime.DAY_OF_WEEK) == AcalDateTime.SUNDAY) {
 			dailyRepeatName = getString(R.string.EveryWeekend);
 			dailyRepeatRule = "FREQ=WEEKLY;BYDAY=SA,SU;COUNT=104";
 		}
 
 		this.repeatRules = new String[] {
 					getString(R.string.OnlyOnce),
 					getString(R.string.EveryDay),
 					dailyRepeatName,
 					everyDowString,
 					String.format(this.getString(R.string.EveryNthOfTheMonth),
 								start.getMonthDay()+AcalDateTime.getSuffix(start.getMonthDay())),
 					String.format(this.getString(R.string.EveryMonthOnTheNthSomeday),
 								weekNum+AcalDateTime.getSuffix(weekNum)+" "+dowLongString),
 					getString(R.string.EveryYear)
 		};
 		this.repeatRulesValues = new String[] {
 				"FREQ=DAILY;COUNT=400",
 				dailyRepeatRule,
 				"FREQ=WEEKLY;BYDAY="+dowStr,
 				"FREQ=MONTHLY;COUNT=60",
 				"FREQ=MONTHLY;COUNT=60;BYDAY="+weekNum+dowStr,
 				"FREQ=YEARLY"
 		};
 		String repeatRuleString = event.getRepetition();
 		if (repeatRuleString == null) repeatRuleString = "";
 		AcalRepeatRule RRule;
 		try {
 			RRule = new AcalRepeatRule(start, repeatRuleString); 
 		}
 		catch( IllegalArgumentException  e ) {
 			Log.i(TAG,"Illegal repeat rule: '"+repeatRuleString+"'");
 			RRule = new AcalRepeatRule(start, null ); 
 		}
 		String rr = RRule.repeatRule.toPrettyString(this);
 		if (rr == null || rr.equals("")) rr = getString(R.string.OnlyOnce);
 		repeatsView.setText(rr);
 	}
 
 	private void setListen(Button b, final int dialog) {
 		b.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				showDialog(dialog);
 
 			}
 		});
 	}
 
 	
 	public void applyChanges() {
 		//check if text fields changed
 		//summary
 		String oldSum = event.getSummary();
 		String newSum = this.eventName.getText().toString() ;
 		String oldLoc = event.getLocation();
 		String newLoc = this.locationView.getText().toString();
 		String oldDesc = event.getDescription();
 		String newDesc = this.notesView.getText().toString() ;
 		
 		if (!oldSum.equals(newSum)) event.setField(EVENT_FIELD.summary, newSum);
 		if (!oldLoc.equals(newLoc)) event.setField(EVENT_FIELD.location, newLoc);
 		if (!oldDesc.equals(newDesc)) event.setField(EVENT_FIELD.description, newDesc);
 		
 		AcalDateTime start = event.getStart();
 		//check if all day
 		if (allDayEvent.isChecked()) {
 			start.setDaySecond(0);
 			start.setAsDate(true);
 			event.setField(EVENT_FIELD.startDate,start);
 			event.setField(EVENT_FIELD.duration,  new AcalDuration("PT24H"));
 		}
 
 		AcalDuration duration = event.getDuration();
 		// Ensure end is not before start
 		if ( duration.getDays() < 0 || duration.getTimeMillis() < 0 ) {
 			start = event.getStart();
 			AcalDateTime end = AcalDateTime.addDuration(start, duration);
 			while( end.before(start) ) end.addDays(1);
 			duration = start.getDurationTo(end);
 			event.setField(EVENT_FIELD.duration, duration);
 		}
 		
 		if (event.getAction() == AcalEvent.ACTION_CREATE ||
 				event.getAction() == AcalEvent.ACTION_MODIFY_ALL) {
 			if ( !this.saveChanges() ){
 				Toast.makeText(this, "Save failed: retrying!", Toast.LENGTH_LONG).show();
 				this.saveChanges();
 			}
 			return; 
 		}
 		
 		//ask the user which instance(s) to apply to
 		this.showDialog(WHICH_EVENT_DIALOG);
 
 	}
 
 	private boolean saveChanges() {
 		
 		try {
 			this.dataRequest.eventChanged(event);
 
 			Log.i(TAG,"Saving event with action " + event.getAction() );
 			if (event.getAction() == AcalEvent.ACTION_CREATE)
 				Toast.makeText(this, getString(R.string.EventSaved), Toast.LENGTH_LONG).show();
 			else if (event.getAction() == AcalEvent.ACTION_MODIFY_ALL)
 				Toast.makeText(this, getString(R.string.ModifiedAllInstances), Toast.LENGTH_LONG).show();
 			else if (event.getAction() == AcalEvent.ACTION_MODIFY_SINGLE)
 				Toast.makeText(this, getString(R.string.ModifiedOneInstance), Toast.LENGTH_LONG).show();
 			else if (event.getAction() == AcalEvent.ACTION_MODIFY_ALL_FUTURE)
 				Toast.makeText(this, getString(R.string.ModifiedThisAndFuture), Toast.LENGTH_LONG).show();
 
 			Intent ret = new Intent();
 			ret.putExtra("changedEvent", sae);
 			this.setResult(RESULT_OK, ret);
 
 			this.finish();
 		}
 		catch (Exception e) {
 			if ( e.getMessage() != null ) Log.d(TAG,e.getMessage());
 			if (Constants.LOG_DEBUG)Log.d(TAG,Log.getStackTraceString(e));
 			Toast.makeText(this, getString(R.string.ErrorSavingEvent), Toast.LENGTH_LONG).show();
 		}
 		return true;
 	}
 
 	private void setupButton(int id, int val) {
 		Button button = (Button) this.findViewById(id);
 		button.setOnClickListener(this);
 		button.setTag(val);
 	}
 
 	@Override
 	public boolean onDown(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
 			float arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
 			float arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onTouch(View arg0, MotionEvent arg1) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		int button = (int)((Integer)arg0.getTag());
 		switch (button) {
 		case APPLY: applyChanges(); break;
 		case CANCEL: finish();
 		}
 	}
 	
 	@Override
 	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
 		this.updateLayout();
 	}
 
 	//Dialogs
 	protected Dialog onCreateDialog(int id) {
 		AcalDateTime start = event.getStart();
 		AcalDateTime end = event.getEnd();
 		switch (id) {
 		case FROM_DATE_DIALOG:
 			return new DatePickerDialog(this,fromDateListener,
 					start.get(AcalDateTime.YEAR),
 					start.get(AcalDateTime.MONTH)-1,
 					start.get(AcalDateTime.DAY_OF_MONTH)
 			);
 		case UNTIL_DATE_DIALOG:
 			return new DatePickerDialog(this,untilDateListener,
 					end.get(AcalDateTime.YEAR),
 					end.get(AcalDateTime.MONTH)-1,
 					end.get(AcalDateTime.DAY_OF_MONTH)
 			);
 		case FROM_TIME_DIALOG:
 			return new TimePickerDialog(this, fromTimeListener,
 					start.getHour(), 
 					start.getMinute(),
 					prefer24hourFormat);
 		case UNTIL_TIME_DIALOG:
 			return new TimePickerDialog(this, untilTimeListener,
 					end.getHour(), 
 					end.getMinute(),
 					prefer24hourFormat);
 		case SELECT_COLLECTION_DIALOG:
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle(getString(R.string.ChooseACollection));
 				builder.setItems(this.collectionsArray, new DialogInterface.OnClickListener() {
 				    public void onClick(DialogInterface dialog, int item) {
 				    	setSelectedCollection(collectionsArray[item]);
 				    }
 				});
 				return builder.create();
 		case ADD_ALARM_DIALOG:
 			builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.ChooseAlarmTime));
 			builder.setItems(alarmRelativeTimeStrings, new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
			    	//translate item to equal alarmValue index 
 			    	alarmList.add(
 			    			new AcalAlarm(
 			    					true, 
 			    					event.getDescription(), 
 			    					alarmValues[item], 
 			    					ActionType.AUDIO, 
 			    					event.getStart(), 
 			    					AcalDateTime.addDuration( event.getStart(), alarmValues[item] )
 			    			)
 			    	);
 			    	event.setField(EVENT_FIELD.alarmList, alarmList);
 			    	updateLayout();
 			    }
 			});
 			return builder.create();
 		case WHICH_EVENT_DIALOG:
 			builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.ChooseInstancesToChange));
 			builder.setItems(eventChangeRanges, new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	switch (item) {
 			    		case 0: event.setAction(AcalEvent.ACTION_MODIFY_SINGLE); saveChanges(); return;
 			    		case 1: event.setAction(AcalEvent.ACTION_MODIFY_ALL); saveChanges(); return;
 			    		case 2: event.setAction(AcalEvent.ACTION_MODIFY_ALL_FUTURE); saveChanges(); return;
 			    	}
 			    }
 			});
 			return builder.create();
 		case SET_REPEAT_RULE_DIALOG:
 			builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.ChooseRepeatFrequency));
 			builder.setItems(this.repeatRules, new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
 			    	String newRule = "";
 			    	if (item != 0) {
 			    		item--;
 			    		newRule = repeatRulesValues[item];
 			    	}
 			    	if ( event.isModifyAction() ) {
 				    	if (EventEdit.this.originalHasOccurrence && !newRule.equals(EventEdit.this.originalOccurence)) {
 				    		event.setAction(AcalEvent.ACTION_MODIFY_ALL);
 				    	} else if (EventEdit.this.originalHasOccurrence) {
 				    		event.setAction(AcalEvent.ACTION_MODIFY_SINGLE);
 				    	}
 			    	}
 			    	event.setField(EVENT_FIELD.repeatRule, newRule);
 			    	updateLayout();
 			    	
 			    }
 			});
 			return builder.create();
 		default: return null;
 		}
 	}
 	// the callback received when the user "sets" the start date in the dialog
 	private DatePickerDialog.OnDateSetListener fromDateListener =
 		new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, 
 				int monthOfYear, int dayOfMonth) {
 
 			AcalDateTime start = event.getStart().clone().applyLocalTimeZone();
 			start.setYearMonthDay(year, monthOfYear + 1, dayOfMonth);
 			event.setField(EVENT_FIELD.startDate, start);
 			updateLayout();
 		}
 	};
 	
 	
 
 	// the callback received when the user "sets" the end date in the dialog
 	private DatePickerDialog.OnDateSetListener untilDateListener =
 		new DatePickerDialog.OnDateSetListener() {
 
 		public void onDateSet(DatePicker view, int year, 
 				int monthOfYear, int dayOfMonth) {
 
 			AcalDateTime start = event.getStart().clone().applyLocalTimeZone();
 			AcalDateTime end = event.getEnd().clone().applyLocalTimeZone();
 			end.setYearMonthDay(year, monthOfYear + 1, dayOfMonth);
 			event.setField(EVENT_FIELD.duration, start.getDurationTo(end));
 			updateLayout();
 		}
 	};
 
 	// the callback received when the user "sets" the start time in the dialog
 	private TimePickerDialog.OnTimeSetListener fromTimeListener =
 		new TimePickerDialog.OnTimeSetListener() {
 
 		public void onTimeSet(TimePicker view, int hour, int minute) {
 
 			AcalDateTime start = event.getStart().clone().applyLocalTimeZone();
 			start.setDaySecond(hour*3600 + minute*60);
 			event.setField(EVENT_FIELD.startDate,start);
 
 			SimpleDateFormat formatter = new SimpleDateFormat("hh:mma");
 			fromTime.setText(formatter.format(start.toJavaDate()));
 			formatter = new SimpleDateFormat("hh:mma");
 			updateLayout();
 		}
 	};
 	
 	private TimePickerDialog.OnTimeSetListener untilTimeListener =
 		new TimePickerDialog.OnTimeSetListener() {
 
 		public void onTimeSet(TimePicker view, int hour, int minute) {
 
 			AcalDateTime start = event.getStart().clone().applyLocalTimeZone();
 			AcalDateTime end = event.getEnd().clone().applyLocalTimeZone();
 			end.setDaySecond(hour*3600 + minute*60);
 			AcalDuration duration = start.getDurationTo(end);
 			event.setField(EVENT_FIELD.duration, duration);
 			SimpleDateFormat formatter = new SimpleDateFormat("hh:mma");
 			untilTime.setText(formatter.format(start.toJavaDate()));
 			updateLayout();
 		}
 	};
 
 	private void connectToService() {
 		try {
 			Intent intent = new Intent(this, CalendarDataService.class);
 			Bundle b  = new Bundle();
 			b.putInt(CalendarDataService.BIND_KEY, CalendarDataService.BIND_DATA_REQUEST);
 			intent.putExtras(b);
 			this.bindService(intent,mConnection,Context.BIND_AUTO_CREATE);
 		}
 		catch (Exception e) {
 			Log.e(TAG, "Error connecting to service: "+e.getMessage());
 		}
 	}
 
 	private synchronized void serviceIsConnected() {
 	}
 
 	private synchronized void serviceIsDisconnected() {
 		this.dataRequest = null;
 	}
 
 
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		connectToService();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		try {
 			this.unbindService(mConnection);
 		}
 		catch (IllegalArgumentException re) { }
 		finally {
 			dataRequest = null;
 		}
 	}
 	
 	public View getAlarmItem(final AcalAlarm alarm, ViewGroup parent) {
 		LinearLayout rowLayout;
 
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		TextView title = null; //, time = null, location = null;
 		rowLayout = (TableRow) inflater.inflate(R.layout.alarm_list_item, parent, false);
 
 		title = (TextView) rowLayout.findViewById(R.id.AlarmListItemTitle);
 		title.setText(alarm.toPrettyString());
 		
 		ImageView cancel = (ImageView) rowLayout.findViewById(R.id.delete_button);
 
 		rowLayout.setTag(alarm);
 		cancel.setOnClickListener(new OnClickListener(){
 			public void onClick(View v) {
 				alarmList.remove(alarm);
 				updateLayout();
 			}
 		});
 		return rowLayout;
 	}
 	
 	/************************************************************************
 	 * 					Service Connection management						*
 	 ************************************************************************/
 
 	
 	private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             // This is called when the connection with the service has been
             // established, giving us the service object we can use to
             // interact with the service.  We are communicating with our
             // service through an IDL interface, so get a client-side
             // representation of that from the raw service object.
             dataRequest = DataRequest.Stub.asInterface(service);
 			serviceIsConnected();
 		}
 		public void onServiceDisconnected(ComponentName className) {
 			serviceIsDisconnected();
 		}
 	};
 
 
 }
