 package at.ac.tuwien.lsdc.actions;
 
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import weka.classifiers.functions.MultilayerPerceptron;
 import weka.core.Instance;
 import weka.core.Instances;
 import at.ac.tuwien.lsdc.Configuration;
 import at.ac.tuwien.lsdc.generator.RequestGenerator;
 import at.ac.tuwien.lsdc.mape.Monitor;
 import at.ac.tuwien.lsdc.resources.App;
 import at.ac.tuwien.lsdc.resources.PhysicalMachine;
 import at.ac.tuwien.lsdc.resources.Resource;
 import at.ac.tuwien.lsdc.resources.VirtualMachine;
 
 public class CreateVmInsertApp extends Action {
 	private static Instances knowledgeBase = null;
 	private App app;
 	private static int vmStartupCosts = 10;
 	private static int pmStartupCosts = 20;
 	
 	private PhysicalMachine selectedPm = null;
 	private boolean preconditionsOk = false;
 	private int costs = 0;
 	private int waitForEvaluation = 10;
 	
 	public static Instances getKnowledgeBase() {
 		if (knowledgeBase ==null ) {
 			try {
 				CreateVmInsertApp.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBCreateVmInsertApp());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return knowledgeBase;
 	}
 	
 	public void terminate() {
 		try {
 			Action.saveKnowledge(Configuration.getInstance().getKBCreateVmInsertApp(), CreateVmInsertApp.getKnowledgeBase());
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public int predict() {
 		// WEKA inputs: PM resource allocations, App SLAs
 		Instance input = new Instance(0);
 		
 		Instances data = CreateVmInsertApp.getKnowledgeBase();
 		
 		MultilayerPerceptron mp = new MultilayerPerceptron();
 		try {
 			mp.buildClassifier(data);
 			mp.classifyInstance(input);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// TODO: use WEKA to classify
 		
 		return 100;
 	}
 
 	@Override
 	public int estimate() {
 		return costs;
 	}
 
 	@Override
 	public boolean preconditions() {
 		return preconditionsOk;
 	}
 	
 	@Override
 	public boolean evaluate() {
 		if (waitForEvaluation>0) {
 			waitForEvaluation--;
 			return false;
 		}
 		else {
 			LinkedList<Integer> cpuallhist = selectedPm.getCpuAllocationHistory(20);
 			LinkedList<Integer> cpuusagehist = selectedPm.getCpuUsageHistory(20);	
 			int beforeInsertionCount = cpuallhist.size()-10;	
 			
 			double cpuratio = calculateAllocationUsageRatio(cpuallhist, cpuusagehist, beforeInsertionCount);
 			
 			LinkedList<Integer> memallhist = selectedPm.getMemoryAllocationHistory(20);
 			LinkedList<Integer> memusagehist = selectedPm.getMemoryUsageHistory(20);	
 				
 			 
 			double memoryratio = calculateAllocationUsageRatio(memallhist, memusagehist, beforeInsertionCount);
 				
 			LinkedList<Integer> storageallhist = selectedPm.getStorageAllocationHistory(20);
 			LinkedList<Integer> storageusagehist = selectedPm.getStorageUsageHistory(20);	
 				
 			double storageratio = calculateAllocationUsageRatio(storageallhist, storageusagehist, beforeInsertionCount);
 			
 			//Create new WEKA - instance
 			Instance inst = new Instance(34);
 			
 			//CPU/Memory/Storage - Allocation history before the new vm was created
 			int valuesStartAt = 10-beforeInsertionCount; //if machine doesn't have 10 values before insertion
 			for (int i = 0; i<10;i++) {
 				if(i>=valuesStartAt){
 					inst.setValue(getKnowledgeBase().attribute(i), clusterValue(cpuallhist.get(i)));
 					inst.setValue(getKnowledgeBase().attribute(i+10),clusterValue( memallhist.get(i)));
 					inst.setValue(getKnowledgeBase().attribute(i+20), clusterValue(storageallhist.get(i)));
 				}
 				else {
 					//TODO: replace NULL VALUE
 					inst.setValue(getKnowledgeBase().attribute(i), Instance.missingValue());
 					inst.setValue(getKnowledgeBase().attribute(i+10), Instance.missingValue());
 					inst.setValue(getKnowledgeBase().attribute(i+20), Instance.missingValue());
 				}
 			}
 			
 			//SLAs
 			//CPU
 			inst.setValue(getKnowledgeBase().attribute(30), clusterValue(app.getCpu()));
 			//Memory
 			inst.setValue(getKnowledgeBase().attribute(31),clusterValue( app.getMemory()));
 			//Storage
 			inst.setValue(getKnowledgeBase().attribute(32), clusterValue(app.getStorage()));
 			
 			//Evaluation
 			inst.setValue(getKnowledgeBase().attribute(33), cpuratio+memoryratio+storageratio);
 
 			getKnowledgeBase().add(inst);
 			
 			
 			
 		}
 		return true;
 	}
 	
 	private int clusterValue (int value) {
 		return (int)(Math.ceil(value/10)*10);
 	}
 	
 	private double calculateAllocationUsageRatio(LinkedList<Integer> allocation, LinkedList<Integer> usage, int beforeInsertionCount) {
 
 		System.out.println (allocation.size() + ", usage " + usage.size());
 		
 		double ratioBefore =0;
 		double ratioAfter = 0;
 		for(int i=0; i<usage.size();i++){ 
 			if(i<beforeInsertionCount) {
				ratioBefore += usage.get(i)/Math.max(allocation.get(i), 1);
 			}
 			else {
				ratioAfter += usage.get(i)/Math.max(allocation.get(i), 1);
 			}
 		}
 		return (ratioAfter/10) - (ratioBefore/Math.max(1, beforeInsertionCount));
 	}
 
 	
 	
 	//calculate if an App fits to a pm
 	//TODO: gst: use WEKA to calc fit factor!!
 	private int calculateFit(App app2, PhysicalMachine pm) {
 		int cpuFit = pm.getCurrentCpuAllocation()-app2.getCpu();
 		int memFit = pm.getCurrentMemoryAllocation()-app2.getMemory();
 		//int storageFit = pm.getCurrentStorageAllocation()-app2.getStorage();
 		
 		return (100-cpuFit)+(100-memFit);
 	}
 
 	@Override
 	public void execute() {
 		VirtualMachine oldVm = null;
 		PhysicalMachine oldPm = null;
 		
 		//if the App existed before
 		if (app.getVm()!=null) {
 			oldVm = app.getVm();
 			oldPm = oldVm.getPm();
 		}
 		
 		//remove the app from the request- queue??? 
 		if(app.getOriginalRequest()!=null) {
 			RequestGenerator.getInstance().removeRequestFromQueue(app.getOriginalRequest());
 		}
 		
 		if (selectedPm.isRunning()==false) {
 			selectedPm.startMachine();
 		}
 		
 		//TODO: gst: hard - coded startup value!!
 		VirtualMachine vm = selectedPm.createNewVm(app.getCpu(), app.getMemory(), app.getStorage(), 10);
 		vm.createApp(this.app);
 		
 		if(oldVm!=null) {
 			oldVm.getApps().remove(app);
 			//TODO: gst: what happens if the VM is now empty??
 			if(oldVm.getApps().size()==0) {
 				oldVm.terminate();
 			}
 		}
 	}
 
 	@Override
 	public void init(Resource problemApp) {
 		this.preconditionsOk=false;
 		this.selectedPm=null;
 		this.costs=0;
 		this.app=null;
 		
 		CreateVmInsertApp.vmStartupCosts = Configuration.getInstance().getVmStartupCosts();
 		CreateVmInsertApp.pmStartupCosts = Configuration.getInstance().getPmStartupCosts();
 		
 		if (problemApp instanceof App){
 			app = (App)problemApp;
 			boolean found = false;
 			int fitFactor = 0;
 			
 			for (PhysicalMachine pm : Monitor.getInstance().getPms()) {
 				if(pm.isRunning())  {
 					if((100-pm.getCurrentCpuAllocation())>=app.getCpu() && (100-pm.getCurrentMemoryAllocation())>=app.getMemory() && (100-pm.getCurrentCpuAllocation())>app.getStorage()) {
 						found=true;
 						costs = vmStartupCosts;
 						
 						if(selectedPm == null || calculateFit(app, pm)>fitFactor) {
 							selectedPm = pm;
 						}
 					}
 				}
 			}
 		
 		
 			//no running machine found => search for a stopped machine
 			//TODO: gst: replace by real action
 			if (found ==false ) {
 				//Start a new machine
 			
 				for(PhysicalMachine pm : Monitor.getInstance().getPms()){
 					if (pm.isRunning()==false) {
 						found=true;
 						costs += pmStartupCosts;
 						selectedPm = pm;
 						break;
 					}
 					
 				}
 			}
 			preconditionsOk = found;
 		}
 		else {
 			preconditionsOk= false;
 		}
 	}
 
 }
