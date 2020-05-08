 package com.example.careapp;
 
 import java.io.File;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 public class ResourcesActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_resources);
 		// Show the Up button in the action bar.
 		setupActionBar();
 	}
 	
 	//Button Listeners
 	public void onJoinButton(View view) {
 		
 	}
 	
 	public void onOfficesButton(View view) {
		//File file = new File("mnt/sdcard/Download/offices.pdf");
		
		File file = new File(getExternalFilesDir(null)+"/offices.pdf");
		//File file2 = new File(file.getAbsolutePath() + "/offices.pdf")
        Toast.makeText(ResourcesActivity.this, file.toString(), Toast.LENGTH_LONG).show();
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && file.exists()) {
             Uri path = Uri.fromFile(file);
             Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setDataAndType(path, "application/pdf");
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
             try {
                 startActivity(intent);
             } 
             catch (ActivityNotFoundException e) {
                 Toast.makeText(ResourcesActivity.this, 
                     "No Application Available to View PDF", 
                    Toast.LENGTH_LONG).show();
             }
         } else {
 			String url = "http://intraweb.stockton.edu/eyos/dean_students/content/docs/Office%20Locations%20EYOS.pdf";
 			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
 			request.setDescription("Download Stockton PDF");
 			request.setTitle("Offices");
 			// in order for this if to run, you must use the android 3.2 to compile your app
 			/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			    request.allowScanningByMediaScanner();
 			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 			}*/
			request.setDestinationInExternalFilesDir(this, null, "office.pdf");
 			// get download service and enqueue file
 			DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
 			manager.enqueue(request);
         }
 	}
 	
 	public void onImprovementButton(View view) {
 		File file = new File("mnt/sdcard/Download/improvement.pdf");
 
         if (file.exists()) {
             Uri path = Uri.fromFile(file);
             Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setDataAndType(path, "application/pdf");
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
             try {
                 startActivity(intent);
             } 
             catch (ActivityNotFoundException e) {
                 Toast.makeText(ResourcesActivity.this, 
                     "No Application Available to View PDF", 
                     Toast.LENGTH_SHORT).show();
             }
         } else {
 			String url = "http://intraweb.stockton.edu/eyos/dean_students/content/docs/MIP%20in%20Writeable%20%283%29.pdf";
 			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
 			request.setDescription("Download Stockton PDF");
 			request.setTitle("Improvement Plan");
 			// in order for this if to run, you must use the android 3.2 to compile your app
 			/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			    request.allowScanningByMediaScanner();
 			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 			}*/
 			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "improvement.pdf");
 	
 			// get download service and enqueue file
 			DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
 			manager.enqueue(request);
         }
 	}
 	
 	public void onUpdateButton(View view) {
 		
 	}
 	
 	public void onGuidebookButton(View view) {
 		File file = new File("mnt/sdcard/Download/guidebook.pdf");
 
         if (file.exists()) {
             Uri path = Uri.fromFile(file);
             Intent intent = new Intent(Intent.ACTION_VIEW);
             intent.setDataAndType(path, "application/pdf");
             intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
             try {
                 startActivity(intent);
             } 
             catch (ActivityNotFoundException e) {
                 Toast.makeText(ResourcesActivity.this, 
                     "No Application Available to View PDF", 
                     Toast.LENGTH_SHORT).show();
             }
         } else {
 			String url = "http://intraweb.stockton.edu/eyos/dean_students/content/docs/CARE%20Mentor%20Guide%20Book.pdf";
 			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
 			request.setDescription("Download Stockton PDF");
 			request.setTitle("Guidebook");
 			// in order for this if to run, you must use the android 3.2 to compile your app
 			/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			    request.allowScanningByMediaScanner();
 			    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 			}*/
 			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "guidebook.pdf");
 	
 			// get download service and enqueue file
 			DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
 			manager.enqueue(request);
         }
 	}
 	
 	public void onFAQButton(View view) {
 		
 	}
 	
 	public void onWorkshopsButton(View view) {
 		Intent intent = new Intent(this, WorkshopsActivity.class);
         startActivity(intent);  
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.resources, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
