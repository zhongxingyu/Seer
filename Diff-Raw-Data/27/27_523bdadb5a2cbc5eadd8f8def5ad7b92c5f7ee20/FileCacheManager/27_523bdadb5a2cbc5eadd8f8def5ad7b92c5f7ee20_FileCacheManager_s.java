 //#condition MM.CacheUseFile
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
 import java.io.*;
 import java.util.*;
 import javax.microedition.io.*;
 import javax.microedition.io.file.*;
 import java.lang.ref.WeakReference;
 
 class FileCacheManager extends CacheManager {
     private final static String CONTENT_PREFIX = "content";
     private final static String CONTENT_EXT = ".dat";
     private final static String CONTENT_EXT_BKP = ".bkp";
     private final static String ENTRIES_NAME = "entries.db";
     private final static String ENTRIES_BCKP = "entries.bkp";
     private final static int INITIAL_CAPACITY = 10;
 
     String m_storeName;
     String m_dbgName;
     String [] m_names = new String [INITIAL_CAPACITY];
     int [] m_indexes = new int [INITIAL_CAPACITY];
     int m_nbEntries = 0;
     int m_totalSize = INITIAL_CAPACITY;
     int m_lastIndex = 0;
     boolean m_tableLoaded;
     boolean m_modified;
 
     static WeakReference [] s_references = new WeakReference [16];
 
     private boolean readEntries (String filename) {
 
         FileConnection fc = null;
         DataInputStream dis = null;
         m_lastIndex = 1;
         try {
             fc = (FileConnection) Connector.open (m_storeName+ENTRIES_BCKP);
             if (!fc.exists()) {
                 fc.close();
                 fc = (FileConnection) Connector.open (m_storeName+ENTRIES_NAME);
             } else {
                 Logger.println (m_dbgName+"readEntries: backup still present ! Using it");
             }
             if (fc.exists ()) { // a table already here
                 dis = fc.openDataInputStream();
                 m_lastIndex = dis.readInt ();
                 boolean again = true;
                 while (again) {
                     try { // try to read a new entry
                         String name = dis.readUTF ();
                         int index = dis.readInt ();
                         if (m_lastIndex < index) {
                             m_lastIndex = index;
                         }
                         addEntry (name, index);
                     } catch (Exception e) { // should check for eof exception
                         again = false;
                     }
                 }
             }
             m_tableLoaded = true;
             m_modified = false;
         } catch (Exception e) {
             Logger.println (m_dbgName+"readEntries: cannot open entries of "+m_storeName+" for reading because of "+e);
         } finally {
             try { 
                 if (dis != null) { dis.close (); }
                 if (fc != null)  { fc.close (); }
             } catch (Exception e) { Logger.println (m_dbgName+"readEntries: cannot close entries of "+m_storeName+" for reading because of "+e); }
         }
         return true;
     }
 
     private boolean saveEntries () {
         FileConnection fc = null, fcBckp = null;
         DataOutputStream dos = null;
         boolean result = false;
         String name = m_storeName+ENTRIES_NAME;
         try {
             fc = (FileConnection) Connector.open (name, Connector.READ_WRITE);
             if (!fc.exists()) {
                 try { fc.create (); } catch (Exception e) { Logger.println (m_dbgName+"saveEntries: cannot create file "+name+" because of "+e); }
             } else { 
                 // backup previous version until end of write
                 try {
                     safeRename (fc, m_storeName, ENTRIES_BCKP);
                     fcBckp = fc;
                     fc = (FileConnection) Connector.open (name, Connector.READ_WRITE);
                     fc.create ();
                 } catch (Exception e) { Logger.println (m_dbgName+"saveEntries: cannot backup "+name+" because of "+e); }
             }
             dos =  fc.openDataOutputStream();
             dos.writeInt (m_lastIndex);
             for (int i = 0; i < m_nbEntries; i++) {
                 dos.writeUTF (m_names[i]);
                 dos.writeInt (m_indexes[i]);
             }
             dos.flush();
             if (fcBckp != null) {
                 fcBckp.delete(); // delete backup
             }
             result = true;
             m_modified = false;
         } catch (Exception e) {
             Logger.println (m_dbgName+"saveEntries: cannot open "+name+" for writing because of "+e);
         } finally {
             try { 
                 if (dos != null) { dos.close (); }
                 if (fc != null) { fc.close (); }
                 if (fcBckp != null) { fcBckp.close(); }
             } catch (Exception e) { Logger.println (m_dbgName+"saveEntries: cannot close file "+name+" for reading because of "+e); }
         }
         return result;
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
         if (name.equals (DELETED)) { //MCP: Prevent accessing a deleted record, see del() method
             return -1;
         } else if (name == null || name.length() == 0) {
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
         //Logger.println ("findEntry: "+name+" => not found");
         return -1;
     }
 
     private int addEntry (String name, int index) {
         //Logger.println ("addEntry "+name+", "+index);
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
         sortEntries (); // should perform only one loop
         return findEntry (name);
     }
 
     // remove the entry by id
     private int removeEntry (int id) {
         //Logger.println ("removeEntry #"+id);
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
 
     // remove the entry by name and return the associated index (to remove the file)
     private int removeEntry (String name) {
         //Logger.println ("removeEntry "+name);
         return removeEntry (findEntry (name));
     }
     
     private byte [] loadData (int id) {
         FileConnection fc = null;
         DataInputStream dis = null;
         int size = 0;
         String path = m_storeName+CONTENT_PREFIX+id;
         byte [] data = null;
         try {
             fc = (FileConnection) Connector.open (path+CONTENT_EXT_BKP);
             if (!fc.exists()) {
                 fc.close();
                 fc = (FileConnection) Connector.open (path+CONTENT_EXT);
             } else {
                 Logger.println (m_dbgName+"loadData: backup still present ! Using it.");
             }
             size = (int)fc.fileSize ();
             if (size > 0) {
                 dis = fc.openDataInputStream();
                 size = dis.readInt();
                 int checksum = dis.readInt();
                 data = new byte[size];
                 dis.readFully (data);
                 if (checksum != computeHashNumber (data)) {
                     Logger.println ("loadData for "+path+": hashcodes differ: "+checksum+" <> "+computeHashNumber (data));
                     data = null;
                 }
             } else {
                 data = new byte[0]; // empty string
             }
         } catch (Exception e) { 
             Logger.println (m_dbgName+"loadData: cannot read data from "+path+"because of "+e);
             data = null; 
         } finally {
             try {
                 if (dis != null) { dis.close (); }
                 if (fc != null) { fc.close (); }
             } catch (Exception e) { Logger.println (m_dbgName+"loadData: cannot close file "+path+" because of "+e); }
         }
         //Logger.println ("loadData returning "+size+" bytes: "+data);
         return data;
     }
 
     private boolean saveData (int id, byte [] data) {
         //Logger.println (m_dbgName+"saveData: "+path+" for "+data.length+" bytes");
         FileConnection fc = null, fcBckp = null;
         DataOutputStream dos = null;
         boolean result = false;
         String path = m_storeName+CONTENT_PREFIX+id+CONTENT_EXT;
         try {
             fc = (FileConnection) Connector.open (path, Connector.READ_WRITE);
             if (!fc.exists()) {
                 fc.create ();
             } else {
                 // backup previous version
                 safeRename (fc, m_storeName, CONTENT_PREFIX+id+CONTENT_EXT_BKP);
                 fcBckp = fc;
                 fc = (FileConnection) Connector.open (path, Connector.READ_WRITE);
                 fc.create ();
             }
             dos =  fc.openDataOutputStream();
             dos.writeInt (data.length);
             dos.writeInt (computeHashNumber (data));
             dos.write (data, 0, data.length);
             dos.flush(); 
             if (fcBckp != null) {
                 fcBckp.delete(); // delete backup
             }
             result = true;
         } catch (Exception e) {
             Logger.println (m_dbgName+"saveData: cannot write data to "+path+" because of "+e);
         } finally {
             try { 
                 if (dos != null) { dos.close (); }
                 if (fc != null) { fc.close (); }
                 if (fcBckp != null) { fcBckp.close(); }
             } catch (Exception e) { Logger.println (m_dbgName+"saveData: cannot close file "+path+" because of "+e); }
         }
         return result;
     }
     
     private boolean removeData (int id) {
         String path = m_storeName+CONTENT_PREFIX+id;
         FileConnection fc = null;
         boolean result = false;
         try {
             // Delete backup if any
             fc = (FileConnection) Connector.open (path+CONTENT_EXT_BKP);
             if (fc.exists()) {
                 fc.delete();
             }
             fc.close();
             // Delete file
             fc = (FileConnection) Connector.open (path+CONTENT_EXT);
             fc.delete ();
             result = true;
         } catch (Exception e) { 
             Logger.println (m_dbgName+"removeData: cannot delete "+path+" because of "+e);
         } finally {
             try {
                 if (fc != null) fc.close ();
             } catch (Exception e) { Logger.println (m_dbgName+"removeData: cannot close file "+path+" because of "+e); }
         }
         return result;
     }
 
     // Delete all RecordStores, with checking deletion for the LG Rodeo
     public static void deleteAllFiles () {
         for (int i = 0; i < s_references.length; i++) {
             if (s_references[i] != null) {
                 FileCacheManager fcm = (FileCacheManager)s_references[i].get();
                 if (fcm != null) {
                     fcm.m_tableLoaded = false;
                     //Logger.println ("FCM.deleteAllFiles: removed "+fcm.m_storeName);
                 }
             }
         }
         recursiveDirDelete (s_basePath);
     }
 
 
     // completely delete a record store, i.e. a directory
     static void deleteFiles (String storename) {
         if (storename.length() == 0) {
             storename = EMPTY;
         }
         String fullName = s_basePath+storename+"/";
         for (int i = 0; i < s_references.length; i++) {
             if (s_references[i] != null) {
                 FileCacheManager fcm = (FileCacheManager)s_references[i].get();
                 if (fcm != null && fullName.equals (fcm.m_storeName)) {
                     fcm.m_tableLoaded = false;
                     //Logger.println ("FCM.deleteFiles: removed "+fcm.m_storeName);
                     break;
                 }
             }
         }
         recursiveDirDelete (fullName);
     }
 
     private static void recursiveDirDelete (String dirname) {
         //Logger.println ("FileCacheManager.recursiveDirDelete: "+dirname);
         FileConnection fc = null;
         try {
             fc = (FileConnection) Connector.open(dirname);
         } catch (Exception e) {
             Logger.println ("FileCacheManager.delete: Cannot list files in "+dirname+" because of "+e);
             return;
         }
         if (fc == null) { return; } // no file or directory with this name
         try {
             Enumeration filelist = fc.list ();
             if (filelist == null) { return; } //empty directory
             while (filelist.hasMoreElements()) {
                 String file = (String) filelist.nextElement();
                 //Logger.println ("FCM: delete "+file);
                 if  (file.endsWith ("/")) { //recursively delete
                     recursiveDirDelete (dirname+file);
                 }
                 // delete file
                 try {
                     FileConnection tmp = (FileConnection)Connector.open(dirname+file, Connector.READ_WRITE);
                     tmp.delete ();
                     tmp.close();
                 } catch (IOException ioe) { Logger.println ("FileCacheManager.delete: cannot delete file "+(dirname+file)+" because of "+ioe); }
             }
             fc.close();
         } catch (Exception e) {
             Logger.println ("FileCacheManager.delete: Exception: "+e);
         }
     }
     
     private static void safeRename (FileConnection fc, String path, String newName) throws Exception {
         // When newName already exists, the rename fails.
         try { 
             fc.rename (newName);
         } catch (IOException e) {
             // Delete the previous file first
             FileConnection fcOld = (FileConnection) Connector.open (path+newName);
             if (fcOld.exists ()) {
                 fcOld.delete ();
             }
             fcOld.close ();
             // Retry
             fc.rename (newName);
         }
     }
     
     public FileCacheManager (String name) {
         super (name);
         if (name.length() == 0) {
             name = EMPTY;
         }
         m_storeName = s_basePath+name;
         if (!m_storeName.endsWith ("/")) {
             m_storeName += "/";
         }
         m_dbgName="["+name+"] ";
         m_tableLoaded = false;
         m_modified = false;
         int target = -1;
         for (int i = 0; i < s_references.length; i++) {
             if (s_references[i] == null || s_references[i].get () == null) {
                 target = i;
                 break;
             }
         }
         if (target == -1) {
             target = s_references.length;
             WeakReference [] tmp = new WeakReference [target*2];
             System.arraycopy (s_references, 0, tmp, 0, target);
             s_references = tmp;
         }
         //Logger.println ("FCM ctr: adding weak reference "+name+" @"+target);
         s_references[target] = new WeakReference (this);
     }
 
     private boolean checkDir (String path) {
         FileConnection fc = null;
         // boolean exists = false;
         //Logger.println ("%% checkDir: trying '"+path+"'");
         try {
             fc = (FileConnection) Connector.open (path);
             if (!fc.exists ()) {
                 fc.close (); // to be sure implementation does not rercod bad states
                 // create subdirs if needed
                 int len = path.length ();
                 if (len > 1) { // we can have at least "a/"
                     int index = path.lastIndexOf ('/', len - 2);
                     if (index > -1) {
                         checkDir (path.substring (0, index+1));
                     }
                 }
                 // create dir itself
                 fc = (FileConnection) Connector.open (path);
                 //Logger.println ("%% checkDir: creating '"+path+"'");
                 fc.mkdir ();
                 //} else {
                 //Logger.println ("%% checkDir: already exists '"+path+"'");
             }
             fc.close ();
         } catch (Exception e) {
             Logger.println (m_dbgName+"open: cannot open or create main store dir "+path+" because of "+e);
             return false;
         }
         return true;
     }
     
     private boolean open () {
         if (m_tableLoaded) {
             return true;
         }
         m_nbEntries = 0;
         if (!checkDir (m_storeName)) {
             return false;
         }
         return readEntries (m_storeName+ENTRIES_NAME);
 
     }
     
     // silently force closing of all stores
     void close () {
         if (m_modified) {
             //Logger.println (m_dbgName+"close store: "+m_storeName);
             saveEntries ();
         }
         // should flush the caches ??
     }
 
     void erase () {
         //Logger.println (m_dbgName+"erase store: "+m_storeName);
         close ();
         m_nbEntries = 0;
         m_tableLoaded = false;
         recursiveDirDelete (m_storeName);
     }
 
     public int getNbCaches () {
         int result = 0;
         if (open ()) {
             result = m_nbEntries;
             close ();
         }
         return result;
     }
 
     synchronized boolean hasRecord (String s) {
         if (open ()) {
             return findEntry (s) >= 0;
             //close (); no need to close, nothing modified
         }
         return false;
     }
 
     synchronized byte[] getByteRecord (String s) {
         //Logger.println (m_dbgName+"getByteRecord for "+s);
         byte [] result = null;
         int id = -1;
         if (open ()) {
             if ( (id = findEntry (s)) >= 0) {
                 result = loadData (m_indexes[id]);
                 if (result != null && result.length == EMPTY.length() && EMPTY.equals (new String (result))) {
                     result = new byte[0]; // empty string
                 }
                 if (result == null) { // corrupted data
                     Logger.println ("getByteRecord for "+s+": removing entry because loadData returned null for "+m_storeName+CONTENT_PREFIX+m_indexes[id]+CONTENT_EXT);
                     removeEntry (id);
                     removeData (m_indexes[id]);
                 } // remove the whole store if removing bad entry failed
             }
             close ();
         } else {
             Logger.println (m_dbgName+"getByteRecord for "+s+" cannot open entries.db");
         }
         return result;
     }
 
     synchronized boolean setRecord (String s, byte[] data) {
         //Logger.println (m_dbgName+" setRecord: "+s+" with "+data.length+" bytes");
         boolean result = false;
         if (open ()) {
             int id = findEntry (s);
             if (id == -1) {
                 m_lastIndex++;
                 id = addEntry (s, m_lastIndex);
             }
             saveData (m_indexes[id], data);
             close (); // table modified!! need to save it
             // save the data in file
         } else {
             Logger.println (m_dbgName+"setRecord: cannot set record "+s+" because entries.db cannot be opened");
         }
         return result;
     }
 
     synchronized boolean deleteRecord (String s) {
         //Logger.println (m_dbgName+"deleteRecord: "+s);
         boolean result = false;
         if (open()) {
             int id = findEntry (s);
             if (id >= 0) {
                 removeData (m_indexes[id]);
                 removeEntry (id);
                 result = true;
             }
             close ();
         } else {
             Logger.println (m_dbgName+"setRecord: cannot delete record "+s+" because entries.db cannot be opened");
         }
         return result;
     }
 
     synchronized int getSizeAvailable () {
         FileConnection fc = null;
         int size = 0;
         try {
             fc = (FileConnection) Connector.open (m_storeName);
             size = (int)fc.availableSize();
         } catch (Exception e) {
             Logger.println (m_dbgName+"getSizeAvailable: cannot open store because of "+e);
             return 0;
         }
         try { 
             fc.close (); 
         } catch (Exception e) { Logger.println (m_dbgName+"getSizeAvailable: cannot open file "+m_storeName+" for reading because of "+e); }
         return size;
     }
 
 }
