 package com.livejournal.karino2.whiteboardcast;
 
 import android.graphics.Bitmap;
 import android.net.Uri;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by karino on 11/28/13.
  */
 public class Presentation {
     public void stopRecord() {
         recorder.stop();
         recorder.release();
         recStats = RecordStatus.DONE;
     }
 
     public void stopRecordBegin() {
         recStats = RecordStatus.DONE_PROCESS;
     }
 
     public boolean afterStop() {
        future.cancel(false);
         future = null;
         return encoderTask.doneEncoder();
     }
 
     public void newRecorder(long currentMill) {
         recorder = new VorbisMediaRecorder();
         recorder.setBeginMill(currentMill);
     }
 
     public VorbisMediaRecorder getRecorder() {
         return recorder;
     }
 
     public void startRecord() {
         recorder.start();
 
         scheduleEncodeTask();
         recStats = RecordStatus.RECORDING;
     }
 
     public void startRecordFirstPhase() {
         recStats = RecordStatus.SETUP;
     }
 
     public void clearSlides() throws IOException {
         getSlideList().deleteAll();
     }
 
     public enum RecordStatus {
         DORMANT, SETUP, RECORDING, PAUSE, DONE_PROCESS, DONE
     }
 
     RecordStatus recStats = RecordStatus.DORMANT;
 
     public RecordStatus recordStatus() {
         return recStats;
     }
 
 
     public boolean canStartRecord() {
         return recStats == RecordStatus.DORMANT ||
                 recStats == RecordStatus.DONE;
     }
 
     public boolean canStopRecord() {
         return recStats == RecordStatus.RECORDING ||
                 recStats == RecordStatus.PAUSE;
     }
 
     private EncoderTask encoderTask = null;
     public EncoderTask getEncoderTask() {
         return encoderTask;
     }
 
     VorbisMediaRecorder recorder;
     Future<?> future = null;
 
 
     public void newEncoderTask(FrameRetrieval frameR, Bitmap parentBmp, String workVideoPath, EncoderTask.ErrorListener elistn) {
         encoderTask = new EncoderTask(frameR, parentBmp, workVideoPath, elistn);
     }
 
     public void pauseRecord() {
         recStats = RecordStatus.PAUSE;
 
         future.cancel(false);
         future = null;
         recorder.stop();
         encoderTask.stop();
 
     }
 
     public void resumeRecord() {
         recStats = RecordStatus.RECORDING;
         long suspendedBegin = recorder.lastBlockEndMil();
         long suspendedDur = System.currentTimeMillis() - suspendedBegin;
         recorder.resume(suspendedDur);
         encoderTask.resume(suspendedDur);
 
     }
 
     private final int FPS = 12;
     //    private final int FPS = 6;
 //    private final int FPS = 30;
     public void scheduleEncodeTask() {
         future = getScheduleExecutor().scheduleAtFixedRate(encoderTask, 0, 1000 / FPS, TimeUnit.MILLISECONDS);
 
     }
 
     ScheduledExecutorService scheduleExecuter = null;
     ScheduledExecutorService getScheduleExecutor() {
         if(scheduleExecuter == null) {
             scheduleExecuter = Executors.newScheduledThreadPool(2);
         }
         return scheduleExecuter;
     }
 
     SlideList slideList;
     SlideList getSlideList() throws IOException {
         if(slideList == null) {
             slideList = SlideList.createSlideListWithDefaultFolder();
         }
         return slideList;
     }
 
     boolean slideEnabled = false;
     public void enableSlide() {
         slideEnabled = true;
     }
 
     public boolean slideAvailable() {
         return slideEnabled;
 
     }
 
     List<File> getSlideFiles() throws IOException {
         return getSlideList().getFiles();
     }
 
 
     public File getResultFile() {
         return lastResult;
     }
     public void setResult(File file) {
         lastResult = file;
     }
 
     public void renameResult(File newNameFile) {
         lastResult.renameTo(newNameFile);
         lastResult = newNameFile;
     }
 
     File lastResult = null;
     Uri lastResultUri = null;
 
     public Uri getResultUri() {
         return lastResultUri;
     }
 
     public void setResultUri(Uri resultUri) {
         this.lastResultUri = resultUri;
     }
 
 
 }
