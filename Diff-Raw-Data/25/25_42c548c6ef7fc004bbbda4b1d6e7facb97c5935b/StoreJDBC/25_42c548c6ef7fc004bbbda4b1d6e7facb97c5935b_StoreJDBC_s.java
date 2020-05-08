 package com.pasadya.data;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.pasadya.data.util.MemberConstants;
 import com.pasadya.data.util.PropertiesLoader;
 
 public abstract class StoreJDBC implements IShopDAO, Serializable {
 
 	protected Properties dbProperties;
 	private Logger log = LoggerFactory.getLogger(StoreJDBC.class);
 
 	private static final long serialVersionUID = 7047510507515434310L;
 
 	static List<ItemVO> itemInventory = new ArrayList<ItemVO>();
 
 	public StoreJDBC() {
 		Properties config = PropertiesLoader
 				.loadProperties(PropertiesLoader.CONFIGURATION_PROPERTIES_FILE);
 		String dbPropsFile = config
 				.getProperty(PropertiesLoader.CONFIG_KEY_DAO_PROPS_FILE);
 
 		this.dbProperties = PropertiesLoader.loadProperties(dbPropsFile);
 	}
 
 	public List<ItemVO> getShopItems() {
 		return itemInventory;
 	}
 
 	public List<CategoryVO> getTopCategories() throws SQLException {
 		List<CategoryVO> topCategories = new ArrayList<CategoryVO>();
 
 		Connection connection = null;
 
 		try {
 			connection = this.getConnection();
 			final String query = "SELECT * FROM Categories WHERE parent_category IS NULL";
 			PreparedStatement statement = connection.prepareStatement(query);
 			ResultSet resultSet = statement.executeQuery();
 			resultSet.next();
 
 			resultSet.beforeFirst();
 
 			while (resultSet.next()) {
 				CategoryVO category = new CategoryVO();
 
 				category.setCategoryID(resultSet.getInt(1));
 				category.setCategoryName(resultSet.getString(2));
 				category.setParentCategory(resultSet.getInt(3));
 				category.setCategoryDescription(resultSet.getString(4));
 
 				topCategories.add(category);
 				System.out.println(resultSet.getString(2));
 			}
 			resultSet.close();
 
 		} catch (SQLException e) {
 
 		} finally {
 			catchAndClose(connection);
 		}
 		return topCategories;
 	}
 
 	public List<CategoryVO> getChildrenCategoriesByParentID(int id) {
 		List<CategoryVO> childrenCategories = null;
 		CategoryVO category = null;
 
 		childrenCategories.add(category);
 
 		return childrenCategories;
 
 	}
 
 	public List<ItemVO> getAllShopItems() {
 		List<ItemVO> shopItems = new ArrayList<ItemVO>();
 
 		Connection connection = null;
 
 		try {
 			connection = this.getConnection();
 			System.out.println("Connection Successful");
 			final String query = "SELECT * FROM Inventory";
 			PreparedStatement preparedStatement = connection
 					.prepareStatement(query);
 
 			ResultSet resultSet = preparedStatement.executeQuery();
 
 			resultSet.beforeFirst();
 
 			while (resultSet.next()) {
				System.out.println("Fetching...\n");
 				shopItems.add(this.mapItem(resultSet));
 			}
 			resultSet.close();
 
 		} catch (SQLException e) {
 			System.out.println("MySQL error in getting All items");
 			e.printStackTrace();
 		} finally {
 			catchAndClose(connection);
 		}
 		return shopItems;
 	}
 
 	public List<ItemVO> getShopItemsByCategoryID(int id) {
 
 		List<ItemVO> inventory = new ArrayList<ItemVO>();
 		Connection connection = null;
 		try {
 			connection = this.getConnection();
 			final String query = "SELECT * FROM Inventory WHERE category = ?";
 			PreparedStatement statement = connection.prepareStatement(query);
 			statement.setInt(1, id);
 			ResultSet resultSet = statement.executeQuery();
 			resultSet.next();
 
 			resultSet.beforeFirst();
 
 			while (resultSet.next()) {
 				ItemVO item = new ItemVO();
 				item.setItemName(resultSet.getString(2));
 				item.setItemDescription(resultSet.getString(3));
 				item.setItemPrice(resultSet.getString(4));
 				inventory.add(item);
 			}
 			resultSet.close();
 		} catch (SQLException e) {
 
 		} finally {
 			catchAndClose(connection);
 		}
 		return inventory;
 	}
 
 	public ItemVO getShopItemByID(int id) {
 		ItemVO item = null;
 
 		return item;
 	}
 
 	// Map the result set of the db query to the VO
 	private ItemVO mapItem(ResultSet resultSet) throws SQLException {
 		final ItemVO item = new ItemVO();
 		item.setItemCategory(resultSet.getString("category"));
 		item.setItemDescription(resultSet.getString("description"));
 		item.setItemName(resultSet.getString("name"));
 		item.setItemPrice(resultSet.getString("price"));
 
 		return item;
 	}
 
 	public MemberVO getMemberInformation(String id, Boolean auth) {
 		MemberVO member = new MemberVO();
 		if (id != null && auth == true) {
 			Connection connection = null;
 			try {
 				connection = this.getConnection();
 				final String query = "SELECT * FROM Member WHERE username = ?";
 				PreparedStatement statement = connection
 						.prepareStatement(query);
 				statement.setString(1, id);
 				ResultSet resultSet = statement.executeQuery();
 				resultSet.next();
 
 				resultSet.beforeFirst();
 
 				while (resultSet.next()) {
 					member.setUsername(resultSet
 							.getString(MemberConstants.USERNAME));
 					member.setPassword(resultSet
 							.getString(MemberConstants.PASSWORD));
 					member.setFname(resultSet
 							.getString(MemberConstants.FIRST_NAME));
 					member.setLname(resultSet
 							.getString(MemberConstants.LAST_NAME));
 					member.setCity(resultSet.getString(MemberConstants.CITY));
 					member.setState(resultSet.getString(MemberConstants.STATE));
 					member.setZip(resultSet.getString(MemberConstants.ZIP));
 					member.setAddress(resultSet
 							.getString(MemberConstants.ADDRESS));
 					member.setEmail(resultSet.getString(MemberConstants.EMAIL));
 				}
 				resultSet.close();
 			} catch (SQLException e) {
 				log.error("SQL Exception in retrieving user information");
 			} finally {
 				catchAndClose(connection);
 			}
 
 		} else {
 			log.error("Error logging in user [" + id + "]");
 
 		}
 		return member;
 	}
 
 	public void setMemberInformation(MemberVO member) {
 		Connection connection = null;
 		try {
 			System.out.println("*********************************");
 			System.out.println(member.getFname());
 			connection = this.getConnection();
 			final String insertQuery = "INSERT INTO Member (username, fname, lname, password, email) values (?, ?, ?, ?, ?) ";
 
 			PreparedStatement preparedStatement = connection
 					.prepareStatement(insertQuery);
 			preparedStatement.setString(1, member.getUsername());
 			preparedStatement.setString(2, member.getFname());
 			preparedStatement.setString(3, member.getLname());
 			preparedStatement.setString(4, member.getPassword());
 			preparedStatement.setString(5, member.getEmail());
 
 			int i = preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			log.error("Error adding new member!");
 		} finally {
 			catchAndClose(connection);
 		}
 
 	}
 
 	public boolean checkMemberEmail(String email) {
 
 		Connection connection = null;
 		boolean foundResult = false;
 		try {
 			connection = this.getConnection();
 			final String usernameQuery = "SELECT email From Member where email = ?";
 			PreparedStatement preparedStatment = connection
 					.prepareStatement(usernameQuery);
 			preparedStatment.setString(1, email);
 
 			ResultSet resultSet = preparedStatment.executeQuery();
 
 			if (resultSet.next()) {
 				foundResult = true;
 			}
 		} catch (SQLException e) {
 
 		} finally {
 			catchAndClose(connection);
 
 		}
 		return foundResult;
 
 	}
 
 	public boolean checkMemberUsername(String username) {
 		boolean foundResult = false;
 		Connection connection = null;
 
 		try {
 			connection = this.getConnection();
 			final String emailQuery = "SELECT username From Member where username = ?";
 			PreparedStatement secondStatment = connection
 					.prepareStatement(emailQuery);
 			secondStatment.setString(1, username);
 
 			ResultSet emailResult = secondStatment.executeQuery();
 
 			while (emailResult.next()) {
 				foundResult = true;
 			}
 		} catch (SQLException e) {
 			// TODO: handle exception
 		} finally {
 			catchAndClose(connection);
 		}
 
 		return foundResult;
 	}
 
 	public boolean authenticateUser(String username, String password) {
 		boolean authenticate = false;
 
 		if (this.getMemberInformation(username, true).getUsername() != null) {
 			if (this.getMemberInformation(username, true).getPassword()
 					.equals(password)) {
 				authenticate = true;
 			}
 		}
 		return authenticate;
 
 	}
 
 	// Convenience method for closing the database and throwing errors
 
 	private void catchAndClose(Connection conn) {
 		try {
 			if (conn != null && !conn.isClosed()) {
 				conn.close();
 			}
 		} catch (SQLException e) {
 			// Swallow the connection
 		}
 	}
 
 	// Abstract method for getting the database Conneciton
 	protected abstract Connection getConnection() throws SQLException;
 
 }
