 package genBot2;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Random;
 
 public class CocktailGeneration {
 	
 	private int populationSize;
 	private Cocktail[] population;
 	
 	private int[] randomPopulationOrder;
 	private int randomPopulationPosition;
 
 	public CocktailGeneration(int populationSize, Cocktail[] population) {
 		
 		this.populationSize = populationSize;
 		
 		this.population = population;
 		
 		this.randomPopulationOrder = generateRandomPopulationOrder(populationSize);
 		
 		this.randomPopulationPosition = 0;
 	}
 	
 	private int[] generateRandomPopulationOrder(int populationSize) {
 		int[] randomPopulationOrder = new int[populationSize];
 		for (int i = 0; i < populationSize; i++) {
 			randomPopulationOrder[i] = i;
 		}
 		
 		Random rnd = new Random();
 		for (int i = randomPopulationOrder.length - 1; i > 0; i--) {
 			int index = rnd.nextInt(i + 1);
 			// Simple swap
 			int a = randomPopulationOrder[index];
 			randomPopulationOrder[index] = randomPopulationOrder[i];
 			randomPopulationOrder[i] = a;
 		}
 
 		return randomPopulationOrder;
 	}
 
 	public int getPopulationSize() {
 		return populationSize;
 	}
 	
 	public Cocktail[] getPopulation() {
 		return population;
 	}
 	
 	public Cocktail getCocktail(int cocktailNumber) {
 		if (cocktailNumber < 0 || cocktailNumber >= population.length) {
 			throw new IllegalArgumentException("There is no Cocktail number " + cocktailNumber + "in this generation!");
 		} else {
 			return population[cocktailNumber];
 		}
 	}
 	
 	public Cocktail getNextRandomCocktail() {
 		Cocktail theCocktail =  getPopulation()[randomPopulationOrder[randomPopulationPosition]];
 		randomPopulationPosition = randomPopulationPosition + 1;
 		
 		return theCocktail;
 	}
 	
 //	public CocktailGeneration clone() {
 //		CocktailGeneration nextGen = new CocktailGeneration(getPopulationSize(), getPopulation());
 //		
 //		return nextGen;
 //	}
 	
 	public double[] rouletteWheelShares() {
 		double fitnessSum = 0;
 		for (int i = 0; i < populationSize; i++) {
 			if (population[i].getFitness() >= 0) {
 				fitnessSum = fitnessSum + population[i].getFitness();
 			}
 		}
 		
 		// throw new exception if sum = 0
 		
 		double[] rouletteWheelShare = new double[populationSize];
 		for (int i = 0; i < populationSize; i++) {
 			rouletteWheelShare[i] = population[i].getFitness() / fitnessSum;
 		}
 		
 		return rouletteWheelShare;
 	}
 	
 	public double[] generateRouletteWheel() {
 		double[] rouletteWheelShare = rouletteWheelShares();
 		
 		double[] rouletteWheel = new double[rouletteWheelShare.length];
 		
 		rouletteWheel[0] = rouletteWheelShare[0];
 		for (int i = 1; i < rouletteWheel.length; i++) {
 			rouletteWheel[i] = rouletteWheel[i - 1] + rouletteWheelShare[i];
 		}
 		
 		return rouletteWheel;
 	}
 	
 	public Cocktail crossover() {
 		Random rnd = new Random();
 		
 		double cocktailSelector1 = rnd.nextDouble();
 		double cocktailSelector2 = rnd.nextDouble();
				
		Cocktail cocktail1 = population[0];
		Cocktail cocktail2 = population[0];
 		
 		double[] rouletteWheel = generateRouletteWheel();
				
 		for (int i = 1; i < rouletteWheel.length; i++) {
			if (cocktailSelector1 > rouletteWheel[i - 1] && cocktailSelector1 < rouletteWheel[i]) {
				cocktail1 = population[i];
 			}
			if (cocktailSelector2 > rouletteWheel[i - 1] && cocktailSelector2 < rouletteWheel[i]) {
				cocktail2 = population[i];
 			}
 		}
 		
 		IngredientArray ingredientArray = IngredientArray.getInstance();
 		
 		double[] ingredients = new double[ingredientArray.getNumberOfIngredients()];
 		
 		for (int i = 0; i < ingredients.length; i++) {
 			if (rnd.nextBoolean()) {
 				ingredients[i] = cocktail1.getAmount(ingredientArray.getAllIngredients()[i]);
 			} else {
 				ingredients[i] = cocktail2.getAmount(ingredientArray.getAllIngredients()[i]);				
 			}
 		}
 		
 		return new Cocktail(ingredients);
 	}
 	
 	public CocktailGeneration allCrossovers(int populationSize) {
 		Cocktail[] population = new Cocktail[populationSize];
 		
 		for (int i = 0; i < populationSize; i++) {
 			population[i] = crossover();
 		}
 		
 		return new CocktailGeneration(populationSize, population);
 	}
 	
 	public void mutateCocktails(double stdDeviation) {
 		if (stdDeviation < 0) {
 			throw new IllegalArgumentException("stdDeviation must be greater than 0 (" + stdDeviation + "was specified)!");
 		}
 		for (int i = 0; i < populationSize; i++) {
 			population[i] = population[i].mutate(stdDeviation);
 		}
 	}
 
 	public void applyElitism(int elitism, CocktailGeneration cocktailGeneration) {
 		if (elitism < 0 || elitism > populationSize) {
 			throw new IllegalArgumentException("Invalid number of elite-Cocktails (" + elitism + ")!");
 		}
 		
 		// rank
 		int[] otherRandomOrder = generateRandomPopulationOrder(cocktailGeneration.getPopulationSize());
 		Cocktail[] previousCocktails = new Cocktail[cocktailGeneration.getPopulationSize()];
 		
 		for (int i = 0; i < populationSize; i++) {
 			previousCocktails[i] = cocktailGeneration.getPopulation()[otherRandomOrder[i]];
 		}
 		
 		Arrays.sort(previousCocktails, Collections.reverseOrder());
 		
 		// now we have the previous cocktails ranked. Now replace <elitism> cocktails in the current population
 		for (int i = 0; i < elitism; i++) {
 			population[i] = previousCocktails[i];
 		}
 	}
 	
 	public String toString() {
 		String out = "";
 		
 		for (int i = 0; i < getPopulationSize(); i++) {
 			out += "Cocktail " + i + ": " + population[i].toString() + "\n";
 		}
 		
 		return out;
 	}
 	
 	public String randomToString() {
 		String out = "";
 		
 		for (int i = 0; i < getPopulationSize(); i++) {
 			out += "Cocktail " + randomPopulationOrder[i] + ": " + population[randomPopulationOrder[i]].toString() + "\n";
 		}
 		
 		return out;
 	}
 }
