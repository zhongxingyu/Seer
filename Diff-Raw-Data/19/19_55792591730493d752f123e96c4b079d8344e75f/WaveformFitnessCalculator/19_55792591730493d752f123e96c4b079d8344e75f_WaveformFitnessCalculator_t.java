 package edu.ntnu.EASY.neuron;
 
 /**
  * Fitness calculator using the waveform distance metric to calculate fitness. 
  * The distance measure used is the sum of the differences in voltages for all
  * points in the spike train.
  *
  */
 public class WaveformFitnessCalculator extends DistanceMetricCalculator {
 
 	public WaveformFitnessCalculator(double[] target) {
 		super(target);
 	}
 
 	@Override
 	public double calculate(double[] phenome) {
 		double delta = 0;
 		//Sum for 1 to lenght - 1, first entry is seed value and irrelevant.
 		for (int i = 1; i < phenome.length - 1; i++) {
 			//Absolute distance is what matters.
 			delta += Math.pow(phenome[i] - target[i], 2);
 		}
 		//Normalize
		delta = Math.sqrt(delta) / phenome.length;
 		// Turn into fitness value in interval [0,1].
 		return 1 / (1 + delta);
 	}
 
 }
