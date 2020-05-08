 // Copyright 2010 owners of the AI Challenge project
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); you may not
 // use this file except in compliance with the License. You may obtain a copy
 // of the License at http://www.apache.org/licenses/LICENSE-2.0 . Unless
 // required by applicable law or agreed to in writing, software distributed
 // under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 // CONDITIONS OF ANY KIND, either express or implied. See the License for the
 // specific language governing permissions and limitations under the License.
 //
 // Author: Jeff Cameron (jeff@jpcameron.com)
 //
 // Plays a game of Planet Wars between two computer programs.
 // NOTICE: code has been modified to allow synchronized gameplay (one player
 // per turn).
 
 import java.io.*;
 import java.util.*;
 
 public class Engine {
 	
 	public static int GAME_MODE_SERIAL = 1;
 	public static int GAME_MODE_PARALLEL = 2;
 	
     public static void KillClients(List<Process> clients) {
 	for (Process p : clients) {
 	    if (p != null) {
 		p.destroy();
 	    }
 	}
     }
 
     public static boolean AllTrue(boolean[] v) {
 	for (int i = 0; i < v.length; ++i) {
 	    if (!v[i]) {
 		return false;
 	    }
 	}
 	return true;
     }
 
     public static boolean clientsDone(boolean[] clientsDone, int gameMode, int playerId) {
     	if (gameMode == GAME_MODE_PARALLEL) {
     		return AllTrue(clientsDone);
     	} else {
     		return clientsDone[playerId];
     	}
     }
 
 
     
 	@SuppressWarnings("static-access")
 	public static void main(String[] args) {
 		// Check the command-line arguments.
 		if (args.length < 3 || args.length > 6) {
 			System.err.println("ERROR: wrong number of command-line "
 					+ "arguments.");
 			System.err
 					.println("USAGE: java -jar PlayGame.jar <map_file_name> "
 							+ "\"java <player_one>\" "
 							+ "\"java <player_two>\" [<game_mode>] [<max_num_turns>] [<max_turn_time>] ");
 			System.err.println("");
 			System.err.println("Explanation:");
 			System.err
 					.println("<map_file_name>:        Location of .txt file of map to use for this game");
 			System.err
 					.println("\"java <player_one>\":  Player1. Make sure to add quotes, and add the 'java' part before the bot name. Also make sure your bot is actually compiled (there should be a .class file of your bot file)");
 			System.err.println("\"java <player_two>\":  Idem");
 			System.err.println("Optional:");
 
 			System.err
 					.println("<game_mode>:            Game mode to run in. Options are: 'parallel' and 'serial'. Serial (used in week 1) means for each turn, first player1 makes a move, and then player2. Parallel (used in week2) means for every turn, each player makes a move at the same time. Default: serial");
 			System.err
 					.println("<max_num_turns>:        Maximum number of turns this game may take. Default: 100");
 			System.err
 					.println("<max_turn_time>:        Maximum number of time a bot is allowed to take per turn. Default: 1000");
 			System.exit(1);
 		}
 		// Initialize the game. Load the map.
 		String mapFilename = args[0];
 		int maxNumTurns = 100;
 		int maxTurnTime = 1000;
 		String logFilename = "log.txt";
 		int gameMode = GAME_MODE_SERIAL;
 		// optional arguments
 		if (args.length >= 4) {
 			if (args[3].equalsIgnoreCase(("parallel"))) {
 				gameMode = GAME_MODE_PARALLEL;
 			} else if (args[3].equalsIgnoreCase(("serial"))) {
 				gameMode = GAME_MODE_SERIAL;
 			} else {
 				System.err
 						.println("The 4th argument is unknown. This should either be 'parallel' or 'serial'");
 				System.exit(1);
 			}
 		}
 		if (args.length >= 5) {
 			maxNumTurns = Integer.parseInt(args[4]);
 		}
 		if (args.length >= 6) {
 			maxTurnTime = Integer.parseInt(args[5]);
 		}
 
 		Game game = new Game(mapFilename, maxNumTurns, 0, logFilename);
 		if (game.Init() == 0) {
 			System.err.println("ERROR: failed to start game. map: "
 					+ mapFilename);
 		}
 		// Start the client programs (players).
 		List<Process> clients = new ArrayList<Process>();
 		for (int i = 1; i <= 2; ++i) {
 			String command = args[i];
 			Process client = null;
 			try {
 				client = Runtime.getRuntime().exec(command);
 			} catch (Exception e) {
 				client = null;
 			}
 			if (client == null) {
 				KillClients(clients);
 				System.err.println("ERROR: failed to start client: " + command);
 				System.exit(1);
 			}
 
 			clients.add(client);
 		}
 		try {
 			Thread.currentThread().sleep(maxTurnTime);
 		} catch (InterruptedException e1) {
 			//pff, nothing
 		}
 		boolean[] isAlive = new boolean[clients.size()];
 		for (int i = 0; i < clients.size(); ++i) {
 			isAlive[i] = (clients.get(i) != null);
 		}
 
 		System.err.println("Engine entering main game loop. Mode '"+ (gameMode == GAME_MODE_PARALLEL? "parallel": "serial") + "'");
 
 		int numTurns = 0;
 		int ap = 0; // MODIFIED: active player, based on current numTurns
 		// Enter the main game loop.
 		while (game.Winner() < 0) {
 			// Send the game state to the clients.
 			//System.err.println("The game state :");
 			//System.err.print(game);
 			game.WriteLogMessage("Game state turn " + numTurns + ": \n" + game);
 			if (gameMode == GAME_MODE_SERIAL) {
 				// MODIFIED: send game state only to active player (ap)
 				if (clients.get(ap) == null ) {
 					break;
 				}
 				sendGameState(game, clients, ap);
 			} else {
 				for (int i = 0; i < clients.size(); ++i) {
 					if (clients.get(i) == null ) {
 						break;
 					}
 					sendGameState(game, clients, i);
 				}
 				
 
 			}
 
 			// Get orders from the clients.
 			StringBuilder[] buffers = new StringBuilder[clients.size()];
 			boolean[] clientDone = new boolean[clients.size()];
 			for (int i = 0; i < clients.size(); ++i) {
 				buffers[i] = new StringBuilder();
 				clientDone[i] = false;
 			}
 			long startTime = System.currentTimeMillis();
 	        while (!clientsDone(clientDone, gameMode, ap) ) {
 
 	        	if( System.currentTimeMillis() - startTime > maxTurnTime){
 	        		//check client done
 	        		for (int i = 0; i < clientDone.length; ++i) {
 	        		    if (!clientDone[i]) {
 	        		    	System.err.println("Client " + (ap+1) + " timeout: you missed a turn! Consider to make your bot faster, or increase the maxTurnTime (argument of PlayGame.jar).");
 	        		    }
 	        		}
 	        		break;
 	        	}
 	        	
 //				int i;
 //				int end;
 //				if (gameMode == GAME_MODE_SERIAL) {
 //					// MODIFIED: one player per turn
 //					i = ap; // gets overridden in for loop
 //					end = ap + 1; // not correct
 //				} else {
 //					i = 0;
 //					end = clients.size();
 //				}
 				
 				for (int i = 0; i < clients.size(); ++i) {
 
 					int j = (ap + i) % 2; // required to switch between whom to start first (not to favouritize player1)
 					
 					if (!isAlive[j] || clientDone[j]) {
 						clientDone[j] = true;
 						continue;
 					}
 					
 					// if serial read only the active player
 					if (gameMode == GAME_MODE_SERIAL) {
 						if (j != ap)
 							continue;
 					}
 					
 					
 					
 					// if it's parallel read both
 					try {
 						InputStream inputStream = clients.get(j)
 								.getInputStream();
 						while (inputStream.available() > 0) {
 							char c = (char) inputStream.read();
 							if (c == '\n') {
 								String line = buffers[j].toString().trim();
 								// System.err.println("P" + (i+1) + ": " +
 								// line);
 								line = line.toLowerCase().trim();
 								game.WriteLogMessage("player" + (j + 1) + " > engine: " + line);
 //								System.err.println("player" + (j + 1)
 //										+ " > engine: " + line);
 //								System.err.flush();
 								// Modified: only process 1 order
                                 if (line.equals("go")) {
         							buffers[j] = new StringBuilder();
                                 } else {
                                 	game.IssueOrder(j + 1, line);
                                 	clientDone[j] = true;
         							buffers[j] = new StringBuilder();
     								break;
                                 }
 
 							} else {
 								buffers[j].append(c);
 							}
 						}
						printBotDebugOutput(clients, j);
 					} catch (Exception e) {
 						System.err.println("WARNING: player " + (j + 1)
 								+ " crashed.");
 						clients.get(j).destroy();
 						game.DropPlayer(j + 1);
 						isAlive[j] = false;
 					}
 				}
 			}
 			// MODIFIED: update active player
 			ap = (ap + 1) % 2;
 
 //			for (int j = 0; j < clients.size(); ++j) {
 //				if (!isAlive[j] || !game.IsAlive(j + 1)) {
 //					continue;
 //				}
 //				if (clientDone[j]) {
 //					continue;
 //				}
 //				// Do NOT drop players at timeouts
 //				/*
 //				 * System.err.println("WARNING: player " + (i+1) +
 //				 * " timed out."); clients.get(i).destroy(); game.DropPlayer(i +
 //				 * 1); isAlive[i] = false;
 //				 */
 //			}
 			
 			// Keep advancing turns, until there are no ships in flight.
 			// This way each player has complete knowledge of game state on
 			// start
 			while (game.getFleets().size() != 0) {
 				game.skipTimeStep();
 			}
 			
 			++numTurns;
 			System.err.println("Turn " + numTurns);
 			System.out.print(game.FlushGamePlaybackString());
 			System.out.flush();
 			game.DoTimeStep();
 
 
 		}
 		KillClients(clients);
 		if (game.Winner() > 0) {
 			System.err.println("Player " + game.Winner() + " Wins!");
 		} else {
 			System.err.println("Draw!");
 		}
 		System.out.println(game.GamePlaybackString());
 	}
 	
 	
 	
     public void WriteLogMessage(String logFilename, String message) {
     	
         if (logFilename == null) {
           return;
         }
         try {
     	  BufferedWriter logFile = new BufferedWriter(new FileWriter(logFilename, true));
           logFile.write(message);
           logFile.newLine();
           logFile.flush();
           logFile.close();
         } catch (Exception e) {
           // whatev
         }
       }
     
     private static void sendGameState(Game game, List<Process> clients, int clientId) {
     	String message = game.PovRepresentation(clientId + 1) + "go\n";
 		try {
 		    OutputStream out = clients.get(clientId).getOutputStream();
 		    OutputStreamWriter writer = new OutputStreamWriter(out);
 		    writer.write(message, 0, message.length());
 		    writer.flush();
 		    //System.err.println("engine > player" + (clientId + 1) +"  : " +  message);
 		    game.WriteLogMessage("engine > player" + (clientId + 1) +": \n" +  message);
 		} catch (Exception e) {
 		    clients.set(clientId, null);
 		}
     }
     
    private static void printBotDebugOutput(List<Process> clients, int clientId) throws IOException {    	
     	StringBuilder buf = new StringBuilder();
        InputStream stderr = clients.get(clientId).getErrorStream();
         while (stderr.available() > 0){
             char c = (char)stderr.read();
             if (c == '\n') {
                 String ln = buf.toString();
                System.err.println("Player " + (clientId+1) + ": " + ln);
                 buf = new StringBuilder();
             }
             else {
                 buf.append(c);
             }
         }
     }
 }
