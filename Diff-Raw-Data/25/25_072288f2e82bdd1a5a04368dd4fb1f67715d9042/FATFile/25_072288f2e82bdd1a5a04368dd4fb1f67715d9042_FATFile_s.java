 package com.test;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.file.DirectoryNotEmptyException;
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: uta
  *
  * All [transact-safe] methods have the [ts_] prefix.
  * The [transact-safe] means that methods can terminated successfully,
  *   or restore the object state to initial condition and throw exception,
  *   or mark system as "dirty" and throw exception (critical error in host FS).
  *
  * All public functions have to be [transact-safe] by default.
  */
 public class FATFile {
     public static final int INVALID_FILE_ID = -1;
     public static final int ROOT_FILE_ID = 0;
     public static final int FILE_MAX_NAME = 110;
     public static final char ZAP_CHAR = 0xCDCD;
 
     public static final int TYPE_FILE = 0;
     public static final int TYPE_FOLDER = 1;
     public static final int TYPE_DELETED = -1;
     
     static final FATFile DELETED_FILE = new FATFile(null, TYPE_DELETED, INVALID_FILE_ID, INVALID_FILE_ID);
 
     final FATFileSystem fs;
 
     public static final int RECORD_SIZE = 3*4 + 3*8 + FILE_MAX_NAME*2;  //256 bytes
     // attributes
     private final int type;
     private int fileId = INVALID_FILE_ID;
     private int  access;
     private long size;
     private long timeCreate;
     private long timeModify;
     private final char[] name = new char[FILE_MAX_NAME];
 
     // properties
     private int parentId = INVALID_FILE_ID;
 
     void checkSelfValid() throws IOException {
         if (fileId == INVALID_FILE_ID)
             throw new IOException("Invalid file id.");
     }
 
     void checkValid() throws IOException {
         if (parentId == INVALID_FILE_ID)
             throw new IOException("Invalid parent id.");
     }
 
     public FATFileChannel getChannel(boolean appendMode) throws IOException {
         synchronized (this) {
             checkValid();
             return new FATFileChannel(this, appendMode);
         }
     }
 
     public void delete() throws IOException {
         synchronized (this) {
             if (isFolder() && !isEmpty())
                 throw new DirectoryNotEmptyException(getName());
             if (isRoot())
                 throw new IOException("Cannot delete root.");
             checkValid();
 
             fs.begin(true);
             try {
                 getParent().ts_deRef(this);
                 fs.ts_dropDirtyFile(this);
                 //commit
             } finally {
                 // primitive rollback - dirty mark in [ts_]
                 fs.end();
             }
         }
     }
 
     public boolean isEmpty() {
         return length() == 0;
     }
 
     public FATFolder getParent() throws IOException {
         synchronized (this) {
             checkValid();
             return fs.ts_getFolder(parentId);
         }
     }
 
     public void force(boolean updateMetadata) throws IOException {
         synchronized (this) {
             checkValid();
             fs.begin(true);
             try {
                 if (updateMetadata)
                     setLastModified(FATFileSystem.getCurrentTime());
                 fs.ts_forceFileContent(this, updateMetadata);
             } finally {
                 // primitive rollback - dirty in [ts_dropDirtyFile]
                 fs.end();
             }
         }
     }
 
     public FATFolder getFolder() {
         // null-transaction for no-io operation
         // no lock - type is final.
         return isFolder()
             ? fs.ts_getFolder(fileId)
             : null;
     }
 
     /**
      * Moves file to new location.
      *
      * @param newParent new owner of the file
      * @throws IOException
      */
     public void moveTo(FATFolder newParent) throws IOException {
         synchronized (this) {
             checkSelfValid(); //can add unconnected files. Here is the only point to do it.
             boolean success = false;
             int oldParentId = parentId;
             fs.begin(true);
             try {
                 //redirect any file attribute change (ex. size) to new parent
                 parentId = newParent.ts_getFolderId();
                 newParent.ts_ref(this);
                 if (oldParentId == INVALID_FILE_ID) {
                     // commit
                     success = true;
                 } else{
                     try {
                         fs.ts_getFolder(oldParentId).ts_deRef(this);
                         // commit
                         success = true;
                     } finally {
                         if (!success) {
                             //rollback process
                             boolean successRollback = false;
                             try {
                                 newParent.ts_deRef(this);
                                 successRollback = true;
                             } finally {
                                 if (!successRollback) {
                                     fs.ts_setDirtyState("Cannot rollback movement of the file. ", false);
                                 }
                             }
                         }
                     }
                 }
             } finally {
                 if (!success) {
                     // rollback
                     parentId = oldParentId;
                 }
                 fs.end();
             }
         }
     }
 
     public String getName() {
         String ret = new String(name);
         int zeroPos = ret.indexOf(ZAP_CHAR);
         return (zeroPos == -1)
                 ? ret
                 : ret.substring(0, zeroPos);
     }
 
     @Override
     public String toString() {
         return getName();
     }
 
     /**
      * Gets the file length.
      *
      * @return the file length
      */
     public long length() {
         return size;
     }
 
     /**
      * Sets the length of this file.
      *
      * If the present length of the file as returned by the length method is
      * greater than the newLength argument then the file will be truncated.
      *
      * If the present length of the file as returned by the length method
      * is smaller than the newLength argument then the file will be extended.
      * In this case, the contents of the extended portion of the file are not defined.
      *
      * @param newLength The desired length of the file
      */
     public void setLength(long newLength) throws IOException {
         synchronized (this) {
             checkValid();
             fs.begin(true);
             try {
                 if (newLength == size)
                     return;
                 fs.setFileLength(this, newLength, size);
                 size = newLength;
                 // commit
                 ts_updateAttributes(); //no rollback - [dirty]
             } finally {
                 fs.end();
             }
         }
     }
 
     /**
      * Tests whether the file denoted by this is a directory.
      *
      * Checks the file type against [TYPE_FOLDER] const.
      *
      * @return [true] if and only if the file denoted by this
      *         exists and is a directory; [false] otherwise
      */
     public boolean isFolder() {
         return type == TYPE_FOLDER;
     }
 
     /**
      * Tests whether the file denoted by this is a normal file.
      *
      * Checks the file type against [TYPE_FILE] const.
      *
      * @return  [true] if and only if the file denoted by this exists and
      *          is a normal file; [false] otherwise
      */
     public boolean isFile()  {
         return type == TYPE_FILE;
     }
 
     /**
      * Gets the file access attribute.
      *
      * @return the file access state.
      */
     public int access() {
         return access;
     }
 
     /**
      * Sets the file access attribute.
      *
      * @param access the file access state.
      */
     public void setAccess(int access) throws IOException {
         synchronized (this) {
             checkValid();
             if (this.access == access)
                 return;
             fs.begin(true);
             try {
                 this.access = access;
                 // commit
                 ts_updateAttributes(); //no rollback - [dirty]
             } finally {
                 fs.end();
             }
         }
     }
 
     /**
      * Gets the file creation time attribute.
      *
      * @return the file creation time in milliseconds.
      */
     public long timeCreate() {
         return timeCreate;
     }
 
     /**
      * Sets the file creation time attribute.
      *
      * @param timeCreate the file creation time in milliseconds.
      */
     public void setTimeCreate(long timeCreate) throws IOException {
         synchronized (this) {
             checkValid();
             if (this.timeCreate == timeCreate)
                 return;
             fs.begin(true);
             try {
                 this.timeCreate = timeCreate;
                 // commit
                 ts_updateAttributes(); //no rollback - [dirty]
             } finally {
                 fs.end();
             }
         }
     }
 
     /**
      * Gets the file modification time attribute.
      *
      * @return the file modification time in milliseconds.
      */
     public long lastModified() {
         return timeModify;
     }
 
     /**
      * Sets the file modification time attribute.
      *
      * @param timeModify the file modification time in milliseconds.
      */
     public void setLastModified(long timeModify) throws IOException {
         synchronized (this) {
             checkValid();
             if (this.timeModify == timeModify)
                 return;
             fs.begin(true);
             try {
                 this.timeModify = timeModify;
                 // commit
                 ts_updateAttributes(); //no rollback - [dirty]
             } finally {
                 fs.end();
             }
         }
     }
 
     /**
      * Check the root status for file.
      *
      * @return [true] if it is a root file.
      */
     public boolean isRoot() {
         return (fileId == ROOT_FILE_ID);
     }
 
 
     /**
      * Opens file from id
      *
      * @param fs the FS object
      * @param type the [TYPE_XXXX] const
      * @param fileId the FS unique file Id (in FATSystem that is the start of file chain)
      * @param parentId  the FS unique parent file Id. Parent file need to be a folder,
      *                  parent holds the file attributes.
      */
     FATFile(FATFileSystem fs, int type, int fileId, int parentId) {
         this.fs = fs;
         this.type = type;
         // both ids validated in upper calls
         this.fileId = fileId;
         this.parentId = parentId;
         if (type == TYPE_DELETED) {
             Arrays.fill(name, (char)0xFFFF);
         }
     }
 
     /**
      * Creates File in FAT with allocated space.
      *
      * Could not be called directly, use [fs.ts_createFile] instead.
      *
      * @param fs the FS object
      * @param name the name of created file
      * @param type the [TYPE_XXXX] const
      * @param size the size of created file, that need to be allocated
      * @param access the desired access
      * @throws IOException
      */
     FATFile(FATFileSystem fs, String name, int type, long size, int access) throws IOException {
         ts_initName(name);
         this.fs = fs;
         fileId = fs.ts_allocateFileSpace(size);
         if (fileId == ROOT_FILE_ID)
             parentId = ROOT_FILE_ID;
         this.type = type;
         this.size = size;
         timeCreate = FATFileSystem.getCurrentTime();
         timeModify = FATFileSystem.getCurrentTime();
         this.access = access;
     }
 
     void ts_initFromBuffer(ByteBuffer bf) {
         // [fileId] and [type] was read before for [ctr] call
         size = bf.getLong();
         timeCreate = bf.getLong();
         timeModify = bf.getLong();
         access = bf.getInt();
         // only UNICODE name for performance and compatibility reasons
         bf.asCharBuffer().get(name);
     }
 
     ByteBuffer ts_serialize(ByteBuffer bf, int version) {
         bf
                 .putInt(fileId)
                 .putInt(type)
                 .putLong(size)
                 .putLong(timeCreate)
                 .putLong(timeModify)
                 .putInt(access);
         // only UNICODE name for performance and compatibility reasons
         bf.asCharBuffer().put(name);
         bf.position(bf.position() + name.length*2);
         return bf;
     }
 
     /**
      * Updates attribute info in parent record if any.
      *
      * Have to be called under [fileLock].
      * @throws IOException
      */
     private void ts_updateAttributes() throws IOException {
         if (parentId != INVALID_FILE_ID) {
             fs.ts_getFolder(parentId).ts_updateFileRecord(this);
         }
     }
 
     private void ts_initName(String fileName) {
         int len = fileName.length();
         if (len > FILE_MAX_NAME)
             throw new IllegalArgumentException("Name is too long. Max length is " + FILE_MAX_NAME);
         synchronized (this) {
             Arrays.fill(name, ZAP_CHAR);
             System.arraycopy(fileName.toCharArray(), 0, name, 0, len);
             //no update here! That is init!
         }
     }
 
     int ts_getFileId() {
         return fileId;
     }
 
     Throwable ttt;
     String info;
     void ts_setFileId(int fileId) {
         synchronized (this) {
             ttt = new Throwable();
             info = Thread.currentThread().toString() + " " + this.hashCode() + " " + this + " " + this.fileId;
             this.fileId = fileId;
         }
     }
 }
