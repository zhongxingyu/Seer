 package me.ilyamirin.anthophila;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicInteger;
 import static junit.framework.Assert.*;
 import lombok.extern.slf4j.Slf4j;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author ilyamirin
  */
 @Slf4j
 public class StorageTest {
 
     private Random r = new Random();
     private Storage storage;
     private File file;
 
     @Before
     public void setUp() throws Exception {
         file = new File("test.bin");
         if (file.exists()) {
             file.delete();
         }
         file.createNewFile();
         RandomAccessFile aFile = new RandomAccessFile("test.bin", "rw");
         storage = new StorageImpl(aFile.getChannel());
     }
 
     @After
     public void tearDown() throws Exception {
     }
 
     @Test
     public void basicOpsTest() {
         ByteBuffer md5Hash = ByteBuffer.allocate(8);
         r.nextBytes(md5Hash.array());
         ByteBuffer chunk = ByteBuffer.allocate(StorageImpl.MAX_CHUNK_LENGTH);
         r.nextBytes(chunk.array());
 
         storage.append(md5Hash, chunk);
 
         assertTrue(storage.contains(md5Hash));
         assertTrue(chunk.equals(storage.read(md5Hash)));
 
         chunk = ByteBuffer.allocate(StorageImpl.MAX_CHUNK_LENGTH / 2);
         r.nextBytes(md5Hash.array());
         r.nextBytes(chunk.array());
 
         storage.append(md5Hash, chunk);
 
         assertTrue(storage.contains(md5Hash));
         assertTrue(chunk.equals(storage.read(md5Hash)));
 
         assertEquals((StorageImpl.MAX_CHUNK_LENGTH * 2) + (2 * 13), file.length());
 
         storage.delete(md5Hash);
 
         assertFalse(storage.contains(md5Hash));
         assertNull(storage.read(md5Hash));
 
         assertEquals((StorageImpl.MAX_CHUNK_LENGTH * 2) + (2 * 13), file.length());
     }
 
     @Test
     public void basicOpsParallelTest() throws InterruptedException {
         int cuncurrentClientsNumber = 10;
         final int cuncurrentRequestsNumber = 1000;
         final CountDownLatch latch = new CountDownLatch(cuncurrentClientsNumber);
        final AtomicInteger assertioErrorsCount = new AtomicInteger(0);
         final AtomicInteger passedRequests = new AtomicInteger(0);
         final AtomicInteger chunksDeletedCounter = new AtomicInteger(0);
 
         for (int i = 0; i < cuncurrentClientsNumber; i++) {
             Runnable runnable = new Runnable() {
                 @Override
                 public void run() {
                     int counter = 0;
                     ByteBuffer md5Hash;
                     ByteBuffer chunk;
                     while (counter < cuncurrentRequestsNumber) {
                         md5Hash = ByteBuffer.allocate(8);
                         r.nextBytes(md5Hash.array());
                         chunk = ByteBuffer.allocate(StorageImpl.MAX_CHUNK_LENGTH);
                         r.nextBytes(chunk.array());
 
                         storage.append(md5Hash, chunk);
 
                         if (!storage.contains(md5Hash)) {
                             log.error("storage does not contains {}", md5Hash);
                            log.info("Assertion Errors Count {}", assertioErrorsCount.incrementAndGet());
                         } else if (!chunk.equals(storage.read(md5Hash))) {
                             log.error("returned value does not equal to original.");
                            log.info("Assertion Errors Count {}", assertioErrorsCount.incrementAndGet());
                         }
 
                         if (r.nextBoolean()) {
                             storage.delete(md5Hash);
                             if (storage.contains(md5Hash)) {
                                 log.error("storage contains deleted key {}", md5Hash);
                                log.info("Assertion Errors Count {}", assertioErrorsCount.incrementAndGet());
                             } else if (storage.read(md5Hash) != null) {
                                 log.error("Storage returned deleted value.");
                                log.info("Assertion Errors Count {}", assertioErrorsCount.incrementAndGet());
                             }
                            chunksDeletedCounter.incrementAndGet();
                         }
 
                         counter++;
 
                         if (passedRequests.incrementAndGet() % cuncurrentRequestsNumber == 0) {
                             log.info("{} requests passed", passedRequests.get());
                         }
                     }//while
 
                     latch.countDown();
                 }
             };
 
             new Thread(runnable).start();
 
         }//for
 
         latch.await();
 
        assertEquals(0, assertioErrorsCount.get());
 
        long totalSpace = (65536 + 13) * (cuncurrentClientsNumber * cuncurrentRequestsNumber
                - chunksDeletedCounter.getAndAdd(cuncurrentClientsNumber));
        assertEquals(totalSpace, file.length());
     }
 }
