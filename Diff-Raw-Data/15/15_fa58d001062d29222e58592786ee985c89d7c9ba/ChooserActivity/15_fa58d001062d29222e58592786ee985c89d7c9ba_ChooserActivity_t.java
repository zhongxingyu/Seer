 package com.albinstjerna.ananke;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.view.View;
 import android.content.Intent;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 
 public class ChooserActivity extends Activity
     implements BinaryChoiceFragment.RecieveDecisionData, MultipleChoiceFragment.RecieveDecisionData {
     
     private ActionBar actionBar;
     public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
         private Fragment mFragment;
         private final Activity mActivity;
         private final String mTag;
         private final Class<T> mClass;
 
         /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
         public TabListener(Activity activity, String tag, Class<T> clz) {
             mActivity = activity;
             mTag = tag;
             mClass = clz;
         }
 
         /* The following are each of the ActionBar.TabListener callbacks */
 
         public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Fragment preInitializedFragment = (Fragment) mActivity.getFragmentManager().findFragmentByTag(mTag);

             // Check if the fragment is already initialized
            if (mFragment == null && preInitializedFragment == null) {
                 // If not, instantiate and add it to the activity
                 mFragment = Fragment.instantiate(mActivity, mClass.getName());
                 ft.add(android.R.id.content, mFragment, mTag);
            } else if (mFragment != null) {
                 // If it exists, simply attach it in order to show it
                 ft.attach(mFragment);
                } else if(preInitializedFragment != null) {
                ft.attach(preInitializedFragment);
                mFragment = preInitializedFragment;
                }

         }
 
         public void onTabUnselected(Tab tab, FragmentTransaction ft) {
             if (mFragment != null) {
                 // Detach the fragment, because another one is being attached
                 ft.detach(mFragment);
             }
         }
 
         public void onTabReselected(Tab tab, FragmentTransaction ft) {
             // User selected the already selected tab. Usually do nothing.
         }
     }
 
     
     public final static String STRING_MESSAGE = "com.albinstjerna.ananke.STRING_MESSAGE";
 
     //    BinaryChoiceFragment binaryFragment;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         // Use the root view.
         //setContentView(R.layout.chooser);
 
         // Setup action bar for tab navigation.
         actionBar = getActionBar();
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         actionBar.setDisplayShowTitleEnabled(false);
 
         // Add tabs:
         Tab tab = actionBar.newTab()
             .setText("Binary Choice")
             .setTabListener(new TabListener<BinaryChoiceFragment> (this, 
                                                                    "binary choice", 
                                                                    BinaryChoiceFragment.class));
         actionBar.addTab(tab);
 
         tab = actionBar.newTab()
             .setText("Multiple Choice")
             .setTabListener(new TabListener<MultipleChoiceFragment> (
                                                                      this, "multiple choice", MultipleChoiceFragment.class));
 
         actionBar.addTab(tab);
 
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         
         actionBar.setSelectedNavigationItem(savedInstanceState.getInt("TABPOS"));
 
     }
 
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
 
         savedInstanceState.putInt("TABPOS", actionBar.getSelectedNavigationIndex());
     }
 
     /** Send the decision to the displayer view. */
     public void handleDecision(String decision) {
         Intent intent = new Intent(this, DisplayerActivity.class);
         intent.putExtra(STRING_MESSAGE, decision);
         startActivity(intent);
     }
     
 }
