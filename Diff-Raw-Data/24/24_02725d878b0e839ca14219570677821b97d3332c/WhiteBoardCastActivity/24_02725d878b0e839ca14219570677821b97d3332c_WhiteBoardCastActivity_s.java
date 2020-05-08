 package com.livejournal.karino2.whiteboardcast;
 
 import com.google.libvorbis.VorbisException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.webkit.WebView;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 public class WhiteBoardCastActivity extends Activity {
 
     static final int DIALOG_ID_ABOUT = 1;
     static final int DIALOG_ID_QUERY_VIEW_SHARE = 2;
 
     private static final String AUDIO_FNAME = "temp.mkv";
 
     private ScheduledExecutorService scheduleExecuter = null;
     private EncoderTask encoderTask = null;
 
     public void showMessage(String msg) {
         Toast.makeText(this, msg, Toast.LENGTH_LONG ).show();
 
     }
 
 
     Handler handler = new Handler();
     VorbisMediaRecorder recorder;
     Future<?> futuer = null;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_whiteboardcast);
 
     }
 
     public boolean canUndo() {
         WhiteBoardCanvas wb = getWhiteBoardCanvas();
         if(wb == null)
             return false;
         return wb.canUndo();
     }
 
     public void undo() {
         getWhiteBoardCanvas().undo();
     }
 
 

     public void stopRecord() {
        if(recStats != RecordStatus.RECORDING) {
             Log.d("WBCast", "stop record called but not recording. " + recStats);
             return;
         }
         // under processing.
         changeRecStatus(RecordStatus.DONE_PROCESS);
         showMessage("record end, start post process...");
         scheduleExecuter.shutdown();
         scheduleExecuter = Executors.newSingleThreadScheduledExecutor();
         recorder.stop();
         recorder.release();
         changeRecStatus(RecordStatus.DONE);
 
         if(!encoderTask.doneEncoder(new Encoder.FinalizeListener(){
             @Override
             public void done() {
                 beginAudioVideoMergeTask();
                 handler.postDelayed(new Runnable(){
                     @Override
                     public void run() {
                         // for debug.
                         if(encoderTask.getErrorBuf().length() != 0) {
                             showMessage("deb error: " + encoderTask.getErrorBuf().toString());
                         }
 
                     }
                 }, 0);
 
             }
         })) {
             showMessage("done encoder fail");
         }
     }
 
     public boolean canRedo() {
         WhiteBoardCanvas wb = getWhiteBoardCanvas();
         if(wb == null)
             return false;
         return wb.canRedo();
     }
 
     public void redo() {
         getWhiteBoardCanvas().redo();
     }
 
     public enum RecordStatus {
         DORMANT, SETUP, RECORDING, PAUSE, DONE_PROCESS, DONE
     }
     RecordStatus recStats = RecordStatus.DORMANT;
 
     public RecordStatus getRecStats() {
         return recStats;
     }
 
     void changeRecStatus(RecordStatus stats) {
         recStats = stats;
         getWhiteBoardCanvas().changeRecStatus(stats);
     }
 
     public void pauseRecord() {
         changeRecStatus(RecordStatus.PAUSE);
         futuer.cancel(false);
         futuer = null;
         recorder.stop();
         encoderTask.stop();
         showMessage("pause");
     }
 
     public void resumeRecord() {
         long suspendedBegin = recorder.lastBlockEndMil();
         long suspendedDur = System.currentTimeMillis() - suspendedBegin;
         recorder.resume(suspendedDur);
         encoderTask.resume(suspendedDur);
         scheduleEncodeTask();
         changeRecStatus(RecordStatus.RECORDING);
         showMessage("resume");
     }
 
 
 
     public void startRecord() {
         if(recStats != RecordStatus.DORMANT &&
                 recStats != RecordStatus.DONE) {
             Log.d("WBCast", "record start but status is not dormant: " + recStats);
             return;
         }
         changeRecStatus(RecordStatus.SETUP);
         scheduleExecuter = Executors.newSingleThreadScheduledExecutor();
         handler.post(new Runnable() {
             @Override
             public void run() {
                 // this method take some times. so get it back to UI first for update overlay.
                 startRecordSecondPhase();
             }
         });
     }
 
     private void startRecordSecondPhase() {
         WhiteBoardCanvas wb = getWhiteBoardCanvas();
         wb.setWholeAreaInvalidate(); // for restart. make it a little heavy.
         try {
             encoderTask = new EncoderTask(wb, wb.getBitmap(), getWorkVideoPath());
         } catch (IOException e) {
             showMessage("Fail to get workVideoPath: " + e.getMessage());
             return;
         }
         long currentMill = System.currentTimeMillis();
 
         if(!encoderTask.initEncoder(currentMill)) {
             showMessage("init encode fail");
             return;
         }
         recorder = new VorbisMediaRecorder();
         recorder.setBeginMill(currentMill);
         try {
             recorder.setOutputFile(getWorkAudioPath());
         } catch (IOException e) {
             showMessage("IOException: Create WhiteBoardCast folder fail: " + e.getMessage());
             return;
         }
         try {
             recorder.prepare();
         } catch (IOException e) {
             showMessage("IOException: MediaRecoder prepare fail: " + e.getMessage());
             return;
         } catch (VorbisException e) {
             showMessage("VorbisException: MediaRecoder prepare fail: " + e.getMessage());
             return;
         }
         recorder.start();
 
         scheduleEncodeTask();
         changeRecStatus(RecordStatus.RECORDING);
         showMessage("record start");
     }
 
     private void scheduleEncodeTask() {
         futuer = scheduleExecuter.scheduleAtFixedRate(encoderTask, 0, 1000 / FPS, TimeUnit.MILLISECONDS);
     }
 
     private WhiteBoardCanvas getWhiteBoardCanvas() {
         return (WhiteBoardCanvas)findViewById(R.id.fullscreen_content);
     }
 
     private static  void ensureDirExist(File dir) throws IOException {
         if(!dir.exists()) {
             if(!dir.mkdir()){
                 throw new IOException();
             }
         }
     }
     public static File getFileStoreDirectory() throws IOException {
         File dir = new File(Environment.getExternalStorageDirectory(), "WhiteBoardCast");
         ensureDirExist(dir);
         return dir;
     }
 
 
     public String getResultPath() throws IOException {
         return getFileStoreDirectory().getAbsolutePath() + "/result.webm";
     }
 
     private String getWorkAudioPath() throws IOException {
         return getFileStoreDirectory().getAbsolutePath() + "/" + AUDIO_FNAME;
     }
 
     private String getWorkVideoPath() throws IOException {
         return getFileStoreDirectory().getAbsolutePath() + "/temp.webm";
     }
 
 
     private void viewVideoIntent() {
         Intent i = new Intent(Intent.ACTION_VIEW);
         i.setDataAndType(Uri.fromFile(lastResult), "video/*");
         i.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, false);
         startActivity(i);
     }
 
     private void shareVideoIntent() {
         Intent i = new Intent(Intent.ACTION_SEND);
         i.setType("video/webm");
 
         SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
         String title = timeStampFormat.format(new Date()) + " recorded";
 
         ContentValues content = new ContentValues(4);
 
 
         content.put(MediaStore.Video.VideoColumns.TITLE, title);
         content.put(MediaStore.Video.VideoColumns.DATE_ADDED,
                 System.currentTimeMillis() / 1000);
         content.put(MediaStore.Video.Media.MIME_TYPE, "video/webm");
         content.put(MediaStore.Video.Media.DATA, lastResult.getAbsolutePath());
         ContentResolver resolver = getBaseContext().getContentResolver();
         Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content);
 
 
         i.putExtra(Intent.EXTRA_STREAM, uri);
         startActivity(Intent.createChooser(i, "Share video"));
 
     }
 
     private void beginAudioVideoMergeTask() {
         try {
             new AudioVideoMergeTask(this, new AudioVideoMergeTask.NotifyFinishListener() {
                 @Override
                 public void onFinish() {
                     handler.postDelayed(new Runnable(){
                         @Override
                         public void run() {
                             try {
                                 renameAndDeleteWorkFiles();
                                 showDialog(DIALOG_ID_QUERY_VIEW_SHARE);
                             } catch (IOException e) {
                                 showMessage("Rename encoded file fail: " + e.getMessage());
                             }
                         }
                     }, 500);
                 }
             }).execute(getWorkVideoPath(), getWorkAudioPath(), getResultPath());
         } catch (IOException e) {
             showMessage("Fail to create WhiteBoardCast folder(2). " + e.getMessage());
         }
     }
 
     File lastResult = null;
 
     private void renameAndDeleteWorkFiles() throws IOException {
         File result = new File(getResultPath());
         File workVideo = new File(getWorkVideoPath());
         File workAudio = new File(getWorkAudioPath());
         if(!result.exists())
             throw new IOException("no encoded file exists.");
         SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
         String filename = timeStampFormat.format(new Date()) + ".webm";
 
         lastResult = new File(getFileStoreDirectory(), filename);
         result.renameTo(lastResult);
         workVideo.delete();
         workAudio.delete();
     }
 
 
     @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         if(event.getAction() == KeyEvent.ACTION_DOWN)
         {
             switch(event.getKeyCode()) {
                 case KeyEvent.KEYCODE_BACK:
                     new AlertDialog.Builder(this)
                             .setIcon(android.R.drawable.ic_dialog_alert)
                         .setMessage(R.string.query_back_message)
                         .setCancelable(true)
                         .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                             public void onClick(DialogInterface dialog, int which) {
                                 finish();
                             }})
                             .setNegativeButton(android.R.string.no, null)
                             .show();
                     return true;
             }
         }
         return super.dispatchKeyEvent(event);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     public void clearCanvas() {
         getWhiteBoardCanvas().clearCanvas();
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case R.id.menu_id_about:
                 showDialog(DIALOG_ID_ABOUT);
                 return true;
             case R.id.menu_id_quit:
                 finish();
                 return true;
         }
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         switch(id) {
             case DIALOG_ID_ABOUT:
                 return createAbout();
             case DIALOG_ID_QUERY_VIEW_SHARE:
                 return createQueryViewShareDialog();
         }
         return super.onCreateDialog(id);
     }
 
     private Dialog createQueryViewShareDialog() {
         LayoutInflater inflater = LayoutInflater.from(this);
         View view = inflater.inflate(R.layout.query_viewsend, null);
         setOnClickListener(view, R.id.button_view, new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 viewVideoIntent();
             }
         });
         setOnClickListener(view, R.id.button_share, new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 shareVideoIntent();
             }
         });
         return new AlertDialog.Builder(this).setTitle(R.string.query_title)
                 .setView(view)
                 .create();
     }
 
     private void setOnClickListener(View view, int id, View.OnClickListener onclick) {
         Button button = (Button)view.findViewById(id);
         button.setOnClickListener(onclick);
     }
 
     private AlertDialog createAbout() {
         final WebView webView = new WebView(this);
         webView.loadUrl("file:///android_asset/licenses.html");
         return new AlertDialog.Builder(this).setTitle(R.string.about_title)
                 .setView(webView)
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog, final int whichButton) {
                         dialog.dismiss();
                     }
                 }).create();
 
     }
 
     private final int FPS = 12;
 
     private Button findButton(int id) {
         return (Button)findViewById(id);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
     }
 
     public void setPenOrEraser(int penIndex) {
         getWhiteBoardCanvas().setPenOrEraser(penIndex);
     }
 }
