 package com.mitsugaru.Tapdex;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.AnimationSet;
 import android.view.animation.LayoutAnimationController;
 import android.view.animation.TranslateAnimation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView;
 
 public class FormListActivity extends ListActivity {
     String formName = "";
     List<String> entryNames = new ArrayList<String>();;
     private ArrayAdapter<String> adapter;
     private DatabaseHandler database;
     private boolean empty = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.formentrylist);
 	Bundle extras = getIntent().getExtras();
 	if (extras != null) {
 	    formName = extras.getString("formName");
 	    if (formName == null) {
 		Log.e(TapdexActivity.TAG, "Null for form name");
 		this.finish();
 	    }
 	} else {
 	    Log.e(TapdexActivity.TAG, "Null extras");
 	    this.finish();
 	}
 	//Set header
 	TextView header = (TextView) findViewById(R.id.headerText);
 	header.setText(formName);
 	// Database
 	database = DatabaseHandler.getInstance(this);
 	// Animation
 	AnimationSet set = new AnimationSet(true);
 
 	Animation animation = new AlphaAnimation(0.0f, 1.0f);
 	animation.setDuration(50);
 	set.addAnimation(animation);
 
 	animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
 		Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
 		-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
 	animation.setDuration(100);
 	set.addAnimation(animation);
 
 	LayoutAnimationController controller = new LayoutAnimationController(
 		set, 0.5f);
 	ListView listView = getListView();
 	listView.setOnItemClickListener(new OnItemClickListener() {
 
 	    @Override
 	    public void onItemClick(AdapterView<?> arg0, View arg1,
 		    int position, long arg3) {
 		if (!empty) {
 		    // Entry list activity
 		    Intent intent = new Intent(getBaseContext(),
 			    EntryListActivity.class);
 		    intent.putExtra("formName", formName);
 		    intent.putExtra("entryName", entryNames.get(position));
 		    startActivity(intent);
 		}
 	    }
 
 	});
 	listView.setLayoutAnimation(controller);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	// Now, inflate our submenu.
 	MenuInflater inflater = new MenuInflater(this);
 	inflater.inflate(R.menu.formmenu, menu);
 	return true;
     }
 
     @Override
     public void onResume() {
 	// Adapter
 	entryNames = database.getEntryNames(formName);
 	if (entryNames.isEmpty()) {
 	    empty = true;
 	    entryNames.add("No entries available! :(");
 	    entryNames.add("To make a new entry:");
 	    entryNames.add("1. Tap the menu key.");
 	    entryNames.add("2. Tap 'New Entry'");
 	    entryNames
 		    .add("3. Name your new entry and enter data into fields.");
 
 	}
 	adapter = new ArrayAdapter<String>(this,
 		android.R.layout.simple_list_item_1, entryNames);
 	setListAdapter(adapter);
 	super.onResume();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	switch (item.getItemId()) {
 	case R.id.addEntry: {
 	    Intent intent = new Intent(getBaseContext(),
 		    NewEntryActivity.class);
 	    intent.putExtra("formName", formName);
 	    startActivity(intent);
 	    return true;
 	}
 	default: {
 	    break;
 	}
 	}
 	return false;
     }
 }
