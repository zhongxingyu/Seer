 package com.sternerlearn;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 
 public class StudentMainMenu extends MyListActivity {
 
 	private enum BUTTONS
 	{
 		LOGOUT,
 		GRADES,
 		ASSIGNMENTS
 	}
 	
 	private String[] myTitles = {
 			"Log Out",
 			"Grades",
 			"Assignments"
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		TITLES = myTitles;
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_student_main_menu);
		
		setTitle( SharedData.getInstance().getAccount().mRealName );
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.student_main_menu, menu);
 		return true;
 	}
 	
 	protected void onListItemClick( ListView aView, View v, int position, long id )
 	{
 		if( position == BUTTONS.GRADES.ordinal() )
 		{
 			startActivity( new Intent(this, GradesActivity.class));
 		}
 		else if( position == BUTTONS.ASSIGNMENTS.ordinal() )
 		{
 			startActivity( new Intent(this, AssignmentsActivity.class));
 		}
 		else if( position == BUTTONS.LOGOUT.ordinal() )
 		{
 			// Log them out. TODO!
 			SharedPreferences settings = getSharedPreferences(Types.PREFS_FILE, MODE_PRIVATE);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.clear();
 			editor.commit();
 			
 			SharedData.getInstance().clear();
 			
 			// Pop to the powerup page
 			Intent intent = new Intent(this, PowerupActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
 			startActivity(intent);
 		}
 	}	
 
 }
