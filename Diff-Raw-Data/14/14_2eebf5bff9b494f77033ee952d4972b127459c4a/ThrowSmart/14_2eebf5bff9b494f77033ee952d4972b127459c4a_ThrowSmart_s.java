 package assignment3;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * ThrowSmart is an extension of ThrowCalculator class that makes a decision
  * using information from the ThrowHistorian regarding the past throw sequences.
  * @author Hypnocode
  */
 public class ThrowSmart extends ThrowCalculator
 {
 
     @Override
     public Throw generateThrow() 
     {
         ArrayList<Throw> throwList;
         int rockSequence = 0;
         int paperSequence = 0;
         int scissorsSequence = 0;
         
         // N == 3 Algorithm
         int n = 3;
         throwList = ThrowHistorian.getLastSequence(n);
         
         if (!throwList.isEmpty())
         {
             // Check for Sequence + Rock
             throwList.add(Throw.ROCK);
             rockSequence = ThrowHistorian.searchSequence(throwList);
             throwList.remove(throwList.size() - 1);
 
             // Check for Sequence + Paper
             throwList.add(Throw.PAPER);
             paperSequence = ThrowHistorian.searchSequence(throwList);
             throwList.remove(throwList.size() - 1);
 
             // Check for Sequence + Scissors
             throwList.add(Throw.SCISSORS);
             scissorsSequence = ThrowHistorian.searchSequence(throwList);
             throwList.remove(throwList.size() - 1);
 
             // Since Paper beats Rock rockSequence must be larger than paperSequence
             if (rockSequence > paperSequence)
             {
                 // Since Rock beats Scissors if sequences are equal Rock is chosen
                 if (rockSequence >= scissorsSequence)
                     return Throw.ROCK;
                 else
                     return Throw.SCISSORS;
             }
             else
             {
                 // Since Scissors beats Paper paperSequence must be larger 
                 // than scissorSequence
                 if (paperSequence > scissorsSequence)
                     return Throw.PAPER;
                 else
                     return Throw.SCISSORS;
             }
         }
         else
         {
             Random rand = new Random();
             
             switch(rand.nextInt(3))
             {
                case 0: return Throw.ROCK;
                case 1: return Throw.PAPER;
                case 2: return Throw.SCISSORS;
             }
         }
     }
     
 }
