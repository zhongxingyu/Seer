 package com.wobuddy.activity;
 
 import com.wobuddy.R;
 import com.wobuddy.R.layout;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class FrontListActivity extends ListActivity {
 	public final static String URL = "url";
 	public final static String[] ITEMS = {
 		"Tutorial",
 		"Notes",
 		"Broadcast"
 	};
 	private String url;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	  super.onCreate(savedInstanceState);
 
 	  url = getIntent().getStringExtra(URL);
 	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ITEMS));
 
 	  ListView lv = getListView();
 	  lv.setTextFilterEnabled(true);
 
 	  lv.setOnItemClickListener(new OnItemClickListener() {
 	    public void onItemClick(AdapterView<?> parent, View view,
 	        int position, long id) {
 	    	// first is tutorial
 	    	if (position == 0) {
 	    		showVideo();
 	    	}
 	    	// second is notes
 	    	else if (position == 1) {
	    		AlertDialog ad = new AlertDialog.Builder(view.getContext()).create();
 	    		ad.setTitle("Last statistic");
 	    		ad.setMessage("Ran 100 feet at 3 mph");
 	    		ad.show();
 	    	}
 	    	// third is broadcast
 	    	else {
 	    		Toast.makeText(getApplicationContext(), "spammed to facebook",
 	    				Toast.LENGTH_SHORT).show();
 	    	}
 	    }
 	  });
 
 	}
 	
 	public void showVideo() {
 		Intent i = new Intent(Intent.ACTION_VIEW,
 				  Uri.parse(url));
 		startActivity(i);
 	}
 	
 }
