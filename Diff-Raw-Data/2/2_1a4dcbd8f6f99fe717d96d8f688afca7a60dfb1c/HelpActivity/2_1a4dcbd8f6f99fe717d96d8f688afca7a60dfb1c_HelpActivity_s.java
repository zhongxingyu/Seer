 package com.test;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.quietlycoding.android.picker.NumberPickerDialog;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class HelpActivity extends Activity {
 	private Button testphp;
 	public static final String SAVED_INFO = "ParqMeInfo";
 	private TextView phpre;
 	public String time;
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
         setContentView(R.layout.myhelp);
         testphp = (Button) findViewById(R.id.testbutton);
         phpre = (TextView) findViewById(R.id.phpresponse);
         
         testphp.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
				Intent myIntent = new Intent(HelpActivity.this, TestMap.class);
				startActivity(myIntent);
 			}
 		});
         
         
         new CountDownTimer(minToMil(120), 1000){
 			@Override
 			public void onFinish() {
 				//unparq
 			}
 
 			@Override
 			public void onTick(long arg0) {
 				int seconds = (int)arg0/1000;
 				phpre.setText(formatMe(seconds));
 				
 			}
         	
         }.start();
 	}
 	public static String formatMe(int seconds){
 		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
 		try {
 			Date x = sdf.parse("00:00:00");
 			x.setSeconds(seconds);
 			return (sdf.format(x));
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "BADBADBAD";
 	}
 	public static long minToMil(int minutes){
     	return minutes*60000;
     }
 	
 
 }
