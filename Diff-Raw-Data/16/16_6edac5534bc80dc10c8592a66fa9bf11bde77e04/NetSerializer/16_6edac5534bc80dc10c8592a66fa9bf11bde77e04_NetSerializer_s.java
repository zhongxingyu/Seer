 package neuronalnetwork;
 
 import geneticalgorithm.Chromosome;
 
 public class NetSerializer {
 	
 	public static void toArray(Layer[] layers, float[] array, int start) {
 		int end = totalConnections(layers) - 1;
 		toArray(layers, array, start, end);
 	}
 	
 	/**
 	 * Serializa la red al array desde start hasta end inclusive.
 	 */
 	public static void toArray(Layer[] layers, float[] array, int start, int end) {
		int offset = start;
 		for (Layer l: layers) {
 			// save weights
 			float[][] weights = l.getWeights();
 			for (int i = 0; i < weights.length; i++) {
 				for (int j = 0; j < weights[0].length; j++) {
 					if (offset > end) {
 						return;
 					}
					array[offset++] = weights[i][j];
 				}
 			}
 			// save bias
 			float[] bias = l.getBias();
 			for (int j = 0; j < bias.length; j++) {
 				if (offset > end) {
 					return;
 				}
				array[offset++] = bias[j];	
 			}
			
 		}
 	}
 
 	public static Chromosome fromArray(int[] structure, float[] array) {
 		int offset = 0;
 		NeuralNetwork net = new NeuralNetwork(structure);
 		for (Layer l: net.getLayers()) {
 			// load weights
 			float[][] weights = l.getWeights();
 			float[] bias = l.getBias();
 			for (int i = 0; i < weights.length; i++) {
 				for (int j = 0; j < weights[0].length; j++) {
 					weights[i][j] = array[offset++];
 				}
 			}
 			// load bias
 			for (int j = 0; j < bias.length; j++) {
 				bias[j] = array[offset++];	
 			}
 		}
 		return new Chromosome(net.getLayers());
 	}
 
 	
 	public static int totalConnections(int[] structure) {
 		int connections = 0;
 		for (int i = 1; i < structure.length; i++) {
 			int input = structure[i - 1];
 			int neurons = structure[i];
 			connections += input * neurons + neurons;
 		}
 		return connections;
 	}
 	
 	public static int totalConnections(Layer[] layers) {
 		int connections = 0;
 		for(Layer l: layers) {
 			connections += l.getInputLen() * l.getNeuronsDim() + l.getNeuronsDim();
 		}
 		return connections;
 	}
 }
