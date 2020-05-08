 package dk.statsbiblioteket.metadatarepository.xmltapes.taper;
 
 import dk.statsbiblioteket.metadatarepository.xmltapes.common.TapeUtils;
import dk.statsbiblioteket.util.FileAlreadyExistsException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created with IntelliJ IDEA.
  * User: abr
  * Date: 7/12/13
  * Time: 12:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DeferringTaper extends AbstractTaper{
 
 
 
 
     private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeferringTaper.class);
 
     private TimerTask task;
     private Timer timer;
 
     private boolean timerStopped = true;
     private boolean timerHaveRunAtLeastOnce = false;
 
     private boolean stopTimer = false;
 
     /**
      * The delay between runs of the taper thread
      */
     private long delay;
 
     /**
      * The maximum allowed age of a file before it will be taped
      */
     private long tapeDelay;
 
 
     public DeferringTaper(File tapingDir) {
         super(tapingDir);
         timer = new Timer();
     }
 
 
     @Override
     public void init() throws IOException {
         super.init();
         startTimer();
     }
 
 
 
     @Override
     public OutputStream createNew(URI id, long estimatedSize) throws IOException {
         throw new UnsupportedOperationException();
     }
 
     private void startTimer() {
         log.debug("Taping timer started, will run every {} milliseconds",delay);
         if (task != null) {
             task.cancel();
         }
 
         task = new TimerTask() {
 
             @Override
             public synchronized void run() {
                 timerStopped = false;
 
                 if (timerHaveRunAtLeastOnce && stopTimer) {
                     cancel();
                     timerStopped = true;
                     return;
                 }
                 try {
                     if (getDelegate() != null && parent != null){
                         saveAll();
                     }
                 } catch (Exception e) {
                     log.error("Failed to save objects", e);
                 } finally {
                     timerHaveRunAtLeastOnce = true;
                 }
             }
         };
         timerHaveRunAtLeastOnce = false;
         timer.schedule(task, 0, delay);
     }
 
     private void stopTimer() {
           stopTimer = true;
           //Close the timer, THEN close the delegates. The other order would cause problems.
           while (!timerStopped) {
               try {
                   Thread.sleep(100);
               } catch (InterruptedException e) {
 
               }
           }
       }
 
 
     /**
      * Get all the files in cache, lock them all, and tape all that are above a certain age.
      * @throws IOException
      */
     private void saveAll() throws IOException {
 
 
         //1. Tape all the files in the tapingDir (getCacheFiles)
         //2. lock parent for writes
         //3. Move all acceptable files from parent to taping dir
         //4. Tape all the files in the tapingDir (getCacheFiles)
 
 
 
 
         lockPool.lockForWriting();
         testClosed();
         try {
             //log.debug("Attempting to save all");
             //1
             tapeAll(getCacheFiles());
 
             //2
             parent.lockPool.lockForWriting();
 
             //3
             long now = System.currentTimeMillis();
             try {
                 List<File> cacheFiles = parent.getCacheFiles();
                 for (File cacheFile : cacheFiles) {
                     log.debug("Found file {} in caching folder",cacheFile.getName());
                     if (cacheFile.lastModified() + tapeDelay < now) {
                         log.debug("File {} is old enough, move to {}",cacheFile.getName(),getDeferredDir());
                         FileUtils.moveFileToDirectory(cacheFile, getDeferredDir(), true);
                     } else {
                         log.debug("File {} is to young, ignore",cacheFile);
                         continue;
                     }
                 }
             } finally {
                 parent.lockPool.unlockForWriting();
             }
 
 
             tapeAll(getCacheFiles());
         } finally {
             lockPool.unlockForWriting();
         }
     }
 
     private void tapeAll(List<File> cacheFiles) throws IOException {
         lockPool.lockForWriting();
         testClosed();
         try {
             for (File cacheFile : cacheFiles) {
                 if (cacheFile.getName().endsWith(TapeUtils.DELETED)){
                     tapeTheTapingFileDeletion(cacheFile);
                 } else {
                     tapeTheTapingFileAddition(cacheFile);
                 }
             }
         } finally {
             lockPool.unlockForWriting();
         }
     }
 
     @Override
     public  void remove(URI id) throws IOException {
         testClosed();
         //This is only called by cache.remove, and that method already locks the cache for writing
 
         while (true){
             lockPool.lockForWriting();
             parent.lockPool.lockForWriting();
             try {
                 log.debug("Removing {}",id);
 
                 File tapingFile = getDeferredFileDeleted(id);
                 if (tapingFile.exists()){
                     log.debug("{} is already scheduled for removal. Wait sleep for now and return later",tapingFile.getName());
                     //sleep and try again
                     throw new FileAlreadyExistsException(tapingFile.toString());
                 }
 
                 //Situations
                 //1. File is in cache
                 //2. File is not in cache, but in tapes
                 //3. File is not in cache or tapes
 
                 parent.lockPool.lockForWriting();
                 try { //Get file from cache
 
                     InputStream cacheFile = parent.getInputStream(id);
                     log.debug("File {} is in cache, so copy the current version to {}",id,tapingFile);
 
                     //write this content to the tapingFile
                     FileOutputStream output = new FileOutputStream(tapingFile);
                     try {
                         IOUtils.copyLarge(cacheFile, output);
                     } finally {
                         cacheFile.close();
                         output.close();
                     }
                     //It is now in taping, so remove it from the cache
                     FileUtils.deleteQuietly(parent.getDeferredFile(id));
 
                 } catch (FileNotFoundException e) { //file was not in cache
                     if (getDelegate().exist(id)){//but in tapes
                         log.debug("File {} was not in cache, but is in tapes. Just mark it for deletion.",id);
                         FileUtils.touch(tapingFile);
                         FileUtils.deleteQuietly(parent.getDeferredFile(id));
                     } else { //nowhere, so ignore this delete
                         log.debug("File {} was nowhere, so ignore this remove",id);
                     }
 
                 } finally {
                     parent.lockPool.unlockForWriting();
                 }
                 break;
 
 
             } catch (FileAlreadyExistsException e){
                 //ignore this, just go to sleep and try again
 
             }   finally {
                 parent.lockPool.unlockForWriting();
                 lockPool.unlockForWriting();
             }
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
 
             }
 
         }
     }
 
 
 
 
 
     public void setDelay(long delay) {
         this.delay = delay;
     }
 
 
     public void setTapeDelay(long tapeDelay) {
         this.tapeDelay = tapeDelay;
     }
 
 
     public long getDelay() {
         return delay;
     }
 
     public long getTapeDelay() {
         return tapeDelay;
     }
 
 
 
     @Override
     public void close() throws IOException {
 
         stopTimer();
         super.close();
     }
 
 
 
 }
