 /**
  * 
  */
 package simulator;
 
 import java.text.DecimalFormat;
 
 import utils.Log;
 import viewer.SCompleteArm;
 
 /**
  * Implements the sensori-motor loop between an Agent and a World.
  * 
  * @author alain.dutech@loria.fr
  */
 public class Simulator {
 	
 	DecimalFormat df3_5 = new DecimalFormat( "000.00000" );
 	
 	DynSystem _syst;
 	public double _timeSimu;
 	
 	public Simulator() {
 		// Initialisation
 		_syst = new DynSystem();
 	}
 	
 	/**
 	 * Set World in a starting state : position and consigne.
 	 */
 	public void reset() {
 		_timeSimu = 0.0;
 		_syst.reset();
 	}
 	
 	public void step( double deltaT ) {
 		_syst.updateAgents(_timeSimu);
 		_syst.updateWorld(_timeSimu, deltaT);
 		
 		_timeSimu += deltaT;
 	}
 	
 	/**
 	 * Run in Batch mode with a given set of Parameters
 	 * @param param
 	 */
 	public void runBatch( Parameters param ) {
 		// Observer
		SCompleteArm armV = new SCompleteArm(_syst._world);
 		_syst._world.addObserver(armV);
 		
 		Log<String> logFile = null;
 		
 		if (param.logScreen ) {
 			System.out.println("# time\t"+armV.explainStr);
 		}
 		if (param.logFile != "") {
 			logFile = new Log<String>(param.logFile);
 			logFile.writeLine("# time\t"+armV.explainStr);
 		}
 		
 		reset();
 		if (param.logScreen) {
 			System.out.println(df3_5.format(_timeSimu)+"\t"+armV.viewStr);
 		}
 		if (logFile != null) {
 			logFile.write(df3_5.format(_timeSimu)+"\t"+armV.viewStr);
 		}
 		while (_timeSimu < param.maxTime ) {
 			step(param.deltaTime);
 			if (param.logScreen) {
 				System.out.println(df3_5.format(_timeSimu)+"\t"+armV.viewStr);
 			}
 			if (logFile != null) {
 				logFile.write(df3_5.format(_timeSimu)+"\t"+armV.viewStr);
 			}
 		}
 		if (logFile != null) {
 			logFile.close();
 		}
 	}
 }
