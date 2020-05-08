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
 import javax.microedition.rms.*;
 
 class RMSCacheManager extends CacheManager {
     RecordStore m_name, m_data, m_csum;
     String m_nameStore, m_dataStore, m_csumStore;
     byte [] csum; // used temporarily to compute the checksum when writing or reading data
 
     // Delete all RecordStores, withchecking deletion for the LG Rodeo
     public static void deleteAllRMS () {
         String[] list = RecordStore.listRecordStores ();
         int size = list != null ? list.length : 0;
         int max = size > 10 ? size : 10;
         int cnt = 0;
         while (size > 0 && cnt++<max) {
             for (int i=0; i<size; i++) {
                 try { RecordStore.deleteRecordStore (list[i]); } catch (Exception e) {}
             }
             list = RecordStore.listRecordStores ();
             size = list != null ? list.length : 0;
         }
         if (size > 0) {
             Logger.println ("ERROR: Did not delete all RS !?");
         }
     }
 
 
     // completely delete a record store
     static void deleteRMS (String baseName) {
         deleteUnchecked (baseName);
         // checking deletion for the LG Rodeo
         int cnt = 0, max = 10;
         int matchLength = baseName.length() + 1; // +1 => n, d or c
         boolean recheck;
         do {
             recheck = false;
             String[] list = RecordStore.listRecordStores ();
             int size = list == null ? 0 : list.length;
             for (int i=0; i<size; i++) {
                 // match /basename[ndc]/ as RecordStore names always finish by n, c or d
                 if (list[i].length() == matchLength && list[i].startsWith(baseName)) {
                     try { RecordStore.deleteRecordStore (list[i]); } catch (Exception e) {}
                     recheck = true; // will need to recheck it was really deleted !
                 }
             }
         } while (cnt++<max && recheck);
     }
     
     private static void deleteUnchecked (String baseName) {
         try { RecordStore.deleteRecordStore (baseName+'n'); } catch (Exception e) { Logger.println ("del: Except. in deleteRecordStore/name: "+e); }
         try { RecordStore.deleteRecordStore (baseName+'d'); } catch (Exception e) { Logger.println ("del: Except. in deleteRecordStore/data: "+e); }
         try { RecordStore.deleteRecordStore (baseName+'c'); } catch (Exception e) { Logger.println ("del: Except. in deleteRecordStore/csum: "+e); }
     }
 
     public RMSCacheManager (String name) {
         super (name);
         m_nameStore = name+'n';
         m_dataStore = name+'d';
         m_csumStore = name+'c';
         csum = new byte [4];
     }
 
     
     private boolean open () {
          if (safeOpen () == false) {
              return safeOpen (); // try again as if the first one failed it has also cleaned the RMS
          }
          return true;
      }
 
     // open the three stores and clean up the stores if an exception is raised
     private boolean safeOpen () {
         boolean error = false;
          try {
              m_name = RecordStore.openRecordStore (m_nameStore, true);
              try {
                  m_data = RecordStore.openRecordStore (m_dataStore, true);
                  try {
                      m_csum = RecordStore.openRecordStore (m_csumStore, true);
                  } catch (Exception e) {
                      error = true; Logger.println ("sOpen: Except. in openRecordStore/csum: "+e);
                  }
              } catch (Exception e) {
                  error = true; Logger.println ("sOpen: Except. in openRecordStore/data: "+e);
              }
          } catch (Exception e) {
              error = true; Logger.println ("sOpen: Except. in openRecordStore/name: "+e);
          }
          if (error) {
              close ();
              erase ();
          }
          return !error;
     }
     
     // silently force closing of all stores
     void close () {
         try { if (m_name != null) m_name.closeRecordStore(); } catch (Exception e) { Logger.println ("close: Except. in closeRecordStore/name: "+e); } finally { m_name = null; }
         try { if (m_data != null) m_data.closeRecordStore(); } catch (Exception e) { Logger.println ("close: Except. in closeRecordStore/data: "+e); } finally { m_data = null; }
         try { if (m_csum != null) m_csum.closeRecordStore(); } catch (Exception e) { Logger.println ("close: Except. in closeRecordStore/csum: "+e); } finally { m_csum = null; }
     }
 
     void erase () {
         try { RecordStore.deleteRecordStore (m_nameStore); } catch (Exception e) { /*Logger.println ("erase: Except. in closeRecordStore/name@"+m_nameStore+": "+e);*/ }
         try { RecordStore.deleteRecordStore (m_dataStore); } catch (Exception e) { /*Logger.println ("erase: Except. in closeRecordStore/data@"+m_dataStore+": "+e);*/ }
         try { RecordStore.deleteRecordStore (m_csumStore); } catch (Exception e) { /*Logger.println ("erase: Except. in closeRecordStore/csum@"+m_csumStore+": "+e);*/ }
     }
 
     public int getNbCaches () {
         int result = 0;
         try {
             if (open ()) {
                 result = m_name.getNumRecords(); 
             }
         } catch (Exception e) { // corrupted RMS
             Logger.println ("getNbCache: Except. in getNumRecords: "+e);
             close (); erase ();
         } finally {
             close ();
         }
         return result;
     }
 
     private int unsecureFind (String s) throws Exception {
         if (s.equals (DELETED)) { //MCP: Prevent accessing a deleted record, see del() method
             return -1;
         } else if (s == null || s.length() == 0) {
             s = EMPTY;
         }
         int nb = 0;
         try {
             nb = m_name.getNumRecords()+1; // +1 as first record is 1
         } catch (Exception e) {
             Logger.println ("uFind: Except. in getNumRecords: "+e);
             return -1;
         }
         byte [] target = s.getBytes();
         byte [] name = null;
         for (int id = 1 ; id < nb ; id++) {
             try {
                 name = m_name.getRecord (id);
             } catch (Exception e) {
                 Logger.println ("uFind: Except. in getRecord/name#"+id+": "+e);
                 return -1;
             }
             if (isEqual (target, name)) { 
                 return id;
             }
         }
         return -1;
     }
 
     boolean unsecureSet (int id, byte [] data) throws Exception {
         if (data == null || data.length == 0) {
             data = EMPTY.getBytes ();
         }
         if (m_data.getSizeAvailable () > data.length) {
             try {
                 m_data.setRecord (id, data, 0, data.length); 
             } catch (Exception e) {
                 Logger.println ("uSet: Except. in setRecord/data#"+id+": "+e);
                 unsecureDel (id);
                 return false;
             }
             try {
                 m_csum.setRecord (id, computeHashcode (data, csum), 0, 4);
             } catch (Exception e) {
                 Logger.println ("uSet: Except. in setRecord/csum#"+id+": "+e);
                 unsecureDel (id);
                 return false;
             }
             return true;
         }
         return false;
     }
 
     boolean unsecureAdd (String s, byte [] data) throws Exception {
         if (s == null || s.length() == 0) {
             s = EMPTY;
         }
         byte [] name = s.getBytes ();
         if (m_data.getSizeAvailable () > data.length && 
             m_name.getSizeAvailable () > name.length) {
             try {
                 m_name.addRecord (name, 0, name.length);
             } catch (Exception e) {
                 Logger.println ("uAdd: Except. in addRecord/name@"+s+": "+e);
                 return false;
             }
             try {
                 m_data.addRecord (data, 0, data.length);
             } catch (Exception e) {
                 Logger.println ("uAdd: Except. in addRecord/data@"+s+": "+e);
                 return false;
             }
             try {
                 m_csum.addRecord (computeHashcode (data, csum), 0, 4);
             } catch (Exception e) {
                 Logger.println ("uAdd: Except. in addRecord/csum@"+s+": "+e);
                 Logger.println ("    Csum store: "+m_csum);
                 Logger.println ("    data: "+data);
                 Logger.println ("    csum buf: "+csum);
                 return false;
             }
             return true;
         }
         return false;
     }
 
     boolean unsecureDel (int id) throws Exception {
         boolean result = true;
         if (id != -1) {
             try { 
                 m_name.setRecord(id, DELETED.getBytes(), 0, DELETED.getBytes().length);
             } catch (Exception e) { 
                 Logger.println ("uDel: Except. in setRecord/name#"+id+": "+e); 
                 result = false;
             }
             try { 
                 m_data.deleteRecord (id);
             } catch (Exception e) { 
                 Logger.println ("uDel: Except. in deleteRecord/data#"+id+": "+e); 
                 result = false;
             }
             try { 
                 m_csum.deleteRecord (id);
             } catch (Exception e) { 
                 Logger.println ("uDel: Except. in deleteRecord/csum#"+id+": "+e); 
                 result = false;
             }
             return true;
         }
         return result;
     }
 
     synchronized byte[] getByteRecord (String s) {
         byte [] result = null;
         int id = -1;
         try {
             if (open () && (id = unsecureFind (s)) >= 0) {
                 try {
                     result = m_data.getRecord (id);
                     if (result.length == EMPTY.length() && EMPTY.equals (new String (result))) {
                         result = new byte[0]; // empty string
                     }
                 } catch (Exception e) {
                     Logger.println ("getByteRecord: Except. in getRecord/data#"+id+": "+e);
                     result = null;
                 }
                 if (result == null || compareChecksums (m_csum.getRecord (id), computeHashcode (result, csum)) == false) { // corrupted data
                     result = null;
                     if (unsecureDel (id) == false) {
                         erase ();
                     } // remove the whole store if removing bad entry failed
                 }
             }
         } catch (Exception e) {
             Logger.println ("getByteRecord: Except. unexpected: "+e);
         } finally { close (); }
         return result;
     }
 
     synchronized boolean hasRecord (String s) {
         try {
             if (open () && unsecureFind (s) >= 0) {
                 return true;
             }
         } catch (Exception e) {
             Logger.println ("hasRecord: Except. unexpected: "+e);
         } finally { close (); }
         return false;
     }
 
     synchronized boolean setRecord (String s, byte[] data) {
         boolean result = false;
         try {
             if (open ()) {
                 int id = unsecureFind (s);
                 if (id == -1) {
                     result = unsecureAdd (s, data);
                 } else {
                     result = unsecureSet (id, data);
                 }
             }
         } catch (Throwable e) { 
             Logger.println ("setRecord: Except. unexpected: "+e);
         } finally { close (); }
         return result;
     }
 
     synchronized boolean deleteRecord (String s) {
         boolean result = false;
         try {
             if (open()) {
                 int id = unsecureFind (s);
                 if (id != -1) {
                     result = unsecureDel (id);
                 }
             }
         } catch (Exception e) { 
             Logger.println ("deleteRecord: Except. unexpected: "+e);
         } finally { close (); }
         return result;
     }
 
     synchronized int getSizeAvailable () {
         int result = 0;
         try {
             if (open ()) {
                 result = m_data.getSizeAvailable ();
             }
         } catch (Exception e) {
             Logger.println ("getSizeAvailable: Except. in getSizeAvailable/data: "+e);
             erase ();
         } finally { close (); }
         return result;
     }
 
 }
