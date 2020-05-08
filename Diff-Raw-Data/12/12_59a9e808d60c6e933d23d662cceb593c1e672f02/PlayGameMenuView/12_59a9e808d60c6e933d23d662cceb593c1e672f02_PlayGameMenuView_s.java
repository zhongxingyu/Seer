 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package connectFour;
 
     import java.util.Scanner;
 
 
 /**
  *
  * @author Ryan
  */
 public class PlayGameMenuView {
  
     private Game game;
     private PlayGameMenuControl playGameMenuControl;
     
     
     private final static String[] [] menuItems = {
         {"T", "Take your turn"},
         {"D", "Display the board"},
         {"N", "New Game"},
         {"O", "Change game options"},
         {"H", "Help"},
         {"Q", "QUIT"}
     };
     private Object gameStatus;
       
     public PlayGameMenuView(Game game) {
         this.playGameMenuControl = new PlayGameMenuControl(game);
     }
     
     public Object getInput(Object object) {
         this.game = (Game) object;
         
        this.game.setStatus = this.game.getStatus();
         do {
             
             this.display();
             
             //get entered command
             String command = this.getCommand();
             switch(command) {
                 case "T":
                     this.playGameMenuControl.takeTurn();
                     break;
                 case "D":
                     playGameMenuControl.displayBoard();
                     break;
                 case "N":
                     playGameMenuControl.startNewGame();
                     break;
                 case "O":
                     playGameMenuControl.displayOptionsMenu();
                     break;
                 case "H":
                     playGameMenuControl.displayHelpMenu();
                     break;
                 case "Q":
                     gameStatus = "QUIT";
                     break;
             }
         } while (!gameStatus.equals("QUIT"));
        return Game.Playing;
     }
     
     
     
     public final void display() {
         System.out.println("\n\t===============================================================");
         System.out.println("\tEnter the letter associated with one of the following commands:");
 
         for (int i = 0; i < PlayGameMenuView.menuItems.length; i++) {
             System.out.println("\t   " + menuItems[i][0] + "\t" + menuItems[i][1]);
         }
         System.out.println("\t===============================================================\n");
     }
 
     
     private boolean validCommand(String command) {
         String[][] items = PlayGameMenuView.menuItems;
 
         for (String[] item : PlayGameMenuView.menuItems) {
             if (item[0].equals(command)) {
                 return true;
             }
         }
         return false;
     }
     
      protected final String getCommand() {
 
         Scanner inFile = ConnectFour.getInputFile();
         String command;
         boolean valid = false;
         do {
             command = inFile.nextLine();
             command = command.trim().toUpperCase();
             valid = validCommand(command);
             if (!valid) {
                 new ConnectFourError().displayError("Invalid command. Please enter a valid command.");
                 continue;
             }
                 
         } while (!valid);
         
         return command;
     }
 
     public PlayGameMenuView(Game game, PlayGameMenuControl playGameMenuControl, Object gameStatus) {
         this.game = game;
         this.playGameMenuControl = playGameMenuControl;
         this.gameStatus = gameStatus;
     }
 
     public Game getGame() {
         return game;
     }
 
     public void setGame(Game game) {
         this.game = game;
     }
 
     public PlayGameMenuControl getPlayGameMenuControl() {
         return playGameMenuControl;
     }
 
     public void setPlayGameMenuControl(PlayGameMenuControl playGameMenuControl) {
         this.playGameMenuControl = playGameMenuControl;
     }
 
     public Object getGameStatus() {
         return gameStatus;
     }
 
     public void setGameStatus(Object gameStatus) {
         this.gameStatus = gameStatus;
     }
      
 }
