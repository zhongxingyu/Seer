 package geneticalgorithm.breakcriteria;
 
 import geneticalgorithm.Chromosome;
 import geneticalgorithm.Configuration;
 import neuronalnetwork.MSE;
 import neuronalnetwork.NetConfiguration;
 
 public class MinErrorBreakCriteria extends BreakCriteria {
 
 	public MinErrorBreakCriteria(Configuration config) {
 		super(config);
 		System.out.println("\tMin error criteria: " + config.minError_breakCriteria);
 	}
 
 	@Override
 	public boolean isFinished() {
 		double acum = 0;
 		NetConfiguration netConfig = config.netConfig;
 		for (Chromosome chrom : config.population) {
 			acum += MSE.calc(chrom.createIndividual(), netConfig.f, netConfig.training);
 		}
		acum /= config.population.length;
 		return acum < config.minError_breakCriteria;
 	}
 
 }
