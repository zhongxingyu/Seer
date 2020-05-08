 package edu.ucsb.cs.cs185.ramonrovirosa.hw4;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import edu.ucsb.cs.cs185.hw4skeleton.R;
 
 import kankan.wheel.widget.OnWheelChangedListener;
 import kankan.wheel.widget.WheelView;
 import kankan.wheel.widget.adapters.ArrayWheelAdapter;
 import kankan.wheel.widget.adapters.NumericWheelAdapter;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 
 
 public class KeepScoreActivity extends FragmentActivity {
 
 	WheelView wheelDateYear;	
 	WheelView wheelDateMonth;
 
 	
 	WheelView wheelDateDay;	
 	Spinner spinnerTeam1;	
 	Spinner spinnerTeam2;
 	
 	WheelView wheelScore1;
 	
 	WheelView wheelScore2;
 	
 	ArrayList<String> data;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_keep_score);
 		
 		
 		if(getIntent().getStringArrayListExtra("scoreList") != null){
 			//Intent intename = getIntent();
 			data = getIntent().getStringArrayListExtra("scoreList");
 			
 
 		}
 		else{
 			data = new ArrayList<String>();
 			
 		}
 		
 	
 		
 		// This is used to update the maximum number of days in a month
 		OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
 			public void onChanged(WheelView wheel, int oldValue, int newValue) {
 
 				updateDays();
 			}
 		};
 		if (savedInstanceState != null) {
 			Integer wYear = Integer.parseInt(savedInstanceState.getString("WYear"));
 			Integer wMonth = Integer.parseInt(savedInstanceState.getString("WMonth"));
 			Integer wDay = Integer.parseInt(savedInstanceState.getString("WDay"));
 			
 			Integer scr1 = Integer.parseInt(savedInstanceState.getString("score1"));
 			Integer scr2 = Integer.parseInt(savedInstanceState.getString("score2"));
 			
 			wheelDateYear = (WheelView) findViewById(R.id.timePickerYear);
 			wheelDateYear.setViewAdapter(new NumericWheelAdapter(this, 2012,2013));
			wheelDateYear.addChangingListener(wheelListener);		
 			wheelDateYear.setCurrentItem(wYear);
 			
 			wheelDateMonth = (WheelView) findViewById(R.id.timePickerMonth);
 			wheelDateMonth.setViewAdapter(new ArrayWheelAdapter<String>(this,
 					getResources().getStringArray(R.array.months_short)));
 			wheelDateMonth.setCurrentItem(wMonth);
 			wheelDateMonth.addChangingListener(wheelListener);
 			wheelDateMonth.setCyclic(true);
 			
 			wheelDateDay = (WheelView) findViewById(R.id.timePickerDay);
 			wheelDateDay.setViewAdapter(new NumericWheelAdapter(this, 1, 31, "%02d"));
 			wheelDateDay.setCurrentItem(wDay);
 			wheelDateDay.setCyclic(true);
 			
 			spinnerTeam1 = (Spinner) findViewById(R.id.team1);
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 			        R.array.teams, android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinnerTeam1.setAdapter(adapter);
 			spinnerTeam1.setSelection(adapter.getPosition(savedInstanceState.getString("t1")));
 			
 			spinnerTeam2 = (Spinner) findViewById(R.id.team2);
 			ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
 			        R.array.teams, android.R.layout.simple_spinner_item);
 			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinnerTeam2.setAdapter(adapter2);
 			spinnerTeam1.setSelection(adapter.getPosition(savedInstanceState.getString("t2")));
 			
 			wheelScore1 = (WheelView) findViewById(R.id.score1);
 			wheelScore1.setViewAdapter(new NumericWheelAdapter(this, 0, 499, "%03d"));
 			wheelScore1.setCurrentItem(scr1);
 
 			wheelScore2 = (WheelView) findViewById(R.id.score2);
 			wheelScore2.setViewAdapter(new NumericWheelAdapter(this, 0, 499, "%03d"));
 			wheelScore2.setCurrentItem(scr2);
 			
 			updateDays();			
 			
 		}
 		else {
 
 			// Year
 			wheelDateYear = (WheelView) findViewById(R.id.timePickerYear);
 			wheelDateYear.setViewAdapter(new NumericWheelAdapter(this, 2012,
 					2013));
 			wheelDateYear.addChangingListener(wheelListener);
			wheelDateYear.setCurrentItem(0);
 
 			// Month
 			wheelDateMonth = (WheelView) findViewById(R.id.timePickerMonth);
 			wheelDateMonth.setViewAdapter(new ArrayWheelAdapter<String>(this,
 					getResources().getStringArray(R.array.months_short)));
			wheelDateMonth.setCurrentItem(4);
 			wheelDateMonth.addChangingListener(wheelListener);
 			wheelDateMonth.setCyclic(true);
 
 			// Day
 			wheelDateDay = (WheelView) findViewById(R.id.timePickerDay);
 			wheelDateDay.setViewAdapter(new NumericWheelAdapter(this, 1, 31,
 					"%02d"));
 			wheelDateDay.setCurrentItem(16);
 			wheelDateDay.setCyclic(true);
 
 			updateDays();
 
 			// Teams
 			spinnerTeam1 = (Spinner) findViewById(R.id.team1);
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter
 					.createFromResource(this, R.array.teams,
 							android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinnerTeam1.setAdapter(adapter);
 
 			spinnerTeam2 = (Spinner) findViewById(R.id.team2);
 			ArrayAdapter<CharSequence> adapter2 = ArrayAdapter
 					.createFromResource(this, R.array.teams,
 							android.R.layout.simple_spinner_item);
 			adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinnerTeam2.setAdapter(adapter2);
 
 			// Scores
 			wheelScore1 = (WheelView) findViewById(R.id.score1);
 			wheelScore1.setViewAdapter(new NumericWheelAdapter(this, 0, 499,
 					"%03d"));
 			wheelScore1.setCurrentItem(0);
 
 			wheelScore2 = (WheelView) findViewById(R.id.score2);
 			wheelScore2.setViewAdapter(new NumericWheelAdapter(this, 0, 499,
 					"%03d"));
 			wheelScore2.setCurrentItem(0);
 		}
 		final ArrayList<String> data2 = data;
 		Button btnEnter = (Button) findViewById(R.id.enter);
 		btnEnter.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View view){
 				Intent intObj = new Intent(KeepScoreActivity.this, KeepScoreList.class);
 				ArrayList<String> scoreList = stringScoreList(data2);
 				intObj.putExtra("scoreList", scoreList);
 				startActivity(intObj);
 				finish();
 				
 			}
 			
 		});
 		 
 	}
 	
 	public ArrayList<String> stringScoreList(ArrayList<String> scoreList){
 
 		WheelView wheelYear = (WheelView) findViewById(R.id.timePickerYear);
 		Integer yr = (wheelYear.getCurrentItem() == 0) ?  2012 : 2013;
 		String yearString = yr.toString();
 
 		WheelView wheelMonth = (WheelView) findViewById(R.id.timePickerMonth);
 		Integer mnth = wheelMonth.getCurrentItem()+1;
 		String monthString = mnth.toString();
 
 		WheelView wheelday = (WheelView) findViewById(R.id.timePickerDay);
 		Integer day = wheelday.getCurrentItem()+1;
 		String dayString = day.toString();
 
 		Spinner spinnerTeam1 = (Spinner) findViewById(R.id.team1);
 		String t1String = spinnerTeam1.getSelectedItem().toString();
 
 		WheelView Score1 = (WheelView) findViewById(R.id.score1);
 		Integer scr1 = Score1.getCurrentItem();
 		String score1 = scr1.toString();
 
 		WheelView Score2 = (WheelView) findViewById(R.id.score2);
 		Integer scr2 = Score2.getCurrentItem();
 		String score2 = scr2.toString();
 
 		Spinner spinnerTeam2 = (Spinner) findViewById(R.id.team2);
 		String t2String = spinnerTeam2.getSelectedItem().toString();
 		
 		String scoreResult = monthString + "/" + dayString + " -- " + t1String+ " (" + score1+ ") vs. "+ t2String+" ("+score2+")"; 
 		scoreList.add(scoreResult);
 		
 		
 //		scoreList.add(dayString);
 //		scoreList.add(monthString);
 //		scoreList.add(t1String);
 //		scoreList.add(score1);
 //		scoreList.add(t2String);
 //		scoreList.add(score2);
 //		scoreList.add(yearString);
 		
 		return scoreList;
 
 		
 	}
 	
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) 
 	{
 	  
 	  WheelView wheelYear = (WheelView)findViewById(R.id.timePickerYear);
 	  Integer yr = wheelYear.getCurrentItem();
 	  String yearString = yr.toString();
 	  
 	  WheelView wheelMonth = (WheelView)findViewById(R.id.timePickerMonth);
 	  Integer mnth = wheelMonth.getCurrentItem();
 	  String monthString = mnth.toString();
 	  
 	  WheelView wheelday = (WheelView)findViewById(R.id.timePickerDay);
 	  Integer day = wheelday.getCurrentItem();
 	  String dayString = day.toString();
 	  
 	  Spinner spinnerTeam1 = (Spinner) findViewById(R.id.team1);
 	  String t1String = spinnerTeam1.getSelectedItem().toString();
 	  
 	  WheelView Score1 = (WheelView)findViewById(R.id.score1);
 	  Integer scr1 = Score1.getCurrentItem();
 	  String score1 = scr1.toString();
 	  
 	  WheelView Score2 = (WheelView)findViewById(R.id.score2);
 	  Integer scr2 = Score2.getCurrentItem();
 	  String score2 = scr2.toString();
 	  
 	  Spinner spinnerTeam2 = (Spinner) findViewById(R.id.team2);
 	  String t2String = spinnerTeam2.getSelectedItem().toString();
 	  
 	  savedInstanceState.putString("WYear", yearString);
 	  savedInstanceState.putString("WMonth", monthString);
 	  savedInstanceState.putString("WDay", dayString);
 	  
 	  savedInstanceState.putString("t1", t1String);
 	  savedInstanceState.putString("score1", score1);
 	  
 	  savedInstanceState.putString("t2", t2String);
 	  savedInstanceState.putString("score2", score2);
 	  
 	  
 	    
 	  super.onSaveInstanceState(savedInstanceState);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_keep_score, menu);
 		ActionBar actionBar = getActionBar();
 		actionBar.setTitle(R.string.EnterGame);
 		return true;
 	}
 	
 	
 	public void onSet(View v)
 	{
 	    wheelDateYear.setCurrentItem(0);
 	    wheelDateMonth.setCurrentItem(3);
 	    wheelDateDay.setCurrentItem(16);
 	    spinnerTeam1.setSelection(0);
 	    spinnerTeam2.setSelection(0);
 	    wheelScore1.setCurrentItem(0);
 	    wheelScore2.setCurrentItem(0);
 	}
 	
 	void updateDays() {
 		Calendar calendar = Calendar.getInstance();
 
 		// Get maximum number of days for the currently selected month
 		// and update the wheelDateDay view by setting its view adapter.
 		// hint:
 		// http://developer.android.com/reference/java/util/Calendar.html#getActualMaximum(int)
 		int year = wheelDateYear.getCurrentItem();
 		int month = wheelDateMonth.getCurrentItem();
 		int day = wheelDateDay.getCurrentItem();
 		calendar.set(year, month, day);
 		int max = calendar.getActualMaximum(Calendar.DATE);
 		wheelDateDay.setViewAdapter(new NumericWheelAdapter(this, 1, max,"%02d"));
 		
 		
 		
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		super.onOptionsItemSelected(item);
 		switch(item.getItemId()){
 		case R.id.Home:
 			homeMenuItem();
 			break;
 		case R.id.Settings:
 			settingMenuItem();
 			break;
 		case R.id.Help:
 			helpMenuItem();
 			break;
 		
 		}
 		
 		return true;
 	}
 	
 	public void homeMenuItem(){
 		Intent intObj = new Intent(KeepScoreActivity.this, KeepScoreList.class);
 		intObj.putExtra("scoreList", data);
 		startActivity(intObj);
 		finish();
 		
 	}
 	
 	public void settingMenuItem(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCancelable(true);
 		//builder.setIcon(R.drawable.dialog_question);
 		builder.setTitle(R.string.settings);
 
 		TextView tv = new TextView(this);
 		tv.setText(R.string.settingsMessage);
 		tv.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv.setPadding(0, 25 , 0, 0);
 		builder.setView(tv);
 		
 		
 		
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	
 	public void helpMenuItem(){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setCancelable(true);
 		//builder.setIcon(R.drawable.dialog_question);
 		builder.setTitle(R.string.settings);
 
 		
 		TextView tv = new TextView(this);
 		tv.setText(R.string.helpMessage);
 		tv.setGravity(Gravity.CENTER_HORIZONTAL);
 		tv.setPadding(0, 15 , 0, 0);
 		//builder.setView(tv);
 		
 		
 		
 		AlertDialog alert = builder.create();
 		alert.setView(tv);
 		alert.show();
 	}
 	
 	
 }
 
