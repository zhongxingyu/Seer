 package model;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * A class for customers.
  * @author Sigurd Lund
  *
  */
 public class Customer {
 
 	private String forename;
 	private String lastname;
 	private String phone;
 	private String address;
 	private String zipCode;
 	private String postalAddress;
 	private int idCustomer;
 	
 	/**
 	 * Creates a new customer.
 	 * @param forename
 	 * @param lastname
 	 * @param phone
 	 * @param adress
 	 * @param postcode
 	 */
 	public Customer(String forename, String lastname, String phone,
 					String address, String zipCode, String postalAddress) {
 		
 		this.forename = forename;
 		this.lastname = lastname;
 		this.phone = phone;
 		this.address = address;
 		this.zipCode = zipCode;
 		this.postalAddress = postalAddress;
 	}
 	
 	/**
 	 * Creates a new Customer with all the variables including the id of the customer in the database.
 	 * This should be used if you need to initialize an already existing customer.
 	 * @param forename
 	 * @param lastname
 	 * @param phone
 	 * @param address
 	 * @param zipCode
 	 * @param postalAddress
 	 * @param idCustomer
 	 */
 	public Customer(String forename, String lastname, String phone,
 					String address, String zipCode, String postalAddress, int idCustomer) {
 		
 		this.forename = forename;
 		this.lastname = lastname;
 		this.phone = phone;
 		this.address = address;
 		this.zipCode = zipCode;
 		this.postalAddress = postalAddress;
 		this.idCustomer = idCustomer;
 	}
 	
 	/**
 	 * Adds the customer to the database.
 	 * @throws SQLException 
 	 */
 	public void addToDatabase() throws SQLException{
 		if (!customerIsUnike(phone)){
 			updateCustomer();
 		}
 		Database db = Database.getDatabase();
 		String query = "INSERT INTO customer (forename, lastname, phone, address, postcode, postaladdress) " +
 		"VALUES ('" + forename + "','" + lastname + "','" + phone + "','"
 		+ address + "','" + zipCode +"','" + postalAddress + "')";
 		
 		idCustomer = db.insertWithIdReturn(query);
 	}
 	
 	/**
 	 * Updates a customer in the database.
 	 * @throws SQLException 
 	 */
 	private void updateCustomer() throws SQLException {
 		Database db = Database.getDatabase();
 		String query = "UPDATE customer SET forename='" + this.forename + "', lastname='" 
 						+ this.lastname + "', address='" + this.address + "', postcode='" 
 						+ this.zipCode + "' WHERE phone='" + this.phone + "'";
 		
 		db.insert(query);
 		ResultSet rs = db.select("SELECT idcustomer from customer WHERE phone = '" + this.phone + "'");
 		if (rs.next()){
 			idCustomer = rs.getInt("idcustomer");
 		}
 	}
 	
 	/**
 	 * Checks that the customer does not already exist in the database.
 	 * A phone number can only have one customer.
 	 * @param phone
 	 * @return
 	 * @throws SQLException 
 	 */
 	private boolean customerIsUnike(String phone) throws SQLException{
 		Database db = Database.getDatabase();
 		String query = "SELECT * FROM customer WHERE phone = '" + phone + "'";
 		
 		ResultSet rs = db.select(query);
 		if (!rs.next()){
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Takes a query in as a String and asks the database for relevant customers to the query.
 	 * The query will be matched on first name, last name and phone number.
 	 * @param query
 	 * @return ArrayList with the customers.
 	 * @throws SQLException 
 	 */
 	public static ArrayList<Customer> getRelevantCustomers(String query) throws SQLException{
 		ArrayList<Customer> customers = new ArrayList<Customer>();
 		Database db = Database.getDatabase();
 		ResultSet rs = db.select("SELECT * FROM customer where forename like '" + query + "%'" +
 								"or lastname like '" + query + "%' or phone like '" + query + "%' ORDER BY forename ASC");
 		while (rs.next()){
 			String forename = rs.getString("forename");
 			String lastname = rs.getString("lastname");
 			String phone = rs.getString("phone");
 			String address = rs.getString("address");
 			String zipCode = rs.getString("postcode");
 			String postalAddress = rs.getString("postaladdress");
 			Customer c = new Customer(forename, lastname, phone, address, zipCode, postalAddress);
 			customers.add(c);
 		}
 		return customers;
 	}
 	
 	/**
 	 * Takes a id of an customer and looks up in the database and check if the customer exist.
 	 * The customer will be returned with all the info from the database.
 	 * @param idCustomer
 	 * @return customer
 	 * @throws SQLException 
 	 */
 	public static Customer getCustomerFromOrder(int idCustomer) throws SQLException{
 		Database db = Database.getDatabase();
 			ResultSet rs = db.select("SELECT * FROM customer WHERE idcustomer=" + idCustomer);
 			if (rs.next()){
 				String forename = rs.getString("forename");
 				String lastname = rs.getString("lastname");
 				String phone = rs.getString("phone");
 				String address = rs.getString("address");
 				String zipCode = rs.getString("postcode");
 				String postalAddress = rs.getString("postaladdress");
 				Customer c = new Customer(forename, lastname, phone, address, zipCode, postalAddress, idCustomer);
 				return c;
 			}
 		return null;
 	}
 	
 	/**
 	 * Returns the forename of a customer.
 	 * @return forename
 	 */
 	public String getForename() {
 		return forename;
 	}
 	
 	/**
 	 * Returns the last name of a customer. 
 	 * @return last name
 	 */
 	public String getLastname() {
 		return lastname;
 	}
 	
 	/**
 	 * Returns the phone number of a customer.
 	 * @return phone number
 	 */
 	public String getPhone() {
 		return phone;
 	}
 
 	/**
 	 * Returns the address of a customer.
 	 * @return address
 	 */
 	public String getAddress() {
 		return address;
 	}
 
 	/**
 	 * Returns the zip code of a customer
 	 * @return
 	 */
 	public String getzipCode() {
 		return zipCode;
 	}
 
 	/**
 	 * Returns the id of a customer.
 	 * @return idCustomer
 	 */
 	public int getIdCustomer(){
 		return this.idCustomer;
 	}
 	
 	/**
 	 * Returns the postal address of a customer.
 	 * @return postal address
 	 */
 	public String getPostalAddress(){
 		return postalAddress;
 	}
 	
 	/**
 	 * Returns the whole name of a customer.
 	 * @return name
 	 */
 	public String getName(){
 		return forename + " " + lastname;
 	}
 	
 }
