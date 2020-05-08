 package oshop.model;
 
import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 @Entity
@Table(name = "order")
 public class Order extends BaseEntity<Integer> {
 
     @ManyToOne(fetch = FetchType.LAZY)
    private Item item;
 
     @ManyToOne(fetch = FetchType.LAZY)
     private Customer customer;
 
     @ManyToOne(fetch = FetchType.LAZY)
     private ShippingAddress shippingAddress;
 
     @ManyToOne(fetch = FetchType.LAZY)
     private Discount discount;
 
     @ManyToOne(fetch = FetchType.LAZY)
     private Prepayment prepayment;
 
    public Item getItem() {
        return item;
     }
 
    public void setItem(Item item) {
        this.item = item;
     }
 
     public Customer getCustomer() {
         return customer;
     }
 
     public void setCustomer(Customer customer) {
         this.customer = customer;
     }
 
     public ShippingAddress getShippingAddress() {
         return shippingAddress;
     }
 
     public void setShippingAddress(ShippingAddress shippingAddress) {
         this.shippingAddress = shippingAddress;
     }
 
     public Discount getDiscount() {
         return discount;
     }
 
     public void setDiscount(Discount discount) {
         this.discount = discount;
     }
 
     public Prepayment getPrepayment() {
         return prepayment;
     }
 
     public void setPrepayment(Prepayment prepayment) {
         this.prepayment = prepayment;
     }
 }
