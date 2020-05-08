 package com.sternerlearn;
 
 import java.util.ArrayList;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class TeacherMainMenu extends ListActivity {
 	
 	private ArrayList<String> mListItems = new ArrayList<String>();
 	private ArrayAdapter<String> mAdapter;
 	
 	AsyncCall mTask;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_teacher_main_menu);
 		
 		mAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, mListItems );
 		setListAdapter( mAdapter );
 		
		setTitle( SharedData.getInstance().getAccount().mRealName );
		
 		mTask = new AsyncCall();
 		mTask.execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.teacher_main_menu, menu);
 		return true;
 	}
 	
 	protected void onListItemClick( ListView aView, View v, int position, long id )
 	{
 		if( 0 == position )
 		{
 			// Log them out.
 			SharedPreferences settings = getSharedPreferences(Types.PREFS_FILE, MODE_PRIVATE);
 			SharedPreferences.Editor editor = settings.edit();
 			editor.clear();
 			editor.commit();
 			
 			SharedData.getInstance().clear();
 			
 			// Pop the stack and go to the powerup page (which will push the login page)
 			Intent intent = new Intent(this, PowerupActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
 			startActivity(intent);
 			
 		}
 		else if( 1 == position )
 		{
 			// This is the "Add Class" option
 			// Push a page to create a new class in the DB
 			startActivity(new Intent(this, AddClassActivity.class));			
 		}
 		else
 		{
 			// Decrement position to account for the fake things and set the current course
 			position = position - 2;
 			Teacher t = SharedData.getInstance().getTeacher();
 			ArrayList<Course> courses;
 			try
 			{
 				courses = t.getCourses();
 			}
 			catch( Exception ex )
 			{
 				courses = null;
 			}
 			if( position < courses.size() )
 			{
 				SharedData.getInstance().mCurrentCourse = courses.get(position);
 				startActivity(new Intent(this, ModifyClassActivity.class));
 				// TODO: add some more error handling
 			}
 		}
 	}	
 	
 	@Override
 	public void onWindowFocusChanged( boolean hasFocus )
 	{
 		super.onWindowFocusChanged(hasFocus);
 		
 		// Re-run the task if we get focus and aren't already running
 		if( hasFocus )
 		{
 			if( mTask == null || AsyncTask.Status.RUNNING != mTask.getStatus() )
 			{
 				mListItems.clear();
 				mAdapter.notifyDataSetChanged();
 				mTask = new AsyncCall();
 				mTask.execute();
 			}
 		}
 	}
 	
 	Teacher getClasses()
 	{
 		Teacher t = SharedData.getInstance().getTeacher();
 		if( t == null )
 		{
 			t = new Teacher( SharedData.getInstance().getAccount() );
 		}
 		
 		try
 		{
 			t.updateClasses();
 		}
 		catch( Exception ex ){}
 		
 		return t;
 	}
 	
 	void finishAsync()
 	{
 		mListItems.clear();
 		mListItems.add("Log Out");
 		mListItems.add("Add Class");
 		
 		Teacher t = SharedData.getInstance().getTeacher();
 		for( Course c : t.mCourses )
 		{
 			mListItems.add(c.mClassName);
 		}
 		
 		mAdapter.notifyDataSetChanged();
 	}
 	
     private class AsyncCall extends AsyncTask<Void, Void, Teacher> {
         @Override
         protected Teacher doInBackground(Void... params) {
             return getClasses();
         }
 
         @Override
         protected void onPostExecute(Teacher result) {
     		SharedData.getInstance().setTeacher(result);
     		finishAsync();        	
         }
 
         @Override
         protected void onPreExecute() {
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
         }
 
     }		
 
 }
