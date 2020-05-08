 package neural.networks.neuroph;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.util.Properties;
 
 import neural.NeuralException;
 import neural.NeuralPropertyFactory;
 import neural.networks.AbstractNetwork;
 import neural.parsec.ast.NetworkDef;
 import neural.parsec.ast.TrainingDef;
 
 import org.neuroph.core.Layer;
 import org.neuroph.core.NeuralNetwork;
 import org.neuroph.core.learning.SupervisedLearning;
 import org.neuroph.core.learning.SupervisedTrainingElement;
 import org.neuroph.core.learning.TrainingSet;
 import org.neuroph.core.transfer.Linear;
 import org.neuroph.nnet.comp.BiasNeuron;
 import org.neuroph.nnet.comp.InputNeuron;
 import org.neuroph.util.ConnectionFactory;
 import org.neuroph.util.LayerFactory;
 import org.neuroph.util.NeuralNetworkFactory;
 import org.neuroph.util.NeuronProperties;
 import org.neuroph.util.TransferFunctionType;
 import org.neuroph.util.random.NguyenWidrowRandomizer;
 
 public class FeedForwardNetwork extends AbstractNetwork {
 
 	private NeuralNetwork network;
 	private Properties activationMap;
 	
 	private TrainingDef trainingDef;
 	private NeuralPropertyFactory<SupervisedLearning> trainingFactory;
 
 	@Override
 	public void initNetwork(NetworkDef def) {
 		super.initNetwork(def);
 
 		//the following code is paraphrased from the create method in the MultiLayerPercepton class
 		//it couldn't be simply called as Neural allows finer control.
 		
 		initActivationMap();
 		network = new NeuralNetwork();
 
 		//ensure first layer exists and is input
 		if (!def.getLayers().get(0).getActivation().equals("input")) 
 			throw new NeuralException("Missing input layer (must be first");
 		
 		// create input layer
 		NeuronProperties inputNeuronProperties = new NeuronProperties(InputNeuron.class, Linear.class);
 		Layer layer = LayerFactory.createLayer(def.getLayers().get(0).getSize(), inputNeuronProperties);
 
 
 		if (def.getLayers().get(0).isBiased()) {
 			layer.addNeuron(new BiasNeuron());
 		}
 
 		network.addLayer(layer);
 
 		// create layers
 		Layer prevLayer = layer;
 
 		// for(Integer neuronsNum : neuronsInLayers)
 		for (int layerIdx = 1; layerIdx < def.getLayers().size(); layerIdx++) {
 			Integer neuronsNum = def.getLayers().get(layerIdx).getSize();
 			// createLayer layer
 			NeuronProperties neuronProperties = new NeuronProperties(
 					TransferFunctionType.valueOf(mapActivation(def.getLayers().get(layerIdx).getActivation())), 
 					def.getLayers().get(layerIdx).isBiased());
 			layer = LayerFactory.createLayer(neuronsNum, neuronProperties);
 
 			// add created layer to network
 			network.addLayer(layer);
 			// createLayer full connectivity between previous and this layer
 			if (prevLayer != null)
 				ConnectionFactory.fullConnect(prevLayer, layer);
 
 			prevLayer = layer;
 		}
 
 		// set input and output cells for network
 		NeuralNetworkFactory.setDefaultIO(network);
 
 		//note no learning rule is set - -
 		
 		network.randomizeWeights(new NguyenWidrowRandomizer(-0.7, 0.7));
 
 	}
 	
 	@Override
 	public void initTraining(TrainingDef def) {
 		super.initTraining(def);
 		this.trainingDef = def;
 		trainingFactory = new NeuralPropertyFactory<SupervisedLearning>(this.getPropertiesFilename(), "training");
 	}
 
 
 	private void initActivationMap() {
 		activationMap = new Properties();
 		try {
 			activationMap.load(new FileInputStream(new File(this.getPropertiesFilename())));
 		} catch (FileNotFoundException e) {
 			throw new NeuralException("Cannot find properties file(" + this.getPropertiesFilename() + ")", e);
 		} catch (IOException e) {
 			throw new NeuralException("IO issues when loading neuroph properties", e);
 		}
 
 	}
 
 	private String mapActivation(String activation) {
 		return activationMap.getProperty("activation." + activation);
 	}
 
 	@Override
 	public void train() {
		SupervisedLearning trainer = trainingFactory.getNewInstance(trainingDef.getType());
 		int inputSize = trainingDef.getInputData()[0].length;
 		int outputSize = trainingDef.getOutputData()[0].length;
 		int numSets = trainingDef.getInputData().length;
 		
 		TrainingSet<SupervisedTrainingElement> trainingSet 
 			= new TrainingSet<SupervisedTrainingElement>(inputSize, outputSize);
 		
 		for (int i=0; i<numSets; i++) {
 			trainingSet.addElement(new SupervisedTrainingElement(trainingDef.getInputData()[i],
 					                                             trainingDef.getOutputData()[i]));
 		}
 		trainingSet.addElement(new SupervisedTrainingElement(new double[]{0, 0}, new double[]{0}));
 		 
		trainer.setNeuralNetwork(network);
		trainer.learn(trainingSet, trainingDef.getError(), trainingDef.getEpochs()); 
 	}
 
 	@Override
 	public void compute(double[] input, double[] output) {
 		network.setInput(input);
 		network.calculate();
 		double[] o = network.getOutput();
 		System.arraycopy(o, 0, output, 0, o.length);
 	}
 
 }
