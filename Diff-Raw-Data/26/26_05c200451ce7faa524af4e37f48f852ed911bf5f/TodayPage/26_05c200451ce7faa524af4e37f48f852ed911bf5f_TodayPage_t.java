 package com.example.test1;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.TimeZone;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.ImageButton;
 import android.widget.TextView;
 
 
 public class TodayPage extends Activity
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.todaypagelayout);
 		
 		Button MorningSet = (Button) findViewById(R.id.button1);
 		Button MorningChange = (Button) findViewById(R.id.button4);
 		
 		Button AfternoonSet = (Button) findViewById(R.id.button2);
 		Button AfternoonChange = (Button) findViewById(R.id.button5);
 		
 		Button EveningSet = (Button) findViewById(R.id.button3);
 		Button EveningChange = (Button) findViewById(R.id.button6);
 		
 		ArrayList<Button> buttonsToFeelingSelect = new ArrayList<Button>();
 		buttonsToFeelingSelect.add(MorningSet);
 		buttonsToFeelingSelect.add(MorningChange);
 		buttonsToFeelingSelect.add(AfternoonSet);
 		buttonsToFeelingSelect.add(AfternoonChange);
 		buttonsToFeelingSelect.add(EveningSet);
 		buttonsToFeelingSelect.add(EveningChange);
 		
 		OnClickListener toFeelingSelect = new OnClickListener(){
 			public void onClick(View v)
 			{
 				Intent nextIntent = new Intent(TodayPage.this, Set_Emotions.class);
 				TodayPage.this.startActivity(nextIntent);
 			}
 		};
 		
 		for (Button b : buttonsToFeelingSelect)
 		{ b.setOnClickListener(toFeelingSelect); }
 			
 		SimpleDateFormat date = new SimpleDateFormat("E - mm-dd", Locale.US);
 		date.setTimeZone(TimeZone.getTimeZone("PST"));
 		TextView dateText = new TextView(this);
 		dateText.setGravity(Gravity.CENTER);
 		dateText.setText("Today - " + date);
 		setContentView(dateText);		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.today_page, menu);
 		return true;
 	}
 	
 	protected void onStart()
 	{
 		super.onStart();
 	}
 	
 	
 	protected void onRestart()
 	{
 		super.onRestart();
 	}
 	
 	protected void onStop()
 	{
 		super.onStop();
 	}
	
	protected void onPause()//save data!
	{
		super.onResume();
	}
	
	protected void onResume()//load what changed
 	{
 		super.onResume();
 	}
 	
 	protected void onDestroy()
 	{
 		super.onDestroy();
 	}
	
	protected void reply_click(ImageButton b)
	{
	     Set_Emotions.ImageButtonToChange = b;
	}
 
 }
