 package com.google.corrigan.owen.wordformed;
 
 import java.util.List;
import java.util.Random;

 import android.app.ListActivity;
 import android.os.Bundle;
 import android.util.Log;
import android.view.View;
 import android.widget.ArrayAdapter;
 
 public class HiScoreActivity extends ListActivity {
 	private HiScoreDataSource datasource;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.hiscore);
 
 		datasource = new HiScoreDataSource(this);
 		datasource.open();
 
 		Log.d("HISCORES", "In HiScoreActivity, reading all scores");
 		List<HiScore> values = datasource.getAllHiScores();
 
 		// Use the SimpleCursorAdapter to show the
 		// elements in a ListView
 		ArrayAdapter<HiScore> adapter = new ArrayAdapter<HiScore>(this,
 				android.R.layout.simple_list_item_1, values);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	protected void onResume() {
 		datasource.open();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		datasource.close();
 		super.onPause();
 	}
 
 }
