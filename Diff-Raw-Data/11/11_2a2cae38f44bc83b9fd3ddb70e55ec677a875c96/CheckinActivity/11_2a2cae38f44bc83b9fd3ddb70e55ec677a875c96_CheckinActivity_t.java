 /**
  * 
  */
 package com.googolmo.foursquare.app;
 
 import android.app.ProgressDialog;
 import android.os.*;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.*;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.googolmo.foursquare.BaseActivity;
 import com.googolmo.foursquare.CheckInApplication;
 import com.googolmo.foursquare.Constants;
 import com.googolmo.foursquare.R;
 import com.googolmo.foursquare.utils.LogUtil;
 
 import fi.foyt.foursquare.api.FoursquareApiException;
 import fi.foyt.foursquare.api.Result;
 import fi.foyt.foursquare.api.ResultMeta;
 import fi.foyt.foursquare.api.entities.Checkin;
 import fi.foyt.foursquare.api.entities.CompactVenue;
 import fi.foyt.foursquare.api.entities.CompleteVenue;
 import fi.foyt.foursquare.api.entities.VenuesSearchResult;
 
 import java.util.ArrayList;
 
 /**
  * @author googolmo
  * 
  */
 public class CheckinActivity extends SherlockActivity {
 
 	private LogUtil mLog = new LogUtil(CheckinActivity.class.getName());
 
     private ActionBar mActionBar;
     private EditText mEditText_Shout;
     private Switch mSwitch_ShareFriends;
     private Switch mSwitch_ShareTwitter;
     private Switch mSwitch_ShareFB;
     private CheckBox mCB_ShareFriends;
     private CheckBox mCB_ShareTwitter;
     private CheckBox mCB_ShareFB;
     private CompactVenue mVenue;
 //    private String mVenueID;
 
     private boolean mShareFriends;
     private boolean  mShareTwitter;
     private boolean mShareFB;
     private ProgressDialog mDialog;
 
     private CheckinAsyncTask mCheckinAsyncTask;
     private ArrayList<CheckinAsyncTask> mAsyncTaskList;
 
 
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.act_checkin);
 
         mActionBar = getSupportActionBar();
 
         mShareFriends = true;
         mShareTwitter = true;
         mShareFB = true;
 
         this.setupView();
 
         try {
             mVenue = (CompactVenue)getIntent().getSerializableExtra("venue");
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
         mLog.debug("================================" + mVenue.getId());
         mAsyncTaskList = new ArrayList<CheckinAsyncTask>();
        mActionBar.setTitle(mVenue.getName());
 
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(Menu.FIRST, Menu.FIRST, Menu.FIRST, "Check in")
                 .setIcon(R.drawable.ic_action_send)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if(item.getItemId() == Menu.FIRST) {
             doCheckin();
         }
         return super.onOptionsItemSelected(item);
     }
 
 	private void setupView() {
         mEditText_Shout = (EditText)findViewById(R.id.checkin_edittext);
         if (Build.VERSION.SDK_INT >= 14) {
             mSwitch_ShareFriends = (Switch)findViewById(R.id.checkin_share_friends_switch);
             mSwitch_ShareTwitter = (Switch)findViewById(R.id.checkin_share_twitter_switch);
             mSwitch_ShareFB = (Switch)findViewById(R.id.checkin_share_fb_switch);
             mSwitch_ShareFriends.setChecked(true);
             mSwitch_ShareTwitter.setChecked(true);
             mSwitch_ShareFB.setChecked(true);
 
             mSwitch_ShareFriends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     if (!isChecked) {
                         mShareFriends = false;
                         mSwitch_ShareTwitter.setEnabled(false);
                         mSwitch_ShareFB.setEnabled(false);
                     } else {
                         mShareFriends = true;
                         mSwitch_ShareTwitter.setEnabled(true);
                         mSwitch_ShareFB.setEnabled(true);
                     }
                 }
             });
             mSwitch_ShareTwitter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     mShareTwitter = isChecked;
                 }
             });
             mSwitch_ShareFB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     mShareFB = isChecked;
                 }
             });
         } else {
             mCB_ShareFriends = (CheckBox)findViewById(R.id.checkin_share_friends_cb);
             mCB_ShareTwitter = (CheckBox)findViewById(R.id.checkin_share_twitter_cb);
             mCB_ShareFB = (CheckBox)findViewById(R.id.checkin_share_fb_cb);
             mCB_ShareFriends.setChecked(true);
             mCB_ShareTwitter.setChecked(true);
             mCB_ShareFB.setChecked(true);
 
             mCB_ShareFriends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     if (!isChecked) {
                         mShareFriends = false;
                         mCB_ShareTwitter.setEnabled(false);
                         mCB_ShareFB.setEnabled(false);
                     } else {
                         mShareFriends = true;
                         mCB_ShareTwitter.setEnabled(true);
                         mCB_ShareFB.setEnabled(true);
                     }
                 }
             });
 
             mCB_ShareTwitter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     mShareTwitter = isChecked;
                 }
             });
 
             mCB_ShareFB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                 @Override
                 public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                     mShareFB = isChecked;
                 }
             });
         }
 	}
 
 //	private void initData() {
 //		mBundle = getIntent().getExtras().getBundle("bundle");
 //		this.mVenueId = mBundle.getString("venueId");
 //		this.mVenueName = mBundle.getString("venueName");
 //		this.mVenueLL = mBundle.getDouble("lat") + ","
 //				+ mBundle.getDouble("lng");
 //		log.debug(mVenueLL);
 //
 //		mIsShared = true;
 //		mIsSharedFB = true;
 //		mIsSharedTwitter = true;
 //	}
 
 
 
 
 
     private void doCheckin() {
         mCheckinAsyncTask = new CheckinAsyncTask();
         mCheckinAsyncTask.execute();
         mAsyncTaskList.add(mCheckinAsyncTask);
     }
 
     private String getBoradcastString() {
         String value = "public";
         if (mShareFriends == false) {
             value = "private";
         } else {
             if (mShareTwitter) {
                 value += ",twitter";
             }
             if (mShareFB) {
                 value += ",facebook";
             }
         }
         return value;
     }
 
     private void doAfterSuccess(Checkin result) {
         //Toast.makeText(this, result.getSource().getName(), Toast.LENGTH_SHORT).show();
         this.finish();
     }
 
     private void doAfterError(ResultMeta error){
         //TODO
         if (error == null) {
             //TODO
         } else {
             Toast.makeText(this, error.getErrorDetail(), Toast.LENGTH_SHORT).show();
         }
     }
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onRestart() {
 		// TODO Auto-generated method stub
 		super.onRestart();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 
     private class CheckinAsyncTask extends AsyncTask<Void,Void,Result<Checkin>> {
 
 
         @Override
         protected Result<Checkin> doInBackground(Void... params) {
             try {
                 String shout = null;
                 if (mEditText_Shout.getText().toString().length() > 0) {
                     shout = mEditText_Shout.getText().toString();
                 }
                 return ((CheckInApplication)getApplication()).getApi().checkinsAdd(mVenue.getId(), null, shout, getBoradcastString(),
                         mVenue.getLocation().getLat() + "," + mVenue.getLocation().getLng(), null, null, null);
 
             } catch (FoursquareApiException e) {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             if (mDialog == null) {
                 mDialog = new ProgressDialog(CheckinActivity.this);
                 mDialog.setCancelable(false);
             }
             if (mDialog.isShowing()) {
                 mDialog.dismiss();
             }
             mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
             mDialog.setMessage("正在提交...");
             mDialog.show();
 
 
 
         }
 
         @Override
         protected void onPostExecute(Result<Checkin> checkinResult) {
             super.onPostExecute(checkinResult);
             mDialog.cancel();
             if (checkinResult == null) {
                 doAfterError(null);
             } else {
                 if (checkinResult.getMeta().getCode() == 200) {
                     doAfterSuccess(checkinResult.getResult());
                 } else {
                     doAfterError(checkinResult.getMeta());
                 }
             }
 
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
             super.onProgressUpdate(values);
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         for (CheckinAsyncTask task : mAsyncTaskList) {
             if (task.getStatus() == AsyncTask.Status.RUNNING) {
                 task.cancel(true);
             }
         }
     }
 
 }
