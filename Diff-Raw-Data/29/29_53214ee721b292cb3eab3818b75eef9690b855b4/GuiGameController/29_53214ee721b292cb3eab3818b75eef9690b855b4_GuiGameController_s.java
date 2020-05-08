 package org.hexgame.controllers;
 
 import java.io.File;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.net.MalformedURLException;
 
 import org.hexgame.controllers.GameController;
 import org.hexgame.core.*;
 import org.hexgame.core.strategies.Strategy;
 import org.hexgame.ui.*;
 import org.hexgame.Main;
 
 public class GuiGameController extends GameController {
   HexGui gui;
 
   public void setGui(HexGui _gui) {
     gui = _gui;
   }
 
   public void start() {
     initGuiComponents();
   }
 
   public void startNewGameAction(int size, String pA, String pB) {
     playerA = constructPlayer(pA, PieceType.WHITE);
     playerB = constructPlayer(pB, PieceType.BLACK);
     game = new Game(size, playerA, playerB);
     board = game.getBoard();
     performNextMove();
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
 
   private Player constructPlayer(String type, PieceType pieceType) {
     Player player = null;
     if(type == "HUMAN") {
       player = new Player(pieceType);
     }
     else {
       try {
         File strategyJar = new File(strategiesPath(), type + ".jar");
         URLClassLoader strategyLoader = new URLClassLoader(new URL[]{strategyJar.toURI().toURL()});
         Class strategyClass = strategyLoader.loadClass("org.hexgame.core.strategies." + type);
         Strategy strategy = (Strategy) strategyClass.newInstance();
         player = new AIPlayer(pieceType, strategy);
       } catch (MalformedURLException ex) {
         System.err.println(ex);
       } catch (ClassNotFoundException ex) {
         System.err.println(ex);
       } catch (InstantiationException ex) {
         System.err.println(ex);
       } catch (IllegalAccessException ex) {
         System.err.println(ex);
       }
     }
     return player;
   }
 
  private String strategiesPath() {
     String path = "";
     try {
       path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
       path = URLDecoder.decode(path, "UTF-8");
       path = new File(path).getParent();
     }
     catch(java.io.UnsupportedEncodingException ex) {
       path = "";
     }
     return new File(path, "strategies").getPath();
   }
 
   private void debug(String msg) {
     System.err.println(msg);
   }
 }
