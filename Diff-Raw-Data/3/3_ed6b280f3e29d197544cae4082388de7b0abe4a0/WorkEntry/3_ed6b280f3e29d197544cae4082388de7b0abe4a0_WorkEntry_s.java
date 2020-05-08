 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.grabber;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.channels.FileLock;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.eccles.FacilitySession;
 import au.edu.uq.cmm.paul.GrabberFacilityConfig.FileArrivalMode;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.PaulException;
 import au.edu.uq.cmm.paul.queue.QueueFileException;
 import au.edu.uq.cmm.paul.queue.QueueFileManager;
 import au.edu.uq.cmm.paul.queue.QueueManager;
 import au.edu.uq.cmm.paul.status.DatafileTemplate;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatus;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.FileWatcherEvent;
 
 /**
  * This class represents a unit of work for the {@link FileGrabber} executor;
  * i.e. a dataset to be "grabbed".  This class does most of the work of grabbing
  * and creation of the queue entries.
  * 
  * @author scrawley
  */
 class WorkEntry implements Runnable {
     private static final Logger LOG = LoggerFactory.getLogger(WorkEntry.class);
     private static final long DEFAULT_GRABBER_TIMEOUT = 600000; // milliseconds == 10 minutes
     
     private final long grabberTimeout;
 
     private final FileGrabber fileGrabber;
     private final QueueManager queueManager;
     private final QueueFileManager fileManager;
     private final FacilityStatusManager statusManager;
     private final File baseFile;
     private final String instrumentBasePath;
     private final Map<File, GrabbedFile> files;
     private final Facility facility;
     private int settling;
     private Date timestamp;
     private long latestFileTimestamp = 0L;
     private final boolean holdDatasetsWithNoUser;
     private final boolean catchup;
     
     private Thread grabberThread;
     private boolean grabAborted;
     private boolean pretending;
     
     
     public WorkEntry(Paul services, FileWatcherEvent event, File baseFile) {
         this.facility = (Facility) event.getFacility();
         this.timestamp = new Date(event.getTimestamp());
         this.statusManager = services.getFacilityStatusManager();
         this.fileGrabber = statusManager.getStatus(facility).getFileGrabber();
         this.queueManager = services.getQueueManager();
         this.fileManager = queueManager.getFileManager();
         this.baseFile = baseFile;
         this.instrumentBasePath = mapToInstrumentPath(facility, baseFile);
         this.files = new ConcurrentHashMap<File, GrabbedFile>();
         this.holdDatasetsWithNoUser = 
                 services.getConfiguration().isHoldDatasetsWithNoUser();
         long timeout = services.getConfiguration().getGrabberTimeout();
         this.grabberTimeout = timeout == 0 ? DEFAULT_GRABBER_TIMEOUT : timeout;
         this.catchup = event.isCatchup();
         settling = facility.getFileSettlingTime();
         if (settling <= 0) {
             settling = FileGrabber.DEFAULT_FILE_SETTLING_TIME;
         }
         addEvent(event);
     }
     
     private String mapToInstrumentPath(Facility facility, File file) {
         String filePath = file.getAbsolutePath();
         FacilityStatus status = statusManager.getStatus(facility);
         String directoryPath = status.getLocalDirectory().getAbsolutePath();
         if (!filePath.startsWith(directoryPath)) {
             throw new PaulException("Bad path base: '" + filePath +
                     "' does not start with '" + directoryPath);
         }
         // This is a hack, but I can't use `File` to generate a Windows-style
         // pathname on Unix / Linux.
         filePath = filePath.substring(directoryPath.length());
         filePath = filePath.replaceAll("/", "\\\\");
         return facility.getDriveName() + ":" + filePath;
     }
 
     public void addEvent(FileWatcherEvent event) {
         File file = event.getFile();
         LOG.debug("Processing event for file " + file);
         synchronized (this) {
             if (grabberThread != null) {
                 LOG.warn("A late file event arrived for file " + file + ": interrupting the grabber");
                 grabberThread.interrupt();
             }
             boolean matched = false;
             List<DatafileTemplate> templates = facility.getDatafileTemplates();
             if (templates.isEmpty()) {
                 if (!files.containsKey(file)) {
                     files.put(file, new GrabbedFile(file, file, null));
                     LOG.debug("Added file " + file + " to map for grabbing");
                 } else {
                     LOG.debug("File " + file + " already in map for grabbing");
                 }
                 updateLatestFileTimestamp(file);
             } else {
                 for (DatafileTemplate template : templates) {
                     Pattern pattern = template.getCompiledFilePattern(
                             facility.isCaseInsensitive());
                     Matcher matcher = pattern.matcher(file.getAbsolutePath());
                     if (matcher.matches()) {
                         if (!files.containsKey(file)) {
                             files.put(file, new GrabbedFile(
                                     new File(matcher.group(1)), file, template));
                             LOG.debug("Added file " + file + " to map for grabbing");
                         } else {
                             LOG.debug("File " + file + " already in map for grabbing");
                         }
                         updateLatestFileTimestamp(file);
                         matched = true;
                         break;
                     }
                 }
                 if (!matched) {
                     LOG.debug("File " + file + " didn't match any template - ignoring");
                 }
             }
         }
     }
 
     private void updateLatestFileTimestamp(File file) {
         long timestamp = file.lastModified();
         if (timestamp > latestFileTimestamp) {
             latestFileTimestamp = timestamp;
         }
     }
 
     public long getLatestFileTimestamp() {
         return latestFileTimestamp;
     }
 
     public final Date getTimestamp() {
         return timestamp;
     }
     
     public final void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     public File getBaseFile() {
         return baseFile;
     }
 
     @Override
     public void run() {
         LOG.debug("Processing workEntry for " + baseFile);
         try {
             boolean alreadyRunning = false;
             synchronized (this) {
                 if (grabberThread == null) {
                     grabAborted = false;
                     grabberThread = Thread.currentThread();
                 } else {
                     alreadyRunning = true;
                 }
             }
             if (alreadyRunning || !datasetCompleted()) {
                 return;
             }
             grabFiles(false);
             statusManager.advanceHWMTimestamp(facility, timestamp);
         } catch (InterruptedException ex) {
             LOG.debug("Handling interrupt on workEntry thread", ex);
             grabAborted = true;
             try {
                 deleteGrabbedFiles();
             } catch (InterruptedException ex2) {
                 LOG.info("Interrupted while tidying up grabbed files");
             }
         } catch (Throwable ex) {
             LOG.error("unexpected exception", ex);
             return;
         } finally {
             synchronized (this) {
                 if (grabAborted) {
                     fileGrabber.enqueueWorkEntry(this);
                 } else {
                     fileGrabber.remove(baseFile);
                 }
                 grabberThread = null;
             }
         }
         LOG.debug("Finished processing workEntry for " + baseFile);
     }
 
     public DatasetMetadata grabFiles(boolean regrabbing) 
             throws InterruptedException, IOException, QueueFileException {
         // Perform pre-grab checks
         if (!regrabbing && !isGrabbable()) {
             LOG.debug("WorkEntry is not grabbable");
         }
         // Prepare for grabbing
         LOG.debug("WorkEntry.grabFiles has " + files.size() + " files to grab");
         FacilitySession session = statusManager.getSession(
                 facility.getFacilityName(), timestamp.getTime());
         // Optionally lock the files, then grab them.
         for (GrabbedFile file : files.values()) {
             if (Thread.interrupted()) {
                 throw new InterruptedException("Interrupted in grabFiles()");
             }
             try (FileInputStream is = new FileInputStream(file.getFile())) {
                 if (facility.isUseFileLocks()) {
                     LOG.debug("acquiring lock on " + file);
                     try (FileLock lock = is.getChannel().lock(0, Long.MAX_VALUE, true)) {
                         LOG.debug("locked " + file);
                         doGrabFile(file, is, regrabbing);
                     }
                     LOG.debug("unlocked " + file);
                 } else {
                     doGrabFile(file, is, regrabbing);
                 }
             } catch (IOException ex) {
                 LOG.error("Unexpected IO Error", ex);
             }
         }
         try {
             if (Thread.interrupted()) {
                 throw new InterruptedException("Interrupted in grabFiles()");
             }
             return saveMetadata(timestamp, session, regrabbing);
         } catch (JsonGenerationException ex) {
             throw new PaulException(ex);
         } 
     }
 
     private boolean isGrabbable() {
         if (facility.getFileArrivalMode() == FileArrivalMode.DIRECT) {
             return true;
         }
         if (facility.getFileArrivalMode() == FileArrivalMode.RSYNC) {
             // If the latest file modification date in the putative dataset is
             // before the LWM, we are not interested in it.
             long latest = Long.MAX_VALUE;
             for (GrabbedFile file: files.values()) {
                 long modified = file.getFile().lastModified();
                 if (modified > latest) {
                     latest = modified;
                 }
             }
            if (latest < facility.getStatus().getGrabberLWMTimestamp().getTime()) {
                 LOG.debug("WorkEntry falls before Grabber LWM");
                 return false;
             }
         }
         // Look for existing Datasets in the queue with the same baseFile.
         List<DatasetMetadata> possibles = 
                     queueManager.lookupDatasets(baseFile.toString());
         if (possibles.size() == 0) {
             return true;
         }
         // Trawl through the existing Datasets, knocking out any files in the 
         // grab list that are already in a Dataset
         for (DatasetMetadata dm: possibles) {
             for (DatafileMetadata df: dm.getDatafiles()) {
                 for (GrabbedFile file: files.values()) {
                     if (file.getFile().equals(df.getSourceFilePathname())) {
                         files.remove(file.getFile());
                         if (files.isEmpty()) {
                             return false;
                         }
                         break;
                     }
                 }
             }
         }
         return true;
     }
 
     /**
      * This method is used by the CatchupAnalyser to decorate the
      * entry with sufficient information that we can generate a
      * passable metadata record for it.
      */
     public void pretendToGrabFiles() {
         pretending = true;
         for (GrabbedFile file : files.values()) {
             Date now = new Date();
             Date fileTimestamp = new Date(file.getFile().lastModified());
             file.setCopiedFile(file.getFile());
             file.setFileTimestamp(fileTimestamp);
             file.setCopyTimestamp(now);
         }
     }
     
     /**
      * This method is called on an interrupt to tidy up any data files that were
      * captured or being captured.  If we are 'pretending', don't do anything because
      * the "pretend copied" files are actually the precious original files!
      * @throws InterruptedException 
      */
     private void deleteGrabbedFiles() throws InterruptedException {
         if (!pretending) {
             for (GrabbedFile file : files.values()) {
                 File copied = file.getCopiedFile();
                 if (copied != null) {
                     try {
                         fileManager.removeFile(copied);
                     } catch (QueueFileException ex) {
                         LOG.warn("Problem while tidying up grabbed files", ex);
                     }
                 }
             }
         }
     }
 
     private boolean datasetCompleted() throws InterruptedException {
         // Wait until the dataset is completed.
         boolean incomplete = true;
         long limit = grabberTimeout < 0 ? Long.MAX_VALUE :
             System.currentTimeMillis() + grabberTimeout;
         do {
             if (!catchup) {
                 // Wait for the file modification interrupts stop arriving ... plus
                 // the settling time.
                 while (true) {
                     try {
                         Thread.sleep(settling);
                         break;
                     } catch (InterruptedException ex) {
                         if (fileGrabber.isShutDown()) {
                             throw ex;
                         }
                     }
                 }
                 // We don't need to abort the grab because we haven't started yet.
                 grabAborted = false;
             }
             // Check that the dataset's non-optional files are all present,
             // and that they meet the minimum size requirements.
             incomplete = false;
             for (DatafileTemplate template : facility.getDatafileTemplates()) {
                 if (template.isOptional()) {
                     continue;
                 }
                 Pattern pattern = template.getCompiledFilePattern(
                         facility.isCaseInsensitive());
                 boolean satisfied = false;
                 for (File file : files.keySet()) {
                     Matcher matcher = pattern.matcher(file.getAbsolutePath());
                     if (matcher.matches()) {
                         if (file.length() >= template.getMinimumSize()) {
                             satisfied = true;
                         } else {
                             LOG.debug("Datafile " + file + " isn't big enough yet");
                         }
                         break;
                     }
                 }
                 if (!satisfied) {
                     LOG.debug("Datafile for template " + template.getFilePattern() + " isn't ready");
                     incomplete = true;
                     break;
                 }
             } 
         } while (incomplete && !catchup && limit > System.currentTimeMillis());
         if (incomplete) {
             LOG.info("Dataset for baseFile " + baseFile + 
                     (catchup ? " is incomplete" : " did not complete within timeout") +
                     ".  Dropping it.");
             return false;
         }
         
         // Prune any files that don't meet their minimum size requirement.
         for (Iterator<Entry<File, GrabbedFile>> it = files.entrySet().iterator();
                 it.hasNext(); /* */) {
             Entry<File, GrabbedFile> entry = it.next();
             long length = entry.getKey().length();
             if (length < entry.getValue().getTemplate().getMinimumSize()) {
                 LOG.info("Dropping datafile " + entry.getKey() + " with size " + length);
                 it.remove();
             }
         }
         
         // Avoid creating an empty Dataset (containing just an admin metadata file)
         if (files.isEmpty()) {
             LOG.info("Dropping empty dataset for baseFile " + baseFile);
             return false;
         }
         return true;
     }
 
     private void doGrabFile(GrabbedFile file, FileInputStream is, boolean regrabbing) 
             throws InterruptedException, IOException, QueueFileException {
         LOG.debug("Start file grabbing for " + file.getFile());
         Date now = new Date();
         Date fileTimestamp = new Date(file.getFile().lastModified());
         String suffix = (file.getTemplate() == null) ?
                 ".data" : file.getTemplate().getSuffix();
         File copiedFile = fileManager.enqueueFile(file.getFile(), suffix, regrabbing);
         file.setCopiedFile(copiedFile);
         file.setFileTimestamp(fileTimestamp);
         file.setCopyTimestamp(now);
         LOG.debug("Done grabbing "+ file.getFile() + " -> " + copiedFile);
     }
 
     private DatasetMetadata saveMetadata(Date now, FacilitySession session, boolean regrabbing)
             throws IOException, JsonGenerationException, QueueFileException, InterruptedException {
         File metadataFile = fileManager.generateUniqueFile(".admin", regrabbing);
         DatasetMetadata dataset = assembleDatasetMetadata(now, session, metadataFile);
         for (DatafileMetadata d : dataset.getDatafiles()) {
             d.updateDatafileHash();
         }
         dataset.updateDatasetHash();
         if (!regrabbing) {
             queueManager.addEntry(dataset, false);
         }
         return dataset;
     }
 
     public DatasetMetadata assembleDatasetMetadata(
             Date now, FacilitySession session, File metadataFile) {
         if (session == null && !holdDatasetsWithNoUser) {
             session = FacilitySession.makeDummySession(facility.getFacilityName(), now);
         }
         String userName = session == null ? null : session.getUserName();
         String account = session == null ? null : session.getAccount();
         String sessionUuid = session == null ? null : session.getSessionUuid();
         String emailAddress = session == null ? null : session.getEmailAddress();
         Date loginTime = session == null ? null : session.getLoginTime();
         List<DatafileMetadata> list = new ArrayList<DatafileMetadata>(files.size());
         for (GrabbedFile g : files.values()) {
             String mimeType = (g.getTemplate() == null) ? 
                     "application/octet-stream" : g.getTemplate().getMimeType();
             DatafileMetadata d = new DatafileMetadata(
                     g.getFile().getAbsolutePath(), 
                     mapToInstrumentPath(facility, g.getFile()),
                     g.getCopiedFile().getAbsolutePath(), 
                     g.getFileTimestamp(), g.getCopyTimestamp(), mimeType,
                     g.getCopiedFile().length(), null);
             list.add(d);
         }
         DatasetMetadata dataset = new DatasetMetadata(
                 baseFile.getAbsolutePath(), 
                 instrumentBasePath, metadataFile.getAbsolutePath(), 
                 userName, facility.getFacilityName(), facility.getId(), 
                 account, emailAddress, now, sessionUuid, loginTime, list);
         return dataset;
     }
 
     public void commitRegrabbedDataset(DatasetMetadata dataset) 
             throws IOException, QueueFileException, InterruptedException {
         DatasetMetadata originalDataset = queueManager.fetchDataset(dataset.getId());
         dataset.setMetadataFilePathname(originalDataset.getMetadataFilePathname());
         // Delete the original dataset's captured files
         for (DatafileMetadata of : originalDataset.getDatafiles()) {
             new File(of.getCapturedFilePathname()).delete();
         }
         // Rename the new dataset's captured files
         for (DatafileMetadata f : dataset.getDatafiles()) {
             File file = fileManager.renameGrabbedDatafile(new File(f.getCapturedFilePathname()));
             f.setCapturedFilePathname(file.toString());
         }
         // This will save the updated dataset metadata to the database and file system.
         queueManager.addEntry(dataset, true);
     }
     
     
 }
