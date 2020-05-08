 package us.happ.android.activity;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import us.happ.android.R;
 import us.happ.android.fragment.BoardFragment;
 import us.happ.android.fragment.ContactsFragment;
 import us.happ.android.fragment.HappFragment;
 import us.happ.android.service.APIService;
 import us.happ.android.service.ServiceHelper;
 import us.happ.android.service.ServiceReceiver;
 import us.happ.android.utils.ContactsManager;
 import us.happ.android.utils.Storage;
 import us.happ.android.view.Drawer;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Parcelable;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.Window;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends ActionBarActivity implements ServiceReceiver.Receiver {
	
	private boolean activityDestroyed = false;
	
 	// Activity results flags
 	private static final int ACTIVITY_COMPOSE = 0x01;
 	
 	// receiver flags
 	private int getMoodsId = -1;
 	private int postMoodsId = -1;
 	
 	// Fragments
 	private BoardFragment mBoardFragment;
 	private ContactsFragment mFriendsFragment;
 
 	
 	// Fragment IDs
 	private static final int FRAGMENT_BOARD = 0x01;
 	private static final int FRAGMENT_FRIENDS = 0x02;
 	private int fragmentId;
 	private HappFragment mFragment;
 	
 	private ServiceReceiver mReceiver;
 	private String mPhoneNumber;
 	private ActionBar actionbar;
 	private Drawer mDrawer;
 	private DrawerLayout mDrawerLayout;
 	private ActionBarDrawerToggle mDrawerToggle;
 	private ProgressDialog mProgressDialog;
 	private ContactsManager mContactsManager;
 	private ServiceHelper mServiceHelper;
 
 	private FragmentTransaction mFragmentTransaction;
 	
 	private TextView selectedMenuItem;
 	private TextView menuHappening;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
         
 		setContentView(R.layout.activity_main);
 		
 		mContactsManager = ContactsManager.getInstance(this);
 		
 		mFragmentTransaction = getSupportFragmentManager().beginTransaction();
 		
 		// Default view = happening board
 		menuHappening = (TextView) findViewById(R.id.menu_happening);
 		selectMenuItem(menuHappening);
 		mBoardFragment = new BoardFragment();
 		fragmentId = FRAGMENT_BOARD;
 		mFragment = mBoardFragment;
 		mFragmentTransaction.add(R.id.content_frame, mBoardFragment);
 		mFragmentTransaction.commit();
 		
 		// Setup receivers
         mReceiver = new ServiceReceiver(new Handler());
         mReceiver.setReceiver(this);
         
         // Get self number
         TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
         mPhoneNumber = ContactsManager.cleanNumber(tMgr.getLine1Number());
         
         // Action Bar
         actionbar = getSupportActionBar();
         actionbar.setDisplayHomeAsUpEnabled(true);
         actionbar.setDisplayShowTitleEnabled(false);
         
         // Navigation drawer
         mDrawer = (Drawer) findViewById(R.id.drawer);
         mDrawer.setOnTouchListener(new OnTouchListener(){
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return true;
 			}
         });
         
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerToggle = new ActionBarDrawerToggle(
                 this,                  /* host Activity */
                 mDrawerLayout,         /* DrawerLayout object */
                 R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                 R.string.drawer_open,  /* "open drawer" description */
                 R.string.drawer_close  /* "close drawer" description */
                 ) {
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
 
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
 
             }
         };
 
         // Set the drawer toggle as the DrawerListener
         mDrawerLayout.setDrawerListener(mDrawerToggle);
         
         // Progress dialog
         mProgressDialog = new ProgressDialog(this);
         
         // Service
         mServiceHelper = ServiceHelper.getInstance();
 		
         // Previously done in async task
         if (!mContactsManager.hasFetchedContacts()){
 	        new fetchContactsTask().execute("");
 	        mProgressDialog.setMessage(getResources().getString(R.string.dialog_retrieve_contacts));
 	    	mProgressDialog.show();
         }
         
 	}
 	
 	@Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 	
 	private class fetchContactsTask extends AsyncTask<String, Void, String> {
 
         @Override
         protected String doInBackground(String... params) {
         	mContactsManager.makeContactsMapping();
         	return "";
         }      
 
         @Override
         protected void onPostExecute(String results) {
         	if (fragmentId != FRAGMENT_BOARD)
         		mProgressDialog.dismiss();
         }
 
         @Override
         protected void onPreExecute() {}
 
         @Override
         protected void onProgressUpdate(Void... values) {}
         
 	}
 	
 	@Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {   
 		
 		if (mDrawerToggle.onOptionsItemSelected(item))
           return true;
 		
     	switch (item.getItemId()) {        
           case android.R.id.home:
         	  return true;
           default:            
         	  return super.onOptionsItemSelected(item);
     	}
     }
 	
 	@Override
 	public void onBackPressed(){
 		if (fragmentId != FRAGMENT_BOARD){
 			switchFragment(FRAGMENT_BOARD);
 			selectMenuItem(menuHappening);
 			return;
 		}
 		if (mFragment.onBackPressed()) return;
 		super.onBackPressed();
 	}
 	
 	public void compose(){
 		mBoardFragment.closeFooter(false);
 		Intent intent = new Intent(this, ComposeActivity.class);
 		startActivityForResult(intent, ACTIVITY_COMPOSE);
 		overridePendingTransition(R.anim.slide_up, R.anim.fade_out);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (requestCode == ACTIVITY_COMPOSE && resultCode == 1){
 
 			Toast.makeText(this, getResources().getString(R.string.toast_post_progress), Toast.LENGTH_SHORT).show();
 			
 			String msg = data.getStringExtra("compose_msg");
 			int tag = data.getIntExtra("compose_tag", 1);
 			String duration = data.getStringExtra("compose_duration");
 			
 			mBoardFragment.setHeader(msg, tag);
 			
 			Bundle extras = new Bundle();
 			String[] numbers = mContactsManager.getAllFriends();
 			extras.putStringArray("n", numbers);
 			extras.putString("number", mPhoneNumber+"");
 			extras.putString("msg", msg);
 			extras.putString("tag", tag+"");
 			extras.putString("duration", duration);
 	        extras.putParcelable(ServiceReceiver.NAME, (Parcelable) mReceiver);
 	        ServiceHelper mServiceHelper = ServiceHelper.getInstance();
 	        postMoodsId = mServiceHelper.startService(this, ServiceHelper.POST_MOODS, extras);
 		}
 	}
     	
 	public void startFetchService(){
 		Bundle extras = new Bundle();
 		String[] numbers = mContactsManager.getAllFriends();
 		extras.putStringArray("n", numbers);
 		extras.putString("me", mPhoneNumber);
 		extras.putParcelable(ServiceReceiver.NAME, (Parcelable) mReceiver);
 		getMoodsId = mServiceHelper.startService(this, ServiceHelper.GET_MOODS, extras);
 	}
 	
 	@Override
 	public void onReceiveResult(int resultCode, Bundle resultData) {
		if (activityDestroyed) return;
		
 		String results = resultData.getString(APIService.RESULTS);
 		int taskId = resultData.getInt(APIService.TASK_ID);
 		
 		if (taskId == getMoodsId){
			if (fragmentId == FRAGMENT_BOARD)
				mBoardFragment.onFetchResult(results);
 			mProgressDialog.dismiss();
 			getMoodsId = -1;
 		} else if (taskId == postMoodsId){
 			try {
 				JSONObject jResults = new JSONObject(results);
 				if (jResults.getInt("status") == 200){
 					Toast.makeText(this, getResources().getString(R.string.toast_post_success), Toast.LENGTH_SHORT).show();
 					Storage.incTotalHapps(this);
 					mDrawer.updateTotalHapps();
 				} else {
 					Toast.makeText(this, getResources().getString(R.string.toast_post_error), Toast.LENGTH_SHORT).show();
 				}
 			} catch (JSONException e){}
 			
 			postMoodsId = -1;
 		}
 		
 		hideSpinner();
 		
 		if (results != null) Log.i("results", results);
 	}
 	
 	public void setProgressDialog(String msg){
 		mProgressDialog.setMessage(msg);
 		mProgressDialog.show();
 	}
 	
	@Override
	public void onDestroy(){
		activityDestroyed = true;
		super.onDestroy();
	}
	
 	/**
 	 * Menu clicks
 	 */
 	public void switchFragment(int id){
     	if (fragmentId == id) {
     		mDrawerLayout.closeDrawers();
     		return;
     	}
     	
     	mFragmentTransaction = getSupportFragmentManager().beginTransaction();
     	
     	if (fragmentId == FRAGMENT_BOARD){
     		mFragmentTransaction.detach(mFragment);
     	} else {
     		mFragmentTransaction.remove(mFragment);
     	}
     	
     	switch (id) {
     		case FRAGMENT_BOARD:
     			if (mBoardFragment == null)
     				mBoardFragment = new BoardFragment();
     			mFragment = mBoardFragment;
     			break;
     		case FRAGMENT_FRIENDS:
     			if (mFriendsFragment == null)
     				mFriendsFragment = new ContactsFragment();
     			mFragment = mFriendsFragment;
     			break;
     		default:
     			return;
     	}
     	
     	if (id == FRAGMENT_BOARD){
     		mFragmentTransaction.attach(mFragment);
     	} else {
     		mFragmentTransaction.add(R.id.content_frame, mFragment);
     	}
     	
     	fragmentId = id;
     	if (mDrawerLayout.isDrawerOpen(mDrawer)){
     		scheduleSwitchFragment();
     	} else {
     		mFragmentTransaction.commit();
     	}
 	}
 	
 	private final Handler drawerHandler = new Handler();
 	
 	private void scheduleSwitchFragment(){
 		drawerHandler.removeCallbacksAndMessages(null);
 		drawerHandler.postDelayed(new Runnable(){
 			@Override
 			public void run(){
 				mFragmentTransaction.commit();
 			}
 		}, 250);
 		mDrawerLayout.closeDrawers();
 	}
 	
 	// Menus
 	private void selectMenuItem(TextView v){
 		if (selectedMenuItem != null){
 			selectedMenuItem.setTextColor(getResources().getColor(R.color.white));
 		}
 		selectedMenuItem = v;
 		v.setTextColor(getResources().getColor(R.color.happ_purple_highlight));
 	}
 	
 	public void showSpinner(){
         setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
 	}
 	
 	public void hideSpinner(){
         setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
 	}
 	
 	public void returnToBoard(){
 		switchFragment(FRAGMENT_BOARD);
 		selectMenuItem(menuHappening);
 	}
 	
 	public void onClickBoard(View v){
 		returnToBoard();
 	}
 	
 	public void onClickFriends(View v){
 		switchFragment(FRAGMENT_FRIENDS);
 		selectMenuItem((TextView) v);
 	}
 }
