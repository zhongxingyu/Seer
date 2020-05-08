 package geneticalgorithm;
 
 import neuronalnetwork.Layer;
 import neuronalnetwork.NeuralNetwork;
 
 public class Chromosome {
 
 	private Layer[] layers;
 	private float fitness;
 	
 	public Chromosome(Layer[] layers) {
 		this.layers = layers;
 	}
 	
 	public Layer[] getLayers() {
 		return layers;
 	}
 
	public float getFitnes() {
 		return fitness;
 	}
 	
 	public void setFitness(float fitness) {
 		this.fitness = fitness;
 	}
 	
 	public NeuralNetwork createIndividual() {
 		return new NeuralNetwork(layers);
 	}
 }
