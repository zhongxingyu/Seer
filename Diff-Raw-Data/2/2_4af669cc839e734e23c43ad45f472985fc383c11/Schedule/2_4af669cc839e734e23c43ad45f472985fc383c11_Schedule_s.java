 package com.lteixeira.guiatv;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class Schedule extends Activity implements OnItemClickListener,OnClickListener{
 	//Debug Tag
 	public final static String TAG = "Schedule";
 	
 	//Request Constants
 	public final static String STARTHOUR = "06:00:00";
 	public final static String ENDHOUR = "05:00:00";
 	public final static String SIGLA = "channelSigla";
 	public final static String START_TIME = "startDate";
 	public final static String END_TIME = "endDate";
 	public final static long MILISECONDS_IN_DAY = 86400000;
 	
 	private Time day;
 	private ListView list;
 	private TextView date;
 	private Button previousDay;
 	private Button nextDaY;
 	private Channel channel;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.schedule_layout);
 		initVariables();
 		writeDay(day);
 		channel = ((GuiaTvApp)getApplicationContext()).getChoice();
 		Channel last = ((GuiaTvApp)getApplicationContext()).getLast();
 		setTitle(channel.getName());
 		if(channel != last || ((GuiaTvApp)getApplicationContext()).getShows() == null){
 			makeUrlRequest(day);
 		}else{
 			list.setAdapter(new ScheduleAdapter(this, R.layout.list_row, ((GuiaTvApp)getApplicationContext()).getShows()));
 		}
 		Log.d(TAG, channel.toString());
 	}
 
 	public ListView getList(){
 		return list;
 	}
 	
 	public void setList(ListView list){
 		this.list = list;
 	}
 	
 	private void makeUrlRequest(Time day){
 		try{
 			URL url = prepareUrl(channel, day);
 			Log.d(TAG, url.toString());
 			RequestScheduleTask task = new RequestScheduleTask(this);
 			((GuiaTvApp)getApplicationContext()).setLast(channel);
 			task.execute(url);
 		}catch(MalformedURLException ex){
 			Log.d(TAG,"MalformedURLException");
 		}
 	}
 	
 	private void initVariables(){
 		list = (ListView) findViewById(R.id.scheduleList);
 		list.setOnItemClickListener(this);
 		date = (TextView) findViewById(R.id.dayTextView);
 		previousDay = (Button) findViewById(R.id.previousDayButton);
 		previousDay.setOnClickListener(this);
 		nextDaY = (Button) findViewById(R.id.nextDayButton);
 		nextDaY.setOnClickListener(this);
 		day = ((GuiaTvApp)getApplication()).getDay();
 		if(day == null){
 			day = new Time();
 			day.setToNow();
 			if(day.hour < 6)
 				day.set(day.toMillis(true) - MILISECONDS_IN_DAY);
 			((GuiaTvApp)getApplication()).setDay(day);
 		}
 	}
 	
 	private void writeDay(Time day){
 		date.setText(String.format("%02d-%02d-%02d", day.monthDay,day.month+1,day.year));
 	}
 	
 	private URL prepareUrl(Channel channel, Time date) throws MalformedURLException{
 		Time endTime = new Time();
 		String[] startDate = {Integer.toString(date.year),Integer.toString(date.month+1),Integer.toString(date.monthDay)};
 		endTime.set(date.toMillis(true) + MILISECONDS_IN_DAY);
 		String[] endDate = {Integer.toString(endTime.year),Integer.toString(endTime.month+1),Integer.toString(endTime.monthDay)};
 		
 		StringBuffer request = new StringBuffer();
 		
 		request.append("http://services.sapo.pt/EPG/GetChannelByDateInterval?");
		request.append(SIGLA + "=" + channel.getSigla());
 		request.append("&" + START_TIME + "=" + startDate[0] + "-" + startDate[1] + "-" + startDate[2] + "%20" + STARTHOUR);
 		request.append("&" + END_TIME + "=" + endDate[0] + "-" + endDate[1] + "-" + endDate[2] + "%20" + ENDHOUR);
 		return new URL(request.toString());
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		Intent showIntent = new Intent(this,ShowActivity.class);
 		Show show = (Show) arg0.getItemAtPosition(arg2);
 		((GuiaTvApp)getApplication()).setShow(show);
 		startActivity(showIntent);
 		Log.d(TAG, show.toString());
 	}
 
 	@Override
 	public void onClick(View v) {
 		if(v == previousDay)
 			day.set(day.toMillis(true)-MILISECONDS_IN_DAY);
 		else
 			day.set(day.toMillis(true)+MILISECONDS_IN_DAY);
 		writeDay(day);
 		((GuiaTvApp)getApplication()).setDay(day);
 		makeUrlRequest(day);
 	}
 }
