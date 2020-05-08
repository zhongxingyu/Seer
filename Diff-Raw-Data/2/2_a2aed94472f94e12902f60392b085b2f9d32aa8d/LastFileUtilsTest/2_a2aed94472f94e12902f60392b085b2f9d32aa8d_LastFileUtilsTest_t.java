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
 package fm.last.commons.io;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Rule;
 import org.junit.Test;
 
 import fm.last.commons.test.TestAssert;
 import fm.last.commons.test.file.ClassDataFolder;
 import fm.last.commons.test.file.TemporaryFolder;
 
 /**
  * Test case for custom FileUtils class.
  */
 public class LastFileUtilsTest {
 
   @Rule
   public ClassDataFolder dataFolder = new ClassDataFolder();
 
   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();
 
   /**
    * Tests requesting a tail that is bigger than the file, should read in entire file.
    * 
    * @throws IOException
    */
   @Test
   public void testTailBiggerThanFile() throws IOException {
     File file = dataFolder.getFile("3805bytes.log");
     String tail = LastFileUtils.tail(file, 50000);
     assertEquals(FileUtils.readFileToString(file), tail);
   }
 
   /**
    * Tests requesting a tail that is the same size as the file, should read in entire file.
    * 
    * @throws IOException
    */
   @Test
   public void testTailSameAsFile() throws IOException {
     File file = dataFolder.getFile("3805bytes.log");
     String tail = LastFileUtils.tail(file, 3805);
     assertEquals(FileUtils.readFileToString(file), tail);
   }
 
   /**
    * Tests requesting a tail that is smaller than the file, only the last specified bytes should be read.
    * 
    * @throws IOException
    */
   @Test
   public void testTailSmallerThanFile() throws IOException {
     File file = dataFolder.getFile("3805bytes.log");
     String tail = LastFileUtils.tail(file, 143);
     assertEquals(
         "2008-01-14 18:25:54,757 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.sh",
         tail);
   }
 
   /**
    * Tests requesting a tail that is smaller than the file, that line breaks are preserved.
    * 
    * @throws IOException
    */
   @Test
   public void testTailLineBreaks() throws IOException {
     File file = dataFolder.getFile("3805bytes.log");
     String tail = LastFileUtils.tail(file, 287);
     assertEquals(
         "2008-01-14 18:25:54,756 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.bat\n2008-01-14 18:25:54,757 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.sh",
         tail);
   }
 
   @Test(expected = java.io.FileNotFoundException.class)
   public void testGetFile_NonExistent() throws FileNotFoundException {
     LastFileUtils.getFile("non-existent-file", this.getClass());
   }
 
   @Test
   public void testGetFile_OnPath() throws FileNotFoundException {
    File buildXML = LastFileUtils.getFile("build.xml", this.getClass());
     assertTrue(buildXML.exists());
   }
 
   /**
    * Note: for this to work, test/conf must be on your classpath.
    * 
    * @throws FileNotFoundException
    */
   @Test
   public void testGetFile_OnClasspath() throws FileNotFoundException {
     File log4jXML = LastFileUtils.getFile("log4j.xml", this.getClass());
     assertTrue(log4jXML.exists());
   }
 
   @Test
   public void testWriteToFile() throws IOException {
     File inputFile = dataFolder.getFile("3805bytes.log");
     File outputFile = new File(tempFolder.getRoot(), "copy.log");
     InputStream inputStream = new FileInputStream(inputFile);
     LastFileUtils.writeToFile(inputStream, outputFile);
     assertEquals(FileUtils.readFileToString(inputFile), FileUtils.readFileToString(outputFile));
   }
 
   @Test
   public void testMoveFileSafely() throws IOException {
     // first copy file from data folder to temp folder so it can be moved safely
     String filename = "3805bytes.log";
     File originalFile = dataFolder.getFile(filename);
     FileUtils.copyFileToDirectory(originalFile, tempFolder.getRoot());
     File inputFile = new File(tempFolder.getRoot(), filename);
     assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
 
     // now do the actual moving
     File movedFile = new File(tempFolder.getRoot(), "copy.log");
     LastFileUtils.moveFileSafely(inputFile, movedFile);
     assertFalse(inputFile.getAbsolutePath() + " exists", inputFile.exists());
     assertTrue(movedFile.getAbsolutePath() + " doesn't exist", movedFile.exists());
     assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(movedFile));
   }
 
   /**
    * Tests that trying to move a non-existent file that has no parent results in a FileNotFoundException and not a
    * NullPointerException.
    * 
    * @throws IOException
    */
   @Test(expected = FileNotFoundException.class)
   public void testMoveFileSafely_NullParent() throws IOException {
     LastFileUtils.moveFileSafely(new File("noparent"), new File(tempFolder.getRoot(), "output"));
   }
 
   @Test(expected = FileNotFoundException.class)
   public void testMoveFileSafely_NonExistent() throws IOException {
     LastFileUtils.moveFileSafely(new File(tempFolder.getRoot(), "nonexistent"), new File(tempFolder.getRoot(), "output"));
   }
 
   @Test
   public void testMoveFileToDirectorySafely() throws IOException {
     // first copy file from data folder to temp folder so it can be moved safely
     File originalFile = dataFolder.getFile("3805bytes.log");
     FileUtils.copyFileToDirectory(originalFile, tempFolder.getRoot());
     File inputFile = new File(tempFolder.getRoot(), originalFile.getName());
     assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
 
     // now do the actual moving
     File newDir = tempFolder.newFolder("FileUtilsTest");
     assertTrue(newDir.exists());
     // copy file over to newdir, not creating it if it doesn't exist
     LastFileUtils.moveFileToDirectorySafely(inputFile, newDir, false);
     assertFalse(inputFile.getAbsolutePath() + " exists", inputFile.exists());
     File movedFile = new File(newDir, inputFile.getName());
     assertTrue(movedFile.getAbsolutePath() + " doesn't exist", movedFile.exists());
     assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(movedFile));
   }
 
   @Test
   public void testMoveFileToDirectorySafely_CreateDir() throws IOException {
     // first copy file from data folder to temp folder so it can be moved safely
     File originalFile = dataFolder.getFile("3805bytes.log");
     FileUtils.copyFileToDirectory(originalFile, tempFolder.getRoot());
     File inputFile = new File(tempFolder.getRoot(), originalFile.getName());
     assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
 
     // now do the actual moving
     File newDir = new File(tempFolder.getRoot(), "FileUtilsTest");
     assertFalse(newDir.exists()); // dir must not exist
     // copy file over to newdir, creating dir if it doesn't exist
     LastFileUtils.moveFileToDirectorySafely(inputFile, newDir, true);
     assertFalse(inputFile.getAbsolutePath() + " exists", inputFile.exists());
     File movedFile = new File(newDir, inputFile.getName());
     assertTrue(movedFile.getAbsolutePath() + " doesn't exist", movedFile.exists());
 
     assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(movedFile));
   }
 
   @Test(expected = FileNotFoundException.class)
   public void testMoveFileToDirectorySafely_NoCreateDir() throws IOException {
     // first copy file from data folder to temp folder so it can be moved safely
     File originalFile = dataFolder.getFile("3805bytes.log");
     FileUtils.copyFileToDirectory(originalFile, tempFolder.getRoot());
     File inputFile = new File(tempFolder.getRoot(), originalFile.getName());
     assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
 
     // now do the actual moving
     File newDir = new File(tempFolder.getRoot(), "FileUtilsTest");
     assertFalse(newDir.exists()); // dir must not exist
     // copy file over to newdir, NOT creating dir if it doesn't exist
     LastFileUtils.moveFileToDirectorySafely(inputFile, newDir, false);
   }
 
   @Test(expected = NullPointerException.class)
   public void testMoveFileToDirectorySafely_NullDir() throws IOException {
     // first copy file from data folder to temp folder so it can be moved safely
     File originalFile = dataFolder.getFile("3805bytes.log");
     FileUtils.copyFileToDirectory(originalFile, tempFolder.getRoot());
     File inputFile = new File(tempFolder.getRoot(), originalFile.getName());
     assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
     LastFileUtils.moveFileToDirectorySafely(inputFile, null, false); // null, create dir false
   }
 
   @Test(expected = NullPointerException.class)
   public void testMoveFileToDirectorySafely_NullDir_CreateDir() throws IOException {
     // first copy file from data folder to temp folder so it can be moved safely
     File originalFile = dataFolder.getFile("3805bytes.log");
     FileUtils.copyFileToDirectory(originalFile, tempFolder.getRoot());
     File inputFile = new File(tempFolder.getRoot(), originalFile.getName());
     assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
     LastFileUtils.moveFileToDirectorySafely(inputFile, null, true); // null, create dir true
   }
 
   @Test
   public void testAppend1File() throws IOException {
     File destination = new File(tempFolder.getRoot(), "merged");
     File file1 = dataFolder.getFile("file1.txt");
     LastFileUtils.appendFiles(destination, file1);
     TestAssert.assertFileEquals(file1, destination);
   }
 
   @Test
   public void testAppend2Files() throws IOException {
     File destination = new File(tempFolder.getRoot(), "merged");
     File file1 = dataFolder.getFile("file1.txt");
     File file2 = dataFolder.getFile("file2.txt");
     LastFileUtils.appendFiles(destination, file1, file2);
     TestAssert.assertFileEquals(dataFolder.getFile("file1-2.txt"), destination);
   }
 
   @Test
   public void testAppend3Files() throws IOException {
     File destination = new File(tempFolder.getRoot(), "merged");
     File file1 = dataFolder.getFile("file1.txt");
     File file2 = dataFolder.getFile("file2.txt");
     File file3 = dataFolder.getFile("file3.txt");
     LastFileUtils.appendFiles(destination, file1, file2, file3);
     TestAssert.assertFileEquals(dataFolder.getFile("file1-2-3.txt"), destination);
   }
 
   @Test
   public void testAppend3Files_List() throws IOException {
     File destination = new File(tempFolder.getRoot(), "merged");
     List<File> files = new ArrayList<File>();
     files.add(dataFolder.getFile("file1.txt"));
     files.add(dataFolder.getFile("file2.txt"));
     files.add(dataFolder.getFile("file3.txt"));
     LastFileUtils.appendFiles(destination, files);
     TestAssert.assertFileEquals(dataFolder.getFile("file1-2-3.txt"), destination);
   }
 }
