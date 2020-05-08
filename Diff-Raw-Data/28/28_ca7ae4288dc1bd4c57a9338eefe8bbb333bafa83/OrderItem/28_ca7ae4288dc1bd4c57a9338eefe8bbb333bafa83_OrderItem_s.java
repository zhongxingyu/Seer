 package uk.ac.aber.dcs.cs12420.aberpizza.data;
 
 import java.math.BigDecimal;
 
 /**
  * Represents a link to a particular Item, which is an
  * interface that should be implemented by the products
  * in your design, and the number of the specified item.
  * 
  * For example, an OrderItem might represent a pizza,
  * with quantity 2.
  * 
  * @author Neil Taylor
  *
  */
 public class OrderItem {
 	private int quantity;
	private Item item; // TODO: Deviation from design by addition of items field
 	
 	public OrderItem(Item item, int quantity) {
 		this.quantity = quantity;
 		this.item = item; 
 	}
 	
 	public int getQuantity() {
 		return quantity;
 	}
 	
 	public BigDecimal getOrderItemTotal() {
 		return item.getPrice().multiply(new BigDecimal(quantity) );
 	}
 	
 }
