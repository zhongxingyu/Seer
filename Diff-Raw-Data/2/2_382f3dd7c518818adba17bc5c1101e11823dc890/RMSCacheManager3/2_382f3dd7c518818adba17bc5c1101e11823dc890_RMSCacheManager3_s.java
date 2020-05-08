 //#condition MM.CacheUseRms3
 /*
  * Copyright (C) 2011 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 import javax.microedition.rms.InvalidRecordIDException;
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 
 import java.lang.Thread;
 
 /**
  * All the entries (keys and data) are cached in memory.
  * A background thread will dump modifications (add, modify delete)
  * after a given flush delay and remove cache after a period of inactivity.
  * Pack operations are triggered regularly to limit RecordStore slow writes
  * after multiple write/delete operations.
  */
 class RMSCacheManager3 extends CacheManager implements Runnable {
 
     // Initial capacity of the key in memory table
     private final static int INITIAL_CAPACITY = 10;
 
     // Delay to wait before writing data back to RMS
     private final static int FLUSH_DELAY = 3000;
 
     // Delay to wait before freeing the in memory cache.
     private final static int INACTIVITY_DELAY = 10000;
 
     private final static String ERASE_ALL = "__ERASE_ALL_RECORDS";
 
     // deleteAllRMS, deleteRMS, getInstance are synchronized
     // to prevent concurrent access to s_instances
     private static RMSCacheManager3 s_instances;
 
     // Delete all RecordStores
     public static synchronized void deleteAllRMS () {
         RMSCacheManager3 cm = s_instances;
         while (cm != null) {
             cm.erase ();
             cm = cm.m_next;
         }
         String[] list = RecordStore.listRecordStores();
         int size = list != null ? list.length : 0;
         for (int i=0; i<size; i++) {
             try { RecordStore.deleteRecordStore (list[i]); }
             catch (Exception e) { Logger.println("RMS3: Could not erase "+list[i]); }
         }
     }
 
     public static synchronized void deleteRMS (String storename) {
         if (storename.length() == 0) {
             storename = EMPTY;
         }
         RMSCacheManager3 cm = s_instances;
         while (cm != null) {
             if (storename.equals(cm.m_storeName)) {
                 cm.erase();
                 return;
             }
             cm = cm.m_next;
         }
         try { RecordStore.deleteRecordStore (storename); }
         catch (Exception e) {
             Logger.println ("RMS3: deleteRMS error: "+e);
         }
     }
 
     public static synchronized RMSCacheManager3 getInstance (String name) {
         RMSCacheManager3 cm = s_instances;
         while (cm != null) {
             if (name.equals(cm.m_storeName)) {
                 return cm;
             }
             cm = cm.m_next;
         }
         s_instances = new RMSCacheManager3 (name, s_instances);
         return s_instances;
     }
 
     public static synchronized void closeAll () {
         RMSCacheManager3 cm = s_instances;
         while (cm != null) {
             cm.finalClose();
             RMSCacheManager3 prev = cm;
             cm = cm.m_next;
             prev.m_next = null;
         }
         s_instances = null;
     }
 
     class Entry {
         String name;
         int index;
         byte[] data;
     }
 
     private String m_storeName;
     private Entry [] m_entries;
     private int m_nbEntries = 0;
     private boolean m_tableLoaded;
     private long m_lastAccess;
 
     private RecordStore m_recordStore;
     private int m_availableSize;
 
     private RMSCacheManager3 m_next;
 
     private ObjLink queue;
     private boolean m_quit;
     private Thread m_thread; // worker thread in charge of async operations
     private Object m_threadLock = new Object();
     private Object m_flushLock = new Object();
 
     private RMSCacheManager3 (String name, RMSCacheManager3 next) {
         super (name);
         if (name.length() == 0) {
             name = EMPTY;
         }
         m_storeName = name;
         m_next = next;
         loadEntries();
     }
 
     private boolean loadEntries () {
         if (m_tableLoaded) {
             return true;
         } else {
             //long ts = System.currentTimeMillis();
             if (openStore()) {
                 int maxRecordId;
                 try {
                     maxRecordId = m_recordStore.getNextRecordID();
                 } catch (Exception e) {
                     return false;
                 }
                 m_entries = new Entry[(maxRecordId/2)+INITIAL_CAPACITY];
                 m_nbEntries = 0;
                 byte[] stringBuff = new byte[4092];
                 int recordId = 1;
                 int deleted = 0;
                 while (recordId < maxRecordId) {
                     try {
                         int stringSize = m_recordStore.getRecord (recordId, stringBuff, 0);
                         String name = new String (stringBuff, 0, stringSize);
                         byte[] data = m_recordStore.getRecord (recordId+1);
                         addEntry(name, recordId, data, false);
                     } catch (InvalidRecordIDException e) {
                         deleted++;
                    } catch (RecordStoreException e) {
                         Logger.println ("RMS3: ReadEntries error: "+e);
                     }
                     recordId += 2;
                 }
                 sortEntries();
                 if (deleted > m_nbEntries * 3 / 4) {
                     Logger.println("RMS3: Packing needed for "+m_storeName);
                     // Add all entries to queue after ERASE_ALL operation
                     addAsyncOperation(ERASE_ALL, null);
                     for (int i = 0; i<m_nbEntries; i++) {
                         final Entry e = m_entries[i];
                         e.index = 0; // force add operation
                         addAsyncOperation(e.name, e);
                     }
                 }
                 m_tableLoaded = true;
                 closeStore();
                 //ts = System.currentTimeMillis()-ts;
                 //Logger.println("RMS3: Loaded "+m_nbEntries+" entries in "+ts+"ms for "+m_storeName);
                 wakeThread(); // ensure thread is awake to let it purge data in 10 sec if not used
                 return true;
             }
         }
         return false;
     }
 
     private void sortEntries () {
         boolean again = true;
         int max = m_nbEntries-2;
         while (again) {
             again = false;
             for (int i = max; i >= 0; i--) {
                 final Entry e = m_entries[i], next = m_entries[i+1];
                 if (e.name.compareTo (next.name) > 0) {
                     m_entries[i+1] = e;
                     m_entries[i] = next;
                     again = true;
                 }
             }
         }
     }
 
     private int findEntry (String name) {
         m_lastAccess = System.currentTimeMillis();
         if (name == null || name.length() == 0) {
             name = EMPTY;
         }
         //Logger.println("Find "+name+" for "+m_storeName);
         int left = 0;
         int right = m_nbEntries-1;
         int pivot, way;
         while (left <= right) {
             pivot = left + (right - left) / 2;
             way = name.compareTo (m_entries[pivot].name);
             if (way == 0) {
                 return pivot;
             } else if (way < 0) {
                 right = pivot-1;
             } else { //way > 0
                 left = pivot+1;
             }
         }
         return -1;
     }
 
     private int addEntry (String name, int index, byte[] data, boolean sort) {
         if (name == null || name.length() == 0) {
             name = EMPTY;
         }
         Entry e = new Entry();
         e.name = name;
         e.index = index;
         e.data = data;
         if (m_nbEntries >= m_entries.length) { // expand the array
             Entry [] tmp = new Entry [m_entries.length*2];
             System.arraycopy (m_entries, 0, tmp, 0, m_nbEntries);
             m_entries = tmp;
         }
         m_entries[m_nbEntries] = e;
         m_nbEntries++;
         if (sort) {
             sortEntries (); // should perform only one loop
             return findEntry (name);
         }
         return -1;
     }
 
     private boolean removeEntry (int id) {
         if (id >= 0 && id < m_nbEntries) {
             int len = m_nbEntries - id - 1;
             if (len > 0) {
                 System.arraycopy (m_entries, id+1, m_entries, id, len);
             }
             m_nbEntries--;
             return true;
         }
         return false;
     }
 
     private boolean openStore () {
         if (m_recordStore == null) {
             //Logger.println("RMS3: open rms: "+m_storeName);
             try {
                 m_recordStore = RecordStore.openRecordStore(m_storeName, true);
                 m_availableSize = m_recordStore.getSizeAvailable();
             } catch (Exception e) {
                 Logger.println("RMS3: open error: "+e+" for "+m_storeName);
                 return false;
             }
         }
         return true;
     }
 
     private void closeStore() {
         if (m_recordStore != null) {
             //Logger.println("RMS3: close rms: "+m_storeName);
             try {
                 m_recordStore.closeRecordStore ();
             } catch (Exception e) {
                 Logger.println("RMS3: close error: "+e+" for "+m_storeName);
             }
             m_recordStore = null;
         }
     }
     // This implementation never closes the RecordStore until application exit.
     public synchronized void close () { }
 
     // Called only by closeAll() on application exit.
     private void finalClose() {
         java.lang.Thread t = m_thread;
         if (!m_quit && t != null && t.isAlive()) {
             m_quit = true;
             synchronized (m_threadLock) {
                 m_threadLock.notify();
             }
             try {
                 t.join();
                 //Logger.println("RMS3: Thread joined for "+m_storeName);
             } catch (InterruptedException e) {
                 Logger.println("RMS3: Thread cannot join for "+m_storeName);
             }
         }
     }
 
     public synchronized void erase () {
         ObjLink.releaseAll(queue);
         queue = null;
         if (m_tableLoaded && m_entries != null) {
             for (int i=0; i<m_nbEntries; i++) {
                 m_entries[i] = null;
             }
             m_nbEntries = 0;
         }
         addAsyncOperation(ERASE_ALL, null);
     }
 
     public synchronized boolean hasRecord (String s) {
         return loadEntries() && findEntry (s) >= 0;
     }
 
     public synchronized byte[] getByteRecord (String s) {
         if (loadEntries()) {
             int id = findEntry (s);
             if (id >= 0) {
                 //Logger.println("RMS3: "+m_storeName+": Read entry:"+s+":"+id);
                 return m_entries[id].data;
             } else {
                 //Logger.println("RMS3: "+m_storeName+": Cannot read entry:"+s);
             }
         }
         return null;
     }
 
     public synchronized boolean setRecord (String s, byte[] data) {
         if (loadEntries()) {
             int id = findEntry (s);
             if (id == -1) {
                 id = addEntry (s, 0, data, true);
                 //Logger.println("RMS3: "+m_storeName+": Add entry:"+s+":"+id);
             } else {
                 //Logger.println("RMS3: "+m_storeName+": Set entry:"+s+":"+id);
                 m_entries[id].data = data;
             }
             addAsyncOperation(s, m_entries[id]);
             return true;
         }
         return false;
     }
 
     public synchronized boolean deleteRecord (String s) {
         if (loadEntries()) {
             int id = findEntry (s);
             if (id >= 0) {
                 //Logger.println("RMS3: "+m_storeName+": Delete entry:"+s+":"+id);
                 Entry e = m_entries[id];
                 removeEntry(id);
                 e.data = null; // mark async operation for deletion
                 addAsyncOperation(s, e);
                 return true;
             } else {
                 Logger.println("RMS3: "+m_storeName+": Cannot delete entry:"+s);
             }
         }
         return false;
     }
 
     public synchronized int getSizeAvailable () {
         return m_availableSize;
     }
 
     private synchronized void addAsyncOperation (String key, Entry e) {
         if (queue == null) {
             queue = ObjLink.create (key, e, null);
         } else {
             ObjLink o = queue;
             while (true) {
                 if (o.m_object.equals(key)) {
                     o.m_param = e;
                     break;
                 } else if (o.m_next == null) {
                     o.m_next = ObjLink.create (key, e, null);
                     break;
                 }
                 o = o.m_next;
             }
         }
         wakeThread(); // ensure thread is awake to execute delayed operations
     }
 
     private synchronized void wakeThread() {
         if (m_thread == null || !m_thread.isAlive()) {
             m_thread = new java.lang.Thread(this);
             m_thread.start();
         }
     }
 
     /** This call will block until all pending operations are done */
     public void flushRecords() {
         synchronized (m_flushLock) {
             //Logger.println("RMS3: flushRecords for "+m_storeName);
             ObjLink pendingOp;
             synchronized(this) {
                 pendingOp = queue; // Get pending operations
                 queue = null;      // and empty the queue
             }
             while (pendingOp != null) {
                 if (pendingOp.m_object.equals(ERASE_ALL)) {
                     closeStore();
                     try {
                         RecordStore.deleteRecordStore(m_storeName);
                     } catch (RecordStoreException e) {
                         Logger.println("RMS3: Erase error: "+e+" for "+m_storeName);
                     }
                 } else if (openStore()) {
                     final Entry e = (Entry)pendingOp.m_param;
                     byte[] data;
                     int index;
                     synchronized (this) {
                         data = e.data;
                         index = e.index;
                     }
                     try {
                         if (data == null) { // delete
                             if (index > 0) { // index could be 0 if data is deleted just after creation
                                 //Logger.println("RMS3: Delete "+e.name+" at "+index);
                                 m_recordStore.deleteRecord(index);
                                 m_recordStore.deleteRecord(index+1);
                             }
                         } else if (index > 0) { // set
                             //Logger.println("RMS3: Write "+e.name+" at "+index);
                             m_recordStore.setRecord(index+1, data, 0, data.length);
                         } else { // add
                             //Logger.println("RMS3: Add "+e.name);
                             byte[] stringBuff = e.name.getBytes();
                             index = m_recordStore.addRecord(stringBuff, 0, stringBuff.length);
                             m_recordStore.addRecord(data, 0, data.length);
                             synchronized (this) {
                                 e.index = index;
                             }
                         }
                     } catch (RecordStoreException ex) {
                         Logger.println("RMS3: write error: "+ex+" for "+e.name);
                     }
                 }
                 pendingOp = ObjLink.release(pendingOp);
             }
             closeStore();
         }
     }
 
     // Main loop for the background thread
     public void run() {
         try {
             while (!m_quit) {
                 synchronized (m_threadLock) {
                     m_threadLock.wait(FLUSH_DELAY);
                 }
                 //long ts = System.currentTimeMillis();
                 flushRecords();
                 //Logger.println("RMS3: Flush in "+(System.currentTimeMillis()-ts)+"ms for "+m_storeName);
                 // Never unload entries for Master manager
                 if (s_masterMgr != this) {
                     synchronized (this) {
                         if (queue == null && System.currentTimeMillis() - m_lastAccess > INACTIVITY_DELAY) {
                             m_entries = null;
                             m_tableLoaded = false;
                             //Logger.println("RMS3: Free memory cache for "+m_storeName);
                             break; // exit loop
                         }
                     }
                 }
             }
             flushRecords();
             //Logger.println ("RMS3: Thread ended for "+m_storeName);
         } catch (Throwable t) {
             Logger.println ("RMS3: Thread died: "+t+" for "+m_storeName);
         } finally {
             m_thread = null;
         }
     }
 }
