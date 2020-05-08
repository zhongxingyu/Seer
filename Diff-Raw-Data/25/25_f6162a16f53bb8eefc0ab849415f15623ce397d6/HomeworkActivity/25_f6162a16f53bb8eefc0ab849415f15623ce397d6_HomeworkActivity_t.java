 package edu.mines.caseysoto.schoolschedulercaseysoto;
 
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.app.LoaderManager;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class HomeworkActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>{
 
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private String homeworkName;
 	private SimpleCursorAdapter adapter;
 	public static final String HW_NAME = "NameOfHomework";
 	private String courseName;
 	private View mCourseText;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		setContentView( R.layout.homework_list );
 		this.getListView().setDividerHeight( 2 );
 		fillData();
 		registerForContextMenu( getListView() );
 
 		mCourseText= findViewById(R.id.courseNameMid);
 
 		// Get the message from the intent
 		Intent intent = getIntent();
 		String message = intent.getStringExtra( MainActivity.COURSE_MNAME);
 		((TextView) mCourseText).setText(message);
 		courseName = message;
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		String[] projection = { HomeworkTable.COLUMN_ID, HomeworkTable.COLUMN_NAME, HomeworkTable.COLUMN_DATE, HomeworkTable.COLUMN_DESCRIPTION, HomeworkTable.COLUMN_COURSE_NAME };
		CursorLoader cursorLoader = new CursorLoader( this, SchedulerContentProvider.CONTENT_URI_H, projection, null, null, null );
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
 		this.adapter.swapCursor( arg1 );
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		this.adapter.swapCursor( null );
 	}
 
 	private void fillData()
 	{
 		// Fields from the database (projection)
 		// Must include the _id column for the adapter to work
		String[] from = new String[] { HomeworkTable.COLUMN_ID, HomeworkTable.COLUMN_NAME, HomeworkTable.COLUMN_DATE, HomeworkTable.COLUMN_DESCRIPTION, HomeworkTable.COLUMN_COURSE_NAME };
 
 		// Fields on the UI to which we map
 		int[] to = new int[] { R.id.label };
 
 		// Ensure a loader is initialized and active.
 		getLoaderManager().initLoader( 0, null, this );
 
 		// Note the last parameter to this constructor (zero), which indicates the adaptor should
 		// not try to automatically re-query the data ... the loader will take care of this.
 		this.adapter = new SimpleCursorAdapter( this, R.layout.list_row, null, from, to, 0 );
 
 		// Let this ListActivity display the contents of the cursor adapter.
 		setListAdapter( this.adapter );
 	}
 
 	public void addHomeworkToList(View view){
 		Intent intent = new Intent(this, AddHomeworkActivity.class);
 		intent.putExtra(MainActivity.COURSE_MNAME, courseName);
 		startActivity(intent);
 	}
 }
