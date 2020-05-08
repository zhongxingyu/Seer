 package com.test;
 
 import org.junit.Test;
 
 import java.io.IOException;
 import java.nio.file.FileAlreadyExistsException;
 import java.nio.file.Path;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Created with IntelliJ IDEA.
  * User: uta
  */
 
 public class FATFileSystemTest  extends FATBaseTest {
/*
     //
     // Test of FS creation.
     //
     static public void testFileCreate(Path path, int clusterSize, int clusterCount,
                                       int allocatorType) throws IOException {
         startUp(path);
         try (FATFileSystem ffs  = FATFileSystem.create(path, clusterSize, clusterCount, allocatorType)) {
 
         }
         tearDown(path);
     }
     @Test
     public void testFileCreate() throws IOException {
         final int[] clusterSizes = new int[] {
             FATFile.RECORD_SIZE,
             4096
         };
         int clusterCount = 2;
         for (int allocatorType : allocatorTypes) {
             for (int clusterSize : clusterSizes) {
                 logStart(getPath(), clusterSize, clusterCount, allocatorType);
                 testFileCreate(getPath(),
                         clusterSize, clusterCount, allocatorType);
                 logOk();
             }
         }
     }
 
     //
     // Test of forward space reservation in folder store.
     //
     static public void testForwardFolderReservation(Path path, int clusterSize, int clusterCount,
                                                     int allocatorType) throws IOException {
         startUp(path);
 
         try (FATFileSystem ffs  = FATFileSystem.create(path, clusterSize, clusterCount, allocatorType)) {
             FATFolder root1 = ffs.getRoot();
             //here: 2 free, 1 <root:empty> record is out of FAT
 
             try {
                 root1.createSubfolder("Test1");
                 //here: 1 free, 1 <root:full>, 1 <Test1:empty>
                 root1.createSubfolder("Test2");
                 //root is full, 2 free clusters need
                 throw new Error("Impossible allocation.");
             } catch (IOException ex) {
                 root1.pack();
                 //check that we does not loose the space.
                 if (ffs.getFreeSize() != FATFile.RECORD_SIZE)
                     throw new Error("Lost cluster on folder allocation.");
                 root1.findFile("Test1").getFolder().createSubfolder("Test2");
                 //here: 0 free, 1 <root:full>, 1 <Test1:full>, 1 <Test2:empty>
                 if (ffs.getFreeSize() != 0)
                     throw new Error("Lost cluster on folder allocation 2.");
             }
         }
         tearDown(path);
     }
     @Test
     public void testForwardFolderReservation() throws IOException {
         for (int allocatorType : allocatorTypes) {
             int clusterSize = FATFile.RECORD_SIZE; //fixed!
             int clusterCount = 3; //fixed!
             logStart(getPath(), clusterSize, clusterCount, allocatorType);
             testForwardFolderReservation(getPath(),
                     clusterSize, clusterCount, allocatorType);
             logOk();
         }
     }
 
     //
     // Test of FS open.
     //
     static public void testFileOpen(Path path, int clusterSize, int clusterCount,
                                       int allocatorType) throws IOException {
         startUp(path);
 
         final FATFileSystem ffs1  = FATFileSystem.create(path, clusterSize, clusterCount, allocatorType);
         FATFolder root1 = ffs1.getRoot();
         root1.createSubfolder("Test1");
         root1.createSubfolder("Test2");
         root1.createSubfolder("Test3");
 
         try {
             // test check name first, available space size
             root1.createSubfolder("Test2");
             throw new Error("Can create duplicates.");
         } catch (FileAlreadyExistsException ex) {
             //OK
         }
 
         ffs1.close();
 
         final FATFileSystem ffs2  = FATFileSystem.open(path);
         FATFolder root2 = ffs2.getRoot();
         if (root2.childFiles.isEmpty())
             throw new Error("Bad root.");
         ffs2.close();
 
         tearDown(path);
     }
     @Test
     public void testFileOpen() throws IOException {
         final int[] clusterSizes = new int[] {
                 FATFile.RECORD_SIZE,
                 4096
         };
         // = 1dir-empty + 1rec-in-root * 3folders count
         int clusterCount = 6; //fixed in minimal value
         for (int allocatorType : allocatorTypes) {
             for (int clusterSize : clusterSizes) {
                 logStart(getPath(), clusterSize, clusterCount, allocatorType);
                 testFileOpen(getPath(),
                         clusterSize, clusterCount, allocatorType);
                 logOk();
             }
         }
     }
 
     //
     //  Test of FS tree test.
     //
     static public void testBaseTree(Path path, int clusterSize, int clusterCount,
                                     int allocatorType) throws IOException {
         startUp(path);
 
         final FATFileSystem ffs1  = FATFileSystem.create(path, clusterSize, clusterCount, allocatorType);
         FATFolder root1 = ffs1.getRoot();
         root1.createSubfolder("Test1")
              .createSubfolder("Test1_2")
              .createSubfolder("Test1_3");
 
         root1.deleteChildren();
         if (root1.findFile("Test1") != null)
             throw new Error("Bad deleteChildren call.");
 
         root1.createSubfolder("Test2");
         root1.createSubfolder("Test3");
         root1.pack();
         
         ffs1.close();
 
         final FATFileSystem ffs2  = FATFileSystem.open(path);
         FATFolder root2 = ffs2.getRoot();
         if (root2.childFiles.size() != 2)
             throw new Error("Bad root.");
         ffs2.close();
 
         tearDown(path);
     }
     @Test
     public void testBaseTree() throws IOException {
         for (int allocatorType : allocatorTypes) {
             int clusterSize = FATFile.RECORD_SIZE*3/2; //fixed!
             int clusterCount = 400; //fixed!
             logStart(getPath(), clusterSize, clusterCount, allocatorType);
             testBaseTree(getPath(), clusterSize, clusterCount, allocatorType);
             logOk();
         }
     }
 */
     /**
      * Test of FS shutdown test.
      */
     static public void testShutdown(Path path, int clusterSize, int clusterCount,
                                     int allocatorType) throws IOException {
         startUp(path);
 
         final FATFileSystem ffs1  = FATFileSystem.create(path, clusterSize, clusterCount, allocatorType);
         final FATFolder root1 = ffs1.getRoot();
 
         ExecutorService executor = Executors.newFixedThreadPool(200);
         for (int i = 0; i < 200; i++) {
             Runnable worker = new Runnable() {
                 @Override public void run() {
                     try {
                         root1.createSubfolder("Test1")
                              .createSubfolder("Test1_2")
                              .createSubfolder("Test1_3");
 
                         root1.deleteChildren();
                         if (root1.findFile("Test1") != null)
                             logLN("Concurrent add call.");
 
                         root1.createSubfolder("Test2");
                         root1.createSubfolder("Test3");
                        //root1.pack();
                     } catch (IOException ex) {
                        logLN(ex.toString());
                     }
                 }
             };
            worker.run();
             executor.execute(worker);
         }
 
         try {
            Thread.sleep(100);
             ffs1.waitForShutdown();
         } catch (InterruptedException ex) {
             //ok
         }
         ffs1.close();
         
         executor.shutdown();
         while (!executor.isTerminated()) {
             try {
                 Thread.sleep(10);
             } catch (InterruptedException ex) {
                 //ok
             }
         }
         
 
         final FATFileSystem ffs2  = FATFileSystem.open(path);
         FATFolder root2 = ffs2.getRoot();
         ffs2.close();
 
         tearDown(path);
     }
     @Test(timeout=66660000)
     public void testShutdown() throws IOException {
         for (int allocatorType : allocatorTypes) {
             allocatorType =   FATSystem.ALLOCATOR_CLASSIC_HEAP;
             int clusterSize = FATFile.RECORD_SIZE; //fixed!
             int clusterCount = 400; //fixed!
             logStart(getPath(), clusterSize, clusterCount, allocatorType);
             testShutdown(getPath(), clusterSize, clusterCount, allocatorType);
             logOk();
         }
    }
 }
