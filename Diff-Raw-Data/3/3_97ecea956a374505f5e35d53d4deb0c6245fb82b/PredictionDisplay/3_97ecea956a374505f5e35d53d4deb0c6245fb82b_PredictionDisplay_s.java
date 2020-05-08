 package com.vishnurajeevan.android;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class PredictionDisplay extends Activity{
 	private static final String TAG = PredictionDisplay.class.getSimpleName();
 	private String stop;
 	private ArrayList<String> predicitons;
 	private TextView first,second,third;
 	
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.prediction_display);
 		
 		predicitons = new ArrayList<String>();
 		
 		first = (TextView)findViewById(R.id.txtFirstPrediction);
 		second = (TextView)findViewById(R.id.txtSecondPrediction);
 		third = (TextView)findViewById(R.id.txtThirdPrediciton);
 		
 		Intent intent = getIntent();
 		stop = intent.getStringExtra("STOP");
 		Log.v(TAG,"before cleansing: "+stop);
		stop = stop.substring(0,6)+"Prediction"+stop.substring(20);
 		stop = stop.replaceAll("&amp;","&");
 		Log.v(TAG,stop);
 		Document predicitonDisplay;
 		
 		try {
 			predicitonDisplay = Jsoup.connect("http://nextbus.com/predictor/"+stop).get();
 			
 			String title = predicitonDisplay.title();
 			
 			Log.v(TAG,title);
 			
 			Element table = predicitonDisplay.select("table[cellspacing=0]").first();
 			String tableText = table.toString();
 			Log.v(TAG,tableText);
 			
 			Iterator<Element> ite = table.select("div.right").iterator();
 			
 			while(ite.hasNext()){
 				Element currentElement = ite.next();
 				predicitons.add(currentElement.text());
 				Log.v(TAG,currentElement.text());
 			}
 			
 //			Iterator<Element> ite2 = table.select("")
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		first.setText(predicitons.get(0) + " Minutes");
 		second.setText(predicitons.get(1)+ " Minutes");
 		third.setText(predicitons.get(2)+ " Minutes");
 		
 		
 	}
 }
