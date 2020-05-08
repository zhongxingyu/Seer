 package org.uva.training.tax;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 import org.uva.training.entity.Item;
 import org.uva.training.entity.Product;
 import org.uva.training.entity.Type;
 
 public class ImportTest {
 
    @Test
    public void testCompute() {
      float computedPrice = 0.55f;
 
       Product product = new Product(Type.CD, "Muse", true);
       Item item = new Item(product, 1, 10.50f);
 
       Tax importTax = new ImportTax(item);
       assertEquals(computedPrice, importTax.getValue());
    }
 }
