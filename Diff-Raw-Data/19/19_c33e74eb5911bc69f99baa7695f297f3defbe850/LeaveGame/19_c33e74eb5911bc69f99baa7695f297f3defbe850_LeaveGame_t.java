package src.Actions;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLFeatureNotSupportedException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 //Returns GameLobby;#ofPeople;NameOfEachPlayer..;...;
 
 
 @WebServlet(urlPatterns={"/LeaveGame"})
 public class LeaveGame extends HttpServlet implements DataSource {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -972529346689447377L;
 
 	Connection connection = null;
 	private String Game =  null;
 	private String User =  null;
 	private int UserID = 0;
 	private int GameID = 0;
 	private int NumPlayers = 0;
 
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		if(request.getParameter("Game") != null){ 
 			this.setGame((String) request.getParameter("Game").toString());
 			GameID =  Integer.parseInt(Game);
 		}
 		if(request.getParameter("User") != null){ 
 			this.setUser((String) request.getParameter("User").toString());
 			UserID =  Integer.parseInt(User);
 		}
 		
 		try {
 		    System.out.println("Loading driver...");
 		    Class.forName("com.mysql.jdbc.Driver");
 		    System.out.println("Driver loaded!");
 		} catch (ClassNotFoundException e) {
 		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
 		}
 		
 		LeaveGame v = new LeaveGame();
         try {
 			connection = v.getConnection();
 			System.out.println("connection made");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		PrintWriter out = response.getWriter();
		if(connection != null && Game != null && User != null){
 			Statement stmt;
 			ResultSet rs;
 			int rs2, rs3, rs4, rs7;
 			ResultSet rs5, rs6;
 			try {
 				stmt = connection.createStatement();
 				
 				// Take Cards from player and put them in the deck before player deleted
 				//Shuffle the deck after
 				rs5 = stmt.executeQuery("SELECT GameDeck FROM tblGames WHERE GameID = " + GameID + ";");
 				rs5.beforeFirst();
 				String GameDeck="", UserHand="";
 				//Check if result set returned any records
 				if(rs5.next()){
 					 GameDeck = rs5.getString(1);
 				}
 				rs6 = stmt.executeQuery("SELECT PlayerHand FROM tblPlayers WHERE PlayerUserID = " + UserID + ";");
 				rs6.beforeFirst();
 				
 				//Check if result set returned any records
 				if(rs6.next()){
 					 UserHand = rs6.getString(1);
 				}
 				
 				//out.println(UserHand);
 				
 				String[] temp = UserHand.split(";");
 				String[] temp2 = GameDeck.split(";");
 				
 				ArrayList<String> UserHandList = new ArrayList<String>(Arrays.asList(temp));
 				ArrayList<String> GameHandList = new ArrayList<String>(Arrays.asList(temp2));
 				
 				GameHandList.addAll(UserHandList);
 				
 				Collections.shuffle(GameHandList, new Random(UserID));
 				GameDeck = "";
 				for(String str: GameHandList){
 					GameDeck = GameDeck + str + ";";
 				}
 				if(GameDeck.endsWith(";")){
 					GameDeck =  GameDeck.substring(0, GameDeck.length()-1);
 				}
 				
 				//out.println(GameDeck);
 			
 				// Update Game Deck
 				rs7 = stmt.executeUpdate("UPDATE tblGames SET GameDeck = '"+ GameDeck +"' WHERE GameID ="+ GameID +";");
 				
 				if(rs7 == 0)
 				{
 					out.print("Error: updating deck");
 				}
 				
 				
 				//Query to delete a user from the Players table - Leave a game
 				rs2 = stmt.executeUpdate("DELETE FROM tblPlayers WHERE PlayerGameID = "+ GameID +" AND PlayerUserID = "+ UserID +";");
 				
 				if(rs2 != 0){
 					//Deleted player from game
 					out.println("PlayerDeleted;"+UserID);
 					
 					//Query to get the numPlayers in a game
 					rs = stmt.executeQuery("SELECT Count(PlayerUserID) AS CountOfPlayers FROM tblPlayers GROUP BY PlayerGameID, PlayerStatus HAVING (PlayerGameID = "+ GameID +" AND PlayerStatus = 1);");
 					//point to 0 position in result set
 					rs.beforeFirst();
 					
 					//Check if result set returned any records
 					if(rs.next()){
 						//get number of players
 						NumPlayers = rs.getInt(1);						
 					}
 					else if(!rs.next()){
 						//No more players are in game
 						NumPlayers = 0;
 						
 						//If last player in game, with status '1' leaves, delete all players from game
 						rs4 = stmt.executeUpdate("DELETE FROM tblPlayers WHERE PlayerGameID ="+ GameID +";");
 						
 						if(rs4 != 0){
 							//Deleted players from game
 							//out.println("All invited players deleted from game");
 						}
 						else{
 							//Error occurred when deleting game
							//out.println("Error: unable to delete players from game.");
 						}
 						
 						
 						//Delete game record from Game table
 						rs3 = stmt.executeUpdate("DELETE FROM tblGames WHERE GameID="+GameID+";");
 						
 						if(rs3 != 0){
 							//Deleted game
 							//out.println("GameDeleted;"+GameID);
 						}
 						else{
 							//Error occurred when deleting game
							//out.println("Error: unable to delete game.");
 						}
 					}
 					//Close record set
 					rs.close();
 				}
 				else{
 					//Error occurred when deleting player from game
					//out.println("Error: unable to delete.");
 				}
 				
 				stmt.close();
 				connection.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 		}
		else
		{
			out.print("");
		}
 		
 	}
 
 
 
 	private void setUser(String strUser) {
 		// TODO Auto-generated method stub
 		User = strUser;
 	}
 
 
 
 	private void setGame(String strGame) {
 		// TODO Auto-generated method stub
 		Game = strGame;
 	}
 
 
 
 	@Override
 	public PrintWriter getLogWriter() throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public void setLogWriter(PrintWriter out) throws SQLException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public void setLoginTimeout(int seconds) throws SQLException {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public int getLoginTimeout() throws SQLException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 
 	@Override
 	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public <T> T unwrap(Class<T> iface) throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public boolean isWrapperFor(Class<?> iface) throws SQLException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 
 	@Override
 	public Connection getConnection() throws SQLException {
         if (connection != null) {
             System.out.println("Cant craete a Connection");
     } else {
             connection = DriverManager.getConnection(
                             "jdbc:mysql://ourdbinstance.cbvrc3frdaal.us-east-1.rds.amazonaws.com:3306/dbAppData", "AWSCards", "Cards9876");
     }
     return connection;
 	}
 
 	@Override
 	public Connection getConnection(String username, String password)
 			throws SQLException {
 		// TODO Auto-generated method stub
         if (connection != null) {
                 System.out.println("Cant create a Connection");
         } else {
                 connection = DriverManager.getConnection(
                                 "jdbc:mysql://ourdbinstance.cbvrc3frdaal.us-east-1.rds.amazonaws.com:3306/dbAppData", username, password);
         }
         return connection;
 	}
 	
 	
 	
 	
 	
 }
