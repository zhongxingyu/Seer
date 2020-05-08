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
    startNewGame(11);
   }
 
   public void startNewGameAction(int size, String pA, String pB) {
     startNewGame(size);
   }
 
   public void placePieceAction(int xCoordinate, int yCoordinate) {
     Move move = new Move(board, currentPlayer, xCoordinate, yCoordinate);
     System.out.println(xCoordinate + " " + yCoordinate);
     if(move.isValid()) {
       performMove(move);
       performNextMove();
     }
     else {
       System.out.println("Move is invalid");
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
   }
 
   private void performNextMove() {
     if(!game.currentPlayer().isHuman() && !game.isFinished()) {
       System.out.println("Ruch komputera");
       Move move = game.currentPlayer().getMove(board);
       while(!move.isValid()) move = game.currentPlayer().getMove(board);
       System.out.println(move.xCoordinate + " " + move.yCoordinate);
       performMove(move);
     }
     else {
       System.out.println("Twój ruch");
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
     System.out.println("Game finished");
   }
 
   private void initGuiComponents() {
     HexGui.start(this);
   }
 
   private void redrawGuiComponents() {
     gui.redrawComponents(game, board);
   }
 }
