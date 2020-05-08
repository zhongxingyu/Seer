 package au.edu.uq.cmm.paul.grabber;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 import org.apache.log4j.Logger;
 
 import au.edu.uq.cmm.aclslib.config.DatafileTemplateConfig;
 import au.edu.uq.cmm.aclslib.config.FacilityConfig;
 import au.edu.uq.cmm.aclslib.service.CompositeServiceBase;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.FileWatcher;
 import au.edu.uq.cmm.paul.watcher.FileWatcherEvent;
 import au.edu.uq.cmm.paul.watcher.FileWatcherEventListener;
 
 /**
  * The FileGrabber service is registered as a listener for FileWatcher events.
  * The first event for a file generates WorkEntry object that is enqueued.  
  * Subsequent events are added to the WorkEntry.
  * <p>
  * The FileGrabber's thread pulls WorkEntry objects from the queue and processed 
  * them as follows:
  * <ul>
  * <li>It waits until the file events stop arriving.</li>
  * <li>It optionally locks the file.</li>
  * <li>It captures the user / account from the ACLS status manager.</li>
  * <li>It copies the file to another directory.</li>
  * <li>It records the file's administrative metadata.</li>
  * <li>It releases the lock.</li>
  * </ul>
  * <p>
  * The functionality is mostly implemented in the {@link WorkEntry} class.
  * 
  * @author scrawley
  */
 public class FileGrabber extends CompositeServiceBase 
         implements FileWatcherEventListener {
     static final Logger LOG = Logger.getLogger(FileGrabber.class);
     static final int DEFAULT_FILE_SETTLING_TIME = 2000;  // 2 seconds
     
     private final BlockingQueue<Runnable> work = new LinkedBlockingDeque<Runnable>();
     private final HashMap<File, WorkEntry> workMap = new HashMap<File, WorkEntry>();
     private final FacilityStatusManager statusManager;
    private File safeDirectory = new File("/tmp/safe");
     private ExecutorService executor;
     private final EntityManagerFactory entityManagerFactory;
     private final Paul services;
     
     public FileGrabber(Paul services, FileWatcher fileWatcher) {
         this.services = services;
         fileWatcher.addListener(this);
         this.statusManager = services.getFacilitySessionManager();
         if (!safeDirectory.exists() || !safeDirectory.isDirectory()) {
            throw new PaulException("The grabber's safe directory doesn't exist");
         }
         this.entityManagerFactory = services.getEntityManagerFactory();
     }
 
     public File getSafeDirectory() {
         return safeDirectory;
     }
 
     public FacilityStatusManager getStatusManager() {
         return statusManager;
     }
 
     public void remove(File file) {
         workMap.remove(file);
     }
 
     @Override
     protected void doShutdown() throws InterruptedException {
         executor.shutdown();
         if (executor.awaitTermination(20, TimeUnit.SECONDS)) {
             LOG.info("FileGrabber's executor shut down");
         } else {
             LOG.warn("FileGrabber's executor didn't shut down cleanly");
         }
     }
 
     @Override
     protected void doStartup() {
         executor = new ThreadPoolExecutor(5, 5, 999, TimeUnit.SECONDS, work);
     }
 
     @Override
     public void eventOccurred(FileWatcherEvent event) {
         File file = event.getFile();
         FacilityConfig facility = event.getFacility();
         LOG.debug("FileWatcherEvent received : " + 
                 facility.getFacilityName() + "," + file + "," + event.isCreate());
         File baseFile = file;
         for (DatafileTemplateConfig datafile : facility.getDatafileTemplates()) {
             Pattern pattern = Pattern.compile(datafile.getFilePattern());
             Matcher matcher = pattern.matcher(file.getAbsolutePath());
             if (matcher.matches()) {
                 baseFile = new File(matcher.group(1));
                 break;
             }
         }
         synchronized (this) {
            WorkEntry workEntry = workMap.get(baseFile);
            if (workEntry == null) {
                workEntry = new WorkEntry(services, event, baseFile);
                workMap.put(baseFile, workEntry);
                executor.execute(workEntry);
                LOG.debug("Added a workEntry");
            } else {
                workEntry.addEvent(event);
            }
         }
     }
 
     public EntityManager getEntityManager() {
         return entityManagerFactory.createEntityManager();
     }
 }
