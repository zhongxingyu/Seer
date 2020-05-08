 package dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import domain.Customer;
 
 /**
  * DAO class for customer entity
  * 
  * @author andrey
  * 
  */
 public class CustomerDAO {
 	
 	private static final String CONN_URL = "jdbc:mysql://localhost:3306/"
 			+ "simple_registration?user=root&password=root";
 	private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
 	
 	private static final String SELECT_BY_USERNAME = "select `username`, `name`,"
 			+ " `surname`, `sex` from `customers` where `username` = ?";
 	private static final String UPDATE_BY_USERNAME = "update customers set `name` = ?,"
 			+ "`surname` = ?, `sex` = ? where `username` = ?";
 	private static final String INSERT_BY_USERNAME = "insert into customers ( `username`,  `name`,"
 			+ "`surname`, `sex`) values ( ?, ?, ?, ?)";
 
 	private Connection conn = null;
 
 	/**
 	 * get Customer by username
 	 * @param username
 	 * @return
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	synchronized public Customer getCustomer(String username) throws SQLException, ClassNotFoundException {
 		PreparedStatement preparedStatement = getConnection().prepareStatement(SELECT_BY_USERNAME);
 		preparedStatement.setString( 1, username);
 		ResultSet rs = preparedStatement.executeQuery();
 		return writeResultSet(rs);
 	}
 
 	/**
 	 * Save or edit Customer. Detects by username
 	 * @param customer
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	synchronized public void editOrSaveCustomer(Customer customer) throws SQLException, ClassNotFoundException {
 		Customer persistedCustomer = getCustomer(customer.getUsername());
 		if ( persistedCustomer != null){
 			PreparedStatement preparedStatement = getConnection().prepareStatement(
 					UPDATE_BY_USERNAME);
 			preparedStatement.setString(1, customer.getName());
 			preparedStatement.setString(2, customer.getSurname());
 			preparedStatement.setString(3, customer.getSex());
 			preparedStatement.setString(4, persistedCustomer.getUsername());
 			preparedStatement.executeUpdate();
 		} else {
 			PreparedStatement preparedStatement = getConnection().prepareStatement(
 					INSERT_BY_USERNAME);
 			preparedStatement.setString(1, customer.getUsername());
 			preparedStatement.setString(2, customer.getName());
			preparedStatement.setString(3, customer.getUsername());
 			preparedStatement.setString(4, customer.getSex());
 			preparedStatement.executeUpdate();
 		}
 		
 	}
 
 	/**
 	 * Convert result set to Customer
 	 * @param resultSet
 	 * @return
 	 * @throws SQLException
 	 */
 	private Customer writeResultSet(ResultSet resultSet) throws SQLException {
 		Customer customer = null;
 		while (resultSet.next()) {
 			customer = new Customer();
 			customer.setUsername(resultSet.getString("username"));
 			customer.setName(resultSet.getString("name"));
 			customer.setSurname(resultSet.getString("surname"));
 			customer.setSex(resultSet.getString("sex"));
 		}
 		return customer;
 	}
 
 	/**
 	 * Get singleton Connection object
 	 * @return
 	 * @throws SQLException
 	 * @throws ClassNotFoundException
 	 */
 	synchronized private Connection getConnection() throws SQLException,
 			ClassNotFoundException {
 		if (conn == null) {
 			Class.forName(MYSQL_DRIVER);
 			conn = DriverManager.getConnection(CONN_URL);
 		}
 		return conn;
 	}
 
 	/**
 	 * try to close connection
 	 */
 	@Override
 	protected void finalize() throws Throwable {
 		conn.close();
 		super.finalize();
 	}
 
 }
