 /**
  * 
  */
 package model;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 //import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * @author
  *
  */
 @XmlRootElement(name="order")
 public class ShoppingCartBean {
 
 	private static final double HSTRate = 1.13;
 	private static final double minOrder = 100;
 	private static final double zeroShipping = 0;
 	private static final double standardShipping = 5;
 	
 	
 	private HashMap<String,ItemBean> shoppingCart;
 	
 //	@XmlAttribute
 //	private int id;
 //	
 //	@XmlAttribute
 //	private Date submitted;
 //	
 //	@XmlElement
 //	private ClientBean customer;
 	
 	@XmlElement(name="item")
 	private Collection<ItemBean> items;
 	
 	private double total;
 	
 	private double shipping;
 	
 	private double HST;
 	
 	private double grandTotal;
 	
 	/**
 	 * 
 	 */
 	public ShoppingCartBean()
 	{
		this.shoppingCart = new HashMap<String, ItemBean>();
 	}
 	
 	
 	/**
 	 * The method add the item specified by itemNumber to the shoppingCart hashmap 
 	 * @param itmeNumber - the number (id) of the item
 	 * @throws Exception 
 	 */
 	public void add(ItemBean item)
 	{
 		this.shoppingCart.put(item.getItemNumber(), item);
 		this.updateTotalPrice();
 	}
 	
 
 	/**
 	 * Update quantity of item in shoppingCart
 	 * @param ShoppingCartBean
 	 * @param itemNumber
 	 * @param qty
 	 */
 	public void updateQty(String itemNumber, int qty)
 	{
 		if (qty == 0)//remove the item from shoppingCart
 		{
 			this.shoppingCart.remove(itemNumber);
 		}else //update qty
 		{
 			this.shoppingCart.get(itemNumber).setQty(qty);
 		}
 	}
 	
 	/**
 	 * Order more for the item already exists in shopping cart
 	 * @param itemNumber
 	 * @param qty
 	 */
 	public void incrementQty(String itemNumber, int qty)
 	{
 		ItemBean item = this.shoppingCart.get(itemNumber);
 		item.setQty(item.getQty() + qty);
 	}
 	
 	
 	/**
 	 * Calculate total price of items in shopping cart
 	 */
 	private void updateTotalPrice()
 	{
 		
 		double totalPrice = 0;
 		for(ItemBean item: this.shoppingCart.values())
 		{
 			totalPrice += (item.getPrice() * item.getQty());
 		}
 		this.setTotal(totalPrice);
 	}
 	
 	/**
 	 * update shipping cost after getting total price
 	 */
 	private void updateShipping()
 	{
 		this.updateTotalPrice();
 		if (this.total > ShoppingCartBean.minOrder)
 		{
 			this.setShipping(ShoppingCartBean.zeroShipping);
 		}else{
 			this.setShipping(ShoppingCartBean.standardShipping);
 		}
 	}
 	
 	/**
 	 * update HST after updating shipping cost
 	 */
 	private void updateHST()
 	{
 		this.updateShipping();
 		this.setHST((this.shipping + this.total)*ShoppingCartBean.HSTRate);
 	}
 	
 	/**
 	 * 
 	 */
 	private void calculateGrandTotal()
 	{
 		this.updateHST();
 		this.setGrandTotal(this.getTotal() + this.getShipping() + this.getHST());
 	}
 	
 	/**
 	 * 
 	 */
 	public void checkOutUpdate()
 	{
 		this.calculateGrandTotal();
 		this.items = this.shoppingCart.values();
 	}
 
 	/**
 	 * @return the items
 	 */
 	public Collection<ItemBean> getItems() {
 		return items;
 	}
 
 	/**
 	 * @return the total
 	 */
 	public double getTotal() {
 		return total;
 	}
 
 	/**
 	 * @param total the total to set
 	 */
 	private void setTotal(double total) {
 		this.total = total;
 	}
 
 	/**
 	 * @return the shipping
 	 */
 	public double getShipping() {
 		return shipping;
 	}
 
 	/**
 	 * @param shipping the shipping to set
 	 */
 	private void setShipping(double shipping) {
 		this.shipping = shipping;
 	}
 
 	/**
 	 * @return the grandTotal
 	 */
 	public double getGrandTotal() {
 		return grandTotal;
 	}
 
 	/**
 	 * @param grandTotal the grandTotal to set
 	 */
 	private void setGrandTotal(double grandTotal) {
 		this.grandTotal = grandTotal;
 	}
 
 	/**
 	 * @param hST the hST to set
 	 */
 	private void setHST(double hST) {
 		HST = hST;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public double getHST()
 	{
 		return this.HST;
 	}
 	
 	/**
 	 * to test if the shopping cart already has the item
 	 * @param itemNumber
 	 * @return - True if this shopping cart has this item, false otherwise.
 	 */
 	public boolean hasItem(String itemNumber)
 	{
 		return this.shoppingCart.containsKey(itemNumber);
 	}
 	
 }
