 package geneticalgorithm;
 
 import neuronalnetwork.NeuralNetwork;
 
 public class GeneticAlgorithm {
 	
 	private Configuration config;
 
 	public GeneticAlgorithm(Configuration config) {
 		this.config = config;
 	}
 
 	/**
 	 * <pre>
 	 * 	Inicializar Poblacin 							- population 
 	 * 	Mientras no se alcance cond. de corte
 	 * 		Evaluar fitness								- evaluate
 	 * 		Seleccionar, 								- select
 	 * 		Cruzar. 									- cross
 	 * 		Mutacin 									- mutate
 	 * 		Reemplazar.									- replace
 	 * </pre>
 	 */
 	public NeuralNetwork getSolution() {
 		initPopulation();
 		while (!config.breakCriteria.isFinished(config)) {
 			config.selectionMethod.evaluate();
 			int[] selected = config.selectionMethod.select();
 			Chromosome[] childs = config.crossoverMethod.cross(selected);
 			config.crossoverMethod.mutate(childs);
 			config.replaceMethod.replace(childs);
 			config.elapsedGen++;
 		}
		return findBest().createIndividual();
 	}
 
 	private void initPopulation() {
 		config.population = new Chromosome[config.N];
 		for (int i = 0; i < config.N; i++) {
 			config.population[i] = new Chromosome(
 				new NeuralNetwork(config.netConfig.structure).getLayers());
 		}
 	}
 
	private Chromosome findBest() {
		Chromosome best = null;
		for (int i = 0; i < config.population.length; i++) {
			Chromosome c = config.population[i];
			if (best == null || best.getFitnes() < c.getFitnes()) {
				best = c;
			}
		}
		return best;
	}
 }
