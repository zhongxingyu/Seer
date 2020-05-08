 package be.noselus.tools;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Map;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 import be.noselus.db.DatabaseHelper;
 
 public class GeocodeTest {
 
 	@Test
 	public void geocode() throws IOException {
 		String value = Geocode.lookup(50.839812952485, 4.31287655636466);
 		Assert.assertEquals("Birminghamstraat 386, 1070 Anderlecht, Belgium", value);
 	}
 	
 	@Test
 	public void reverse_geocode() throws IOException {
 		Map<String, Double> value = Geocode.lookup("Birminghamstraat 386, 1070 Anderlecht, Belgium");
 		Assert.assertEquals(50.839812952485, value.get("latitude"), 0.01);
 		Assert.assertEquals(4.31287655636466, value.get("longitude"), 0.01);
 	}
 	
 	@Test
 	public void runner() throws IOException, SQLException, ClassNotFoundException {
 		Connection db = DatabaseHelper.openConnection(true, false);
 		
 		PreparedStatement update = db.prepareStatement("UPDATE person SET long = ?, lat = ? WHERE id = ?;");
 		
 		PreparedStatement stat = db.prepareStatement("SELECT id, address, postal_code, town FROM person WHERE address IS NOT NULL AND lat IS NULL");
 		stat.execute();
 		
 		while (stat.getResultSet().next()) {
 			int id = stat.getResultSet().getInt("id");
 			String address = stat.getResultSet().getString("address").trim().replace(' ', ' ').replace(',', ' ').replace("\'", " ") + " " + stat.getResultSet().getString("town").replace(' ', ' ').trim();
 			
 			address = address.replace('é', 'e');
 			address = address.replace('è', 'e');
 			address = address.replace('â', 'a');
 			address = address.replace('ö', 'o');
 			
 			try {
 				Thread.sleep(2000);
 				Map<String, Double> geo = Geocode.lookup(address);
 				System.out.println(id);
 				
 				update.setDouble(1, geo.get("longitude"));
 				update.setDouble(2, geo.get("latitude"));
 				update.setInt(3, id);
 				update.execute();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		update.close();
 		stat.close();
 		db.close();
 	}
 	
 }
