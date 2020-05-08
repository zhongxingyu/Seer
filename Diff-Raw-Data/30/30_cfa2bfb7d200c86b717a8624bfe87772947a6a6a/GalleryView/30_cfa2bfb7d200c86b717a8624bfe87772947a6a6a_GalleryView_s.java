 package org.lingsync.elicitation.collection;
 
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
 
 				if (videoTitleSubParts[0].equals("fielddb")) {
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
 
		//Test to see if there are any videos and display welcome screen if not
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
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, SESSION_LIST_VIEW_ID, 0, R.string.menu_view_session_list);
 		menu.add(0, NEW_SESSION_ID, 0, R.string.menu_new_session);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case SESSION_LIST_VIEW_ID:
 			showSessionList();
 			return true;
 		case NEW_SESSION_ID:
 			createNote();
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
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
