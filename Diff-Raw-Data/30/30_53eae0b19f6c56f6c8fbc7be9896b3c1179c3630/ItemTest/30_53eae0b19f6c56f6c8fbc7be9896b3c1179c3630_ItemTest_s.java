 package model.item;
 
 import model.common.Barcode;
 import model.item.Item;
 import model.item.ItemVault;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 
 public class ItemTest {
 
     public static Item item;
     @Before
     public void setup(){
 StorageManager.getInstance().setFactory(new  SerializationDAOFactory());
 StorageManager.getInstance().hitStart();
StorageManager.getInstance().setFactory(new  SerializationDAOFactory());
StorageManager.getInstance().hitStart();
         item = new Item();
         item.generateTestData();
     }
 
     @After
     public void teardown(){
         ItemVault.getInstance().clear();
     }
 
     @Test
     public void testItemCreation(){
         assertEquals("Id should be -1", -1, item.getId());
         assertEquals("Item is not saved", false, item.isSaved());
         assertEquals("Item is not valid", false, item.isValid());
         assertEquals("Item should pass validation", true, item.validate().getStatus());
         assertEquals("Item should save", true, item.save().getStatus());
         assertEquals("Id should be 0", 0, item.getId());
         assertEquals("Item is saved", true, item.isSaved());
         assertEquals("Item is Valid", true, item.isValid());
         assertNotSame("Vault returns a copy", item, item._itemVault.get(item.getId()));
         assertEquals("Vault copy and local copy have same ids", item.getId(), item._itemVault.get(item.getId()).getId());
     }
 
     @Test
     public void testItemModification(){
     	item.validate();
         item.save();
         Item itemCopy = item._itemVault.get(item.getId());
         itemCopy.setProductId(2319);
         // The next two tests are two ways to say the same thing. I leave the for sake of reference.)
         assertTrue("Local modification doesn't change item in vault", itemCopy.getProductId() != item._itemVault.get(item.getId()).getProductId());
         assertThat(itemCopy.getProductId(), not(equalTo(item._itemVault.get(item.getId()).getProductId())));
         //
         assertEquals("Item should pass validation", true, itemCopy.validate().getStatus());
         assertEquals("Item should save", true, itemCopy.save().getStatus());
         assertEquals("Vault should not have created a new item", 1, item._itemVault.size());
     }
     
     @Test
     public void testValidateEntryDate() throws Exception {
     	/* These should be true. */
 		item.setEntryDate(new DateTime(2012, 1, 1, 1, 1));
 		assertTrue(item.validate().getStatus());
 		
 		item.setEntryDate(new DateTime(2000, 1, 1, 1, 1));
 		assertTrue(item.validate().getStatus());
 		
 		item.setEntryDate(new DateTime());
 		assertTrue(item.validate().getStatus());
 		
 		/* These should be false. */
 		item.setEntryDate(new DateTime(1999, 1, 1, 1, 1));
 		assertFalse(item.validate().getStatus());
 		
 		item.setEntryDate(new DateTime(2084, 1, 1, 1, 1));
 		assertFalse(item.validate().getStatus());
 		
 	}
 }
