 package com.sc2mafia.mafiaplusplus;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import org.mozilla.javascript.*;
 
 import com.sc2mafia.mafiaplusplus.event.*;
 
 /**
  * The Class Game. This class controls the entire game, including day/night
  * cycles, lynching, handling messages, as well as handling players and calling
  * relevant methods
  */
 public class Game {
 
     private Player[] players;
     private HashMap<Player, Integer> votes = new HashMap<Player, Integer>();
     private boolean day;
     private boolean started = false;
     private int cycles = 0;
     private String globalScript;
     private Context cx;
     private Scriptable globalScope;
 
     private ArrayList<PlayerLynchedListener> lynchListeners = new ArrayList<PlayerLynchedListener>();
     private ArrayList<PlayerKilledListener> killListeners = new ArrayList<PlayerKilledListener>();
     private ArrayList<GameOverListener> gameOverListeners = new ArrayList<GameOverListener>();
     private ArrayList<SystemMessageListener> systemMessageListeners = new ArrayList<SystemMessageListener>();
 
     /**
      * Instantiates a new game.
      * 
      * @param players
      *            the players to initiate the game with
      * @param globalScript
      *            the global JavaScript file, containing variables and methods
      *            shared with all players, as well as game settings
      */
     public Game(Player[] players, String globalScript) {
 	this.globalScript = globalScript;
 	this.players = players.clone();
 	for (int i = 0; i < players.length; i++) {
 	    votes.put(players[i], 0);
 	}
     }
 
     /**
      * Starts the game. This will run the global script and initialises each
      * player, then starts night or day depending on the nightStart variable in
      * the global script. By default the game starts at day.
      */
     public void startGame() {
 	ContextFactory.initGlobal(new SandboxedContextFactory());
 	cx = Context.enter();
 	cx.setClassShutter(new ClassShutter() {
 	    public boolean visibleToScripts(String className) {
		if (className.startsWith("com.sc2mafia.mafia.")
 			|| className.startsWith("java.lang.")) {
 		    return true;
 		}
 		return false;
 	    }
 	});
 	globalScope = new ImporterTopLevel(cx);
 	this.cx.evaluateString(globalScope, globalScript, "GlobalScript", 1,
 		null);
 	for (Player p : players) {
 	    p.initRole(cx, globalScope);
 	}
 	started = true;
 	if (getGlobalScriptVar("nightStart") instanceof Boolean
 		&& (Boolean) getGlobalScriptVar("nightStart")) {
 	    startNight();
 	} else {
 	    startDay();
 	}
 
     }
 
     /**
      * Starts night. This is when any actions needed to prepare the role for the
      * night phase should be done.
      */
     public void startNight() {
 	day = false;
 	cycles++;
 	sortPlayersByPriority();
 	for (Player p : players) {
 	    p.nightStart(this);
 	}
     }
 
     /**
      * Ends night. This is when any night actions should be done.
      */
     public void endNight() {
 	day = false;
 	sortPlayersByPriority();
 	for (Player p : players) {
 	    p.nightEnd(this);
 	}
     }
 
     /**
      * Starts day. This is when roles should check if they are still alive, and
      * when roles should react to any night actions.
      */
     public void startDay() {
 	day = true;
 	cycles++;
 	sortPlayersByPriority();
 	for (Player p : players) {
 	    votes.put(p, 0);
 	    p.dayStart(this);
 	}
 	checkWins();
     }
 
     /**
      * Ends day. This is when roles should react to the lynch result and any
      * other actions that occurred during the day.
      */
     public void endDay() {
 	day = true;
 	sortPlayersByPriority();
 	for (Player p : players) {
 	    p.dayEnd(this);
 	}
 	checkWins();
     }
 
     /**
      * Register a lynch vote. This should be called by the wrapper when a player
      * votes for another player.
      * 
      * @param voter
      *            the voter
      * @param vote
      *            the player who was voted to be lynched
      * @throws InvalidVoteException
      *             thrown if a dead player voted, or if the vote was for a dead
      *             player
      */
     public void playerVoted(Player voter, Player vote)
 	    throws InvalidVoteException {
 	if (vote.isAlive() && voter.isAlive()) {
 	    votes.put(voter.getLynchVote(),
 		    votes.get(vote) - voter.voteWeight());
 	    votes.put(vote, votes.get(vote) + voter.voteWeight());
 	    countVotes();
 	} else if (vote.isAlive()) {
 	    throw new InvalidVoteException("Dead players cannot vote.");
 	} else {
 	    throw new InvalidVoteException("Dead players cannot be voted for.");
 	}
     }
 
     private void countVotes() {
 	int numMajority = countLiving() / 2 + 1;
 	for (Player p : votes.keySet()) {
 	    if (votes.get(p) >= numMajority) {
 		lynch(p);
 	    }
 	}
     }
 
     /**
      * Should be called by a role when it is killed. Sends events to all
      * relevant listeners.
      * 
      * @param player
      *            the player who was killed
      * @param killers
      *            the killers
      */
     void playerKilled(Player player, Player[] killers) {
 	PlayerKilledEvent event = new PlayerKilledEvent(this, player, killers);
 	for (PlayerKilledListener l : killListeners) {
 	    l.handlePlayerKilledEvent(event);
 	}
     }
 
     private void lynch(Player player) {
 	player.lynched(this);
 	PlayerLynchedEvent event = new PlayerLynchedEvent(this, player);
 	for (PlayerLynchedListener l : lynchListeners) {
 	    l.handlePlayerLynchedEvent(event);
 	}
     }
 
     /**
      * Returns all living players.
      * 
      * @return an array containing the living players
      */
     public Player[] getLivingPlayers() {
 	ArrayList<Player> living = new ArrayList<Player>();
 	for (Player p : players) {
 	    if (p.isAlive()) {
 		living.add(p);
 	    }
 	}
 	return living.toArray(new Player[living.size()]);
     }
 
     private int countLiving() {
 	return getLivingPlayers().length;
     }
 
     /**
      * Returns all dead players.
      * 
      * @return an array containing the dead players
      */
     public Player[] getDeadPlayers() {
 	ArrayList<Player> dead = new ArrayList<Player>();
 	for (Player p : players) {
 	    if (!p.isAlive()) {
 		dead.add(p);
 	    }
 	}
 	return dead.toArray(new Player[dead.size()]);
     }
 
     /**
      * Returns all players.
      * 
      * @return an array containing all players
      */
     public Player[] getPlayers() {
 	return players;
     }
 
     private void sortPlayersByPriority() {
 	Collections.sort(Arrays.asList(players), new Comparator<Player>() {
 	    @Override
 	    public int compare(Player p1, Player p2) {
 		return p1.getPriority() - p2.getPriority();
 	    }
 	});
     }
 
     private void checkWins() {
 	ArrayList<Player> winners = new ArrayList<Player>();
 	for (Player p : getPlayers()) {
 	    if (p.canGameEnd(this) == false) {
 		return;
 	    } else if (p.isWinner(this)) {
 		winners.add(p);
 	    }
 	}
 	GameOverEvent event = new GameOverEvent(this,
 		winners.toArray(new Player[winners.size()]));
 	;
 	for (GameOverListener l : gameOverListeners) {
 	    l.handleGameOverEvent(event);
 	}
     }
 
     /**
      * Sends a system message directly to one or more players. This should be
      * handled by the wrapper.
      * 
      * @param message
      *            the message to send
      * @param players
      *            the players to send the message to
      */
     public void sendSystemMessage(String message, Player[] players) {
 	SystemMessageEvent event = new SystemMessageEvent(this, players,
 		message);
 	;
 	for (SystemMessageListener l : systemMessageListeners) {
 	    l.handleSystemMessageEvent(event);
 	}
     }
 
     /**
      * Processes a message sent by a player. This passes it to the
      * processMessage method in each role, who then deal with it as they see
      * fit.
      * 
      * @param message
      *            the message to process
      */
     public void processMessage(Message message) {
 	for (Player p : getPlayers()) {
 	    p.handleMessage(message, this);
 	}
     }
 
     /**
      * Checks if the game is currently in the day cycle. The game considers the
      * day cycle to be any time between dayStart and nightStart being called.
      * 
      * @return true, if the game is in a day cycle, false otherwise
      */
     public boolean isDay() {
 	return day;
     }
 
     /**
      * Checks if the game has been started.
      * 
      * @return true, if the game has been started, false otherwise
      */
     public boolean isStarted() {
 	return started;
     }
 
     /**
      * Gets the number of day/night cycles the game has gone through. This
      * number is iterated every time startDay and startNight are called.
      * 
      * @return the number of cycles
      */
     public int getCycles() {
 	return cycles;
     }
 
     /**
      * Gets the value of a variable from the global script.
      * 
      * @param varName
      *            the name of the variable
      * @return an Object containing the value, or NOT_FOUND if the variable was
      *         not found
      */
     Object getGlobalScriptVar(String varName) {
 	return globalScope.get(varName, globalScope);
     }
 
     /**
      * Adds the SystemMessageListener to listen for system messages.
      * 
      * @param listener
      *            the listener to add
      */
     public synchronized void addEventListener(SystemMessageListener listener) {
 	systemMessageListeners.add(listener);
     }
 
     /**
      * Removes the SystemMessageListener.
      * 
      * @param listener
      *            the listener to remove
      */
     public synchronized void removeEventListener(SystemMessageListener listener) {
 	systemMessageListeners.remove(listener);
     }
 
     /**
      * Adds the GameOverListener to listen for game over events.
      * 
      * @param listener
      *            the listener to add
      */
     public synchronized void addEventListener(GameOverListener listener) {
 	gameOverListeners.add(listener);
     }
 
     /**
      * Removes the GameOverListener.
      * 
      * @param listener
      *            the listener to remove
      */
     public synchronized void removeEventListener(GameOverListener listener) {
 	gameOverListeners.remove(listener);
     }
 
     /**
      * Adds the PlayerLynchedListener to listen for when a player is lynched.
      * 
      * @param listener
      *            the listener to add
      */
     public synchronized void addEventListener(PlayerLynchedListener listener) {
 	lynchListeners.add(listener);
     }
 
     /**
      * Removes the GameOverListener.
      * 
      * @param listener
      *            the listener
      */
     public synchronized void removeEventListener(PlayerLynchedListener listener) {
 	lynchListeners.remove(listener);
     }
 
     /**
      * Adds the PlayerKilledListener to listen for when a player is killed.
      * 
      * @param listener
      *            the listener to add
      */
     public synchronized void addEventListener(PlayerKilledListener listener) {
 	killListeners.add(listener);
     }
 
     /**
      * Removes the PlayerKilledListener.
      * 
      * @param listener
      *            the listener to remove
      */
     public synchronized void removeEventListener(PlayerKilledListener listener) {
 	killListeners.remove(listener);
     }
 
 }
