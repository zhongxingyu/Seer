 package ntnu.it1901.gruppe4.db;
 
 import java.sql.*;
 import java.util.List;
 import java.util.Map;
 import java.lang.Exception;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcConnectionSource;
 import com.j256.ormlite.stmt.PreparedQuery;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.Where;
 import com.j256.ormlite.table.TableUtils;
 
 /**
  * API to communicate with pizza server
  * 
  * @author David M.
  * @author Lars Kinn Ekroll
  */
 public class DataAPI {
 	private static Dao<Customer, Integer> customerDao;
 	private static Dao<Address, Integer> addressDao;
 	private static Dao<Dish, Integer> dishDao;
 	private static Dao<Order, Integer> orderDao;
 	private static Dao<OrderItem, Integer> orderItemDao;
 	private static Dao<Config, String> configDao;
 
 	private static JdbcConnectionSource conn = null;
 
 	private static String url = "";
 
 	/**
 	 * Opens a connection to a database
 	 * 
 	 * @param file
 	 *            The filename of the database
 	 */
 	public static void open(String file) {
 		if (conn != null) {
 			System.out.println("[Debug] Connection already open. Closing...");
 			close();
 		}
 		url = "jdbc:sqlite:" + file;
 		// String url = "jdbc:sqlite:./data.db";
 
 		System.out.println("[Debug] Opening database " + url + "...");
 
 		try {
 			// Class.forName("org.sqlite.JDBC");
 			conn = new JdbcConnectionSource(url);
 
 			if (conn == null)
 				throw new Exception("Failed to connect to database.");
 
 			setupDatabase();
 			setupSettings();
 			/* setupSettings() should only be called here, but since the GUIs
 			 * clear the database on startup, it has to be called again later,
 			 * from outside this class.
 			 */
 		} catch (Exception e) {
 			System.err.println("[Error] Failed to open database connection: "
 					+ e.getMessage());
 		}
 	}
 
 	/**
 	 * Sets up the database
 	 */
 	private static void setupDatabase() throws SQLException {
 		if (conn != null) {
 			System.out.println("[Debug] Initializing tables...");
 
 			customerDao = DaoManager.createDao(conn, Customer.class);
 			addressDao = DaoManager.createDao(conn, Address.class);
 			dishDao = DaoManager.createDao(conn, Dish.class);
 			orderDao = DaoManager.createDao(conn, Order.class);
 			orderItemDao = DaoManager.createDao(conn, OrderItem.class);
 			configDao = DaoManager.createDao(conn, Config.class);
 
 			TableUtils.createTableIfNotExists(conn, Customer.class);
 			TableUtils.createTableIfNotExists(conn, Address.class);
 			TableUtils.createTableIfNotExists(conn, Dish.class);
 			TableUtils.createTableIfNotExists(conn, Order.class);
 			TableUtils.createTableIfNotExists(conn, OrderItem.class);
 			TableUtils.createTableIfNotExists(conn, Config.class);
 		} else {
 			System.err
 					.println("[Error] Tried to setup database without a connection");
 		}
 	}
 	
 	/**
 	 * Ensures that the program settings exist, by setting any missing settings
 	 * to defaults from {@link Settings#DEFAULT_SETTINGS}. Does not overwrite
 	 * existing settings!
 	 */
 	public static void setupSettings() {
 		// TODO: This should be private, eventually.
 		for (Map.Entry<String, String> entry : Settings.DEFAULT_SETTINGS.entrySet()) {
 			try {
 				configDao.createIfNotExists(new Config(entry.getKey(), entry.getValue()));
 			} catch (SQLException e) {
 				System.err.println("Error setting default config value: " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Delete all the data
 	 */
 	public static void clearDatabase() {
 		if (conn != null) {
 			try {
 				System.out.println("[Debug] Initializing tables...");
 				TableUtils.clearTable(conn, Customer.class);
 				TableUtils.clearTable(conn, Address.class);
 				TableUtils.clearTable(conn, Dish.class);
 				TableUtils.clearTable(conn, Order.class);
 				TableUtils.clearTable(conn, OrderItem.class);
 				TableUtils.clearTable(conn, Config.class);
 			} catch (SQLException e) {
 				System.err.println("[Error] While clearing database: "
 						+ e.getMessage());
 			}
 		} else {
 			System.err
 					.println("[Error] Tried to clear database without a connection");
 		}
 	}
 
 	/**
 	 * Inserts example data into database
 	 */
 	public static void createExampleData() {
 		try {
 			Customer c = new Customer("Eksemplus Eksempelsen", "512256128");
 
 			List<Customer> cl = customerDao.queryForMatching(c);
 
 			if (cl == null || cl.size() == 0) {
 
 				Customer c1 = new Customer("Finn", "2002");
 				Customer c2 = new Customer("Jake", "1001");
 				Customer c3 = new Customer("Princess Bubblegum", "3003");
 
 				Address a1 = new Address(c, "Internettveien 64", 1024);
 				Address a2 = new Address(c, "Addresseveien 32", 2048);
 				
 				Address a3 = new Address(c1, "Land of Ooo", 5000);
 				Address a4 = new Address(c2, "Land of Ooo", 5000);
 				Address a5 = new Address(c3, "Candy Kingdom", 7000);
 
 				Dish d1 = new Dish("Pizza Capriciosa", 50,
 						"Skinke & Champignon", true);
 				Dish d2 = new Dish("Pizza Pepperoni", 52, "Pepperoni; nom nom",
 						true);
 				Dish d3 = new Dish("Hårete pizza", 35,
 						"Billigere, men med spesiell topping", true);
 				Dish d4 = new Dish("Krempizza", 60, "Bløtkake på pizzabunn",
 						true);
 				Dish d5 = new Dish("Kokkens spesial", 70,
 						"Med kokkens spesialsaus", true);
 				Dish d6 = new Dish("Coca Cola 1.5L", 40,
 						"Din favorittbrus, en-og-en-halv-liter", true);
 				Dish d7 = new Dish("Rømmedressing", 25,
 						"Plastskei ikke inkludert", true);
 				Dish d8 = new Dish("Plastskei", 2, "Trengs til saus/dressing",
 						true);
 				Dish d9 = new Dish("Serviett", 10,
 						"Luksusserviett av stoff. Sydd av barn i Bangladesh",
 						true);
 
 				saveCustomer(c);
 				saveCustomer(c1);
 				saveCustomer(c2);
 				saveCustomer(c3);
 				saveAddress(a1);
 				saveAddress(a2);
 				saveAddress(a3);
 				saveAddress(a4);
 				saveAddress(a5);
 				saveDish(d1);
 				saveDish(d2);
 				saveDish(d3);
 				saveDish(d4);
 				saveDish(d5);
 				saveDish(d6);
 				saveDish(d7);
 				saveDish(d8);
 				saveDish(d9);
 
 				OrderMaker om = new OrderMaker();
 				Order o = om.getOrder();
 				o.setIdAddress(a1);
 				om.addItem(d3);
 				om.addItem(d4);
 				om.addItem(d5);
 				om.save();
 
 				System.out.println("[Debug] Inserted example data");
 			} else {
 				System.out.println("[Debug] Example data already present");
 			}
 		} catch (SQLException e) {
 			System.err.println("Error inserting example data: "
 					+ e.getMessage());
 		}
 	}
 
 	/**
 	 * Closes the connection to the database
 	 */
 	public static void close() {
 		System.out.println("[Debug] Closing database");
 
 		if (conn != null) {
 			try {
 				conn.close();
 			} catch (SQLException e) {
 			} finally {
 				conn = null;
 			}
 		}
 	}
 
 	// Customer
 
 	/**
 	 * Stores a Customer to the database. If the ID matches an existing Customer,
 	 * the match is updated. If not, a new Customer is added to the database.
 	 * 
 	 * @param c
 	 *            a reference to the Customer object containing the data to be
 	 *            stored
 	 */
 	public static void saveCustomer(Customer c) {
 		try {
 			customerDao.createOrUpdate(c);
 		} catch (SQLException e) {
 			System.err.println("Error storing customer: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Fetches customer data and stores it in a Customer object
 	 * 
 	 * @param id
 	 *            unique ID used to identify a customer in the database
 	 *            (idCustomer)
 	 * @return a reference to a new Customer object containing the data
 	 */
 	public static Customer getCustomer(int id) {
 		try {
 			if (id == 0)
 				return null;
 			return customerDao.queryForId(id);
 		} catch (SQLException e) {
 			System.err.println("Error fetching customer: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Fetches customer data and stores it in a Customer object
 	 * 
 	 * @param address
 	 *            an Address object containing a reference to a Customer object
 	 *            containing the unique ID of the customer.
 	 * @return a reference to a new Customer object containing the data
 	 */
 	public static Customer getCustomer(Address address) {
 		try {
 			if (address == null)
 				return null;
 			return customerDao.queryForId(address.getIdCustomer().getIdCustomer());
 		} catch (SQLException e) {
 			System.err.println("Error fetching customer by address: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Searches for customers by substring
 	 * 
 	 * @param search
 	 *            The search string
 	 * @return a reference to a new Customer object containing the data
 	 */
 	public static List<Customer> findCustomers(String search) {
 		try {
 			search = search.trim().replace("  ", " ").replace("  ", " ");
 
 			String[] strings = search.split(" ");
 
 			QueryBuilder<Customer, Integer> qb = customerDao.queryBuilder();
 			Where<Customer, Integer> where = qb.where();
 
 			// Test for each word in the string sequence.
 			//
 			// "david m" will search for any name containing "david" and "m"
 			//
 			// LIKE is not case sensitive
 
 			int i = 0;
 			for (; i < strings.length - 1; i++) {
 				where.eq("phone", strings[i]);
 				where.or();
 				where.like("name", "%" + strings[i] + "%");
 				where.and();
 			}
 			where.eq("phone", strings[i]);
 			where.or();
 			where.like("name", "%" + strings[i] + "%");
 
 			return customerDao.query(where.prepare());
 		} catch (SQLException e) {
 			System.err.println("Error searching for customer: "
 					+ e.getMessage());
 			return null;
 		}
 	}
 
 	// Address
 
 	/**
 	 * Stores an Address to the database. If the ID matches an existing Address,
 	 * the match is updated. If not, a new Address is added to the database.
 	 * 
 	 * @param a
 	 *            a reference to the Address object containing the data to be
 	 *            stored
 	 */
 	public static void saveAddress(Address a) {
 		try {
 			addressDao.createOrUpdate(a);
 		} catch (SQLException e) {
 			System.err.println("Error storing address: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Fetches address data and stores it in a Address object
 	 * 
 	 * @param id
 	 *            a unique ID used to identify an address in the database
 	 *            (idAddress)
 	 * @return a reference to a new Address object containing the data
 	 */
 	public static Address getAddress(int id) {
 		try {
 			if (id == 0)
 				return null;
 			return addressDao.queryForId(id);
 		} catch (SQLException e) {
 			System.err.println("Error fetching address: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Fetches address associated with an order
 	 * 
 	 * @param order
 	 *            an Order object containing an Address object containing the id
 	 * @return a reference to a new Address object containing the data
 	 */
 	public static Address getAddress(Order order) {
 		try {
 			if (order == null)
 				return null;
 			return addressDao.queryForId(order.getIdAddress().getIdAddress());
 		} catch (SQLException e) {
 			System.err.println("Error fetching address by order: "
 					+ e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Fetches list of address data associated with a customer and stores it in
 	 * a List<Address> object
 	 * 
 	 * @param customer
 	 *            the customer whose addresses should be fetched
 	 * @return a reference to a new List<Address> object containing the data
 	 */
 	public static List<Address> getAddresses(Customer customer) {
 		try {
 			if (customer == null)
 				return null;
 			return addressDao.queryForEq("idCustomer_id",
 					customer.getIdCustomer());
 		} catch (SQLException e) {
 			System.err.println("Error fetching addresses: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Finds addresses containing the search string
 	 * 
 	 * @param s
 	 *            the search string
 	 * @return a reference to a new List<Address> object with the matching
 	 *         addresses
 	 */
 
 	public static List<Address> findAddresses(String s) {
 		try {
 			return addressDao.query(addressDao.queryBuilder().where()
 					.like("addressLine", "%" + s + "%").prepare());
 		} catch (SQLException e) {
 			System.err.println("Error when searching for addresses: "
 					+ e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Stores a Dish to the database. If the ID matches an existing Dish,
 	 * the match is updated. If not, a new Dish is added to the database.
 	 * 
 	 * @param dish
 	 *            a reference to the Dish object containing the data to be
 	 *            stored
 	 */
 	public static void saveDish(Dish dish) {
 		try {
 			dishDao.createOrUpdate(dish);
 		} catch (SQLException e) {
 			System.err.println("Error storing dish: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Fetches dish data and stores it in a Dish object
 	 * 
 	 * @param id
 	 *            a unique ID used to identify an dish in the database (idDish)
 	 * @return a reference to a new Dish object containing the data
 	 */
 	public static Dish getDish(int id) {
 		try {
 			if (id == 0)
 				return null;
 			return dishDao.queryForId(id);
 		} catch (SQLException e) {
 			System.err.println("Error fetching dish: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Finds dishes containing the search string
 	 * 
 	 * @param s
 	 *            the search string
 	 * @return a reference to a new List<Dish> object with the matching dishes
 	 */
 	public static List<Dish> findDishes(String s) {
 		try {
 			return dishDao.query(dishDao.queryBuilder().where()
 					.like("name", "%" + s + "%").prepare());
 		} catch (SQLException e) {
 			System.err.println("Error when searching for dish: "
 					+ e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Stores an Order to the database. If the ID matches an existing Order,
 	 * the match is updated. If not, a new Order is added to the database.
 	 * 
 	 * @param order
 	 *            a reference to the Order object containing the data to be
 	 *            stored
 	 */
 	public static void saveOrder(Order order) {
 		try {
 			orderDao.createOrUpdate(order);
 		} catch (SQLException e) {
 			System.err.println("Error storing order: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Removes an existing order in the database
 	 * 
 	 * @param order
 	 *            a reference to the Order object containing the data to be
 	 *            removed
 	 */
 	public static void remOrder(Order order) {
 		try {
 			// First remove all the orderItems
 			List<OrderItem> orderItems = getOrderItems(order);
 			for (OrderItem item : orderItems) {
 				remOrderItem(item);
 			}
 			// Then remove the order
 			orderDao.delete(order);
 		} catch (SQLException e) {
 			System.err.println("Error removing order: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Fetches order data and stores it in a Order object
 	 * 
 	 * @param id
 	 *            a unique ID used to identify an dish in the database (idOrder)
 	 * @return a reference to a new Order object containing the data
 	 */
 	public static Order getOrder(int id) {
 		try {
 			if (id == 0)
 				return null;
 			return orderDao.queryForId(id);
 		} catch (SQLException e) {
 			System.err.println("Error fetching order: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Fetches a list of orders
 	 * 
 	 */
 	public static List<Order> getOrders() {
 		try {
 			return orderDao.queryForAll();
 		} catch (SQLException e) {
 			System.err.println("Error fetching order: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Stores an OrderItem to the database. If the ID matches an existing OrderItem,
 	 * the match is updated. If not, a new OrderItem is added to the database.
 	 * 
 	 * @param orderItem
 	 *            a reference to the OrderItem object containing the data to be
 	 *            stored
 	 */
 	public static void saveOrderItem(OrderItem orderItem) {
 		try {
 			orderItemDao.createOrUpdate(orderItem);
 		} catch (SQLException e) {
 			System.err.println("Error storing orderitem: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Stores an OrderItem from the database
 	 * 
 	 * @param orderItem
 	 *            a reference to the OrderItem object containing the data to be
 	 *            removed
 	 */
 	public static void remOrderItem(OrderItem orderItem) {
 		try {
 			orderItemDao.delete(orderItem);
 		} catch (SQLException e) {
 			System.err.println("Error removing orderitem: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Fetches order item data and stores it in a OrderItem object
 	 * 
 	 * @param id
 	 *            a unique ID used to identify an dish in the database
 	 *            (idOrderItem)
 	 * @return a reference to a new OrderItem object containing the data
 	 */
 	public static OrderItem getOrderItem(int id) {
 		try {
 			if (id == 0)
 				return null;
 			return orderItemDao.queryForId(id);
 		} catch (SQLException e) {
 			System.err.println("Error fetching order item: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Fetches a list of order items associated with an order
 	 * 
 	 * @param order
 	 *            the order the order items should be associated with
 	 * @return a reference to a List<OrderItem> containing the order items
 	 */
 	public static List<OrderItem> getOrderItems(Order order) {
 		try {
 			return orderItemDao.queryForEq("idOrder_id", order.getIdOrder());
 		} catch (SQLException e) {
 			System.err.println("Error fetching order items: " + e.getMessage());
 			return null;
 		}
 	}
 	
 	// Config
 	
 	/**
 	 * Stores a configuration value to the database
 	 * 
 	 * @param key - the name of the configuration value (a String)
 	 * @param value - the value (a String)
 	 */
 	static void setConfig(String key, String value) {
 		try {
 			configDao.createOrUpdate(new Config(key, value));
 		} catch (SQLException e) {
 			System.err.println("Error storing config value: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Fetches a configuration value from the database
 	 * 
 	 * @param key - the name of the configuration (a String)
 	 * @return the value, as a String
 	 */
 	static String getConfig(String key) {
 		try {
			return configDao.queryForId(key).getValue();
 		} catch (SQLException e) {
 			System.err.println("Error fetching config value: " + e.getMessage());
 			return null;
 		}
 	}
 }
