 package RPS;
 
 /** 
  * Represents a match of Rock, Paper, Scissors.
  * A match consists of n rounds and the stats for those rounds.
  *
  * @author Team Kai
  * @version 1.0
  */
 public class Match
 {
     /**
      * Number of rounds left in the match
      */
     private int throwCount;
     
     /**
      * [0]->P1 Wins, [1]->P2 Wins, [2]->Ties
      */
     private int[] stats;
     
     
     
     /**
      * Default Constructor for Match.
      * Initializes number of rounds to zero.
      */
     public Match()
     {
         throwCount = 0;
         stats = new int[3];
         stats[0]=stats[1]=stats[2]=0;
     }
     
     
     
     /**
      * Constructor for Match specifying number of rounds.
      * 
      * @param   int Number of rounds desired
      */
     public Match(int throwCount)
     {
         this.throwCount = throwCount;
         stats = new int[3];
     }
     
     
     
     /**
      * Conducts one round of Rock, Paper, Scissors.
      * Determines winner of the round.
      * Decrements the number of rounds left by one.
      * 
      * @param   int Integer representation of Player 1's throw.
      * @param   int Integer representation of Player 2's throw.
      * @return  int -1: P1 Wins, 0: Tie, 1: P2 Wins, -2:  Invalid Result
      */
     public int checkRound(int p1Throw, int p2Throw)
     {
         // Check that both throws are valid
        if(!throwIsValid(p1Throw) || !throwIsValid(p2Throw))
         {
             return -2;
         }
         
         // Decrement number of rounds by one
         throwCount --;
         
         // Tie
         if(p1Throw == p2Throw)
         {
             stats[2]++;
             return 0;
         }
         // P1 Wins
         else if((p1Throw == 0 && p2Throw == 2) ||
                 (p1Throw == 1 && p2Throw == 0) ||
                 (p1Throw == 2 && p2Throw == 1))
         {
             stats[0]++;
             return -1;
         }
         // P2 Wins
         else
         {
             stats[1]++;
             return 1;
         }
     }
     
     
     
     /**
      * Returns the current match's stats.
      * 
      * @return  int[]   [0]->P1 Wins, [1]->P2 Wins, [2]->Ties
      */
     public int[] getStats()
     {
         return stats.clone();
     }
     
     
     
     /**
      * Returns the current match's remaining rounds.
      * 
      * @return  int[]   [0]->P1 Wins, [1]->P2 Wins, [2]->Ties
      */
     public int getThrowCount()
     {
         return throwCount;
     }
     
     
     
     /**
      * Sets number of rounds to desired amount.
      * 
      * @param   int Number of rounds desired
      */
     public void setThrowCount(int throwCount)
     {
         this.throwCount = throwCount;
     }
     
     
     
     /**
      * Returns whether the match is over.
      * 
      * @return  boolean
      */
     public boolean matchIsOver()
     {
         // Number of rounds remaining is zero or lower
         return (throwCount <= 0);
     }
     
     
     
     /**
      * Returns whether the throw is valid.
      * 
      * @return  boolean
      */
     private boolean throwIsValid(int currentThrow)
     {
         return currentThrow >= 0 && currentThrow <= 2;
     }
 }
