 package de.unikassel.ann.config;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import de.unikassel.ann.algo.TrainingModule;
 import de.unikassel.ann.algo.WorkModule;
 import de.unikassel.ann.model.NetError;
 import de.unikassel.ann.model.Layer;
 import de.unikassel.ann.model.Network;
 import de.unikassel.ann.model.Neuron;
 import de.unikassel.ann.model.Synapse;
 import de.unikassel.ann.model.func.ActivationFunction;
 import de.unikassel.ann.rand.Randomizer;
 import de.unikassel.ann.strategy.Strategy;
import de.unikassel.ann.gui.TrainErrorListener;
 
 public class NetConfig {
 	
 	Network network;
 	List<Strategy> strategies;
 	WorkModule workModule;
 	TrainingModule trainingModule;
 	Randomizer randomizer;
 	List<NetError> errorLogs;
 	private int restartAmount = 0;
 	List<TrainErrorListener> trainErrorListener;
 	
 	public NetConfig() {
 		network = new Network();
 		network.setConfig(this);
 		strategies = new ArrayList<Strategy>();
 		errorLogs = new ArrayList<NetError>();
 		trainErrorListener = new ArrayList<TrainErrorListener>();
 	}
 	
 	public boolean shouldStopTraining() {
 		for (Strategy s : strategies) {
 			if (s.shouldStop()) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean shouldRestartTraining() {
 		for (Strategy s : strategies) {
 			if (s.shouldRestart()) {
 				initWeights();
 				restartAmount++;
 				reset();
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void reset() {
 		for (Strategy s : strategies) {
 			s.reset();
 		}
 		trainingModule.reset();
 	}
 
 	public void initWeights() {
 		Random r = new Random();
 		for (Synapse s : network.getSynapseSet()) {
 			s.setWeight((r.nextDouble() * 2) - 1);
 		}
 	}
 	
 	/**
 	 * Generates the network with this configuration
 	 */
 	public void buildNetwork() {
 		network.finalizeStructure();
 	}
 	
 	public Network getNetwork() {
 		return network;
 	}
 	
 	public void addOrUpdateExisting(Strategy strat) {
 		Strategy toRemove = null;
 		for (Strategy s : strategies) {
 			if (s.getClass().getSimpleName().equals(strat.getClass().getSimpleName())) {
 				toRemove = s;
 			}
 		}
 		if (toRemove != null) {
 			strategies.remove(toRemove);
 		}
 		strat.setConfig(this);
 		strategies.add(strat);
 	}
 
 	public void addLayer(int neuronCount, boolean bias, ActivationFunction function) {
 		Layer l = new Layer();
 		if (bias) {
 			l.addNeuron(new Neuron(function, true));
 		}
 		for (int i=0; i<neuronCount; i++) {
 			Neuron n = new Neuron(function, false);
 			l.addNeuron(n);
 		}
 		network.addLayer(l);
 		
 	}
 
 	public void addTrainingModule(TrainingModule train) {
 		this.trainingModule = train;
 		train.setConfig(this);
 	}
 	public void addWorkModule(WorkModule work) {
 		this.workModule = work;
 	}
 
 	public TrainingModule getTrainingModule() {
 		return trainingModule;
 	}
 	public WorkModule getWorkingModule() {
 		return workModule;
 	}
 
 	public List<Strategy> getStrategies() {
 		return strategies;
 	}
 
 	public void printStats() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("error = ");
 		NumberFormat fmt = new DecimalFormat("0.00000");
 		sb.append(fmt.format(trainingModule.getCurrentError()));
 //		sb.append(" / ");
 //		sb.append(fmt.format(trainingModule.getCurrentSingleError()));
 		sb.append("\n");
 		sb.append("learnSteps = ");
 		sb.append(trainingModule.getCurrentStep());
 		sb.append("\n");
 		sb.append("iterations = ");
 		sb.append(trainingModule.getCurrentIteration());
 		sb.append("\n");
 		if (restartAmount > 0) {
 			sb.append("restarted "+restartAmount+" times\n");
 		}
 		System.out.println(sb.toString());
 		
 	}
 	
 //	private class TrainErrorThread extends Thread {
 //		TrainErrorThread() {
 //			start();
 //		}
 //		public void run() {
 //			while (getTrainingModule().isTrainingNow()) {
 ////				trainErrorListener.get(0).addError(getTrainingModule().getCurrentIteration(), getTrainingModule().getCurrentError());
 //				trainErrorListener.get(0).updateUI();
 //			}
 //		}
 //	}
 //	
 //	private class MyWorker extends SwingWorker<Void, Void> {
 //		
 //		public MyWorker() {
 //			execute();
 //		}
 //		@Override
 //	    public Void doInBackground() {
 //			while (getTrainingModule().isTrainingNow()) {
 ////				trainErrorListener.get(0).addError(getTrainingModule().getCurrentIteration(), getTrainingModule().getCurrentError());
 //				trainErrorListener.get(0).updateUI();
 //			}
 //			return null;
 //	    }
 //	}
 
 	public void addTrainErrorListener(TrainErrorListener tel) {
 		trainErrorListener.add(tel);
 	}
 
 	public void notifyError(Double currentError) {
 		for (TrainErrorListener tel: trainErrorListener) {
 			tel.addError(getTrainingModule().getCurrentIteration(), currentError);
 			
 		}
 	}
 }
