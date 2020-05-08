 package ntnu.it1901.gruppe4.db;
 
 import java.util.Date;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 import ntnu.it1901.gruppe4.db.DataAPI;
 
 /**
  * Data class for orders
  * 
  * @author David M.
  */
 @DatabaseTable(tableName = "order")
 public class Order {
 	@DatabaseField(canBeNull = false, generatedId = true)
 	int idOrder;
 
 	@DatabaseField(useGetSet = true)
 	Date orderTime;
 
 	@DatabaseField(useGetSet = true)
 	Date deliveryTime;
 
 	@DatabaseField(useGetSet = true)
 	int state;
 	// States:
 	// 0: Not saved
 	// 10: Saved
 	// 20: Ready for delivery
 	// 30: In transit
 	// 40: Delivered & paid for
 
 	@DatabaseField(useGetSet = true)
 	float totalAmount;
 
 	// PS: The idAddress object might not actually contain the data. The
 	// idAddress object will be an Address object without any id, except for the
 	// idAddress field. This is it's only use, and it's used by DataAPI.
 
 	@DatabaseField(useGetSet = true, foreign = true, canBeNull = false)
 	Address idAddress;
 
 	public Order() {
		setOrderTime(new Date());
 		setState(0);
 	}
 
 	public Order(Address address) {
 		setIdAddress(address);
 		setDeliveryTime(new Date()); // Supposedly this is the current time
 		setState(0);
 	}
 
 	public Address getIdAddress() {
 		return idAddress;
 	}
 
 	public void setIdAddress(Address idAddress) {
 		this.idAddress = idAddress;
 	}
 
 	public int getIdOrder() {
 		return idOrder;
 	}
 
 	public Date getOrderTime() {
 		return orderTime;
 	}
 
 	public void setOrderTime(Date orderTime) {
 		this.orderTime = orderTime;
 	}
 
 	public Date getDeliveryTime() {
 		return deliveryTime;
 	}
 
 	public void setDeliveryTime(Date deliveryTime) {
 		this.deliveryTime = deliveryTime;
 	}
 
 	public int getState() {
 		return state;
 	}
 
 	public void setState(int state) {
 		this.state = state;
 	}
 
 	public float getTotalAmount() {
 		return totalAmount;
 	}
 
 	public void setTotalAmount(float totalAmount) {
 		this.totalAmount = totalAmount;
 	}
 	
 	/**
 	 * Persists the Order to the database by updating an existing Order,
 	 * or -- if one doesn't exist -- adding a new Order. 
 	 */
 	public void save() {
 		DataAPI.saveOrder(this);
 	}
 }
