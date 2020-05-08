 package ca.ilanguage.oprime.content;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 
 import ca.ilanguage.oprime.activity.HTML5GameActivity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.Toast;
 
 public class JavaScriptInterface implements Serializable {
 
   private static final long serialVersionUID = -4666851545498417224L;
   protected String TAG = OPrime.OPRIME_TAG;
   protected boolean D = false;
   protected Context mContext;
   protected String mOutputDir;
   public MediaPlayer mMediaPlayer;
   protected int mRequestedMediaPlayer = 0;
   protected Handler mHandler;
   public ListenForEndAudioInterval mListenForEndAudioInterval;
   public int mCurrentAudioPosition;
   protected String mAssetsPrefix;
   protected String mAudioPlaybackFileUrl;
   protected String mAudioRecordFileUrl;
   protected String mTakeAPictureFileUrl;
   public HTML5GameActivity mUIParent;
 
   /**
    * Can pass in all or none of the parameters. Expects the caller to set the
    * context after initialization. This allows this class to be serialized and
    * sent as an Extra for maximum modularity.
    * 
    * @param d
    *          Whether or not the app should log out
    * @param tag
    *          The TAG for the logging
    * @param outputDir
    *          usually on the sdcard where users can see the files, not in the
    *          data dirs (problems opening the files in a webview if you choose
    *          the data dirs)
    * @param context
    *          usually getApplicationContext()
    * @param UIParent
    *          The UI where UI events can happen ie, sending info back to the
    *          webview (usually: this)
    * @param assetsPrefix
    *          the folders deep in the assets dir to get to the base where the
    *          chrome extension is.(example: release/)
    */
   public JavaScriptInterface(boolean d, String tag, String outputDir,
       Context context, HTML5GameActivity UIParent, String assetsPrefix) {
     D = d;
     TAG = tag;
     mOutputDir = outputDir;
     mContext = context;
     if (D)
       Log.d(TAG, "Initializing the Javascript Interface (JSI).");
     mAudioPlaybackFileUrl = "";
     mUIParent = UIParent;
     mAssetsPrefix = assetsPrefix;
     mHandler = new Handler();
   }
 
   public JavaScriptInterface(Context context) {
     mContext = context;
     mOutputDir = OPrime.OUTPUT_DIRECTORY;
     mAudioPlaybackFileUrl = "";
     if (D)
       Log.d(TAG, "Initializing the Javascript Interface (JSI).");
     mHandler = new Handler();
 
   }
 
   public void pauseAudio() {
     if (mMediaPlayer != null) {
       if (mMediaPlayer.isPlaying()) {
         mMediaPlayer.pause();
         mCurrentAudioPosition = mMediaPlayer.getCurrentPosition();
       }
     }
   }
 
   public void stopAudio() {
     if (mMediaPlayer != null) {
       if (mMediaPlayer.isPlaying()) {
         mMediaPlayer.stop();
       }
       mMediaPlayer.release();
       mMediaPlayer = null;
     }
   }
 
   public void playAudio(String urlstring) {
     urlstring = urlstring.trim();
     if (urlstring == null || urlstring == "") {
       return;
     }
     if (D)
       Log.d(TAG, "In the play Audio JSI :" + urlstring + ": playing:"+mAudioPlaybackFileUrl+":");
     if (mAudioPlaybackFileUrl.contains(urlstring)) {
       /*
        * Same audio file
        */
       if (D)
         Log.d(TAG, "Resuming play of the same file :" + mAudioPlaybackFileUrl + ":");
       if (mMediaPlayer != null) {
         if (mMediaPlayer.isPlaying()) {
           mMediaPlayer.pause();
           mCurrentAudioPosition = mMediaPlayer.getCurrentPosition();
           return;
         } else {
           mMediaPlayer.seekTo(mCurrentAudioPosition);
           mMediaPlayer.start();
           return;
         }
       }
     } else {
       /*
        * New audio file
        */
       if (D)
         Log.d(TAG, "Playing new file from the beginning :" + mAudioPlaybackFileUrl + ":");
       if (mMediaPlayer != null) {
         if (mMediaPlayer.isPlaying()) {
           mMediaPlayer.stop();
         }
         mMediaPlayer.release();
         mMediaPlayer = null;
       }
     }
     this.setAudioFile(urlstring, 0, 0);
 
   }
 
   /**
    * This is a private method which is used by this class to attach a file to
    * the media player. It is called by either playAudio or playIntervalOfAudio
    * (if its a new audio file or the media player is null).
    * 
    * Preconditions: the mediaplayer is null. IF both cueTo and endAt are 0 then
    * it will play the entire audio
    * 
    * @param urlstring
    *          The file name either on the sdcard, on the web, or in the assets
    *          folder.
    * @param cueTo
    *          The position in milliseconds to play from.
    * @param endAt
    *          The position in milliseconds to end playback. If then the audio
    *          will play completely.
    */
   protected void setAudioFile(final String urlstring, final int cueTo,
       final int endAt) {
     mMediaPlayer = new MediaPlayer();
     try {
       if (urlstring.contains("android_asset")) {
         String tempurlstring = urlstring.replace("file:///android_asset/", "");
         mAudioPlaybackFileUrl = tempurlstring;
         AssetFileDescriptor afd = mContext.getAssets().openFd(
             mAudioPlaybackFileUrl);
         mMediaPlayer.setDataSource(afd.getFileDescriptor(),
             afd.getStartOffset(), afd.getLength());
         afd.close();
       } else if (urlstring.contains("sdcard")) {
         mAudioPlaybackFileUrl = urlstring;
         mMediaPlayer.setDataSource(mAudioPlaybackFileUrl);
       } else {
         if (D)
           Log.d(TAG, "This is what the audiofile looked like:" + urlstring);
         String tempurlstring = urlstring.replaceFirst("/", mAssetsPrefix);
         mAudioPlaybackFileUrl = tempurlstring;
         if (D)
           Log.d(TAG, "This is what the audiofile looks like:"
               + mAudioPlaybackFileUrl);
 
         AssetFileDescriptor afd = mContext.getAssets().openFd(
             mAudioPlaybackFileUrl);
         mMediaPlayer.setDataSource(afd.getFileDescriptor(),
             afd.getStartOffset(), afd.getLength());
         afd.close();
       }
       mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
         public void onPrepared(MediaPlayer mp) {
           if (D)
             Log.d(TAG, "Starting to play the audio.");
           if (cueTo == 0 && endAt == 0) {
             mMediaPlayer.start();
           } else {
             playIntervalOfAudio(mAudioPlaybackFileUrl, cueTo, endAt);
           }
         }
       });
       mMediaPlayer
           .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mp) {
               if (D)
                 Log.d(TAG, "Audio playback is complete, releasing the audio.");
 
               mMediaPlayer.release();
               mMediaPlayer = null;
               // mUIParent.loadUrlToWebView();
               LoadUrlToWebView v = new LoadUrlToWebView();
               v.setMessage("javascript:OPrime.hub.publish('playbackCompleted','"
                   + mAudioPlaybackFileUrl + "');");
               v.execute();
             }
           });
       mMediaPlayer.prepareAsync();
     } catch (IllegalArgumentException e) {
       Log.e(TAG, "There was a problem with the sound " + e.getMessage());
       e.printStackTrace();
     } catch (IllegalStateException e) {
       Log.e(
           TAG,
           "There was a problem with the sound, starting anyway"
               + e.getMessage());
       mMediaPlayer.start();// TODO check why this is still here.
     } catch (IOException e) {
       Log.e(TAG, "There was a problem with the  sound " + e.getMessage());
       e.printStackTrace();
 
     } catch (Exception e) {
       Log.e(TAG, "There was a problem with the  sound " + e.getMessage());
       e.printStackTrace();
     }
   }
 
   public void playIntervalOfAudio(String urlstring, final int startTimeMS,
       final int endTimeMS) {
     if (D)
       Log.d(TAG, "In milliseconds from " + startTimeMS + " to " + endTimeMS);
     if (mMediaPlayer != null) {
       mRequestedMediaPlayer = 0;
       // If the audio is already playing, pause it
       if (mMediaPlayer.isPlaying()) {
         mMediaPlayer.pause();
       }
       // If there is a background timer waiting for the previous audio interval
       // to finish, kill it
       if (mListenForEndAudioInterval != null
           && !mListenForEndAudioInterval.isCancelled()) {
         mListenForEndAudioInterval.cancel(true);
         // mListenForEndAudioInterval = null;
       }
       mMediaPlayer.seekTo(startTimeMS);
       mMediaPlayer
           .setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
             public void onSeekComplete(MediaPlayer mediaPlayer) {
               if (D)
                 Log.d(
                     TAG,
                     "current audio position... "
                         + mMediaPlayer.getCurrentPosition());
               mMediaPlayer.start();
               mMediaPlayer.setOnSeekCompleteListener(null);
 
               // If the endtime is valid, start an async task to loop until the
               // audio gets to the endtime
               if (endTimeMS < mMediaPlayer.getDuration()) {
                 mListenForEndAudioInterval = new ListenForEndAudioInterval();
                 mListenForEndAudioInterval.setEndAudioInterval(endTimeMS);
                 mListenForEndAudioInterval.execute();
               }
             }
           });
     } else {
       if (mRequestedMediaPlayer < 2) {
         this.setAudioFile(urlstring, startTimeMS, endTimeMS);
         mRequestedMediaPlayer++;
       } else {
         this.showToast("There is a problem starting the media player for this audio: "
             + urlstring);
       }
     }
   }
 
   public void startAudioRecordingService(String resultfilename) {
     new File(mOutputDir).mkdirs();
     if (mAudioRecordFileUrl != null) {
       return;
     }
     if (D)
       Log.d(TAG, "This is what the audiofile looked like:" + resultfilename);
     String tempurlstring = "";
     tempurlstring = resultfilename.replaceFirst("/", "").replaceFirst("file:",
         "");
     mAudioRecordFileUrl = mOutputDir + tempurlstring;
     if (D)
       Log.d(TAG, "This is what the audiofile looks like:" + mAudioRecordFileUrl);
 
     Intent intent;
     intent = new Intent(OPrime.INTENT_START_AUDIO_RECORDING);
     intent.putExtra(OPrime.EXTRA_RESULT_FILENAME, mAudioRecordFileUrl);
     mUIParent.startService(intent);
     // Publish audio recording started
     LoadUrlToWebView v = new LoadUrlToWebView();
     v.setMessage("javascript:OPrime.hub.publish('audioRecordingSucessfullyStarted','"
         + mAudioRecordFileUrl + "');");
     v.execute();
   }
 
   public void stopAudioRecordingService(String resultfilename) {
     // TODO could do some checking to see if the same file the HTML5 wants us to
     // stop is similarly named to the one in the Java
     // if(mAudioRecordFileUrl.contains(resultfilename))
     if (mAudioRecordFileUrl == null) {
       return;
     }
     Intent audio = new Intent(OPrime.INTENT_START_AUDIO_RECORDING);
     mUIParent.stopService(audio);
     // Publish stopped audio
     LoadUrlToWebView v = new LoadUrlToWebView();
     v.setMessage("javascript:OPrime.hub.publish('audioRecordingSucessfullyStopped','"
         + mAudioRecordFileUrl + "');");
     v.execute();
     // TODO add broadcast and listener from audio service to be sure the file
     // works(?)
     LoadUrlToWebView t = new LoadUrlToWebView();
     t.setMessage("javascript:OPrime.hub.publish('audioRecordingCompleted','"
         + mAudioRecordFileUrl + "');");
     t.execute();
     // null out the audio file to be sure this is called once per audio file.
     mAudioRecordFileUrl = null;
   }
 
   public String getAudioDir() {
     // if its the sdcard, or a web url send that instead
     return mOutputDir;// "file:///android_asset/";
   }
 
   public void takeAPicture(String resultfilename){
     new File(mOutputDir).mkdirs();
     
     if (D)
       Log.d(TAG, "This is what the image file looked like:" + resultfilename);
     String tempurlstring = "";
     tempurlstring = resultfilename.replaceFirst("/", "").replaceFirst("file:",
         "");
     mTakeAPictureFileUrl = mOutputDir + tempurlstring;
     if (D)
       Log.d(TAG, "This is what the image file looks like:" + mTakeAPictureFileUrl);
 
     // Publish picture taking started
     LoadUrlToWebView v = new LoadUrlToWebView();
     v.setMessage("javascript:OPrime.hub.publish('pictureCaptureSucessfullyStarted','"
         + mTakeAPictureFileUrl + "');");
     v.execute();
 
     Intent intent;
     intent = new Intent(OPrime.INTENT_TAKE_PICTURE);
     intent.putExtra(OPrime.EXTRA_RESULT_FILENAME, mTakeAPictureFileUrl);
     mUIParent.startActivityForResult(intent, OPrime.PICTURE_TAKEN);
   }
   
   public void showToast(String toast) {
     Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
     if (D)
       Log.d(TAG, "Showing toast " + toast);
 
   }
 
   public void shareIt(String message) {
     Intent share = new Intent(Intent.ACTION_SEND);
     share.setType("text/plain");
     share.putExtra(Intent.EXTRA_TEXT, message);
     mContext.startActivity(Intent.createChooser(share, "Share with"));
   }
 
   public class LoadUrlToWebView extends AsyncTask<Void, Void, String> {
     private String mMessage;
 
     @Override
     protected String doInBackground(Void... params) {
 
       String result = "";
       return result;
     }
 
     protected void onPreExecute() {
     }
 
     protected void setMessage(String message) {
       mMessage = message;
     }
 
     protected void onPostExecute(String result) {
       if (mUIParent != null && mUIParent.mWebView != null) {
         Log.d(
             TAG,
             "\tPost execute LoadUrlToWebView task. Now trying to send a pubsub message to the webview.");
         mUIParent.mWebView.loadUrl(mMessage);
       }
     }
   }
 
   public class ListenForEndAudioInterval extends AsyncTask<Void, Void, String> {
     private int endAudioInterval;
 
     @Override
     protected String doInBackground(Void... params) {
       if (mMediaPlayer == null) {
         return "No media playing";
       }
 
       long currentPos = mMediaPlayer.getCurrentPosition();
       while (currentPos < endAudioInterval) {
         try {
           // wait some period
           Thread.sleep(100);
           currentPos = mMediaPlayer.getCurrentPosition();
         } catch (InterruptedException e) {
           return "Cancelled";
         }
       }
       mMediaPlayer.pause();
       Log.d(TAG, "\tPaused audio at ... " + mMediaPlayer.getCurrentPosition());
       return "End audio interval";
     }
 
     protected void onPreExecute() {
     }
 
     protected void setEndAudioInterval(int message) {
       endAudioInterval = message;
     }
 
     protected void onPostExecute(String result) {
       Log.d(TAG,
           "\t" + result + ": Stopped listening for audio interval at ... "
              + mMediaPlayer.getCurrentPosition());
     }
   }
 
   public Context getContext() {
     return mContext;
   }
 
   public void setContext(Context mContext) {
     this.mContext = mContext;
   }
 
   public String getTAG() {
     return TAG;
   }
 
   public void setTAG(String tAG) {
     TAG = tAG;
   }
 
   public boolean isD() {
     return D;
   }
 
   public void setD(boolean d) {
     D = d;
   }
 
   public String getOutputDir() {
     return mOutputDir;
   }
 
   public void setOutputDir(String mOutputDir) {
     this.mOutputDir = mOutputDir;
   }
 
   public String getAudioFileUrl() {
     return mAudioPlaybackFileUrl;
   }
 
   public void setAudioFileUrl(String mAudioPlaybackFileUrl) {
     this.mAudioPlaybackFileUrl = mAudioPlaybackFileUrl;
   }
 
   public HTML5GameActivity getUIParent() {
     return mUIParent;
   }
 
   public void setUIParent(HTML5GameActivity mUIParent) {
     this.mUIParent = mUIParent;
   }
 
   public String getAssetsPrefix() {
     return mAssetsPrefix;
   }
 
   public void setAssetsPrefix(String mAssetsPrefix) {
     this.mAssetsPrefix = mAssetsPrefix;
   }
 
 }
