 package database.match;
 
 import gui.Language;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import database.players.Player;
 import database.players.Single;
 import database.players.Team2;
 
 /**
  * Represents a match of two players
  * 
  * @author Tobias Denkinger
  * 
  */
 public class Match extends Edge<Player> implements Comparable<Match> {
 
 	public static int STATE_INITIAL = 0;
 	public static int STATE_RUNNING = 1;
 	public static int STATE_FINISHED = 2;
 	
 	private List<Game> games;
 	private int group = -1, maxSentences = 3, state = 0;
 	private Player leftPlayer, rightPlayer, loser, winner;
 	private double priority;
 	private Date startedDate, endedDate;
 
 	private List<Match> submatches;
 	private boolean unsaved = false;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param d
 	 *            the higher this is the earlier it appears in lists
 	 * @param player
 	 *            Player on the left side
 	 * @param player2
 	 *            Player in the right side
 	 */
 	public Match(double d, int group, Player player, Player player2) {
 		priority = d;
 		this.group = group;
 		this.leftPlayer = player;
 		this.rightPlayer = player2;
 		games = new ArrayList<Game>();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param player
 	 *            Player on the left side
 	 * @param player2
 	 *            Player on the right side
 	 */
 	public Match(Player player, Player player2) {
 		this(0, -1, player, player2);
 	}
 
 	private void addSentence(Game sentence) {
 		setUnsaved(true);
 		games.add(sentence);
 		if (getLeftSentences() == maxSentences) {
 			winner = leftPlayer;
 			loser = rightPlayer;
 			winner.addMatch(this);
 			loser.addMatch(this);
 			endGame();
 		}
 		if (getRightSentences() == maxSentences) {
 			winner = rightPlayer;
 			loser = leftPlayer;
 			winner.addMatch(this);
 			loser.addMatch(this);
 			endGame();
 		}
 		if (winner != null)
 			state = STATE_FINISHED;
 	}
 
 	/**
 	 * Adds a sentence to the Game
 	 * 
 	 * @param leftBalls
 	 *            Balls of left Player
 	 * @param rightBalls
 	 *            Balls of right Player
 	 */
 	public void addSentence(int leftBalls, int rightBalls) {
 		addSentence(new Game(leftBalls, rightBalls));
 	}
 
 	public void addSentence(String tendence) {
 		addSentence(new Game(tendence));
 	}
 
  	@Override
  	public int compareTo(Match arg0) {
  		if (!isOK() && arg0.isOK())
  			return -1;
  		if (isOK() && !arg0.isOK())
  			return 1;
  		if (!isOK() && !arg0.isOK())
  			return 0;
  		if (priority > arg0.priority)
  			return 1;
  		if (priority < arg0.priority)
  			return -1;
  		if (startedDate == null || arg0.startedDate == null)
  			return 0;
  		return startedDate.compareTo(arg0.startedDate);
  	}
 
 	@Override
 	public String edgePrintBottom() {
 		String result = "";
 		if (state == STATE_FINISHED)
 			for (int i = 0; i < games.size(); i++) {
 				result += games.get(i).getTendence();
 				if (i != games.size() - 1)
 					result += ",";
 			}
 		return result;
 	}
 
 	@Override
 	public String edgePrintTop() {
 		if (state == STATE_FINISHED)
 			return getLeftSentences() + ":" + getRightSentences();
 		return "";
 	}
 
 	/**
 	 * Ends the Game
 	 */
 	public void endGame() {
 		state = STATE_FINISHED;
 		if (endedDate == null)
 			endedDate = new Date();
 		setUnsaved(true);
 	}
 
 	/**
 	 * Returns if games are equal
 	 * 
 	 * @param g
 	 *            game to compare to
 	 * @return true if games are equal, false otherwise
 	 */
 	public boolean equals(Match g) {
 		return (g.getLeft().equals(getLeft()) & g.getRight().equals(getRight()))
 				| (g.getLeft().equals(getRight()) & g.getRight().equals(
 						getLeft()));
 	}
 
 	@Override
 	public boolean equals(Object e) {
 		return toString().equals(e.toString());
 	}
 
 	/**
 	 * Returns the group the game is assigned to
 	 * 
 	 * @return group or -1 if not assigned to a group
 	 */
 	public int getGroup() {
 		return group;
 	}
 
 	@Override
 	public Player getLeft() {
 		return getLeftPlayer();
 	}
 
 	/**
 	 * Balls won by the left Player
 	 * 
 	 * @return left Player's balls
 	 */
 	public int getLeftBalls() {
 		int result = 0;
 		for (Game g : games)
 			result += g.getLeftBalls();
 		return result;
 	}
 
 	/**
 	 * Player on the left side
 	 * 
 	 * @return Player on the left side
 	 */
 	public Player getLeftPlayer() {
 		return leftPlayer;
 	}
 
 	/**
 	 * Sentences won by the left Player
 	 * 
 	 * @return left Player's sentences
 	 */
 	public int getLeftSentences() {
 		int result = 0;
 		for (Game g : games)
 			if (g.getLeftBalls() > g.getRightBalls())
 				result++;
 		return result;
 	}
 
 	/**
 	 * Return the games priority
 	 * 
 	 * @return the games priority
 	 */
 	public double getPriority() {
 		return priority;
 	};
 
 	@Override
 	public Player getRight() {
 		return getRightPlayer();
 	};
 
 	/**
 	 * Balls won by the right Player
 	 * 
 	 * @return right Players balls
 	 */
 	public int getRightBalls() {
 		int result = 0;
 		for (Game g : games)
 			result += g.getRightBalls();
 		return result;
 	};
 
 	/**
 	 * Player on the right side
 	 * 
 	 * @return Player on the right side
 	 */
 	public Player getRightPlayer() {
 		return rightPlayer;
 	};
 
 	/**
 	 * Sentences won by the right Player
 	 * 
 	 * @return right Player's sentences
 	 */
 	public int getRightSentences() {
 		int result = 0;
 		for (Game g : games)
 			if (g.getLeftBalls() < g.getRightBalls())
 				result++;
 		return result;
 	};
 
 	/**
 	 * The Game's state
 	 * 
 	 * @return the Game's state
 	 */
 	public int getState() {
 		return state;
 	};
 
 	public Date getTimeRunning() {
 		if (endedDate != null)
 			return new Date(endedDate.getTime() - startedDate.getTime());
 		return new Date((new Date()).getTime() - startedDate.getTime());
 	}
 
 	@Override
 	public Player getWinner() {
 		return winner;
 	}
 
 	/**
 	 * Checks containing sentences for typos
 	 * 
 	 * @return FALSE if there is definitely a typo, otherwise TRUE
 	 */
 	public boolean isOK() {
 		boolean result = true;
 		for (Game s : games)
 			result = result & s.isOK();
 		return result;
 	}
 
 	/**
 	 * Returns the existence of changes
 	 * 
 	 * @return existence of changes
 	 */
 	public boolean isUnsaved() {
 		return unsaved;
 	}
 
 	/**
 	 * Long HTML description
 	 * 
 	 * @return long HTML description
 	 */
 	public String longInfo() {
 		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
 		String result = "<html>" + shortInfo(true) + "<br>"
 				+ Language.get("Start") + ":\t" + sdf.format(startedDate)
 				+ "<br>" + Language.get("End") + ":\t" + sdf.format(endedDate)
 				+ "<br>" + Language.get("games") + ": ";
 		result += games.get(0).toString();
 		for (int i = 1; i < games.size(); i++)
 			result += ", " + games.get(i).toString();
 		result += "</html>";
 		return result;
 	}
 
 	/**
 	 * Medium HTML description
 	 * 
 	 * @return medium HTML description
 	 */
 	public String mediumInfo() {
 		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
 		return "<html>" + shortInfo(true) + "<br>" + Language.get("Start")
 				+ ":\t" + sdf.format(startedDate) + "<br>"
 				+ Language.get("balls") + ":\t" + getLeftBalls() + ":"
 				+ getRightBalls() + "</html>";
 	}
 
 	/**
 	 * Sets the priority to specific value
 	 * 
 	 * @param priority
 	 *            new priority
 	 */
 	public void setPriority(double priority) {
 		this.priority = priority;
 	}
 
 	/**
 	 * Sets a value for the Game's state
 	 * 
 	 * @param state
 	 *            new State
 	 */
 	public void setState(int state) {
 		if (this.state == STATE_RUNNING & state == STATE_FINISHED) {
 			winner.addMatch(this);
 			loser.addMatch(this);
 			startedDate = new Date();
 		}
 		if (this.state == STATE_FINISHED & state < STATE_FINISHED) {
 			winner.delMatch(this);
 			loser.delMatch(this);
 			winner = null;
 			loser = null;
 			games.clear();
 		}
 		this.state = state;
 		setUnsaved(true);
 	}
 
 	/**
 	 * Notifies if changes have been made
 	 * 
 	 * @param unsaved
 	 *            if changes have been made
 	 */
 	public void setUnsaved(boolean unsaved) {
 		this.unsaved = unsaved;
 	}
 
 	/**
 	 * Short HTML description
 	 * 
 	 * @return short HTML description
 	 */
 	public String shortInfo() {
 		return shortInfo(false);
 	}
 
 	/**
 	 * Short HTML description
 	 * 
 	 * @return short HTML description
 	 */
 	private String shortInfo(boolean fullName) {
 		DateFormat df = new SimpleDateFormat("m");
 		if (fullName)
 			return leftPlayer.getFullName() + " : " + rightPlayer.getFullName()
 					+ " (" + getLeftSentences() + ":" + getRightSentences()
 					+ ")";
 		return toString() + " (" + getLeftSentences() + ":"
 				+ getRightSentences() + ")" + " "
 				+ df.format(getTimeRunning()) + "min";
 	}
 
 	/**
 	 * Starts the Game
 	 */
 	public void startGame() {
 		setUnsaved(true);
 		if (startedDate == null)
 			startedDate = new Date();
 		if (leftPlayer.getClass() == Team2.class & submatches == null) {
 			submatches = new ArrayList<Match>();
 			for (int[] matching : leftPlayer.getTournament().getProperties().subMatches) {
 				Player p1 = new Single(leftPlayer.getTournament(), leftPlayer
 						.getPersons().get(matching[0] - 1)), p2 = new Single(
 						leftPlayer.getTournament(), rightPlayer.getPersons()
 								.get(matching[1] - 1));
 				submatches.add(new Match(p1, p2));
 			}
 		}
 		state = 1;
 	}
 
 	@Override
 	public String toString() {
 		return leftPlayer + " : " + rightPlayer;
 	}
 
 	@Override
 	public Player getLoser() {
 		return loser;
 	}
 
 	public void setGroup(int group2) {
 		group = group2;
 	}
 
 	public Match flip() {
 		Match m = new Match(rightPlayer, leftPlayer);
 		for (Game g : games) {
 			m.addSentence(new Game(g.getRightBalls(), g.getLeftBalls()));
 		}
 		return m;
 	}
 }
