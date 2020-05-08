 package edu.jhu.cs.damsl.engine.storage;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.jhu.cs.damsl.catalog.Addressable;
 import edu.jhu.cs.damsl.catalog.Durable;
 import edu.jhu.cs.damsl.catalog.Defaults;
 import edu.jhu.cs.damsl.catalog.Schema;
 import edu.jhu.cs.damsl.catalog.identifiers.FileId;
 import edu.jhu.cs.damsl.catalog.identifiers.FileId.FileKind;
 import edu.jhu.cs.damsl.catalog.identifiers.IndexId;
 import edu.jhu.cs.damsl.catalog.identifiers.PageId;
 import edu.jhu.cs.damsl.catalog.identifiers.TableId;
 import edu.jhu.cs.damsl.catalog.identifiers.TupleId;
 import edu.jhu.cs.damsl.engine.storage.accessor.FileAccessor;
 import edu.jhu.cs.damsl.engine.storage.file.StorageFile;
 import edu.jhu.cs.damsl.engine.storage.index.Index;
 import edu.jhu.cs.damsl.engine.storage.index.IndexManager;
 import edu.jhu.cs.damsl.engine.storage.iterator.file.header.StorageFileHeaderIterator;
 import edu.jhu.cs.damsl.engine.storage.iterator.index.IndexIterator;
 import edu.jhu.cs.damsl.engine.storage.iterator.page.StorageIterator;
 import edu.jhu.cs.damsl.engine.storage.iterator.page.WrappedStorageIterator;
 import edu.jhu.cs.damsl.engine.storage.iterator.util.MultiplexedListIterator;
 import edu.jhu.cs.damsl.engine.storage.page.Page;
 import edu.jhu.cs.damsl.engine.storage.page.PageHeader;
 import edu.jhu.cs.damsl.engine.transactions.TransactionAbortException;
import edu.jhu.cs.damsl.utils.LRUCache;
 import edu.jhu.cs.damsl.factory.file.StorageFileFactory;
 
 
 /**
  * Manages a collection of files belonging to the database.
  *
  * A FileManager provides an interface to the files which hold the data in the database. Files can
  * be added directly, or as part of a relation. Files in turn contain pages, the reading and writing
  * of which are managed by a buffer pool.
  *
  * Note this class is not thread-safe. Callers must implement table-level
  * locking to ensure consistency with multi-threaded relation manipulation.
  */
 public class FileManager<IdType     extends TupleId,
                          HeaderType extends PageHeader,
                          PageType   extends Page<IdType, HeaderType>,
                          FileType   extends StorageFile<IdType, HeaderType, PageType>>
                 implements FileAccessor<TableId, IdType, HeaderType, PageType>
 {
   protected static final Logger logger = LoggerFactory.getLogger(FileManager.class);
 
   // File factory
   StorageFileFactory<IdType, HeaderType, PageType, FileType> fileFactory;
 
   // Relations.
   HashMap<FileId, FileType> dbFiles;
   
   // Cache page headers as they are written out, that is cache headers
   // of pages on disk. Cache entries should be invalidated as pages are
   // read back in (at which point they may be updated in memory).
   // We use a ConcurrentSkipListMap since a ConcurrentHashMap is not cloneable.
   ConcurrentSkipListMap<TableId, LRUCache<PageId, HeaderType>> cachedHeaders;
 
   // Index manager
   IndexManager<IdType, HeaderType, PageType, FileType> idxMgr;
 
   public FileManager(StorageFileFactory<IdType, HeaderType, PageType, FileType> f)
   {
     fileFactory = f;
     dbFiles = new HashMap<FileId, FileType>();
     cachedHeaders = new ConcurrentSkipListMap<
         TableId, LRUCache<PageId, HeaderType>>(
           new Comparator<TableId>() {
             public int compare(TableId a, TableId b) { 
               return Integer.valueOf(a.getAddress()).compareTo(b.getAddress());
             }
           }
         );
 
     idxMgr = new IndexManager<IdType, HeaderType, PageType, FileType>(f);
   }
 
   @SuppressWarnings("unchecked")
   private FileManager(FileManager<IdType, HeaderType, PageType, FileType> f)
   {
     fileFactory = f.fileFactory;
     dbFiles = (HashMap<FileId, FileType>) f.dbFiles.clone();
     cachedHeaders = 
       (ConcurrentSkipListMap<TableId, LRUCache<PageId, HeaderType>>)
         f.cachedHeaders.clone();
     idxMgr = f.idxMgr.getShadow();
   }
 
   public FileManager<IdType, HeaderType, PageType, FileType> getShadow() {
     return new FileManager<IdType, HeaderType, PageType, FileType>(this); 
   }
 
   // Basic accessors.
   public IndexManager<IdType, HeaderType, PageType, FileType>
   getIndexManager() {
     return idxMgr;
   }
 
   // Reporting.
   public String toString() {
     String r = "";
     for ( Map.Entry<FileId, FileType> e : dbFiles.entrySet() ) {
       r += e.getKey().toString() + ": " + e.getValue().toString() + "\n";
     }
     return r;
   }
 
   // File management helpers.
   <IdType extends Durable>
   FileId addFile(HashMap<FileId, FileType> files, IdType id, FileKind k,
                  Integer pageSize, Long fileCapacity, Schema sch)
     throws FileNotFoundException
   {
     FileId r = null;
     try {
       FileType f = fileFactory.getFile(k, sch);
       List<FileId> fList = id.files();
       r = f.fileId();
       fList.add(r);
       files.put(r, f);
     } catch (FileNotFoundException e) { e.printStackTrace(); }
     
     return r;
   }
   
   FileId addFile(TableId rel, Integer pageSize, Long fileCapacity, Schema sch)
     throws FileNotFoundException
   {
     FileId r = addFile(dbFiles, rel, FileKind.Relation, pageSize, fileCapacity, sch);
     dbFiles.get(r).setRelation(rel);
     return r;
   }
 
   void loadFile(HashMap<FileId, FileType> files, TableId rel, FileId id, Schema sch)
   {
     String filePath = id.file().getAbsolutePath();
     try {
       FileType f = fileFactory.getFile(id, sch, rel);
       files.put(f.fileId(), f);
     } catch (FileNotFoundException e) { e.printStackTrace(); }
   }
 
   <IdType extends Durable>
   void removeFile(HashMap<FileId, FileType> files, IdType id, FileId f) {
     List<FileId> rf = id.files();
     if ( rf != null && rf.contains(f) ) {
       File fPath = f.file();
       FileType s = files.get(f);
       if ( s != null && fPath.exists() && fPath.canWrite() ) {
         rf.remove(f);
         files.remove(f);
         fPath.delete();
       }
     } else {
       logger.error("expected table {} to use file {}", id, f);
     }
   }
   
   void removeFile(TableId rel, FileId f) {
     removeFile(dbFiles, rel, f);
     evictHeadersForFile(rel, f); 
   }
 
   // Database file modifiers.
   // Catalog modification happens elsewhere.
   
   public void addRelation(TableId rel, Schema sch)
     throws FileNotFoundException
   {
     List<FileId> files = rel.files();
     if ( files == null || files.isEmpty() ) {
       addFile(rel, fileFactory.getPageSize(), Defaults.getDefaultFileSize(), sch);
     } else {
       logger.warn("found duplicate db files for relation {}", files);
       rel = null;
     }
   }
 
   public void loadRelation(TableId rel, Schema sch) {
     List<FileId> files = rel.files();
     if ( files != null ) {
       for (FileId id : files) { loadFile(dbFiles, rel, id, sch); }
     }
   }
 
   public void clearRelation(TableId rel) {
     List<FileId> files = rel.files();
     if ( files == null || files.isEmpty() ) {
       logger.warn("no db files found for relation {}", files);
     } else {
       for (FileId fId : files) { removeFile(rel, fId); }
     }
   }
 
   public void removeRelation(TableId rel) {
     clearRelation(rel);
     List<FileId> rf = rel.files();
     if ( rf != null && rf.isEmpty() ) { 
       cachedHeaders.remove(rel);
     }
     else { logger.warn("found ghost db files for relation {}", rel); }
   }
 
   // Table accessors.
   
   // Returns a multiplexed iterator that directly uses a page allocator (i.e.,
   // the buffer pool), to bypass any page caching.
   public StorageIterator scanRelation(TableId rel) {
     LinkedList<StorageIterator> iterators = new LinkedList<StorageIterator>();
     for ( FileType f : getFiles(rel) ) { 
       iterators.add(f.iterator()); 
     }
 
     return new WrappedStorageIterator(
         new MultiplexedListIterator<Tuple, StorageIterator>(iterators));
   }
 
   // Files.  
   public LinkedList<FileType> getFiles(TableId rel) {
     LinkedList<FileType> r = null;
     List<FileId> files = rel.files();
     if ( files != null && files.size() > 0 ) {
       r = new LinkedList<FileType>();
       for (FileId fId : files) {
         FileType f = dbFiles.get(fId);
         if ( f != null ) r.add(f);
       }
     }
     return r;
   }
   
   public boolean isRelationFile(FileId fileId) {
     return dbFiles.containsKey(fileId);
   }
   
   public TableId getFileRelation(FileId fileId) {
     FileType f = dbFiles.get(fileId);
     return ( f == null ? null : f.getRelation());
   }
   
 
   // Pages.
   public PageType readPage(PageType buf, PageId id) {
     PageType r = null;
     FileId fId = id.fileId();
     if ( fId != null ) {
       boolean relFile = isRelationFile(fId);
       FileType f = relFile ? dbFiles.get(fId) : null;
       if ( f != null ) {
         r = buf;
         int read = f.readPage(r, id);
         if ( read != r.capacity() ) r = null;
         
         // Evict the page header from the cache since it is likely to be updated.
         if ( relFile ) evictHeader(id);
       }
     }
     return r;
   }
 
   public int writePage(PageType page) {
     int r = 0;
     FileId fId = page.getId().fileId();
     if ( fId != null ) {
       boolean relFile = isRelationFile(fId);
       FileType f = relFile ? dbFiles.get(fId) : null;
       if ( f != null ) {
         r = f.writePage(page);
         // Cache the disk-based page header.
         if ( relFile ) { cacheHeader(page.getId(), page); }
       }
     }
     return r;
   }
 
   public PageId getWriteablePage(
       TableId rel, short requestedSpace, Collection<PageId> cached)
   {
     PageId r = null;
     
     // Find a page with sufficient free space based on cached headers.
     LRUCache<PageId, HeaderType> diskPages = cachedHeaders.get(rel);
     if ( diskPages != null ) {
       synchronized (diskPages) {
         for (Map.Entry<PageId, HeaderType> e : diskPages.entrySet()) {
           HeaderType h = e.getValue();
           if ( ( cached == null || !cached.contains(e.getKey()) )
                 && h.isSpaceAvailable(requestedSpace) ) 
           {
             r = e.getKey();
             break;
           }
         }
       }
     }
     if ( r != null ) return r;
 
     // Next, search through disk page headers.
     // This operation could be extremely expensive and should be a rare event.
     List<FileId> files = rel.files();
     if ( files != null ) {
       Iterator<FileId> fileIt = files.iterator();
       while ( r == null && fileIt.hasNext() ) {
         FileType f = dbFiles.get(fileIt.next());
         StorageFileHeaderIterator<IdType, HeaderType, PageType> hIt = f.header_iterator();
         while ( r == null && hIt.hasNext() ) {
           PageId id = hIt.getPageId();
           HeaderType h = hIt.next();
           if ( h != null && ( cached == null || !cached.contains(id) )
               && h.isSpaceAvailable(requestedSpace) )
           { r = id; }
         }
       }
     }
     
     return r;
   }
 
   public PageId extendFile(TableId rel, PageType buf, short requestedSpace) {
     PageId r = null;
     List<FileId> files = rel.files();
     if ( files != null ) {
       Iterator<FileId> fileIt = files.iterator();
       while ( r == null && fileIt.hasNext() ) {
         FileType f = dbFiles.get(fileIt.next());
         if ( f != null && f.remaining() > requestedSpace ) {
           r = new PageId(f.fileId(), f.numPages());
           f.extend(1);
           f.initializePage(buf);
           buf.setId(r);
         }
       }
     }
     
     return r;
   }
 
   
   // Header cache maintenance
   Long getDefaultCacheSize() { return 0L; }
 
   void evictHeader(PageId id) {
     LRUCache<PageId, HeaderType> cache =
         cachedHeaders.get(getFileRelation(id.fileId()));
     if ( cache != null ) {
       synchronized(cache) { cache.remove(id); }
     }
   }
 
   void evictHeadersForFile(TableId rel, FileId id) {
     LRUCache<PageId, HeaderType> cache = cachedHeaders.get(rel);
     if ( cache != null ) {
       synchronized (cache) {
         for ( Map.Entry<PageId, HeaderType> e : cache.entrySet() ) {
           if ( e.getKey().fileId() == id ) { cache.remove(e.getKey()); }
         }
       }
     }
   }
 
   void cacheHeader(PageId id, PageType p) {
     TableId rel = getFileRelation(id.fileId());
     LRUCache<PageId, HeaderType> cache = cachedHeaders.get(rel);
     if ( cache == null ) {
       cache = new LRUCache<PageId, HeaderType>(getDefaultCacheSize());
       cachedHeaders.putIfAbsent(rel, cache);
     }
     synchronized(cache) { 
       if ( !cache.containsKey(id) ) { cache.put(id, p.getHeader()); }
     }
   }
 
 
   // Index manager proxy methods.
 
   public void addIndex(TableId rel, IndexId idx) 
     throws FileNotFoundException
   {
     LinkedList<FileType> files = getFiles(rel);
     idxMgr.addIndex(rel, idx, files);
   }
   
   public void removeIndex(IndexId id) {
     idxMgr.removeIndex(id);
   }
 
   public void loadIndex(IndexId id, FileId fileId) {
     if ( isRelationFile(fileId) ) { 
       FileType file = dbFiles.get(fileId);
       if ( file != null ) { idxMgr.loadIndex(id, file); }
     }
   }
   
   public IndexIterator<IdType> scanIndex(IndexId id) {
     IndexIterator<IdType> r = null;
     Index<IdType> idx = idxMgr.getIndex(id);
     if ( idx != null ) { r = idx.scan(); }
     return r;
   }
   
   public IndexIterator<IdType> seekIndex(IndexId id, Tuple key) 
     throws TransactionAbortException
   {
     IndexIterator<IdType> r = null;
     Index<IdType> idx = idxMgr.getIndex(id);
     if ( idx != null ) { r = idx.seek(key); }
     return r;
   }
 
   // Index accessors
   public LinkedList<Index<IdType>> getIndexes(List<IndexId> indexes) {
     return idxMgr.getIndexes(indexes);
   }
 }
