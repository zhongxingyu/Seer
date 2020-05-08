 package pl.twopointtwo.sitenote;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.Toast;
 import pl.twopointtwo.sitenote.R;
 
 public class MainActivity extends Activity {
 	private static final int CAMERA_REQUEST = 1888;
 
 	public static final String TWOJA_FOTKA_START = "pl.twopointtwo.sitenote.dodajZdjecie";
 	public static final String SHOW_NOTE_FILE = "pl.twopointtwo.sitenote.SHOW_NOTE_FILE";
 	public static final String SHOW_NOTE_DATE = "pl.twopointtwo.sitenote.SHOW_NOTE_DATE";
 	public static final String SHOW_NOTE_NOTE = "pl.twopointtwo.sitenote.SHOW_NOTE_NOTE";
 	public static final String SHOW_NOTE_LATITUDE = "pl.twopointtwo.sitenote.SHOW_NOTE_LATITUDE";
 	public static final String SHOW_NOTE_LATITUDE_REF = "pl.twopointtwo.sitenote.SHOW_NOTE_LATITUDE_REF";
 	public static final String SHOW_NOTE_LONGITUDE = "pl.twopointtwo.sitenote.SHOW_NOTE_LONGITUDE";
 	public static final String SHOW_NOTE_LONGITUDE_REF = "pl.twopointtwo.sitenote.SHOW_NOTE_LONGITUDE_REF";
 	public static final String PHOTO_DIR = "/SightNoteTmp/";
 	public static final String THUMB_DIR = "/SightNoteThumbs/";
 
 	private Uri mImageCaptureUri1;
 	private Boolean isAfterOnActivityResult;
 
 	public static ArrayList<DataSet> noteList = new ArrayList<DataSet>();
 
 	private ImageAdapter imageAdapter;
 	private ListView lv;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		Log.d("LogInfo001", "MA - onCreate");
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		isAfterOnActivityResult = false;
 		if (noteList.isEmpty()) {
 			Log.d("LogInfo001", "isEmpty");
 			feedNoteListFromDatabase();
 		}
 
 		lv = (ListView) findViewById(R.id.listView1);
 
 		imageAdapter = new ImageAdapter(this, noteList);
 		lv.setAdapter(imageAdapter);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				DataSet item = (DataSet) parent.getItemAtPosition(position);
 				Intent intent = new Intent(getApplicationContext(), ShowNoteActivity.class);
 
 				// Set of parameters sent to ShowNoteActivity, it could be done
 				// in one Array of Strings
 				// but we are still not sure what finally we'll need to send
 				intent.putExtra(SHOW_NOTE_FILE, item.getFile());
 				intent.putExtra(SHOW_NOTE_DATE, item.getDateTime());
 				intent.putExtra(SHOW_NOTE_NOTE, item.getNote());
 				intent.putExtra(SHOW_NOTE_LATITUDE, item.getExifGpsLatitude());
 				intent.putExtra(SHOW_NOTE_LATITUDE_REF, item.getExifGpsLatitudeRef());
 				intent.putExtra(SHOW_NOTE_LONGITUDE, item.getExifGpsLongitude());				
 				intent.putExtra(SHOW_NOTE_LONGITUDE_REF, item.getExifGpsLongitudeRef());
 				startActivity(intent);
 			}
 		});
 		registerForContextMenu(lv);
 	}
 
 	@Override
 	protected void onResume() {
 		Log.d("LogInfo001", "MA - onResume");
 		super.onResume();
 
 		if (isAfterOnActivityResult) {
 			// po nacisnieciu zapisz w app aparatu, wywolywana jest
 			// onActivityResult a pozniej onResume
 			// dlatego przenioslem intent dla TwojaFotkaActivity do onResume
 			isAfterOnActivityResult = false;
 			Intent intent = new Intent(this, TwojaFotkaActivity.class);
 			intent.putExtra(TWOJA_FOTKA_START, mImageCaptureUri1.getPath());
 			startActivity(intent);
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.con_menu_dell_all:
 			deleteAllNotes();
 			// Toast.makeText(getApplicationContext(),
 			// "edit pos="+info.position+"; idDB="
 			// +noteList.get(info.position).getDbId(),
 			// Toast.LENGTH_SHORT).show();
 			return true;
 		case R.id.con_menu_del:
 			deleteOneNote(info.position);
 
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_add_button) {
 			addPhoto();
 		}
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void deleteAllNotes() {
 		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
 		adb.setTitle(R.string.delete_msg);
 		adb.setMessage(R.string.delete_all_msg);
 		// final int positionToRemove = position;
 		adb.setNegativeButton(R.string.neg_button, null);
 		adb.setPositiveButton(R.string.pos_button, new AlertDialog.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 
 				DbHelper mDbHelper = new DbHelper(MainActivity.this);
 				SQLiteDatabase db = mDbHelper.getReadableDatabase();
 				db.delete(DbHelper.FeedSiteNote.TABLE_NAME, null, null);
 
 				noteList.clear();
 				imageAdapter.notifyDataSetChanged();
 			}
 		});
 		adb.show();
 	}
 
 	private void deleteOneNote(int listInd) {
 		AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
 		adb.setTitle(R.string.delete_msg);
 		adb.setMessage(R.string.delete_record_msg);
 		final int listIndex = listInd;
 		final String dbId = noteList.get(listIndex).getDbId();
 		adb.setNegativeButton(R.string.neg_button, null);
 		adb.setPositiveButton(R.string.pos_button, new AlertDialog.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 
 				DbHelper mDbHelper = new DbHelper(MainActivity.this);
 				SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 				String selection = DbHelper.FeedSiteNote._ID + " LIKE ?";
 				String[] selectionArgs = { dbId };
 
 				db.delete(DbHelper.FeedSiteNote.TABLE_NAME, selection, selectionArgs);
 
 				noteList.remove(listIndex);
 				imageAdapter.notifyDataSetChanged();
 			}
 		});
 		adb.show();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void addPhoto() {
 		String storageState = Environment.getExternalStorageState();
 		if (!Environment.MEDIA_MOUNTED.equals(storageState)) {
 			Toast.makeText(this, R.string.storage_alert, Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		File photoDir = new File(Environment.getExternalStorageDirectory() + PHOTO_DIR);
 		photoDir.mkdirs();
 
 		mImageCaptureUri1 = Uri.fromFile(new File(photoDir, "thmb" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
 
 		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 		cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri1);
 		cameraIntent.putExtra("return-data", true);
 		startActivityForResult(cameraIntent, CAMERA_REQUEST);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("LogInfo001", "MA - onActivityResult");
 
 		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
 			isAfterOnActivityResult = true;
 		}
 	}
 
 	private void feedNoteListFromDatabase() {
 		DbHelper mDbHelper = new DbHelper(this);
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 
 		// Define a projection that specifies which columns from the database
 		// you will actually use after this query.
 		String[] projection = { DbHelper.FeedSiteNote._ID, DbHelper.FeedSiteNote.COLUMN_NAME_ID, DbHelper.FeedSiteNote.COLUMN_NAME_DATE_TIME,
 				DbHelper.FeedSiteNote.COLUMN_NAME_FILE, DbHelper.FeedSiteNote.COLUMN_NAME_THUMB, DbHelper.FeedSiteNote.COLUMN_NAME_NOTE,
 				DbHelper.FeedSiteNote.COLUMN_NAME_LOCATION, DbHelper.FeedSiteNote.COLUMN_NAME_ROTATION,
 				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_ORIENTATION,
 				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDE, DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDEREF,
 				DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDE, DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDEREF };
 
 		// How you want the results sorted in the resulting Cursor
 		String sortOrder = DbHelper.FeedSiteNote._ID + " DESC";
 
 		Cursor c = db.query(DbHelper.FeedSiteNote.TABLE_NAME, // The table
 				projection, // The columns to return
 				null, // The columns for the WHERE clause
 				null, // The values for the WHERE clause
 				null, // don't group the rows
 				null, // don't filter by row groups
 				sortOrder // The sort order
 				);
 
 		if (c.getCount() > 0) {
 			c.moveToFirst();
 			while (!c.isAfterLast()) {				
 				noteList.add(new DataSet(c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote._ID)), 
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_ID)), 
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_DATE_TIME)), 
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_FILE)), 
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_THUMB)), 
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_NOTE)), 
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_LOCATION)),
 						c.getDouble(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_ROTATION)),
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_ORIENTATION)),
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDE)),
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LATITUDEREF)),
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDE)),
 						c.getString(c.getColumnIndexOrThrow(DbHelper.FeedSiteNote.COLUMN_NAME_EXIF_GPS_LONGITUDEREF))						
 						));
 				c.moveToNext();
 			}
 		}
 
 	}
 }
