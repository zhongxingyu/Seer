 package nl.javadude.monopoly.domain;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * Plumbing class for the Game of Monopoly.
  */
 @SuppressWarnings("serial")
 public class Game implements Serializable, IGame
 {
 
 	private Queue<Player> players = new LinkedList<Player>();
 	private Player currentPlayer;
 	private Board board = new Board();
     private TurnState turnState = TurnState.END_TURN;
 
     private Logger log = LoggerFactory.getLogger(Game.class);
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public Player addPlayer(Player player) {
 		players.add(player);
         return player;
 	}
 
 	public Player addPlayer(String name) {
         final Player player = new Player(getBoard(), name);
         players.add(player);
         return player;
 	}
 
 	/**
 	 * Initialize the game, set the first player.
 	 */
 	public void startPlay() {
 		currentPlayer = players.poll();
         players.add(currentPlayer);
         currentPlayer.activate();
 		turnState = turnState.transition(currentPlayer);
 	}
 
 	public List<Player> getPlayers() {
 		return new ArrayList<Player>(players);
 	}
 
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 
 	public void setCurrentPlayer(Player player) {
 		currentPlayer = player;
 	}
 
 
     public Player nextPlayer() {
         log.debug("Trying moving to next player in state {}", turnState);
         if (turnState == TurnState.TURN_ACTION ||
                 turnState == TurnState.ROLLED_SAME_ONCE ||
                 turnState == TurnState.ROLLED_SAME_TWICE) {
             //end current players turn
             turnState = turnState.transition(currentPlayer);
             log.trace("Ended current players turn {}", turnState);
             //swap players
             currentPlayer.deactivate();
             currentPlayer = players.poll();
             players.add(currentPlayer);
             currentPlayer.activate();
 
             //start next players turn
             turnState = turnState.transition(currentPlayer);
             log.info("Started next {}'s turn", currentPlayer.getName());
         }
         return currentPlayer;
     }
 
 
 	public boolean playerInGame(String name) {
 		return (findPlayer(name) != null) ? true : false;
 	}
 
 	public Player findPlayer(String name) {
 		for (Player p : players) {
 			if (p.getName().equals(name)) {
 				return p;
 			}
 		}
 		return null;
 	}
 
     public void move() {
         if (turnState == TurnState.START_TURN ||
                 turnState == TurnState.ROLLED_SAME_ONCE ||
                 turnState ==TurnState.ROLLED_SAME_TWICE) {
             board.move(getCurrentPlayer(), Dice.INSTANCE.view());
             turnState = turnState.transition(getCurrentPlayer());
         }
     }
 
     public List<ISquare> getSquares()
     {
         return getBoard().getSquares();
     }
 
     public Player rollDice(int d1, int d2)
     {
         Dice dice = Dice.INSTANCE;
        	dice.setDiceValues(d1, d2);
        	move();
        	return currentPlayer;
     }
 
     public boolean currentPlayerBuy()
     {
         return currentPlayer.buy();
     }
 }
