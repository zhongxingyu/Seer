 package PizzaPck;
 
 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.concurrent.Delayed;
 import java.util.concurrent.TimeUnit;
 
 import com.trolltech.qt.gui.QMessageBox;
 
 /**
  * This class creates the connection to the database and have all the methods for 
  * executing queries on the database
  * 
  * @author Everyone
  *
  */
 public class DB {
 	//Fields 
 	String driver = "org.gjt.mm.mysql.Driver";
 	String host = "jdbc:mysql://mysql.stud.ntnu.no/batunges_data";
 	String user = "batunges_uber13";
 	String pass = "uber13";
 	Connection connect;
 
 
 	/**
 	 * Creates a connection to the database
 	 * @throws SQLException
 	 * @Throws {@link ClassNotFoundException}
 	 */
 	public DB() {
 		try {
 			//Load driver and connect
 			Class.forName(driver);
 			connect = DriverManager.getConnection(host, user, pass);
 
 		} catch(ClassNotFoundException err){
 			ErrorMessage.databaseError(null,"Failed to load driver"+
 					"\nPlease check your internet connection" );
 		} catch(SQLException sq) {
 			System.out.println("Failed to connect");
 			ErrorMessage.databaseError(null,"Failed to connect"+
 					"\nPlease check your internet connection" );
 		}
 	}
 
 
 
 
 	/**
 	 * Generic insert function for the database. Uses instances of objects
 	 * to build insert queries. The fields and data present in the object is
 	 * used as a template. 
 	 * @throws RuntimeException
 	 * @param obj
 	 */
 	public void insert(Object obj){
 		try{
 
 			Class <? extends Object> clazz = obj.getClass();
 
 			String classname = clazz.getSimpleName();
 
 			StringBuilder sb = new StringBuilder();
 			sb.append("INSERT INTO "+ classname +"(");
 
 			//Get field names
 			Field[] fields = clazz.getDeclaredFields();
 			boolean commaAdded = false;
 			for (Field field : fields) {
 				if(!commaAdded) {
 					commaAdded = true;
 				}
 				else {
 					sb.append(", "); 
 				}
 				sb.append(field.getName());
 			}
 			sb.append(") VALUES(");
 
 			commaAdded = false;
 			//Get field data
 			for(Field field : fields) {
 				String fieldName = field.getName();
 				field.setAccessible(true);
 				Object value = field.get(obj);
 				field.setAccessible(false);
 
 				if(!commaAdded){
 					commaAdded=true;
 				}else{
 					sb.append(", ");
 				}
 				sb.append(parseValue(value));
 			}
 			sb.append(")");
 
 			String query1 = sb.toString();
 
 			connect.createStatement().execute(query1);	

 		}catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	/**
 	 * Create a String object of a inserted Object. Used by the generic
 	 * insert function to manipulate data.
 	 * @param value
 	 * @return
 	 */
 	private Object parseValue(Object value) {
 		return String.format("'%s'", value).toLowerCase();
 	}
 	/**
 	 * C
 	 * @param value
 	 * @return
 	 */
 	private String parseValue(String value) {
 		return String.format("'%s'", value);
 	}
 
 
 	/**
 	 * Retrieve all rows from table products. The data retrieved are packed inside a list of
 	 * string arrays. Each string array contain productID, name, price and 
 	 * Ingredients, in the given order. 
 	 * @throws SQLException
 	 *
 	 * @return {@link LinkedList}
 	 */
 	public LinkedList <String[]> getMenu(){
 		LinkedList <String[]> list = new LinkedList <String[]>();
 		try{
 
 			String query = "SELECT * FROM product";
 			ResultSet rs = connect.createStatement().executeQuery(query);
 			rs.first();
 			boolean running = true;
 			while(running){
 				list.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)});
 				running =rs.next();
 			}		
 			return list;
 
 		} catch(SQLException sq) {
 			ErrorMessage.failMenu(null,"Major issue has occured!"+
 		"\nPlease restart the program.");
 			try {
 				wait(3);
 				System.exit(0);
 			} catch (InterruptedException e) {
 			}
 			
 			System.exit(0);
 
 		}
 		return list;
 	}
 
 	/**
 	 *This is a helper function for the search function
 	 *This function takes the query entered by user and a boolean value, and builds a partial where statement
 	 *based on the query. The additional boolean value, if true, is used to build a partial query based on the customerID.
 	 *The function is able to build query's where the condition is: first name, last name, phone number, customer id or 
 	 *both the first name and the last name.
 	 * @param SearchQuery
 	 * @param customer 
 	 * @return String where statement
 	 * @throws NumberFormatException silently ignores it
 	 */
 	private String determineType(String SearchQuery, boolean customer) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(" WHERE");
 		String[] tokens = SearchQuery.split(" ");
 		boolean correct = true;
 		if(tokens.length == 1) {
 			
 			try {
 				Integer.parseInt(tokens[0]);
 				if(customer) {
 					sb.append(" CustomerID = "+tokens[0]+"");
 					return sb.toString();
 				}
 				else {
 					sb.append(" phone = '"+tokens[0]+"'");
 					return sb.toString();
 				}
 				
 			}catch(NumberFormatException e) {
 				//Silently ignore
 			}
 			
 			sb.append(" first_name = '"+tokens[0].toLowerCase()+"' OR last_name = '"+tokens[0].toLowerCase()+"'");
 			return sb.toString();
 			
 		}
 		else if(tokens.length == 2) {
 			sb.append(" first_name = '"+tokens[0].toLowerCase()+"' and last_name = '"+tokens[1].toLowerCase()+"'");
 			return sb.toString();
 		}
 		else {
 			System.out.println("To many tokens");
 		}
 		
 		return "";
 	}
 	
 	/**
 	 * Multi purpose search function for the customer table. It uses the determineType function to build the Where
 	 * statement. 
 	 * @param SearchQuery
 	 * @param key Boolean if true retrieves the customerID
 	 * @param customer Boolean if true search a after an customer with the given customerID
 	 * @return ArrayList<String[]> contains the retrieved customers or the customerID
 	 * @Throws {@link SQLException}
 	 */
 	public ArrayList<String[]> search(String SearchQuery, boolean key, boolean customer) {
 		ArrayList <String[]> list = new ArrayList<String[]>();
 		String query = "SELECT customerID, first_name, last_name, adress, city, "+
 				"zipcode, phone FROM customer "+determineType(SearchQuery, customer)+"";
 		System.out.println(query);
 		
 		try {
 			ResultSet rs = connect.createStatement().executeQuery(query);
 			if(key) {
 				rs.first();
 				list.add(new String[] {rs.getString(1)});
 				return list;
 			}
 			
 			//return null if no information is found
 			if (!rs.first()){
 				return null; 
 			}
 			boolean running = true;
 			while(running) {
 				String[] data = {
 						rs.getString(2), //fornavn
 						rs.getString(3), //etternavn
 						rs.getString(4), //addresse
 						rs.getString(5), //sted
 						rs.getString(6), // postkode
 						rs.getString(7), //telefon
 						rs.getString(1) // id
 				};
 				list.add(data);
 				running = rs.next();
 			}
 			
 		}catch(SQLException sq ) {
 			sq.printStackTrace();
 		}
 		return list;
 	}
 	
 
 
 	/**
 	 * Updates a customer in the customer table by using the customerID.
 	 * 
 	 * @param customer the new customer
 	 * @param CustomerID the old customer(customer id is located at first position)
 	 * @throws SQLException
 	 */
 	public void update(String[] customer, String customerID){
 		String query = "UPDATE customer SET first_name = '"+customer[0]+"'"+
 				", last_name = '"+customer[1]+"'"+
 				", adress = '"+customer[2]+"'"+
 				", city = '"+customer[3]+"'"+
 				" , zipcode = '"+customer[4]+"'"+
 				", phone = '"+customer[5]+"' WHERE customerID = '"+customerID+"'";
 
 		System.out.println(query);
 		try {
 			connect.createStatement().execute(query);
 		}catch(SQLException sq) {
 			System.out.println("UPDATE: Failed!!");
 			sq.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Retrieve all orders which is finish and not delivered as a arraylist with String[] arrays
 	 * String[] = {orderID, customerID, delivery, finish, 
 	 * delivered, time, currentTime, customerID, firste_name, 
 	 * last_name, address, zipcode, city, phone} 
 	 * 
 	 * @return  ArrayList<String[]>
 	 * @throws {@link SQLException}
 	 */
 	public ArrayList <String[]> getAllDeliveries(){
 		String query = "SELECT * FROM orders WHERE finish ='1' AND delivered = '0'";
 		ArrayList<String[]> list = new ArrayList<String[]>();
 		boolean running = true;
 		try{
 			ResultSet rs = connect.createStatement().executeQuery(query);
 
 			if(!rs.first()) {
 				return null;
 			}
 
 			while(running) {
 				//Encapsulate retrieved data 
 				String[] data = {
 						//from order
 						rs.getString(1), //OrderID
 						rs.getString(2), //customerID
 						rs.getString(3), //delivery
 						rs.getString(4), //finish
 						rs.getString(5), //delivered
 						rs.getTimestamp(6).toString(),//time
 						rs.getTimestamp(7).toString(),  //currentTime
 
 						//from customer
 						"", //customerId
 						"", //first_name
 						"", //last_name
 						"", //adress
 						"", //zipcode
 						"", //city 
 						"" //phone
 
 				};
 
 				list.add(data);
 				//Handle next row
 				running = rs.next();
 			}
 
 
 			for (String[] strings : list) {
 				try {
 					query = "SELECT * FROM customer WHERE customerID = '"+strings[1]+"' ";
 					ResultSet fu = connect.createStatement().executeQuery(query);
 					if(fu.first() == false) {
 						
 					}
 					strings[7] = fu.getString(1);	//customerID
 					strings[8] = fu.getString(2);	//first_name
 					strings[9] = fu.getString(3);	//last_name
 					strings[10] = fu.getString(4);	//adress
 					strings[11] = fu.getString(5);	//zipcode
 					strings[12] = fu.getString(6);	//city
 					strings[13] = fu.getString(7);	//phone
 
 				}catch(SQLException sq) {
 					
 					sq.printStackTrace();
 				}
 			}
 			return list;
 
 		}catch (SQLException e) {
 			// TODO: handle exception
 			System.out.println(e);
 		}
 
 		return null;
 	}
 
 
 	/**
 	 * Retrive all orders from the orders table. The data retrieved are packed
 	 * inside string arrays. Each string array contains orderID, customerID,
 	 * delivery, finish, delivered, time and currentTime, in the given order. 
 	 * 
 	 * @return ArrayList<String[]>
 	 * @throws SQLException
 	 */
 	public ArrayList <String[]> getAllOrders() {
 		String query = "SELECT * FROM orders WHERE finish = '0' ORDER BY currentTime ASC";
 
 		ArrayList<String[]> list = new ArrayList<String[]>();
 		boolean running = true;
 		try {
 			ResultSet rs = connect.createStatement().executeQuery(query);
 
 
 			if(!rs.first()) {
 				return null;
 			}
 
 			while(running) {
 				//Encapsulate retrieved data 
 				String[] data = {
 						rs.getString(1), //OrderID
 						rs.getString(2), //customerID
 						rs.getString(3), //delivery
 						rs.getString(4), //finish
 						rs.getString(5), //delivered
 						rs.getTimestamp(6).toString(),//time
 						rs.getTimestamp(7).toString()  //currentTime
 				};
 				list.add(data);
 				//Handle next row
 				running = rs.next();
 			}
 			return list;
 
 		}catch(SQLException sq) {
 			System.out.println("getAllOrders() failed");
 			sq.printStackTrace();
 		}
 		return null;
 
 	}
 	
 	
 	//TODO: Skrive om denne kommentaren 
 	/**
 	 * Retrieves all rows in table receipt where orderID == orderNr. The data
 	 * Retrieved are packed inside string arrays. Each string array contain orderID, 
 	 * productID, comment, size,amount, pizza name, and ingredients. Pizza name and ingridents are
 	 * Retrieved by performing an additional query, and retrieving the name and ingridents from
 	 * products where productID == productID
 	 * @param orderNr
 	 * @return ArrayList<String[]>
 	 */
 
 	public ArrayList<String[]> displayOrders(String orderNr){
 		ArrayList <String[]> aList = new ArrayList<String[]>();
 		String query = "SELECT * FROM receipt WHERE orderID = '"+orderNr+"'";
 		boolean running = true;
 
 		try {
 			ResultSet rs = connect.createStatement().executeQuery(query);
 			rs.first();
 			while(running) {
 				String[] data = {
 
 						rs.getString(1), //OrderID
 						rs.getString(2), //ProductID
 						rs.getString(3), //Comment 
 						rs.getString(4), //size
 						rs.getString(5), //amount
 						"",				 //Pizza navn
 						""				//ingridienser
 				};
 				aList.add(data);
 				running = rs.next();
 			}
 
 
 
 			for (String[] strings : aList) {
 				try {
 					query = "SELECT * FROM product WHERE productID = '"+strings[1]+"' ";
 					ResultSet fu = connect.createStatement().executeQuery(query);
 					if(fu.first() == false) {		
 					}
 					strings[5] = fu.getString(2);	//name
 					strings[6] = fu.getString(4);	//ingridents
 				}catch(SQLException sq) {
 					sq.printStackTrace();
 				}
 			}
 			return aList;
 		}catch (SQLException sq) {
 			System.out.println("diplayOrder() failed");
 			sq.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public void delete(Object del) {
 
 	}
 
 	/**
 	 * Retrieve productID from product where PizzaName == name. 
 	 * 
 	 * @param string PizzaName
 	 * @return String
 	 * @throws SQLException
 	 */
 	public String getPizzaID(String PizzaName) {
 		String  query = "SELECT productID FROM product WHERE name = '"+PizzaName+"'";
 		try {
 			ResultSet rs = connect.createStatement().executeQuery(query);
 			rs.first();
 			return rs.getString(1);
 		}catch(SQLException sq) {
 			System.out.println("ERROR: getPizzaID()");
 			sq.printStackTrace();
 		}
 		return "";
 	}
 	
 	/**
 	 * Updates the order status to finished
 	 * 
 	 * @param orderID
 	 * @throws SQLException
 	 */
 	public void updateFinishStatus(String orderID){
 		String query = "UPDATE orders SET finish = '1' WHERE orderID = '"+orderID+"'";
 		try{
 			connect.createStatement().execute(query);
 		}
 		catch (SQLException e) {
 			// TODO: handle exception
 			System.out.println(e);
 		}
 	}
 	
 	/**
 	 * Updates the delivered status in order to delivered according to the orderID
 	 *
 	 * @param orderID
 	 * @throws SQLException
 	 */
 	public void updateDeliveredStatus(String orderID){
 		String query = "UPDATE orders SET delivered = '1' WHERE orderID = '"+orderID+"'";
 		try{
 			connect.createStatement().execute(query);
 		}
 		catch (SQLException e) {
 			// TODO: handle exception
 			System.out.println(e);
 		}
 	}
 
 	
 
 	/**
 	 * Retrieve the last inserted orderID from table orders
 	 * @return String
 	 * @throws SQLException
 	 */
 	public String getOrderID() {
 		String query = "SELECT MAX(orderid) FROM orders";
 		try {
 			ResultSet rs = connect.createStatement().executeQuery(query);
 			rs.first();
 			return rs.getString(1);
 		}catch(SQLException sq) {
 			System.out.println("ERROR: getOrderID()");
 			sq.printStackTrace();
 		}
 		return "";
 	}
 
 	
 	
 	/**
 	 *  updates the order with time and if the order is going to be delivered
 	 *  
 	 * @param time
 	 * @param delivery
 	 * @param orderID
 	 */
 	public void updateTime(Timestamp time, int delivery, String orderID) {
 		String query = "UPDATE orders SET time = '"+time.toString()+"'"+
 				", delivery = '"+delivery+"'  WHERE orderID = '"+orderID+"'";
 
 		try {
 			connect.createStatement().execute(query);
 
 		}catch(SQLException sq) {
 			System.out.println("updateTime() failed!!");
 			sq.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * This method receive results from product and receipt tables in 
 	 * the database
 	 * Returns an arraylist of string[] arrays were each string[] consists of 
 	 * { receipt.orderID, receipt.productID, receipt.size, receipt.amount, 
 	 * products.name, products.price}
 	 * 
 	 * @param orderID
 	 * @return
 	 * @throws SQLException
 	 */
 	public ArrayList<String[]> getReceipt(String orderID){
 		String query = "SELECT * FROM receipt WHERE orderID='"+orderID+"'";
 		ArrayList<String[]> products = new ArrayList<String[]>();
 		boolean running = true;
 		ResultSet rs;
 		try {
 			rs = connect.createStatement().executeQuery(query);
 			rs.first();
 			while (running) {
 				String[] data = {
 						rs.getString(1), //orderID
 						rs.getString(2), //productID
 						rs.getString(4), //size
 						rs.getString(5), //	amount
 						"", //name from products table in database
 						"" //price from products table in database
 				};
 				products.add(data);
 				running =rs.next();
 			}
 			
 			for (String[] strings : products) {
 				try {
 					query = "SELECT * FROM product WHERE productID = '"+strings[1]+"'";
					System.out.println(query);
 					rs = connect.createStatement().executeQuery(query);
 					rs.first();
 					strings[4] = rs.getString(2);	//name
 					strings[5] = rs.getString(3);	//price
 				}catch(SQLException sq) {
 					sq.printStackTrace();
 				}
 			}
 			return products;
 			
 		} catch (SQLException e) {
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * Deletes the order in orders where orders.orderID ='orderID'
 	 * Throws an exception if the orderID doesn't exist 
 	 * 
 	 * @throws SQLException
 	 * @param orderID
 	 */
 	public void deleteOrder(String orderID){
 		String query = "DELETE FROM orders WHERE orderID ='"+orderID+"'";
 		
 		try {
 			connect.createStatement().execute(query);
 			
			
 		} catch (SQLException e) {
			// TODO: handle eception
 			e.printStackTrace();
 		}
 	}
 }
 
 
 
 
 
 
 
 
