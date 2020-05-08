 package com.MSSE.MobileDVR;
 
 import java.util.Date;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.LinearLayout;
 
 public class ChannelGuide extends Activity
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(getContentView());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.channel_guide, menu);
 		return true;
 	}
 	
 	android.view.View getContentView()
 	{
 		LinearLayout result = (LinearLayout) findViewById(R.layout.activity_channel_guide);
 		
		result.removeAllViews();
		
		ListingSource source = getListingSource();
		
		Channel[] channels = source.getChannels();
		Date now = new Date();
		int minutes = 24*60;
		ShowTimeSlot[] timeSlots = source.getTimeSlots(now, minutes);
		
 		return result;
 	}
 	
 	private ListingSource getListingSource()
 	{
 		return new DummyListingSource();
 	}
 
 }
