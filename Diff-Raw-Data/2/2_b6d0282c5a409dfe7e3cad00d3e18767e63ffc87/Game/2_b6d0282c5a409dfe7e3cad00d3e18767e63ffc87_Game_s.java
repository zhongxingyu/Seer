 package Server.gameModule;
 
 import java.rmi.RemoteException;
 
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import Remote.IPlayer;
 import Remote.IPlayerClient;
 import Remote.RemoteGame;
 import Server.userModule.UserImpl;
 
 /**
  * Implements the rules of the game This is the Remote Game Object (RMI Server)
  * 
  * @author mouhyi
  * 
  */
 public class Game extends UnicastRemoteObject implements RemoteGame {
 
 	public final int ROUNDS = 4;
 
 	// player's seat = player's index in the ArrayList
 	private ArrayList<Player> players;
 	private ArrayList<Player> initPlayers;
 	private int round;
 	// no side pots
 	private double pot;
 	private double curBet;
 	private Deck deck;
 	private double ante;
 	private double bringIn;
 	// index of current player in the arrayList
 	private int curPlayer;
 	// If everybody left is all in
 	private boolean allIn;
 	private int id;
 	// counts the number of matched calls in a betting round
 	private int count;
 
 	private Semaphore sem;
 
 	private ArrayList<IPlayerClient> PClients;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param ante
 	 * @param bringIn
 	 * @param list
 	 */
 	public Game(double ante, double bringIn, List<Player> list, int id)
 			throws RemoteException {
 		players = new ArrayList<Player>(list);
 		initPlayers = new ArrayList<Player>(list);
 		this.ante = ante;
 		this.bringIn = bringIn;
 		curBet = 0;
 		curPlayer = 0;
 		round = 1;
 		deck = new Deck();
 		pot = 0.0;
 		this.id = id;
 		PClients = new ArrayList<IPlayerClient>();
 		count = 0;
 
 		sem = new Semaphore(1);
 		try {
 			sem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void reset() {
 		players = new ArrayList<Player>(initPlayers);
 		curBet = 0;
 		curPlayer = 0;
 		round = 1;
 		deck = new Deck();
 		pot = 0.0;
 		this.id++;
 
 		count = 0;
 
 		sem = new Semaphore(1);
 		try {
 			sem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void addPlayers(List<Player> list) {
 		for (Player p : list) {
 			if (!players.contains(p)) {
 				players.add(p);
 				initPlayers.add(p);
 			}
 		}
 	}
 
 	public IPlayerClient getPClient(int id) {
 		for (IPlayerClient pcl : PClients) {
 			try {
 				if (pcl.getUserId() == id) {
 					return pcl;
 				}
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Runs a five card stud Game
 	 */
 	@Override
 	public void play() throws RemoteException {
 
 		for (IPlayerClient pcl : PClients) {
 			pcl.InitiateGameDisplay();
 		}
 
 		// ante
 		collectAnte();
 
 		// update clients
 		for (IPlayerClient pcl : PClients) {
 			pcl.updateDuringRound("Ante collected");
 		}
 
 		// first round
 
 		round = 1;
 		curPlayer = doFirstRound();
 
 		for (IPlayerClient pcl : PClients) {
 			pcl.updateAfterRound("Round 1");
 			// /pcl.updateAfterRound("1 st call");
 		}
 
 		curBet = bringIn;
 		players.get(curPlayer).bet(bringIn);
 
 		for (IPlayerClient pcl : PClients) {
 			// pcl.updateDuringRound("Player has bet bringin ");
 			try {
 				pcl.updateDuringRound(""
 						+ (new UserImpl()).getUserObject(
 								players.get(curPlayer).getUserId()).getName()
 						+ "  has bet the bring in");
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// END First Round
 
 		curPlayer = getNextPlayer();
 		count = 1;
 
 		// this line showed me the bug
 		// while(true);
 
 		doBetting();
 		round = 2;
 
 		// round: 2,3,4
 		while (round <= ROUNDS) {
 			if (players.size() < 2) {
 				break;
 			}
 			if (allIn) {
 				allInShowdown();
 				round++;
 				break;
 			}
 			count = 0;
 			curBet = 0;
 
 			this.deal();
 
 			for (IPlayerClient pcl : PClients) {
 				pcl.updateAfterRound("ROUND " + round);
 
 			}
 
 			// case of a tie
 			ArrayList<Player> tmp = this.getBestHand();
 			Player PBesthand = tmp.get(tmp.size() - 1);
 			curPlayer = players.indexOf(PBesthand);
 
 			doBetting();
 			round++;
 		}
 
 		// notify players of the winners & amount won and divide pot
 		if (players.size() < 2) {
 
 			// this.getWinner();
 
 			players.get(0).addChips(pot);
 			try {
 				players.get(0).updateChips();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			for (IPlayerClient pcl : PClients) {
 				try {
 					pcl.updateAfterRound("Winner is "
 							+ (new UserImpl()).getUserObject(
 									players.get(curPlayer).getUserId())
 									.getName());
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 
 		} else {
 			ArrayList<Player> winners = this.getWinner();
 			String win = "Winner: ";
 			for (Player p : winners) {
 				p.addChips(pot / winners.size());
 				try {
 					// p.updateChips();
 					win += (new UserImpl()).getUserObject(p.getUserId())
 							.getName()
 							+ "  ";
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 
 			for (Player p : players) {
 				try {
 					p.updateChips();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 
 			for (IPlayerClient pcl : PClients) {
 				// should be afterRound
 				pcl.updateAfterRound(win);
 			}
 
 		}
 
 		// update stats
 		for (Player p : players) {
 			try {
 				int id = p.getUserId();
 				double initChips = p.getIniChips();
 				boolean won = (p.getChips() - initChips > 0);
 				Data.Statistics.updateUserStatistics(id, p.getChips()
 						- initChips, won);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// Game over
 
 		// clear old game data
		this.reset();
 
 	}
 
 	public int getNextPlayer() {
 		return (curPlayer + 1) % players.size();
 	}
 
 	/**
 	 * Remotes client
 	 * 
 	 * @param userId
 	 * @throws RemoteException
 	 */
 	public void removePlayerProxy(int userId) throws RemoteException {
 		PClients.remove(this.getPClient(userId));
 	}
 
 	/**
 	 * deletes a player from this game. Should be called when a player folds out
 	 * or he has been inactive for a long period specified in this class
 	 * 
 	 * @param player
 	 *            - player to be removed
 	 * @author mouhyi
 	 * @throws SQLException
 	 */
 	@Override
 	public void removePlayer(int userId) throws RemoteException {
 		Player player = this.getPlayer(userId);
 		String Pname = "";
 		double initChips = 0;
 		if (player != null) {
 			try {
 				Pname = (new UserImpl()).getUserObject(
 						this.getPlayer(userId).getUserId()).getName();
 				initChips = (new UserImpl()).getUserObject(
 						this.getPlayer(userId).getUserId()).getChips();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			synchronized (players) {
 				if (player != null) {
 					players.remove(player);
 					player.confirmBet();
 					try {
 						player.updateChips();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			for (IPlayerClient pcl : PClients) {
 				pcl.updateAfterRound(Pname + " has FOLDED");
 			}
 
 			try {
 				Data.Statistics.updateUserStatistics(userId, player.getChips()
 						- initChips, false);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// notify doBetting
 		sem.release();
 
 	}
 
 	/**
 	 * Updates the game state with a player calling the current bet
 	 * 
 	 * @param userId
 	 *            - userId of the player making the bet
 	 * @return 0 if player has sufficient chips, -1 otherwise, -2 if it's not
 	 *         player's turn
 	 */
 	@Override
 	public int call(int userId) throws RemoteException {
 
 		if ((!this.getPlayer(userId).isTurn())
 				|| players.get(curPlayer).getUserId() != userId) {
 			return -2;
 		}
 
 		Player player = this.getPlayer(userId);
 		if (player == null || player.bet(curBet) == -1) {
 			return -1;
 		}
 
 
 		for (IPlayerClient pcl : PClients) {
 			try {
 				pcl.updateDuringRound(""
 						+ (new UserImpl()).getUserObject(userId).getName()
 						+ " CALLS");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		count++;
 		this.getPlayer(userId).setDone(true);
 		this.getPlayer(userId).setTurn(false);
 
 		sem.release();
 
 		return 0;
 	}
 
 	/**
 	 * Updates the game state whith a player raising the current bet
 	 * 
 	 * @param userId
 	 *            - userId of the player making the bet player
 	 * @param bet
 	 *            - value of the raise
 	 * @return 0 on success, -1 on failure, -2 if it's not player's turn
 	 */
 	@Override
 	public int raise(int userId, double bet) throws RemoteException {
 
 		if ((!this.getPlayer(userId).isTurn())
 				|| players.get(curPlayer).getUserId() != userId) {
 			return -2;
 		}
 		Player player = this.getPlayer(userId);
 		if (player == null) {
 			return -1;
 		}
 		if (player.bet(bet) == 0) {
 			curBet = bet;
 			count = 1;
 		} else {
 			return -1;
 		}
 
 
 		for (IPlayerClient pcl : PClients) {
 			try {
 				pcl.updateDuringRound(""
 						+ (new UserImpl()).getUserObject(userId).getName()
 						+ "  RAISES WITH " + bet);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		sem.release();
 
 		return 0;
 	}
 
 	/**
 	 * Flag the game when a player goes all in
 	 * 
 	 * @param userId
 	 *            - player all in
 	 */
 	@Override
 	public int allIn(int userId) throws RemoteException {
 
 		if (!this.getPlayer(userId).isTurn()) {
 			return -2;
 		}
 
 		for (IPlayerClient pcl : PClients) {
 			try {
 				pcl.updateDuringRound((new UserImpl()).getUserObject(userId)
 						.getName()
 						+ " is all in");
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		allIn = true;
 		curBet = getPlayer(userId).getChips();
 		count++;
 
 		sem.release();
 		return 0;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void allInShowdown() {
 		for (int i = round; i <= ROUNDS; i++) {
 			this.deal();
 		}
 	}
 
 	/**
 	 * Should be called at the end of each betting round to update players chips
 	 * and add the bets to the pot
 	 */
 	public void confirmBet() {
 		for (Player p : players) {
 			p.bet(curBet);
 			p.confirmBet();
 		}
 		pot += curBet * players.size();
 		curBet = 0;
 	}
 
 	/**
 	 * Fetches this game's list of players for a specific player
 	 * 
 	 * @param userId
 	 * @return player with corresponding userId or null if user not found
 	 * @author mouhyi
 	 */
 	public Player getPlayer(int userId) throws RemoteException {
 		for (Player p : players) {
 			try {
 				if (p.getUserId() == userId) {
 					return p;
 				}
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	// TODO implement doBetting() used in each round & Game Controller (RMI
 	// callbacks)
 
 	/**
 	 * Determines the winner(s) of this game. If there is only one player
 	 * remaining, then he is the winner. Otherwise, there is a showdown to
 	 * determine the player with the highest hand value. This method should be
 	 * called only at the end of the game
 	 * 
 	 * @return the winner of this game
 	 * @author mouhyi
 	 */
 	public ArrayList<Player> getWinner() {
 		for (Player p : players) {
 			p.mergeHand();
 		}
 		return this.getBestHand();
 
 	}
 
 	/**
 	 * Determines the player(s) with the best face up hand in this round
 	 * 
 	 * @return ArrayList<Player>
 	 * @author mouhyi
 	 */
 	public ArrayList<Player> getBestHand() {
 		ArrayList<Player> playersCpy = new ArrayList<Player>(players);
 		Collections.sort(playersCpy);
 		ArrayList<Player> best = new ArrayList<Player>();
 		Player bestPlayer = playersCpy.get(playersCpy.size() - 1);
 		int i = playersCpy.size() - 1;
 		// check == here
 		while (i >= 0 && playersCpy.get(i) == bestPlayer) {
 			best.add(playersCpy.get(i));
 			i--;
 		}
 		return best;
 	}
 
 	/**
 	 * Determines the index of the first player to bet(highest hand) in the
 	 * current round. If there is a tie, the player with lowest seat number is
 	 * returned Must be called at the beginning of each round.
 	 * 
 	 * @return the player betting first
 	 * @author mouhyi
 	 */
 	public int firstToBet() {
 		ArrayList<Player> best = this.getBestHand();
 		int minSeat = -1;
 		for (Player p : best) {
 			if (players.indexOf(p) < minSeat) {
 				minSeat = players.indexOf(p);
 
 			}
 		}
 		return minSeat;
 	}
 
 	/**
 	 * Collect the ante and puts it in the main pot
 	 * 
 	 * @author mouhyi
 	 */
 	public void collectAnte() {
 		for (Player p : players) {
 			p.bet(ante);
 			p.confirmBet();
 			pot += ante;
 		}
 	}
 
 	/**
 	 * Each player is dealt one card face down, followed by one card face up
 	 * Then determines the player with the lowest-ranking up card
 	 * 
 	 * @return minSeat - index of player with lowest up card
 	 * @author mouhyi
 	 */
 	public int doFirstRound() {
 		for (Player p : players) {
 			p.getCard(deck.drawCard(), true);
 		}
 		Player minPlayer = null;
 		Card minCard = null;
 		for (Player p : players) {
 			Card tmp = deck.drawCard();
 			p.getCard(tmp, false);
 			if ((minCard == null) || (minCard.compareBySuit(tmp) > 0)) {
 				minCard = tmp;
 				minPlayer = p;
 			}
 		}
 		return players.indexOf(minPlayer);
 	}
 
 	/**
 	 * Deals a card to each player in the game
 	 * 
 	 * @author mouhyi
 	 * 
 	 */
 	public void deal() {
 		for (Player p : players) {
 			p.getCard(deck.drawCard(), false);
 		}
 	}
 
 	/**
 	 * 
 	 * @param count
 	 *            - number of calls to the last bet
 	 */
 	// TODO: implement RMI
 	public void doBetting() {
 
 		// RMI
 		// betting goes in increasing indices and wraps around
 		// chips are added to pot at the end in confirmBet
 		// or immedialtely if a player folds
 		
 		 // notify curplayer(callBack) wait for a flag
 		 // if player folds(or timedout: // TODO) remove him from players and confirm his bet and update pot
 		 //else curBet = player's bet (error if bet<curBet) count++ for call,  count=1 for a raise 
 		 //repeat until count = players.size or all-in=true, or one player left 
 		//confirmBet
 
 		while (count < players.size()) {
 			players.get(curPlayer).setTurn(true);
 			players.get(curPlayer).setDone(false);
 
 			for (IPlayerClient pcl : PClients) {
 				try {
 					pcl.updateDuringRound("It is "
 							+ (new UserImpl()).getUserObject(
 									players.get(curPlayer).getUserId())
 									.getName() + "'s turn.");
 
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				} catch (SQLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			try {
 				sem.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			// notify curPlayer and wait for him to play
 			// removes him if timeout
 			// notify the other players
 
 
 			curPlayer = this.getNextPlayer();
 		}
 
 		confirmBet();
 
 		for (IPlayerClient pcl : PClients) {
 			try {
 				pcl.updateDuringRound("Round " + round + " DONE");
 
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	// Getters
 	public ArrayList<IPlayer> getPlayers() throws RemoteException {
 		synchronized (players) {
 			ArrayList<IPlayer> cpy = new ArrayList<IPlayer>();
 			for (Player p : players) {
 				cpy.add((IPlayer) p);
 			}
 			return cpy;
 		}
 	}
 
 	// Getters
 	public int getRound() throws RemoteException {
 		return round;
 	}
 
 	public double getPot() throws RemoteException {
 		return pot;
 	}
 
 	public double getCurBet() throws RemoteException {
 		return curBet;
 	}
 
 	public double getAnte() throws RemoteException {
 		return ante;
 	}
 
 	public double getBringIn() throws RemoteException {
 		return bringIn;
 	}
 
 	public int getCurPlayerId() throws RemoteException {
 		return players.get(curPlayer).getUserId();
 	}
 
 	@Override
 	public void registerPlayer(IPlayerClient p) throws RemoteException {
 		synchronized (PClients) {
 			PClients.add(p);
 		}
 
 	}
 
 	public void sendMessage(String from, String message) {
 		for (IPlayerClient element : PClients) {
 			try {
 				element.getChatMessage(from, message);
 			} catch (RemoteException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public boolean isPlayerIn(int userId) throws RemoteException {
 		return (this.getPlayer(userId) == null) ? false : true;
 	}
 
 }
