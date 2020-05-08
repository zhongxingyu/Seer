 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 import com.github.groupENIGMA.journalEgocentrique.model.Mood;
 
 public class MoodActivity extends Activity {
 
 	private DB database;
 	private Entry myEntry;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_mood);
 		Intent intent = getIntent();
		long entryId = intent.getLongExtra("EntryId", 0);
 		database = new DB(getApplicationContext());
		database.open();
 		myEntry = database.getEntry(entryId);
 		GridView grid = (GridView)findViewById(R.id.moodGrid);
 		final MoodAdapter imgAdapter = new MoodAdapter(this);
 		grid.setAdapter(imgAdapter);
 		grid.setOnItemClickListener(new OnItemClickListener() {
 	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 	            database.setMood(myEntry, (Mood)imgAdapter.getItem(position));
 	            Intent intent = new Intent(getApplicationContext(), ListActivity.class);
 	            startActivity(intent);
 	        }
 	    });
 	}
 
 }
