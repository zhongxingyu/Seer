 package org.hexgame.controllers;
 
 import org.hexgame.controllers.GameController;
 import org.hexgame.core.*;
 import org.hexgame.core.strategies.RandomStrategy;
 import org.hexgame.ui.*;
 
 public class GuiGameController extends GameController {
   HexGui gui;
 
   public void setGui(HexGui _gui) {
     gui = _gui;
   }
 
   public void start() {
     initGuiComponents();
   }
 
  public void startNewGameAction(int size, String pA, String pB) {
    startNewGame(size);
   }
 
   public void placePieceAction(int xCoordinate, int yCoordinate) {
     if(game.isFinished()) return;
 
     Move move = new Move(board, currentPlayer, xCoordinate, yCoordinate);
     debug(String.format("HUMAN selected: %d %d", xCoordinate, yCoordinate));
     if(move.isValid()) {
       performMove(move);
     }
     else {
       debug("Invalid move!");
     }
   }
 
   public void undoMoveAction() {
     Move move = game.undoMove();
     gui.clearHexagon(move.xCoordinate, move.yCoordinate);
     performNextMove();
   }
 
   public void exitAction() {
     System.exit(0);
   }
 
  private void startNewGame(int size) {
     playerA = new Player(PieceType.WHITE);
	if(gui.jRadioButton1.isSelected())
    		playerB = new Player(PieceType.BLACK);
	else
    		playerB = new AIPlayer(PieceType.BLACK, new RandomStrategy());
     game = new Game(size, playerA, playerB);
     board = game.getBoard();
     performNextMove();
   }
   
   private void updateTitle() {
     gui.updateTitle(String.format("%s's turn | Hex", currentPlayer.toString()));
   }
 
   private void performNextMove() {
     currentPlayer = game.currentPlayer();
     updateTitle();
     if(!game.currentPlayer().isHuman() && !game.isFinished()) {
       debug("AI move");
       Move move = game.currentPlayer().getMove(board);
       while(!move.isValid()) move = game.currentPlayer().getMove(board);
       debug(String.format("AI selected: %d %d", move.xCoordinate, move.yCoordinate));
       performMove(move);
     }
     else {
       debug("HUMAN move");
     }
   }
 
   private void performMove(Move move) {
     game.performMove(move);
     gui.paintHexagon(move.xCoordinate, move.yCoordinate);
     redrawGuiComponents();
     currentPlayer = game.currentPlayer();
     if(game.isFinished()) finish();
     else performNextMove();
   }
 
   private void finish() {
     String msg = "Game finished. ";
     if(game.getWinner() == null) msg += "It's a draw.";
     else msg += game.getWinner().toString() + " won.";
     debug(msg);
     gui.updateTitle(msg);
     gui.alert(msg);
   }
 
   private void initGuiComponents() {
     HexGui.start(this);
   }
 
   private void redrawGuiComponents() {
     gui.redrawComponents(game, board);
   }
 
   private void debug(String msg) {
     System.err.println(msg);
   }
 }
