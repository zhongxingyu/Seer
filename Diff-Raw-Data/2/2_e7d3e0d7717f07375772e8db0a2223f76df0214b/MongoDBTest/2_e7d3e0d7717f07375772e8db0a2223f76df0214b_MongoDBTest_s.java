 package com.brotherlogic.memory.core;
 
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.brotherlogic.memory.db.DBFactory;
 
 public class MongoDBTest extends DBTest
 {
    @Test
    public void testGetLatestMemory() throws IOException
    {
       // Add a memory
       UntappdMemory memory = new UntappdMemory();
       memory.setImagePath("/blah");
       memory.setTimestamp(10L);
       memory.setBeerName("IPA");
       DBFactory.buildInterface().storeMemory(memory);
 
       // Check that the latest memory is this one
       Memory mem = DBFactory.buildInterface().retrieveLatestMemory(UntappdMemory.class);
       Assert.assertEquals("Stored object not retrieved", memory, mem);
 
       // Store a different memory
       DiscogsMemory discogs = new DiscogsMemory();
       discogs.setImagePath("/blah2");
       discogs.setTimestamp(5L);
       discogs.setArtist("David Bowie");
       DBFactory.buildInterface().storeMemory(discogs);
 
       // Check that we can retrieve both the latest versions
       Assert.assertEquals("Cannot retrieve Discogs memory", discogs, DBFactory.buildInterface()
             .retrieveLatestMemory(DiscogsMemory.class));
       Assert.assertEquals("Cannot retrieve Untappd memory", memory, DBFactory.buildInterface()
             .retrieveLatestMemory(UntappdMemory.class));
 
       // Add in a newer Untappd
       UntappdMemory untappd2 = new UntappdMemory();
       untappd2.setImagePath("/blah3");
      untappd2.setTimestamp(4L);
       untappd2.setBeerName("IPA");
       DBFactory.buildInterface().storeMemory(untappd2);
 
       // We should now get the newer one
       Assert.assertEquals("Retrieved wrong Untappd memory", untappd2, DBFactory.buildInterface()
             .retrieveLatestMemory(UntappdMemory.class));
    }
 }
