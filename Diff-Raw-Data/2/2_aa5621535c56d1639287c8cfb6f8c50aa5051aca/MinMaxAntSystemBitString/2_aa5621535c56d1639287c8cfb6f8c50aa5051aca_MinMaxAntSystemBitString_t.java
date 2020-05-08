 package nature;
 
 public class MinMaxAntSystemBitString extends Algorithm<BitString> {
 	private FitnessGoal<BitString> fitnessGoal;
 	private BitString current;
 	private double rho;
 	private double[] pheromone;
 
 	public MinMaxAntSystemBitString(ProgressListener<BitString> progressListener, FitnessGoal<BitString> fitnessGoal, BitString bitString, double rho) {
 		super(progressListener);
 
		this.rho = Math.min(Math.max(rho, 0.0), 1.0);
 		this.fitnessGoal = fitnessGoal;
 		this.current = bitString;
 	}
 
 	private void updatePheromones(BitString bitString) {
 		boolean[] string = bitString.getString();
 		double tauMin = 1.0 / bitString.length();
 		double tauMax = 1.0 - 1.0 / bitString.length();
 		for (int i = 0; i < pheromone.length; i++) {
 			if (string[i]) {
 				pheromone[i] = Math.min((1.0 - rho) * pheromone[i] + rho, tauMax);
 			} else {
 				pheromone[i] = Math.max((1.0 - rho) * pheromone[i], tauMin);
 			}
 		}
 	}
 
 	@Override
 	public void init() {
 		pheromone = new double[current.length()];
 		current = current.constructMutation(pheromone);
 		updatePheromones(current);
 	}
 
 	@Override
 	public void step(long iteration) {
 		BitString mutation = current.constructMutation(pheromone);
 
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
