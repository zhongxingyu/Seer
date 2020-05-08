 package com.appspot.pilo_shar;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.location.Location;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.TextView;
 
 import com.appspot.pilo_shar.communicator.AnonymousCommunicator;
 import com.appspot.pilo_shar.communicator.Communicator;
 import com.appspot.pilo_shar.utils.ProgressMultiPartEntity;
 import com.appspot.pilo_shar.utils.ProgressMultiPartEntity.ProgressListener;
 import com.appspot.pilo_shar.utils.gps.LocationHelper;
 import com.appspot.pilo_shar.utils.gps.LocationResult;
 
 public class ShareImageActivity extends Activity {
 	private Bitmap bitmap;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_preview_image_share);
 		initImageToView();
 		initListener();
 		requestGPS();
 	}
 
 	private void initListener() {
 		// Find button resources
 		Button btnShare = (Button) findViewById(R.id.btn_share);
 
 		// Register listener
 		btnShare.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Start task to upload image
 				new AsyncPostImage(bitmap).execute();
 			}
 		});
 	}
 
 	private void initImageToView() {
 		Intent intent = getIntent(); // get the intent called this activity
 		Bundle extras = intent.getExtras();
 		String action = intent.getAction();
 
 		// if this is from the share menu
 		if (Intent.ACTION_SEND.equals(action)
 				&& extras.containsKey(Intent.EXTRA_STREAM)) {
 			// Get resource path from intent callee
 			Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 
 			// Find Image View
 			ImageView previewImage = getImageView();
 			try {
 				// Load bitmap from stream
 				ContentResolver cr = getContentResolver();
 				InputStream is = cr.openInputStream(uri);
 				bitmap = BitmapFactory.decodeStream(is);
 
 				// Load image
 				previewImage.setImageBitmap(bitmap);
 			} catch (Exception e) {
 				Log.v("Tag", e.getMessage());
 			}
 		}
 
 	}
 
 	private ImageView getImageView() {
 		ImageView iv = (ImageView) findViewById(R.id.preview_image);
 		iv.setScaleType(ScaleType.CENTER_CROP);
 		return iv;
 	}
 
 	private void requestGPS() {
 		LocationHelper locHelper = new LocationHelper();
 		locHelper.getLocation(getApplicationContext(), locationResult);
 
 		TextView locationTv = (TextView) findViewById(R.id.tv_location_result);
 		locationTv.setText("Getting location...");
 	}
 
 	private LocationResult locationResult = new LocationResult() {
 
 		@Override
 		public void gotLocation(Location location) {
 			// Assign location to activity
 			TextView locationTv = (TextView) findViewById(R.id.tv_location_result);
 			if (location != null)
 				locationTv.setText("Longitude=" + location.getLongitude()
 						+ ", Latitude=" + location.getLatitude());
 		}
 	};
 
 	/**
 	 * Start task to upload image
 	 */
 	private class AsyncPostImage extends AsyncTask<Void, Integer, Void> {
 
 		private ProgressDialog pd;
 		private Bitmap bitmap;
 		private long totalSize;
 
 		public AsyncPostImage(Bitmap bitmap) {
 			this.bitmap = bitmap;
 
 		}
 
 		@Override
 		protected void onPreExecute() {
 			pd = new ProgressDialog(ShareImageActivity.this);
 			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			pd.setMessage("Uploading Picture...");
 			pd.setCancelable(false);
 			pd.show();
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			// Get prepare upload url, this url will be expired after 10minutes
 			String upload_url = prepare_upload_url();
 
 			ByteArrayOutputStream bao = new ByteArrayOutputStream();
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
 			byte[] data = bao.toByteArray();
 
 			// Using custom MultipartEntity for progressbar
 			ProgressMultiPartEntity entity = new ProgressMultiPartEntity(
 					HttpMultipartMode.BROWSER_COMPATIBLE,
 					new ProgressListener() {
 
 						@Override
 						public void transferred(long num) {
 							publishProgress((int) ((num / (float) totalSize) * 100));
 						}
 
 					});
 
 			entity.addPart("file",
 					new ByteArrayBody(data, "image/jpeg", "file"));
 			totalSize = entity.getContentLength();
 
 			String blobStoreId = new AnonymousCommunicator().doPost(upload_url,
 					entity);
 			return null;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... progress) {
 			pd.setProgress((int) (progress[0]));
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			pd.dismiss();
 		}
 
 		private String prepare_upload_url() {
 			Communicator communicator = new AnonymousCommunicator();
 			String url = communicator.doGet(Urls.PREPARE_UPLOAD_URL);
 			return url;
 		}
 	}
 }
