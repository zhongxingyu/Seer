 package in.ccl.net;
 
 import in.ccl.ui.R;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.widget.Toast;
 
 
 /**
  * Created class for  download an image from server and save image in sdcard.
  * Download image from server using AsyncTask.
  * @author venkanna Babu
  *
  */
 
 public class DownloaderImage extends AsyncTask<String, Void, Bitmap> {
 
 	private Context mContext;
 
 	private ProgressDialog mProgressDialog;
 
 	private int imageId;
 	
 	public final static String APP_PATH_SD_CARD = "/CCL/";
	public final static String APP_THUMBNAIL_PATH_SD_CARD = "/download";
 
 	public DownloaderImage(Context mcontext2, int id) {
 		mContext = mcontext2;
 		imageId = id;
 
 	}
 
 	@Override
 	protected Bitmap doInBackground(String... params) {
 
 		return downloadBitmap(params[0]);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		/*mProgressDialog  = new ProgressDialog(mContext);
 		mProgressDialog.setTitle(mContext.getResources().getString(R.string.downloading_image));
 		mProgressDialog.show();*/
 		mProgressDialog = ProgressDialog.show(mContext, "Wait", mContext.getResources().getString(R.string.downloading_image));
 
 	}
 
 	@Override
 	protected void onPostExecute(Bitmap result) {
 		mProgressDialog.dismiss();
 		//Checking sdcard is available or not other displaying message which Please insert the sdcard
 		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
 			boolean isSaved = saveImageToExternalStorage(result);
           //Image successfully download displaying succuss message otherwise fail message.
 			if (isSaved) {
 				Toast.makeText(mContext, mContext.getResources().getString(R.string.downloading_succuss),Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(mContext,  mContext.getResources().getString(R.string.downloading_fail),Toast.LENGTH_SHORT).show();
 			}
 
 		} else {
 
 			Toast.makeText(mContext, mContext.getResources().getString(R.string.no_sdcard), Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 	
 	/**
 	 * This method  download Bitmap image and return bitmap or null
 	 * @param url
 	 * @return bitmap
 	 */
 	
 	
 	private Bitmap downloadBitmap(String url) {
 
 		final DefaultHttpClient client = new DefaultHttpClient();
 		final HttpGet getRequest = new HttpGet(url);
 
 		try {
 			HttpResponse response = client.execute(getRequest);
 			final int statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode != HttpStatus.SC_OK) {
 				return null;
 			}
 
 			final HttpEntity entity = response.getEntity();
 			if (entity != null) {
 				InputStream inputStream = null;
 				try {
 					inputStream = entity.getContent();
 					final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
 					return bitmap;
 				} finally {
 					if (inputStream != null) {
 						inputStream.close();
 					}
 					entity.consumeContent();
 				}
 			}
 		} catch (Exception e) {
 			// Could provide a more explicit error message for IOException or
 			// IllegalStateException
 			getRequest.abort();
 		} finally {
 			if (client != null) {
 
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method create the two file and store the photos in particular file in sdcard.
 	 * @param Bitmap image
 	 * @return boolean value
 	 */
 	
 	public boolean saveImageToExternalStorage(Bitmap image) {
		String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + APP_THUMBNAIL_PATH_SD_CARD;
   System.out.println("nagesh download image path"+fullPath);
 		try {
 		File dir = new File(fullPath);
 		if (!dir.exists()) {
 		dir.mkdirs();
 		}
 
 		OutputStream fOut = null;
 		String mFilePath = "photos-" + "" + imageId + ".png";
 		File file = new File(fullPath, mFilePath);
 		file.createNewFile();
 		fOut = new FileOutputStream(file);
 		// 100 means no compression, the lower you go, the stronger the compression
 		image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
 		fOut.flush();
 		fOut.close();
 
 		MediaStore.Images.Media.insertImage(mContext.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
 
 		return true;
 
 		} catch (Exception e) {
 		return false;
 		}
 		}
 
 }
