 package com.cesarandres.ps2link.base;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.WeakReference;
 
import android.annotation.TargetApi;
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.os.AsyncTask;
import android.os.Build;
 import android.os.Environment;
 import android.view.Display;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 import com.cesarandres.ps2link.ApplicationPS2Link;
 
 public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
 	private final WeakReference<ImageView> imageViewReference;
 	private Activity context;
 	private String data;
 	public static final String DOWNLOAD_DIR = ".cache";
 	public static final String PUBLIC_DIR = "AuraxisControlCenter";
 
 	public BitmapWorkerTask(ImageView imageView, Activity context) {
 		// Use a WeakReference to ensure the ImageView can be garbage
 		// collected
 		this.imageViewReference = new WeakReference<ImageView>(imageView);
 		this.context = context;
 	}
 
 	// Decode image in background.
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	@Override
 	protected Bitmap doInBackground(String... params) {
 		data = params[0];
 
 		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), PUBLIC_DIR + File.separator + DOWNLOAD_DIR);
 		if (!mediaStorageDir.exists()) {
 			if (!mediaStorageDir.mkdirs()) {
 				return null;
 			}
 		}
 
 		File file = new File(mediaStorageDir.getAbsoluteFile() + File.separator + data);
 		if (!file.exists()) {
 			Display display = context.getWindowManager().getDefaultDisplay();
 			Point size = new Point();
 			int width, height;
 			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
 				display.getSize(size);
 				width = size.x;
 				height = size.y;
 			} else {
 				width = display.getWidth(); // deprecated
 				height = display.getHeight(); // deprecated
 			}
 
 			InputStream ims;
 			try {
 				if (isCancelled()) {
 					return null;
 				}
 				ims = context.getAssets().open(data);
 				Bitmap image = decodeSampledBitmapFromResource(ims, width, height);
 				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
 				image.compress(Bitmap.CompressFormat.JPEG, 85, bytes);
 
 				FileOutputStream fos = new FileOutputStream(file);
 				fos.write(bytes.toByteArray());
 				fos.close();
 				ApplicationPS2Link.setBackground(image);
 				return image;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 			try {
 				if (isCancelled()) {
 					return null;
 				}
 				Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
 				ApplicationPS2Link.setBackground(image);
 				return image;
 			} catch (OutOfMemoryError e) {
 				System.gc();
 				return null;
 			}
 		}
 
 	}
 
 	// Once complete, see if ImageView is still around and set bitmap.
 	@Override
 	protected void onPostExecute(Bitmap bitmap) {
 		if (!isCancelled() || (imageViewReference != null && bitmap != null)) {
 			final ImageView imageView = imageViewReference.get();
 			if (imageView != null) {
 				imageView.setImageBitmap(bitmap);
 				imageView.setScaleType(ScaleType.CENTER_CROP);
 			}
 		}
 	}
 
 	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
 		// Raw height and width of image
 		final int height = options.outHeight;
 		final int width = options.outWidth;
 		int inSampleSize = 1;
 
 		if (height > reqHeight || width > reqWidth) {
 
 			// Calculate ratios of height and width to requested height and
 			// width
 			final int heightRatio = Math.round((float) height / (float) reqHeight);
 			final int widthRatio = Math.round((float) width / (float) reqWidth);
 
 			// Choose the smallest ratio as inSampleSize value, this will
 			// guarantee
 			// a final image with both dimensions larger than or equal to the
 			// requested height and width.
 			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
 		}
 
 		return inSampleSize;
 	}
 
 	public static Bitmap decodeSampledBitmapFromResource(InputStream ims, int reqWidth, int reqHeight) {
 
 		// First decode with inJustDecodeBounds=true to check dimensions
 		final BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(ims, null, options);
 
 		// Calculate inSampleSize
 		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
 
 		// Decode bitmap with inSampleSize set
 		options.inJustDecodeBounds = false;
 		return BitmapFactory.decodeStream(ims, null, options);
 	}
 }
