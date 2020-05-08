 package jp.gr.java_conf.neko_daisuki.anaudioplayer;
 
 import java.io.File;
 import java.io.IOException;
 
 import android.app.Service;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.util.SparseArray;
 
 public class AudioService extends Service {
 
     public static class PlayArgument {
 
         public int offset;
     }
 
     public static class InitArgument {
 
         public String directory;
         public String[] files;
         public int position;
     }
 
     public static class PlayingArgument {
 
         public int position;
     }
 
     private interface Player {
 
         public void play(String path, int offset) throws IOException;
         public void pause();
         public int getCurrentPosition();
         public void release();
         public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener);
     }
 
     private static class TruePlayer implements Player {
 
         private MediaPlayer mMp = new MediaPlayer();
 
         public void play(String path, int offset) throws IOException {
             mMp.reset();
             mMp.setDataSource(path);
             mMp.prepare();
             mMp.seekTo(offset);
             mMp.start();
         }
 
         public void pause() {
             mMp.pause();
         }
 
         public int getCurrentPosition() {
             return mMp.getCurrentPosition();
         }
 
         public void release() {
            mMp.reset();
             mMp.release();
         }
 
         public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
             mMp.setOnCompletionListener(listener);
         }
     }
 
     private static class FakePlayer implements Player {
 
         private int mPosition;
 
         public FakePlayer(int position) {
             mPosition = position;
         }
 
         public void play(String path, int offset) throws IOException {
         }
 
         public void pause() {
         }
 
         public int getCurrentPosition() {
             return mPosition;
         }
 
         public void release() {
         }
 
         public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
         }
     }
 
     private class CompletionListener implements MediaPlayer.OnCompletionListener {
 
         @Override
         public void onCompletion(MediaPlayer _) {
             mCompletionProc.run();
         }
     }
 
     private static class IncomingHandler extends Handler {
 
         private abstract static class MessageHandler {
 
             protected AudioService mService;
 
             public MessageHandler(AudioService service) {
                 mService = service;
             }
 
             public abstract void handle(Message msg);
 
             protected void reply(Message msg, Message res) {
                 try {
                     msg.replyTo.send(res);
                 }
                 catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
 
             protected void sendPlaying(Message msg) {
                 PlayingArgument a = new PlayingArgument();
                 a.position = mService.mPosition;
                 reply(msg, Message.obtain(null, MSG_PLAYING, a));
             }
         }
 
         private static class PauseHandler extends MessageHandler {
 
             public PauseHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 mService.mPlayer.pause();
             }
         }
 
         private static class WhatTimeCompletionHandler extends MessageHandler {
 
             public WhatTimeCompletionHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 Message reply = Message.obtain(null, MSG_COMPLETION);
                 try {
                     msg.replyTo.send(reply);
                 }
                 catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         private static class WhatTimeHandler extends MessageHandler {
 
             public WhatTimeHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 int what = MSG_WHAT_TIME;
                 int pos = mService.mPlayer.getCurrentPosition();
                 Message reply = Message.obtain(null, what, pos, 0, msg.obj);
                 reply(msg,  reply);
             }
         }
 
         private static class PlayHandler extends MessageHandler {
 
             public PlayHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 PlayArgument a = (PlayArgument)msg.obj;
                 mService.play(a.offset);
             }
         }
 
         private static class WhatTimePlayingHandler extends MessageHandler {
 
             public WhatTimePlayingHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 sendPlaying(msg);
                 mService.mHandler.sendWhatTime();
             }
         }
 
         private static class WhatFileHandler extends MessageHandler {
 
             public WhatFileHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 sendPlaying(msg);
             }
         }
 
         private static class WhatTimeNotPlayingHandler extends MessageHandler {
 
             public WhatTimeNotPlayingHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 reply(msg, Message.obtain(null, MSG_NOT_PLAYING));
             }
         }
 
         private static class InitHandler extends MessageHandler {
 
             public InitHandler(AudioService service) {
                 super(service);
             }
 
             public void handle(Message msg) {
                 InitArgument a = (InitArgument)msg.obj;
                 mService.mDirectory = a.directory;
                 mService.mFiles = a.files;
                 mService.mPosition = a.position;
             }
         }
 
         private AudioService mService;
         private SparseArray<MessageHandler> mHandlers;
         private MessageHandler mWhatTimeHandler;
         private MessageHandler mWhatTimePlayingHandler;
 
         public IncomingHandler(AudioService service) {
             initializeHandlers(service);
         }
 
         @Override
         public void handleMessage(Message msg) {
             mHandlers.get(msg.what).handle(msg);
         }
 
         public void complete() {
             MessageHandler h = new WhatTimeCompletionHandler(mService);
             mHandlers.put(MSG_WHAT_TIME, h);
         }
 
         public void sendPlaying() {
             mHandlers.put(MSG_WHAT_TIME, mWhatTimePlayingHandler);
         }
 
         public void sendWhatTime() {
             mHandlers.put(MSG_WHAT_TIME, mWhatTimeHandler);
         }
 
         private void initializeHandlers(AudioService service) {
             mService = service;
             mHandlers = new SparseArray<MessageHandler>();
             mHandlers.put(MSG_PLAY,  new PlayHandler(service));
             mHandlers.put(MSG_INIT, new InitHandler(service));
             mHandlers.put(MSG_PAUSE, new PauseHandler(service));
             mHandlers.put(MSG_WHAT_FILE, new WhatFileHandler(service));
             mHandlers.put(MSG_WHAT_TIME,
                           new WhatTimeNotPlayingHandler(service));
             mWhatTimeHandler = new WhatTimeHandler(service);
             mWhatTimePlayingHandler = new WhatTimePlayingHandler(service);
         }
     }
 
     private abstract class CompletionProcedure {
 
         public abstract void run();
     }
 
     private class StopProcedure extends CompletionProcedure {
 
         @Override
         public void run() {
             mHandler.complete();
         }
     }
 
     private class PlayNextProcedure extends CompletionProcedure {
 
         @Override
         public void run() {
             mPosition += 1;
             play(0);
         }
     }
 
     /*
      * Protocol for the service
      * ========================
      *
      * +-------------+---------------+-----------------------------------------+
      * |Request      |Response       |Description                              |
      * +=============+===============+=========================================+
      * |MSG_INIT     |(nothing)      |Initializes the service with a file list.|
      * |             |               |The service set current audio as a first |
      * |             |               |one in the list.                         |
      * +-------------+---------------+-----------------------------------------+
      * |MSG_PLAY     |(nothing)      |Plays the current audio from given       |
      * |             |               |offset.                                  |
      * +-------------+---------------+-----------------------------------------+
      * |MSG_PAUSE    |(nothing)      |                                         |
      * +-------------+---------------+-----------------------------------------+
      * |MSG_WHAT_TIME|MSG_WHAT_TIME  |Tells current offset.                    |
      * +             +---------------+-----------------------------------------+
      * |             |MSG_PLAYING    |Tells new file started.                  |
      * +             +---------------+-----------------------------------------+
      * |             |MSG_COMPLETION |Tells that the list ended.               |
      * +             +---------------+-----------------------------------------+
      * |             |MSG_NOT_PLAYING|Tells that no music is on air.           |
      * +-------------+---------------+-----------------------------------------+
      * |MSG_WHAT_FILE|MSG_PLAYING    |Tells what file the service playing.     |
      * +-------------+---------------+-----------------------------------------+
      *
      * About MSG_NOT_PLAYING
      * ---------------------
      *
      *  Sometimes Android kills the process which is playing music. Android re-
      *  creates the service, but the service gets initialized (The service is
      *  playing nothing). So, when a user restart the application, because the
      *  application is resumed to be playing the killed music, then it sends
      *  MSG_WHAT_TIME. The service must tell that no music is playing to stop
      *  the timer, etc.
      */
     public static final int MSG_PLAY = 0x00;
     public static final int MSG_INIT = 0x01;
     public static final int MSG_PLAYING = 0x02;
     public static final int MSG_PAUSE = 0x04;
     public static final int MSG_WHAT_TIME = 0x08;
     public static final int MSG_WHAT_FILE = 0x10;
     public static final int MSG_COMPLETION = 0x20;
     public static final int MSG_NOT_PLAYING = 0x40;
 
     private static final String LOG_TAG = MainActivity.LOG_TAG;
 
     private String mDirectory;
     private String[] mFiles;
     private int mPosition;
 
     private IncomingHandler mHandler;
     private Messenger mMessenger;
     private Player mPlayer;
     private CompletionProcedure mCompletionProc;
     private CompletionProcedure mStopProc;
     private CompletionProcedure mPlayNextProc;
 
     @Override
     public IBinder onBind(Intent intent) {
         Log.i(LOG_TAG, "One client was bound with AudioService.");
         return mMessenger.getBinder();
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         Log.i(LOG_TAG, "The client was unbound of AudioService.");
         return super.onUnbind(intent);
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         mHandler = new IncomingHandler(this);
         mMessenger = new Messenger(mHandler);
         mPlayer = new TruePlayer();
         mPlayer.setOnCompletionListener(new CompletionListener());
         mStopProc = new StopProcedure();
         mPlayNextProc = new PlayNextProcedure();
 
         Log.i(LOG_TAG, "AudioService was created.");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         Player player = mPlayer;
         player.pause();
         /*
          * MSG_WHAT_TIME message comes even after onDestroy(). So I placed
          * FakePlayer to handle MSG_WHAT_TIME.
          */
         mPlayer = new FakePlayer(player.getCurrentPosition());
         player.release();
 
         Log.i(LOG_TAG, "AudioService was destroyed.");
     }
 
     private void updateCompletionProcedure() {
         boolean isLast = mPosition == mFiles.length - 1;
         mCompletionProc = isLast ? mStopProc : mPlayNextProc;
     }
 
     private void play(int offset) {
         String file = mFiles[mPosition];
         String path = mDirectory + File.separator + file;
         try {
             mPlayer.play(path, offset);
         }
         catch (IOException e) {
             e.printStackTrace();
             // TODO: The handler must return an error to a client.
             return;
         }
         updateCompletionProcedure();
         mHandler.sendPlaying();
 
         Log.i(LOG_TAG, String.format("Play: %s from %d", path, offset));
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
