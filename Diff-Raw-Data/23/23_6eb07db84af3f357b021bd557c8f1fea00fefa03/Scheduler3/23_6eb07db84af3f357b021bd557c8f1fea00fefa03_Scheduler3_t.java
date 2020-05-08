 package at.ac.tuwien.ldsc.group1.application;
 
 import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
 import at.ac.tuwien.ldsc.group1.domain.CloudStateInfo;
 import at.ac.tuwien.ldsc.group1.domain.Event;
 import at.ac.tuwien.ldsc.group1.domain.EventType;
 import at.ac.tuwien.ldsc.group1.domain.components.Application;
 import at.ac.tuwien.ldsc.group1.domain.components.Component;
 import at.ac.tuwien.ldsc.group1.domain.components.Machine;
 import at.ac.tuwien.ldsc.group1.domain.components.PhysicalMachine;
 import at.ac.tuwien.ldsc.group1.domain.components.PhysicalMachineImpl;
 import at.ac.tuwien.ldsc.group1.domain.components.VirtualMachine;
 import at.ac.tuwien.ldsc.group1.domain.components.VirtualMachineImpl;
 import at.ac.tuwien.ldsc.group1.domain.exceptions.ResourceUnavailableException;
 import at.ac.tuwien.ldsc.group1.domain.exceptions.SchedulingNotPossibleException;
 import at.ac.tuwien.ldsc.group1.domain.federation.FederationPartner;
 import com.google.common.collect.TreeMultiset;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class Scheduler3 implements Scheduler {
 
 	private final double ramThreshold = 1.15;
 	private final double cpuThreshold = 1.15;
 	private final double overprovidingThreshold = 15;
 	private final double MAGICPROPORTION = 4400/1900;
 	private final int DEFAULTPMMAXCPU = 2400;
 	private final int DEFAULTPMMAXRAM = 4700;
     private int maxPMs;
     private int currentPms = 0;
     private long internalTime = 0L;
     long lastInternalTime = -1L;
     double lastTotalConsumption = 0;
     
 
     //Use maps to map VM --> PM and App --> VM
     private Map<VirtualMachine, PhysicalMachine> pmAllocations;
     List<Application> runningApps = new ArrayList<>();
     private Map<Application, VirtualMachine> appAllocations = new Hashtable<>();
     private Queue<Application> queuedApplications = new LinkedList<>();
     
     private List<FederationPartner> partnerList;
     
     private boolean eventHandled = false;
     private boolean overProvide = false;
     private double penalty = 0.0;
 
     @Autowired
     @Qualifier("scenarioWriter2")
     CsvWriter scenarioWriter;
 
     private final CloudOverallInfo overallInfo = new CloudOverallInfo();
    private TreeMultiset<Event> events;
     
     public Scheduler3(int maxPMs) {
         this.maxPMs = maxPMs;
        
     }
 
     @Override
     public void schedule(Event event) {
         Application application = event.getApplication();
         if (event.getEventType() == EventType.START) {
             try {
                 //Handle logging
                 if(event.getEventTime() != internalTime && eventHandled)
                     this.writeLog();
                 //Add Application
                 eventHandled = false;
                 this.addApplication(application);
                 application.start();
                 updateEventTime(event);
                 eventHandled = true;
                 //Add stop event
                 events.add(new Event(event.getEventTime() + application.getDuration(), EventType.STOP, application));
                 
                 
 
             } catch (ResourceUnavailableException e) {
                 e.printErrorMsg();
             } catch (SchedulingNotPossibleException e) {
                 System.out.println("[" + internalTime + "/" + event.getEventTime() + "] Application delayed...");
                 queuedApplications.add(application);
             }
         } else {
             if(event.getEventTime() != internalTime && eventHandled)
                 this.writeLog();
             this.removeApplication(application);
             application.stop();
             updateEventTime(event);
             eventHandled = true;
             Application nextApplication = queuedApplications.poll();
             if (nextApplication != null) {
                 long startTime = internalTime;
                 events.add(new Event(startTime, EventType.START, nextApplication));
             }
 
         }
     }
 
   
 
 	private void updateEventTime(Event event) {
         lastInternalTime = internalTime;
         internalTime = event.getEventTime();
     }
 
     @Override
     public void handleEvents(TreeSet<Event> events) {
         if(maxPMs <= 0)
             throw new RuntimeException("The cloud does not contain any physical machines");
        TreeMultiset<Event> multiset = TreeMultiset.create(events);
        this.events = multiset;
         System.out.println(" ###########################################################################################################################################");
         System.out.println(" #########################################                           Scheduler 3_New                            ################################");
         System.out.println(" ###########################################################################################################################################");

        for(Event event: events) {
             if(!event.isToBeSkipped()) schedule(event);
             event.setToBeSkipped(true);
         }
         System.out.println("Number of queued applications:" + queuedApplications.size());
         /* TODO: check if queue still contains some applications and schedule them
                   It might be possible that the queue still contains some applications which have not been
                   executed yet. Simply calling another loop at this point, could possibly introduce an endless loop.
                   We need to also consider the case that there are applications which are too large to run on any
                   physical machine (even if its empty).*/
     }
 
     @Override
     public PhysicalMachine addApplication(Application application) throws ResourceUnavailableException, SchedulingNotPossibleException {
         //1. Find a physical machine which can host this application
         int neededRam = application.getRam();
         int neededHddSize = application.getHddSize();
         int neededCpuInMHz = application.getCpuInMhz();
         PhysicalMachine pm = selectOptimalPM(neededRam, neededHddSize, neededCpuInMHz);
         
 //        System.out.println("PM selected: " + pm.toString());
         
         //2 get VM and resize it, every PM will have max 1 VM
         VirtualMachine vm = (VirtualMachine) pm.getComponents().get(0);
         
        
         	if(pm.getOverprovidionPercentage() != 0){
         		//there was overproviding before on this machine
         		System.out.println("overproviding workflow");
         		double overProvidingPercentageBefore = pm.getOverprovidionPercentage();
         		
         		
         		vm.addOverProvidedComponent(application); //resources are allocated inside this method
         		vm.start();
         		appAllocations.put(application, vm);
         		adjustEventTimes(pm,overProvidingPercentageBefore, pm.getOverprovidionPercentage());
 //        		System.out.println("PM after App added2: " + pm.toString());
         		return pm;
         		
         	}else{
         		System.out.println("normal workflow");
                 
                 
                 //Try to allocate resources and start the VM
                 try {
                     vm.addComponent(application); //resources are allocated inside this method
                     
                 } catch (ResourceUnavailableException e) {
                     e.printResourceAllocationErrorLog(pm, vm, neededCpuInMHz, neededHddSize, neededRam);
                 }
                 if(pm.getOverprovidionPercentage()  != 0)  adjustEventTimes(pm,0, pm.getOverprovidionPercentage());
                 
                 //if everything worked, we add the (app, vm) tuple to the map of applications
                 appAllocations.put(application, vm);
                 runningApps.add(application);
                 
                 return pm;
         		
         	}
        
         
     }
 
 	
 
 	@Override
     public void removeApplication(Application application) {
         //1. find the virtual machine on which this application runs
         //   and remove it.
 		if(application.getId() == 28){
 			System.out.println("wtf");
 		}
 		
         VirtualMachine currentVm = appAllocations.remove(application);
         runningApps.remove(application);
         if (currentVm != null) {
         	//get pm
         	PhysicalMachine pm = pmAllocations.get(currentVm);
         	double overProvidingPercentageBefore = pm.getOverprovidionPercentage();
             currentVm.removeComponent(application);     // free resources inside this method
             System.out.println("removing application"+application.getId());
             this.adjustEventTimes(pm, overProvidingPercentageBefore, pm.getOverprovidionPercentage());
             //2. Kill VM if not needed anymore (we just removed the last app from it)
             if (currentVm.getComponents() == null || currentVm.getComponents().isEmpty()) {
                 currentVm.stop(); //this also removes this VM from its parent
                 // if there are no applications running on this VM then it implies that appAllocations does not
                 // contain the currentVM
                 assert (!appAllocations.containsValue(currentVm));
 
                 //3. Kill PM if not needed anymore (we just removed the last VM from it)
                 PhysicalMachine currentPm = pmAllocations.remove(currentVm);
                 if (currentPm != null && (currentPm.getComponents() == null || currentPm.getComponents().isEmpty())) {
                     currentPm.stop();
                     currentPms--;
                     pmAllocations.remove(currentPm);
                    
                 }
             }
         } else {
         	
             System.out.println("How come app is running on no virtual machine?" + application.getId());
             throw new RuntimeException("error");
             
             
             
         }
     }
 
     private PhysicalMachine selectOptimalPM(Integer neededRam, Integer neededHddSize, Integer neededCpuInMHz) throws SchedulingNotPossibleException {
     	
         if (this.pmAllocations == null) {
             this.pmAllocations = new Hashtable<>();
             PhysicalMachine pm = createNewPM();
             pm.start();
             VirtualMachine vm = null; 
             try {
 				vm = new VirtualMachineImpl(pm);
 			} catch (ResourceUnavailableException e) {
 				e.printStackTrace();
 				System.err.println("Something went terrible wrong: not enogh space on a new PM to start VM on it.");
 			}
             pmAllocations.put(vm, pm);
             vm.start();
             overallInfo.setTotalVMs(overallInfo.getTotalVMs() + 1);
             overallInfo.setTotalPMs(overallInfo.getTotalPMs() + 1);
             return pm;
         } else {
       
 
         	PhysicalMachine selectedPm = selectMinimalOverprovidedPm(neededRam, neededHddSize, neededCpuInMHz);
         	if(selectedPm != null){
         		return selectedPm;
         	}
         	
             //list iterated and no pm could give back -> start new pm
             PhysicalMachine pm = createNewPM();
             pm.start();
             VirtualMachine vm = null; 
             try {
 				vm = new VirtualMachineImpl(pm);
 			} catch (ResourceUnavailableException e) {
 				e.printStackTrace();
 				System.err.println("Something went terrible wrong: not enogh space on a new PM to start VM on it.");
 			}
             pmAllocations.put(vm, pm);
             vm.start();
             overallInfo.setTotalVMs(overallInfo.getTotalVMs() + 1);
             overallInfo.setTotalPMs(overallInfo.getTotalPMs() + 1);
             return pm;
         }
     }
     
 
 	//iterate over PMList give back first possible
     private PhysicalMachine selectMinimalOverprovidedPm(int neededRam,int neededHddSize,int neededCpuInMHz){
     	
     	//order pms by Overprovidingpercentage
     	ArrayList<PhysicalMachine> machines = new ArrayList(this.pmAllocations.values());
     	
     	Collections.sort(machines, new Comparator<PhysicalMachine>() {
     	    public int compare(PhysicalMachine s1, PhysicalMachine s2) {
     	    	if(s1.getOverprovidionPercentage() < s2.getOverprovidionPercentage()){
     	    		return -1;
     	    	}
     	    	else if(s1.getOverprovidionPercentage() > s2.getOverprovidionPercentage()){
     	    		return 1;
     	    	}else{
     	    		return 0;
     	    	}
     	    }
     	});
     	
 //    	System.out.println("----------------Sorting--------------");
 //    	for(PhysicalMachine a : machines){
 //    		System.out.println(a.toString());
 //    	}
 //    	System.out.println("------------------------------");
     	
     	//select first that meets requirements
     	
     	
     	 for (PhysicalMachine pm : machines) {
              if (pm.getCpuAvailable() >= neededCpuInMHz &&
                      pm.getRamAvailable() >= neededRam &&
                      pm.getHddAvailable() >= neededHddSize ) {
                  return pm;
              }
          }
     	//if there is non return null
     	 return null;
     }
 
 
     //Scheduler2: create new PM inclusive VM
     private PhysicalMachine createNewPM() throws SchedulingNotPossibleException {
         if (this.currentPms < maxPMs) {
             this.currentPms++;
             PhysicalMachine pm = new PhysicalMachineImpl();
             pm.setOverprovidedRam((int) (pm.getRamMax() * ramThreshold));
             pm.setOverprovidedCpuInMhz((int) (pm.getCpuInMhzMax() * cpuThreshold));
             
             return pm;
         } else {
             throw new SchedulingNotPossibleException();
         }
     }
 
     private void writeLog() {
         int timestamp;
         int totalRAM = 0;
         int totalCPU = 0;
         int totalSize = 0;
         int runningVMs = 0;
         double totalPowerConsumption = 0;
         int inSourced = 0;        //TODO
         int outSourced = 0;        //TODO
 
         timestamp = (int) internalTime;
         //Note that the pmAllocations map can contain each PM several times, thus we need to create a set from it first
         Set<PhysicalMachine> pms = new HashSet<>(pmAllocations.values());
         for (Machine pm : pms) {
             totalRAM += pm.getRam();
             totalCPU += pm.getCpuInMhz();
             totalSize += pm.getHddSize();
             runningVMs += pm.getComponents().size();
             //this consumption is the overall powerConsumption of the cloud in the moment
             totalPowerConsumption += pm.getPowerConsumption();
         }
 
         CloudStateInfo info = new CloudStateInfo(timestamp, totalRAM, totalCPU, totalSize, currentPms, runningVMs, totalPowerConsumption, inSourced, outSourced);
         this.updatePowerConsumption(lastTotalConsumption);
         lastTotalConsumption = totalPowerConsumption;
         this.scenarioWriter.writeLine(info);
     }
 
 
     private void updatePowerConsumption(double lastTotalConsumption) {
         //total consumption after the previous event * time interval between last and new event in seconds
         this.overallInfo.setTotalPowerConsumption(lastTotalConsumption * (lastInternalTime / 1000));
     }
 
     @Override
     public void finalize() {
         this.scenarioWriter.close();
     }
 
     @Override
     public CloudOverallInfo getOverAllInfo() {
         overallInfo.setScheduler(this.getClass().getSimpleName());
         overallInfo.setTotalDuration(internalTime);
         return this.overallInfo;
     }
 
     @Override
     public void setMaxNumberOfPhysicalMachines(int nr) {
         this.maxPMs = nr;
     }
     
          
     
     private void adjustEventTimes(PhysicalMachine pm ,double overProvidingPercentageBefore,	double overprovidionPercentageAfter) {
     	
     	if(overProvidingPercentageBefore == overprovidionPercentageAfter) return;
     	
     	System.out.println("BeforePercentage:" + overProvidingPercentageBefore );
     	System.out.println("AfterPercentage:" + overprovidionPercentageAfter );
     	
     	double penalty = 100+(overprovidionPercentageAfter-overProvidingPercentageBefore);
     	
     	//search for stop events of apps that are running on this pm
     	//apps running on this pm -> runningApplications
     	VirtualMachine vm = (VirtualMachine) pm.getComponents().get(0);
     	List<Application> runningApplications = new ArrayList<Application>();
     	for(Component c: vm.getComponents()){
     		Application app = (Application) c;
     		runningApplications.add(app);
     	}
 
     	//we search for stop events of apps that are running on this pm
     	for(Event e : events){
     		if(e.getEventType().equals(EventType.STOP)){
     			for(Application a : runningApplications){
     				if(e.getApplication().equals(a)){
     					//found event, increase time by penality
 //    					System.out.println("old event timestamp(2): "+e.getEventTime());
     					e.setEventTime((long) (e.getEventTime()*(penalty/100)));
 //    					System.out.println("new event timestamp(2): "+e.getEventTime());
     				}
     			}
     		}
 
     	}
     	
     	
     	
     	//debug output
 //    	System.out.println("################   Events   ##################");
 //    	for(Event e : events){
 //    		System.out.println(e.toString());
 //    	}
 //    	System.out.println("##############################################");
 		
 	}
 
     @Override
 	public void setNumberOfFederationPartners(int nr) {
 		//todo init federationPartners
 		partnerList = new ArrayList<FederationPartner>();
 		for(int i = 0 ; i < nr; i++){
 			FederationPartner partner = new FederationPartner();
 			partnerList.add(partner);
 		}
 		
 	}
 	
 }
