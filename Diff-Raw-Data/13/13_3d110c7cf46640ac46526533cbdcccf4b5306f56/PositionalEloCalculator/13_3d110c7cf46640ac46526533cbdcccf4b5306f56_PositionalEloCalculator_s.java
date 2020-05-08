 import java.util.*;
 public class PositionalEloCalculator implements RankingCalculationStrategy {
 	
	public static final double K = 20.0;
 	public static final double INITIAL = 1200;
 	
 	public void calculate(Contest contest) {
 		ArrayList<Contestant> contestants = contest.getContestants();
		double firstSeed = contestants.size() /2 + 1;
 	
 		for (int i = 0; i < contestants.size(); ++i) {
 			Contestant a = contestants.get(i);
 			double rA = a.getPreviousRanking();
 			double pA = a.getCurrentPosition();
 			double score = 0.0;
 	
 			for (int j = 0; j < contestants.size(); ++j) {
 				if (i == j) continue;
				Contestant b = contestants.get(i);
 			    double rB = b.getPreviousRanking();
 			    double pB = b.getCurrentPosition();
			    double eA = 1.0 / ( 1 + Math.pow(10.0, (rB - rA) / 400));
 			    score += eA;
 			}
 			double seed = rA == INITIAL ? firstSeed : score;
 			int newRanking = (int) Math.ceil(rA + K * (seed - pA));
 			a.setCurrentRanking(newRanking);
 		}
 	}
 }
