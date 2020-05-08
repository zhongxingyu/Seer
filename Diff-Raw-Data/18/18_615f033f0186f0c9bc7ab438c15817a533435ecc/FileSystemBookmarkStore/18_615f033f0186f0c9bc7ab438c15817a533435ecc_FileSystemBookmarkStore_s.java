 /*******************************************************************************
  * Copyright 2006, The Board of Regents of the University of Wisconsin System.
  * All rights reserved.
  *
  * A non-exclusive worldwide royalty-free license is granted for this Software.
  * Permission to use, copy, modify, and distribute this Software and its
  * documentation, with or without modification, for any purpose is granted
  * provided that such redistribution and use in source and binary forms, with or
  * without modification meets the following conditions:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *c
  * 3. Redistributions of any form whatsoever must retain the following
  * acknowledgement:
  *
  * "This product includes software developed by The Board of Regents of
  * the University of Wisconsin System."
  *
  *THIS SOFTWARE IS PROVIDED BY THE BOARD OF REGENTS OF THE UNIVERSITY OF
  *WISCONSIN SYSTEM "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
  *BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  *PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE BOARD OF REGENTS OF
  *THE UNIVERSITY OF WISCONSIN SYSTEM BE LIABLE FOR ANY DIRECT, INDIRECT,
  *INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  *LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  *LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  *OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  *ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 
 package edu.wisc.my.portlets.bookmarks.dao.file;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import edu.wisc.my.portlets.bookmarks.dao.BookmarkStore;
 import edu.wisc.my.portlets.bookmarks.domain.BookmarkSet;
 
 /**
  * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
  * @version $Revision$
  */
 public class FileSystemBookmarkStore implements BookmarkStore {
     protected final Log logger = LogFactory.getLog(this.getClass());
     
     private String baseStorePath = null;
     
     
     /**
      * @return Returns the baseStorePath.
      */
     public String getBaseStorePath() {
         return this.baseStorePath;
     }
     /**
      * @param baseStorePath The baseStorePath to set.
      */
     public void setBaseStorePath(String baseStorePath) {
         this.baseStorePath = baseStorePath;
     }
     
     /**
      * @see edu.wisc.my.portlets.bookmarks.dao.BookmarkStore#getBookmarkSet(java.lang.String, java.lang.String)
      */
     public BookmarkSet getBookmarkSet(String owner, String name) {
         final File storeFile = this.getStoreFile(owner, name);
         
         try {
             final FileInputStream fis = new FileInputStream(storeFile);
             final BufferedInputStream bis = new BufferedInputStream(fis);
             final XMLDecoder d = new XMLDecoder(bis);
 
             try {
                 final BookmarkSet bs = (BookmarkSet)d.readObject();
                 return bs;
             }
             finally {
                 d.close();
             }
         }
         catch (FileNotFoundException fnfe) {
            logger.error("Error reading BookmarkSet for owner='" + owner + "', name='" + name + "'", fnfe);
            return null;
         }
     }
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.dao.BookmarkStore#storeBookmarkSet(edu.wisc.my.portlets.bookmarks.domain.BookmarkSet)
      */
     public void storeBookmarkSet(BookmarkSet bookmarkSet) {
         if (bookmarkSet == null) {
             throw new IllegalArgumentException("AddressBook may not be null");
         }
         
         final File storeFile = this.getStoreFile(bookmarkSet.getOwner(), bookmarkSet.getName());
         
         try {
             final FileOutputStream fos = new FileOutputStream(storeFile);
             final BufferedOutputStream bos = new BufferedOutputStream(fos); 
             final XMLEncoder e = new XMLEncoder(bos);
             try {
                 e.writeObject(bookmarkSet);
             }
             finally {
                 e.close();
             }
         }
         catch (FileNotFoundException fnfe) {
            logger.error("Error Storing BookmarkSet='" + bookmarkSet + "'", fnfe);
         }
     }
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.dao.BookmarkStore#removeBookmarkSet(java.lang.String, java.lang.String)
      */
     public void removeBookmarkSet(String owner, String name) {
         final File storeFile = this.getStoreFile(owner, name);
         storeFile.delete();
     }
     
 
     /**
      * @see edu.wisc.my.portlets.bookmarks.dao.BookmarkStore#createBookmarkSet(java.lang.String, java.lang.String)
      */
     public BookmarkSet createBookmarkSet(String owner, String name) {
         final BookmarkSet bookmarkSet = new BookmarkSet();
         bookmarkSet.setOwner(owner);
         bookmarkSet.setName(name);
         bookmarkSet.setCreated(new Date());
         bookmarkSet.setModified(bookmarkSet.getCreated());
 
         this.storeBookmarkSet(bookmarkSet);
         
         return bookmarkSet;
     }
     
     /**
      * Generates the file name String for an owner and book name.
      * 
      * @param owner The owner of the bookmark set.
      * @param name The name of the bookmark set.
      * @return The file name for the owner and name.
      */
     protected String getStoreFileName(String owner, String name) {
         final StringBuilder fileNameBuff = new StringBuilder();
         
         fileNameBuff.append(owner != null ? "_" + owner : "null");
         fileNameBuff.append("_");
         fileNameBuff.append(name != null ? "_" + name : "null");
         fileNameBuff.append(".bms.xml");
         
         return fileNameBuff.toString();
     }
 
     /**
      * Generates the {@link File} object to use for storing, retrieving
      * and deleting the bookmark set.
      * 
      * @param owner The owner of the bookmark set.
      * @param name The name of the bookmark set.
      * @return The File for the owner and name.
      */
     protected File getStoreFile(String owner, String name) {
         final String fileStoreName = this.getStoreFileName(owner, name);
         final File basePath = this.getStoreDirectory();
         final File storeFile = new File(basePath, fileStoreName);
         return storeFile;
     }
     
     /**
      * @return The directory to store AddressBooks in.
      */
     protected File getStoreDirectory() {
         return (this.baseStorePath == null ? new File(".") : new File(this.baseStorePath));
     }
 }
