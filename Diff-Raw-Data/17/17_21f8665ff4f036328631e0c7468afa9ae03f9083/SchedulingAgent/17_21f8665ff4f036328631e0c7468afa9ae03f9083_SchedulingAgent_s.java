 package at.ac.tuwien.lsdc.mape;
 
 import at.ac.tuwien.lsdc.actions.Action;
 import at.ac.tuwien.lsdc.generator.RequestGenerator;
 import at.ac.tuwien.lsdc.resources.Resource;
 
 public class SchedulingAgent {
 	private static volatile SchedulingAgent instance;
 	private Analyser analyser;
 	private Planner planner;
 	private Executor executor;
 	
 	private SchedulingAgent() {
 		analyser = new Analyser();
 		planner = new KISSPlanner();
 		executor = new Executor();
 	}
 	
 	public static SchedulingAgent getInstance() {
 		if (SchedulingAgent.instance == null) {
 			synchronized (SchedulingAgent.class) {
 				if (SchedulingAgent.instance == null) {
 					SchedulingAgent.instance = new SchedulingAgent();
 				}
 			}
 		}
 		
 		return SchedulingAgent.instance;
 	}
 	
	public void start() {
 		while (true) { 
 			//TODO: gst: werden immer Requests generiert??
			RequestGenerator.getInstance().generateRequest();
 			Resource problem = analyser.getTopProblem();
 			Action solution = planner.selectAction(problem);
 			executor.execute(solution);
 			Monitor.getInstance().nextTick();
 		}
 	}
 }
