 package com.sa.activity;
 
 import java.util.LinkedList;
 
 import com.sa.db.bean.Photo;
 import com.sa.smartalbum.R;
 
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.ImageView;
 
 public class MainActivity extends BaseActivity {
 
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	public static final int RESULT_DELETE = 17;
 
 	// private Integer[] mThumbIds = { R.drawable.ic_launcher };
 	private Button button;
 	private GridView gridView;
 	private LinkedList<Photo> photos = new LinkedList<Photo>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestLocationUpdates();
 		initializeButton();
 		initGridView();
 		photos.addAll(db.getAllPhotos());
 	}
 
 	/**
 	 * Initializes a button and attach a listener for taking a photo.
 	 */
 	private void initializeButton() {
 		button = (Button) findViewById(R.id.take_photo_button);
 		button.setOnClickListener(new Button.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startTakePhotoActivity();
 			}
 		});
 	}
 
 	/**
 	 * Creates an intent to take a photo using Camera API.
 	 */
 	public void startTakePhotoActivity() {
 		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		// takePhotoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
 		// takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new
 		// File(filename)));
 
 		// launch camera intent with photo id
 		startActivityForResult(takePhotoIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	/**
 	 * Initializes a thumbnail grid view.
 	 */
 	private void initGridView() {
 		gridView = (GridView) findViewById(R.id.gridview);
 		gridView.setAdapter(new ImageAdapter(this));
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				startViewPhotoActivity(position);
 			}
 		});
 	}
 
 	/**
 	 * Creates an intent to view photo in detail.
 	 */
 	public void startViewPhotoActivity(int position) {
 		Intent viewPhotoIntent = new Intent(MainActivity.this, DetailActivity.class);
 		viewPhotoIntent.putExtra("position", position);
 		viewPhotoIntent.putExtra("id", photos.get(position).getId());
 
		startActivity(viewPhotoIntent);
 	}
 
 	/**
 	 * Callback for when activity result is returned
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_DELETE) {
			photos.remove(data.getIntExtra("position", -1));
			updateGrid();
 		}
 		else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				createPhoto((Bitmap) data.getExtras().get("data"));
 			}
 			else if (resultCode == RESULT_CANCELED) {
 				// User cancelled the image capture
 				createPhoto(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
 			}
 			else {
 				// Image capture failed, advise user
 				makeToast("Oops. Something went wrong. Failed to save a photo.\n");
 			}
 		}
 	}
 
 	private void createPhoto(Bitmap bitmap) {
 		Photo p = new Photo();
 		p.setBitmap(bitmap);
 		p.setLocation(lastLocation);
 
 		if (savePhoto(p)) {
 			makeToast("Image saved");
 			photos.push(p);
 			updateGrid();
 		}
 		else {
 			makeToast("Oops. Something went wrong. Failed to save a photo.\n");
 		}
 	}
 
 	/**
 	 * This class loads the image gallery in grid view.
 	 */
 	public class ImageAdapter extends BaseAdapter {
 
 		private Context mContext;
 
 		public ImageAdapter(Context c) {
 			mContext = c;
 		}
 
 		@Override
 		public int getCount() {
 			// return mThumbIds.length;
 			return photos.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ImageView imageView;
 			if (convertView == null) {
 				// if it's not recycled, initialize some attributes
 				imageView = new ImageView(mContext);
 				imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
 				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 				imageView.setPadding(4, 60, 4, 60);
 			}
 			else {
 				imageView = (ImageView) convertView;
 			}
 
 			Photo p = photos.get(position);
 			imageView.setImageBitmap(p.getBitmap());
 
 			return imageView;
 		}
 	}
 
 	public void updateGrid() {
 		gridView.invalidateViews();
 		gridView.setAdapter(new ImageAdapter(this));
 	}
 
 	@Override
 	int getMenuId() {
 		return R.menu.main;
 	}
 
 	@Override
 	int getLayoutId() {
 		return R.layout.activity_main;
 	}
 }
