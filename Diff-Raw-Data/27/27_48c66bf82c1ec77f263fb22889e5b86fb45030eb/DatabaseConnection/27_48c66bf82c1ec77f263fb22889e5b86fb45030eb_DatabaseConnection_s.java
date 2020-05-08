 package nl.ru.cs.irma.irmawriter;
 
 import java.awt.image.BufferedImage;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Properties;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 
 public class DatabaseConnection {
 	private static Connection con = null;
	
 
 	public static Vector<Card> loadFromPrinterCards(Properties config) throws SQLException, IOException{
 		Connection con = null;
 		PreparedStatement stmt = null;
 		Vector<Card> cards = new Vector<Card>();
 		
 		try {
 			con = DriverManager.getConnection(config.getProperty("database_url"), config.getProperty("database_username"), config.getProperty("database_password"));
 			
 			stmt = con.prepareStatement("SELECT eduPersonPrincipalName, givenName, surname, email, photo, cardID " +
 										"FROM PilotParticipants "+
 										"WHERE cardStatus='from printer'");
 			
 			ResultSet result = stmt.executeQuery();
 			while(result.next()) {
 				String userID = result.getString("eduPersonPrincipalName");
 				String name = result.getString("givenName") + " " + result.getString("surname");
 				String email = result.getString("email");
 				int cardId = result.getInt("cardID");
 				BufferedImage photo = ImageIO.read(result.getBinaryStream("photo"));
				cards.add(new Card(userID, name, email, photo, cardId));
 			}
 		}
 		finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 		}
 		
 		return cards;
 	}
 	
 	public static void setCardStatusPersonalized(int cardId, Properties config) throws SQLException, FileNotFoundException, IOException {
 		PreparedStatement stmt = null;
 		
 		try {
 			con = DriverManager.getConnection(config.getProperty("database_url"), config.getProperty("database_username"), config.getProperty("database_password"));
 			con.setAutoCommit(false);
 			
 			stmt = con.prepareStatement("UPDATE PilotParticipants SET cardStatus='personalised' WHERE cardId = ?");
 			stmt.setInt(1, cardId);
 			stmt.execute();
 		}
 		finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 		}
 	}
 	
 	/*private static void loadConfig() throws FileNotFoundException, IOException {
 		if(config == null) {
 			config = new Properties();
 			config.load(new FileInputStream("config.properties"));
 		}
 	}*/
 	
 	public static void rollback() throws SQLException {
 		con.rollback();
 	}
 	
 	public static void commit() throws SQLException {
 		con.commit();
 	}
 }
