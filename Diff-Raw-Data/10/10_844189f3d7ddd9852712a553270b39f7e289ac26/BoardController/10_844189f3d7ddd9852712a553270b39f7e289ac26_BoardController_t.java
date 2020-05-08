 package demo.client.local.game.controllers;
 
 import com.google.gwt.user.client.Timer;
 
 import demo.client.local.game.gui.Block;
 import demo.client.local.game.gui.ControllableBoardDisplay;
 import demo.client.local.game.tools.BoardMessageBus;
 import demo.client.local.game.tools.ClearState;
 import demo.client.local.game.tools.GameHeartBeat;
 import demo.client.local.game.tools.Pacer;
 import demo.client.local.lobby.Client;
 import demo.client.shared.game.model.BackgroundBlockModel;
 import demo.client.shared.game.model.BlockOverflow;
 import demo.client.shared.game.model.BoardModel;
 import demo.client.shared.meta.ScoreTracker;
 
 /**
  * This class runs the main game loop for Block Drop, updating the model and view.
  * 
  * @author mbarkley <mbarkley@redhat.com>
  * 
  */
 public class BoardController {
 
   // Must be protected so that OppController can access it
   protected ControllableBoardDisplay boardDisplay;
   private SecondaryDisplayController secondaryController;
   private BoardMessageBus messageBus;
 
   protected BoardModel model;
   protected Block activeBlock;
   protected Block nextBlock;
 
   // A Timer for running the game loop
   private Timer updateTimer;
   // A Timer for maintaining a connection with the server while not actively playing
   private GameHeartBeat heartBeatTimer = new GameHeartBeat();
 
   // The number of iterations for a block to drop one square on the board. Must be a multiple of 4.
   private int dropIncrement = 16;
   // The time (in milliseconds) between calls to the game loop.
   private int loopTime = 25;
   // A counter of elapsed iterations since a block last dropped.
   private int loopCounter = 0;
 
   // True if the game is paused.
   private boolean pause = false;
   // Controls flow of keep alive messages sent to server
   private Pacer pausePacer = new Pacer(dropIncrement / 2, false);
   // Controls flow of movement when arrow key is held down
   private Pacer movePacer = new Pacer(3);
   // Controls flow of rotation when arrow key is held down
   private Pacer rotatePacer = new Pacer(3);
   // True when a block should be moved horizontally without any delay
   private boolean singleColMove = false;
   // True when a block should be moved vertically without any delay
   private boolean singleRowMove = false;
   private boolean singleRotate;
 
   // Controls speed of row clearing animation
   private ClearState clearState = ClearState.START;
 
   /**
    * Create a BoardController instance.
    * 
    * @param boardDisplay
    *          A display for drawing the Block Drop game.
    * @param secondaryController
    *          A controller for the score list and the canvas displaying the next block.
    * @param messageBus
    *          A bus for broadcasting moves and score updates.
    */
   public BoardController(ControllableBoardDisplay boardDisplay, SecondaryDisplayController secondaryController,
           BoardMessageBus messageBus) {
     this.boardDisplay = boardDisplay;
     this.secondaryController = secondaryController;
     this.messageBus = messageBus;
 
     // Initiate BoardModel.
     model = new BoardModel();
     activeBlock = new Block(model.getActiveBlock(), boardDisplay.getSizeCategory());
     nextBlock = new Block(model.getNextBlock(), boardDisplay.getSizeCategory());
 
     // Create a updateTimer to run the game loop.
     updateTimer = new Timer() {
       @Override
       public void run() {
         update();
       }
     };
   }
 
   /**
    * Reset this controller in preparation for a call to {@link BoardController#startGame()
    * startGame}.
    */
   protected void reset() {
     activeBlock = new Block(model.getActiveBlock(), boardDisplay.getSizeCategory());
     nextBlock = new Block(model.getNextBlock(), boardDisplay.getSizeCategory());
     clearState = ClearState.START;
   }
 
   /**
    * Start the game loop for this controller.
    */
   public void startGame() {
     updateTimer.scheduleRepeating(loopTime);
   }
 
   /**
    * Handle user input, and update the game state and view.
    */
   protected void update() {
     // If paused, periodically send heartbeat to the server only
     if (pause) {
       if (pausePacer.isReady()) {
         messageBus.sendPauseUpdate(Client.getInstance().getPlayer());
         pausePacer.clear();
       }
       else {
         pausePacer.increment();
       }
       return;
     }
 
     boolean moved = false;
 
     // Check for rows to clear. Rows will stay in model until fully dealt with.
     int numFullRows = model.numFullRows();
     if (numFullRows > 0) {
       if (clearState.equals(ClearState.START))
         messageBus.sendMoveUpdate(model, Client.getInstance().getPlayer());
       clearRows(numFullRows);
     }
     // Check if there are rows to receive
     else if (model.getRowsToAdd() > 0) {
       addRowsToBottom();
       messageBus.sendMoveUpdate(model, Client.getInstance().getPlayer());
     }
     // Only drop a new block if we are not clearing or adding rows currently.
     else {
       // Reset the active block if necessary.
       if (!activeBlock.isModel(model.getActiveBlock())) {
         activeBlock = new Block(model.getActiveBlock(), boardDisplay.getSizeCategory());
         nextBlock = new Block(model.getNextBlock(), boardDisplay.getSizeCategory());
         secondaryController.drawBlockToNextCanvas(nextBlock);
       }
       // Update the position of the active block and record movement.
       moved = activeBlockUpdate();
 
       try {
         // If the block could not drop, start a new block.
         if (!moved && model.getPendingRowMove() > 0) {
           model.initNextBlock();
         }
       } catch (BlockOverflow e) {
         updateTimer.cancel();
 
         // Keep alive presence in game room
         heartBeatTimer.scheduleRepeating(loopTime * dropIncrement);
 
         // Display game over prompt to user
         boardDisplay.gameOver();
       }
       redraw();
       // Reset for next loop.
       model.setDrop(false);
       if (rotate())
         incrementRotate();
       if (horizontalMove())
         incrementMovePacer();
       model.setPendingRowMove(0);
       singleRowMove = false;
       loopCounter = loopCounter == dropIncrement ? 0 : loopCounter + 1;
       if (moved)
         messageBus.sendMoveUpdate(model, Client.getInstance().getPlayer());
     }
   }
 
   public void redraw() {
     boardDisplay.clearBoard();
     Block allSquares = new Block(model.getAllSquares(), boardDisplay.getSizeCategory());
     boardDisplay.drawBlock(0, 0, allSquares);
   }
 
   /**
    * Reset internal state and call {@link BoardController#startGame() startGame}.
    */
   public void restart() {
     // Reset board model and controller
     model = new BoardModel();
     boardDisplay.clearBoard();
     reset();
 
     // Subtract score penalty and update score
     secondaryController.getScoreTracker().setScore(
             secondaryController.getScoreTracker().getScore() - ScoreTracker.LOSS_PENALTY);
     secondaryController.updateAndSortScore(secondaryController.getScoreTracker());
     messageBus.sendScoreUpdate(secondaryController.getScoreTracker(), null);
 
     // Show time
     startGame();
     heartBeatTimer.cancel();
   }
 
   // This method should never be called when there are full rows.
   private void addRowsToBottom() {
     BackgroundBlockModel bgModel = model.getNonFullRows();
     Block bg = new Block(bgModel, boardDisplay.getSizeCategory());
     boardDisplay.undrawBlock(0, 0, bg);
     boardDisplay.undrawBlock(Block.indexToCoord(model.getActiveBlockCol(), boardDisplay.getSizeCategory()),
             Block.indexToCoord(model.getActiveBlockRow(), boardDisplay.getSizeCategory()), activeBlock);
     model.addRows();
     bgModel = model.getNonFullRows();
     bg = new Block(bgModel, boardDisplay.getSizeCategory());
     boardDisplay.drawBlock(0, 0, bg);
     boardDisplay.drawBlock(Block.indexToCoord(model.getActiveBlockCol(), boardDisplay.getSizeCategory()),
             Block.indexToCoord(model.getActiveBlockRow(), boardDisplay.getSizeCategory()), activeBlock);
   }
 
   /**
    * Animate the clearing of full rows from the board. On completion, remove full rows from the game
    * model. This method should be called once per game loop iteration until the full rows are
    * removed.
    * 
    * @param numFullRows
    *          The number of full rows on the board, used for calculating the score.
    */
   protected void clearRows(int numFullRows) {
     if (clearState.getCounter() == 0)
       switch (clearState) {
       case START:
         // Get blocks to be cleared.
         break;
       case FIRST_UNDRAW:
       case SECOND_UNDRAW:
       case THIRD_UNDRAW:
       case LAST_UNDRAW:
         boardDisplay.clearBoard();
        boardDisplay.drawBlock(0, 0, new Block(model.getNonFullRows(), boardDisplay.getSizeCategory()));
         break;
       case FIRST_REDRAW:
       case SECOND_REDRAW:
       case THIRD_REDRAW:
         boardDisplay.clearBoard();
        boardDisplay.drawBlock(0, 0, new Block(model.getAllSquares(), boardDisplay.getSizeCategory()));
         break;
       case DROPPING:
         boardDisplay.clearBoard();
         model.clearFullRows();
         // Redraw background blocks.
        boardDisplay.drawBlock(0, 0, new Block(model.getAllSquares(), boardDisplay.getSizeCategory()));
         // Update the score.
         secondaryController.updateScore(numFullRows);
         messageBus.sendScoreUpdate(secondaryController.getScoreTracker(), secondaryController.getTarget());
         break;
       }
     clearState = clearState.getNextState();
   }
 
   /**
    * Update the position of the active block in the model and redraw it in the display.
    * 
    * @return True iff the active block moved during this call.
    */
   protected boolean activeBlockUpdate() {
     // If the user wishes to drop the block, do nothing else.
     if (model.isDropping()) {
       setColMove(0);
       model.setPendingRowMove(model.getDropDistance());
     }
     else {
       if (rotate()) {
         model.rotateActiveBlock();
       }
       // Check if the user wants to increase the speed at which the block drops
       if (model.isFast()) {
         model.setPendingRowMove(1);
         // Otherwise maintain the normal speed.
       }
       else if (!singleRowMove) {
         // Drop by one row every time counter hits dropIncrement.
         model.setPendingRowMove(loopCounter == dropIncrement ? 1 : 0);
       }
     }
 
     // Attempt to move model.
     boolean moved = horizontalMove() ? model.moveActiveBlock(true) : model.moveActiveBlock(false);
     // If that didn't work, ignore the colMove (so that the block may still drop).
     if (!moved && horizontalMove())
       moved = model.moveActiveBlock(false);
 
     return moved;
   }
 
   private boolean horizontalMove() {
     return singleColMove && model.getPendingColMove() != 0 || movePacer.isReady();
   }
 
   private boolean rotate() {
     return singleRotate || rotatePacer.isReady();
   }
 
   public void clearRotation() {
     rotatePacer.clear();
   }
 
   private void incrementMovePacer() {
     if (!singleColMove)
       movePacer.increment();
     else {
       singleColMove = false;
       model.setPendingColMove(0);
     }
   }
 
   /**
    * Increment the rotation flow control. The first call to this method will cause the active block
    * to be rotated in the next game loop iteration. Successive calls will not immediately cause a
    * rotation.
    * 
    * The end result when a button is held down is that the active block will immediately rotate
    * 90-degrees, briefly pause, and then rapidly continue rotating until the button is released.
    */
   public void incrementRotate() {
     if (!singleRotate)
       rotatePacer.increment();
     else
       singleRotate = false;
   }
 
   private void clearMovePacer() {
     movePacer.clear();
   }
 
   /**
    * Set the horizontal movement of the active block in the next game loop iteration.
    * 
    * @param i
    *          The number of squares to move (right is positive).
    */
   public void setColMove(int i) {
     model.setPendingColMove(i);
     if (i != 0)
       incrementMovePacer();
     else
       clearMovePacer();
   }
 
   /**
    * Set the horizontal movement of the active block in the next game loop iteration.
    * 
    * Unlike {@link BoardController#setColMove(int) setColMove}, this method does not use a pacer.
    * This method should be called when only a single 90-degree rotation is required.
    * 
    * @param i
    *          The number of squares to move (right is positive).
    */
   public void setColMoveOnce(int i) {
     model.setPendingColMove(i);
     singleColMove = true;
   }
 
   /**
    * Check if the game is paused.
    * 
    * @return True iff the game is paused.
    */
   public boolean isPaused() {
     return pause;
   }
 
   /**
    * Set the active block to drop vertically in the next game loop iteration.
    * 
    * @param b
    *          True iff the block should drop.
    */
   public void setDrop(boolean b) {
     model.setDrop(b);
   }
 
   /**
    * Pause or unpause the game.
    * 
    * @param b
    *          True to pause the game. False to unpause.
    */
   public void setPaused(boolean b) {
     if (b && !pause) {
       boardDisplay.pause();
       pausePacer.clear();
     }
     else if (!b && pause) {
       boardDisplay.unpause();
     }
     pause = b;
   }
 
   /**
    * Set the active block to drop at an increased speed.
    * 
    * @param fast
    *          True to increase the block drop speed. False to set it to the normal speed.
    */
   public void setFast(boolean fast) {
     model.setFast(fast);
   }
 
   /**
    * Get the SecondaryDisplayController used by this BoardController.
    * 
    * @return The SecondaryDisplayController used by this BoardController.
    */
   public SecondaryDisplayController getSecondaryController() {
     return secondaryController;
   }
 
   /**
    * Add rows to the bottom of the board. Added rows will have a single missing square in a random
    * position.
    * 
    * @param rowsToAdd
    *          The number of rows to be added.
    */
   public void addRows(int rowsToAdd) {
     model.setRowsToAdd(rowsToAdd);
   }
 
   /**
    * Stop the all timers initiated by this controller.
    */
   public void stop() {
     updateTimer.cancel();
     heartBeatTimer.cancel();
   }
 
   /**
    * Get the pending horizontal movement of the active block. The active block will be moved by this
    * many squares horizontally (if possible) in the next game loop iteration.
    * 
    * @return The pending horizontal movement of the active block.
    */
   public int getColMove() {
     return model.getPendingColMove();
   }
 
   /**
    * Rotate the active block 90-degrees in the next game loop iteration without the use of the
    * {@link BoardController#incrementRotate() incrementRotate} method.
    */
   public void rotateOnce() {
     singleRotate = true;
   }
 
   /**
    * Set the number of squares that the active block should move horizontally (if possible) in the
    * next game loop iteration.
    * 
    * @param i
    *          The number of squares down to move in the next game loop iteration.
    */
   public void setRowMoveOnce(int i) {
     singleRowMove = true;
     model.setPendingRowMove(i);
   }
 }
