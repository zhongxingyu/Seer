 //#condition MM.CacheUseRms2
 /*
  * Copyright (C) 2010 France Telecom
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
 
 import javax.microedition.rms.RecordStore;
 import javax.microedition.rms.RecordStoreException;
 
 /**
  * This implementation is speed oriented but it requires the RMS
  * implementation to work flawlessly.
  * RecordStore is always kept open, to ensure fast reads / writes.
  * It keeps all the key entries in memory until the manager is closed
  * as writing the entries at each write is much too expensive on some phones.
  * These Managers are only opened once and kept opened until application exits.
  * This ensure that only one close/pack is done per RecordStore as this operation
  * is also a very very slow operation on some phones.
  */
 class RMSCacheManager2 extends CacheManager implements Runnable {
 
     // Initial capacity of the key in memory table
     private final static int INITIAL_CAPACITY = 10;
 
     // Max delay before flushing data (close/open RMS) after a write/delete operation
     private final long MAX_FLUSH_DELAY = 3000;
 
     // deleteAllRMS, deleteRMS, getInstance are synchronized
     // to prevent concurrent access to s_instances 
     private static RMSCacheManager2 s_instances;
     
     // Delete all RecordStores
     public static synchronized void deleteAllRMS () {
         RMSCacheManager2 cm = s_instances;
         while (cm != null) {
             cm.erase ();
             cm = cm.m_next;
         }
         String[] list = RecordStore.listRecordStores();
         int size = list != null ? list.length : 0;
         for (int i=0; i<size; i++) {
             try { RecordStore.deleteRecordStore (list[i]); }
             catch (Exception e) { Logger.println("RMSCache: Could not erase "+list[i]); }
         }
     }
 
     public static synchronized void deleteRMS (String storename) {
         if (storename.length() == 0) {
             storename = EMPTY;
         }
         RMSCacheManager2 cm = s_instances;
         RMSCacheManager2 prev = null;
         while (cm != null) {
             if (storename.equals(cm.m_storeName)) {
                 cm.erase();
                 if (prev == null) {
                     s_instances = cm.m_next;
                 } else {
                     prev.m_next = cm.m_next;
                 }
                 return;
             }
             prev = cm;
             cm = cm.m_next;
         }
         try { RecordStore.deleteRecordStore (storename); }
         catch (Exception e) {
             Logger.println ("RMSCache: deleteRMS error: "+e);
         }
     }
     
     public static synchronized RMSCacheManager2 getInstance (String name) {
         RMSCacheManager2 cm = s_instances;
         while (cm != null) {
             if (name.equals(cm.m_storeName)) {
                 return cm;
             }
             cm = cm.m_next;
         }
         s_instances = new RMSCacheManager2 (name, s_instances);
         return s_instances;
     }
     
     public static synchronized void closeAll () {
         RMSCacheManager2 cm = s_instances;
         while (cm != null) {
             cm.finalClose();
             RMSCacheManager2 prev = cm;
             cm = cm.m_next;
             prev.m_next = null;
         }
         s_instances = null;
     }
     
     private String m_storeName;
     private String [] m_names = new String [INITIAL_CAPACITY];
     private int [] m_indexes = new int [INITIAL_CAPACITY];
     private int m_nbEntries = 0;
     private int m_totalSize = INITIAL_CAPACITY;
     private boolean m_tableLoaded;
     private boolean m_modified;
     private RecordStore m_recordStore;
     
     private RMSCacheManager2 m_next;
 
     private ObjLink m_asyncQueue;
     private boolean m_quit;
     private java.lang.Thread m_thread;
 
     private RMSCacheManager2 (String name, RMSCacheManager2 next) {
         super (name);
         if (name.length() == 0) {
             name = EMPTY;
         }
         m_storeName = name;
         m_tableLoaded = false;
         m_modified = false;
         m_next = next;
         m_thread = new java.lang.Thread (this);
         m_thread.start();
     }
     
     private boolean readEntries () {
         m_nbEntries = 0;
         try { 
             byte[] entriesData = m_recordStore.getRecord (1);
             int count = 0;
             int nbRecords = Decoder.bytesToInt16 (entriesData, count); 
             count += 2;
             for (int i= 0; i<nbRecords; i++) {
                 int size = Decoder.bytesToInt16 (entriesData, count); 
                 count += 2;
                 String name = new String (entriesData, count, size);
                 count += size;
                 int index = Decoder.bytesToInt32 (entriesData, count); 
                 count += 4;
                 addEntry (name, index, false);
             }
         } catch (Exception e) {
             Logger.println ("RMSCache: ReadEntries error: "+e);
         }
         m_tableLoaded = true;
         m_modified = false;
         return true;
     }
 
     private boolean saveEntries () {
         byte[] entriesData = new byte[2048];
         int count = 0;
         Decoder.int16ToBytes (m_nbEntries, entriesData, count);
         count += 2;
         for (int i = 0; i < m_nbEntries; i++) {
             final byte[] d = m_names[i].getBytes ();
             final int l = d.length;
             int newSize = count + l + 6; // 4 + 2: string & data byte lengths  
             if (entriesData.length < newSize) { // resize needed
                 byte[] tmp = entriesData;
                 // At least double the current size to prevent to many reallocations
                 if (newSize < tmp.length * 2) {
                     newSize = tmp.length * 2;
                 }
                 entriesData = new byte[newSize];
                 System.arraycopy (tmp, 0, entriesData, 0, count);
             }
             Decoder.int16ToBytes (l, entriesData, count);
             count += 2;
             System.arraycopy (d, 0, entriesData, count, l);
             count += l;
             Decoder.int32ToBytes (m_indexes[i], entriesData, count);
             count += 4;
         }
         try {
             m_recordStore.setRecord (1, entriesData, 0, count);
             m_modified = false;
             return true;
         } catch (RecordStoreException e) {
             Logger.println ("RMSCache: SaveEntries error: "+e);
             return false;
         }
     }
 
     private void sortEntries () {
         boolean again = true;
         int max = m_nbEntries-1;
         while (again) {
             again = false;
             for (int i = 0; i < max; i++) {
                 if (m_names[i].compareTo (m_names[i+1]) > 0) {
                     String tmp = m_names[i+1];
                     m_names[i+1] = m_names[i];
                     m_names[i] = tmp;
                     int k = m_indexes[i+1];
                     m_indexes[i+1] = m_indexes[i];
                     m_indexes[i] = k;
                     again = true;
                 }
             }
         }
     }
 
     private int findEntry (String name) {
         if (name == null || name.length() == 0) {
             name = EMPTY;
         }
         int left = 0;
         int right = m_nbEntries-1;
         int pivot, way;
         while (left <= right) {
             pivot = left + (right - left) / 2;
             way = name.compareTo (m_names[pivot]);
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
 
     private int addEntry (String name, int index, boolean sort) {
         if (name == null || name.length() == 0) {
             name = EMPTY;
         }
         if (m_nbEntries >= m_totalSize) { // expand the array
             String [] tmpNames = new String [m_totalSize+INITIAL_CAPACITY];
             System.arraycopy (m_names, 0, tmpNames, 0, m_totalSize);
             m_names = tmpNames;
             int [] tmpIndexes = new int [m_totalSize+INITIAL_CAPACITY];
             System.arraycopy (m_indexes, 0, tmpIndexes, 0, m_totalSize);
             m_indexes = tmpIndexes;
             m_totalSize += INITIAL_CAPACITY;
         }
         m_names[m_nbEntries] = name;
         m_indexes[m_nbEntries] = index;
         m_nbEntries++;
         m_modified = true;
         if (sort) sortEntries (); // should perform only one loop
         return findEntry (name);
     }
 
     private boolean removeEntry (int id) {
         if (id >= 0 && id < m_nbEntries) {
             int len = m_nbEntries - id - 1;
             if (len > 0) {
                 System.arraycopy (m_names, id+1, m_names, id, len);
                 System.arraycopy (m_indexes, id+1, m_indexes, id, len);
             }
             m_nbEntries--;
             m_modified = true;
             return true;
         }
         return false;
     }
 
     private boolean markRemovedEntry (int id) {
         if (id >= 0 && id < m_nbEntries) {
             m_indexes[id] = -m_indexes[id];
             return true;
         }
         return false;
     }
 
     private boolean markReusedEntry (int id) {
         if (id >= 0 && id < m_nbEntries && m_indexes[id] < -1) {
             m_indexes[id] = -m_indexes[id];
             return true;
         }
         return false;
     }
     
     private byte [] loadData (int id) {
         try {
             byte[] data = m_recordStore.getRecord (id);
             if (data != null && data.length == EMPTY.length() && EMPTY.equals(new String (data))) {
                 return new byte[0]; // empty string
             }
             return data;
         } catch (RecordStoreException e) {
            Logger.println("RMSCache: loadData error: "+e+" for "+m_storeName);
             return null;
         }
     }
 
     private boolean saveData (int id, byte [] data, boolean add) {
         if (data == null || data.length == 0) {
             data = EMPTY.getBytes();
         }
         try {
             if (add) m_recordStore.addRecord (data, 0, data.length);
             else m_recordStore.setRecord(id, data, 0, data.length);
             return true;
         } catch (RecordStoreException e) {
            Logger.println("RMSCache: saveData error: "+e+" for "+m_storeName);
            return false;
         }
     }
     
     private boolean removeData (int id) {
         try {
             m_recordStore.deleteRecord(id);
             return true;
         } catch (RecordStoreException e) {
            Logger.println("RMSCache: removeData error: "+e+" for "+m_storeName);
             return false;
         }
     }
 
     private boolean open () {
         if (m_recordStore == null) {
             try {
                 m_recordStore = RecordStore.openRecordStore (m_storeName, true);
                 if (m_recordStore.getNumRecords() == 0) {
                     // RecordStore creation, add an empty entries table
                     m_recordStore.addRecord(new byte[2], 0, 2);
                 }
             } catch (Exception e) {
                 Logger.println("RMSCache: open error: "+e+" for "+m_storeName);
                 m_nbEntries = 0;
                 return false;
             }
         }
         if (m_tableLoaded) {
             return true;
         }
         m_nbEntries = 0;
         return readEntries ();
     }
 
     // This implementation never closes the RecordStore until application exit.
     public void close () { }
 
     // Called only by closeAll() on application exit. 
     private synchronized void finalClose() {
         m_asyncQueue = null;
         if (!m_quit && m_thread != null) {
             m_quit = true;
             synchronized (m_thread) {
                 m_thread.notify();
             }
         }
     }
     private void finalCloseAsync() {
         Logger.println("RMSCache: final close: "+m_storeName);
         if (m_modified) {
             saveEntries ();
         }
         if (m_recordStore != null) {
             try {
                 m_recordStore.closeRecordStore ();
             } catch (Exception e) {
                 Logger.println("RMSCache: close error: "+e+" for "+m_storeName);
             }
             m_recordStore = null;
         }
     }
 
     public synchronized void erase () {
         m_asyncQueue = null; // purge write queue
         m_modified = false; // prevents saving entries, just close
         finalCloseAsync ();
         m_nbEntries = 0;
         m_tableLoaded = false;
         try {
             RecordStore.deleteRecordStore (m_storeName);
         } catch (Exception e) {
             Logger.println("RMSCache: erase error: "+e+" for "+m_storeName);
         }
     }
 
     public synchronized int getNbCaches () {
         int result = 0;
         if (open ()) {
             result = m_nbEntries;
         }
         return result;
     }
 
     public synchronized boolean hasRecord (String s) {
         if (m_tableLoaded || open ()) {
             int entry = findEntry (s);
             return entry >= 0;
         }
         return false;
     }
 
     public synchronized byte[] getByteRecord (String s) {
         // First, check for an async operation for the given key
         ObjLink o = m_asyncQueue;
         while (o != null) {
             if (o.m_object.equals(s)) {
                 return (byte[])o.m_param;
             }
             o = o.m_next;
         }
         if (open ()) {
             int id = findEntry (s);
             if (id >= 0) {
                 int index = m_indexes[id];
                 if (index >= 0) {
                     byte[] result = loadData (m_indexes[id]);
                     if (result == null) { // corrupted data
                        //Logger.println ("RMSCache: getByteRecord error for "+s+": removing null entry for "+m_storeName+'/'+m_indexes[id]);
                         removeEntry (id);
                         removeData (m_indexes[id]);
                     } // remove the whole store if removing bad entry failed
                     return result;
                 }
             }
         }
         return null;
     }
 
     public synchronized boolean setRecord (String s, byte[] data) {
         if (open ()) {
             int id = findEntry (s);
             if (id == -1) {
                 id = addEntry (s, -1, true);
                 //Logger.println("RMS2: "+m_storeName+": Added entry:"+s+":"+id);
             } else {
                 //Logger.println("RMS2: "+m_storeName+": Reuse entry:"+s+":"+id);
                 markReusedEntry (id);
             }
             addAsyncOp (s, data);
             return true;
         }
         return false;
     }
 
     public synchronized boolean deleteRecord (String s) {
         if (open()) {
             int id = findEntry (s);
             if (id >= 0) {
                 // Write null to delete record asynchronously
                 markRemovedEntry (id);
                 addAsyncOp (s, null);
                 return true;
             }
         }
         return false;
     }
 
     public synchronized int getSizeAvailable () {
         int result = 0;
         try {
             if (open ()) {
                 result = m_recordStore.getSizeAvailable ();
             }
         } catch (Exception e) {
             Logger.println ("RMSCache: getSizeAvailable error: "+e+" for "+m_storeName);
             erase ();
         }
         return result;
     }
 
     private synchronized void flush () {
         if (m_recordStore != null) {
             finalCloseAsync();
             open();
         }
     }
 
     private void addAsyncOp (String key, byte[] value) {
         synchronized (this) {
         if (m_asyncQueue == null) {
             m_asyncQueue = ObjLink.create (key, value, null);
         } else {
             ObjLink o = m_asyncQueue;
             while (true) {
                 if (o.m_object.equals(key)) {
                     o.m_param = value;
                     break;
                 } else if (o.m_next == null) {
                     o.m_next = ObjLink.create (key, value, null);
                     break;
                 }
                 o = o.m_next;
             }
         }
         }
         synchronized (m_thread) {
             m_thread.notify();
         }
     }
 
     private synchronized ObjLink pickAsyncOp () {
         ObjLink o = m_asyncQueue;
         if (o != null) {
             m_asyncQueue = o.m_next;
         }
         return o;
     }
 
     // Main loop for the background thread write
     public void run() {
         try {
             boolean needFlush = false;
             long lastFlushTime = 0;
             while (!m_quit) {
                 if (m_asyncQueue == null) {
                     synchronized (m_thread) {
                         try { m_thread.wait (Integer.MAX_VALUE); } catch (InterruptedException e) {}
                     }
                 }
                 while (doAsyncOp()) needFlush = true;
                 // Always flush RMS after multiple write/delete operations
                 if (needFlush && System.currentTimeMillis() - lastFlushTime > MAX_FLUSH_DELAY) {
                     flush();
                     needFlush = false;
                     lastFlushTime = System.currentTimeMillis();
                 }
             }
         } catch (Throwable t) {
             Logger.println ("RMSCache: "+m_storeName+": Thread died: "+t);
         } finally {
             finalCloseAsync();
             m_thread = null;
         }
     }
 
     private synchronized boolean doAsyncOp () {
         ObjLink o = pickAsyncOp();
         if (o == null) return false;
         String s = (String)o.m_object;
         byte[] data = (byte[])o.m_param;
         ObjLink.release (o);
         if (open ()) {
             int id = findEntry (s);
             if (id != -1) {
                 int index = m_indexes[id];
                 if (index == -1) {
                     try {
                         index = m_recordStore.getNextRecordID();
                     } catch (RecordStoreException e) {
                         Logger.println ("RMSCache: error: Could not get next recordID: "+e+" for "+m_storeName);
                         return false;
                     }
                     m_indexes[id] = index;
                     return saveData (index, data, true);
                 } else if (data != null) {
                     return saveData (index, data, false);
                 } else if (index < -1) {
                     removeData (-index);
                 }
                 return removeEntry (id);
             } else {
                Logger.println("RMS2: "+m_storeName+": Error: Could not find "+s+" in index table");
             }
         }
         return false;
     }
 }
