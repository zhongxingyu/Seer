 package com.strollimo.android.view;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.GravityCompat;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Html;
 import android.text.SpannableStringBuilder;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.strollimo.android.R;
 import com.strollimo.android.StrollimoApplication;
 import com.strollimo.android.StrollimoPreferences;
 import com.strollimo.android.controller.AccomplishableController;
 import com.strollimo.android.model.BaseAccomplishable;
 import com.strollimo.android.model.Mystery;
 import com.strollimo.android.model.Secret;
 import com.strollimo.android.util.Analytics;
 import com.strollimo.android.util.Utils;
 
 import java.util.HashMap;
 import java.util.List;
 
 public class MainActivity extends AbstractTrackedFragmentActivity {
 
     public enum MenuItemFragment {
         MAP("Map", MapFragment.class),
         DEBUG("Debug", DebugFragment.class),
         PROFILE("Profile", null),
         QUESTS("Quests", null),
         ACHIEVEMENTS("Achievements", null),
         ABOUT("About", null);
 
         private String mLabel;
         private Class<? extends Fragment> mFragment;
 
         private MenuItemFragment(String label, Class<? extends Fragment> fragment) {
             mLabel = label;
             mFragment = fragment;
         }
 
         public String getLabel() {
             return mLabel;
         }
 
         public Class<? extends Fragment> getFragment() {
             return mFragment;
         }
 
         public static String[] getLabels(){
             MenuItemFragment[] items = values();
             String[] labels = new String[items.length];
             for (int i = 0; i < items.length; i++) {
                 labels[i] = items[i].getLabel();
             }
             return labels;
         }
     }
 
     private ActionBar mActionBar;
 
     private String mTitle = "Strollimo";
 
     private String mDrawerTitle = "Menu";
     private DrawerLayout mDrawerLayout;
     private ActionBarDrawerToggle mDrawerToggle;
     private ListView mDrawerList;
     private HashMap<Class<Fragment>, Fragment> mFragmentCache = new HashMap<Class<Fragment>, Fragment>();
     private AlertDialog mQuestCompleteDialog;
     private StrollimoPreferences mPreferences;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mPreferences = StrollimoApplication.getService(StrollimoPreferences.class);
 
         mActionBar = getActionBar();
         mActionBar.setTitle(mTitle);
 
         if (Utils.isDebugBuild()) {
             mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
             mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
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
             mDrawerLayout.setDrawerListener(mDrawerToggle);
             mDrawerList = (ListView) findViewById(R.id.main_drawer_list);
             mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                     R.layout.menu_item, MenuItemFragment.getLabels()));
             // Set the list's click listener
             mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
             selectItem(this, 0);
         } else {
             mActionBar.setDisplayHomeAsUpEnabled(false);
             mActionBar.setHomeButtonEnabled(false);
             Class<Fragment> fragmentClass = (Class<Fragment>) MenuItemFragment.MAP.getFragment();
             replaceFragment(this, fragmentClass);
         }
     }
 
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         if (mDrawerToggle != null){
             // Sync the toggle state after onRestoreInstanceState has occurred.
             mDrawerToggle.syncState();
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (isQuestComplete()) {
 
             if (!mPreferences.isFeedbackCompleted()) {
                 startActivity(new Intent(this, FeedbackFormActivity.class));
             }
 
             AlertDialog d = getQuestCompleteDialog();
             if (!d.isShowing()) {
                 d.show();
                 // Make the textview clickable. Must be called after show()
                 ((TextView)d.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
 
                 Analytics.track(Analytics.Event.QUEST_COMPLETE);
             }
        }
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         if (mDrawerToggle != null){
             mDrawerToggle.onConfigurationChanged(newConfig);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (mDrawerToggle != null){
             // Pass the event to ActionBarDrawerToggle, if it returns
             // true, then it has handled the app icon touch event
             if (mDrawerToggle.onOptionsItemSelected(item)) {
                 return true;
             }
         }
         // Handle your other action bar items...
 
         return super.onOptionsItemSelected(item);
     }
 
     private class DrawerItemClickListener implements ListView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView parent, View view, int position, long id) {
             selectItem(view.getContext(), position);
         }
     }
 
     private boolean isQuestComplete() {
         AccomplishableController accomplishableController = StrollimoApplication.getService(AccomplishableController.class);
         List<Mystery> mysteries = accomplishableController.getAllMysteries();
 
         for (Mystery mystery : mysteries) {
             for (String secretId : mystery.getChildren()) {
                 Secret secret = accomplishableController.getSecretById(secretId);
                 if (secret == null) {
                     Log.e(MainActivity.class.getSimpleName(), "Error - secret is not available isQuestComplete the images: " + secretId);
                     continue;
                 }
                 BaseAccomplishable.PickupState state = secret.getPickupState();
                 switch (state) {
                     case UNPICKED:
                         return false;
                     default:
                         // keep going
                 }
             }
         }
 
         return true;
     }
 
     /** Swaps fragments in the main content view */
     private void selectItem(Context context, int position) {
         // Create a new fragment and specify the planet to show based on position
         Class<Fragment> fragmentClass = (Class<Fragment>) MenuItemFragment.values()[position].getFragment();
         if (fragmentClass != null) {
 
             replaceFragment(context, fragmentClass);
 
             // Highlight the selected item, update the title, and close the drawer
             mDrawerList.setItemChecked(position, true);
         }
         //setName(mPlanetTitles[position]);
         mDrawerLayout.closeDrawer(mDrawerList);
     }
 
     private void replaceFragment(Context context, Class<Fragment> fragmentClass) {
         String fragmentName = fragmentClass.getName();
         Fragment fragment = Fragment.instantiate(context, fragmentName);
         mFragmentCache.put(fragmentClass, fragment);
         // Insert the fragment by replacing any existing fragment
         FragmentManager fragmentManager = getSupportFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.main_content, fragment)
                 .commit();
     }
 
 
     private AlertDialog getQuestCompleteDialog() {
         if (mQuestCompleteDialog == null) {
             SpannableStringBuilder msg = new SpannableStringBuilder();
             msg.append("Congratulation, you've completed the Covent Garden quest and finished the Beta!\n\n");
             msg.append("We're adding more quests soon, so stay tuned!\n\n");
             msg.append(Html.fromHtml("We'd love to hear your opinion! Wether you've liked it or not please contact us by: <a href='mailto:strollimo@gmail.com'>strollimo@gmail.com</a><br/><br/>"));
             msg.append(Html.fromHtml("<a href='https://plus.google.com/u/0/communities/107619132512578312178'>Strollimo beta community on Google+</a><br/><br/>"));
             msg.append(Html.fromHtml("<a href='https://www.strollimo.com'>Strollimo.com</a>"));
 
            mQuestCompleteDialog = new AlertDialog.Builder(this).create();
             mQuestCompleteDialog.setCancelable(false);
             mQuestCompleteDialog.setCanceledOnTouchOutside(false);
             mQuestCompleteDialog.setMessage(msg);
         }
         return mQuestCompleteDialog;
     }
 
     @Override
     protected void onDestroy() {
         Analytics.flush();
         super.onDestroy();
     }
 
     //    @Override
 //    public void setName(CharSequence title) {
 //        mTitle = title;
 //        getActionBar().setName(mTitle);
 //    }
 
 }
