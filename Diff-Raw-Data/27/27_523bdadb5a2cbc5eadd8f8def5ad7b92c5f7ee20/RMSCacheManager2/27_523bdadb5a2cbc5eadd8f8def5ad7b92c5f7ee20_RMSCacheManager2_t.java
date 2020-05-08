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
  * This implementation uses the special __SAFE RecordStore to know if the all
  * RecordStores where properly closed. If this is not the case, all the RecordStores
  * are removed ! 
  * To prevent loosing important data, the application can still use Managers with
  * names starting with a '_' (e.g. '_SECURE'). This will ensure that the RecordStore
  * is closed when not used, and that they are not erased when the __SAFE marker is not
  * found. 
  */
 class RMSCacheManager2 extends CacheManager {
     private final static int INITIAL_CAPACITY = 10;
 
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
             String name = list[i];
             if (name.charAt(0) != '_') {
                 try { RecordStore.deleteRecordStore (name); } 
                 catch (Exception e) { Logger.println("RMSCache: Could not erase "+name); }
             }
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
                 cm.m_tableLoaded = false;
                 cm.m_modified = false; // speeds up close()
                 cm.finalClose();
                 if (prev == null) {
                     s_instances = cm.m_next;
                 } else {
                     prev.m_next = cm.m_next;
                 }
                 break;
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
         if (s_instances == null) {
             // On application start, delete __SAFE RecordStore,
             // if not present, delete all RMS !
             try {
                 RecordStore.deleteRecordStore ("__SAFE");
             } catch (RecordStoreException e) {
                 Logger.println("RMSCache: Error: No safeguard, clear RMS !");
                 deleteAllRMS ();
             }
         }
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
         // On application exit, create the __SAFE RecordStore
         try {
             RecordStore.openRecordStore("__SAFE", true).closeRecordStore();
         } catch (Exception e) {
             Logger.println ("RMSCache: Error: Could not create safeguard !");
         }
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
 
     private RMSCacheManager2 (String name, RMSCacheManager2 next) {
         super (name);
         if (name.length() == 0) {
             name = EMPTY;
         }
         m_storeName = name;
         m_tableLoaded = false;
         m_modified = false;
         m_next = next;
     }
     
     private boolean readEntries () {
         m_nbEntries = 0;
         try { 
             byte[] entriesData = m_recordStore.getRecord (1);
             int count = 0;
             int nbRecords = bytesToInt16 (entriesData, count); 
             count += 2;
             for (int i= 0; i<nbRecords; i++) {
                 int size = bytesToInt16 (entriesData, count); 
                 count += 2;
                 String name = new String (entriesData, count, size);
                 count += size;
                 int index = bytesToInt32 (entriesData, count); 
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
         int16ToBytes (m_nbEntries, entriesData, count);
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
             int16ToBytes (l, entriesData, count);
             count += 2;
             System.arraycopy (d, 0, entriesData, count, l);
             count += l;
             int32ToBytes (m_indexes[i], entriesData, count);
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
 
     private void int16ToBytes (int src, byte[] dst, int offset) {
         dst[offset] = (byte) (src >>> 8);
         dst[offset+1] = (byte) src;
     }
 
     private void int32ToBytes (int src, byte[] dst, int offset) {
         dst[offset] = (byte) (src >>> 24);
         dst[offset+1] = (byte) (src >>> 16);
         dst[offset+2] = (byte) (src >>> 8);
         dst[offset+3] = (byte) src;
     }
 
     private int bytesToInt16 (byte[] src, int offset) {
         return ((src[offset] & 0xFF) << 8) +
                (src[offset+1] & 0xFF);
     }
 
     private int bytesToInt32 (byte[] src, int offset) {
         return (src[offset] << 24) +
                ((src[offset+1] & 0xFF) << 16) +
                ((src[offset+2] & 0xFF) << 8) +
                (src[offset+3] & 0xFF);
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
 
     private int removeEntry (int id) {
         if (id >= 0 && id < m_nbEntries) {
             int len = m_nbEntries - id - 1;
             if (len > 0) {
                 System.arraycopy (m_names, id+1, m_names, id, len);
                 System.arraycopy (m_indexes, id+1, m_indexes, id, len);
             }
             m_nbEntries--;
             m_modified = true;
         }
         return id;
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
     // Except for RecordStore starting with an _
     public synchronized void close () {
         if (m_storeName.charAt(0) == '_') {
             finalClose();
         }
     }
 
     // Called only by closeAll() on application exit. 
     private synchronized void finalClose() {
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
         m_modified = false; // prevents saving entries, just close
         finalClose ();
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
             //close (); // NOT DONE, TOO SLOW !
         }
         return result;
     }
 
     public synchronized boolean hasRecord (String s) {
         if (m_tableLoaded || open ()) {
             boolean found = findEntry (s) >= 0;
             //close ();
             return found;
         }
         return false;
     }
 
     public synchronized byte[] getByteRecord (String s) {
         byte [] result = null;
         int id = -1;
         if (open ()) {
             if ( (id = findEntry (s)) >= 0) {
                 result = loadData (m_indexes[id]);
                 if (result == null) { // corrupted data
                     Logger.println ("RMSCache: getByteRecord error for "+s+": removing null entry for "+m_storeName+'/'+m_indexes[id]);
                     removeEntry (id);
                     removeData (m_indexes[id]);
                 } // remove the whole store if removing bad entry failed
             }
             //close (); // NOT DONE, TOO SLOW !
         } else {
             Logger.println ("RMSCache: getByteRecord error: Could not open entries table for "+m_storeName);
         }
         return result;
     }
 
     public synchronized boolean setRecord (String s, byte[] data) {
         boolean result = false;
         if (open ()) {
             int id = findEntry (s);
             if (id == -1) {
                 int index;
                 try {
                     index = m_recordStore.getNextRecordID();
                 } catch (RecordStoreException e) {
                     Logger.println ("RMSCache: setRecord error: Could not get next recordID: "+e+" for "+m_storeName);
                     close();
                     return false;
                 }
                 id = addEntry (s, index, true);
                 saveData (m_indexes[id], data, true);
             } else {
                 saveData (m_indexes[id], data, false);
             }
             //close (); // table modified!! need to save it, NOT DONE, TOO SLOW !
         } else {
             Logger.println ("RMSCache: setRecord error: Could not open entries table for "+m_storeName);
         }
         return result;
     }
 
     public synchronized boolean deleteRecord (String s) {
         boolean result = false;
         if (open()) {
             int id = findEntry (s);
             if (id >= 0) {
                 removeData (m_indexes[id]);
                 removeEntry (id);
                 result = true;
             }
             //close (); // NOT DONE, TOO SLOW !
         } else {
             Logger.println ("RMSCache: deleteRecord error: Could not open entries table for "+m_storeName);
         }
         return result;
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
         } //finally { close (); } // NOT DONE, TOO SLOW !
         return result;
     }
 }
