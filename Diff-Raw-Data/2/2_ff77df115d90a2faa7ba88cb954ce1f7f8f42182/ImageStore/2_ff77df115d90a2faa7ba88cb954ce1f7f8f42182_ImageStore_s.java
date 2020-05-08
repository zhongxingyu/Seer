 package dal.admin;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 /**
  * 
  * This class is responsible for inserting and retrieving images from our
  * database
  * 
  * @author Stian Sandve / Morten Wærsland
  * 
  */
 
 public class ImageStore implements IImageStore {
 	protected Connection conn;
 	SimpleDateFormat dateformat;
 
 	public ImageStore(Connection conn) {
 		this.conn = conn;
 		this.dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	}
 
 	public synchronized boolean insert(Image img) {
 		try {
 
 			PreparedStatement statement = conn.prepareStatement(
 				"INSERT IGNORE INTO images " +
				" (url, external_id, description, keyword created_time)" +
 				" VALUES (?, ?, ?, ?, ?);");
 
 			statement.setString(1, img.getUrl());
 			statement.setLong(2, img.getID());
 			statement.setString(3, StringUtils.removeEmojis(img.getDescription()));
 			statement.setString(4, dateformat.format(img.getCreatedTime()));
 			return statement.executeUpdate() == 1;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public synchronized ArrayList<Image> getLast(int numberOfRows) {
 		if (numberOfRows < 0) {
 			throw new IllegalArgumentException();
 		}
 
 		ArrayList<Image> images = new ArrayList<Image>();
 
 		try {
 			PreparedStatement statement = conn.prepareStatement(
 				"SELECT id, url, external_id, description, keyword, created_time" +
 				" FROM images WHERE blocked='0'"+
 				" ORDER BY created_time DESC LIMIT ?;");
 			statement.setInt(1, numberOfRows);
 			
 			ResultSet result = statement.executeQuery();
 			while (result.next()) {
 				Image img = new Image(
 						result.getString("url"),
 						result.getLong("external_id"),
 						result.getString("description"),
 						result.getString("keyword"),
 						result.getDate("created_time")
 				);
 				img.setInternalId(result.getInt("id"));
 				images.add(img);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return images;
 	}
 
 	public synchronized boolean block(long external_id) {
 		// int external_id = img.getID();
 		try {
 			PreparedStatement statement = conn
 					.prepareStatement("UPDATE images SET blocked=true WHERE external_id="
 							+ external_id + ";");
 			return statement.executeUpdate() == 1;
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public synchronized boolean unBlock(long external_id) {
 		try {
 			PreparedStatement statement = conn
 					.prepareStatement("UPDATE images SET blocked=false"
 							+ " WHERE external_id=" + external_id + ";");
 			return statement.executeUpdate() == 1;
 
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 	}
 
 }
