 package com.plug.main;
 
 import keendy.projects.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.plug.Action;
 import com.plug.PlugApplication;
 import com.plug.database.model.User;
 import com.plug.doodle.DrawingActivity;
 import com.plug.note.NoteEditorActivity;
 import com.plug.note.NotesListActivity;
 import com.plug.notebook.NotebooksListActivity;
 import com.plug.session.LoginActivity;
 
 /**
  * HomeActivity that handles all key presses during home view
  * 
  * TODO Implement all onClick features
  */
 
 public class HomeActivity extends Activity implements OnClickListener {
 
 	public static final String TAG = HomeActivity.class.getSimpleName();
 	
 	PlugApplication application;
 	
 	/* Called when the activity is first created. */
 //	@InjectView(R.id.home_createnote)     private Button create;
 //	@InjectView(R.id.home_doodleNote) private Button doodle;
 //	@InjectView(R.id.home_notebooks)  private Button notebooks;
 //	@InjectView(R.id.home_allnotes)   private Button allnotes;
 	
 	private Button create;
 	private Button doodle;
 	private Button notebooks;
 	private Button allnotes;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		application = (PlugApplication) this.getApplicationContext();
 //		setContentView(R.layout.home);
 		if(User.getLoggedInUser(this) != null) {
   		setContentView(R.layout.home);
   		init();
 //  		showNotification();
 
 		} else {
			startActivity(new Intent(this, LoginActivity.class));
 		}
 	}
 
 //	private void showNotification() {
 //		NotificationManager manager = (NotificationManager) this
 //		.getSystemService(Context.NOTIFICATION_SERVICE);
 //		
 //		Notification notification = new Notification(R.drawable.plug_icon_green_small,
 //		"PLUG Notes",System.currentTimeMillis());
 //		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
 //		new Intent(this, HomeActivity.class), 0);
 //		notification.setLatestEventInfo(this, "PLUG Notes",
 //		"by JACK", contentIntent);
 //		notification.flags = Notification.FLAG_INSISTENT;
 //		manager.notify(0, notification);
 //		
 //	}
 
 	/* Initialize Buttons */
 	private void init() {
 		create = (Button) findViewById(R.id.home_createnote);
 		doodle = (Button) findViewById(R.id.home_doodleNote);
 		notebooks = (Button) findViewById(R.id.home_notebooks);
 		allnotes = (Button) findViewById(R.id.home_allnotes);
 		
 		create.setOnClickListener(this);
 		doodle.setOnClickListener(this);
 		notebooks.setOnClickListener(this);
 		allnotes.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		Intent mIntent = new Intent();
 		switch (v.getId()) {
   		case R.id.home_createnote:
   			mIntent.setAction(Intent.ACTION_INSERT);
   			mIntent.setClass(HomeActivity.this, NoteEditorActivity.class);
   			startActivity(mIntent);
   			break;
   		case R.id.home_doodleNote:
   			startActivity(new Intent(HomeActivity.this, DrawingActivity.class));
   			break;
   		case R.id.home_allnotes:
   			mIntent.setClass(HomeActivity.this, NotesListActivity.class);
   			mIntent.setAction(Action.VIEW_ALL_NOTES);
   			startActivity(mIntent);
   			break;
   		case R.id.home_notebooks:
   			mIntent.setClass(HomeActivity.this, NotebooksListActivity.class);
   			mIntent.setAction(Action.VIEW_ALL_NOTEBOOKS);
   			startActivity(mIntent);
   			break;
 		}
 	}
 	
 //	public void onClickCreate(View view) {
 //		Intent mIntent = new Intent();
 //		mIntent.setAction(Intent.ACTION_INSERT);
 //		mIntent.setClass(HomeActivity.this, NoteEditorActivity.class);
 //		startActivity(mIntent);
 //	}
 //	
 //	public void onClickNotebooks(View v){
 //	}
 //	
 //	public void onClickDoodle(View v){
 ////		HomeActivity.this.finish();
 //		startActivity(new Intent(HomeActivity.this, DoodleActivity.class));
 //	}
 //	
 //	public void onClickAllnotes(View v){
 ////		HomeActivity.this.finish();
 //		startActivity(new Intent(HomeActivity.this, ListTabActivity.class));
 //	}
 //
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		MenuInflater inflater = getMenuInflater();
 //	    inflater.inflate(R.menu.home_menu, menu);
 //	    return true;
 //	}
 //
 //	@Override
 //	public boolean onOptionsItemSelected(MenuItem item) {
 //		switch(item.getItemId()){
 //		case R.id.refresh:
 //			return true;
 //		default:
 //	        return super.onOptionsItemSelected(item);
 //		}
 //	}
 	
 	
 	
 //	@Override
 //	public boolean onCreateOptionsMenu(Menu menu) {
 //		MenuInflater inflater = getMenuInflater();
 //	    inflater.inflate(R.menu.home_menu, menu);
 //	    return true;
 //	}
 //
 //	@Override
 //	public boolean onOptionsItemSelected(MenuItem item) {
 //	    // Handle item selection
 //	    switch (item.getItemId()) {
 //	    case R.id.refresh:
 //	    	
 //	    	return true;
 //	    default:
 //	        return super.onOptionsItemSelected(item);
 //	    }
 //	}
   @Override
   public void onBackPressed() {
   	Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
   	intent.putExtra("EXIT", true);	
     startActivity(intent);	
     super.onBackPressed();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
   	super.onCreateOptionsMenu(menu);
   	MenuInflater inflater = this.getMenuInflater();
   	inflater.inflate(R.menu.home_menu, menu);
   	
   	return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
   	switch(item.getItemId()) {
   		case R.id.logout:
   			User.logout(this);
   			finish();
   			break;
   	}
   	
   	return true;
   }
   
 }
