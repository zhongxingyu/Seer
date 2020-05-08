 /* Source: This class is based on the sample code (only modified to suit our app) at
  * http://developer.android.com/training/camera/photobasics.html. Copy of license at
  * http://www.apache.org/licenses/LICENSE-2.0*/
 package uw.changecapstone.tweakthetweet;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class PhotoActivity extends Activity {
 
 	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
 	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
 	private ImageView mImageView;
 	private Bitmap mImageBitmap;
 	private String mCurrentPhotoPath;
 	private Intent mIntent;
 	private static final String JPEG_FILE_PREFIX = "IMG_";
 	private static final String JPEG_FILE_SUFFIX = ".jpg";	
 	final static String PHOTO_PATH = "PHOTO_PATH";
 	private static int TAKE_PICTURE = 0;
 	private static int CHOOSE_PICTURE =1;
 	private static boolean hasPhoto = false;
 	final static String HAS_PHOTO = "HAS_PHOTO";
 	private final static int PHOTO_OK = 6;
 	/* Photo album for this application */
 	private String getAlbumName() {
 		return getString(R.string.album_name);
 	}
 	
 	private File getAlbumDir() {
 		File storageDir = null;
 
 		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 			
 			storageDir = new File( Environment.getExternalStoragePublicDirectory
 					(Environment.DIRECTORY_PICTURES), 
 				    getAlbumName()
 				);
 
 			if (storageDir != null) {
 				if (! storageDir.mkdirs()) {
 					if (! storageDir.exists()){
 						Log.d("Camera Pictures", "failed to create directory");
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
 
 	private void setPic() {
 
 		/* There isn't enough memory to open up more than a couple camera photos */
 		/* So pre-scale the target bitmap into which the file is decoded */
 
 		/* Get the size of the ImageView */
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
 			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
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
 
 	private void dispatchTakePictureIntent() {
 
 		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
 		startActivityForResult(takePictureIntent, TAKE_PICTURE);
 	}
 
 	private void handleBigCameraPhoto() {
 
 		if (mCurrentPhotoPath != null) {
 			setPic();
 			galleryAddPic();
 			//mCurrentPhotoPath = null;
 		}
 	}
 
 	Button.OnClickListener mTakePicOnClickListener = 
 		new Button.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			dispatchTakePictureIntent();
 		}
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_photo);
 		mIntent = getIntent();
 		mImageView = (ImageView) findViewById(R.id.imageView1);
 		mImageBitmap = null;
 
 		Button picBtn = (Button) findViewById(R.id.btnTakePic);
 		setBtnListenerOrDisable( 
 				picBtn, 
 				mTakePicOnClickListener,
 				MediaStore.ACTION_IMAGE_CAPTURE
 		);
 		Button loadBtn = (Button) findViewById(R.id.btnChoosePic);
 		loadBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent i = new Intent (
 						Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				startActivityForResult(i, CHOOSE_PICTURE);
 			}
 		});
 	}
 	/* note: used mCurrentPhotoPath for intent instead of 
      * String photoPath = data.get("PHOTO_PATH"); because the later gives null.
      * also moved mCurrentPhotoPath = null; here because
      * if done in handleBigCameraPhoto intent will have null value*/
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
 			Toast.makeText(this, "Image saved to:\n" +
 	                mCurrentPhotoPath, Toast.LENGTH_LONG).show();
 			handleBigCameraPhoto();
 			hasPhoto = true;			
 			//Intent intent = new Intent(this, TestStringBuilderConfirm.class);
 			mIntent.putExtra(HAS_PHOTO, hasPhoto);
 			mIntent.putExtra(PHOTO_PATH, mCurrentPhotoPath);
 						
 			if (mCurrentPhotoPath != null)
 				mCurrentPhotoPath = null;
 			setResult(RESULT_OK, mIntent);
 			finish();
 		}
 		else if (requestCode == CHOOSE_PICTURE && resultCode == RESULT_OK){
 			Uri selectedImageUri = data.getData();
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
             
             Cursor cursor = getContentResolver().query(selectedImageUri,
                     filePathColumn, null, null, null);
             cursor.moveToFirst();
  
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             String picturePath = cursor.getString(columnIndex);
             cursor.close();
             mImageView.setImageURI(selectedImageUri);
             System.out.println("Image Path: "+picturePath);
             mImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
             hasPhoto = true;
            //Intent intent = new Intent(this, TestStringBuilderConfirm.class);
             mIntent.putExtra(HAS_PHOTO, hasPhoto);
 			mIntent.putExtra(PHOTO_PATH, mCurrentPhotoPath);
 			setResult(RESULT_OK, mIntent);
 			finish();
 		}
 	}
 
 	// Some lifecycle callbacks so that the image can survive orientation change
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
 		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
 		mImageView.setImageBitmap(mImageBitmap);
 		mImageView.setVisibility(
 				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
 						ImageView.VISIBLE : ImageView.INVISIBLE
 		);
 	}
 
 	/**
 	 * Indicates whether the specified action can be used as an intent. This
 	 * method queries the package manager for installed packages that can
 	 * respond to an intent with the specified action. If no suitable package is
 	 * found, this method returns false.
 	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
 	 *
 	 * @param context The application's environment.
 	 * @param action The Intent action to check for availability.
 	 *
 	 * @return True if an Intent with the specified action can be sent and
 	 *         responded to, false otherwise.
 	 */
 	public static boolean isIntentAvailable(Context context, String action) {
 		final PackageManager packageManager = context.getPackageManager();
 		final Intent intent = new Intent(action);
 		List<ResolveInfo> list =
 			packageManager.queryIntentActivities(intent,
 					PackageManager.MATCH_DEFAULT_ONLY);
 		return list.size() > 0;
 	}
 
 	private void setBtnListenerOrDisable( 
 			Button btn, 
 			Button.OnClickListener onClickListener,
 			String intentName
 	) {
 		if (isIntentAvailable(this, intentName)) {
 			btn.setOnClickListener(onClickListener);        	
 		} else {
 			btn.setText( 
 				getText(R.string.cannot).toString() + " " + btn.getText());
 			btn.setClickable(false);
 		}
 	}
 
 }
