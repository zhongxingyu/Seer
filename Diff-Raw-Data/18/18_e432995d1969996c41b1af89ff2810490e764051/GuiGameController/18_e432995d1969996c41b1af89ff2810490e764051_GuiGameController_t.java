 package org.hexgame.controllers;
 
 import org.hexgame.controllers.GameController;
 import org.hexgame.core.*;
 import org.hexgame.core.strategies.RandomStrategy;
 import org.hexgame.ui.*;
 
 public class GuiGameController extends GameController {
   HexGui gui;
 
   public void setGui(HexGui _gui) {
     gui = _gui;
    startNewGame(11);
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
     System.err.println(String.format("HUMAN selected: %d %d", xCoordinate, yCoordinate));
     if(move.isValid()) {
       performMove(move);
     }
     else {
       System.err.println("Invalid move!");
     }
   }
 
   public void undoMoveAction() {
     game.undoMove();
   }
 
   public void exitAction() {
     System.exit(0);
   }
 
   private void startNewGame(int size) {
     playerA = new Player(PieceType.WHITE);
     playerB = new AIPlayer(PieceType.BLACK, new RandomStrategy());
     game = new Game(size, playerA, playerB);
     board = game.getBoard();
     currentPlayer = game.currentPlayer();
    performNextMove();
   }
 
   private void performNextMove() {
     if(!game.currentPlayer().isHuman() && !game.isFinished()) {
       System.err.println("AI move");
       Move move = game.currentPlayer().getMove(board);
       while(!move.isValid()) move = game.currentPlayer().getMove(board);
       System.err.println(String.format("AI selected: %d %d", move.xCoordinate, move.yCoordinate));
       performMove(move);
     }
     else {
       System.err.println("HUMAN move");
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
    System.err.println("Game finished.");
    if(game.getWinner() == null) System.err.println("It's a draw.");
    else System.err.println(game.getWinner().getPieceType() + " won.");
   }
 
   private void initGuiComponents() {
     HexGui.start(this);
   }
 
   private void redrawGuiComponents() {
     gui.redrawComponents(game, board);
   }
 }
