 package geneticalgorithm.selector;
 
 import geneticalgorithm.Chromosome;
 import geneticalgorithm.Configuration;
 
 import java.util.Arrays;
 
 /**
  * Por cada chormosoma, me quedo con los k mas aptos seteados en la
  * configuracion.
  */
 public class EliteSelector extends CandidateSelector {
 
 	// Used to sort chromosome to choose the best k
 	private ChromosomeContainer[] array;
 
 	public EliteSelector(Configuration config) {
 		super(config);
 	}
 
 	@Override
 	public int[] select() {
 		return selectBestK((int) (config.G * config.N));
 	}
 
 	private int[] selectBestK(int k) {
 		sortPopulationByFitness();
 		// Get the best k individuals
 		int[] selected = new int[k];
 		for (int i = 0; i < selected.length; i++) {
 			selected[i] = array[i].index;
 		}
 		return selected;
 	}
 
 	private void sortPopulationByFitness() {
 		Chromosome[] population = config.population;
 		if (array == null) {
 			initArray(population.length);
 		}
 		for (int i = 0; i < population.length; i++) {
 			array[i].index = i;
			array[i].fitness = population[i].getFitnes();
 		}
 		Arrays.sort(array);
 	}
 
 	private void initArray(int length) {
 		array = new ChromosomeContainer[length];
 		for (int i = 0; i < length; i++) {
 			array[i] = new ChromosomeContainer();
 		}
 	}
 
 	@Override
 	public void replace(Chromosome[] childs) {
 		Chromosome[] oldPpulation = config.population;
 		Chromosome[] newPopulation = new Chromosome[config.N];
 		int[] selected = selectBestK(config.N - childs.length);
 		int index = 0;
 		while (index < childs.length) {
 			newPopulation[index] = childs[index];
 			index++;
 		}
 		for (int i = 0; i < selected.length; index++, i++) {
 			newPopulation[index] = oldPpulation[selected[i]];
 		}
 		// update population
 		config.population = newPopulation;
 	}
 
 	private static class ChromosomeContainer implements
 			Comparable<ChromosomeContainer> {
 		int index;
 		float fitness;
 
 		@Override
 		public int compareTo(ChromosomeContainer o) {
 			return (int) (o.fitness - fitness);
 		}
 	}
 }
