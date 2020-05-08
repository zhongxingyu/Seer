 
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
     int version;
     CacheState state;
     boolean exists;
     boolean dirty;
     int owner;
     Flags flags;
 
     public CacheEntry(DFSFilename key, String data, CacheState state, int version, boolean exists, int owner, Flags flags) {
       this.key = key;
       this.data = data;
       this.state = state;
       this.version = version;
       this.exists = exists;
       this.owner = owner;
       this.flags = flags;
       this.dirty = false;
     }
 
     public DFSFilename getKey() {
       return key;
     }
 
     public String getData() {
       return data;
     }
 
     public boolean isDirty() {
       return dirty;
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
 
     public int getVersion() {
       return version;
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
       entry = new CacheEntry(key, null, CacheState.INVALID, 0, knownInMaster, kNotOwned, new Flags());
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
 
       int version = Integer.parseInt(parts[0]);
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
       setDirty(key);
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
 
   /**
    * Store the given entry on disk.
    */
   protected void store(DFSFilename key) {
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
       PersistentStorageReader reader = owner.getReader(key.getPath());
       if (reader.ready()) {
         String oldData = component.readRemainingContentsToString(reader);
         tempWriter.write(key.getPath() + "\n" + oldData);
         tempWriter.close();
       } else {
         tempWriter.delete();
       }
     } catch (FileNotFoundException e) {
     } catch (IOException e) {
       throw new IllegalStateException("Cannot copy old file content to temp file!",
                                       e);
     }
 
     try {
       PersistentStorageWriter writer;
       try {
         writer = owner.getWriter(key.getPath(), false);
       } catch (IOException ioe) {
         System.err.println("Cannot overwrite cached file " + key.toString() + "!");
         throw new IllegalStateException("Cannot write to cache!",
                                         ioe);
       }
 
       CacheEntry entry = store.get(key);
      writer.write(entry.getVersion() + "," + entry.exists() + "\n" + entry.getData().toString());
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
 
     entry.version++;
     entry.data = data;
     entry.exists = true;
 
     store(key);
     setDirty(key);
   }
 
   /**
    * Set the cache entry to reflect the arguments given. This marks the entry
    * clean and sets its state to READ_ONLY.
    */
   public void set(DFSFilename key, int version, String data, int owner, boolean exists) {
     set(key, version, data, owner, exists, true);
   }
 
   public void set(DFSFilename key, int version, String data, int owner, boolean exists, boolean storeOverride) {
     CacheEntry entry = store.get(key);
     if (entry == null) {
       entry = new CacheEntry(key, data, CacheState.READ_ONLY, version, exists, owner, new Flags());
       store.put(key, entry);
     }
 
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
   public void takeOwnership(DFSFilename key, int version) {
     CacheEntry entry = store.get(key);
     if (entry == null || entry.getState().equals(CacheState.INVALID))
       throw new IllegalStateException("Cache entry is invalid!");
 
     if (entry.getState().equals(CacheState.READ_WRITE))
       return;
 
     if (entry.getVersion() != version)
       throw new IllegalStateException("Cached version doesn't match!");
 
     entry.state = CacheState.READ_WRITE;
     entry.owner = owner.addr;
   }
 
   /**
    * Mark the given entry as deleted. This requires the entry be READ_WRITE
    * and that the node passed to the constructor own the cache entry.
    * Throws an IllegalStateException if these constraints are not met.
    *
    * This increments the version number, sets the entry dirty, sets its
    * data buffer to null, and writes these data to disk.
    */
   public void delete(DFSFilename key) {
     CacheEntry entry = store.get(key);
     if (entry == null)
       throw new IllegalStateException("Cache entry is invalid!");
 
     if (!entry.getState().equals(CacheState.READ_WRITE))
       throw new IllegalStateException("Cache entry is not writable!");
 
     entry.version++;
     entry.data = null;
     entry.exists = false;
     store(key);
     setDirty(key);
   }
 
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
     entry.version++;
     store(key);
   }
 
   public void setDirty(DFSFilename key) {
     setDirty(key, true);
   }
 
   protected void setDirty(DFSFilename key, boolean writeLog) {
     CacheEntry entry = store.get(key);
     assert entry != null;
 
     if (!entry.dirty) {
       entry.dirty = true;
       dirtyEntries.add(entry.key);
       if (writeLog)
         log("+" + key.toString());
     }
   }
 
   public void setClean(DFSFilename key) {
     setClean(key, true);
   }
 
   protected void setClean(DFSFilename key, boolean writeLog) {
     CacheEntry entry = store.get(key);
     assert entry != null;
 
     if (entry.dirty) {
       entry.dirty = false;
       dirtyEntries.remove(entry.key);
       if (writeLog)
         log("-" + key.toString());
       
       if (isMaster) {
         try {
           PersistentStorageWriter writer = owner.getWriter(key.toString(), false);
           writer.delete();
         } catch (IOException e) {
           throw new IllegalStateException("Cannot unlink now-clean cache entry from disk!", e);
         }
       }
     }
   }
 
   public Iterator<DFSFilename> dirtyIterator() {
     return dirtyEntries.iterator();
   }
 }
