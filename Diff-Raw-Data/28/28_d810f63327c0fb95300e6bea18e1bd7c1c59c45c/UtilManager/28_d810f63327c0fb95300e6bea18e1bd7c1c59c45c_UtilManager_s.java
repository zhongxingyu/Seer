 package de.hpi_web.cloudSim.profiling.utilization;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 import org.cloudbus.cloudsim.core.CloudSimTags;
 import org.cloudbus.cloudsim.core.SimEntity;
 import org.cloudbus.cloudsim.core.SimEvent;
 
 import de.hpi_web.cloudSim.arx.NewArx;
 import de.hpi_web.cloudSim.multitier.factories.VmFactory;
 import de.hpi_web.cloudSim.profiling.builders.VmBuilder;
 import de.hpi_web.cloudSim.profiling.datacenter.ProfilingVm;
 /*
  * The UtilManager triggers the updating process of the cloudlets and checks whether thresholds are exceeded 
  */
 public class UtilManager extends SimEntity {
 	
 	public static final int RUN = 7000;
 	public static final int CLOUDLET_UPDATE = 7001;
 	public static final int ROUND_COMPLETED = 7002;
 	public static final int UTIL_SIM_FINISHED = 7003;
 	
 	private int i = 0;
 	
 	private UtilizationThreshold cpuThreshold;
 	private UtilizationThreshold memThreshold;
 	private UtilizationThreshold hdThreshold;
 	private UtilizationThreshold bwThreshold;
 	private VmBuilder vmBuilder;
 	
 	private Map<DatacenterBroker,List<List<Double>>> layers;
     
     private double delay; //seconds
     private List<Integer> brokers;
     private List<Integer> finishedTurnBrokers;
     
 
 	public UtilManager(String name, double delay,
 			UtilizationThreshold cpuThreshold,
 			UtilizationThreshold memThreshold,
 			UtilizationThreshold hdThreshold,
 			UtilizationThreshold bwThreshold,
 			HashMap<DatacenterBroker, List<List<Double>>> layers,
 			VmBuilder vmBuilder) {
 		super(name);
 		
 		this.layers = layers;
 		this.brokers = new ArrayList<Integer>();
 		this.finishedTurnBrokers = new ArrayList<Integer>();
 		
 		for (DatacenterBroker d : layers.keySet())
 			brokers.add(d.getId());
 		
 		this.delay = delay;
 		this.cpuThreshold = cpuThreshold;
 		this.memThreshold = memThreshold;
 		this.hdThreshold = hdThreshold;
 		this.bwThreshold = bwThreshold;
 		
 		this.vmBuilder = vmBuilder;
 		
 		
 		for (DatacenterBroker tier : layers.keySet()) {
 			Log.printLine("Broker values: "+ layers.get(tier).get(0).size()); 
 		}
 
 	}
 	
 
 
 	@Override
 	public void startEntity() {
 		//sendNow("utilManager", CloudSimTags.REGISTER_RESOURCE, getId());
 		//start with a short delay, to avoid interference
 		schedule(getId(), 10, RUN);
 		
 	}
 
 	@Override
 	public void processEvent(SimEvent ev) {
 
 		switch (ev.getTag()) {
 			case UtilManager.RUN:
 				processRun(ev);
 				break;
 			case UtilManager.ROUND_COMPLETED:
 				processCompleted(ev);
 				break;
 		}
 				
 	}
 
 	private void processCompleted(SimEvent ev) {
 		int brokerId = (int) ev.getData();
 		if (brokers.contains(brokerId) && !finishedTurnBrokers.contains(brokerId)) {
 			finishedTurnBrokers.add(brokerId);
 			if (finishedTurnBrokers.containsAll(brokers)) {
 				finishedTurnBrokers.clear();
 				
 				try {
 					Thread.sleep(new Double(delay*1000).intValue());
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				Log.printLine(CloudSim.clock() + ": " + getName() + ": Completed Round ");
 				if(i < NewArx.RUNNING_VALUES) {
 					schedule(getId(), 3, RUN);
 				} else {
 					//TODO check if it makes a difference if we end each layer here instead of all
 					for (DatacenterBroker tier : layers.keySet() )
 						schedule(tier.getId(), 3, UTIL_SIM_FINISHED);
 				}
 			}
 		}
 	}
 
 	private void processRun(SimEvent ev) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": UtilManager is running... ");
 		
 		//for each tier
 		for (DatacenterBroker tier : layers.keySet()) {
 			//1 = cpu
 			//2 = memory
 			//3 = disk read
 			//4 = disk write
 			//5 = network in
 			//6 = network out
 			List<Double> cpuUtils = layers.get(tier).get(0);
 			List<Double> memUtils = convertToRelative(layers.get(tier).get(1), vmBuilder.getRam(), 1024);
 			List<Double> diskReadUtils = convertToRelative(layers.get(tier).get(2), vmBuilder.getDiskAccessRate(), 1);
 			List<Double> diskWriteUtils = convertToRelative(layers.get(tier).get(3), vmBuilder.getDiskAccessRate(), 1);
			List<Double> bwInUtils = convertToRelative(layers.get(tier).get(4), vmBuilder.getBandwidth(), 1);
			List<Double> bwOutUtils = convertToRelative(layers.get(tier).get(5), vmBuilder.getBandwidth(), 1);
 			//schedule(tier.getId(),1, UtilManager.CLOUDLET_UPDATE, cpuUtils.get(i));
 
 			// check if enough vms are present / too much vms present and handle this event => regarding threshold
			int runningVms = tier.getCloudletSubmittedList().size();
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": Vms running: " + runningVms);
 			double cpuUtilizationPerVm = (cpuUtils.get(i)/(double)runningVms);
 			// TODO: need to make those in percent!
 			double memUtilizationPerVm = (memUtils.get(i)/(double)runningVms);
 			double diskReadUtilizationPerVm = (diskReadUtils.get(i)/(double)runningVms);
 			double diskWriteUtilizationPerVm = (diskWriteUtils.get(i)/(double)runningVms);
 			double bwInUtilizationPerVm = (bwInUtils.get(i)/(double)runningVms);
 			double bwOutUtilizationPerVm = (bwOutUtils.get(i)/(double)runningVms);
 			Log.printLine("Current CPU Util: " + cpuUtilizationPerVm);
 			Log.printLine("Current Mem Util: " + memUtilizationPerVm);
 			Log.printLine("Current Disk Read Util: " + diskReadUtilizationPerVm);
 			Log.printLine("Current Disk Write Util: " + diskWriteUtilizationPerVm);
 			Log.printLine("Current BwIn Util: " + bwInUtilizationPerVm);
 			Log.printLine("Current BwOut Util: " + bwOutUtilizationPerVm);
 			Log.printLine("Running Vms: " + runningVms);
			
 			if (checkUpperThreshold(runningVms, cpuUtilizationPerVm,
 					memUtilizationPerVm, diskReadUtilizationPerVm,
 					diskWriteUtilizationPerVm, bwInUtilizationPerVm,
 					bwOutUtilizationPerVm)) {
 				// create new vm
 				Log.printLine("Too few Vms... creating");
 				//Vm v = VmFactory.createVm(tier.getId());
 				ProfilingVm v = vmBuilder.setUserId(tier.getId()).build();
 				schedule(tier.getId(), 1, CloudSimTags.VM_CREATE, v);
 			}
 			else if (checkLowerThreshold(runningVms, cpuUtilizationPerVm,
 					memUtilizationPerVm, diskReadUtilizationPerVm,
 					diskWriteUtilizationPerVm, bwInUtilizationPerVm,
 					bwOutUtilizationPerVm)) {
 				// destroy vm
 				Log.printLine("Too many Vms... destroying");
 				schedule(tier.getId(), 1, CloudSimTags.VM_DESTROY);
 			}
 			//update utilization of all cloudlets
 			UtilWrapper wrapper = new UtilWrapper(cpuUtils.get(i), memUtils.get(i), diskReadUtils.get(i),diskWriteUtils.get(i),bwInUtils.get(i),bwOutUtils.get(i));
 			
 			schedule(tier.getId(), 2, UtilManager.CLOUDLET_UPDATE, wrapper);
 		}
 		this.i++;
 		
 	}
 
 
 
 	private boolean checkLowerThreshold(int runningVms,
 			double cpuUtilizationPerVm, double memUtilizationPerVm,
 			double diskReadUtilizationPerVm, double diskWriteUtilizationPerVm,
 			double bwInUtilizationPerVm, double bwOutUtilizationPerVm) {
 		return runningVms > 1 &&
 					(cpuThreshold.belowThreshold(cpuUtilizationPerVm)) &&
 					(memThreshold.belowThreshold(memUtilizationPerVm)) &&
 					(hdThreshold.belowThreshold(diskWriteUtilizationPerVm+diskReadUtilizationPerVm)) &&
 					(bwThreshold.belowThreshold(bwInUtilizationPerVm+bwOutUtilizationPerVm));
 	}
 
 
 
 	private boolean checkUpperThreshold(int runningVms,
 			double cpuUtilizationPerVm, double memUtilizationPerVm,
 			double diskReadUtilizationPerVm, double diskWriteUtilizationPerVm,
 			double bwInUtilizationPerVm, double bwOutUtilizationPerVm) {
 		return runningVms > 0 &&
 				(cpuThreshold.aboveThreshold(cpuUtilizationPerVm)) ||
 				(memThreshold.aboveThreshold(memUtilizationPerVm)) ||
 				(hdThreshold.aboveThreshold(diskReadUtilizationPerVm+diskWriteUtilizationPerVm)) ||
 				(bwThreshold.aboveThreshold(bwInUtilizationPerVm+bwOutUtilizationPerVm));
 	}
 	
 
 	private List<Double> convertToRelative(List<Double> list, double maximum, int conversionDivisor) {
 		ArrayList<Double> relativeValues = new ArrayList<>();
 		for (Double e : list) {
 			relativeValues.add( (e*100)/((double)conversionDivisor * maximum ));
 		}
 		return relativeValues;
 	}
 
 
 
 	@Override
 	public void shutdownEntity() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
