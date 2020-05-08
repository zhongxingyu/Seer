 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol.fileexchange;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.bitrepository.common.FileStore;
 import org.bitrepository.protocol.TestMessageFactory;
 
 /** 
  * Models the storing and retrival of files as needed in test modeling pillar behaviour.
  */
 public class TestFileStore implements FileStore {    
     private static final String ROOT_STORE = "target/test-classes/filestorage";
     private final String storageDir;
     /** The file store already contains this file when it created. The fileID corresponds to the file ID found in the 
      * {@LINK TestMessageFactory}
      */
     public static final String DEFAULT_FILE_ID = TestMessageFactory.FILE_ID_DEFAULT;
     private final String storeName;
     
     /** The default test file.*/
     public static final File DEFAULT_TEST_FILE = new File("src/test/resources/test-files/", DEFAULT_FILE_ID);
 
     /**
      * Constructor, where the FileStore is initialized with some files.
      * @param storeName The id for the FileStore.
      * @param initialFiles The files to copy into the FileStore during initialization.
      */
     public TestFileStore(String storeName, File ... initialFiles) {
         this.storeName = storeName;
         storageDir = ROOT_STORE + "/" + storeName;
 
         for(File initFile : initialFiles) {
             try {
                 FileUtils.copyFileToDirectory(initFile, new File(storageDir));
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     @Override
     public FileInputStream getFileAsInputstream(String fileID) {
         try {
             return new FileInputStream(new File(storageDir, fileID));
         } catch (Exception e) {
             throw new RuntimeException(e);
         } 
     }
 
     @Override
    public File downloadFileForValidation(String fileID, InputStream inputStream) {
         File theFile = new File(fileID);
 
         // Check if a file exists.
         if (theFile.exists()) {
             throw new RuntimeException("Unable to store the indicated file, it already exists");
         }
 
         // Create directory for the file, if requested.
         if (theFile.getParentFile() != null) {
             theFile.getParentFile().mkdirs();
         }
 
         // Save InputStream to the file.
         try {
            BufferedOutputStream bufferedOutputstream = null;
            try {
             bufferedOutputstream = new BufferedOutputStream(new FileOutputStream(theFile));
             byte[] buffer = new byte[32 * 1024];
             int bytesRead = 0;
             while ((bytesRead = inputStream.read(buffer)) != -1) {
                 bufferedOutputstream.write(buffer, 0, bytesRead);
             }
         }
         finally {
             bufferedOutputstream.close();
         }
        } catch (IOException e) {
            throw new RuntimeException("Could not download file for validation.", e);
        }
         
         return theFile;
     }
     
     @Override
     public void moveToArchive(String fileID) {
         // This does nothing.
         return;
     }
 
     @Override
     public void replaceFile(String fileID, InputStream inputStream)  throws IOException  {
         deleteFile(fileID);
         storeFile(fileID, inputStream);
     }
 
     @Override
     public void deleteFile(String fileID) {
         getFile(fileID).delete();
     }
 
     @Override
     public File getFile(String fileID) {
         return new File(storageDir, fileID);
     }
 
     @Override
     public Collection<String> getAllFileIds() {
         String[] ids = new File(storageDir).list();
         List<String> res = new ArrayList<String>();
         for(String id : ids) {
             res.add(id);
         }
         return res;
     }
 
     @Override
     public boolean hasFile(String fileID) {
         return (getFile(fileID)).isFile();
     }
 
     @Override
     public String toString() {
         return "TestFileStore [storageDir=" + storageDir + ", storeName=" + storeName + "]";
     }
 
     public void storeFile(String fileID, InputStream in) throws IOException {
         downloadFileForValidation(fileID, in);
         moveToArchive(fileID);
     }
 }
