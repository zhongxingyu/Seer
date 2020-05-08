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
 
 import au.edu.uq.cmm.eccles.FacilitySession;
 import au.edu.uq.cmm.paul.Paul;
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
     
     private static Logger LOG = LoggerFactory.getLogger(AbstractFileGrabber.class);
     
     public static class Statistics {
         private final int datasetsInFolder;
         private final int datasetsInDatabase;
         private int groupsWithDuplicatesInDatabase;
         private int datasetsUnmatchedInFolder;
         private int groupsUnmatchedInDatabase;
 
         public Statistics(int datasetsInFolder, int datasetsInDatabase, 
                 int groupsWithDuplicatesInDatabase, int datasetsUnmatchedInFolder, 
                 int groupsUnmatchedInDatabase) {
             super();
             this.datasetsInFolder = datasetsInFolder;
             this.datasetsInDatabase = datasetsInDatabase;
             this.groupsWithDuplicatesInDatabase = groupsWithDuplicatesInDatabase;
             this.datasetsUnmatchedInFolder = datasetsUnmatchedInFolder;
             this.groupsUnmatchedInDatabase = groupsUnmatchedInDatabase;
         }
 
         public final int getDatasetsInFolder() {
             return datasetsInFolder;
         }
 
         public final int getDatasetsInDatabase() {
             return datasetsInDatabase;
         }
 
         public final int getGroupsWithDuplicatesInDatabase() {
             return groupsWithDuplicatesInDatabase;
         }
 
         public final int getDatasetsUnmatchedInFolder() {
             return datasetsUnmatchedInFolder;
         }
 
         public final int getGroupsUnmatchedInDatabase() {
             return groupsUnmatchedInDatabase;
         }
     }
     
     public enum ProblemType {
         METADATA_MISSING, METADATA_SIZE,
         FILE_MISSING, FILE_SIZE, FILE_SIZE_2,
         FILE_HASH, FILE_HASH_2, IO_ERROR;
     }
     
     public static class Problem {
         private final DatasetMetadata dataset;
         private final DatafileMetadata datafile;
         private final String details;
         private final ProblemType type;
         
         public Problem(DatasetMetadata dataset, DatafileMetadata datafile, 
                 ProblemType type, String details) {
             super();
             this.dataset = dataset;
             this.datafile = datafile;
             this.details = details;
             this.type = type;
         }
         
         public final DatasetMetadata getDataset() {
             return dataset;
         }
         
         public final DatafileMetadata getDatafile() {
             return datafile;
         }
         
         public final String getDetails() {
             return details;
         }
 
         public final ProblemType getType() {
             return type;
         }
     }
     
     public static class Problems {
         private final List<Problem> problems;
 
         public Problems(List<Problem> problem) {
             this.problems = problem;
         }
 
         public int getNosProblems() {
             return problems.size();
         }
 
         public final int getIoError() {
             return count(ProblemType.IO_ERROR);
         }
 
         public final int getFileSize2() {
             return count(ProblemType.FILE_SIZE_2);
         }
 
         public final int getFileSize() {
             return count(ProblemType.FILE_SIZE);
         }
 
         public final int getFileHash2() {
             return count(ProblemType.FILE_HASH_2);
         }
 
         public final int getFileHash() {
             return count(ProblemType.FILE_HASH);
         }
 
         public final int getFileMissing() {
             return count(ProblemType.FILE_MISSING);
         }
 
         public final int getMetadataSize() {
             return count(ProblemType.METADATA_SIZE);
         }
 
         public final int getMetadataMissing() {
             return count(ProblemType.METADATA_MISSING);
         }
 
         private int count(ProblemType type) {
             int count = 0;
             for (Problem problem : problems) {
                 if (problem.getType() == type) {
                     count++;
                 }
             }
             return count;
         }
 
         public final List<Problem> getProblems() {
             return problems;
         }
     }
     
     public static class Group implements Comparable<Group> {
         private final String basePathname;
         private DatasetMetadata inFolder;
         private List<DatasetMetadata> allInDatabase = new ArrayList<DatasetMetadata>();
         
         public Group(String basePathname) {
             super();
             this.basePathname = basePathname;
         }
 
         public final String getBasePathname() {
             return basePathname;
         }
 
         public final DatasetMetadata getInFolder() {
             return inFolder;
         }
 
         public final List<DatasetMetadata> getAllInDatabase() {
             return allInDatabase;
         }
         
         public final List<DecoratedDatasetMetadata> getAllDecorated() {
             List<DecoratedDatasetMetadata> res = 
                     new ArrayList<DecoratedDatasetMetadata>(allInDatabase.size() + 1);
             if (inFolder != null) {
                 res.add(new DecoratedDatasetMetadata(inFolder, inFolder));
             }
             for (DatasetMetadata dataset : allInDatabase) {
                 res.add(new DecoratedDatasetMetadata(dataset, inFolder));
             }
             return res;
         }
         
         public final boolean isUnmatchedInDatabase() {
             if (inFolder == null) {
                 return true;
             }
             for (DatasetMetadata dataset : allInDatabase) {
                 if (!matches(dataset, inFolder)) {
                     return true;
                 }
             }
             return false;
         }
 
         public final boolean isDuplicatesInDatabase() {
             if (inFolder == null) {
                 return false;
             }
             int count = 0;
             for (DatasetMetadata dataset : allInDatabase) {
                 if (matches(dataset, inFolder)) {
                     count++;
                 }
             }
             return count > 1;
         }
         
         public final void setInFolder(DatasetMetadata inFolder) {
             this.inFolder = inFolder;
         }
         
         public final void addInDatabase(DatasetMetadata inDatabase) {
             this.allInDatabase.add(inDatabase);
         }
 
         @Override
         public int compareTo(Group o) {
             return basePathname.compareTo(o.getBasePathname());
         }
     }
     
     public static class DecoratedDatasetMetadata extends DatasetMetadata {
         private final DatasetMetadata inFolder;
         private final boolean isInFolder;
 
         public DecoratedDatasetMetadata(DatasetMetadata dataset, DatasetMetadata inFolder) {
             super(dataset.getSourceFilePathnameBase(), 
                     dataset.getFacilityFilePathnameBase(), 
                     dataset.getMetadataFilePathname(),
                     dataset.getUserName(), 
                     dataset.getFacilityName(), 
                     dataset.getFacilityId(), 
                     dataset.getAccountName(), 
                     dataset.getEmailAddress(),
                     dataset.getCaptureTimestamp(), 
                     dataset.getSessionUuid(), 
                     dataset.getSessionStartTimestamp(), 
                     dataset.getDatafiles());
             this.inFolder = inFolder;
             this.isInFolder = dataset == inFolder;
             this.setId(dataset.getId());
         }
         
         public boolean isInFolder() {
             return isInFolder;
         }
         
         public boolean isMatched() {
             return inFolder != null && matches(inFolder, this);
         }
         
         public boolean isUnmatched() {
             return inFolder == null || !matches(inFolder, this);
         }
     }
     
     private static final Comparator<DatasetMetadata> ORDER_BY_BASE_PATH_AND_TIME =
             new Comparator<DatasetMetadata>() {
                 @Override
                 public int compare(DatasetMetadata o1, DatasetMetadata o2) {
                     int res = o1.getFacilityFilePathnameBase().compareTo(
                             o2.getFacilityFilePathnameBase());
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
                     int res = o1.getFacilityFilePathnameBase().compareTo(
                             o2.getFacilityFilePathnameBase());
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
     private Statistics afterLWM;
     private Statistics beforeHWM;
     private Statistics afterHWM;
     private Problems problems;
     private Statistics beforeQEnd;
     private Statistics afterQEnd;
 
     private Date lwm;
     private Date hwm;
     private Date qEnd;
     
     
     public Analyser(Paul services, Facility facility) {
         super(services, facility);
         fsm = services.getFacilityStatusManager();
         uncNameMapper = services.getUncNameMapper();
         emf = services.getEntityManagerFactory();
     }
     
     public Analyser analyse(Date lwmTimestamp, Date hwmTimestamp, Date queueEndTimestamp) {
         this.lwm = lwmTimestamp;
         this.hwm = hwmTimestamp;
         this.qEnd = queueEndTimestamp;
         LOG.info("Analysing queues and folders for " + getFacility().getFacilityName());
         SortedSet<DatasetMetadata> inFolder = buildInFolderMetadata();
         SortedSet<DatasetMetadata> inDatabase = buildInDatabaseMetadata();
         LOG.info("Grouping datasets for " + getFacility().getFacilityName());
         grouped = groupDatasets(inFolder, inDatabase);
         LOG.info("Gathering statistics for " + getFacility().getFacilityName());
         all = gatherStats(grouped, PredicateUtils.truePredicate());
         if (lwmTimestamp == null) {
             beforeLWM = null;
             afterLWM = null;
         } else {
             final long lwm = lwmTimestamp.getTime();
             beforeLWM = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() <= lwm;
                 }
             });
             afterLWM = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() > lwm;
                 }
             });
         }
         if (hwmTimestamp == null) {
             beforeHWM = null;
             afterHWM = null;
         } else {
             final long hwm = hwmTimestamp.getTime();
             beforeHWM = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() <= hwm;
                 }
             });
             afterHWM = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() > hwm;
                 }
             });
         }
         if (queueEndTimestamp == null) {
             beforeQEnd = null;
             afterQEnd = null;
         } else {
             final long qEnd = queueEndTimestamp.getTime();
             beforeQEnd = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() <= qEnd;
                 }
             });
             afterQEnd = gatherStats(grouped, new Predicate() {
                 public boolean evaluate(Object metadata) {
                     return ((DatasetMetadata) metadata).getLastFileTimestamp().getTime() > qEnd;
                 }
             });
         }
         LOG.info("Performing queue entry integrity checks for " + getFacility().getFacilityName());
         problems = integrityCheck(inDatabase);
         return this;
     }
     
     private Problems integrityCheck(SortedSet<DatasetMetadata> inDatabase) {
         List<Problem> problems = new ArrayList<Problem>();
         for (DatasetMetadata dataset : inDatabase) {
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
                     LOG.error("stored hash - " + datafile.getDatafileHash());
                     File file = new File(datafile.getCapturedFilePathname());
                     if (!file.exists()) {
                         logProblem(dataset, datafile, ProblemType.FILE_MISSING, problems, 
                                 "Data file missing: " + file);
                     } else if (file.length() != datafile.getFileSize()) {
                         logProblem(dataset, datafile, ProblemType.FILE_SIZE, problems,
                                 "Data file size mismatch: " + file + 
                                 ": admin metadata says " + datafile.getFileSize() + 
                                 " but actual captured file size is " + file.length());
                     } else if (!datafile.getDatafileHash().equals(HashUtils.fileHash(file))) {
                         logProblem(dataset, datafile, ProblemType.FILE_HASH, problems,
                                 "Data file hash mismatch between metadata and " + file);
                     } else {
                         LOG.error("captured hash - " + HashUtils.fileHash(file));
                     }
                     File source = new File(datafile.getSourceFilePathname());
                     if (source.exists()) {
                         if (source.length() != file.length()) {
                             logProblem(dataset, datafile, ProblemType.FILE_SIZE_2, problems, 
                                     "Data file size mismatch: " + file + 
                                     ": original file size is " + source.length() + 
                                     " but actual captured file size is " + file.length());
                         } else if (!datafile.getDatafileHash().equals(HashUtils.fileHash(source))) {
                             logProblem(dataset, datafile, ProblemType.FILE_HASH_2, problems,
                                     "Data file hash mismatch between metadata and " + source);
                         } else {
                             LOG.error("source hash - " + HashUtils.fileHash(source));
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
         for (Group group : grouped) {
             if (group.getInFolder() != null && predicate.evaluate(group.getInFolder())) {
                 datasetsInFolder++;
                 if (group.getAllInDatabase().size() == 0) {
                     datasetsUnmatchedInFolder++;
                 }
             }
             int inDatabase = 0;
             for (DatasetMetadata dataset : group.getAllInDatabase()) {
                 if (predicate.evaluate(dataset)) {
                     inDatabase++;
                 }
             }
             datasetsInDatabase += inDatabase;
             if (inDatabase > 1) {
                 groupsWithDuplicatesInDatabase++;
             }
             for (DatasetMetadata dataset : group.getAllInDatabase()) {
                 if (predicate.evaluate(dataset)) {
                     if (group.inFolder == null || !matches(group.inFolder, dataset))
                     groupsUnmatchedInDatabase++;
                 }
             }
         }
         return new Statistics(datasetsInFolder, datasetsInDatabase, 
                 groupsWithDuplicatesInDatabase, datasetsUnmatchedInFolder, groupsUnmatchedInDatabase);
     }
     
     private static boolean matches(DatasetMetadata d1, DatasetMetadata d2) {
         return d1.getFacilityFilePathnameBase().equals(d2.getFacilityFilePathnameBase()) &&
                 d1.getLastFileTimestamp().getTime() == d2.getLastFileTimestamp().getTime();
     }
     
     private List<Group> groupDatasets(
             Collection<DatasetMetadata> inFolder,
             Collection<DatasetMetadata> inDatabase) {
         ArrayList<Group> grouped = createGroupsFromDatabase(inDatabase);
         mergeGroupsFromFolder(grouped, inFolder);
         return grouped;
     }
 
     private ArrayList<Group> createGroupsFromDatabase(
             Collection<DatasetMetadata> inDatabase) {
         ArrayList<Group> grouped = new ArrayList<Group>();
         
         Group group = null;
         for (DatasetMetadata dataset : inDatabase) {
             String pathname = dataset.getFacilityFilePathnameBase();
             if (group == null || !group.getBasePathname().equals(pathname)) {
                 group = new Group(pathname);
                 grouped.add(group);
             }
             group.addInDatabase(dataset);
         }
         return grouped;
     }
     
     private ArrayList<Group> mergeGroupsFromFolder(ArrayList<Group> grouped,
             Collection<DatasetMetadata> inFolder) {
         ArrayList<Group> res = new ArrayList<Group>();
         Iterator<Group> git = grouped.iterator();
         Iterator<DatasetMetadata> dit = inFolder.iterator();
         Group group = git.hasNext() ? git.next() : null;
         DatasetMetadata dataset = dit.hasNext() ? dit.next() : null;
         while (group != null || dataset != null) {
             if (dataset == null) {
                 res.add(group);
                 group = git.hasNext() ? git.next() : null;
             } else if (group == null) {
                 Group newGroup = new Group(dataset.getFacilityFilePathnameBase());
                 newGroup.setInFolder(dataset);
                 res.add(newGroup);
                 dataset = dit.hasNext() ? dit.next() : null;
             } else {
                 int cmp = group.getBasePathname().compareTo(dataset.getFacilityFilePathnameBase());
                 if (cmp == 0) {
                     res.add(group);
                     group.setInFolder(dataset);
                     group = git.hasNext() ? git.next() : null;
                     dataset = dit.hasNext() ? dit.next() : null;
                 } else if (cmp < 0) {
                     res.add(group);
                    group = git.hasNext() ? git.next() : null;
                 } else {
                     Group newGroup = new Group(dataset.getFacilityFilePathnameBase());
                     newGroup.setInFolder(dataset);
                     res.add(newGroup);
                    dataset = dit.hasNext() ? dit.next() : null;
                 }
             }
         }
         return res;
     }
 
     private SortedSet<DatasetMetadata> buildInDatabaseMetadata() {
         TreeSet<DatasetMetadata> inDatabase =  new TreeSet<DatasetMetadata>(ORDER_BY_BASE_PATH_AND_TIME_AND_ID);
         EntityManager em = emf.createEntityManager();
         try {
             TypedQuery<DatasetMetadata> query = em.createQuery(
                     "from DatasetMetadata m where m.facilityName = :name", 
                     DatasetMetadata.class);
             query.setParameter("name", getFacility().getFacilityName());
             inDatabase.addAll(query.getResultList());
         } finally {
             em.close();
         }
         return inDatabase;
     }
 
     private SortedSet<DatasetMetadata> buildInFolderMetadata() {
         TreeSet<DatasetMetadata> inFolder = new TreeSet<DatasetMetadata>(ORDER_BY_BASE_PATH_AND_TIME);
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
             FacilitySession session = fsm.getLoginDetails(
                     getFacility().getFacilityName(), entry.getTimestamp().getTime());
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
 
     public final Statistics getAfterLWM() {
         return afterLWM;
     }
 
     public final Statistics getBeforeHWM() {
         return beforeHWM;
     }
 
     public final Statistics getAfterHWM() {
         return afterHWM;
     }
 
     public final Statistics getBeforeQEnd() {
         return beforeQEnd;
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
 
     public final Date getQEnd() {
         return qEnd;
     }
 
     public final void setProblems(Problems problems) {
         this.problems = problems;
     }
     
 }
