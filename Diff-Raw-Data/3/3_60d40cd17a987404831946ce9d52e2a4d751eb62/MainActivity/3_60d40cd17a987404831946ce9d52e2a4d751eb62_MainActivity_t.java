 package mdiss.umappin.ui;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.mapsforge.android.maps.MapActivity;
 
 import mdiss.umappin.R;
 import mdiss.umappin.adapters.LateralMenuAdapter;
 import mdiss.umappin.asynctasks.DiscussionHeadersAsyncTask;
 import mdiss.umappin.asynctasks.GetNewsAsyncTask;
 import mdiss.umappin.asynctasks.RoutesAsyncTask;
 import mdiss.umappin.asynctasks.profile.ProfileAsyncTask;
 import mdiss.umappin.entities.LateralMenuItem;
 import mdiss.umappin.fragments.MapFragment;
 import mdiss.umappin.fragments.PictureFragment;
 import mdiss.umappin.utils.AlbumStorageDirFactory;
 import mdiss.umappin.utils.BaseAlbumDirFactory;
 import mdiss.umappin.utils.Constants;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.FragmentManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 public class MainActivity extends MapActivity {
 
 	public static final int ACTION_TAKE_PHOTO_B = 1;
 	private static final int ACTION_TAKE_PHOTO_S = 2;
 
 	private ImageView mImageView;
 	private Bitmap mImageBitmap;
 
 	private String mCurrentPhotoPath;
 
 	private static final String JPEG_FILE_PREFIX = "IMG_";
 	private static final String JPEG_FILE_SUFFIX = ".jpg";
 
 	private AlbumStorageDirFactory mAlbumStorageDirFactory = new BaseAlbumDirFactory();
 
 	private DrawerLayout mDrawerLayout;
 	private ListView mDrawerList;
 	private ActionBarDrawerToggle mDrawerToggle;
 	// private String[] menuOptions = { "Timeline", "Profile", "Messages",
 	// "Map", "Routes", "Games", "Take a photo" };
 
 	private LateralMenuItem[] menuOptions = new LateralMenuItem[] {
 			new LateralMenuItem("Timeline", R.drawable.ic_menu_friendslist),
 			new LateralMenuItem("Profile", R.drawable.ic_contact_picture),
 			new LateralMenuItem("Messages", R.drawable.sym_action_email),
 			new LateralMenuItem("Map", R.drawable.ic_menu_mapmode),
 			new LateralMenuItem("Routes", R.drawable.ic_menu_mylocation),
 			new LateralMenuItem("Take a photo", R.drawable.ic_menu_camera) };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		new GetNewsAsyncTask(this).execute();
 		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
 		mDrawerList = (ListView) findViewById(R.id.left_drawer);
 
 		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
 		// mDrawerList.setAdapter(new ArrayAdapter<String>(this,
 		// R.layout.row_drawer_menu, menuOptions));
 		mDrawerList.setAdapter(new LateralMenuAdapter(this, R.layout.row_drawer_menu, menuOptions));
 
 		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
 
 		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
 		mDrawerLayout, /* DrawerLayout object */
 		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
 		R.string.open_drawer, /* "open drawer" description for accessibility */
 		R.string.close_drawer /* "close drawer" description for accessibility */
 		) {
 			public void onDrawerClosed(View view) {
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 
 			public void onDrawerOpened(View drawerView) {
 				invalidateOptionsMenu(); // creates call to
 											// onPrepareOptionsMenu()
 			}
 		};
 
 		mDrawerLayout.setDrawerListener(mDrawerToggle);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getActionBar().setHomeButtonEnabled(true);
 	}
 
 	@Override
 	protected void onPause() {
 		System.gc();
 		// MapView map = new MapView(this, new MapnikTileDownloader());
 		// If there is no map created it crashes trying to destroy maps
 		super.onPause();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.action_logout) {
 			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
 			alertDialog.setTitle(getString(R.string.logout_title));
 			alertDialog.setMessage(getString(R.string.logout_message));
 			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_positive),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int arg1) {
 							clearPrefs();
 							dialog.dismiss();
 							finish();
 						}
 					});
 			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.button_negative),
 					new DialogInterface.OnClickListener() {
 
 						@Override
 						public void onClick(DialogInterface dialog, int arg1) {
 							dialog.dismiss();
 						}
 					});
 			alertDialog.show();
 			return true;
 		} else if (mDrawerToggle.onOptionsItemSelected(item)) {
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/* The click listner for ListView in the navigation drawer */
 	private class DrawerItemClickListener implements ListView.OnItemClickListener {
 		@Override
 		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			switch (position) {
 			case 0:// Timeline
 				getActionBar().setTitle("Timeline");
 				new GetNewsAsyncTask(MainActivity.this).execute();
 				cleanBackStack();
 				break;
 			case 1:// Profile
 				setTitle("Profile");
 				new ProfileAsyncTask(MainActivity.this).execute();
 				cleanBackStack();
 				break;
 			case 2:// Messages
 				setTitle("Messages");
 				new DiscussionHeadersAsyncTask(MainActivity.this).execute();
 				cleanBackStack();
 				break;
 			case 3:// Map
				cleanBackStack();
 				setTitle("OpenStreetMap");
 				MapFragment fragment = new MapFragment();
 				getFragmentManager().beginTransaction().addToBackStack("map").replace(R.id.content_frame, fragment)
 						.commit();
 				break;
 			case 4:
 				setTitle("My routes");
 				new RoutesAsyncTask(MainActivity.this).execute("");
 				cleanBackStack();
 				break;
 			default:// Take a photo
 				setTitle("Take a photo");
 				PictureFragment fragmentPicture = new PictureFragment();
 				getFragmentManager().beginTransaction().addToBackStack("photo")
 						.replace(R.id.content_frame, fragmentPicture).commit();
 				dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
 			}
 			mDrawerLayout.closeDrawer(mDrawerList);
 		}
 	}
 
 	private void clearPrefs() {
 		SharedPreferences prefs = getSharedPreferences(Constants.prefsName, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.clear();
 		editor.commit();
 		Log.i(Constants.logPrefs, "preferences cleared");
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		// Sync the toggle state after onRestoreInstanceState has occurred.
 		mDrawerToggle.syncState();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		// Pass any configuration change to the drawer toggls
 		mDrawerToggle.onConfigurationChanged(newConfig);
 	}
 
 	private void handleSmallCameraPhoto(Intent intent) {
 		Bundle extras = intent.getExtras();
 		mImageBitmap = (Bitmap) extras.get("data");
 		mImageView.setImageBitmap(mImageBitmap);
 		mImageView.setVisibility(View.VISIBLE);
 	}
 
 	private void handleBigCameraPhoto() {
 
 		if (mCurrentPhotoPath != null) {
 			setPic();
 			galleryAddPic();
 			mCurrentPhotoPath = null;
 		}
 
 	}
 
 	private String getAlbumName() {
 		return getString(R.string.album_name);
 	}
 
 	private File getAlbumDir() {
 		File storageDir = null;
 
 		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 
 			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
 
 			if (storageDir != null) {
 				if (!storageDir.mkdirs()) {
 					if (!storageDir.exists()) {
 						Log.d("CameraSample", "failed to create directory");
 						return null;
 					}
 				}
 			}
 
 		} else {
 			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
 		}
 
 		return storageDir;
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	private File createImageFile() throws IOException {
 		// Create an image file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
 		File albumF = getAlbumDir();
 		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
 		return imageF;
 	}
 
 	private File setUpPhotoFile() throws IOException {
 
 		File f = createImageFile();
 		mCurrentPhotoPath = f.getAbsolutePath();
 
 		return f;
 	}
 
 	Button.OnClickListener mTakePicOnClickListener = new Button.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
 		}
 	};
 
 	Button.OnClickListener mTakePicSOnClickListener = new Button.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			dispatchTakePictureIntent(ACTION_TAKE_PHOTO_S);
 		}
 	};
 
 	public void dispatchTakePictureIntent(int actionCode) {
 		System.gc();
 		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		switch (actionCode) {
 		case ACTION_TAKE_PHOTO_B:
 			File f = null;
 
 			try {
 				f = setUpPhotoFile();
 				mCurrentPhotoPath = f.getAbsolutePath();
 				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
 			} catch (IOException e) {
 				e.printStackTrace();
 				f = null;
 				mCurrentPhotoPath = null;
 			}
 			break;
 
 		default:
 			break;
 		} // switch
 
 		startActivityForResult(takePictureIntent, actionCode);
 	}
 
 	private void setPic() {
 
 		/* There isn't enough memory to open up more than a couple camera photos */
 		/* So pre-scale the target bitmap into which the file is decoded */
 
 		/* Get the size of the ImageView */
 		mImageView = (ImageView) this.findViewById(R.id.current_picture);
 		int targetW = mImageView.getWidth();
 		int targetH = mImageView.getHeight();
 
 		/* Get the size of the image */
 		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
 		bmOptions.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
 		int photoW = bmOptions.outWidth;
 		int photoH = bmOptions.outHeight;
 
 		/* Figure out which way needs to be reduced less */
 		int scaleFactor = 1;
 		if ((targetW > 0) || (targetH > 0)) {
 			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
 		}
 
 		/* Set bitmap options to scale the image decode target */
 		bmOptions.inJustDecodeBounds = false;
 		bmOptions.inSampleSize = scaleFactor;
 		bmOptions.inPurgeable = true;
 
 		/* Decode the JPEG file into a Bitmap */
 		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
 
 		/* Associate the Bitmap to the ImageView */
 		mImageView.setImageBitmap(bitmap);
 		mImageView.setVisibility(View.VISIBLE);
 	}
 
 	private void galleryAddPic() {
 		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
 		File f = new File(mCurrentPhotoPath);
 		Uri contentUri = Uri.fromFile(f);
 		mediaScanIntent.setData(contentUri);
 		this.sendBroadcast(mediaScanIntent);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ACTION_TAKE_PHOTO_B: {
 			if (resultCode == RESULT_OK) {
 				handleBigCameraPhoto();
 			} else {
 				cleanBackStack();
 				new GetNewsAsyncTask(this).execute();
 			}
 			break;
 		} // ACTION_TAKE_PHOTO_B
 
 		case ACTION_TAKE_PHOTO_S: {
 			if (resultCode == RESULT_OK) {
 				handleSmallCameraPhoto(data);
 			} else {
 				
 			}
 			break;
 		} // ACTION_TAKE_PHOTO_S
 		} // switch
 	}
 
 	@Override
 	public void onBackPressed() {
 		AlertDialog dialog = new AlertDialog.Builder(this).create();
 		dialog.setTitle(getString(R.string.title_exit_dialog));
 		dialog.setMessage(getString(R.string.message_exit_dialog));
 		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.button_positive),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int arg1) {
 						dialog.dismiss();
 						MainActivity.this.finish();
 					}
 				});
 		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.button_negative),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int arg1) {
 						dialog.dismiss();
 					}
 				});
 		if (getFragmentManager().getBackStackEntryCount() <= 1 && this.getTitle().equals("Timeline")) {
 			dialog.show();
 		} else if (getFragmentManager().getBackStackEntryCount() <= 1 && !this.getTitle().equals("Timeline")) {
 			cleanBackStack();
 			new GetNewsAsyncTask(this).execute();
 		} else {
 			super.onBackPressed();
 		}
 	}
 
 	public void cleanBackStack() {
 		getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
 	}
 }
