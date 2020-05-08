 package pizzaProgram.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import pizzaProgram.dataObjects.Customer;
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.Order;
 import pizzaProgram.dataObjects.OrderDish;
 import pizzaProgram.events.Event;
 import pizzaProgram.events.EventHandler;
 
 /**
  * TODO: Create class javadoc TODO: Add input sanitation
  * 
  * @author Hvard Eidheim, Henning M. Wold
  * 
  */
 public class DatabaseConnection implements EventHandler {
 	private static final String DATABASE_HOST = "jdbc:mysql://mysql.stud.ntnu.no/it1901g03_PizzaBase";
 	private static final String DATABASE_USERNAME = "it1901g03_admin";
 	private static final String DATABASE_PASSWORD = "batman";
 	public static final int DEFAULT_TIMEOUT = 3000;
 	/**
 	 * The maximum amount of allowed characters in a short VARCHAR column in the
 	 * database
 	 */
 	static final int VARCHAR_MAX_LENGTH_SHORT = 50;
 	/**
 	 * The maximum amount of allowed characters in a long VARCHAR column in the
 	 * database
 	 */
 	static final int VARCHAR_MAX_LENGTH_LONG = 100;
 
 	private Connection connection;
 	private QueryHandler queryHandler;
 	private ArrayList<Customer> customers;
 	private HashMap<Integer, Customer> customerMap;
 	private HashMap<Integer, String> customerComments;
 	private HashMap<Integer, String> orderComments;
 	private ArrayList<Dish> dishes;
 	private HashMap<Integer, Dish> dishMap;
 	private ArrayList<Extra> extras;
 	private HashMap<Integer, Extra> extrasMap;
 	private ArrayList<Order> orders;
 	private HashMap<Integer, Order> ordermap;
 
 	public DatabaseConnection() {
 		// this.queryHandler = new QueryHandler();
 	}
 
 	public void buildContents() throws SQLException {
 		createCustomerCommentList();
 		createOrderCommentList();
 		createCustomerList();
 		createDishList();
 		createExtrasList();
 		createOrdersList();
 	}
 
 	/**
 	 * Method that attempt to make a connection to the mySQL database that
 	 * contains the data.
 	 */
 	public void connect() {
 		try {
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			connection = DriverManager.getConnection(DATABASE_HOST,
 					DATABASE_USERNAME, DATABASE_PASSWORD);
 			System.out.println("The connection was a success!");
 
 		} catch (SQLException e) {
 			System.out.println("Connection failed: " + e.getMessage());
 		} catch (ClassNotFoundException e) {
 			System.out.println("Failed during driverinitialization: "
 					+ e.getMessage());
 		} catch (InstantiationException e) {
 			System.out.println("Failed during driverinstantiation: "
 					+ e.getMessage());
 		} catch (IllegalAccessException e) {
 			System.out.println("Failed during driverinstantiation: "
 					+ e.getMessage());
 		}
 	}
 
 	/**
 	 * Method that tries to disconnect from the database if there is a valid
 	 * connection
 	 * 
 	 * @return returns true if there was no valid connection, or the
 	 *         disconnection was successful. Returns false if the method was
 	 *         unable to disconnect from the database.
 	 */
 	public boolean disconnect() {
 		if (connection != null
 				&& isConnected(DatabaseConnection.DEFAULT_TIMEOUT)) {
 			try {
 				this.connection.close();
 			} catch (SQLException e) {
 				System.out.println("Failed to close MySQL connection: "
 						+ e.getMessage());
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Method that checks if the the client is still connected to the database.
 	 * 
 	 * @param timeoutInMilliseconds
 	 *            The time in milliseconds as an int to wait for an answer from
 	 *            the server
 	 * @return true if the connection is still valid, false if not
 	 */
 	public boolean isConnected(int timeoutInMilliseconds) {
 		if (this.connection != null) {
 			try {
 				return this.connection.isValid(timeoutInMilliseconds);
 			} catch (SQLException e) {
 				System.err.println("Something is wrong with the connection: "
 						+ e.getMessage());
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Method to fetch data from the the mySQL database using the provided query
 	 * 
 	 * @param query
 	 *            - a {@link java.lang.String String} containing the query that
 	 *            is to be sent to the database
 	 * @return a {@link java.sql.ResultSet ResultSet} containing the result of
 	 *         the query
 	 */
 	ResultSet fetchData(String query) {
 		try {
 			return connection.createStatement().executeQuery(query);
 		} catch (SQLException e) {
 			System.err.println("Query Failed: " + e.getMessage());
 		}
 		return null;
 	}
 
 	/**
 	 * Method to insert data into the mySQL database using the provided query
 	 * 
 	 * @param query
 	 *            - a {@link java.lang.String String} containing the query that
 	 *            is to be sent to the database
 	 * @return true if the insertion was a success, false in all other cases
 	 */
 	boolean insertIntoDB(String query) {
 		try {
 			return connection.createStatement().execute(query);
 		} catch (SQLException e) {
 			System.err.println("Query Failed: " + e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 * Method that purges the existing {@link java.util.ArrayList ArrayList} and
 	 * the existing {@link java.util.HashMap HashMap} of
 	 * {@link pizzaProgram.dataObjects.Customer customers}, and then creates new
 	 * ones based on the data fetched from the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void createCustomerList() throws SQLException {
 		customers = new ArrayList<Customer>();
 
 		customerMap = new HashMap<Integer, Customer>();
 		ResultSet results = fetchData("SELECT * FROM Customer;");
 		while (results.next()) {
 			Customer tempCustomer = new Customer(results.getInt(1),
 					results.getString(2), results.getString(3),
 					results.getString(4), results.getInt(5),
 					results.getString(6), results.getInt(7),
 					customerComments.get(results.getInt(8)));
 
 			customers.add(tempCustomer);
 			customerMap.put(tempCustomer.customerID, tempCustomer);
 		}
 		results.close();
 	}
 
 	/**
 	 * Method that purges the existing the existing {@link java.util.HashMap
 	 * HashMap} of customer notes, and then creates a new one based on the data
 	 * fetched from the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void createCustomerCommentList() throws SQLException {
 		customerComments = new HashMap<Integer, String>();
 		ResultSet results = fetchData("SELECT * FROM CustomerNotes;");
 		customerComments.put(-1, "");
 		while (results.next()) {
 			customerComments.put(results.getInt(1), results.getString(2));
 		}
 		results.close();
 	}
 
 	/**
 	 * Method for adding a new customer to the database. The method checks if
 	 * the exact combination of firstName+lastName+phoneNumber already exists in
 	 * the database by creating a unique identifier (see below for details), and
 	 * does not add any new rows if such a combination already exists in the
 	 * database.
 	 * 
 	 * @param firstName
 	 *            The first, and middle, name of the customer as a String no
 	 *            longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 	 *            VARCHAR_MAX_LENGTH_SHORT}
 	 * @param lastName
 	 *            The last name of the customer as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 	 *            VARCHAR_MAX_LENGTH_SHORT}
 	 * @param address
 	 *            The address of the customer as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 	 *            VARCHAR_MAX_LENGTH_LONG}
 	 * @param phoneNumber
 	 *            The phone number of the customer as an integer
 	 * @param comment
 	 *            Comments to this customer as a String
 	 * @return returns true if the Customer was added successfully to the
 	 *         database, returns false in all other cases.
 	 * @throws SQLException
 	 */
 	public boolean addCustomer(String firstName, String lastName,
 			String address, int postalCode, String city, int phoneNumber,
 			String comment) throws SQLException {
 		if (firstName.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 				|| lastName.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 				|| address.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 				|| city.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT) {
 			throw new IllegalArgumentException(
 					"Names and cities cannot contain more than "
 							+ DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 							+ " characters and addresses cannot contain more than "
 							+ DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 							+ " characters.");
 		}
 		/*
 		 * Unique identifier composed from the first name, last name and phone
 		 * number of the customer. It is not possible to create a new customer
 		 * who has both the same first name, last name and phone number as
 		 * another customer.
 		 */
 		String identifier = firstName.toLowerCase() + lastName.toLowerCase()
 				+ phoneNumber;
 		if (!fetchData(
 				"SELECT Identifier FROM Customer WHERE Identifier='"
 						+ identifier + "';").next()) {
 			int commentID = -1;
 			if (!(comment == null || comment.equals(""))) {
 				insertIntoDB("INSERT INTO CustomerNotes (Note) VALUES ('"
 						+ comment + "');");
 				ResultSet commentIDset = fetchData("SELECT NoteID FROM CustomerNotes WHERE Note='"
 						+ comment + "';");
 				if (commentIDset.next()) {
 					commentID = commentIDset.getInt(1);
 				}
 			}
 			return insertIntoDB("INSERT INTO Customer (FirstName, LastName, Address, PostalCode, City, TelephoneNumber, CommentID, Identifier) VALUES ('"
 					+ firstName
 					+ "', '"
 					+ lastName
 					+ "', '"
 					+ address
 					+ "', "
 					+ postalCode
 					+ ", '"
 					+ city
 					+ "', "
 					+ phoneNumber
 					+ ", "
 					+ commentID + ", '" + identifier + "');");
 		}
 		return false;
 	}
 
 	/**
 	 * Simplified version (without a comment parameter) of the method for adding
 	 * a new customer to the mySQL database.
 	 * 
 	 * @param firstName
 	 *            The first, and middle, name of the customer as a String no
 	 *            longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 	 *            VARCHAR_MAX_LENGTH_SHORT}
 	 * @param lastName
 	 *            The last name of the customer as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_SHORT
 	 *            VARCHAR_MAX_LENGTH_SHORT}
 	 * @param address
 	 *            The address of the customer as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 	 *            VARCHAR_MAX_LENGTH_LONG}
 	 * @param phoneNumber
 	 *            The phone number of the customer as an integer
 	 * @return returns true if the Customer was added successfully to the
 	 *         database, returns false in all other cases.
 	 * @throws SQLException
 	 */
 	public boolean addCustomer(String firstName, String lastName,
 			String address, int postalCode, String city, int phoneNumber)
 			throws SQLException {
 		return addCustomer(firstName, lastName, address, postalCode, city,
 				phoneNumber, null);
 	}
 
 	/**
 	 * Method for adding a new dish to the mySQL database.
 	 * 
 	 * @param price
 	 *            the price of the dish as an integer
 	 * @param name
 	 *            the name of the dish as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 	 *            VARCHAR_MAX_LENGTH_LONG}
 	 * @param containsGluten
 	 *            set to true if the dish contains gluten, false if not
 	 * @param containsNuts
 	 *            set to true if the dish contains nuts, false if not
 	 * @param containsDairy
 	 *            set to true if the dish contains dairy products, false if not
 	 * @param isVegetarian
 	 *            set to true if the dish is fully vegetarian in nature, false
 	 *            if not
 	 * @param isSpicy
 	 *            set to true if the dish is spicy in nature, false if not
 	 * @param description
 	 *            a description of the dish as a String
 	 * @return returns true if the dish was successfully added to the database,
 	 *         false in all other cases
 	 */
 	public boolean addDish(int price, String name, boolean containsGluten,
 			boolean containsNuts, boolean containsDairy, boolean isVegetarian,
 			boolean isSpicy, String description) {
 		if (name.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_LONG) {
 			throw new IllegalArgumentException(
 					"The name of the dish cannot be more than "
 							+ DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 							+ " characters long.");
 		}
 		return insertIntoDB("INSERT IGNORE INTO Dishes (Price, Name, ContainsGluten, ContainsNuts, ContainsDairy, IsVegetarian, IsSpicy, Description) VALUES ("
 				+ price
 				+ ", '"
 				+ name
 				+ "', "
 				+ containsGluten
 				+ ", "
 				+ containsNuts
 				+ ", "
 				+ containsDairy
 				+ ", "
 				+ isVegetarian
 				+ ", " + isSpicy + ", '" + description + "');");
 	}
 
 	/**
 	 * Adds a dish extra to the database (e.g. extra bacon, without onions,
 	 * large pizza)
 	 * 
 	 * @param name
 	 *            the name of the dish extra as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 	 *            VARCHAR_MAX_LENGTH_LONG}
 	 * @param price
 	 *            the price modifier of the dish extra as a string, where the
 	 *            first character is an operator (*, + or -), and the rest of
 	 *            the characters is a floating point number (e.g. *1.25 or +30)
 	 * @return returns true if the dish extra was successfully added to the
 	 *         database, false in all other cases
 	 */
 	public boolean addExtra(String name, String price) {
 		if (name.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_LONG) {
 			throw new IllegalArgumentException(
 					"The name of the extra cannot be more than "
 							+ DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 							+ " characters long.");
 		}
 		return insertIntoDB("INSERT IGNORE INTO Extras (Name, Price) VALUES ('"
 				+ name + "', '" + price + "');");
 	}
 
 	/**
 	 * Method that purges the existing {@link java.util.ArrayList ArrayList} and
 	 * the existing {@link java.util.HashMap HashMap} of
 	 * {@link pizzaProgram.dataObjects.Dish dishes}, and then creates new ones
 	 * based on the data fetched from the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void createDishList() throws SQLException {
 		dishes = new ArrayList<Dish>();
 		dishMap = new HashMap<Integer, Dish>();
 		ResultSet results = fetchData("SELECT * FROM Dishes;");
 		while (results.next()) {
 			Dish tempDish = new Dish(results.getInt(1), results.getInt(2),
 					results.getString(3), results.getBoolean(4),
 					results.getBoolean(5), results.getBoolean(6),
 					results.getBoolean(7), results.getBoolean(8),
 					results.getString(9));
 
 			dishes.add(tempDish);
 			dishMap.put(tempDish.dishID, tempDish);
 		}
 		results.close();
 	}
 
 	/**
 	 * Method that purges the existing {@link java.util.ArrayList ArrayList} and
 	 * the existing {@link java.util.HashMap HashMap} of
 	 * {@link pizzaProgram.dataObjects.Extra extras}, and then creates new ones
 	 * based on the data fetched from the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void createExtrasList() throws SQLException {
 		extras = new ArrayList<Extra>();
 		extrasMap = new HashMap<Integer, Extra>();
 		ResultSet results = fetchData("SELECT * FROM Extras;");
 		while (results.next()) {
 			Extra tempExtra = new Extra(results.getInt(1),
 					results.getString(2), results.getString(3));
 			extras.add(tempExtra);
 			extrasMap.put(tempExtra.id, tempExtra);
 		}
 		results.close();
 	}
 
 	/**
 	 * Method that purges the existing {@link java.util.ArrayList ArrayList} and
 	 * the existing {@link java.util.HashMap HashMap} of
 	 * {@link pizzaProgram.dataObjects.Order orders}, and then creates new ones
 	 * based on the data fetched from the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void createOrdersList() throws SQLException {
 		orders = new ArrayList<Order>();
 		ordermap = new HashMap<Integer, Order>();
 		HashMap<Integer, OrderDish> tempOrderDishMap = new HashMap<Integer, OrderDish>();
 
 		ResultSet results = fetchData("SELECT Orders.OrdersID,"
 				+ " Orders.CustomerID, Orders.TimeRegistered,"
 				+ " Orders.OrdersStatus, Orders.DeliveryMethod,"
 				+ " Orders.CommentID, OrdersContents.OrdersContentsID,"
 				+ " OrdersContents.DishID, DishExtrasChosen.DishExtraID"
 				+ " FROM Orders LEFT JOIN OrdersContents ON"
 				+ " Orders.OrdersID=OrdersContents.OrdersID"
 				+ " LEFT JOIN DishExtrasChosen ON"
 				+ " OrdersContents.OrdersContentsID=DishExtrasChosen.OrdersContentsID "
 				+ "ORDER BY Orders.OrdersID;");
 
 		while (results.next()) {
 			if (ordermap.get(results.getInt(1)) == null) {
 				Order tempOrder = new Order(results.getInt(1),
 						customerMap.get(results.getInt(2)), results.getDate(3),
 						results.getString(4), results.getString(5),
 						orderComments.get(results.getInt(6)));
 				orders.add(tempOrder);
 				ordermap.put(results.getInt(1), tempOrder);
 
 			}
 			if (tempOrderDishMap.get(results.getInt(7)) == null) {
 				if (dishMap.get(results.getInt(8)) != null) {
 					tempOrderDishMap.put(results.getInt(7), new OrderDish(
 							results.getInt(1), dishMap.get(results.getInt(8))));
 				}
 			}
 			if (extrasMap.get(results.getInt(9)) != null) {
 				tempOrderDishMap.get(results.getInt(7)).addExtra(
 						extrasMap.get(results.getInt(9)));
 			}
 		}
 		for (OrderDish d : tempOrderDishMap.values()) {
 			if (ordermap.get(d.orderID) != null)
 				ordermap.get(d.orderID).addOrderDish(d);
 		}
 		results.close();
 	}
 
 	/**
 	 * Method that purges the existing the existing {@link java.util.HashMap
 	 * HashMap} of order comments, and then creates a new one based on the data
 	 * fetched from the database.
 	 * 
 	 * @throws SQLException
 	 */
 	public void createOrderCommentList() throws SQLException {
 		orderComments = new HashMap<Integer, String>();
 		ResultSet results = fetchData("SELECT * FROM OrderComments;");
 		orderComments.put(-1, "");
 		while (results.next()) {
 			orderComments.put(results.getInt(1), results.getString(2));
 		}
 		results.close();
 	}
 
 	/**
 	 * Method that adds a new order to the database if, and only if, the
 	 * database does not already contain an order that is not yet completed
 	 * (that is the OrdersStatus field has been set to delivered)
 	 * 
 	 * @param customer
 	 *            The {@link pizzaProgram.dataObjects.Customer Customer} object
 	 *            that has made the order
 	 * @param isDeliverAtHome
 	 *            A boolean set to true if this is a delivery, false if it is a
 	 *            pickup order
 	 * @param comment
 	 *            A String containing any additional info for this particular
 	 *            order
 	 * @return Returns true if the order was successfully added to the database,
 	 *         false in all other cases
 	 * @throws SQLException
 	 */
 	public boolean addOrder(Customer customer, boolean isDeliverAtHome,
 			String comment) throws SQLException {
 		String deliverymethod = (isDeliverAtHome ? "deliver at home"
 				: "pickup at restaurant");
 		if (!fetchData(
 				"SELECT * FROM Orders WHERE CustomerID=" + customer.customerID
 						+ " AND OrdersStatus<>'delivered';").next()) {
 			int commentID = -1;
 			if (!(comment == null || comment.equals(""))) {
 				insertIntoDB("INSERT INTO OrderComments (Comment) VALUES ('"
 						+ comment + "');");
				ResultSet commentIDset = fetchData("SELECT CommentID FROM OrderComments WHERE Comment='"
 						+ comment + "';");
 				if (commentIDset.next()) {
 					commentID = commentIDset.getInt(1);
 				}
 			}
 			return insertIntoDB("INSERT INTO Orders (CustomerID, TimeRegistered, DeliveryMethod, CommentID) VALUES ("
 					+ customer.customerID
 					+ ", NOW(), '"
 					+ deliverymethod
 					+ "', " + commentID + ");");
 		}
 		return false;
 
 	}
 
 	/**
 	 * Method that adds one dish, and its associated extras to an existing order
 	 * 
 	 * @param order
 	 *            The {@link pizzaProgram.dataObjects.Order Order} that is
 	 *            having a {@link pizzaProgram.dataObjects.Dish Dish} added to
 	 *            it
 	 * @param dish
 	 *            The {@link pizzaProgram.dataObjects.Dish Dish} to be added to
 	 *            the {@link pizzaProgram.dataObjects.Order Order}
 	 * @param extras
 	 *            An {@link java.util.ArrayList ArrayList} containing the
 	 *            {@link pizzaProgram.dataObjects.Extra Extras} ordered to
 	 *            modify the current {@link pizzaProgram.dataObjects.Dish Dish}
 	 * @throws SQLException
 	 */
 	public void addDishToOrder(Order order, Dish dish, ArrayList<Extra> extras)
 			throws SQLException {
 		int ordersContentsID = -1;
 		insertIntoDB("INSERT INTO OrdersContents (OrdersID, DishID) VALUES ("
 				+ order.getID() + ", " + dish.dishID + ");");
 		if (!(extras == null || extras.isEmpty())) {
 			ResultSet currentOrderContentsID = fetchData("SELECT OrdersContentsID FROM OrdersContents WHERE OrdersID="
 					+ order.getID()
 					+ " AND DishID="
 					+ dish.dishID
 					+ " ORDER BY OrdersContentsID;");
 			if (currentOrderContentsID.last()) {
 				ordersContentsID = currentOrderContentsID.getInt(1);
 			}
 			for (Extra e : extras) {
 				insertIntoDB("INSERT INTO DishExtrasChosen (OrdersContentsID, DishExtraID) VALUES ("
 						+ ordersContentsID + ", " + e.id + ");");
 			}
 		}
 
 	}
 
 	public void changeOrder(Order order) {
 	}
 
 	/**
 	 * @return returns the list of customers as an {@link java.util.ArrayList
 	 *         ArrayList}
 	 */
 	public ArrayList<Customer> getCustomers() {
 		return this.customers;
 	}
 
 	/**
 	 * @return returns the list of orders as an {@link java.util.ArrayList
 	 *         ArrayList}
 	 */
 	public ArrayList<Order> getOrders() {
 		return this.orders;
 	}
 
 	/**
 	 * @return returns the list of dishes as an {@link java.util.ArrayList
 	 *         ArrayList}
 	 */
 	public ArrayList<Dish> getDishes() {
 		return dishes;
 	}
 
 	/**
 	 * @return returns the list of dish extras as an {@link java.util.ArrayList
 	 *         ArrayList}
 	 */
 	public ArrayList<Extra> getExtras() {
 		return extras;
 	}
 
 	public static void main(String[] args) throws SQLException {
 		DatabaseConnection connection = new DatabaseConnection();
 		connection.connect();
 		long starttid = System.currentTimeMillis();
 		connection.createCustomerCommentList();
 		connection.createOrderCommentList();
 		connection.createCustomerList();
 		connection.createDishList();
 		connection.createExtrasList();
 		connection.createOrdersList();
		connection.addOrder(connection.getCustomers().get(0), true, "Kunden truer med  \"drepe dere alle\" om vi ikke klarer  levere pizzaen innen 20min!!");
		connection.createOrdersList();
		Order tempOrder = null;
		for (Order o : connection.getOrders()){
			if (o.getCustomer() == connection.getCustomers().get(0)){
				tempOrder = o;
				break;
			}
		}
		for (Order o : connection.getOrders()){
			System.out.println(o.toString());
 		}
//		ArrayList<Extra> tempextras1 = new ArrayList<Extra>();
//		tempextras1.add(connection.getExtras().get(0));
//		ArrayList<Extra> tempextras2 = new ArrayList<Extra>();
//		tempextras2.add(connection.getExtras().get(1));
//		if (tempOrder != null){
//			connection.addDishToOrder(tempOrder, connection.getDishes().get(4), tempextras1);
//			connection.addDishToOrder(tempOrder, connection.getDishes().get(3), tempextras2);
//		}
 		System.out.println(System.currentTimeMillis() - starttid);
 		System.out.println();
 		connection.disconnect();
 	}
 
 	public void handleEvent(Event event) {
 
 		this.queryHandler.handleEvent(event);
 	}
 }
