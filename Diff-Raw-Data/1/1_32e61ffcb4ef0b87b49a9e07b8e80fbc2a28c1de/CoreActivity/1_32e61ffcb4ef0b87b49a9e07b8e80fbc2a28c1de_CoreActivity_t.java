 package com.lastcrusade.fanclub;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.app.Fragment;
 
 import com.actionbarsherlock.view.MenuItem;
 import com.lastcrusade.fanclub.components.MenuFragment;
 import com.lastcrusade.fanclub.components.MusicLibraryFragment;
 import com.lastcrusade.fanclub.components.PlaybarFragment;
 import com.lastcrusade.fanclub.components.PlaylistFragment;
 import com.lastcrusade.fanclub.service.MusicLibraryService;
 import com.lastcrusade.fanclub.service.MusicLibraryService.MusicLibraryServiceBinder;
 import com.lastcrusade.fanclub.util.BluetoothUtils;
 import com.lastcrusade.fanclub.util.ITitleable;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 
 public class CoreActivity extends SlidingFragmentActivity{
     private Fragment activeContent;
     private Fragment menu;
     
 
     MusicLibraryService mMusicLibraryService;
     boolean boundToService; //Since you cannot instantly bind, set a boolean
                             // after its safe to call methods
 
     /** Defines callbacks for service binding, passed to bindService() */
     private ServiceConnection musicLibraryConn = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                 IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             MusicLibraryServiceBinder binder = (MusicLibraryServiceBinder) service;
             mMusicLibraryService = binder.getService();
             boundToService = true;
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundToService = false;
         }
     };
     
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
 
         //set the layout for the content - this is just a placeholder
         setContentView(R.layout.content_frame);
         
         //add the menu fragment
         setBehindContentView(R.layout.menu_frame);
         switchFragment(getString(R.string.menu), false);
         
         //add the initial content fragment and set the title on the action bar
         switchActiveContent(getString(R.string.playlist));
         setTitle(getString(R.string.playlist));
 
        
         // setup the sliding bar
         getSlidingMenu().setBehindOffsetRes(R.dimen.show_content);
         setSlidingActionBarEnabled(false);
         
        //add the playbar fragment onto the active content view
         getSupportFragmentManager()
             .beginTransaction()
             .replace(R.id.playbar, new PlaybarFragment())
             .commit();
 
         // enables the icon to act as the up
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         
         setTitle(getString(R.string.playlist));
 
         //Add user to user list
         CustomApp curApp = ((CustomApp)getApplication());
         curApp.getUserList().addUser(BluetoothUtils.getLocalBluetoothName());
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         // home references the app icon
         if (item.getItemId() == android.R.id.home) {
             toggle(); // toggles the state of the sliding menu
             if(getSlidingMenu().isMenuShowing() && menu.isAdded()){
                 setTitle(((ITitleable)menu).getTitle());
             }
             return true;
         }
         return false;
     }
     
     
 
     public void switchActiveContent(String fragmentName) {
         // switch out the content fragments
         switchFragment(fragmentName, true);
 
         // close the sliding menu and show the full content fragment
         getSlidingMenu().showContent();
     }
 
     /*
      * Switches out the current fragment with the fragment represented by the name
      * that is passed in. If content is true, the fragment is replaced as the main content
      *  - if it is false, the fragment is replaced as the menu
      */
     private void switchFragment(String fragmentName, boolean content) {
         if (content) {
             activeContent = getFragment(fragmentName);
             getSupportFragmentManager().beginTransaction()
                     .replace(R.id.content_frame, activeContent)
                     .addToBackStack(null).commit();
         } else {
             menu = getFragment(fragmentName);
             getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.menu_frame, menu)
                 .commit();
         }
     }
 
     /*
      * Get the fragment associated with the name passed in.
      * If later we decide we want to save the state of the fragments we can
      * rework this to get saved states instead of creating a new fragment
      * every time
      */
     private Fragment getFragment(String fragmentName) {
         Fragment newFragment = null;
         
         if(fragmentName.equals(getString(R.string.playlist))){
             newFragment = new PlaylistFragment();
         }
         else if(fragmentName.equals(getString(R.string.music_library))){
             newFragment = new MusicLibraryFragment();
         }
         else if(fragmentName.equals(getString(R.string.menu))){
             newFragment =  new MenuFragment();
         }
         
         return newFragment;
     }
 
     public MusicLibraryService getMusicLibraryService(){
         return mMusicLibraryService;
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         //To test Music service
         Intent intentML = new Intent(this, MusicLibraryService.class);
         bindService(intentML, musicLibraryConn, Context.BIND_AUTO_CREATE);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         // Unbind from the MusicLibrary service
         if (boundToService) {
             unbindService(musicLibraryConn);
             boundToService = false;
         }
     }
 }
