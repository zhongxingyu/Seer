 package com.timetravellingtreasurechest.gui;
 
 import com.timetravellingtreasurechest.app.MainActivity;
 import com.timetravellingtreasurechest.app.R;
 import com.timetravellingtreasurechest.report.ReportData;
 import com.timetravellingtreasurechest.services.DatabaseService;
 import com.timetravellingtreasurechest.services.ImageConverter;
 import com.timetravellingtreasurechest.services.ServiceServer;
 import com.timetravellingtreasurechest.share.IReportSharingService;
 import com.timetravellingtreasurechest.share.ReportSharingService;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ManageReportActivity extends Activity {
 	
 	// Dependencies
 	ReportData usingReport;
 	ReportSharingService reportSharingService = new ReportSharingService();
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 //        MainActivity.cameraSurfaceView.stopPreview();
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_manage_report);
 		MainActivity.addLegacyOverflowButton(this.getWindow());
         TextView reportText = (TextView)this.findViewById(R.id.reportText);
         usingReport = MainActivity.latestReport;//(ReportData) getIntent().getSerializableExtra(MainActivity.REPORT);
         reportText.setText(usingReport == null ? "Missing Report" : usingReport.getReportText());
         Button share = (Button)this.findViewById(R.id.Share);
         share.setOnClickListener(new OnClickListener() {
                 public void onClick(View v) {
 	            		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 	                    sharingIntent.setType("image/jpeg");
 	                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Physiognomy reading!");
 	                    sharingIntent.putExtra(Intent.EXTRA_TEXT, MainActivity.latestReport.getReportText());
 	                    sharingIntent.putExtra("sms_body", MainActivity.latestReport.getReportText());
 	                    sharingIntent.putExtra(Intent.EXTRA_STREAM, MainActivity.latestReport.getImageUri());
 	                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
 //	                	Intent myIntent = new Intent();
 //	                	myIntent.setClassName(ManageReportActivity.this, "com.timetravellingtreasurechest.gui.ShareActivity");
 //	                	//myIntent.putExtra(MainActivity.REPORT, usingReport);
 //	                	startActivity(myIntent);  
                 }
         });
         
         ImageView reportImage = (ImageView) this.findViewById(R.id.reportImage);
         try {
         	reportImage.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), MainActivity.latestReport.getImageUri()));
         } catch (Exception e) {
         	e.printStackTrace();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
     	menu.add(0, 1, 0, "Report History"); //.setIntent(new Intent().setClassName(ServiceServer.getAndroidContext(), "com.timetravellingtreasurechest.gui.ReportHistoryActivity"));
     	menu.add(0, 2, 0, "Delete Report");
     	getMenuInflater().inflate(R.menu.report_history, menu);
     	
         return super.onCreateOptionsMenu(menu);
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
     	Intent intent;
     	
     	switch (item.getItemId()) {
     	case 1:
     		intent = new Intent();
     		intent.setClassName(ServiceServer.getAndroidContext(), "com.timetravellingtreasurechest.gui.ReportHistoryActivity");
     		startActivity(intent);
     		return true;
     	case 2:
     		DatabaseService db = new DatabaseService(ServiceServer.getAndroidContext());
     		db.deleteReport(MainActivity.latestReport.getImageUri().getPath());
     		intent = new Intent();
     		intent.setClassName(ServiceServer.getAndroidContext(), "com.timetravellingtreasurechest.app.MainActivity");
     		startActivity(intent);
     		return true;
     	}
         return super.onOptionsItemSelected(item);
     }
 }
