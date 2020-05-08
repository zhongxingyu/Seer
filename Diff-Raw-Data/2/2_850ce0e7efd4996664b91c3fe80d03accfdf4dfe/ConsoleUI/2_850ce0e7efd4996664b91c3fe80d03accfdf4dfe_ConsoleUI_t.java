 package edu.victone.scrabblah.ui;
 
 import edu.victone.scrabblah.logic.common.Coordinate;
 import edu.victone.scrabblah.logic.common.Move;
 import edu.victone.scrabblah.logic.common.Tile;
 import edu.victone.scrabblah.logic.common.Word;
 import edu.victone.scrabblah.logic.player.AIPlayer;
 import edu.victone.scrabblah.logic.player.HumanPlayer;
 import edu.victone.scrabblah.logic.player.Player;
 
 import java.util.Random;
 import java.util.Scanner;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 7:32 PM
  */
 
 //TODO: refactor scanner input for error-catching
 
 public class ConsoleUI extends UserInterface {
     Scanner scanner;
 
     public ConsoleUI() {
         super();
 
         scanner = new Scanner(System.in);
 
         printGreeting();
 
         int numPlayers = queryNumberPlayers();
 
         setNumberPlayers(numPlayers);
 
         for (int i = 1; i <= numPlayers; i++) {
             addPlayerToGame(queryPlayerData(i));
         }
 
 
         //verifyPlayerData(); //??
 
         startGame();
 
         turnLoop(); //loops until game is over
 
         //DEBUG
 
         //END DEBUG
 
         //Player winner = gameState.getWinner();
 
         //display endgame
 
         printGoodbye();
     }
 
     private void printHeader() {
         System.out.println("***********************************************");
     }
 
     private static void clearConsole() {
         try {
             String os = System.getProperty("os.name");
             if (os.contains("Windows")) {
                 Runtime.getRuntime().exec("cls");
             } else {
                 Runtime.getRuntime().exec("clear");
             }
         } catch (Exception e) {
             //  Handle exception.
             System.out.println("Fatal Error: " + e + "; Terminating.");
             System.exit(1);
         }
     }
 
     private void printGreeting() {
         printHeader();
         System.out.println("Scrabblah - UAB CS466 - Games Seminar\n(c) Victor Wilson 2013");
         //printHeader();
     }
 
     private void printGoodbye() {
         Player w = getWinner();
         printHeader();
         if (w != null) {
             System.out.println("Game Over! " + w.getName() + " wins with " + w.getScore() + " points!");
         } else {
             System.out.println("Nobody won.  You must be debugging (or something bad has happened.)");
         }
         printHeader();
     }
 
     private void printPlayerSummary() {
         printHeader();
         System.out.println("PLAYERS:");
         for (Player p : gameState.getPlayerList()) {
             System.out.println(p);
         }
     }
 
 
     @Override
     protected int queryNumberPlayers() {
         //todo refactor for error catching
         int n;
         printHeader();
         do {
             System.out.print("How many players? (2-4): ");
             n = scanner.nextInt();
             if (n < 2 || n > 4) {
                 System.out.println("Please enter either 2, 3, or 4.");
             }
         } while (n < 2 || n > 4);
         return n;
     }
 
     @Override
     protected Player queryPlayerData(int rank) {
         printHeader();
         String type = "";
         do {
             System.out.print("Is Player " + rank + " a human player? (Y/n): ");
             type = scanner.next();
             if (!type.toLowerCase().equals("y") && !type.toLowerCase().equals("n")) {
                 System.out.println("Please enter 'y' or 'n'.");
             }
         } while (!type.toLowerCase().equals("y") && !type.toLowerCase().equals("n"));
 
         String name = "";
         if (type.toLowerCase().equals("y")) {
             do {
                 System.out.print("Player " + rank + " Name: ");
                 name = scanner.next();
                 if (name.equals("")) {
                     System.out.println("Please enter a name.  Any name.");
                 }
             } while (name.equals(""));
         } else {
             name = UserInterface.playerNames[new Random().nextInt(UserInterface.playerNames.length)];
         }
         return (type.toLowerCase().equals("y") ? new HumanPlayer(name, rank) : new AIPlayer(name, rank));
     }
 
     @Override
     protected Move queryMoveType() {
         String m = null;
         boolean notValid;
         do {
             System.out.print("(P)lay a tile\ns(H)uffle Rack\n(S)wap tiles\n(E)nd Turn\n(R)ecall Tiles\n(Q)uit\nEnter your move: ");
             m = scanner.next().toLowerCase();
             notValid = !m.equals("p") &&
                     !m.equals("h") &&
                     !m.equals("s") &&
                     !m.equals("e") &&
                     !m.equals("r") &&
                     !m.equals("q");
             if (notValid) {
                 System.out.println("Invalid entry.  Please try again.");
             }
         } while (notValid);
 
         switch (m.charAt(0)) {
             case 'p':
                 return Move.PLAY;
             case 's':
                 return Move.SWAP;
             case 'a':
                 return Move.PASS;
             case 'r':
                 return Move.RESIGN;
             case 'h':
                 return Move.SHUFFLE;
             default:
                 //throw new Exception("Bad move input.");
                 return null;
         }
     }
 
     @Override
     protected void playTurn(Player currentPlayer) {
         clearConsole();
         printPlayerSummary();
         System.out.print(gameState.getGameBoard());
         System.out.println("Current Player: " + currentPlayer);
         System.out.println(currentPlayer.getTileRack());
 
         switch (queryMoveType()) {
             case PLAY:
                 queryPlay(currentPlayer);
                 break;
             case SWAP:
                 break;
             case PASS:
                 break;
             case RESIGN:
                 break;
         }
     }
 
     private void queryPlay(Player currentPlayer) {
         printHeader();
         System.out.println(currentPlayer.getTileRack());
         String letter;
         do {
             System.out.println("Enter the letter (# to quit: )");
             letter = scanner.next();
            // if (currentPlayer.getTileRack().toStringArray().contains(null)) ;
         } while (true);
 
     }
 
     @Override
     public void pass(Player p) {
     }
 
     @Override
     public void swap(Player p) {
     }
 
   /*  @Override
     public boolean play(Player player, Tile tile, Coordinate coord, boolean orientation) {
         return false;
     }
     */
 
     @Override
     public void resign(Player p) {
     }
 
 //    @Override
 //    protected void displayGame() {
 //    }
 }
