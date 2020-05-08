 package ntnu.it1901.gruppe4.db;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 import ntnu.it1901.gruppe4.db.DataAPI;
 
 /**
  * Data class for order items
  * 
  * @author David M.
  */
 @DatabaseTable(tableName = "orderitem")
 public class OrderItem {
 
 	@DatabaseField(canBeNull = false, generatedId = true)
 	int idOrderItem;
 
 	@DatabaseField(useGetSet = true, foreign = true, canBeNull = false)
 	Order idOrder;
 
 	// PS: We don't always need a dish item, in case we include arbitrary things
 	// like a discount
 	@DatabaseField(useGetSet = true, foreign = true)
 	Dish idDish;
 
 	@DatabaseField(useGetSet = true, canBeNull = false)
 	String name;
 
 	@DatabaseField(useGetSet = true)
 	float amount;
 
 	@DatabaseField(useGetSet = true)
 	String description;
 
 	private OrderItem() {
 	}
 
 	/**
 	 * Constructor that creates a new OrderItem from a dish. The name and price
 	 * of the dish will be copied to the orderItem.
 	 * 
 	 * @param order
 	 *            the order that this OrderItem should be placed in
 	 * @param dish
 	 *            the dish that this OrderItem should inherit it's name and
 	 *            price from.
 	 */
 	public OrderItem(Order order, Dish dish) {
 		setIdOrder(order);
 		setIdDish(dish);
 		setAmount(dish.getPrice());
 		setName(dish.getName());
 		setDescription(dish.getDescription());
 	}
 
 	/**
 	 * Constructor that creates a new OrderItem from name and price. This
 	 * orderItem will not be associated with any dish.
 	 * 
 	 * @param order
 	 *            the order that this OrderItem should be placed in
 	 * @param name
 	 *            The name of the order item
 	 * @param amount
 	 *            The price of this order item
 	 */
 	public OrderItem(Order order, String name, float amount) {
 		setIdOrder(order);
 		setIdDish(null);
 		setAmount(amount);
 		setName(name);
 	}
 
 	/**
 	 * Returns the data id of this object
 	 * 
 	 * @return the data id of this object
 	 */
 	public int getIdOrderItem() {
 		return idOrderItem;
 	}
 
 	/**
 	 * Returns the order that this order item is a part of
 	 * 
 	 * @return the order that this order item is a part of
 	 */
 	public Order getIdOrder() {
 		return idOrder;
 	}
 
 	/**
 	 * Associated this order item with a new order
 	 * 
 	 * @param idOrder
 	 *            the new order to place this order item in
 	 */
 	public void setIdOrder(Order idOrder) {
 		this.idOrder = idOrder;
 	}
 
 	/**
 	 * Returns the dish that this order item was created from. It may be null,
 	 * if this order item is a custom item.
 	 * 
 	 * @return the dish that this order was created from, or null
 	 */
 	public Dish getIdDish() {
 		return idDish;
 	}
 
 	/**
 	 * Sets the dish reference of this order item. May be null.
 	 * 
 	 * @param idDish
 	 *            the new dish, or null
 	 */
 	public void setIdDish(Dish idDish) {
 		this.idDish = idDish;
 	}
 
 	/**
 	 * Returns the name of this order item. May be the name of a dish, or a
 	 * descriptive name of an order, like "Pizza Pepperoni /w extra cheese" or
 	 * "Coke 33cl".
 	 * 
 	 * @return the name of this order item
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name of the order item.
 	 * 
 	 * @param name
 	 *            the new name of the order item.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Returns the price of the order item. It may or may not be consistent with
 	 * the current price of the dish. The price is assigned at the time when the
 	 * order item was created.
 	 * 
 	 * @return the price of this order item.
 	 */
 	public float getAmount() {
 		return amount;
 	}
 
 	/**
 	 * Sets the price of the order item.
 	 * 
 	 * @param amount
 	 *            the new price of the order item.
 	 */
 	public void setAmount(float amount) {
 		this.amount = amount;
 	}
 
 	/**
 	 * Returns the description of the item.
 	 * 
 	 * @return the price of this order item.
 	 */
	public String getDecsription() {
 		return description;
 	}
 
 	/**
 	 * Sets the description of the order item.
 	 * 
 	 * @param description
 	 *            the new description of the order item.
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Persists the OrderItem to the database by updating an existing OrderItem,
 	 * or -- if one doesn't exist -- adding a new OrderItem.
 	 */
 	public void save() {
 		DataAPI.saveOrderItem(this);
 	}
 }
