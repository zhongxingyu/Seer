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
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.TypedQuery;
 
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.collections.PredicateUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.queue.QueueManager.DateRange;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.UncPathnameMapper;
 
 
 /**
  * This variation on the DataGrabber gathers DatasetMetadata records all files
  * in a facility's directory tree, and compares them against the records in the DB.
  * The analyser also performs some basic integrity checks on the queue.
  * 
  * @author scrawley
  */
 public class Analyser extends AbstractFileGrabber {
     
     private static Logger LOG = LoggerFactory.getLogger(Analyser.class);
     
     public enum ProblemType {
         METADATA_MISSING, METADATA_SIZE,
         FILE_MISSING, FILE_SIZE, FILE_SIZE_2,
         FILE_HASH, FILE_HASH_2, IO_ERROR;
     }
     
     private static final Comparator<DatasetMetadata> ORDER_BY_BASE_PATH_AND_TIME =
             new Comparator<DatasetMetadata>() {
                 @Override
                 public int compare(DatasetMetadata o1, DatasetMetadata o2) {
                     int res = o1.getSourceFilePathnameBase().compareTo(
                             o2.getSourceFilePathnameBase());
                     if (res == 0) {
                         res = Long.compare(
                                 o1.getLastFileTimestamp().getTime(), 
                                 o2.getLastFileTimestamp().getTime());
                     }
                     return res;
                 }
     };
     
     private static final Comparator<DatasetMetadata> ORDER_BY_BASE_PATH_AND_TIME_AND_ID =
             new Comparator<DatasetMetadata>() {
                 @Override
                 public int compare(DatasetMetadata o1, DatasetMetadata o2) {
                     int res = o1.getSourceFilePathnameBase().compareTo(
                             o2.getSourceFilePathnameBase());
                     if (res == 0) {
                         res = Long.compare(
                                 o1.getLastFileTimestamp().getTime(), 
                                 o2.getLastFileTimestamp().getTime());
                     }
                     if (res == 0) {
                         res = o1.getId().compareTo(o2.getId());
                     }
                     return res;
                 }
     };
     
     private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
     private FacilityStatusManager fsm;
     private EntityManagerFactory emf;
     private UncPathnameMapper uncNameMapper;
     private List<Group> grouped;
     private Statistics all;
     private Statistics beforeLWM;
     private Statistics intertidal;
     private Statistics afterHWM;
     private Problems problems;
     private Statistics beforeQStart;
     private Statistics inQueue;
     private Statistics afterQEnd;
 
     private Date lwm;
     private Date hwm;
     private Date qStart;
     private Date qEnd;
     private Date fStart;
     private Date fEnd;
     private boolean checkHashes;
     
     
     public Analyser(Paul services, Facility facility) {
         super(services, facility);
         fsm = services.getFacilityStatusManager();
         uncNameMapper = services.getUncNameMapper();
         emf = services.getEntityManagerFactory();
     }
     
     public Analyser analyse(Date lwmTimestamp, Date hwmTimestamp, DateRange range, 
             boolean checkHashes) {
         this.lwm = lwmTimestamp;
         this.hwm = hwmTimestamp;
         if (range == null) {
             this.qStart = null;
             this.qEnd = null;
         } else {
             this.qStart = range.getFromDate();
             this.qEnd = range.getToDate();
         }
         this.checkHashes = checkHashes;
         LOG.info("Analysing queues and folders for " + getFacility().getFacilityName());
         SortedSet<DatasetMetadata> inFolder = buildInFolderMetadata();
         SortedSet<DatasetMetadata> inDatabase = buildInDatabaseMetadata();
         LOG.debug("Got " + inFolder.size() + " in folders and " + inDatabase.size() + " in database");
         LOG.info("Grouping datasets for " + getFacility().getFacilityName());
         grouped = groupDatasets(inFolder, inDatabase);
         LOG.debug("Got " + grouped.size() + " groups");
         LOG.info("Gathering statistics for " + getFacility().getFacilityName());
         determineFolderRange(inFolder);
         all = gatherStats(grouped, PredicateUtils.truePredicate());
         if (hwmTimestamp == null || lwmTimestamp == null) {
             beforeLWM = null;
             afterHWM = null;
             intertidal = null;
         } else {
             final long lwm = lwmTimestamp.getTime();
             beforeLWM = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() < lwm;
                 }
             });
             final long hwm = hwmTimestamp.getTime();
             afterHWM = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() > hwm;
                 }
             });
             intertidal = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     long ts = ((DatasetMetadata) metadata).getLastFileTimestamp().getTime();
                     return ts >= lwm && ts <= hwm;
                 }
             });
         }
         if (range == null) {
             afterQEnd = null;
             beforeQStart = null;
             inQueue = null;
         } else {
             final long qStart = this.qStart.getTime();
             beforeQStart = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() < qStart;
                 }
             });
             final long qEnd = this.qEnd.getTime();
             afterQEnd = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() > qEnd;
                 }
             });
             inQueue = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     long ts = ((DatasetMetadata) metadata).getLastFileTimestamp().getTime();
                     return ts >= qStart && ts <= qEnd;
                 }
             });
         }
         LOG.info("Performing queue entry integrity checks for " + getFacility().getFacilityName());
         problems = integrityCheck(grouped);
         return this;
     }
     
     private void determineFolderRange(SortedSet<DatasetMetadata> inFolder) {
         if (inFolder.isEmpty()) {
             fStart = null;
             fEnd = null;
         } else {
             Iterator<DatasetMetadata> it = inFolder.iterator();
             DatasetMetadata ds = it.next();
             fStart = fEnd = ds.getLastFileTimestamp();
             while (it.hasNext()) {
                 ds = it.next();
                 Date ts = ds.getLastFileTimestamp();
                 if (ts.getTime() < fStart.getTime()) {
                     fStart = ts;
                 } else if (ts.getTime() > fEnd.getTime()) {
                     fEnd = ts;
                 }
             }
         }
     }
 
     private Problems integrityCheck(List<Group> grouped) {
         List<Problem> problems = new ArrayList<Problem>();
         for (Group group : grouped) {
             // Check only the latest queue entry.  Older ones are not really 
             // relevant, and besides they typically have the "problem" that 
             // one or more captured component datafiles no longer matches the
             // in-folder dataset. (Which has typically been recaptured.)
             DatasetMetadata dataset = group.getLatestInDatabase();
             if (dataset == null) {
                 continue;
             }
             File adminFile = new File(dataset.getMetadataFilePathname());
             if (!adminFile.exists()) {
                 logProblem(dataset, null, ProblemType.METADATA_MISSING, problems, 
                         "Metadata file missing: " + adminFile);
             } else if (adminFile.length() == 0) {
                 logProblem(dataset, null, ProblemType.METADATA_SIZE, problems, 
                         "Metadata file empty: " + adminFile);
             }
             for (DatafileMetadata datafile : dataset.getDatafiles()) {
                 try {
                     String hash = checkHashes ? datafile.getDatafileHash() : null;
                     if (checkHashes) {
                         LOG.debug("stored hash - " + hash);
                     }
                     File file = new File(datafile.getCapturedFilePathname());
                     if (!file.exists()) {
                         logProblem(dataset, datafile, ProblemType.FILE_MISSING, problems, 
                                 "Data file missing: " + file);
                     } else if (file.length() != datafile.getFileSize()) {
                         logProblem(dataset, datafile, ProblemType.FILE_SIZE, problems,
                                 "Data file size mismatch: " + file + 
                                 ": admin metadata says " + datafile.getFileSize() + 
                                 " but actual captured file size is " + file.length());
                     } else if (hash != null && !hash.equals(HashUtils.fileHash(file))) {
                         logProblem(dataset, datafile, ProblemType.FILE_HASH, problems,
                                 "Data file hash mismatch between metadata and " + file);
                     } else if (checkHashes) {
                         LOG.debug("captured hash - " + HashUtils.fileHash(file));
                     }
                     File source = new File(datafile.getSourceFilePathname());
                     if (source.exists()) {
                         if (source.length() != datafile.getFileSize()) {
                             logProblem(dataset, datafile, ProblemType.FILE_SIZE_2, problems, 
                                     "Data file size mismatch: " + file + 
                                     ": original file size is " + source.length() + 
                                     " but admin metadata says " + datafile.getFileSize());
                         } else if (hash != null && !hash.equals(HashUtils.fileHash(source))) {
                             logProblem(dataset, datafile, ProblemType.FILE_HASH_2, problems,
                                     "Data file hash mismatch between metadata and " + source);
                         } else if (checkHashes) {
                             LOG.debug("source hash - " + HashUtils.fileHash(source));
                         }
                     }
                 } catch (IOException ex) {
                     LOG.error("Unexpected IOException while checking hashes", ex);
                     logProblem(dataset, datafile, ProblemType.IO_ERROR, problems,
                             "IO error while checking file hashes - see logs");
 
                 }
             }
         }
         LOG.info("Queue integrity check for '" + getFacility().getFacilityName() + 
                  "' found " + problems.size() + " problems (listed above)");
         return new Problems(problems);
     }
     
     private void logProblem(DatasetMetadata dataset, DatafileMetadata datafile, ProblemType type,
             List<Problem> list, String details) {
         LOG.info("Problem in dataset #" + dataset.getId() + ": " + details);
         list.add(new Problem(dataset, datafile, type, details));
     }
 
     private Statistics gatherStats(List<Group> grouped, Predicate predicate) {
         int datasetsInFolder = 0;
         int datasetsInDatabase = 0;
         int datasetsUnmatchedInFolder = 0;
         int groupsUnmatchedInDatabase = 0;
         int groupsWithDuplicatesInDatabase = 0;
         int groupsInDatabase = 0;
         for (Group group : grouped) {
             if (group.getInFolder() != null && predicate.evaluate(group.getInFolder())) {
                 datasetsInFolder++;
                 if (group.getAllInDatabase().size() == 0) {
                     datasetsUnmatchedInFolder++;
                 }
             }
             int inDatabase = 0;
             boolean matched = false;
             for (DatasetMetadata dataset : group.getAllInDatabase()) {
                 if (predicate.evaluate(dataset)) {
                     inDatabase++;
                     if (group.inFolder != null && matches(group.inFolder, dataset)) {
                         matched = true;
                     }
                 }
             }
             datasetsInDatabase += inDatabase;
             if (!matched && group.inFolder != null) {
                 groupsUnmatchedInDatabase++;
             }
             if (inDatabase > 1) {
                 groupsWithDuplicatesInDatabase++;
             }
             if (inDatabase > 0) {
                 groupsInDatabase++;
             }
         } 
         return new Statistics(datasetsInFolder, datasetsInDatabase, groupsInDatabase,
                 groupsWithDuplicatesInDatabase, datasetsUnmatchedInFolder, 
                 groupsUnmatchedInDatabase);
     }
     
     static boolean matches(DatasetMetadata d1, DatasetMetadata d2) {
         return d1.getSourceFilePathnameBase().equals(d2.getSourceFilePathnameBase()) &&
                 d1.getLastFileTimestamp().getTime() == d2.getLastFileTimestamp().getTime();
     }
     
     private List<Group> groupDatasets(
             Collection<DatasetMetadata> inFolder,
             Collection<DatasetMetadata> inDatabase) {
         ArrayList<Group> groups = createGroupsFromDatabase(inDatabase);
         groups = mergeGroupsFromFolder(groups, inFolder);
         return groups;
     }
 
     private ArrayList<Group> createGroupsFromDatabase(
             Collection<DatasetMetadata> inDatabase) {
         ArrayList<Group> groups = new ArrayList<Group>();
         Group group = null;
         for (DatasetMetadata dataset : inDatabase) {
             if (!intertidal(dataset.getCaptureTimestamp()) && 
                 !intertidal(dataset.getLastFileTimestamp())) {
                 continue;
             }
             String pathname = dataset.getSourceFilePathnameBase();
             if (group == null || !group.getBasePathname().equals(pathname)) {
                 group = new Group(pathname);
                 groups.add(group);
             }
             group.addInDatabase(dataset);
         }
         return groups;
     }
     
     private boolean intertidal(Date timestamp) {
         return (lwm == null || timestamp.getTime() >= lwm.getTime()) &&
                (hwm == null || timestamp.getTime() <= hwm.getTime());
     }
 
     private ArrayList<Group> mergeGroupsFromFolder(ArrayList<Group> groups,
             Collection<DatasetMetadata> inFolder) {
         ArrayList<Group> res = new ArrayList<Group>();
         Iterator<Group> git = groups.iterator();
         Iterator<DatasetMetadata> dit = inFolder.iterator();
         Group group = git.hasNext() ? git.next() : null;
         DatasetMetadata dataset = dit.hasNext() ? dit.next() : null;
         while (group != null || dataset != null) {
             if (dataset == null) {
                 res.add(group);
                 group = git.hasNext() ? git.next() : null;
             } else if (group == null) {
                 if (intertidal(dataset.getLastFileTimestamp())) {
                     Group newGroup = new Group(dataset.getSourceFilePathnameBase());
                     newGroup.setInFolder(dataset);
                     res.add(newGroup);
                 }
                 dataset = dit.hasNext() ? dit.next() : null;
             } else {
                 int cmp = group.getBasePathname().compareTo(dataset.getSourceFilePathnameBase());
                 if (cmp == 0) {
                     res.add(group);
                     group.setInFolder(dataset);
                     group = git.hasNext() ? git.next() : null;
                     dataset = dit.hasNext() ? dit.next() : null;
                 } else if (cmp < 0) {
                     res.add(group);
                     group = git.hasNext() ? git.next() : null;
                 } else {
                     if (intertidal(dataset.getLastFileTimestamp())) {
                         Group newGroup = new Group(dataset.getSourceFilePathnameBase());
                         newGroup.setInFolder(dataset);
                         res.add(newGroup);
                     }
                     dataset = dit.hasNext() ? dit.next() : null;
                 }
             }
         }
         return res;
     }
 
     private SortedSet<DatasetMetadata> buildInDatabaseMetadata() {
         TreeSet<DatasetMetadata> inDatabase = 
                 new TreeSet<DatasetMetadata>(ORDER_BY_BASE_PATH_AND_TIME_AND_ID);
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<DatasetMetadata> query = em.createQuery(
                    "from DatasetMetadata m left join fetch m.datafiles " +
                     "where m.facilityName = :name", 
                     DatasetMetadata.class);
             query.setParameter("name", getFacility().getFacilityName());
             for (DatasetMetadata ds : query.getResultList()) {
                 if (inDatabase.add(ds)) {
                     ds.getDatafiles().size();
                 }
             }
         } finally {
             em.close();
         }
         return inDatabase;
     }
 
     private SortedSet<DatasetMetadata> buildInFolderMetadata() {
         TreeSet<DatasetMetadata> inFolder = 
                 new TreeSet<DatasetMetadata>(ORDER_BY_BASE_PATH_AND_TIME);
         String folderName = getFacility().getFolderName();
         if (folderName == null) {
             return inFolder;
         }
         File localDir = uncNameMapper.mapUncPathname(folderName);
         if (localDir == null) {
             return inFolder;
         }
         fsm.getStatus(getFacility()).setLocalDirectory(localDir);
         analyseTree(localDir, Long.MIN_VALUE, Long.MAX_VALUE);
         for (Runnable runnable : queue) {
             WorkEntry entry = (WorkEntry) runnable;
             SessionDetails session = fsm.getSessionDetails(
                     getFacility(), entry.getTimestamp().getTime(), entry.getBaseFile());
             entry.pretendToGrabFiles();
             inFolder.add(entry.assembleDatasetMetadata(null, session, new File("")));
         }
         return inFolder;
     }
 
     @Override
     protected void enqueueWorkEntry(WorkEntry entry) {
         queue.add(entry);
     }
 
     public final List<Group> getGrouped() {
         return grouped;
     }
 
     public final Statistics getAll() {
         return all;
     }
 
     public final Statistics getBeforeLWM() {
         return beforeLWM;
     }
 
     public final Statistics getIntertidal() {
         return intertidal;
     }
 
     public final Statistics getAfterHWM() {
         return afterHWM;
     }
 
     public final Statistics getBeforeQStart() {
         return beforeQStart;
     }
 
     public final Statistics getInQueue() {
         return inQueue;
     }
 
     public final Statistics getAfterQEnd() {
         return afterQEnd;
     }
 
     public final Problems getProblems() {
         return problems;
     }
 
     public final Date getLWM() {
         return lwm;
     }
 
     public final Date getHWM() {
         return hwm;
     }
 
     public final Date getqStart() {
         return qStart;
     }
 
     public final Date getqEnd() {
         return qEnd;
     }
 
     public final Date getfStart() {
         return fStart;
     }
 
     public final Date getfEnd() {
         return fEnd;
     }
 
     public final void setProblems(Problems problems) {
         this.problems = problems;
     }
 
     @Override
     protected boolean isShutDown() {
         return false;
     }
 }
