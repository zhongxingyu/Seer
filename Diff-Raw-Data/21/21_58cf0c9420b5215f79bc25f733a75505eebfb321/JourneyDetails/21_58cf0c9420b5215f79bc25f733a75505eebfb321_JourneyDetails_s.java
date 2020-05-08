 /*
  * Project: Karpool
  * Package: com.saventech.karpool
  * File: JourneyDetails.java
  * Date: Mar 25, 2011
  */
 
 package com.saventech.karpool;
 
 import java.util.ArrayList;
 
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TabHost;
 
 public class JourneyDetails extends TabActivity {
 	
 	private SharedPreferences mPreferences; 
 	Session session;
 	String Regusername="";
 	String Regpwd="";
 	public static ArrayList<String> ridelist1 = new ArrayList<String>();
 
 	public void onCreate(Bundle savedInstanceState) {
 		
 		Log.i("JourneyDetails","You are now in Journeydetails table");
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.journeydetails);
 	    Intent newintent=  getIntent();
 	    
 	    
 	    try
 	    {
 	    	Regusername=newintent.getStringExtra("RegisterUsername");
 	   	  
 	 	    Regpwd=newintent.getStringExtra("RegisterPassword");
 	 	 
 	 	    mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
 	 	   
 	 	    if(Regusername.toString().equals("loginid") && Regpwd.toString().equals("loginpwd"))
 	 	    {
 	 	    	session=new Session();
 	 		    Log.i("JourneyDetails", "Session checking");
 	 			if(!session.checkinfo(mPreferences))
 	 			{
 	 				 Log.i("JourneyDetails", "No session");
 	 				Intent intent=new Intent(JourneyDetails.this,Login.class);
 	 				startActivity(intent);
 	 			
 	 			}
 	 	    	
 	 	    }
 	 	    else
 	 	    {
 	 	    	SharedPreferences.Editor editor=mPreferences.edit();
 	 	    	
 	             editor.putString("UserName", Regusername);
 	             
 	             editor.putString("PassWord", Regpwd);
 	            
 	             editor.commit();  
 
 	 		    
 	 	    }
 	    	
 	    }
 	    catch(Exception e)
 	    {
 	    	session=new Session();
  		    Log.i("JourneyDetails", "Session checkingNotifications");
  			if(!session.checkinfo(mPreferences))
  			{
  				 Log.i("JourneyDetails", "No session");
  				Intent intent=new Intent(JourneyDetails.this,Login.class);
  				startActivity(intent);
  			
  			}
 	    }
 	   
 	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
 	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
 	    Intent intent;  // Reusable Intent for each tab
 
 	    // Create an Intent to launch an Activity for the tab (to be reused)
 	    intent = new Intent().setClass(this, DriverJourneyDetails.class);
 
 	    // Initialize a TabSpec for each tab and add it to the TabHost
 	    spec = tabHost.newTabSpec("Driver").setIndicator("DRIVER",res.getDrawable(R.drawable.ic_tab_newroute)).setContent(intent);
 	    tabHost.addTab(spec);
 	    tabHost.setBackgroundResource(R.drawable.radialback);//setBackgroundDrawable(R.drawable.radialback);
 	    // Do the same for the other tabs
 	    intent = new Intent().setClass(this, RiderJourneyDetails.class);
 	    spec = tabHost.newTabSpec("rider").setIndicator("RIDER",res.getDrawable(R.drawable.ic_tab_newroute)).setContent(intent);
 	    tabHost.addTab(spec);
 
 	    tabHost.setCurrentTab(1);
 	}
 	
 	public boolean onKeyDown(int keyCode, KeyEvent event) 
 	{
 		if(keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	/**
 	 * Creating menu options for both rider/driver after login into application
 	 */
 	
 	private static final int ABOUT = Menu.FIRST ;
 	private static final int CONTACT_US = Menu.FIRST+1 ;
 	private static final int INSTRUCTIONS = Menu.FIRST+2 ;
 	private static final int PREFERENCES = Menu.FIRST+3 ;
 	private static final int MORE = Menu.FIRST+4;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		menu.add(0, ABOUT, 0, "ABOUT");	
 		menu.add(0, CONTACT_US, 0, "CONTACT_US");	
 		menu.add(0, INSTRUCTIONS, 0, "INSTRUCTIONS");
 		menu.add(0, PREFERENCES, 0, "PREFERENCES");	
 		menu.add(0, MORE, 0, "MORE");	
 		return true;
 	}
 	
 	/**
 	 *  For the selected items in the option menu of karpool application
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		Log.i("JourneyDetails_onOptionsItemSelected","Menu button selected");
 		switch(item.getItemId())
 		{
 		case ABOUT:
 			return true;
 		case CONTACT_US:
 			return true;
 		case INSTRUCTIONS:
 			return true;
 		case PREFERENCES:
 			preferences();
 			return true;
 		case MORE:
 			more();
 			return true;
 		}
 		return false;
 	}
 	public void more()
 	{
 		Intent in = new Intent(this, More.class);
 		Log.i("JourneyDetails_more", "More menu option has been selected");
 		startActivity(in);
 		
 	}
 	
 	public void preferences()
 	{
 		Log.i("JourneyDetails_preferences","preferences menu option has been selected");
 		Intent in = new Intent(this, Preferences.class);
 		startActivity(in);
 	}
 	
 }
