 package org.alabs.nolotiro;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.support.v4.app.Fragment;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.util.Log;
 
 import android.support.v7.app.ActionBar.Tab;
 import android.widget.Toast;
 
 import org.alabs.nolotiro.dialogs.ChangeLocationDialogFragment;
 import org.alabs.nolotiro.dialogs.ChooseLocationDialogFragment;
 import org.alabs.nolotiro.dialogs.FindLocationDialogFragment;
 
 public class MainActivity extends ActionBarActivity implements ChangeLocationDialogFragment.ChangeLocationDialogListener,
         FindLocationDialogFragment.FindLocationDialogListener, ChooseLocationDialogFragment.ChooseLocationDialogListener {
 
     private static final String TAG = "MainActivity";
     private static final String GIVES_TAG = "gives";
     private static final String WANTS_TAG = "wants";
     private ActionBar actionBar;
     private Woeid currentWoeid;
 
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         currentWoeid = new Woeid(1, "Málaga", "Andalusia", "Spain");
         actionBar = getSupportActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
         Tab tab = actionBar.newTab()
                 .setText(R.string.gives)
                 .setTabListener(new TabListener<AdsFragment>(this, GIVES_TAG, AdsFragment.class));
         actionBar.addTab(tab);
 
         tab = actionBar.newTab()
                 .setText(R.string.wants)
                 .setTabListener(new TabListener<AdsFragment>(this, WANTS_TAG, AdsFragment.class));
         actionBar.addTab(tab);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         Intent intent;
         switch (item.getItemId()) {
             case R.id.create_ad:
                 intent = new Intent(this, CreateAdActivity.class);
                 intent.putExtra("location", currentWoeid.getName());
                 startActivity(intent);
                 return true;
             case R.id.action_location:
                 showChangeLocationDialog();
                 return true;
             case R.id.action_refresh:
                 //AdsFragment fragment
                 //ActionBar.Tab t = actionBar.getSelectedTab();
                 //fragment.refreshAds();
                 // FIXME: getSupportFragmentManager() doesn't have findFragmentByTag() method
                 //Fragment f = this.getFragmentManager().findFragmentByTag(GIVES_TAG);
                 return true;
             case R.id.action_settings:
                 intent = new Intent(this, SettingsActivity.class);
                 startActivity(intent);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     //step1
     public void showChangeLocationDialog() {
         ChangeLocationDialogFragment frag = new ChangeLocationDialogFragment();
         frag.show(getSupportFragmentManager(), "ChangeLocationDialogFragment");
     }
 
     // step2: This is called when user has chosen to add a new location
     public void onChangeDialogNeutralClick(DialogFragment dialog) {
         DialogFragment frag = new FindLocationDialogFragment();
         frag.show(getSupportFragmentManager(), "FindLocationDialog");
     }
 
     // step3: Find woeid
     public void onFindDialogPositiveClick(DialogFragment dialog, String location) {
         Log.i(TAG, "location: " + location);
         dialog.dismiss();
 
         DialogFragment frag = new ChooseLocationDialogFragment(location);
         frag.show(getSupportFragmentManager(), "ChooseLocationDialog");
     }
 
     @Override
     public void onChooseDialogPositiveClick(DialogFragment dialog, Woeid woeid) {
         Toast.makeText(this, woeid.toString(), Toast.LENGTH_SHORT).show();
         dialog.dismiss();
     }
 
     public class TabListener<T extends Fragment> implements ActionBar.TabListener {
         private Fragment mFragment;
         private final Activity mActivity;
         private final String mTag;
         private final Class<T> mClass;
 
         public TabListener(Activity activity, String tag, Class<T> clz) {
             mActivity = activity;
             mTag = tag;
             mClass = clz;
         }
 
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
             if (mFragment == null) {
                 mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(Utils.getContentViewCompat(), mFragment, mTag);
             } else {
                 ft.attach(mFragment);
             }
         }
 
         public void onTabUnselected(Tab tab, FragmentTransaction ft) {
             if (mFragment != null) {
                 ft.detach(mFragment);
                 Log.i("ActionBar", tab.getText() + " deseleccionada.");
             }
         }
 
         public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
 
         }
 
         public Fragment getFragment() {
             return mFragment;
         }
 
     }
 
 }
