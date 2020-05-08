 import java.io.IOException;
 import java.util.PriorityQueue;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 public class Factory {
 	
 	private double masterClk;
 	private MachiningCenter mc;
 	private InspectionStation is;
 	private PriorityQueue<Event> scheduler;
 	private Logger logger;
 	
 	public Factory(){
     	try {
 			BasicConfigurator.configure( new FileAppender(new PatternLayout(),"log.out"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
     	BasicConfigurator.configure();
     	logger = Logger.getLogger(Factory.class);
 
 		masterClk = 0;
 		mc = new MachiningCenter(this, logger);
 		is = new InspectionStation(this, logger);
 		scheduler = new PriorityQueue<Event>();
 	}
 	
 	public MachiningCenter getMachiningCenter(){
 		return this.mc;
 	}
 	
 	public InspectionStation getInspectionStation(){
 		return this.is;
 	}
 	
 	private void initialize(){
 		mc.scheduleArrivalEvent(masterClk);
 		mc.scheduleBreakDownEvent(masterClk);
 	}
 	
 	public void addEvent(Event evt){
 		scheduler.add(evt);
 	}
 	
 	public void run(){
 		initialize();
//		int breakDownEventsCount = 0;
 		
//		while(breakDownEventsCount < 14){
		while(masterClk < 40*50){
 			Event evt = scheduler.poll();
 			
 			masterClk = evt.getTime();
 			EventType type = evt.getType();
 			
 			if(type == EventType.inspectionCompletion){
 				is.inspectionCompletionEvent(masterClk);
 			} else if(type == EventType.mcArrival){
 				mc.arrivalEvent(masterClk);
 			} else if(type == EventType.mcBreakDown){
 				mc.breakDown(masterClk);
//				breakDownEventsCount++;
 			} else if(type == EventType.mcRepair){
 				mc.repairEvent(masterClk);
 			} else if(type == EventType.mcServiceCompletion){
 				mc.serviceCompletionEvent(masterClk);
 			}
 		}
 	}
 }
