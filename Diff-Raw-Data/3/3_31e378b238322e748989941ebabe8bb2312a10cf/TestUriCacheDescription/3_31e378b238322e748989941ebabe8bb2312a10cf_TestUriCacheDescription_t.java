 package org.hackystat.sensorbase.uricache;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Exercises CacheDescription functionality.
  * 
  * @author Pavel Senin.
  * 
  */
 public class TestUriCacheDescription {
 
   private static final String fileSeparator = System.getProperty("file.separator");
 
   /** Used for temporarily caches home */
   private static final String tmpFolderName = String.valueOf(System.currentTimeMillis());
   /** The general storage place. */
   private static final String dcStoragePath = System.getProperties().getProperty("user.dir")
       + fileSeparator + "build" + fileSeparator + "uricache-tests" + fileSeparator + tmpFolderName;
 
   private static final String cacheDefaultName = "testCache";
 
   private UriCacheDescription cacheDescription1;
   private UriCacheDescription cacheDescription2;
 
   /** User e-mail */
   private static final String userEmail = "javadude@javatesthost.org";
   /** User host key. */
   private static final String sensorBaseHost = "http://sensorbase143.javatesthost.org:20910";
 
   private static final String desc = ".desc";
 
   /**
    * Sets up test with the temporarily test description file.
    * 
    * @throws Exception if unable to proceed.
    */
   @Before
   public void setUp() throws Exception {
 
     // making sure we got test folder in here
     File f = new File(dcStoragePath);
     if (!f.exists()) {
       f.mkdirs();
     }
 
     // saving first description file
     this.cacheDescription1 = new UriCacheDescription(cacheDefaultName, sensorBaseHost, userEmail);
     this.cacheDescription1.save(dcStoragePath);
 
    Thread.yield();
    Thread.sleep(100);

     // saving second description file
     this.cacheDescription2 = new UriCacheDescription(cacheDefaultName, sensorBaseHost, userEmail);
     this.cacheDescription2.save(dcStoragePath);
 
   }
 
   /**
    * Tests CacheDescription functionality.
    */
   @Test
   public void testCacheDescription() {
     try {
 
       // getting freshly baked description from the file
       String descFileName = dcStoragePath + fileSeparator + this.cacheDescription1.getName() + desc;
       UriCacheDescription desc = new UriCacheDescription(new File(descFileName));
 
       assertTrue("Should load properties from the file.", sensorBaseHost.equalsIgnoreCase(desc
           .getsensorBaseHost()));
       assertTrue("Should load properties from the file..", userEmail.equalsIgnoreCase(desc
           .getUserEmail()));
       assertEquals("Should load properties from the file...", this.cacheDescription1
           .getCreationTime(), desc.getCreationTime());
       assertTrue("Should load properties from the file....", this.cacheDescription1.getName()
           .equalsIgnoreCase(desc.getName()));
     }
     catch (IOException e) {
       fail("Should be able to load cache properties!\n" + e.getMessage());
 
     }
   }
 
   /**
    * Tests CacheDescription comparator.
    */
   @Test
   public void testCacheDescriptionComparator() {
     try {
 
       UriCacheDescriptionTimeComparator comparator = new UriCacheDescriptionTimeComparator();
 
       UriCacheDescription desc1 = new UriCacheDescription(new File(dcStoragePath + fileSeparator
           + this.cacheDescription1.getName() + desc));
 
       UriCacheDescription desc2 = new UriCacheDescription(new File(dcStoragePath + fileSeparator
           + this.cacheDescription2.getName() + desc));
 
       assertSame("Comparator should say equal", 0, comparator.compare(desc1, desc1));
 
       assertTrue("Comparator should say less", 0 > comparator.compare(desc1, desc2));
 
       assertTrue("Comparator should say greater", 0 < comparator.compare(desc2, desc1));
 
     }
     catch (IOException e) {
       fail("Should be able to load cache properties!\n" + e.getMessage());
 
     }
   }
 
   /**
    * Tears down test environment by deleting temporarily test description file.
    * 
    * @throws Exception if error encountered.
    */
   @After
   public void tearDown() throws Exception {
 
     String descFileName = dcStoragePath + fileSeparator + this.cacheDescription1.getName() + desc;
     File file2Delete = new File(descFileName);
     file2Delete.delete();
 
     descFileName = dcStoragePath + fileSeparator + this.cacheDescription2.getName() + desc;
     file2Delete = new File(descFileName);
     file2Delete.delete();
 
     File f = new File(dcStoragePath);
     if (f.exists()) {
       f.delete();
     }
   }
 
 }
