 package com.bdt.kiradb;
 
 import com.bdt.kiradb.mykdbapp.Person;
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 public class S3BackingStoreTest {
     private Core db;
 
     @Before
     public void setup() throws KiraCorruptIndexException, IOException {
         String key = System.getProperty("aws.key", null);
         String secret = System.getProperty("aws.secret", null);
         String bucket = System.getProperty("aws.bucket", null);
 
         if (key == null || secret == null || bucket == null) {
             System.out.printf("Skipping S3 backing store test.  One or more of AWS System properties aws.[key|secret|bucket] not set.");
         } else {
             db = new S3KiraDB(Utils.makeTemporaryDirectory().getAbsolutePath(), key, secret, bucket);
             db.createIndex();
             System.out.printf("S3 test:  key=%s, secret=%s, bucket=%s\n", key, secret, bucket);
         }
     }
 
     @After
     public void teardown() throws IOException {
         if (db != null) {
             db.deleteIndex();
         }
     }
 
     @Test
    @Ignore
     public void testS3BackingStore() throws IOException, InterruptedException, KiraException, ClassNotFoundException {
         if (db == null) {
             return;
         }
 
         Person p = new Person();
         p.setStoreMode(RecordDescriptor.STORE_MODE_BACKING);
         System.out.println("Testing S3 backing store...");
 
         p.setAccount("3219");
         p.setName("Fred Jones");
         p.setCreatedAt(new Date());
         System.out.println("Writing person...");
         db.storeObject(p);
         System.out.println("Reading person...");
         Person np = (Person) db.retrieveObjectByPrimaryKey(p, p.getAccount());
         System.out.println("Read object: " + np.getName());
         assertNotNull("The result should not be null", np);
         assertEquals("The person's name when read is not the same as when written", p.getName(), np.getName());
 
         File directory = new File(p.getRecordName());
         FileUtils.deleteDirectory(directory);
     }
 
 }
