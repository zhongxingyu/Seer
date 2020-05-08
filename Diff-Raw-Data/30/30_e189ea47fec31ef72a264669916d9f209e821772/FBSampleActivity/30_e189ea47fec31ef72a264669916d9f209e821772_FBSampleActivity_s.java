 package com.randy.fbsample;
 
 import java.util.Arrays;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
import android.view.View;
 
 import com.facebook.Session;
import com.facebook.Session.OpenRequest;
 import com.facebook.Session.StatusCallback;
import com.facebook.SessionLoginBehavior;
 import com.facebook.SessionState;
 import com.randy.fbsample.fragments.LoginFragment;
 import com.randy.fbsample.fragments.ProfileFragment;
 
 public class FBSampleActivity extends FragmentActivity implements StatusCallback {
 	private static final String TAG = "FBSampleActivity";
 	
 	public static final int FRAG_LOGIN = 0;
 	public static final int FRAG_PROFILE = 1;
 
 	
 	private final FragmentManager mFragmentManager = getSupportFragmentManager();
 	private Fragment mCurrentFragment;
 	private int mCurrentFragId = -1;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_fbsample);
 		
 		
         
 		FBSampleApplication.setFBSampleActivity(this); //set static reference to activity
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		Session.openActiveSession(this, false, this);
 		if(Session.getActiveSession() == null || Session.getActiveSession().getState() != SessionState.OPENED) {
 			goToNavFragment(FRAG_LOGIN);
 		}
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	      super.onActivityResult(requestCode, resultCode, data);
 	      if (Session.getActiveSession() != null) {
 	    	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 	      }
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		Session.getActiveSession().removeCallback(this);
 	}
 	
 	@Override
 	public void onDestroy() {
 		FBSampleApplication.setFBSampleActivity(null);
 		super.onDestroy();
 	}
 	
 	public void goToNavFragment(int fragId) {
 		if (fragId != mCurrentFragId) {
 			mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
 			FragmentTransaction transaction = mFragmentManager.beginTransaction();
 			transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
 			if (mCurrentFragment != null) {
 				transaction.remove(mCurrentFragment);
 			}
 			
 			switch(fragId){
 				case FRAG_LOGIN:{
 					mCurrentFragment = new LoginFragment();
 					break;
 				}
 				case FRAG_PROFILE:{
 					mCurrentFragment = new ProfileFragment();
 					break;
 				}
 				
 			}
 			
 			transaction.add(R.id.content, mCurrentFragment);
 			transaction.commit();
 			mCurrentFragId = fragId;
 		}
 	}
 
 	//FB Session state callback
 	@Override
 	public void call(Session session, SessionState state, Exception exception) {
 		if (state == SessionState.OPENED) {
			goToNavFragment(FRAG_PROFILE);
 		} else if (state == SessionState.CLOSED) { //Close normally
 			goToNavFragment(FRAG_LOGIN);
 		} else if (state == SessionState.CLOSED_LOGIN_FAILED) { //close incorrectly
 			goToNavFragment(FRAG_LOGIN);
 		}
 		
 		if (exception != null) {
 			goToNavFragment(FRAG_LOGIN);
 			Log.e(TAG, "Error in facebook session - " + Log.getStackTraceString(exception));
 		}
 	}
 
 }
