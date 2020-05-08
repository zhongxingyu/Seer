 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package connectFour;
 
 /**
  *
  * @author Ryan
  */
 public class PlayGameMenuControl {
 
     private Game game;
     private Board board;
     private BoardView displayBoard = new BoardView();
     
     public PlayGameMenuControl(Game game)  {
         this.game = game;
         this.board = game.getBoard();
     }
     
     
     public void takeTurn()  {
         System.out.println("\n\ttakeTurn() called");
     }
     
     public void displayBoard() {
         System.out.println("\n\tdisplayBoard() called");
     }
     
     public void startNewGame() {
         System.out.println("\n\tstartNewGame() called");
         this.displayBoard();
     }
     
     public void displayOptionsMenu() {
        GameOptionsMenuView gameOptionsMenu = ConnectFour.getGameOptionsMenu();
         gameOptionsMenu.getInput(this.game);
     }
     
     
     public void displayHelpMenu() {
        HelpMenuView helpMenu = ConnectFour.getHelpMenu();
         helpMenu.getInput();
     }
     
     
 }
