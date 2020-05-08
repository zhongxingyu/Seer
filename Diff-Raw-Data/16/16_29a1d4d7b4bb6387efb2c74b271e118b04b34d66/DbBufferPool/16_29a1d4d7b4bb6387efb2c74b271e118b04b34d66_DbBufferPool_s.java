 package edu.jhu.cs.damsl.engine.storage;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 import edu.jhu.cs.damsl.catalog.Defaults;
 import edu.jhu.cs.damsl.catalog.Defaults.SizeUnits;
 import edu.jhu.cs.damsl.catalog.identifiers.FileId;
 import edu.jhu.cs.damsl.catalog.identifiers.PageId;
 import edu.jhu.cs.damsl.catalog.identifiers.TupleId;
 import edu.jhu.cs.damsl.engine.storage.BaseBufferPool;
 import edu.jhu.cs.damsl.engine.storage.accessor.FileAccessor;
 import edu.jhu.cs.damsl.engine.storage.file.StorageFile;
 import edu.jhu.cs.damsl.engine.storage.page.Page;
 import edu.jhu.cs.damsl.engine.storage.page.PageHeader;
import edu.jhu.cs.damsl.engine.utils.LRUCache;
 import edu.jhu.cs.damsl.factory.page.PageFactory;
 
 public class DbBufferPool<EntityIdType,
                           IdType     extends TupleId,
                           HeaderType extends PageHeader,
                           PageType   extends Page<IdType, HeaderType>,
                           FileType   extends StorageFile<IdType, HeaderType, PageType>>
                 extends BaseBufferPool<IdType, HeaderType, PageType>
 {
   // Page cache, tracking used buffer pool pages in access order.
   LinkedHashMap<PageId, PageType> pageCache;
   
   // Cache content summary, by relation.
   // This (partially) tracks which pages are in the cache for each relation.
   HashMap<EntityIdType, LRUCache<FileId, PageId>> cachedRelations;
 
   // Interface to read and write pages from files.
   FileAccessor<EntityIdType, IdType, HeaderType, PageType> fileAccessor;
 
   // Buffer pool statistics.
   long numRequests;
   long numHits;
   long numEvictions;
   long numFailedEvictions;
 
   public DbBufferPool(PageFactory<IdType, HeaderType, PageType> factory,
                       FileAccessor<EntityIdType, IdType, HeaderType, PageType> fAccess,
                       Integer bufferSize, SizeUnits bufferUnit,
                       Integer pageSize, SizeUnits pageUnit)
   {
     super(factory, bufferSize, bufferUnit, pageSize, pageUnit);
     fileAccessor = fAccess;
     pageCache = new LinkedHashMap<PageId, PageType>();
     cachedRelations = new HashMap<EntityIdType, LRUCache<FileId, PageId>>();
     numRequests = numHits = numEvictions = numFailedEvictions = 0;
   }
 
   // Pull the page into the buffer pool, evicting pages as needed.
   // This method is thread-safe.
   public PageType getPage(PageId id) {
     PageType p = null;
 
     numRequests += 1;
 
     // Each thread will get a unique page buffer into which data is read.
     synchronized(this) {
       p = pageCache.get(id);
       if ( p != null ) { numHits += 1; return p; }
     }
 
     // Take a page from the free list if one is available, otherwise evict
     // a cached page.
     p = getPageIfReady();
     if ( p == null ) { p = evictPage(); }
 
     // If neither option above yields a page, block on the free page list.
     // We must not be synchronized on the buffer pool at this point, otherwise
     // we may deadlock.
     if ( p == null ) {
       try {
         p = getPage();
       } catch (InterruptedException e) { e.printStackTrace(); }
     }
     
     // We should now have a page we can use. From here on, we only need to
     // synchronize cache updates since each concurrent caller has a unique page.
     if ( p != null ) {
       PageType returnPage = null;
 
       // The file manager returns a page instance according to the given type
       // of file (e.g. an index file or storage file), that is backed by the
       // page 'p' acquired above. This ensures the getPage(id) method returns
       // an appropriately specialized page rather than a generic one.
       PageType readPage = fileAccessor.readPage(p, id);
       if ( readPage != null ) {
         synchronized(this) {
           if ( !pageCache.containsKey(id) ) {
             p = readPage;
             pageCache.put(id, readPage);
           } else {
             // Another thread may have concurrently retrieved the page, and added
             // it to the cache. Return our page to the free list.
             returnPage = p;
             p = pageCache.get(id);
           }
         }
       } else {
         // Return our page to the free list on any read failure.
         returnPage = p;
         p = null;
       }
       
       if ( returnPage != null ) { releasePage(returnPage); }
     } else {
       logger.warn("no suitable page found to read page id {}", id);
     }
     return p;
   }
 
   // Return a cached page to the free list without flushing it, regardless
   // of whether the page is dirty.
   // This method is thread-safe.
   public void discardPage(PageId id) {
     PageType p = null;
     synchronized (this) {
       p = pageCache.get(id);
       pageCache.remove(id);
     }
     if ( p != null ) { releasePage(p); }
   }
 
   // Cache eviction policy, yielding the page that has just been evicted for
   // reuse, without putting it back on the free page list.
   // This method implements a NO-STEAL policy, that is we cannot evict dirty
   // pages. Thus the method returns null if there is no suitable page available.
   // This method is not thread-safe, thread-safety is left to the caller.
   PageType evictPage() {
     PageType r = null;
     PageId id = null;
     // Iterate in insertion order with LinkedHashMap.
     for (Map.Entry<PageId, PageType> e : pageCache.entrySet()) {
       if ( !e.getValue().isDirty() ) { id = e.getKey(); break; }
     }
 
     if ( id == null ) {
       Iterator<PageId> keyIt = pageCache.keySet().iterator();
       if ( keyIt.hasNext() ) { 
         id = keyIt.next();
         flushPage(id);
         r = getPageIfReady();
       }
     } else {
       r = pageCache.get(id);
       pageCache.remove(id);
     }
     if ( r != null ) { numEvictions += 1; }
     else { numFailedEvictions += 1; }
     return r;
   }
   
   // Flush a cached page to disk if it is dirty, return to the free list.
   // This method is thread-safe.
   public void flushPage(PageId id) {
     PageType p = null;
 
     // At most one thread can have a handle to the page to be flushed.
     synchronized(this) {
       p = pageCache.get(id);
       if ( p != null ) {
         if ( p.isDirty() ) {
           // Reset the dirty bit so that no other page gets a handle.
           // Note the page is still in the cache, thus if the write fails,
           // we have not lost data.
           p.setDirty(false);
         }
         else { p = null; }
       }
     }
 
     if ( p != null ) {
       int written = fileAccessor.writePage(p);
       if ( written == getPageSize() ) {
         // Remove the page from the cache as long as no other thread has
         // updated the page in the meantime.
         PageType returnPage = null;
         synchronized(this) { 
           if ( !p.isDirty() ) {
             pageCache.remove(id);
             returnPage = p;
           }
         }
         if ( returnPage != null ) { releasePage(returnPage); }
       }
       else {
         logger.warn("failed to flush page {}, retaining", id);
       }
     }
   }
   
   // Flush all dirty pages to disk.
   public void flushPages() {
     LinkedList<PageId> keys = new LinkedList<PageId>();
     synchronized(this) { keys.addAll(pageCache.keySet()); }
     for (PageId id : keys) { flushPage(id); }
   }
 
   // Buffered data access.
 
   public PageId getWriteablePage(EntityIdType rel, short requestedSpace) {
     PageId r = null;
     LRUCache<FileId, PageId> cached = null;
     synchronized(this) { 
       cached = cachedRelations.get(rel);
       if ( cached != null ) {
         for (PageId id : cached.values()) {
           PageType p = pageCache.get(id);
           if ( p != null && p.getHeader().getFreeSpace() > requestedSpace ) {
             r = id;
             break;
           }
         }
       }
     }
 
     if ( r == null ) {
       r = fileAccessor.getWriteablePage(
             rel, requestedSpace, cached == null? null : cached.values());
 
       // Create a cached page corresponding to an extended file page.
       if ( r == null ) {
         PageType p = getPageIfReady();
         if ( p == null ) { p = evictPage(); }
         if ( p != null ) {
           r = fileAccessor.extendFile(rel, p, requestedSpace);
           synchronized(this) {
             pageCache.put(r, p);
             if ( cached != null ) { cached.put(r.fileId(), r); }
             else {
               cached = new LRUCache<FileId, PageId>(Defaults.defaultPoolContentCacheSize);
               cached.put(r.fileId(), r);
             }
             cachedRelations.put(rel, cached);
           }
         }
       }
     }
     return r;
   }
 
   public String toString() {
     double hitRate = Long.valueOf(numHits).doubleValue() / Long.valueOf(numRequests).doubleValue();
 
     String r = "hits: "              + (numRequests > 0? Double.toString(hitRate) : "<>") +
                " requests: "         + Long.toString(numRequests) +
                " hits: "             + Long.toString(numHits) +
                " evictions: "        + Long.toString(numEvictions) +
                " failed evictions: " + Long.toString(numFailedEvictions) + "\n";
 
     r += "free pages: " + Integer.toString(getNumFreePages()) +
          " total pages: " + Integer.toString(getNumPages());
 
     return r;
   }
 
 }
