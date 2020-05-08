 package nature;
 
 import java.util.Random;
 
 public class OnePlusOnePermutation extends Algorithm<Permutation> {
 	private static final Random random = new Random();
 
 	private FitnessGoal<Permutation> fitnessGoal;
 	private Permutation current;
 
 	public OnePlusOnePermutation(ProgressListener<Permutation> progressListener, FitnessGoal<Permutation> fitnessGoal, Permutation permutation) {
 		super(progressListener);
 
 		this.fitnessGoal = fitnessGoal;
 		this.current = permutation;
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
 
 		if (fitnessGoal.compare(currentFitness, mutationFitness) <= 0) {
 			current = mutation;
 
 			progressListener.select(current, currentFitness);
 			if (fitnessGoal.isOptimal(current, currentFitness)) {
 				progressListener.done();
 				cancel();
 			}
 		}
 	}
 }
