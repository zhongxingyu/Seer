 package suite.algo;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import suite.util.Util;
 
 public class ArtificialNeuralNetwork {
 
 	private float learningRate = 0.2f;
 	private int nLayers;
 	private List<float[][]> weightsByLayer = new ArrayList<>();
 
 	public ArtificialNeuralNetwork(List<Integer> layerSizes, Random random) {
 		nLayers = layerSizes.size() - 1;
 
 		for (int layer = 0; layer < nLayers; layer++) {
 			int nInputs = layerSizes.get(layer);
 			int nOutputs = layerSizes.get(layer + 1);
 			float weights[][] = new float[nInputs][nOutputs];
 
 			for (int i = 0; i < nInputs; i++)
 				for (int j = 0; j < nOutputs; j++)
 					weights[i][j] = 2 * random.nextFloat() - 1f;
 
 			weightsByLayer.add(weights);
 		}
 	}
 
 	public float[] feed(float inputs[]) {
 		return Util.last(calculateActivations(inputs));
 	}
 
 	public void train(float inputs[], float expected[]) {
 		backwardPropagate(calculateActivations(inputs), expected);
 	}
 
 	private List<float[]> calculateActivations(float values[]) {
 		List<float[]> outputs = new ArrayList<>();
 		outputs.add(values);
 
 		for (int layer = 0; layer < nLayers; layer++) {
 			float weights[][] = weightsByLayer.get(layer);
 			int nInputs = weights.length;
 			int nOutputs = weights[0].length;
 
 			float values1[] = new float[nOutputs];
 
 			for (int j = 0; j < nOutputs; j++) {
 				float sum = 0f;
 				for (int i = 0; i < nInputs; i++)
 					sum += values[i] * weights[i][j];
 				values1[j] = activationFunction(sum);
 			}
 
 			outputs.add(values = values1);
 		}
 
 		return outputs;
 	}
 
 	private void backwardPropagate(List<float[]> activations, float expected[]) {
 		float errors[] = null;
 
 		for (int layer = nLayers; layer > 0; layer--) {
 			float weights0[][] = weightsByLayer.get(layer - 1);
 			int n0 = weights0.length;
 			int n1 = weights0[0].length;
 			float diffs[] = new float[n1];
 
 			float ins[] = activations.get(layer - 1);
 			float outs[] = activations.get(layer);
 
 			if (layer < nLayers) {
 				float weights1[][] = weightsByLayer.get(layer);
 				int n2 = weights1[0].length;
 
 				for (int j = 0; j < n1; j++) {
 					float sum = 0f;
 					for (int k = 0; k < n2; k++)
 						sum += errors[k] * weights1[j][k];
 					diffs[j] = sum;
 				}
 			} else
 				for (int j = 0; j < n1; j++)
 					diffs[j] = expected[j] - outs[j];
 
 			float errors1[] = new float[n1];
 
 			for (int j = 0; j < n1; j++) {
 				errors1[j] = diffs[j] * activationFunctionGradient(outs[j]);
 				for (int i = 0; i < n0; i++)
 					weights0[i][j] += learningRate * errors1[j] * ins[i];
 			}
 
 			errors = errors1;
 		}
 	}
 
 	private float activationFunction(float value) {
 		return 1f / (1f + (float) Math.exp(-value)) - 0.5f;
 	}
 
 	private float activationFunctionGradient(float value) {
		return value * (1f - value);
 	}
 
 }
