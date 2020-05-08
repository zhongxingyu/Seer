 /*
  * wePoker: Play poker with your friends, wherever you are!
  * Copyright (C) 2012, The AmbientTalk team.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2 of the License, or (at your
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package edu.vub.at.nfcpoker;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import android.util.Log;
 
 import com.esotericsoftware.kryonet.Connection;
 
 import edu.vub.at.commlib.CommLib;
 import edu.vub.at.commlib.Future;
 import edu.vub.at.nfcpoker.comm.Message;
 import edu.vub.at.nfcpoker.comm.Message.ClientAction;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionMessage;
 import edu.vub.at.nfcpoker.comm.Message.ClientActionType;
 import edu.vub.at.nfcpoker.comm.Message.ReceiveHoleCardsMessage;
 import edu.vub.at.nfcpoker.comm.Message.ReceivePublicCards;
 import edu.vub.at.nfcpoker.comm.Message.RequestClientActionFutureMessage;
 import edu.vub.at.nfcpoker.comm.Message.RoundWinnersDeclarationMessage;
 import edu.vub.at.nfcpoker.comm.Message.StateChangeMessage;
 import edu.vub.at.nfcpoker.ui.ServerViewInterface;
 
 public class PokerGame implements Runnable {
 	
 	@SuppressWarnings("serial")
 	public class RoundEndedException extends Exception {}
 
 	// Blinds
 	private static final int SMALL_BLIND = 5;
 	private static final int BIG_BLIND = 10;
 	
 	// Communication
 	private ConcurrentSkipListMap<Integer, Future<ClientAction>> actionFutures = new ConcurrentSkipListMap<Integer, Future<ClientAction>>();  
 
 	// Connections
 	// private ConcurrentSkipListMap<Integer, Connection> clientsInGame = new ConcurrentSkipListMap<Integer, Connection>();
 	
 	// Rounds
 	public volatile PokerGameState gameState;
 	private List<PlayerState> clientsIdsInRoundOrder = Collections.synchronizedList(new LinkedList<PlayerState>());
 	private ConcurrentSkipListMap<Integer, PlayerState> playerState  = new ConcurrentSkipListMap<Integer, PlayerState>();
 	private int chipsPool = 0;
 	
 	// GUI
 	private ServerViewInterface gui;
 	
 	public PokerGame(ServerViewInterface gui) {
 		this.gameState = PokerGameState.STOPPED;
 		this.gui = gui;
 	}
 	
 	public void run() {
 		while (true) {
 			chipsPool = 0;
 			gui.resetCards();
 			updatePoolMoney();
 			actionFutures.clear();
 			while (clientsIdsInRoundOrder.size() < 2) {
 				try {
 					Log.d("wePoker - PokerGame", "# of clients < 2, changing state to stopped");
 					newState(PokerGameState.WAITING_FOR_PLAYERS);
 					synchronized(this) {
 						this.wait();
 					}
 				} catch (InterruptedException e) {
 					Log.wtf("wePoker - PokerGame", "Thread was interrupted");
 				}
 			}
 
 			List<PlayerState> currentPlayers = new ArrayList<PlayerState>();
 			Set<Card> cardPool = new HashSet<Card>();
 
 			synchronized (clientsIdsInRoundOrder) {
 				currentPlayers.addAll(clientsIdsInRoundOrder);
 			}
 			
 			try {
 				Deck deck = new Deck();
 				
 				// Reset player actions
 				for (PlayerState player : currentPlayers) {
 					player.gameMoney = 0;
 					player.gameHoleCards = null;
 				}
 				
 				// decide on blinds and dealer.
 				PlayerState dealer = currentPlayers.get(currentPlayers.size() - 1);
 				PlayerState smallBlind = currentPlayers.get(0);
 				PlayerState bigBlind = currentPlayers.get(1);
 				gui.setPlayerButtons(dealer, smallBlind, bigBlind);
 
 				addBet(smallBlind, SMALL_BLIND);
 				addBet(bigBlind, BIG_BLIND);
 				broadcast(new Message.TableButtonsMessage(dealer.clientId, smallBlind.clientId, SMALL_BLIND, bigBlind.clientId, BIG_BLIND));
 				
 				// hole cards
 				for (PlayerState player : currentPlayers) {
 					Card preflop[] = deck.drawCards(2);
 					player.gameHoleCards = preflop;
 					Connection c = player.connection;
 					if (c == null) {
 						player.roundActionType = ClientActionType.Fold;
 						continue;
 					}
 					c.sendTCP(new ReceiveHoleCardsMessage(preflop[0], preflop[1]));
 				}
 				newState(PokerGameState.PREFLOP);
 				roundTable();
 				
 				
 				// flop cards
 				Card[] flop = deck.drawCards(3);
 				cardPool.addAll(Arrays.asList(flop));
 				gui.revealCards(flop);
 				broadcast(new ReceivePublicCards(flop));
 				newState(PokerGameState.FLOP);
 				roundTable();
 
 				// turn cards
 				Card[] turn = deck.drawCards(1);
 				cardPool.add(turn[0]);
 				gui.revealCards(turn);
 				broadcast(new ReceivePublicCards(turn));
 				newState(PokerGameState.TURN);
 				roundTable();
 				
 				// river cards
 				Card[] river = deck.drawCards(1);
 				cardPool.add(river[0]);
 				gui.revealCards(river);
 				broadcast(new ReceivePublicCards(river));
 				newState(PokerGameState.RIVER);
 				roundTable();					
 			} catch (RoundEndedException e1) {
 				/* ignore */
 				Log.d("wePoker - PokerGame", "Everybody folded at round " + gameState);
 			}
 			
 			// results
 			boolean endedPrematurely = gameState != PokerGameState.RIVER;
 			newState(PokerGameState.END_OF_ROUND);
 			
 			ArrayList<PlayerState> remainingPlayers = new ArrayList<PlayerState>();
 			for (PlayerState player : currentPlayers) {
 				if (player.roundActionType != ClientActionType.Fold &&
 					player.roundActionType != ClientActionType.Unknown) {
 					remainingPlayers.add(player);
 				}
 			}
 			
 			if (endedPrematurely) {
 				// If only one player left
 				if (remainingPlayers.size() == 1) {
 					final PlayerState lastPlayer = remainingPlayers.get(0);
 					addMoney(lastPlayer, chipsPool);
 
 					List<String> winnerNames = new ArrayList<String>();
 					winnerNames.add(lastPlayer.name);
 					
 					broadcast(new RoundWinnersDeclarationMessage(remainingPlayers, winnerNames, false, null, chipsPool));
 					gui.showWinners(remainingPlayers, chipsPool);
 				} else {
 					Log.wtf("wePoker - PokerGame", "Ended prematurely with more than one player?");
 				}
 			} else {
 				// Calculate who has the best cards
 				TreeMap<PlayerState, Hand> hands = new TreeMap<PlayerState, Hand>();
 				Hand bestHand = null;
 				for (PlayerState player : remainingPlayers) {
 					final Hand playerHand = Hand.makeBestHand(cardPool, Arrays.asList(player.gameHoleCards));
 					if (bestHand == null || bestHand.compareTo(playerHand) < 0)
 						bestHand = playerHand;
 					hands.put(player, playerHand);
 				}
 				if (!hands.isEmpty()) {
 					List<PlayerState> bestPlayers = findWinners(hands, bestHand);
 					
 					List<String> winnerNames = new ArrayList<String>();
 					for (PlayerState player: bestPlayers) {
 						addMoney(player, chipsPool / bestPlayers.size());
 						winnerNames.add(player.name);
 					}
 					
 					broadcast(new RoundWinnersDeclarationMessage(bestPlayers, winnerNames, true, bestHand, chipsPool));
 					gui.showWinners(bestPlayers, chipsPool);
 				}
 			}
 			
 			cycleClientsInGame();
 			
 			// finally, sleep
 			try {
 				Thread.sleep(10000);
 			} catch (InterruptedException e) {
 				Log.wtf("wePoker - PokerGame", "Thread.sleep was interrupted", e);
 			}
 		}
 	}
 
 	public List<PlayerState> findWinners(TreeMap<PlayerState, Hand> hands, final Hand bestHand) {
 		List<PlayerState> bestPlayers = new ArrayList<PlayerState>();
 
 		for (PlayerState nextPlayer : hands.keySet()) {
 			Hand nextHand = hands.get(nextPlayer);
 			if (nextHand.compareTo(bestHand) == 0) {
 				bestPlayers.add(nextPlayer);
 			}
 		}
 
 		return bestPlayers;
 	}
 	
 	private void cycleClientsInGame() {
 		if (clientsIdsInRoundOrder.size() <= 1) return;
 		synchronized (clientsIdsInRoundOrder) {
 			clientsIdsInRoundOrder.add(clientsIdsInRoundOrder.remove(0));
 		}
 	}
 	
 	private void askClientActions(PlayerState player, int round) {
 		if ((player.roundActionType == ClientActionType.Fold) ||
 			(player.roundActionType == ClientActionType.AllIn)) {
 			return;
 		}
 		
 		Future<ClientAction> fut = CommLib.createFuture();
 		actionFutures.put(player.clientId, fut);
 		Log.d("wePoker - PokerGame", "Creating & Sending new future " + fut.getFutureId() + " to " + player.clientId);
 		Connection c = player.connection;
 		if (c == null) {
 			// If client disconnected -> Fold
 			player.roundActionType = ClientActionType.Fold;
 			broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold), player.clientId));
 			return;
 		}
 		c.sendTCP(new RequestClientActionFutureMessage(fut, round));
 		if (fut != null && !fut.isResolved()) {
 			fut.setFutureListener(null);
 		}
 	}
 	
 	private boolean verifyClientActions(PlayerState player, int round, int minBet) {
 		if ((player.roundActionType == ClientActionType.Fold) ||
 			(player.roundActionType == ClientActionType.AllIn)) {
 			return true;
 		}
 		
 		Future<ClientAction> fut = actionFutures.get(player.clientId);
 		if (fut == null) return true;
 		ClientAction ca = fut.get();
 		if (ca == null) return true;
 		
 		switch (ca.actionType) {
 		case Unknown:
 			return false;
 		case Fold:
 		case AllIn:
 			return true;
 		case Check:
 		case Bet:
 			// Client sends diffMoney
 			if (player.roundMoney + ca.extraMoney < minBet) {
 				// Can happen when a player checks before another player bets
 				// We should ask a new action to the player
 				player.roundActionType = ClientActionType.Unknown;
 				return false;
 			} else {
 				return true;
 			}
 		default:
 			Log.d("wePoker - PokerGame", "Unknown client action message (processClientActions)");
 			return false;
 		}
 	}
 	
 	// Processes Client Actions
 	// - Updates money
 	// - Broadcasts actions
 	// Returns minimum bet
 	private int processClientActions(PlayerState player, int round, int minBet) {
 		if (player.roundActionType == ClientActionType.Fold ||
 			player.roundActionType == ClientActionType.AllIn) {
 			return minBet;
 		}
 		
 		Future<ClientAction> fut = actionFutures.get(player.clientId);
 		if (fut == null) {
 			broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold, player.roundMoney, 0), player.clientId));
 			player.roundActionType = ClientActionType.Fold;
 			return minBet;
 		}
 		ClientAction ca = fut.get();
 		if (ca == null) {
 			broadcast(new ClientActionMessage(new ClientAction(ClientActionType.Fold, player.roundMoney, 0), player.clientId));
 			player.roundActionType = ClientActionType.Fold;
 			return minBet;
 		}
 
 		if (ca.handled) {
 			return minBet;
 		}
 
 		ca.handled = true;
 		player.roundActionType = ca.actionType;
 		broadcast(new ClientActionMessage(ca, player.clientId));
 		
 		switch (player.roundActionType) {
 		case Fold:
 			gui.updatePlayerStatus(player);
 			return minBet;
 		case Check: // Or CALL
 		case Bet:
 			// Client sends diffMoney
 			addBet(player, ca.extraMoney);
 			if (player.roundMoney < minBet) {
 				Log.wtf("wePoker - PokerGame", "Invalid extra money");
 				return minBet;
 			}
 			return player.roundMoney;
 		case AllIn:
 			// Client sends diffMoney
 			addBet(player, ca.extraMoney);
 			return minBet;
 		default:
 			Log.d("wePoker - PokerGame", "Unknown client action message");
 			return minBet;
 		}
 	}
 	
 	private void addBet(PlayerState player, int extra) {
 		player.roundMoney += extra;
 		player.money -= extra;
 		if (player.money < 0) {
 			Log.wtf("wePoker - PokerGame", "Player bets more money than he/she owns!");
 			player.money = 0;
 		}
 		Log.d("wePoker - PokerGame", "Player "+player.name+" bets \u20AC"+extra+" extra");
 		gui.updatePlayerStatus(player);
 		addChipsToPool(extra);
 	}
 	
 	private void addMoney(PlayerState player, int extra) {
 		player.money += extra;
 		gui.updatePlayerStatus(player);
 	}
 	
 	// Idea:
 	//   Ask all players in parallel to bet
 	//   Handle cases where player 2 checks before player 1 bets
 	//     -> player2 should perform the action again
 	//   Handle cases where player 1 bets (100) and player 2 raises (200)
 	//     -> Should be handled by the second tableRound (1) && increasedBet
 	//   Stop early if not enough players
 	private void roundTable() throws RoundEndedException {
 		int minBet = 0;
 		boolean increasedBet = true;
 
 		List<PlayerState> clientOrder;
 		synchronized (clientsIdsInRoundOrder) {
 			clientOrder = new LinkedList<PlayerState>(clientsIdsInRoundOrder);
 		}
 
 		if (clientOrder.size() < 2) {
 			throw new RoundEndedException();
 		}
 		
 		// Reset player actions
 		for (PlayerState player : playerState.values()) {
 			player.roundActionType = ClientActionType.Unknown;
 			player.roundMoney = 0;
 			actionFutures.remove(player.clientId);
 		}
 		
 		// Add blinds
 		if (gameState == PokerGameState.PREFLOP) {
 			// Small and big blind bet last in the first round.
 			clientOrder.add(clientOrder.remove(0));
 			clientOrder.add(clientOrder.remove(0));
 		}
 		
 		// Two table rounds if needed
 		for (int tableRound = 0; tableRound < 2 && increasedBet; tableRound++) {
 			int playersRemaining = clientOrder.size();
 			increasedBet = false;
 
 			// Ask all client actions (in parallel)
 			if (tableRound == 0) {
 				for (PlayerState player : clientOrder) {
 					askClientActions(player, tableRound);
 				}
 			}
 			
 			// Process the client action (one-by-one, in round order)
 			for (PlayerState player : clientOrder) {
 				// Keep asking for valid input
 				while (!verifyClientActions(player, tableRound, minBet)) {
 					askClientActions(player, tableRound);
 				}
 				int newMinBet = processClientActions(player, tableRound, minBet);
 				if (newMinBet > minBet) {
 					increasedBet = true;
 					if (tableRound > 1) {
 						Log.wtf("wePoker - PokerGame", "Increased bet in second round?");
 					}
 					minBet = newMinBet;
 				}
 				if (player.roundActionType == ClientActionType.Fold) {
 					playersRemaining--;
 				}
 				if (playersRemaining <= 1) {
 					throw new RoundEndedException();
 				}
 			}
 		}
 	}
 	
 	
 	private void addChipsToPool(int extra) {
 		chipsPool += extra;
 		updatePoolMoney();
 	}
 
 	private void updatePoolMoney() {
 		broadcast(new Message.PoolMessage(chipsPool));
 		gui.updatePoolMoney(chipsPool);
 	}
 
 	private void newState(PokerGameState newState) {
 		Log.v("wePoker - PokerGame", "Updating poker game state"+newState.toString());
 		gameState = newState;
 		broadcast(new StateChangeMessage(newState));
 		gui.updateGameState(newState);
 	}
 
 	public synchronized void addPlayer(Connection c, int clientId, String nickname, int avatar, int money) {
 		Log.v("wePoker - PokerGame", "Adding player "+clientId);
 		PlayerState player = new PlayerState(c, clientId, money, nickname, avatar);
 		playerState.put(clientId, player);
 		clientsIdsInRoundOrder.add(player);
 		gui.addPlayer(player);
 		c.sendTCP(new StateChangeMessage(gameState));
 		this.notify();
 	}
 	
 
 	public synchronized void setNickname(int clientId, String nickname) {
 		PlayerState player = playerState.get(clientId);
 		if (player != null) {
 			player.name = nickname;
 			gui.updatePlayerStatus(player);
 		}
 	}
 	
 	public synchronized void removePlayer(int clientId) {
 		Log.v("wePoker - PokerGame", "Removing player "+clientId);
 		PlayerState player = playerState.get(clientId);
 		if (player != null) {
 			player.connection = null;
 			player.roundActionType = ClientActionType.Fold;
 			gui.removePlayer(player);
 			clientsIdsInRoundOrder.remove(player);
 			playerState.remove(player.clientId);
 		}
 		Future<ClientAction> fut = actionFutures.get(clientId);
 		if (fut != null && ! fut.isResolved()) {
 			fut.resolve(new ClientAction(Message.ClientActionType.Fold, 0, 0));
 		}
 	}
 	
 	public synchronized void broadcast(Message m) {
 		for (PlayerState p : playerState.values()) {
 			Connection c = p.connection;
 			if (c != null)
 				c.sendTCP(m);
 		}
 	}
 }
