 package nature;
 
 import java.util.Random;
 
 public class SimulatedAnnealingBitString extends Algorithm<BitString> {
 	private static final Random random = new Random();
 
 	private FitnessGoal<BitString> fitnessGoal;
 	private BitString current;
 	private double initialTemperature;
 	private double finalTemperature;
 	private long maxTime;
 
 	private CoolingSchedule coolingSchedule;
 
 
 	public SimulatedAnnealingBitString(ProgressListener<BitString> progressListener, FitnessGoal<BitString> fitnessGoal, BitString bitString, double initialTemperature, double finalTemperature, long maxTime, CoolingSchedule coolingSchedule) {
 		super(progressListener);
 
 		this.initialTemperature = initialTemperature;
 		this.finalTemperature = finalTemperature;
 		this.maxTime = maxTime;
 		this.fitnessGoal = fitnessGoal;
 		this.current = bitString;
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
 		BitString mutation = current.localMutation();
 
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
