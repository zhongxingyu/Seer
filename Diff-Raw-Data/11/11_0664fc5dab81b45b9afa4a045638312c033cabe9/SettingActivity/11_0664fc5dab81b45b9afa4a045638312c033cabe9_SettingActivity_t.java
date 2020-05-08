 package lorent.stb.tv;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.content.DialogInterface.OnClickListener;
 
 import com.qd.bean.TunerParam;
 import com.qd.jni.JniLoad;
 import com.qd.tool.DBFactory;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.TabActivity;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.NetworkInfo;
 import android.net.Uri;
 //import android.net.ethernet.EthernetDevInfo;
 //import android.net.ethernet.EthernetManager;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RadioButton;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.TabWidget;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class SettingActivity extends TabActivity implements android.view.View.OnClickListener
 {	
     public  Uri CHANNEL_TB_URI = Uri.parse("content://stb.info/channel_tb");
 	
 	public  Uri LOVE_TB_URI = Uri.parse("content://stb.info/love_tb");
 	
 	public  Uri LOCK_TB_URI = Uri.parse("content://stb.info/lock_tb");
 	
 	public Uri FRELIST_TB_URI = Uri.parse("content://stb.info/frelist_tb");
 	
 	public  Uri AUDIO_TB_URI = Uri.parse("content://stb.info/audio_tb");
 	
 	private Uri CA_TB_URI = Uri.parse("content://stb.info/ca_tb");
 	
 	private  Uri CHANNEL_INFO_TB_URI = Uri.parse("content://stb.info/channel_info_tb");
 	private TabHost mTabHost;
 	private TabWidget tabWidget;
 	private Handler handleTitle=new Handler();
 	private int tab_position = 0;
 	/******************************************************
 	 * search part
 	 *****************************************************/
 	private LinearLayout linearLayout_search_set,linearLayout_search_search;
 	private boolean isSearch_Set=true,isSearch_Search=false,isOutput_set=false;
 	private Button auto_search,handler_search,freq_search;
 	private TextView search_type_txt,setting_time;
 	
 	private ProgressBar bar;
 	private TextView searchResult,freq,freqcount,tvandbc,signal_quality,signal_strength,ber;
 	private boolean isGetTuner=true; 
 	private int barMax;	
 	private String fre="";
 	private String symbol="";
 	private String modulation = "";
 	private EditText main_freq,single_freq,start_freq,end_freq;
 	
 	/******************************************************
 	 * output part
 	 *****************************************************/
 	
 	private LinearLayout linearLayout_output_set;
 	private static final String TV_SET="tv_set";
 	private Spinner spinner_tv_type,spinner_resolution,spinner_aspect_ratio;
 	private String[] tvType ={"PAL","NTSC","SECAM"};
 	private String[] resolution = {"720p","720i","1080p","1080i"};
 	private String[] aspect = {"1080p*720p","1080i*720i","720p*576p","720i*576i"};
 	private EditText edit_transparency,edit_brightness,edit_saturation,edit_chroma;
 	private TextView list_search,output_set,version_info,ui_show,channel_info,default_set;
 	
 	/******************************************************
 	 * restore factory part
 	 *****************************************************/
 	
 	private LinearLayout linearLayout_restore_factory,linearLayout_restore_factory_main,linearLayout_restore_factory_one,linearLayout_restore_factory_two;
 	private boolean isRestore_factory = false,isRestore_factory_main=true,isRestore_factory_one=false,isRestore_factory_two=false;
 	private Button btn_restore_factory,btn_restore_factory_congfirm,btn_restore_factory_cancel;
 	
 	private LinearLayout linearLayout_channel_info;
 	private boolean isChannel_info=false;
 	private TextView service_num_txt,service_name_txt,service_provider_txt,service_id_txt,pcr_pid_txt,aud_pid_txt,vid_pid_txt;
     
 	
     /******************************************************
 	 * ui show part
 	 *****************************************************/
 	
 	private LinearLayout linearLayout_show;
 	private boolean isShow=false;
 	private Spinner spinner_language,spinner_timeOut;
 	private String[] languages = new String[]{};
 	private String[] timeOuts = new String[]{};
 	
 	/******************************************************
 	 * version part
 	 *****************************************************/
 	private LinearLayout linearLayout_version;
 	private boolean isVersion = false;
 	private TextView manufacturers_serial_number_txt,stb_serial_number_txt,hardware_version_txt,
 	software_version_txt,loader_version,ca_version_txt;
 	
     private static final String update_set_ui ="lorent.stb.tv.update.set";
    
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		 tabWidget.setFocusable(true);
 	     tabWidget.getChildAt(0).setFocusable(true);
 		 registerReceiver(updateSetUI,new IntentFilter(update_set_ui));
 		
 	}
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.setting);
      
      registerReceiver(SearchReceiver, new IntentFilter("lorent.stb.tv.dvb"));//注册广播
      languages = getResources().getStringArray(R.array.ui_set_language);
      timeOuts = getResources().getStringArray(R.array.ui_set_time);
      mTabHost =getTabHost();   
      tabWidget=mTabHost.getTabWidget();
      
      mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(getString(R.string.list_search)).setContent(R.id.linearLayout_search));   
      mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator(getString(R.string.output_set)).setContent(R.id.linearLayout_output_set));   
      mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator(getString(R.string.version_info)).setContent(R.id.linearLayout_version_info));   
      mTabHost.addTab(mTabHost.newTabSpec("tab_test4").setIndicator(getString(R.string.ui_show)).setContent(R.id.linearLayout_ui_show));
      mTabHost.addTab(mTabHost.newTabSpec("tab_test5").setIndicator(getString(R.string.channel_info)).setContent(R.id.linearLayout_channel_info));   
      mTabHost.addTab(mTabHost.newTabSpec("tab_test6").setIndicator(getString(R.string.default_set)).setContent(R.id.linearLayout_restore_factory_settings));
      
      
      for(int i=0;i<6;i++)
      {
     	 tabWidget.getChildAt(i).getLayoutParams().height=45;
     	 tabWidget.getChildAt(i).getLayoutParams().width=40;
     	 View view = this.getTabWidget().getChildAt(i);   
     	 ((TextView)view.findViewById(android.R.id.title)).setTextSize(13);
     	 
      }
      mTabHost.setCurrentTab(0);  
     
      mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
 		
 		public void onTabChanged(String tabId) {
 //			tab_position = mTabHost.getCurrentTab();
 			System.out.println("tab_position="+tab_position);
 			// TODO Auto-generated method stub
 			
 			handleTitle.postAtFrontOfQueue(new Runnable(){
 				public void run() {
 					// TODO Auto-generated method stub
 					
 //					mTabHost.setCurrentTab(tab_position);
 //					tabWidget.getChildAt(tab_position).setFocusable(true);
 					
 					tabWidget.focusCurrentTab(tab_position);
 					
 				}});
 		}
 	});
 		
      linearLayout_search_set = (LinearLayout)findViewById(R.id.linearLayout_search_set);
      linearLayout_search_search = (LinearLayout)findViewById(R.id.linearLayout_search_search);
      linearLayout_output_set = (LinearLayout)findViewById(R.id.linearLayout_output_set);
 
      linearLayout_version = (LinearLayout)findViewById(R.id.linearLayout_version_info);
      manufacturers_serial_number_txt = (TextView)findViewById(R.id.manufacturers_serial_number);
      stb_serial_number_txt = (TextView)findViewById(R.id.stb_serial_number);
      hardware_version_txt = (TextView)findViewById(R.id.hardware_version);
      software_version_txt = (TextView)findViewById(R.id.software_version);
      loader_version = (TextView)findViewById(R.id.loader_version);
      ca_version_txt = (TextView)findViewById(R.id.ca_version);
      
      linearLayout_show = (LinearLayout)findViewById(R.id.linearLayout_ui_show);
      spinner_language = (Spinner)findViewById(R.id.language_spinner);
      spinner_language.setOnKeyListener(new onRightLeft());
      spinner_language.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 		public void onItemSelected(AdapterView<?> parent, View view,
 				int position, long id) {
 			// TODO Auto-generated method stub
 			System.out.println("spinner_language ===============position="+position); 
 			if(position==0)
 			  {
 				  updateLanguage(Locale.SIMPLIFIED_CHINESE);
 			  }
 			  else if(position==1)
 			  {
 				  updateLanguage(Locale.ENGLISH);
 			  }
 		}
 
 		public void onNothingSelected(AdapterView<?> parent) {
 			// TODO Auto-generated method stub
 			
 		}});
      spinner_timeOut = (Spinner)findViewById(R.id.timeout_spinner);
      spinner_timeOut.setOnKeyListener(new onRightLeft());
      loadSet_show();
      
      linearLayout_channel_info = (LinearLayout)findViewById(R.id.linearLayout_channel_info);
      service_num_txt = (TextView)findViewById(R.id.service_number);
      service_name_txt = (TextView)findViewById(R.id.service_name);
      service_provider_txt = (TextView)findViewById(R.id.service_provider);
      service_id_txt = (TextView)findViewById(R.id.service_ID);
      pcr_pid_txt = (TextView)findViewById(R.id.pcr_pid);
      aud_pid_txt = (TextView)findViewById(R.id.aud_pid);
      vid_pid_txt = (TextView)findViewById(R.id.vid_pid);
      /*Intent in = getIntent();
      String serviceId = in.getStringExtra("serviceId");*/
     // loadChannelInfo(19+"");
     
     
     
      linearLayout_restore_factory = (LinearLayout)findViewById(R.id.linearLayout_restore_factory_settings);
      linearLayout_restore_factory_main = (LinearLayout)findViewById(R.id.linearLayout_restore_factory_main);
      linearLayout_restore_factory_one = (LinearLayout)findViewById(R.id.linearLayout_restore_factory_one);
      linearLayout_restore_factory_two = (LinearLayout)findViewById(R.id.linearLayout_restore_factory_two);
      btn_restore_factory = (Button)findViewById(R.id.restore_factory_settings);
      btn_restore_factory.setOnKeyListener(new onRightLeft());
      btn_restore_factory.setOnClickListener(new Button.OnClickListener(){
 
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			isRestore_factory_main = false;
 			linearLayout_restore_factory_main.setVisibility(View.INVISIBLE);
 			isRestore_factory_one = true;
 			linearLayout_restore_factory_one.setVisibility(View.VISIBLE);
 		}});
      btn_restore_factory_congfirm = (Button)findViewById(R.id.restore_confirm);
      btn_restore_factory_congfirm.setOnClickListener(new Button.OnClickListener(){
 
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			isRestore_factory_one = false;
 			linearLayout_restore_factory_one.setVisibility(View.INVISIBLE);
 			isRestore_factory_two = true;
 			linearLayout_restore_factory_two.setVisibility(View.VISIBLE);
 			
 			//清除本地数据
 			/*File f1 = new File("data/data/lorent.stb.tv/databases");
 			if(f1.exists())
 			deleteDir(f1);*/
 			clearDBData();
 			
 			File f2 = new File("data/data/lorent.stb.tv/shared_prefs");
 			if(f2.exists())
 			deleteDir(f2);
 			isRestore_factory_two = false;
 			linearLayout_restore_factory_two.setVisibility(View.INVISIBLE);
 			isRestore_factory_main = true;
 			linearLayout_restore_factory_main.setVisibility(View.VISIBLE);
 			
 			//send bradcaset update system ui
 			SettingActivity.this.sendBroadcast(new Intent().setAction(update_set_ui));
 			
 		}});
      btn_restore_factory_cancel = (Button)findViewById(R.id.restore_cancel);
      btn_restore_factory_cancel.setOnClickListener(new Button.OnClickListener(){
 
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			isRestore_factory_one = false;
 			linearLayout_restore_factory_one.setVisibility(View.INVISIBLE);
 			isRestore_factory_main = true;
 			linearLayout_restore_factory_main.setVisibility(View.VISIBLE);
 		}});
      
    
      setting_time = (TextView)findViewById(R.id.setting_time);
      new Thread(new Runnable(){
 
 			public void run(){
 				while(true){
 			      
			        SimpleDateFormat df1 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			 		String sDate=df1.format(new Date());
 			        try {
 			        	   Message msg = new Message();
 					       String[] dates ={sDate};
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
      
      auto_search = (Button)findViewById(R.id.auto_search);
      handler_search = (Button)findViewById(R.id.handler_search);
      freq_search = (Button)findViewById(R.id.freq_search);
      
      auto_search.setOnKeyListener(new onLeft());
      handler_search.setOnKeyListener(new onLeft());
      freq_search.setOnKeyListener(new onLeft());
      
      auto_search.setOnClickListener(this);
      handler_search.setOnClickListener(this);
      freq_search.setOnClickListener(this);
      search_type_txt = (TextView)findViewById(R.id.search_type);
      main_freq = (EditText)findViewById(R.id.main_freq);
      
      single_freq = (EditText)findViewById(R.id.single_freq);
      
      start_freq = (EditText)findViewById(R.id.start_freq);
      
      end_freq = (EditText)findViewById(R.id.end_freq);
      
      edit_transparency = (EditText)findViewById(R.id.transparency);
      edit_brightness = (EditText)findViewById(R.id.brightness);
      edit_saturation = (EditText)findViewById(R.id.saturation);
      edit_chroma = (EditText)findViewById(R.id.chroma);
      spinner_tv_type = (Spinner)findViewById(R.id.tv_type);
      spinner_resolution = (Spinner)findViewById(R.id.resolution);
      spinner_aspect_ratio = (Spinner)findViewById(R.id.aspect_ratio);
      
      spinner_tv_type.setOnKeyListener(new onRightLeft());
      spinner_resolution.setOnKeyListener(new onRightLeft());
      spinner_aspect_ratio.setOnKeyListener(new onRightLeft());
      
      loadSet_output();
      
      
      
      freq = (TextView)findViewById(R.id.searchChannel_freq);
      freqcount = (TextView)findViewById(R.id.searchaChannel_freqCount);
      tvandbc =(TextView)findViewById(R.id.searchaChannel_tv_bc);
      ber = (TextView)findViewById(R.id.searchChannel_ber);
      signal_quality = (TextView)findViewById(R.id.signal_quality);
      signal_strength = (TextView)findViewById(R.id.signal_strength);
      
 	 bar=(ProgressBar)findViewById(R.id.seekbar);
 	 bar.setMax(100);
 	 searchResult=(TextView)findViewById(R.id.showResult);
      
 	 
     }
     
    class onLeft implements View.OnKeyListener
    {
 		public boolean onKey(View v, int keyCode, KeyEvent event)
 		{
 			// TODO Auto-generated method stub
 			if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
 			{
 				return true;
 			}
 			return false;
 		}
 	}
    
     class onRightLeft implements View.OnKeyListener
     {
 
 		public boolean onKey(View v, int keyCode, KeyEvent event)
 		{
 			// TODO Auto-generated method stub
 			if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
 			{
 				return true;
 			}
 			return false;
 		}
 	}
     
     private void clearDBData()
     {
     	ContentResolver c =getContentResolver();
     	c.delete(CHANNEL_TB_URI, null, null);
     	c.delete(AUDIO_TB_URI, null, null);
     	c.delete(CA_TB_URI, null, null);
     	c.delete(CHANNEL_INFO_TB_URI, null, null);
     	c.delete(FRELIST_TB_URI, null, null);
     	c.delete(LOCK_TB_URI, null, null);
     	c.delete(LOVE_TB_URI, null, null);
     }
     
     /**
      * 删除本地文件
      * @param dir
      * @return
      */
     private  boolean deleteDir(File dir)
     {  
 
        if (dir.isDirectory())
        {  
         
              String[] children = dir.list();  
          
              for (int i=0; i<children.length; i++)
              {  
                boolean success = deleteDir(new File(dir, children[i]));  
                if (!success)
                {  
 
                    return false;  
                }  
              }
              System.out.println("directory dir.name="+dir.getName());
      
        }
        else 
        {
     	   return dir.delete();
        }
        
        return dir.delete();  
      } 
     
     
     /**
      * 语言设置
      * @param language
      */
     private void updateLanguage(Locale locale)
     {
 		Log.d("ANDROID_LAB", locale.toString());
 		try
 		{
 			Object objIActMag, objActMagNative;
 			Class clzIActMag = Class.forName("android.app.IActivityManager");
 			Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
 			Method mtdActMagNative$getDefault = clzActMagNative.getDeclaredMethod("getDefault");
 			// IActivityManager iActMag = ActivityManagerNative.getDefault();
 			objIActMag = mtdActMagNative$getDefault.invoke(clzActMagNative);
 			// Configuration config = iActMag.getConfiguration();
 			Method mtdIActMag$getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
 			Configuration config = (Configuration) mtdIActMag$getConfiguration.invoke(objIActMag);
 			config.locale = locale;
 			// iActMag.updateConfiguration(config);
 			// android.permission.CHANGE_CONFIGURATION
 			// onCreate();
 			Class[] clzParams = { Configuration.class };
 			Method mtdIActMag$updateConfiguration = clzIActMag.getDeclaredMethod(
 					"updateConfiguration", clzParams);
 			mtdIActMag$updateConfiguration.invoke(objIActMag, config);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
     /**
      * load Channel info
      */
     private void loadChannelInfo(String ServiceId)
     {
     	
         int id=0;
         int pcrPid=0;
         int vidPid=0;
         int audPid=0;
       
         String sql = "ServiceId = ?";
         Cursor Mycursor = getContentResolver().query(CHANNEL_TB_URI, new String[]{"_id","PcrPID","EleVideoPid"}, sql, new String[]{ServiceId}, null);
 		if(Mycursor.moveToFirst())
 		{
 			id = Mycursor.getInt(Mycursor.getColumnIndex("_id"));	
 			pcrPid = Mycursor.getInt(Mycursor.getColumnIndex("PcrPID"));
 			vidPid = Mycursor.getInt(Mycursor.getColumnIndex("EleVideoPid"));
 			
 		}
 		
 		String sql1 = "channel_id = ?";
 		Cursor Mycursor1 = getContentResolver().query(AUDIO_TB_URI, new String[]{"eleAudioPid"}, sql1, new String[]{id+""}, null);
 		
 		if(Mycursor1.moveToFirst())
 		{
 			audPid = Mycursor1.getInt(Mycursor1.getColumnIndex("eleAudioPid"));
 		}
 		if(Mycursor1!=null)
 		Mycursor1.close();
 		
 		
 		pcr_pid_txt.setText(pcrPid+"");
 		vid_pid_txt.setText(vidPid+"");
 		aud_pid_txt.setText(audPid+"");
     }
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId())
 		{
 		case R.id.auto_search:
 			autoSearch();
 			
 			break;
 		case R.id.handler_search:
 			handleSearch();
 			break;
 		case R.id.freq_search:
 			freqSearch();
 			break;
 		}
 	}
 	/**
 	 * 
 	 * 显示页面时间
 	 */
 	private Handler mHandler = new Handler() {
 
 		public void handleMessage(Message msg) {
 			// 接收Message传过来的本地时间值
 			Bundle b=msg.getData();
 			String[] times=b.getStringArray("setTime");
 			setting_time.setText(times[0]);
 			
         }
 	};
 	//save data
 	private void saveSetData(){
 		SharedPreferences.Editor sharedata = getSharedPreferences(TV_SET, 0).edit();
 	        sharedata.putInt("tvType", spinner_tv_type.getSelectedItemPosition());
 	        sharedata.putInt("resolution", spinner_resolution.getSelectedItemPosition());
 	        sharedata.putInt("aspect", spinner_aspect_ratio.getSelectedItemPosition());
 	        sharedata.putString("transparency", edit_transparency.getText().toString().trim());
 	        sharedata.putString("brightness", edit_brightness.getText().toString().trim());
 	        sharedata.putString("saturation", edit_saturation.getText().toString().trim());
 	        sharedata.putString("chroma", edit_chroma.getText().toString().trim());
 	        
 	        sharedata.putInt("language",spinner_language.getSelectedItemPosition() );
 	        sharedata.putInt("timeOut",spinner_timeOut.getSelectedItemPosition() );
 	        
 	        sharedata.commit();
 	}
 	/**
 	 * 捕获home键
 	 */
 	public void onAttachedToWindow() 
 	{
 		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
 		super.onAttachedToWindow();
 	};
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if(keyCode == KeyEvent.KEYCODE_BACK)
 		{
 		  if(!isSearch_Search)
 		  {
 			/*new Thread(new Runnable(){
 
 				@Override
 				public void run() {
 					// TODO Auto-generated method stub
 					saveSetData();
 					
 				}}).start();*/
 	        this.finish();
 		   }
 	        return true;
 		}
 		if(keyCode == KeyEvent.KEYCODE_Z)
 		{
 			if(!isSearch_Search)
 			{
 			
 				/*new Thread(new Runnable(){
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						saveSetData();
 						
 					}}).start();
 	       */
 	        this.finish();
 			}
 	        return true;
 		}
 		if(keyCode == KeyEvent.KEYCODE_HOME)
 		{
 			if(!isSearch_Search)
 			{
 				/*new Thread(new Runnable(){
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						saveSetData();
 						
 					}}).start();
 	        */
 	        Intent intent = new Intent(Intent.ACTION_MAIN);   
             intent.addCategory(Intent.CATEGORY_HOME);   
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);   
             startActivity(intent);   
             
 	        this.finish();
 			}
 			return true;
 		}
 		
 		if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT)
         {
 		  /*if(isRestore_factory_one)
 		  {
 //			  tabWidget.focusCurrentTab(tab_position);
 			if(btn_restore_factory_congfirm.hasFocus())
 			{
 				
 				btn_restore_factory_congfirm.setFocusable(false);
 				btn_restore_factory_cancel.setFocusable(true);
 			}
 		  }*/
 		 
 		  if(tabWidget.hasFocus() && (!isSearch_Search))
 		  {
 			tab_position++;
 			if(tab_position == 6)
             {
 				tab_position = 0;
         	}
 			mTabHost.setCurrentTab(tab_position);
 			return true;
 		  }
         }
 		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
 		{
 			/*if(isRestore_factory_one)
 			{
 				if(btn_restore_factory_cancel.hasFocus())
 				{
 				   btn_restore_factory_cancel.setFocusable(false);
 				   btn_restore_factory_congfirm.setFocusable(true);
 				}
 			}*/
 		    if(tabWidget.hasFocus() && (!isSearch_Search))
 		    {
 			  tab_position--;
 			  if(tab_position < 0)
 			  {
 				tab_position = 5;
 			  }
 			  mTabHost.setCurrentTab(tab_position);
 		    
 			  return true;
 		    }
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	//焦点改变时更换背景图片
 	/*class MyFocusChangeListener implements OnFocusChangeListener{
 
 		public void onFocusChange(View v, boolean hasFocus) {
 		
 			if(list_search.hasFocus()){
 				output_set.setBackgroundColor(0);
 				version_info.setBackgroundColor(0);
 				channel_info.setBackgroundColor(0);
 				default_set.setBackgroundColor(0);
 				
 				list_search.setBackgroundColor(R.color.period_listview_item);
 			}
 						
 			else if(output_set.hasFocus()){
 				list_search.setBackgroundColor(0);
 				version_info.setBackgroundColor(0);
 				channel_info.setBackgroundColor(0);
 				default_set.setBackgroundColor(0);
 				
 				output_set.setBackgroundColor(R.color.period_listview_item);
 			}
 			else if(version_info.hasFocus()){
 				list_search.setBackgroundColor(0);
 				output_set.setBackgroundColor(0);
 				channel_info.setBackgroundColor(0);
 				default_set.setBackgroundColor(0);
 				
 				version_info.setBackgroundColor(R.color.period_listview_item);
 			}
 			
 			else if(channel_info.hasFocus())
 			{
 				list_search.setBackgroundColor(0);
 				output_set.setBackgroundColor(0);
 				version_info.setBackgroundColor(0);
 				default_set.setBackgroundColor(0);
 				
 				channel_info.setBackgroundColor(R.color.period_listview_item);
 			}
 			
 			else if(default_set.hasFocus())
 			{
 				list_search.setBackgroundColor(0);
 				output_set.setBackgroundColor(0);
 				version_info.setBackgroundColor(0);
 				channel_info.setBackgroundColor(0);
 				
 				default_set.setBackgroundColor(R.color.period_listview_item);
 			}
 	   }
     }
 	*/
     /**
      * load set data
      */
     private void loadSet_output()
     {
     	
 		SharedPreferences sharedata = getSharedPreferences(TV_SET, 0);
 		
 		int tv_type_tempt = sharedata.getInt("tvType", 0);
 		int resolution_tempt = sharedata.getInt("resolution", 0);
         int aspect_tempt = sharedata.getInt("aspect",0);
 		String transparency_tempt = sharedata.getString("transparency", getString(R.string.default_output_transparency));
 		String brightness_tempt = sharedata.getString("brightness", getString(R.string.default_output_brightness));
 		String saturation_tempt = sharedata.getString("saturation", getString(R.string.default_output_saturation));
 		String chroma_tempt = sharedata.getString("chroma", getString(R.string.default_output_chroma));
 		
 		SpinnerAdapter adapter_type = new SpinnerAdapter(this,  
 	            android.R.layout.simple_spinner_item, tvType);  
 		adapter_type.setDropDownViewResource(R.layout.spinner_content);
 	    spinner_tv_type.setAdapter(adapter_type); 
 	    spinner_tv_type.setSelection(tv_type_tempt);
 		
 		SpinnerAdapter adapter_resolution = new SpinnerAdapter(this,  
 	            android.R.layout.simple_spinner_item, resolution);  
 		adapter_resolution.setDropDownViewResource(R.layout.spinner_content);
 	    spinner_resolution.setAdapter(adapter_resolution); 
 	    spinner_resolution.setSelection(resolution_tempt);
 		
 		SpinnerAdapter adapter_aspect = new SpinnerAdapter(this,  
 	            android.R.layout.simple_spinner_item, aspect);  
 		adapter_aspect.setDropDownViewResource(R.layout.spinner_content);
 	    spinner_aspect_ratio.setAdapter(adapter_aspect); 
 	    spinner_aspect_ratio.setSelection(aspect_tempt);
 		
 	    edit_transparency.setText(transparency_tempt);
 	    edit_brightness.setText(brightness_tempt);
 	    edit_saturation.setText(saturation_tempt);
 	    edit_chroma.setText(chroma_tempt);
 		
     }
     /**
      * 界面显示
      */
     private void loadSet_show()
     {
     	
 		SharedPreferences sharedata = getSharedPreferences(TV_SET, 0);
 		
 		int language_tempt = sharedata.getInt("language", 0);
 		int timeOut_tempt = sharedata.getInt("timeOut", 0);
 		
 		
 		SpinnerAdapter adapter_language = new SpinnerAdapter(this,  
 	            android.R.layout.simple_spinner_item, languages);  
 		adapter_language.setDropDownViewResource(R.layout.spinner_content);
 	    spinner_language.setAdapter(adapter_language); 
 	    spinner_language.setSelection(language_tempt);
 		
 		SpinnerAdapter adapter_timeOut = new SpinnerAdapter(this,  
 	            android.R.layout.simple_spinner_item, timeOuts);  
 		adapter_timeOut.setDropDownViewResource(R.layout.spinner_content);
 	    spinner_timeOut.setAdapter(adapter_timeOut); 
 	    spinner_timeOut.setSelection(timeOut_tempt);
 	    
 	   /* if(language_tempt==0)
 		  {
 			  setLanguage(Locale.SIMPLIFIED_CHINESE);
 		  }
 		  else if(language_tempt==1)
 		  {
 			  setLanguage(Locale.ENGLISH);
 
 		  }*/
     }
     /**
      * 
      */
     private void getTunerParam()
     {
     	isGetTuner =true;
     	new Thread(new Runnable()
     	{
 			public void run()
 			{
 				TunerParam tuner=null;
 				while(isGetTuner)
 				{
 					try{
 					tuner=JniLoad.GetTunerAttribute();
 					ber.setText(tuner.getBer()+"");
 					signal_quality.setText(tuner.getSignal_quality()+"");
 					signal_strength.setText(tuner.getSignal_strength()+"");
 					Thread.sleep(500);
 					}catch(Exception e)
 					{
 						continue;
 					}
 				}
 				
 			}}).start();
     }
 	/**
 	 * auto search
 	 */
 	private void autoSearch()
 	{
 		    if(main_freq.getText().toString().equals(""))
 		    	Toast.makeText(SettingActivity.this, "请输入主频点", Toast.LENGTH_SHORT).show();
 		    else
 		    {
 		    bar.setProgress(0);
 		    int freq_tempt=Integer.parseInt(main_freq.getText().toString().trim());
 		    search_type_txt.setText(getString(R.string.auto_search));
 			isSearch_Set = false;
 			linearLayout_search_set.setVisibility(View.INVISIBLE);
 			isSearch_Search = true;
 			linearLayout_search_search.setVisibility(View.VISIBLE);
 			
 			searchResult.setText(getString(R.string.start_search));
 			//初始化
 			JniLoad.Init();
 			System.out.println("freq_tempt="+freq_tempt);
 			//JniLoad.Config(1, Integer.parseInt(fre), 0, Integer.parseInt(symbol), Integer.parseInt(modulation));
 			JniLoad.Config(1, freq_tempt, 0, 6875, 3);
 			
 			JniLoad.Start();
 			getTunerParam();
 		    }
 	}
 	/**
 	 *  handler search
 	 */
 	private void handleSearch()
 	{
 		if(single_freq.getText().toString().equals(""))
 		{
 			showDialog(1);
 		}
 		else
 		{
 			bar.setProgress(0);
 			int freq_tempt=Integer.parseInt(single_freq.getText().toString().trim());
 			search_type_txt.setText(getString(R.string.handler_search));
 			isSearch_Set = false;
 			linearLayout_search_set.setVisibility(View.INVISIBLE);
 			isSearch_Search = true;
 			linearLayout_search_search.setVisibility(View.VISIBLE);
 			
 			searchResult.setText(getString(R.string.start_search));
 			JniLoad.Init();				
 			JniLoad.Config(0, freq_tempt , 0, 6875, 3);
 			JniLoad.Start();
 			getTunerParam();
 		}
 	}
 	
 	/**
 	 * freq  Search
 	 */
 	private void freqSearch()
 	{
 		if(start_freq.getText().toString().equals(""))
 		{
 			showDialog(2);
 		}
 		else if(end_freq.getText().toString().equals(""))
 		{
 			showDialog(3);
 		}
 		else
 		{
 		bar.setProgress(0);
 		int start_freq_tempt=Integer.parseInt(start_freq.getText().toString().trim());
 		int end_freq_tempt = Integer.parseInt(end_freq.getText().toString().trim());
 		search_type_txt.setText(getString(R.string.freq_search));
 		isSearch_Set = false;
 		linearLayout_search_set.setVisibility(View.INVISIBLE);
 		isSearch_Search = true;
 		linearLayout_search_search.setVisibility(View.VISIBLE);
 		
 		searchResult.setText(getString(R.string.start_search));
 		JniLoad.Init();				
 		JniLoad.Config(3, start_freq_tempt , end_freq_tempt, 6875, 3);
 		JniLoad.Start();
 		getTunerParam();
 		}
 		
 	}
 	
 	protected Dialog onCreateDialog(int id)
 	{
     	Dialog dialog = null;
 		
     	if(id==1)
     	{
 			dialog = new AlertDialog.Builder(SettingActivity.this).setTitle("标题")
 		        .setMessage("请输入搜索频点！").setPositiveButton("确定",
 		                new DialogInterface.OnClickListener() {
 		                    public void onClick(DialogInterface dialog,
 		                            int whichButton) {
 		                    	
 		                        setResult(RESULT_OK);//确定按钮事件
 		                      
 		                    }
 		                }).create();
 		}
 		if(id==2)
 		{
 			dialog = new AlertDialog.Builder(SettingActivity.this).setTitle("标题")
 		        .setMessage("请输入开始频点！").setPositiveButton("确定",
 		                new DialogInterface.OnClickListener()
 		        		{
 		                    public void onClick(DialogInterface dialog,
 		                            int whichButton)
 		                    {	
 		                        setResult(RESULT_OK);//确定按钮事件
 		                    }
 		                }).create();
 		}
 		if(id==3)
 		{
 			dialog = new AlertDialog.Builder(SettingActivity.this).setTitle("标题")
 		        .setMessage("请输入结束频点！").setPositiveButton("确定",
 		                new DialogInterface.OnClickListener()
 		        		{
 		                    public void onClick(DialogInterface dialog,
 		                            int whichButton)
 		                    {	
 		                        setResult(RESULT_OK);//确定按钮事件 
 		                    }
 		                }).create();
 		}
 		
 		return dialog;
     }
 	
 	private BroadcastReceiver updateSetUI = new BroadcastReceiver()
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			// TODO Auto-generated method stub
 			spinner_language.setSelection(0);
 			spinner_timeOut.setSelection(0);
 			spinner_aspect_ratio.setSelection(0);
 			spinner_resolution.setSelection(0);
 			spinner_tv_type.setSelection(0);
 			
 		}
 	};
 
 	//广播接收器 接收 预约 和 预录提示信息
     private BroadcastReceiver SearchReceiver = new BroadcastReceiver()
     {
 		@Override
 		public void onReceive(Context arg0, Intent arg1)
 		{
 			// TODO Auto-generated method stub					
 				
 			    Bundle b=arg1.getExtras();
 				for (String type : b.keySet())
 				{
 
 					String[] args = b.getStringArray(type);
 					if(type.equals("20000006"))
 					{
 						freq.setText(args[0]);
 					}
 					else if (type.equals("20000011"))
 					{					
 						//设置当前搜索到的频点数
 						freqcount.setText(barMax+ "/" + args[0]);
 					}
 					else if (type.equals("20000008"))
 					{
 						tvandbc.setText(args[0] + "/" + args[1]);
 					}
 					else if(type.equals("20000014"))
 					{
 						barMax=Integer.parseInt(args[0]);
 						
 					}
 					else if(type.equals("20000012"))
 					{
 						// 设置进度条
 						bar.setProgress(Integer.parseInt(args[0]));
 						
 						if (Integer.parseInt(args[0]) == 100)
 						{
 							searchResult.setText(getString(R.string.search_end).toString());
 						}
 						else
 						{
 							searchResult.setText(getString(R.string.search_ing).toString());
 						}
 					}
 					else if(type.equals("20000009"))
 					{
 						isGetTuner = false; //关闭线程
 						JniLoad.Stop();
 						getContentResolver().delete(CHANNEL_TB_URI, null, null);
 						Toast.makeText(SettingActivity.this, "����ʧ��!", Toast.LENGTH_LONG).show(); 
 						linearLayout_search_search.setVisibility(View.INVISIBLE);
 						isSearch_Search = false;
 						isSearch_Set = true;
 						linearLayout_search_set.setVisibility(View.VISIBLE);
 					}
 					else if(type.equals("20000013"))
 					{
 						isGetTuner =false; //关闭线程
 						JniLoad.Stop();
 						saveSearchData();  
 					}
 				}			
 		}
 		    	
 	};
 	
 	//save data
 	private void saveSearchData()
 	{
 		
 		DBFactory.insertData(SettingActivity.this, CHANNEL_TB_URI, CHANNEL_INFO_TB_URI);
 		searchResult.setText("");
 		linearLayout_search_search.setVisibility(View.INVISIBLE);
 		isSearch_Search = false;
 		isSearch_Set = true;
 		linearLayout_search_set.setVisibility(View.VISIBLE);
 		Toast.makeText(SettingActivity.this, "搜索结束!", Toast.LENGTH_LONG).show();
 		
 	}
 	
 	protected void onPause()
 	{
 		super.onPause();
 		//ֹͣ停止搜索
 		
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				// TODO Auto-generated method stub
 				saveSetData();
 				
 			}
 		}).start();
 		
     	JniLoad.Stop();
     	unregisterReceiver(SearchReceiver);//取消注册
     	unregisterReceiver(updateSetUI);
 	};
 	
 	@Override
 	protected void onStop()
 	{
 		// TODO Auto-generated method stub
 		super.onStop();
 		
 	}
 	
 	private class SpinnerAdapter extends ArrayAdapter<String>
 	{  
 	    Context context;  
 	    String[] items = new String[] {};  
 	  
 	    public SpinnerAdapter(final Context context, final int textViewResourceId, final String[] objects)
 	    {  
 	        super(context, textViewResourceId, objects);  
 	        this.items = objects;  
 	        this.context = context;  
 	    }  
 	  
 	    public View getDropDownView(int position, View convertView, ViewGroup parent)
 	    {  
 	  
 	        if (convertView == null)
 	        {  
 	            LayoutInflater inflater = LayoutInflater.from(context);  
 	            convertView = inflater.inflate(  
 	                    android.R.layout.simple_spinner_item, parent, false);  
 	        }  
 	  
 	        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);  
 	        tv.setText(items[position]);  
 	        tv.setTextColor(Color.BLACK);  
 	        tv.setTextSize(20);  
 	        return convertView;  
 	    }  
 	  
 	    @Override  
 	    public View getView(int position, View convertView, ViewGroup parent)
 	    {  
 	        if (convertView == null)
 	        {  
 	            LayoutInflater inflater = LayoutInflater.from(context);  
 	            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);  
 	        }  
 	  
 	        // android.R.id.text1 is default text view in resource of the android.  
 	        // android.R.layout.simple_spinner_item is default layout in resources of android.  
 	  
 	        TextView tv = (TextView)convertView.findViewById(android.R.id.text1);  
 	        tv.setText(items[position]);  
 	        tv.setTextColor(Color.BLACK);  
 	        tv.setTextSize(9); 
 	        tv.setIncludeFontPadding(false);
 	        return convertView;  
 	    }  
 
       }
 }
 
