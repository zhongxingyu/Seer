 package pizzaProgram.database.databaseUtils;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.xml.crypto.Data;
 
 import pizzaProgram.core.Constants;
 import pizzaProgram.core.DatabaseConstants;
 import pizzaProgram.dataObjects.Customer;
 import pizzaProgram.dataObjects.Dish;
 import pizzaProgram.dataObjects.Extra;
 import pizzaProgram.dataObjects.Order;
 import pizzaProgram.dataObjects.OrderDish;
 import pizzaProgram.dataObjects.Setting;
 
 public class DatabaseDataObjectGenerator {
 
 	public static ArrayList<Dish> generateDishList(ResultSet results) throws SQLException {
 		ArrayList<Dish> dishList = new ArrayList<Dish>();
 		Dish currentDish;
 		while(results.next()){
 			currentDish = DatabaseDataObjectGenerator.createDish(results, 1);	
 			dishList.add(currentDish);
 		}
 		return dishList;
 	}
 
 	public static ArrayList<Customer> generateCustomerList(ResultSet results) throws SQLException {
 		ArrayList<Customer> customerList = new ArrayList<Customer>();
 		Customer currentCustomer;
 		while(results.next()){
 			currentCustomer = DatabaseDataObjectGenerator.createCustomer(results, 1, 9);
 			customerList.add(currentCustomer);
 		}
 		return customerList;
 	}
 
 	public static ArrayList<Extra> generateExtrasList(ResultSet results) throws SQLException {
 		ArrayList<Extra> extrasList = new ArrayList<Extra>();
 		Extra currentExtra;
 		while(results.next()){
 			currentExtra = DatabaseDataObjectGenerator.createExtra(results, 1);
 			extrasList.add(currentExtra);
 		}
 		return extrasList;
 	}
 	
 	public static ArrayList<Setting> generateSettingsList(ResultSet results) throws SQLException
 	{
 		ArrayList<Setting> settingsList = new ArrayList<Setting>();
 		Setting currentSetting;
 		while(results.next()){
 			currentSetting = DatabaseDataObjectGenerator.createSetting(results, 1);
 			settingsList.add(currentSetting);
 		}
 		return settingsList;
 	}
 
 	public static ArrayList<Order> generateOrderListFromResultSet(ResultSet result) throws SQLException{
 		System.out.println("generating order list");
 		ArrayList<Order> orderList = new ArrayList<Order>();
 		//order has dummy parameters due to java not liking uninitialized variables. 
 		//The variable will instantiate in the first loop; this one should therefore disappear
 		
 		//order of tables in incoming query: Customers - CustomerNotes - Dishes - Extras - OrderComments - Orders - OrdersContents
 		int customerTableOffset = 1;
 		int customerNotesTableOffset = customerTableOffset + Constants.CUSTOMERS_TABLE_NUM_COLUMNS;
 		int dishTableOffset = customerNotesTableOffset + Constants.CUSTOMER_NOTES_TABLE_NUM_COLUMNS;
 		int extrasTableOffset = dishTableOffset + Constants.DISHES_TABLE_NUM_COLUMNS;
 		int orderCommentsTableOffset = extrasTableOffset + Constants.EXTRAS_TABLE_NUM_COLUMNS;
 		int orderTableOffset = orderCommentsTableOffset + Constants.ORDER_COMMENTS_TABLE_NUM_COLUMNS;
 		int orderContentsTableOffset = orderTableOffset + Constants.ORDERS_TABLE_NUM_COLUMNS;
 		
 		Order currentOrder = new Order(-1, null, null, "", "", "");
 		OrderDish currentOrderDish = new OrderDish(-1, null);
 		Customer currentCustomer;
 		Dish currentDish;
 		Extra currentExtra;
 		int currentOrderID = -1;
 		int currentOrderContentID = -1;
 		while(result.next()){
 			if(result.getInt(customerTableOffset) != currentOrderID){
 				currentOrderID = result.getInt(customerTableOffset);
 				currentCustomer = DatabaseDataObjectGenerator.createCustomer(result, customerTableOffset, Constants.CUSTOMERS_TABLE_NUM_COLUMNS+1);
 				currentOrder = DatabaseDataObjectGenerator.createOrder(result, currentCustomer, orderTableOffset, orderCommentsTableOffset);
 				orderList.add(currentOrder);
 			}
 			if(result.getInt(orderContentsTableOffset) != currentOrderContentID){
 				currentOrderContentID = result.getInt(orderContentsTableOffset);
 				currentDish = DatabaseDataObjectGenerator.createDish(result, dishTableOffset);
 				currentOrderDish = new OrderDish(currentOrder.orderID, currentDish);
 				currentOrder.addOrderDish(currentOrderDish);
 			}
 			currentExtra = DatabaseDataObjectGenerator.createExtra(result, extrasTableOffset);
 			currentOrderDish.addExtra(currentExtra);
 		}
 		return orderList;
 	}
 
 	static Customer createCustomer(ResultSet resultSet, int customerTableColumnOffset, int customerNotesTableColumnOffset) throws SQLException{
 		int customerID = resultSet.getInt(resultSet.findColumn(DatabaseConstants.CUSTOMER_ID));
 		String firstName = resultSet.getString(resultSet.findColumn(DatabaseConstants.FIRST_NAME));
 		String lastName = resultSet.getString(resultSet.findColumn(DatabaseConstants.LAST_NAME));
 		String address = resultSet.getString(resultSet.findColumn(DatabaseConstants.ADDRESS));
 		String postalCode = resultSet.getString(resultSet.findColumn(DatabaseConstants.POSTAL_CODE));
 		String city = resultSet.getString(resultSet.findColumn(DatabaseConstants.CITY));
 		int phoneNumber = resultSet.getInt(resultSet.findColumn(DatabaseConstants.PHONE_NUMBER));
 		String comment = resultSet.getString(resultSet.findColumn(DatabaseConstants.CUSTOMER_NOTE));
 		Customer customer = new Customer(customerID, firstName, lastName, address, postalCode, city, phoneNumber, comment);
 		return customer;
 	}
 
 	static Dish createDish(ResultSet resultSet, int columnOffset) throws SQLException{
 		int dishID = resultSet.getInt(resultSet.findColumn(DatabaseConstants.DISH_ID));
 		int price = resultSet.getInt(resultSet.findColumn(DatabaseConstants.DISH_PRICE));
 		String name = resultSet.getString(resultSet.findColumn(DatabaseConstants.DISH_NAME));
 		boolean containsGluten = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.CONTAINS_GLUTEN));
 		boolean containsNuts = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.CONTAINS_NUTS));
 		boolean containsDairy = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.CONTAINS_DAIRY));
 		boolean isVegetarian = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.IS_VEGETARIAN));
 		boolean isSpicy = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.IS_SPICY));
 		String description = resultSet.getString(resultSet.findColumn(DatabaseConstants.DISH_DESCRIPTION));
 		boolean isActive = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.DISH_IS_ACTIVE));
 		Dish dish = new Dish(dishID, price, name, containsGluten, containsNuts, containsDairy, isVegetarian, isSpicy, description, isActive);
 		return dish;
 	}
 
 	static Extra createExtra(ResultSet resultSet, int columnOffset) throws SQLException{
 		int extrasID = resultSet.getInt(resultSet.findColumn(DatabaseConstants.EXTRAS_ID));
 		String name = resultSet.getString(resultSet.findColumn(DatabaseConstants.EXTRAS_NAME));
 		String price = resultSet.getString(resultSet.findColumn(DatabaseConstants.EXTRAS_PRICE));
 		boolean isActive = resultSet.getBoolean(resultSet.findColumn(DatabaseConstants.EXTRAS_IS_ACTIVE));
 		Extra extra = new Extra(extrasID, name, price, isActive);
 		return extra;
 	}
 
 	static Order createOrder(ResultSet resultSet, Customer customer, int orderTableColumnOffset, int orderCommentsTableColumnOffset) throws SQLException{
 		int orderID = resultSet.getInt(resultSet.findColumn(DatabaseConstants.ORDERS_ID));
 		String timeRegistered = resultSet.getString(resultSet.findColumn(DatabaseConstants.ORDERS_TIME_REGISTERED));
 		String orderStatus = resultSet.getString(resultSet.findColumn(DatabaseConstants.ORDERS_STATUS));
 		String deliveryMethod = resultSet.getString(resultSet.findColumn(DatabaseConstants.ORDERS_DELIVERY_METHOD));
 		String comment = resultSet.getString(resultSet.findColumn(DatabaseConstants.ORDERS_COMMENT));
 		Order order = new Order(orderID, customer, timeRegistered, orderStatus, deliveryMethod, comment);
 		return order;
 	}
 
 	public static Setting createSetting(ResultSet results, int columnOffset) throws SQLException {
		String configKey = results.getString(results.findColumn(DatabaseConstants.CONFIG_KEY));
		String configValue = results.getString(results.findColumn(DatabaseConstants.CONFIG_VALUE));
 		Setting setting = new Setting(configKey, configValue);
 		return setting;
 	}
 
 }
