 package de.geotweeter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.widget.ImageView;
 import de.geotweeter.widgets.AccountSwitcherRadioButton;
 
 /*
  * Vorlage: https://github.com/thest1/LazyList/blob/master/src/com/fedorvlasov/lazylist/ImageLoader.java
  */
 public class BackgroundImageLoader {
 	private Context application_context;
 	private static FileCache file_cache;
 	private Map<String, Bitmap> bitmap_cache = Collections
 			.synchronizedMap(new WeakHashMap<String, Bitmap>());
 	static ExecutorService executor_service = null;
 	final int loading_image_id = R.drawable.loading_image;
 	private static final String LOG = "BackgroundImageLoader";
 
 	public BackgroundImageLoader(Context applicationContext) {
 		application_context = applicationContext;
 		if (executor_service == null) {
 			executor_service = Executors.newFixedThreadPool(5);
 		}
 		if (file_cache == null) {
 			file_cache = new FileCache(applicationContext);
 		}
 	}
 
 	public void displayImage(String url, AsyncImageView image_view,
 			boolean store_persistent) {
 		if (url == null) {
 			return;
 		}
 
 		Bitmap bitmap = bitmap_cache.get(url);
 
 		if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 			Log.d(LOG, "View: " + image_view.toString() + " url: " + url);
 		}
 
 		if (bitmap != null) {
 			image_view.setUrl(url);
 			image_view.setImageBitmap(bitmap);
 		} else {
 			image_view.setImageResource(loading_image_id);
 			image_view.setUrl(url);
 			/* Queue Image to download */
 			executor_service.submit(new ImageLoader(url, image_view,
 					store_persistent, true));
 		}
 
 	}
 
 	public void displayImage(String url, ImageView image_view,
 			boolean store_persistent) {
 		if (url == null) {
 			return;
 		}
 
 		Bitmap bitmap = bitmap_cache.get(url);
 
 		if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 			Log.d(LOG, "View: " + image_view.toString() + " url: " + url);
 		}
 
 		if (bitmap != null) {
 			image_view.setImageBitmap(bitmap);
 		} else {
 			image_view.setImageResource(loading_image_id);
 			/* Queue Image to download */
 			executor_service.submit(new ImageLoader(url, image_view,
 					store_persistent, false));
 		}
 	}
 
 	public class ImageLoader implements Runnable {
 		String url;
 		ImageView image_view;
 		private boolean store_persistent;
 		private boolean is_async;
 
 		public ImageLoader(String url, ImageView image_view,
 				boolean store_persistent, boolean is_async) {
 			this.url = url;
 			this.image_view = image_view;
 			this.store_persistent = store_persistent;
 			this.is_async = is_async;
 		}
 
 		@Override
 		public void run() {
 			if (is_async) {
 				final AsyncImageView img = (AsyncImageView) image_view;
 				if (bitmap_cache.containsKey(url)) {
 					((Activity) img.getContext()).runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							if (img.getUrl().equals(url)) {
 								img.setImageBitmap(bitmap_cache.get(url));
 							}
 						}
 					});
 					return;
 				}
 				// if (imageViewReused(url, image_view)) return;
 				final Bitmap bmp = loadBitmap(url, store_persistent);
 				if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 					Log.d(LOG, "Finished loading " + url);
 				}
 
 				// if (imageViewReused(url, image_view)) return;
 				((Activity) img.getContext()).runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						if (bmp != null) {
 							if (img.getUrl().equals(url)) {
 								img.setImageBitmap(bmp);
 							}
 						}
 					}
 
 				});
 			} else {
 				if (bitmap_cache.containsKey(url)) {
 					((Activity) image_view.getContext())
 							.runOnUiThread(new Runnable() {
 								@Override
 								public void run() {
 									image_view.setImageBitmap(bitmap_cache
 											.get(url));
 								}
 							});
 					return;
 				}
 				// if (imageViewReused(url, image_view)) return;
 				final Bitmap bmp = loadBitmap(url, store_persistent);
 				if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 					Log.d(LOG, "Finished loading " + url);
 				}
 
 				// if (imageViewReused(url, image_view)) return;
 				((Activity) image_view.getContext())
 						.runOnUiThread(new Runnable() {
 
 							@Override
 							public void run() {
 								if (bmp != null) {
 									image_view.setImageBitmap(bmp);
 								}
 							}
 
 						});
 			}
 		}
 
 	}
 
 	public void displayImage(String url,
 			AccountSwitcherRadioButton radioButton, boolean store_persistent) {
 		Bitmap bitmap = bitmap_cache.get(url);
 
 		if (bitmap != null) {
 			radioButton.setButtonBitmap(bitmap_cache.get(url));
 		} else {
 			radioButton.setButtonDrawable(loading_image_id);
 			/* Queue Image to download */
 			executor_service.submit(new AccountSwitcherRadioButtonImageLoader(
 					url, radioButton, store_persistent));
 		}
 
 	}
 
 	public class AccountSwitcherRadioButtonImageLoader implements Runnable {
 		String url;
 		AccountSwitcherRadioButton radioButton;
 		private boolean store_persistent;
 
 		public AccountSwitcherRadioButtonImageLoader(String url,
 				AccountSwitcherRadioButton radioButton, boolean store_persistent) {
 			this.url = url;
 			this.radioButton = radioButton;
 			this.store_persistent = store_persistent;
 		}
 
 		@Override
 		public void run() {
 			if (bitmap_cache.containsKey(url)) {
 				((Activity) radioButton.getContext())
 						.runOnUiThread(new Runnable() {
 							@Override
 							public void run() {
 								radioButton.setButtonBitmap(bitmap_cache
 										.get(url));
 							}
 						});
 				return;
 			}
 			final Bitmap bmp = loadBitmap(url, store_persistent);
 			if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 				Log.d(LOG, "Finished loading " + url);
 			}
 
 			((Activity) radioButton.getContext()).runOnUiThread(new Runnable() {
 
 				@Override
 				public void run() {
 					if (bmp != null) {
 						radioButton.setButtonBitmap(bitmap_cache.get(url));
 					}
 				}
 
 			});
 		}
 
 	}
 
 	public Bitmap getBitmap(String url, boolean store_persistent) {
 		if (bitmap_cache.containsKey(url)) {
 			return bitmap_cache.get(url);
 		} else {
 			return loadBitmap(url, store_persistent);
 		}
 	}
 
 	public Bitmap loadBitmap(String url, boolean store_persistent) {
 		Bitmap bitmap = null;
 		File cache_file = file_cache.getFile(url);
 		if (cache_file.exists()) {
 			if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 				Log.d(LOG, "Loading " + url + " from cache.");
 			}
 			try {
 				bitmap = new BitmapDrawable(application_context.getResources(),
 						BitmapFactory.decodeFile(cache_file.getCanonicalPath()))
 						.getBitmap();
 				bitmap_cache.put(url, bitmap);
 				return bitmap;
 			} catch (IOException e) {
 			}
 		}
 
 		if (Debug.LOG_BACKGROUND_IMAGE_LOADER) {
 			Log.d(LOG, "Loading " + url + " from web");
 		}
 
 		try {
 			URL url_handler = new URL(url);
 			InputStream bitmapStream;
 			if (url_handler.getHost().equals("img.youtube.com")) {
 				try {
 					bitmapStream = url_handler.openConnection()
 							.getInputStream();
 				} catch (FileNotFoundException e) {
 					url_handler = new URL(url.replace("maxresdefault.jpg",
 							"hqdefault.jpg"));
 					bitmapStream = url_handler.openConnection()
 							.getInputStream();
 				}
 			} else {
 				bitmapStream = url_handler.openConnection().getInputStream();
 			}
 			bitmap = new BitmapDrawable(application_context.getResources(),
 					BitmapFactory.decodeStream(bitmapStream)).getBitmap();
 			bitmap_cache.put(url, bitmap);
 			if (store_persistent) {
 				FileOutputStream file_stream = new FileOutputStream(cache_file);
 				bitmap.compress(Bitmap.CompressFormat.PNG, 85, file_stream);
 				file_stream.flush();
 				file_stream.close();
 			}
 			return bitmap;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return bitmap;
 	}
 
 	public void clearCache() {
 		file_cache.clear();
 	}
 
 	class FileCache {
 
 		private File cache_dir;
 
 		public FileCache(Context context) {
 			cache_dir = context.getExternalFilesDir(null);
			if (cache_dir == null || !cache_dir.exists()) {
 				cache_dir = context.getCacheDir();
 			}
 			cache_dir = new File(cache_dir, Constants.PATH_AVATAR_IMAGES);
 			if (!cache_dir.exists()) {
 				cache_dir.mkdirs();
 			}
 
 			/*
 			 * File nomedia_file = new File(cache_dir, ".nomedia"); if
 			 * (!nomedia_file.exists()) { try { FileWriter fwriter = new
 			 * FileWriter(nomedia_file); fwriter.write(""); fwriter.close(); }
 			 * catch (IOException e) { } }
 			 */
 		}
 
 		public File getFile(String url) {
 			String filename = url.replaceAll("[^a-zA-Z0-9\\-_.]+", "_");
 			File f = new File(cache_dir, filename);
 			return f;
 		}
 
 		public void clear() {
 			File[] files = cache_dir.listFiles();
 			if (files == null) {
 				return;
 			}
 			for (File file : files) {
 				file.delete();
 			}
 		}
 	}
 }
