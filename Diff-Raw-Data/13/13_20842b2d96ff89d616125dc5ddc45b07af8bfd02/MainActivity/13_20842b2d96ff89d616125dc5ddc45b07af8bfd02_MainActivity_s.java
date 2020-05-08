 /*
  * Waker project 2013
  * 
  * folks studio
  * 
  * by caifangmao8@gmail.com
  */
 package com.cfm.waker;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import com.cfm.waker.adapter.AlarmListAdapter;
 import com.cfm.waker.dao.WakerDatabaseHelper;
 import com.cfm.waker.entity.Alarm;
 import com.cfm.waker.log.WLog;
 import com.cfm.waker.service.WakerService;
 import com.cfm.waker.service.WakerService.LocalBinder;
 import com.cfm.waker.ui.SettingActivity;
 import com.cfm.waker.ui.base.BaseSlidableActivity;
 import com.cfm.waker.view.RowBlock;
 import com.cfm.waker.view.WakerViewPager;
 import com.cfm.waker.widget.DebossFontText;
 import com.cfm.waker.widget.DialPicker;
 import com.cfm.waker.widget.WakerToast;
 import com.cfm.waker.widget.DialPicker.OnTimePickListener;
 import com.cfm.waker.widget.WeekSelector;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class MainActivity extends BaseSlidableActivity implements OnTimePickListener{
 	
 	private DebossFontText timeText;
 	private DebossFontText amPm;
 	private DialPicker dialPicker;
 	private WeekSelector weekSelector;
 	private RelativeLayout dialLayout;
 	private RelativeLayout featureLayout;
 	
 	private TimeRunnable tRunnable;
 	
 	private ViewMoveRunnable vmRunnable;
 	private ViewMoveRunnable weekRunnable;
 	private ViewMoveRunnable mContentRunnable;
 	private Handler handler;
 	
 	private Calendar calendar;
 	private SimpleDateFormat dateFormat;
 	
 	private boolean pickingTime;
 	
 	private FrameLayout viewPagerLayout;
 	private WakerViewPager viewPager;
 	private ArrayList<Alarm> alarmList;
 	private AlarmListAdapter alarmListAdapter;
 	
 	private WakerService wakerService;
 	
 	private boolean isBinded;
 	
 	private ServiceConnection serviceConnection = new ServiceConnection(){
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			LocalBinder binder = (LocalBinder) service;
 			wakerService = binder.getService();
 			isBinded = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			isBinded = false;
 		}
 		
 	};
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		dialPicker = (DialPicker) findViewById(R.id.time_pick);
 		dialPicker.setOnTimePickListener(this);
 		dialLayout = (RelativeLayout) findViewById(R.id.dial_layout);
 		featureLayout = (RelativeLayout) findViewById(R.id.params_layout);
 		
 		timeText = (DebossFontText) findViewById(R.id.time);
 		amPm = (DebossFontText) findViewById(R.id.am_pm);
 		timeText.marginShow(false);
 		amPm.marginShow(false);
 		
 		weekSelector = (WeekSelector) findViewById(R.id.selector);
 		
 		tRunnable = new TimeRunnable();
 		handler = new Handler();
 		handler.post(tRunnable);
 		
 		if(mApplication.is24()){
 			dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
 			amPm.setVisibility(View.GONE);
 		}else{
 			dateFormat = new SimpleDateFormat("hh:mm", Locale.CHINA);
 			amPm.setVisibility(View.VISIBLE);
 		}
 
 		pickingTime = false;
 		
 		viewPagerLayout = (FrameLayout) findViewById(R.id.viewpager_layout);
 		
 		viewPager = (WakerViewPager) findViewById(R.id.alarm_list);
 		alarmList = new ArrayList<Alarm>();
 		alarmListAdapter = new AlarmListAdapter(this, alarmList);
 		viewPager.setAdapter(alarmListAdapter);
 		
 		setOnSlideListener(new OnSlideListener(){
 			int vy,dy;
 			
 			@Override
 			public void onHorizontallySlidePressed(){}
 			
 			@Override
 			public void onHorizontallySlide(int distance){}
 			
 			@Override
 			public void onHorizontallySlideReleased(boolean isActionPerformed){}
 			
 			@Override
 			public void onVerticallySlidePressed(){
 				vy = viewPagerLayout.getScrollY();
 				dy = dialLayout.getScrollY();
 				handler.removeCallbacks(vmRunnable);
 				handler.removeCallbacks(weekRunnable);
 				handler.removeCallbacks(mContentRunnable);
 				
 				if(vy >= viewPagerLayout.getMeasuredHeight() / 4){
 					((RowBlock) viewPager.getChildAt(viewPager.getCurrentItem())).prepareForAlarmsInit();
 				}
 			}
 			
 			@Override
 			public void onVerticallySlide(int distance){
 				if(dialPicker.getMode() != DialPicker.MODE_CONFIRM){
 					int disV = vy - distance * 2;
 					int disD = dy - distance;
 					
 					if(disD <= -viewPagerLayout.getMeasuredHeight() / 4 ){
 						disD = -viewPagerLayout.getMeasuredHeight() / 4;
 						disV = 0;
 					}
 					
 					if(disD >= 0){
 						disD = 0;
 						disV = viewPagerLayout.getMeasuredHeight() / 2;
 					}
 					
 					viewPagerLayout.scrollTo(0, disV);
 					dialLayout.scrollTo(0, disD);
 				}
 			}
 			
 			@Override
 			public void onVerticallySlideReleased(){
 				WLog.print(TAG, viewPagerLayout.getMeasuredHeight() + "");
 				int yPosition = dialLayout.getScrollY();
 				if(yPosition < -viewPagerLayout.getMeasuredHeight() / 8){
 					contentMovement(0);
 					((RowBlock) viewPager.getChildAt(viewPager.getCurrentItem())).performAlarmsInit();
 				}
 				if(yPosition >= -viewPagerLayout.getMeasuredHeight() / 8 && yPosition < featureLayout.getMeasuredHeight() / 2){
 					contentMovement(1);
 				}
 				
 				dialLayout.requestLayout();
 			}
 		});
 		
 		new Thread(){
 			@Override
 			public void run(){
 				try{
 					Thread.sleep(400);
 				}catch(Exception e){
 					
 				}
 				contentMovement(1);
 			}
 		}.start();
 		
 		updateAlarmsByDatabase();
 		
 		Intent serviceIntent = new Intent(this, WakerService.class);
 		startService(serviceIntent);
 		
 		theme.registerThemeObject(dialPicker);
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
 		Intent intent = new Intent(this, WakerService.class);
 		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
 	}
 	
 	@Override
 	public void onStop(){
 		super.onStop();
 		if(isBinded){
 			unbindService(serviceConnection);
 			isBinded = false;
 		}
 	}
 	
 	//time tick movement
 	private class TimeRunnable implements Runnable{
 		@Override
 		public void run(){
 		   
 			if(!pickingTime){
 				calendar = Calendar.getInstance();
 				
 				dialPicker.performDial(6 * calendar.get(Calendar.SECOND));
 				timeText.setText(dateFormat.format(calendar.getTime()));
 				if(!mApplication.is24()) amPm.setText(new SimpleDateFormat("a", Locale.CHINA).format(calendar.getTime()));
 				
 				handler.postDelayed(tRunnable, 500);
 				
 			}
 			
 		}
 	}
 	
 	private class ViewMoveRunnable implements Runnable{
 		private View view;
 		private int destinationX, destinationY;
 		private float moveX, moveY;
 
 		public ViewMoveRunnable(View view, int destinationX, int destinationY){
 			this.view = view;
 			moveX = view.getScrollX();
 			moveY = view.getScrollY();
 			this.destinationX = destinationX;
 			this.destinationY = destinationY;
 		}
 		
 		@Override
 		public void run() {
 			if((int) moveX != destinationX || (int) moveY != destinationY){
 				moveX += (destinationX - moveX) * 0.51F;
 				moveY += (destinationY - moveY) * 0.51F;
 				view.scrollTo((int) moveX, (int) moveY);
 				
 				handler.postDelayed(this, 20);
 			}
 			
 		}
 		
 	}
 	
 	private void contentMovement(int position){
 		handler.removeCallbacks(vmRunnable);
 		handler.removeCallbacks(weekRunnable);
 		handler.removeCallbacks(mContentRunnable);
 		
 		switch(position){
 		case 0:
 			vmRunnable = new ViewMoveRunnable(viewPagerLayout, 0, 0);
 			weekRunnable = new ViewMoveRunnable(featureLayout, 0, -featureLayout.getMeasuredHeight());
 			mContentRunnable = new ViewMoveRunnable(dialLayout, 0, -viewPagerLayout.getMeasuredHeight() / 4);
 			break;
 		case 1:
 			vmRunnable = new ViewMoveRunnable(viewPagerLayout, 0, viewPagerLayout.getMeasuredHeight() / 2);
 			weekRunnable = new ViewMoveRunnable(featureLayout, 0, -featureLayout.getMeasuredHeight());
 			mContentRunnable = new ViewMoveRunnable(dialLayout, 0, 0);
 			break;
 		case 2:
 			vmRunnable = new ViewMoveRunnable(viewPagerLayout, 0, viewPagerLayout.getMeasuredHeight() / 2);
 			weekRunnable = new ViewMoveRunnable(featureLayout, 0, 0);
 			mContentRunnable = new ViewMoveRunnable(dialLayout, 0, featureLayout.getMeasuredHeight() / 2);
 			break;
 		}
 		
 		handler.post(vmRunnable);
 		handler.post(weekRunnable);
 		handler.post(mContentRunnable);
 	}
 	
 	private Alarm addAlarm(Calendar calendar){
 		Alarm alarm = new Alarm(calendar, mApplication.is24());
 		alarm.setWeek(weekSelector.getWeekSet());
 		
 		WLog.print(TAG, "weekSelector" + Integer.toBinaryString(weekSelector.getWeekSet()) + "B");
 		
 		WakerDatabaseHelper.getInstance(this).insertAlarm(alarm);
 		
 		updateAlarmsByDatabase();
 		
 		return alarm;
 	}
 	
 	private void updateAlarmsByDatabase(){
 		List<Alarm> tmp_list = WakerDatabaseHelper.getInstance(this).getAlarms(mApplication.is24());
 
 		
 		addAlarmsIntoRow(tmp_list);
 	}
 	
 	private void addAlarmsIntoRow(List<Alarm>  alarms){
 		alarmList.clear();
 		if(null != alarms){
 			alarmList.addAll(alarms);
 		}
 		alarmListAdapter.notifyDataSetChanged();
		if(viewPager.getCurrentItem() == alarmListAdapter.getCount() - 1){
 			((RowBlock) viewPager.getChildAt(viewPager.getCurrentItem())).performLastAlarmInit();
 		}
 	}
 	
 	@Override
 	public void onStartPick(){
 		pickingTime = true;
 		calendar = Calendar.getInstance();
 		timeText.setText(dateFormat.format(calendar.getTime()));
 	}
 	
 	@Override
 	public void onPick(int value, int increment){
 		WLog.print("Activity", increment + "");
 		calendar.setTimeInMillis(calendar.getTimeInMillis() + (increment * 10000));
 		timeText.setText(dateFormat.format(calendar.getTime()));
 		if(!mApplication.is24()) amPm.setText(new SimpleDateFormat("a", Locale.CHINA).format(calendar.getTime()));
 		
 	}
 	
 	@Override
 	public void onStopPick(){
 		dialPicker.setMode(DialPicker.MODE_CONFIRM);
 		
 		contentMovement(2);
 	}
 	
 	@Override
 	public void onCenterClick(){
 		if(weekSelector.getWeekSet() == 0){
 			WakerToast.makeNegativeText(this, getString(R.string.must_select_week_of_day), Toast.LENGTH_LONG).show();
 		}else{
 			pickingTime = false;
 			
 			Alarm alarm = addAlarm(calendar);
 			
 			wakerService.setAlarm(alarm);
 			
 			WakerToast.makePositiveText(this, getString(R.string.alarm_set, alarm.getFormatedTime()), Toast.LENGTH_LONG).show();
 			
 			contentMovement(1);
 			dialPicker.setMode(DialPicker.MODE_PICK);
 			
 			handler.post(tRunnable);
 		}
 	}
 
 	@Override
 	protected Class<? extends Activity> getLeftActivityClass() {
 		return this.getClass();
 	}
 
 	@Override
 	protected Class<? extends Activity> getRightActivityClass() {
 		return SettingActivity.class;
 	}
 	
 	@Override
 	protected Drawable getLeftDrawable(){
 		return getResources().getDrawable(R.drawable.icon_exit);
 	}
 	
 	@Override
 	protected Drawable getRightDrawable(){
 		return getResources().getDrawable(R.drawable.icon_settings);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event){
 		if(keyCode == KeyEvent.KEYCODE_BACK && dialPicker.getMode() == DialPicker.MODE_CONFIRM){
 			pickingTime = false;
 			contentMovement(1);
 			dialPicker.setMode(DialPicker.MODE_PICK);
 			
 			handler.post(tRunnable);
 			
 		    return true;
 		}
 		
 		return super.onKeyDown(keyCode, event);
 	}
 
 }
