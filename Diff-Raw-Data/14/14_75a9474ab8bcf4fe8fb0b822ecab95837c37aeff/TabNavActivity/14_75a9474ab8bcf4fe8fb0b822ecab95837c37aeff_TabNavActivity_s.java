 package com.masenf.core;
 
 import java.util.ArrayList;
 
 import com.masenf.core.progress.ProgressCallback;
 import com.masenf.core.progress.ProgressFragment;
 import com.masenf.core.progress.ProgressListAdapter;
 import com.masenf.core.progress.ProgressManager;
 import com.masenf.wtaandroid.R;
 import com.masenf.wtaandroid.R.id;
 import com.masenf.wtaandroid.R.layout;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.util.Log;
 
 public class TabNavActivity extends Activity {
 
 	private static final String TAG = "TabNavActivity";
 	protected int sel_tab = 0;
 	protected int prev_tab = -1;
 	private ProgressManager pm;
 	private ArrayList<IonBackButtonPressed> backButtonCallbacks;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "onCreate() - TabNavActivity created");
 		this.setContentView(R.layout.tabnav_activity);
 		
 		if (savedInstanceState != null) {
 			sel_tab = savedInstanceState.getInt("active_tab_idx", 0);
 			prev_tab = savedInstanceState.getInt("prev_tab", -1);
 		}

		pm = ProgressManager.initManager(this);		
 	}
 	@Override
 	public void onStart() {
 		super.onStart();
 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction ft = fm.beginTransaction();
 		Fragment GlobalProgress = fm.findFragmentByTag("GlobalProgress");
 		if (GlobalProgress == null) {
 			Log.v(TAG,"onStart() - creating Fragment tagged GlobalProgress");
 			ft.add(R.id.progress_fragment_placeholder, new ProgressFragment(pm.getAdapter()), "GlobalProgress");
 		} else {
 			Log.v(TAG,"onStart() - attaching Fragment tagged GlobalProgress");
 			((ProgressFragment) GlobalProgress).setAdapter(pm.getAdapter());
 			ft.attach(fm.findFragmentByTag("GlobalProgress"));
 		}
 		ft.commit();
 	}
 	@Override
 	public void onPause() {
 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction ft = fm.beginTransaction();
 		Fragment GlobalProgress = fm.findFragmentByTag("GlobalProgress");
 		if (GlobalProgress != null) {
 			Log.v(TAG,"onStop() - detaching Fragment tagged GlobalProgress");
 			ft.detach(GlobalProgress);
 		}
 		ft.commit();
 		super.onStop();
 	}
 	public void registerBackButtonCallback(IonBackButtonPressed cb) {
 		if (backButtonCallbacks == null) {
 			backButtonCallbacks = new ArrayList<IonBackButtonPressed>();
 		}
 		backButtonCallbacks.add(cb);
 	}
 	
     @Override
     public void onBackPressed() {
     	if (backButtonCallbacks != null) {
     		FragmentManager fm = getFragmentManager();
 	    	for (IonBackButtonPressed cb : backButtonCallbacks) {
 	    		Fragment thisF = fm.findFragmentByTag(cb.getFragmentTag());
 	    		if (thisF != null && thisF.isVisible()) {
 	    			if (cb.onBackPressed())
 	    				return;
 	    		}
 	    	}
     	}
     	if (prev_tab == -1) {
     		super.onBackPressed();
     	} else {
     		getActionBar().setSelectedNavigationItem(prev_tab);
     		prev_tab = -1;
     	}
     }	
     @Override
     public void onSaveInstanceState(Bundle outState) {
     	super.onSaveInstanceState(outState);
 		Log.d(TAG, "onSaveInstanceState() - writing active tab state");
     	outState.putInt("active_tab_idx", getActionBar().getSelectedNavigationIndex());
     	outState.putInt("prev_tab", prev_tab);
     }
 }
