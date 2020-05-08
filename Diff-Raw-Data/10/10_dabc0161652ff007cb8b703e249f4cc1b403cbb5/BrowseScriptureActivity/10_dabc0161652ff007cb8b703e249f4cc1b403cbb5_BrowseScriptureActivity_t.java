 package com.digitalbias.android;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class BrowseScriptureActivity extends ListActivity {
 	
 	public static final String TAG = "Scriptures";
 
 	public static final String BROWSE_MODE = "BrowseMode";
 
 	public static final int BROWSE_SCRIPTURES_MODE = 0; 
 	public static final int BROWSE_VOLUME_MODE = 1; 
 	public static final int BROWSE_BOOK_MODE = 2; 
 
 	private static final int ACTIVITY_PREFERENCES = 0;
 	private static final int ACTIVITY_DOWNLOAD = 1;
 	private static final int ACTIVITY_BOOKMARK = 2;
 	private static final int ACTIVITY_READ_CHAPTER = 3;
 
 	public static boolean DEBUG = true;
 	
 	private Cursor mCursor;
 	private ScriptureDbAdapter mAdapter;
 	private int mBrowseMode; 
 	
 	private Long mBookId;
 	private Long mVolumeId;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         applyPreferences();
         initializeDatabase();
         if(mAdapter.canMakeValidConnection()){
 	    	browseScriptures();
         } else {
         	getGoodDatabase();
         }
         closeDatabase();
     }
     
     @Override
     public void onNewIntent(Intent intent){
     	log("New INTENT");
     	Bundle extras = intent.getExtras();
    	if(extras!= null) {
	    	mVolumeId = extras.getLong(ScriptureDbAdapter.VOLUME_ID);
	    	mBookId = extras.getLong(ScriptureDbAdapter.BOOK_ID);
	    	mBrowseMode = extras.getInt(BROWSE_MODE);
	    	log(mVolumeId + " : " + mBookId + " : " + mBrowseMode);
    	}
     	beginBrowsing(intent);
     }
     
     private void initializeDatabase() {
         if(mCursor != null) {
         	mCursor.close();
         	mCursor = null;
         }
         if(mAdapter != null) {
         	mAdapter.close();
         }
         mAdapter = new ScriptureDbAdapter(this);
 	}
 
     private void closeDatabase() {
 //        mAdapter.close();
 //        mAdapter = null;
 	}
 
 	protected void getGoodDatabase(){
     	showDownloadDialog();
     }
     
     protected void showDownloadDialog(){
 
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Invalid Database. Do you wish to download a database or choose an existing one?");
 		builder.setTitle("Invalid database");
 		builder.setNegativeButton("Choose", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int which){
 				openPreferences();
 			}
 		});
 		builder.setPositiveButton("Download", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int which){
 				startDownloadDatabase();
 			}
 		});
 		builder.setCancelable(false);
 		AlertDialog dialog = builder.create();
 		dialog.show();
     }
     
     protected void startDownloadDatabase(){
         Intent i = new Intent(this, DownloadDatabaseActivity.class);
         startActivityForResult(i, ACTIVITY_DOWNLOAD);
     }
     
     protected void startManageBookmarks(){
         Intent i = new Intent(this, ManageBookmarksActivity.class);
         startActivityForResult(i, ACTIVITY_BOOKMARK);
     }
     
     protected void applyPreferences() throws SQLiteException {
         setTheme(SetPreferencesActivity.getPreferedTheme(this));
     }
     
     private void browseScriptures(){
         setContentView(R.layout.scripture_list);
         mBrowseMode = BROWSE_SCRIPTURES_MODE;
 		mAdapter.open();
 		mCursor = mAdapter.fetchAllVolumes();
     	startManagingCursor(mCursor);
     	String[] from = new String[] { ScriptureDbAdapter.VOLUME_TITLE };
     	int[] to = new int[] {R.id.text1 };
     	
     	SimpleCursorAdapter volumes = new SimpleCursorAdapter(this, R.layout.scripture_row, mCursor, from, to);
     	setListAdapter(volumes);
     }
     
     private void browseVolume(Long volumeId){
         setContentView(R.layout.volume_list);
         mBrowseMode = BROWSE_VOLUME_MODE;
         mBookId = null;
         mVolumeId = volumeId;
     	
         Log.i(BrowseScriptureActivity.TAG, volumeId.toString());
 		Cursor c = mAdapter.fetchSingleVolume(volumeId.toString());
 		TextView titleText = (TextView) findViewById(R.id.volume_title);
     	String title = c.getString(c.getColumnIndex( ScriptureDbAdapter.VOLUME_TITLE_LONG));
     	titleText.setText(title);
         Button homeButton = (Button) findViewById(R.id.back_home);
         homeButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view){
         		browseScriptures();
         	}
         });
 
     	mCursor = mAdapter.fetchVolumeBooks(volumeId.toString());
     	startManagingCursor(mCursor);
     	String[] from = new String[] { ScriptureDbAdapter.BOOK_TITLE };
     	int[] to = new int[] {R.id.text1 };
     	
     	SimpleCursorAdapter volumes = new SimpleCursorAdapter(this, R.layout.scripture_row, mCursor, from, to);
     	setListAdapter(volumes);
     }
     
     private void browseBook(Long bookId){
         setContentView(R.layout.book_list);
         mBrowseMode = BROWSE_BOOK_MODE;
         mBookId = bookId;
 
         TextView titleText = (TextView) findViewById(R.id.book_title);
         TextView mSubTitleText = (TextView) findViewById(R.id.book_subtitle);
         Button volumeButton = (Button) findViewById(R.id.back_volume);
         Button homeButton = (Button) findViewById(R.id.back_home);
     	
 		Cursor c = mAdapter.fetchSingleBook(bookId.toString());
         String title = c.getString( c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_TITLE));
         String subTitle = c.getString( c.getColumnIndexOrThrow(ScriptureDbAdapter.BOOK_SUBTITLE));
         c.close();
         c = mAdapter.fetchSingleVolume(mVolumeId.toString());
         String volumeTitle = c.getString(c.getColumnIndex(ScriptureDbAdapter.VOLUME_TITLE_LONG));
         c.close();
 
     	titleText.setText(title);
     	mSubTitleText.setText(subTitle);
     	volumeButton.setText(volumeTitle);
 
         volumeButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view){
         		browseVolume(mVolumeId);
         	}
         });
         homeButton.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view){
         		browseScriptures();
         	}
         });
 
     	mCursor = mAdapter.fetchBookChapters(bookId.toString());
     	startManagingCursor(mCursor);
     	String[] from = new String[] { ScriptureDbAdapter.CHAPTER_TITLE};
     	int[] to = new int[] {R.id.text1 };
     	
     	SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.scripture_row, mCursor, from, to);
     	setListAdapter(adapter);
     }
 
     private void openChapter(int position, Long id){
         TextView titleText = (TextView) findViewById(R.id.book_title);
         Button volumeButton = (Button) findViewById(R.id.back_volume);
 
         Cursor c = mCursor;
         c.moveToPosition(position);
         Intent i = new Intent(this, ReadChapterActivity.class);
         i.putExtra(ScriptureDbAdapter.TABLE_ID, id);
         i.putExtra(ScriptureDbAdapter.BOOK_TITLE, titleText.getText());
         i.putExtra(ScriptureDbAdapter.BOOK_ID, mBookId);
         i.putExtra(ScriptureDbAdapter.VOLUME_ID, mVolumeId);
         i.putExtra(ScriptureDbAdapter.VOLUME_TITLE, volumeButton.getText());
         i.putExtra(ScriptureDbAdapter.BOOK_TITLE_SHORT, getShortBookTitle(mBookId));
         i.putExtra(ScriptureDbAdapter.CHAPTER_TITLE, c.getString(
                 c.getColumnIndexOrThrow(ScriptureDbAdapter.CHAPTER_TITLE)));
         i.putExtra(ReadChapterActivity.CALLING_ACTIVITY, this.getClass().getName());
         
         startActivityForResult(i, ACTIVITY_READ_CHAPTER);
     }
     
     private String getShortBookTitle(Long bookId){
     	Cursor c = mAdapter.fetchSingleBook(bookId.toString());
     	return c.getString(c.getColumnIndex(ScriptureDbAdapter.BOOK_TITLE_SHORT));
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
     	super.onListItemClick(l, v, position, id);
     	switch (mBrowseMode){
 			case BROWSE_SCRIPTURES_MODE:
 				browseVolume(new Long(id));
 				break;
 			case BROWSE_VOLUME_MODE:
 				browseBook(new Long(id));
 				break;
 			case BROWSE_BOOK_MODE:
 				openChapter(position, new Long(id));
 				break;
 		}
     }
     
     protected void beginBrowsing(Intent intent){
     	int intendedBrowseMode = intent == null ? BROWSE_SCRIPTURES_MODE : intent.getIntExtra(BROWSE_MODE, BROWSE_SCRIPTURES_MODE);
     	switch (intendedBrowseMode){
 			case BROWSE_SCRIPTURES_MODE:
 				browseScriptures();
 				break;
     		case BROWSE_VOLUME_MODE:
 				browseVolume(intent.getExtras().getLong(ScriptureDbAdapter.VOLUME_ID));
     			break;
     		case BROWSE_BOOK_MODE:
 				browseBook(intent.getExtras().getLong(ScriptureDbAdapter.BOOK_ID));
     			break;
     	}
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         if (resultCode == RESULT_OK) {
         	switch (requestCode) {
         		case ACTIVITY_PREFERENCES:
         			log("preferences came back");
         	    	stopManagingCursor(mCursor);
         	        applyPreferences();
         	        initializeDatabase();
         	        if(mAdapter.canMakeValidConnection()){
         		    	browseScriptures();
         	        } else {
         	        	getGoodDatabase();
         	        }
 		            break;
         		case ACTIVITY_READ_CHAPTER:
                     beginBrowsing(intent);
         			break;
         	}
         }
     }
     
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	boolean result = super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.browse_menu, menu);
     	return result;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle all of the possible menu actions.
         switch (item.getItemId()) {
 	        case R.id.open_preferences:
 	        	openPreferences();
 	        	break;
 	        case R.id.download_database:
 	        	startDownloadDatabase();
 	        	break;
 	        case R.id.manage_bookmarks:
 	        	startManageBookmarks();
 	        	break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     public void openPreferences(){
         Intent i = new Intent(this, SetPreferencesActivity.class);
         i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
         startActivityForResult(i, ACTIVITY_PREFERENCES);
     }
     
     public void log(String message){
     	log("Browse Scriptures", message);
     }
     
     public void log(String tag, String message){
     	if(DEBUG){
         	Log.i(tag, message);
     	}
     }
 
     public boolean onKeyDown(int keyCode, KeyEvent event) { 
     	boolean result = false;
         if(keyCode == KeyEvent.KEYCODE_BACK) { 
         	switch (mBrowseMode){
 				case BROWSE_SCRIPTURES_MODE:
 			       	finish();
 					break;
 				case BROWSE_VOLUME_MODE:
 					browseScriptures();
 					break;
 				case BROWSE_BOOK_MODE:
 					browseVolume(mVolumeId);
 					break;
         	}
         	result = true;
         } 
         return result; 
     }     
 }
