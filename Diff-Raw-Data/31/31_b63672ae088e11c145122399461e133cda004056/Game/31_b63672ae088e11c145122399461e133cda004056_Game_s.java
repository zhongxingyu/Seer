 package assignment3;
 
 import java.util.Scanner;
 
 /**
 *
 * @author Hypnocode
 * 3/8/2012
 */
 public class Game
 {
     
     private static Match aMatch;
     private static int rounds = 0;
     
     public static void MainMenu()
     {
         Scanner scan = new Scanner(System.in);
         
         //Display logo and prompt for number of throws
         System.out.println("************************************");
        System.out.println("* Rock Paper Scissors 2012 *");
        System.out.println("* Hypnocode *");
         System.out.println("************************************");
         System.out.println("\n\n\n\n\n");
         //System.out.println("Please enter the number of throws");
         //rounds = scan.nextInt();
         
         System.out.println("\033");
     }
     public static void main(String[] args)
     {
         MainMenu();
         
         //if (args[1].contains("0123456789"))
         {
             rounds = 3;//Integer.parseInt(args[1]);
         }
         /*
         else
         {
             System.out.println("Argument is not an integer.");
             System.out.println("One round selected.");
         }
         */
         aMatch = new Match(rounds);
         
         while(!aMatch.isMatchOver())
         {
             aMatch.makeThrows();
         }
         
         aMatch.declareWinner();
     }
 
 }
 
