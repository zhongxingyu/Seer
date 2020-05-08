 package com.faziz.playarea;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.lang.math.RandomUtils;
 
 /**
  *
  * @author faziz
  */
 public class PlayArea implements Runnable {
 
     /** Logger for this class. */
     private static final Logger logger = Logger.getLogger(PlayArea.class.getName());
     private Referee referee = null;
     /** Matrix size of the play area. */
     public static final int MATRIX_SIZE = 100;
     /** Play area matrix. */
     private Cell[][] playAreaCells = new Cell[MATRIX_SIZE][MATRIX_SIZE];
 
     private PlayArea() {
         initializePlayAreaCells();
     }
     private static PlayArea PLAYAREA = new PlayArea();
 
     public static PlayArea getInstance() {
         return PLAYAREA;
     }
     private List<Player> existingPlayers = new ArrayList<Player>();
     /** 
      * Blocking queue to hold on to the movement requests. Serve the request in 
      * FIFO manner.
      */
     private BlockingQueue<MoveRequest> queue = new ArrayBlockingQueue<MoveRequest>(100, true);
 
     /**
      * Called by the player to the playarea to make a move request.
      * @param player
      * @param direction 
      */
     public void requestMove(Player player, MovementDirection direction) {
         try {
             queue.put(new MoveRequest(player, direction));
         } catch (InterruptedException ex) {
             logger.log(Level.SEVERE, "Could not put new request, queue exhausted.", ex);
             throw new IllegalStateException("Could not put new request, queue exhausted.", ex);
         }
     }
 
     public void run() {
         while (true) {
             try {
                 MoveRequest moveRequest = queue.take();
                 Player player = moveRequest.getPlayer();
                 MovementDirection direction = moveRequest.getDirection();
 
                 if (referee.moveIsAllowed(player, direction)) {
                     if (referee.moveIsFouled(player, direction)) {
                         player.flag();
                     }
                     movePlayer(player, direction);
                     if (existingPlayers.contains(player) == false) {
                         existingPlayers.add(player);
                     }
                 } else {
                     player.rejectMoveRequest(direction);
                     existingPlayers.remove(player);
                 }
 
                 if (existingPlayers.size() == 1) {
                     Player winningPlayer = existingPlayers.get(0);
                     logger.log(Level.INFO, "Please: {0} won!", winningPlayer);
                     break;
                 }
             } catch (InterruptedException ex) {
                 logger.log(Level.SEVERE, "Could not process requests.", ex);
                 throw new IllegalStateException("Could not process requests.", ex);
             }
         }
     }
 
     /**
      * Looks up the new cell for the player as per his request.
      * @param player
      * @param direction
      * @return 
      */
     private final Cell lookupRequestedCell(Player player, MovementDirection direction) {
         return player.getCell().getCellInDirection(direction);
     }
 
     /**
      * Moves the player to the new cell.
      * @param player
      * @param direction 
      */
     private final void movePlayer(Player player, MovementDirection direction) {
         Cell cell = lookupRequestedCell(player, direction);
         cell.setPlayer(player);
 
         player.getCell().setPlayer(null);
         player.setCell(cell);
         player.moveAccepted();
     }
 
     /**
      * Initializing the matrix with cells.
      */
     private final void initializePlayAreaCells() {
         logger.log(Level.INFO, "Initializing the grid");
         for (int row = 0; row < MATRIX_SIZE; row++) {
             for (int column = 0; column < MATRIX_SIZE; column++) {
                 Cell cell = new Cell(row, column, this);
                 playAreaCells[row][column] = cell;
             }
         }
     }
 
     public final Cell getCell(int row, int column) {
         try {
             return playAreaCells[row][column];
         } catch (ArrayIndexOutOfBoundsException ex) {
             return null;
         }
     }
 
     /**
      * @param referee the referee to set
      */
     public void setReferee(Referee referee) {
         this.referee = referee;
     }
 
     /**
      * Internal wrapper class.
      */
     class MoveRequest {
 
         private Player player = null;
         private MovementDirection direction = null;
 
         public MoveRequest(Player player, MovementDirection direction) {
             this.player = player;
             this.direction = direction;
         }
 
         /**
          * @return the player
          */
         public Player getPlayer() {
             return player;
         }
 
         /**
          * @return the direction
          */
         public MovementDirection getDirection() {
             return direction;
         }
     }
 
     /**
      * Tries to fit a player to a randomly selected cell.
      * Recursively calls itself until it finds an empty cell.
      * @param player 
      */
     public void registerPlayer(Player player) {
         int row = RandomUtils.nextInt(MATRIX_SIZE);
         int column = RandomUtils.nextInt(MATRIX_SIZE);
 
         Cell cell = playAreaCells[row][column];
         if (cell.isOccupied()) {
             registerPlayer(player);
         }
 
        logger.log(Level.FINE, "Registering the player with cell: {0}", cell);
         cell.setPlayer(player);
         player.setCell(cell);
     }

     public void reintroducePlayer(Player player) {
         if (player.isPlayerPermanatlyDisallowed() == false) {
             player.ready();
         }
     }
 
     public void evictPlayer(Player player) {
         player.getCell().setPlayer(null);
         player.flag();
     }
 }
