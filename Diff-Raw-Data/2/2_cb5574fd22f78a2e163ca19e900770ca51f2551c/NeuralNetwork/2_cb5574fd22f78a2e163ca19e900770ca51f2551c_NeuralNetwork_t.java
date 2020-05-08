 package org.neuralnetworking.core;
 
 import java.util.List;
 
 import org.neuralnetworking.util.LayerQueue;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Holds entire network through layers.
  * 
  * @author serkan
  * 
  */
 public class NeuralNetwork {
 
 	Logger logger = LoggerFactory.getLogger(NeuralNetwork.class);
 
 	private Layer inputLayer;
 
 	private Layer outputLayer;
 
 	private LayerQueue<Layer> layers;
 
 	public NeuralNetwork(int inputSize, int outputSize, int layerCount) {
 		logger.info("Input size : " + inputSize);
 		logger.info("Output size : " + outputSize);
 		logger.info("Hidden Layer Count : " + layerCount);
 		layers = new LayerQueue<Layer>();
 		// creating input layer
 		inputLayer = new Layer(inputSize, 0);
 		layers.add(inputLayer);
 		// previous layer
 		Layer preLayer = inputLayer;
		int preLayerSize = preLayer.getSize();
 		if (layerCount != 0) {
 			preLayerSize = preLayer.getSize();
 			// constructing hidden layers
 			Layer layer = null;
 			// detecting hidden layers' neuron size
 			int layerSize = inputSize - outputSize;
 			for (int i = 0; i < layerCount; i++) {
 				layer = new Layer(layerSize, preLayerSize);
 				layers.add(layer);
 				preLayer = layer;
 			}
 		}
 		// creating output layer
 		outputLayer = new Layer(outputSize, preLayerSize);
 		layers.add(outputLayer);
 	}
 
 	public void initInputs(double[] inputs) {
 		List<Neuron> neurons = inputLayer.getNeurons();
 		Neuron temp = null;
 		for (int i = 0; i < inputs.length; i++) {
 			temp = neurons.get(i);
 			temp.setNeuronInput(inputs[i]);
 		}
 	}
 
 	/**
 	 * @return the layers
 	 */
 	public LayerQueue<Layer> getLayers() {
 		return layers;
 	}
 
 	/**
 	 * Size of containing layers.
 	 * 
 	 * @return layers count
 	 */
 	public int getSizeOfLayers() {
 		return layers.size();
 	}
 
 	/**
 	 * Sum of all containing neurons.
 	 * 
 	 * @return Neurons size
 	 */
 	public int getSizeOfNeurons() {
 		int neuronSize = 0;
 		for (Layer layer : layers) {
 			neuronSize += layer.getSize();
 		}
 		return neuronSize;
 	}
 
 }
