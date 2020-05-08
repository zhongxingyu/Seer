 package de.raptor2101.GalDroid.Activities.Views;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Activities.GalDroidApp;
 import de.raptor2101.GalDroid.Activities.Listeners.CommentLoaderListener;
 import de.raptor2101.GalDroid.Activities.Listeners.TagLoaderListener;
 import de.raptor2101.GalDroid.WebGallery.GalleryCache;
 import de.raptor2101.GalDroid.WebGallery.GalleryImageView;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 import de.raptor2101.GalDroid.WebGallery.Tasks.CommentLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.ImageLoaderTask;
 import de.raptor2101.GalDroid.WebGallery.Tasks.TagLoaderTask;
 import android.content.Context;
 import android.media.ExifInterface;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ProgressBar;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 public class ImageInformationView extends TableLayout {	
 	private final static String ClassTag = "ImageInformationView";
 	
 	public class ExtractInformationTask extends AsyncTask<Void,Void,Void> {
 		private GalleryImageView mGalleryImageView;
 		
 		public ExtractInformationTask(GalleryImageView imageView) {
 			mGalleryImageView = imageView;
 		}
 		
 		@Override
 		protected Void doInBackground(Void... params) {
 			if(!mGalleryImageView.isLoaded()) {
 				ImageLoaderTask task = mGalleryImageView.getImageLoaderTask();
 				if(task!=null) {
 					try {
 						task.get();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (ExecutionException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			return null;
 		}
 		
 		@Override
 		protected void onPostExecute(Void result) {
 			if(mGalleryImageView.isLoaded()) {
 				extractObjectInformation(mGalleryImageView.getGalleryObject());
 			}
 		}
 	}
 	
 	private TagLoaderListener mTagLoaderListener;
 	private CommentLoaderListener mCommentLoaderListener;
 	
 	private TagLoaderTask mTagLoaderTask;
 	private CommentLoaderTask mCommentLoaderTask;
 	private ExtractInformationTask mExtractTask;
 	
 	private GalleryImageView mCurrentImageView;
 	
 	public ImageInformationView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initialize();
 	}
 
 	public ImageInformationView(Context context) {
 		super(context);
 		initialize();
 	}
 	
 	public void setGalleryImageView(GalleryImageView imageView) {
 		mCurrentImageView = imageView;
 		if(getVisibility() == VISIBLE) {
 			executeExtrationTask();
 		}
 	}
 	
 	@Override
 	public void setVisibility(int visibility) {
 		if(visibility == VISIBLE) {
 			executeExtrationTask();
 		}
 		
 		super.setVisibility(visibility);
 	}
 	
 	private void executeExtrationTask() {
		clearImageInformations();
		mExtractTask = new ExtractInformationTask(mCurrentImageView);
		mExtractTask.execute();
 	}
 
 	private void initialize(){
 		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         inflater.inflate(R.layout.image_information_view, this);
 		
 		mTagLoaderListener = new TagLoaderListener((TextView)findViewById(R.id.textTags), (ProgressBar)findViewById(R.id.progressBarTags));
 		mCommentLoaderListener = new CommentLoaderListener((ViewGroup)findViewById(R.id.layoutComments), (ProgressBar)findViewById(R.id.progressBarComments));
 		
 	}
 	
 	private void clearImageInformations() {
 		TextView textView = (TextView) findViewById(R.id.textTitle);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textTags);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textUploadDate);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifCreateDate);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifAperture);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifExposure);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifFlash);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifISO);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifModel);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifModel);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifMake);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifFocalLength);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textExifWhiteBalance);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textGeoLat);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textGeoLong);
 		textView.setText("");
 		
 		textView = (TextView) findViewById(R.id.textGeoHeight);
 		textView.setText("");
 	}
 
 	private void extractObjectInformation(GalleryObject galleryObject) {
 		GalDroidApp app = (GalDroidApp) getContext().getApplicationContext();
 		WebGallery webGallery = app.getWebGallery();
 		GalleryCache galleryCache = app.getGalleryCache();
 		
 		TextView textTitle = (TextView) findViewById(R.id.textTitle);
 		TextView textUploadDate = (TextView) findViewById(R.id.textUploadDate);
 		
 		
 		textTitle.setText(galleryObject.getTitle());
 		textUploadDate.setText(galleryObject.getDateUploaded().toLocaleString());
 		
 		if(mTagLoaderTask != null && (mTagLoaderTask.getStatus() == Status.RUNNING || mTagLoaderTask.getStatus() == Status.PENDING)) {
 			Log.d(ClassTag, "Aborting TagLoaderTask");
 			mTagLoaderTask.cancel(true);
 		}
 		
 		if(mCommentLoaderTask != null && (mCommentLoaderTask.getStatus() == Status.RUNNING || mCommentLoaderTask.getStatus() == Status.PENDING)) {
 			Log.d(ClassTag, "Aborting CommentLoaderTask");
 			mCommentLoaderTask.cancel(true);
 		}
 		
 		mTagLoaderTask = new TagLoaderTask(webGallery, mTagLoaderListener);
 		mTagLoaderTask.execute(galleryObject);
 		
 		mCommentLoaderTask = new CommentLoaderTask(webGallery, mCommentLoaderListener);
 		mCommentLoaderTask.execute(galleryObject);
 		
 		extractExifInformation(galleryObject, galleryCache);
 	}
 	
 	private void extractExifInformation(GalleryObject galleryObject, GalleryCache galleryCache) {
 		File cachedFile = galleryCache.getFile(galleryObject.getImage().getUniqueId());
 		try {
 			ExifInterface exif = new ExifInterface(cachedFile.getAbsolutePath());
 			
 			
 			TextView textField = (TextView) findViewById(R.id.textExifCreateDate);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_DATETIME));
 			
 			textField = (TextView) findViewById(R.id.textExifAperture);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_APERTURE));
 			
 			try {
 				textField = (TextView) findViewById(R.id.textExifExposure);
 				float exposureTime = 1.0f / Float.parseFloat(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
 				textField.setText(String.format("1/%.0fs", exposureTime));
 			} catch (Exception e) {
 				
 			}
 			
 			try {
 				textField = (TextView) findViewById(R.id.textExifFlash);
 				int flash = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
 				textField.setText(String.format("%d", flash));
 			} catch (Exception e) {
 				
 			}
 			
 			textField = (TextView) findViewById(R.id.textExifISO);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_ISO));
 			
 			textField = (TextView) findViewById(R.id.textExifModel);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
 			
 			textField = (TextView) findViewById(R.id.textExifModel);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_MODEL));
 			
 			textField = (TextView) findViewById(R.id.textExifMake);
 			textField.setText(exif.getAttribute(ExifInterface.TAG_MAKE));
 			
 			try {
 				double focalLength = exif.getAttributeDouble(ExifInterface.TAG_FOCAL_LENGTH, 0);
 				textField = (TextView) findViewById(R.id.textExifFocalLength);
 				textField.setText(String.format("%.0fmm", focalLength));
 			} catch (Exception e) {
 
 			}
 			
 			try {
 				textField = (TextView) findViewById(R.id.textExifWhiteBalance);
 				int whiteBalance = exif.getAttributeInt(ExifInterface.TAG_FLASH, 0);
 				if(whiteBalance == ExifInterface.WHITEBALANCE_AUTO) {
 					textField.setText(R.string.object_exif_whitebalance_auto);
 				}
 				else {
 					textField.setText(R.string.object_exif_whitebalance_manual);
 				}
 			} catch (Exception e) {
 				
 			}
 			
 			textField = (TextView) findViewById(R.id.textGeoLat);
 			String latitude = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
 			textField.setText(latitude);
 			
 			textField = (TextView) findViewById(R.id.textGeoLong);
 			String longitude = parseDegMinSec(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
 			textField.setText(longitude);
 			
 			textField = (TextView) findViewById(R.id.textGeoHeight);
 			String height = exif.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
 			textField.setText(parseHeight(height));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private String parseDegMinSec(String tagGpsValue) {
 		try {
 			String[] values = tagGpsValue.split("[,/]");
 			float deg = Float.parseFloat(values[0])/Float.parseFloat(values[1]);
 			float min = Float.parseFloat(values[2])/Float.parseFloat(values[3]);
 			float sec = Float.parseFloat(values[4])/Float.parseFloat(values[5]);
 			return String.format("%.0f° %.0f' %.2f\"", deg,min,sec);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 	
 	private String parseHeight(String tagGpsValue) {
 		try {
 			String[] values = tagGpsValue.split("/");
 			float height = Float.parseFloat(values[0])/Float.parseFloat(values[1]);
 			return String.format("%.2fm", height);
 		} catch (Exception e) {
 			return "";
 		}
 	}
 	
 	public ExtractInformationTask getExtractionInfromationTask() {
 		return mExtractTask;
 	}
 }
