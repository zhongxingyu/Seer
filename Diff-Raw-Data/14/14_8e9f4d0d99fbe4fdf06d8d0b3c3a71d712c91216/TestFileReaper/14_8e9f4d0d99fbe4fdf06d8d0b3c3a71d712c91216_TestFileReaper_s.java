 package com.od.jtimeseries.server.util;
 
 import junit.framework.TestCase;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 21-Nov-2009
  * Time: 17:58:54
  * To change this template use File | Settings | File Templates.
  */
 public class TestFileReaper extends TestCase {
 
     List<File> filesToCleanUp = new LinkedList<File>();
     public File reaperTestDir;
     public final String REAPER_FILE_PREFIX = "reaperTest.";
 
 
     public void setUp() throws IOException {
         createFiles(1,10);
     }
 
     public void tearDown() {
         for ( File f : filesToCleanUp) {
             f.delete();
         }
         filesToCleanUp.clear();
     }
 
     public void testReaperWithBadFilePattern() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTestBAD.*", 5, -1, -1);
         r.reap();
         assertEquals(10, getFileCount());
     }
 
     public void testReaperDeleteByFileCount() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", 5, -1, -1);
         checkReaping(r);
     }
 
     public void testReaperDeleteByFileCount2() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", 5, 1024 * 100, 10000);
         checkReaping(r);
     }
 
     public void testReaperDeleteBySize() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", -1, 1024 * 5, -1);
         checkReaping(r);
     }
 
     public void testReaperDeleteBySize2() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", 15, 1024 * 5, 10000);
         checkReaping(r);
     }
 
     public void testOneLargeFileDoesNotCauseAllOthersToBeDeletedForCumulativeSize() throws IOException {
         assertTrue(getReaperTestFile(5).delete());
         createFiles(5, 5, 1024 * 10);
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", 15, 1024 * 8, 10000);
         r.reap();
         //files are checked in reverse timestamp order, so 10 first
         //when 5 is checked, its size would take us over the cumulative max of 1024 * 8, so 5 is deleted
         //once 5 is deleted, all the rest of the files then fit within the cumulative size, apart from 1, so 1 is
         //deleted. We end up with all files apart from 5 and 1
         checkFilesExist(2, 3, 4, 6, 7, 8, 9, 10);
 
     }
 
     public void testReaperDeleteByTimestampAllFilesNew() throws IOException {
        FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", -1, -1, 100);
         r.reap();
         assertEquals(10, getFileCount());
     }
 
     public void testReaperDeleteByTimestampAllFilesOld() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", -1, -1, 100);
         try {
             Thread.sleep(200);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         r.reap();
         assertEquals(0, getFileCount());
     }
 
     public void testReaperDeleteByTimestampOneFileNew() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", -1, -1, 100);
         oneFileWithUpdatedTimestamp(r);
     }
 
     public void testReaperDeleteByTimestampOneFileNew2() throws IOException {
         FileReaper r = new FileReaper("Test Reaper", reaperTestDir, "reaperTest.*", 20, 1024 * 100, 100);
         oneFileWithUpdatedTimestamp(r);
     }
 
     private void oneFileWithUpdatedTimestamp(FileReaper r) {
         try {
             Thread.sleep(200);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         getReaperTestFile(1).setLastModified(System.currentTimeMillis());
         r.reap();
         assertEquals(1, getFileCount());
         checkFilesExist(1);
     }
 
     private void checkReaping(FileReaper r) throws IOException {
         r.reap();
         assertEquals(5, getFileCount());
         checkFilesExist(6, 7, 8, 9, 10);
         createFiles(11, 12);
         assertEquals(7, getFileCount());
         r.reap();
         assertEquals(5, getFileCount());
         checkFilesExist(8, 9, 10, 11, 12);
 
         createFiles(13, 13);
         File f = getReaperTestFile(8);
         f.setLastModified(System.currentTimeMillis());
         r.reap();
         //since 8 has a later timestamp, 8 is kept, since deletion is ordered by timestamp
         assertEquals(5, getFileCount());
         checkFilesExist(10, 11, 12, 13, 8);
     }
 
     private File getReaperTestFile(int number) {
         File f = new File(reaperTestDir, REAPER_FILE_PREFIX + number);
         return f;
     }
 
 
     private int getFileCount() {
         int count = 0;
         for ( File f : filesToCleanUp) {
             if ( f.isFile() && f.exists() ) {
                 count++;
             }
         }
         return count;
     }
 
     private void checkFilesExist(int... fileNumbers) {
         for ( int number : fileNumbers) {
             assertTrue("File " + number + " did not exist", new File(reaperTestDir, REAPER_FILE_PREFIX + number).exists());
         }
     }
 
     private void createFiles(int startNumber, int endNumber) throws IOException {
         createFiles(startNumber, endNumber, 1024);
     }
 
     private void createFiles(int startNumber, int endNumber, int size) throws IOException {
         File tmpDir = new File(System.getProperty("java.io.tmpdir"));
         reaperTestDir = new File(tmpDir, "TestFileReaper");
 
         if ( ! reaperTestDir.exists()) {
             assertTrue(reaperTestDir.mkdir());
         }
 
         for ( int loop=startNumber; loop <= endNumber; loop++) {
             File f = getReaperTestFile(loop);
             if ( f.exists() ) {
                 assertTrue(f.delete());
             }
             RandomAccessFile ra = new RandomAccessFile(f, "rws");
             ra.write(0);
             ra.seek(size - 1);
             ra.write(1);
             ra.close();
             assertEquals(f.length(), size);
             filesToCleanUp.add(f);
         }
         filesToCleanUp.add(reaperTestDir);
     }
 }
