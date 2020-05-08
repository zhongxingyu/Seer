 package gsn.atl.surfacedrawing;
 
 import android.content.*;
 import android.graphics.*;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.*;
 import android.widget.FrameLayout;
 
 import java.io.IOException;
 
 public class MainActivity extends Activity implements
         SurfaceHolder.Callback,
         MediaPlayer.OnPreparedListener,
         VideoControllerView.MediaPlayerControl{
     private final static String TAG = "Main";
 
     // video player
     SurfaceView videoSurface;
     SurfaceView closedCaptioningSurface;
     MediaPlayer player;
     VideoControllerView controller;
 
     // closed captioning service
     private ClosedCaptioningService closedCaptioningService;
     boolean bound = false;
 
     // broadcast receiver for CC messages
     private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             Bundle bundle = intent.getExtras();
             if (bundle != null){
                 String text = bundle.getString("CC_MESSAGE");
                 SurfaceHolder closedCaptioningHolder = closedCaptioningSurface.getHolder();
 
                 // draw CC text
                 if (closedCaptioningHolder != null) {
                     closedCaptioningHolder.setFormat(PixelFormat.TRANSPARENT);
                     Point size = new Point();
                     getWindowManager().getDefaultDisplay().getSize(size);
                     Canvas canvas = closedCaptioningHolder.lockCanvas(null);
                     Paint paint = new Paint();
                     paint.setColor(0xffffffff);
                     paint.setTextSize(64);
                     canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                     canvas.drawText(text, size.x * .25f, size.y * .75f, paint);
                     closedCaptioningHolder.unlockCanvasAndPost(canvas);
                 }
             }
         }
     };
 
     private ServiceConnection connection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             closedCaptioningService = ((ClosedCaptioningService.ClosedCaptioningBinder)service).getService();
             Log.d(TAG, "Connected to CC Service");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
             closedCaptioningService = null;
             Log.d(TAG, "Disconnected from CC Service");
         }
     };
 
     void doBindService(){
         bindService(new Intent(this, ClosedCaptioningService.class), connection, Context.BIND_AUTO_CREATE);
         bound = true;
     }
 
     void doUnbindService(){
         if (bound){
             unbindService(connection);
             bound = false;
         }
     }
 
     @Override
     protected void onDestroy(){
         super.onDestroy();
         doUnbindService();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
         SurfaceHolder videoHolder = videoSurface.getHolder();
         videoHolder.addCallback(this);
 
         closedCaptioningSurface = (SurfaceView) findViewById(R.id.closedCaptioningSurface);
 
         player = new MediaPlayer();
         controller = new VideoControllerView(this);
 
         try {
             player.setAudioStreamType(AudioManager.STREAM_MUSIC);
             player.setDataSource(this, Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
             player.setOnPreparedListener(this);
         } catch (IllegalArgumentException e){
             e.printStackTrace();
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (IllegalStateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     protected void onPause(){
         super.onPause();
         unregisterReceiver(broadcastReceiver);
         player.stop();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event){
         controller.show();
         return false;
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         player.setDisplay(holder);
         player.prepareAsync();
     }
 
 
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
 
     }
 
     @Override
     public void start() {
         player.start();
     }
 
     @Override
     public void pause() {
         player.pause();
     }
 
     @Override
     public int getDuration() {
         return player.getDuration();
     }
 
     @Override
     public int getCurrentPosition() {
         return player.getCurrentPosition();
     }
 
     @Override
     public void seekTo(int pos) {
         player.seekTo(pos);
     }
 
     @Override
     public boolean isPlaying() {
         return player.isPlaying();
     }
 
     @Override
     public int getBufferPercentage() {
         return 0;
     }
 
     @Override
     public boolean canPause() {
         return true;
     }
 
     @Override
     public boolean canSeekBackward() {
         return true;
     }
 
     @Override
     public boolean canSeekForward() {
         return true;
     }
 
     @Override
     public boolean isFullScreen() {
         return false;
     }
 
     @Override
     public void toggleFullScreen() {
 
     }
 
     @Override
     public void onPrepared(MediaPlayer mp) {
         controller.setMediaPlayer(this);
         controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
         player.start();
         doBindService();
         registerReceiver(broadcastReceiver, new IntentFilter(ClosedCaptioningService.NOTIFICATION));
     }
 }
