 package neuronalnetwork;
 
 import java.util.List;
 
 import neuronalnetwork.function.TransferFunction;
 
 public class MSE {
 
 	public static float calc(NeuralNetwork net, TransferFunction f,
 			List<TrainItem> testExamples) {
 		float mse = 0;
 		for (TrainItem test : testExamples) {
 			mse += calc(net, f, test.input, test.output);
 		}
		return mse / (2 * testExamples.size());
 	}
 
 	public static float calc(NeuralNetwork net, TransferFunction f,
 			float[] input, float[] expectedOutput) {
 		float[] outout = net.evaluate(input, f);
 		float mse = 0;
 		for (int i = 0; i < outout.length; i++) {
 			float diff = expectedOutput[i] - outout[i];
 			mse += diff * diff;
 		}
 		return mse;
 	}
 }
