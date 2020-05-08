 package com.ece.smartGallery.activity;
 
 import java.io.File;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 
 import com.ece.smartGallery.R;
 import com.ece.smartGallery.DBLayout.Album;
 import com.ece.smartGallery.DBLayout.Photo;
 import com.ece.smartGallery.activity.bluetooth.BluetoothChat;
 import com.ece.smartGallery.activity.fb.FBActivity;
 import com.ece.smartGallery.adapter.HomeGridAdapter;
 import com.ece.smartGallery.entities.DatabaseHandler;
 
 public class HomeActivity extends Activity {
 	private LinearLayout addPhoto;
 	private GridView gridView;
 	private int albumId;
 	private final String TAG = this.getClass().getName();
 	private List<Photo> photoList;
 	DatabaseHandler db;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 		Log.d(TAG, "on Create");
 		this.addPhoto = (LinearLayout) findViewById(R.id.add_new_photo);
 		this.addPhoto.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				addNewPhoto();
 			}
 		});
 		db = new DatabaseHandler(getApplicationContext());
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		Intent intent = this.getIntent();
 		if (intent != null) {
 			albumId = intent.getIntExtra(Album.ALBUM, 0);
 		}
 		loadPhoto();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		Intent intent;
 		switch (item.getItemId()) {
 		case R.id.action_share_via_fb:
 			intent = new Intent(this, FBActivity.class);
 			Album a = db.getAllAlbums().get(0);
 			Photo p = db.getPhoto(a, a.getCount() - 1);
 			intent.putExtra(Photo.PHOTO, p.getImage());
 			startActivity(intent);
 			return true;
 		case R.id.action_share_via_nfc:
 			intent = new Intent(this, BeamActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.action_share_via_bluetooth:
 			intent = new Intent(this, BluetoothActivity.class);
 			startActivity(intent);
 			return true;
 		case R.id.action_bluetooth:
         	intent = new Intent(this, BluetoothChat.class);
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void addNewPhoto() {
 		Photo p = new Photo();
 		File path = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
 		File sample = new File(path, "1.jpg");
 		p.setImage(Uri.fromFile(sample));
 		p.setLocation("Pittsburgh");
 		p.setTimeStamp(System.currentTimeMillis());
 		Album album = db.getAlbum(albumId);
 		boolean success = db.addPhoto(album, p);
 		if (success) {
 			Log.d(TAG, "add new photo success!");
 		}
 		this.loadPhoto();
 //		Intent intent = new Intent(this, EditActivity.class);
 //		intent.setAction(Intent.ACTION_INSERT);
 //		intent.putExtra(Album.ALBUM, album.getId());
 //		startActivity(intent);
 	}
 
 	public void loadPhoto() {
 		photoList = db.getAllPhotos(albumId);
 		Log.d(TAG, "Album retrieved successfully, length = " + photoList.size());
 		gridView = (GridView) findViewById(R.id.gallery_list);
 		HomeGridAdapter adapter = new HomeGridAdapter(this, this.photoList);
 		gridView.setAdapter(adapter);
 		Log.d(TAG, "grid view adapter set");
 	}
 
 	// this method is used to go to edit page directly to test more easily
 	// will be removed once integrate all parts together.
 	public void test_edit(View view) {
 		Intent intent = new Intent(this, EditActivity.class);
 		startActivity(intent);
 	}
 
 }
