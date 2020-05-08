 package com.challengecomplete.android.activity;
 
 import java.util.Date;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.FrameLayout;
 
 import com.challengecomplete.android.R;
 import com.challengecomplete.android.fragment.CurrentGoalsFragment;
 import com.challengecomplete.android.fragment.MainFragment;
 import com.challengecomplete.android.fragment.SideFragment;
 import com.challengecomplete.android.models.goals.GoalContentProvider;
 import com.challengecomplete.android.models.goals.GoalProcessor;
 import com.challengecomplete.android.models.goals.GoalProcessor.SyncContentValues;
 import com.challengecomplete.android.models.goals.GoalTable;
 import com.challengecomplete.android.service.APIService;
 import com.challengecomplete.android.service.ServiceHelper;
 import com.challengecomplete.android.service.ServiceReceiver;
 import com.challengecomplete.android.utils.ChallengeComplete;
 import com.challengecomplete.android.view.ScrollView;
 
 public class MainActivity extends FragmentActivity implements ServiceReceiver.Receiver{
 	private static final String TAG = "MainActivity";
 	
 	private static final int INTENT_LOGIN = 1;
 	
 	private int currentFragment;
 	public static final int FRAGMENT_CURRENTGOALS = 1;
 	public static final int FRAGMENT_BUCKETGOALS = 2;
 	
 	private ScrollView mScrollView;
 	private SideFragment mSideFragment;
 	public ServiceReceiver mReceiver;
 	private ProgressDialog mProgressDialog;
 	
 	private int fetchMeId = -1;
 	private int syncId = -1;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         mScrollView = (ScrollView) findViewById(R.id.scrollview);
         
         FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_container);
 //        MainFragment mFragment = new MainFragment();
         CurrentGoalsFragment mFragment = new CurrentGoalsFragment();
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         ft.add(fl.getId(), mFragment);
         currentFragment = FRAGMENT_CURRENTGOALS;
         
         FrameLayout sp = (FrameLayout) findViewById(R.id.side_panel);
         mSideFragment = new SideFragment();
         ft.add(sp.getId(), mSideFragment);
   
         ft.commit();
         
         // Initializing home button
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
         
         // Setup receivers
 		mReceiver = new ServiceReceiver(new Handler());
         mReceiver.setReceiver(this);
         
         // If user is not logged in, start LoginActivity
         if (!ChallengeComplete.isLoggedIn(this)){
         	Intent intent = new Intent(this, LoginActivity.class);
         	startActivityForResult(intent, INTENT_LOGIN);
         	return;
         }
         
         // Otherwise, fetch /api/me
         fetch();
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch (requestCode){
     		case INTENT_LOGIN:
     			fetchMe();
     			break;
     	}
     }
     
     public void fetch(){
     	fetchMe();
         syncGoals();
     }
     
     public void fetchMe(){
     	// Only show dialog if first time fetching
     	if (!ChallengeComplete.hasFetchedMe(this))
     		mProgressDialog = ChallengeComplete.showDialog(this);
 		
 		Bundle extras = new Bundle();
 		extras.putParcelable(ServiceReceiver.NAME, (Parcelable) mReceiver);
 		
         ServiceHelper mServiceHelper = ServiceHelper.getInstance();
     	int taskId = mServiceHelper.startService(this, ServiceHelper.GET_ME, extras);
     	
     	fetchMeId = taskId;
     	
     	Log.i(TAG, "TaskId: " + taskId);
 	}
     
     public void syncGoals(){
     	Bundle extras = new Bundle();
 		extras.putParcelable(ServiceReceiver.NAME, (Parcelable) mReceiver);
 		
         ServiceHelper mServiceHelper = ServiceHelper.getInstance();
     	int taskId = mServiceHelper.startService(this, ServiceHelper.SYNC, extras);
     	
     	syncId= taskId;
     	
     	Log.i(TAG, "TaskId: " + taskId);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {    
     	switch (item.getItemId()) {        
           case android.R.id.home:
         	  if (mScrollView.isOpened())
         		  mScrollView.bounce();
         	  else
         		  mScrollView.open();         
         	  return true;        
           default:            
         	  return super.onOptionsItemSelected(item);    
     	}
     }
     
     // Switching the fragment container view to another fragment
     public void switchFragment(int fragmentId){
     	if (currentFragment == fragmentId) {
    		mScrollView.close();
     		return;
     	}
     	
     	FrameLayout fl = (FrameLayout) findViewById(R.id.fragment_container);
     	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
     	Fragment mFragment = null;
     	
     	switch (fragmentId) {
     		case FRAGMENT_CURRENTGOALS:
     			mFragment = new CurrentGoalsFragment();
     			break;
     		case FRAGMENT_BUCKETGOALS:
     			mFragment = new MainFragment();
     			break;
     		default:
     			return;
     	}
     	
     	mScrollView.scrollOut(getFragmentSwitchRunnable(ft, mFragment, fl, fragmentId));
 
     }
     
     // Callback for scrollOut
     public Runnable getFragmentSwitchRunnable(final FragmentTransaction ft, 
     		final Fragment mFragment, final FrameLayout fl, final int fragmentId){
     	
     	return new Runnable() {
 	    	@Override
 			public void run() {
 		    	currentFragment = fragmentId;
 		    	ft.replace(fl.getId(), mFragment);
 		    	ft.commit();
 				mScrollView.close();
 			}
     	};
     }
 
 	@Override
 	public void onReceiveResult(int resultCode, Bundle resultData) {
 		String results = resultData.getString(APIService.RESULTS);
 		int taskId = resultData.getInt(APIService.TASK_ID);
 		
 		if (taskId == fetchMeId){
 
 			if (results != null){
 				try {
 					JSONObject jObject = new JSONObject(results);
 					ChallengeComplete.setUserId(this, jObject.getInt("id"));
 					ChallengeComplete.setUserAvatar(this, jObject.getString("avatar"));
 					ChallengeComplete.setUserName(this, jObject.getString("name"));
 					ChallengeComplete.setUserPointsTotal(this, jObject.getInt("points"));
 					ChallengeComplete.setUserPointsMonth(this, jObject.getInt("points_this_month"));
 					mSideFragment.displayUser();
 					ChallengeComplete.setFetchedMe(this);
 				} catch (JSONException e){}
 			}
 			
 			fetchMeId = -1;
 			
 			// Sync after fetch
 	        syncGoals();
 			
 		} else if (taskId == syncId){
 			Log.i("RESULTS", results);
 			if (results != null) {
 				SyncContentValues contentValues = GoalProcessor.bulkCreateContentValues(results);
 				
 				ContentValues[] createdContentValues = contentValues.created;
 				ContentValues[] updatedContentValues = contentValues.updated;
 				
 				if (createdContentValues != null && createdContentValues.length > 0)
 					getContentResolver().bulkInsert(GoalContentProvider.CONTENT_URI, createdContentValues);
 				
 				if (updatedContentValues != null){
 					for (int i = 0; i < updatedContentValues.length; i++){
 						ContentValues cv = updatedContentValues[i];
 						getContentResolver().update(GoalContentProvider.CONTENT_URI, 
 								updatedContentValues[i], GoalTable.COLUMN_ID + "=" + cv.get(GoalTable.COLUMN_ID), null);
 					}
 				}
 				
 				
 				ChallengeComplete.setLastSynced(this,  new Date().getTime()/1000);
 				// TODO
 				// Notify Processor.
 				// getProcessor by Id -> runTask (GET_TASKS) to update database
 	//			getContentResolver()
 			}	
 			syncId = -1;
 		}
 		
 		ChallengeComplete.dismissDialog(mProgressDialog);
 	}
 }
