 package nl.ru.cs.irma.irmawriter;
 
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	private static final int IMAGE_SIZE = 158;
 
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
				BufferedImage scaledPhoto = resizeImage(photo);
				cards.add(new Card(userID, name, email, scaledPhoto, cardId));
 			}
 		}
 		finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 		}
 		
 		return cards;
 	}

	private static BufferedImage resizeImage(BufferedImage photo) {
		int maxDimension = Math.max(photo.getWidth(), photo.getHeight());
		double scale = IMAGE_SIZE / (double) maxDimension;
		int newWidth = (int)(scale*photo.getWidth());
		int newHeight = (int)(scale*photo.getHeight());
		BufferedImage scaledPhoto = new BufferedImage(newWidth, newHeight, photo.getType());
		Graphics2D g = scaledPhoto.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(photo, 0, 0, newWidth, newHeight, null);
		g.dispose();
		return scaledPhoto;
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
