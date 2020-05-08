 import java.util.ArrayList;
 import java.util.Random;
 
 public class NeuralNetwork {
 
 	int inAmount;
 	int outAmount[];
 	ArrayList<double[][]> weights;
 	Random randGen;
 
 	public NeuralNetwork( int input, int output ) {
 		this(input, new int[] {output});
 	}
 	
 	public NeuralNetwork( int inputArguments, int outputArguments[] ) {
 		inAmount = inputArguments;
 		outAmount = outputArguments;
 		randGen = new Random();
 		weights = new ArrayList<double[][]>();
		int in = inAmount;
 		for (int n = 0; n < outAmount.length; n++ ){
 			int out = outAmount[n];
 			double[][] element = new double[in][out];
 			for ( int i = 0; i < in; i++ ) {
 				for ( int o = 0; o < out; o++ ) {
 					element[i][o] = randGen.nextDouble();
 				}
 			}
 			weights.add(element);
			in = out;
 		}
 	}
 
 	@Override
 	public String toString() {
 		String output = "Neural Network with " + inAmount + " inputs";
 		output += ", " + (outAmount.length-1) + " hidden layer(s)";
 		output += " and " + outAmount[outAmount.length-1] + " outputs.";
 		return output;
 	}
 
 	public double[] giveOutput( double[] input ) {
 		double[][] output = {input};
 		for ( int n = 0; n < weights.size(); n++ ) {
 			output = Matrix.multiply(output, weights.get(n));
 		}
 		return output[0];
 
 	}
 
 }
