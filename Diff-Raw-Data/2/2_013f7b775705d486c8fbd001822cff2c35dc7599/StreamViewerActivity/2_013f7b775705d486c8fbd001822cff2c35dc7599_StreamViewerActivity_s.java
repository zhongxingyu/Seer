 package cryptocast.client;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cryptocast.comm.StreamMessageInChannel;
 import cryptocast.crypto.BroadcastEncryptionClient;
 import cryptocast.crypto.SchnorrGroup;
 import cryptocast.crypto.naorpinkas.*;
 import cryptocast.util.SerializationUtils;
 
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.MediaController;
 import android.widget.ProgressBar;
 
 /**
  * This activity is responsible for decrypting the received data
  * and viewing it.
  */
 public class StreamViewerActivity extends ClientActivity
                                   implements AudioStreamMediaPlayer.OnCompletionListener,
                                              AudioStreamMediaPlayer.OnErrorListener,
                                              MediaController.MediaPlayerControl, 
                                              OnTouchListener,
                                              OnPreparedListener {
     
     private static final Logger log = LoggerFactory
             .getLogger(StreamViewerActivity.class);
     
     private AudioStreamMediaPlayer player = new AudioStreamMediaPlayer();
     private MediaController mediaController;
     private InetSocketAddress connectAddr;
     private File keyFile;
     private Socket sock;
     private ProgressBar spinner;
     
     @Override
     protected void onCreate(Bundle b) {
         super.onCreate(b);
         setContentView(R.layout.activity_stream_viewer);
         Bundle args = getIntent().getExtras();
         connectAddr = (InetSocketAddress) args.getSerializable("connectAddr");
         keyFile = (File) args.getSerializable("keyFile");
         log.debug("Created with: connectAddr={} keyFile={}", connectAddr, keyFile);
         
         mediaController = new MediaController(this);
         mediaController.setMediaPlayer(this);
         mediaController.setAnchorView(findViewById(R.id.MediaController1));
         mediaController.setEnabled(true);
         
         findViewById(R.id.MediaController1).setOnTouchListener(this);
         spinner = (ProgressBar) findViewById(R.id.progressBar1);
         spinner.setVisibility(View.VISIBLE);
         
         player.setOnCompletionListener(this);
         player.setOnErrorListener(this);
         player.setOnPreparedListener(this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         connectToStream();
     }
     
     private void connectToStream() {
         log.debug("Connecting to {}", connectAddr);
        Socket sock = new Socket();
 
         try {
             sock.connect(connectAddr, 5000);
         } catch (Exception e) {
             log.error("Could not connect to target server", e);
             showErrorDialog("Could not connect to server!", finishOnClick);
             return;
         }
         log.debug("Connected to {}", connectAddr);
         receiveData();
     }
 
     private void receiveData() {
         NPKey<BigInteger, SchnorrGroup> key;
         try {
             key = SerializationUtils.readFromFile(keyFile);
         } catch (Exception e) {
             log.error("Could not load key from file: ", e);
             showErrorDialog("Invalid key file!", finishOnClick);
             app.getServerHistory().invalidateKeyFile(connectAddr);
             return;
         }
         log.debug("Key file successfully read.");
         try {
             BroadcastEncryptionClient in =
                     new BroadcastEncryptionClient(
                             new StreamMessageInChannel(sock.getInputStream()), 
                             new SchnorrNPClient(key));
             log.debug("Waiting for first byte");
             in.read();
             log.debug("Starting media player");
             player.setRawDataSource(in, "audio/mpeg");
             player.prepare();
         } catch (Exception e) {
             log.error("Error while playing stream", e);
             showErrorDialog("Error while playing stream!", finishOnClick);
             return;
         }
     }
     
     
     @Override
     protected void onResume() {
         super.onResume();
         player.start();
     }
     
     @Override
     protected void onPause() {
         player.pause();
         super.onPause();
     }
 
     @Override
     public void onCompletion(AudioStreamMediaPlayer p) {
         try {
             sock.close();
         } catch (Throwable e) { }
     }
     
     @Override
     public boolean onError(AudioStreamMediaPlayer p, int what, int extra) {
         log.error("MediaPlayer error: {} {}", formatError(what), formatError(extra));
         return false;
     }
     
     private String formatError(int what) {
         switch (what) {
         case MediaPlayer.MEDIA_ERROR_UNKNOWN: return "MEDIA_ERROR_UNKNOWN";
         case MediaPlayer.MEDIA_ERROR_SERVER_DIED: return "MEDIA_ERROR_SERVER_DIED";
         case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK: 
             return "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK";
         default: return "MediaError(" + what + ")";
         }
     }
 
     @Override
     protected void onStop() {
         player.stop();
         super.onStop();
     }
     
     /** Handles a click on the bottom menu.
      * @param item The clicked menu item
      */
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         return false;
     }
 
     /**
      * Toggle playback play/pause. Will pause if in play mode and continue if
      * in pause mode.
      */
     public void togglePlay() { }
 
     /**
      * @return Whether the player is in playing mode.
      */
     public boolean isPlaying() { return false; }
 
     @Override
     public boolean canPause() {
         return true;
     }
 
     @Override
     public boolean canSeekBackward() {
         return false;
     }
 
     @Override
     public boolean canSeekForward() {
         return false;
     }
 
     @Override
     public int getBufferPercentage() {
         return 0;
     }
 
     @Override
     public int getCurrentPosition() {
         return 0;
     }
 
     @Override
     public int getDuration() {
         return 0;
     }
 
     @Override
     public void pause() {
         player.pause();
     }
 
     @Override
     public void seekTo(int pos) {
     }
 
     @Override
     public void start() {
         player.start();
     }
 
     @Override
     public boolean onTouch(View arg0, MotionEvent arg1) {
         mediaController.show();
         return false;
     }
 
     @Override
     public void onPrepared(MediaPlayer arg0) {
         spinner.setVisibility(View.INVISIBLE);
     }
     @Override  
     public void onBackPressed() {
         log.debug("Back pressed.");
         super.onBackPressed();
     }
 }
