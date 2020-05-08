 /* $Id: FolderWatcher.java,v 1.5 2007/12/04 13:22:01 mke Exp $
  * $Revision: 1.5 $
  * $Date: 2007/12/04 13:22:01 $
  * $Author: mke $
  *
  * The SB Util Library.
  * Copyright (C) 2005-2007  The State and University Library of Denmark
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 /*
  * The State and University Library of Denmark
  * CVS:  $Id: FolderWatcher.java,v 1.5 2007/12/04 13:22:01 mke Exp $
  */
 package dk.statsbiblioteket.util.watch;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.log4j.Logger;
 import dk.statsbiblioteket.util.qa.QAInfo;
 import dk.statsbiblioteket.util.LineReader;
 
 /**
  * Watches folders for content changes, the notifies listeners about these
  * changes. A grace-period can be set: If one or more files are added, the
  * watcher waits a little bit, then checks the file sizes for the added files.
  * If the files have grown, the watcher waits again and so on.
  */
 // TODO: Wait until all files have been copied into the changing folder, before notifying
 @QAInfo(state=QAInfo.State.QA_NEEDED,
         level=QAInfo.Level.NORMAL,
         author="te")
 public class FolderWatcher extends Observable<FolderListener> implements
                                                               Runnable {
    private static Logger log = Logger.getLogger(FolderWatcher.class);
 
     private File watchedFolder;
     private List<File> oldContent;
     private int pollInterval;
     private boolean watch = true;
 
      // Waits 200 ms before notifying of additions
     private static final int DEFAULT_GRACE = 200;
     private int grace;
 
     /**
      * Create a watcher for the given watchedFolder. The watcher checks for
      * changes, fires an event if any changes happens, then sleeps for
      * pollInterval seconds before the next check. Note that this does not
      * guarantee that the folder will be checked every pollInterval seconds.
      * @param watchedFolder the folder to watch for changes.
      * @param pollInterval  how often, in seconds, to check for changes.
      * @throws IOException  if the content of the watched folder could not be
      *                      determined.
      */
     public FolderWatcher(File watchedFolder, int pollInterval) throws
                                                                IOException {
         this(watchedFolder, pollInterval, DEFAULT_GRACE);
     }
 
     /**
      * Create a watcher for the given watchedFolder. The watcher checks for
      * changes, fires an event if any deletions happens, then sleeps for
      * pollInterval seconds before the next check. If an addition happens,
      * the watcher collects the file sizes, waits {@link #grace} ms, then
      * fires an event if the sizes haven't changes, else waits again.
      * @param watchedFolder the folder to watch for changes.
      * @param pollInterval  how often, in seconds, to check for changes.
      * @param grace         the grace period for addition notifications.
      * @throws IOException  if the content of the watched folder could not be
      *                      determined.
      */
     public FolderWatcher(File watchedFolder, int pollInterval, int grace) throws
                                                                IOException {
         log.debug("Creating watcher for folder '" + watchedFolder
                   + "' with an interval of " + pollInterval + " seconds and a "
                   + "grace period of " + grace + "ms");
         this.watchedFolder = watchedFolder;
         this.pollInterval = pollInterval;
         this.grace = grace;
         oldContent = getContent();
         Thread thread = new Thread(this);
         thread.start();
     }
 
     /**
      * @return an alphabetically sorted list of the watched folder's content.
      *         If the watched folder does not exist, null is returned.
      * @throws IOException if an I/O error occured during folder access.
      */
     @QAInfo(comment="Check if sort on files is an alphanumeric sort")
     public List<File> getContent() throws IOException {
         if (!watchedFolder.exists()) {
             log.trace("Watched folder '" + watchedFolder + "' does not exist");
             return null;
         }
         log.trace("Returning content of folder '" + watchedFolder + "'");
         File[] content = watchedFolder.listFiles();
         Arrays.sort(content);
         return Arrays.asList(content);
     }
 
     public synchronized void run() {
         try {
             while (watch) {
                 try {
                     List<File> newContent = getContent();
                     if (oldContent == null && newContent != null) {
                         alert(newContent, FolderEvent.EventType.watchedCreated);
                     } else if (oldContent != null && newContent == null) {
                         alert(newContent, FolderEvent.EventType.watchedRemoved);
                     } else if (oldContent != null & newContent != null) {
                         List<File> added = new ArrayList<File>(newContent);
                         added.removeAll(oldContent);
                         if (added.size() > 0) {
                             alert(getStableContent(oldContent),
                                   FolderEvent.EventType.added);
                         }
                         List<File> removed = new ArrayList<File>(oldContent);
                         removed.removeAll(newContent);
                         if (removed.size() > 0) {
                             alert(removed, FolderEvent.EventType.removed);
                         }
                     }
                     oldContent = newContent;
                 } catch (IOException e) {
                     log.error("An I/O exception occured when polling for "
                               + "changes for folder '" + watchedFolder
                               + "'. Polling continues", e);
                 }
                 try {
                     wait(pollInterval * 1000);
                 } catch (InterruptedException e) {
                     log.warn("Sleeping " + pollInterval * 1000 + " seconds was "
                              + "interrupted", e);
                 }
             }
             log.debug("Stopping watching '" + watchedFolder + "'");
         } catch(Exception e) {
             log.error("an unexpected exception occured while watching folder '"
                       + watchedFolder + "'. Watcher is closing down", e);
         }
     }
 
     /**
      * Wait until the added files has stabilized.
      * @param oldContent the old content of the folder.
      * @return the added files when the list is deemed stable.
      * @throws java.io.IOException if the content could not be determined.
      */
     private List<File> getStableContent(List<File> oldContent) throws
                                                                IOException {
         List<File> added = new ArrayList<File>(getContent());
         added.removeAll(oldContent);
         if (grace == 0) {
             return added;
         }
         long lastSize = -1;
         long currentSize = addSizes(added);
         while (lastSize != currentSize) {
             try {
                 wait(grace);
             } catch (InterruptedException e) {
                 log.warn("Sleeping grace " + grace + "ms was interrupted", e);
             }
             added = new ArrayList<File>(getContent());
             added.removeAll(oldContent);
             lastSize = currentSize;
             currentSize = addSizes(added);
         }
         return added;
     }
 
     private long addSizes(List<File> files) {
         long total = 0;
         try {
             for (File file: files) {
                 if (file.isFile() && file.canRead()) {
                     total += file.length();
                 }
             }
         } catch (Exception e) {
             log.warn("Exception counting file sizes. Ignoring and continuing",
                      e);
         }
         return total;
     }
 
 
     private void alert(List<File> content, FolderEvent.EventType eventType) {
         log.trace("Alerting " + getListeners().size() + " listeners of event "
                   + eventType + " for folder '" + watchedFolder + "'");
         FolderEvent event = new FolderEvent(watchedFolder, content, eventType);
         for (FolderListener listener: getListeners()) {
             listener.folderChanged(event);
         }
     }
 
     /**
      * Stop watching the folder.
      */
     public void close() {
         log.trace("close called for folder '" + watchedFolder + "'");
         watch = false;
     }
 
     /* Getters */
     public File getWatchedFolder() {
         return watchedFolder;
     }
 
     public int getPollInterval() {
         return pollInterval;
     }
 
 
     public void addFolderListener(FolderListener listener) {
         addListener(listener);
     }
     public void removeFolderListener(FolderListener listener) {
         removeListener(listener);
     }
 
 }
