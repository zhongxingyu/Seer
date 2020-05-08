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
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 
 import fm.last.commons.test.file.ClassDataFolder;
 import fm.last.commons.test.file.TemporaryFolder;
 
 /**
  * Unit test case for the LogFileManager.
  */
 public class LogFileManagerTest {
 
   @Rule
   public ClassDataFolder dataFolder = new ClassDataFolder();
 
   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();
 
   private LogFileManager logFileManager;
 
   @Before
   public void setup() throws IOException {
     logFileManager = new LogFileManagerImpl(dataFolder.getFolder().getAbsolutePath());
   }
 
   @Test
   public void testFindAll() {
     List<String> logFiles = logFileManager.findAllLogFiles();
    assertEquals(4, logFiles.size());
     assertTrue(logFiles.contains("808.log"));
     assertTrue(logFiles.contains("808.log.1"));
     assertTrue(logFiles.contains("808.log.2"));
     assertTrue(logFiles.contains("808.log.gz"));
   }
 
   @Test
   public void testFindAll_NonExistentFolder() {
     logFileManager = new LogFileManagerImpl("non-existent-folder");
     List<String> logFiles = logFileManager.findAllLogFiles();
     assertEquals(0, logFiles.size());
   }
 
   @Test
   public void testTail() throws IOException {
    String tail = logFileManager.tail("808.log", 142);
     assertEquals(
         "2008-01-14 18:25:54,757 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.sh",
         tail);
   }
 
   @Test
   public void testDeleteBefore() throws IOException, InterruptedException {
     logFileManager = new LogFileManagerImpl(tempFolder.getRoot().getAbsolutePath());
     File logFile1 = tempFolder.newFile("1.log");
     FileUtils.writeStringToFile(logFile1, "bla");
     File otherFile1 = tempFolder.newFile("1.bla");
     FileUtils.writeStringToFile(otherFile1, "bla");
     Thread.sleep(4000);
     DateTime cutoff = new DateTime();
     Thread.sleep(1000);
     File logFile2 = tempFolder.newFile("2.log");
     FileUtils.writeStringToFile(logFile2, "bla");
     logFileManager.deleteBefore(cutoff);
 
     // make sure only older file (logFile1) was deleted
     List<String> logFiles = logFileManager.findAllLogFiles();
     assertEquals(1, logFiles.size());
     assertEquals(logFile2.getName(), logFiles.get(0));
 
     // make sure non-log file was not deleted
     File[] files = tempFolder.getRoot().listFiles();
     assertEquals(2, files.length);
     for (File file : files) {
       String fileName = file.getName();
       assertTrue(fileName.equals(logFile2.getName()) || fileName.equals(otherFile1.getName()));
     }
   }
 }
