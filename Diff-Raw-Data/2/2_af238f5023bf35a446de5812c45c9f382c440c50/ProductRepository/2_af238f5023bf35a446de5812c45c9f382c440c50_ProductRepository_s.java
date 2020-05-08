 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Repositories.ProductRepository;
 
 import Store.Product;
 import Store.ProductSize;
 import java.awt.Color;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.Set;
 
 /**
  *
  * @author volen
  */
 public class ProductRepository implements IProductRepository {
 
     private final HashMap<String, ProductInfo> mProducts;
 
    ProductRepository() {
         mProducts = new HashMap<>();
         InitializeProducts();
     }
 
     @Override
     public Set<Product> getProducts() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public ProductInfo getProductInfo(String sku) {
         if (mProducts.containsKey(sku)) {
             return mProducts.get(sku);
         }
         return null;
     }
 
     @Override
     public Boolean reserveProduct(String sku, int count) {
         if (!mProducts.containsKey(sku)) {
             return false;
         }
         ProductInfo info = mProducts.get(sku);
         if (info.getInStock() < count) {
             return false;
         }
         info.setReserved(count);
         return true;
     }
 
     @Override
     public void releaseProduct(String sku, int count) {
         if (!mProducts.containsKey(sku)) {
             return;
         }
         ProductInfo info = mProducts.get(sku);
         int reserved = info.getReserved();
         int toReserve = reserved > count ? reserved - count : 0;
         info.setReserved(toReserve);
     }
 
     @Override
     public void updateProductInfo(ProductInfo info) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addNewProduct(ProductInfo info) {
         ProductInfo info2 = new ProductInfo();
         info2.setInStock(info.getInStock());
         info2.setProduct(info.getProduct());
         info2.setPercentDiscount(info.getPercentDiscount());
         info2.setPurchasePrice(info.getPurchasePrice());
         info2.setReserved(info.getReserved());
 
         mProducts.put(info2.getProduct().getSKU(), info2);
     }
 
     @Override
     public void removeProduct(ProductInfo info) {
         mProducts.remove(info.getProduct().getSKU());
     }
 
     private void InitializeProducts() {
         Product p1 = new Product("3590001");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.yellow);
         p1.setSize(ProductSize.Small);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(49.99d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590002");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.blue);
         p1.setSize(ProductSize.Small);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(49.99d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590003");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.green);
         p1.setSize(ProductSize.Small);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(43.89d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590004");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.yellow);
         p1.setSize(ProductSize.Large);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(39.99d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590005");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.yellow);
         p1.setSize(ProductSize.ExtraLarge);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(45.65));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590006");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.green);
         p1.setSize(ProductSize.Medium);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(42.11d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590007");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.green);
         p1.setSize(ProductSize.Large);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(47.23d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
 
         p1 = new Product("3590008");
         p1.setCategory("T-Shirts");
         p1.setColor(Color.blue);
         p1.setSize(ProductSize.Medium);
         p1.setDescription("Amazing T-Shirt, perfect replica of the original 1994 World Championship official T-Shirt.");
         p1.setTitle("Official FIFA T-Shirt");
         p1.setSalePrice(new BigDecimal(44.51d));
 
         mProducts.put(p1.getSKU(), getProductInfo(p1));
     }
 
     private ProductInfo getProductInfo(Product p) {
         ProductInfo info = new ProductInfo();
         info.setProduct(p);
         info.setInStock(15);
         info.setPurchasePrice(new BigDecimal(11.23d));
         return info;
     }
 }
