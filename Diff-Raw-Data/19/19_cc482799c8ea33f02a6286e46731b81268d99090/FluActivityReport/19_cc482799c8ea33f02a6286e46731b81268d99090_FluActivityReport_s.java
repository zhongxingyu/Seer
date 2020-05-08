 package com.josephblough.fluchallenge.activities;
 
 import com.josephblough.fluchallenge.ApplicationController;
 import com.josephblough.fluchallenge.R;
 import com.josephblough.fluchallenge.data.TimePeriod;
 import com.josephblough.fluchallenge.services.FluActivityReportDownloaderService;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FluActivityReport extends Activity implements OnSeekBarChangeListener {
 
     private final static String TAG = "FluActivityReport";
 
     private ProgressDialog progress = null;
     public static final String ERROR_MSG = "There was an error downloading the Flu report";
     private static final String SAVED_CURRENT_PERIOD = "FluActivityReport.savedCurrentPeriod";
     private static final String SAVED_ZOOM_STATE = "FluActivityReport.savedZoomedState";
 
     private static final String IMAGE_URL = "http://www.cdc.gov/flu/weekly/%ARCHIVE_DIRECTORY%/images/%IMAGE_FILENAME%";
     private static final String IMAGE_DIRECTORY_PLACEHOLDER = "%ARCHIVE_DIRECTORY%";
     private static final String IMAGE_FILENAME_PLACEHOLDER = "%IMAGE_FILENAME%";
     
 
     private TextView mapTitle = null;
     private SeekBar periodSeekbar = null;
     private WebView mapImage = null;
     private Integer savedImageIndex = null;
     private boolean isZoomEnabled = false;
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.d(TAG, "onCreate");
 
         if (savedInstanceState != null) {
             if (savedInstanceState.containsKey(SAVED_CURRENT_PERIOD)) {
         	this.savedImageIndex = savedInstanceState.getInt(SAVED_CURRENT_PERIOD);
             }
             if (savedInstanceState.containsKey(SAVED_ZOOM_STATE)) {
         	this.isZoomEnabled = savedInstanceState.getBoolean(SAVED_ZOOM_STATE);
             }
         }
         
         setContentView(R.layout.flu_activity_report);
 
         this.mapTitle = (TextView) findViewById(R.id.flu_activity_week_label);
 	this.periodSeekbar = (SeekBar) findViewById(R.id.flu_activity_week_seekbar);
 	this.mapImage = (WebView) findViewById(R.id.flu_activity_map);
 	this.mapImage.setWebViewClient(new WebViewClient() {
 	    @Override
 	    public void onPageFinished(WebView view, String url) {
 	        super.onPageFinished(view, url);
 	        
 		if (progress != null)
 		    progress.dismiss();
 	    }
 	});
 	this.mapImage.getSettings().setSupportZoom(false);
 	this.mapImage.setBackgroundColor(0);
 
 	this.periodSeekbar.setOnSeekBarChangeListener(this);
     }
     
     private void loadFluActivityReport() {
 	Intent intent = new Intent(this, FluActivityReportDownloaderService.class);
 	intent.putExtra(FluActivityReportDownloaderService.EXTRA_MESSENGER,
 		new Messenger(new Handler() {
 		    @Override
 		    public void handleMessage(Message message) {
 			if (message.arg1 == Activity.RESULT_OK) {
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
 
 	progress = ProgressDialog.show(this, "", "Downloading map image...");
 	mapImage.loadDataWithBaseURL(null, getImageWebPageHtml(period), "text/html", "utf-8", null);
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
	progress = ProgressDialog.show(this, "", "Downloading map image...");
	mapImage.loadDataWithBaseURL(null, getImageWebPageHtml(period), "text/html", "utf-8", null);
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
     
     private String generateUrl(final TimePeriod period) {
 	String archiveDirectory = "weeklyarchives" + (period.year-1) + "-" + period.year;
 	String imageFilename = "usmap" + period.number + ".jpg";
 	String imageUrl = 
 	    IMAGE_URL.replace(IMAGE_DIRECTORY_PLACEHOLDER, archiveDirectory).
 	    replace(IMAGE_FILENAME_PLACEHOLDER, imageFilename);
 	return imageUrl;
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         
         outState.putInt(SAVED_CURRENT_PERIOD, this.periodSeekbar.getProgress());
         outState.putBoolean(SAVED_ZOOM_STATE, this.isZoomEnabled);
     }
 
     private String getImageWebPageHtml() {
 	ApplicationController app = (ApplicationController)getApplicationContext();
 	TimePeriod period = app.fluReport.periods.get(this.periodSeekbar.getProgress());
 	return getImageWebPageHtml(period);
     }
     
     private String getImageWebPageHtml(final TimePeriod period) {
 	if (isZoomEnabled) {
 	    return "<html><body><center><img src=\"" + generateUrl(period) + "\" /></center></body></html>";
 	}
 	else {
 	    //final String sizingAttribute = (mapImage.getHeight() >= mapImage.getWidth()) ? "width" : "height";
 	    final String sizingAttribute = 
 		(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) ? "width" : "height";
 	    return "<html><body><center><img src=\"" + generateUrl(period) + "\" " + sizingAttribute + "=\"100%\" /></center></body></html>";
 	}
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
 	MenuInflater inflater = getMenuInflater();
 	inflater.inflate(R.menu.flu_activity_report_menu, menu);
 	//zoomMenuItem.setChecked(isZoomEnabled);
 	return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
 	switch (item.getItemId()) {
 	case R.id.menu_item_toggle_zoom:
 	    final String title = getString((!isZoomEnabled) ? R.string.menu_item_disable_zoom : R.string.menu_item_enable_zoom);
 	    //Log.d(TAG, "Setting title to " + title);
 	    item.setTitle(title);
 	    toggleZoom();
 	    break;
 	}
 	return super.onOptionsItemSelected(item);
     }
     
     private void toggleZoom() {
 	isZoomEnabled = !isZoomEnabled;
 	this.mapImage.getSettings().setSupportZoom(isZoomEnabled);
 	this.mapImage.getSettings().setBuiltInZoomControls(isZoomEnabled);
 	mapImage.loadDataWithBaseURL(null, getImageWebPageHtml(), "text/html", "utf-8", null);
     }
 }
