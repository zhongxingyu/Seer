 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package brawllogic;
 
 import java.util.EnumMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Stack;
 
 /**
  *
  * @author patrick
  */
 public class BrawlLogic {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
         Fighter bennett = new Fighter("Original", "Bennett");
         Fighter darwin = new Fighter("Original", "Darwin");
         
         Map<Player, Fighter> playerMap = new EnumMap(Player.class);
         
         playerMap.put(Player.LEFT, bennett);
         playerMap.put(Player.RIGHT, darwin);
         
         BrawlGame game = new BrawlGame(playerMap);
         
         Scanner in = new Scanner(System.in);
         
         while (!game.isGameOver()) {
             printGameState(game);
             System.out.print("  >  ");
             try {
                 handleInput(game, in.nextLine());
             } catch (GameplayException ex) {
                 System.out.println(ex.getMessage());
             }
         }
         
         Player winner = game.getWinner();
         
         if (winner == null) {
             System.out.println("Tie game!");
         } else {
             System.out.println((winner == Player.LEFT ? "Left" : "Right") + " player wins!");
         }
     }
     
     private static void printGameState(BrawlGame game) {
         
         System.out.println();
         
         for (Player player : Player.values()) {
             
             Stack<Card> discard = game.getDiscard(player);
             
             String active = discard.isEmpty() ? "<empty>" : discard.peek().getShorthand();
             
             System.out.println(game.getFighter(player).getName() + ": " + active);
         }
         
         System.out.println();
         
         for (Base base : game.getBases()) {

            Stack<Card> leftStack = base.getBaseStack(Player.LEFT).getStack();

            for (int i = leftStack.size() - 1; i >= 0; i--) {
                System.out.print(" " + leftStack.get(i).getShorthand());
             }
             
             System.out.print(" |" + base.getBaseCards().peek().getShorthand() + "|");
             
             for (Card card : base.getBaseStack(Player.RIGHT).getStack()) {
                 System.out.print(" " + card.getShorthand());
             }
             System.out.println();
         }
         System.out.println();
     }
     
     private static void handleInput(BrawlGame game, String input) throws GameplayException {
         
         BasePosition bp;
         Player s;
         Player p;
         
         switch (input.charAt(0)) {
             // LEFT PLAYER CONTROLS
             case 'w':
                 bp = BasePosition.HIGH;
                 s = Player.LEFT;
                 p = Player.LEFT;
                 break;
             case 'e':
                 bp = BasePosition.HIGH;
                 s = Player.RIGHT;
                 p = Player.LEFT;
                 break;
             case 's':
                 bp = BasePosition.MID;
                 s = Player.LEFT;
                 p = Player.LEFT;
                 break;
             case 'd':
                 bp = BasePosition.MID;
                 s = Player.RIGHT;
                 p = Player.LEFT;
                 break;
             case 'x':
                 bp = BasePosition.LOW;
                 s = Player.LEFT;
                 p = Player.LEFT;
                 break;
             case 'c':
                 bp = BasePosition.LOW;
                 s = Player.RIGHT;
                 p = Player.LEFT;
                 break;
             case 'z':
                 game.draw(Player.LEFT);
                 return;
             // RIGHT PLAYER CONTROLS
             case 'o':
                 bp = BasePosition.HIGH;
                 s = Player.LEFT;
                 p = Player.RIGHT;
                 break;
             case 'p':
                 bp = BasePosition.HIGH;
                 s = Player.RIGHT;
                 p = Player.RIGHT;
                 break;
             case 'l':
                 bp = BasePosition.MID;
                 s = Player.LEFT;
                 p = Player.RIGHT;
                 break;
             case ';':
                 bp = BasePosition.MID;
                 s = Player.RIGHT;
                 p = Player.RIGHT;
                 break;
             case '.':
                 bp = BasePosition.LOW;
                 s = Player.LEFT;
                 p = Player.RIGHT;
                 break;
             case '/':
                 bp = BasePosition.LOW;
                 s = Player.RIGHT;
                 p = Player.RIGHT;
                 break;
             case ',':
                 game.draw(Player.RIGHT);
                 return;
             default:
                 System.out.println("Unrecognized command.");
                 return;
         }
         
         
         game.tryMove(p, bp, s);
         
     }
 }
