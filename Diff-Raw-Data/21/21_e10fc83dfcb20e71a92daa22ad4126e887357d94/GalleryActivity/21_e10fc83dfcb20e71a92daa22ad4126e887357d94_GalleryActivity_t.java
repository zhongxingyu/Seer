 package edu.illinois.geosight;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 import edu.illinois.geosight.servercom.GeosightEntity;
 import edu.illinois.geosight.servercom.User;
 
 /**
  * 
  * GalleryActivity class to view geotagged photos stored on local device.
  * 
  * @author Yo Han
  * 
  */
 public class GalleryActivity extends Activity implements OnClickListener {
 
 	private ImageAdapter mImgAdapter;
 	private LoadImagesFromSDCard loadImagesTask = new LoadImagesFromSDCard();
 	private UploadImageTask uploadTask = new UploadImageTask();;
 	private ArrayList<String> imgPaths = new ArrayList<String>();
 
 	private Gallery mGallery;
 	private ImageView mImg;
 	private ProgressBar mProgress;
 	private Button mUploadButton;
 	private Button mCancelButton;
 	private String currentImgPath;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gallery);
 		setView();
 
 		mGallery.setOnItemClickListener(new OnItemClickListener() {
 			Bitmap bm = null;
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				if (bm != null)
 					bm.recycle();
 				currentImgPath = imgPaths.get(position);
 				try {
 					bm = new BitmapScaler(new File(currentImgPath), 500).getScaled();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				mImg.setImageBitmap(bm);
 				mUploadButton.setEnabled(true);
 			}
 		});
 		loadImages();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		loadImagesTask.cancel(true);
 		uploadTask.cancel(true);
 	};
 
 	@Override
 	public void onClick(View v) {
 		int id = v.getId();
 		if (id == R.id.galleryUploadButton) {
 			if( User.current != null ){
 				mProgress.setVisibility(View.VISIBLE);
 				mUploadButton.setEnabled(false);
 				uploadTask.execute(new File(currentImgPath));
 				uploadTask = new UploadImageTask(); // for subsequent upload
 			} else {
 				LoginDialog.show(this);
 			}
 		} else if (id == R.id.galleryCancelButton) {
 			uploadTask.cancel(true);
 			finish();
 		}
 	}
 
 	/**
 	 * Set up components for view
 	 */
 	private void setView() {
 		mGallery = (Gallery) findViewById(R.id.gallery);
 		mImg = (ImageView) findViewById(R.id.galleryImage);
 		mProgress = (ProgressBar) findViewById(R.id.galleryUploadProgress);
 		mUploadButton = (Button) findViewById(R.id.galleryUploadButton);
 		mCancelButton = (Button) findViewById(R.id.galleryCancelButton);
 
 		mImgAdapter = new ImageAdapter(getApplicationContext());
 		mGallery.setAdapter(mImgAdapter);
 
 		mUploadButton.setOnClickListener(this);
 		mCancelButton.setOnClickListener(this);
 	}
 
 	/**
 	 * Load images.
 	 */
 	private void loadImages() {
 		final Object data = getLastNonConfigurationInstance();
 		if (data == null) {
 			loadImagesTask.execute();
 		} else {
 			final Bitmap[] photos = (Bitmap[]) data;
 			if (photos.length == 0) {
 				loadImagesTask.execute();
 			}
 			for (Bitmap photo : photos) {
 				addImage(photo);
 			}
 		}
 	}
 
 	/**
 	 * Add image(s) to the grid view adapter.
 	 * 
 	 * @param value
 	 *            Array of LoadedImages references
 	 */
 	private void addImage(Bitmap... value) {
 		for (Bitmap image : value) {
 			mImgAdapter.addPhoto(image);
 			mImgAdapter.notifyDataSetChanged(); // display images
 		}
 	}
 
 	/**
 	 * Adapter for image files.
 	 */
 	class ImageAdapter extends BaseAdapter {
 
 		private Context mContext;
 		private ArrayList<Bitmap> photos = new ArrayList<Bitmap>();
 
 		public ImageAdapter(Context context) {
 			mContext = context;
 		}
 
 		public void addPhoto(Bitmap photo) {
 			photos.add(photo);
 		}
 
 		public int getCount() {
 			return photos.size();
 		}
 
 		public Object getItem(int position) {
 			return photos.get(position);
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final ImageView imgView;
 			if (convertView == null) {
 				imgView = new ImageView(mContext);
 			} else {
 				imgView = (ImageView) convertView;
 			}
 			imgView.setImageBitmap(photos.get(position));
 			imgView.setLayoutParams(new Gallery.LayoutParams(150, 120));
 			imgView.setAdjustViewBounds(true);
 			return imgView;
 		}
 
 		public ArrayList<Bitmap> getPhotos() {
 			return photos;
 		}
 	}
 
 	/**
 	 * Asynchronous task for loading the images from the SD card.
 	 */
 	class LoadImagesFromSDCard extends AsyncTask<Object, Bitmap, Object> {
 
 		private static final int THUMBNAIL_DIMENSION = 70;
 
 		@Override
 		protected Object doInBackground(Object... params) {
 			Bitmap bitmap = null;
 			Bitmap newBitmap = null;
 
 			String[] projection = { MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID };
 
 			// Create the cursor pointing to the SDCard
 			Cursor cursor = managedQuery(
 					MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
 					projection, // Which columns to return
 					null, 		// Return all rows
 					null, 
 					null);
 			
 			int imgIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID);
 			int thumIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
 			int size = cursor.getCount();
 
 			if (size == 0) {
 				Toast.makeText(getApplicationContext(), "No images avaiable", Toast.LENGTH_LONG).show();
 			} else {
 				int imageID = 0, thumbID = 0;
 				for (int i = 0; i < size; i++) {
 					cursor.moveToPosition(i);
 					imageID = cursor.getInt(imgIndex);
 					thumbID = cursor.getInt(thumIndex);
 					try {

						Uri imageUri = getUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageID);
						String imgFilepath = getRealPathFromURI( imageUri );
						
 						if (imgFilepath == null)
 							continue;
 						
						// check if the image is geotagged.
						// don't display thumbnail if it's not.
 						if (isGeotagged(imgFilepath) && isJPEG(imgFilepath)) {
 							
 							Uri thumbURI = getUri( MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, thumbID);
 							InputStream thumbIS = getContentResolver().openInputStream( thumbURI );
 							bitmap = BitmapFactory.decodeStream( thumbIS );
 
 							if (bitmap != null) {
 								newBitmap = Bitmap.createScaledBitmap(bitmap,
 										THUMBNAIL_DIMENSION,
 										THUMBNAIL_DIMENSION, true);
 								bitmap.recycle();
 								if (newBitmap != null) {
 									imgPaths.add(imgFilepath);
 									publishProgress(newBitmap);
 								}
 							}
 						}
 					} catch (IOException e) {
 						// ignore missing photos
 					}
 				}
 			}
 			cursor.close();
 			return null;
 		}
 
 		@Override
 		public void onProgressUpdate(Bitmap... value) {
 			addImage(value);
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			setProgressBarIndeterminateVisibility(false);
 		}
 
 		private Uri getUri(Uri type, int imageID) {
 			return Uri.withAppendedPath(type, "" + imageID);
 		}
 
 	}
 
 	/**
 	 * Asynchronous task for loading the images from the SD card.
 	 */
 	private class UploadImageTask extends AsyncTask<File, Double, Object> {
 
 		@Override
 		protected Object doInBackground(File... files) {
 			GeosightEntity.uploadImage(files[0], new ProgressCallback() {
 				@Override
 				public void onProgress(double progress) {
 					publishProgress(progress * 100);
 				}
 			});
 
 			return null;
 		}
 
 		@Override
 		public void onProgressUpdate(Double... progress) {
 			for (int i = 0; i < progress.length; i++) {
 				mProgress.setProgress(progress[i].intValue());
 			}
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			GalleryActivity.this.finish();
 			Toast.makeText(GalleryActivity.this, "Upload Complete", Toast.LENGTH_LONG).show();
 			mProgress.setVisibility(View.GONE);
 		}
 	}
 
 	/**
 	 * Gets the filepath from uri
 	 * 
 	 * @param contentUri
 	 *            target path
 	 * @return path corresponding to uri; null if uri is invalid
 	 */
 	public String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		String rv = null;
 		Cursor cursor = managedQuery(contentUri, proj, // Which columns
 				null, // WHERE clause; which rows to return (all rows)
 				null, // WHERE clause selection arguments (none)
 				null); // Order-by clause (ascending by name)
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		try {
 			rv = cursor.getString(column_index);
 		} catch (Exception e) {
 			return null;
 		}
 		return rv;
 	}
 
 	/**
 	 * Check if given image on filepath is geotagged.
 	 * 
 	 * @param filepath
 	 *            path to the file
 	 * @return true if it contains both latitude and longitude. false otherwise
 	 * @throws IOException
 	 *             thrown when file doens't exist at filepath
 	 */
 	private boolean isGeotagged(String filepath) throws IOException {
 		if (filepath != null) {
 			ExifInterface e = new ExifInterface(filepath);
 			return (e.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null) &&
 					(e.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) != null);
 		}
 		return false;
 	}
 
 	private boolean isJPEG(String filepath) {
 		String filenameArray[] = filepath.split("\\.");
 		String extension = filenameArray[filenameArray.length - 1];
 		return (extension.equalsIgnoreCase("JPEG") || extension.equalsIgnoreCase("JPG"));
 	}
 
 }
