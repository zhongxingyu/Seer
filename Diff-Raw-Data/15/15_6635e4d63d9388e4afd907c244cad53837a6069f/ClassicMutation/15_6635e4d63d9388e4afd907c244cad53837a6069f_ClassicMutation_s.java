 package com.g4.java.mutation;
 
 import com.g4.java.model.Individual;
 import com.g4.java.util.RandomGenerator;
 
 public class ClassicMutation implements Mutation {
 
 	protected double mutationPercentage;
 	private double alleleProb;
 
 	public ClassicMutation(double mutationPercentage, double alleleProb) {
 		this.mutationPercentage = mutationPercentage;
 		this.alleleProb = alleleProb;
 	}
 
 	public Individual mutate(Individual entity, int iteration) {
 		double[] bits = entity.getLupusArray();
 		
 		for (int i = 0; i < bits.length; ++i) {
			double charProba = Math.random();
 			if (charProba < this.alleleProb) {
 				bits[i] = RandomGenerator.getDouble(-1.5, 1.5);
 			}
 		}
 		return Individual.creator(entity.getData(), bits);
 	}
 
 	public double getMutationProbability() {
 		return mutationPercentage;
 	}
 
 	public boolean shouldMutate() {
 		double prob = RandomGenerator.getDouble();
 		return prob < mutationPercentage;
 	}
 
   @Override
   public void updateMutationProbability(int iteration) {
   }
 
 }
