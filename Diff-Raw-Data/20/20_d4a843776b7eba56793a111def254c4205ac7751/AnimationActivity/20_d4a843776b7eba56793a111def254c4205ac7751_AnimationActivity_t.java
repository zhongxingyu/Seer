 package fi.testbed2.activity;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import fi.testbed2.app.MainApplication;
 import fi.testbed2.task.DownloadImagesTask;
 import fi.testbed2.util.SeekBarUtil;
 import fi.testbed2.view.AnimationView;
 import fi.testbed2.R;
 
 public class AnimationActivity extends AbstractActivity implements OnClickListener, SeekBar.OnSeekBarChangeListener {
 
    private AnimationView animationView;
 	private ImageButton playPauseButton;
 	private boolean isPlaying = true;
 	private TextView timestampView;
     private SeekBar seekBar;
 
     private DownloadImagesTask task;
 
     private int orientation;
 
     private boolean allImagesDownloaded;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // we want more space for the animation
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         setContentView(R.layout.animation);
 
         initButtons();
         initViews();
         initAnimation();
 
         orientation = this.getResources().getConfiguration().orientation;
 
     }
 
     private void initButtons() {
 
         playPauseButton = (ImageButton) findViewById(R.id.playpause_button);
         playPauseButton.setOnClickListener(this);
 
         seekBar = (SeekBar)findViewById(R.id.seek);
         seekBar.setOnSeekBarChangeListener(this);
 
     }
 
     private void initViews() {
         animationView = (AnimationView) findViewById(R.id.animation_view);
         animationView.setAllImagesDownloaded(false);
         timestampView = (TextView) findViewById(R.id.timestamp_view);
     }
 
     private void initAnimation() {
 
         animationView.post(new Runnable() {
             @Override
             public void run() {
                 final Rect bounds = getSavedMapBounds();
                 updatePlayingState(true);
                 if (bounds==null) {
                     animationView.start(timestampView, seekBar);
                 } else {
                     animationView.start(timestampView, seekBar, bounds);
                 }
 
             }
         });
 
     }
 
 
     /**
      * Updates the Downloading text in top left corner
      * @param text
      */
     public void updateDownloadProgressInfo(final String text) {
         runOnUiThread(new Runnable() {
             public void run() {
                 animationView.setDownloadProgressText(text);
                 timestampView.invalidate();
             }
         });
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         saveMapBounds();
         orientation = newConfig.orientation;
         updateBoundsToView();
     }
 
     private String getMapBoundsPreferenceKey() {
        return MainApplication.PREF_ORIENTATION_PREFERENCE_KEY_PREFIX + orientation;
     }
 
     /**
      * Saves the bounds of the map user has previously viewed to persistent storage.
      */
     private void saveMapBounds() {
 
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         Editor editor = sharedPreferences.edit();
         Rect bounds = animationView.getBounds();
 
         // bounds String format is left:top:right:bottom
         if (editor!=null && bounds!=null) {
             editor.putString(getMapBoundsPreferenceKey(),
                     "" + bounds.left + ":" + bounds.top + ":" + bounds.right + ":" + bounds.bottom);
             editor.commit();
         }
 
     }
 
     /**
      * Returns the saved map bounds user has previously used
      * @return
      */
     private Rect getSavedMapBounds() {
 
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         // left:top:right:bottom
         String frameBoundsPref = sharedPreferences.getString(getMapBoundsPreferenceKey(), null);
 
         if (frameBoundsPref==null) {
             return null;
         }
 
         String[] parts = frameBoundsPref.split(":");
         final Rect bounds = new Rect(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 
         return bounds;
 
     }
 
     private void updateBoundsToView() {
         animationView.updateBounds(getSavedMapBounds());
     }
 
     private void updateFrameDelayToView() {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         animationView.setFrameDelay(Integer.parseInt(
                 sharedPreferences.getString(MainApplication.PREF_ANIM_FRAME_DELAY, "1000")));
     }
 
     private boolean startAnimationAutomatically() {
         SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         return sharedPreferences.getBoolean(MainApplication.PREF_ANIM_AUTOSTART, true);
     }
 
     @Override
 	protected void onPause() {
         super.onPause();
         task.abort();
         this.pauseAnimation();
         this.saveMapBounds();
 	}
 
     @Override
     protected void onResume() {
         super.onResume();
         updateBoundsToView();
         updateFrameDelayToView();
         if (!allImagesDownloaded) {
             task = new DownloadImagesTask(this);
             task.execute();
         }
     }
 
     public void onAllImagesDownloaded() {
         allImagesDownloaded = true;
         animationView.setAllImagesDownloaded(true);
         animationView.setDownloadProgressText(null);
         animationView.refresh(getApplicationContext());
         animationView.previous();
         updatePlayingState(false);
         if (startAnimationAutomatically()) {
             playAnimation();
         }
     }
 
     private void playAnimation() {
         updatePlayingState(true);
         animationView.play();
     }
 
     private void pauseAnimation() {
         updatePlayingState(false);
         animationView.pause();
     }
 
     @Override
 	public void onClick(View v) {
         if (!allImagesDownloaded) {
             return;
         }
 		switch(v.getId()) {
             case R.id.playpause_button:
                 animationView.playpause();
                 updatePlayingState(!isPlaying);
                 break;
             default:
                 return;
 		}
 	}
 
     private void updatePlayingState(boolean animationIsPlaying) {
 
         isPlaying = animationIsPlaying;
 
         if (isPlaying) {
             playPauseButton.setImageResource(R.drawable.ic_media_pause);
         } else {
             playPauseButton.setImageResource(R.drawable.ic_media_play);
         }
 
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unbindDrawables(findViewById(R.id.AnimationRootView));
         MainApplication.clearData();
     }
 
     @Override
     public void onRefreshButtonSelected() {
         this.pauseAnimation();
         this.allImagesDownloaded = false;
         Intent intent = new Intent();
         this.setResult(MainApplication.RESULT_REFRESH, intent);
         this.finish();
     }
 
     @Override
     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
         if (fromUser) {
            this.pauseAnimation();
             animationView.goToFrame(SeekBarUtil.getFrameIndexFromSeekBarValue(progress,
                     MainApplication.getTestbedParsedPage().getAllTestbedImages().size()));
         }
     }
 
     @Override
     public void onStartTrackingTouch(SeekBar seekBar) {
         this.pauseAnimation();
     }
 
     @Override
     public void onStopTrackingTouch(SeekBar seekBar) {
        // Don't do anything
     }
 }
