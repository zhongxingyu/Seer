 public class ImageSnippets {
 
 
 	/**
 	 * Launch the picture libraries. Overwrite onActivityResult() to get the callback with the code CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE
 	 * @param activity
 	 */
 	public static void takePhotoFromLibrary (Activity activity) {
 		// create Intent to take a picture and return control to the calling application
 		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 
 		// start the image capture Intent
 		activity.startActivityForResult(intent, CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	
 	/**
 	 * Launch the camera activity. Overwrite onActivityResult() to get the callback with the code CHOOSE_IMAGE_ACTIVITY_REQUEST_CODE
 	 * @param activity
	 * @return
 	 */
 	public static String takePhotoFromCamera (Activity activity) {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		File file = getOutputMediaFile(MEDIA_TYPE_IMAGE); // create a file to save the image
 		if (file == null)	{
 			showErrorDialog(activity, "Check your memory card", "It is not possible to save the photo to your external storage. Please check that you have turned off the USB storage option.");
 			return null;
 		}
 		Uri fileUri = Uri.fromFile(file);
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
 		// start the image capture Intent
 		activity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 
 		return file.getAbsolutePath();
 	}
 
 	/** Create a File for saving an image or video */
 	private static File getOutputMediaFile(int type){
 		String state = Environment.getExternalStorageState();
 		if(!state.equals(Environment.MEDIA_MOUNTED)){
 			Log.d ("DEBUG", "media not mounted");
 			return null;
 		}
 		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YOUR_APP_NAME_HERE");
 		// This location works best if you want the created images to be shared
 		// between applications and persist after your app has been uninstalled.
 
 		// Create the storage directory if it does not exist
 		if (! mediaStorageDir.exists()){
 			if (! mediaStorageDir.mkdirs()){
 				Log.d("DEBUG", "failed to create directory");
 				return null;
 			}
 		}
 
 		// Create a media file name
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		File mediaFile;
 
 		if (type == MEDIA_TYPE_IMAGE){
 			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
 		} else {
 			return null;
 		}
 
 		return mediaFile;
 	}
 
 
 	/**
 	 * Get file path given an uri
 	 * @param activity
 	 * @param uri
 	 * @return
 	 */
 	public static String getPath(Activity activity, Uri uri) {
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
 		int column_index = cursor
 		.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 
 	
 	/**
 	 * Get a Bitmap object from the given file path
 	 * @param context
 	 * @param path
 	 * @return
 	 */
 	public static Bitmap getBitmap (Context context, String path) {
 		Bitmap imageBitmap;
 
 		try {
 			FileInputStream fis = new FileInputStream(path);
 
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inJustDecodeBounds = true;
 			BitmapFactory.decodeStream(fis,null,options);
 
 			// The image shouldn't be bigger as the display width, so we'll scale down til this dimension
 			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 			int displayWidth = display.getWidth(); 
 			final int IMAGE_MAX_SIZE=displayWidth;
 
 			//Find the correct scale value. It should be the power of 2.
 			int scale = 1;
 			if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
 				scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(options.outHeight, options.outWidth)) / Math.log(0.5)));
 			}
 			fis.close();
 			fis = new FileInputStream(path);
 
 			BitmapFactory.Options o2 = new BitmapFactory.Options();
 			o2.inSampleSize=scale;
 			o2.inPurgeable=true;
 			imageBitmap = BitmapFactory.decodeStream (fis, null, o2);
 			fis.close();
 
 			return imageBitmap;
 
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 
 
 	/**
 	 * Translate the image header orientation to degrees (for server and for android)
 	 * @param exifOrientation
 	 * @return
 	 */
 	public static float exifOrientationToDegrees (int exifOrientation) {
 		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
 			return 90;
 		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
 			return 180;
 		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
 			return 270;
 		}
 		return 0;
 	}
 
 
 
 	/**
 	 * Get the rotation for the given image path. Android doesn't rotate the image itself
 	 * but the info. is in the image header
 	 * @param path
 	 * @return
 	 */
 	public static float getRotation (String path) {
 		ExifInterface exif;
 		String orientation = null;
 		try {
 			exif = new ExifInterface(path);
 			orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return exifOrientationToDegreesServer(Integer.parseInt(orientation));
 	}
 
 	
 	/**
 	 * Rotate the given image and return a bitmap with the image already rotated
 	 * @param activity
 	 * @param path
 	 * @param imageBitmap
 	 * @return
 	 */
 	public static Bitmap rotatePicture(Activity activity, String path, Bitmap imageBitmap) {
 		ExifInterface exif;
 		String orientation = null;
 		try {
 			exif = new ExifInterface(path);
 			orientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 
 		if (exifOrientationToDegrees(Integer.parseInt(orientation)) != 0) {
 			try {
 				Matrix matrix = new Matrix();
 				matrix.postRotate(exifOrientationToDegrees(Integer.parseInt(orientation)));
 				imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
 			}
 			catch(Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 		return imageBitmap;
 
 	}
 	
 
 }
