 package com.hag.bucketlst.activity;
 
 // test push from eclipse in testBranch?
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TabHost;
 
 import com.hag.bucketlst.R;
 import com.hag.bucketlst.application.BLApp;
 import com.hag.bucketlst.customWindows.CustomTab;
 
 public class BucketMain extends CustomTab {
     /** Called when the activity is first created. */
     
 	private static final int ACTIVITY_GET_DISPNAME = 1010;
 	private Thread splashTread;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
        this.title.setText("bucketLST");
         
         if(BLApp.firstRun(this)){
         	splashTread = new Thread () {
         		@Override
                 public void run() {
         			Intent getDispName = new Intent(getApplicationContext(), runOnce.class);
         			startActivityForResult(getDispName, ACTIVITY_GET_DISPNAME);
         		}
         	};
         	splashTread.start();
         }
 
         //Resources res = getResources(); // Resource object to get Drawables
         TabHost tabHost = getTabHost();  // The activity TabHost
         TabHost.TabSpec spec;  // Resusable TabSpec for each tab
         Intent intent;  // Reusable Intent for each tab
 
         // Create an Intent to launch an Activity for the tab (to be reused)
         intent = new Intent().setClass(this, MyTasks.class);
         // Initialize a TabSpec for each tab and add it to the TabHost
         spec = tabHost.newTabSpec("My Tasks").setIndicator("My Tasks").setContent(intent);
         tabHost.addTab(spec);
 
         // Do the same for the other tabs
         intent = new Intent().setClass(this, MyDoneTasks.class);
         spec = tabHost.newTabSpec("Checked Tasks").setIndicator("My Done Tasks").setContent(intent);
         tabHost.addTab(spec);
 
         intent = new Intent().setClass(this, NCategoryView.class);
         spec = tabHost.newTabSpec("Bucket Sort").setIndicator("My Buckets").setContent(intent);
         tabHost.addTab(spec);
 
         tabHost.setCurrentTab(0);
     }
    
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent)
     {
     	super.onActivityResult(requestCode, resultCode, intent);  
         if ((requestCode == ACTIVITY_GET_DISPNAME) && (resultCode == RESULT_OK))
         {
         	splashTread.stop();
         }
         
         if ((requestCode == ACTIVITY_GET_DISPNAME) && (resultCode == RESULT_CANCELED))
         {
         	finish();
         }
 	}
     
 }
