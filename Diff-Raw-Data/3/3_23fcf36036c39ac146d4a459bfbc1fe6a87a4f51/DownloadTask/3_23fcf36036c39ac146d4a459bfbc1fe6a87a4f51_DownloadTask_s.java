 package com.scurab.java.ftpleecher;
 
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.*;
 
 /**
  * Base class representing one particular task<br/>
  * Task can contain n files/folders
  */
 public class DownloadTask implements FTPDownloadListener {
 
     /**
      * collections where are all working threads *
      */
     private List<FTPDownloadThread> mData;
 
     /**
      * collection of active working threads *
      */
     private List<FTPDownloadThread> mWorkingThreads;
 
     private boolean mDeleteAfterMerge;
 
     public DownloadTask(Collection<FTPDownloadThread> data) {
         mData = new ArrayList<FTPDownloadThread>(data);
         mWorkingThreads = new ArrayList<FTPDownloadThread>(data);
         bind();
     }
 
     private void bind() {
         for (FTPDownloadThread t : mData) {
             t.registerListener(this);
             t.setParentTask(this);
         }
     }
 
     //region notification
 
     @Override
     public void onError(FTPDownloadThread source, Exception e) {
     }
 
     @Override
     public void onFatalError(FTPDownloadThread source, FatalFTPException e) {
     }
 
     @Override
     public void onDownloadProgress(FTPDownloadThread source, double down, double downPerSec) {
     }
 
     @Override
     public void onStatusChange(FTPDownloadThread source, FTPDownloadThread.State state) {
         performStatusChange(source, state);
     }
 
     //endregion notification
 
     private void performStatusChange(final FTPDownloadThread source, final FTPDownloadThread.State state) {
         final boolean finished = (state == FTPDownloadThread.State.Finished);
         boolean merge = false;
 
         //ignore these states, because they are set from this class
         if (state != FTPDownloadThread.State.Merging) {
             if (finished) {
                 synchronized (mWorkingThreads) {
                     mWorkingThreads.remove(source);
                 }
                 //finished is called here or in download thread if not separated and file with same size is found
                 //on local drive
             } else {
                 synchronized (mWorkingThreads) {
                     if (source.getParentTask() != this) {
                         System.err.println("This thread is not from this task!" + source.getContext().toString());
                         return;
                     } else {
                         if (state == FTPDownloadThread.State.Downloaded) {
                             mWorkingThreads.remove(source);
                         } else {
                             if (!mWorkingThreads.contains(source)) {
                                 //can be restarted, finished are handled before
                                 mWorkingThreads.add(source);
                             }
                         }
                     }
                 }
             }
             merge = mWorkingThreads.size() == 0 && mData.size() > 1;
             //we are done in this task, now is time to merge
             if (merge) {
                 //must be called in diff thread to let finish current downloading thread
                 performMerge();
             }
         }
     }
 
     private void performMerge() {
         Thread t = new Thread(new Runnable() {
             @Override
             public void run() {
                     /*
                      * wait for sec to finish last thread
                      */
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                 onMergeFiles();
             }
         });
         t.setName("MergeThread");
         t.start();
     }
 
     public void onMergeFiles() {
         HashMap<Long, FTPDownloadThread[]> subGroups = getSubGroups();
         for (Long l : subGroups.keySet()) {
             try {
                 FTPDownloadThread[] arr = subGroups.get(l);
                 //if it's null it's still not ready
                 if (arr != null) {
                     mergeFiles(arr);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void unregisterListener(FTPDownloadThread[] parts) {
         for (FTPDownloadThread ft : parts) {
             ft.unregisterListener(this);
         }
     }
 
     private void mergeFiles(FTPDownloadThread[] parts) throws Exception {
         final String sep = System.getProperty("file.separator");
         FTPContext context = parts[0].getContext();
 
         //create output file and rename it if exists
         File outputFile = new File(context.outputDirectory + sep + context.fileName);
         if (outputFile.exists()) {
             outputFile.renameTo(new File(context.outputDirectory + sep + context.fileName + ".old" + System.currentTimeMillis()));
         }
 
         //final output stream
         FileOutputStream fos = new FileOutputStream(outputFile);
         for (int i = 0, n = parts.length; i < n; i++) {
             FTPDownloadThread thread = parts[i];
             try {
                 //set state
                 thread.setFtpState(FTPDownloadThread.State.Merging);
                 context = thread.getContext();
 
                 //region copy
                 FileInputStream fis = new FileInputStream(context.localFile);
                 int copied = IOUtils.copy(fis, fos);
                 if (context.currentPieceLength != copied) {
                     System.err.println(String.format("Copied:%s, Should be:%s", copied, context.currentPieceLength));
                 }
                 fis.close();
                 //end region
 
                 //set final state
                 thread.setFtpState(FTPDownloadThread.State.Finished);
             } catch (Exception e) {
                 thread.setFtpState(FTPDownloadThread.State.Error);
                 throw e;
             }
         }
         try {
             fos.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
         if (mDeleteAfterMerge) {
             for (int i = 0, n = parts.length; i < n; i++) {
                 FTPDownloadThread thread = parts[i];
                 context = thread.getContext();
                 try {
                     context.localFile.delete();
                 } catch (Exception e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
 
         }
     }
 
     /**
      * Get subgroup, each soubgroup is one file separated to parts
      *
      * @return
      */
     private HashMap<Long, FTPDownloadThread[]> getSubGroups() {
         boolean someNotFinished = false;
         HashMap<Long, FTPDownloadThread[]> result = new HashMap<Long, FTPDownloadThread[]>();
 
         for (FTPDownloadThread ft : mData) {
             FTPContext c = ft.getContext();
             //ignore files which are not separated
             if (c.parts > 1) {
 
                 FTPDownloadThread[] arr = result.get(c.groupId);
                 if (arr == null) {
                     arr = new FTPDownloadThread[c.parts];
                     result.put(c.groupId, arr);
                 }
                 arr[c.part] = ft;
                 someNotFinished |= ft.getFtpState() != FTPDownloadThread.State.Downloaded;
             }
         }
         //this can happend for example if someone restarted download in the middle of checking
         if (someNotFinished) {
             return null;
         }
         return result;
     }
 
 
     public List<FTPDownloadThread> getData() {
         return Collections.unmodifiableList(mData);
     }
 
     public void setDeleteAfterMerge(boolean b) {
         mDeleteAfterMerge = b;
     }
 
     public boolean getDeleteAfterMeger() {
         return mDeleteAfterMerge;
     }
 }
