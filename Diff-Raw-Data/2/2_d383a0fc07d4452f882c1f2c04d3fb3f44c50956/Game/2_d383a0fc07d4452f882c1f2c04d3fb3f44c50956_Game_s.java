 package Actions;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLFeatureNotSupportedException;
 import java.sql.Statement;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 //? User=UserId&GameID=
 //Returns RefreshGame;GameRound#;#OfPlayers;PlayersInGame;
 //CardCzarUserName;NumberOfCardsInHand;CardStrings;blackcardString;
 
 
 //CardString = CardID + ";" + cardText
 
 @WebServlet(urlPatterns={"/RefreshGame"})
 public class Game extends HttpServlet implements DataSource {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -972529346689447377L;
 	private String User =  null;
 	private String Game = null;
 	private int UserID;
 	private int GameID;
 	Connection connection = null;
 	
 	private int CurrentRound;
 	
 	//Player In game variables
 	private int NumPlayers;
 	private String Players;
 	private String LastBlackCard="";
 	private String LastWhiteCard="";
 	private String LastWinningPlayer="";
 	private String LastBlackCardID="";
 	private String LastWhiteCardID="";
 	private String LastWinningPlayerID="";
 	
 	
 	//Card Czar Information
 	private String CzarName = "";
 	private int CzarID;
 	private int NumSubmitted = 0;
 	private String SubmittedCardString = "";
 	
 	//Player card Info - (If User is not the Czar)
 	private String Hand = "";
 	private String HandIDs = "";
 	private String HandText = "";
 	private int NumCards;
 	private boolean IsCardSubmitted = false;
 	
 	//Black card variables
 	private String BlackCards = "";
 	private String BlackCardID;
 	private String BlackCardText = "";
 	
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException {
 		
 		if(request.getParameter("User") != null){ 
 			this.setUser((String) request.getParameter("User").toString());
 			UserID =  Integer.parseInt(User);
 		}
 		if(request.getParameter("Game") != null){ 
 			this.setGame((String) request.getParameter("Game").toString());
 			GameID =  Integer.parseInt(Game);
 		}
 		
 		try {
 		    System.out.println("Loading driver...");
 		    Class.forName("com.mysql.jdbc.Driver");
 		    System.out.println("Driver loaded!");
 		} catch (ClassNotFoundException e) {
 		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
 		}
 		
 		Game v = new Game();
         try {
 			connection = v.getConnection();
 			System.out.println("connection made");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		PrintWriter out = response.getWriter();
 		
 		//Multiple Queries will need to be run here.
 		if(connection != null && User != null && Game != null){
 				Statement stmt;
 				ResultSet rs, rs2, rs3, rs4, rs5, rs6, rs7, rs8;
 				try {
 					stmt = connection.createStatement();
 					
 					
 					//Get Players in Game
 					rs3 = stmt.executeQuery("SELECT Username, PlayerScore FROM tblUsers INNER JOIN tblPlayers ON tblUsers.UserID = tblPlayers.PlayerUserID WHERE PlayerGameID = "+ GameID +" AND PlayerStatus = 1 GROUP BY Username, PlayerScore;");
 					
 					if(!rs3.isBeforeFirst()){
 						//out.println("None in the List");
 					}
 					else{
 						//out.print("Successful");
 						Players = "";
 						while(rs3.next()){
 							//out.print("; List");
 							Players = Players + rs3.getString(1) + ";" + rs3.getInt(2) + ";";
 							NumPlayers++;
 						}
 					}
 					
 					// get current round, black cards list, and game judge
 					rs = stmt.executeQuery("SELECT GameCurRound, GameBlackCards, GameJudge, LastBlackCard, LastWhiteCard, LastWinningPlayer FROM tblGames WHERE GameID = "+GameID+";");
 					
 					if(rs.next())
 					{
 						//Get game info
 						CurrentRound = rs.getInt(1);
 						BlackCards = rs.getString(2);
 						CzarID = rs.getInt(3);
 						LastBlackCardID = rs.getString(4);
 						LastWhiteCardID = rs.getString(5);
 						LastWinningPlayerID = rs.getString(6);
 						
 						
 						//out.println(CzarID+"");
 						//Get CardCzar User name
 						rs7 = stmt.executeQuery("SELECT UserID, Username FROM tblUsers WHERE UserID In("+CzarID+","+LastWinningPlayerID+");");
 						//out.println("SELECT UserID, Username FROM tblUsers WHERE UserID In("+CzarID+","+LastWinningPlayerID+");");
 						CzarName="Test";
 						LastWinningPlayer="";
 						while(rs7.next())
 						{
 							if(rs7.getString(1).equals(Integer.toString(CzarID))){
 								CzarName = rs7.getString(2);
 							}
 							else if(rs7.getString(1).equals(LastWinningPlayerID)){
 								LastWinningPlayer = rs7.getString(2);
 							}
 							
 						}
 						
 						//Parse Black cards here to get top card
 						//String BlackCards= "";
                         String[] blackList = BlackCards.split(";");
                         BlackCardID = blackList[0];
 						//Use top black card ID to get Black Card text
                         rs2 = stmt.executeQuery("SELECT CardID, CardText FROM tblCards WHERE CardID In("+LastBlackCardID+","+LastWhiteCardID+","+BlackCardID+");");
 						BlackCardText="";
 						LastBlackCard="";
 						LastWhiteCard="";
 						while(rs2.next())
 						{
 							if(rs2.getString(1).equals(BlackCardID)){
 								BlackCardText = rs2.getString(2);
 							}
 							else if(rs2.getString(1).equals(LastBlackCardID)){
 								LastBlackCard= rs2.getString(2);
 							}
 							else if(rs2.getString(1).equals(LastWhiteCardID)){
 								LastWhiteCard= rs2.getString(2);
 							}
 						}
 						rs2.close();
 						
 						// If User refreshing is the Card Czar
 						if (CzarID == UserID)
 						{
 							//Return card strings of all players that submitted a card
 							rs4 = stmt.executeQuery("SELECT CardID, CardText FROM tblCards INNER JOIN tblPlayers ON tblCards.CardID = tblPlayers.SubmittedCard WHERE PlayerGameID = "+ GameID +";");
 						
 							if(!rs4.isBeforeFirst()){
 								//out.println("None in the List");
 							}
 							else{
 								//out.print("Successful");
								//NumSubmitted = 0;
 								SubmittedCardString = "";
 								while(rs4.next()){
 									//out.print("; List");
 									SubmittedCardString = SubmittedCardString + rs4.getInt(1) + ";" + rs4.getString(2) + ";";
 									NumSubmitted++;
 								}
 								
 
 							}
 							
 							if(NumSubmitted == 0)
 							{
 								SubmittedCardString = ";";
 							}
 							
 							rs4.close();
 							
 							//Output for CardCzar
 							out.print("RefreshGame;"+CurrentRound+";"+NumPlayers+";"+Players+CzarName+";"+NumSubmitted+";"+SubmittedCardString+BlackCardText+";"+IsCardSubmitted+";"+LastWhiteCard+";"+LastBlackCard+";"+LastWinningPlayer);
 						
 						}
 						// If User is not the Czar
 						else if(CzarID != UserID)
 						{
 							rs5 = stmt.executeQuery("SELECT PlayerHand, SubmittedCard FROM tblPlayers WHERE PlayerUserID = "+UserID+" AND PlayerGameID = "+GameID+";");
 							
 //							if(!rs5.isBeforeFirst())
 //							{
 //								out.print("rs5 :"+HandIDs);
 //								//HandIDs = rs5.getString(1);
 //							}
 //							else
 //							{
 //								out.print("HERE");
 //							}
 							
 							if(rs5.next())
 							{
 								//out.print("rs5 before:"+HandIDs);
 								HandIDs = rs5.getString(1);
 								
 								//out.print("Submitted CardID:"+ rs5.getInt(2));
 								//Check if user submitted a card
 								if(rs5.getInt(2) > 0)
 								{
 									//Set submitted to true
 									IsCardSubmitted = true;
 								}
 								else
 								{
 									//Card has not been submitted
 									IsCardSubmitted = false;
 								}
 								//out.print("rs5 after:"+HandIDs);
 							}
 							
 							
 							rs5.close();
 							//Get card text from user's hand
 							Hand = HandIDs.replaceAll(";", ",");
 							//out.print(Hand);
 							//Query to get card text
 							rs6 = stmt.executeQuery("SELECT CardID, CardText FROM tblCards WHERE CardID In("+Hand+");");
 							
 							if(!rs6.isBeforeFirst()){
 								//out.println("None in the List");
 							}
 							else{
 								//out.print("Successful");
 								NumCards = 0;
 								while(rs6.next()){
 									//out.print("; List");
 									HandText = HandText + rs6.getInt(1) + ";" + rs6.getString(2) + ";";
 									NumCards++;
 								}
 							}
 							rs6.close();
 							
 							
 							//Print Info
 							out.print("RefreshGame;"+CurrentRound+";"+NumPlayers+";"+Players+CzarName+";"+NumCards+";"+HandText+BlackCardText+ ";"+IsCardSubmitted+";"+LastWhiteCard+";"+LastBlackCard+";"+LastWinningPlayer);						}
 						
 				
 					}
 					//Reset variables
 					HandText = "";
 					HandIDs = "";
 					NumPlayers = 0;
 					Game = null;
 					User = null;
 					GameID = 0;
 					UserID = 0;
 					
 					rs.close();
 					rs3.close();
 					stmt.close();
 					connection.close();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 			else
 			{
 				//User, game, or connection is null print out blank
 				out.print("");
 			}
 		
 			//out.print("RefreshGame;"+CurrentRound+";"+NumPlayers+";"+Players);
 		
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
 
 
 	public String getUser() {
 		return User;
 	}
 
 
 	public void setUser(String user) {
 		User = user;
 	}
 
 	/*
 	public String getPassword() {
 		return password;
 	}
 
 
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	*/
 	
 	
 	
 	
 	
 }
 
 
