 /**
  *  Main project file
  *  
  *  @author Andrew Hollenbach <anh7216@rit.edu>
  *  @author Andrew DeVoe <ard5852@rit.edu>
  * 
  * 
  * 
  * 
  */
 
 package steam.dbexplorer;
 
 import java.awt.print.Printable;
 import java.sql.*;
 import java.util.List;
 
 import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
 import com.github.koraktor.steamcondenser.steam.community.GameAchievement;
 import com.github.koraktor.steamcondenser.steam.community.GameStats;
 import com.github.koraktor.steamcondenser.steam.community.SteamId;
 import com.github.koraktor.steamcondenser.steam.community.WebApi;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
			WebApi.setApiKey("STEAM API KEY HERE");
 			
 			Connection con;
 			Statement stmt;
			String url = "jdbc:postgresql://SERVER URL HERE";
 			Class.forName("org.postgresql.Driver");
			con = DriverManager.getConnection(url, "DBUSERNAME","DBPASSWORD");
 			
 			stmt = con.createStatement();
 			
			stmt.execute("select * from player;");
 			SteamId id;
 			List<SteamId> friends;
 			try {
 				//basic steam API code, TEST
 				ResultSet rs = stmt.getResultSet();
 				rs.next();
 				Long n = rs.getLong("steamId");
 				id = SteamId.create(n, true);
 				System.out.println(id.getNickname());
 				System.out.println(id.getMemberSince());
 				try {
 					friends = id.getFriends();
 					System.out.println("Size:"+friends.size());
 					//GameStats stats = id.getGameStats("tf2");
 					//List<GameAchievement> achievements = stats.getAchievements();
 					for (SteamId temp:friends) {
 						try {
 							System.out.println (temp.getSteamId64());
 							SteamId f = SteamId.create(temp.getSteamId64(), true) ;
 							System.out.println("insert into player values ("+f.getSteamId64()+", '"+f.getNickname()+"', '"+f.getCustomUrl()+"', null, null, null);");
 							stmt.executeUpdate("insert into player values ("+f.getSteamId64()+", '"+f.getNickname()+"', '"+f.getCustomUrl()+"', null, null, null);");
 							
 							System.out.println("insert into friend values ("+id.getSteamId64()+", "+f.getSteamId64()+");");
 							stmt.executeUpdate("insert into friend values ("+id.getSteamId64()+", "+f.getSteamId64()+");");
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 			stmt.close();
 			con.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			
 		}
 		System.out.println("Test complete");
 	}
 }
