 package org.jonblack.bluetrack.activities;
 
 
 import org.jonblack.bluetrack.R;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentTransaction;
 import android.bluetooth.BluetoothAdapter;
 import android.content.DialogInterface;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 
 
 public class MainActivity extends Activity
 {
   private static final String TAG = "MainActivity";
   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main_activity);
     
     // Get the local bluetooth adapter and check if bluetooth is supported.
     BluetoothAdapter localBtAdapter = BluetoothAdapter.getDefaultAdapter();
     if (localBtAdapter == null)
     {
       Log.w(TAG, "Bluetooth isn't supported on device.");
       
       AlertDialog ad = new AlertDialog.Builder(this).create();
       ad.setCancelable(false);
       Resources r = getResources();
       String msg = r.getString(R.string.error_bluetooth_not_supported,
                                r.getString(R.string.app_name));
       ad.setMessage(msg);
       ad.setButton(AlertDialog.BUTTON_POSITIVE,
                    r.getString(R.string.button_exit),
                    new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int which)
         {
           finish();
           dialog.dismiss();
         }
       });
       ad.show();
       
       return;
     }
     
     Resources r = getResources();
     
     ActionBar actionBar = getActionBar();
     actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     
     ActionBar.Tab tab1 = actionBar.newTab().setText(r.getText(R.string.ab_tab_live_tracking));
     tab1.setTag("tracking");
     tab1.setTabListener(new TabListener<LiveTrackingFragment>(this,
                                                               "tracking",
                                                               LiveTrackingFragment.class));
     actionBar.addTab(tab1);
     
     ActionBar.Tab tab2 = actionBar.newTab().setText(r.getText(R.string.ab_tab_sessions));
     tab2.setTag("session");
     tab2.setTabListener(new TabListener<SessionFragment>(this,
                                                          "session",
                                                          SessionFragment.class));
     actionBar.addTab(tab2);
     
     ActionBar.Tab tab3 = actionBar.newTab().setText(r.getText(R.string.ab_tab_devices));
     tab3.setTag("devices");
     tab3.setTabListener(new TabListener<DevicesFragment>(this,
                                                          "devices",
                                                          DevicesFragment.class));
     actionBar.addTab(tab3);
   }
   
   @Override
   protected void onDestroy()
   {
     super.onDestroy();
   }
   
   @Override
   public void onSaveInstanceState(Bundle outState)
   {
     super.onSaveInstanceState(outState);
     
     ActionBar actionBar = getActionBar();
     outState.putInt("selectedTabIdx", actionBar.getSelectedNavigationIndex());
   }
   
   @Override
   public void onRestoreInstanceState(Bundle savedInstanceState)
   {
     super.onRestoreInstanceState(savedInstanceState);
     
     // Restore selected tab
     Log.d(TAG, "Restoring selected tab.");
     int selectedTabIdx = savedInstanceState.getInt("selectedTabIdx", 0);
     getActionBar().setSelectedNavigationItem(selectedTabIdx);
   }
   
   // See http://developer.android.com/reference/android/app/ActionBar.html#newTab%28%29
   public static class TabListener<T extends Fragment> implements ActionBar.TabListener
   {
     private final Activity mActivity;
     private final String mTag;
     private final Class<T> mClass;
     private final Bundle mArgs;
     private Fragment mFragment;
     
     public TabListener(Activity activity, String tag, Class<T> clz) {
         this(activity, tag, clz, null);
     }
     
     public TabListener(Activity activity, String tag, Class<T> clz, Bundle args)
     {
       mActivity = activity;
       mTag = tag;
       mClass = clz;
       mArgs = args;
       
       // Check to see if we already have a fragment for this tab, probably
       // from a previously saved state.  If so, deactivate it, because our
       // initial state is that a tab isn't shown.
       mFragment = mActivity.getFragmentManager().findFragmentByTag(mTag);
       if (mFragment != null && !mFragment.isDetached())
       {
         FragmentTransaction ft = mActivity.getFragmentManager().beginTransaction();
         ft.detach(mFragment);
         ft.commit();
       }
     }
     
     public void onTabSelected(Tab tab, FragmentTransaction ft)
     {
       if (mFragment == null)
       {
         mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
         ft.add(android.R.id.content, mFragment, mTag);
       }
       else
       {
         ft.attach(mFragment);
       }
     }
     
     public void onTabUnselected(Tab tab, FragmentTransaction ft)
     {
       if (mFragment != null)
       {
         ft.detach(mFragment);
       }
     }
     
     public void onTabReselected(Tab tab, FragmentTransaction ft)
     {
     }
   }
 }
