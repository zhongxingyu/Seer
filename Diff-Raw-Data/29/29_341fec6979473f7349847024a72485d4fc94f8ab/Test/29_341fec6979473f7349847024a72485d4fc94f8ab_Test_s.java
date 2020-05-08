 package com.mvanveggel.gordijnapp;
 
 import android.app.Activity;
 import android.database.SQLException;
 import android.os.Bundle;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Test extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.test);
 		TextView tv = (TextView) findViewById(R.id.tvTest);
 		String data = "";
 		
 		DataManager info = new DataManager(this);
 		try {
 			info.open();
 			data = info.getAll();
 			info.close();
 
 		} catch (SQLException e) {
 			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
 		}
 		tv.setText(data);
 
 	}
 }
