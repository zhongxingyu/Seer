 package nature;
 
 import java.util.Random;
 
 public class SimulatedAnnealingPermutation extends Algorithm<Permutation> {
 	private static final Random random = new Random();
 
 	private FitnessGoal<Permutation> fitnessGoal;
 	private Permutation current;
 	private double initialTemperature;
 	private double finalTemperature;
 	private long maxTime;
 
 	private CoolingSchedule coolingSchedule;
 
 
 	public SimulatedAnnealingPermutation(ProgressListener<Permutation> progressListener, FitnessGoal<Permutation> fitnessGoal, Permutation Permutation, double initialTemperature, double finalTemperature, long maxTime, CoolingSchedule coolingSchedule) {
 		super(progressListener);
 
 		this.initialTemperature = initialTemperature;
 		this.finalTemperature = finalTemperature;
 		this.maxTime = maxTime;
 		this.fitnessGoal = fitnessGoal;
 		this.current = Permutation;
 		this.coolingSchedule = coolingSchedule;
 	}
 
 	private double alpha(long time) {
 		return Math.exp(1.0 / coolingSchedule.temperature(initialTemperature, finalTemperature, maxTime, time));
 	}
 
 	@Override
 	public void init() {
 		progressListener.select(current, fitnessGoal.evaluate(current));
 	}
 
 	@Override
 	public void step(long iteration) {
 		Permutation mutation = (random.nextDouble() < 0.5 ? current.twoOpt() : current.threeOpt());
 
 		int currentFitness = fitnessGoal.evaluate(current);
 		int mutationFitness = fitnessGoal.evaluate(mutation);
 
 		double probability = Math.min(1.0, Math.pow(alpha(iteration), fitnessGoal.difference(currentFitness, mutationFitness)));
 		if (random.nextDouble() < probability) {
 			current = mutation;
			currentFitness = mutationFitness;
 
 			progressListener.select(current, currentFitness);
 			if (fitnessGoal.isOptimal(current, currentFitness)) {
 				progressListener.done();
 				cancel();
 			}
 		}
 
 		if (iteration >= maxTime) {
 			progressListener.done();
 			cancel();
 		}
 	}
 }
