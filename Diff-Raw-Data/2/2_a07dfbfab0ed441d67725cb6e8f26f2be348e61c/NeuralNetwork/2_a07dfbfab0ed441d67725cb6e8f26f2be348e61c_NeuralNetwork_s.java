 package cs485.neuralnetwork;
 
 import java.awt.List;
 import java.io.File;
 import java.util.ArrayList;
 
 import org.encog.ml.data.MLData;
 import org.encog.ml.data.MLDataSet;
 import org.encog.ml.data.basic.BasicMLDataSet;
 import org.encog.neural.data.NeuralData;
 import org.encog.neural.data.basic.BasicNeuralData;
 import org.encog.neural.data.basic.BasicNeuralDataSet;
 import org.encog.neural.networks.BasicNetwork;
 import org.encog.neural.networks.layers.BasicLayer;
 import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
 import org.encog.persist.EncogDirectoryPersistence;
 
 import scr.Action;
 import scr.SensorModel;
 
 public class NeuralNetwork {
 	
 	private final String FILENAME = "network.eg";
 	private BasicNetwork network;
 	private BasicNeuralDataSet dataset;
 	
 	public NeuralNetwork() {
 		try {
 			load();
 		} catch (Exception e) {
 			initialize();
 		}
 		dataset = new BasicNeuralDataSet();
 	}
 	
 	//Build & Initialize the Network
 	public void initialize() {
 		network = new BasicNetwork();
 		network.addLayer(new BasicLayer(23));	//Input Layer
 		network.addLayer(new BasicLayer(10));	//Hidden Layer
 		network.addLayer(new BasicLayer(7));	//Output Layer
 		network.getStructure().finalizeStructure();
 		network.reset();	//Initialize weights
 	}
 	
 	public void addData(BasicNeuralData input, BasicNeuralData idealOutout){
 		dataset.add(input, idealOutout);
 	}
 	
 	//trainer function
 	public void train() {
 		final ResilientPropagation train = new ResilientPropagation(network, dataset);
 		
 		int epoch = 1;
 		do {
 			train.iteration();
 			epoch += 1;
 		} while (train.getError() > 0.01);
 		
 		this.save();
 	}
 	
 	//Get Action based upon SensorModel
 	public Action getAction(SensorModel sensors) {
 		
 		
 		//Enter sensor Data into Neural Network
 		BasicNeuralData input = sensorToInput(sensors);
 		BasicNeuralData output = (BasicNeuralData) network.compute(input);
 		
 		//Create Action
 		Action newAction = new Action();
 		newAction.accelerate = output.getData(0);
 		newAction.brake = output.getData(1);
 		newAction.clutch = output.getData(2);
 		newAction.gear = (int) output.getData(3);
 		newAction.steering = output.getData(4);
 		if (output.getData(5) < 0.50 && output.getData(5) >= 0)
 			newAction.restartRace = true;
 		else
 			newAction.restartRace = false;
 		newAction.focus = (int) output.getData(6);
 		return newAction;
 	}
 	
 	public static BasicNeuralData sensorToInput(SensorModel sensors) {
 		//Fill sensor Data
 		ArrayList<Double> inputList = new ArrayList<Double>();
 		inputList.add(sensors.getSpeed());	//Speed
 		inputList.add(sensors.getAngleToTrackAxis());	//Angle
 		for (double d : sensors.getTrackEdgeSensors())	//19 Track Edge sensors
 			inputList.add(d);
 		/*
 		for (double d : sensors.getFocusSensors())	//5 Focus Sensors
 			inputList.add(d);
 		*/
 		inputList.add(sensors.getTrackPosition());	//Track Position
 		inputList.add((double)sensors.getGear());	//Gear
 		
 		double[] inputArray = new double[inputList.size()];
 		for (int i = 0; i < inputList.size(); i++)
 			inputArray[i] = inputList.get(i);
 		
 		BasicNeuralData input = new BasicNeuralData(inputArray);
 		return input;
 	}
 	
 	
 	public void save() {
 		EncogDirectoryPersistence.saveObject(new File(FILENAME), this.network);
 	}
 	
 	public void load() {
 		network = (BasicNetwork)EncogDirectoryPersistence.loadObject(new File(FILENAME));
 	}
 
 }
