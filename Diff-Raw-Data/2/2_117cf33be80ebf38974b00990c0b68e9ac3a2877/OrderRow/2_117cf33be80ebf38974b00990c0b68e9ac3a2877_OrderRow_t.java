 package is.projekt;
 
 public class OrderRow {
 
     private double price;
     private int quantity;
     private Product product;
 
     public OrderRow(Product product, float price, int quantity) {
         this.product = product;
         this.price = price;
         this.quantity = quantity;
     }
 
     public double getPrice() {
         return price;
     }
 
     public void setPrice(double price) {
         this.price = price;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     public Product getProduct() {
         return product;
     }
 
     public void setProduct(Product product) {
         this.product = product;
     }
 }
