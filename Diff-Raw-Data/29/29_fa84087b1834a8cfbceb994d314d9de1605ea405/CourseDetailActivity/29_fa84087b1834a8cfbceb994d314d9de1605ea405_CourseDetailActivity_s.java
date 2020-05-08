 package no.hials.muldvarp.courses;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ProgressDialog;
 import android.content.*;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.widget.Toast;
 import java.io.File;
 import java.io.FileReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.hials.muldvarp.MuldvarpService;
 import no.hials.muldvarp.MuldvarpService.LocalBinder;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.domain.Course;
 import no.hials.muldvarp.utility.DownloadUtilities;
 import no.hials.muldvarp.utility.TabListener;
 import no.hials.muldvarp.view.FragmentPager;
 
 /**
  *
  * @author kristoffer
  */
 public class CourseDetailActivity extends FragmentActivity {
     Course activeCourse;
     MuldvarpService mService;
     LocalBroadcastManager mLocalBroadcastManager;
     BroadcastReceiver     mReceiver;
     boolean mBound = false;
     ProgressDialog dialog;
     Integer id;
     ActionBar actionBar;
     TabListener cdwf;
     TabListener cdhf;
     TabListener cdef;
     FragmentPager pager;
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.course_detail);
         
         if(savedInstanceState == null) {
             dialog = new ProgressDialog(CourseDetailActivity.this);
             dialog.setMessage(getString(R.string.loading));
             dialog.setIndeterminate(true);
             dialog.setCancelable(false);
             dialog.show();
 
             // We use this to send broadcasts within our local process.
             mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
              // We are going to watch for interesting local broadcasts.
             IntentFilter filter = new IntentFilter();
             filter.addAction(MuldvarpService.ACTION_SINGLECOURSE_UPDATE);
             mReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     System.out.println("Got onReceive in BroadcastReceiver " + intent.getAction());
                     if (intent.getAction().compareTo(MuldvarpService.ACTION_SINGLECOURSE_UPDATE) == 0) {                    
                         System.out.println("Toasting" + intent.getAction());
                         Toast.makeText(context, "Course updated", Toast.LENGTH_LONG).show();
                         new GetCourseFromCache().execute(getString(R.string.cacheCourseSingle));
                     } 
                 }
             };
             mLocalBroadcastManager.registerReceiver(mReceiver, filter);
             id = getIntent().getIntExtra("id", 0);
             Intent intent = new Intent(this, MuldvarpService.class);
             bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
         }
         if(actionBar == null) {
 //            cdwf = new TabListener<CourseDetailWorkFragment>(
 //               CourseDetailActivity.this, "Tema", CourseDetailWorkFragment.class);
 //            cdhf = new TabListener<CourseDetailHandinsFragment>(
 //               CourseDetailActivity.this, "Obligatorisk", CourseDetailHandinsFragment.class);
 //            cdef = new TabListener<CourseDetailExamFragment>(
 //               CourseDetailActivity.this, "Eksamen", CourseDetailExamFragment.class);
             
             actionBar = getActionBar();
             actionBar.setHomeButtonEnabled(true);
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
             actionBar.setDisplayShowTitleEnabled(false);  
             actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE); // ??            
 
             pager = (FragmentPager) findViewById(R.id.pager);
             pager.initializeAdapter(getSupportFragmentManager(), actionBar);
                         
 //            ActionBar.Tab tab = actionBar.newTab();
 //            tab.setText(R.string.work)
 //               .setTabListener(cdwf);
             pager.addTab("Tema", CourseDetailWorkFragment.class, null);
             pager.addTab("Obligatorisk", CourseDetailHandinsFragment.class, null);
             pager.addTab("Eksamen", CourseDetailExamFragment.class, null);
 
 //            tab = actionBar.newTab();
 //            tab.setText(R.string.handins)
 //               .setTabListener(cdhf);
 //            actionBar.addTab(tab);
 //
 //            tab = actionBar.newTab();
 //            tab.setText(R.string.exam)
 //               .setTabListener(cdef);
 //            actionBar.addTab(tab);            
         }
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
     }
     
     private class GetCourseFromCache extends AsyncTask<String, Void, Course> {
         protected Course doInBackground(String... urls) {
             Course c = new Course();
             try{
 //                String url = "http://master.uials.no:8080/muldvarp/resources/course/1";
 //                Reader json = new InputStreamReader(DownloadUtilities.getJSONData(url));
 //                c = DownloadUtilities.buildGson().fromJson(json, Course.class);
                 
                 File f = new File(getCacheDir(), urls[0]);
                 c = DownloadUtilities.buildGson().fromJson(new FileReader(f), Course.class);
             }catch(Exception ex){
                 Logger.getLogger(CourseDetailActivity.class.getName()).log(Level.SEVERE, null, ex);
             }
             return c;
         }
         
         @Override
         protected void onPostExecute(Course c) {            
             activeCourse = c;
             
 //            CourseDetailWorkFragment workFragment = (CourseDetailWorkFragment)cdwf.getFragment();
 //            workFragment.ready();
             CourseDetailWorkFragment work = (CourseDetailWorkFragment)pager.getTab(0);
             work.ready(c);
             
             CourseDetailHandinsFragment handin = (CourseDetailHandinsFragment)pager.getTab(1);
             handin.ready(c);
             
             CourseDetailExamFragment exam = (CourseDetailExamFragment)pager.getTab(2);
             exam.ready(c);
             
             dialog.dismiss();
         }
     }
 
     public Course getActiveCourse() {
         return activeCourse;
     }
     
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             LocalBinder binder = (LocalBinder) service;
             mService = binder.getService();
             mBound = true;
             mService.requestCourse(id);
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mBound = false;
         }
     };
     
     @Override
     protected void onStop() {
         super.onStop();
         // Unbind from the service
         if (mBound) {
             unbindService(mConnection);
             mBound = false;
         }
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
     }
 }
