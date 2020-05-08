 package Data;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class Statistics {
 	
 	
 	/** 
 	 * This updates the statistics in the database for a user
 	 * @param user
 	 * @param newGameWinnings
 	 * @param gameWon
 	 * @return
 	 * @author Peter, mouhyi
 	 * @throws SQLException 
 	 */
 	
 	public static int updateUserStatistics(int userId, double newGameWinnings, boolean gameWon) throws SQLException {
 		int updated = -1;
 		PreparedStatement pstmt = null;
 
 		// return -1 if the user does not exist
 		if (!UserData.exists(userId))
 			return -1;
 		
 
 
 		try {
 			Connection con = Methods.connectToDB("5CARD");
 			String query = "UPDATE 'Players' SET gameWinnings=? wins=?, losses=?"
 					+ " WHERE u_id=?";
 			pstmt = con.prepareStatement(query);
 			
 			double [] stats = getPlayerStats(userId);
 			
 			
 			pstmt.setDouble(1, stats[0]+newGameWinnings);
 			if(gameWon){
 				pstmt.setInt(2, (int)stats[1]+1);
 			}
 			if(!gameWon){
 				pstmt.setInt(3, (int) stats[2]+1);
 			}
 			pstmt.setInt(4, userId);
 
 			System.out.println(pstmt.toString());
 
 			updated = pstmt.executeUpdate();
 
 		} catch (SQLException e) {
 			Methods.printSQLException(e);
 		} finally {
 			if (pstmt != null) {
 				pstmt.close();
 			}
 			return updated;
 		}
 	}
 	/**
 	 * This generates an array of Strings that is the leaderboard of top 25, ordered by game winnings
 	 * @return
 	 * @author Peter
 	 */
 	public static String[][] createLeaderBoard() throws SQLException{
 		Statement stmt = null;
 		int i=0;
 		String[][] leaderboard = new String[20][5];
 		try {
 			Connection con = Methods.connectToDB("5CARD");
 			String query = "SELECT * FROM Players ORDER BY gameWinnings DESC LIMIT 20";
 			stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				leaderboard[i][0]=rs.getString("u_id");
 				leaderboard[i][1]=rs.getString("gameWinnings");
 				leaderboard[i][2]=rs.getString("wins");
 				leaderboard[i][3]=rs.getString("losses");
 				leaderboard[i][4]="Great!";
 				++i;
 			}
 
 		} catch (SQLException e) {
 			Methods.printSQLException(e);
 		} finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 			return leaderboard;
 		}
 		
 	}
 	
 	/**
 	 * Retrieves a row of the 'players' table
 	 * @param userId
 	 * @return
 	 * @throws SQLException
 	 * @author mouhyi
 	 */
 	public static double[] getPlayerStats(int userId) throws SQLException{
 		Statement stmt = null;
 		double [] stats = new double[3];
 		try {
 			Connection con = Methods.connectToDB("5CARD");
 			String query = "SELECT * FROM 5Card.Players WHERE u_id='"+userId+"'";
 			stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 			if (rs.next()) {
 				stats[0] = rs.getDouble("GameWinnings");
				stats[1] = rs.getInt("wons");
 				stats[2] = rs.getInt("losses");
 			}
 		}catch (SQLException e) {
 			Methods.printSQLException(e);
 		} finally {
 			if (stmt != null) {
 				stmt.close();
 			}
 			return stats;
 		}
 	}
 	
 }
 	
