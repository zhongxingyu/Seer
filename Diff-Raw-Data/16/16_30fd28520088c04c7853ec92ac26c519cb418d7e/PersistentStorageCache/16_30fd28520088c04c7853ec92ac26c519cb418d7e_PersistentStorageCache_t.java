 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import edu.washington.cs.cse490h.lib.Node;
 import edu.washington.cs.cse490h.lib.PersistentStorageReader;
 import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class PersistentStorageCache {
   public static enum CacheState implements Comparable<CacheState> {
     READ_ONLY,
       READ_WRITE,
       INVALID;
   }
 
   public static class CacheEntry {
     DFSFilename key;
     String data;
     FileVersion version;
     CacheState state;
     boolean exists;
     //boolean dirty;
     int owner;
     Flags flags;
 
     public CacheEntry(DFSFilename key, String data, CacheState state, FileVersion version, boolean exists, int owner, Flags flags) {
       this.key = key;
       this.data = data;
       this.state = state;
       this.version = version;
       this.exists = exists;
       this.owner = owner;
       this.flags = flags;
     }
 
     public DFSFilename getKey() {
       return key;
     }
 
     public String getData() {
       return data != null ? data : "";
     }
 
     public boolean isDirty() {
       return version.getRevision() > 0;
     }
     
     public boolean isInvalid() {
       return version.getVersion() < 0; // HMMm
     }
 
     public boolean exists() {
       return exists;
     }
 
     public Flags getFlags() {
       return flags;
     }
 
     public int getOwner() {
       return owner;
     }
 
     public void setOwner(int o) {
       owner = o;
     }
 
     public CacheState getState() {
       return state;
     }
 
     public void setState(CacheState s) {
       state = s;
     }
 
     public void setVersion(FileVersion version) {
       this.version = version;
     }
     
     public FileVersion getVersion() {
       return version;
     }
 
     public String toFileHeader() {
       return getVersion() + "," + 
         exists() + "," + 
         getState() + "," + 
         getOwner();
     }
 
     @Override
     public int hashCode() {
       return key.hashCode();
     }
 
     @Override
     public boolean equals(Object other) {
       if (other instanceof CacheEntry)
         return ((CacheEntry) other).key.equals(key);
       return false;
     }
   }
 
   public static final String kTempFileName = ".cachetemp";
   public static final String kLogFileName = ".dirtylog";
   public static final int kNotOwned = -1;
 
   Node owner;
   DFSComponent component;
   private final boolean isMaster;
   Map<DFSFilename, CacheEntry> store;
   HashSet<DFSFilename> dirtyEntries;
 
   /**
    * Instantiate a cache. Repairs any invalid state seen in the
    * PersistentStorageLayer and replays the dirty log.
    *
    * @param owner Node that will be using the cache; used to access the
    *      PersistentStorageLayer.
    * @param component DFSComponent for utility methods.
    * @param isMaster if true, the PersistentStorageLayer will always
    *      contain a valid copy of the contents of the cache. That is, files marked
    *      clean in the cache will not be purged from disk, and all non-invalid
    *      entries inserted into the cache will be written to disk.
    *
    *      NOTE: This affects the start-up behavior of the cache in the following
    *      way:
    *       - If isMaster == false, entries loaded from disk will be marked as
    *         INVALID and not owned.
    *       - If isMaster == true, entries loaded from disk will retain the same
    *         values that they had when they were written.
    *
    *      This does _NOT_ affect the behavior of the dirty flag.
    */
   public PersistentStorageCache(Node owner,
                                 DFSComponent component,
                                 boolean isMaster) {
     this.owner = owner;
     this.component = component;
     this.isMaster = isMaster;
     this.store = new HashMap<DFSFilename, CacheEntry>();
     this.dirtyEntries = new HashSet<DFSFilename>();
 
     if (Utility.fileExists(owner, kTempFileName))
       restoreInvalidCacheState();
 
     readLog();
   }
 
   /**
    * Clean up any previous write operations and reflect appropriate changes in
    * the in-memory cache.
    */
   protected void restoreInvalidCacheState() {
     PersistentStorageReader reader;
     try {
       reader = owner.getReader(kTempFileName);
     } catch (IOException ioe) {
       return;
     }
 
     try {
       if (!reader.ready()) {
         // Delete temp file.
         PersistentStorageWriter writer = owner.getWriter(kTempFileName, false);
         if (!writer.delete()) {
           throw new IllegalStateException("Cache startup exception: disk permissions are " +
                                           "invalid. Cannot delete temp file!");
         }
       } else {
         String filename = reader.readLine();
         PersistentStorageWriter originalWriter;
         try {
           originalWriter = owner.getWriter(filename, false);
         } catch (IOException ioe) {
           throw new IllegalStateException("Cache startup exception: cannot write file " +
                                           filename, ioe);
         }
         String data = component.readRemainingContentsToString(reader);
         originalWriter.write(data);
       }
     } catch (IOException e) {
       throw new IllegalStateException("IOException occurred while restoring the cache " +
                                       "state.",
                                       e);
     }
   }
 
   /**
    * Replay the dirty log. Loads all entries in the log to memory and marks/
    * unmarks them dirty as appropriate. Does not change the dirty log.
    *
    * If the dirty log does not exist, returns without complaint. If the log
    * exists but no data is available, complains and allows operations to
    * continue. If an IOException occurs while handling the log, throws an
    * IllegalStateException and returns.
    */
   protected void readLog() {
     try {
       if (!Utility.fileExists(owner, kLogFileName))
         return;
 
       PersistentStorageReader logReader = owner.getReader(kLogFileName);
       if (!logReader.ready()) {
         System.err.println("WARN: Could not read cache dirtylog!");
         return;
       }
 
       System.err.println("NOTE: About to replay dirty log");
       while (logReader.ready()) {
         String entry = logReader.readLine();
         DFSFilename entryKey = new DFSFilename(entry.substring(1));
 
         load(entryKey);
 
         if (entry.substring(0,1).equals("+"))
           setDirty(entryKey, false);
         else if (entry.substring(0,1).equals("-"))
           setClean(entryKey, false);
         else
           System.err.println("WARN: Invalid dirty state (" + entry.substring(0,1) + ") in log file!");
       }
       System.err.println("NOTE: Log replay complete!");
     } catch (IOException ioe) {
       System.err.println("While replaying log:");
       ioe.printStackTrace();
       System.err.println("Please fix this before you restart the cache!");
       throw new IllegalStateException("IOException while reading log!", ioe);
     }
   }
 
   /**
    * Invalidate a cache entry. Do not call this function if the cache is a master
    * cache; master caches should have no invalid entries other than nonexistent
    * entries.
    *
    * @param key Key of the cache entry to invalidate.
    * @param knownInMaster true if it is known that a cache entry exists in the
    *      master.
    */
   public void invalidate(DFSFilename key, boolean knownInMaster) {
     CacheEntry entry = store.get(key);
     if (entry == null) {
       entry = new CacheEntry(key, null, CacheState.INVALID, new FileVersion(new TransactionId(0,0),0,0), knownInMaster, kNotOwned, new Flags());
       store.put(key, entry);
     }
 
     entry.state = CacheState.INVALID;
     entry.exists = knownInMaster;
   }
 
   /**
    * Loads a cache entry from disk. If the entry is not found, marks the
    *
    * @param key Key of the cache entry to load.
    */
   protected void load(DFSFilename key) {
     CacheEntry entry = store.get(key);
     if (entry != null)
       return;
 
     try {
       PersistentStorageReader reader = owner.getReader(key.getPath());
       if (!reader.ready()) {
         invalidate(key, false);
         return;
       }
 
       String metadata = reader.readLine();
       String[] parts = metadata.split(",");
       if (parts.length != 4) {
         invalidate(key, false);
         return;
       }
 
       FileVersion version = FileVersion.parse(parts[0]);
       boolean exists = Boolean.parseBoolean(parts[1]);
       CacheState state = CacheState.valueOf(parts[2]);
       int owner = Integer.parseInt(parts[3]);
 
       if (!isMaster) {
         state = CacheState.INVALID;
         owner = kNotOwned;
       }
 
       String data = null;
       if (exists)
         data = component.readRemainingContentsToString(reader);
 
       set(key, version, data, owner, exists, false);
       store.get(key).state = state;
     } catch (FileNotFoundException e) {
       invalidate(key, false);
     } catch (IOException e) {
       throw new IllegalStateException("Cannot load cache item from disk!", e);
     } catch (Exception e) {
       // TODO(andrew): In project 3, we may have some different semantics
       // when we fail to load a file.
       try {
         PersistentStorageWriter writer = owner.getWriter(key.toString(), false);
         writer.delete();
       } catch (IOException ioe) {
         System.err.println("While deleting corrupted dirty file " + key + ":");
         ioe.printStackTrace();
       }
 
       invalidate(key, false);
     }
   }
 
   protected void copyToTempFile(DFSFilename key, String data) {
     PersistentStorageWriter tempWriter;
     try {
       tempWriter = owner.getWriter(kTempFileName, false);
     } catch (IOException ioe) {
       System.err.println("Unexpected cache exception: cannot write to " +
                          "temp file!");
       throw new IllegalStateException("Temp file cannot be written!",
                                       ioe);
     }
     
     try {
       String oldData;
       if (data == null) {
         PersistentStorageReader reader = owner.getReader(key.getPath());
         if (!reader.ready()) {
           tempWriter.delete();
           return;
         }
         
         oldData = component.readRemainingContentsToString(reader);
       } else {
         oldData = data;
       }
 
       tempWriter.write(key.getPath() + "\n" + oldData);
       tempWriter.close();
     } catch (FileNotFoundException e) {
     } catch (IOException e) {
       throw new IllegalStateException("Cannot copy old file content to temp file!",
                                       e);
     }
   }
 
   protected void deleteTempFile() {
     try {
       PersistentStorageWriter tempWriter = owner.getWriter(kTempFileName, false);
       tempWriter.delete();
     } catch (IOException e) {
       throw new IllegalStateException("Cannot delete temp file!", e);
     }
   }
 
   /**
    * Store the given entry on disk.
    */
   protected void store(DFSFilename key) {
     copyToTempFile(key, null);
 
     doStore(store.get(key));
     
     deleteTempFile();
   }
 
   protected void doStore(CacheEntry entry) {
     try {
       PersistentStorageWriter writer;
       try {
         writer = owner.getWriter(entry.getKey().getPath(), false);
       } catch (IOException ioe) {
         System.err.println("Cannot overwrite cached file " + 
                            entry.getKey().toString() + "!");
         throw new IllegalStateException("Cannot write to cache!",
                                         ioe);
       }
 
       String data = entry.getData() != null ? entry.getData() : "";
       writer.write(entry.toFileHeader() + "\n" + data);
     } catch (IOException e) {
       throw new IllegalStateException("Cannot write cache data to disk!", e);
     }
   }
 
   /**
    * Append the given line to the dirty log.
    */
   protected void log(String line) {
     try {
       PersistentStorageWriter logWriter = owner.getWriter(kLogFileName, true);
       logWriter.write(line + "\n");
     } catch (IOException ioe) {
       System.err.println("While appending to dirty log:");
       ioe.printStackTrace();
       throw new IllegalStateException("Cannot append to dirty log!", ioe);
     }
   }
 
   /**
    * Returns the state of the given cache entry. Returns INVALID if the given
    * entry does not exist.
    */
   public CacheState getState(DFSFilename key) {
     load(key);
     CacheEntry entry = store.get(key);
     if (entry == null)
       return CacheState.INVALID;
 
     return entry.getState();
   }
 
   /**
    * Get a cache entry for the given key. If an entry doesn't exist, one is
    * created in the INVALID state and returned.
    */
   public CacheEntry get(DFSFilename key) {
     load(key);
     return store.get(key);
   }
 
   /**
    * Set data with the cache entry keyed by key to data. This constitutes
    * a write to the cache, and requires the entry to be both owned and read-
    * write. An IllegalStateException is thrown if not.
    *
    * This increments the version, sets the entry to exist, and sets the
    * entry dirty.
    */
   public void update(DFSFilename key, String data) {
     CacheEntry entry = store.get(key);
     if (entry == null)
       throw new IllegalStateException("Entry is currently invalidated!");
 
     if (!entry.getState().equals(CacheState.READ_WRITE) || entry.getOwner() != owner.addr)
       throw new IllegalStateException("Entry is not currently read-write!");
 
     entry.version.incrementRevision();
     entry.data = data;
     entry.exists = true;
 
     store(key);
     setDirty(key);
   }
 
   /**
    * Set the cache entry to reflect the arguments given. This marks the entry
    * clean and sets its state to READ_ONLY.
    */
   public void set(DFSFilename key, FileVersion version, String data, int owner, boolean exists) {
     set(key, version, data, owner, exists, true);
   }
 
   /**
    * Set the cache entry to reflect the arguments given. This marks the entry
    * clean and sets its state to READ_ONLY. If storeOverride is false, no data
    * will be written to disk. Else data will be written to disk if this cache is
    * a master cache
    *
    * @param key Cache key to set
    * @param version Version associated with arguments to this method
    * @param data Data contained by the key
    * @param owner Owner of the key
    * @param exists True if the file specified by the key exists at the given version.
    * @param storeOverride False to disable writing data to disk under any circumstances.
    */
   protected void set(DFSFilename key, 
                      FileVersion version, 
                      String data, 
                      int owner, 
                      boolean exists, 
                      boolean storeOverride) {
     CacheEntry entry = store.get(key);
     if (entry == null) {
       entry = new CacheEntry(key, data, CacheState.READ_ONLY, version, exists, owner, new Flags());
       store.put(key, entry);
     }
 
     entry.state = CacheState.READ_ONLY;
     entry.version = version;
     entry.data = data;
     entry.owner = owner;
     entry.exists = exists;
 
     setClean(key);
     if (isMaster && storeOverride)
       store(key);
   }
 
   /**
    * Set the cache entry's state to READ_WRITE and set the owner to the address
    * of the node passed to the constructor. Ensures that the entry is versioned
    * at @p version.
    *
    * @param key Key of the entry to take ownership of.
    * @param version Version expected in the cache.
    */
   public void takeOwnership(DFSFilename key, FileVersion version) {
     CacheEntry entry = store.get(key);
     if (entry == null || entry.getState().equals(CacheState.INVALID))
       throw new IllegalStateException("Cache entry is invalid!");
 
     if (entry.getState().equals(CacheState.READ_WRITE))
       return;
 
     if (!entry.getVersion().equals(version))
       throw new IllegalStateException("Cached version doesn't match!");
 
    System.err.println("TO, ver=" + entry.version);
     entry.state = CacheState.READ_WRITE;
     entry.owner = owner.addr;
 
     if (isMaster)
       store(key);
   }
 
   /**
    * Rename the given file in the cache to the given filename.
    *
    * @param from existing file
    * @param to new file name.
    */
   public void rename(DFSFilename from, DFSFilename to) {
     load(from);
     CacheEntry entry = store.get(from);
     if (entry == null || entry.getState().equals(CacheState.INVALID))
       throw new IllegalArgumentException("File not found!");
 
     CacheEntry destEntry = new CacheEntry(entry.key,
                                           "",
                                           entry.state,
                                           entry.version,
                                           false,
                                           entry.owner,
                                           entry.flags);
 
     copyToTempFile(destEntry.getKey(), "");
     
     destEntry.exists = entry.exists;
     destEntry.data = entry.data;
 
     doStore(destEntry);
 
     entry.exists = false;
     entry.data = "";
     doStore(entry);
 
     // TODO(andrew): not quite fault-tolerant.
     store.remove(entry);
     entry.key = to;
     store.put(to, entry);
   }
     
 
   /**
    * Set the cache entry's ownership to kNotOwned. Expects the entry's
    * version to be equal to @p version.
    *
    * @param key Cache key to relinquish ownership on.
    * @param version Expected version of the cache entry.
    * @throws IllegalStateException if version doesn't match or the item
    *       is not currently owned.
    */
   public void relinquishOwnership(DFSFilename key, FileVersion version) {
     CacheEntry entry = store.get(key);
     
     if (entry == null || entry.getState().equals(CacheState.INVALID))
       throw new IllegalStateException("Cache entry is invalid!");
 
     entry.state = CacheState.READ_ONLY;
     entry.owner = kNotOwned;
 
     if (isMaster || entry.isDirty())
       store(key);
   }
 
   /**
    * Forget the cache entry associated with the given key. Marks the entry
    * as invalid and deletes it from disk.
    *
    * @param key Key of the cache entry to forget.
    * @throws IllegalStateException if the cache key is invalid.
    */
   public void forget(DFSFilename key) {
     load(key);
     CacheEntry entry = store.get(key);
     if (entry == null)
       throw new IllegalStateException("Cache entry is invalid!");
     
     try {
       PersistentStorageWriter writer = owner.getWriter(key.toString(), false);
       writer.delete();
     } catch (IOException e) {
       throw new IllegalStateException("Error forgetting cache entry: " + key,
                                       e);
     }
 
     entry.state = CacheState.INVALID;
     entry.data = null;
     entry.owner = kNotOwned;
     entry.exists = false;
   }
 
   /**
    * Mark the given entry as deleted. This requires the entry be READ_WRITE
    * and that the node passed to the constructor own the cache entry.
    * Throws an IllegalStateException if these constraints are not met.
    *
    * This increments the version number, sets the entry dirty, sets its
    * data buffer to null, and writes these data to disk.
    *
    * @param key Cache key to delete.
    * @throws IllegalStateException if the cache entry is invalid or not
    *       owned by you.
    */
   public void delete(DFSFilename key) {
     load(key);
     CacheEntry entry = store.get(key);
     if (entry == null)
       throw new IllegalStateException("Cache entry is invalid!");
 
     if (!entry.getState().equals(CacheState.READ_WRITE))
       throw new IllegalStateException("Cache entry is not writable!");
 
     entry.version.incrementRevision();
     entry.data = null;
     entry.exists = false;
     store(key);
     setDirty(key);
   }
 
   /**
    * Create an entry in the cache that is unowned. Intended for use on
    * the server. If the entry was marked deleted, reuses the version numbers
    * so clients are consistent.
    *
    * @param key Cache key to create.
    * @throws IllegalStateException if the cache is not a master cache.
    */
   public void create(DFSFilename key) {
     if (!isMaster)
       throw new IllegalStateException("Cannot create a file in a slave cache!");
     
     load(key);
     CacheEntry entry = store.get(key);
     if (entry.getState().equals(CacheState.INVALID)) {
       entry.state = CacheState.READ_WRITE;
     }
     entry.data = "";
     entry.owner = kNotOwned;
     entry.exists = true;
     entry.version = entry.version.nextVersion();
     store(key);
   }
 
   /**
    * Mark a cache entry dirty. The key must exist.
    * 
    * @param key Cache key to set dirty.
    * @throws IllegalArgumentException if the cache entry does not exist.
    */
   public void setDirty(DFSFilename key) {
     setDirty(key, true);
   }
 
   protected void setDirty(DFSFilename key, boolean writeLog) {
     CacheEntry entry = store.get(key);
     if (entry == null)
       throw new IllegalArgumentException("No such key!");
 
     if (!entry.isDirty()) {
//      FileVersion v = entry.getVersion();
//      v.incrementRevision(); //dirty
//      entry.setVersion(v);
       dirtyEntries.add(entry.key);
       if (writeLog)
         log("+" + key.toString());
     }
   }
 
   /**
    * Mark a cache entry clean. The key must exist.
    * 
    * @param key Cache key to set clean.
    * @throws IllegalArgumentException if the cache entry does not exist.
    */
   public void setClean(DFSFilename key) {
     setClean(key, true);
   }
 
   protected void setClean(DFSFilename key, boolean writeLog) {
     CacheEntry entry = store.get(key);
     if (entry == null)
       throw new IllegalArgumentException("No such key!");
 
     if (entry.isDirty()) {
      // FUCK.
//      FileVersion v = entry.getVersion();
//      v.clearRevision();
//      entry.setVersion(v);
       dirtyEntries.remove(entry.key);
       if (writeLog)
         log("-" + key.toString());
       
       if (isMaster) {
         try {
           PersistentStorageWriter writer = owner.getWriter(key.getPath(), false);
           writer.delete();
         } catch (IOException e) {
           throw new IllegalStateException("Cannot unlink now-clean cache entry from disk!", e);
         }
       }
     }
   }
 
   /**
    * Returns an iterator of dirty files.
    *
    * @return an iterator of dirty files.
    */
   public Iterator<DFSFilename> dirtyIterator() {
     return dirtyEntries.iterator();
   }
 }
