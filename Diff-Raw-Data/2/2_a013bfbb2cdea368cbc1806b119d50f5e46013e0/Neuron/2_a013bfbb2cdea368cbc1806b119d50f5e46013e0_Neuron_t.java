 import java.util.Arrays;
 import java.util.Random;
 
 public class Neuron {
 	private float[] w;
 	private float threshold;
 
 	public Neuron(int inputLenght, float threshold) {
 		Random generator = new Random();
 		this.threshold = threshold;
 		w = new float[inputLenght];
 		for (int i = 0; i < inputLenght; i++) {
 			w[i] = generator.nextFloat() - 0.5f; // [-0.5 , 0.5]
 		}
 	}
 
 	public float evaluate(float[] input) {
 		float total = 0;
 		for (int i = 0; i < input.length; i++) {
 			total += w[i] * input[i];
 		}
		return (total < threshold) ? 0 : 1;
 	}
 	
 	public float[] getWeights() {
 		return w;
 	}
 
 	public float getThreshold() {
 		return threshold;
 	}
 	
 	public void setThreshold(float threshold) {
 		this.threshold = threshold;
 	}
 	
 	@Override
 	public String toString() {
 		String s = "Neuron {";
 		s += Arrays.toString(w) + ", ";
 		s += threshold;
 		return s + "}";
 	}
 }
