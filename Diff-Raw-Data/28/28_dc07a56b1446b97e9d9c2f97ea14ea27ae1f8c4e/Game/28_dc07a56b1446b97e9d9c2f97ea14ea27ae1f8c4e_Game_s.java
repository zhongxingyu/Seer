 /*
  * Copyright 2013 Dan Mercer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.themercerbros.liarsdice.desktop;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.UnknownHostException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 
 import com.themercerbros.liarsdice.desktop.io.Broadcaster;
 import com.themercerbros.liarsdice.desktop.io.IO;
 import com.themercerbros.liarsdice.desktop.player.ComputerPlayer;
 import com.themercerbros.liarsdice.desktop.player.HumanPlayer;
 import com.themercerbros.liarsdice.desktop.player.Player;
 import com.themercerbros.liarsdice.desktop.player.RemotePlayer;
 import com.themercerbros.liarsdice.desktop.player.RemotePlayer.Quit;
 
 public class Game implements Broadcaster {
 	private static final String ARG_THINK_OUT_LOUD = "--think-out-loud";
 	private static final String ARG_NUM_OF_COMPUTERS = "--num-of-computers";
 	private static final String ARG_NO_REMOTES = "--no-remotes";
 	private static final String ARG_NAME = "--name";
 
 	public static final int NUM_OF_SIDES = 6;
 	private static final int MAX_DICE_PER_PLAYER = 99;
 
 	private final IO io = new IO();
 
 	private final int dicePerPlayer;
 	private final ArrayList<Player> players;
 
 	private int diceInPlay = 0;
 	private int lengthOfLongestName = 0;
 
 	private ServerSocket server;
 	private final ArrayList<Player> listeningPlayers;
 
 	public Game(int port, int numOfDice, Map<String, String> argVals, ArrayList<String> args) {
 		if (numOfDice <= 0 || numOfDice > MAX_DICE_PER_PLAYER) {
 			dicePerPlayer = io.askForInt("How many dice per player?", 1, MAX_DICE_PER_PLAYER + 1);
 		} else {
 			dicePerPlayer = numOfDice;
 		}
 		
 		players = new ArrayList<Player>();
 		listeningPlayers = new ArrayList<Player>();
 		
 		// Add human player
 		final HumanPlayer human;
 		if (argVals.containsKey(ARG_NAME)) {
 			human = new HumanPlayer(dicePerPlayer, argVals.get(ARG_NAME));
 		} else {
 			human = new HumanPlayer(dicePerPlayer);
 		}
 		players.add(human);
 		listeningPlayers.add(human);
 
 		// Add remote players		
 		if (!args.contains(ARG_NO_REMOTES) && io.askBoolean("Do you want to add a remote human player?")) {
 			try {
 				io.say("Using address " + InetAddress.getLocalHost().getHostAddress());
 			} catch (UnknownHostException e1) {
 				io.say("Unknown IP address");
 			}
 			try {
 				server = new ServerSocket(port);
 				io.say("Using port " + port);
 			} catch (IOException e) {
 				io.say("Error listening on port " + port);
 				server = null;
 			}
 			if (server != null) {
 				do {
 					RemotePlayer remote;
 					try {
 						remote = new RemotePlayer(dicePerPlayer, server, human.getName());
 					} catch (IOException e) {
 						boolean retry = io.askBoolean("Connection error. Retry?");
 						if (retry) {
 							continue;
 						} else {
 							break;
 						}
 					}
 					io.say(remote.getName() + " joined the game.");
 					players.add(remote);
 					listeningPlayers.add(remote);
 				} while (io.askBoolean("Add another remote player?"));
 			}
 		} else {
 			server = null;
 		}
 		
 		if (args.contains(ARG_THINK_OUT_LOUD)) {
 			ComputerPlayer.thinkOutLoud = true;
 		}
 		int numOfComputers = -1;
 		if (argVals.containsKey(ARG_NUM_OF_COMPUTERS)) {
 			try {
 				numOfComputers = Integer.parseInt(argVals.get(ARG_NUM_OF_COMPUTERS));
 			} catch (NumberFormatException e) {
 				// Do nothing
 			}
 		}
 		int minNumOfComputers = players.size() == 1 ? 1 : 0;
 		if (numOfComputers < minNumOfComputers){
 			numOfComputers = io.askForInt("How many computer players do you want?", minNumOfComputers, 1024);
 		}
 		for (int i = 0; i < numOfComputers; i++) {
 			addComputerPlayer(dicePerPlayer);
 		}
 		
 		// Count dice in play
 		diceInPlay = dicePerPlayer * players.size();
 		
 		// Randomize players
 		Collections.shuffle(players, new SecureRandom());
 		
 		// Get length of longest name
 		for (Player p : players) {
 			lengthOfLongestName = Math.max(lengthOfLongestName,
 					p.getName().length());
 		}
 	}
 	
 	private void addComputerPlayer(int numOfDice) {
 		Player p = new ComputerPlayer(numOfDice);
 		io.say("Computer player \"%s\" joined the game.", p.getName());
 		players.add(p);
 	}
 	
 	public void play() {
 		int playerIndex = 0;
 
 		int roundCounter = 1;
 		// This loop repeats until the game ends (i.e. players.size() == 1)
 		game:
 		while (true) {
 			msg("");
 			msg("=================================================");
 			msg("                 ROUND " + (roundCounter++));
 			msg("=================================================");
 			msg("There are " + diceInPlay + " dice in play.");
 
 			for (Player p : players) {
 				int diceCount = p.getDiceRolls().length;
 				int percent = diceCount * 100 / diceInPlay;
 				msg("%-" + lengthOfLongestName + "s has %d dice. (%d%%)\t%s", p.getName(), 
 						diceCount, percent, getBar(diceCount, dicePerPlayer));
 			}
 
 			for (Player p : players) {
 				p.rollDice();
 			}
 			
 			Guess lastGuess = null;
 			Player lastPlayer = null;
 			
 			// This loop repeats until the round ends (i.e. a Call is thrown)
 			round:
 			while (true) { // Breaks out when catches a Call
 				// Wrap around if reached the end of list
 				if (playerIndex >= players.size()) {
 					playerIndex = 0;
 				}
 				Player currentPlayer = players.get(playerIndex);
 				msg("");
 				msg("It's " + currentPlayer.getName() + "'s turn!");
 
 				Guess next;
 				try {
 					do {
 						next = currentPlayer.takeTurn(lastGuess, diceInPlay);
 						if (next == null) {
 							throw new NullPointerException(currentPlayer.getClass().getSimpleName() + ".takeTurn() returned null");
 						}
 						if (next.number > NUM_OF_SIDES) {
 							throw new IllegalArgumentException(currentPlayer.getClass().getSimpleName() + " guessed a nonexistent number");
 						}
 						if (!(lastGuess == null || lastGuess.isValidNextGuess(next))) {
 							currentPlayer.tell("That is not a valid guess.");
 						} else {
 							break;
 						}
 					} while (true);
 
 					// Successful guess
 					lastPlayer = currentPlayer;
 					lastGuess = next;
 					msg("-- " + lastPlayer.getName() + " guessed " + next.toHumanString());
 					playerIndex += 1; // Increment playerIndex
 					
 				} catch (Call c) {
 					if (lastPlayer == null) {
 						msg("ERROR: " + currentPlayer.getClass().getSimpleName() + " threw a Call when guess == null");
 						System.exit(-1);
 					}
 
 					msg(currentPlayer.getName() + " called out " + lastPlayer.getName());
 					boolean wasRight = checkGuess(lastGuess);
 					if (wasRight) {
 						lastPlayer.onSuccessfulDefense();
 						msg(lastPlayer.getName() + " guessed correctly!");
						msg(currentPlayer.getName() + " loses one die.");
						currentPlayer.loseOne();
						if (currentPlayer.isOut()) {
							currentPlayer.onLose();
							msg(currentPlayer.getName() + " is out!");
							players.remove(currentPlayer);
						}
 					} else {
 						currentPlayer.onSuccessfulOffense();
 						msg(lastPlayer.getName() + " did not guess correctly!");
						msg(lastPlayer.getName() + " loses one die.");
						lastPlayer.loseOne();
						if (lastPlayer.isOut()) {
							lastPlayer.onLose();
							msg(lastPlayer.getName() + " is out!");
							players.remove(lastPlayer);
						}
 					}
 					diceInPlay--; 
 					break round;
 
 				} catch (Quit e) {
 					players.remove(currentPlayer);
 					listeningPlayers.remove(currentPlayer);
 					diceInPlay -= currentPlayer.getDiceCount();
 					msg(currentPlayer.getName() + " left the game.");
 					
 					if (players.size() == 1) {
 						// only one player left.
 						Player lastOneStanding = players.get(0);
 						msg(lastOneStanding.getName() + " is the last player standing. That's the game!");
 						break game;
 					}
 				}
 			}
 			
 			if (players.size() == 1) {
 				// player won!
 				Player winner = players.get(0);
 				winner.onWin();
 				msg(winner.getName() + " won!");
 				break game;
 			}
 		}
 		
 		for (Player p : listeningPlayers) {
 			if (p instanceof RemotePlayer) {
 				((RemotePlayer) p).disconnect();
 			}
 		}
 	}
 
 	private static String getPercentageBar(int percent) {
 		StringBuilder sb = new StringBuilder("[");
 		for (int i = 5; i <= 100; i += 5) {
 			if (i <= percent) {
 				sb.append("=");
 			} else {
 				sb.append(" ");
 			}
 		}
 		sb.append("]");
 		return sb.toString();
 	}
 	
 	private static String getBar(int filled, int total) {
 		StringBuilder sb = new StringBuilder("[");
 		for (int i = 0; i < filled; i++) {
 			sb.append("=");
 		}
 		for (int i = filled; i < total; i++) {
 			sb.append(" ");
 		}
 		sb.append("]");
 		return sb.toString();
 	}
 
 	private boolean checkGuess(Guess lastGuess) {
 		final int number = lastGuess.number;
 		
 		int[] dice;
 		int count = 0;
 		for (Player p : players) {
 			dice = p.getDiceRolls();
 			final int diceLength = dice.length;
 			int playerDiceCount = 0;
 			for (int i = 0; i < diceLength; i++) {
 				if (dice[i] == number) {
 					playerDiceCount++;
 				}
 			}
 			msg("%s has %d %s", p.getName(), playerDiceCount, Guess.numberToHumanString(number, true));
 			count += playerDiceCount;
 		}
 		msg("GUESS: " + lastGuess.quantity);
 		msg("TOTAL: " + count);
 		return count >= lastGuess.quantity;
 	}
 	
 	private void msg(String message, Object... args) {
 		if (args != null && args.length != 0) {
 			message = String.format(message, args);
 		}
 		for (Player p : listeningPlayers) {
 			p.tell(message);
 		}
 	}
 
 	public void msg(Player speaker, String message) {
 		for (Player p : listeningPlayers) {
 			if (p != speaker)
 				p.tell(message);
 		}
 	}
 
 }
