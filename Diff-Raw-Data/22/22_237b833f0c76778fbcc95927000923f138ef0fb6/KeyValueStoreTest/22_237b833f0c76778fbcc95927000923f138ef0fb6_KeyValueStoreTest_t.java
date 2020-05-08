 package ru.gosuslugi.geps.ng.dao;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.List;
 
 /**
  * User: rnasyrov
  * Date: 08.01.13
  * Time: 13:28
  */
 public class KeyValueStoreTest {
 
     @Test
     public void shouldGetSize() throws Exception {
        Assert.assertEquals(3, KeyValueStore.getSize());
     }
 
     @Test
     public void shouldCreateStore() throws Exception {
         Assert.assertNotNull(KeyValueStore.get("message:123"));
         Assert.assertNull(KeyValueStore.get("message:666"));
     }
 
     @Test
     public void shouldGetNextId() throws Exception {
         Assert.assertEquals(1, KeyValueStore.getNextId());
         Assert.assertEquals(2, KeyValueStore.getNextId());
         Assert.assertEquals(3, KeyValueStore.getNextId());
     }
 
     @Test
     public void shouldGetByPrefix() throws Exception {
         List<String> messages = KeyValueStore.getByPrefix("message");
        Assert.assertEquals(3, messages.size());
     }
 }
