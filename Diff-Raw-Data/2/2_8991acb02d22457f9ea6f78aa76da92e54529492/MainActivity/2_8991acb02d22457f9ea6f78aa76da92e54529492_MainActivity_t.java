 package jp.gr.java_conf.neko_daisuki.anaudioplayer;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.Interpolator;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import jp.gr.java_conf.neko_daisuki.android.widget.RotatingUzumakiSlider;
 import jp.gr.java_conf.neko_daisuki.android.widget.UzumakiHead;
 import jp.gr.java_conf.neko_daisuki.android.widget.UzumakiSlider;
 
 public class MainActivity extends Activity {
 
     private enum PlayerState {
         PLAYING,
         PAUSED
     };
 
     private static class PreferencesUtil {
 
         private static final String KEY_PLAYER_STATE = "PlayerState";
 
         public static PlayerState getPlayerState(SharedPreferences prefs) {
             String value = prefs.getString(KEY_PLAYER_STATE, "");
             return value.equals(PlayerState.PLAYING.name())
                 ? PlayerState.PLAYING
                 : PlayerState.PAUSED;
         }
 
         public static void putPlayerState(Editor editor, PlayerState state) {
             editor.putString(KEY_PLAYER_STATE, state.name());
         }
 
         public static String[] getStringArray(SharedPreferences prefs, String key) {
             String s = prefs.getString(key, null);
             return s != null ? s.split("\n") : new String[0];
         }
 
         public static void putStringArray(Editor editor, String key, String[] values) {
             int len = values.length;
             editor.putString(key, 0 < len ? buildArray(values) : null);
         }
 
         private static String buildArray(String[] sa) {
             StringBuilder buffer = new StringBuilder(sa[0]);
             for (int i = 1; i < sa.length; i++) {
                 buffer.append("\n");
                 buffer.append(sa[i]);
             }
             return buffer.toString();
         }
     }
 
     private static class FileSystem {
 
         public String directory;
 
         /**
          * An array of files' name. This must be non-null.
          */
         public String[] files = new String[0];
 
         public void copyFrom(FileSystem src) {
             this.directory = src.directory;
             this.files = src.files;
         }
     }
 
     private static class ActivityHolder {
 
         protected MainActivity activity;
 
         public ActivityHolder(MainActivity activity) {
             this.activity = activity;
         }
     }
 
     private abstract static class Adapter extends ArrayAdapter<String> {
 
         protected LayoutInflater inflater;
         protected MainActivity activity;
 
         public Adapter(MainActivity activity, String[] objects) {
             super(activity, 0, objects);
             this.initialize(activity);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             return convertView == null ? this.getView(position, this.makeConvertView(parent), parent) : this.makeView(position, convertView);
         }
 
         protected abstract View makeConvertView(ViewGroup parent);
         protected abstract View makeView(int position, View convertView);
 
         private void initialize(MainActivity activity) {
             this.activity = activity;
             String service = Context.LAYOUT_INFLATER_SERVICE;
             this.inflater = (LayoutInflater)activity.getSystemService(service);
         }
     }
 
     private static class FileAdapter extends Adapter {
 
         private static class Row {
 
             public ImageView playingIcon;
             public TextView name;
         }
 
         public FileAdapter(MainActivity activity, String[] objects) {
             super(activity, objects);
         }
 
         @Override
         protected View makeView(int position, View convertView) {
             String file = this.activity.getPlayingFile();
             boolean isPlaying = this.isPlayingDirectoryShown() && this.activity.shownFiles.files[position].equals(file);
             int src = isPlaying ? R.drawable.ic_playing : R.drawable.ic_blank;
             Row row = (Row)convertView.getTag();
             row.playingIcon.setImageResource(src);
             row.name.setText(this.getItem(position));
 
             return convertView;
         }
 
         @Override
         protected View makeConvertView(ViewGroup parent) {
             View view = this.inflater.inflate(R.layout.file_row, parent, false);
             Row row = new Row();
             row.playingIcon = (ImageView)view.findViewById(R.id.playing_icon);
             row.name = (TextView)view.findViewById(R.id.name);
             view.setTag(row);
             return view;
         }
 
         private boolean isPlayingDirectoryShown() {
             MainActivity activity = this.activity;
             String shown = activity.shownFiles.directory;
             String playing = activity.playingFiles.directory;
             return (shown != null) && shown.equals(playing);
         }
     }
 
     private static class DirectoryAdapter extends Adapter {
 
         private static class Row {
 
             public ImageView playingIcon;
             public TextView path;
         }
 
         public DirectoryAdapter(MainActivity activity, String[] objects) {
             super(activity, objects);
         }
 
         @Override
         protected View makeView(int position, View convertView) {
             String path = this.activity.directories[position];
             this.setPlayingIcon(path, convertView);
 
             return convertView;
         }
 
         @Override
         protected View makeConvertView(ViewGroup parent) {
             View view = this.inflater.inflate(R.layout.dir_row, parent, false);
             Row row = new Row();
             row.playingIcon = (ImageView)view.findViewById(R.id.playing_icon);
             row.path = (TextView)view.findViewById(R.id.path);
             view.setTag(row);
             return view;
         }
 
         private void setPlayingIcon(String path, View view) {
             String directory = this.activity.getPlayingDirectory();
             boolean isPlaying = path.equals(directory);
             int src = isPlaying ? R.drawable.ic_playing : R.drawable.ic_blank;
             Row row = (Row)view.getTag();
             row.playingIcon.setImageResource(src);
             row.path.setText(path);
         }
     }
 
     private abstract static class ProcedureOnConnected extends ActivityHolder implements Runnable {
 
         public ProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public abstract void run();
     }
 
     private static class PlayProcedureOnConnected extends ProcedureOnConnected {
 
         public PlayProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.sendInit();
             this.activity.sendPlay();
             this.activity.onConnectedWithService();
         }
     }
 
     private static class ResumeProcedureOnConnected extends ProcedureOnConnected {
 
         public ResumeProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.sendWhatFile();
             this.activity.onConnectedWithService();
         }
     }
 
     private interface ServiceUnbinder {
 
         public void unbind();
     }
 
     private class TrueServiceUnbinder extends ActivityHolder implements ServiceUnbinder {
 
         public TrueServiceUnbinder(MainActivity activity) {
             super(activity);
         }
 
         public void unbind() {
             this.activity.unbindService(this.activity.connection);
         }
     }
 
     private class FakeServiceUnbinder implements ServiceUnbinder {
 
         public void unbind() {
         }
     }
 
     private interface ServiceStarter {
 
         public void start();
     }
 
     private class TrueServiceStarter extends ActivityHolder implements ServiceStarter {
 
         public TrueServiceStarter(MainActivity activity) {
             super(activity);
         }
 
         public void start() {
             Intent intent = new Intent(this.activity, AudioService.class);
             this.activity.startService(intent);
         }
     }
 
     private class FakeServiceStarter implements ServiceStarter {
 
         public void start() {
         }
     }
 
     private interface ServiceStopper {
 
         public void stop();
     }
 
     private class TrueServiceStopper extends ActivityHolder implements ServiceStopper {
 
         public TrueServiceStopper(MainActivity activity) {
             super(activity);
         }
 
         public void stop() {
             this.activity.unbindAudioService();
             Intent intent = new Intent(this.activity, AudioService.class);
             this.activity.stopService(intent);
         }
     }
 
     private class FakeServiceStopper implements ServiceStopper {
 
         public void stop() {
         }
     }
 
     private interface MessengerWrapper {
 
         public void send(Message msg) throws RemoteException;
     }
 
     private static class TrueMessenger implements MessengerWrapper {
 
         private Messenger messenger;
 
         public TrueMessenger(Messenger messenger) {
             this.messenger = messenger;
         }
 
         public void send(Message msg) throws RemoteException {
             this.messenger.send(msg);
         }
     }
 
     private static class FakeMessenger implements MessengerWrapper {
 
         public void send(Message _) throws RemoteException {
         }
     }
 
     private class Connection extends ActivityHolder implements ServiceConnection {
 
         private Runnable procedureOnConnected;
 
         public Connection(MainActivity activity, Runnable procedureOnConnected) {
             super(activity);
             this.procedureOnConnected = procedureOnConnected;
         }
 
         public void onServiceConnected(ComponentName className, IBinder service) {
             Messenger messenger = new Messenger(service);
             this.activity.outgoingMessenger = new TrueMessenger(messenger);
 
             this.procedureOnConnected.run();
 
            Log.i(LOG_TAG, "MainActivity connected to AudioService.");
         }
 
         public void onServiceDisconnected(ComponentName className) {
             Log.i(LOG_TAG, "MainActivity disconnected from AudioService.");
         }
     }
 
     private class OnStartRotatingListener extends ActivityHolder implements RotatingUzumakiSlider.OnStartRotatingListener {
 
         public OnStartRotatingListener(MainActivity activity) {
             super(activity);
         }
 
         public void onStartRotating(RotatingUzumakiSlider slider) {
             this.activity.onStartSliding();
         }
     }
 
     private class OnStopRotatingListener extends ActivityHolder implements RotatingUzumakiSlider.OnStopRotatingListener {
 
         public OnStopRotatingListener(MainActivity activity) {
             super(activity);
         }
 
         public void onStopRotating(RotatingUzumakiSlider slider) {
             this.activity.procAfterSeeking.run();
         }
     }
 
     private class PlayAfterSeeking extends ActivityHolder implements Runnable {
 
         public PlayAfterSeeking(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.startTimer();
             this.activity.sendPlay();
         }
     }
 
     private class StayAfterSeeking implements Runnable {
 
         public void run() {
         }
     }
 
     private class SliderLogger implements UzumakiSlider.Logger {
 
         private MainActivity activity;
 
         public SliderLogger(MainActivity activity) {
             this.activity = activity;
         }
 
         public void log(String msg) {
             this.activity.log(msg);
         }
     }
 
     private class OnStartHeadMovingListener implements UzumakiSlider.OnStartHeadMovingListener {
 
         private MainActivity activity;
 
         public OnStartHeadMovingListener(MainActivity activity) {
             this.activity = activity;
         }
 
         public void onStartHeadMoving(UzumakiSlider slider, UzumakiHead head) {
             this.activity.onStartSliding();
         }
     }
 
     private class OnStopHeadMovingListener extends ActivityHolder implements UzumakiSlider.OnStopHeadMovingListener {
 
         public OnStopHeadMovingListener(MainActivity activity) {
             super(activity);
         }
 
         public void onStopHeadMoving(UzumakiSlider slider, UzumakiHead head) {
             this.activity.procAfterSeeking.run();
         }
     }
 
     private abstract class MenuDispatcher {
 
         protected MainActivity activity;
 
         public MenuDispatcher(MainActivity activity) {
             this.activity = activity;
         }
 
         public boolean dispatch() {
             this.callback();
             return true;
         }
 
         protected abstract void callback();
     }
 
     private class AboutDispatcher extends MenuDispatcher {
 
         public AboutDispatcher(MainActivity activity) {
             super(activity);
         }
 
         protected void callback() {
             this.activity.showAbout();
         }
     }
 
     private static class IncomingHandler extends Handler {
 
         private abstract class MessageHandler extends ActivityHolder {
 
             public MessageHandler(MainActivity activity) {
                 super(activity);
             }
 
             public abstract void handle(Message msg);
         }
 
         private class CompletionHandler extends MessageHandler {
 
             public CompletionHandler(MainActivity activity) {
                 super(activity);
             }
 
             public void handle(Message msg) {
                 this.completeSlider();
                 this.activity.pause();
             }
 
             /**
              * Moves the slider head to the last.
              *
              * During AAP is on background, UI is not updated. UI is updated
              * when AAP comes back to foreground. If music is on air,
              * MSG_WHAT_TIME message updates the slider. Similaly,
              * MSG_COMPLETION must update UI.
              */
             private void completeSlider() {
                 RotatingUzumakiSlider slider = this.activity.slider;
                 slider.setProgress(slider.getMax());
             }
         }
 
         private class WhatTimeHandler extends MessageHandler {
 
             public WhatTimeHandler(MainActivity activity) {
                 super(activity);
             }
 
             public void handle(Message msg) {
                 this.activity.updateCurrentTime(msg.arg1);
             }
         }
 
         private class NotPlayingHandler extends MessageHandler {
 
             public NotPlayingHandler(MainActivity activity) {
                 super(activity);
             }
 
             public void handle(Message msg) {
                 this.activity.pause();
             }
         }
 
         private class NopHandler extends MessageHandler {
 
             public NopHandler() {
                 super(null);
             }
 
             public void handle(Message _) {
             }
         }
 
         private class PlayingHandler extends MessageHandler {
 
             public PlayingHandler(MainActivity activity) {
                 super(activity);
             }
 
             public void handle(Message msg) {
                 AudioService.PlayingArgument a = (AudioService.PlayingArgument)msg.obj;
                 this.activity.playingFilePosition = a.position;
                 this.activity.showPlayingFile();
 
                 this.activity.incomingHandler.enableResponse();
             }
         }
 
         private SparseArray<MessageHandler> handlers;
         private MessageHandler whatTimeHandler;
         private MessageHandler completionHandler;
         private MessageHandler nopHandler;
 
         public IncomingHandler(MainActivity activity) {
             this.handlers = new SparseArray<MessageHandler>();
             this.handlers.put(
                     AudioService.MSG_PLAYING,
                     new PlayingHandler(activity));
             this.handlers.put(
                     AudioService.MSG_NOT_PLAYING,
                     new NotPlayingHandler(activity));
 
             this.whatTimeHandler = new WhatTimeHandler(activity);
             this.completionHandler = new CompletionHandler(activity);
             this.nopHandler = new NopHandler();
 
             this.ignoreResponseUntilPlaying();
         }
 
         @Override
         public void handleMessage(Message msg) {
             this.handlers.get(msg.what).handle(msg);
         }
 
         /**
          * Orders to ignore MSG_WHAT_TIME/MSG_COMPLETION messages until a next
          * MSG_PLAYING.
          *
          * When a user selects a new music in playing another one, some
          * MSG_WHAT_TIME messages may be included in the message queue. These
          * messages will change time before starting to play the new music. So,
          * MSG_WHAT_TIME must be ignored until a next MSG_PLAY response.
          *
          * Same case is on MSG_COMPLETION. If a music finished, AudioService
          * send back MSG_COMPLETION for each MSG_WHAT_TIME message in the queue.
          * These responses start playing a next music (finally, no music but
          * last one will be skipped). This is a reason why I must drop
          * MSG_COMPLETION responses once I accepted first one.
          *
          * NOTE: At first, I tried to use a new messenger and a handler, but
          * they did not work expectedly. I guess that all messengers must share
          * one singleton message queue.
          */
         public void ignoreResponseUntilPlaying() {
             this.setWhatTimeHandler(this.nopHandler);
             this.setCompletionHandler(this.nopHandler);
         }
 
         private void enableResponse() {
             this.setWhatTimeHandler(this.whatTimeHandler);
             this.setCompletionHandler(this.completionHandler);
         }
 
         private void setWhatTimeHandler(MessageHandler handler) {
             this.handlers.put(AudioService.MSG_WHAT_TIME, handler);
         }
 
         private void setCompletionHandler(MessageHandler handler) {
             this.handlers.put(AudioService.MSG_COMPLETION, handler);
         }
     }
 
     private static abstract class ContentTask extends AsyncTask<Void, Void, List<String>> {
 
         protected MainActivity activity;
         protected List<String> emptyList;
 
         public ContentTask(MainActivity activity) {
             this.activity = activity;
             this.emptyList = new ArrayList<String>();
         }
 
         protected List<String> queryExistingMp3() {
             return this.selectMp3(this.queryAudio());
         }
 
         protected List<String> makeList(String s) {
             List<String> l = new ArrayList<String>();
             l.add(s);
             return l;
         }
 
         private boolean isMp3(String path) {
             File file = new File(path);
             /*
              * The second expression is for rejecting non-mp3 files. This way is
              * not strict, because there might be a non-mp3 file with ".mp3"
              * extension.
              */
             return file.exists() && path.endsWith(".mp3");
         }
 
         private List<String> selectMp3(List<String> files) {
             List<String> l = new ArrayList<String>();
 
             for (String file: files) {
                 boolean pred = this.isMp3(file);
                 l.addAll(pred ? this.makeList(file) : this.emptyList);
             }
 
             return l;
         }
 
         private List<String> fetchRecord(Cursor c) {
             try {
                 List<String> l = new ArrayList<String>();
 
                 int index = c.getColumnIndex(MediaStore.MediaColumns.DATA);
                 while (c.moveToNext()) {
                     l.add(c.getString(index));
                 }
 
                 return l;
             }
             finally {
                 c.close();
             }
         }
 
         private List<String> queryAudio() {
             String trackColumn = MediaStore.Audio.AudioColumns.TRACK;
             String pathColumn = MediaStore.MediaColumns.DATA;
             String order = String.format("%s, %s", trackColumn, pathColumn);
             Cursor c = this.activity.getContentResolver().query(
                     MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                     new String[] { pathColumn },
                     null,   // selection
                     null,   // selection arguments
                     order); // order
             return c != null ? this.fetchRecord(c) : new ArrayList<String>();
         }
     }
 
     private static class FileListingTask extends ContentTask {
 
         private String path;
 
         public FileListingTask(MainActivity activity, String path) {
             super(activity);
             this.path = path;
         }
 
         @Override
         protected void onPostExecute(List<String> files) {
             this.activity.showFiles(files);
         }
 
         @Override
         protected List<String> doInBackground(Void... voids) {
             Log.i(LOG_TAG, "FileListingTask started.");
 
             List<String> files = this.selectFiles(this.queryExistingMp3());
 
             Log.i(LOG_TAG, "FileListingTask ended.");
             return files;
         }
 
         private void addFile(List<String> l, String path) {
             File file = new File(path);
             if (!file.getParent().equals(this.path)) {
                 return;
             }
             l.add(file.getName());
         }
 
         private List<String> selectFiles(List<String> files) {
             List<String> l = new ArrayList<String>();
 
             for (String file: files) {
                 this.addFile(l, file);
             }
 
             return l;
         }
     }
 
     private static class DirectoryListingTask extends ContentTask {
 
         public DirectoryListingTask(MainActivity activity) {
             super(activity);
         }
 
         @Override
         protected void onPostExecute(List<String> directories) {
             this.activity.showDirectories(directories.toArray(new String[0]));
         }
 
         @Override
         protected List<String> doInBackground(Void... voids) {
             Log.i(LOG_TAG, "DirectoryListingTask started.");
 
             List<String> audio = this.queryExistingMp3();
             List<String> directories = this.listDirectories(audio);
             Collections.sort(directories);
 
             Log.i(LOG_TAG, "DirectoryListingTask ended.");
             return directories;
         }
 
         private List<String> listDirectories(List<String> audio) {
             Set<String> set = new HashSet<String>();
             for (String path: audio) {
                 File file = new File(path);
                 set.add(file.getParent());
             }
 
             List<String> directories = new ArrayList<String>();
             for (String path: set) {
                 directories.add(path);
             }
 
             return directories;
         }
     }
 
     private static class TrueSliderListener extends ActivityHolder implements UzumakiSlider.OnSliderChangeListener {
 
         public TrueSliderListener(MainActivity activity) {
             super(activity);
         }
 
         public void onProgressChanged(UzumakiSlider _) {
             this.activity.showCurrentTime();
         }
     }
 
     private static class FakeSliderListener implements UzumakiSlider.OnSliderChangeListener {
 
         public void onProgressChanged(UzumakiSlider _) {
         }
     }
 
     public static final String LOG_TAG = "anaudioplayer";
 
     // Widgets
     private ViewFlipper flipper;
     private ListView dirList;
     private ImageButton nextButton0;
 
     private View prevButton1;
     private TextView dirLabel;
     private ListView fileList;
     private ImageButton nextButton1;
 
     private View prevButton2;
     private ImageButton playButton;
     private RotatingUzumakiSlider slider;
     private TextView title;
     private TextView currentTime;
     private TextView totalTime;
 
     // Objects supporting any widgets. They are stateless.
     private Animation leftInAnimation;
     private Animation leftOutAnimation;
     private Animation rightInAnimation;
     private Animation rightOutAnimation;
     private View.OnClickListener pauseListener;
     private View.OnClickListener playListener;
     private SparseArray<MenuDispatcher> menuDispatchers = new SparseArray<MenuDispatcher>();
 
     /**
      * Timer. This is stateful, but it is configured automatically.
      */
     private TimerInterface timer;
 
     // Stateful internal data
     private PlayerState playerState;
     private String[] directories;
     private FileSystem shownFiles = new FileSystem();
     private FileSystem playingFiles = new FileSystem();
     private int playingFilePosition;
 
     private Runnable procAfterSeeking;
 
     private ServiceStarter serviceStarter;
     private ServiceStopper serviceStopper;
     private ServiceUnbinder serviceUnbinder;
     private ServiceConnection connection;
     private MessengerWrapper outgoingMessenger;
     private Messenger incomingMessenger;
     private IncomingHandler incomingHandler;
 
     // Stateless internal data (reusable)
     private TimerInterface fakeTimer;
     private UzumakiSlider.OnSliderChangeListener trueSliderListener;
     private UzumakiSlider.OnSliderChangeListener fakeSliderListener;
     private MessengerWrapper fakeOutgoingMessenger;
 
     @Override
     public void onStart() {
         super.onStart();
 
         new DirectoryListingTask(this).execute();
     }
 
     @Override
     public Object onRetainNonConfigurationInstance() {
         return this.connection;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.main);
 
         this.playerState = PlayerState.PAUSED;
         this.findViews();
         this.initializeFlipButtonListener();
         this.initializeDirList();
         this.initializeFileList();
         this.initializeAnimation();
         this.initializePlayButton();
         this.initializeTimer();
         this.initializeSlider();
         this.initializeMenu();
 
         this.incomingHandler = new IncomingHandler(this);
         this.incomingMessenger = new Messenger(this.incomingHandler);
         this.fakeOutgoingMessenger = new FakeMessenger();
 
         Log.i(LOG_TAG, "MainActivity was created.");
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         MenuDispatcher dispatcher = this.menuDispatchers.get(item.getItemId());
         return dispatcher != null ? dispatcher.dispatch() : super.onOptionsItemSelected(item);
     }
 
     private void showAbout() {
         Intent i = new Intent(this, AboutActivity.class);
         this.startActivity(i);
     }
 
     private void initializeMenu() {
         this.menuDispatchers.put(R.id.about, new AboutDispatcher(this));
     }
 
     private void initializeSlider() {
         this.slider.addOnStartHeadMovingListener(new OnStartHeadMovingListener(this));
         this.slider.addOnStopHeadMovingListener(new OnStopHeadMovingListener(this));
         this.slider.addOnStartRotatingListener(new OnStartRotatingListener(this));
         this.slider.addOnStopRotatingListener(new OnStopRotatingListener(this));
         this.slider.setLogger(new SliderLogger(this));
 
         this.trueSliderListener = new TrueSliderListener(this);
         this.fakeSliderListener = new FakeSliderListener();
     }
 
     private void initializeTimer() {
         this.timer = this.fakeTimer = new FakeTimer();
     }
 
     private void findViews() {
         this.flipper = (ViewFlipper)this.findViewById(R.id.flipper);
 
         this.dirList = (ListView)this.findViewById(R.id.dir_list);
         this.nextButton0 = (ImageButton)this.findViewById(R.id.next0);
 
         this.prevButton1 = (View)this.findViewById(R.id.prev1);
         this.dirLabel = (TextView)this.findViewById(R.id.dir_label);
         this.fileList = (ListView)this.findViewById(R.id.file_list);
         this.nextButton1 = (ImageButton)this.findViewById(R.id.next1);
 
         this.prevButton2 = (View)this.findViewById(R.id.prev2);
         this.playButton = (ImageButton)this.findViewById(R.id.play);
         this.slider = (RotatingUzumakiSlider)this.findViewById(R.id.slider);
 
         this.title = (TextView)this.findViewById(R.id.title);
         this.currentTime = (TextView)this.findViewById(R.id.current_time);
         this.totalTime = (TextView)this.findViewById(R.id.total_time);
     }
 
     private void initializePlayButton() {
         this.pauseListener = new PauseButtonListener(this);
         this.playButton.setOnClickListener(this.pauseListener);
         this.playListener = new PlayButtonListener(this);
     }
 
     private class PauseButtonListener extends ActivityHolder implements View.OnClickListener {
 
         public PauseButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.pause();
         }
     }
 
     private class PlayButtonListener extends ActivityHolder implements View.OnClickListener {
 
         public PlayButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.play();
         }
     }
 
     private static final long ANIMATION_DURATION = 250;
     private static final int INTERPOLATOR = android.R.anim.linear_interpolator;
 
     private Animation loadAnimation(int id, Interpolator interp) {
         Animation anim = AnimationUtils.loadAnimation(this, id);
         anim.setDuration(ANIMATION_DURATION);
         anim.setInterpolator(interp);
         return anim;
     }
 
     private void initializeAnimation() {
         Interpolator interp = AnimationUtils.loadInterpolator(this, INTERPOLATOR);
         this.leftInAnimation = this.loadAnimation(R.anim.anim_left_in, interp);
         this.leftOutAnimation = this.loadAnimation(R.anim.anim_left_out, interp);
         this.rightInAnimation = this.loadAnimation(R.anim.anim_right_in, interp);
         this.rightOutAnimation = this.loadAnimation(R.anim.anim_right_out, interp);
     }
 
     private void initializeDirList() {
         this.dirList.setOnItemClickListener(new DirectoryListListener(this));
     }
 
     private void showDirectories(String[] dirs) {
         this.directories = dirs;
         this.dirList.setAdapter(new DirectoryAdapter(this, dirs));
     }
 
     private void initializeFlipButtonListener() {
         ImageButton[] nextButtons = { this.nextButton0, this.nextButton1 };
         this.setClickListener(nextButtons, new NextButtonListener(this));
 
         View[] previousButtons = { this.prevButton1, this.prevButton2 };
         this.setClickListener(previousButtons, new PreviousButtonListener(this));
     }
 
     private String getPlayingDirectory() {
         return this.playingFiles.directory;
     }
 
     private void selectDirectory(String directory) {
         this.shownFiles.directory = directory;
 
         new FileListingTask(this, directory).execute();
 
         this.dirLabel.setText(directory);
         this.enableButton(this.nextButton0, true);
         this.showNext();
     }
 
     private void showFiles(List<String> files) {
         this.showFiles(files.toArray(new String[0]));
     }
 
     private void showFiles(String[] files) {
         this.shownFiles.files = files;
         this.fileList.setAdapter(new FileAdapter(this, files));
     }
 
     private void stopTimer() {
         this.timer.cancel();
         this.timer = this.fakeTimer;
     }
 
     private void setSliderChangeListener(UzumakiSlider.OnSliderChangeListener l) {
         this.slider.clearOnSliderChangeListeners();
         this.slider.addOnSliderChangeListener(l);
     }
 
     private void enableSliderChangeListener() {
         this.setSliderChangeListener(this.trueSliderListener);
     }
 
     private void disableSliderChangeListener() {
         this.setSliderChangeListener(this.fakeSliderListener);
     }
 
     private void pause() {
         this.procAfterSeeking = new StayAfterSeeking();
         this.stopTimer();
         this.stopAudioService();
         this.changePauseButtonToPlayButton();
         this.enableSliderChangeListener();
         this.outgoingMessenger = this.fakeOutgoingMessenger;
         this.playerState = PlayerState.PAUSED;
     }
 
     private class PlayerTask extends TimerTask {
 
         public PlayerTask(MainActivity activity) {
             this.handler = new Handler();
             this.proc = new Proc(activity);
         }
 
         private class Proc extends ActivityHolder implements Runnable {
 
             public Proc(MainActivity activity) {
                 super(activity);
             }
 
             public void run() {
                 this.activity.sendMessage(AudioService.MSG_WHAT_TIME);
             }
         }
 
         @Override
         public void run() {
             this.handler.post(this.proc);
         }
 
         private Handler handler;
         private Runnable proc;
     }
 
     private void showCurrentTime() {
         this.showTime(this.currentTime, this.slider.getProgress());
     }
 
     private void updateCurrentTime(int position) {
         this.slider.setProgress(position);
         this.showCurrentTime();
     }
 
     private void startTimer() {
         this.timer = new TrueTimer();
         /*
          * Each Timer requests new TimerTask object (Timers cannot share one
          * task).
          */
         this.timer.scheduleAtFixedRate(new PlayerTask(this), 0, 10);
     }
 
     private void changePlayButtonToPauseButton() {
         this.playButton.setOnClickListener(this.pauseListener);
         this.playButton.setImageResource(R.drawable.ic_pause);
     }
 
     private void changePauseButtonToPlayButton() {
         this.playButton.setOnClickListener(this.playListener);
         this.playButton.setImageResource(R.drawable.ic_play);
     }
 
     private void onConnectedWithService() {
         this.startTimer();
         this.procAfterSeeking = new PlayAfterSeeking(this);
         this.changePlayButtonToPauseButton();
         this.disableSliderChangeListener();
     }
 
     private void play() {
         /*
          * Stops the current timer. New timer will start by
          * PlayProcedureOnConnected later.
          */
         this.stopTimer();
 
         this.startAudioService();
         this.bindAudioService(new PlayProcedureOnConnected(this));
 
         this.playerState = PlayerState.PLAYING;
     }
 
     private void sendWhatFile() {
         this.sendMessage(AudioService.MSG_WHAT_FILE);
     }
 
     private void sendInit() {
         AudioService.InitArgument a = new AudioService.InitArgument();
         a.directory = this.playingFiles.directory;
         a.files = this.playingFiles.files;
         a.position = this.playingFilePosition;
         this.sendMessage(AudioService.MSG_INIT, a);
     }
 
     private void sendPlay() {
         AudioService.PlayArgument a = new AudioService.PlayArgument();
         a.offset = this.slider.getProgress();
         this.sendMessage(AudioService.MSG_PLAY, a);
     }
 
     private String getPlayingFile() {
         String[] files = this.playingFiles.files;
         int pos = this.playingFilePosition;
         // Returning "" must be harmless.
         return pos < files.length ? files[pos] : "";
     }
 
     private String getPlayingPath() {
         String dir = this.getPlayingDirectory();
         return dir + File.separator + this.getPlayingFile();
     }
 
     private int getDuration(String path) {
         String col = MediaStore.Audio.AudioColumns.DURATION;
         Cursor c = this.getContentResolver().query(
                 MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                 new String[] { col },
                 String.format("%s=?", MediaStore.Audio.Media.DATA),
                 new String[] { path },
                 null);  // order
         try {
             // FIXME: This code crashes when no record is found.
             c.moveToNext();
             return c.getInt(c.getColumnIndex(col));
         }
         finally {
             c.close();
         }
     }
 
     private void showTime(TextView view, int time_msec) {
         int time_sec = time_msec / 1000;
         int min = (time_sec / 60) % 100;
         int sec = time_sec % 60;
         view.setText(String.format("%02d:%02d", min, sec));
     }
 
     private void enableButton(ImageButton button, boolean enabled) {
         /*
          * Why did not I use style?
          * ========================
          *
          * I tried to give the following settings only in *.xml, but it did not
          * work. I do not know why still. So I give the settings at here.
          */
         button.setClickable(enabled);
         int resourceId = enabled ? R.drawable.nav_right : R.drawable.ic_blank;
         button.setImageResource(resourceId);
     }
 
     private void selectFile(int position) {
         this.pause();
         this.incomingHandler.ignoreResponseUntilPlaying();
 
         this.playingFiles.copyFrom(this.shownFiles);
         this.playingFilePosition = position;
 
         this.enableButton(this.nextButton1, true);
         this.showNext();
         this.slider.setProgress(0);
         this.dirList.invalidateViews();
 
         this.play();
     }
 
     /**
      * Updates views which relate only with a playing file. Current time does
      * not relate with a playing file only, but also time, so it is out of
      * targets of this method.
      */
     private void showPlayingFile() {
         this.fileList.invalidateViews();
 
         String path = this.getPlayingPath();
         int duration = this.getDuration(path);
         this.slider.setMax(duration);
         this.title.setText(this.getPlayingFile());
         this.showTime(this.totalTime, duration);
     }
 
     private void showPrevious() {
         this.flipper.setInAnimation(this.leftInAnimation);
         this.flipper.setOutAnimation(this.rightOutAnimation);
         this.flipper.showPrevious();
     }
 
     private void showNext() {
         this.flipper.setInAnimation(this.rightInAnimation);
         this.flipper.setOutAnimation(this.leftOutAnimation);
         this.flipper.showNext();
     }
 
     private abstract class ListListener implements AdapterView.OnItemClickListener {
 
         public ListListener(MainActivity activity) {
             this.activity = activity;
         }
 
         protected MainActivity activity;
     }
 
     private class DirectoryListListener extends ListListener {
 
         public DirectoryListListener(MainActivity activity) {
             super(activity);
         }
 
         public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
             this.activity.selectDirectory(this.activity.directories[position]);
         }
     }
 
     private class FileListListener extends ListListener {
 
         public FileListListener(MainActivity activity) {
             super(activity);
         }
 
         public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
             this.activity.selectFile(position);
         }
     }
 
     private void setClickListener(View[] buttons, View.OnClickListener listener) {
         for (View button: buttons) {
             button.setOnClickListener(listener);
         }
     }
 
     private abstract class FlipButtonListener implements View.OnClickListener {
 
         public FlipButtonListener(MainActivity activity) {
             this.activity = activity;
         }
 
         protected MainActivity activity;
     }
 
     private class NextButtonListener extends FlipButtonListener {
 
         public NextButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.showNext();
         }
     }
 
     private class PreviousButtonListener extends FlipButtonListener {
 
         public PreviousButtonListener(MainActivity activity) {
             super(activity);
         }
 
         @Override
         public void onClick(View view) {
             this.activity.showPrevious();
         }
     }
 
     private interface TimerInterface {
 
         public void scheduleAtFixedRate(TimerTask task, long deley, long period);
         public void cancel();
     }
 
     private class TrueTimer implements TimerInterface {
 
         public TrueTimer() {
             this.timer = new Timer(true);
         }
 
         public void scheduleAtFixedRate(TimerTask task, long deley, long period) {
             this.timer.scheduleAtFixedRate(task, deley, period);
         }
 
         public void cancel() {
             this.timer.cancel();
         }
 
         private Timer timer;
     }
 
     private class FakeTimer implements TimerInterface {
 
         public void scheduleAtFixedRate(TimerTask task, long deley, long period) {
         }
 
         public void cancel() {
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         Log.i(LOG_TAG, "MainActivity was destroyed.");
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         this.resumeState();
 
         if (this.playerState == PlayerState.PLAYING) {
             this.bindAudioService(new ResumeProcedureOnConnected(this));
             this.serviceStarter = new FakeServiceStarter();
             this.serviceStopper = new TrueServiceStopper(this);
         }
         else {
             this.serviceStarter = new TrueServiceStarter(this);
             this.serviceStopper = new FakeServiceStopper();
             this.serviceUnbinder = new FakeServiceUnbinder();
             this.connection = null;
             /*
              * To initialize other members (timer, the play button, outgoing
              * messenger, etc), pause() is called.
              */
             this.pause();
         }
 
         Log.i(LOG_TAG, "MainActivity was resumed.");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         this.stopTimer();
         this.unbindAudioService();
         this.saveState();
 
         Log.i(LOG_TAG, "MainActivity was paused.");
     }
 
     private SharedPreferences getPrivatePreferences() {
         return getPreferences(Context.MODE_PRIVATE);
     }
 
     private void saveState() {
         Editor editor = getPrivatePreferences().edit();
 
         // widgets' states
         this.saveInt(editor, Key.PAGE_INDEX, this.flipper.getDisplayedChild());
         this.saveButton(editor, Key.NEXT_BUTTON0_ENABLED, this.nextButton0);
         this.saveButton(editor, Key.NEXT_BUTTON1_ENABLED, this.nextButton1);
         this.saveTextView(editor, Key.DIRECTORY_LABEL, this.dirLabel);
         this.saveInt(editor, Key.DURATION, this.slider.getMax());
         this.saveInt(editor, Key.PROGRESS, this.slider.getProgress());
         this.saveTextView(editor, Key.TITLE, this.title);
         this.saveTextView(editor, Key.CURRENT_TIME, this.currentTime);
         this.saveTextView(editor, Key.TOTAL_TIME, this.totalTime);
 
         // internal data
         PreferencesUtil.putPlayerState(editor, this.playerState);
         saveStringArray(editor, Key.DIRECTORIES, this.directories);
         this.saveFileSystem(editor,
                             this.shownFiles,
                             Key.SHOWN_DIRECTORY,
                             Key.SHOWN_FILES);
         this.saveFileSystem(editor,
                             this.playingFiles,
                             Key.PLAYING_DIRECTORY,
                             Key.PLAYING_FILES);
         this.saveInt(editor,
                      Key.PLAYING_FILE_POSITION,
                      this.playingFilePosition);
 
         editor.commit();
     }
 
     private void resumeState() {
         SharedPreferences prefs = getPrivatePreferences();
 
         // Widgets
         int childIndex = prefs.getInt(Key.PAGE_INDEX.getKey(), 0);
         this.flipper.setDisplayedChild(childIndex);
         this.restoreButton(prefs, Key.NEXT_BUTTON0_ENABLED, this.nextButton0);
         this.restoreButton(prefs, Key.NEXT_BUTTON1_ENABLED, this.nextButton1);
         this.restoreTextView(prefs, Key.DIRECTORY_LABEL, this.dirLabel);
         this.restoreSlider(prefs);
         this.restoreTextView(prefs, Key.TITLE, this.title);
         this.restoreTextView(prefs, Key.CURRENT_TIME, this.currentTime);
         this.restoreTextView(prefs, Key.TOTAL_TIME, this.totalTime);
 
         // Internal data
         this.playerState = PreferencesUtil.getPlayerState(prefs);
         this.directories = PreferencesUtil.getStringArray(
                 prefs,
                 Key.DIRECTORIES.name());
         this.restoreFileSystem(prefs,
                                this.shownFiles,
                                Key.SHOWN_DIRECTORY,
                                Key.SHOWN_FILES);
         this.restoreFileSystem(prefs,
                                this.playingFiles,
                                Key.PLAYING_DIRECTORY,
                                Key.PLAYING_FILES);
         this.playingFilePosition = prefs.getInt(
                 Key.PLAYING_FILE_POSITION.name(),
                 0);
 
         // Restores UI.
         this.showFiles(this.shownFiles.files);
     }
 
     private enum Key {
         PAGE_INDEX,
         NEXT_BUTTON0_ENABLED,
         NEXT_BUTTON1_ENABLED,
         DIRECTORY_LABEL,
         DURATION,
         PROGRESS,
         TITLE,
         CURRENT_TIME,
         TOTAL_TIME,
 
         DIRECTORIES,
         SHOWN_DIRECTORY,
         SHOWN_FILES,
         PLAYING_DIRECTORY,
         PLAYING_FILES,
         PLAYING_FILE_POSITION,
 
         PLAYER_STATE;
 
         public String getKey() {
             return this.name();
         }
     }
 
     private void saveInt(Editor editor, Key key, int n) {
         editor.putInt(key.name(), n);
     }
 
     private void restoreSlider(SharedPreferences prefs) {
         /*
          * The default value is dummy. Its role is only avoiding the exception
          * of divide by zero.
          */
         this.slider.setMax(prefs.getInt(Key.DURATION.name(), 1));
 
         this.slider.setProgress(prefs.getInt(Key.PROGRESS.name(), 0));
     }
 
     private void saveTextView(Editor editor, Key key, TextView view) {
         editor.putString(key.name(), view.getText().toString());
     }
 
     private void restoreTextView(SharedPreferences prefs, Key key, TextView view) {
         view.setText(prefs.getString(key.name(), null));
     }
 
     private void saveString(Editor editor, Key key, String value) {
         editor.putString(key.name(), value);
     }
 
     private void saveStringArray(Editor editor, Key key, String[] values) {
         PreferencesUtil.putStringArray(editor, key.name(), values);
     }
 
     private void saveFileSystem(Editor editor,
                                 FileSystem fs,
                                 Key directoryKey,
                                 Key filesKey) {
         this.saveString(editor, directoryKey, fs.directory);
         this.saveStringArray(editor, filesKey, fs.files);
     }
 
     private void restoreFileSystem(SharedPreferences prefs,
                                    FileSystem fs,
                                    Key directoryKey,
                                    Key filesKey) {
         fs.directory = prefs.getString(directoryKey.name(), null);
         fs.files = PreferencesUtil.getStringArray(prefs, filesKey.name());
     }
 
     private void restoreButton(SharedPreferences prefs, Key key, ImageButton button) {
         this.enableButton(button, prefs.getBoolean(key.getKey(), false));
     }
 
     private void saveButton(Editor editor, Key key, ImageButton button) {
         editor.putBoolean(key.getKey(), button.isClickable());
     }
 
     private void sendMessage(int what, Object o) {
         Message msg = Message.obtain(null, what, o);
         msg.replyTo = this.incomingMessenger;
         try {
             this.outgoingMessenger.send(msg);
         }
         catch (RemoteException e) {
             // TODO: MainActivity must show error to users.
             e.printStackTrace();
         }
     }
 
     private void sendMessage(int what) {
         this.sendMessage(what, null);
     }
 
     private void log(String msg) {
         this.title.setText(msg);
     }
 
     private void startAudioService() {
         this.serviceStarter.start();
         this.serviceStarter = new FakeServiceStarter();
         this.serviceStopper = new TrueServiceStopper(this);
     }
 
     private void stopAudioService() {
         this.serviceStopper.stop();
         this.serviceStarter = new TrueServiceStarter(this);
         this.serviceStopper = new FakeServiceStopper();
     }
 
     private void unbindAudioService() {
         this.serviceUnbinder.unbind();
         this.serviceUnbinder = new FakeServiceUnbinder();
     }
 
     private Intent makeAudioServiceIntent() {
         return new Intent(this, AudioService.class);
     }
 
     private void bindAudioService(Runnable procedureOnConnected) {
         Intent intent = this.makeAudioServiceIntent();
         this.connection = new Connection(this, procedureOnConnected);
         this.bindService(intent, this.connection, Context.BIND_AUTO_CREATE);
         this.serviceUnbinder = new TrueServiceUnbinder(this);
     }
 
     private void onStartSliding() {
         this.stopTimer();
         this.enableSliderChangeListener();
         this.sendMessage(AudioService.MSG_PAUSE);
     }
 
     private void initializeFileList() {
         this.fileList.setOnItemClickListener(new FileListListener(this));
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
