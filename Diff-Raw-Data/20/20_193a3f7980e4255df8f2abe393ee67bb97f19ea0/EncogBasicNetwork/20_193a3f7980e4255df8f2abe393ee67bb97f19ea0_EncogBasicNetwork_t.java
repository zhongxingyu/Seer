 package neural.networks.encog;
 
import java.util.Arrays;

 import neural.networks.AbstractNetwork;
 import neural.parsec.ast.Layer;
 import neural.parsec.ast.NetworkDef;
 import neural.parsec.ast.TrainingDef;
 
 import org.encog.engine.network.activation.ActivationFunction;
 import org.encog.engine.network.activation.ActivationSigmoid;
 import org.encog.ml.data.MLDataSet;
 import org.encog.ml.data.basic.BasicMLDataSet;
 import org.encog.ml.train.MLTrain;
 import org.encog.neural.networks.BasicNetwork;
 import org.encog.neural.networks.layers.BasicLayer;
 import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
 
 public class EncogBasicNetwork extends AbstractNetwork {
 	
 	private BasicNetwork basicNetwork;
 	private TrainingDef trainingDef;
 	
 	@Override
 	public void initNetwork(NetworkDef def) {
 		super.initNetwork(def);
 		basicNetwork = new BasicNetwork();
 		createLayers(def);
 		basicNetwork.getStructure().finalizeStructure();
 		basicNetwork.reset(); // reset weights
 	}
 
 	private void createLayers(NetworkDef def) {
 		Layer layer;
 		ActivationFunction activationFunction;
 		int numberOfLayers = def.getLayers().size();
 		for (int i=0; i<numberOfLayers; i++) {
 			
 			layer = def.getLayers().get(i);
 			//convert to abstract factory later
 			if (layer.getActivation().equals("input"))
 				activationFunction = null;
 			else if (layer.getActivation().equals("sigmoid"))
 				activationFunction = new ActivationSigmoid();
 			else throw new UnsupportedOperationException("Need to build in more acitvation types");
 			
 			basicNetwork.addLayer(new BasicLayer(activationFunction, layer.isBiased(), layer.getSize()));
 			//System.out.println("Adding layer "+i+" "+layer.toString());
 		}
 	}
 
 	@Override
 	public void initTraining(TrainingDef def) {
 		super.initTraining(def);
 		this.trainingDef = def;
 	}
 
 	public void train() {
		System.out.println("Input : "+Arrays.deepToString(trainingDef.getInputData()));
		System.out.println("output: "+Arrays.deepToString(trainingDef.getOutputData()));
 		MLDataSet dataSet = new BasicMLDataSet(trainingDef.getInputData(), trainingDef.getOutputData());		
 		MLTrain trainer;
 		
 		//replace with factory
 		if (trainingDef.getType().equals("resilient_propagation"))
 			trainer = new ResilientPropagation(basicNetwork, dataSet);
 		else
 			throw new UnsupportedOperationException("Havent implemented properly yet");
 		
 		int epoch = 0;
 		do {
 			trainer.iteration();
 			epoch++;
 			System.out.println("Epoch "+epoch+": error = "+trainer.getError());
 		} while (trainer.getError() > trainingDef.getError());
 		
 	}
 	
 	public void compute(double[] input, double[] output) {
 		basicNetwork.compute(input, output);
 	}
 
 }
