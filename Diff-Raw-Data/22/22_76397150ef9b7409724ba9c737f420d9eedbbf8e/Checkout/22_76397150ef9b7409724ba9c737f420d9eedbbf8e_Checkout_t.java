 import java.util.HashMap;
 import java.util.Map;
 
 public class Checkout {
 
   private DiscountCalculator discountCalculator;
   private ProductDao productDao;
 
   private Map<Product, Integer> scannedItems = new HashMap<Product, Integer>();
 
   public Checkout() {
   }
 
   public void scan(String barcode) {
     Product product = productDao.load(barcode);
    if (product == null) {
      throw new RuntimeException(String.format("Product for barcode '%s' not found.", barcode));
    }

     int itemCount = scannedItems.get(product) != null ? scannedItems.get(product) : 0;
     scannedItems.put(product, itemCount + 1);
   }
 
   public int total() {
     return calculatePrice(scannedItems) - discountCalculator.calculate(scannedItems);
   }
 
   private int calculatePrice(Map<Product, Integer> items) {
     int totalPrice = 0;
     for (Map.Entry<Product, Integer> itemCount : items.entrySet()) {
       Product product = itemCount.getKey();
       int count = itemCount.getValue();
 
       totalPrice += (count * product.getPrice());
     }
     return totalPrice;
   }
 
   public void setDiscountCalculator(DiscountCalculator discountCalculator) {
     this.discountCalculator = discountCalculator;
   }
 
   public void setProductDao(ProductDao productDao) {
     this.productDao = productDao;
   }
 
 }
