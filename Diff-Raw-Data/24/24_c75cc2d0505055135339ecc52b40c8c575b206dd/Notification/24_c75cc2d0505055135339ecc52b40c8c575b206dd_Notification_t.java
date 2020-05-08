 package com.acadplnr;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class Notification extends Activity{
 	TextView HttpStuff;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.notification);
 		HttpStuff = (TextView) findViewById(R.id.tvHttp);
 		String j ="http://www.iitk.ac.in";
 		GetMethodEx test = new GetMethodEx(j);
 		String returned;
 		
		try{
 			returned = test.getInternetData();
 			HttpStuff.setText(returned);
 		}catch(Exception e){
 			e.printStackTrace();
 			String error = e.toString();
 			Dialog h = new Dialog(this);
 			h.setTitle(" :(");
 			TextView tv1 = new TextView(this);
 			tv1.setText(error);
 			h.setContentView(tv1);
 			h.show();
		}
	}
 		
}  

