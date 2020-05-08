 package com.ouchadam.fang.presentation.controller;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.github.frankiesardo.icepick.annotation.Icicle;
 import com.github.frankiesardo.icepick.bundle.Bundles;
 import com.novoda.notils.caster.Fragments;
 import com.ouchadam.fang.R;
 import com.ouchadam.fang.debug.DebugActivity;
 import com.ouchadam.fang.debug.FeedServiceInfo;
 import com.ouchadam.fang.presentation.PlayerEvent;
 import com.ouchadam.fang.presentation.PodcastPlayerEventBroadcaster;
 import com.ouchadam.fang.presentation.item.ActivityResultHandler;
 import com.ouchadam.fang.presentation.item.LatestFragment;
 import com.ouchadam.fang.setting.SettingsActivity;
 
 public class FragmentControllerActivity extends FangActivity {
 
     @Icicle
     public String activityTitle;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.debug, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.ab_debug:
                 onDebugClicked();
                 break;
 
             case R.id.ab_refresh:
                 onRefreshClicked();
                 break;
 
             case R.id.ab_settings:
                 onSettingsClicked();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void onDebugClicked() {
         startActivity(new Intent(this, DebugActivity.class));
     }
 
     private void onRefreshClicked() {
         Intent refreshIntent = FeedServiceInfo.refresh(this);
         startService(refreshIntent);
     }
 
     private void onSettingsClicked() {
         Intent settingsIntent = new Intent(this, SettingsActivity.class);
         startActivity(settingsIntent);
     }
 
     @Override
     protected void onFangCreate(Bundle savedInstanceState) {
         Bundles.restoreInstanceState(this, savedInstanceState);
         initActionBar();
         if (hasEmptyContent()) {
             showDefaultFragment();
         }
     }
 
     private void initActionBar() {
         if (activityTitle != null) {
             getActionBar().setTitle(activityTitle);
         }
         invalidateOptionsMenu();
     }
 
     private boolean hasEmptyContent() {
         return Fragments.findFragmentById(getSupportFragmentManager(), R.id.content_frame) == null;
     }
 
     private void showDefaultFragment() {
         getActionBar().setTitle("Latest");
         invalidateOptionsMenu();
         getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new LatestFragment()).commit();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         if (getActionBar() != null && getActionBar().getTitle() != null) {
             activityTitle = getActionBar().getTitle().toString();
         }
         Bundles.saveInstanceState(this, outState);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         ActivityResultHandler activityResultHandler = new ActivityResultHandler();
         activityResultHandler.handleResult(requestCode, resultCode, data, onResult);
     }
 
     private final ActivityResultHandler.OnResult onResult = new ActivityResultHandler.OnResult() {
         @Override
         public void onPlaySelected(long itemId, Uri itemSource) {
             showPanel();
             setData(itemId);
             // TODO auto Expand or just play?
 
             PodcastPlayerEventBroadcaster broadcaster = new PodcastPlayerEventBroadcaster(FragmentControllerActivity.this);
             broadcaster.broadcast(new PlayerEvent.Factory().newSource(itemId, itemSource));
             broadcaster.broadcast(new PlayerEvent.Factory().play());
         }
     };
 }
