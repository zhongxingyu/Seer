 package BetMatchProcessing;
 
 import HandicapProcessing.HandicapProcessing;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-1-16
  * Time: 上午12:11
  * To change this template use File | Settings | File Templates.
  */
 public class BetHandicapMatchGuarantee extends BetMatchBasic {
 
     public int betMatch(double minExpectation, double minProbability, double baseMoney, HandicapProcessing handicapProcessing) {
         String aid = "Guarantee" + String.valueOf(minExpectation) + String.valueOf(minProbability);
         if (handicapProcessing.getWinExpectation() > minExpectation && (handicapProcessing.getWinProbability() +
                handicapProcessing.getWinHalfProbability() + handicapProcessing.getDrawProbability()) > minProbability) {
             betOnMatch(handicapProcessing.getMatchInformation().getMatchId(),
                     handicapProcessing.getMatchInformation().getCid(),
                     handicapProcessing.getMatchInformation().getClientId(),
                    0, baseMoney, aid, handicapProcessing.getWinExpectation(),
                     (handicapProcessing.getWinProbability() + handicapProcessing.getWinHalfProbability()
                             + handicapProcessing.getDrawProbability()), handicapProcessing.getMatchInformation().getMatchTime());
             return 0;
         } else if (handicapProcessing.getLoseExpectation() > minExpectation && (handicapProcessing.getLoseProbability()
                + handicapProcessing.getLoseHalfProbability() + handicapProcessing.getDrawProbability()) > minProbability) {
             betOnMatch(handicapProcessing.getMatchInformation().getMatchId(),
                     handicapProcessing.getMatchInformation().getCid(),
                     handicapProcessing.getMatchInformation().getClientId(),
                    1, baseMoney, aid, handicapProcessing.getLoseExpectation(),
                     (handicapProcessing.getLoseProbability() + handicapProcessing.getLoseHalfProbability()
                             + handicapProcessing.getDrawProbability()), handicapProcessing.getMatchInformation().getMatchTime());
             return 0;
         }
 
         System.out.println("Don't not bet on this match...");
         System.out.println("Win Expectation: " + handicapProcessing.getWinExpectation());
         System.out.println("Win Probability: " + (handicapProcessing.getWinProbability() + handicapProcessing.getWinHalfProbability()
                 + handicapProcessing.getDrawProbability()));
         System.out.println("Lose Expectation: " + handicapProcessing.getLoseExpectation());
         System.out.println("Lose Probability: " + (handicapProcessing.getLoseProbability() + handicapProcessing.getLoseHalfProbability()
                 + handicapProcessing.getDrawProbability()));
 
         return -1;
     }
 }
