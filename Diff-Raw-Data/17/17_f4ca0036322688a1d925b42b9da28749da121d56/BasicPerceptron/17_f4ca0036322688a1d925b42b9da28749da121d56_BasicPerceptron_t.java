 package umk.neural.network.identify.numbers.perceptrons;
 
 import umk.neural.network.identify.numbers.activators.Activator;
 
 public class BasicPerceptron implements Perceptron {
 
 	private Activator activator;
 
 	private double[] weights;
 
 	@Override
 	public Activator getActivator() {
 		return activator;
 	}
 
 	@Override
 	public void setActivator(Activator activator) {
 		this.activator = activator;
 	}
 
 	@Override
 	public double[] getWeights() {
 		return weights;
 	}
 
 	@Override
 	public void setWeights(double[] weights) {
 		this.weights = weights;
 	}
 
 	private double compute(double[] in) {
 		if (in.length != weights.length) {
 			throw new IllegalArgumentException(
 					"Input data size does not match weights size");
 		}
 		double sum = 0;
 		for (int i = 0; i < in.length; i++) {
 			sum += in[i] * weights[i];
			//System.out.println(sum);
 			/**
 			 * Display input * weights 
 			 */
			//System.out.println("No."+i+" input*wagi = "+in[i] * weights[i]);
 		}
 		return sum;
 	}
 
 	@Override
 	public double eval(double[] in) {
 		return activator.eval(compute(in));
 	}
 
 }
