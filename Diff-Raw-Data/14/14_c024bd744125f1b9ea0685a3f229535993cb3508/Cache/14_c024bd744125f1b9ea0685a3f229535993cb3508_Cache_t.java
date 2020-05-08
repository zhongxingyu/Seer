 /******************************************************************************
  * Cache.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.cache;
 
 import java.net.*;
 import java.io.*;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Iterator;
 
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.openlaszlo.utils.FileUtils;
 import org.openlaszlo.utils.ChainedException;
 import org.openlaszlo.utils.LZHttpUtils;
 import org.openlaszlo.utils.MathUtils;
 import org.openlaszlo.server.LPS;
 
 import org.openlaszlo.data.Data;
 import org.openlaszlo.data.DataSource;
 import org.openlaszlo.data.DataSourceException;
 
 import org.apache.commons.collections.SequencedHashMap;
 
 import org.apache.log4j.*;
 
 import EDU.oswego.cs.dl.util.concurrent.ReentrantLock;
 import EDU.oswego.cs.dl.util.concurrent.Sync;
 
 /**
  * A base class for maintaining a disk-backed cache of requests.
  *
  * Cache has 2 sequenced hash map of Items.  Each item uniquely
  * represents something to be cached (and transcoded).  The
  * first map contains items that are "in memory".  Items that
  * are "in memory" are also stored on disk.  The second map
  * contains items that are only on disk.
  *
  * The cache chucks out (via LRU) items from each map when it is beyond 
  * its maximum size for that map.  Each time an item is requested,
  * it is moved to the front of the "in-memory" map.
  *
  * Each item has its own lock.  When an item is requested, 
  * we lock it and then go to the DataSource to see if 
  * we need to refresh any copy of it we have.
  * When an item has it's size change or it marked dirty, it
  * is also marked as needing its size changed reckoned with the
  * cache sizes.  Whenever the cache is locked and runs into
  * an item that needs reckoning, it updates the cache sizes
  * to reflect the item.
  *
  * The cache supports multiple encodings of individual items.
  *
  * @author <a href="mailto:bloch@laszlosystems.com">Eric Bloch</a>
  */
 public abstract class Cache {
 
     public final String DEFAULT_DISK_MAX = "500000000";
     public final String DEFAULT_MEM_MAX  = "1000000";
     public final String DEFAULT_MEM_ITEM_MAX  = "0";
 
     /** Logger. */
     private static Logger mLogger = Logger.getLogger(Cache.class);
     private static Logger mLockLogger = Logger.getLogger("trace.cache.lock");
 
     /** Name */
     private final String mName;
 
     /** See the constructor. */
     private File mCacheDirectory;
 
     /** Sequenced Maps of Cached Items keyed by request.  
      * All access to these maps must must be synchronized
      */
     private SequencedHashMap mDiskMap = null;
     private SequencedHashMap mMemMap = null;
     private SequencedHashMap mActiveMap = null;
 
     /** Maxmimum size for the on-disk cache in bytes */
     private long mMaxDiskSize = 0;
 
     /** Maxmimum size for the in-memory cache in bytes */
     private long mMaxMemSize = 0;
 
     /** Maxmimum size for the in-memory cache item in bytes */
     private long mMaxMemItemSize = 0;
 
     /** Current size of the in-disk cache in bytes */
     private long mDiskSize = 0;
 
     /** Current size of the in-memory cache in bytes */
     private long mMemSize = 0;
 
     /** Current cache id */
     private long mCurName = 0;
     private Object mNameLock = new Object();
 
     /**
      * Creates a new <code>Cache</code> instance.
      *
      * @param name
      * @param cacheDirectory a <code>File</code> naming a directory
      * where cache files should be kept
      * @param props
      */
     public Cache(String name, File cacheDirectory, Properties props)
         throws IOException {
 
         mCacheDirectory = cacheDirectory;
         mName = name;
         cacheDirectory.mkdirs();
 
         String maxSize;
         String load;
         String mapsize;
 
         // TODO: 2004-09-07 bloch catch NumberParseException's here
         maxSize = props.getProperty(mName + ".disk.size", DEFAULT_DISK_MAX);
         if (maxSize != null) {
             mMaxDiskSize = Long.parseLong(maxSize);
         }
         maxSize = props.getProperty(mName + ".mem.size", DEFAULT_MEM_MAX);
         if (maxSize != null) {
             mMaxMemSize = Long.parseLong(maxSize);
         }
         maxSize = props.getProperty(mName + ".mem.item.max", DEFAULT_MEM_ITEM_MAX);
         if (maxSize != null) {
             mMaxMemItemSize = Long.parseLong(maxSize);
         }
 
         load = props.getProperty(mName + ".disk.load");
         if (load != null) {
             int l = Integer.parseInt(load);
             mapsize = props.getProperty(mName + ".disk.mapsize");
             if (mapsize != null) {
                 float m = Float.parseFloat(mapsize);
                 mDiskMap = new SequencedHashMap(l, m);
             } else {
                 mDiskMap = new SequencedHashMap(l);
             }
         } else {
             mDiskMap = new SequencedHashMap();
         }
         load = props.getProperty(mName + ".mem.load");
         if (load != null) {
             int l = Integer.parseInt(load);
             mapsize = props.getProperty(mName + ".mem.mapsize");
             if (mapsize != null) {
                 float m = Float.parseFloat(mapsize);
                 mMemMap = new SequencedHashMap(l, m);
             } else {
                 mMemMap = new SequencedHashMap(l);
             }
         } else {
             mMemMap = new SequencedHashMap();
         }
         load = props.getProperty(mName + ".active.load");
         if (load != null) {
             int l = Integer.parseInt(load);
             mapsize = props.getProperty(mName + ".active.mapsize");
             if (mapsize != null) {
                 float m = Float.parseFloat(mapsize);
                 mActiveMap = new SequencedHashMap(l, m);
             } else {
                 mActiveMap = new SequencedHashMap(l);
             }
         } else {
             mActiveMap = new SequencedHashMap();
         }
 
         long t = System.currentTimeMillis();
         loadFromDirectory();
         //        mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="loading " + p[0] + " took " + p[1] + " seconds"
  */
         //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
         //                                Cache.class.getName(),"051018-183", new Object[] {mName, MathUtils.formatDouble((System.currentTimeMillis() - t) / 1000.0, 2)})
         //        );
     }
 
     /**
      * this routine is a place holder which can be overridden to hook into
      * the cache loading.  It is used by the cm/CompilationManager to get
      * at the contents of the dependencyTracker as it is being loaded from
      * disk and modify various paths that might have changed since
      * prefetching.
      */
     protected void afterCacheRead(Object metaData) {} // override me
 
     /**
      * @return an item if it exists or null otherwise
      */
     protected synchronized Item getItem(Serializable key) {
         //        mLogger.debug("getItem");
 
         Item item = (Item)mMemMap.get(key);
         if (item != null) {
             return item;
         }
         return (Item)mDiskMap.get(key);
     }
 
     /**
      * Find and optionally lock item in the cache that matches this key 
      * If the item doesn't exist, create it.  If the item 
      * does exist, move it to the top of the LRU list by removing
      * and re-adding.   
      * @return the locked item
      */
     protected synchronized Item findItem(Serializable key, String enc, boolean doLockAndLeaveActive) 
         throws IOException {
 
         //mLogger.debug("findItem");
 
         SequencedHashMap curMap = mMemMap;
         SequencedHashMap newMap = mMemMap;
 
         boolean hasMemCache = (mMaxMemSize != 0);
 
         if (!hasMemCache) {
             newMap = mDiskMap;
         }
 
         Item item = (Item) curMap.get(key);
         if (item == null) {
             curMap = mDiskMap;
             item = (Item) curMap.get(key);
             if (item == null && doLockAndLeaveActive) {
                 curMap = mActiveMap;
                 item = (Item) curMap.get(key);
                 // TODO: [2003-08-27 bloch] add assert in java 1.4 item.active()
             }
         }
 
         // New items default to the mem cache if it exists.
         // Note that some items that are too big may
         // be cached on disk (and not in mem) but may 
         // temporarily be in the "mem cache" map.
         // This is confusing, but simple and safe to implement
         // w/out complicated locking.  This is because
         // we don't want to lock the maps while we're
         // fetching the items and we won't know the size
         // until we fetch the item.
         //
         // TODO: [2003-09-04 bloch] 
         // Note: this now only happens when doLockAndLeaveActive is
         // false.  At some point we could rearchitect to remove
         // this wart
 
         try {
             if (item == null) {
                 item = new Item(key, enc, hasMemCache);
                 if (doLockAndLeaveActive) {
                     item.lock();
                     item.setActive(true);
                 }
 
                 //                mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Made new item for " + p[0]
  */
                 //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                 //                                Cache.class.getName(),"051018-270", new Object[] {key.toString()})
                 //);
 
             } else {
                 if (doLockAndLeaveActive) {
                     item.lock();
                     item.setActive(true);
                 }
                 if (item.needsReckoning()) {
                     //                    mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Reckoning an old item for " + p[0]
  */
                     //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                     //                                Cache.class.getName(),"051018-285", new Object[] {key.toString()})
                     //);
                     item.reckon();
                 }
                 if (item.dirty()) {
                     //                    mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Found a dirty item for " + p[0]
  */
                     //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                     //                                Cache.class.getName(),"051018-296", new Object[] {key.toString()})
                     //);
                     if (doLockAndLeaveActive) {
                         item.removeAndUnlock();
                     } else {
                         item.remove();
                     }
                     curMap.remove(key);
                     item = new Item(key, enc, hasMemCache);
                     if (doLockAndLeaveActive) {
                         item.lock();
                         item.setActive(true);
                     }
                     //                    mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Removed and made new item for " + p[0]
  */
                     //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                     //                                Cache.class.getName(),"051018-315", new Object[] {key.toString()})
                     //);
                 } else {
                     // Remove the item and re-add it below
                     //                    mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Found old item for " + p[0]
  */
 
                     //                          org.openlaszlo.i18n.LaszloMessages.getMessage(
                     //                                Cache.class.getName(),"051018-325", new Object[] {key.toString()})
                     //);
                     curMap.remove(key);
                     if (curMap == mDiskMap) {
                         if (newMap == mMemMap) {
                             // Update sizes when we're moving from disk to mem
                             long size = item.getSize();
                             if (size <= mMaxMemItemSize || mMaxMemItemSize <= 0) {
                                 item.readIntoMemory();
                                 // Update sizes after we read into memory in case
                                 // the above read fails for some bizarro reason.
                                 mDiskSize -= size;
                                 mMemSize += size;
                             } else {
                                 newMap = mDiskMap;
                             }
                         }
                     }
                 }
             }
     
             if (!doLockAndLeaveActive) {
                 newMap.put(key, item);
             } else {
                 mActiveMap.put(key, item);
             }
 
         } catch (IOException e) {
             // If we get any kind of exception, we better unlock the item
             // since no one will be able to unlock it.
             if (doLockAndLeaveActive && item != null) {
                 item.unlock();
             }
             throw e;
         } catch (RuntimeException re) {
             // If we get any kind of exception, we better unlock the item
             // since no one will be able to unlock it.
             if (doLockAndLeaveActive && item != null) {
                 item.unlock();
             }
             throw re;
         }
 
         return item;
     }
 
     /**
      * Reckon this item and update the cache.
      */
     protected synchronized void updateCache(Item item)  {
 
         item.lock();
         item.reckon();
         item.unlock();
 
         // While we have the cache locked, update the maps
         updateMapsAndReckonItems(false /* leave at least one item in each map */);
     }
 
     /**
      * Reckon and deactivate this item and update the cache.
      */
     protected synchronized void updateCacheAndDeactivateItem(Item item)  {
 
         item.lock();
         item.reckon();
 
         if (item.active()) {
             Object key = item.getKey();
             mActiveMap.remove(key);
             if (item.isInMemory()) {
                 mMemMap.put(key, item);
             } else {
                 mDiskMap.put(key, item);
             }
             item.setActive(false);
         }
 
         item.unlock();
 
         // While we have the cache locked, update the maps
         updateMapsAndReckonItems(true);
     }
 
     /**
      * Set the maximum size of the on-disk cache.
      * Can cause the cache to remove things.
      */
     public synchronized void setMaxDiskSize(long size) {
         if (size >= mMaxDiskSize || size == -1) {
             mMaxDiskSize = size;
             return;
         }
         mMaxDiskSize = size;
         updateMapsAndReckonItems(true); 
     }
 
     /**
      * Get the maximum size of the on-disk cache cache
      */
     public synchronized long getMaxDiskSize() {
         return mMaxDiskSize;
     }
 
     /**
      * Get the current size of the on-disk cache
      */
     public synchronized long getDiskSize() {
         return mDiskSize;
     }
 
     /**
      * Set the maxmimum size of the in-memory cache.
      * Can cause the cache to remove things.
      */
     public synchronized void setMaxMemSize(long size) {
         if (size >= mMaxMemSize || size == -1) {
             mMaxMemSize = size;
             return;
         }
         mMaxMemSize = size;
         updateMapsAndReckonItems(true); 
     }
 
     /**
      * Get the maxmimum size of the in-memory cache
      */
     public synchronized long getMaxMemSize() {
         return mMaxMemSize;
     }
 
     /**
      * Get the current size of the in-memory cache
      */
     public synchronized long getMemSize() {
         return mMemSize;
     }
 
     /**
      * Update the maps and remove items that
      * push us beyond the maximum size of the
      * cache.  Never empty out the maps.
      * Always leave at least one item if clearAll is false.
      *
      * Also ignore items that are in progress.
      * @param clearAll if false, must leave one elt in each map
      */
     public synchronized void updateMapsAndReckonItems(boolean clearAll) {
 
         long removedBytes = 0;
 
         //mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " cache at " + p[1] + " bytes on-disk and " + p[2] + " bytes in-memory."
  */
         //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
         //                                Cache.class.getName(),"051018-482", new Object[] {mName, new Long(mDiskSize), new Long(mMemSize)})
         //        );
         // Infinite mem size... (nothing will get reckoned, hope that's ok)
         if (mMaxMemSize == -1) {
             return;
         }
 
         int done = 0;
         if (!clearAll) {
             done = 1;
         }
 
         // If we have a mem cache, push items from memory to disk
         while (mMemSize > mMaxMemSize && mMemMap.size() > done) {
             Map.Entry first = mMemMap.getFirst();
             Object key = first.getKey();
             Item item = (Item)first.getValue();
             item.lock();
 
             if (item.needsReckoning()){
                 item.reckon();
             }
 
             if (!item.dirty()) {
                 // Some items that were too large for memory
                 // might be in this map, but are actually
                 // accounted for in the disksize already.
                 // We can skip these.
                 if (item.mInfo.isInMemory()) {
                     removedBytes = item.removeFromMemory();
                     mMemSize -= removedBytes;
                     mDiskSize += removedBytes;
                 } else {
                     //                    mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="mem item was already on disk"
  */
                     //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                     //                                Cache.class.getName(),"051018-521")
                     //                    );
                 }
                 //                mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Pushing item " + p[0] + " from mem to disk (" + p[1] + " bytes)"
  */
                 //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                 //                                Cache.class.getName(),"051018-530", new Object[] {key, new Long(removedBytes)})
                 //                );
                 mMemMap.remove(key);
                 mDiskMap.put(key, item);
             } else {
                 if (item.mInfo.isInMemory()) {
                     mMemMap.remove(key);
                     //                    mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Removing dirty item " + p[0] + " from mem "
  */
                     //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                     //                                Cache.class.getName(),"051018-543", new Object[] {key})
                     //);
                 }
             }
             item.unlock();
         }
 
         // Infinite disk size... (no disk items will get reckoned, hope that's ok)
         if (mMaxDiskSize == -1) {
             return;
         }
 
         while (mDiskSize > mMaxDiskSize && mDiskMap.size() > done) {
             Map.Entry first = mDiskMap.getFirst();
             Object key = first.getKey();
             Item item = (Item)first.getValue();
             item.lock();
 
             if (item.needsReckoning()){
                 item.reckon();
             }
 
             if (!item.dirty()) {
                 removedBytes = item.getSize();
                 item.removeAndUnlock();
                 //                mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Removing item " + p[0] + " from disk (" + p[1] + " bytes)"
  */
                 //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                 //                                Cache.class.getName(),"051018-574", new Object[] {key, new Long(removedBytes)})
                 // );
                 mDiskSize -= removedBytes;
                 mDiskMap.remove(key);
             } else {
                 mDiskMap.remove(key);
                 //                mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Removing dirty item " + p[0] + " from disk "
  */
                 //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                 //                                Cache.class.getName(),"051018-586", new Object[] {key})
                 //);
             }
         }
         //        mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " now at disk: " + p[1] + " bytes."
  */
         //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
         //                                Cache.class.getName(),"051018-596", new Object[] {mName, new Long(mDiskSize)})
         //);
         //        mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes=p[0] + " now at mem: " + p[1] + " bytes."
  */
         //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
         //                                Cache.class.getName(),"051018-604", new Object[] {mName, new Long(mMemSize)})
         //);
     }
 
     /**
      * Dump textual XML representation of cache to buf
      *
      * @param buf buffer to append
      * @param name name for XML element
      * @param details if true, dump details
      */
     public synchronized void dumpXML(StringBuffer buf, String name, boolean details)  {
 
         buf.append("<" + name + " \n");
             buf.append("disk-total=\"");
             buf.append(getMaxDiskSize());
             buf.append("\" ");
             buf.append("disk-in-use=\"");
             buf.append(getDiskSize());
             buf.append("\" ");
             buf.append("mem-total=\"");
             buf.append(getMaxMemSize());
             buf.append("\" ");
             buf.append("mem-in-use=\"");
             buf.append(getMemSize());

            buf.append("\" ");
            buf.append("diskmap-entries=\"");
            buf.append(mDiskMap.size());


            buf.append("\" ");
            buf.append("memmap-entries=\"");
            buf.append(mMemMap.size());

            buf.append("\" ");
            buf.append("activemap-entries=\"");
            buf.append(mActiveMap.size());

             buf.append("\" ");
         buf.append("    >\n");
         if (details) {
             buf.append("<active>\n");
                 dumpMap(buf, mActiveMap, false);
             buf.append("</active>\n");
             buf.append("<in-memory>\n");
                 dumpMap(buf, mMemMap, true);
             buf.append("</in-memory>\n");
             buf.append("<disk>\n");
                 dumpMap(buf, mDiskMap, true);
             buf.append("</disk>\n");
         }
         buf.append("</" + name + ">\n");
     }
 
     /**
      * @param buf buffer to append
      * @param map map to dump
      */
     private synchronized void dumpMap(StringBuffer buf, SequencedHashMap map, 
             boolean lockItems) {
 
         Iterator iter = map.iterator();
         while (iter.hasNext()) {
             buf.append("<item ");
             Object key = iter.next();
             Item item = (Item) map.get(key);
             if (lockItems) {
                 item.lock();
             }
             String keystring = key.toString();
             if (keystring.length() > 128) {
                 keystring  = keystring.substring(0,127) + "...";
             }
             buf.append("key=\"" + XMLUtils.escapeXml(keystring) + "\" ");
             buf.append("in-memory=\"" + (new Boolean(item.isInMemory())).toString() + "\" " );
             buf.append("dirty=\"" + (new Boolean(item.dirty())).toString() + "\" " );
             buf.append("active=\"" + (new Boolean(item.active())).toString() + "\" " );
             buf.append("needs-reckon=\"" + (new Boolean(item.needsReckoning())).toString() + "\" " );
             buf.append("mem-to-reckon=\"" + item.memToReckon() + "\" " );
             buf.append("disk-to-reckon=\"" + item.diskToReckon() + "\" " );
             buf.append("size=\"" + item.getSize() + "\" ");
             buf.append("key-size=\"" + item.getKeySize() + "\" ");
             buf.append("path-name=\"" + item.getPathName() + "\" ");
             buf.append("info-name=\"" + item.getInfoName() + "\" ");
             long lm = item.getInfo().getLastModified();
             buf.append("last-modified=\"" + lm + "\" ");
             buf.append("last-modified-gmt=\"" + LZHttpUtils.getDateString(lm) + "\" ");
             buf.append("/>\n");
             if (lockItems) {
                 item.unlock();
             }
         }
     }
 
     /**
      * Load the state of the cache from what is in the cache
      * Then update the cache log itself to represent
      * what we now know.
      *
      * FIXME: [2003-03-11] bloch the last-used time is lost when
      * we shutdown; this means the LRU ordering isn't preserved on
      * restart.  We should add lastUsed times to the cachedInfo.
      */
     private synchronized void loadFromDirectory() throws IOException {
 
         String[] fileNames = mCacheDirectory.list();
 
         for(int i = 0; i < fileNames.length; i++) {
             if (!fileNames[i].endsWith(".dat")) 
                 continue;
 
             int x = fileNames[i].indexOf(".dat");
             String name = fileNames[i].substring(0, x);
             long n;
             try {
                 n = Long.parseLong(name);
                 if (n > mCurName) {
                     mCurName = n;
                 }
             } catch (NumberFormatException ne) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="ignoring a strange .dat file in the cache named: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-717", new Object[] {fileNames[i]})
 );
                 continue;
             }
 
             Item item = null;
             File file = new File(mCacheDirectory + File.separator + fileNames[i]);
             try {
                 item = new Item(file);
             } catch (Throwable e) {
                 mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="can't load cache file: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-733", new Object[] {fileNames[i]})
                 );
 
                 mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="exception: "
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-742")
                         , e);
                 mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="deleting cache files for : " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-750", new Object[] {fileNames[i]})
                 );
                 // Deletes name* files.
                 if (!FileUtils.deleteFilesStartingWith(mCacheDirectory, name)) {
                     mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="couldn't delete some files with name: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-760", new Object[] {fileNames[i]})
 );
                 }
                 continue;
             }
 
             Object key = item.getKey();
             if (item.isInMemory()) {
                 mMemMap.put(key, item);
                 mMemSize += item.getSize();
             } else {
                 mDiskMap.put(key, item);
                 mDiskSize += item.getSize();
             }
 
             String keystring = key.toString();
             if (keystring.length() > 128) {
                 keystring  = keystring.substring(0,127) + "...";
             }
             //            mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="loaded cached item for " + p[0]
  */
             //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
             //                                Cache.class.getName(),"051018-785", new Object[] {keystring})
             //            );
 
         }
 
         updateMapsAndReckonItems(true);
 
     }
 
     /**
      * Clear the cache
      */
     public synchronized boolean clearCache() {
         mLogger.info(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Clearing " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-804", new Object[] {mName})
 );
 
         boolean ok1 = clearMap(mActiveMap);
         boolean ok2 = clearMap(mDiskMap);
         boolean ok3 = clearMap(mMemMap);
         boolean ok4 = true;
 
         mMemSize = 0;
         mDiskSize = 0;
 
         ok4 = FileUtils.deleteFiles(mCacheDirectory);
 
         return ok1 && ok2 && ok3 && ok4;
     }
 
     /**
      * Clear entries from given map
      * @param map to clear
      */
     private boolean clearMap(SequencedHashMap map) {
 
         boolean ok = true;
         while (map.size() > 0) {
             Map.Entry first = map.getFirst();
             Object key = first.getKey();
             Item item = (Item)first.getValue();
             item.lock();
             if (!item.removeAndUnlock()) {
                 ok = false;
             }
             map.remove(key);
         }
         
         return ok;
     }
 
     /**
      * Return a unique name
      *
      * Since time always moves fwd, we shouldn't
      * have to worry about using names we've
      * previously "allocated".
      *
      * FIXME: [2003-02-25 bloch] if someone sets the clock
      * back, we need to clear the cache; we could easily
      * detect this using timestamps on files in the cache.
      */
      private long nextUniqueName() {
 
          long name;
 
          synchronized (mNameLock) {
              name = System.currentTimeMillis();
              if (name <= mCurName) {
                  name = mCurName + 1;
              }
              mCurName = name;
          }
 
          return name;
      }
 
      /**
       * A class that represents an item in the Cache.
       *
       * @author <a href="mailto:bloch@laszlosystems.com">Eric Bloch</a>
       */
      protected class Item implements Serializable {
 
         transient private Sync mLock;
         transient private boolean mDirty  = false;
         transient private boolean mNeedsReckoning = false;
         /** true if the item is actively being updated */
         transient private boolean mActive = false;
 
         transient private byte    mBuffer[];
 
         transient private long mMemSizeToReckon = 0;
         transient private long mDiskSizeToReckon = 0;
 
 
         /**
          * Information about this cached item.
          */
         CachedInfo mInfo = null;
 
         /** 
          * Construct an Item based on this request
          * and lock it for write.
          */
         public Item(Serializable key, String enc, boolean inMemory) { 
 
             mLock = new ReentrantLock();
             mInfo = new CachedInfo(key, enc, inMemory, nextUniqueName());
             mBuffer = null;
         }
 
         /**
          * @return true if item is dirty
          */
         public boolean dirty() {
             return mDirty;
         }
 
         /**
          * @return true if item is active
          */
         public boolean active() {
             return mActive;
         }
 
         /** set if item is active */
         public void setActive(boolean a) {
             mActive = a;
         }
 
         /**
          * @return true if item is dirty
          */
         public boolean needsReckoning () {
             return mNeedsReckoning;
         }
 
         /**
          * @return mem to reckon
          */
         public long memToReckon() {
             return mMemSizeToReckon;
         }
 
         /**
          * @return disk to reckon
          */
         public long diskToReckon() {
             return mDiskSizeToReckon;
         }
 
         /**
          * Update cache sizes with size from item
          * that haven't been reckonned yet.
          * Must have item and entire cache locked to call this
          */
         public void reckon() {
             mMemSize += mMemSizeToReckon;
             mDiskSize += mDiskSizeToReckon;
 
             mDiskSizeToReckon = 0;
             mMemSizeToReckon = 0;
 
             mNeedsReckoning = false;
             //            mLogger.debug("reckoned an item");
         }
 
 
         /**
          * Item should be locked during this call
          */
         public void readIntoMemory() throws IOException {
             FileInputStream fis = null;
             try {
                 String p = getPathName();
                 fis = new FileInputStream(p);
                 mBuffer = new byte[(int)mInfo.getSize()];
                 fis.read(mBuffer, 0, (int)mInfo.getSize());
                 mInfo.setInMemory(true);
             } catch (IOException e) {
                 markDirty();
                 throw e;
             } finally {
                 FileUtils.close(fis);
             }
         }
 
         /**
          * Item should be locked during this call
          * @return size of item
          * caller is responsible for updating cache sizes
          */
         public long removeFromMemory() {
             mBuffer = null;
             mInfo.setInMemory(false);
             return mInfo.getSize();
         }
 
         /** 
          * Construct an Item based on the contents of this file
          */
         public Item(File f) throws IOException, OptionalDataException,
             StreamCorruptedException, ClassNotFoundException
         { 
             mLock = new ReentrantLock();
 
             InputStream in = null;
             try {
                 in = new BufferedInputStream(new FileInputStream(f));
                 ObjectInputStream istr = new ObjectInputStream(in);
                 mInfo = (CachedInfo)istr.readObject();
         // after reading the object, call our override routine
                 afterCacheRead(mInfo.getMetaData());
             } finally {
                 FileUtils.close(in);
             }
 
             // FIXME: [2003-08-15 pkang] isInMemory() will always evaluate to
             // true for items that are read from disk. This is because those
             // items are written out only when they're fetched and, by default,
             // they are created to exist in memory. Since it's better that we
             // don't read every item into memory during start-up, isInMemory is
             // set to false here.
 
             mInfo.setInMemory(false);
 
 //            if (mInfo.isInMemory()) {
 //                readIntoMemory();
 //            }
         }
 
         /**
          * Lock the item
          */
         public void lock() {
             mLockLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="acquiring lock: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1032", new Object[] {getKey()})
 );
             try {
                 mLock.acquire();
             } catch (InterruptedException e) {
                 throw new ChainedException(e);
             }
             mLockLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="acquired lock: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1045", new Object[] {getKey()})
 );
         }
 
         /*
          * Done with item
          */
         public void unlock() {
             mLockLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="releasing read lock: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1059", new Object[] {getKey()})
 );
             mLock.release();
             mLockLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="released read lock: " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1068", new Object[] {getKey()})
 );
         }
 
         /**
          * @return the key for this item.
          */
         public Serializable getKey() {
             if (mInfo != null)
                 return mInfo.getKey();
             else
                 return "uninitialized item";
         }
 
         /**
          * @return whether this item is in memory or not
          */
         public boolean isInMemory() {
             if (mInfo != null)
                 return mInfo.isInMemory();
             else
                 return false;
         }
 
         /**
          * Return the cached size of this item in bytes 
          * Must have the item read locked for this.
          */
         public long getSize() {
             return mInfo.getSize();
         }
 
         /**
          * Return the size of this items key in bytes.
          * May return -1 if the size is unknown/expensive to compute.
          * Must have the item read locked for this.
          */
         public long getKeySize() {
             return mInfo.getKeySize();
         }
 
         /**
          * @return item's info
          */
         public CachedInfo getInfo() {
             return mInfo;
         }
     
         /**
          * Return the path name of the cached version of this item
          */
         public String getPathName() {
             // FIXME: [2003-03-03 bloch] unhardcode gz extension someday
             return mCacheDirectory.getAbsolutePath() 
                 + File.separator + mInfo.getName() + ".swf" + 
                 (mInfo.getEncoding() == null ? "" : ".gz") ;
         }
 
         /**
          * Return the path name of the cached info for this item
          */
         public String getInfoName() {
             return mCacheDirectory.getAbsolutePath() 
                 + File.separator + mInfo.getName() + ".dat";
         }
 
         /**
          * Encoding the given item using the requested encoding.
          * Encode to a temporary file and then rename.
          *
          * @return the size in bytes of the encoded file
          * caller is responsible for updating item and cache sizes
          */
         public long encode(String enc) throws IOException {
 
             File f = new File(getPathName());
             File tempDir = f.getParentFile();
             File tempFile = File.createTempFile("lzuc-", null, tempDir);
             //            mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Temporary file is " + p[0]
  */
             //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
             //                                Cache.class.getName(),"051018-1152", new Object[] {tempFile.getAbsolutePath()})
             //);
             FileUtils.encode(f, tempFile, enc);
             long size = tempFile.length();
             if (!f.delete()) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't delete " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1163", new Object[] {f.getAbsolutePath()})
 );
             }
             if (!tempFile.renameTo(f)) {
 mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't rename temporary file to " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1173", new Object[] {f.getAbsolutePath()})
                 );
             }
             return size;
         }
 
         /**
          * Mark the item as dirty
          */
         public void markDirty() {
             if (mDirty) {
                 return;
             }
             // Removed the cache file and any buffer and mark the item
             // as dirty
             File f = new File(getPathName());
             if (f.exists() && !f.delete()) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't delete " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1163", new Object[] {getPathName()})
 );
             }
             f = new File(getInfoName());
             if (f.exists() && !f.delete()) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't delete " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1163", new Object[] {getInfoName()})
 );
             }
             mDirty = true;
             mBuffer = null;
             mNeedsReckoning = true;
             //            mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Marking item dirty: " + p[0]
  */
             //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
             //                                Cache.class.getName(),"051018-1219", new Object[] {mInfo.getKey()})
             //);
             if (mInfo.isInMemory()) {
                 mMemSizeToReckon  -= mInfo.getSize();
             } else {
                 mDiskSizeToReckon -= mInfo.getSize();
             }
             //            mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="disk to reckon : " + p[0] + ", mem to reckon : " + p[1]
  */
             //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
             //                                Cache.class.getName(),"051018-1232", new Object[] {new Long(mDiskSizeToReckon), new Long(mMemSizeToReckon)})
             //            );
             mInfo.setSize(0);
         }
     
         /**
          * @return true if this item is still valid
          * given the current data
          */
         public boolean validForData(Data d) {
             // TODO: [2003-01-14 bloch] Make sure the cached 
             // ETag, Content-Length, and Content-MD5 are ok.
             // Need to add these to the CachedInfo class and put
             // logic here to deal with missing/present values.
             // Will need to get the right interface between
             // Item/CachedInfo/Data to make this work.
             //
             //if (hdr != null) {
             //}
 
             return true;
         }
 
         /**
          * Remove the cached version of this item and then unlock it.
          * Must have item locked for this.  Caller is responsible for updatng cache sizes
          * @return true if there were no problems
          */
         public boolean removeAndUnlock() {
             boolean ok = remove();
             unlock();
             return ok;
         }
 
         /**
          * Remove the cached version of this item 
          * @return true if there were no problems
          * Caller is responsible for updatng cache sizes.
          */
         private boolean remove() {
 
             boolean ok = true;
             //            mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="removing item for " + p[0] + " (" + p[1] + " - " + p[2] + " bytes) ."
  */
             //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
             //                                Cache.class.getName(),"051018-1280", new Object[] {mInfo.getKey(), getPathName(), new Long(getSize())})
             //            );
             File f = new File(getPathName());
             if (f.exists() && !f.delete()) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't delete " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1163", new Object[] {getPathName()})
 );
                 ok = false;
             }
             f = new File(getInfoName());
             if (f.exists() && !f.delete()) {
                 mLogger.warn(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't delete " + p[0]
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 Cache.class.getName(),"051018-1163", new Object[] {getInfoName()})
 );
                 ok = false;
             }
 
             return ok;
         }
 
         /**
          * @return stream depending on whether we're in mem
          * or on disk
          */
         public InputStream getStream() {
             if (mInfo.isInMemory()) {
                 return new ByteArrayInputStream(mBuffer);
             } else {
                 try {
                     return new FileInputStream(getPathName());
                 } catch (FileNotFoundException e) {
                     throw new ChainedException(e.getMessage());
                 }
             }
         }
 
         /**
          * @return returns raw byte-array data.
          * This should only be used to retrieve immutable data! If the caller modifies this byte array,
          * it could cause trouble.
          */
         public byte[] getRawByteArray() {
             try {
                 if (!mInfo.isInMemory()) {
                     readIntoMemory();
                 }
                 if (mInfo.isInMemory()) {
                     return mBuffer;
                 } else {
                     ByteArrayOutputStream bos = null;
                     FileInputStream instream = null;
                     try {
                         // If it's still not in memory, maybe it was too big, so let's copy it into
                         // a new byte array and return that.
                         bos = new ByteArrayOutputStream();
                         instream = new FileInputStream(getPathName());
                         FileUtils.send(instream, bos);
                         return bos.toByteArray();
                     } catch (FileNotFoundException e) {
                         throw new ChainedException(e.getMessage());
                     } finally {
                         if (bos != null) {
                             bos.close();
                         }
                         if (instream != null) {
                             instream.close();
                         }
                     }
                     
                 }
             } catch (IOException e) {
                 throw new ChainedException(e.getMessage());
             }
         }
 
 
 
 
         /**
          * @return the file object 
          */
         public File getFile() {
             return new File(getPathName());
         }
 
         /**
          * Get the meta data for the current item
          */
         public Serializable getMetaData() {
             return mInfo.getMetaData();
         }
 
         /**
          * Update the item from the given input stream
          * and meta data and mark the item as clean 
          * (not dirty).
          *
          * @param in input stream; ignored if null
          * @param metaData
          */
         public void update(InputStream in, Serializable metaData) 
             throws IOException {
 
             //            mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="updating item stream and metadata"
  */
             //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
             //                                Cache.class.getName(),"051018-1399")
             //);
             update(metaData);
 
             if (in != null) {
                 update(in);
             }
 
             markClean();
         }
 
         /**
          * Update the item from the given input stream
          *
          * @param in input stream
          */
         public void update(InputStream in)
             throws IOException {
 
             FileOutputStream out = null;
 
             try {
 
                 //                mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="updating item from stream"
  */
                 //                        org.openlaszlo.i18n.LaszloMessages.getMessage(
                 //                                Cache.class.getName(),"051018-1428")
                 //);
 
                 out = new FileOutputStream(getPathName());
                 long size = FileUtils.send(in, out);
                 FileUtils.close(out);
 
                 String enc = mInfo.getEncoding();
                 if (enc != null && !enc.equals("")) {
                     size = encode(enc);
                 }
     
                 long oldSize = mInfo.getSize();
                 mInfo.setSize(size);
                 mNeedsReckoning = true;
 
                 // Remove size from any old item
                 if (mInfo.isInMemory()) {
                     mMemSizeToReckon  -= oldSize;
                 } else {
                     mDiskSizeToReckon -= oldSize;
                 } 
     
                 // Check to see if we're too big for memory
                 if (mInfo.isInMemory() && mMaxMemItemSize > 0 &&
                         size > mMaxMemItemSize) {
                     mLogger.debug("this item too big for memory ");
                     mInfo.setInMemory(false);
                 }
     
                 if (mInfo.isInMemory()) {
                     mLogger.debug("reading item into memory ");
                     readIntoMemory();
                     mMemSizeToReckon += size;
                 } else {
                     mDiskSizeToReckon += size;
                 }
     
             } finally {
                 FileUtils.close(out);
             }
         }
 
         /**
          * Mark the item as clean
          */
         public void markClean() {
             mDirty = false;
         }
 
         /**
          * Update the item's meta data.
          *
          * @param metaData
          */
         public void update(Serializable metaData) 
             throws IOException {
 
             mInfo.setMetaData(metaData);
             updateInfo();
         }
 
         /**
          * Update the item's info
          */
         public void updateInfo() throws IOException {
 
             FileOutputStream out = null;
 
             try {
 
                 // Write out the info (.dat) file
                 out = new FileOutputStream(getInfoName());
                 ObjectOutputStream ostr = new ObjectOutputStream(out);
                 ostr.writeObject(mInfo);
                 ostr.flush();
                 ostr.close();
 
             } finally {
                 FileUtils.close(out);
             }
         }
     }
 }
