 // Copyright 2010 Google Inc.
 // Copyright 2011 NPR
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package org.npr.android.news;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.npr.android.util.PlaylistEntry;
 import org.npr.android.util.PlaylistRepository;
 import org.npr.api.ApiConstants;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.KeyEvent;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * @author mfrederick@google.com (Michael Frederick)
  *         A base class for all Activities that want to display the default layout,
  *         including the PlaylistView.
  */
 public abstract class RootActivity extends Activity implements
   Trackable, Refreshable, OnClickListener {
   private static final String LOG_TAG = RootActivity.class.getName();
   private NavigationView navigationView;
   private PlaylistView playlistView;
   private ProgressBar progressIndicator;
   private BroadcastReceiver updateReceiver;
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
     super.onCreate(savedInstanceState);
 
     setContentView(R.layout.main);
 
     // Override the normal volume controls so that the user can alter the volume
     // when a stream is not playing.
     setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
     ViewGroup titleFrame = (ViewGroup) findViewById(R.id.TitleContent);
     navigationView = new NavigationView(this);
     titleFrame.addView(navigationView,
         new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
             LayoutParams.MATCH_PARENT));
     navigationView.setVisibility(View.GONE);
 
     ImageButton mainSearchButton =
         (ImageButton) findViewById(R.id.MainSearchButton);
     mainSearchButton.setOnClickListener(this);
     
     ImageButton mainNavButton =
         (ImageButton) findViewById(R.id.MainNavButton);
     mainNavButton.setOnClickListener(this);
 
     playlistView = new PlaylistView(this);
     titleFrame.addView(playlistView,
         new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,
             LayoutParams.MATCH_PARENT));
 
     progressIndicator =
         (ProgressBar) findViewById(R.id.WindowProgressIndicator);
 
     trackNow();
 
     // Initializes the crittercism library
     /*Crittercism.init(getApplicationContext(),
         "", // Code removed from open source
         true);
     */
   }
 
 
   protected PlaylistView getPlaylistView() {
     return playlistView;
   }
 
   protected void startIndeterminateProgressIndicator() {
     progressIndicator.setVisibility(View.VISIBLE);
     // setSupportProgressBarIndeterminateVisibility(true); // Once we move to a proper action bar
   }
 
   protected void stopIndeterminateProgressIndicator() {
     progressIndicator.setVisibility(View.INVISIBLE);
     // setSupportProgressBarIndeterminateVisibility(false); // Once we move to a proper action bar
   }
 
 
   @Override
   protected void onStart() {
     super.onStart();
 
     if (!ApiConstants.instance().apiKeyIsValid()) {
       final AlertDialog dialog = new AlertDialog.Builder(this)
           .setOnCancelListener(new DialogInterface.OnCancelListener() {
             @Override
             public void onCancel(DialogInterface dialog) {
               finish();
             }
           })
           .setMessage(R.string.msg_api_key_error)
           .create();
       dialog.show();
 
       // Make the TextView clickable. Must be called after show()
       // http://stackoverflow.com/questions/1997328/android-clickable-hyperlinks-in-alertdialog
       ((TextView) dialog.findViewById(android.R.id.message))
           .setMovementMethod(LinkMovementMethod.getInstance());
 
     }
 
    bringPlayerNavToFront();
  }
  
  protected void bringPlayerNavToFront()
  {
     // Navigation on top of player, player on top of content
     playlistView.bringToFront();
     navigationView.bringToFront();
   }
 
 
   @Override
   public boolean isRefreshable() {
     return false;
   }
 
   @Override
   public void refresh() {
   }
 
   @Override
   public void trackNow() {
   }
 
   @Override
   public void finish() {
     super.finish();
     noAnimation();
   }
 
   protected void startActivityWithoutAnimation(Intent i) {
     startActivity(i);
     noAnimation();
   }
 
   /**
    * Prevents the default animation on the pending transition. Only works on
    * SDK version 5 and up, but may be safely called from any version.
    */
   protected void noAnimation() {
     try {
       Method overridePendingTransition =
         Activity.class.getMethod("overridePendingTransition", new Class[]{
           int.class, int.class});
       overridePendingTransition.invoke(this, 0, 0);
     } catch (SecurityException e) {
       Log.w(LOG_TAG, "", e);
     } catch (NoSuchMethodException e) {
       // Don't log an error here; we anticipate an error on SDK < 5
     } catch (IllegalArgumentException e) {
       Log.w(LOG_TAG, "", e);
     } catch (IllegalAccessException e) {
       Log.w(LOG_TAG, "", e);
     } catch (InvocationTargetException e) {
       Log.w(LOG_TAG, "", e);
     }
   }
 
   /**
    * Tells the player to start playing the playlist entry provided and
    * updates the display to reflect the actions.
    *
    * @param entry The PlaylistEntry to play
    */
   protected void playEntryNow(PlaylistEntry entry) {
     playlistView.playEntryNow(entry);
   }
 
   /**
    * Tells the player to start playing the playable and stop if/when
    * the playable finishes. The player will not continue on to the items
    * in the playlist. The display is updated to reflect the actions.
    *
    * @param playable A Playable stream or podcast to play.
    */
   protected void playSingleNow(Playable playable) {
     startIndeterminateProgressIndicator();
 
     if (updateReceiver != null) {
       unregisterReceiver(updateReceiver);
       updateReceiver = null;
     }
     updateReceiver = new PlaybackUpdateReceiver();
     registerReceiver(updateReceiver,
         new IntentFilter(PlaybackService.SERVICE_UPDATE_NAME));
 
     playlistView.playSingleNow(playable);
   }
 
   private class PlaybackUpdateReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       int duration = intent.getIntExtra(PlaybackService.EXTRA_DURATION, 1);
       if (duration != 1) {
         stopIndeterminateProgressIndicator();
         if (updateReceiver != null) {
           unregisterReceiver(updateReceiver);
           updateReceiver = null;
         }
       }
     }
   }
 
   @Override
   public void onCreateContextMenu(ContextMenu menu, View view,
                                   ContextMenu.ContextMenuInfo menuInfo) {
     Log.d(LOG_TAG, "Creating context menu for list items");
 
     super.onCreateContextMenu(menu, view, menuInfo);
     MenuInflater inflater = getMenuInflater();
     inflater.inflate(R.menu.playlist_context_menu, menu);
 
     // Set the menu header to the name of the item in the playlist
     try {
       AdapterView.AdapterContextMenuInfo info =
           (AdapterView.AdapterContextMenuInfo) menuInfo;
       View nameText = info.targetView.findViewById(R.id.playlist_item_title);
       if (nameText != null && nameText instanceof TextView) {
         menu.setHeaderTitle(((TextView) nameText).getText());
       }
     } catch (ClassCastException e) {
       Log.e(LOG_TAG, "MenuInfo was an unexpected type", e);
     }
 
   }
 
   @Override
   public boolean onContextItemSelected(MenuItem item) {
     AdapterView.AdapterContextMenuInfo info =
         (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 
     Log.d(LOG_TAG, "Context menu item clicked for position " + info.position);
     switch (item.getItemId()) {
       case R.id.PlaylistReadEntry:
         PlaylistEntry readEntry =
             playlistView.getPlaylistEntryAtPosition(info.position);
         if (readEntry != null) {
           playlistView.setExpanded(false);
           Intent i = new Intent(this, NewsStoryActivity.class);
           i.putExtra(Constants.EXTRA_STORY_ID, readEntry.storyID);
           startActivityWithoutAnimation(i);
         }
         return true;
       case R.id.PlaylistRemoveEntry:
         PlaylistEntry removeEntry =
             playlistView.getPlaylistEntryAtPosition(info.position);
         if (removeEntry != null) {
           PlaylistRepository repository =
               new PlaylistRepository(getApplicationContext(),
                   getContentResolver());
           repository.delete(removeEntry);
           playlistView.updatePlaylist();
         }
         return true;
       default:
         return super.onContextItemSelected(item);
     }
   }
 
 
   @Override
   public void onClick(View v) {
     switch (v.getId()) {
       case R.id.MainSearchButton:
         startActivityWithoutAnimation(new Intent(this, SearchActivity.class));
         break;
       case R.id.MainNavButton:
         showHideNavigationMenu();
         break;
     }
   }
 
   private void showHideNavigationMenu() {
     if (navigationView.getVisibility() == View.GONE) {
       navigationView.setVisibility(View.VISIBLE);
       navigationView.requestFocus();
     } else {
       navigationView.setVisibility(View.GONE);
     }
   }
 
   @Override
   public boolean onKeyDown(int keyCode, KeyEvent event) {
     if (keyCode == KeyEvent.KEYCODE_BACK &&
       event.getRepeatCount() == 0) {
       if (navigationView.getVisibility() == View.VISIBLE) {
         navigationView.setVisibility(View.GONE);
         return true;
       } else if (playlistView.isExpanded()) {
         playlistView.setExpanded(false);
         return true;
       }
     }
     return super.onKeyDown(keyCode, event);
   }
 
   public String getActiveId() {
     return playlistView.getActiveId();
   }
 }
