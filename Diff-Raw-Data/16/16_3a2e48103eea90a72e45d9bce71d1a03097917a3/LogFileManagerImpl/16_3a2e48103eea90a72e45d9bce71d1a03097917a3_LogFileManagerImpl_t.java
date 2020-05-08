 /*
  * Copyright 2010 Last.fm
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package fm.last.citrine.service;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.AgeFileFilter;
 import org.apache.commons.io.filefilter.AndFileFilter;
 import org.apache.commons.io.filefilter.FileFileFilter;
 import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.joda.time.DateTime;
 
 import fm.last.commons.io.LastFileUtils;
 
 /**
  * Log file manager implementation which processes files written to disk under a configured folder location.
  */
 public class LogFileManagerImpl implements LogFileManager {
 
   private File baseLogFolder = new File(System.getProperty("java.io.tmpdir"));
  private IOFileFilter logFileFilter = new AndFileFilter(new WildcardFileFilter("*.log*"), FileFileFilter.FILE);
 
   /**
    * Creates a new instance which will read log files under the base log path (java tmp dir by default).
    */
   public LogFileManagerImpl() {
   }
 
   /**
    * Creates a new instance which will read log files under the passed base log path.
    */
   public LogFileManagerImpl(String baseLogPath) {
     setBaseLogPath(baseLogPath);
   }
 
   /**
    * Creates a new instance which will read log files under the passed base log path.
    */
   public LogFileManagerImpl(File baseLogFolder) {
     this.baseLogFolder = baseLogFolder;
   }
 
   @Override
   public List<String> findAllLogFiles() {
     String[] files = baseLogFolder.list(logFileFilter);
     List<String> logFiles = new ArrayList<String>();
     if (files != null) {
       for (String logFile : files) {
         logFiles.add(logFile);
       }
     }
     Collections.sort(logFiles, Collections.reverseOrder());
     return logFiles;
   }
 
   @Override
   public String tail(String logFileName, long tailBytes) throws IOException {
     File logFile = new File(baseLogFolder, logFileName);
     return LastFileUtils.tail(logFile, tailBytes);
   }
 
   /**
    * @param baseLogPath the baseLogPath to set
    */
   public void setBaseLogPath(String baseLogPath) {
     this.baseLogFolder = new File(baseLogPath);
   }
 
   @Override
   public void deleteBefore(DateTime deleteBefore) throws IOException {
     File[] filesToDelete = baseLogFolder.listFiles((FileFilter) new AndFileFilter(logFileFilter, new AgeFileFilter(
         deleteBefore.toDate())));
     for (File fileToDelete : filesToDelete) {
       FileUtils.forceDelete(fileToDelete);
     }
   }
 
   @Override
   public boolean exists(String logFileName) {
     File logFile = new File(baseLogFolder, logFileName);
     return logFile.exists();
   }
 
 }
