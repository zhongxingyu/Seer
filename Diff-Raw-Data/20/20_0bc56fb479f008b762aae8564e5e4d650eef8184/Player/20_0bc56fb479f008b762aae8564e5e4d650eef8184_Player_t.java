 package assignment3;
 
 import java.util.*;
 /**
 *
 * @author THypnocode
 */
 public class Player
 {
     private int score;
     Throw myThrow;
     
     public Player()
             {
                 score = 0;
             }
    
     
     public void compareThrows(Player player)
     {    
         if((myThrow == Throw.ROCK) && (player.myThrow == Throw.PAPER))
         {
             player.incrementScore();
            System.out.println("Human = Rock\tComputer = Paper");
             System.out.println("Human wins Round!");
         }
         else if((myThrow == Throw.ROCK) && (player.myThrow == Throw.SCISSORS))
         {
             incrementScore();
            System.out.println("Human = Rock\tComputer = Scissors");
             System.out.println("Human wins Round!");
         }
         else if((myThrow == Throw.SCISSORS) && (player.myThrow == Throw.PAPER))
         {
             incrementScore();
            System.out.println("Human = Scissors\tComputer = Paper");
             System.out.println("Computer wins Round!");
         }
         else if((myThrow == Throw.SCISSORS) && (player.myThrow == Throw.ROCK))
         {
             player.incrementScore();
            System.out.println("Human = Scissors\tComputer = Rock");
             System.out.println("Computer wins Round!");
         }
         else if((myThrow == Throw.PAPER && player.myThrow == Throw.ROCK))
         {
             incrementScore();
            System.out.println("Human = Paper\tComputer = Rock");
             System.out.println("Human wins Round!");
         }
         else if((myThrow == Throw.PAPER && player.myThrow == Throw.SCISSORS))
         {
             player.incrementScore();
            System.out.println("Human = Paper\tComputer = Scissors");
             System.out.println("Computer wins Round!");
         }
         else
             System.out.println("It was a tie!");
        
        System.out.println();
     }
     
     public int getScore()
     {
         return score;
     }
     
     public void incrementScore()
     {
         score++;
     }
 }
