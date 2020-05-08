 package ar.edu.itba.paw.grupo1.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.itba.paw.grupo1.model.Property;
 
 public class JDBCPropertyDao extends AbstractDao implements PropertyDao {
 
 	private static Logger logger = Logger.getLogger(JDBCPropertyDao.class);
 
 	public JDBCPropertyDao(Connection conn) {
 		super(conn);
 	}
 
 	public List<Property> getProperties(int userId) {
 
 		List<Property> properties = new ArrayList<Property>();
 		PreparedStatement statement;
 
 		try {
 			statement = conn
 					.prepareStatement("select * from properties where userId = ?");
 			statement.setInt(1, userId);
 			if (statement.execute()) {
 				ResultSet myCursor = statement.getResultSet();
 
 				while (myCursor.next()) {
 					Property property = buildProperty(myCursor);
 					properties.add(property);
 				}
 			}
 			statement.close();
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 		}
 		return properties;
 	}
 
 	public Property get(int id) {
 
 		PreparedStatement statement;
 		Property property = null;
 
 		try {
 			statement = conn
 					.prepareStatement("select * from properties where id = ?");
 			statement.setInt(1, id);
 			if (statement.execute()) {
 				ResultSet myCursor = statement.getResultSet();
 				if (myCursor.next()) {
 
 					property = buildProperty(myCursor);
 				}
 			}
 			statement.close();
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 		}
 
 		return property;
 	}
 
 	public void save(Property property) {
 
 		try {
 			PreparedStatement statement;
 			if (property.isNew()) {
 
 				statement = conn
 						.prepareStatement("INSERT INTO properties (propertytype, operationtype, address,"
 								+ " neighbourhood, price, rooms, indoorspace, outdoorspace, description, antiquity,"
 								+ " cable, phone, pool, lounge, paddle, barbecue, published, userid) "
 								+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 				setPlaceHolders(statement, property);
 
 			} else {
 				statement = conn
 						.prepareStatement("UPDATE properties SET propertyType = ?, operationType = ?, address = ?, "
 								+ "neighbourhood = ?, price = ?, rooms = ?, indoorSpace = ?, outdoorSpace = ?, description = ?, "
 								+ "antiquity = ?, cable = ?, phone = ?, pool = ?, lounge = ?, paddle = ?, barbecue = ?, published = ?, "
 								+ "userId = ? WHERE id = ?");
 				setPlaceHolders(statement, property);
 				statement.setInt(19, property.getId());
 
 			}
 			statement.execute();
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 		}
 	}
 
 	private void setPlaceHolders(PreparedStatement stmt, Property property)
 			throws SQLException {
 
 		stmt.setInt(1, property.getPropertyType());
 		stmt.setInt(2, property.getOperationType());
 		stmt.setString(3, property.getAddress());
 		stmt.setString(4, property.getNeighbourhood());
 		stmt.setDouble(5, property.getPrice());
 		stmt.setInt(6, property.getRooms());
 		stmt.setDouble(7, property.getIndoorSpace());
 		stmt.setDouble(8, property.getOutdoorSpace());
 		stmt.setString(9, property.getDescription());
 		stmt.setInt(10, property.getAntiquity());
 		stmt.setBoolean(11, property.isCable());
 		stmt.setBoolean(12, property.isPhone());
 		stmt.setBoolean(13, property.isPool());
 		stmt.setBoolean(14, property.isLounge());
 		stmt.setBoolean(15, property.isPaddle());
 		stmt.setBoolean(16, property.isBarbecue());
 		stmt.setBoolean(17, property.isPublished());
 		stmt.setInt(18, property.getUserId());
 	}
 
 	private Property buildProperty(ResultSet cursor) throws SQLException {
 
 		int id = cursor.getInt("id");
 		int propertyType = cursor.getInt("propertyType");
 		int operationType = cursor.getInt("operationType");
 		String address = cursor.getString("address");
 		String neighbourhood = cursor.getString("neighbourhood");
 		double price = cursor.getDouble("price");
 		int rooms = cursor.getInt("rooms");
 		double indoorSpace = cursor.getDouble("indoorSpace");
 		double outdoorSpace = cursor.getDouble("outdoorSpace");
 		String description = cursor.getString("description");
 		int antiquity = cursor.getInt("antiquity");
 		boolean published = cursor.getBoolean("published");
 		int userId = cursor.getInt("userId");
 		boolean cable = cursor.getBoolean("cable");
 		boolean phone = cursor.getBoolean("phone");
 		boolean pool = cursor.getBoolean("pool");
 		boolean lounge = cursor.getBoolean("lounge");
 		boolean paddle = cursor.getBoolean("paddle");
 		boolean barbecue = cursor.getBoolean("barbecue");
 
 		return new Property(id, propertyType, operationType, address,
 				neighbourhood, price, rooms, indoorSpace, outdoorSpace,
 				description, antiquity, cable, phone, pool, lounge, paddle,
 				barbecue, published, userId);
 
 	}
 
 	public boolean checkOwnership(Integer userId, Integer propertyId) {
 
 		PreparedStatement statement;
 
 		try {
 			statement = conn
 					.prepareStatement("select * from properties where userId = ? and id = ?");
 			statement.setInt(1, userId);
 			statement.setInt(2, propertyId);
 
 			if (statement.execute()) {
 				ResultSet myCursor = statement.getResultSet();
 
 				if (myCursor.next()) {
 					return true;
 				}
 			}
 			statement.close();
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 		}
 		return false;
 	}
 
 	public int getUser(int id) {
 		int userId = 0;
 
 		PreparedStatement statement;
 
 		try {
 			statement = conn
 					.prepareStatement("select userId from properties where id = ?");
 			statement.setInt(1, id);
 
 			if (statement.execute()) {
 				ResultSet myCursor = statement.getResultSet();
 
 				if (myCursor.next()) {
 					return myCursor.getInt("userId");
 				}
 			}
 			statement.close();
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 		}
 		return userId;
 	}
 
 	public List<Property> query(String operation, String property,
 			double rangeFrom, double rangeTo) {
 
 		String query = "SELECT * FROM properties WHERE ";
 
 		if (operation.equals("selling")) {
 			query += "operationType = 0 AND ";
 		} else if (operation.equals("leasing")) {
 			query += "operationType = 1 AND ";
 		}
 
 		if (property.equals("house")) {
 			query += "propertyType = 0 AND ";
 		} else if (property.equals("flat")) {
 			query += "propertyType = 1 AND ";
 		}
 
 		query += "price >= " + rangeFrom + " AND price <= " + rangeTo;
System.out.println(query);
 		List<Property> properties = new ArrayList<Property>();
 		PreparedStatement statement;
 
 		try {
 			statement = conn.prepareStatement(query);
 
 			if (statement.execute()) {
 				ResultSet myCursor = statement.getResultSet();
 
 				while (myCursor.next()) {
 					Property prop = buildProperty(myCursor);
 					properties.add(prop);
 				}
 			}
 			statement.close();
 
 		} catch (SQLException e) {
 			logger.warn("", e);
 		}
 
 		return properties;
 	}
 }
