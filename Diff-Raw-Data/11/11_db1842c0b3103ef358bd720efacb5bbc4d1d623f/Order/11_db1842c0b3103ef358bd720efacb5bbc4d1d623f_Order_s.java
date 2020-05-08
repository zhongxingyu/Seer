 package model;
 
 import java.util.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 /**
  * A class for orders
  * 
  * @author Sigurd Lund and Øvind Binde
  *
  */
 public class Order {
 	
 	private Customer customer;
 	private ArrayList<Product> products;
 	private String status;
 	private String comment;
 	private int idOrder;
 	private DeliveryFee deliveryFee;
 	private static double limitFreeDelivery;
 	private int allergy;
 	private int delivery;
 	private Date dateAdded;
 	
 	private final static Locale locale = new Locale("no");
 	private final static TimeZone timeZone = TimeZone.getTimeZone("Europe/Oslo");
 	private static Calendar calendar = Calendar.getInstance(timeZone, locale);
 	
 	/**
 	 * Creates a new order with all the information needed.
 	 * This is used if you want to edit an existing order, and the order object is not initialized.
 	 * @param customer
 	 * @param products
 	 * @param status
 	 * @param comment
 	 */
 	private Order(int idOrder, Customer customer, ArrayList<Product> products, String status, String comment, 
 					int allergy, int delivery, long date){
 		this.idOrder = idOrder;
 		this.customer = customer;
 		this.products = products;
 		this.status = status;
 		this.comment = comment;
 		this.allergy = allergy;
 		this.delivery = delivery;
 		this.dateAdded = new Date(date);
 		limitFreeDelivery = Properties.getLimitFreeDelivery();
 		this.deliveryFee = DeliveryFee.getDeliveryFee();
 	}
 	
 	/**
 	 * Creates a new Order with only a customer. More can be added later on.
 	 * Use this to create a new order.
 	 * @param customer
 	 */
 	public Order(Customer customer){
 		this.customer = customer;
 		this.status = "Under bestilling";
 		this.products = new ArrayList<Product>();
 		this.comment = "";
 		limitFreeDelivery = Properties.getLimitFreeDelivery();
 		this.deliveryFee = DeliveryFee.getDeliveryFee();
 		this.setDelivery(true);
 		this.setAllergy(false);
 	}
 	
 	
 	/**
 	 * Returns the totalprice of an given Order.
 	 * @param order
 	 * @return totalprice
 	 */
 	public double getOrderTotalPrice(){
 		double totalprice = 0;
 		if (products.size()<1)
 			return 0;
 	    for (Product p : products) {
 	    	int quantity = p.getQuantity();	
 	        totalprice += ((Product) p).getPrice()*quantity;
 	    }
 	    if (products.contains(deliveryFee) && totalprice > limitFreeDelivery){
 	    	double fee = deliveryFee.getPrice();
 	    	deliveryFee.setPrice(0);
 	    	totalprice -= fee;
 	    }
 	    else if (products.contains(deliveryFee)){
 	    	deliveryFee.setPriceToOriginal();
 	    }
 		return totalprice;
 	}
 	
 	/**
 	 * Adds the order to the Database.
 	 * @return 1 if the order was added, and 0 else.
 	 */
 	public void addOrderToDatabase(){
 		Integer quantity = 0;
 		Database db = Database.getDatabase();
 		//Adds the order
 		String query = "INSERT INTO orders (status, comments, customer_idcustomer, allergy, delivery, time) " +
 		  			   "VALUES ('"+this.status + "','" + comment + "','" + customer.getIdCustomer() + "','" + allergy + "','" + delivery + "','" + calendar.getTimeInMillis() + "')";
 		idOrder = db.insertWithIdReturn(query);
 		//Adds the relation with the products in the order
 	    for (Product p : products) {
 	    	quantity = p.getQuantity();
 	    	int idproduct = p.getIdproduct();
 	    	query = "INSERT INTO product_has_order (product_idproduct, orders_idorder, quantity) " +
 	    	"values('" + idproduct + "','" + idOrder + "','" + quantity + "')";
 	    	db.insert(query);
 		}
 	}
 	
 	/**
 	 * Adds a given product to the order
 	 * @param product
 	 * @param quantity
 	 */
 	public void addProductToOrder(Product product, int quantity){
 		//Adds the quantity if the product exist in the order
 		if (!(product instanceof DeliveryFee) && products.contains(product)){
 			for (Product p : products) {
 				if (p.equals(product)){
 					p.addQuantity(quantity);
 				}
 			}
 		}
 		//Adds the product
		else {
 			products.add(product);
 		}
 		//Adds delivery fee if needed
		if (delivery==1 && !(product instanceof DeliveryFee) && !products.contains(deliveryFee)){
 			products.add(deliveryFee);
 		}
 	}
 	
 	/**
 	 * Removes products from the order. If the order has no other products
 	 * than the delivery fee after the removal, the delivery fee is also removed
 	 * @param product
 	 * @param quantity
 	 */
 	public void removeProductFromOrder(Product product, int quantity){
 		if (products.contains(product)){
 			for (Product p : products){
 				if (p.equals(product)){
 					if (p.getQuantity() > quantity){
 						p.removeQuantity(quantity);
 						break;
 					}
 					else {
 						products.remove(p);
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	
 	/**
 	 * Takes a new status in and updates the order status in the database
 	 * @param status
 	 */
 	public void setStatus(String status){
 		this.status = status;
 		Database db = Database.getDatabase();
 		String query = "UPDATE orders SET status='" + status + "' WHERE idorder=" + idOrder;
 		db.insert(query);
 	}
 	
 	/**
 	 * Adds delivery fee for the order.
 	 */
 	private void addDeliveryFee(){
 		if (!products.contains(deliveryFee))
 			this.addProductToOrder(deliveryFee, 1);
 	}
 	
 	/**
 	 * Removes the delivery fee.
 	 */
 	private void removeDeliveryFee(){
 		if (products.contains(deliveryFee))
 			products.remove(deliveryFee);
 	}
 	
 	/**
 	 * Gets all the orders with two given statuses and order it by ascending from the database.
 	 * @param status1
 	 * @param status2
 	 * @return ArrayList with orders
 	 */
 	public static ArrayList<Order> getRelevantOrders(String status1, String status2){
 		ArrayList<Order> relevantOrders = new ArrayList<Order>();
 		try {
 			Database db;
 			db = Database.getDatabase();
 			ResultSet rs = db.select("SELECT * FROM orders WHERE status='" + status1 + "' OR status='" + status2 
 									+ "' ORDER BY time ASC");
 			while(rs.next()){
 				ArrayList<Product> products = Product.getProductsFromOrder(rs.getInt("idorder"));
 				Customer customer = Customer.getCustomerFromOrder(rs.getInt("customer_idcustomer"));
 				int idOrder = rs.getInt("idorder");
 				String status = rs.getString("status");
 				String comment = rs.getString("comments");
 				int allergy = rs.getInt("allergy");
 				int delivery = rs.getInt("delivery");
 				long date = rs.getLong("time");
 				relevantOrders.add(new Order(idOrder, customer, products, status, comment, allergy, delivery, date));
 			}
 			return relevantOrders;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the orders and order it by ascending from the database.
 	 * @return all orders
 	 */
 	public static ArrayList<Order> getAllOrders(){
 		ArrayList<Order> allOrders = new ArrayList<Order>();
 		try {
 			Database db;
 			db = Database.getDatabase();
 			ResultSet rs = db.select("SELECT * FROM orders ORDER BY time ASC");
 			while(rs.next()){
 				ArrayList<Product> products = Product.getProductsFromOrder(rs.getInt("idorder"));
 				Customer customer = Customer.getCustomerFromOrder(rs.getInt("customer_idcustomer"));
 				int idOrder = rs.getInt("idorder");
 				String status = rs.getString("status");
 				String comment = rs.getString("comments");
 				int allergy = rs.getInt("allergy");
 				int delivery = rs.getInt("delivery");
 				long date = rs.getLong("time");
 				allOrders.add(new Order(idOrder, customer, products, status, comment, allergy, delivery, date));
 			}
 			return allOrders;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the orders that are finished and sorts it by ascending from the database.
 	 * @return finished orders
 	 */
 	public static ArrayList<Order> getFinishedOrders(){
 		ArrayList<Order> allOrders = new ArrayList<Order>();
 		try {
 			Database db;
 			db = Database.getDatabase();
 			ResultSet rs = db.select("SELECT * FROM orders where status!='Utlevert' and status!='Levert' ORDER BY time ASC");
 			while(rs.next()){
 				ArrayList<Product> products = Product.getProductsFromOrder(rs.getInt("idorder"));
 				Customer customer = Customer.getCustomerFromOrder(rs.getInt("customer_idcustomer"));
 				int idOrder = rs.getInt("idorder");
 				String status = rs.getString("status");
 				String comment = rs.getString("comments");
 				int allergy = rs.getInt("allergy");
 				int delivery = rs.getInt("delivery");
 				long date = rs.getLong("time");
 				allOrders.add(new Order(idOrder, customer, products, status, comment, allergy, delivery, date));
 			}
 			return allOrders;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Deletes an order from the database.
 	 * @param order
 	 */
 	public static void deleteOrderFromDatabase(Order order){
 		Database db = Database.getDatabase();
 		String query = "DELETE FROM orders where idorder='" + order.idOrder + "'";
 		db.insert(query);
 	}
 	
 	/**
 	 * Sets a comment in the order.
 	 * @param comment
 	 */
 	public void setComment(String comment){
 		this.comment = comment;
 	}
 	
 	/**
 	 * Returns the comment of an order.
 	 * @return comment
 	 */
 	public String getComment(){
 		return this.comment;
 	}
 	
 	/**
 	 * Returns the status of an order.
 	 * @return status
 	 */
 	public String getStatus(){
 		return this.status;
 	}
 	
 	/**
 	 * Returns the id of an order
 	 * @return idorder
 	 */
 	public int getIdOrder(){
 		return idOrder;
 	}
 	
 	/**
 	 * Returns the customer of an order.
 	 * @return customer
 	 */
 	public Customer getCustomer(){
 		return this.customer;
 	}
 	
 	/**
 	 * Returns all the products in the order.
 	 * @return products
 	 */
 	public ArrayList<Product> getProductsInOrder(){
 		return products;
 	}
 	
 	/**
 	 * Sets the delivery to true or false.
 	 * Adds a delivery fee if it's true.
 	 * @param boolean
 	 */
 	public void setDelivery(boolean b){
 		if (b){
 			delivery = 1;
 			addDeliveryFee();
 		}
 		else{
 			delivery = 0;
 			removeDeliveryFee();
 		}
 	}
 	
 	/**
 	 * Returns true if the order is going to be delivered
 	 * and false if not.
 	 * @return 
 	 */
 	public boolean getDelivery(){
 		return (delivery==1) ? true : false;
 	}
 	
 	/**
 	 * Sets the allergy status to true or false.
 	 * @param boolean
 	 */
 	public void setAllergy(boolean b){
 		if (b){
 			allergy = 1;
 		}
 		else{
 			allergy = 0;
 		}
 	}
 	
 	/**
 	 * Returns true if the customer is allergic
 	 * @return
 	 */
 	public boolean getAllergy(){
 		return (allergy==1) ? true : false;
 	}
 	
 	public String getDateAndTime(){
 		return getDate() + ", " + getTime();
 	}
 	
 	public String getDate(){
 		SimpleDateFormat df = new SimpleDateFormat("dd");
 		return df.format(dateAdded);
 	}
 	
 	public String getTime(){
 		DateFormat df = DateFormat.getTimeInstance(3, locale);
 		return df.format(dateAdded);
 	}
 
 }
