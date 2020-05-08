 package isnork.g3;
 
 import java.awt.print.Printable;
 import java.util.SortedMap;
 
 public class WaterProofPrecomputation implements Precomputation {
 
 	private final int sideLength;
 	//private final int viewRadius;
 	private final Pokedex dex;
 	SortedMap<Integer, String> speciesRanking;
 	
     public WaterProofPrecomputation(int sideLength, int viewRadius, Pokedex dex) {
 		this.sideLength = sideLength;
 		//this.viewRadius = viewRadius;
 		this.dex = dex;
 		speciesRanking = dex.getSpeciesRanking();
 	}
     
     /* (non-Javadoc)
 	 * @see isnork.g3.Precomputation#topToMedianHappinessRatio()
 	 */
     @Override
 	public double topToMedianHappinessRatio(){
     	return (1.0 * dex.getHappiness(speciesRanking.get(speciesRanking.size() - 1))) / 
     			dex.getHappiness(speciesRanking.get(speciesRanking.size() / 2));
     }
     
     /* (non-Javadoc)
 	 * @see isnork.g3.Precomputation#creatureDensity()
 	 */
     @Override
 	public double creatureDensity(){
     	int totalCreatures = 0;
     	for(String name : speciesRanking.values()){
     		totalCreatures += (dex.getMaxCount(name) + dex.getMinCount(name)) / 2;
     	}
     	return (1.0 * totalCreatures) / (sideLength * sideLength);
     }
     
     /* (non-Javadoc)
 	 * @see isnork.g3.Precomputation#dangerousToTotalRatio()
 	 */
     @Override
 	public double dangerousToTotalRatio(){
     	int totalCreatures = 0;
     	int dangerous = 0;
     	for(String name : speciesRanking.values()){
     		int average = (dex.getMaxCount(name) + dex.getMinCount(name)) / 2;
     		totalCreatures += average;
     		if(dex.isDangerous(name))
     			dangerous += average;
     	}
     	return (1.0 * dangerous) / totalCreatures;
     }
     
     /* (non-Javadoc)
 	 * @see isnork.g3.Precomputation#movingToStationaryDangerousRatio()
 	 */
     @Override
 	public double movingToStationaryDangerousRatio(){
     	int moving = 0;
     	int stationary = 0;
     	for(String name : speciesRanking.values()){
     		if(dex.isDangerous(name)){
         		int average = (dex.getMaxCount(name) + dex.getMinCount(name)) / 2;
         		if(dex.isMoving(name))
         			moving += average;
         		else
         			stationary += average;
     		}
     	}
    	return (1.0 * moving) / stationary;
     }
     
     /* (non-Javadoc)
 	 * @see isnork.g3.Precomputation#naiveHighScore()
 	 */
     @Override
 	public double naiveHighScore(){
     	double score = 0;
     	for(String name : speciesRanking.values()){
     		int average = Math.max((dex.getMaxCount(name) + dex.getMinCount(name)) / 2, 0);
     		int max = Math.min(average, 3);
     		int happiness = dex.getHappiness(name);
     		switch (max){
     		case 0: score += happiness / 2; break;
     		case 1: score += happiness; break;
     		case 2: score += (happiness * 1.5); break;
     		case 3: score += (happiness * 1.75); break;
     		default: break;
     		}
     	}
     	return score;
     }
 
 
 
 /*	// From http://algs4.cs.princeton.edu/11model/Binomial.java.html
 	// Binomial PMF -- (N choose k) * p^k * (1-p)^(N-k)
     public double binomial(int N, int k, double p) {
         double[][] b = new double[N+1][k+1];
 
         // base cases
         for (int i = 0; i <= N; i++)
             b[i][0] = Math.pow(1.0 - p, i);
         b[0][0] = 1.0;
 
         // recursive formula
         for (int i = 1; i <= N; i++) {
             for (int j = 1; j <= k; j++) {
                 b[i][j] = p * b[i-1][j-1] + (1.0 - p) *b[i-1][j];
             }
         }
         return b[N][k];
     }*/
 }
