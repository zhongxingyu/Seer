 package com.example.wsn02;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.androidplot.series.XYSeries;
 import com.androidplot.xy.LineAndPointFormatter;
 import com.androidplot.xy.SimpleXYSeries;
 import com.androidplot.xy.XYPlot;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	public static DataProvider DB;
 	public static String TAG = "com.example.wsn02";
 	
 	private static TextView tv_msg;
 	private Intent intent;
 	private XYPlot plot_battery;
 	
 	public Handler handler = new Handler(){
 		@Override
 		public void handleMessage( Message msg ){
 			Log.i(TAG, "MainActivity::handleMessage()");
 			// get data from msg
 			
 			String result = msg.getData().getString( "result" );
 			
 // TODO rm
 			// display received debugging value
 			tv_msg.setText( result );
 			
 			// debugging
 			Log.d(TAG, "XXX " + result );
 			
 			super.handleMessage( msg );
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.i(TAG, "MainActivity::onCreate()");
 		setContentView(R.layout.activity_main);
 
 		// init
 		tv_msg = (TextView) findViewById( R.id.tv_msg );
 		intent = new Intent( this, HiddenService.class );
 		
 		DB = new DataProvider(this);
 		DB.onCreate();
 		
 		intent = new Intent( this, HiddenService.class );
 		intent.putExtra("handler", new Messenger( this.handler ) );
 		this.startService( intent );
 	}
 
 	public void buttonClick(View v){
 		Log.d(TAG, "MainActivity::buttonClick()");
 		XYPlot plot = (XYPlot) findViewById( R.id.plot_battery );
 		
 		/////
 //		this.DB.batterySave(100);
 		/////
 //*
 		List<Number> xseries = new ArrayList<Number>();
 		List<Number> yseries = new ArrayList<Number>();
 		List<DataElement> dataelements = null;
 		dataelements = this.DB.batteryData();
 		
 		for( int idx=0; idx<dataelements.size(); ++idx){
 			Log.d(MainActivity.TAG, "AAA " + dataelements.get(idx).getTimestamp());
 			xseries.add( dataelements.get(idx).getTimestamp() );
 			yseries.add( dataelements.get(idx).getValue() );
 		}
 		String title = "Battery Usage";
 		this.drawGraph( plot, xseries, yseries, title);
 /*/
 	// FIXME cleaning up
 		List<DataElement> dataelements = this.DB.batteryData();
 		for( int idx=0; idx<dataelements.size(); ++idx){
 			this.DB.batteryDeleteId( dataelements.get(idx).getID() );
 		}
 		dataelements = this.DB.batteryData();
 		for( int idx=0; idx<dataelements.size(); ++idx){
 			Log.d(MainActivity.TAG, "AAA " + dataelements.get(idx).getTimestamp());
 		}
 //*/
 	}
 
 // TODO deprecation - are there other possibilities?
 	@SuppressWarnings("deprecation")
 	public void drawGraph( XYPlot plot
 			, List< Number > xseries
 			, List< Number > yseries
 			, String title ){
 		Log.d(TAG, "MainActivity::drawGraph()");
 		
 		// set up data series
 		XYSeries series = new SimpleXYSeries(
 				xseries,
 				yseries,
 				title );
 			
 		
 		// create a formatter
 // TODO solve deprecated waring
 		LineAndPointFormatter format = new LineAndPointFormatter(
 				Color.rgb(0, 200, 0),               // line color
 				Color.rgb(0, 100, 0),               // point color
 				Color.rgb(150, 190, 150));          // fill color (optional)
 
 		// add series1 and series2 to the XYPlot
 		plot.addSeries( series, format );
 
 		
 		// Reduce the number of range labels
 	    plot.setTicksPerRangeLabel(3);
 	    
 	    // By default, AndroidPlot displays developer guides to aid in laying out your plot.
 	    // To get rid of them call disableAllMarkup():
 // TODO solve deprecated waring
 	    plot.disableAllMarkup();
 	      
 	    plot.getBackgroundPaint().setAlpha(0);
 	    plot.getGraphWidget().getBackgroundPaint().setAlpha(0);
 	    plot.getGraphWidget().getGridBackgroundPaint().setAlpha(0); 
 	}
 
 	
 	public static void setMsg( String msg ){
 		tv_msg.setText( msg );
 	}
 }
