 package com.josephblough.fluchallenge.activities;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.josephblough.fluchallenge.ApplicationController;
 import com.josephblough.fluchallenge.R;
 import com.josephblough.fluchallenge.data.TimePeriod;
 import com.josephblough.fluchallenge.services.FluActivityReportDownloaderService;
 import com.josephblough.fluchallenge.tasks.TimePeriodImageDownloaderTask;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.ImageView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FluActivityReport extends Activity implements OnSeekBarChangeListener {
 
     private final static String TAG = "FluActivityReport";
 
     private ProgressDialog progress = null;
     private static final String ERROR_MSG = "There was an error downloading the Flu report";
     private static final String IMAGE_URL = "http://www.cdc.gov/flu/weekly/%ARCHIVE_DIRECTORY%/images/%IMAGE_FILENAME%";
     private static final String IMAGE_DIRECTORY_PLACEHOLDER = "%ARCHIVE_DIRECTORY%";
     private static final String IMAGE_FILENAME_PLACEHOLDER = "%IMAGE_FILENAME%";
     private static final String SAVED_CURRENT_PERIOD = "FluActivityReport.savedCurrentPeriod";
     private static final String SAVED_CURRENT_BITMAP = "FluActivityReport.savedCurrentBitmap";
     
     private TextView mapTitle = null;
     private SeekBar periodSeekbar = null;
     private ImageView mapImage = null;
     private Map<Integer, Bitmap> imageCache;
     private Integer savedImageIndex = null;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
 
 	this.imageCache = new HashMap<Integer, Bitmap>();
         
         if (savedInstanceState != null) {
             if (savedInstanceState.containsKey(SAVED_CURRENT_PERIOD)) {
         	this.savedImageIndex = savedInstanceState.getInt(SAVED_CURRENT_PERIOD);
                 if (savedInstanceState.containsKey(SAVED_CURRENT_BITMAP)) {
                     Bitmap currentBitmap = savedInstanceState.getParcelable(SAVED_CURRENT_BITMAP);
                     if (this.savedImageIndex != null && currentBitmap != null) {
                 	imageCache.put(this.savedImageIndex, currentBitmap);
                     }
                 }
             }
         }
         
         setContentView(R.layout.flu_activity_report);
 
         this.mapTitle = (TextView) findViewById(R.id.flu_activity_week_label);
 	this.periodSeekbar = (SeekBar) findViewById(R.id.flu_activity_week_seekbar);
 	this.mapImage = (ImageView) findViewById(R.id.flu_activity_map);
 
 	this.periodSeekbar.setOnSeekBarChangeListener(this);
     }
     
     private void loadFluActivityReport() {
 	Intent intent = new Intent(this, FluActivityReportDownloaderService.class);
 	intent.putExtra(FluActivityReportDownloaderService.EXTRA_MESSENGER,
 		new Messenger(new Handler() {
 		    @Override
 		    public void handleMessage(Message message) {
 			if (message.arg1 == Activity.RESULT_OK) {
 			    //Log.d(TAG, "loadFluActivityReport handleMessage");
 			    done();
 			} else {
 			    error(ERROR_MSG);
 			}
 		    }
 		}));
 	startService(intent);
 
 	progress = ProgressDialog.show(this, "", "Downloading Flu activity report");
     }
     
     private void error(final String error) {
 	if (progress != null)
 	    progress.dismiss();
 	
 	Toast toast = Toast.makeText(this, error, Toast.LENGTH_LONG);
 	toast.setGravity(Gravity.BOTTOM, 0, 0);
 	toast.show();
     }
     
     private void done() {
 	if (progress != null)
 	    progress.dismiss();
 	
 	setTitle(ListPresentation.FLU_ACTIVIY_TITLE);
 	final ApplicationController app = (ApplicationController)getApplicationContext();
 	this.periodSeekbar.setMax(app.fluReport.periods.size()-1);
 
 	int currentPeriod = (this.savedImageIndex == null) ? app.fluReport.periods.size()-1 : this.savedImageIndex.intValue();
 	this.savedImageIndex = null;
 	
 	//Log.d(TAG, "Max: " + (app.fluReport.periods.size()-1) + ", Current: " + currentPeriod);
 	final TimePeriod period = app.fluReport.periods.get(currentPeriod);
 	this.mapTitle.setText(period.subtitle);
 	this.periodSeekbar.setProgress(currentPeriod);
 
 	if (imageCache.containsKey(currentPeriod) && imageCache.get(Integer.valueOf(currentPeriod)) != null) {
 	    done(imageCache.get(currentPeriod));
 	}
 	else {
 	    //Log.d(TAG, "done -> updateImage");
 	    updateImage(period);
 	}
     }
 
     private void updateImage(TimePeriod period) {
 	progress = ProgressDialog.show(this, "", "Downloading map image...");
 	String archiveDirectory = "weeklyarchives" + (period.year-1) + "-" + period.year;
 	String imageFilename = "usmap" + period.number + ".jpg";
 	String imageUrl = 
 	    IMAGE_URL.replace(IMAGE_DIRECTORY_PLACEHOLDER, archiveDirectory).replace(IMAGE_FILENAME_PLACEHOLDER, imageFilename);
 	new TimePeriodImageDownloaderTask(this).execute(imageUrl);
     }
     
     public static Bitmap getBitmapFromURL(String src) {
         try {
             URL url = new URL(src);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setDoInput(true);
             connection.connect();
             InputStream input = connection.getInputStream();
             Bitmap myBitmap = BitmapFactory.decodeStream(input);
             return myBitmap;
         } catch (IOException e) {
             Log.e(TAG, e.getMessage(), e);
         }
         return null;
     }
 
     public void done(final Bitmap bitmap) {
 	if (progress != null)
 	    progress.dismiss();
 	
 	if (bitmap == null) {
             Toast toast = Toast.makeText(this, "Unable to download image", Toast.LENGTH_SHORT);
             toast.setGravity(Gravity.BOTTOM, 0, 0);
             toast.show();
 	}
 	else {
 	    if (bitmap != null) {
 		mapImage.setImageBitmap(bitmap);
 		imageCache.put(periodSeekbar.getProgress(), bitmap);
 	    }
 	}
     }
     
     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 	final ApplicationController app = (ApplicationController)getApplicationContext();
 	TimePeriod period = app.fluReport.periods.get(progress);
 	mapTitle.setText(period.subtitle);
     }
 
     public void onStartTrackingTouch(SeekBar seekBar) {
 	// TODO Auto-generated method stub
 	
     }
 
     public void onStopTrackingTouch(SeekBar seekBar) {
 	final ApplicationController app = (ApplicationController)getApplicationContext();
 	TimePeriod period = app.fluReport.periods.get(seekBar.getProgress());
 	mapTitle.setText(period.subtitle);
 	if (imageCache.containsKey(seekBar.getProgress()) &&
 		imageCache.get(seekBar.getProgress()) != null) {
 	    done(imageCache.get(seekBar.getProgress()));
 	}
 	else {
 	    //Log.d(TAG, "onStopTrackingTouch -> updateImage");
 	    updateImage(period);
 	}
     }
 
     @Override
     protected void onPause() {
         super.onPause();
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         
         //Log.d(TAG, "onResume");
         
 	ApplicationController app = (ApplicationController)getApplicationContext();
 	if (app.fluReport != null)
 	    done();
 	else
 	    loadFluActivityReport();
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
         outState.putInt(SAVED_CURRENT_PERIOD, this.periodSeekbar.getProgress());
         Bitmap currentBitmap = this.imageCache.get(Integer.valueOf(this.periodSeekbar.getProgress()));
         if (currentBitmap != null) {
             outState.putParcelable(SAVED_CURRENT_BITMAP, currentBitmap);
         }
     }
 }
