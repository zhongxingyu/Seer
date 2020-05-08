 package Actions;
 import java.io.*;
 import java.sql.*;
 import java.util.logging.Logger;
 
 
 import javax.servlet.*;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.*;
 import javax.sql.DataSource;
 
 @WebServlet(urlPatterns={"/CreateGame"})
 
 
 //Note: This takes in a UserID. It can be changed to take a Username
 // 		just add the following lines of code to query and get the UserID.
 //
 //	Resultset rs5;
 //	Statement stmt5;
 //  
 //	stmt5 = connection.createStatement();
 //	//Query to get Users friend ID
 //	rs5 = stmt.executeQuery("SELECT UserID FROM tblUsers WHERE Username = '" + User + "';");
 //	if(rs5.next())
 //	}
 //  	UserID = rs.getInt(1);
 //	}
 
 
 public class CreateGame extends HttpServlet implements DataSource {
 
  		private String User =  null;
  		private String PlayerHand = null;
 		private int UserID, GameID;
 		private int rounds = -1;
 		
 		//private String qryRandomDeck = "SELECT CardID FROM tblCards WHERE CardType = 1 ORDER BY RAND() LIMIT 60;";
  		private String GameDeck = null;
  		private String BlackCards = null;
 		Connection connection = null;
 
  		
 		public String getUser() {
 			return User;
 		}
 		public void setUser(String user) {
 			User = user;
 		}
 
  		public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  			if(request.getParameter("User") != null){ 
  				this.setUser((String) request.getParameter("User").toString());
  				UserID =  Integer.parseInt(User);
  			}
 
 			if(request.getParameter("rounds") != null){
 				this.setRounds(Integer.parseInt((String) request.getParameter("rounds").toString()));
  			}
  			
  			
 			PrintWriter out = response.getWriter();
  			try {
  			    System.out.println("Loading driver...");
  			    Class.forName("com.mysql.jdbc.Driver");
  				//Check if user exists in database
  			}
 			catch(ClassNotFoundException e){
 						e.printStackTrace();	
 			}
  			
  			CreateGame ds = new CreateGame();
  		
  				try {
 					connection = ds.getConnection();
 				} catch (SQLException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
  			
  			    if(User!= null){
  				
 					Statement stmt, stmt2, stmt3, stmt4, stmt5;
  					ResultSet rs,rs4, rs5;
  					int rs2, rs3;
  					try {
  						stmt = connection.createStatement();
 						stmt2 = connection.createStatement();
 						stmt3 = connection.createStatement();
 						stmt4 = connection.createStatement();
 						stmt5 = connection.createStatement();
 						
 						// get random deck of 60 cards
 						rs = stmt.executeQuery("SELECT CardID FROM tblCards WHERE CardType = 1 ORDER BY RAND() LIMIT 60;");
 						//rs5.next();
 						//out.print("First Card: " + rs5.getInt(1));
 						// comma separate 60 cards
 						if(!rs.isBeforeFirst()){
 							//No cards in query
 						}
 						else{
 							GameDeck = "";
 							//Create comma separated string with card deck
 							//out.println("Before Deck: "+ GameDeck);
 							while(rs.next()){
 								GameDeck = GameDeck + rs.getInt(1) + ";";
 							}
 							//strip last comma off of game deck
 							if(GameDeck.endsWith(";")){
 								GameDeck = GameDeck.substring(0, GameDeck.length()-1);								
 							}
 							//System.out.println("Game Deck: " + GameDeck);
 							//out.println("After Deck: "+ GameDeck);
 							//out.println("Deck Created");
 						}
 						
 						//Code to grab first 5 from gameDeck - rebuild game deck and UserHand
                         String UserHand =  "";
                         String[] Temp = GameDeck.split(";");
                         GameDeck = "";
                         for(int i = 0; i<(Temp.length); i++){
                                 if(i<=4){
                                         UserHand = UserHand + Temp[i] + ";";
                                 }
                                 else{
                                         GameDeck = GameDeck + Temp[i] + ";";
                                 }
 
                         }
                         
 						if(GameDeck.endsWith(";")){
 							GameDeck = GameDeck.substring(0, GameDeck.length()-1);								
 						}
 						if(UserHand.endsWith(";")){
 							UserHand = UserHand.substring(0, UserHand.length()-1);								
 						}
 						
                         //out.println(UserHand);
                         //out.println(GameDeck);
 						
 						// Get 5 random black CardIDs
 						rs5 = stmt5.executeQuery("SELECT CardID FROM tblCards WHERE CardType = 0 ORDER BY RAND() LIMIT 5;");
 						
 						if(rs5.isBeforeFirst())
 						{
 							BlackCards = "";
 							//Create string for black card IDs
 							while(rs5.next())
 							{
 								BlackCards = BlackCards + rs5.getInt(1) + ";";
 							}
 							// Strip last comma off of game deck 
 							if(BlackCards.endsWith(";"))
 							{
 								BlackCards = BlackCards.substring(0, BlackCards.length()-1);
 							}
 						}
 						//out.println(BlackCards);
 						
 						
 						//out.print("Before Game creation");
 						//Create game record
 						//System.out.println("INSERT INTO tblGames (GameRounds, GameJudge, GameCurRound, GameDeck) VALUES (" + rounds + "," + UserID +"," + 0 + ",'"+ GameDeck +"');");
 						
 						rs2 = stmt2.executeUpdate("INSERT INTO tblGames (GameRounds, GameJudge, GameCurRound, GameDeck, GameBlackCards) VALUES (" + rounds + "," + UserID +"," + 0 + ",'"+ GameDeck +"','"+ BlackCards +"');");                           
 						System.out.println(rs2);
 						if(rs2!=0){
							//out.println("Game Created!\n");
 						}
 						else{
 							out.println("Unable to create game!");
 							return;
  						}
 						
 						//Get game ID
  						rs4 = stmt4.executeQuery("SELECT * FROM tblGames ORDER BY GameID DESC LIMIT 1;");
 						
  						//Ensure record set has record
 						if(rs4.next()){
 							//out.print("Success "+ rs4.getInt(1) +"\n");
 							GameID = rs4.getInt(1);
 						}
 						
 						
 						//Add user that created game into players table -- 'tblPlayers'
 						rs3 = stmt3.executeUpdate("INSERT INTO tblPlayers (PlayerGameID, PlayerUserID, PlayerStatus, PlayerHand) VALUES (" + GameID + "," + UserID +",1,'"+ UserHand +"');");                           
 						System.out.println(rs3);
 						if(rs3!=0){
 							out.println("Game:"+ GameID);
 						}
 						else{
 							out.println("Unable to add user to game!");
  						}										
 						
 						//Close record sets and connection
  						rs.close();
  						rs4.close();
  						rs5.close();
  						stmt5.close();
  						stmt.close();
  						stmt2.close();
  						stmt3.close();
  						stmt4.close();
  						connection.close();	
  					}
  					catch(SQLException e){
  						e.printStackTrace();	
  					}
  			
  			}
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
 		public int getRounds() {
 			return rounds;
 		}
 		public void setRounds(int rounds) {
 			this.rounds = rounds;
 		}
 
 		
 }
