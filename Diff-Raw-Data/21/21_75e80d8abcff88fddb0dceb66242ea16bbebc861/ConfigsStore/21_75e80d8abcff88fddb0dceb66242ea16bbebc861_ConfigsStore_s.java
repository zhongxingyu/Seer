 package dal.admin;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Retrieve, add/update and delete configurations in the database
  * @author Stian Sandve <stian@sandve.org>
  */
 public class ConfigsStore implements IConfigsStore {
 	Connection conn;
 
 	public ConfigsStore(Connection conn) {
 		this.conn = conn;
 	}
 
 	@Override
 	public String getConfig(String name) {
 		String value = "";
 		try (PreparedStatement statement = conn.prepareStatement("SELECT value from configs WHERE name = ?;")) {
 			statement.setString(1, name);
 			try (ResultSet r = statement.executeQuery()) {
				r.next();
				value = r.getString("value");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} 
 		return value;
 	}
 
 	@Override
 	public boolean addConfig(String name, String value) {
 		try (PreparedStatement statement = conn.prepareStatement("INSERT INTO configs (name, value)" +
 				" VALUES (?, ?) ON DUPLICATE KEY UPDATE value = ?;")) {
 			statement.setString(1, name);
 			statement.setString(2, value);
 			statement.setString(3, value);
 			return statement.executeUpdate() > 0 ? true : false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public boolean deleteConfig(String name) {
 		try (PreparedStatement statement = conn.prepareStatement("DELETE FROM configs WHERE name = ?")) {
 			statement.setString(1, name);
 			return statement.executeUpdate() > 0 ? true : false;
 		} catch (SQLException e) {
 			return false;
 		}
 	}	
 }
