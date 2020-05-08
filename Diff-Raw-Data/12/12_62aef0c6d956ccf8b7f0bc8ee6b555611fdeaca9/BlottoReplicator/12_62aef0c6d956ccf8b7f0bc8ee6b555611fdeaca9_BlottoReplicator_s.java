 package edu.ntnu.EASY.blotto;
 
 import static edu.ntnu.EASY.util.Util.RNG;
 import edu.ntnu.EASY.incubator.Replicator;
 import edu.ntnu.EASY.util.Util;
 
 public class BlottoReplicator implements Replicator<double[]> {
 
 	private int genomeLength;
 	private double mutationRate;
 	private double crossoverRate;
 	
	public BlottoReplicator(int genomeLength){
 		this.genomeLength = genomeLength;
 	}
 	
 	@Override
 	public double[] mutate(double[] genome) {
 		for (int i = 0; i < genome.length; i++) {
 			if(RNG.nextDouble() <= mutationRate) {
 				genome[i] += RNG.nextGaussian();
 				if(genome[i] < 0) 
 					genome[i] = 0;
 			}
 		}
 		Util.normalize(genome);
 		return genome;
 	}
 
 	@Override
 	public double[] combine(double[] g1, double[] g2) {
 		double[] child = new double[g1.length];
 		System.arraycopy(g1,0,child,0,g1.length);
 
 		// Randomly crossover.
 		if(RNG.nextDouble() <= crossoverRate) {
 			int cross = RNG.nextInt(child.length);
 			System.arraycopy(g2,cross + 1,child,cross + 1,child.length - cross - 1);
 		}
 		Util.normalize(child);
 		return child;
 	}
 
 	@Override
 	public double[] randomGenome() {
 		double[] genome = new double[genomeLength];
 		for(int i = 0; i < genomeLength; i++){
 			genome[i] = RNG.nextDouble();
 		}
 		Util.normalize(genome);
 		return genome;
 	}
 }
 
 
