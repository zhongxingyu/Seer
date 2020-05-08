 package org.vimeoid.activity;
 
 import java.io.FileInputStream;
 
 import org.vimeoid.R;
 import org.vimeoid.media.VimeoVideoPlayingTask;
 import org.vimeoid.util.Dialogs;
 import org.vimeoid.util.Invoke;
 import org.vimeoid.util.Utils;
 
 import android.app.Activity;
 import android.graphics.PixelFormat;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.SurfaceView;
 import android.view.View;
 
 public class Player extends Activity {
     
     public static final String TAG = "PlayerActivity";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.TRANSPARENT);		
 		
 		setContentView(R.layout.player);
 		
 		final long videoId = getIntent().getLongExtra(Invoke.VIDEO_ID_EXTRA, -1);
 		if (videoId == -1) throw new IllegalStateException("Video ID must be passed to player");
 		
		//final View progressView = findViewById(R.id.progressHolder);
		//final View surfaceWrapper = findViewById(R.id.surfaceWrapper);
 		final SurfaceView surface = (SurfaceView) findViewById(R.id.surface);
 		
 		Log.d(TAG, "Running video player for video " + videoId);
 		
 		new VimeoVideoPlayingTask(this, surface.getHolder()) {
 			
 			protected void onPreExecute() {
                //progressView.setVisibility(View.VISIBLE);
                //surfaceWrapper.setVisibility(View.GONE);
 				super.onPreExecute();
 			};
 			
 			protected void onPostExecute(FileInputStream dataSource) {
 				super.onPostExecute(dataSource);
                //progressView.setVisibility(View.GONE);  
                //getWindow().setFormat(PixelFormat.TRANSPARENT);
                //surfaceWrapper.setVisibility(View.VISIBLE);
                //surface.requestFocus();				
 			};
 			
 			protected void onNoSpaceForVideoCache(long required, long actual) {
 				Dialogs.makeLongToast(Player.this, Utils.format(getString(R.string.no_space_for_video_cache), 
 							                       "required", String.valueOf(required >> 10),
 							                       "actual", String.valueOf(actual >> 10)));
 				finish();				
 			};
 			
 		}.execute(videoId);
 		
 	}
 	
 	@Override
 	protected void onStop() {
 	    super.onStop();
 	    
 	    VimeoVideoPlayingTask.ensureCleanedUp();
 	}
 	
 }
