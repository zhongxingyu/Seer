 package org.vu.contest.team24.tuner.model;
 
 import java.util.Random;
 
 import org.vu.contest.team24.RandomSingleton;
 
 public class DoubleGene implements Gene {
 	private double min;
 	private double max;
 	private double value;
 	private Random random;
 	
 	public DoubleGene(double min, double max) {
 		this.min = min;
 		this.max = max;
 		this.random = RandomSingleton.getInstance().getRandom();
 		this.value = (this.random.nextDouble() * (this.max - this.min)) - this.min;
 	}
 	
 	public DoubleGene(DoubleGene gene) {
 		this.min = gene.min;
 		this.max = gene.max;
 		this.random = RandomSingleton.getInstance().getRandom();
 		this.value = gene.value;
 	}
 	
 	@Override
 	public void mutate() {		
		double amount = this.random.nextGaussian() * (this.max - this.min);
 		
 		setValue(this.value + amount);
 	}
 	
 	public void crossover(Gene otherGene) {
 		DoubleGene otherDoubleGene = (DoubleGene)otherGene;
 		
 		double ourValue = (Double) this.getValue();
 		double theirValue = (Double) otherGene.getValue();
 		double averageValue = 0.5f * ourValue + 0.5f * theirValue;
 		
 		this.setValue(averageValue);
 		otherDoubleGene.setValue(averageValue);
 	}
 	
 	@Override
 	public Object getValue() {
 		return this.value;
 	}
 	
 	private void setValue(double value) {
 		if(value > this.max) {
 			value = this.max;
 		} else if(value < this.min) {
 			value = this.min;
 		}
 		this.value = value;
 	}
 }
