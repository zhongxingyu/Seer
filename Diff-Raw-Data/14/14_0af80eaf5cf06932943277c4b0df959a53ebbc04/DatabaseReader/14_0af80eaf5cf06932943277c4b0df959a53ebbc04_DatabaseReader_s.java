 package pizzaProgram.database.databaseUtils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import pizzaProgram.dataObjects.Customer;
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.Order;
 import pizzaProgram.dataObjects.OrderDish;
 import pizzaProgram.database.DatabaseConnection;
 
 public class DatabaseReader {
 	
 	public static ArrayList<Customer> getAllCustomers(){
 		ResultSet results = DatabaseConnection.fetchData("SELECT * FROM Customer LEFT JOIN CustomerNotes ON ( Customer.commentID = CustomerNotes.NoteID );");
 		ArrayList<Customer> customerList = new ArrayList<Customer>();
 		try {
 			customerList = generateCustomerList(results);
 			return customerList;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllCustomersFailedMessage();
 		}
 		return null;
 	}
 	
 	public static ArrayList<Customer> searchCustomerByString(String searchQuery){
 		String query = generateCustomerSearchQuery(searchQuery);
 		ResultSet results = DatabaseConnection.fetchData(query);
 		ArrayList<Customer> customerList = new ArrayList<Customer>();
 		try {
 			customerList = generateCustomerList(results);
 			return customerList;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showSearchCustomersFailedMessage();
 		}
 		return null;
 	}
 	
 	public static ArrayList<Dish> getAllActiveDishes() {
 		ResultSet results = DatabaseConnection.fetchData("SELECT * FROM Dishes WHERE (isactive=1);");
 		ArrayList<Dish> dishList = new ArrayList<Dish>();
 		try {
 			dishList = generateDishList(results);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllDishesFailedMessage();
 		}
 		return dishList;
 	}
 	
 	public static ArrayList<Dish> getAllDishes() {
 		ResultSet results = DatabaseConnection.fetchData("SELECT * FROM Dishes;");
 		ArrayList<Dish> dishList = new ArrayList<Dish>();
 		try {
 			dishList = generateDishList(results);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllDishesFailedMessage();
 		}
 		return dishList;
 	}
 	
 	public static ArrayList<Extra> getAllActiveExtras() {
 		ResultSet results = DatabaseConnection.fetchData("SELECT * FROM Extras WHERE (isactive=1);");
 		ArrayList<Extra> dishList = new ArrayList<Extra>();
 		try {
 			dishList = generateExtrasList(results);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllExtrasFailedMessage();
 		}
 		return dishList;
 	}
 	
 	public static ArrayList<Extra> getAllExtras() {
 		ResultSet results = DatabaseConnection.fetchData("SELECT * FROM Extras;");
 		ArrayList<Extra> dishList = new ArrayList<Extra>();
 		try {
 			dishList = generateExtrasList(results);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllExtrasFailedMessage();
 		}
 		return dishList;
 	}
 	
 	public static ArrayList<Order> getAllUncookedOrders() {
 		ResultSet result = DatabaseConnection.fetchData(getOrderSelectionQuery("Orders.OrdersStatus = '"+Order.REGISTERED+"' OR Orders.OrdersStatus = '"+Order.BEING_COOKED+"'", ""));
 		ArrayList<Order> orderList = new ArrayList<Order>();
 		try {
 			orderList = generateOrderListFromResultSet(result);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllUncookedOrdersFailedMessage();
 		}
 		return orderList;
 	}
 	
 	private static String generateCustomerSearchQuery(String searchQuery) {
 		String query = "SELECT * FROM Customer LEFT JOIN CustomerNotes ON (Customer.commentID = CustomerNotes.NoteID) WHERE (";
 		String[] keywords = searchQuery.split(" ");
 		int counter = 0;
 		for(String keyword : keywords){
 			if(counter != 0){
 				query += "OR";
 			}
 			query += "(Customer.FirstName LIKE '%"+keyword+"%') OR ";
 			query += "(Customer.LastName LIKE '%"+keyword+"%') OR ";
 			query += "(Customer.TelephoneNumber LIKE '%"+keyword+"%')";
 			counter++;
 		}
 		query += ") LIMIT 30;";
 		return query;
 	}
 
 	private static ArrayList<Customer> generateCustomerList(ResultSet results) throws SQLException {
 		ArrayList<Customer> customerList = new ArrayList<Customer>();
 		Customer currentCustomer;
 		while(results.next()){
 			currentCustomer = createCustomer(results, 1, 9);
 			customerList.add(currentCustomer);
 		}
 		return customerList;
 	}
 
 	private static ArrayList<Dish> generateDishList(ResultSet results) throws SQLException {
 		ArrayList<Dish> dishList = new ArrayList<Dish>();
 		Dish currentDish;
 		while(results.next()){
 			currentDish = createDish(results, 1);	
 			dishList.add(currentDish);
 		}
 		return dishList;
 	}
 
 	private static ArrayList<Extra> generateExtrasList(ResultSet results) throws SQLException {
 		ArrayList<Extra> extrasList = new ArrayList<Extra>();
 		Extra currentExtra;
 		while(results.next()){
 			currentExtra = createExtra(results, 1);
 			extrasList.add(currentExtra);
 		}
 		return extrasList;
 	}
 
 	private static ArrayList<Order> generateOrderListFromResultSet(ResultSet result) throws SQLException{
 		System.out.println("generating order list");
 		ArrayList<Order> orderList = new ArrayList<Order>();
 		//order has dummy parameters due to java not liking uninitialized variables. 
 		//The variable will instantiate in the first loop; this one should therefore disappear
 		Order currentOrder = new Order(-1, null, null, "", "", "");
 		OrderDish currentOrderDish = new OrderDish(-1, null);
 		Customer currentCustomer;
 		Dish currentDish;
 		Extra currentExtra;
 		int currentOrderID = -1;
 		int currentOrderContentID = -1;
 		while(result.next()){
 			if(result.getInt(1) != currentOrderID){
 				currentOrderID = result.getInt(1);
 				currentCustomer = createCustomer(result, 1, 9);
 				currentOrder = createOrder(result, currentCustomer, 27, 25);
 				orderList.add(currentOrder);
 			}
 			if(result.getInt(33) != currentOrderContentID){
 				currentOrderContentID = result.getInt(33);
				currentDish = createDish(result, 11);
 				currentOrderDish = new OrderDish(currentOrder.orderID, currentDish);
 				currentOrder.addOrderDish(currentOrderDish);
 			}
			currentExtra = createExtra(result, 21);
 			currentOrderDish.addExtra(currentExtra);
 		}
 		return orderList;
 	}
 	
 	public static ArrayList<Order> getOrdersByKeywords(String keywordString, String[] orderStatusStringList) {
 		String whereClause = generateOrderSearchWhereClause(keywordString, orderStatusStringList);
 		String query = getOrderSelectionQuery(whereClause, "LIMIT 30");
 		ResultSet results = DatabaseConnection.fetchData(query);
 		ArrayList<Order> orderList = new ArrayList<Order>();
 		try {
 			orderList = generateOrderListFromResultSet(results);
 			return orderList;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showSearchOrdersFailedMessage();
 		}
 		return null;
 	}
 	
 	private static String generateOrderSearchWhereClause(String keywordString, String[] orderStatus){
 		String[] keywords = keywordString.split(" ");
 		String whereClause = "(";
 		int counter = 0;
 		for(String status : orderStatus){
 			if(counter != 0){
 				whereClause += " OR ";
 			}
 			
 			counter++;
 			whereClause += "(Orders.OrdersStatus = '"+status+"')";
 		}
 		
 		whereClause += ") AND (";
 		counter = 0;
 		for(String keyword : keywords){
 			if(counter != 0){
 				whereClause += "OR ";
 			}
 			whereClause += "(Orders.OrdersStatus LIKE '%"+keyword+"%') OR ";
 			whereClause += "(Orders.DeliveryMethod LIKE '%"+keyword+"%') OR ";
 			whereClause += "(Orders.OrdersID LIKE '%"+keyword+"%') ";
 			counter++;
 		}
 		whereClause += ")";
 		return whereClause;
 	}
 	
 	private static String getOrderSelectionQuery(String whereClause, String extraOptions){
 		String query = "SELECT Customer.* , CustomerNotes.* , Dishes.* , Extras.* , OrderComments.* , Orders.*, OrdersContents.* FROM Orders " +
 		"LEFT JOIN OrderComments ON ( Orders.CommentID = OrderComments.CommentID ) " +
 		"INNER JOIN OrdersContents ON ( Orders.OrdersID = OrdersContents.OrdersID ) " +
 		"INNER JOIN DishExtrasChosen ON ( OrdersContents.OrdersContentsID = DishExtrasChosen.OrdersContentsID ) " +
 		"LEFT JOIN Customer ON ( Orders.CustomerID = Customer.CustomerID ) " +
 		"LEFT JOIN CustomerNotes ON ( Customer.CommentID = CustomerNotes.NoteID ) " +
 		"LEFT JOIN Dishes ON ( OrdersContents.DishID = Dishes.DishID ) " +
 		"LEFT JOIN Extras ON ( Extras.ExtrasID = DishExtrasChosen.DishExtraID ) " +
 		"WHERE (" + whereClause + ") "+extraOptions+" ;"; 
 		return query;
 	}
 	
 	public static ArrayList<Order> getAllUndeliveredOrders() {
 		ResultSet result = DatabaseConnection.fetchData(getOrderSelectionQuery("Orders.OrdersStatus = '"+Order.HAS_BEEN_COOKED+"' OR Orders.OrdersStatus = '"+Order.BEING_DELIVERED+"'", ""));
 		ArrayList<Order> orderList = new ArrayList<Order>();
 		try {
 			orderList = generateOrderListFromResultSet(result);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			DatabaseResultsFeedbackProvider.showGetAllUndeliveredOrdersFailedMessage();
 		}
 		return orderList;
 	}
 	
 	public static ArrayList<Order> getUndeliveredOrdersByKeywords(String searchQuery) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	private static Customer createCustomer(ResultSet resultSet, int customerTableColumnOffset, int customerNotesTableColumnOffset) throws SQLException{
 		int customerID = resultSet.getInt(customerTableColumnOffset + 0);
 		String firstName = resultSet.getString(customerTableColumnOffset + 1);
 		String lastName = resultSet.getString(customerTableColumnOffset + 2);
 		String address = resultSet.getString(customerTableColumnOffset + 3);
 		int postalCode = resultSet.getInt(customerTableColumnOffset + 4);
 		String city = resultSet.getString(customerTableColumnOffset + 5);
 		int phoneNumber = resultSet.getInt(customerTableColumnOffset + 6);
 		String comment = resultSet.getString(customerNotesTableColumnOffset + 1);
 		Customer customer = new Customer(customerID, firstName, lastName, address, postalCode, city, phoneNumber, comment);
 		return customer;
 	}
 	
 	private static Dish createDish(ResultSet resultSet, int columnOffset) throws SQLException{
 		int dishID = resultSet.getInt(columnOffset + 0);
 		int price = resultSet.getInt(columnOffset + 1);
 		String name = resultSet.getString(columnOffset + 2);
 		boolean containsGluten = resultSet.getBoolean(columnOffset + 3);
 		boolean containsNuts = resultSet.getBoolean(columnOffset + 4);
 		boolean containsDairy = resultSet.getBoolean(columnOffset + 5);
 		boolean isVegetarian = resultSet.getBoolean(columnOffset + 6);
 		boolean isSpicy = resultSet.getBoolean(columnOffset + 7);
 		String description = resultSet.getString(columnOffset + 8);
 		boolean isActive = resultSet.getBoolean(columnOffset + 9);
 		Dish dish = new Dish(dishID, price, name, containsGluten, containsNuts, containsDairy, isVegetarian, isSpicy, description, isActive);
 		return dish;
 	}
 	
 	private static Extra createExtra(ResultSet resultSet, int columnOffset) throws SQLException{
 		int extrasID = resultSet.getInt(columnOffset + 0);
 		String name = resultSet.getString(columnOffset + 1);
 		String price = resultSet.getString(columnOffset + 2);
 		boolean isActive = resultSet.getBoolean(columnOffset + 3);
 		Extra extra = new Extra(extrasID, name, price, isActive);
 		return extra;
 	}
 	
 	private static Order createOrder(ResultSet resultSet, Customer customer, int orderTableColumnOffset, int orderCommentsTableColumnOffset) throws SQLException{
		int orderID = resultSet.getInt(orderTableColumnOffset + 0);
		String timeRegistered = resultSet.getString(orderTableColumnOffset + 2);
		String orderStatus = resultSet.getString(orderTableColumnOffset + 3);
		String deliveryMethod = resultSet.getString(orderTableColumnOffset + 4);
 		String comment = resultSet.getString(orderCommentsTableColumnOffset + 1);
 		Order order = new Order(orderID, customer, timeRegistered, orderStatus, deliveryMethod, comment);
 		return order;
 	}
 
 }//END
