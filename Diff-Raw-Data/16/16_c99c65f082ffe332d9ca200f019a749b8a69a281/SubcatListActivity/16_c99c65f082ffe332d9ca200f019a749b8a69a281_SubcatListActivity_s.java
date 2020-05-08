 package com.qayto.mobile;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class SubcatListActivity extends ListActivity {
 
 	int subcatIndex = 0;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ServerRequestHelper srq = new ServerRequestHelper();
 		Bundle extras = getIntent().getExtras();
 
 		if (extras != null) {
 			subcatIndex = extras.getInt("subcatIndex");
 		}
 		JSONArray jdata = srq.getSubcategories("" + subcatIndex);
 		
 		ArrayList<String> data = new ArrayList<String>();
 		
 		try {
 			for (int i = 0; i<jdata.length(); i++) {
 				data.add(jdata.getString(i));
 			}
 		} catch (JSONException e) {
 			Log.e("JSON error...", e.getMessage());
 		}
 		
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, data));
 		ListView lv = this.getListView();
 		lv.setTextFilterEnabled(true);
 		lv.setBackgroundColor(Color.BLACK);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Intent intent = new Intent();
 				intent.setClass(SubcatListActivity.this, WaitingActivity.class);
 				intent.putExtra("subcatIndex", subcatIndex);
                 startActivity(intent);
 			}
 		});
 	}
 	
 }
