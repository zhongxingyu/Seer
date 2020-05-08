 package pizzaProgram.core;
 
 public class DatabaseConstants {
 	public static final String CUSTOMER_ID = "Customer.CustomerID";
 	public static final String FIRST_NAME = "Customer.FirstName";
 	public static final String LAST_NAME = "Customer.LastName";
 	public static final String ADDRESS = "Customer.Address";
 	public static final String POSTAL_CODE = "Customer.PostalCode";
 	public static final String CITY = "Customer.City";
 	public static final String PHONE_NUMBER = "Customer.TelephoneNumber";
 	public static final String NOTE_ID = "Customer.CommentID";
 	public static final String IDENTIFIER = "Customer.Identifier";
 	public static final String CUSTOMER_NOTE = "CustomerNotes.Note";
 	
 	public static final String DISH_ID = "Dishes.DishID";
 	public static final String DISH_PRICE = "Dishes.Price";
 	public static final String DISH_NAME = "Dishes.Name";
 	public static final String CONTAINS_GLUTEN = "Dishes.ContainsGluten";
 	public static final String CONTAINS_NUTS = "Dishes.ContainsNuts";
 	public static final String CONTAINS_DAIRY = "Dishes.ContainsDairy";
 	public static final String IS_VEGETARIAN = "Dishes.IsVegetarian";
 	public static final String IS_SPICY = "Dishes.IsSpicy";
 	public static final String DISH_DESCRIPTION = "Dishes.Description";
 	public static final String DISH_IS_ACTIVE = "Dishes.isActive";
 	
 	public static final String EXTRAS_ID = "Extras.ExtrasID";
 	public static final String EXTRAS_NAME = "Extras.Name";
 	public static final String EXTRAS_PRICE = "Extras.Price";
 	public static final String EXTRAS_IS_ACTIVE = "Extras.isActive";
 	
 	public static final String ORDERS_ID = "Orders.OrdersID";
 	public static final String ORDERS_TIME_REGISTERED = "Orders.TimeRegistered";
 	public static final String ORDERS_STATUS = "Orders.OrdersStatus";
 	public static final String ORDERS_DELIVERY_METHOD = "Orders.DeliveryMethod";
 	public static final String ORDERS_COMMENT = "OrderComments.Comment";
	
	public static final String CONFIG_KEY = "Config.ConfigKey";
	public static final String CONFIG_VALUE = "Config.ConfigValue";
 }
