 import org.apache.log4j.Logger;
 
 import distributionGenerators.ExpDistGenerator;
 import distributionGenerators.UniformDistGenerator;
 
 
 public class MachiningCenter extends Server{
 
 	private boolean brokenDown;
 	private double currentServiceStartTime;
 	private double currentServiceCompletionTime;
 	private double repairTime;
 	private ExpDistGenerator arrivalScheduler;
 	private UniformDistGenerator serviceScheduler;
 	private ExpDistGenerator breakDownScheduler;
 	private UniformDistGenerator repairScheduler;
 	private Factory factory;
 	private int itemsCount;
 	private Logger logger;
 	
 	private final double ARRIVAL_RATE = 1;
 	private final double MIN_SERVCICE_TIME = 0.65;
 	private final double MAX_SERVICE_TIME = 0.70;
 	private final double BREAKDOWN_RATE = 1.0 / (6 * 60);
 	private final double MIN_REPAIR_TIME = 8;
 	private final double MAX_REPAIR_TIME = 12;
 	
 	public MachiningCenter(Factory factory, Logger logger){
 		super();
 		this.factory = factory;
 		this.logger = logger;
 		itemsCount = 0;
 		brokenDown = false;
 		currentServiceCompletionTime = -1;
 		repairTime = 0;
 		arrivalScheduler = new ExpDistGenerator(ARRIVAL_RATE);
 		serviceScheduler = new UniformDistGenerator(MIN_SERVCICE_TIME, MAX_SERVICE_TIME);
 		breakDownScheduler = new ExpDistGenerator(BREAKDOWN_RATE);
 		repairScheduler = new UniformDistGenerator(MIN_REPAIR_TIME, MAX_REPAIR_TIME);
 	}
 	
 	public boolean isBrokenDown(){
 		return this.brokenDown;
 	}
 	
 	public void setBrokenDown(boolean brokenDown){
 		this.brokenDown = brokenDown;
 	}
 	
 	public void scheduleArrivalEvent(double clk){
 		double dt = arrivalScheduler.next();
 		double t = clk + dt;
 		Event evt = new Event(t, EventType.mcArrival);
 		factory.addEvent(evt);
 	}
 	
 	public void arrivalEvent(double clk){
 		Item item = new Item(itemsCount++, clk);
 		arrivalEvent(clk, item);
 	}
 
 	public void arrivalEvent(double clk, Item item){
 		if(this.isIdle())
 			scheduleServiceCompletionEvent(clk, item);
 		
 		this.addItem(item);
 		scheduleArrivalEvent(clk);
 		
 		logger.info("#mcArrival [" + clk + "] " + item.getId());
 		logger.info("#MachiningCenterQueueLength [" + clk + "] " + this.getQueueLength());
 	}
 	
 	public void scheduleServiceCompletionEvent(double clk, Item item){
 		currentServiceStartTime = clk;
 		double dt = serviceScheduler.next();
 		double t = 0;
 		if(brokenDown)
 			t = repairTime + dt;
 		else 
 			t = clk + dt;
 		
 		currentServiceCompletionTime = t;
 		ItemEvent evt = new ItemEvent(item, t, EventType.mcServiceCompletion);
 		factory.addEvent(evt);		
 	}
 	
 	public void serviceCompletionEvent(double clk){
		if(brokenDown)
 			return;
 		
 		Item finishedItem = this.nextItem();
 		double serviceTime = clk - currentServiceStartTime;
 		
 		if(!this.isIdle())
 			scheduleServiceCompletionEvent(clk, this.peekItem());
 		
 		logger.info("#mcServiceCompletion [" + clk + "] " + finishedItem.getId() + " serviceTime=" + serviceTime);
 		logger.info("#MachiningCenterQueueLength [" + clk + "] " + this.getQueueLength());
 		
 		factory.getInspectionStation().arrivalEvent(clk, finishedItem);
 	}
 	
 	public void scheduleRepairEvent(double clk){
 		double dt = repairScheduler.next();
 		double t = clk + dt;
 		repairTime = t;
 		Event evt = new Event(t, EventType.mcRepair);
 		factory.addEvent(evt);
 	}
 	
 	public void breakDown(double clk){
 		brokenDown = true;
 		logger.info("#mcBreakDown [" + clk + "]");
 		scheduleRepairEvent(clk);
 
 		if(!this.isIdle()){
 			// delay the current processed item
 			Item crntItem = this.peekItem();
 			double t = repairTime + (currentServiceCompletionTime - clk);
 			ItemEvent evt = new ItemEvent(crntItem, t, EventType.mcServiceCompletion);
 			factory.addEvent(evt);
 		}
 	}
 	
 	public void scheduleBreakDownEvent(double clk){
 		double dt = breakDownScheduler.next();
 		double t = clk + dt;
 		Event evt = new Event(t, EventType.mcBreakDown);
 		factory.addEvent(evt);		
 	}
 	
 	public void repairEvent(double clk){
 		brokenDown = false;
 		logger.info("#mcRepair [" + clk + "]");
 		scheduleBreakDownEvent(clk);
 	}
 }
