 /**
  * Description: This is the MainActivity class. It is the starting point of the app and will direct the app through
  * 	the other activities as needed. 
  *
  * Documentation Statement: We worked on this Android App all on our own. We did not receive 
  * 	any help on this project from any other student enrolled or not enrolled in the CSCI498 
  * 	class. 
  *
  * @author Craig J. Soto II
  * @author Ben Casey
  * point distribution: Ben- 55% : Craig - 45% 
  */
 
 package edu.mines.caseysoto.schoolschedulercaseysoto;
 
 import android.annotation.SuppressLint;
 import android.app.ListActivity;
 import android.app.LoaderManager;
 import android.content.ContentValues;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 
 @SuppressLint("NewApi")
 public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, InputDialogFragment.Listener{
 
 	private SimpleCursorAdapter adapter;
 	private static final int DELETE_ID = Menu.FIRST + 1;
 	private static final int EDIT_ID = Menu.FIRST + 2;
 	private String courseName;
 	public static final String COURSE_MNAME = "NameOfCourse";
 	public static final String HW_NAME_TEXT = "NameOfHW";
 	public static final String DATE_TEXT = "DueDate";
 	public static final String DESC_TEXT = "Description";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.classes_list);
		this.getListView().setDividerHeight( 4);
 		fillData();
 		registerForContextMenu( getListView() );
 	}
 
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	@Override
 	  public boolean onOptionsItemSelected( MenuItem item )
 	  {
 	    switch( item.getItemId() )
 	    {
 	    case R.id.action_allHomework:
 	    	Intent i = new Intent(this, AllHomeworkActivity.class);
 			startActivity(i);
 	    default:
 	          return super.onOptionsItemSelected(item);
 	    }
 	  }
 	public void onDialog(View view){
 		Bundle args = new Bundle();
 		args.putInt( "dialogID", 1 );
 		args.putString( "prompt", getString( R.string.statement ) );
 
 		InputDialogFragment dialog = new InputDialogFragment();
 		dialog.setArguments( args );
 		dialog.show( getFragmentManager(), "Dialog" );
 
 	}
 	/**
 	 * Inserts the course into the Course Table.
 	 * checks to see if there are now 2 course of the same name and deletes the last inserted course
 	 */
 	public void insertNewCourse(){
 		ContentValues values = new ContentValues();
 		//values.put(CourseTable.COLUMN_ID, "idd");
 		values.put( CourseTable.COLUMN_NAME, courseName );
 		String[] projection = { CourseTable.COLUMN_ID, CourseTable.COLUMN_NAME};
 		String[] selection = {courseName};
 		getContentResolver().insert( SchedulerContentProvider.CONTENT_URI, values );
 		
 		//chgecks to see if that course name has already been added
 		Cursor cursor = getContentResolver().query( SchedulerContentProvider.CONTENT_URI, projection, "name=?", selection, CourseTable.COLUMN_ID + " DESC" );
 		if(cursor.getCount() >1){
 			cursor.moveToFirst();
 			Uri courseUri = Uri.parse( SchedulerContentProvider.CONTENT_URI + "/" +  cursor.getString(cursor.getColumnIndexOrThrow( CourseTable.COLUMN_ID )) );
 			getContentResolver().delete(courseUri, null, null);
 			Toast toast = Toast.makeText(getApplicationContext(),"Have already added " +courseName+" course!" , Toast.LENGTH_LONG);
 			toast.show();
 			fillData();
 		}
 		cursor.close();
 		
 	}
 	
 	/**
 	 * Updates the course Name and it's corresponding homework.
 	 * @param newCourseName : used to update the name while courseName is the old course name to query
 	 */
 	public void updateNewCourse(String newCourseName){
 		ContentValues values = new ContentValues();
 		values.put( CourseTable.COLUMN_NAME, newCourseName );
 		String[] projection = { CourseTable.COLUMN_ID, CourseTable.COLUMN_NAME};
 		String[] selection = {courseName};
 		String[] querySelection = {newCourseName};
 		//chgecks to see if that course name is already in database and adds if not. 
 		Cursor cursor = getContentResolver().query( SchedulerContentProvider.CONTENT_URI, projection, "name=?", querySelection, CourseTable.COLUMN_ID + " DESC" );
 		//Log.d("SchoolScheduler::Update Debu", "curosor count : " + cursor.getCount());
 		if(cursor.getCount() <1){
 			int rowsUpdated = getContentResolver().update( SchedulerContentProvider.CONTENT_URI, values, "name=?", selection );
 			Log.d("SchoolScechulder::update Debug", rowsUpdated + ": " + this.courseName + ": " +newCourseName );	
 			fillData();
 			
 			String[] selectionC = {courseName};
 			String[] projection2 = {HomeworkTable.COLUMN_ID, HomeworkTable.COLUMN_NAME, HomeworkTable.COLUMN_DATE, HomeworkTable.COLUMN_DESCRIPTION, HomeworkTable.COLUMN_COURSE_NAME};
 			
 			Cursor cursorC = getContentResolver().query(SchedulerContentProvider.CONTENT_URI_H, projection2, "course=?", selectionC, null);
 			ContentValues valuesC = new ContentValues();
 			valuesC.put( HomeworkTable.COLUMN_COURSE_NAME, newCourseName );
 			for(int i=0; i < cursorC.getCount(); ++i){
 				rowsUpdated = getContentResolver().update( SchedulerContentProvider.CONTENT_URI_H, valuesC, "course=?", selectionC );
 				
 			}
 		}
 		cursor.close();
 		
 		
 	}
 	/**
 	 * overriden function from listView that when clicked will open up the homework activity to show the courses homework.
 	 */
 	@Override
 	protected void onListItemClick( ListView l, View v, int position, long id )
 	{
 		super.onListItemClick( l, v, position, id );
 		Intent i = new Intent( this, HomeworkActivity.class );
 		Uri courseUri = Uri.parse( SchedulerContentProvider.CONTENT_URI + "/" + id );
 		String[] projection = { CourseTable.COLUMN_NAME };
 
 		//gets the uris for the same id, moves it to first position.
 		Cursor cursor = getContentResolver().query( courseUri, projection, null, null, null );
 		String name= "";
 		cursor.moveToFirst();	    
 		name = cursor.getString( cursor.getColumnIndexOrThrow( CourseTable.COLUMN_NAME ) );
 		cursor.close();
 		i.putExtra(COURSE_MNAME, name);
 		i.putExtra( SchedulerContentProvider.CONTENT_ITEM_TYPE, courseUri );
 		startActivity( i );
 	}
 
 	/**
 	 * overridden function from listview, if long pressed will delete or edit the course.
 	 * The delete, deletes the course and deletes the corresponding homework from the homework table
 	 * The edit uses the input Dialog and that changes the course name and corresponding homework.
 	 */
 	@Override
 	public boolean onContextItemSelected( MenuItem item )
 	{
 		switch( item.getItemId() )
 		{
 		case DELETE_ID:
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
 			Uri uri = Uri.parse( SchedulerContentProvider.CONTENT_URI + "/" + info.id );
 			
 			//query to get the course name that is bieng deleted
 			String[] projection2 = { CourseTable.COLUMN_NAME };
 			Cursor cursor2 = getContentResolver().query( uri, projection2, null, null, null );
 			String name2= "";
 			cursor2.moveToFirst();	    
 			name2 = cursor2.getString( cursor2.getColumnIndexOrThrow( CourseTable.COLUMN_NAME ) );
 			cursor2.close();
 			this.courseName= name2;
 			
 			getContentResolver().delete( uri, null, null );
 			
 			//get all homework associsated with this course and delete it.
 			String[] projection = { HomeworkTable.COLUMN_ID, HomeworkTable.COLUMN_NAME, HomeworkTable.COLUMN_DATE, HomeworkTable.COLUMN_DESCRIPTION, HomeworkTable.COLUMN_COURSE_NAME };
 			String[] querySelection = { this.courseName };
 			//gets the uris for the same id, moves it to first position.
 			uri = Uri.parse( SchedulerContentProvider.CONTENT_URI_H + "/");
 			Cursor cursor = getContentResolver().query( uri, projection, "course=?", querySelection, null );
 			cursor.moveToFirst();
 			for(int i=0; i < cursor.getCount(); ++i){
 				String id =  cursor.getString(cursor.getColumnIndexOrThrow(HomeworkTable.COLUMN_ID));
 				uri = Uri.parse( SchedulerContentProvider.CONTENT_URI_H + "/" + id );
 				getContentResolver().delete( uri, null, null );
 				cursor.moveToNext();
 			}
 			cursor.close();
 			fillData();
 			return true;
 		case EDIT_ID: 
 			info = (AdapterContextMenuInfo)item.getMenuInfo();
 			uri = Uri.parse( SchedulerContentProvider.CONTENT_URI + "/" + info.id );
 			String[] projection3 = { CourseTable.COLUMN_NAME };
 
 			//gets the uris for the same id, moves it to first position.
 			cursor2 = getContentResolver().query( uri, projection3, null, null, null );
 			name2= "";
 			cursor2.moveToFirst();	    
 			name2 = cursor2.getString( cursor2.getColumnIndexOrThrow( CourseTable.COLUMN_NAME ) );
 			cursor2.close();
 			this.courseName= name2;
 			Bundle args = new Bundle();
 			args.putInt( "dialogID", 2 );
 			args.putString( "prompt", getString( R.string.statement ) );
 
 			InputDialogFragment dialog = new InputDialogFragment();
 			dialog.setArguments( args );
 			dialog.show( getFragmentManager(), "Dialog" );
 		}
 		return super.onContextItemSelected( item );
 	}
 	/**
 	 * onCreateLoader loads the initial course table with anything that follows the projection in the database.
 	 */
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		String[] projection = { CourseTable.COLUMN_ID, CourseTable.COLUMN_NAME };
 		CursorLoader cursorLoader = new CursorLoader( this, SchedulerContentProvider.CONTENT_URI, projection, null, null, null );
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
 	
 	/**
 	 * the main aspect of updated the listview, so that the insertion, deletion, and editing shows up. 
 	 * the adapter also adds background color for odd- even rows.
 	 */
 	private void fillData()
 	{
 		// Fields from the database (projection)
 		// Must include the _id column for the adapter to work
 		String[] from = new String[] { CourseTable.COLUMN_NAME };
 
 		// Fields on the UI to which we map
 		int[] to = new int[] { R.id.label };
 
 		// Ensure a loader is initialized and active.
 		getLoaderManager().initLoader( 0, null, this );
 
 		// Note the last parameter to this constructor (zero), which indicates the adaptor should
 		// not try to automatically re-query the data ... the loader will take care of this.
 		this.adapter = new SimpleCursorAdapter( this, R.layout.list_row, null, from, to, 0 ){
 			@Override
 		    public View getView(int position, View convertView, ViewGroup parent) {
 				 View v = super.getView(position, convertView, parent);
 
 			        if (position %2 ==1) {
 			            v.setBackgroundColor(Color.argb(TRIM_MEMORY_MODERATE, 100, 100, 100));
 			        } else {
 			            v.setBackgroundColor(Color.argb(TRIM_MEMORY_MODERATE, 170, 170, 170)); //or whatever was original
 			        }
 
 			        return v;
 			}
 			
 		};
 
 
 		// Let this ListActivity display the contents of the cursor adapter.
 		setListAdapter( this.adapter );
 	}
 
 	/**
 	 * overrides the dialog fragment for inputting course. depending on eit or inserting. 
 	 * @param dialogID : the id returned to see if it is an insert or edit.
 	 * @param input : the returned string.
 	 */
 	@Override
 	public void onInputDone( int dialogID, String input )
 	{
 		Log.d( "School_Scheduler", "\"" + input + "\" received from input dialog with id =" + dialogID );
 
 		if(dialogID == 1){
 			this.courseName = input;
 			insertNewCourse();
 		}
 		else if(dialogID == 2){
 			updateNewCourse(input);
 			
 			
 		}
 
 	}
 
 	/**
 	 * Callback method from InputDialogFragment when the user clicks Cancel.
 	 * 
 	 * @param dialogID The dialog producing the callback.
 	 */
 	@Override
 	public void onInputCancel( int dialogID )
 	{
 		Log.d( "School_Scheduler", "No input received from input dialog with id =" + dialogID );
 	}
 
 	/** The menu displayed on a long touch. */
 	@Override
 	public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
 	{
 		super.onCreateContextMenu( menu, v, menuInfo );
 		menu.add( 0, DELETE_ID, 0, R.string.menu_delete );
 		menu.add( 0, EDIT_ID, 0, R.string.menu_edit );
 	}
 
 
 }
