 /*
  * Copyright 2013 The Last Crusade ContactLastCrusade@gmail.com
  * 
  * This file is part of SoundStream.
  * 
  * SoundStream is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SoundStream is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SoundStream.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.thelastcrusade.soundstream;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.SearchManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.SearchView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 import com.thelastcrusade.soundstream.components.ConnectFragment;
 import com.thelastcrusade.soundstream.components.InstructionsDialog;
 import com.thelastcrusade.soundstream.components.MenuFragment;
 import com.thelastcrusade.soundstream.components.PlaybarFragment;
 import com.thelastcrusade.soundstream.model.SongMetadata;
 import com.thelastcrusade.soundstream.service.ConnectionService;
 import com.thelastcrusade.soundstream.service.IMessagingService;
 import com.thelastcrusade.soundstream.service.MessagingService;
 import com.thelastcrusade.soundstream.service.MusicLibraryService;
 import com.thelastcrusade.soundstream.service.ServiceLocator;
 import com.thelastcrusade.soundstream.service.ServiceNotBoundException;
 import com.thelastcrusade.soundstream.util.BroadcastRegistrar;
 import com.thelastcrusade.soundstream.util.IBroadcastActionHandler;
 import com.thelastcrusade.soundstream.util.ITitleable;
 import com.thelastcrusade.soundstream.util.Trackable;
 import com.thelastcrusade.soundstream.util.Transitions;
 
 
 public class CoreActivity extends SlidingFragmentActivity implements Trackable {
     private final String TAG = CoreActivity.class.getSimpleName();
 
     private Fragment menu;
     private PlaybarFragment playbar;
     private BroadcastRegistrar registrar;
 
     private ServiceLocator<MusicLibraryService>   musicLibraryLocator;
     private ServiceLocator<MessagingService>      messagingServiceLocator;
     private GoogleAnalytics mGaInstance;
     private Tracker mGaTracker;
     
     private List<WeakReference<Fragment>> fragSet = new ArrayList<WeakReference<Fragment>>();
 
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         
         ActionBar bar = getActionBar();
         bar.show();
 
         //Get the GoogleAnalytics singleton. Note that the SDK uses
         // the application context to avoid leaking the current context.
         mGaInstance = GoogleAnalytics.getInstance(this);
         // Use the GoogleAnalytics singleton to get a Tracker.
         mGaTracker = mGaInstance.getTracker(getString(R.string.ga_trackingId));
         
         //set the layout for the content - this is just a placeholder
         setContentView(R.layout.content_frame);
         
         //set the layout for the menu
         setBehindContentView(R.layout.menu_frame);
         
         //add the menu
         menu = new MenuFragment();
         getSupportFragmentManager()
             .beginTransaction()
             .replace(R.id.menu_frame, menu)
             .commit();
         
         playbar = new PlaybarFragment();
         getSupportFragmentManager()
             .beginTransaction()
             .replace(R.id.playbar, playbar)
             .commit();
         
         
         //We want to start off at the connect page if this is the first time
         // the activity is created
         if(savedInstanceState == null) {
             Transitions.transitionToConnect(this);
         }
         else{
             enableSlidingMenu();
         }
         // setup the sliding bar
         setSlidingActionBarEnabled(false);
         getSlidingMenu().setBehindWidthRes(R.dimen.show_menu);
         createServiceLocators();
         registerReceivers();
     }
     
     /* (non-Javadoc)
      * @see android..v4.app.FragmentActivity#onResume()
      */
     @Override
     protected void onResume() {
         // TODO Auto-generated method stub
         super.onResume();
         
         SharedPreferences prefs = getSharedPreferences(
                 getPackageName(), MODE_PRIVATE);
 
         if (prefs.getBoolean("firstrun", true)) {
             
             prefs.edit().putBoolean("firstrun", false).commit();
 
             new InstructionsDialog(this).show();
         }
     }
 
 
     @Override
     public boolean onKeyUp(int keyCode, KeyEvent event) {
         //necessary because of the way that the sliding menu handles back presses
         if(keyCode == KeyEvent.KEYCODE_BACK){
             onBackPressed();
             return false;
         }
         else{
             return super.onKeyUp(keyCode, event);
         }
         
     }
     
     @Override
     public void onBackPressed() {
         //if we are in the connect fragment or the menu is open, 
         //back out of the app by pulling up the home screen
         if(getTitle().equals(getString(R.string.select)) || getSlidingMenu().isMenuShowing()){
             Intent startMain = new Intent(Intent.ACTION_MAIN);
             startMain.addCategory(Intent.CATEGORY_HOME);
             startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(startMain);
         }
         //otherwise, open the menu
         else{
             showMenu();
         }
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the options menu from XML
         getMenuInflater().inflate(R.menu.search_menu, menu);
 
         // Get the SearchView and set the searchable configuration
         SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         MenuItem searchItem = menu.findItem(R.id.search);
         SearchView searchView = (SearchView) searchItem.getActionView();
         
         searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchActivity.class)));
         searchView.setIconifiedByDefault(true);
         
         if (isConnectActive()) {
             searchItem.setVisible(false);
         }
 
         return true;
     }
     
     private boolean isConnectActive(){
        return true;
//        return getSupportFragmentManager().findFragmentByTag(Transitions.CURRENT_CONTENT) instanceof ConnectFragment;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         // home references the app icon
         Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(Transitions.CURRENT_CONTENT);
         if (item.getItemId() == android.R.id.home && !(currentFragment instanceof ConnectFragment)) {
             toggle(); // toggles the state of the sliding menu
             
             int title = ((ITitleable)currentFragment).getTitle();
             if(getSlidingMenu().isMenuShowing() && menu.isAdded()){
                 title = ((ITitleable)menu).getTitle();
             }
             setTitle(title);
             
             return true;
         }
         return false;
     }
 
     @Override
     protected void onDestroy() {
         unregisterReceivers();
         unbindServiceLocators();
         super.onDestroy();
     }
 
     private void unbindServiceLocators(){
         messagingServiceLocator.unbind();
         musicLibraryLocator.unbind();
     }
 
     private void createServiceLocators() {
         messagingServiceLocator = new ServiceLocator<MessagingService>(
                 this, MessagingService.class, MessagingService.MessagingServiceBinder.class);
 
         musicLibraryLocator = new ServiceLocator<MusicLibraryService>(
                 this, MusicLibraryService.class, MusicLibraryService.MusicLibraryServiceBinder.class);
     }
 
     @Override
     public void onStart() {
       super.onStart();
       mGaTracker.sendView(TAG);
       //we are also keeping Easytracker till we figure out what else we get automagicly
       EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
       //For Google Analytics
       EasyTracker.getInstance().activityStop(this);
       super.onStop();
     }
 
     private void registerReceivers() {
         this.registrar = new BroadcastRegistrar();
         this.registrar
             .addLocalAction(ConnectionService.ACTION_HOST_DISCONNECTED, new IBroadcastActionHandler() {
             
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
                     //Send the user to a page where they can start a network or join a different network
                     Transitions.transitionToConnect(CoreActivity.this);
                 }
             })
             .addLocalAction(ConnectionService.ACTION_HOST_CONNECTED, new IBroadcastActionHandler() {
 
                 @Override
                 public void onReceiveAction(Context context, Intent intent) {
                     //send the library to the connected host
                     List<SongMetadata> metadata = getMusicLibraryService().getMyLibrary();
                     getMessagingService().sendLibraryMessageToHost(metadata);
                 }
             })
             .register(this);
     }
 
     private void unregisterReceivers() {
         this.registrar.unregister();
     }
 
     public void showContent(){
         getSlidingMenu().showContent();
     }
     
     public void disableSlidingMenu(){
         getActionBar().setDisplayHomeAsUpEnabled(false);
         getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
     }
     
     public void enableSlidingMenu(){
      // enables the icon to act as the up
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
     }
     
     public void showPlaybar(){
         getSupportFragmentManager()
         .beginTransaction()
         .show(playbar)
         .commit();
     }
     
     public void hidePlaybar(){
         getSupportFragmentManager()
         .beginTransaction()
         .hide(playbar)
         .commit();
     }
 
     private IMessagingService getMessagingService() {
         MessagingService messagingService = null;
         try {
             messagingService = this.messagingServiceLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return messagingService;
     }
 
     private MusicLibraryService getMusicLibraryService() {
         MusicLibraryService musicLibraryService = null;
         try {
             musicLibraryService = this.musicLibraryLocator.getService();
         } catch (ServiceNotBoundException e) {
             Log.wtf(TAG, e);
         }
         return musicLibraryService;
     }
 
     public Tracker getTracker(){
         return mGaTracker;
     }
     
     @Override
     public void onAttachFragment (Fragment fragment) {
         fragSet.add(new WeakReference<Fragment>(fragment));
     }
 
     public List<Fragment> getAttachedFragments() {
         List<Fragment> ret = new ArrayList<Fragment>();
         for(WeakReference<Fragment> ref : fragSet) {
             Fragment f = ref.get();
             if(f != null) {
                 ret.add(f);
             }
         }
         return ret;
     }
 }
