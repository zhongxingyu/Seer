 package genBot2;
 
 import java.util.Arrays;
 import java.util.Random;
 
 public class Cocktail implements Comparable<Cocktail> {
 	
 	private IngredientAmount[] ingredientAmounts;
 	private double fitness;
 
 	public Cocktail(double[] amount) {
 		
 		Ingredient[] ingredients = IngredientArray.getInstance().getAllIngredients();
 		
 		if (amount.length != ingredients.length) {
 			throw new IllegalArrayLengthException("amount has length " + amount.length + ", should have length " + ingredients.length);
 		}
 		
 		double sum = 0;
 		for (int i = 0; i < amount.length; i++) {
 			if (amount[i] < 0) {
 				throw new IllegalIngredientException("No ingredients < 0 allowed");
 			}
 			sum = sum + amount[i];
 		}
 		
 		ingredientAmounts = new IngredientAmount[ingredients.length];
 		
 		for (int i = 0; i < ingredients.length; i++) {
 			ingredientAmounts[i] = new IngredientAmount(ingredients[i], amount[i] / sum);
 		}
 		
 		// -1 is a placeholder for not set yet
 		fitness = -1;
 	}
 	
 	public static Cocktail newRandomCocktail() {
 		int ingredientNumber = IngredientArray.getInstance().getAllIngredients().length;
 		
 		double[] randoms = new double[ingredientNumber + 1];
 		Random generator = new Random();
 		
 		for (int i = 0; i < randoms.length - 2; i++) {
 			randoms[i] = generator.nextDouble();
 		}
 		randoms[randoms.length - 2] = 0;
 		randoms[randoms.length - 1] = 1;
 		
 		Arrays.sort(randoms);
 		
 		double[] ingredientAmounts = new double[ingredientNumber];
 		
 		for (int i = 0; i < ingredientNumber; i++) {
 			ingredientAmounts[i] = randoms[i + 1] - randoms[i];
 		}
 		
 		return new Cocktail(ingredientAmounts);
 	}
 	
 	public IngredientAmount[] getIngredientAmounts() {
 		return ingredientAmounts;
 	}
 	
 	public double getAmount(Ingredient ingredient) {
 		for (int i = 0; i < ingredientAmounts.length; i++) {
 			if (ingredientAmounts[i].getIngredient().equals(ingredient)) {
 				return ingredientAmounts[i].getAmount();
 			}
 		}
 		throw new IllegalIngredientException("Ingredient " + ingredient.getName() + "is not defined");
 	}
 	
 	public double getAmount(String ingredientName) {
 		Ingredient ingredient = IngredientArray.getInstance().getByName(ingredientName);
 		
 		try {
 			return getAmount(ingredient);
 		} catch (IllegalIngredientException e) {
 			throw new IllegalIngredientNameException("The ingredient " + ingredientName + "has not been defined!");
 		}
 	}
 	
 	public void setFitness(double fitness) {
 		this.fitness = fitness;
 	}
 	
 	public double getFitness() {
 		return fitness;
 	}
 	
 	public Cocktail mutate(double stdDeviation) {
 		if (stdDeviation < 0) {
 			throw new IllegalArgumentException("stdDeviation must be greater than 0 (" + stdDeviation + "was specified)!");
 		}
 		
 		Ingredient[] ingredients = IngredientArray.getInstance().getAllIngredients();
 		double[] amounts = new double[IngredientArray.getInstance().getNumberOfIngredients()];
 		
 		for (int i = 0; i < IngredientArray.getInstance().getNumberOfIngredients(); i++) {
 			amounts[i] = getAmount(ingredients[i]);
 		}
 		
 		// mutate one of the amounts
 		Random rnd = new Random();
 		int mutatedIngredient = rnd.nextInt(IngredientArray.getInstance().getNumberOfIngredients());
 		double change = rnd.nextGaussian() * stdDeviation;
 		
 		// Make sure the bounds are not violated
 		if (change + amounts[mutatedIngredient] < 0) {
 			change = amounts[mutatedIngredient] * (-1);
 		} else if (change + amounts[mutatedIngredient] > 1) {
 			change = 1 - amounts[mutatedIngredient];
 		}
 		
 		// change the amount of the specific ingredient
 		amounts[mutatedIngredient] = amounts[mutatedIngredient] + change;
 		
 		// now change the other amounts
 		double changeOthers = (change / (IngredientArray.getInstance().getNumberOfIngredients() - 1)) * (-1);
 		
 		for (int i = 0; i < IngredientArray.getInstance().getNumberOfIngredients(); i++) {
 			if (i != mutatedIngredient) {
 				amounts[i] = amounts[i] + changeOthers;
 			}
 		}
 		
 		return new Cocktail(amounts);
 	}
 	
 	public String toString() {
 		String out = "";
 		
 		for (int i = 0; i < ingredientAmounts.length; i++) {
 			out += ingredientAmounts[i].toString();
 //			if (i < ingredientAmounts.length - 1) {
 				out += ", ";
 //			}
 		}
 		out += "Fitness: " + fitness;
 		
 		return out;
 	}
 	
 	public void setFitness(CheckFitness fitnessCheck) {
 		fitness = fitnessCheck.checkFitness(this);
 	}
 
 	@Override
 	public int compareTo(Cocktail otherCocktail) {
 		if (getFitness() < otherCocktail.getFitness()) {
 			return -1;
 		} else if (getFitness() > otherCocktail.getFitness()) {
 			return 1;
 		} else {
 			return 0;
 		}
 	}
 }
