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
 import com.cfm.waker.receiver.AlarmReceiver;
 import com.cfm.waker.service.WakerService;
 import com.cfm.waker.ui.SettingActivity;
 import com.cfm.waker.ui.base.BaseSlidableActivity;
 import com.cfm.waker.widget.DebossFontText;
 import com.cfm.waker.widget.DialTimePicker;
 import com.cfm.waker.widget.DialTimePicker.OnTimePickListener;
import com.cfm.waker.widget.FontTextView;
 import com.cfm.waker.widget.WeekSelector;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.view.ViewPager;
 import android.view.View;
 import android.widget.FrameLayout;
 
 public class MainActivity extends BaseSlidableActivity implements OnTimePickListener{
 	
 	private DebossFontText timeText;
 	private DebossFontText amPm;
 	private DialTimePicker dialTimePicker;
 	private WeekSelector weekSelector;
 	
 	private TimeRunnable tRunnable;
 	private Handler timeHandler;
 	
 	private ViewMoveRunnable vmRunnable;
 	private Handler vmHandler;
 	
 	private Calendar calendar;
 	private SimpleDateFormat dateFormat;
 	
 	private boolean pickingTime;
 	
 	private FrameLayout viewPagerLayout;
 	private ViewPager viewPager;
 	private ArrayList<Alarm> alarmList;
 	private AlarmListAdapter alarmListAdapter;
 	
 	private int alarmCount;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		dialTimePicker = (DialTimePicker) findViewById(R.id.time_pick);
 		dialTimePicker.setOnTimePickListener(this);
 		
 		timeText = (DebossFontText) findViewById(R.id.time);
 		amPm = (DebossFontText) findViewById(R.id.am_pm);
 		
 		weekSelector = (WeekSelector) findViewById(R.id.selector);
 		
 		tRunnable = new TimeRunnable();
 		timeHandler = new Handler();
 		timeHandler.post(tRunnable);
 		
 		vmHandler = new Handler();
 		
 		if(mApplication.is24()){
 			dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
 			amPm.setVisibility(View.GONE);
 		}else{
 			dateFormat = new SimpleDateFormat("hh:mm", Locale.CHINA);
 			amPm.setVisibility(View.VISIBLE);
 		}
 		
 		
 		pickingTime = false;
 		
 		viewPagerLayout = (FrameLayout) findViewById(R.id.viewpager_layout);
 		
 		viewPager = (ViewPager) findViewById(R.id.alarm_list);
 		alarmList = new ArrayList<Alarm>();
 		alarmListAdapter = new AlarmListAdapter(this, alarmList);
 		viewPager.setAdapter(alarmListAdapter);
 		
 		alarmCount = 0;
 		
 		mOnSlideListener = new OnSlideListener(){
 			int y;
 			
 			@Override
 			public void onHorizontallySlidePressed(){viewPagerShow(false);}
 			
 			@Override
 			public void onHorizontallySlide(int distance){}
 			
 			@Override
 			public void onHorizontallySlideReleased(boolean isActionPerformed){}
 			
 			@Override
 			public void onVerticallySlidePressed(){
 				y = viewPagerLayout.getScrollY();
 				vmHandler.removeCallbacks(vmRunnable);
 				viewPagerLayout.setVisibility(View.VISIBLE);
 			}
 			
 			@Override
 			public void onVerticallySlide(int distance){
 				int dis = y - distance;
 				
 				if(dis <= 0 ){
 					dis = 0;
 				}
 				
 				if(dis >= viewPagerLayout.getMeasuredHeight()){
 					dis = viewPagerLayout.getMeasuredHeight();
 				}
 				
 				viewPagerLayout.scrollTo(0, dis);
 			}
 			
 			@Override
 			public void onVerticallySlideReleased(){
 				WLog.print(TAG, viewPagerLayout.getMeasuredHeight() + "");
 				if(viewPagerLayout.getScrollY() > viewPagerLayout.getMeasuredHeight() / 4){
 					viewPagerShow(false);
 				}else{
 					viewPagerShow(true);
 				}
 			}
 		};
 		
 		updateAlarmsByDatabase();
 		
 		Intent serviceIntent = new Intent(this, WakerService.class);
 		startService(serviceIntent);
 	}
 	
 	//time tick movement
 	private class TimeRunnable implements Runnable{
 		@Override
 		public void run(){
 		   
 			if(!pickingTime){
 				calendar = Calendar.getInstance();
 				
 				dialTimePicker.performDial(6 * calendar.get(Calendar.SECOND));
 				timeText.setText(dateFormat.format(calendar.getTime()));
 				if(!mApplication.is24()) amPm.setText(new SimpleDateFormat("a", Locale.CHINA).format(calendar.getTime()));
 				
 				timeHandler.postDelayed(tRunnable, 500);
 				
 			}
 			
 		}
 	}
 	
 	private class ViewMoveRunnable implements Runnable{
 		private View view;
 		private int destinationX, destinationY;
 		private float moveX, moveY;
 		private boolean hide;
 
 		public ViewMoveRunnable(View view, int destinationX, int destinationY, boolean hide){
 			this.view = view;
 			moveX = view.getScrollX();
 			moveY = view.getScrollY();
 			this.destinationX = destinationX;
 			this.destinationY = destinationY;
 			this.hide = hide;
 		}
 		@Override
 		public void run() {
 			if((int) moveX != destinationX || (int) moveY != destinationY){
 				if(view.getVisibility() == View.INVISIBLE) view.setVisibility(View.VISIBLE);
 				moveX += (destinationX - moveX) * 0.51F;
 				moveY += (destinationY - moveY) * 0.51F;
 				view.scrollTo((int) moveX, (int) moveY);
 				
 				WLog.print(TAG, "runnable run");
 				
 				vmHandler.postDelayed(vmRunnable, 20);
 			}else{
 				if(hide){
 					WLog.print(TAG, "runnable gone");
 					view.setVisibility(View.INVISIBLE);
 				}
 			}
 			
 		}
 		
 	}
 	
 	//show or hide the viewPager in a smooth way
 	//scroll viewPagerLayout here because if we scroll viewPager itself, viewPager will become unstable
 	//don't know why, still looking for an answer :P
 	private void viewPagerShow(boolean isShow){
 		if(isShow){
 			vmRunnable = new ViewMoveRunnable(viewPagerLayout, 0, 0, false);
 		}else{
 			vmRunnable = new ViewMoveRunnable(viewPagerLayout, 0, viewPagerLayout.getMeasuredHeight() / 2, true);
 		}
 		
 		vmHandler.post(vmRunnable);
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
 		pickingTime = false;
 		
 		//to setup a alarm and when the alarm being triggered start a AlarmReceiver anyway
 		//it's AlarmReceiver's job to decide whether start a new ShakeActivity to perform a alarm or not.
 		Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
 		
 		calendar.set(Calendar.SECOND, 0);
 		
 		final boolean beforeTime = calendar.getTimeInMillis() <= System.currentTimeMillis();
 		
 		WLog.print(TAG, "beforeTime:" + beforeTime);
 		
 		
 		
 		intent.putExtra("com.cfm.waker.alarm_id", addAlarm(calendar).getId());
 		//put a boolean into intent to tell AlarmReceiver to stop this alarm and set a new alarm for the next trigger day 
 		//in case the trigger time of this alarm is before the currentTime
 		intent.putExtra("com.cfm.waker.before_current_time", beforeTime);
 		intent.putExtra("com.cfm.wakier.flag", alarmCount + 1);
 		
 		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, ++alarmCount);
 		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
 		
 		timeHandler.post(tRunnable);
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
 	public void onResume(){
 		super.onResume();
 	}
 
 }
