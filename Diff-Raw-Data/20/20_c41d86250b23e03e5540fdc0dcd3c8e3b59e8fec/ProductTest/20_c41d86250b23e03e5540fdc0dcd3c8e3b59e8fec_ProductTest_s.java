 package model.product;
 
 import model.common.Barcode;
 import model.common.Unit;
 import model.item.ItemVault;
 import model.productcontainer.StorageUnit;
 import model.productcontainer.StorageUnitVault;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class ProductTest {
 
     public static Product product;
     public static StorageUnit su;
     @Before
     public void setup(){
         su = new StorageUnit();
         su.setName("Test");
         su.validate();
         su.save();
 
         product = new Product();
         product.setDescription("MyProduct");
         product.set3MonthSupply(3);
         product.setBarcode(Barcode.newFromId("111"));
         product.setContainerId(su.getId());
         product.setCreationDate(new DateTime());
         product.setShelfLife(2);
         product.setSize(new Unit(3, "oz"));
         product.setStorageUnitId(su.getId());
     }
 
     @After
     public void teardown(){
        StorageUnitVault.clear();
        ProductVault.clear();
     }
 
     @Test
     public void testItemCreation(){
         assertEquals("Id should be -1", -1, product.getId());
         assertEquals("Product is not saved", false, product.isSaved());
         assertEquals("Product is not valid", false, product.isValid());
         assertEquals("Product should be saveable because its not valid",
                 false, product.save().getStatus());
         //Technically this should be false but we havnt wired up Barcodes and other classes yet
         assertEquals("Product should pass validation", true, product.validate().getStatus());
         assertEquals("Product should save", true, product.save().getStatus());
         assertEquals("Id should be 0", 0, product.getId());
         assertEquals("Product is saved", true, product.isSaved());
         assertEquals("Product is Valid", true, product.isValid());
         assertNotSame("Vault returns a copy", product, product.productVault.get(product.getId()));
         assertEquals("Vault copy and local copy have same ids", product.getId(), product.productVault.get(product.getId()).getId());
     }
 
     @Test
     public void testProductModification(){
         Product productCopy = product.productVault.get(product.getId());
         product.validate();
         product.save();
         productCopy.setContainerId(1);
         assertFalse(productCopy.getContainerId()
                 == product.productVault.get(product.getId()).getContainerId());
         assertEquals("Product should not be saveable because its not valid",
                 false, productCopy.save().getStatus());
         assertEquals("Product should pass validation", true, productCopy.validate().getStatus());
         assertEquals("Product should save", true, productCopy.save().getStatus());
         assertEquals("Vault should not have created a new Product", 1, product.productVault.size());
     }
 }
