 package setServer;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
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
 	
 	private static class User {
 		public String username;
 		public int numWins;
 		public int numLosses;
 		public int currentTable;
 		
 		public User(String username, int numWins, int numLosses, int currentTable) {
 			this.username = username;
 			this.numWins = numWins;
 			this.numLosses = numLosses;
 			this.currentTable = currentTable;
 		}
 	};
 	
 	private static class Table {
 		public String name;
 		public int numPlayers;
 		public int maxPlayers;
 		public Vector<Integer> players;
 		public int numGoPressed;
 		
 		public Table(String name, int numPlayers, int maxPlayers) {
 			this.name = name;
 			this.numPlayers = numPlayers;
 			this.maxPlayers = maxPlayers;
 			this.players = new Vector<Integer>();
 			this.numGoPressed = 0;
 		}
 		
 		public void addPlayer(int userID) {
 			numPlayers++;
 			players.add(userID);
 		}
 		
 		public void removePlayer(int userID) {
 			numPlayers--;
 			players.remove(new Integer(userID));
 		}
 		
 		public String playerString(Map<Object, User> userMap) {
 			String out = "P;" + numPlayers + ";" + maxPlayers;
 			Iterator<Integer> it = players.iterator();
 			while(it.hasNext()) {
 				out += ";" + userMap.get(it.next()).username;
 			}
 			return out;
 		}
 	}
 
 	public static void main(String[] args) {
 		
 		// BlockingQueue receives all client messages, allows reads only when non-empty, and supports threads
 		BlockingQueue<Message> inMessages = new LinkedBlockingQueue<Message>();
 		BlockingQueue<Message> outMessages = new LinkedBlockingQueue<Message>();
 		
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
 		
 		while(isRunning) // Messages are handled in the order they are received. If each message-handling is fast, there shouldn't be a problem.
 		{
 			try {
 				Message inM = inMessages.take();
 				String [] splitM = inM.message.split("[;]"); // Message parts split by semicolons
 				switch(splitM[0].charAt(0)) {// Switch on first character in message (command character)
 					case 'R': // Register: R;Username;Password
 						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
 						
 						/////////////////////////////////////////
 						boolean usernameAlreadyExists = false; //
 						/////////////////////////////////////////
 						
 						if(usernameAlreadyExists) {
 							outMessages.put( new Message(inM.clientID, "X") );
 						} else {
 							////////////////////////////////////////
 							// TODO: Add user/pass combo to MySQL //
 							////////////////////////////////////////
 							
 							User newUser = new User(splitM[1], 0, 0, -1);
 							userMap.put(inM.clientID, newUser);
 							
 							outMessages.put( new Message(inM.clientID, allTableString(tableMap)) );
 						}
 						break;
 					case 'L': // Login:  L;Username;Password
 						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
 					
 						//////////////////////////////////////////
 						// TODO: Check user/pass combo in MySQL //
 						//////////////////////////////////////////
 						boolean loginSuccessful = true;
 						int numWins = 0, numLosses = 0;
 						//////////////////////////////////////////
 						
 						if(loginSuccessful) {
 							User newUser = new User(splitM[1], numWins, numLosses, -1);
 							userMap.put(inM.clientID, newUser);
 							
 							outMessages.put( new Message(inM.clientID, allTableString(tableMap)) );
 						} else {
 							outMessages.put( new Message(inM.clientID, "X") );
 						}
 						
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
 						outMessages.put(new Message(inM.clientID, newTable.playerString(userMap)));
 						// Broadcast new table update
 						outMessages.put(new Message(-1, "U;" + numTables + ";" + newTable.name + ";" + newTable.numPlayers + ";" + newTable.maxPlayers));
 						
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
								tableJ.numPlayers++;
 								tableJ.addPlayer(inM.clientID);
 								outMessages.put(new Message(-1, "U;" + userJ.currentTable + ";" + tableJ.name + ";" + tableJ.numPlayers + ";" + tableJ.maxPlayers));
 								/////////// TODO: Copy-and-pasted code; can it be consolidated?
 								String outString = tableJ.playerString(userMap);
 								Iterator<Integer> it = tableJ.players.iterator();
 								while(it.hasNext()) {
 									outMessages.put(new Message(it.next(), outString)); // Send message to each player at table
 								}
 								////////////
 							} else {
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
 							outMessages.put(new Message(-1, "U;" + userE.currentTable + ";" + tableE.name + ";" + tableE.numPlayers + ";" + tableE.maxPlayers));
 							if(tableE.numPlayers > 0) {
 								/////////
 								String outString = tableE.playerString(userMap);
 								Iterator<Integer> it = tableE.players.iterator();
 								while(it.hasNext()) {
 									outMessages.put(new Message(it.next(), outString)); // Send message to each player at table
 								}
 								/////////
 							} else {
 								tableMap.remove(userE.currentTable);
 							}
 							
 						} else {
 							System.err.println("Player exited non-existant table!");
 						}
 						userE.currentTable = -1;
 						
 						break;
 					case 'G': // 'Go' (Start game) Signal: G
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						
 						Table tableG = tableMap.get(Integer.parseInt(splitM[1]));
 						if( tableG != null ) {
 							tableG.numGoPressed++;
 							if(tableG.numGoPressed == tableG.maxPlayers) { 
 								// TODO: Start the damn game!!!
 							}
 						} else {
 							System.err.println("Player exited non-existant table!");
 						}
 						
 						break;
 					case 'S': // Set made: S;Card1;Card2;Card3
 						if(splitM.length != 4) {System.err.println("Message Length Error!"); break;}
 						// TODO: Validate set; If invalid, ignore; If valid, award points, broadcast changes (lost/new cards)
 						// Also make sure to check for: [No sets possible!] or [Game is over!] 
 						break;
 					case 'X': // Mistake made: X
 						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
 						// TODO: Dock a point from user; Broadcast docked point
 						break;
 				}
 			} catch (InterruptedException e) {
 				// Do nothing?
 			}
 		}
 		
 	}
 	
 	private static String allTableString(Map<Object, Table> tableMap) {
 		String out = "I;" + tableMap.size();
 		Iterator<Entry<Object, Table>> it = tableMap.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<Object, Table> entry = (Map.Entry<Object, Table>) it.next();
 			int tableNum = (Integer) entry.getKey();
 			Table curTable = (Table) entry.getValue();
 			out += ";" + tableNum + ";" + curTable.name + ";" + curTable.numPlayers + ";" + curTable.maxPlayers;
 		}
 		return out;
 	}
 
 }
