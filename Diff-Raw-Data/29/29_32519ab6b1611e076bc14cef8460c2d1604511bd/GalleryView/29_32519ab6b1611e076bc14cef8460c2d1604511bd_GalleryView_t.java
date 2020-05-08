 package com.androidmontreal.weddingvideoguestbook;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.GridView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class GalleryView extends Activity {
 	public String TAG = PrivateConstants.TAG;
 	private static final int SESSION_LIST_VIEW_ID = Menu.FIRST;
 	private static final int NEW_SESSION_ID = Menu.FIRST + 1;
 
 	private DatumsDbAdapter mDbHelper;
 
 	GridView gridView;
 
 	ArrayList<Bitmap> galleryImages = new ArrayList<Bitmap>();
 	ArrayList<Long> galleryRowIds = new ArrayList<Long>();
 	ArrayList<String> galleryFileNames = new ArrayList<String>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_gallery);
 
 		// Query for all videos on external storage
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
 					Bitmap b;
 					try {
 						rowID = Long.parseLong(videoTitleSubParts[3]);
 						b = MediaStore.Video.Thumbnails.getThumbnail(cr, id,
 								MediaStore.Video.Thumbnails.MINI_KIND, null);
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
 					if (b == null) {
 						Log.v(TAG, "NO THUMBNAIL AVAILABLE!");
 						continue;
 					} else {
 						galleryImages.add(b);
 						galleryRowIds.add(rowID);
 						galleryFileNames.add(videoTitle); 
 					}
 				}
 			} while (c.moveToNext());
 		}
 		c.close();
 
 		// Test to see if there are any videos and display welcome screen if not
 		int numberOfThumbnails = galleryImages.size();
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
 
 		gridView.setAdapter(new GalleryImageAdapter(this, galleryImages,
 				galleryRowIds, galleryFileNames));
 
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
