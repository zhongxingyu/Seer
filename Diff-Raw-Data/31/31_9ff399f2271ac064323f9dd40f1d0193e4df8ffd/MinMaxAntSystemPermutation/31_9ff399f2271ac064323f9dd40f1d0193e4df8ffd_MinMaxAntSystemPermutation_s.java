 package nature;
 
 public class MinMaxAntSystemPermutation extends Algorithm<Permutation> {
 	private FitnessGoal<Permutation> fitnessGoal;
 	private Permutation current;
 	private double rho, alpha, beta;
 	private double[][] pheromone;
 
 	public MinMaxAntSystemPermutation(ProgressListener<Permutation> progressListener, FitnessGoal<Permutation> fitnessGoal, Permutation permutation, double rho, double alpha, double beta) {
 		super(progressListener);
 
 		this.fitnessGoal = fitnessGoal;
 		this.current = permutation;
 		this.rho = rho;
 		this.alpha = alpha;
 		this.beta = beta;
 	}
 
 	private void updatePheromones(Permutation permutation) {
 		int[] perm = permutation.getPermutation();
 
 		double tauMin = 1.0 / permutation.length();
 		double tauMax = 1.0 - 1.0 / permutation.length();
 
 		for (int i = 0; i < permutation.length(); i++) {
 			for (int j = 0; j <= i; j++) {
 				boolean edgeInPermutation = false;
 
 				for (int k = 0; k < perm.length - 1; k++) {
 					if (perm[k] == i && perm[k + 1] == j || perm[k + 1] == i && perm[k] == j) {
 						edgeInPermutation = true;
 						break;
 					}
 				}
 
 				if (edgeInPermutation) {
 					pheromone[i][j] = Math.min((1.0 - rho) * pheromone[i][j] + rho, tauMax);
 				} else {
 					pheromone[i][j] = Math.max((1.0 - rho) * pheromone[i][j], tauMin);
 				}
 
 				pheromone[j][i] = pheromone[i][j];
 			}
 		}
 	}
 
 	@Override
 	public void init() {
 		progressListener.select(current, fitnessGoal.evaluate(current));
 
 		int n = current.length();
 		double p = 1.0 / (n - 1);
 		pheromone = new double[n][n];
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j <= i; j++) {
 				pheromone[i][j] = p;
 				pheromone[j][i] = p;
 			}
 		}
 		current = current.constructMutation(pheromone, alpha, beta);
 		updatePheromones(current);
 	}
 
 	@Override
 	public void step(long iteration) {
 		Permutation mutation = current.constructMutation(pheromone, alpha, beta);
 
 		int currentFitness = fitnessGoal.evaluate(current);
 		int mutationFitness = fitnessGoal.evaluate(mutation);
 
 		if (fitnessGoal.compare(currentFitness, mutationFitness) <= 0) {
 			current = mutation;
 
 			progressListener.select(current, currentFitness);
 			if (fitnessGoal.isOptimal(current, currentFitness)) {
 				progressListener.done();
 				cancel();
 			}
 		}
 
 		updatePheromones(current);
 	}
 }
