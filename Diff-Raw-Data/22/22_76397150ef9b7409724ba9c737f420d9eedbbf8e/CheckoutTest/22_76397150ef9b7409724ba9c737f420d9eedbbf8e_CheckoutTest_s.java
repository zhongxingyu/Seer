 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class CheckoutTest {
 
   private Checkout checkout;
   private ProductDaoImpl productDao;
   private DiscountCalculator discountCalculator;
 
   @Before
   public void setUp() {
     List<Product> products = new ArrayList<Product>();
     products.add(new Product("A", 50));
     products.add(new Product("B", 30));
     products.add(new Product("C", 20));
     products.add(new Product("D", 15));
     productDao = new ProductDaoImpl(products);
 
     Map<String, Discount> discounts = new HashMap<String, Discount>();
     discounts.put("A", new Discount(3, 20));
     discounts.put("B", new Discount(2, 15));
     discountCalculator = new DiscountCalculator(discounts);
 
     checkout = new Checkout();
     checkout.setDiscountCalculator(discountCalculator);
     checkout.setProductDao(productDao);
   }
 
   @Test
   public void totalShouldReturnZeroWhenThereAreNoItemsScanned() {
     assertEquals(0, checkout.total());
   }
 
   @Test
   public void totalShouldReturn50WhenScannedA() {
     checkout.scan("A");
     assertEquals(50, checkout.total());
   }
 
   @Test
   public void totalShouldReturn30WhenScanningB() {
     checkout.scan("B");
     assertEquals(30, checkout.total());
   }
 
   @Test
   public void totalShouldReturn80WhenScanningAB() {
     scanMultipleItems("AB");
     assertEquals(80, checkout.total());
   }
 
   @Test
   public void totalShouldReturn115WhenScanningABCD() {
     scanMultipleItems("ABCD");
     assertEquals(115, checkout.total());
   }
 
   @Test
   public void totalShouldReturn100WhenScanningAA() {
     scanMultipleItems("AA");
     assertEquals(100, checkout.total());
   }
 
   @Test
   public void totalShouldReturn130WhenScanningAAA() {
     scanMultipleItems("AAA");
     assertEquals(130, checkout.total());
   }
 
   @Test
   public void totalShouldReturn180WhenScanningAAAA() {
     scanMultipleItems("AAAA");
     assertEquals(180, checkout.total());
   }
 
   @Test
   public void totalShouldReturn230WhenScanningAAAAA() {
     scanMultipleItems("AAAAA");
     assertEquals(230, checkout.total());
   }
 
   @Test
   public void totalShouldReturn260WhenScanningAAAAAA() {
     scanMultipleItems("AAAAAA");
     assertEquals(260, checkout.total());
   }
 
   @Test
   public void totalShouldReturn160WhenScanningAAAB() {
     scanMultipleItems("AAAB");
     assertEquals(160, checkout.total());
   }
 
   @Test
   public void totalShouldReturn45WhenScanningBB() {
     scanMultipleItems("BB");
     assertEquals(45, checkout.total());
   }
 
   @Test
   public void totalShouldReturn175WhenScanningAAABB() {
     scanMultipleItems("AAABB");
     assertEquals(175, checkout.total());
   }
 
   @Test
   public void totalShouldReturn190WhenScanningAAABBD() {
     scanMultipleItems("AAABBD");
     assertEquals(190, checkout.total());
   }
 
   @Test
   public void totalShouldReturn190WhenScanningDABABA() {
     scanMultipleItems("DABABA");
     assertEquals(190, checkout.total());
   }
 
   @Test
   public void scanningIndividualItemShouldReturnCorrectTotal() {
     assertEquals(0, checkout.total());
     checkout.scan("A");
     assertEquals(50, checkout.total());
     checkout.scan("B");
     assertEquals(80, checkout.total());
     checkout.scan("A");
     assertEquals(130, checkout.total());
     checkout.scan("A");
     assertEquals(160, checkout.total());
     checkout.scan("B");
     assertEquals(175, checkout.total());
   }
 
   private void scanMultipleItems(String products) {
     String[] items = products.split("");
 
     for (String item : items) {
       if (item.equals("")) {
         continue;
       }
       checkout.scan(item);
     }
   }
 }
