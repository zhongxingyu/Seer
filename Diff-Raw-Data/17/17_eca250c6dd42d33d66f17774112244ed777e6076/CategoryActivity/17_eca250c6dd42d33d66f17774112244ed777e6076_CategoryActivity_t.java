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
 		
		String categories[] = new String[20];
		int i = 0;
		for ( HashMap<String, Object> h : catsArray ) {
			categories[i] = h.keySet().toString().substring(1, h.keySet().toString().length()-1);
			i++;
		}
 		
		TextView cat = (TextView) findViewById(R.id.cats);
		String allofthem = "";
		for ( String s : categories ) {
			allofthem += s;
			allofthem +="\n";
		}
		cat.setText(allofthem);
 	}
 	
 }
