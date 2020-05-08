 package rezend.musix.core;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import rezend.musix.constants.ErrorCodes;
 
 /**
  * This class holds all transactions to the MySQL database, including the
  * connection itself. It contains the minimum set of operations to add and
  * select all products from database. For more complex systems, other ORMs and
  * persistence solutions could be explored.
  * 
  * This DAO assumes that the proper permissions were already given to the
  * specified user for the given database.
  * 
  * @author Rafael Rezende
  * 
  */
 public class ProductDAO {
 
 	/** MySQL driver */
 	private final static String DRIVER = "com.mysql.jdbc.Driver";
 	/** MySQL URL */
 	private final static String URL = "jdbc:mysql://localhost/ecomm_schema";
 	/** MySQL username */
 	private final static String USERNAME = "ecomm";
 	/** MySQL password */
 	private final static String PASSWORD = "ecomm";
 	/** Unique connection to the MySQL database */
 	private static Connection connection = null;
 
 	/**
 	 * Serialize object content into DB. Insert operations for an existing key
 	 * will update product name and price (ON DUPLICATE KEY)
 	 */
 	private static final String ECOMM_INSERT = "INSERT INTO ecomm_schema.products(id, name, price) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE price=VALUES(price), name=VALUES(name)";
 	/**
 	 * Deserialize object from DB. This query selects all items from the product
 	 * table. New or a more general queries should be created for a dynamic
 	 * request.
 	 */
 	private static final String ECOMM_SELECT = "SELECT id, name, price FROM ecomm_schema.products";
 	/**
 	 * Create table if it does not exist. Warning: User permission required in
 	 * the current database!
 	 */
 	private static final String ECOMM_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ecomm_schema.Products(id INT NOT NULL, name VARCHAR(200) NOT NULL, price FLOAT NOT NULL, PRIMARY KEY (id))";
 
 	/**
 	 * This function attempts to connect to the database if there is no existing
 	 * connection already open.
 	 * 
 	 * @return Successful connection to database.
 	 */
 	public static boolean connect() {
 		// if there is no existing connection, create a new one
 		if (connection == null) {
 			try {
 				// load the MySQL driver
 				Class.forName(DRIVER);
 				// create connection
 				connection = DriverManager.getConnection(URL, USERNAME,
 						PASSWORD);
 			} catch (SQLException | ClassNotFoundException e) {
 				e.printStackTrace();
 				System.err.println(ErrorCodes.DB_CONN_FAILED);
 				return false;
 			}
 			;
 		}
 
 		// attempt to create table when connection is created
 		createProductTable();
 
 		return true;
 	}
 
 	/**
 	 * Create table "products" if it does not exist.
 	 */
 	public static void createProductTable() {
 
 		try {
 			PreparedStatement statement = connection.prepareStatement(
 					ECOMM_CREATE_TABLE, Statement.RETURN_GENERATED_KEYS);
 			// execute query
 			statement.executeUpdate();
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println(ErrorCodes.CREATE_TABLE_FAILURE);
 		}
 	}
 
 	/**
 	 * Add the parameter Product to database.
 	 * 
 	 * @param prod
 	 *            Product to add to the database.
 	 */
 	public static void addProduct(Product prod) {
 
 		// check if there is an existing connection
 		if (connection == null) {
 			System.err.println(ErrorCodes.NO_CONN_AVAILABLE);
 			return;
 		}
 
 		try {
 			PreparedStatement statement = connection.prepareStatement(
 					ECOMM_INSERT, Statement.RETURN_GENERATED_KEYS);
 
 			// complete statement with Product fields
 			statement.setInt(1, prod.getId());
 			statement.setString(2, prod.getName());
 			statement.setDouble(3, prod.getPrice());
 			// execute query
 			statement.executeUpdate();
 
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println(ErrorCodes.ADD_PRODUCT_FAILURE);
 		}
 	}
 
 	/**
 	 * Fetch all products from the database.
 	 * 
 	 * @return a ResultSet containing all products.
 	 */
 	public static ResultSet fetchProducts() {
 
 		// holds all products from DB
 		ResultSet result = null;
 
 		try {
 			// create the select statement
 			PreparedStatement statement = connection
 					.prepareStatement(ECOMM_SELECT);
 			// execute the statement and store the result
 			result = statement.executeQuery();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println(ErrorCodes.SELECT_PRODUCT_FAILURE);
 		}
 		return result;
 	}
 
 	/**
 	 * Fetchs the products from database and print them to the system output.
 	 */
 	public static void printProducts() {
 
 		// fetch products from DB
 		ResultSet result = fetchProducts();
 
 		// check if the fetching has failed
 		if (result == null) {
 			System.err.println(ErrorCodes.SELECT_PRODUCT_FAILURE);
 			return;
 		}
 
 		try {
 			// print products
 			while (result.next()) {
 				String id = result.getString("id");
 				String name = result.getString("name");
 				String price = result.getString("price");
 				System.out.println("Product id: " + id + " - " + name
 						+ " - price: " + price);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.err.println(ErrorCodes.SELECT_PRODUCT_FAILURE);
 		}
 
 	}
 }
