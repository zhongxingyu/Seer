 package com.stephenn.problems.knapsack.genetic;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 public class Tournament {
 	List<Entity> population = new ArrayList<Entity>();
 	float target;
 	float[] prices;
 	Random rand = new Random();
 	
 	public Tournament(int populationSize, float[] prices, float target){
 		this.target = target;
 		this.prices = prices;
 		
 		for(int i=0; i< populationSize; i++){
 			population.add(Entity.newRand(prices, target, rand));
 		}
 		Collections.sort(population);
 		Collections.reverse(population);
 	}
 	
 	public void display(){
		
 		System.out.println("\t"+Arrays.toString(this.prices)+"\t"+this.target);
 		
 		for(int i=0; i< population.size(); i++){
 			Entity entity = population.get(i);
 			System.out.println(""+i+"\t"+Arrays.toString(entity.items)+"\t"+entity.fitness());
 		}
 		System.out.println();
 	}
 	
 	public void cycle(){
 		List<Entity> nextGen = new ArrayList<Entity>();
 		
 		int tenPercent = 1 + (int)((float)population.size() * 0.10); 
 		
 		// keep the best 10%
 		for(int i=0; i < tenPercent; i++){
 			nextGen.add(population.get(i));
 		}
 		// mate ignoring the bottom 10%
 		for(int i=0; i < population.size() - tenPercent; i++){
 			nextGen.add(population.get(i).mate(population.get(i+1), rand));
 		}
 		
 		Collections.sort(nextGen);
 		Collections.reverse(nextGen);
 		this.population = nextGen;
 	}
 }
