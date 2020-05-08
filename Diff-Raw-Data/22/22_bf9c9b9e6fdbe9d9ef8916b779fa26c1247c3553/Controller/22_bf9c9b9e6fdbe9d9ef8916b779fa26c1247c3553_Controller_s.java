 package com.challenge.dropboxdiet;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class Controller 
 {
 	private ArrayList<Perk> perks;
 	
 	public Controller(ArrayList<Perk> perks)
 	{
 		this.perks = perks;
 	}
 	
 	private ArrayList<Perk> getCaloriePositivePerks()
 	{
 		ArrayList<Perk> caloriePositivePerks = new ArrayList<Perk>();
 		for(Perk perk:perks)
 		{
 			if(perk.isCaloriePositive())
 				caloriePositivePerks.add(perk);
 		}
 		return caloriePositivePerks;
 	}
 	
 	private ArrayList<Perk> getCalorieNegativePerks()
 	{
 		ArrayList<Perk> calorieNegativePerks = new ArrayList<Perk>();
 		for(Perk perk:perks)
 		{
 			if(!perk.isCaloriePositive())
 				calorieNegativePerks.add(perk);
 		}
 		return calorieNegativePerks;
 	}
 	
 	private ArrayList<ArrayList<Integer>> generateCombinations(int numberOfPerks)
 	{
 		ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
 		for(int i=1;i<numberOfPerks;i++)
 		{
 			CombinationGenerator gen = new CombinationGenerator(numberOfPerks, i);
 			while(gen.hasMore())
 			{
 				int[] x = gen.getNext();
 				ArrayList<Integer> combination = new ArrayList<Integer>();
 				for(int item:x)
 					combination.add(new Integer(item));
 				combinations.add(combination);
 			}
 		}
 		return combinations;
 	}
 	
 	public ArrayList<ArrayList<Perk>> findCalorieNeutralPerkGroups()
 	{
 		ArrayList<ArrayList<Perk>> calorieNeutralPerks = new ArrayList<ArrayList<Perk>>();
 		ArrayList<Perk> caloriePositivePerks = this.getCaloriePositivePerks();
 		ArrayList<Perk> calorieNegativePerks = this.getCalorieNegativePerks();
 		ArrayList<ArrayList<Integer>> caloriePositiveCombinations = this.generateCombinations(caloriePositivePerks.size());
 		ArrayList<ArrayList<Integer>> calorieNegativeCombinations = this.generateCombinations(calorieNegativePerks.size());
 		ArrayList<ArrayList<Perk>> caloriePositivePerkCombinations = this.buildPerkCombinationsList(caloriePositivePerks, caloriePositiveCombinations);
 		ArrayList<ArrayList<Perk>> calorieNegativePerkCombinations = this.buildPerkCombinationsList(calorieNegativePerks, calorieNegativeCombinations);
 		if(caloriePositivePerkCombinations.size()==0||calorieNegativePerkCombinations.size()==0)
 		{
 			return calorieNeutralPerks;
 		}
 		for(ArrayList<Perk> caloriePositivePerkCombination:caloriePositivePerkCombinations)
 		{
 			for(ArrayList<Perk> calorieNegativePerkCombination:calorieNegativePerkCombinations)
 			{
 				try
 				{
 					if(this.sumPerkCombinations(caloriePositivePerkCombination, calorieNegativePerkCombination)==0)
 					{
 						ArrayList<Perk> neutralCombination = new ArrayList<Perk>(caloriePositivePerkCombination);
 						neutralCombination.addAll(calorieNegativePerkCombination);
 					}
 				
 				}
 				catch(Exception ex)
 				{
 					
 				}
 			}
 		}
 		return calorieNeutralPerks;
 	}
 	
 	
 	private ArrayList<ArrayList<Perk>> buildPerkCombinationsList(ArrayList<Perk> perks, ArrayList<ArrayList<Integer>> combinations)
 	{
 		ArrayList<ArrayList<Perk>> perkCombinations = new ArrayList<ArrayList<Perk>>();
 		for(ArrayList<Integer> combination: combinations)
 		{
 			ArrayList<Perk> perkCombination = new ArrayList<Perk>();
 			for(Integer i:combination)
 			{
 				perkCombination.add(perks.get(i));
 			}
 			perkCombinations.add(perkCombination);
 		}
 		return perkCombinations;
 	}
 	
 	private int sumPerkCombinations(ArrayList<Perk> perk1, ArrayList<Perk> perk2) throws Exception
 	{
 		if(perk1.size()==0 && perk2.size()==0)
 			throw new Exception("Both Perk combinations contain no perks!");
 		int sum = 0;
 		for(Perk perk:perk1)
 		{
 			sum+= perk.getCaloricImpact();
 		}
 		for(Perk perk:perk2)
 		{
 			sum+= perk.getCaloricImpact();
 		}
		System.out.println(sum);
 		return sum;
 	}
 }
