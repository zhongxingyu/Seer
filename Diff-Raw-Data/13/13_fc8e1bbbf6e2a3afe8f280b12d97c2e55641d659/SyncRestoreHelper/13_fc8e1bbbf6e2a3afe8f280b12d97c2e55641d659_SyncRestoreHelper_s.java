 /**
  *  Copyright 2010-2013 Konstantin Livitski
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the Data-bag Project License.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Data-bag Project License for more details.
  *
  *  You should find a copy of the Data-bag Project License in the
  *  `data-bag.md` file in the `LICENSE` directory
  *  of this package or repository.  If not, see
  *  <http://www.livitski.name/projects/data-bag/license>. If you have any
  *  questions or concerns, contact the project's maintainers at
  *  <http://www.livitski.name/contact>. 
  */
     
 package name.livitski.databag.app.sync;
 
 import static name.livitski.databag.app.sync.ResolutionAction.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Timestamp;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import name.livitski.databag.app.ConfigurableService;
 import name.livitski.databag.app.Configuration;
 import name.livitski.databag.app.filter.PathMatcher;
 import name.livitski.databag.db.Cursor;
 import name.livitski.databag.db.DBException;
 import name.livitski.databag.db.Manager;
 import name.livitski.databag.db.Transaction;
 import name.livitski.databag.db.schema.FileDAO;
 import name.livitski.databag.db.schema.FileDTO;
 import name.livitski.databag.db.schema.LastSyncDAO;
 import name.livitski.databag.db.schema.LastSyncDTO;
 import name.livitski.databag.db.schema.NodeNameDAO;
 import name.livitski.databag.db.schema.NodeNameDTO;
 import name.livitski.databag.db.schema.ReplicaDAO;
 import name.livitski.databag.db.schema.ReplicaDTO;
 import name.livitski.databag.db.schema.SyncLogDTO;
 import name.livitski.databag.db.schema.VersionDAO;
 import name.livitski.databag.db.schema.VersionDTO;
 import name.livitski.databag.diff.Delta.Type;
 
 public abstract class SyncRestoreHelper extends ConfigurableService
 {
  /**
   * Creates an instance associated with a replica in a database.
   * The caller must {@link #close()} the instance when done using it or aborted.
   * @param db database manager
   * @param replicaId internal identifier of a replica's record in the database
   * or <code>null</code> if there is no current replica for this service
   * @param config configuration parameters
   * @throws DBException if there is an error while retrieving replica
   * information 
   */
  public SyncRestoreHelper(Manager db, Number replicaId, Configuration config)
   throws DBException
  {
   super(db, config);
   this.replica = null == replicaId ? null : db.findDAO(ReplicaDAO.class).findReplica(replicaId);
   // TODO: notify superclass when the replica changes
  }
 
  /**
   * Returns the operation name that will be output in log messages and
   * stored in the log tables for diagnostic purposes.
   * Call {@link #startOperation} to set the name at the beginning of
   * an operation and {@link #endOperation(Throwable)} to discard it at the end. 
   * Outside of the operation's context, this method returns
   * <code>null</code>.
   * @see #startOperation
   * @see #endOperation(Throwable)
   */
  public String getOperationName()
  {
   return operationName;
  }
 
  @Override
  protected ReplicaDTO getCurrentReplica()
  {
   return replica;
  }
 
  /**
   * Captures the {@link #getOperationTimestamp() timestamp}
   * in the beginning of the operation and ensures that only
   * one operation is ran at a time. Calls {@link #openLogRecord(String, Map)}
   * to log the operation's start.
   * @param operationName the operation name for logging purposes
   * @param params the operation's parameters to log or
   * <code>null</code> if this operation does not accept parameters
   * @param logOperation whether the operation should be logged on the
   * shared medium
   * @see #getOperationName()
   * @see #openLogRecord(String, Map)
   * @throws DBException if there is an error writing the log record to
   * the database
   */
  protected void startOperation(String operationName, Map<String, String> params, boolean logOperation)
   throws DBException
  {
   synchronized (this)
   {
    if (null != startTime)
     throw new IllegalStateException(
       "Operation '" + operationName + "' is already running since " + startTime);
    this.startTime = new Timestamp(System.currentTimeMillis());
    this.operationName = operationName;
   }
   logRecord = logOperation ? openLogRecord(operationName, params) : null;
  }
 
  /**
   * Returns the operation start time that will be assigned to
   * the data items touched during this operation.
   * Call {@link #startOperation} to capture the current
   * timestamp at the beginning of an operation and
   * {@link #endOperation(Throwable)} to discard the timestamp at its end. 
   * Outside of an operation's context, this method returns
   * the current timestamp, which may be different among multiple calls.
   * @see #startOperation
   * @see #endOperation(Throwable)
   */
  @Override
  protected Timestamp getOperationTimestamp()
  {
   return null == startTime ? super.getOperationTimestamp() : startTime;
  }
 
  /**
   * Logs the current operation's completion status, 
   * clears the {@link #getOperationTimestamp() timestamp}
   * in of the current operation and allows this object to run
   * another operation.
   * @param status the status of the operation if it has failed or 
   * <code>null</code> if the operation succeeded
   * @see #updateLogRecord(SyncLogDTO, Throwable)
   */
  protected void endOperation(Throwable status)
  {
   if (null != logRecord)
    try
    {
     updateLogRecord(logRecord, status);
     logRecord = null;
    }
    catch (Throwable updateFailure)
    {
     log().log(Level.WARNING, "Status update failed for the log " + logRecord, updateFailure);
    }
   synchronized (this)
   {
    startTime = null;
    operationName = null;
   }
  }
 
  /**
   * Synchronizes the file denoted by a record in the shared storage
   * with its respective location in
   * {@link #getCurrentReplica() the current replica}.
   * Note that this method cannot process shared files that are
   * {@link VersionDTO#isDeletionMark() marked deleted} at their
   * {@link VersionDAO#findCurrentVersion(FileDTO) current versions}.
   * First, determines the absolute path to the local replica of
   * that file. Then, calls {@link #analyzeFile(FileDTO, File)}
   * to determine an {@link ResolutionAction action to take}
   * with respect to that file, and performs that action. When
   * a file is replaced or becomes the new version on the shared
   * medium, {@link LastSyncDTO last synchronization records} are
   * updated.
   * @param record shared storage record for the file to synchronize.
   * @throws IOException if there is an error reading or writing local
   * files or shared stream data
   * @throws DBException if there is an error accessing shared database
   * @throws RuntimeException if {@link #analyzeFile} returns
   * {@link ResolutionAction#UNKNOWN} or an unsupported action,
   * or the file record's information is inconsistent 
   */
  protected void syncToLocal(FileDTO record) throws IOException, DBException
  {
    Manager db = getDb();
    Logger log = log();
    File local = db.findDAO(NodeNameDAO.class).toLocalFile(record.getNameId(), replica.getPath());
    VersionDAO versionDAO = db.findDAO(VersionDAO.class);
    LastSyncDAO syncDAO = db.findDAO(LastSyncDAO.class); 
    log.finest("Synchronizing " + record + " with " + local);
    ResolutionAction action = analyzeFile(record, local);
    switch(action)
    {
    case NONE:
     log.finer("Skipped " + record + ", file " + local);
     break;
    case DISCARD:
     VersionDTO version = versionDAO.findCurrentVersion(record);
     restoreVersion(version, local);
     // add/update a sync record during synchronization (set version=restored version)
     syncDAO.recordSync(replica, version);
     break;
    case UPDATE:
     if (local.isDirectory())
      throw new RuntimeException("Internal error processing " + record
        + ". New version at '" + local + "' is a directory");
  //   { // TODO: this fragment may be handy when directory replacements are enabled
  //    // delete the file record
  //    log.finer("File '" + local + "' has been replaced by a directory");
  //    Timestamp deleted = new Timestamp(local.lastModified());
  //    deleteFile(record, deleted);
  //    // enumerate new files in the directory
  //    Set<File> paths = new HashSet<File>();
  //    scanPaths(paths, null, local, new HashSet<File>());
  //    for (File path : paths)
  //     addNewFile(path, true);
  //   }
     else
     {
      version = addVersion(record, local);
      // add/update a sync record when a new version is created (set version=new version)
      syncDAO.recordSync(replica, version);
     }
     break;
    case BRANCH: 
     throw new UnsupportedOperationException("Operation " + action
       + " not implemented. Cannot process " + record);
 //  case RENAME:
     // TODO: update the Javadoc of ResolutionAction constants when implementing these actions
     // TODO: take care of the proper sync records when implementing these actions,
     // including the migrated db case with no sync records
    default:
     throw new RuntimeException("Internal error processing " + record
       + ". Undefined or invalid operation: " + action);
    }
   }
 
  /**
   * Marks a file on the shared medium as deleted. 
   * Adds a {@link VersionDTO#isDeletionMark() deletion mark record}
   * to the file's version tree and updates the
   * {@link LastSyncDAO#recordSync sync record for that file}
   * accordingly.
   * @param record shared storage record for the file to mark deleted
   * @param when the best guess of the time when the file was deleted
   * @throws DBException if there is an error accessing shared database
   */
  protected void deleteFile(FileDTO record, Timestamp when) throws DBException
  {
   Manager db = getDb();
   VersionDAO versionDAO = db.findDAO(VersionDAO.class);
   Transaction txn = db.beginTransaction();
   try
   {
    VersionDTO currentVersion = versionDAO.findCurrentVersion(record);
    when = adjustTimeToBaseVersion(currentVersion, when);
    VersionDTO mark = VersionDTO.newDeletionMark(record, when);
    mark.setBaseVersionId(currentVersion.getId());
    versionDAO.insert(mark);
    record.setCurrentVersionId(mark.getId());
    db.findDAO(FileDAO.class).update(record);
    if (null != replica)
     db.findDAO(LastSyncDAO.class).recordSync(replica, mark);
    txn.commit();
    txn = null;
   }
   finally
   {
    if (null != txn)
     try { txn.abort(); }
     catch (Throwable fail)
     {
      log().log(Level.WARNING,
        "Rollback failed after unsuccessful deletion marking of "
        + record + " at " + when, fail);
     }
   }
  }
 
  /**
   * Stores a new version of a file on the shared medium.
   * Creates a new {@link VersionDTO version record} for the file
   * using its {@link FileDTO#getCurrentVersionId() current version}
   * as {@link VersionDTO#setBaseVersionId the base}. To preserve
   * ascending order of timestamps within the version tree, adjusts
   * the new version's modified time if it is earlier than that of
   * the file's current version. Attempts to use the
   * {@link #getOperationTimestamp() current operation's timestamp}
   * as the adjusted time. If the adjusted time is still eralier   
   * than the current version's timestamp value, uses that value plus
   * one millisecond.
   * Delegates the task of differential compression of the new image
   * to the cached {@link #getImageBuilder() image builder},
   * but falls back to saving the complete image if the compressed
   * data exceeds certain thresholds. On fallback, makes sure that version
   * graph for the file remains connected in both forward and reverse
   * directions. 
   * @param record identifies a file on the shared medium
   * @param local points to the new version of the file
   * @return database record for the newly created version
   * @throws DBException if there is an error accessing shared database
   * @throws IOException if there is an error reading the local
   * file, or reading or writing stream data to the medium
   */
  protected VersionDTO addVersion(FileDTO record, File local)
   throws DBException, IOException
  {
   final long fileSize = local.length();
   Timestamp modifiedTime;
   final Logger log = log();
   ImageBuilder worker = getImageBuilder();
   Manager db = getDb();
   final VersionDAO versionDAO = db.findDAO(VersionDAO.class);
   FileDAO fileDAO = db.findDAO(FileDAO.class);
   Transaction txn = db.beginTransaction();
   InputStream image = null;
   try
   {
    // NOTE: the following statements repeat on retry
    VersionDTO current = versionDAO.findCurrentVersion(record);
    if (current.isDeletionMark())
     throw new IllegalArgumentException("Cannot add a version derived from deletion mark: " + current);
    modifiedTime = adjustNewVersionTimestamp(local, record, current);
    worker.setVersion(current);
    final VersionDTO updated = new VersionDTO(record);
    updated.setBaseVersionId(current.getId());
    updated.setModifiedTime(modifiedTime);
    updated.setSize(fileSize);
    // TODO: digest
    versionDAO.insert(updated);
    // end repeat statements
    boolean saved = false;
    // compare file stats to low thresholds
    // generate deltas and compare stats to high thresholds
    try
    {
     if (worker.buildDeltas(local, new ImageBuilder.DeltaStore() {
       public void saveDelta(Type type, InputStream stream) throws IOException,
         DBException
       {
        versionDAO.saveDelta(updated, stream, type);
       }
      }))
     {
      long heapSize = Runtime.getRuntime().totalMemory();
      CumulativeDeltaStats threshold = new CumulativeDeltaStats(
        (long)(fileSize * (double) getParameterValue(Configuration.DELTA_CHAIN_SIZE)),
        (long)(heapSize * (double) getParameterValue(Configuration.CUMULATIVE_DELTA_SIZE))
        );
      // NOTE: order of comparison is significant in case of an integer overflow
      if (!worker.getCumulativeStats().exceeds(threshold))
      {
       saved = true;
       log.fine("Saved a set of deltas for " + updated);
      }
      else
      {
       log.fine(worker.getCumulativeStats() + " for new " + updated + " exceeded threshold " + threshold);
       // abort deltas if there is a full image of previous version
       if (current.isImageAvailable())
        log.fine("Base " + current
          + " already has an image stored, aborting deltas for " + updated + " ...");
       // otherwise save image of the current version
       else
       {
        log.fine("Saving complete image of " + local + " as " + updated +  " ...");
        image = new FileInputStream(local);
        versionDAO.saveImage(updated, image);
        saved = true;
       }
      }
     }
    }
    catch (OutOfMemoryError mem)
    {
     System.gc();
     log.log(Level.WARNING, "Ran out of memory when storing new " + updated
       + " of " + record, mem);
    }
    // if comparisons passed, log that
    if (!saved)
    {
     txn.abort();
     txn = null;
     txn = db.beginTransaction();
     // NOTE: these are (almost) repeat statements
     current = versionDAO.findCurrentVersion(record);
     modifiedTime = adjustNewVersionTimestamp(local, record, current);
     // worker.setVersion(current); // superfluous as we don't use worker anymore
     updated.setBaseVersionId(current.getId());
     updated.setModifiedTime(modifiedTime);
     updated.setSize(fileSize);
     // TODO: digest
     versionDAO.insert(updated);
     // end repeat statements
     log.fine("No deltas have been generated for " + updated);
     // store complete images of both versions to preserve graph continuity
     log.fine("Saving complete image of " + updated + " ...");
     image = new FileInputStream(local);
     versionDAO.saveImage(updated, image);
     if (!current.isImageAvailable())
     {
      InputStream currentImage = null;
      try
      {
       currentImage = worker.buildImage();
       versionDAO.saveImage(current, currentImage);
      }
      finally
      {
       // Allow exception in close() to override initial exception
       // as it will store status of asynchronous restore process
       if (null != currentImage)
        currentImage.close();
      }
     }
    }
    record.setCurrentVersionId(updated.getId());
    fileDAO.update(record);
    txn.commit();
    txn = null;
    return updated;
   }
   finally
   {
    if (null != image)
     try { image.close(); }
     catch(Exception ex)
     {
      log.log(Level.WARNING, "Close failed for file " + local, ex);
     }
    if (null != txn)
     try { txn.abort(); }
     catch (Throwable fail)
     {
      log.log(Level.WARNING, "Rollback failed after unsuccessful version update for " + record, fail);
     }
   }
  }
 
  /**
   * Adds a dummy record to the file's version tree. The dummy record will
   * restore to an image identical to that of its base version, but follow
   * the {@link FileDTO#getCurrentVersionId() current version} of its file
   * in time. Upon success, the dummy version record becomes its file's
   * current version. The name field of the new version record is not set.
   * @param file describes the file on the shared medium 
   * @param baseVersionId points to the base version that the dummy record
   * will mirror 
   * @return dummy version record added to the database 
   * @throws DBException if there is an error accessing shared database
   * @throws IOException if there is an error writing stream data to the
   * shared medium
   */
  protected VersionDTO addDummyVersion(FileDTO file, Number baseVersionId)
   throws DBException, IOException
  {
   Logger log = log();
   Manager db = getDb();
   final VersionDAO versionDAO = db.findDAO(VersionDAO.class);
   FileDAO fileDAO = db.findDAO(FileDAO.class);
   ImageBuilder builder = getImageBuilder();
   Transaction txn = db.beginTransaction();
   try
   {
    VersionDTO baseVersion = versionDAO.findVersion(file, baseVersionId.intValue());
    if (null == baseVersion)
     throw new IllegalArgumentException("Version #" + baseVersionId
       + " is not available for " + file);
    if (baseVersion.isDeletionMark())
     throw new IllegalArgumentException("Cannot revert a file to a deletion mark: " + baseVersion);
    builder.setVersion(baseVersion);
    VersionDTO currentVersion = versionDAO.findCurrentVersion(file);
    Timestamp time = getOperationTimestamp();
    time = adjustTimeToBaseVersion(baseVersion, time);
    time = adjustTimeToBaseVersion(currentVersion, time);
    final VersionDTO dummyVersion = new VersionDTO(file);
    dummyVersion.setModifiedTime(time);
    dummyVersion.setBaseVersionId(baseVersion.getId());
    dummyVersion.setSize(baseVersion.getSize());
    dummyVersion.setDigest(baseVersion.getDigest());
    versionDAO.insert(dummyVersion);
    builder.dummyDeltas(new ImageBuilder.DeltaStore() {
     public void saveDelta(Type type, InputStream stream)
      throws IOException, DBException
     {
      versionDAO.saveDelta(dummyVersion, stream, type);
     }
    });
    file.setCurrentVersionId(dummyVersion.getId());
    fileDAO.update(file);
    txn.commit();
    txn = null;
    return dummyVersion;
   }
   finally
   {
    if (null != txn)
     try { txn.abort(); }
     catch (Throwable fail)
     {
      log.log(Level.WARNING, "Rollback failed after unsuccessful dummy version insert for "
        + file + ", base version #" + baseVersionId, fail);
     }
   }
  }
 
  /**
   * Restores a file version to a specific location. Overwrites
   * the prior contents of that location if that is a file. Throws
   * an exception if the location points to a directory. 
   */
  protected void restoreVersion(VersionDTO version, File path)
   throws IOException, DBException
  {
   File location = path.getAbsoluteFile().getParentFile();
   if (null != location && !location.isDirectory() && !location.mkdirs())
    throw new IOException("Could not create directory " + location + " to store file " + path.getName());
   ImageBuilder worker = getImageBuilder();
   worker.setVersion(version);
   worker.storeImage(path);
  }
 
  /**
   * Determines whether a local file is the new, current, or outdated
   * version of a tracked file and performs conflict resolution
   * if necessary.
   * @see ResolutionAction
   */
  protected ResolutionAction analyzeFile(FileDTO file, File local)
  	throws IOException, DBException
  {
   ResolutionAction action = resolutionActionForFile(file);
  Logger log = log();
   VersionDTO match = null;
   if (!local.exists())
   {
    // file doesn't exist - safe to discard it
    action = DISCARD;
   }
   else
   {
    Manager db = getDb();
    LastSyncDAO syncDAO = db.findDAO(LastSyncDAO.class); 
    NodeNameDTO name = db.findDAO(NodeNameDAO.class).find(file.getNameId());
    // look for matching version if local path points to a file
    match = local.isDirectory() ? null : findMatchingVersion(local, name);
    if (null != match)
    {
     if (match.getFileId() == file.getId() && match.getId() == file.getCurrentVersionId())
      // the file is current
      action = NONE;
    else if (NONE == action)
     // asked to keep stale versions 
     log.info("Skipping stale file '" + local
       + "' (size = " + local.length() + ", modified at " + new java.util.Date(local.lastModified())
     + ") as requested");
     else
      // known stale version - safe to replace 
      action = DISCARD;
    }
    else // null == match
    {
     // no match - find the most recent version
     match = db.findDAO(VersionDAO.class).findCurrentVersion(file);
     // add new version if the local file has a recent change and lastSync is current
     LastSyncDTO lastSync = syncDAO.findRecord(file.getId(), replica);
     if (null != lastSync && new Integer(match.getId()).equals(lastSync.getVersionId())
       && match.getModifiedTime().getTime() < local.lastModified())
      action = UPDATE;
     // raise a conflict otherwise if there are no specific instructions
     else if (UNKNOWN == action)
      throw new IllegalStateException(
        "Please specify how to resolve the conflict between local file "
        + local + " (size = " + local.length() + ", modified at "
        + new java.util.Date(local.lastModified()) + ") and " + match
        + ". The file has "
        + (null == lastSync ? "no synchronization record for the current replica." : "a " + lastSync)
        );
    }
    // create sync records for skipped files during database migration
    if (null != match && NONE == action)
    {
     // check whether a sync record exists in a migrated file storage
     if (!syncDAO.existsRecord(match.getFileId(), replica))
      // create a record if it does not exist (set version=synced version)
      syncDAO.recordSync(replica, match);
    }
   }
   return action;
  }
 
  /**
   * Returns the user-specified resolution action for a shared
   * file record. Current implementation returns
   * {@link Configuration#DEFAULT_ACTION} setting for any file.
   * @param file a shared file record
   * @return the action that applies to that file
   */
  protected ResolutionAction resolutionActionForFile(FileDTO file)
  {
   // TODO: query resolution rules based on the file record (file granularity actions) 
   return getParameterValue(Configuration.DEFAULT_ACTION);
  }
 
  /**
   * Looks up a match for a local file among named shared file's versions.
   * Prefers matches of existing file's versions to those of deleted files.
   * Second order of preference is a
   * {@link LastSyncDAO#findRecord(long, ReplicaDTO) last synced version}
   * of its respective file over a regular version. Third order of preference
   * is by the modification date, descending.  
   * @param local the local file to find matching version of
   * @param name the name of a shared file to consider
   * @return matching version object or <code>null</code>
   * if there is no match
   * @throws DBException if there is an error accessing shared
   * database
   */
  protected VersionDTO findMatchingVersion(File local, NodeNameDTO name)
    throws DBException
  {
   Manager db = getDb();
   VersionDAO versionDAO = db.findDAO(VersionDAO.class);
   FileDTO liveFile = versionDAO.findExistingFile(name);
   // TODO: option for checking the digest instead of time (need hash index on digest)
   Cursor<VersionDTO> matches = versionDAO.findVersions(
     name,
     local.length(),
     local.lastModified(),
     getParameterValue(Configuration.ALLOWED_TIMESTAMP_DISCREPANCY)
   );
   VersionDTO match = null;
   try
   {
    for (VersionDTO candidate; null != (candidate = matches.next());)
    {
     if (null == match)
      match = candidate;
     // TODO: better procedure for multiple match resolution (use hash or differencing)
     else
     {
      int order;
      // Prefer versions of the live file
      order = null != liveFile && match.getFileId() == liveFile.getId() ? 1 : 0;
      order -= null != liveFile && candidate.getFileId() == liveFile.getId() ? 1 : 0;
      // Then favor last synced versions
      if (0 == order)
      {
       if (isLastSyncedVersion(match))
        order = 1;
       if (isLastSyncedVersion(candidate))
        order -= 1;
      }
      // If all else fails, favor the most recent version
      if (0 == order)
       order = match.getModifiedTime().compareTo(candidate.getModifiedTime());
      if (0 > order)
       match = candidate;
     }
    }
    // TODO: also try unlinked records from the LastSyncDAO, including a digest check
   }
   finally
   {
    try { matches.close(); }
    catch (Exception ex)
    {
     log().log(Level.FINE, "Error closing " + matches, ex);
    }
   }
   return match;
  }
 
  /**
   * Places a new file into the shared storage. Performs an
   * optional check whether the local file is an obsolete version
   * of a deleted or renamed shared file. For the check to
   * succeed, the old version must have been
   * {@link LastSyncDAO#findRecord previously synchronized}
   * with shared medium. If that's the case, the
   * obsolete local file is deleted instead. Creates a
   * {@link LastSyncDAO#recordSync sync record} for each
   * new file.
   * @param path relative path to the new file from the
   * {@link ReplicaDTO#getPath() replica's root}. 
   * @param deleteObsolete request to check whether the
   * local file is an obsolete version of a deleted shared
   * file and delete it if so
   * @throws name.livitski.databag.db.ConstraintViolationException
   * if the file at this path exists on shared medium and hasn't been
   * deleted
   * @throws DBException if there is an error accessing shared
   * database
   * @throws IOException if there is an error reading the local
   * file, or writing stream data to the medium
   * @see #SyncRestoreHelper
   */
  protected void addNewFile(File path, boolean deleteObsolete)
   throws IOException, DBException
  {
   Manager db = getDb();
   Logger log = log();
   File local = new File(replica.getPath(), path.getPath());
   long time = local.lastModified();
   long size = local.length();
   // TODO: digest
   if (0L == time)
    throw new IOException("Could not read attributes of file " + local);
   Timestamp timestamp = new Timestamp(time);
   FileInputStream image = null;
   NodeNameDAO nameDAO = db.findDAO(NodeNameDAO.class);
   FileDAO fileDAO = db.findDAO(FileDAO.class);
   VersionDAO versionDAO = db.findDAO(VersionDAO.class);
   LastSyncDAO syncDAO = db.findDAO(LastSyncDAO.class);
   Transaction txn = db.beginTransaction();
   try
   {
    checkAncestors(path);
    checkDescendants(path);
    FileDTO file = new FileDTO();
    NodeNameDTO node = nameDAO.find(path, true);
    file.setNameId(node.getId());
    // check whether the file is obsolete and should be deleted instead
    if (deleteObsolete)
    {
     VersionDTO version = findMatchingVersion(local, node);
     if (null != version)
     {
      FileDTO other = fileDAO.findFile(version.getFileId());
      VersionDTO othersCurrentVersion = versionDAO.findCurrentVersion(other);
      // if the matching file is live, issue a warning and don't add
      if (!othersCurrentVersion.isDeletionMark() && path.equals(nameDAO.toLocalFile(other.getNameId())))
      {
       log().warning("Local file " + local + " matches version #" + version.getId() + " of "
           + other + ", there is no need to add it to the shared storage, skipping ...");
       local = null;
      }
      // when a local file matches a version of a deleted or renamed file ...
      else
      {
       // ... read its synchronization record
       LastSyncDTO lastSync = syncDAO.findRecord(other.getId(), replica);
       // if the local file has been previously synced to the matching version...
       if (null != lastSync && null != lastSync.getVersionId() && lastSync.getVersionId() == version.getId())
       {
        // ... propagate the deletion
        log.fine("Local file " + local + " is an outdated version " + version.getId()
          + " of " + other + ", deleting ...");
        if (local.delete() && othersCurrentVersion.isDeletionMark())
 	// update the sync record to reflect the deletion
 	syncDAO.recordSync(replica, othersCurrentVersion);
        local = null;
       }
       else
       {
        // no sync record for matching version
        log.fine("Local file " + local + " matches version " + version.getId()
          + " of " + other + ", but hasn't been synced to that version. Will add it as a new file.");
        local.setLastModified(getOperationTimestamp().getTime());
       }
      }
     }
    }
    if (null != local)
    {
     log.finer("Storing new file " + local + " of size " + size + " modified on " + timestamp + " ..." );
     fileDAO.insert(file);
     timestamp = adjustNewVersionTimestamp(local, file, null);
     VersionDTO version = new VersionDTO(file);
     version.setModifiedTime(timestamp);
     version.setSize(size);
     // TODO: digest
     versionDAO.insert(version);
     image = new FileInputStream(local);
     versionDAO.saveImage(version, image);
     file.setCurrentVersionId(version.getId());
     fileDAO.update(file);
     // add a sync record when a file is added to shared storage
     syncDAO.recordSync(replica, version);
    }
    txn.commit();
    txn = null;
   }
   catch (DBException ex)
   {
    throw new DBException("Could not store new file " + local, ex);
   }
   finally
   {
    if (null != image)
     try { image.close(); }
     catch(Exception ex)
     {
      log.log(Level.FINE, "Close failed for file " + local, ex);
     }
    if (null != txn)
     try { txn.abort(); }
     catch(Throwable ex)
     {
      log.warning("Could not undo the add transaction for file: " + path);
      log.log(Level.FINER, "Rollback failed: " + ex.getMessage(), ex);
     }
   }
  }
 
  /**
   * Returns the canonical path to the current replica's root directory
   * on the local system or <code>null</code> if there is no replica
   * selected.
   * @throws IOException if there is an error while trying to obtain
   * the canonical form of replica's root path
   */
  protected File getReplicaRoot() throws IOException
  {
   String rootPath = null == replica ? null : replica.getPath();
   return null == rootPath ? null : new File(rootPath).getCanonicalFile();
  }
 
  /**
   * Deletes a file or sub-directory recursively, but does not follow
   * links within the directory structure.
   * @param path points to the item being deleted
   * @param parentCanonical {@link File#getCanonicalFile() canonical}
   * form of the <code>path</code>'s parent
   * @throws IOException if there is an error deleting an item
   */
  // TODO: unused method - use with caution
  protected static void deleteRecursively(File path, File parentCanonical)
  	throws IOException
  {
   if (!path.exists())
    return;
   else if (!path.isDirectory())
   {
    if (!path.delete())
     throw new IOException("Could not delete file " + path);
   }
   else
   {
    File canonical = path.getCanonicalFile();
    File realParent = canonical.getParentFile();
    // do not delete contents of linked directories
    if (path.getName().equals(canonical.getName())
      && (null == parentCanonical && null == realParent
        || null != parentCanonical && parentCanonical.equals(realParent)))
    {
     for (File child : path.listFiles())
      deleteRecursively(child, canonical);
    }
    if (!path.delete())
     throw new IOException("Could not delete directory or link " + path);
   }
  }
 
  /**
   * Checks shared storage for descendants of a would-be file at
   * <code>path</code>. Throws a {@link java.lang.IllegalStateException}
   * if such descendants are found.
   */
  protected void checkDescendants(File path) throws DBException
  {
   Cursor<FileDTO> descendants = null;
   try
   {
    Manager db = getDb();
    descendants = db.findDAO(FileDAO.class).findDescendantFiles(path);
    FileDTO first = descendants.next();
    if (null != first)
    {
     File descendant = db.findDAO(NodeNameDAO.class).toLocalFile(first.getNameId());
     throw new IllegalStateException("Cannot add a file at '" + path
       + "'. Descendant '" + descendant
       + "' requires a directory at that location.");
    }
   }
   finally
   {
    if (null != descendants)
     try { descendants.close(); }
     catch(DBException ex)
     {
      log().log(Level.FINE, "Could not close the cursor over descendants of: " + path, ex);
     }
   }
  }
 
  /**
   * Checks shared storage for descendants of a would-be file at
   * <code>path</code>. Throws a {@link java.lang.IllegalStateException}
   * if such descendants are found.
   */
  protected void checkAncestors(File path) throws DBException
  {
   Manager db = getDb();
   FileDTO file = db.findDAO(FileDAO.class).findAncestorFile(path);
   if (null != file)
   {
    File ancestor = db.findDAO(NodeNameDAO.class).toLocalFile(file.getNameId());
    throw new IllegalStateException("Cannot add a file at '" + path
      + "'. Ancestor '" + ancestor + "' is a file, not a directory.");
   }
  }
 
  /**
   * Scans the current replica for all files matching the
   * {@link #getEffectiveFilter() effective filter} and an optional pattern.
   * @param pattern the pattern to match or <code>null</code> to match
   * all files that pass the effective filter
   * @return an unordered set of matching local files
   * @throws IOException if there is an error scanning files
   * @throws DBException if there is an error retrieving the effective filter
   * @see DirectoryScanner
   */
  protected Set<File> scanLocal(PathMatcher pattern) throws IOException, DBException
  {
   File root = getReplicaRoot();
   DirectoryScanner scanner = new DirectoryScanner(root);
   scanner.setPattern(pattern);
   scanner.setFilter(getEffectiveFilter());
   scanner.scan();
   return scanner.getFiles();
  }
 
  /**
   * Tells whether this version object is designated as the last synchronized
   * version of its file within the current replica.
   */
  private boolean isLastSyncedVersion(VersionDTO version) throws DBException
  {
   LastSyncDTO lastSync = getDb().findDAO(LastSyncDAO.class).findRecord(version.getFileId(), replica);
   if (null == lastSync || lastSync.isDeleted())
    return false;
   Integer lastSyncVersionId = lastSync.getVersionId();
   return null != lastSyncVersionId && version.getId() == lastSyncVersionId;
  }
 
  /**
   * @param local path to the local file
   * @param record information about the shared file
   * @param current the shared file's current version or <code>null</code> if
   * the shared file record has not been stored yet
   */
  private Timestamp adjustNewVersionTimestamp(File local, FileDTO record, VersionDTO current)
  {
   Timestamp modifiedTime = new Timestamp(local.lastModified());
   Timestamp adjustedTime;
   if (modifiedTime.getTime() > System.currentTimeMillis())
   {
    adjustedTime = getOperationTimestamp();
    log().warning(
      "Modification time of file '" + local + "' (" + modifiedTime
      + ") is in future. Adjusting that file's timestamp to " + adjustedTime + " ...");
    local.setLastModified(adjustedTime.getTime());
    modifiedTime = adjustedTime;
   }
   else
    adjustedTime = modifiedTime;
   adjustedTime = adjustTimeToBaseVersion(current, adjustedTime);
   if (adjustedTime != modifiedTime)
   {
    log().info("New version of " + record + " has an earlier modification time ("
      + modifiedTime + ") than its current " + current
      + ". Changing the local timestamp of '" + local + "' to " + adjustedTime + " ...");
    local.setLastModified(adjustedTime.getTime());
   }
   return adjustedTime;
  }
 
  private Timestamp adjustTimeToBaseVersion(VersionDTO base, Timestamp time)
  {
   Timestamp baseTime = null == base ? null : base.getModifiedTime();
   if (null != baseTime && time.before(baseTime))
   {
    time = new Timestamp(1L + baseTime.getTime());
   }
   return time;
  }
 
  private ReplicaDTO replica;
  private Timestamp startTime;
  private String operationName;
  private SyncLogDTO logRecord;
 }
