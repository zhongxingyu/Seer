 import java.util.*;
 public class PositionalEloCalculator implements RankingCalculationStrategy {
 	
 	public static final double INITIAL = 1200;
 	
 	public void calculate(Contest contest) {
                int K = 10;
 		ArrayList<Contestant> contestants = contest.getContestants();
 		double firstSeed = contestants.size() / 2.0;
 	
 		for (int i = 0; i < contestants.size(); ++i) {
 			Contestant a = contestants.get(i);
 			double rA = a.getPreviousRanking();
 			double pA = a.getCurrentPosition();
 			double score = 0.0;
                        if (rA < 1400) K = 25;
                        else K = 10;
 	
 			for (int j = 0; j < contestants.size(); ++j) {
 				if (i == j) continue;
 				Contestant b = contestants.get(j);
 			    double rB = b.getPreviousRanking();
 			    double pB = b.getCurrentPosition();
                             double eA = 0.0D;
                            eA = Math.pow(10.0D, (rB - rA) / 400.0D);
                             eA = 1.0D / (1.0D + eA);
 			    score += eA;
 			}
			double seed = (rA == INITIAL) ? firstSeed : score;
                        System.out.println(seed + " " + pA);
 			int newRanking = (int) Math.ceil(rA + K * (seed - pA));
 			a.setCurrentRanking(newRanking);
 		}
 	}
 }
