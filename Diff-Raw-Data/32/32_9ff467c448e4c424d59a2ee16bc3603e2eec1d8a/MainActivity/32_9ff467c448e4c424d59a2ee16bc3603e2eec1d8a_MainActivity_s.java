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
 import android.database.Cursor;
 import android.media.MediaMetadataRetriever;
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
 
     private static class FileSystem {
 
         int directoryPosition = UNSELECTED;
         String[] files = new String[0];
     }
 
     private static class ActivityHolder {
 
         protected MainActivity activity;
 
         public ActivityHolder(MainActivity activity) {
             this.activity = activity;
         }
     }
 
     private abstract class Adapter extends ArrayAdapter<String> {
 
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
 
     private class FileAdapter extends Adapter {
 
         private class Row {
 
             public ImageView playingIcon;
             public TextView name;
         }
 
         public FileAdapter(MainActivity activity, String[] objects) {
             super(activity, objects);
         }
 
         @Override
         protected View makeView(int position, View convertView) {
             Row row = (Row)convertView.getTag();
             MainActivity activity = this.activity;
             boolean isPlaying = this.isPlayingDirectoryShown() && (position == activity.filePosition);
             int src = isPlaying ? R.drawable.ic_playing : R.drawable.ic_blank;
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
             int dirPos = activity.playingFile.directoryPosition;
             return dirPos == activity.selectedFile.directoryPosition;
         }
     }
 
     private class DirectoryAdapter extends Adapter {
 
         private class Row {
 
             public ImageView playingIcon;
             public TextView path;
         }
 
         public DirectoryAdapter(MainActivity activity, String[] objects) {
             super(activity, objects);
         }
 
         @Override
         protected View makeView(int position, View convertView) {
             int dirPos = this.activity.selectedFile.directoryPosition;
             int selectedColor = 0xfff4a0bd; // TODO: Remove magic number.
             int unSelectedColor = android.R.color.transparent;
             int color = dirPos == position ? selectedColor : unSelectedColor;
             convertView.setBackgroundColor(color);
 
             Row row = (Row)convertView.getTag();
             int src = position != this.activity.playingFile.directoryPosition ? R.drawable.ic_blank : R.drawable.ic_playing;
             row.playingIcon.setImageResource(src);
             row.path.setText(this.getItem(position));
 
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
     }
 
     private class PlayProcedureOnConnected extends ActivityHolder implements Runnable {
 
         public PlayProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.sendPlay();
             this.activity.onConnectedWithService();
         }
     }
 
     private class ResumeProcedureOnConnected extends ActivityHolder implements Runnable {
 
         public ResumeProcedureOnConnected(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
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
 
             Log.i(LOG_TAG, "MainActivity conneted to AudioService.");
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
                 this.activity.nextProc.run();
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
 
         private class NopHandler extends MessageHandler {
 
             public NopHandler() {
                 super(null);
             }
 
             public void handle(Message _) {
             }
         }
 
         private class PlayHandler extends MessageHandler {
 
             private IncomingHandler handler;
 
             public PlayHandler(IncomingHandler handler) {
                 super(null);
                 this.handler = handler;
             }
 
             public void handle(Message _) {
                 this.handler.enableResponse();
             }
         }
 
         private SparseArray<MessageHandler> handlers;
         private MessageHandler whatTimeHandler;
         private MessageHandler completionHandler;
         private MessageHandler nopHandler;
 
         public IncomingHandler(MainActivity activity) {
             this.handlers = new SparseArray<MessageHandler>();
             this.handlers.put(AudioService.MSG_PLAY, new PlayHandler(this));
 
             this.whatTimeHandler = new WhatTimeHandler(activity);
             this.completionHandler = new CompletionHandler(activity);
             this.nopHandler = new NopHandler();
 
             this.ignoreResponseUntilPlay();
         }
 
         @Override
         public void handleMessage(Message msg) {
             this.handlers.get(msg.what).handle(msg);
         }
 
         /**
          * Orders to ignore MSG_WHAT_TIME/MSG_COMPLETION messages until a next
          * MSG_PLAY.
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
         public void ignoreResponseUntilPlay() {
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
 
        private List<String> queryAudio() {
            List<String> l = new ArrayList<String>();
 
             String trackColumn = MediaStore.Audio.AudioColumns.TRACK;
             String pathColumn = MediaStore.MediaColumns.DATA;
             String order = String.format("%s, %s", trackColumn, pathColumn);
             Cursor c = this.activity.getContentResolver().query(
                     MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                     new String[] { pathColumn },
                     null,   // selection
                     null,   // selection arguments
                     order); // order
            try {
                int index = c.getColumnIndex(pathColumn);
                while (c.moveToNext()) {
                    l.add(c.getString(index));
                }
            }
            finally {
                c.close();
            }

            return l;
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
 
     private abstract static class NextProc implements Runnable {
 
         protected MainActivity activity;
 
         public NextProc(MainActivity activity) {
             this.activity = activity;
         }
 
         public abstract void run();
     }
 
     private static class PlayNothingProc extends NextProc {
 
         public PlayNothingProc(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.pause();
         }
     }
 
     private static class PlayNextProc extends NextProc {
 
         public PlayNextProc(MainActivity activity) {
             super(activity);
         }
 
         public void run() {
             this.activity.playNextAudio();
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
     private static final int UNSELECTED = -1;
 
     // Widgets
     private ViewFlipper flipper;
     /**
      * pageIndex is needed for save/restore instance state. I tried some ways
      * to know current page index on runtime, but I did not find a useful way
      * in any cases. Counting manually is easiest.
      */
     private int pageIndex;
 
     private ListView dirList;
     private ImageButton nextButton0;
 
     private View prevButton1;
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
     private String[] dirs = new String[0];
     private FileSystem selectedFile = new FileSystem();
     private FileSystem playingFile = new FileSystem();
     private int filePosition = UNSELECTED;
 
     /**
      * <code>state</code> tells which playing or paused the player is. I tried
      * to reuse other objects to indicate this, but such design cannot tell what
      * I do strongly. Using a special variable is most simple.
      */
     private PlayerState state = PlayerState.PAUSED;
     private Runnable procAfterSeeking;
     private NextProc nextProc;
 
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
 
         this.findViews();
         this.initializeFlipButtonListener();
         this.initializeDirList();
         this.initializeFileList();
         this.initializeAnimation();
         this.initializePlayButton();
         this.initializeTimer();
         this.initializeSlider();
         this.initializeMenu();
 
         this.pageIndex = 0;
         this.incomingHandler = new IncomingHandler(this);
         this.incomingMessenger = new Messenger(this.incomingHandler);
         this.fakeOutgoingMessenger = new FakeMessenger();
 
         Log.i(LOG_TAG, "Created.");
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
     //private static final int INTERPOLATOR = android.R.anim.decelerate_interpolator;
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
         this.dirs = dirs;
         this.dirList.setAdapter(new DirectoryAdapter(this, dirs));
     }
 
     private void initializeFlipButtonListener() {
         ImageButton[] nextButtons = { this.nextButton0, this.nextButton1 };
         this.setClickListener(nextButtons, new NextButtonListener(this));
         for (ImageButton button: nextButtons) {
             this.enableButton(button, false);
         }
 
         View[] previousButtons = { this.prevButton1, this.prevButton2 };
         this.setClickListener(previousButtons, new PreviousButtonListener(this));
     }
 
     private String getPlayingDirectory() {
         int pos = this.playingFile.directoryPosition;
         return this.dirs[pos];
     }
 
     private String getSelectedDirectory() {
         int pos = this.selectedFile.directoryPosition;
         return this.dirs[pos];
     }
 
     private void selectDir(int position) {
         this.selectedFile.directoryPosition = position;
         String dir = this.getSelectedDirectory();
         new FileListingTask(this, dir).execute();
 
         this.dirList.invalidateViews();
         this.enableButton(this.nextButton0, true);
         this.showNext();
     }
 
     private void showFiles(List<String> files) {
         this.showFiles(files.toArray(new String[0]));
     }
 
     private void showFiles(String[] files) {
         this.selectedFile.files = files;
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
         this.state = PlayerState.PAUSED;
         this.stopTimer();
         this.stopAudioService();
         this.changePauseButtonToPlayButton();
         this.enableSliderChangeListener();
         this.outgoingMessenger = this.fakeOutgoingMessenger;
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
         this.state = PlayerState.PLAYING;
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
     }
 
     private void sendPlay() {
         String path = this.getPlayingPath();
         int offset = this.slider.getProgress();
         Object a = AudioService.makePlayArgument(path, offset);
         this.sendMessage(AudioService.MSG_PLAY, a);
     }
 
     private String getPlayingFile() {
         int pos = this.filePosition;
         boolean pred = pos == UNSELECTED;
         // Returning "" must be harmless.
         return pred ? "" : this.playingFile.files[pos];
     }
 
     private String getPlayingPath() {
         String dir = this.getPlayingDirectory();
         return dir + File.separator + this.getPlayingFile();
     }
 
     private int getDuration(String path) {
         // MediaMetadataRetriever is not reusable.
         MediaMetadataRetriever meta = new MediaMetadataRetriever();
         meta.setDataSource(path);
         int key = MediaMetadataRetriever.METADATA_KEY_DURATION;
         String datum;
         try {
             datum = meta.extractMetadata(key);
         }
         finally {
             meta.release();
         }
         return Integer.parseInt(datum);
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
 
     private void playNextAudio() {
         this.playNewAudio(this.filePosition + 1);
         this.fileList.invalidateViews();
     }
 
     private void updateNextProcedure() {
         int last = this.playingFile.files.length - 1;
         this.nextProc = this.filePosition < last ? new PlayNextProc(this) : new PlayNothingProc(this);
     }
 
     private void playNewAudio(int position) {
         this.pause();
         this.incomingHandler.ignoreResponseUntilPlay();
 
         this.filePosition = position;
         this.updateNextProcedure();
 
         String path = this.getPlayingPath();
         int duration = this.getDuration(path);
         this.slider.setMax(duration);
         this.slider.setProgress(0);
         this.showPlayingFile();
 
         this.play();
     }
 
     private void selectFile(int position) {
         FileSystem fs = this.playingFile;
         fs.directoryPosition = this.selectedFile.directoryPosition;
         fs.files = this.selectedFile.files;
         this.filePosition = position;
 
         this.fileList.invalidateViews();
         this.enableButton(this.nextButton1, true);
         this.showNext();
 
         this.playNewAudio(position);
     }
 
     private void showPlayingFile() {
         this.title.setText(this.getPlayingFile());
         this.showTime(this.currentTime, this.slider.getProgress());
         this.showTime(this.totalTime, this.slider.getMax());
     }
 
     private void showPrevious() {
         this.flipper.setInAnimation(this.leftInAnimation);
         this.flipper.setOutAnimation(this.rightOutAnimation);
         this.flipper.showPrevious();
         this.pageIndex -= 1;
     }
 
     private void showNext() {
         this.flipper.setInAnimation(this.rightInAnimation);
         this.flipper.setOutAnimation(this.leftOutAnimation);
         this.flipper.showNext();
         this.pageIndex += 1;
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
             this.activity.selectDir(position);
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
     protected void onResume() {
         super.onResume();
 
         /*
          * I wondered if here is better than onRestoreInstanceState() to restore
          * members for the service, but I believe so now. Because:
          *
          * 1. Here is symmetric with onPause(). onPause() disconnects the
          * service, so reconnecting at here is beautiful.
          *
          * 2. onResume() is called always. Android does not call
          * onRestoreInstanceState() when this activity is newly created,
          * although I must initialize some members with non-null. onResume() is
          * called even in this case. I can think onResume() as a stateful
          * constructor (onCreate() is a stateless constructor. Because there are
          * no information about application's state when onCreate() is called).
          */
         if (this.state == PlayerState.PLAYING){
             Intent intent = this.makeAudioServiceIntent();
 
             Runnable proc = new ResumeProcedureOnConnected(this);
             Connection conn = new Connection(this, proc);
 
             this.bindService(intent, conn, 0);
             this.serviceStarter = new FakeServiceStarter();
             this.serviceStopper = new TrueServiceStopper(this);
             this.serviceUnbinder = new TrueServiceUnbinder(this);
             this.connection = conn;
             this.incomingHandler.enableResponse();
 
             /*
              * You may notice that some members (timer, the play button,
              * outgoing messenger, etc) are not initialized even at here. Don't
              * worry, they will be initialized through
              * ResumeProcedureOnConnected, which will call
              * onConnectedWithService() finally.
              */
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
 
         Log.i(LOG_TAG, "Resumed.");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
         this.stopTimer();
         this.unbindAudioService();
 
         Log.i(LOG_TAG, "Paused.");
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         // widgets' states
         outState.putInt(Key.PAGE_INDEX.getKey(), this.pageIndex);
         this.saveButton(outState, Key.NEXT_BUTTON0_ENABLED, this.nextButton0);
         this.saveButton(outState, Key.NEXT_BUTTON1_ENABLED, this.nextButton1);
         this.savePlayerState(outState);
         outState.putInt(Key.DURATION.getKey(), this.slider.getMax());
         outState.putInt(Key.PROGRESS.getKey(), this.slider.getProgress());
         this.saveTextView(outState, Key.TITLE, this.title);
         this.saveTextView(outState, Key.CURRENT_TIME, this.currentTime);
         this.saveTextView(outState, Key.TOTAL_TIME, this.totalTime);
 
         // internal data
         outState.putStringArray(Key.DIRS.getKey(), this.dirs);
         this.saveFileSystem(outState,
                             this.selectedFile,
                             Key.SELECTED_FILE_DIRECTORY_POSITION,
                             Key.SELECTED_FILE_FILES);
         this.saveFileSystem(outState,
                             this.playingFile,
                             Key.PLAYING_FILE_DIRECTORY_POSITION,
                             Key.PLAYING_FILE_FILES);
         outState.putInt(Key.FILE_POSITION.getKey(),
                         this.filePosition);
 
         Log.i(LOG_TAG, "Instance state was saved.");
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
 
         // Widgets
         this.pageIndex = savedInstanceState.getInt(Key.PAGE_INDEX.getKey());
         for (int i = 0; i < this.pageIndex; i++) {
             this.flipper.showNext();
         }
         this.restoreButton(savedInstanceState,
                            Key.NEXT_BUTTON0_ENABLED,
                            this.nextButton0);
         this.restoreButton(savedInstanceState,
                            Key.NEXT_BUTTON1_ENABLED,
                            this.nextButton1);
         this.slider.setMax(savedInstanceState.getInt(Key.DURATION.getKey()));
         this.slider.setProgress(savedInstanceState.getInt(Key.PROGRESS.getKey()));
         this.restorePlayerState(savedInstanceState);
         this.restoreTextView(savedInstanceState, Key.TITLE, this.title);
         this.restoreTextView(savedInstanceState,
                              Key.CURRENT_TIME,
                              this.currentTime);
         this.restoreTextView(savedInstanceState, Key.TOTAL_TIME, this.totalTime);
 
         // Internal data
         this.dirs = savedInstanceState.getStringArray(Key.DIRS.getKey());
         this.restoreFileSystem(savedInstanceState,
                                this.selectedFile,
                                Key.SELECTED_FILE_DIRECTORY_POSITION,
                                Key.SELECTED_FILE_FILES);
         this.restoreFileSystem(savedInstanceState,
                                this.playingFile,
                                Key.PLAYING_FILE_DIRECTORY_POSITION,
                                Key.PLAYING_FILE_FILES);
         String key = Key.FILE_POSITION.getKey();
         this.filePosition = savedInstanceState.getInt(key);
         this.updateNextProcedure();
 
         // Restores UI.
         this.showDirectories(this.dirs);
         this.showFiles(this.selectedFile.files);
 
         Log.i(LOG_TAG, "Instance state was restored.");
     }
 
     private enum PlayerState {
         PLAYING,
         PAUSED
     }
 
     private enum Key {
         PAGE_INDEX,
         NEXT_BUTTON0_ENABLED,
         NEXT_BUTTON1_ENABLED,
         DURATION,
         PROGRESS,
         PLAYER_STATE,
         TITLE,
         CURRENT_TIME,
         TOTAL_TIME,
 
         DIRS,
         SELECTED_FILE_DIRECTORY_POSITION,
         SELECTED_FILE_FILES,
         PLAYING_FILE_DIRECTORY_POSITION,
         PLAYING_FILE_FILES,
         FILE_POSITION;
 
         public String getKey() {
             return this.name();
         }
     }
 
     private void saveTextView(Bundle outState, Key key, TextView view) {
         outState.putCharSequence(key.getKey(), view.getText());
     }
 
     private void restoreTextView(Bundle savedInstanceState, Key key, TextView view) {
         view.setText(savedInstanceState.getCharSequence(key.name()));
     }
 
     private void savePlayerState(Bundle outState) {
         outState.putString(Key.PLAYER_STATE.getKey(), this.state.name());
     }
 
     private PlayerState getPlayerStateOfString(String s) {
         String paused = PlayerState.PAUSED.name();
         return s.equals(paused) ? PlayerState.PAUSED : PlayerState.PLAYING;
     }
 
     private void restorePlayerState(Bundle savedInstanceState) {
         String value = savedInstanceState.getString(Key.PLAYER_STATE.getKey());
         this.state = this.getPlayerStateOfString(value);
     }
 
     private void saveFileSystem(Bundle outState,
                                 FileSystem fs,
                                 Key dirKey,
                                 Key filesKey) {
         outState.putInt(dirKey.getKey(), fs.directoryPosition);
         outState.putStringArray(filesKey.getKey(), fs.files);
     }
 
     private void restoreFileSystem(Bundle savedInstanceState,
                                    FileSystem fs,
                                    Key dirKey,
                                    Key filesKey) {
         fs.directoryPosition = savedInstanceState.getInt(dirKey.getKey());
         fs.files = savedInstanceState.getStringArray(filesKey.getKey());
     }
 
     private void restoreButton(Bundle savedInstanceState, Key key, ImageButton button) {
         boolean enabled = savedInstanceState.getBoolean(key.getKey());
         this.enableButton(button, enabled);
     }
 
     private void saveButton(Bundle outState, Key key, ImageButton button) {
         outState.putBoolean(key.getKey(), button.isClickable());
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
         this.bindService(intent, this.connection, 0);
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
