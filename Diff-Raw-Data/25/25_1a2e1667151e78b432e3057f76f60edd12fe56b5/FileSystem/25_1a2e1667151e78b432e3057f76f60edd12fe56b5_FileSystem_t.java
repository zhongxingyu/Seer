 /**
  * ====================
  *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
  * Copyright 2011-2013 Tirasa. All rights reserved.
  *
  * The contents of this file are subject to the terms of the Common Development
  * and Distribution License("CDDL") (the "License"). You may not use this file
  * except in compliance with the License.
  *
  * You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL
  * See the License for the specific language governing permissions and limitations
  * under the License.
  *
  * When distributing the Covered Code, include this CDDL Header Notice in each file
  * and include the License file at https://oss.oracle.com/licenses/CDDL.
  * If applicable, add the following below this CDDL Header, with the fields
  * enclosed by brackets [] replaced by your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * ====================
  */
package org.connid.bundles.csvdir.database;

 import java.io.File;
 import java.io.FileFilter;
 import org.connid.bundles.csvdir.CSVDirConfiguration;
 import org.identityconnectors.common.logging.Log;
 
 public class FileSystem {
 
     private static final Log log = Log.getLog(FileSystem.class);
 
     private CSVDirConfiguration conf;
 
     private File sourcePath = null;
 
     private FileFilter fileFilter = null;
 
     private long highestTimeStamp;
 
     public FileSystem(final CSVDirConfiguration conf) {
         this.conf = conf;
         this.sourcePath = new File(conf.getSourcePath());
         this.fileFilter = new FileFilter() {
 
             @Override
             public boolean accept(final File file) {
                 return isMatched(file);
             }
         };
     }
 
     public final File[] getAllCsvFiles() {
         final File[] csvFiles = sourcePath.listFiles(fileFilter);
         return returnNewArrayIfCsvFilesIsEmpty(csvFiles);
     }
 
     public final File getLastModifiedCsvFile() {
         final File[] csvFiles = getAllCsvFiles();
 
         long tm = 0L;
 
         File lastModifiedFile = null;
 
         for (File file : csvFiles) {
             if (file.lastModified() > tm) {
                 tm = file.lastModified();
                 lastModifiedFile = file;
             }
         }
 
         return lastModifiedFile;
     }
 
     public final File[] getModifiedCsvFiles(final long timeStamp) {
         final File[] csvFiles = sourcePath.listFiles(new FileFilter() {
 
             @Override
             public boolean accept(final File file) {
                 return isMatched(file) && file.lastModified() > timeStamp;
             }
         });
 
         for (File file : csvFiles) {
             if (file.lastModified() > highestTimeStamp) {
                 highestTimeStamp = file.lastModified();
             }
         }
 
         return returnNewArrayIfCsvFilesIsEmpty(csvFiles);
     }
 
     private File[] returnNewArrayIfCsvFilesIsEmpty(final File[] csvFiles) {
         if (csvFiles != null) {
             return csvFiles;
         } else {
             return new File[]{};
         }
     }
 
     public final long getHighestTimeStamp() {
         return highestTimeStamp;
     }
 
     private boolean isMatched(File file) {
         return !file.isDirectory()
                 && (file.getName().matches(conf.getFileMask())
                 || file.getName().matches(
                 FileToDB.DEFAULT_PREFIX + ".*\\.csv"));
     }
 }
