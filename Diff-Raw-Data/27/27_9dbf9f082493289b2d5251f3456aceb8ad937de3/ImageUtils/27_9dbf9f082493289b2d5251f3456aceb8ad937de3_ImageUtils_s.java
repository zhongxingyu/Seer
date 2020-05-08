 package info.winiex.androidbasement.image.utils;
 
 import info.winiex.androidbasement.R;
 import info.winiex.androidbasement.utils.AndroidVersion;
 import info.winiex.androidbasement.utils.AppData;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 public class ImageUtils {
 
 	public final static int IMAGE_TYPE_RESOURCE = 0;
 
 	public final static int IMAGE_TYPE_HTTP = 1;
 
 	public final static int IMAGE_TYPE_DISK = 2;
 
 	public final static Bitmap defaultBitmap = BitmapFactory.decodeResource(
 			AppData.getResources(), R.drawable.empty_image);
 
 	public static BitmapFactory.Options getResourceImageOptions(int resId) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeResource(AppData.getResources(), resId, options);
 		return options;
 	}
 
 	public static BitmapFactory.Options getDiskImageOptions(String imageFilePath) {
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeFile(imageFilePath, options);
 		return options;
 	}
 
 	public static int calculateInSampleSize(BitmapFactory.Options options,
 			int reqWidth, int reqHeight) {
 
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 
 		if (height > reqHeight || width > reqWidth) {
 
 			final int heightRatio = Math.round((float) height
 					/ (float) reqHeight);
 			final int widthRatio = Math.round((float) width / (float) reqWidth);
 
 			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 		}
 
 		return inSampleSize;
 	}
 
 	public static Bitmap decodeSampledBitmapFromResource(int resId,
 			int reqWidth, int reqHeight) {
 
 		final BitmapFactory.Options options = getResourceImageOptions(resId);
 		options.inSampleSize = calculateInSampleSize(options, reqWidth,
 				reqHeight);
 		options.inJustDecodeBounds = false;
 
 		return BitmapFactory.decodeResource(AppData.getResources(), resId,
 				options);
 	}
 
 	public static Bitmap decodeBitmapFromResource(int resId, int reqWidth,
 			int reqHeight) {
 		final BitmapFactory.Options options = getResourceImageOptions(resId);
 		options.inJustDecodeBounds = false;
 
 		return BitmapFactory.decodeResource(AppData.getResources(), resId,
 				options);
 	}
 
 	public static Bitmap decodeSampleBitmapFromDisk(String imageFilePath,
 			int reqWidth, int reqHeight) {
 		final BitmapFactory.Options options = getDiskImageOptions(imageFilePath);
 		options.inSampleSize = calculateInSampleSize(options, reqWidth,
 				reqHeight);
 		options.inJustDecodeBounds = false;
 
 		return BitmapFactory.decodeFile(imageFilePath, options);
 	}
 
 	public static Bitmap decodeBitmapFromDisk(String imageFilePath,
 			int reqWidth, int reqHeight) {
 		final BitmapFactory.Options options = getDiskImageOptions(imageFilePath);
 		options.inJustDecodeBounds = false;
 
 		return BitmapFactory.decodeFile(imageFilePath, options);
 	}
 
 	public static Bitmap decodeBitmapFromHttp(String uriStr, int reqWidth,
 			int reqHeight) {
 		// TODO
 		return null;
 	}
 
 	public static long getBitmapSizeInBytes(Bitmap bitmap) {
	    if(AndroidVersion.isHoneycombMr2OrHigher()) {
	        return bitmap.getByteCount();
	    } else {
	        return bitmap.getRowBytes() * bitmap.getHeight();
	    }
 	}
 }
