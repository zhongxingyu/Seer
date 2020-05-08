 package com.ineed.help.a;
 
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.ineed.help.a.R;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.app.Application;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.SyncStateContract.Constants;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TabHost;
 import android.widget.ToggleButton;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.bluetooth.*;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.app.PendingIntent;
 import android.telephony.*;
 import android.location.*;
 import android.location.GpsStatus.Listener;
 import android.telephony.*;
 import android.text.Editable;
 import android.text.method.KeyListener;
 import android.hardware.*;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.util.Log;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.Cursor;
 
 import java.util.Locale;
 
 
 
 public class app_start extends Activity implements OnCheckedChangeListener 
 {
 	  
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		
 		/*
 		Locale locale = new Locale("ru");
 		Locale.setDefault(locale);
 		Configuration config = new Configuration();
 		config.locale = locale;
 		getResources().updateConfiguration(config,null);
 		
 		*/
 		
 		
 
 		
 	}
 	
 	
 /*	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	
 
 		        
 		        
 		        //Configuration sysConfig = getResources().getConfiguration();
 		        // newConfig.locale = Locale.TRADITIONAL_CHINESE;
 	
 		
 		
 	}*/
 	
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
     	  super.onCreate(savedInstanceState);
 
     	  /*
     	   *   	  
     	Locale locale = new Locale("ru");
   		Locale.setDefault(locale);
   		Configuration config = new Configuration();
   		config.locale = locale;
   		getResources().updateConfiguration(config,null);
 
    	  String languageToLoad  = "ru";
     	    Locale locale = new Locale(languageToLoad); 
     	    Locale.setDefault(locale);
     	    Configuration config = new Configuration();
     	    config.locale = locale;
     	    getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
 
 */
 
     	  Resources res =  getResources();
   		Configuration newConfig = new Configuration(res.getConfiguration());
           Locale le_locale =  res.getConfiguration().locale;
           String ll = le_locale.getDisplayName().toLowerCase();
   	     if (ll.contains("ru") ||ll.contains("ru_ru") || ll.contains("russian") || ll.contains("русский"))
   	     {
   	        	newConfig.locale = new Locale("ru");
   	            res.updateConfiguration(newConfig, null);
   	            getBaseContext().getResources().updateConfiguration(newConfig, 
   		        	    getBaseContext().getResources().getDisplayMetrics());
   	        
   	     }
   	    else
   	     {
   	        		 newConfig.locale =  new Locale("en");
   	        	        res.updateConfiguration(newConfig, null);
   	       	        getBaseContext().getResources().updateConfiguration(newConfig, 
   	        	        	    getBaseContext().getResources().getDisplayMetrics());
   	     }
 
   	     
         setContentView(R.layout.tab_main);
 
 
 
 
 
         
         TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
 
 		tabs.setup();
 
 		TabHost.TabSpec spec = tabs.newTabSpec("Сюда вписать телефон лучше 112");
 /** тут могут быть различные звуки голуби мыши птицы кошки, то что не будет как будто вызывать вопросов, 
  * но всё таки сообщит о том, что звонок дошёл или началась запись разговора*/
 		
 		spec.setContent(R.id.tab1);
 		spec.setIndicator(getResources().getString(R.string.tab1_text));
 		tabs.addTab(spec);
 
 		spec = tabs.newTabSpec("Здесь смс на всякий случай кому-то из друзей");
 		spec.setContent(R.id.tab2);
 		spec.setIndicator(getResources().getString(R.string.tab2_text));
 		tabs.addTab(spec);
 
 		spec = tabs.newTabSpec("Здесь можно настроить почтовый ящик на который отправится сообщение, например в вк оно стрельнет на стенку");
 		spec.setContent(R.id.tab3);
 		spec.setIndicator(getResources().getString (R.string.tab3_text));
 		tabs.addTab(spec);
 
 		tabs.setCurrentTab(1);
 		
 		String incomingNumber = new Intent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);
 		
 		if (incomingNumber != null)
 		{
 		
 		EditText et = (EditText) findViewById(R.id.editText1);
 		et.setText(incomingNumber);
 		}
 		ToggleButton toggleHelpButton;
 		
 		toggleHelpButton = (ToggleButton) findViewById(R.id.toggleButton1);
 		
 		toggleHelpButton.setOnCheckedChangeListener(this);
 		
 /*        Button btnContact = (Button) findViewById(R.id.btn_contact);
 		
 		btnContact.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				 List numberList = new ArrayList();
 			        Uri baseUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,contactId);
 			        Uri targetUri = Uri.withAppendedPath(baseUri,Contacts.Data.CONTENT_DIRECTORY);
 			        Cursor cursor = getContentResolver().query(targetUri,
 			                    new String[] {Phone.NUMBER},Data.MIMETYPE+"='"+Phone.CONTENT_ITEM_TYPE+"'",null, null);
 			        startManagingCursor(cursor);
 			        while(cursor.moveToNext()){
 			            numberList.add(cursor.getString(0));
 			        }
 			        cursor.close();
 
 			}
 		});*/
 		
 		
 		Button btnClose = (Button) findViewById(R.id.button3);
       //  btnClose.setText(le_locale.getDisplayName());
         
         
         btnClose.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 			       
 			        	finish();
 			           System.exit(0);
 			           
 				
 			}
 			
 			
 			
 			
 		});
  
        
         
         
         
     }
 
     public boolean loadInitData()
     {    	    	
     	return false;
     }
 	
 /*    public boolean isRunning(Context ctx) {
         ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
         List<RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
 
         for (RunningTaskInfo task : tasks) {
             if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) 
                 return true;                                  
         }
 
         return false;
     }
 */
     
     
     
     
 	@Override
 	public void onCheckedChanged(CompoundButton toggleHelpButton, boolean isChecked) 
 	{
 		final Button bb2 = (Button)findViewById(R.id.button1);
 		Button btnClose = (Button) findViewById(R.id.button3);
 		// TODO Auto-generated method stub
 		bb2.setText(R.string.btn_active);
     
 		if (isChecked)
 		{
 			   String nuText;
 		    	EditText phoneNum = (EditText) findViewById(R.id.editText3);
 		    	nuText = phoneNum.getText().toString();
 		    	
 		    	  
 		    EditText smsTextEdit = (EditText) findViewById(R.id.editText2);
 		    String smsText = smsTextEdit.getText().toString();
 		    	
 		    	
 		    EditText phoneTextEdit = (EditText) findViewById(R.id.editText1);
 	        String dialNum = phoneTextEdit.getText().toString();
 		    
 		   
 			
 			Intent listenButton = new Intent();
 		    listenButton.setClass(this, send_sms.class);
 		    
 		    listenButton.putExtra("smsTXT",smsText);
 		    listenButton.putExtra("smsPhone",nuText);
 		    listenButton.putExtra("callPhone",dialNum);
 		    
 			startActivity(listenButton);
 			
         	
 			bb2.setText(R.string.alrighty);
         	
         	
         	//send_sms ssms = new send_sms();
          //   send_sms ssms = new send_sms();
         //	ssms.dispatchKeyEvent(KeyEvent event);
         	
        // Intent sendS = new Intent();
      
 		//sendS.setClass(this, send_sms.class);
        
         	
 	     //  com.ineed.help.send_sms ssms = new com.ineed.help.send_sms();
 	     //  Class sms_help = ssms.getClass();   
 		
 	        
 	        btnClose.setOnClickListener(new OnClickListener() {
 				
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 				       
 					bb2.setText(R.string.some_joke);
 				        	return;
 				           
 					
 				}
 			});
 			
 			
 		}
 		else
 		{
 			bb2.setText(R.string.bye_word);
 	        
 	        btnClose.setOnClickListener(new OnClickListener() {
 				
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 				       
 				        	finish();
 				           System.exit(0);
 				           
 					
 				}
 			});
 			
 		finishActivity(1); //no
 		
 		return;
 		}
 		
 		
 	//	Timer tick = new Timer()	;		
 	////		tick.scheduleAtFixedRate(new TimerTask()
 	//		{@Override public void run()
 	//		{ bb2.setText("всё оке? давай, не болей кароч!");}}, 0, 2222);
 		//toggleHelpButton.setChecked(false); //создаёт луп? - no
 		
 
 		
 	}
 	 
     public boolean gotKeyDown(int keyCode, KeyEvent event)  
     {  
         //replaces the default 'Back' button action  
         if(keyCode==KeyEvent.KEYCODE_VOLUME_UP)  
         {  
             //do whatever you want the 'Back' button to do  
             //as an example the 'Back' button is set to start a new Activity named 'NewActivity'  
            // this.startActivity(new Intent(YourActivity.this,NewActivity.class));  
         	// String smsText = getSmsText();
         	Button bb1 = (Button)findViewById(R.id.button1);
         	bb1.setText("ЭКСТРА");
         	
         		
         		//sendBroadcast(execSMS);
         		//sendBroadcast(execSMS);
         		//sendBroadcast(execSMS);
         		
         	
         }  
         return true;  
     }
 
 
 
     
 }
