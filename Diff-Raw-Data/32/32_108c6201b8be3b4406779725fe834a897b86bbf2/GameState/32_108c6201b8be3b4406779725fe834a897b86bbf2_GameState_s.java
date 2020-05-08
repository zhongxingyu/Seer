 package info.kulhan.reversi.model;
 
 import java.io.Serializable;
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * Game state
  */
 public class GameState extends Observable implements Serializable, Observer {
     /**
      * Player on the move
      */
     private Player currentPlayer;
     
     /**
      * Board
      */
     private Board board;
     
     /**
      * State of the game state
      */
     public enum State {
         /**
          * Game is waiting for user input
          */
         INTERACTIVE,
         
         /**
          * Game has ended
          */
         ENDED,
         
         /**
          * Game is waiting for computer input
          */
         WAITING
     }
     
     /**
     * true if game ended
      */
     private State state;
     
     /**
      * Type of game
      */
     public enum Type {
         /**
          * One player game against computer opponent
          */
         ONE_PLAYER,
         
         /**
          * Two player game on same computer
          */
         TWO_PLAYER
     }
     
     /**
      * Game type
      */
     private Type type;
     
     /**
      * true if begin() has been called
      */
     private boolean begun;
     
     /**
      * Create new game state
      */
     public GameState() {
         board = new Board();
         board.addObserver(this);
         begun = false;
         reset();
     }
     
     /**
      * Create new game state from different state
      */
     public GameState(GameState s) {
         board = new Board();
         board.addObserver(this);
         begun = false;
         reset(s);
     }
     
     /**
      * Create new game state
      */
     public GameState(Type t) {
         board = new Board();
         board.addObserver(this);
         begun = false;
         reset();
         type = t;
     }
     
     /**
      * Create new game state from different state
      */
     public GameState(GameState s, Player newCurrentPlayer) {
         this(s);
         currentPlayer = newCurrentPlayer;
     }
 
     /**
      * Reset to new game state
      */
     public void reset() {
         currentPlayer = Player.BLACK;
         state = State.INTERACTIVE;
         type = Type.TWO_PLAYER;
         board.reset(); // will trigger update() and thus notify observers
     }
     
     /**
      * Reset to given state
      * @param s 
      */
     public void reset(GameState s) {
         currentPlayer = s.currentPlayer;
         state = s.state;
         type = s.type;
         board.reset(s.board); // will trigger update() and thus notify observers
     }
     
     /**
      * Return board
      * @return
      */
     public Board getBoard() {
         return board;
     }
     
     /**
      * Return current player
      * @return 
      */
     public Player getCurrentPlayer() {
         return currentPlayer;
     }
 
     /**
      * Set current player
      * @param p 
      */
     public void setCurrentPlayer(Player p) {
         if (p == currentPlayer) { return; }
         
         currentPlayer = p;
         setChanged();
         
         if (!begun) {
             notifyObservers();
         }
     }
     
     /**
      * Return current player
      * @return 
      */
     public Player getOpponentPlayer() {
         if (currentPlayer == Player.NONE) {
             return Player.NONE;
         }
         
         return currentPlayer == Player.BLACK ? Player.WHITE : Player.BLACK;
     }
 
     /**
      * Listen for board updates
      * @param o
      * @param o1 
      */
     @Override
     public void update(Observable o, Object o1) {
         setChanged();
         
         if (!begun) {
             notifyObservers();
         }
     }
     
     /**
      * Begin not notifying observers
      */
     public void begin() {
         begun = true;
     }
     
     /**
      * End not notifying observers
      * If any value changed, observers will be notified now.
      */
     public void end() {
         begun = false;
         notifyObservers();
     }
     
     /**
      * Return state
      * @return
      */
     public State getState() {
         return state;
     }
     
     /**
      * Make game ended
      */
     public void setState(State s) {
         if (s != state) {
             setChanged();
         }
         
         state = s;
         
         if (!begun) {
             notifyObservers();
         }
     }
     
     /**
      * Return game type
      * @return 
      */
     public Type getType() {
         return type;
     }
     
     /**
      * Set game type
      * @param t 
      */
     public void setType(Type t) {
         if (t != type) {
             setChanged();
         }
         
         type = t;
         
         if (!begun) {
             notifyObservers();
         }
     }
 }
