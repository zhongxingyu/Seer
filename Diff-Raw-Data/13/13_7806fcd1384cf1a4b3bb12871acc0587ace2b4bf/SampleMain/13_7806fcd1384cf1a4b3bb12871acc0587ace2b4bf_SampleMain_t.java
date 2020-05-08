 package it.unipi.rupos.processmining;
 
 import org.processmining.contexts.cli.ProMFactory;
 import org.processmining.contexts.cli.ProMManager;
 import org.deckfour.xes.model.XLog;
 import org.deckfour.xes.model.XTrace;
 import org.processmining.plugins.petrinet.replay.ReplayAction;
 import org.processmining.plugins.petrinet.replayfitness.ReplayFitnessSetting;
 import org.processmining.plugins.petrinet.replayfitness.TotalFitnessResult;
 import org.processmining.plugins.petrinet.replayfitness.TotalPerformanceResult;
 import org.processmining.plugins.petrinet.replayfitness.PerformanceVisualJS;
 import org.processmining.plugins.petrinet.replayfitness.PNVisualizzeJS;
 import org.processmining.plugins.bpmn.TraslateBPMNResult;
 
 /**
  * @author Dipartimento di Informatica - Rupos
  *
  */
 public class SampleMain {
     public static void main(String [] args) throws Exception {
 	
     	//String logFile = "../prom5_log_files/TracceRupos.mxml";
     	//String netFile = "../prom5_log_files/TracceRuposAlpha.pnml";
     	//String logFile = "../prom5_log_files/InviaFlusso.mxml";
     	//String netFile = "../prom5_log_files/InviaFlussoWoped.pnml";
     	//String logFile = "../prom5_log_files/InviaFlusso.mxml";
     	//String netFile = "../prom5_log_files/InviaFlussoWoped.pnml";
     	
 	//String logFile = "../prom5_log_files/provepar.mxml";
     	//String netFile = "../prom5_log_files/provepar3xProm6.pnml";
 	//String logFile = "../prom5_log_files/Export_Protocollo.xes";
     	//String netFile = "../prom5_log_files/porva.pnml";
 	String logFile = "../prom5_log_files/wsfm.mxml";
     	String netFile = "../prom5_log_files/residency.pnml";
         String BpmnFile = "../prom5_log_files/Residency.xpdl";
 	//String netFile = "../prom5_log_files/ReteAdHocRicorsionePerformace3xProm6.pnml";
         //String logFile = "../prom5_log_files/sequence.mxml";
     	//String netFile = "../prom5_log_files/seqAlphahiddenx6.pnml";
     	//String logFile = "../prom5_log_files/sequence.mxml";
     	//String netFile = "../prom5_log_files/sequence_prom6.pnml";
     	
 	ProMManager manager = new ProMFactory().createManager();
 	PetriNetEngine engine = manager.createPetriNetEngine(netFile);
 	System.out.println(engine);
 
 	engine = manager.createPetriNetEngine(netFile);
 	System.out.println(engine);
 
 	XLog log = manager.openLog(logFile);
 	System.out.println("Log size: " + log.size());
 
 	ReplayFitnessSetting settings = engine.suggestSettings(log);
 	System.out.println("Settings: " + settings);
 	settings.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
 	settings.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
 	settings.setAction(ReplayAction.REMOVE_HEAD, false);
 	settings.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, false);
 	settings.setAction(ReplayAction.INSERT_DISABLED_MATCH, false);
 	settings.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, false);
 	
 	
 	long startFitness = System.currentTimeMillis();
 	// TotalFitnessResult fitness = engine.getFitness(log, settings);
 	// System.out.println("Fitness: " + fitness);
 	long endFitness = System.currentTimeMillis();
 
 	//visualizza i dati di conformance con nella pagina html 
 	//PNVisualizzeJS js = new PNVisualizzeJS(manager.getPluginContext().getConnectionManager());
 	//js.generateJS("../javascrips/conformance.html", engine.net, fitness);
 	
 
 
 	System.out.println("Fitness for a single TRACE");
 
 	long startFitness2 = System.currentTimeMillis();
 	//fitness = engine.getFitness(log.get(0), settings);
 	//System.out.println("Fitness: " + fitness);
 	long endFitness2 = System.currentTimeMillis();
 	
 	
 	System.out.println("Time fitness single call " + (endFitness - startFitness));
 	System.out.println("Time fitness multiple calls " + (endFitness2 - startFitness2));
 	
 	
 	long startPerformance= System.currentTimeMillis();
 	// TotalPerformanceResult performance = engine.getPerformance(log, settings);
 	// System.out.println(performance);
 	long endPerformance = System.currentTimeMillis();
 
 	long startPerformance2 = System.currentTimeMillis();
 	//TotalPerformanceResult performance = engine.getPerformance(log.get(3), settings);
 	//System.out.println("Fitness: " + performance);
 	long endPerformance2 = System.currentTimeMillis();
 
 	//PerformanceVisualJS js2 = new PerformanceVisualJS(manager.getPluginContext().getConnectionManager());
 	//js2.generateJS("../javascrips/Performance.html", engine.net, performance.getList().get(0));
 	
 	
 	System.out.println("Time Performance single call " + (endPerformance - startPerformance));
 	System.out.println("Time Performance multiple calls " + (endPerformance2 - startPerformance2));
 
 
 	//traslate BPMN to  PN
 
 	TraslateBPMNResult traslate = manager.bpmnTOpn(BpmnFile);
 	System.out.println(traslate);
 
 
 	ReplayFitnessSetting settings2 = manager.suggestSettings(traslate.getPetri(), log);
 	settings2.setAction(ReplayAction.INSERT_ENABLED_MATCH, true);
 	settings2.setAction(ReplayAction.INSERT_ENABLED_INVISIBLE, true);
 	settings2.setAction(ReplayAction.REMOVE_HEAD, false);
 	settings2.setAction(ReplayAction.INSERT_ENABLED_MISMATCH, false);
 	settings2.setAction(ReplayAction.INSERT_DISABLED_MATCH, true);
 	settings2.setAction(ReplayAction.INSERT_DISABLED_MISMATCH, false);
 		
 	TotalFitnessResult fitnesstrasl = manager.getFitness(traslate.getPetri(), log, settings2, traslate.getMarking());
 	
 	System.out.println(fitnesstrasl);
 	
 	 
 	TotalPerformanceResult performance1 = manager.getPerformance(traslate.getPetri(), log.get(3), settings2, traslate.getMarking());
 	System.out.println("Fitness: " + performance1);
 
 	
 
 	PerformanceVisualJS js22 = new PerformanceVisualJS(manager.getPluginContext().getConnectionManager());
 		
 	js22.generateJS("../javascrips/PerformancedaBpmn.html", traslate.getPetri(), performance1.getList().get(0));
 	
 
 
	PNVisualizzeJS js = new PNVisualizzeJS(manager.getPluginContext().getConnectionManager());
	js.generateJS("../javascrips/conformance.html", traslate.getPetri(), fitnesstrasl);
 
 
 	
 	manager.closeContext();
     }
 }
