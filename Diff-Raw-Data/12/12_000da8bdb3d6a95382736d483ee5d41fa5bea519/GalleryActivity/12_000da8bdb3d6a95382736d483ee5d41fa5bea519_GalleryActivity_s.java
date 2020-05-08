 package com.androidmontreal.weddingvideoguestbook;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.GridView;
 
 import com.androidmontreal.weddingvideoguestbook.db.DBItem;
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class GalleryActivity extends Activity {
 	public String TAG = PrivateConstants.TAG;
 	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST;
 	private static final int NEW_SESSION_ID = Menu.FIRST + 1;
 
 	private DatumsDbAdapter mDbHelper;
 
 	GridView gridView;
 
	ArrayList<DBItem> galleryItems = new ArrayList<DBItem>();
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gallery);
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		// Query for all videos on external storage
 		// TODO instead open all videos in the app's folder? or use the database
 		// to know the filenames
 		// TODO this wasnt showing the video we just recorded (onResume woudl
 		// improve this?)
 
 		ContentResolver cr = getContentResolver();
 		String[] proj = { BaseColumns._ID, MediaStore.Video.Media.DATA,
 				MediaStore.Video.VideoColumns.TITLE,
 				MediaStore.Video.VideoColumns._ID };
 
 		Cursor c = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
 				null, null, null);
 		if (c.moveToFirst()) {
 			do {
 				int id = c.getInt(0);
 				int videoTitleIndex = c
 						.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE);
 				String videoTitle = c.getString(videoTitleIndex);
 				String[] videoTitleParts = videoTitle.split("[.]");
 				String[] videoTitleSubParts = videoTitleParts[0].split("_");
 
 				if (videoTitleSubParts[0].equals(PrivateConstants.DATA_KEYWORD)) {
 					// Get SQL session id (row id)
 					Long rowID;
 					try {
 						rowID = Long.parseLong(videoTitleSubParts[3]);
 					} catch (Exception e) {
 						Log.v(TAG, "Found a malformed video file. "
 								+ videoTitle);
 						continue;
 					}
 					// Test to make sure that a session file exists for the
 					// video file
 					mDbHelper = new DatumsDbAdapter(this);
 					mDbHelper.open();
 					Cursor note = mDbHelper.fetchNote(rowID);
 					mDbHelper.close();
 					String testString;
 					try {
 						testString = note
 								.getString(note
 										.getColumnIndexOrThrow(DatumsDbAdapter.KEY_FIELD1));
 					} catch (Exception e) {
 						// No session file
 						continue;
 					}
 
 					String videoPath = c
 							.getString(c
 									.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
 					DBItem item = new DBItem(videoPath, rowID, videoTitle);
 					if (galleryItems.indexOf(item) == -1) {
 						galleryItems.add(item);
 					}
 				}
 			} while (c.moveToNext());
 		}
 		c.close();
 
 		// Test to see if there are any videos and display welcome screen if not
 		int numberOfThumbnails = galleryItems.size();
 		if (numberOfThumbnails == 0) {
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 			alert.setTitle(R.string.notification);
 			alert.setMessage(R.string.dialog_welcome);
 
 			alert.setPositiveButton(R.string.ok,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog,
 								int whichButton) {
 							dialog.cancel();
 						}
 					});
 			AlertDialog alertDialog = alert.create();
 			alertDialog.show();
 		}
 
 		gridView = (GridView) findViewById(R.id.galleryGridView);
 
 		gridView.setAdapter(new GalleryImageAdapter(this, galleryItems));
 
 		// Get device details
 		DeviceDetails mDeviceDetails = new DeviceDetails(this, true, TAG);
 		Log.v(TAG, mDeviceDetails.getCurrentDeviceDetails());
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.mainmenu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.action_new_video:
 			createNote();
 			break;
 
 		default:
 			break;
 		}
 
 		// return super.onMenuItemSelected(featureId, item);
 		return true;
 
 	}
 
 	public void showSessionList() {
 		Intent i = new Intent(this, SessionListView.class);
 		startActivity(i);
 	}
 
 	private void createNote() {
 		mDbHelper = new DatumsDbAdapter(this);
 		mDbHelper.open();
 		Long id = mDbHelper.createNote("", "", "", "", "", "");
 		mDbHelper.close();
 		Intent i = new Intent(this, SessionAccess.class);
 		i.putExtra(DatumsDbAdapter.KEY_ROWID, id);
 		startActivity(i);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(this); // Add this method.
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().activityStop(this); // Add this method.
 	}
 }
