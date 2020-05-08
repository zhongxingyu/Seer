 /*
  * Copyright 2010-2011 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.serialization.writer;
 
 import com.ning.metrics.serialization.event.Event;
 import com.ning.metrics.serialization.event.StubEvent;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.ObjectOutput;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class TestDiskSpoolEventWriter
 {
     private Runnable commandToRun;
     private long secondsToWait;
     private final ScheduledExecutorService executor = new StubScheduledExecutorService()
     {
         @Override
         public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit)
         {
             commandToRun = command;
             secondsToWait = TimeUnit.SECONDS.convert(delay, unit);
 
             return null;
         }
     };
     private File spoolDir;
     private File tmpDir;
     private File quarantineDir;
     private File lockDir;
     private final EventHandler writerThrowsIOExceptionOnCommit = new StubEventHandler(new MockEventWriter(true, false, false));
     private final EventHandler writerThrowsIOExceptionOnWrite = new StubEventHandler(new MockEventWriter(false, false, true));
     private final EventHandler writerThrowsIOExceptionOnCommitAndRollback = new StubEventHandler(new MockEventWriter(true, true, false));
     private final EventHandler writerThrowsIOExceptionOnAll = new StubEventHandler(new MockEventWriter(true, true, true));
     private final EventHandler writerSucceeds = new StubEventHandler();
     private final Event eventThrowsOnWrite = new StubEvent()
     {
         @Override
         public void writeExternal(final ObjectOutput out) throws IOException
         {
             throw new IOException();
         }
     };
     private String spoolPath;
 
     @BeforeMethod(alwaysRun = true)
     void setup()
     {
         spoolPath = System.getProperty("java.io.tmpdir") + "/diskspooleventwriter-" + System.currentTimeMillis();
         spoolDir = new File(spoolPath);
         tmpDir = new File(spoolDir, "_tmp");
         quarantineDir = new File(spoolDir, "_quarantine");
         lockDir = new File(spoolDir, "_lock");
 
         prepareSpoolDirs();
     }
 
     @Test(groups = "fast")
     public void testWriteIOFailure() throws Exception
     {
         final DiskSpoolEventWriter writer = createWriter(writerSucceeds);
 
         try {
             writer.write(eventThrowsOnWrite);
             Assert.fail("expected IOException");
         }
         catch (IOException e) {
             Assert.assertEquals(e.getClass(), IOException.class);
         }
 
         testSpoolDirs(1, 0, 0);
         writer.rollback();
         testSpoolDirs(0, 0, 1);
     }
 
     @Test(groups = "fast")
     public void testNoBusyWait() throws Exception
     {
         final AtomicReference<List<File>> spooledFileList = new AtomicReference<List<File>>(Collections.<File>emptyList());
 
         @SuppressWarnings({"UnusedDeclaration"}) final
         DiskSpoolEventWriter writer = new DiskSpoolEventWriter(writerSucceeds, spoolPath, true, 30, executor, SyncType.NONE, 1, 1)
         {
             @Override
             protected List<File> getSpooledFileList()
             {
                 return spooledFileList.get();
             }
         };
         //will have empty files in spooled dir
         commandToRun.run();
         Assert.assertEquals(secondsToWait, 30);
 
         spooledFileList.set(Arrays.asList(new File(tmpDir, "fuu")));
         commandToRun.run();
         Assert.assertEquals(secondsToWait, 0);
     }
 
     @Test(groups = "fast")
     public void testPersistentWriterSucceeds() throws Exception
     {
         final DiskSpoolEventWriter writer = createWriter(writerSucceeds);
 
         testSpoolDirs(0, 0, 0);
         writer.write(createEvent());
         writer.write(createEvent());
         testSpoolDirs(1, 0, 0);
         writer.commit();
         testSpoolDirs(0, 1, 0);
         commandToRun.run();
         testSpoolDirs(0, 0, 0);
     }
 
     @Test(groups = "fast")
     public void testPersistentWriteFails() throws Exception
     {
         final DiskSpoolEventWriter writer = createWriter(writerThrowsIOExceptionOnWrite);
 
         testSpoolDirs(0, 0, 0);
         writer.write(createEvent());
         testSpoolDirs(1, 0, 0);
         writer.commit();
         commandToRun.run();
         testSpoolDirs(0, 0, 1);
     }
 
     // misleading name. flush() is actually what fails.
     @Test(groups = "fast")
     public void testPersistentCommitFails() throws Exception
     {
         final DiskSpoolEventWriter writer = createWriter(writerThrowsIOExceptionOnCommit);
 
         prepareSpoolDirs();
         testSpoolDirs(0, 0, 0);
         writer.write(createEvent());
         testSpoolDirs(1, 0, 0);
         writer.commit();
         testSpoolDirs(0, 1, 0);
         commandToRun.run();
         testSpoolDirs(0, 0, 1);
     }
 
     @Test(groups = "fast")
     public void testPersistentRollbackFails() throws Exception
     {
         final DiskSpoolEventWriter writer = createWriter(writerThrowsIOExceptionOnCommitAndRollback);
 
         prepareSpoolDirs();
         testSpoolDirs(0, 0, 0);
         writer.write(createEvent());
         testSpoolDirs(1, 0, 0);
         writer.commit();
         testSpoolDirs(0, 1, 0);
         commandToRun.run();
         testSpoolDirs(0, 0, 1);
     }
 
     // FIXME make one event that fails, one that succeeds. This is a lousy test.
     // the trouble: StubEvent isn't easy to extend to do this.
     @Test(groups = "fast", enabled = false)
     public void testQuarantineByEvent() throws Exception
     {
         // create a writer that fails for events with the name "fail"
         final DiskSpoolEventWriter writer = createWriter(new StubEventHandler(new MockEventWriter(false, false, false)
         {
             private boolean hasFailedOneEvent = false;
 
             @Override
             public void write(final Event event) throws IOException
             {
                 if (!hasFailedOneEvent) {
                     hasFailedOneEvent = true;
                     throw new IOException("IGNORE - Expected exception for tests");
                 }
             }
         }));
 
         testSpoolDirs(0, 0, 0);
         writer.write(createEvent()); // this event will succeed and be deleted from the files
         writer.write(createEvent()); // this event will fail and be quarantined
         testSpoolDirs(1, 0, 0);
         writer.commit();
         testSpoolDirs(0, 1, 0);
         commandToRun.run();
         testSpoolDirs(0, 0, 1);
 
         // this isn't a comprehensive test at all. it doesn't tell us if some events have succeeded!
         Assert.assertEquals(quarantineDir.listFiles().length, 1);
     }
 
     @Test(groups = "fast")
     public void testPersistentMassiveFail() throws Exception
     {
         final DiskSpoolEventWriter writer = createWriter(writerThrowsIOExceptionOnAll);
 
         prepareSpoolDirs();
         testSpoolDirs(0, 0, 0);
         writer.write(createEvent());
         testSpoolDirs(1, 0, 0);
         writer.commit();
         testSpoolDirs(0, 1, 0);
         commandToRun.run();
         testSpoolDirs(0, 0, 1);
     }
 
    @Test(groups = "fast")
    public void testInvalidConfiguration() throws Exception
    {
        try {
            // Shouldn't be able to write to /dev/null/...
            new DiskSpoolEventWriter(writerSucceeds, "/dev/null", true, 1, executor, SyncType.NONE, 1, 1);
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }
    }

     private DiskSpoolEventWriter createWriter(final EventHandler persistentWriter)
     {
         return new DiskSpoolEventWriter(persistentWriter, spoolPath, true, 1, executor, SyncType.NONE, 1, 1);
     }
 
     private void testSpoolDirs(final int tmpCount, final int spoolCount, final int quarantineCount)
     {
         Assert.assertEquals(listBinFiles(tmpDir).length, tmpCount);
         Assert.assertEquals(listBinFiles(spoolDir).length, spoolCount);
         Assert.assertEquals(listBinFiles(quarantineDir).length, quarantineCount);
         Assert.assertEquals(listBinFiles(lockDir).length, 0); // lockDir should never have files lying around in it
     }
 
     private void prepareSpoolDirs()
     {
         cleanDirectory(spoolDir);
         cleanDirectory(tmpDir);
         cleanDirectory(quarantineDir);
         cleanDirectory(lockDir);
     }
 
     private File[] listBinFiles(final File dir)
     {
         return dir.listFiles(new FileFilter()
         {
             @Override
             public boolean accept(final File pathname)
             {
                 return pathname.getName().endsWith(".bin");
             }
         });
     }
 
     private void cleanDirectory(final File quarantineDir)
     {
         if (quarantineDir.exists()) {
             for (final File file : quarantineDir.listFiles()) {
                 if (file.isFile()) {
                     file.delete();
                 }
             }
         }
     }
 
     private Event createEvent()
     {
         return new StubEvent();
     }
 }
