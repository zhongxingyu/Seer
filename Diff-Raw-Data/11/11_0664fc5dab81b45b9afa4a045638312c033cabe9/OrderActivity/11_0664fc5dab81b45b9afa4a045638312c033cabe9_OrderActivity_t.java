 package lorent.stb.tv;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.qd.bean.EPGInfo;
 import com.qd.jni.JniLoad;
 import com.qd.jni.JniPlayer;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnVideoSizeChangedListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.SurfaceHolder.Callback;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class OrderActivity extends Activity implements Callback {
 	
 	public static final Uri CHANNEL_TB_URI = Uri.parse("content://stb.info/channel_tb");
 	public static final Uri FRELIST_TB_URI = Uri.parse("content://stb.info/frelist_tb");
 	public static final Uri AUDIO_TB_URI = Uri.parse("content://stb.info/audio_tb");
 	private static final Uri CA_TB_URI = Uri.parse("content://stb.info/ca_tb");
 	
 	private TextView mintime_order,txt_load,countDown,nvod_introduce;
 	private LinearLayout linearLayout_load,linearLayout_main,linearLayout_nvod_introduce;
 	private boolean isNVODIntroduce=false;
 	private static final int msg_nvod_introduce=1;
 	private SurfaceView linearLayout_surfaceView;
 	private ProgressBar bar2;
 	private AudioManager am;               //音量管理对象
 	private int bar2current,bar2max;       //系统音乐的最大值和当前值
 	private Timer t=new Timer();
 	private ArrayList<HashMap<String, String>> listName = new ArrayList<HashMap<String, String>>();
 	private HashMap<String, String> mapName;
 	private ArrayList<HashMap<String, Object>> listTime = new ArrayList<HashMap<String, Object>>();
 	private HashMap<String, Object> mapTime;
 	private ListView listView_name,listView_time;
 	private boolean hasSearchData=false;    //搜索到NVOD数据
 	private boolean hasOrder=false;         //有预约
 	private SurfaceView mPreview;
 	private SurfaceHolder holder;
 	private boolean isMinTV=false;      
 	private boolean isFullscreen=false;
 	private MediaPlayer mMediaPlayer;
 	private String path="";
 	private static final String TAG="OrderActivity.java";
 	private int mVideoWidth;
     private int mVideoHeight;
     private boolean mIsVideoSizeKnown = false;
     private boolean mIsVideoReadyToBePlayed = false;
     
     private TextView nvod_full_name,nvod_full_introduce,nvod_full_timeAndName,nvod_time;
     private int bannerCount=0;          //ָ指南键按下次数
 	private boolean isInput=false;     //ָ指南键输入判断
 	private boolean restart_timer=true;  //是否重启定时器
 	private boolean showAllView=false;  //状态full
 	private boolean isFull=false;
 	private boolean isIntroduce=false;
 	private boolean isbanner=false;
 	private LinearLayout linearLayoutFull,linearLayout_banner,linearLayout_introduce1,linearLayout_banner2;
 	private Timer tFull=new Timer();
 	private Timer banner_timer=new Timer();
 	private int name_serviceId=0;
 	private int name_eventId=0;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.order);
 		System.out.println("order oncreate");
 		
 		getView();
 	        
 	}
 	
 	//布局
 	private void getView(){
 		
 		linearLayoutFull = (LinearLayout)findViewById(R.id.linearLayout_nvod_fullscreen);        //全频时视频层上面的窗口 包含fullscreen1和linearLayout_horital
     	linearLayout_introduce1 = (LinearLayout)findViewById(R.id.linearLayout_nvod_full_introduce);  //节目简介的view
     	linearLayout_banner=(LinearLayout)findViewById(R.id.linearLayout_nvod_full_banner); //banner条
     	linearLayout_banner2 = (LinearLayout)findViewById(R.id.linearLayout_nvod_fullscreen2);                //banner条的view
 		nvod_full_name = (TextView)findViewById(R.id.nvod_fullscreen_name);
 		
 		nvod_full_introduce = (TextView)findViewById(R.id.nvod_fullscreen_introduce);
 		nvod_full_timeAndName = (TextView)findViewById(R.id.nvod_fullscreen_now);
 		nvod_time = (TextView)findViewById(R.id.nvod_full_time);
 		
 		bar2 = (ProgressBar)findViewById(R.id.seekbar_order);    //电视音量
         am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 		bar2max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 		bar2current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
 		bar2.setMax(bar2max);
         bar2.setProgress(bar2current);
         nvod_introduce=(TextView)findViewById(R.id.nvod_introduce);
        
         mPreview = (SurfaceView) findViewById(R.id.surfaceView_order);
 		holder = mPreview.getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		
 		
 		linearLayout_nvod_introduce = (LinearLayout)findViewById(R.id.linearLayout_nvod_introduce);
 		linearLayout_load=(LinearLayout)findViewById(R.id.LinearLayout_top_load);
 		linearLayout_main=(LinearLayout)findViewById(R.id.LinearLayout_order);
 		linearLayout_surfaceView=(SurfaceView)findViewById(R.id.surfaceView_order);
 		txt_load=(TextView)findViewById(R.id.txt_load);
 		listView_time=(ListView)findViewById(R.id.listview_time_order);
 		//全屏观看
 		listView_time.setOnItemClickListener(new OnItemClickListener(){
 
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				final TextView tNum = (TextView) view.findViewById(R.id.order_time_title);
 				nvod_full_timeAndName.setText(tNum.getText().toString()+"  "+nvod_full_name.getText().toString());
 				
 			    isMinTV=false;           //玲珑窗开关
 			    linearLayout_main.setVisibility(View.INVISIBLE);
 			    isFullscreen=true;      //全频状态开关，指视频窗口
 			    
 			    Map<String, String> mp = (Map<String, String>)listView_time.getItemAtPosition(listView_time.getSelectedItemPosition());   
 		        int ServiceId=Integer.parseInt(mp.get("ServiceId")); 
 			    changeChannel(ServiceId);
 			}});
 		listView_name=(ListView)findViewById(R.id.listview_program_order);
 		listView_name.setOnItemSelectedListener(new ListView.OnItemSelectedListener(){
         
 			public void onItemSelected(AdapterView<?> parent, View view,
 					int position, long id) {
 				// TODO Auto-generated method stub
 				if(listTime!=null)
 				listTime.clear();
 				final TextView tNum = (TextView) view.findViewById(R.id.order_name_title);
 				nvod_full_name.setText(tNum.getText().toString());
 				Map<String, String> mp = (Map<String, String>)listView_name.getItemAtPosition(listView_name.getSelectedItemPosition());   
 				name_serviceId=Integer.parseInt(mp.get("sidTempt_tag"));
 				name_eventId=Integer.parseInt(mp.get("eventId"));
 				String refSid=mp.get("sidTempt_tag"); 
 				getTimeList(refSid);
 			}
             public void onNothingSelected(AdapterView<?> parent) {
 				// TODO Auto-generated method stub
 				
 			}});
 		mintime_order=(TextView)findViewById(R.id.mintime_order);
 	        
 	        new Thread(new Runnable(){
 
 				public void run(){
 					while(true){
 					  
				        SimpleDateFormat df1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
 				        String minTime=df1.format(new Date());
 				        SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");		      
 				        String sFull = df2.format(new Date());
 				        try {
 				        	   Message msg = new Message();
 				        	   
 				        	   String[] dates ={sFull,minTime};
 						       Bundle b = new Bundle();
 						       b.putStringArray("setTime", dates);
 						       msg.setData(b);
 						       mHandler.sendMessage(msg);
 						      
 						       Thread.sleep(1000);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					
 				}}).start();
 	    
 	}
 	
 	private int[] ServiceIds=null;
 	/**
 	 * 获取时间列表
 	 * @param refSid
 	 */
 	protected void getTimeList(String refSid) {
 		// TODO Auto-generated method stub
 		
 		String sql = "RefServiceId = ?";
 		Cursor Mycursor = getContentResolver().query(CHANNEL_TB_URI,new String[]{"ServiceId"}, sql, new String[]{refSid},null);
         int length=Mycursor.getCount();
         ServiceIds=new int[length];
         for(int i=0;Mycursor.moveToNext();i++)
         {
         	ServiceIds[i]=Mycursor.getInt(Mycursor.getColumnIndex("ServiceId"));
         }
         Mycursor.close();
        
         showTimeList();      
    
         
 	}
 	private void showTimeList()
 	{
 		int length=ServiceIds.length;
 		int serviceId=0;
 	     for(int i=0;i<length;i++)
 	        {
 	        	//开始时间调整
 	        	String starth1="";
 	        	String startm1="";
 	        	serviceId=ServiceIds[i];
 	        	EPGInfo[] e=JniLoad.EpgArray(serviceId);
 	        	if(e.length<2)return;
 	        	int st=e[1].getProgramStartTime();
 	        	int starth = st / 3600 + 8;
 				if(starth>=24){
 					starth=starth%24;
 				}
 				
 				if (e[1].getProgramStartTime() % 3600 == 0) {
 					if (starth < 10) {
 						starth1 = "0" + starth;
 					} else {
 						starth1 = starth + "";
 					}
 					startm1 = "00";
 				} else {
 
 					if (starth < 10) {
 						starth1 = "0" + starth;
 					} else {
 						starth1 = starth + "";
 					}
 					int startm = e[1].getProgramStartTime() % 3600 / 60;
 
 					if (startm < 10) {
 						startm1 = "0" + startm;
 					} else {
 						startm1 = startm + "";
 					}
 				}
 
 				//结束时间调整
 				String endh1 = "";
 				String endm1 = "";
 				int endh = (e[1].getProgramStartTime() + e[1]
 						.getProgramDurationTime()) / 3600 + 8;
 	            if(endh>=24){
 	            	endh=endh%24;
 	            }
 				if ((e[1].getProgramStartTime() + e[1]
 						.getProgramDurationTime()) % 3600 == 0) {
 					if (endh < 10) {
 						endh1 = "0" + endh;
 					} else {
 						endh1 = endh + "";
 					}
 					endm1 = "00";
 				} else {
 					if (endh < 10) {
 						endh1 = "0" + endh;
 					} else {
 						endh1 = endh + "";
 					}
 
 					int endm = (e[1].getProgramStartTime() + e[1]
 							.getProgramDurationTime()) % 3600 / 60;
 
 					if (endm < 10) {
 						endm1 = "0" + endm;
 					} else {
 						endm1 = endm + "";
 					}
 				}
 	            
 				String list_time=starth1 + ":" + startm1 + "-"+ endh1 + ":" + endm1;
 				mapTime = new HashMap<String, Object>();
 				mapTime.put("title", list_time);
 				mapTime.put("image", R.drawable.presence_invisible);
 				mapTime.put("tag", st+"");
 				mapTime.put("ServiceId", serviceId+""); //为播放节目保存频道的ServerId
 				listTime.add(mapTime);
 	        }
 	        HashMap<String,Object> tempt=null;
 	        for(int i=0;i<length;i++)
 	        {
 	          for(int j=length-1;j>i;j--)
 	          {
 	        	  int now=Integer.parseInt(listTime.get(j).get("tag").toString());
 	        	  int next=Integer.parseInt(listTime.get(j-1).get("tag").toString());
 	              if(now<next)
 	              {
 	        	    tempt=listTime.get(j-1);
 	        	    listTime.set(j-1, listTime.get(j));
 	        	    listTime.set(j, tempt);
 	              }
 	          }
 	        }
 	        for(int i=0;i<length;i++)
 	        {
 	           listTime.get(i).put("num", i+1);	
 	        }
 	        SimpleAdapter adapter_time = new SimpleAdapter(OrderActivity.this, listTime,
 					R.layout.order_time_content, new String[]{"num","title","image"}, new int[]{R.id.order_time_num,R.id.order_time_title,R.id.order_time_image});
 
 			listView_time.setAdapter(adapter_time);
 	}
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		System.out.println("order resume");
 		registerReceiver(NvodMsgReceiver, new IntentFilter("lorent.stb.tv.dvb"));//注册广播
 		
 		JniLoad.SetFreqset(387000, 6875, 3);
 		JniLoad.NVODConfig(getFreqNum());
 	    JniLoad.NVODStart();
 	    setTimeout();
 	  
 	}
 	private Handler mHandler=new Handler(){
 		public void handleMessage(Message msg) {
 			Bundle b=msg.getData();
 			String[] times=b.getStringArray("setTime");
 			nvod_time.setText(times[0]);
 			String time_tempt=times[1].replace("周", "星期");
 			mintime_order.setText(time_tempt);
 			
 		};
 	};
 	
 	/**
 	 * 处理超时消息
 	 */
 	private Handler handlerTimeout = new Handler();
 	private int getFreqNum(){
 		int freqNum=0;
 		
 		String sql = "freq = ?";
 		Cursor Mycursor = getContentResolver().query(FRELIST_TB_URI, new String[]{"freqseq"}, sql, new String[]{"387000"}, null);
 		
         if(Mycursor.moveToNext())
         {
         	freqNum=Mycursor.getInt(Mycursor.getColumnIndex("freqseq"));
         }
 		
 		Mycursor.close();
         return freqNum;
 	}
 	/**
 	 * 返回事件集合
 	 * @return
 	 */
 	private int[] getSid(){
 		int freqseq=getFreqNum();
 		
 		String sql = "ServiceType = ? and FreqSeq = ?";
 		Cursor Mycursor = getContentResolver().query(CHANNEL_TB_URI,new String[]{"ServiceId"}, sql, new String[]{"4",freqseq+""}, null);
         int length=Mycursor.getCount();
         int[] serviceId=new int[length];
         for(int i=0;Mycursor.moveToNext();i++)
         {
         	serviceId[i]=Mycursor.getInt(Mycursor.getColumnIndex("ServiceId"));
         }
         Mycursor.close();
 		return serviceId;
 	}
 	/**
 	 * 超时设定
 	 */
 	private void setTimeout()
 	{
 		t.schedule(new TimerTask(){
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				if(!hasSearchData)
 				{
 				  handlerTimeout.post(runnableUi);
 				}
 			}}, 60000);
 	}
 	Runnable runnableUi = new Runnable(){
 
 		public void run() {
 			// TODO Auto-generated method stub
 			txt_load.setText("NVOD 搜索超时！");
 			OrderActivity.this.finish();
 		}};
 	 //广播接收器 接收 预约 预录提示信息
     private BroadcastReceiver NvodMsgReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			// TODO Auto-generated method stub
 			Bundle b=arg1.getExtras();
 			Message msg=new Message();
 			msg.setData(b);
 			handlerDVBMsg.sendMessage(msg);
 			}
 		    	
 	};
 	/**
 	 * 
 	 * 处理dvb消息
 	 */
 	private Handler handlerDVBMsg =new Handler(){
 		public void handleMessage(Message msg) {
 			
 			Bundle b=msg.getData();
 			for (String type : b.keySet()) {
 				String[] args = b.getStringArray(type);				
 				if(type.equals(DVB.MESSAGE_NVOD_RECV_FINISH)) {
 		                //搜索成功
 					hasSearchData=true;
 					linearLayout_load.setVisibility(View.INVISIBLE);
 					isMinTV=true;
 					linearLayout_main.setVisibility(View.VISIBLE);
 					linearLayout_surfaceView.setVisibility(View.VISIBLE);
 					getNameList();	
 				}
 				else if(type.equals(DVB.MESSAGE_EIT_RECV_PF)) 
 				{
 	                //更新listtime表
 					if(ServiceIds!=null)
 					{
 					listTime.clear();
 					showTimeList(); //只更新当前选择的节目时间
 					}
 			    }
 				
 			}			
 		};
 	};
 	/**
 	 * 获取节目列表
 	 */
 	private void getNameList(){
 		
 		int[] serviceId=getSid();
 		for(int i=0;i<serviceId.length;i++)
 		{
 		  EPGInfo[] eName=JniLoad.EpgArray(serviceId[i]);
 		  String name=eName[0].getProgramName();
 		  mapName=new HashMap<String,String>();
 		  mapName.put("num", i+1+"");
 		  mapName.put("title", name);
 		  mapName.put("sidTempt_tag", serviceId[i]+"");//保存该节目上对应的ServerId
 		  mapName.put("eventId", eName[0].getEventID()+"");
 		  listName.add(mapName);
 		}
 	
 		SimpleAdapter adapter_name = new SimpleAdapter(OrderActivity.this, listName,
 				R.layout.order_name_content, new String[]{"num","title"}, new int[]{R.id.order_name_num,R.id.order_name_title});
 
 		listView_name.setAdapter(adapter_name);
 		
 	}
 	
 	//ָ指南键输入逻辑判断
 	 private void setInput(){
 		if(restart_timer)
 		{
 			
 		 if(!isInput)
 		{
 			isInput=true;
 			full_set();
 			bannerCount++;
 		}
 		else{
 			if(bannerCount<3){
 				bannerCount++;
 			}
 		}
 		}
 	 }
 	 /**
 		 * 捕获指南键
 		 */
 		private void full_set(){
 			
 			tFull.schedule(new TimerTask(){
 
 				@Override
 				public void run() {
 					// TODO Auto-generated method stub
 					restart_timer=false;
 					if(showAllView)
 					{
 						System.out.println("show all view");
 						showAllView=false;
 						Message m=new Message();
 						m.what=2;
 						handlerBanner.sendMessage(m);
 						bannerCount=0;
 						restart_timer=true;
 					    isInput=false;
 					}
 					else
 					{
 					if(bannerCount==1){
 						//显示banner条和广告
 						System.out.println("bannerCount=1");
 						Message msg=new Message();
 						msg.what=1;
 						handlerBanner2.sendMessage(msg);
 						
 					}
 					else if(bannerCount==2)
 					{
 						//显示所有view
 						System.out.println("bannerCount=2");
 						Message msg=new Message();
 						msg.what=2;
 						handlerBanner2.sendMessage(msg);
 						
 						
 					    bannerCount=0;
 					    restart_timer=true;
 					    showAllView=true;
 					    isInput=false;
 					    
 					}
 					}
 				}}, 1000);
 		}
 		
 		/**
 		 * 显示全屏的view
 		 */
 		private Handler handlerBanner2= new Handler(){
 			public void handleMessage(Message msg) {
 				switch(msg.what)
 				{
 				case 1:
 					//显示banner条
 					if(isFullscreen)
 					{
 					isFull=true;
 					linearLayoutFull.setVisibility(View.VISIBLE);
 					if(isIntroduce)
 					{
 						isIntroduce=false;
 						linearLayout_introduce1.setVisibility(View.INVISIBLE);
 						linearLayout_introduce1.getBackground().setAlpha(180);
 					}
 					if(!isbanner)
 					{
 					isbanner=true;
 					linearLayout_banner.setVisibility(View.VISIBLE);
 					linearLayout_banner2.getBackground().setAlpha(180);
 					}
 					bannerSet();
 					}
 					break;
 				case 2:
 					//显示所有view
 					
 					isFull=true;
 					linearLayoutFull.setVisibility(View.VISIBLE);
 					if(!isIntroduce)
 					{
 					isIntroduce=true;
 					linearLayout_introduce1.setVisibility(View.VISIBLE);
 					linearLayout_introduce1.getBackground().setAlpha(180);
 					}
 					if(!isbanner)
 					{
 					isbanner=true;
 					linearLayout_banner.setVisibility(View.VISIBLE);
 					linearLayout_banner2.getBackground().setAlpha(180);
 					}
 					String full_list_instroduce=JniLoad.GetShortDescriptor(name_serviceId, name_eventId);		    		
 					nvod_full_introduce.setText(full_list_instroduce);
 					
 				    break;
 				}
 			};
 		};
 		
 		/**
 		 * 
 		 * 隐藏banner条、节目简介等信息
 		 */
 		private Handler handlerBanner = new Handler()
 		{
 			
 			public void handleMessage(Message msg){
 				
 			switch(msg.what)
 			{
 				case 1:
 					/*JniLoad.PVRStop();
 					pvr_state=Pvr.pvr_end;
 					if(isPVR)
 					{
 						setPVRList();   //预录定时结束，更新预录列表
 					}
 					TVMinActivity.this.showDialog(8);*/
 					break;
 				case 2:
 					linearLayoutFull.setVisibility(View.INVISIBLE);
 					isFull=false;
 					break;
 				case 3:
 					linearLayoutFull.setVisibility(View.INVISIBLE);
 					isFull=false;
 					bannerCount=0;       //计数器清零
 					restart_timer=true; //定时器开关打开
 				    isInput=false;
 			}
 			}
 		};
 		/**
 		 * 
 		 * 全屏状态下banner条的定时显示
 		 */
 		private void bannerSet() {
 			
 
 			banner_timer.schedule(new TimerTask() {
 
 				@Override
 				public void run() {
 					
 				Message m=new Message();
 				m.what=3;
 				handlerBanner.sendMessage(m);
 				}
 
 			}, 3000);
 		}
 	 public boolean onKeyDown(int keyCode, KeyEvent event)
 	 {
 		 if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
 	        {
 	        	
 	        	if(listView_time.hasFocus())
 	        	{
                    listView_time.setFocusable(false);
                    listView_name.setFocusable(true);
 	        	}
 	        	
 	        	return true;
 	        }
 	        if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT)
 	        {
 	        	if(listView_name.hasFocus())
 	        	{
 	        	  listView_name.setFocusable(false);
 	        	  listView_time.setFocusable(true);
 	        	}
 	        	
 	        	return true;
 	        }
 	        if(keyCode == KeyEvent.KEYCODE_DPAD_UP)
 	        {
 	        	if(listView_name.hasFocus())
 	        	{
 	        		if(listView_name.getSelectedItemPosition() == 0)
 	        			listView_name.setSelection(listName.size()-1);
 	        	}
 	        	else if(listView_time.hasFocus())
 	        	{
 	        		if(listView_time.getSelectedItemPosition() == 0)
 	        			listView_time.setSelection(listTime.size()-1);
 	        	}
 	        	return true;
 	        }
 	        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
 	        {
 	        	if(listView_name.hasFocus())
 	        	{
 	        		if(listView_name.getSelectedItemPosition() == listName.size()-1)
 	        			listView_name.setSelection(0);
 	        	}
 	        	else if(listView_time.hasFocus())
 	        	{
 	        		if(listView_time.getSelectedItemPosition() == listTime.size()-1)
 	        			listView_time.setSelection(0);
 	        	}
 	        	return true;
 	        }
 	      //节目简介 binner条 广告 信息键
 			if(keyCode==KeyEvent.KEYCODE_O){
 				if(isMinTV && listView_name.hasFocus())
 				{
 					Map<String, String> mp = (Map<String, String>)listView_name.getItemAtPosition(listView_name.getSelectedItemPosition());   
 	                int eventId=Integer.parseInt(mp.get("eventId"));
 	                int order_ServiceId = Integer.parseInt(mp.get("sidTempt_tag"));
 				    
 	                String full_list_instroduce=JniLoad.GetShortDescriptor(order_ServiceId, eventId);		    		
 	                nvod_introduce.setText(full_list_instroduce);
 	                if(!isNVODIntroduce)
 		            {
 		            isNVODIntroduce=true;
 		            linearLayout_nvod_introduce.setVisibility(View.VISIBLE);
 		            }
 		            handler_nvod_intruduce.removeMessages(msg_nvod_introduce);
 		            handler_nvod_intruduce.sendMessageDelayed(handler_nvod_intruduce.obtainMessage(msg_nvod_introduce), 5000);
 	  		    	
 		            
 				}
 				else if(isFullscreen)
 				{
 					 setInput();
 				}
 				    return true;
 	     	}  
 		 //红色键预约
 	     if(keyCode==KeyEvent.KEYCODE_E){
 	    		
 	    		if(listView_time.hasFocus())
 	    		{
 	    			final ImageView lockImage = (ImageView) listView_time.getSelectedView().findViewById(R.id.order_time_image);
 					lockImage.setImageResource(R.drawable.presence_online);
 	    		}
 	    		
 	    		
 	    	/*	//取选中的频道时间
 	    		if(listView_time.hasFocus())
 	    		{
 	    			if(!hasOrder)
 	    			{
 	    			hasOrder=true;
 	    		    showCountDown();
 	    			}
 	    			else
 	    			{
 	    				hasOrder=false;
 	    				countDown.setText("");
 	    				Toast.makeText(OrderActivity.this, "ԤԼ�㲥��ȡ��", Toast.LENGTH_LONG).show();
 	    			}
 	    		}
 	    		else
 	    		{
 	    			Toast.makeText(OrderActivity.this, "��ѡ��㲥ʱ�䣡", Toast.LENGTH_SHORT).show();
 	    		}*/
 	    		return true;
 			}
 	    	if(keyCode==KeyEvent.KEYCODE_BACK)
 	    	{
 	    		if(isFullscreen)
 	    		{
 	    			isFullscreen=false;
 	    			isMinTV=true;
 	    			linearLayout_main.setVisibility(View.VISIBLE);
 	    			JniPlayer.setWindowSize(20, 50, 260, 180);
 	    		}
 	    		/*else if(isMinTV)
 	    		{
 	    			isMinTV = false;
 	    			linearLayout_main.setVisibility(View.INVISIBLE);
 	    			isFullscreen = true;
 	    			JniPlayer.setWindowSize(0, 0, 0, 0);
 	    		}*/
 	    		return true;
 	    	}
 	    	
 	    	if(keyCode == KeyEvent.KEYCODE_Z)
 	    	{
 	    		
 	    		this.finish();
 	    		return true;
 	    	}
 	    	
 	    	//音乐音量调整
 			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
 				if (bar2current < bar2max) {
 
 					bar2current++;
 					am.setStreamVolume(AudioManager.STREAM_MUSIC, bar2current, 0);
 					bar2.setProgress(bar2current);
 				}
 				return true;
 			}
 			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
 				if (bar2current > 0) {
 					bar2current--;
 					am.setStreamVolume(AudioManager.STREAM_MUSIC, bar2current, 0);
 					bar2.setProgress(bar2current);
 				}
 				return true;
 			}
 	 return super.onKeyDown(keyCode, event);
 
 	 }
 	 
     /**
      * 显示节目简介
      */
     private Handler handler_nvod_intruduce = new Handler()
 	{
 		public void handleMessage(Message msg) {
 			linearLayout_nvod_introduce.setVisibility(View.INVISIBLE);
 			isNVODIntroduce=false;
 			
 		};
 	};
   
   /**
    * 播放节目
    * @param sid
    */
   private void changeChannel(int sid)
   {
 		int freq=0;
 		int sysmbol = 0; 
 		int videoPid=0;
 		int videoType=0;
 		int audioPid=0;
 		int audioType=0;
 		
 		int tsId=0,pmtPid=0,serviceId=0,id=0,freqSeq=0;
         
 		String sql = "ServiceId = ?";
 		Cursor Mycursor = getContentResolver().query(CHANNEL_TB_URI, new String[]{"_id","FreqSeq","TsId","ServiceId","PmtPid", "EleVideoPid","VideoMediaType"}, sql, new String[]{sid+""}, null);
 		if(Mycursor.moveToNext()) {
     		 
 			 id = Mycursor.getInt(Mycursor.getColumnIndex("_id"));
 			 freqSeq = Mycursor.getInt(Mycursor.getColumnIndex("FreqSeq"));
 			 tsId = Mycursor.getInt(Mycursor.getColumnIndex("TsId"));
     		 serviceId=Mycursor.getInt(Mycursor.getColumnIndex("ServiceId"));
     		 pmtPid = Mycursor.getInt(Mycursor.getColumnIndex("PmtPid"));
 			 videoPid=Mycursor.getInt(Mycursor.getColumnIndex("EleVideoPid"));
 			 videoType = Mycursor.getInt(Mycursor.getColumnIndex("VideoMediaType"));
 			 System.out.println("videoPid="+videoPid+"videoType="+videoType+"tsid="+tsId+"serviceId="+serviceId);             
 		}
 		
 
 		Mycursor.close();
 		/*String sql1="select freq from frelist_tb where freqseq=(select FreqSeq from channel_tb where _id=?)";
 		Cursor Mycursor1=db.rawQuery(sql1, new String[]{id+""});*/
 		
 		String sql1 = "freqSeq = ?";
 		Cursor Mycursor1 = getContentResolver().query(FRELIST_TB_URI, new String[]{"freq","sysmbol"}, sql1, new String[]{freqSeq+""}, null);
 		
 		if(Mycursor1.moveToNext())
 		{
 			freq=Mycursor1.getInt(Mycursor1.getColumnIndex("freq"));
 			sysmbol=Mycursor1.getInt(Mycursor1.getColumnIndex("sysmbol"));
 			System.out.println("freq="+freq+"/sysmbol="+sysmbol);
 		}
 		Mycursor1.close();
 		
 		String sql2 = "channel_id = ?";
 		Cursor Mycursor2 = getContentResolver().query(AUDIO_TB_URI, new String[]{"enAudioMediaType","eleAudioPid"}, sql2, new String[]{id+""}, null);
 		
 		if(Mycursor2.moveToNext())
 		{
 			audioPid = Mycursor2.getInt(Mycursor2.getColumnIndex("eleAudioPid"));
 			audioType = Mycursor2.getInt(Mycursor2.getColumnIndex("enAudioMediaType")); 
 			
 			System.out.println("audioPid="+audioPid+"audioType="+audioType);
 			
 		}
 		
 		Mycursor2.close();
 		Cursor Mycursor3 = getContentResolver().query(CA_TB_URI, null, sql2, new String[]{id+""}, null);
 		
 		if(!Mycursor3.moveToFirst())
 		{
 			//如果没有加密后三个参数为0
 			tsId=0;
 			pmtPid=0;
 			serviceId=0;
 		}
 		Mycursor3.close();
 		
 				
 		path="mpeg2ts:///"+freq+"/"+sysmbol+"/"+videoPid+"/"+audioPid+"/"+tsId+"/"+pmtPid+"/"+serviceId+"/";
 		System.out.println("changeChannel() path="+path);
 		JniPlayer.setWindowSize(0, 0, 0, 0);
 		//public static native int changeProgram(int freqency, int modulation, int symboRate, int org_net_id, int net_id, int pmtPid, 
 		//int videoPid, int audioPid, int pcrPid, int serviceId, int tsId);
 		
 //		(int freqency, int modulation, int symboRate, int org_net_id, int net_id, int pmtPid, 
 //				int videoPid, int audioPid, int pcrPid, int serviceId, int tsId)
 		
 		JniPlayer.changeProgram(freq*1000, 0, sysmbol*1000, 0, 0, pmtPid, videoPid, audioPid, 0, serviceId, tsId);
 		//JniPlayer.changeProgram(freq, videoPid, videoType, audioPid, audioType, tsId, pmtPid, serviceId);
 		
 	}
   
   public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k)
   {
       Log.d(TAG, "surfaceChanged called");
      
   }
 
   public void surfaceDestroyed(SurfaceHolder surfaceholder)
   {
       Log.d(TAG, "surfaceDestroyed called");
   }
 
   public void surfaceCreated(SurfaceHolder holder)
   {
       Log.d(TAG, "surfaceCreated called");   
   }
 
   @Override
   protected void onPause()
   {
   	  	
       super.onPause();
       System.out.println("order onpause");
       JniPlayer.stopProgram();
       unregisterReceiver(NvodMsgReceiver);//取消注册
       this.finish();  
   }
  
 }
