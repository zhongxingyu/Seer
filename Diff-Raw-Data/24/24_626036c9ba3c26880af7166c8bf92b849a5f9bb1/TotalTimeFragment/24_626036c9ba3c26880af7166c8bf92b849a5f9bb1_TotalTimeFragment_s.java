 package com.pinokia.considerateapp;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.TextView;
 
 public class TotalTimeFragment extends Fragment {
 
 	// global variables
 	WebView wv;
 	TextView text;
 	
 	static int chartWidth = 500;// = 400;
 	static int chartHeight = 220;// = 240;
 	double max = 10;	
 	
 	Timer dailyTimer = new Timer();
 	long dailyDelay = 60 * 1000; // number of millisec in 1 minute
 	
 	Timer secondTimer = new Timer();
 	long secondDelay = 1000;
 
 	String graphString = "";
 
 	// Power Check
 	double tMinus5_tt = 0;
 	double tMinus4_tt = 0;
 	double tMinus3_tt = 0;
 	double tMinus2_tt = 0;
 	double tMinus1_tt = 0;
 
 	class timerDailyTask extends TimerTask {
 		public void run() {
 
 			// System.out.println("BEFORE: "+ stopwatch.getTotalTime());
 			tMinus5_tt = tMinus4_tt;
 			tMinus4_tt = tMinus3_tt;
 			tMinus3_tt = tMinus2_tt;
 			tMinus2_tt = tMinus1_tt;
 			tMinus1_tt = 0;
 			StatsService.getStopWatch().setTotalTime(0);
 			// System.out.println("AFTER: "+ stopwatch.getTotalTime());
 
 			System.out.println("Total Time day passed");
 		}
 	}
 	
 	class timerSecondTask extends TimerTask {
 		public void run() {
			double timeSpentSeconds = (double) StatsService.getStopWatch().getTotalTime() / 1000.00;
			
			int hours = (int) timeSpentSeconds / (60 * 60);
			int mins = (int) (timeSpentSeconds / (60)) % 60;
			int secs = (int) (timeSpentSeconds) % 60;
			
			// CAN'T SET TEXT FROM DIFFERENT THREAD
			//text.setText("You have been on your phone for\n" + hours + " hours " + mins + " mins and " + secs + " secs today.");
			//wv.loadData(graphString, "text/html", "UTF-8");
 		}
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		View view = inflater.inflate(R.layout.stats_layout, container, false);
 		text = (TextView) view.findViewById(R.id.text);
 		wv = (WebView) view.findViewById(R.id.graph);
 		wv.setBackgroundColor(0);
 		
 		// System.out.println("at oncreate");
 		dailyTimer.schedule(new timerDailyTask(), 0, dailyDelay);
 		secondTimer.schedule (new timerSecondTask(), 0, secondDelay);
 		return view;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		super.onSaveInstanceState(savedInstanceState);
 		setUserVisibleHint(true);
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		System.out.println("OnPause: TotalTime");
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		System.out.println("OnResume: TotalTime");
 
 		long timeSpentMillis = StatsService.getStopWatch().getTotalTime();
 		int hours = (int) timeSpentMillis / (1000 * 60 * 60);
 		int mins = (int) (timeSpentMillis / (1000 * 60)) % 60;
 		int secs = (int) (timeSpentMillis / (1000)) % 60;
 
 		double timeSpentSeconds = (double) StatsService.getStopWatch()
 				.getTotalTime() / 1000.00;
 		if (timeSpentSeconds > max)
 			max = timeSpentSeconds;
 		
 		tMinus1_tt = timeSpentSeconds;		
 		String plotPointsTotalTime = ""
 				+ Double.toString(((tMinus5_tt / max) * 100.00)) + ","
 				+ Double.toString(((tMinus4_tt / max) * 100.00)) + ","
 				+ Double.toString(((tMinus3_tt / max) * 100.00)) + ","
 				+ Double.toString(((tMinus2_tt / max) * 100.00)) + ","
 				+ Double.toString(((tMinus1_tt / max) * 100.00));
 
 		graphString = "<center><img src='http://1.chart.apis.google.com/chart"
 				+ "?chf=bg,s,67676700|c,s,67676700" // transparent background
 				+ "&chxl=0:|3 days ago|2 days ago|1 day ago|yesterday|today" // chart labels
 				+ "&chxr=0,1,5,1|1,0," + max + ",5" // axis range
 				+ "&chxs=0,000000,14,0,lt,000000|1,000000,14,1,l,000000" // chart axis style
 				+ "&chxt=x,y" // chart axis ordering
 				+ "&chs=" + chartWidth + "x" + chartHeight // chart size
 				+ "&cht=lc" // chart type
 				+ "&chco=58D9FC,EE58FC" // line colors
 				+ "&chd=t:" + plotPointsTotalTime // chart data
 				+ "&chls=3' />"; // line style (thickness)
 		
 		System.out.println("TOTALTIME_URL" + graphString);
 		
 		text.setText("You have been on your phone for\n" + hours + " hours " + mins + " mins and " + secs + " secs today.");
 		wv.loadData(graphString, "text/html", "UTF-8");
 	}
 }
