 package com.thimbleware.jmemcached;
 
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 
 import com.thimbleware.jmemcached.storage.mmap.MemoryMappedBlockStore;
 
 /**
  */
 public class MemoryMappedBlockStoreTest {
     private MemoryMappedBlockStore bs;
 
     @Before
     public void setup() throws IOException {
         bs = new MemoryMappedBlockStore(400000, "test.dat", 8);
     }
 
     @After
     public void teardown() throws IOException {
         bs.close();
     }
 
     @Test
     public void writeBlocks() {
         for (int i = 0; i < 1800; i++){
             byte[] sentData = new byte[3];
             sentData[0] = 'h';
             sentData[1] = 'i';
             sentData[2] = '\n';
 
             long before = bs.getFreeBytes();
             MemoryMappedBlockStore.Region region = bs.alloc(3, sentData);
 
             long after = bs.getFreeBytes();
             assertEquals("after allocating region, free space available", after, before - region.physicalSize);
 
             assertNotNull("region returned", region);
             assertTrue("size is less or equal to region size", region.size <= region.physicalSize);
             assertTrue("region is valid", region.valid);
            assertEquals("rounded up to nearest region boundary", region.physicalSize, 8L);
 
             byte[] receivedData = bs.get(region);
             assertEquals("region data matches",  new String(sentData), new String(receivedData));
             
             if (i % 5 == 0) {
                 before = bs.getFreeBytes();
                 bs.free(region);
                 after = bs.getFreeBytes();
                 assertEquals("after freeing region, free space available", before + region.physicalSize, after);
                 assertFalse("after freeing, region is not valid", region.valid);
             }
 
         }
         
     }
 }
