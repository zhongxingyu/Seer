 package com.mitsugaru.Tapdex;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.example.android.apis.graphics.CameraPreview;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
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
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class TapdexActivity extends ListActivity {
     // Class variables
     private final Activity activity = this;
     public static final String TAG = "TAPDEX";
     private DatabaseHandler database;
     private List<String> formNames = new ArrayList<String>();
     private ArrayAdapter<String> adapter;
     private boolean empty = false;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setContentView(R.layout.main);
 	// Database
 	database = new DatabaseHandler(this);
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
 		    Intent intent = new Intent(getBaseContext(),
 			    FormListActivity.class);
 		    intent.putExtra("formName", formNames.get(position));
 		    startActivity(intent);
 		}
 	    }
 
 	});
 	listView.setLayoutAnimation(controller);
     }
 
     @Override
     public void onResume() {
 	// Adapter
 	formNames = database.getFormNames();
 	if (formNames.isEmpty()) {
 	    empty = true;
 	    formNames.add("No forms available! :(");
 	    formNames.add("To make a new form:");
 	    formNames.add("1. Tap the menu key.");
 	    formNames.add("2. Tap 'New Form'");
 	    formNames
 		    .add("3. Name and design your new form with the fields you want.");
 
 	}
 	adapter = new ArrayAdapter<String>(this,
 		android.R.layout.simple_list_item_1, formNames);
 	setListAdapter(adapter);
 	super.onResume();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
 	// Now, inflate our submenu.
 	MenuInflater inflater = new MenuInflater(this);
 	inflater.inflate(R.menu.mainmenu, menu);
 	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	Intent intent;
 	switch (item.getItemId()) {
 	case R.id.newform:
 	    // Show new form activity
 	    intent = new Intent(
 		    findViewById(android.R.id.content).getContext(),
 		    NewFormActivity.class);
 	    startActivity(intent);
 	    return true;
 	case R.id.camera:
 	    // TODO put this elsewhere, sorta
 	    // show dialog of download
 	    intent = new Intent(
 		    findViewById(android.R.id.content).getContext(),
 		    CameraPreview.class);
 	    startActivity(intent);
 	    return true;
 	case R.id.settings:
 	    // TODO show settings view
 	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setMessage("Writen by Vincent Deloso")
 		    .setCancelable(true)
 		    .setPositiveButton("OK",
 			    new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog,
 					int id) {
 				    dialog.cancel();
 				}
 			    });
 	    AlertDialog alert = builder.create();
 	    alert.show();
 	    return true;
 	default:
 	    // INFO non-menuitem? IDEK
 	    return false;
 	}
     }
 
     /**
      * Show the confirm exit dialog when the back button is pressed
      */
     public void onBackPressed() {
 	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	builder.setMessage("Are you sure you want to exit?")
 		.setCancelable(false)
 		.setPositiveButton("Yes",
 			new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int id) {
 				activity.finish();
 			    }
 			})
 		.setNegativeButton("No", new DialogInterface.OnClickListener() {
 		    public void onClick(DialogInterface dialog, int id) {
 			dialog.cancel();
 		    }
 		});
 	AlertDialog alert = builder.create();
 	alert.show();
     }
 }
