 package edu.berkeley.cs.cs162.Test;
 
 import static org.junit.Assert.*;
 
 import edu.berkeley.cs.cs162.Synchronization.Lock;
 import edu.berkeley.cs.cs162.Synchronization.ReaderWriterLock;
 import org.junit.Test;
 
 import java.util.ArrayList;
 
 public class ReaderWriterLockTest {
 
     @Test // Basic test to demonstrate that multiple readers can acquire the lock
     public void testMultipleReaders() throws InterruptedException {
 
         SharedResource<Integer> count = new SharedResource<Integer>(0);
         final Lock mutex = new Lock();
         final ReaderWriterLock lock = new ReaderWriterLock();
         ArrayList<Thread> threads = new ArrayList<Thread>();
 
         class ReaderThread extends Thread {
 
             private SharedResource<Integer> sharedInteger;
 
             public ReaderThread(SharedResource<Integer> i){
                 sharedInteger = i;
             }
 
             public void run() {
                 lock.readLock();
                 mutex.acquire();
                 sharedInteger.setResource(sharedInteger.getResource() + 1);
                 mutex.release();
             }
         }
 
         for(int i = 0; i < 100; i++){
             Thread reader = new ReaderThread(count);
             threads.add(reader);
             reader.start();
         }
 
         for(Thread thread : threads){
             thread.join();
         }
 
         assertEquals(100, count.getResource().intValue());
     }
 
     @Test // Basic test to demonstrate only one writer at a time may acquire the lock
     public void testNoMultipleWriters() throws InterruptedException {
         SharedResource<Integer> count = new SharedResource<Integer>(0);
         final ReaderWriterLock lock = new ReaderWriterLock();
         ArrayList<Thread> threads = new ArrayList<Thread>();
 
         class WriterThread extends Thread {
 
             private SharedResource<Integer> sharedInteger;
 
             public WriterThread(SharedResource<Integer> i){
                 sharedInteger = i;
             }
 
             public void run() {
                 lock.writeLock();
                 sharedInteger.setResource(sharedInteger.getResource() + 1);
             }
         }
 
        for(int i = 0; i < 100; i++){
            Thread reader = new WriterThread(count);
            threads.add(reader);
            reader.start();
         }
 
         for(Thread thread : threads){
            thread.join();
         }
 
         assertEquals(1, count.getResource().intValue());
     }
 
     @Test // Tests that readers cannot acquire a lock if a writer has it
     public void testSingleWriter() throws InterruptedException {
 
     }
 }
