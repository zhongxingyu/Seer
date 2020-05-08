 package ch.almana.android.sessionsmodels.helper;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import ch.almana.android.sessionsmodels.log.Logger;
 
 public class CacheBuilderAsyncTask extends AsyncTask<File, Object, Object> {
 
 	private final int imageSize;
 	private final Context ctx;
 
 	public CacheBuilderAsyncTask(Context ctx, int imageSize) {
 		super();
 		this.imageSize = imageSize;
 		this.ctx = ctx.getApplicationContext();
 	}
 
 	@Override
 	protected Object doInBackground(File... params) {
 		for (File file : params) {
 			try {
 				Logger.i("Prebuilding image " + file.getName());
 				ImageHelper.scaleImage(ctx, file, imageSize);
 			} catch (FileNotFoundException e) {
 				Logger.e("Cannot prescale image", e);
 			}
 		}
 		return null;
 	}
 
 	public static void cacheFiles(Context ctx, File[] images, int imageSize) {
		if (images == null) {
			return;
		}
 		CacheBuilderAsyncTask cb = new CacheBuilderAsyncTask(ctx, imageSize);
 		cb.execute(images);
 	}
 
 }
