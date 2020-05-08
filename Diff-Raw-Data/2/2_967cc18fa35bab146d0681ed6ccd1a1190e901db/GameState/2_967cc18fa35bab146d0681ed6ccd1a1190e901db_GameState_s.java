 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.common.*;
 import edu.victone.scrabblah.logic.player.Player;
 import edu.victone.scrabblah.logic.player.PlayerList;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Stack;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 5:00 PM
  */
 
 public class GameState {
   private PlayerList playerList;
   private TileBag tileBag;
   private Player winner;
 
   private Stack<GameBoard> gameBoards;
   private Stack<Turn> plays;
   private String statusMessage;
   private int turnCounter = 1;
   private boolean active;
 
   public GameState() {
     gameBoards = new Stack<>();
     gameBoards.push(new GameBoard());
     plays = new Stack<>();
     tileBag = new TileBag();
     playerList = new PlayerList();
 
   }
 
   public GameBoard getGameBoard() {
     return gameBoards.peek();
 
   }
 
   public void pushGameBoard(GameBoard newGameBoard) {
     gameBoards.push(newGameBoard);
 
   }
 
   public int getNumberRemainingTiles() {
     return tileBag.size();
   }
 
   public PlayerList getPlayerList() {
     return playerList;
   }
 
   public boolean addPlayer(Player p) {
     return playerList.addPlayer(p);
   }
 
   private int getNumberPlayers() {
     return playerList.size();
   }
 
   public boolean startGame() {
     if (active) {
       statusMessage = "ERROR: Can't start a game already in progress.";
       return false;
     } else {
       active = true;
     }
 
     if (playerList == null) {
       statusMessage = "ERROR: No Game.";
       return false;
     }
 
     if (getNumberPlayers() <= 1) {
       statusMessage = "ERROR: Not enough players.  Add " + (2 - getNumberPlayers()) + " to " +
           (4 - getNumberPlayers()) + " players.";
     }
 
     playerList.setIndex(new Random().nextInt(getNumberPlayers()));
 
     for (Player p : playerList) {
       for (int i = 0; i < 7; i++) {
         p.getTileRack().addTile(tileBag.removeTile());
       }
     }
     return true;
   }
 
   //todo: maybe move this method to GameBoard
   public void playWord(Word w) {
     //place each tile of w on the board
     //check for validity
     //if valid end turn, return true
     //if not...
 
     if (Dictionary.contains(w.getWord())) {
       ArrayList<Tile> tilesToRemove = new ArrayList<>(7);
       GameBoard newBoard = new GameBoard(getGameBoard());
       //are all tiles in the player's tile rack (or on the board?)
       Tile t;
       if (w.isHorizontal()) {
         int wx = w.getHead().getX();
         int y = w.getHead().getY();
         for (int x = wx, ptr = 0; x < wx + w.getWord().length(); x++, ptr++) {
           t = new Tile(w.getWord().charAt(ptr));
           Coordinate c = new Coordinate(x, y);
           if (getGameBoard().getCellAt(c).isEmpty()) {
             if (getCurrentPlayer().getTileRack().contains(t)) {
               tilesToRemove.add(t);
               newBoard.getCellAt(c).setTile(t);
             } else {
               statusMessage = "ERROR: Tile " + t + " not in rack";
               return;
             }
           } else if (!getGameBoard().getCellAt(c).getTile().equals(t)) {
             statusMessage = "ERROR: Word " + w.getWord() + " doesn't fit";
             return;
           }
         }
       } else {
         int x = w.getHead().getX();
         int wy = w.getHead().getY();
         for (int y = wy, ptr = 0; y < wy + w.getWord().length(); y++, ptr++) {
           t = new Tile(w.getWord().charAt(ptr));
           Coordinate c = new Coordinate(x, y);
           if (getGameBoard().getCellAt(c).isEmpty()) {
             if (getCurrentPlayer().getTileRack().contains(t)) {
               tilesToRemove.add(t);
               newBoard.getCellAt(c).setTile(t);
             } else {
               statusMessage = "ERROR: Tile " + t + " not in rack";
               return;
             }
           } else if (!getGameBoard().getCellAt(c).getTile().equals(t)) {
             statusMessage = "ERROR: Tile " + t + " doesn't fit";
             return;
           }
         }
       }
 
       if (GameEngine.isLegalGameBoard(newBoard)) {
         pushGameBoard(newBoard);
       } else {
         statusMessage = "ERROR: Illegal Tile Placement";
         return;
       }
       for (Tile tile : tilesToRemove) {
         getCurrentPlayer().getTileRack().removeTile(tile);
       }
       //todo: nonnull push
       plays.push(null);
       endTurn();
     } else {
       statusMessage = "ERROR: " + w.getWord() + " is not in the dictionary.";
     }
   }
 
   public void resign() {
     getCurrentPlayer().resign();
     playerList.incrementIndex();
   }
 
   public void swapTiles(ArrayList<Tile> tilesToSwap) {
     if (getNumberRemainingTiles() >= tilesToSwap.size()) {
       for (Tile t : tilesToSwap) {
         getCurrentPlayer().getTileRack().removeTile(t);
       }
 
       getCurrentPlayer().getTileRack().addTiles(tileBag.swapTiles(tilesToSwap));
       playerList.incrementIndex();
       turnCounter++;
     } else {
       statusMessage = "ERROR: Not enough tiles to swap.";
     }
   }
 
   public void pass() {
     statusMessage = getCurrentPlayer() + " passes.";
     pushGameBoard(new GameBoard(gameBoards.peek()));
     playerList.incrementIndex();
     turnCounter++;
   }
 
   public void endTurn() {
     if (GameEngine.isLegalState(this)) {
       Player p = getCurrentPlayer();
 
       p.addScore(GameEngine.computeScore(gameBoards.elementAt(gameBoards.size() - 2), gameBoards.peek()));
 
       while (p.getTileRack().size() < 7) {
         p.getTileRack().addTile(tileBag.removeTile());
       }
 
       playerList.incrementIndex();
       turnCounter++;
     } else {
       statusMessage = "ERROR: Illegal game state.";
     }
   }
 
   public Player getCurrentPlayer() {
     return playerList.getCurrentPlayer();
   }
 
   public boolean isActive() {
     return active;
   }
 
   public boolean isGameOver() {
     if (tileBag.size() == 0) {
       for (Player p : playerList) {
         if (p.getTileRack().size() == 0) {
           setWinner(p);
           return true;
         }
       }
     } else {
       //if all but one player has resigned
       ArrayList<Player> activePlayers = playerList.getActivePlayers();
       if (playerList.getActivePlayers().size() == 1) {
         setWinner(activePlayers.get(0));
         return true;
       }
     }
     return false;
   }
 
   private void setWinner(Player p) {
     winner = p;
   }
 
   public Player getWinner() {
     return winner;
   }
 
   public int getTurn() {
     return turnCounter;
   }
 
   public void setStatusMessage(String statusMessage) {
     this.statusMessage = statusMessage;
   }
 
   public String getStatusMessage() {
     String r = statusMessage;
     statusMessage = null;
     return r;
   }
 
   public boolean errorPresent() {
     return statusMessage != null;
   }
 
   @Override
   public String toString() {
     return "A gamestate draws near.  Command?";
   }
 }
