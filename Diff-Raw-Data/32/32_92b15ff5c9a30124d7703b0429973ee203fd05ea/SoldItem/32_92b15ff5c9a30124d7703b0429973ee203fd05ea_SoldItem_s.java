 package ee.ut.math.tvt.salessystem.domain.data;
 
 import javax.persistence.*;
 //tegelt pole nt column(...=name vaja, sest by default on see juba korrektne
 
 /**
  * Already bought StockItem. SoldItem duplicates name and price for preserving history. 
  */
 @Entity
 @Table(name="SOLDITEM")
 public class SoldItem implements Cloneable, DisplayableItem {
 	@Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
 	
 	@ManyToOne
     @JoinColumn(name = "STOCKITEM_ID", nullable = false)
     private StockItem stockItem;
 	
     @Column(name = "name")
     private String name;
     
     @Column(name = "quantity")
     private Integer quantity;
     
     @Column(name = "itemprice")
     private double price;
     
     public SoldItem(StockItem stockItem, int quantity) {
     	this.id = stockItem.getId();
         this.stockItem = stockItem;
         this.name = stockItem.getName();
        this.price = stockItem.getPrice()*quantity;
         this.quantity = quantity;
         
     }
    public SoldItem() {
     	        
     }
     
     
     public Long getId() {
         return id;
     }
     
     public void setId(Long id) {
         this.id = id;
     }
     
     public String getName() {
         return name;
     }
     
     public void setName(String name) {
         this.name = name;
     }
     
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
     public Integer getQuantity() {
         return quantity;
     }
     
     public void setQuantity(Integer quantity) {
         this.quantity = quantity;
     }
 
     public double getSum() {
        return price * ((double) quantity);
     }
 
     public StockItem getStockItem() {
         return stockItem;
     }
 
     public void setStockItem(StockItem stockItem) {
         this.stockItem = stockItem;
     }
     
 }
