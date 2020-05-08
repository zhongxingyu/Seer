 package genBot2;
 
 public class MutationAndIntermediateRecombination implements Recombination {
 
 	private IntermediateRecombination intermediateRecombination;
 	private StandardMutation mutation;
 	
 	public MutationAndIntermediateRecombination(double variableArea, double stdDeviation) {
 		this.intermediateRecombination = new IntermediateRecombination(variableArea);
 		this.mutation = new StandardMutation(stdDeviation);
 	}
 
 	@Override
 	public CocktailGeneration recombine(CocktailGeneration population,
 			int newPopulationSize, boolean[] booleanAllowedIngredients) throws FitnessNotSetException {
 		
 		CocktailGeneration nextGeneration = intermediateRecombination.recombine(population, newPopulationSize, booleanAllowedIngredients);		
		nextGeneration = mutation.recombine(nextGeneration, newPopulationSize, booleanAllowedIngredients);
 		
 		return nextGeneration;
 	}
 
 	@Override
 	public double getMutationStdDeviation() {
 		return mutation.getStdDeviation();
 	}
 
 	@Override
 	public void setMutationStdDeviation(double stdDeviation) {
 		mutation.setMutationStdDeviation(stdDeviation);
 	}
 
 }
