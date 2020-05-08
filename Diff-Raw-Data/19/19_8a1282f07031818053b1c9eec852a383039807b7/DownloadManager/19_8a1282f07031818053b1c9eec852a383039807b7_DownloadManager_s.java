 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.downloadmanager;
 
 import Sirius.navigator.resource.PropertyManager;
 
 import org.apache.log4j.Logger;
 

 import java.io.File;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.event.EventListenerList;
 

 /**
  * The download manager manages all current downloads. New downloads are added to a collection, completed downloads are
  * removed. Erraneous downloads remain in the collection. The download manager observes all download objects for state
  * changed and informs the download manager panel via the DownloadListChangedListener interface.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class DownloadManager implements Observer /*, Configurable*/ {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final Logger LOG = Logger.getLogger(DownloadManager.class);
     private static DownloadManager instance = null;
 
     //~ Instance fields --------------------------------------------------------
 
     private boolean enabled;
     private File destinationDirectory;
     private LinkedList<Download> downloads = new LinkedList<Download>();
     private EventListenerList listeners = new EventListenerList();
     private int countDownloadsTotal = 0;
     private int countDownloadsRunning = 0;
     private int countDownloadsErraneous = 0;
     private int countDownloadsCompleted = 0;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DownloadManager object.
      */
     private DownloadManager() {
         if (PropertyManager.getManager().getDownloadDestination() != null) {
             destinationDirectory = PropertyManager.getManager().getDownloadDestination();
             if (!destinationDirectory.isDirectory() || !destinationDirectory.canWrite()) {
                 LOG.error("The download manager can't use the directory '" + destinationDirectory.getAbsolutePath()
                             + "'. The download manager will be disabled.");
                 enabled = false;
             } else {
                 enabled = true;
             }
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * It's a Singleton. There can only be one download manager.
      *
      * @return  The download manager.
      */
     public static DownloadManager instance() {
         if (instance == null) {
             instance = new DownloadManager();
         }
 
         return instance;
     }
 
     /**
      * This method is used to add new downloads to the download list.
      *
      * @param  downloads  wfss A collection of downloads to add.
      */
     public void add(final Collection<Download> downloads) {
         if ((downloads == null) || (downloads.size() <= 0)) {
             return;
         }
 
         this.downloads.addAll(0, downloads);
         countDownloadsTotal += downloads.size();
 
         for (final Download download : downloads) {
             download.addObserver(this);
             download.startDownload();
         }
 
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 downloads,
                 DownloadListChangedEvent.Action.ADDED));
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 downloads,
                 DownloadListChangedEvent.Action.CHANGED_COUNTERS));
     }
 
     /**
      * Removes oboslete downloads. Only running downloads aren't obsolote.
      */
     public synchronized void removeObsoleteDownloads() {
         final Collection<Download> downloadsRemoved = new LinkedList<Download>();
 
         for (final Download download : downloads) {
             if ((download.getStatus() == Download.COMPLETED) || (download.getStatus() == Download.ERROR)) {
                 downloadsRemoved.add(download);
             }
         }
 
         if (downloadsRemoved.size() > 0) {
             for (final Download download : downloadsRemoved) {
                 downloads.remove(download);
                 countDownloadsTotal--;
 
                 switch (download.getStatus()) {
                     case Download.ERROR: {
                         countDownloadsErraneous--;
                         break;
                     }
                     case Download.COMPLETED: {
                         countDownloadsCompleted--;
                         break;
                     }
                 }
             }
 
             notifyDownloadListChanged(new DownloadListChangedEvent(
                     this,
                     downloadsRemoved,
                     DownloadListChangedEvent.Action.REMOVED));
             notifyDownloadListChanged(new DownloadListChangedEvent(
                     this,
                     downloadsRemoved,
                     DownloadListChangedEvent.Action.CHANGED_COUNTERS));
         }
     }
 
     /**
      * Returns the current download list.
      *
      * @return  The current download list.
      */
     public Collection<Download> getDownloads() {
         return downloads;
     }
 
     /**
      * Returns the count of erraneous downloads.
      *
      * @return  The count of erraneous downloads.
      */
     public int getCountDownloadsErraneous() {
         return countDownloadsErraneous;
     }
 
     /**
      * Returns the count of running downloads.
      *
      * @return  The count of running downloads.
      */
     public int getCountDownloadsRunning() {
         return countDownloadsRunning;
     }
 
     /**
      * Returns the count of completed downloads.
      *
      * @return  The count of completed downloads.
      */
     public int getCountDownloadsCompleted() {
         return countDownloadsCompleted;
     }
 
     /**
      * Returns the total count of downloads.
      *
      * @return  The total count of downloads.
      */
     public int getCountDownloadsTotal() {
         return countDownloadsTotal;
     }
 
     /**
      * Returns the destination directory.
      *
      * @return  The destination directory for downloads.
      */
     public File getDestinationDirectory() {
         return destinationDirectory;
     }
 
     /**
      * Returns a flag which tells whether download manager is enabled or not.
      *
      * @return  The flag whether the download manager is enabled or not.
      */
     public boolean isEnabled() {
         return enabled;
     }
 
     @Override
     public synchronized void update(final Observable o, final Object arg) {
         if (!(o instanceof Download)) {
             return;
         }
 
         final Download download = (Download)o;
 
         switch (download.getStatus()) {
             case Download.COMPLETED: {
                 countDownloadsRunning--;
                 countDownloadsCompleted++;
                 break;
             }
             case Download.ERROR: {
                 countDownloadsRunning--;
                 countDownloadsErraneous++;
                 break;
             }
             case Download.RUNNING: {
                 countDownloadsRunning++;
                 break;
             }
         }
 
         notifyDownloadListChanged(new DownloadListChangedEvent(
                 this,
                 download,
                 DownloadListChangedEvent.Action.CHANGED_COUNTERS));
     }
 
     /**
      * Adds a new DownloadListChangedListener.
      *
      * @param  listener  The listener to add.
      */
     public void addDownloadListChangedListener(final DownloadListChangedListener listener) {
         listeners.add(DownloadListChangedListener.class, listener);
     }
 
     /**
      * Removes a DownloadListChangedListener.
      *
      * @param  listener  The listener to remove.
      */
     public void removeDownloadListChangedListener(final DownloadListChangedListener listener) {
         listeners.remove(DownloadListChangedListener.class, listener);
     }
 
     /**
      * Notifies all current DownloadListChangedListeners.
      *
      * @param  event  The event to notify about.
      */
     protected synchronized void notifyDownloadListChanged(final DownloadListChangedEvent event) {
         for (final DownloadListChangedListener listener : listeners.getListeners(DownloadListChangedListener.class)) {
             listener.downloadListChanged(event);
         }
     }
 }
