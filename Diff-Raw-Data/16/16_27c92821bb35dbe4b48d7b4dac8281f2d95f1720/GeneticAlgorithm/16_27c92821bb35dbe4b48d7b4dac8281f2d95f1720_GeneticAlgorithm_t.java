 package geneticalgorithm;
 
 import neuronalnetwork.MSE;
 import neuronalnetwork.NetConfiguration;
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
 			config.mutationMethod.mutate(childs);
 			config.replaceMethod.replace(childs);
 			showPopulationStats();
 			config.elapsedGen++;
 		}
 		Chromosome best = findBest();
 		showFinalStats(best);
 		return best.createIndividual();
 	}
 
 	private void initPopulation() {
 		config.population = new Chromosome[config.N];
 		for (int i = 0; i < config.N; i++) {
 			config.population[i] = new Chromosome(new NeuralNetwork(
 					config.netConfig.structure).getLayers());
 		}
 	}
 
 	private void showPopulationStats() {
 		float avgMse = calcAvgMSE();
 		System.out.print("Generation: " + config.elapsedGen);
 		System.out.println(" -- AVG MSE: " + avgMse);
 	}
 	
 	private void showFinalStats(Chromosome best) {
 		NetConfiguration netConfig = config.netConfig;
 		float msetrain = MSE.calc(best.createIndividual(), netConfig.f, netConfig.training);
 		float mseGener = MSE.calc(best.createIndividual(), netConfig.f, netConfig.testing);
 		System.out.println("*** Algorithm finished. ***");
 		System.out.println("MSE Training " + msetrain);
 		System.out.println("MSE testing " + mseGener);
 	}
 	
 	private float calcAvgMSE() {
 		NeuralNetwork net = new NeuralNetwork();
 		float total = 0;
 		for (Chromosome c : config.population) {
 			net.setLayers(c.getLayers());
 			total += MSE.calc(net, config.netConfig.f, config.netConfig.testing);
 		}
 		return total / config.population.length;
 	}
 	
 	private Chromosome findBest() {
 		Chromosome best = null;
 		for (int i = 0; i < config.population.length; i++) {
 			Chromosome c = config.population[i];
			if (best == null || best.getFitness() < c.getFitness()) {
 				best = c;
 			}
 		}
 		return best;
 	}
 }
