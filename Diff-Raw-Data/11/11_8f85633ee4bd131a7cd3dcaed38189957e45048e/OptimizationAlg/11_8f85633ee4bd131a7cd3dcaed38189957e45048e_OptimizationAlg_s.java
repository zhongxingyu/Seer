 package brown.tac.adx.optimization;
 
 import brown.tac.adx.models.Modeler;
 
 public abstract class OptimizationAlg {
 	/*
 	 * Takes in reference to modeler so that algos can query the models
 	 */
 	protected Modeler _modeler;
 	/*
 	 * Takes in optimization messenger to liase between opt algos and agent
 	 */
 	protected OptimizationMessenger _optMessenger;
 	
 	protected OptimizationAlg(Modeler modeler, OptimizationMessenger optMessenger){
 		_modeler = modeler;
 		_optMessenger = optMessenger;
 	}
	abstract void makeDecision();
 }
