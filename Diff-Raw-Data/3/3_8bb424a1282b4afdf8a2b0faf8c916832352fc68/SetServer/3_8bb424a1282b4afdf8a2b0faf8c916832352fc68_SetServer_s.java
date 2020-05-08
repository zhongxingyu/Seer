 package setServer;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 
 /*
  * SetServer: 
  * 	Executable that manages all set operations and server/client I/O
  * 
  * 		Attributes:
  * 			inMessages-		Input message queue
  * 			outMessages-	Output message queue
  * 			(userMap-		Maps clientID to User object)
  * 
  * 		Runs:
  * 			mainServer thread (handles all client I/O)
  * 			Game Management Loop
  * 				- Read and respond to client messages
  * 				- Check table statuses
  * 
  */
 
 public class SetServer {
 
 	public static void main(String[] args) throws SQLException {
 		
 		// BlockingQueue receives all client messages, allows reads only when non-empty, and supports threads
 		final BlockingQueue<Message> inMessages = new LinkedBlockingQueue<Message>();
 		final BlockingQueue<Message> outMessages = new LinkedBlockingQueue<Message>();
 		
 		// Map from user id (integer) to User object
 		Map<Object, User> userMap = new HashMap<Object, User>(); 
 		
 		// Map from tableNum to Table object
 		int numTables = 0;
 		Map<Object, Table> tableMap = new HashMap<Object, Table>();
 	
 		// Runs the client interface server
 		MainServer mainServer = new MainServer(inMessages, outMessages);
 		mainServer.start(); // thread
 
 		// All of the server mechanics are handled by the MainServer and its subclasses,
 		// so that SetServer only has to read from inMessages to get incoming client messages,
 		// and write to outMessages to send them. These queues contain Message objects, which
 		// simply contain a clientID and a message string.
 		
 		boolean isRunning = true;
 		Connection connection = null;
 		Statement stmt = null;
 		ResultSet usertable = null;
 	 
 		
 		
 		while(isRunning) // Messages are handled in the order they are received. If each message-handling is fast, there shouldn't be a problem.
 		{
 			try {
 				final Message inM = inMessages.take();
 				String [] splitM = inM.message.split("[;]"); // Message parts split by semicolons
 				switch(splitM[0].charAt(0)) {// Switch on first character in message (command character)
 					case 'R': // Register: R;Username;Password
 						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
 					 
 						try {
 							connection = DriverManager
 							.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
 					 
 						} catch (SQLException e) {
 							System.out.println("Connection Failed!");
 							e.printStackTrace();
 							return;
 						}	
 						stmt = connection.createStatement();
 						usertable=stmt.executeQuery("SELECT * FROM `users` WHERE `username` =  '"+splitM[1]+"';");
 						if (usertable.next()){
 							outMessages.put( new Message(inM.clientID, "X;R") );
 							System.out.println("User already exists");
 						}
 
 						 else {
 							 stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('"+splitM[1]+"', '"+splitM[2]+"');");	
 							 System.out.println("User created");
 							 outMessages.put( new Message(inM.clientID, "I;false;"+allTableString(tableMap)+";"+allUserString(userMap)) );
 							 User newUser = new User(splitM[1], 1000, -1, false);
 							 userMap.put(inM.clientID, newUser);
 							 outMessages.put( new Message(-1, "U;" + splitM[1] + ";N/A") );
 						}
 						stmt.close();
 						connection.close();
 						break;
 					case 'L': // Login:  L;Username;Password
 						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
 						try {
 							connection = DriverManager
 							.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
 					 
 						} catch (SQLException e) {
 							System.out.println("Connection Failed!");
 							e.printStackTrace();
 							return;
 						}	
 						stmt = connection.createStatement();
 						usertable = stmt.executeQuery("SELECT * FROM `users` WHERE `username` =  '"+splitM[1]+"';");
 						if (!usertable.next()){
 							System.out.println("User not found");
 						}
 						else{
 							boolean alreadyOnline = false;
 							Iterator<Entry<Object, User>> it = userMap.entrySet().iterator();
 							while (it.hasNext()) {
 								Map.Entry<Object, User> entry = (Map.Entry<Object, User>) it.next();
 								User curUser = entry.getValue();
 								if(curUser.username.compareTo(splitM[1]) == 0) {
 									alreadyOnline = true;
 									break;
 								}
 							}
 							
 							if(alreadyOnline) {
 								outMessages.put( new Message(inM.clientID, "X;A") );
 							} else {
 								String passt = usertable.getString("password");
 								boolean isAdmin=false;
 								if (splitM[2].equals(passt)){
 									String accountType = usertable.getString("type");
 									int userRating = usertable.getInt("score");
 									if(accountType.equals("admin")){
 										isAdmin=true;
 									}
 									outMessages.put( new Message(inM.clientID, "I;"+isAdmin+";"+allTableString(tableMap)+";"+allUserString(userMap)) );
 									User newUser = new User(splitM[1], userRating, -1, isAdmin);
 									userMap.put(inM.clientID, newUser);
 									outMessages.put( new Message(-1, "U;" + splitM[1] + ";N/A") );
 								}
 								else{
 									outMessages.put( new Message(inM.clientID, "X;L") );
 								}	
 							}
 						}
 						stmt.close();
 						connection.close();
 						break;
 					case 'T': // Create Table: T;Name;NumPlayers
 						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
 						
 						User userT = userMap.get(inM.clientID);
 						if(userT.currentTable >= 0) {
 							outMessages.put(new Message(inM.clientID, "A"));
 							break;
 						}
 						
 						userT.currentTable = numTables;
 						
 						Table newTable = new Table(splitM[1], 0, Integer.parseInt(splitM[2]));
 						newTable.addPlayer(inM.clientID);
 						
 						// Give client "Table Made" message
 						outMessages.put( new Message(inM.clientID, newTable.playerString(userMap)) );
 						// Broadcast new table update
 						outMessages.put( new Message(-1, "U;" + numTables + ";" + newTable.name + ";" + newTable.numPlayers + ";" + newTable.maxPlayers + ";" + newTable.status()) );
 						outMessages.put( new Message(-1, "U;" + userT.username + ";" + numTables) );
 						
 						tableMap.put(numTables, newTable);
 						numTables++;
 						
 						break;
 					case 'J': // Join Table: J;TableNum
 						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}
 						
 						User userJ = userMap.get(inM.clientID);
 						if(userJ.currentTable >= 0) {
 							outMessages.put(new Message(inM.clientID, "A"));
 							break;
 						}
 						
 						userJ.currentTable = Integer.parseInt(splitM[1]);
 						
 						Table tableJ = tableMap.get(userJ.currentTable);
 						if( tableJ != null ) {
 							if(tableJ.numPlayers < tableJ.maxPlayers) {
 								if(tableJ.numGoPressed < tableJ.maxPlayers) { // Game not in progress
 									tableJ.addPlayer(inM.clientID);
 									outMessages.put(new Message(-1, "U;" + userJ.currentTable + ";" + tableJ.name + ";" + tableJ.numPlayers + ";" + tableJ.maxPlayers + ";" + tableJ.status()));
 									outMessages.put( new Message(-1, "U;" + userJ.username + ";" + userJ.currentTable) );
 									sendToTable(outMessages, tableJ, tableJ.playerString(userMap));
 								} else { // Game in progress
 									userJ.currentTable = -1;
 									outMessages.put(new Message(inM.clientID, "H")); // Table is playing
 								}
 							} else {
 								userJ.currentTable = -1;
 								outMessages.put(new Message(inM.clientID, "F")); // Table is full
 							}
 						} else {
 							System.err.println("Table requested that does not exist!");
 							userJ.currentTable = -1;
 							outMessages.put(new Message(inM.clientID, "F")); // Pretend table is full
 						}
 						
 						break;
 					case 'E': // Exit Table: E
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						
 						User userE = userMap.get(inM.clientID);
 						if(userE.currentTable < 0) { // User exited non-existent table
 							System.err.println("User exited non-existent table!");
 							outMessages.put(new Message(inM.clientID, "E")); 
 							break;
 						}
 						
 						Table tableE = tableMap.get(userE.currentTable);
 						outMessages.put(new Message(inM.clientID, "E"));
 						
 						if( tableE != null ) {
 							tableE.removePlayer(inM.clientID);
 							outMessages.put(new Message(-1, "U;" + userE.currentTable + ";" + tableE.name + ";" + tableE.numPlayers + ";" + tableE.maxPlayers + ";" + tableE.status()));
 							outMessages.put( new Message(-1, "U;" + userE.username + ";N/A") );
 							if(tableE.numPlayers > 0) {
 								sendToTable(outMessages, tableE, tableE.playerString(userMap));
 							} else {
 								tableMap.remove(userE.currentTable);
 							}
 							
 						} else {
 							System.err.println("Player exited non-existant table!");
 						}
 						userE.currentTable = -1;
 						
 						break;
 					case 'D': // Disconnect: D
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						
 						mainServer.socketMap.remove(inM.clientID);
 						
 						User userD = userMap.get(inM.clientID);
 						if(userD != null) {
 							
 							outMessages.put( new Message(-1, "U;" + userD.username + ";X") );
 							
 							if(userD.currentTable < 0) { // User not at table
 								userMap.remove(inM.clientID);
 								break;
 							}
 							
 							Table tableD = tableMap.get(userD.currentTable);
 							
 							if( tableD != null ) {
 								tableD.removePlayer(inM.clientID);
 								if(tableD.numPlayers > 0) {
 									sendToTable(outMessages, tableD, tableD.playerString(userMap));
 									if(tableD.numGoPressed == tableD.maxPlayers) { // Game is playing
 										Connection connectionD = null;
 										Statement stmtD = null;
 										connectionD = DriverManager.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
 										stmtD = connectionD.createStatement();
 										
 										stmtD.executeUpdate("UPDATE `users` SET `score`=score-10 WHERE `username`='"+userD.username+"';");	
 										
 										stmtD.close();
 										connectionD.close();
 									} else {
 										sendToTable(outMessages, tableD, "R"); // Table reset
										outMessages.put(new Message(-1, "U;" + userD.currentTable + ";" + tableD.name + ";" + tableD.numPlayers + ";" + tableD.maxPlayers + ";" + tableD.status()));
 										tableD.numGoPressed = 0;
 									}
 								} else {
 									tableMap.remove(userD.currentTable);
 								}
 								
 							} else {
 								System.err.println("Disconnect Table Error!");
 							}
 							
 							userMap.remove(inM.clientID);
 						}
 						
 						break;
 					case 'G': // 'Go' (Start game) Signal: G
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						User userG = userMap.get(inM.clientID);
 						Table tableG = tableMap.get(userG.currentTable);
 						if( tableG != null ) {
 							tableG.numGoPressed++;
 							if(tableG.numGoPressed == tableG.maxPlayers) { 
 								//String cardOrder = "T;12;01;02;03;04;05;06;07;08;09;10;11;12";
 								tableG.resetScores();
 								sendToTable(outMessages, tableG, tableG.playerString(userMap));
 								tableG.initializeDeck();
 								sendToTable(outMessages, tableG, tableG.tableString());
 								inMessages.put(new Message(inM.clientID, "H")); //Initial set existence check
 							}
 						}
 						break;
 					case 'S': // Set made: S;Card1;Card2;Card3
 						if(splitM.length != 4) {System.err.println("Message Length Error!"); break;}
 						User userS = userMap.get(inM.clientID);
 						final Table tableS = tableMap.get(userS.currentTable);
 						if(tableS.setExists(Integer.parseInt(splitM[1]), Integer.parseInt(splitM[2]), Integer.parseInt(splitM[3]))) {
 							final int[] newCards = tableS.removeSet(Integer.parseInt(splitM[1]), Integer.parseInt(splitM[2]), Integer.parseInt(splitM[3]));
 							outMessages.put(new Message(inM.clientID, "Y"));
 							sendToTable(outMessages, tableS, "S;" + splitM[1] + ";" + splitM[2] + ";" + splitM[3]);
 							tableS.players.put(inM.clientID, tableS.players.get(inM.clientID) + 1);
 							sendToTable(outMessages, tableS, tableS.playerString(userMap));
 							if(newCards[0] >= 0) { // Table is 12 or less
 								if(newCards[1] >= 0) { // There is new cards
 									Thread sendNewCardsThread = new Thread() {
 										public void run() {
 										    try {
 										    	Thread.sleep(1000);
 											} catch (InterruptedException e) {
 												// Do nothing
 											}
 										    try {
 												inMessages.put(new Message(inM.clientID, "H"));
 											} catch (InterruptedException e) {
 												System.err.println("Error in Set Checking Thread!");
 											}
 										    sendToTable(outMessages, tableS, "N;" + newCards[0] + ";" + newCards[1] + ";" + newCards[2]);
 										}
 									};
 									sendNewCardsThread.start();
 								} else if(tableS.noMoreSets()) { // GAME OVER!!!
 									gameOver(outMessages, userMap, userS.currentTable, tableS);
 								}
 							} else {
 								inMessages.put(new Message(inM.clientID, "H"));
 							}
 							
 						} // else ignore message
 						break;
 					case 'H': // Check for sets: H
 						// THE SERVER SENDS THIS MESSAGE TO ITSELF TO RECURSIVELY CHECK FOR SETS
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						User userH = userMap.get(inM.clientID);
 						final Table tableH = tableMap.get(userH.currentTable);
 						if(tableH.noMoreSets()) {
 							final int[] newCards = tableH.newCards();
 							if(newCards[1] >= 0) {
 								sendToTable(outMessages, tableH, "O");
 								Thread sendNewCardsThread = new Thread() {
 									public void run() {
 									    try {
 									    	Thread.sleep(1000);
 										} catch (InterruptedException e) {
 											// Do nothing
 										}
 									    try {
 											inMessages.put(new Message(inM.clientID, "H"));
 										} catch (InterruptedException e) {
 											System.err.println("Error in Set Checking Thread!");
 										}
 									    sendToTable(outMessages, tableH, "N;" + newCards[0] + ";" + newCards[1] + ";" + newCards[2]);
 									}
 								};
 								sendNewCardsThread.start();
 							} else {
 								gameOver(outMessages, userMap, userH.currentTable, tableH);
 							}
 						}
 						break;
 					case 'X': // Mistake made: X
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						outMessages.put(new Message(inM.clientID, "D")); // Reflect mistake back to MainClient
 						User userX = userMap.get(inM.clientID);
 						Table tableX = tableMap.get(userX.currentTable);
 						tableX.players.put(inM.clientID, tableX.players.get(inM.clientID) - 1);
 						sendToTable(outMessages, tableX, tableX.playerString(userMap));
 						break;
 					case 'C': // Chat sent: C;Message
 						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}					
 						User userC = userMap.get(inM.clientID);
 						outMessages.put(new Message(-1, "C;" + userC.username + ";" + splitM[1]));
 						break;
 					case 'Q': // Table Chat: Q;Message
 						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}					
 						User userQ = userMap.get(inM.clientID);
 						Table tableQ = tableMap.get(userQ.currentTable);
 						sendToTable(outMessages, tableQ, "Q;" + userQ.username + ";" + splitM[1]);
 						break;
 				}
 			} catch (InterruptedException e) {
 				// Do nothing?
 			}
 		}
 		
 	}
 	
 	private static void gameOver(BlockingQueue<Message> outMessages, Map<Object, User> userMap, int tableNum, Table table) throws SQLException {
 		
 		Iterator<Entry<Object, Integer> > it = table.players.entrySet().iterator();
 		int MaxScore = -12345; // Arbitrary low number
 		int TotalPlayers = table.numGoPressed;
 		
 		List<String> Winner= new ArrayList<String>();
 		
 		while (it.hasNext()) {
 			Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();	
 			if(entry.getValue() == MaxScore){
 				int userID = (Integer) entry.getKey();
 				User curUser = userMap.get(userID);
 				Winner.add(curUser.username);
 			}
 			else if(entry.getValue() > MaxScore){
 				int userID = (Integer) entry.getKey();
 				User curUser = userMap.get(userID);
 				Winner.clear();
 				Winner.add(curUser.username);
 				MaxScore = entry.getValue();
 			}
 		}
 		
 		Iterator<Entry<Object, Integer> > it2 = table.players.entrySet().iterator();
 		double WinnerScore= ((double) (TotalPlayers - Winner.size()))/((double) Winner.size())*10;
 		Connection connection = null;
 		Statement stmt = null;
 		connection = DriverManager.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
 		stmt = connection.createStatement();
 		
 		while (it2.hasNext()){
 			Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it2.next();
 			int userID = (Integer) entry.getKey();
 			User curUser = userMap.get(userID);
 			if(Winner.contains(curUser.username)){
 				try {
 					outMessages.put( new Message(userID, "G;1;" + curUser.rating + ";" + ((int)(curUser.rating + WinnerScore))) );
 				} catch (InterruptedException e) { System.err.println("Game Over Error!"); }
 				curUser.rating += WinnerScore;
 				stmt.executeUpdate("UPDATE `users` SET `score`=score+"+WinnerScore+" WHERE `username`='"+curUser.username+"';");
 			}
 			else{
 				try {
 					outMessages.put( new Message(userID, "G;0;" + curUser.rating + ";" + (curUser.rating - 10)) );
 				} catch (InterruptedException e) { System.err.println("Haxxors!!!"); }
 				curUser.rating -= 10;
 				stmt.executeUpdate("UPDATE `users` SET `score`=score-10 WHERE `username`='"+curUser.username+"';");	
 			}	
 		}
 		
 		stmt.close();
 		connection.close();
 		
 		table.numGoPressed = 0; // Reset table
 		table.resetScores(); // Resets scores to 0
 		table.initializeDeck(); // Reset cards
 		// sendToTable(outMessages, table, "G"); // Tell everyone game over
 		sendToTable(outMessages, table, table.playerString(userMap)); // Update scores to 0
 		try {
 			outMessages.put(new Message(-1, "U;" + tableNum + ";" + table.name + ";" + table.numPlayers + ";" + table.maxPlayers + ";" + table.status()));
 		} catch (InterruptedException e1) { }
 	}
 	
 	private static void sendToTable(BlockingQueue<Message> outMessages, Table outTable, String outString) {
 		Iterator<Entry<Object, Integer> > it = outTable.players.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();
 			int userID = (Integer) entry.getKey();
 			try {
 				outMessages.put(new Message(userID, outString));
 			} catch (InterruptedException e) {
 				System.err.println("Failed to send message to table " + outTable.name + "!");
 			}
 		}
 	}
 	
 	private static String allTableString(Map<Object, Table> tableMap) {
 		String out = "" + tableMap.size();
 		Iterator<Entry<Object, Table>> it = tableMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<Object, Table> entry = (Map.Entry<Object, Table>) it.next();
 			int tableNum = (Integer) entry.getKey();
 			Table curTable = (Table) entry.getValue();
 			out += ";" + tableNum + ";" + curTable.name + ";" + curTable.numPlayers + ";" + curTable.maxPlayers + ";" + curTable.status();
 		}
 		return out;
 	}
 	
 	private static String allUserString(Map<Object, User> userMap) {
 		String out = "" + userMap.size();
 		Iterator<Entry<Object, User>> it = userMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<Object, User> entry = (Map.Entry<Object, User>) it.next();
 			User curUser = (User) entry.getValue();
 			String tableString = "" + ((curUser.currentTable < 0)?("N/A"):(curUser.currentTable));
 			out += ";" + curUser.username + ";" + tableString;
 		}
 		return out;
 	}
 
 }
