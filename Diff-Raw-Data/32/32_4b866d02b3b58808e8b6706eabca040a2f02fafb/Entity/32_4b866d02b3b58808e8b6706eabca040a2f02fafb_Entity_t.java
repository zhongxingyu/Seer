 package com.stephenn.problems.knapsack.genetic;
 
 import java.util.*;
 
 public class Entity implements Comparable<Entity> {
 	int[] items;
 	float[] prices;
 	float target;
 	
 	public Entity(int[] items, float[] prices, float target){
 		this.items = items;
 		this.prices = prices;
 		this.target = target;
 	}
 	
 	public static Entity newRand(float[] prices, float target, Random rand){
 		int[] items = new int[prices.length];
 		float cost = target;
 		for (int i=0; i< items.length; i++){
 			float afford = cost / prices[i];
 			items[i] = (int)(rand.nextFloat() * afford);
 			cost -= items[i] * prices[i];
 		}
 		return new Entity(items, prices, target);
 	}
 	
 	public float fitness(){
 		float cost = 0;
 		for (int i=0; i < items.length; i++){
			cost += (items[i] * prices[i]);
 		}
 		
 		if (cost <= target){
 			return cost;
 		} else {
 			return -1;
 		}
 	}
 
 	@Override
 	public int compareTo(Entity other) {
		if (this.fitness() > other.fitness()){
			return 1;
		} else {
			return -1;
		}
 	}
 	
 	public Entity mate(Entity other, Random rand){
 		int[] child = new int[this.items.length];
 		for(int i=0; i< this.items.length; i++){
 			// take a split proportion of the parents item[i]
 			float p = rand.nextFloat();
 			
 			child[i] = (int) ((this.items[i] * p) + (other.items[i] * (1f-p)));
 			
 			// mutate, a chance to add or remove 10%
 			switch(rand.nextInt(10)){
 				case 0:
 					child[i] += child[i] * 0.10;
 					break;
 				case 1:
 					child[i] -= child[i] * 0.10;
 					break;
 			}
 		}
 		
 		return new Entity(child, this.prices, this.target);
 	}
 }
