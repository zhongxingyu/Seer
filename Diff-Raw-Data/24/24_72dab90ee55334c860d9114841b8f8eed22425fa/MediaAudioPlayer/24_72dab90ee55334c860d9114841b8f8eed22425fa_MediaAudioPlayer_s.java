 /*
    Copyright 2012 Mikhail Chabanov
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package mc.lib.audio;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 
 import mc.lib.interfaces.OnCompleteListener;
 import mc.lib.interfaces.OnProgressListener;
 import mc.lib.stream.StreamHelper;
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
 import android.os.Handler;
 import android.util.Log;
 
 public class MediaAudioPlayer implements IAudioPlayer, OnPreparedListener, OnCompletionListener, OnErrorListener
 {
     private static final String LOGTAG = MediaAudioPlayer.class.getSimpleName();
 
     private MediaPlayer mediaPlayer = new MediaPlayer();
    private OnCompleteListener completeListener;
     private OnProgressListener progressListener;
     private Handler handler;
     private Context context;
     private boolean paused;
 
     private Runnable progressNotifier = new Runnable()
     {
         @Override
         public void run()
         {
             if(progressListener != null)
                 progressListener.notifyProgress(mediaPlayer.getCurrentPosition(), getDuration());
             Log.i(LOGTAG, "Progress notifier called. Progress:" + mediaPlayer.getCurrentPosition());
             handler.postDelayed(progressNotifier, 500);
         }
     };
 
     public MediaAudioPlayer(Context context)
     {
         super();
         this.context = context;
         mediaPlayer.setOnErrorListener(this);
         mediaPlayer.setOnPreparedListener(this);
         mediaPlayer.setOnCompletionListener(this);
     }
 
     @Override
     public void play(int resId)
     {
         AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
         if(afd == null)
         {
             Log.e(LOGTAG, "Cannot open  from res:" + resId);
             return;
         }
         play(afd);
         StreamHelper.close(afd);
     }
 
     @Override
     public void play(String filePath)
     {
         try
         {
             reset();
             mediaPlayer.setDataSource(filePath);
             mediaPlayer.prepareAsync();
         }
         catch(Exception e)
         {
             Log.e(LOGTAG, "Error on preparing track " + filePath, e);
         }
     }
 
     @Override
     public void play(File path)
     {
         play(path.getAbsolutePath());
     }
 
     @Override
     public void play(Uri uri)
     {
         try
         {
             reset();
             mediaPlayer.setDataSource(context, uri);
             mediaPlayer.prepareAsync();
         }
         catch(Exception e)
         {
             Log.e(LOGTAG, "Error on preparing track " + uri, e);
         }
     }
 
     @Override
     public void play(InputStream is)
     {
         String tmpFile;
         try
         {
             tmpFile = context.getCacheDir() + "/audio.tmp";
            Runtime.getRuntime().exec("chmod 644 " + tmpFile);
             FileOutputStream fos = new FileOutputStream(tmpFile);
             StreamHelper.copyStream(is, fos);
             StreamHelper.close(fos);
         }
         catch(Exception e)
         {
             Log.e(LOGTAG, "Cannot create cache file.", e);
             return;
         }
         play(tmpFile);
     }
 
     @Override
     public void play(AssetFileDescriptor afd)
     {
         try
         {
             reset();
             mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
             mediaPlayer.prepareAsync();
         }
         catch(Exception e)
         {
             Log.e(LOGTAG, "Error on preparing track " + afd, e);
         }
     }
 
     private void reset()
     {
         if(isPlaying())
             stop();
         mediaPlayer.reset();
     }
 
     @Override
     public boolean getRepeat()
     {
         return mediaPlayer.isLooping();
     }
 
     @Override
     public void setRepeat(boolean repeat)
     {
         mediaPlayer.setLooping(repeat);
     }
 
     @Override
     public void stop()
     {
         stopProgressNotifier();
         mediaPlayer.stop();
         paused = false;
         Log.i(LOGTAG, "Play stoped");
     }
 
     @Override
     public void pause()
     {
         stopProgressNotifier();
         mediaPlayer.pause();
         paused = true;
         Log.i(LOGTAG, "Play paused");
     }
 
     @Override
     public void resume()
     {
         if(paused)
         {
             mediaPlayer.start();
             startProgressNotifier();
             paused = false;
             Log.i(LOGTAG, "Play resumed");
         }
     }
 
     @Override
     public boolean isPlaying()
     {
         return mediaPlayer.isPlaying();
     }
 
     @Override
     public void setVolume(float volume)
     {
         mediaPlayer.setVolume(volume, volume);
     }
 
     @Override
     public void onCompletion(MediaPlayer mp)
     {
         if(mp.equals(mediaPlayer))
         {
             stopProgressNotifier();
             if(completeListener != null)
             {
                 completeListener.complete(getClass());
             }
             Log.i(LOGTAG, "Play completed");
         }
     }
 
     @Override
     public void onPrepared(MediaPlayer mp)
     {
         if(mp.equals(mediaPlayer))
         {
             Log.i(LOGTAG, "Audio prepared. Start playing");
             mediaPlayer.start();
             startProgressNotifier();
         }
     }
 
     private void startProgressNotifier()
     {
         if(progressListener != null)
         {
             if(handler == null)
                 handler = new Handler();
             handler.post(progressNotifier);
         }
     }
 
     private void stopProgressNotifier()
     {
         if(handler != null)
             handler.removeCallbacks(progressNotifier);
     }
 
     @Override
     public int getDuration()
     {
         return mediaPlayer.getDuration();
     }
 
     @Override
     public boolean onError(MediaPlayer mp, int what, int extra)
     {
         Log.e(LOGTAG, "Media preparation error:" + what + ", " + extra);
         return true;
     }
 
     @Override
    public OnCompleteListener getOnCompleteListener()
     {
         return completeListener;
     }
 
     @Override
    public void setOnCompleteListener(OnCompleteListener listener)
     {
         this.completeListener = listener;
     }
 
     @Override
     public OnProgressListener getOnProgressListener()
     {
         return progressListener;
     }
 
     @Override
     public void setOnProgressListener(OnProgressListener listener)
     {
         this.progressListener = listener;
     }
 }
