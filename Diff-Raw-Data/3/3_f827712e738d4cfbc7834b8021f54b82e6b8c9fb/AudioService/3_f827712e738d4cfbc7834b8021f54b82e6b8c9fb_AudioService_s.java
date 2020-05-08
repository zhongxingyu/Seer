 package jp.gr.java_conf.neko_daisuki.anaudioplayer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Service;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.media.MediaPlayer.OnSeekCompleteListener;
 import android.media.MediaPlayer;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 import android.util.SparseArray;
 
 public class AudioService extends Service {
 
     public static class Utils {
 
         private static SparseArray<String> mMessages;
 
         public static String getMessageString(Message msg) {
             Object o = msg.obj;
             String args = o != null ? o.toString() : "(null)";
             return String.format(LOCALE, "%s: %s", mMessages.get(msg.what),
                                  args);
         }
 
         static {
             mMessages = new SparseArray<String>();
             mMessages.put(MSG_WHAT_LIST, "MSG_WHAT_LIST");
             mMessages.put(MSG_LIST, "MSG_LIST");
             mMessages.put(MSG_WHAT_STATUS, "MSG_WHAT_STATUS");
             mMessages.put(MSG_PLAYING, "MSG_PLAYING");
             mMessages.put(MSG_PAUSED, "MSG_PAUSED");
             mMessages.put(MSG_INIT, "MSG_INIT");
             mMessages.put(MSG_PLAY, "MSG_PLAY");
             mMessages.put(MSG_PAUSE, "MSG_PAUSE");
         }
     }
 
     public static class Argument {
 
         protected String join(String[] sa) {
             return sa != null ? joinNonNullStrings(sa) : "(null)";
         }
 
         protected StringBuffer quote(String s) {
             StringBuffer buffer = new StringBuffer("\"");
             buffer.append(s);
             buffer.append("\"");
             return buffer;
         }
 
         private String joinNonNullStrings(String[] sa) {
             StringBuilder buffer = new StringBuilder("[");
             int len = sa.length;
             buffer.append(quote(0 < len ? sa[0] : ""));
             for (int i = 1; i < len; i++) {
                 buffer.append(", ");
                 buffer.append(quote(sa[i]));
             }
             buffer.append("]");
             return buffer.toString();
         }
     }
 
     public static class ListArgument extends Argument {
 
         public String directory;
         public String[] files;
 
         public String toString() {
             String fmt = "directory=%s, files=%s";
             return String.format(LOCALE, fmt, quote(directory), join(files));
         }
     }
 
     public static class PlayingArgument extends Argument {
 
         public int filePosition;
         public long timeAtStart;
         public int offsetAtStart;
 
         public String toString() {
             String fmt = joinStrings(new String[] {
                     "filePosition=%d",
                     "timeAtStart=%d (%s)",
                     "offsetAtStart=%d"
             });
             return String.format(LOCALE, fmt, filePosition, timeAtStart,
                                  DATE_FORMAT.format(new Date(timeAtStart)),
                                  offsetAtStart);
         }
 
         private String joinStrings(String[] sa) {
             StringBuilder buffer = new StringBuilder(sa[0]);
             for (int i = 1; i < sa.length; i++) {
                 buffer.append(", ");
                 buffer.append(sa[i]);
             }
             return buffer.toString();
         }
     }
 
     public static class PausedArgument extends Argument {
 
         public int currentOffset;
 
         public String toString() {
             return String.format(LOCALE, "currentOffset=%d", currentOffset);
         }
     }
 
     public static class PlayArgument extends Argument {
 
         public int filePosition;
         public int offset;
 
         public String toString() {
             String fmt = "filePosition=%d, offset=%d";
             return String.format(LOCALE, fmt, filePosition, offset);
         }
     }
 
     public static class InitArgument extends Argument {
 
         public String directory;
         public String[] files;
 
         public String toString() {
             String fmt = "directory=%s, files=%s";
             return String.format(LOCALE, fmt, quote(directory), join(files));
         }
     }
 
     private interface Player {
 
         public void play(String path, int offset) throws IOException;
         public void pause();
         public int getCurrentPosition();
         public void release();
         public void setOnCompletionListener(OnCompletionListener listener);
         public void seekTo(int offset);
         public boolean isPlaying();
     }
 
     private class TruePlayer implements Player {
 
         private class SeekCompleteListener implements OnSeekCompleteListener {
 
             /**
              * Hmm, MediaPlayer.start() calls back OnSeekCompleteListener once
              * more (I do not know why). This causes sending MSG_PLAYING twice.
              */
             private abstract class Proc {
 
                 public abstract void run(MediaPlayer mp);
             }
 
             private class TrueProc extends Proc {
 
                 @Override
                 public void run(MediaPlayer mp) {
                     mTimeAtStart = new Date().getTime();
                     mOffsetAtStart = mOffset;
                     mp.start();
                     reply(mReplyTo, obtainPlayingMessage());
                 }
             }
 
             private class FakeProc extends Proc {
 
                 @Override
                 public void run(MediaPlayer mp) {
                 }
             }
 
             private int mOffset;
             private Proc mProc = new TrueProc();
 
             public SeekCompleteListener(int offset) {
                 mOffset = offset;
             }
 
             public void onSeekComplete(MediaPlayer mp) {
                 mProc.run(mp);
                 mProc = new FakeProc();
             }
         }
 
         private class PreparedListener implements OnPreparedListener {
 
             private int mOffset;
 
             public PreparedListener(int offset) {
                 mOffset = offset;
             }
 
             public void onPrepared(MediaPlayer mp) {
                 mp.seekTo(mOffset);
             }
         }
 
         private MediaPlayer mMp = new MediaPlayer();
 
         public TruePlayer() {
             mMp.setAudioStreamType(AudioManager.STREAM_MUSIC);
         }
 
         public void play(String path, int offset) throws IOException {
             mMp.setOnPreparedListener(new PreparedListener(offset));
             enableOnSeekCompleteListener(offset);
             mMp.reset();
             mMp.setDataSource(path);
             mMp.prepareAsync();
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
 
         public void setOnCompletionListener(OnCompletionListener listener) {
             mMp.setOnCompletionListener(listener);
         }
 
         @Override
         public void seekTo(int offset) {
             enableOnSeekCompleteListener(offset);
             mMp.seekTo(offset);
         }
 
         @Override
         public boolean isPlaying() {
             return mMp.isPlaying();
         }
 
         private void enableOnSeekCompleteListener(int offset) {
             mMp.setOnSeekCompleteListener(new SeekCompleteListener(offset));
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
 
         public void setOnCompletionListener(OnCompletionListener listener) {
         }
 
         @Override
         public void seekTo(int offset) {
         }
 
         @Override
         public boolean isPlaying() {
             return false;
         }
     }
 
     private class CompletionListener implements OnCompletionListener {
 
         @Override
         public void onCompletion(MediaPlayer _) {
             mCompletionProc.run();
         }
     }
 
     private static class IncomingHandler extends Handler {
 
         private interface MessageHandler {
 
             public void handle(Message msg);
         }
 
         private class WhatListHandler implements MessageHandler {
 
             @Override
             public void handle(Message msg) {
                 ListArgument a = new ListArgument();
                 a.directory = mService.mDirectory;
                 a.files = mService.mFiles;
                 reply(msg, Message.obtain(null, MSG_LIST, a));
             }
         }
 
         private class WhatStatusHandler implements MessageHandler {
 
             @Override
             public void handle(Message msg) {
                 Message res = mService.mPlayer.isPlaying()
                         ? mService.obtainPlayingMessage()
                         : mService.obtainPausedMessage();
                 reply(msg, res);
             }
         }
 
         private class InitHandler implements MessageHandler {
 
             @Override
             public void handle(Message msg) {
                 InitArgument a = (InitArgument)msg.obj;
                 mService.mDirectory = a.directory;
                 mService.mFiles = a.files;
                 mService.writeList();
             }
         }
 
         private class PlayHandler implements MessageHandler {
 
             @Override
             public void handle(Message msg) {
                mService.mReplyTo = msg.replyTo;
                 PlayArgument a = (PlayArgument)msg.obj;
                 mService.updateFilePosition(a.filePosition);
                 mService.play(a.offset);
             }
         }
 
         private class PauseHandler implements MessageHandler {
 
             @Override
             public void handle(Message msg) {
                 mService.pause();
                 reply(msg, mService.obtainPausedMessage());
             }
         }
 
         private AudioService mService;
         private SparseArray<MessageHandler> mHandlers;
 
         public IncomingHandler(AudioService service) {
             mService = service;
 
             mHandlers = new SparseArray<MessageHandler>();
             mHandlers.put(MSG_WHAT_LIST,  new WhatListHandler());
             mHandlers.put(MSG_WHAT_STATUS, new WhatStatusHandler());
             mHandlers.put(MSG_INIT, new InitHandler());
             mHandlers.put(MSG_PLAY, new PlayHandler());
             mHandlers.put(MSG_PAUSE, new PauseHandler());
         }
 
         @Override
         public void handleMessage(Message msg) {
             String s = Utils.getMessageString(msg);
             Log.i(LOG_TAG, String.format(LOCALE, "recv: %s", s));
 
             mHandlers.get(msg.what).handle(msg);
         }
 
         private void reply(Message msg, Message res) {
             mService.reply(msg.replyTo, res);
         }
     }
 
     private abstract class CompletionProcedure {
 
         public abstract void run();
     }
 
     private class StopProcedure extends CompletionProcedure {
 
         @Override
         public void run() {
             reply(mReplyTo, obtainPausedMessage());
             postProcessOfPause();
         }
     }
 
     private class PlayNextProcedure extends CompletionProcedure {
 
         @Override
         public void run() {
             updateFilePosition(mFilePosition + 1);
             play(0);
         }
     }
 
     public static final int MSG_WHAT_LIST = 0;
     public static final int MSG_LIST = 1;
     public static final int MSG_WHAT_STATUS = 2;
     public static final int MSG_PLAYING = 3;
     public static final int MSG_PAUSED = 4;
     public static final int MSG_INIT = 5;
     public static final int MSG_PLAY = 6;
     public static final int MSG_PAUSE = 7;
 
     private static final String PATH_DIRECTORY = "directory";
     private static final String PATH_FILES = "files";
     private static final String PATH_FILE_POSITION = "file_position";
 
     private static final String LOG_TAG = "service";
     private static final Locale LOCALE = Locale.ROOT;
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
             "yyyy-MM-dd HH:mm:ss.SSS",
             LOCALE);
 
     private String mDirectory;
     private String[] mFiles;
     private int mFilePosition;
     /**
      * Path of a playing file. This is needed because MSG_INIT overwrites
      * mDirectory and mFiles, so this service misses which file is playing.
      */
     private String mPlayingPath;
 
     private long mTimeAtStart;
     private int mOffsetAtStart;
 
     private IncomingHandler mHandler;
     private Messenger mMessenger;
     private Player mPlayer;
     private Player mTruePlayer;
     private CompletionProcedure mCompletionProc;
     private CompletionProcedure mStopProc;
     private CompletionProcedure mPlayNextProc;
     private Messenger mReplyTo;
 
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
         readList();
 
         mHandler = new IncomingHandler(this);
         mMessenger = new Messenger(mHandler);
         mTruePlayer = new TruePlayer();
         mTruePlayer.setOnCompletionListener(new CompletionListener());
         mPlayer = new FakePlayer(0);
         mStopProc = new StopProcedure();
         mPlayNextProc = new PlayNextProcedure();
 
         Log.i(LOG_TAG, "AudioService was created.");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
 
         Player player = mPlayer;
         pause();    // NOTICE: This causes side effect to change mPlayer.
         player.release();
 
         Log.i(LOG_TAG, "AudioService was destroyed.");
     }
 
     private void updateCompletionProcedure() {
         boolean isLast = mFilePosition == mFiles.length - 1;
         mCompletionProc = isLast ? mStopProc : mPlayNextProc;
     }
 
     private String joinPath(String s, String t) {
         return String.format(LOCALE, "%s%s%s", s, File.separator, t);
     }
 
     private void play(int offset) {
         mPlayer = mTruePlayer;
 
         String name = mFiles[mFilePosition];
         String path = joinPath(mDirectory, name);
         if (path.equals(mPlayingPath)) {
             mPlayer.seekTo(offset);
             return;
         }
 
         try {
             mPlayer.play(path, offset);
         }
         catch (IOException e) {
             handleError(e);
             return;
         }
         mPlayingPath = path;
         updateCompletionProcedure();
 
         Log.i(LOG_TAG, String.format(LOCALE, "Play: %s from %d", path, offset));
     }
 
     private void pause() {
         mPlayer.pause();
         postProcessOfPause();
     }
 
     private void postProcessOfPause() {
         mPlayer = new FakePlayer(mPlayer.getCurrentPosition());
     }
 
     private Message obtainPlayingMessage() {
         PlayingArgument a = new PlayingArgument();
         a.timeAtStart = mTimeAtStart;
         a.offsetAtStart = mOffsetAtStart;
         a.filePosition = mFilePosition;
         return Message.obtain(null, MSG_PLAYING, a);
     }
 
     private Message obtainPausedMessage() {
         PausedArgument a = new PausedArgument();
         a.currentOffset = mPlayer.getCurrentPosition();
         return Message.obtain(null, MSG_PAUSED, a);
     }
 
     private void reply(Messenger replyTo, Message res) {
         String s = Utils.getMessageString(res);
         Log.i(LOG_TAG, String.format(LOCALE, "send: %s", s));
 
         try {
             replyTo.send(res);
         } catch (RemoteException e) {
             handleError(e);
         }
     }
 
     private void handleError(Exception e) {
         // TODO: Send MSG_ERROR.
         e.printStackTrace();
     }
 
     private String[] readArray(String path) {
         FileInputStream in;
         try {
             in = openFileInput(path);
         }
         catch (FileNotFoundException e) {
             Log.i(LOG_TAG, String.format(LOCALE, "%s not found.", path));
             return new String[0];
         }
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         try {
             try {
                 List<String> l = new LinkedList<String>();
                 String s;
                 while ((s = reader.readLine()) != null) {
                     l.add(s);
                 }
                 return l.toArray(new String[0]);
             }
             finally {
                 reader.close();
             }
         }
         catch (IOException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     private String readDirectory() {
         String[] sa = readArray(PATH_DIRECTORY);
         return 0 < sa.length ? sa[0] : null;
     }
 
     private String[] readFiles() {
         return readArray(PATH_FILES);
     }
 
     private int readFilePosition() {
         String[] sa = readArray(PATH_FILE_POSITION);
         return 0 < sa.length ? Integer.valueOf(sa[0]) : 0;
 
     }
 
     private void readList() {
         mDirectory = readDirectory();
         mFiles = readFiles();
         mFilePosition = readFilePosition();
     }
 
     private void writeArray(String path, String[] sa) {
         FileOutputStream out;
         try {
             out = openFileOutput(path, 0);
         } catch (FileNotFoundException e) {
             Log.i(LOG_TAG, String.format(LOCALE, "%s not found.", path));
             return;
         }
         PrintWriter writer = new PrintWriter(out);
         try {
             for (String s: sa) {
                 writer.println(s);
             }
         }
         finally {
             writer.close();
         }
     }
 
     private void writeFilePosition() {
         String filePosition = Integer.toString(mFilePosition);
         writeArray(PATH_FILE_POSITION, new String[] { filePosition });
     }
 
     private void updateFilePosition(int newValue) {
         mFilePosition = newValue;
         writeFilePosition();
     }
 
     private void writeList() {
         writeArray(PATH_DIRECTORY, new String[] { mDirectory });
         writeArray(PATH_FILES, mFiles);
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
