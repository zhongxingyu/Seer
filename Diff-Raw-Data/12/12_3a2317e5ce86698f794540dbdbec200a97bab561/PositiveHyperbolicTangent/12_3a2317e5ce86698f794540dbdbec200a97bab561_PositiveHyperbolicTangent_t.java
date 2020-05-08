 package hunternif.nn.activation;
 
 import hunternif.nn.IActivationFunction;
 
 public class PositiveHyperbolicTangent implements IActivationFunction {
 
 	@Override
 	public double produce(double input) {
		return 1 / (1 + Math.exp(-2*input));
 	}
 
 	@Override
 	public double derivative(double input) {
		return 0.5/Math.cosh(input)/Math.cosh(input);
 	}
 
 }
