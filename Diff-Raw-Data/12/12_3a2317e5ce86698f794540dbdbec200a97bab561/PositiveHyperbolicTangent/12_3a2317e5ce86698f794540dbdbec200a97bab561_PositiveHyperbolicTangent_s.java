 package hunternif.nn.activation;
 
 import hunternif.nn.IActivationFunction;
 
 public class PositiveHyperbolicTangent implements IActivationFunction {
 
 	@Override
 	public double produce(double input) {
		return 1 / (1 + Math.exp(-input));
 	}
 
 	@Override
 	public double derivative(double input) {
		return Math.exp(input) / Math.pow(Math.exp(input) + 1, 2);
 	}
 
 }
