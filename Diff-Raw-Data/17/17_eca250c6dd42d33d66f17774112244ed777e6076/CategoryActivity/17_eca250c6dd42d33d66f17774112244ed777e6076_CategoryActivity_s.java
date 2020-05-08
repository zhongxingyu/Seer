 package com.android.aigproject;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class CategoryActivity extends Activity {
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.category);
 
 		String source = "http://app-inventor-gallery.appspot.com/rpc?tag=get_categories";
 		Log.d("CATS","###########################");
 		
 		ArrayList<HashMap<String, Object>> catsArray = new ArrayList<HashMap<String, Object>>();
 		catsArray = AIGProjectActivity.retrieveQueryArray(source);
 		
		TextView cat = (TextView) findViewById(R.id.cats);
		Log.d("CATS","$$$$$$$$$$$$$$$$$$$$$$$$$$$");
 		
		cat.setText("JSON### "+catsArray.toString());
 	}
 	
 }
