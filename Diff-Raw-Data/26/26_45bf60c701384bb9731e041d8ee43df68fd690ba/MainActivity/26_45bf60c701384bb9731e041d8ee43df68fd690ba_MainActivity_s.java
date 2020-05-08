 package com.cfm.waker;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import com.cfm.waker.adapter.AlarmListAdapter;
 import com.cfm.waker.db.WakerDatabaseHelper;
 import com.cfm.waker.entity.Alarm;
 import com.cfm.waker.receiver.AlarmReceiver;
 import com.cfm.waker.ui.SettingActivity;
 import com.cfm.waker.ui.ShakeActivity;
 import com.cfm.waker.ui.base.BaseSlidableActivity;
 import com.cfm.waker.view.RowBlock;
 import com.cfm.waker.widget.DialTimePicker;
 import com.cfm.waker.widget.DialTimePicker.OnTimePickListener;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends BaseSlidableActivity implements OnTimePickListener{
 	
 	private TextView timeText;
 	private TextView amPm;
 	private DialTimePicker dialTimePicker;
 	
 	private TimeRunnable tRunnable;
 	private Handler timeHandler;
 	
 	private ViewMoveRunnable vmRunnable;
 	private Handler vmHandler;
 	
 	private Calendar calendar;
 	private SimpleDateFormat dateFormat;
 	
 	private boolean pickingTime;
 	
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
 		
 		Typeface typeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/swiss_ht.ttf");
 		
 		timeText = (TextView) findViewById(R.id.time);
 		timeText.setTypeface(typeface);
 		amPm = (TextView) findViewById(R.id.am_pm);
 		amPm.setTypeface(typeface);
 		
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
 		
 		viewPager = (ViewPager) findViewById(R.id.alarm_list);
 		alarmList = new ArrayList<Alarm>();
 		alarmListAdapter = new AlarmListAdapter(this, alarmList);
 		viewPager.setAdapter(alarmListAdapter);
 		
 		alarmCount = 0;
 		
 		mOnVerticallySlideListener = new OnVerticallySlideListener(){
 			int y;
 			
 			@Override
 			public void onVerticallySlidePressed(){
 				y = viewPager.getScrollY();
 				vmHandler.removeCallbacks(vmRunnable);
 				viewPager.setVisibility(View.VISIBLE);
 			}
 			
 			@Override
 			public void onVerticallySlide(int distance){
 				if(viewPager.getScrollY() >= 0 && viewPager.getScrollY() <= viewPager.getMeasuredHeight()){
					
 					viewPager.scrollTo(0, y - distance);
 				}
 			}
 			
 			@Override
 			public void onVerticallySlideReleased(){
 				Log.d(TAG, viewPager.getMeasuredHeight() + "");
 				if(viewPager.getScrollY() > viewPager.getMeasuredHeight() / 4){
 					
 					viewPagerShow(false);
 				}else{
 					viewPagerShow(true);
 				}
 			}
 		};
 		
 		addAlarmsByDatabase();
 	}
 	
 	private class TimeRunnable implements Runnable{
 		@Override
 		public void run(){
 		   
 			if(!pickingTime){
 				calendar = Calendar.getInstance();
 				
 				dialTimePicker.performDial(6 * calendar.get(Calendar.SECOND));
 				timeText.setText(dateFormat.format(calendar.getTime()));
 				if(!mApplication.is24()) amPm.setText(calendar.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");
 				
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
 				moveX += (destinationX - moveX) * 0.6F;
 				moveY += (destinationY - moveY) * 0.6F;
 				view.scrollTo((int) moveX, (int) moveY);
 				
 				Log.d(TAG, "runnable run");
 				
 				vmHandler.postDelayed(vmRunnable, 20);
 			}else{
 				if(hide){
 					Log.d(TAG, "runnable gone");
 					view.setVisibility(View.INVISIBLE);
 					hide = false;
 				}
 			}
 			
 		}
 		
 	}
 	
 	private void viewPagerShow(boolean isShow){
 		if(isShow){
 			vmRunnable = new ViewMoveRunnable(viewPager, 0, 0, false);
 		}else{
 			vmRunnable = new ViewMoveRunnable(viewPager, 0, viewPager.getMeasuredHeight() / 2, true);
 		}
 		
 		vmHandler.post(vmRunnable);
 	}
 	
 	private void addAlarm(Calendar calendar){
 		Alarm alarm = new Alarm(calendar, mApplication.is24());
 		WakerDatabaseHelper.getInstance(this).insertAlarm(alarm);
 		
 		addAlarmsByDatabase();
 	}
 	
 	private void addAlarmsByDatabase(){
 		List<Alarm> tmp_list = WakerDatabaseHelper.getInstance(this).getAlarms(mApplication.is24());
 		if(null != tmp_list)
 			addAlarmsIntoRow(tmp_list);
 		
 	}
 	
 	private void addAlarmsIntoRow(List<Alarm>  alarms){
 		alarmList.clear();
 		alarmList.addAll(alarms);
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
 		Log.d("Activity", increment + "");
 		calendar.setTimeInMillis(calendar.getTimeInMillis() + (increment * 10000));
 		timeText.setText(dateFormat.format(calendar.getTime()));
 		if(!mApplication.is24()) amPm.setText(getString(calendar.get(Calendar.AM_PM) == Calendar.AM ? R.string.am : R.string.pm));
 		
 	}
 	
 	@Override
 	public void onStopPick(){
 		pickingTime = false;
 		
 		Intent intent = new Intent(MainActivity.this, ShakeActivity.class);
 		
 		calendar.set(Calendar.SECOND, 0);
 		
 		addAlarm(calendar);
 		
 		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, ++alarmCount);
 		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
 		//Toast.makeText(this, "alarm " + dateFormat.format(calendar.getTime()) + " set!", Toast.LENGTH_LONG).show();
 		/*
 		Intent intent = new Intent(MainActivity.this, ShakeActivity.class);
 		startActivity(intent);
 		*/
 		
 		timeHandler.post(tRunnable);
 	}
 
 	@Override
 	protected Class<? extends Activity> getLeftActivityClass() {
 		// TODO Auto-generated method stub
 		return this.getClass();
 	}
 
 	@Override
 	protected Class<? extends Activity> getRightActivityClass() {
 		// TODO Auto-generated method stub
 		return SettingActivity.class;
 	}
 	
 	@Override
 	protected View getLeftView(){
 		return null;
 	}
 	
 	@Override
 	protected View getRightView(){
 		return null;
 	}
 
 }
