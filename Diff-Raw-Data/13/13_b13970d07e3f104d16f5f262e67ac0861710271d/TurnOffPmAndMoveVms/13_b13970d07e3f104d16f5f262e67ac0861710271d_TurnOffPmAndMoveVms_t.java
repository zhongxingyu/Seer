 package at.ac.tuwien.lsdc.actions;
 
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.apache.commons.math3.random.RandomData;
 import org.apache.commons.math3.random.RandomDataImpl;
 
 import weka.classifiers.Classifier;
 import weka.classifiers.Evaluation;
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
 
 public class TurnOffPmAndMoveVms extends Action {
 	private static Instances knowledgeBase = null;
 	private static Classifier classifier = null;
 	private static Evaluation evaluation = null;
 	private static RandomData randomData = new RandomDataImpl();
 	
 	
 
 
 	private PhysicalMachine pm;
 	private static int vmStartupCosts = 10;
 	private static int pmStartupCosts = 20;
 	
 	
 	private boolean preconditionsOk = false;
 	private int costs = 0;
 	private int prediction = 0;
 	private int waitForEvaluation = 10;
 	private Instance curInstance;
	private LinkedList<MoveVm> moveactions;
 	
 	
 	public static Instances getKnowledgeBase() {
 		if (knowledgeBase ==null ) {
 			try {
 				//load knowledgebase from file
 				TurnOffPmAndMoveVms.knowledgeBase = Action.loadKnowledge(Configuration.getInstance().getKBCreateAppInsertIntoVm());
 				
 				//prediction is also performed therefore the classifier and the evaluator must be instantiated
 				if(!isOnlyLearning()) {
 					classifier = new MultilayerPerceptron();
 					classifier.buildClassifier(TurnOffPmAndMoveVms.getKnowledgeBase());
 					evaluation = new Evaluation(TurnOffPmAndMoveVms.getKnowledgeBase());
 					evaluation.crossValidateModel(classifier, knowledgeBase, 10, knowledgeBase.getRandomNumberGenerator(randomData.nextLong(1, 1000)));
 					
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return knowledgeBase;
 	}
 	
 	public void terminate() {
 		try {
 			Action.saveKnowledge(Configuration.getInstance().getKBCreateAppInsertIntoVm(), TurnOffPmAndMoveVms.getKnowledgeBase());
 		} catch (IOException e) {
 			
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public int predict() {
 		if (isOnlyLearning()) { //Randomized predictions for learning
 			return randomData.nextInt(0, 100);
 		}
 		else { //Use WEKA - evaluation for prediction
 			//Create new WEKA - instance
 			return prediction;
 		}			
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
 		//TODO: gst how to evaluate this action???
 		
 		
 		/*if (curInstance == null) {
 			curInstance = createInstance(0); // create a Instance with the past values
 		}
 		
 		if (app.getSuspendedTicks()>0 || app.getVm().getSuspendedTicks()>0 || app.getVm().getPm().getSuspendedTicks()>0) {
 			return false;
 		}
 		else if (waitForEvaluation>0) {
 			waitForEvaluation--;
 			return false;
 		}
 		else {
 			System.out.println("APP - Running Ticks: " + app.getRunningTicks());
 			LinkedList<Integer> cpuusagehist = selectedVm.getPm().getCpuUsageHistory(10);	
 			
 			LinkedList<Integer> memusagehist = selectedVm.getPm().getMemoryUsageHistory(10);	
 				
 			LinkedList<Integer> storageusagehist = selectedVm.getPm().getStorageUsageHistory(10);	
 
 			double evaluation =(255- calculateUsageRatio(cpuusagehist, 85) - calculateUsageRatio(memusagehist, 85) - calculateUsageRatio(storageusagehist, 85))/255 ;
 			
 			//subtract SLA Violations
 			evaluation -= (app.getCpuSlaErrorcount()+app.getMemorySlaErrorcount()+app.getStorageSlaErrorcount())/10;
 			
 			//minimum of 0
 			evaluation = Math.max(0, evaluation);
 			
 			curInstance.setValue(getKnowledgeBase().attribute(33), evaluation);
 			getKnowledgeBase().add(curInstance);
 		}
 		return true;*/
 		return true;
 	}
 
 
 	private int clusterValue (int value) {
 		return (int)(Math.ceil(value/10)*10);
 	}
 	
 	private double calculateUsageRatio(LinkedList<Integer> usage, int goal) {
 		double result = 0;
 		for(int i=0; i<usage.size();i++){ 
 			result += Math.abs(85-usage.get(i))/usage.size();
 		}
 		return result;
 	}
 
 	//calculate if an App fits to a pm
 	//TODO: gst: use WEKA to calc fit factor!!
 	private int calculateFit(App app2, VirtualMachine vm) {
 		int output = 0;
 		
 		//is free space available in the VM
 		if (app2.getCpu()+vm.getCurrentCpuUsage() < vm.getCurrentCpuAllocation() && app2.getMemory()+vm.getCurrentMemoryUsage() < vm.getCurrentMemoryAllocation() && app2.getStorage() + vm.getCurrentStorageUsage() < vm.getCurrentCpuAllocation()) {
 		
 			Instance instance = createInstance(Instance.missingValue());
 			instance.setDataset(TurnOffPmAndMoveVms.getKnowledgeBase());
 			
 			
 			try {
 				output = (int) (evaluation.evaluateModelOnce(classifier, instance) *100);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return output;
 	}
 
 	@Override
 	public void execute() {
 		//remove the app from the old vm and insert it into the new vm 
		for (MoveVm move: moveactions) {
 			move.execute();
 		}
 		pm.stopMachine();
 	}
 
 	@Override
 	public void init(Resource problemPm) {
 		this.preconditionsOk=false;
 		this.curInstance = null;
 		this.costs=0;
 		this.pm=null;
 		
 		this.costs = Configuration.getInstance().getAppMovingCosts();
 		
 		if (problemPm instanceof PhysicalMachine){ //only pms can be turned off
 			pm = (PhysicalMachine)problemPm;
 			if (pm.isRunning()){
 				preconditionsOk=true;
 				prediction = 0;
				MoveVm movevmaction;
				moveactions = new LinkedList<MoveVm>();
 				for (VirtualMachine vm : pm.getVms()) { //move existing VMs
					movevmaction = new MoveVm();
 					movevmaction.init(vm);
 					prediction +=(int) (movevmaction.predict() / pm.getVms().size()*100);
 					costs += movevmaction.estimate();
 					moveactions.add(movevmaction);
 				}
 			}
 		}
 	}
 	
 	
 	//create an instance in the format of CreateAppInsertIntoVm.arff
 	//eval can either be a Instance - MissingValue or the evaluation value
 	//TODO: gst What is the right instance data?
 	private Instance createInstance(double eval) {
 		Instance instance = new Instance(64);
 		/*
 		LinkedList<Integer> cpuallhist = pm.getCpuAllocationHistory(10);
 		LinkedList<Integer> cpuusehist = selectedVm.getCpuUsageHistory(10);
 
 		LinkedList<Integer> memallhist = selectedVm.getMemoryAllocationHistory(10);
 		LinkedList<Integer> memusehist = selectedVm.getMemoryUsageHistory(10);
 		
 			
 		LinkedList<Integer> storageallhist = selectedVm.getStorageAllocationHistory(10);
 		LinkedList<Integer> storageusehist = selectedVm.getStorageUsageHistory(10);
 		
 		//CPU/Memory/Storage - Allocation history before the new vm was created
 		for (int i = 0; i<10;i++) {
 				//cpu allocation
 				instance.setValue(getKnowledgeBase().attribute(i), clusterValue(cpuallhist.get(i)));
 				//cpu usage
 				instance.setValue(getKnowledgeBase().attribute(i+10), clusterValue(cpuusehist.get(i)));
 				
 				//memory allocation
 				instance.setValue(getKnowledgeBase().attribute(i+20),clusterValue( memallhist.get(i)));
 				//memory usage
 				instance.setValue(getKnowledgeBase().attribute(i+30),clusterValue( memusehist.get(i)));
 				
 				//storage allocation
 				instance.setValue(getKnowledgeBase().attribute(i+40), clusterValue(storageallhist.get(i)));
 				//storage usage 
 				instance.setValue(getKnowledgeBase().attribute(i+50),clusterValue( storageusehist.get(i)));
 		}
 		
 		//SLAs
 		//CPU
 		instance.setValue(getKnowledgeBase().attribute(60), clusterValue(app.getCpu()));
 		//Memory
 		instance.setValue(getKnowledgeBase().attribute(61),clusterValue(app.getMemory()));
 		//Storage
 		instance.setValue(getKnowledgeBase().attribute(62), clusterValue(app.getStorage()));
 		
 		//Evaluation
 		instance.setValue(getKnowledgeBase().attribute(63), eval);
 */
 		return instance;
 	}
 
 }
