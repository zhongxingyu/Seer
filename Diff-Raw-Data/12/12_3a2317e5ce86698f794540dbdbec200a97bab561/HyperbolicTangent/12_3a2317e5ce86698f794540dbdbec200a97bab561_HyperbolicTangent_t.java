 package hunternif.nn.activation;
 
 import hunternif.nn.IActivationFunction;
 
 public class HyperbolicTangent implements IActivationFunction {
 
 	@Override
 	public double produce(double input) {
 		return Math.tanh(input);
 	}
 
 	@Override
 	public double derivative(double input) {
		return 1/Math.cosh(input)/Math.cosh(input);
 	}
 
 }
