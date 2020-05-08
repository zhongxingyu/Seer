 package com.tools.tvguide.activities;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
 import com.tools.tvguide.R;
 import com.tools.tvguide.adapters.ChannelDetailListAdapter;
 import com.tools.tvguide.adapters.DateAdapter;
 import com.tools.tvguide.adapters.ResultProgramAdapter;
 import com.tools.tvguide.adapters.DateAdapter.DateData;
 import com.tools.tvguide.components.AlarmSettingDialog;
 import com.tools.tvguide.components.AlarmSettingDialog.OnAlarmSettingListener;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.data.Channel;
 import com.tools.tvguide.data.Program;
 import com.tools.tvguide.managers.AlarmHelper.AlarmListener;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.CollectManager;
 import com.tools.tvguide.managers.ContentManager;
 import com.tools.tvguide.views.DetailLeftGuide;
 import com.tools.tvguide.views.DetailLeftGuide.OnChannelSelectListener;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ChannelDetailActivity extends Activity implements AlarmListener 
 {
     private static final String TAG = "ChannelDetailActivity";
     private String mChannelName;
     private String mChannelId;
     private List<Channel> mChannelList;
     private TextView mChannelNameTextView;
     private TextView mDateTextView;
     private ListView mProgramListView;
     private ChannelDetailListAdapter mListViewAdapter;
     
     private ListView mDateChosenListView;
     private DateAdapter mDateAdapter;
     private Timer mTimer;
     private DetailLeftGuide mLeftMenu;
     private ImageView mFavImageView;
     
     private List<HashMap<String, String>> mProgramList;             // Key: time, title
     private List<HashMap<String, String>> mOnPlayingProgram;        // Key: time, title
     private MyProgressDialog mProgressDialog;
     private int mCurrentSelectedDay;
     private List<ResultProgramAdapter.IListItem> mItemDataList;
     
     private enum SelfMessage {MSG_UPDATE_PROGRAMS, MSG_UPDATE_ONPLAYING_PROGRAM};
     private final int TIMER_SCHEDULE_PERIOD = 3 * 60 * 1000;        // 3 minute
     private final String SEP = ":　";
 
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_channel_detail);
         
         // configure the SlidingMenu
         SlidingMenu menu = new SlidingMenu(this);
 //        menu.setMode(SlidingMenu.LEFT_RIGHT);
         menu.setMode(SlidingMenu.LEFT);
         menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
         menu.setShadowWidthRes(R.dimen.shadow_width);
         menu.setShadowDrawable(R.drawable.shadow);
         menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
         menu.setFadeDegree(0.35f);
         mLeftMenu = new DetailLeftGuide(this);
         menu.setMenu(mLeftMenu);
 //        menu.setSecondaryMenu(R.layout.channel_detail_right);
 //        menu.setSecondaryShadowDrawable(R.drawable.shadowright);
         menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
         
         mChannelId = getIntent().getStringExtra("id");
         mChannelName = getIntent().getStringExtra("name");
         mChannelList = (List<Channel>) getIntent().getSerializableExtra("channel_list");
         if (mChannelList == null)
         	mChannelList = new ArrayList<Channel>();
         mProgramList = new ArrayList<HashMap<String,String>>();
         mOnPlayingProgram = new ArrayList<HashMap<String,String>>();
         mProgressDialog = new MyProgressDialog(this);
         mCurrentSelectedDay = getProxyDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
         mItemDataList = new ArrayList<ResultProgramAdapter.IListItem>();
         AppEngine.getInstance().getAlarmHelper().addAlarmListener(this);
      
         initViews();
         updateAll();
         
         mTimer = new Timer(true);
         mTimer.schedule(new TimerTask() 
         {
             @Override
             public void run() 
             {
                 updateOnplayingProgram();
             }
         }, TIMER_SCHEDULE_PERIOD, TIMER_SCHEDULE_PERIOD);
     }
     
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         mTimer.cancel();
         AppEngine.getInstance().getAlarmHelper().removeAlarmListener(this);
     }
     
     @Override
     public void onNewIntent (Intent intent)
     {
         setIntent(intent);
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig)
     {
         super.onConfigurationChanged(newConfig);
         
         // 检测屏幕的方向：纵向或横向  
         if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
         {
             // 当前为横屏， 在此处添加额外的处理代码 
         }
         else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
         {
             // 当前为竖屏， 在此处添加额外的处理代码  
         }
     }
     
     public void initViews()
     {
         mChannelNameTextView = (TextView) findViewById(R.id.channeldetail_channel_name_tv);
         mDateTextView = (TextView) findViewById(R.id.channeldetail_date_tv);
         mProgramListView = (ListView) findViewById(R.id.channeldetail_program_listview);
         mDateChosenListView = (ListView) findViewById(R.id.channeldetail_date_chosen_listview);
         mFavImageView = (ImageView) findViewById(R.id.channeldetail_fav_iv);
         
         List<DateData> dateList = new ArrayList<DateAdapter.DateData>();
         dateList.add(new DateData(getResources().getString(R.string.Mon)));
         dateList.add(new DateData(getResources().getString(R.string.Tue)));
         dateList.add(new DateData(getResources().getString(R.string.Wed)));
         dateList.add(new DateData(getResources().getString(R.string.Thu)));
         dateList.add(new DateData(getResources().getString(R.string.Fri)));
         dateList.add(new DateData(getResources().getString(R.string.Sat)));
         dateList.add(new DateData(getResources().getString(R.string.Sun)));
         mDateAdapter = new DateAdapter(this, dateList);
         mDateChosenListView.setAdapter(mDateAdapter);
         mDateAdapter.setCurrentIndex(mCurrentSelectedDay - 1);
         
         mLeftMenu.setChannelList(mChannelList);
         for (int i=0; i<mChannelList.size(); ++i)
         {
         	if (mChannelList.get(i).id.equals(mChannelId))
         	{
         		mLeftMenu.setCurrentIndex(i);
         		mLeftMenu.setSelection(i);
         	}
         }
         mLeftMenu.setOnChannelSelectListener(new OnChannelSelectListener() 
         {
             @Override
             public void onChannelSelect(Channel channel) 
             {
                 mChannelId = channel.id;
                 mChannelName = channel.name;
                 updateAll();
             }
         });
         
         mDateChosenListView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, final int position, long id) 
             {
                 mDateAdapter.setCurrentIndex(position);
                 mCurrentSelectedDay = position + 1;
                 updateProgramList();
             }
         });
         
         mProgramListView.setOnScrollListener(new OnScrollListener() 
         {            
             @Override
             public void onScrollStateChanged(AbsListView view, int scrollState) 
             {
                 foldDateListView();
             }
             
             @Override
             public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) 
             {
             }
         });
         
         mProgramListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, final int position, long id) 
             {
                 foldDateListView();
 
                 final Program program = mListViewAdapter.getProgram(position);
                 
                 String hour = program.time.split(":")[0];
                 String minute = program.time.split(":")[1];
                 
                 AlarmSettingDialog alarmSettingDialog = new AlarmSettingDialog(ChannelDetailActivity.this, mCurrentSelectedDay, Integer.parseInt(hour),
                                 Integer.parseInt(minute), mChannelId, mChannelName, getProgramString(program.time, program.title));
                 
                 alarmSettingDialog.setAlarmSettingListener(new OnAlarmSettingListener() 
                 {
                     @Override
                     public void onAlarmSetted(boolean success) 
                     {
                         if (success)
                             mListViewAdapter.addAlarmProgram(program);
                         else
                             mListViewAdapter.removeAlarmProgram(program);
                     }
                 });
                 
                 alarmSettingDialog.show();
             }
         });
         
         Toast.makeText(ChannelDetailActivity.this, getResources().getString(R.string.alarm_tips_can_set), Toast.LENGTH_SHORT).show();
     }
     
     public void onClick(View view)
     {
         switch (view.getId()) 
         {
             case R.id.channeldetail_date_iv:
                 toggleDateListView();
                 break;
             case R.id.channeldetail_fav_iv:
                 toggleFavIcon();
                 break;
             default:
                 break;
         }
     }
     
     @Override
     public void onAlarmed(HashMap<String, Object> info) 
     {
         String programString = (String) info.get("program");
         if (programString == null)
             return;
         
         Program program = convertToProgram(programString);
         mListViewAdapter.removeAlarmProgram(program);
     }
     
     private void toggleDateListView()
     {
         if (mDateChosenListView.getVisibility() == View.GONE)
             unfoldDateListView();
         else
             foldDateListView();
     }
     
     private void foldDateListView()
     {
         if (mDateChosenListView.getVisibility() == View.VISIBLE)
         {
             Animation pushRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
             mDateChosenListView.startAnimation(pushRightOut);
             mDateChosenListView.setVisibility(View.GONE);
         }
     }
     
     private void unfoldDateListView()
     {
         if (mDateChosenListView.getVisibility() == View.GONE)
         {
             Animation pushRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
             pushRightIn.setFillAfter(true);
             mDateChosenListView.startAnimation(pushRightIn);
             mDateChosenListView.setVisibility(View.VISIBLE);
         }
     }
     
     private void toggleFavIcon()
     {
         
         CollectManager manager = AppEngine.getInstance().getCollectManager();
         if (manager.isChannelCollected(mChannelId))
         {
             collectChannel(false);
             mFavImageView.setImageResource(R.drawable.btn_fav);
         }
         else
         {
             collectChannel(true);
             mFavImageView.setImageResource(R.drawable.btn_fav_checked);
         }
     }
     
     public void collectChannel(boolean doCollect)
     {
         CollectManager manager = AppEngine.getInstance().getCollectManager();
         if (doCollect)
         {
             HashMap<String, Object> info = new HashMap<String, Object>();
             info.put("name", mChannelName);
             manager.addCollectChannel(mChannelId, info);
             Toast.makeText(this, R.string.collect_success, Toast.LENGTH_SHORT).show();
         }
         else
         {
             if (manager.isChannelCollected(mChannelId))
                 manager.removeCollectChannel(mChannelId);
             Toast.makeText(this, R.string.cancel_collect, Toast.LENGTH_SHORT).show();
         }
     }
     
     private void updateAll()
     {
    	mCurrentSelectedDay = getDayOfToday();
    	mDateAdapter.setCurrentIndex(mCurrentSelectedDay - 1);
    	
     	updateTitle();
     	updateFavIcon();
     	updateProgramList();
     }
     
     private void updateTitle()
     {
         mChannelNameTextView.setText(mChannelName);
     }
     
     private void updateFavIcon()
     {
     	if (AppEngine.getInstance().getCollectManager().isChannelCollected(mChannelId))
             mFavImageView.setImageResource(R.drawable.btn_fav_checked);
     	else
     		mFavImageView.setImageResource(R.drawable.btn_fav);
     }
 
     private void updateProgramList()
     {
         mProgramList.clear();
         mOnPlayingProgram.clear();
         boolean isSyncLoad = AppEngine.getInstance().getContentManager().loadProgramsByChannel2(mChannelId, mCurrentSelectedDay, mProgramList, 
                                 mOnPlayingProgram, new ContentManager.LoadListener() 
         {
             @Override
             public void onLoadFinish(int status) 
             {
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_UPDATE_PROGRAMS.ordinal());
             }
         });
         if (isSyncLoad == false)
             mProgressDialog.show();
     }
     
     private void updateOnplayingProgram()
     {
         mOnPlayingProgram.clear();
         AppEngine.getInstance().getContentManager().loadOnPlayingProgramByChannel(mChannelId, mOnPlayingProgram, new ContentManager.LoadListener() 
         {    
             @Override
             public void onLoadFinish(int status) 
             {
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_UPDATE_ONPLAYING_PROGRAM.ordinal());
             }
         });
     }
 
     @Override
     public void onBackPressed() 
     {
         finish();
     };
     
     public void back(View view)
     {
         if (view instanceof Button)
         {
             // The same effect with press back key
             finish();
         }
     }
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             SelfMessage selfMsg = SelfMessage.values()[msg.what];
             switch (selfMsg) 
             {
                 case MSG_UPDATE_PROGRAMS:
                     mItemDataList.clear();
                     mProgressDialog.dismiss();
                     List<Program> programList = new ArrayList<Program>();
                     for (int i=0; i<mProgramList.size(); ++i)
                     {
                         Program program = new Program();
                         program.time = mProgramList.get(i).get("time");
                         program.title = mProgramList.get(i).get("title");
                         programList.add(program);
                     }
                     mListViewAdapter = new ChannelDetailListAdapter(ChannelDetailActivity.this, programList);
                     mProgramListView.setAdapter(mListViewAdapter);
                     
                     if (isTodayChosen() && mOnPlayingProgram.size() > 0)
                     {
                         Program onplayingProgram = new Program();
                         onplayingProgram.time = mOnPlayingProgram.get(0).get("time");
                         onplayingProgram.title = mOnPlayingProgram.get(0).get("title");
                         int position = mListViewAdapter.setOnplayingProgram(onplayingProgram);
                         mProgramListView.setSelection(position);
                     }
                     
                     foldDateListView();
                     break;
                 case MSG_UPDATE_ONPLAYING_PROGRAM:
                     if (isTodayChosen() && mOnPlayingProgram.size() > 0)
                     {
                         Program onplayingProgram = new Program();
                         onplayingProgram.time = mOnPlayingProgram.get(0).get("time");
                         onplayingProgram.title = mOnPlayingProgram.get(0).get("title");
                         mListViewAdapter.setOnplayingProgram(onplayingProgram);
                     }
                     break;
                 default:
                     break;
             }
             
         }
     };
     
     private boolean isTodayChosen()
     {
    	return mCurrentSelectedDay == getDayOfToday();
    }
    
    private int getDayOfToday()
    {
    	return getProxyDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
     }
     
     /*
      * Trasfer the day to the server host day: Monday~Sunday -> 1~7
      */
     private int getProxyDay(int day)
     {
         assert(day >=1 && day <=7);
         int hostDay = 0;
         switch (day)
         {
             case Calendar.MONDAY:
                 hostDay = 1;
                 break;
             case Calendar.TUESDAY:
                 hostDay = 2;
                 break;
             case Calendar.WEDNESDAY:
                 hostDay = 3;
                 break;
             case Calendar.THURSDAY:
                 hostDay = 4;
                 break;
             case Calendar.FRIDAY:
                 hostDay = 5;
                 break;
             case Calendar.SATURDAY:
                 hostDay = 6;
                 break;
             case Calendar.SUNDAY:
                 hostDay = 7;
                 break;
         }
         return hostDay;
     }
     
     private String getProgramString(String time, String title)
     {
         return time + SEP + title;
     }
     
     private Program convertToProgram(String programString)
     {
         String[] parts = programString.split(SEP);
         if (parts.length == 2)
         {
             Program program = new Program();
             program.time = parts[0];
             program.title = parts[1];
             return program;
         }
         return null;
     }
 }
