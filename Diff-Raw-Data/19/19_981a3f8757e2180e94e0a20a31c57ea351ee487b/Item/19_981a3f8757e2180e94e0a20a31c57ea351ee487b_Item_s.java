 package model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * Represents a physical item in the Home Inventory System.
  *
  * @invariant product != null
  * @invariant barcode != null
  * @invariant entryDate != null
  * @invariant expirationDate != null
  */
 @SuppressWarnings("serial")
 public class Item implements Comparable<Object>, Serializable {
 	private Product product;
 	private Barcode barcode;
 	private Date entryDate;
 	private Date expirationDate;
 	private Date exitTime;
 	private ProductContainer container;
 	
 	/** Constructs a new Item with the specified data.
 	 * 
 	 * @param product This Item's Product
 	 * @param container The ProductContainer this Item is to be stored in
 	 * @param manager The ItemManager to optimize accesses to this item
 	 * 
 	 * @pre product != null
 	 * @pre container != null
 	 * @pre manager != null
 	 * 
 	 * @post this.container != null
 	 * 
 	 */
 	public Item(Product product, ProductContainer container, ItemManager manager) {
 		this(new Barcode(), product, container, new Date(), manager);
 	}
 	
 	/** Constructs a new Item with the specified data.
 	 * 
 	 * @param product This Item's Product
 	 * @param entryDate The date this Item was entered into the system
 	 * @param container The ProductContainer this Item is to be stored in
 	 * @param manager The ItemManager to optimize accesses to this item
 	 * 
 	 * @pre product != null
 	 * @pre entryDate != null
 	 * @pre container != null
 	 * @pre manager != null
 	 * 
 	 * @post this.container != null
 	 * 
 	 */
 	public Item(Product product, Date entryDate, ProductContainer container, ItemManager manager) {
 		this(new Barcode(), product, container, entryDate, manager);
 	}
 	
 	/** Constructs a new Item with the specified barcode, product, and container. 
 	 *    Sets entryDate to now.
 	 * @param barcode the Item's barcode
 	 * @param product this Item's corresponding Product
 	 * @param container the ProductContainer this Item is to be stored in
 	 * 
 	 * @pre barcode != null
 	 * @pre product != null
 	 * @pre container != null
 	 * @pre manager != null
 	 * 
 	 * @post this.container != null
 	 * 
 	 */
 	public Item(Barcode barcode, Product product, ProductContainer container, 
 			ItemManager manager) {
 		this(barcode, product, container, new Date(), manager);
 	}
 	
 	/** Constructs a new Item with the specified barcode, product, and container.
 	 * @param barcode the Item's barcode
 	 * @param product this Item's corresponding Product
 	 * @param container the ProductContainer this Item is to be stored in
 	 * 
 	 * @pre barcode != null
 	 * @pre product != null
 	 * @pre container != null
 	 * @pre manager != null
 	 * 
 	 * @post this.container != null
 	 * 
 	 */
 	public Item(Barcode barcode, Product product, ProductContainer container, Date entryDate, 
 			ItemManager manager){
 		this.product = product;
 		this.container = container;
 		this.barcode = barcode;
 		this.setEntryDate(entryDate);
 		this.setExpirationDate();
 		manager.manage(this);
 	}
 	
 	/** Gets this Item's Product
 	 * 
 	 * @return this Item's Product
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public Product getProduct() {
 		return product;
 	}
 	
 	/** Gets this Item's Product barcode
 	 * 
 	 * @return Product (String) barcode
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public String getProductBarcode() {
 		return product.getBarcode();
 	}
 	
 	/** Gets this Item's barcode
 	 * 
 	 * @return this item's barcode
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public String getBarcode() {
 		return barcode.getValue();
 	}
 	
 	/** Gets this Item's entry date
 	 * 
 	 * @return this item's entry date
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public Date getEntryDate() {
 		return entryDate;
 	}
 	
 	/** Gets this Item's exit time
 	 * 
 	 * @return the Item's exit time
 	 * 
 	 * @pre Item has been removed
 	 * @post true
 	 */
 	public Date getExitTime() {
 		return exitTime;
 	}
 	
 	/**
 	 * Sets exit time to now, container to null
 	 * 
 	 * @pre true
 	 * @post exitTime != null
 	 * @post container == null
 	 */
 	public void remove(){
 		this.exitTime = new Date();
 		this.container = null;
 	}
 	
 	/** Gets this Item's expiration date
 	 * 
 	 * @return the Item's expiration date
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public Date getExpirationDate() {
 		return this.expirationDate;
 	}
 	
 	/** Gets this Item's parent container
 	 * 
 	 * @return this Item's parent ProductContainer
 	 * 
 	 * @pre true
 	 * @post true
 	 * 
 	 */
 	public ProductContainer getContainer() {
 		return container;
 	}
 	
 	@Override
 	/** Compares this Item to another object
 	 * @param o the object to compare this Item to
 	 * @return zero if the items are equal, otherwise a number greater than or less than zero
 	 * 
 	 * @pre o != null
 	 * @pre (o instanceof Item)
 	 * @post true
 	 */
 	public int compareTo(Object o) {
 		Item other = (Item) o;
 		return barcode.compareTo(other.barcode);
 	}
 	
 	
 	// private setters
 	@SuppressWarnings("deprecation")
 	private void setEntryDate(Date date) {
 		// From the Data Dictionary:
 		// Must be non-empty. Cannot be in the
 		// future or prior to 1/1/2000.
 		if(date == null){
 			this.entryDate = new Date();
 			return;
 		}
 		
 		if(date.after(new Date()) || date.before(new Date(100, 0, 0, 0, 0, 0))){
 			throw new IllegalArgumentException("Date must not be in future");
 		}
 		this.entryDate = date;
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void setExpirationDate(){
 		Date d = this.entryDate;
 		this.expirationDate = new Date(d.getYear(), d.getMonth() 
 			+ this.product.getShelfLife(), 
 			d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
 	}
 }
