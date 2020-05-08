 package foodsimulationmodel.relogo.environment;
 
 import java.util.Iterator;
 
 import foodsimulationmodel.relogo.UserObserver;
 
 import repast.simphony.context.Context;
 import repast.simphony.engine.schedule.AbstractAction;
 import repast.simphony.engine.schedule.ScheduleParameters;
 import repast.simphony.relogo.Observer;
 
 public class SimulationAction extends AbstractAction {
 	private Observer ob;
 	
 	
 	public SimulationAction(ScheduleParameters params) {
 		super(params);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public SimulationAction(ScheduleParameters params, Context context) {
 		super(params);
 		Iterator itr = context.iterator();
 		while(itr.hasNext()){
 			Object next = itr.next();
 			if(next.getClass() == UserObserver.class){
 				ob = (Observer) next;
 			}
 		}
 	}
 
 	@Override
 	public void execute() {
 		if(ob == null){
 			System.out.println("Observer not found");
 		}
 		else{
 			((UserObserver) ob).go();
 		}
 	}
 
 }
