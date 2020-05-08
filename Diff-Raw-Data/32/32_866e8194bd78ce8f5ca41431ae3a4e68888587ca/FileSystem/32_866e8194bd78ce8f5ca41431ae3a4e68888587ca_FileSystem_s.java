 /*
  * ====================
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2011 Tirasa. All rights reserved.     
  * 
  * The contents of this file are subject to the terms of the Common Development 
  * and Distribution License("CDDL") (the "License").  You may not use this file 
  * except in compliance with the License.
  * 
  * You can obtain a copy of the License at 
  * http://IdentityConnectors.dev.java.net/legal/license.txt
  * See the License for the specific language governing permissions and limitations 
  * under the License. 
  * 
  * When distributing the Covered Code, include this CDDL Header Notice in each file
  * and include the License file at identityconnectors/legal/license.txt.
  * If applicable, add the following below this CDDL Header, with the fields 
  * enclosed by brackets [] replaced by your own identifying information: 
  * "Portions Copyrighted [year] [name of copyright owner]"
  * ====================
  */
 package org.connid.csvdir;
 
 import java.io.File;
 import java.io.FileFilter;
 import org.identityconnectors.common.logging.Log;
 
 /**
  *
  * @author massi
  */
 public class FileSystem {
 
     private static final Log log = Log.getLog(FileSystem.class);
 
     private CSVDirConfiguration configuration;
 
     private File sourcePath = null;
 
     private FileFilter fileFilter = null;
 
     private long highestTimeStamp;
 
     public FileSystem(final CSVDirConfiguration csvdc) {
         configuration = csvdc;
         sourcePath = new File(configuration.getSourcePath());
         fileFilter = new FileFilter() {
 
             @Override
             public boolean accept(final File file) {
                 return !file.isDirectory() && file.getName().matches(
                         configuration.getFileMask());
             }
         };
     }
 
     public final File[] getAllCsvFiles() {
         final File[] csvFiles = sourcePath.listFiles(fileFilter);
         return returnNewArrayIfCsvFilesIsEmpty(csvFiles);
     }
 
     public final File[] getModifiedCsvFiles(final long timeStamp) {
         final File[] csvFiles = sourcePath.listFiles(new FileFilter() {
 
             @Override
             public boolean accept(final File file) {
                 return !file.isDirectory()
                         && file.getName().matches(configuration.getFileMask())
                         && file.lastModified() > timeStamp;
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
 }
