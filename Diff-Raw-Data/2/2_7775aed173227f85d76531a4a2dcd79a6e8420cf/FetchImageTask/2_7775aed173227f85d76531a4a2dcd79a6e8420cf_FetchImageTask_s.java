 package cat.mobilejazz.database;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.AsyncTask;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import cat.mobilejazz.utilities.debug.Debug;
 import cat.mobilejazz.utilities.io.IOUtils;
 
 public class FetchImageTask<V extends View> extends AsyncTask<Void, Void, Bitmap> {
 
 	public static interface ImageSetter<V> {
 
 		public void onSetImage(V view, Bitmap bitmap);
 
 	}
 
 	public static class ImageViewSetter implements ImageSetter<ImageView> {
 
 		@Override
 		public void onSetImage(ImageView view, Bitmap bitmap) {
 			view.setImageBitmap(bitmap);
 		}
 
 	}
 
 	public static class TextViewSetter implements ImageSetter<TextView> {
 
 		@Override
 		public void onSetImage(TextView view, Bitmap bitmap) {
 			view.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(view.getResources(), bitmap), null, null,
 					null);
 		}
 
 	}
 
 	private String mImageUrl;
 	private String mDefaultUrl;
 	private int mDefaultDrawable;
 	private V mView;
 	private ImageSetter<V> mImageSetter;
 
 	public FetchImageTask(String imageUrl, String defaultUrl, V view, ImageSetter<V> imageSetter) {
 		super();
 		mImageUrl = imageUrl;
 		mDefaultUrl = defaultUrl;
 		mView = view;
 		mImageSetter = imageSetter;
 	}
 
 	public FetchImageTask(String imageUrl, int defaultDrawable, V view, ImageSetter<V> imageSetter) {
 		super();
 		mImageUrl = imageUrl;
 		mDefaultDrawable = defaultDrawable;
 		mView = view;
 		mImageSetter = imageSetter;
 	}
 
 	private File getCacheFile() {
 		return new File(mView.getContext().getCacheDir(), mImageUrl);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		File cacheFile = getCacheFile();
 		if (cacheFile.exists()) {
 			try {
 				onPostExecute(BitmapFactory.decodeStream(new FileInputStream(cacheFile)));
 				cancel(true);
 			} catch (FileNotFoundException e) {
 				// should not happen (see if)
 				Debug.logException(e);
 			}
 		}
 	}
 
 	@Override
 	protected Bitmap doInBackground(Void... params) {
 		try {
 			URL imageUrl = new URL(mImageUrl);
 			URLConnection connection = imageUrl.openConnection();
 			connection.setUseCaches(true);
 
 			File cacheFile = getCacheFile();
 			if (!cacheFile.getParentFile().exists()) {
 				cacheFile.getParentFile().mkdirs();
 			}
 			OutputStream cache = new FileOutputStream(cacheFile);
 			IOUtils.copy(connection.getInputStream(), cache);
 
 			return BitmapFactory.decodeStream(new FileInputStream(cacheFile));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(Bitmap result) {
 		if (result == null) {
 			if (mDefaultUrl != null && !mImageUrl.equals(mDefaultUrl)) {
 				// default url:
 				fetchImage(mDefaultUrl, mDefaultUrl, mView, mImageSetter);
			} else {
 				// default res id:
 				mImageSetter.onSetImage(mView, BitmapFactory.decodeResource(mView.getResources(), mDefaultDrawable));
 			}
 		} else {
 			mImageSetter.onSetImage(mView, result);
 		}
 	}
 
 	public static <V extends View> void fetchImage(String imageUrl, String defaultUrl, V view,
 			ImageSetter<V> imageSetter) {
 		new FetchImageTask<V>(imageUrl, defaultUrl, view, imageSetter).execute();
 	}
 
 	public static <V extends View> void fetchImage(String imageUrl, int defaultDrawable, V view,
 			ImageSetter<V> imageSetter) {
 		new FetchImageTask<V>(imageUrl, defaultDrawable, view, imageSetter).execute();
 	}
 
 	public static void fetchImage(String imageUrl, String defaultUrl, ImageView view) {
 		new FetchImageTask<ImageView>(imageUrl, defaultUrl, view, new ImageViewSetter()).execute();
 	}
 
 	public static void fetchImage(String imageUrl, int defaultDrawable, ImageView view) {
 		new FetchImageTask<ImageView>(imageUrl, defaultDrawable, view, new ImageViewSetter()).execute();
 	}
 	
 	public static void fetchImage(String imageUrl, String defaultUrl, TextView view) {
 		new FetchImageTask<TextView>(imageUrl, defaultUrl, view, new TextViewSetter()).execute();
 	}
 
 	public static void fetchImage(String imageUrl, int defaultDrawable, TextView view) {
 		new FetchImageTask<TextView>(imageUrl, defaultDrawable, view, new TextViewSetter()).execute();
 	}
 }
