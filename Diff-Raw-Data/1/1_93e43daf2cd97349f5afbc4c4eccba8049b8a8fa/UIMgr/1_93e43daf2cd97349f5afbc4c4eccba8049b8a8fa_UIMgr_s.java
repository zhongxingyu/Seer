 package com.moupress.app.ui;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import kankan.wheel.widget.WheelView;
 import kankan.wheel.widget.adapters.ArrayWheelAdapter;
 import kankan.wheel.widget.adapters.NumericWheelAdapter;
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.content.Context;
 import android.gesture.GestureOverlayView;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.moupress.app.ui.SlideButton.OnChangeListener;
 import com.moupress.app.ui.SlideButton.SlideButton;
 import com.moupress.app.ui.SlideButton.TextSlideButtonAdapter;
 import com.moupress.app.ui.SlideButton.SlideButtonAdapter;
 import com.moupress.app.Const;
 import com.moupress.app.R;
 import com.moupress.app.util.DbHelper;
 
 public class UIMgr {
 
 	Activity activity;
 
 	public UIMgr(Activity activity) {
 		this.activity = activity;
 		initAlarmSettings();
 		initUI();
 		initSoonzeControls();
 	}
 
 	/**
 	 * Initialize all UIs
 	 */
 	private void initUI() {
 		this.initHomeUI();
 		this.initSnoozeUI();
 		this.initAlarmTimeUI();
 		this.initAlarmSoundUI();
 		this.initToolbarUI();
 		this.initMainContainer();
 	}
 
 	// =======================Home UI==============================================
 	public ListView hsListView;
 	private AlarmListViewAdapter hsListAdapter;
 	private String[] hsDisplayTxt = { "Rain  10C", "No Alarm Set", "Gesture" };
 	private int[] hsDisplayIcon = { R.drawable.world, R.drawable.clock,
 			R.drawable.disc };
 	private boolean[] hsSelected = { false, false, false };
 	
 
 	/**
 	 * Initilise home screen.
 	 */
 	private void initHomeUI() {
 		hsListView = (ListView) activity.findViewById(R.id.hslistview);
 		hsListAdapter = new AlarmListViewAdapter(hsDisplayTxt, hsDisplayIcon, hsSelected);
 		hsListView.setAdapter(hsListAdapter);
 		hsListView.setOnItemClickListener(optionListOnItemClickListener);
 	}
 
 	// =======================Snooze UI==============================================
 	public ListView snoozeListView;
 	private AlarmListViewAdapter snoozeAdapter;
 	private String[] snoozeDisplayTxt = { "Gesture", "Flip", "Swing" };
 	private int[] snoozeDisplayIcon = { R.drawable.disc, R.drawable.disc,R.drawable.disc };
 	private boolean[] snoozeSelected = { true, true, true };
 
 	/**
 	 * Initialize snooze screen
 	 */
 	private void initSnoozeUI() {
 		snoozeListView = (ListView) activity.findViewById(R.id.snoozelistview);
 		snoozeAdapter = new AlarmListViewAdapter(snoozeDisplayTxt,snoozeDisplayIcon, snoozeSelected);
 		snoozeListView.setAdapter(snoozeAdapter);
 		snoozeListView.setOnItemClickListener(optionListOnItemClickListener);
 	}
 
 	// =======================Alarm Time UI==============================================
 	public ListView alarmListView;
 	private AlarmListViewAdapter alarmAdapter;
 	private WheelView hours;
 	private WheelView minutes;
 	private WheelView amOrpm;
 	private Button btnUpdateTimeOk;
 	private Button btnUpdateTimeCancel;
 
 	private String[] alarmDisplayTxt = { "8:00 am", "9:00 am", "10:00 am" };
 	private int[] alarmDisplayIcon = { R.drawable.clock, R.drawable.clock,R.drawable.clock };
 	private boolean[] alarmSelected = { false, false, false };
 	private static int ALARM_POSITION = 0;
 	private String[] AMPM = { "am", "pm" };
 	private boolean bSettingAlarmTimeDisableFlip;
 	private NewsAlarmSlidingUpPanel timeSlidingUpPanel;
 	
 	private static final String[] weekdays = new String[]{"S","M","T","W","T","F","S"};
 	private boolean[] daySelected = new boolean[]{false,false,false,false,false,false,false};
 	private SlideButtonAdapter viewAdapter;
 	private SlideButton slideBtn;
 
 	/**
 	 * Initialize Alarm Time Screen
 	 */
 	private void initAlarmTimeUI() {
 		alarmListView = (ListView) activity.findViewById(R.id.alarmlistview);
 		alarmAdapter = new AlarmListViewAdapter(alarmDisplayTxt,alarmDisplayIcon, alarmSelected);
 		alarmListView.setAdapter(alarmAdapter);
 		alarmListView.setOnItemClickListener(optionListOnItemClickListener);
 
 		alarmListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				ALARM_POSITION = position;
 
 				buttonBarSlidingUpPanel.toggle();
 				// timeSlidingUpPanel.toggle();
 				return true;
 			}
 		});
 
 		btnUpdateTimeOk = (Button) activity.findViewById(R.id.timeaddok);
 		btnUpdateTimeCancel = (Button) activity.findViewById(R.id.timeaddcancel);
 		btnUpdateTimeOk.setOnClickListener(alarmWheelButtonListener);
 		btnUpdateTimeCancel.setOnClickListener(alarmWheelButtonListener);
 		timeSlidingUpPanel = (NewsAlarmSlidingUpPanel) activity.findViewById(R.id.timeupdatepanel);
 		timeSlidingUpPanel.setOpen(false);
 
 		timeSlidingUpPanel
 				.setPanelSlidingListener(new NewsAlarmSlidingUpPanel.PanelSlidingListener() {
 
 					@Override
 					public void onSlidingUpEnd() {
 					}
 
 					@Override
 					public void onSlidingDownEnd() {
 						bSettingAlarmTimeDisableFlip = false;
 						buttonBarSlidingUpPanel.toggle();
 					}
 				});
 bSettingAlarmTimeDisableFlip = false;
 		hours = (WheelView) activity.findViewById(R.id.wheelhour);
 		minutes = (WheelView) activity.findViewById(R.id.wheelminute);
 		amOrpm = (WheelView) activity.findViewById(R.id.wheelsecond);
 
 		hours.setViewAdapter(new NumericWheelAdapter(activity, 0, 12));
 		hours.setCurrentItem(6);
 		minutes.setViewAdapter(new NumericWheelAdapter(activity, 0, 59, "%02d"));
 		minutes.setCurrentItem(30);
 		amOrpm.setViewAdapter(new ArrayWheelAdapter<String>(activity, AMPM));
 		
 		slideBtn =(SlideButton) activity.findViewById(R.id.slideBtn);
 	    slideBtn.setOnChangedListener(new OnChangeListener()
 	    {
 
 	    	public void OnChanged(int i,boolean direction,View v) {
 
 	    		if(direction == true)
 	    		{
 		    		((TextView)v).setTextColor(activity.getResources().getColor(R.color.royal_blue));
 		    		daySelected[i]=true;
 	    		}
 	    		else
 	    		{
 		    		((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));
 		    		daySelected[i]=false;
 	    		}
 	    	}
 
 			@Override
 			public void OnSelected(int i,  View v, int mode) {
 				
 				if(mode == 0)
 				{
 					if(daySelected[i]==true)
 					{
 						daySelected[i]= false;
 						((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));
 					}
 					else
 					{
 						daySelected[i]= true;
 						((TextView)v).setTextColor(activity.getResources().getColor(R.color.royal_blue));
 					}
 				}
 				else if(mode == 1)
 				{
 					daySelected[i]= true;
 					((TextView)v).setTextColor(activity.getResources().getColor(R.color.royal_blue));
 
 				}
 				else if (mode == 2)
 				{
 					daySelected[i]= false;
 					((TextView)v).setTextColor(activity.getResources().getColor(R.color.black));
 
 				}
 				
 			}
 	    	
 	    });
 		viewAdapter = new TextSlideButtonAdapter(weekdays, activity);
 		slideBtn.setViewAdapter(viewAdapter);
         bSettingAlarmTimeDisableFlip = false;
 		//System.out.println("Weekday "+weekday.getWeekDayRank());
 		slideBtn.setSlidePosition(weekday.getWeekDayRank()-1);
 		bSettingAlarmTimeDisableFlip = false;
 	}
 	
 
 	// =======================Alarm Sound UI==============================================
 	public ListView soundListView;
 	private AlarmListViewAdapter soundAdapter;
 	private String[] soundDisplayTxt = { "BBC", "933", "My Events" };
 	private int[] soundDisplayIcon = { R.drawable.radio, R.drawable.radio,R.drawable.radio };
 	private boolean[] soundSelected = { false, false, true };
 	private static final int BBC_OR_933 = 1;
 
 	/**
 	 * Initialize Alarm Sound Screen
 	 */
 	private void initAlarmSoundUI() {
 		soundListView = (ListView) activity.findViewById(R.id.soundlistview);
 		soundAdapter = new AlarmListViewAdapter(soundDisplayTxt,soundDisplayIcon, soundSelected);
 		soundListView.setAdapter(soundAdapter);
 		soundListView.setOnItemClickListener(optionListOnItemClickListener);
 	}
 	
 	public boolean[] getSoundSelected() {return soundSelected;}
 
 	// ==============Alarm Toolbar UI==============================================
 	public Button btnHome;
 	public Button btnSoonze;
 	public Button btnAlarm;
 	public Button btnSound;
 	private NewsAlarmSlidingUpPanel buttonBarSlidingUpPanel;
 
 	/**
 	 * Initialize Toolbar UI
 	 */
 	private void initToolbarUI() {
 		btnHome = (Button) activity.findViewById(R.id.homebtn);
 		btnHome.setOnClickListener(toolbarButtonListener);
 
 		btnSoonze = (Button) activity.findViewById(R.id.snoozebtn);
 		btnSoonze.setOnClickListener(toolbarButtonListener);
 
 		btnAlarm = (Button) activity.findViewById(R.id.alarmbtn);
 		btnAlarm.setOnClickListener(toolbarButtonListener);
 
 		btnSound = (Button) activity.findViewById(R.id.soundbtn);
 		btnSound.setOnClickListener(toolbarButtonListener);
 
 		buttonBarSlidingUpPanel = (NewsAlarmSlidingUpPanel) activity.findViewById(R.id.removeItemPanel);
 		buttonBarSlidingUpPanel.setOpen(true);
 		buttonBarSlidingUpPanel
 				.setPanelSlidingListener(new NewsAlarmSlidingUpPanel.PanelSlidingListener() {
 
 					@Override
 					public void onSlidingUpEnd() {
 					}
 
 					@Override
 					public void onSlidingDownEnd() {
 						bSettingAlarmTimeDisableFlip = true;
 						timeSlidingUpPanel.toggle();
 					}
 				});
 
 		alarmInfoViewSlipper = (ViewFlipper) activity.findViewById(R.id.optionflipper);
 	}
 	
 	//================Main Container==========================================
 	public LinearLayout llMainContainer = null;
 	/**
 	 * Register the main container with onTouchlistener	
 	 */
 	private void initMainContainer() {
 	    llMainContainer = (LinearLayout)this.activity.findViewById(R.id.mainContainer);
 	    llMainContainer.setOnTouchListener(new View.OnTouchListener() {
 	        float XStart = 0;
             float XEnd = 0;
             int toDisplayChildId = 0;
             static final int EFFECTIVE_MOVEMENT = 50;
             @Override
             public boolean onTouch(View v, MotionEvent event)
             {
                 
                 switch(event.getAction())
                 {
                   case MotionEvent.ACTION_DOWN:
                        //System.out.println("Action Down On Touch!! "+event.getX()+" "+event.getY()+" Action "+event.getAction());
                        XStart = XEnd = event.getX();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //System.out.println("Action Move On Touch!! "+event.getY()+" Action "+event.getAction());
                        XEnd = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(XEnd > XStart + EFFECTIVE_MOVEMENT)
                        {
                            System.out.println("from left: " + XStart + "to right: " + XEnd + " Page: "+ alarmInfoViewSlipper.getDisplayedChild());
                            toDisplayChildId = alarmInfoViewSlipper.getDisplayedChild();
                            if(toDisplayChildId != 0)
 //                               flipperListView(3);
 //                           else
                                flipperListView(toDisplayChildId - 1);
                        }
                        else if(XEnd < XStart - EFFECTIVE_MOVEMENT){
                          System.out.println("from right: " + XStart + "to left: " + XEnd);
                            toDisplayChildId = alarmInfoViewSlipper.getDisplayedChild();
                            if(toDisplayChildId != 3)
 //                               flipperListView(0);
 //                           else
                                flipperListView(toDisplayChildId + 1);
                        }
                        XEnd = XStart = 0;
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        return true;
                     default:
                         //System.out.println("Touch Event : " + event.getAction());
                         return true;
                 }
             }
         });
 	}
 
 //All Listener Events===================================
 	public GestureOverlayView gesturesView;
 	public ViewFlipper alarmInfoViewSlipper;
 	private OnListViewItemChangeListener onListViewItemChangeListener;
 
 	/**
 	 * Initialize Display alarm time text
 	 */
 	private void initAlarmSettings() {
 		DbHelper helper = new DbHelper(this.activity);
 		Calendar cal = Calendar.getInstance();
 		int hours, mins;
 		int nextAlarmPosition = -1;
 		long nextAlarm = 0;
 		// alarm Time
 		for (int i = 0; i < alarmDisplayTxt.length; i++) {
 			alarmSelected[i] = helper.GetBool(Const.ISALARMSET
 					+ Integer.toString(i));
 
 			hours = helper.GetInt(Const.Hours + Integer.toString(i));
 			mins = helper.GetInt(Const.Mins + Integer.toString(i));
 			cal.setTimeInMillis(System.currentTimeMillis());
 			if (hours != Const.DefNum && mins != Const.DefNum) {
 				cal.set(Calendar.HOUR_OF_DAY, hours);
 
 				cal.set(Calendar.MINUTE, mins);
 			}
 			hours = cal.get(Calendar.HOUR);
 			mins = cal.get(Calendar.MINUTE);
 			switch (cal.get(Calendar.AM_PM)) {
 			case Calendar.AM:
 				alarmDisplayTxt[i] = Integer.toString(hours) + ":"
 						+ String.format("%02d", mins) + " " + this.AMPM[0];
 				break;
 			case Calendar.PM:
 				alarmDisplayTxt[i] = Integer.toString(hours) + ":"
 						+ String.format("%02d", mins) + " " + this.AMPM[1];
 				break;
 			default:
 				break;
 			}
 
 			if (alarmSelected[i]) {
 				long tmp = cal.getTimeInMillis();
 				if (tmp > System.currentTimeMillis())
 					tmp += AlarmManager.INTERVAL_DAY;
 				if (nextAlarm != 0) {
 					if (nextAlarm > tmp) {
 						nextAlarm = tmp;
 						nextAlarmPosition = i;
 					}
 				} else {
 					nextAlarm = tmp;
 					nextAlarmPosition = i;
 				}
 
 			}
 		}
 		if (nextAlarmPosition != -1) {
 			hsDisplayTxt[1] = alarmDisplayTxt[nextAlarmPosition];
 		}
 	}
 
 	
 	/**
 	 * List view Item click
 	 */
 	
 	AdapterView.OnItemClickListener optionListOnItemClickListener = new AdapterView.OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 
 			switch (parent.getId()) {
 			case R.id.snoozelistview:
 				toggleSelectListItem(snoozeAdapter, snoozeSelected, position);
 				// Call back function for Snooze Mode selected/unselected
 				onListViewItemChangeListener.onSnoozeModeSelected(position,
 						snoozeSelected[position]);
 				break;
 			case R.id.soundlistview:
 				if (position <= BBC_OR_933 && soundSelected[position] == false && soundSelected[1 - position] == true) {
 					// make BBC and 993 broadcasting mutual exclusive
 					toggleSelectListItem(soundAdapter, soundSelected, 1 - position);
 				}
 				toggleSelectListItem(soundAdapter, soundSelected, position);
 				// Call back function for Alarm Sound selected/unselected
 				onListViewItemChangeListener.onAlarmSoundSelected(position,
 						soundSelected[position]);
 				break;
 			case R.id.alarmlistview:
 				toggleSelectListItem(alarmAdapter, alarmSelected, position);
 				// Call back function for alarm time selected/unselected
 				onListViewItemChangeListener.onAlarmTimeSelected(position,
 						alarmSelected[position]);
 				break;
 			case R.id.hslistview:
 				hsListViewClicked(position);
 			}
 		}
 	};
 
 	/**
 	 * Alarm Wheel's Setting Button Click
 	 */
 	Button.OnClickListener alarmWheelButtonListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 
 			switch (v.getId()) {
 			case R.id.timeaddok:
 				alarmAdapter.updateTxtArrayList("" + hours.getCurrentItem()
 						+ ":" + String.format("%02d", minutes.getCurrentItem())
 						+ " " + (amOrpm.getCurrentItem() == 0 ? "am" : "pm"),
 						ALARM_POSITION);
 				timeSlidingUpPanel.toggle();
 				// Call Back function on Alarm Time Change
 				int hours24 = amOrpm.getCurrentItem() == 0 ? hours
 						.getCurrentItem() : hours.getCurrentItem() + 12;
 				onListViewItemChangeListener.onAlarmTimeChanged(ALARM_POSITION,
 						alarmSelected[ALARM_POSITION], hours24,
 						minutes.getCurrentItem(), 0, 0, daySelected);
 				//Get Weekdays selected
 				System.out.println("Days Selected" + daySelected[0]);
 				
 				break;
 			case R.id.timeaddcancel:
 				timeSlidingUpPanel.toggle();
 				break;
 			}
 			;
 		}
 
 	};
 
 	/**
 	 * Toolbar button listener
 	 */
 	Button.OnClickListener toolbarButtonListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			switch (v.getId()) {
 			case R.id.homebtn:
 				flipperListView(Const.SCREENS.HomeUI.ordinal());
 				break;
 			case R.id.snoozebtn:
 				flipperListView(Const.SCREENS.SnoozeUI.ordinal());
 				break;
 			case R.id.alarmbtn:
 				flipperListView(Const.SCREENS.AlarmTimeUI.ordinal());
 				break;
 			case R.id.soundbtn:
 				flipperListView(Const.SCREENS.AlarmSoundUI.ordinal());
 				break;
 			default:// no match
 				System.out.println("btn is from nowhere.");
 			}
 		}
 	};
 
 	private void toggleSelectListItem(AlarmListViewAdapter listAdapter,boolean[] chked, int pos) {
 		chked[pos] = !chked[pos];
 		listAdapter.invertSelect(pos);
 		listAdapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Home Screen List Item Click Response
 	 * 
 	 * @param position
 	 */
 	private void hsListViewClicked(int position) {
 		switch (position) {
 		case 0:
 			// display weather info
 			break;
 		case 1:
 			flipperListView(Const.SCREENS.AlarmTimeUI.ordinal());
 			break;
 		case 2:
 			flipperListView(Const.SCREENS.AlarmSoundUI.ordinal());
 			break;
 		}
 	}
 
 	/**
 	 * common adapter used in all listview
 	 * 
 	 * @author Saya
 	 * 
 	 */
 	private class AlarmListViewAdapter extends BaseAdapter {
 
 		private ArrayList<NewsAlarmListItem> optionArrayList;
 		private LayoutInflater viewInflator;
 
 		public AlarmListViewAdapter(String[] displayStrings, int[] displayInts,boolean[] displayChecked) {
 			viewInflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			optionArrayList = new ArrayList<NewsAlarmListItem>();
 			loadArrayList(displayStrings, displayInts, displayChecked);
 			// txtisplays = displayStrings;
 			// icons = displayInts;
 		}
 
 		public void loadArrayList(String[] displayStrings, int[] displayInts,
 				boolean[] displayChecked) {
 			for (int i = 0; i < displayStrings.length; i++) {
 				addToArrayList(displayStrings[i], displayInts[i],displayChecked[i]);
 			}
 		}
 
 		public void addToArrayList(String displayString, int displayInt,
 				boolean displayChk) {
 
 			optionArrayList.add(new NewsAlarmListItem(displayInt,displayString, displayChk));
 		}
 
 		public void updateTxtArrayList(String displayString, int position) {
 			if (displayString.length() == 0) {
 				optionArrayList.get(position).setOptionTxt(Const.NON_WEATHER_MSG);
 			} else
 				optionArrayList.get(position).setOptionTxt(displayString);
 			this.notifyDataSetChanged();
 		}
 
 		@Override
 		public int getCount() {
 			return optionArrayList.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return optionArrayList.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = viewInflator.inflate(R.layout.home_screen_item,null);
 			}
 			ImageView imgView = (ImageView) convertView.findViewById(R.id.alarmitemicon);
 			imgView.setImageResource(optionArrayList.get(position).getOptionIcon());
 			TextView textView = (TextView) convertView.findViewById(R.id.alarmitemtxt);
 			textView.setText(optionArrayList.get(position).getOptionTxt());
 			ImageView chkImgView = (ImageView) convertView.findViewById(R.id.checked);
 			chkImgView.setImageResource(R.drawable.checkbtn);
 			if (optionArrayList.get(position).isOptionSelected()) {
 				chkImgView.setVisibility(View.VISIBLE);
 			} else {
 				chkImgView.setVisibility(View.INVISIBLE);
 			}
 			return convertView;
 		}
 
 		public void invertSelect(int position) {
 			optionArrayList.get(position).setOptionSelected(!optionArrayList.get(position).isOptionSelected());
 		}
 	}
 
 	/**
 	 * UI Flipper Animation
 	 * When user is setting alarm time using timeSlidingUpPanel, disable flip
 	 * 
 	 * @param toDisplayedChild
 	 */
 	private void flipperListView(int toDisplayedChild) {
 		if (!bSettingAlarmTimeDisableFlip) {
 			if (alarmInfoViewSlipper.getDisplayedChild() > toDisplayedChild) {
 				alarmInfoViewSlipper.setInAnimation(activity, R.anim.slidein);
 				alarmInfoViewSlipper.setOutAnimation(activity, R.anim.slideout);
 				alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild);
 			} else if (alarmInfoViewSlipper.getDisplayedChild() < toDisplayedChild) {
 				alarmInfoViewSlipper.setInAnimation(activity,R.anim.slideinfromright);
 				alarmInfoViewSlipper.setOutAnimation(activity,R.anim.slideouttoleft);
 				alarmInfoViewSlipper.setDisplayedChild(toDisplayedChild);
 			}
 			buttonBarSlidingUpPanel.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	
 	private void initSoonzeControls() {
 		gesturesView = (GestureOverlayView) activity.findViewById(R.id.gestures);
 	}
 	// =============================Consumed from otherClasses=============================================
 	public void registerListViewItemChangeListener(OnListViewItemChangeListener onListViewItemChangeListener) {
 		this.onListViewItemChangeListener = onListViewItemChangeListener;
 	}
 
 	public void updateHomeWeatherText(String displayString) {
 		hsListAdapter.updateTxtArrayList(displayString, 0);
 	}
 
 	/**
 	 * temp solution
 	 */
 	public void showSnoozeView() {
 		flipperListView(4);
 		buttonBarSlidingUpPanel.setVisibility(View.INVISIBLE);
 	}
 
 	public void updateHomeAlarmText() {
 		initAlarmSettings();
 		hsListAdapter.updateTxtArrayList(hsDisplayTxt[1], 1);
 	}
 
 }
