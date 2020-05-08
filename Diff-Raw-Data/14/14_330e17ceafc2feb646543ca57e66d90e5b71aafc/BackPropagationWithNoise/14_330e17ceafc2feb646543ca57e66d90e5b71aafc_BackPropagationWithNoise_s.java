 package neuronalnetwork;
 
 import java.util.List;
 
 import neuronalnetwork.function.TransferFunction;
 
 public class BackPropagationWithNoise extends BackPropagation {
 
 	private float pNoise = 0.01f;
 	private float noiseAmount = 0.01f;
 	
 	public BackPropagationWithNoise(TransferFunction f, float eta) {
 		super(f, eta);
 	}
 
 	public void train(NeuralNetwork net, List<TrainItem> train, int epochs) {
 		this.net = net;
 		while (epochs-- > 0) { 
 			for (TrainItem item: train) {
 				train(item.input, item.output);
 			}
 			addNoise();
 		}
 	}
 	
 	private void addNoise() {
 		for (Layer l: net.getLayers()) {
 			float[][] weights = l.getWeights();
 			for (int i = 0; i < weights.length; i++) {
				for (int j = 0; j < weights[0].length; j++) {
 					if (pNoise > Math.random()) {
						weights[i][j] += weights[i][j] * noiseAmount; 
 					}
 				}
 			}
 		}
 	}
 }
