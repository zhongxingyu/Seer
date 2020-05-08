 package de.hpi_web.cloudSim.profiling.utilization;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Queue;
 
 import org.cloudbus.cloudsim.Cloudlet;
 import org.cloudbus.cloudsim.Datacenter;
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.UtilizationModel;
 import org.cloudbus.cloudsim.UtilizationModelFull;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 import org.cloudbus.cloudsim.core.CloudSimTags;
 import org.cloudbus.cloudsim.core.SimEntity;
 import org.cloudbus.cloudsim.core.SimEvent;
 
 import de.hpi_web.cloudSim.multitier.staticTier.VmFactory;
 
 import arx.ARX;
 
 public class UtilManager extends SimEntity {
 	
 	public static final int RUN = 7000;
 	public static final int CLOUDLET_UPDATE = 7001;
 	public static final int ROUND_COMPLETED = 7002;
 	public static final int UTIL_SIM_FINISHED = 7003;
 	
 	private int i = 0;
 	//private int brokerId;
 	private int upperThreshold;
 	private int lowerThreshold;
 	private Map<DatacenterBroker,List<Double>> layers;
 
 
 	//TODO first test with fixed values
     
     private int delay; //seconds
     
 
 	public UtilManager(String name, int delay, int upperThreshold, int lowerThreshold, HashMap<DatacenterBroker, List<Double>> layers) {
 		super(name);
 		
 		this.layers = layers;
 		
 		this.delay = delay;
 		this.upperThreshold = upperThreshold;
 		this.lowerThreshold = lowerThreshold;
 		
 //		//TODO only for testing has to be exchanged soon
 //		List<Double> cpuUtils = new ArrayList<Double>();
 //		cpuUtils.add(0.9);
 //		cpuUtils.add(0.8);
 //		cpuUtils.add(5.0);
 //		
 //		
 //		for (DatacenterBroker tier : layers) {
 //			this.layers.put(tier, cpuUtils);
 //		}
 	}
 	
 
 
 	@Override
 	public void startEntity() {
 		//sendNow("utilManager", CloudSimTags.REGISTER_RESOURCE, getId());
 		schedule(getId(), 0, RUN);
 		
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
 //			case UtilManager.CLOUDLET_CREATION:
 //				processCloudletCreation(ev);
 //				break;
 		}
 				
 	}
 
 	private void processCompleted(SimEvent ev) {
 		
 		try {
 			Thread.sleep(delay*1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Completed Round ");
 		
 	}
 
 	private void processRun(SimEvent ev) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": UtilManager is running... ");
 		
 		//for each tier
 		for(DatacenterBroker tier : layers.keySet()) {
 			List<Double> cpuUtils = layers.get(tier);
 			
 			
 			// check if enough vms are present / too much vms present and handle this event => regarding threshold
 			int runningVms = tier.getCloudletSubmittedList().size();
 			double utilizationPerVm = (cpuUtils.get(i)/(double)runningVms);
 			if (utilizationPerVm > this.upperThreshold) {
 				// create new vm
				Vm v = VmFactory.createVm(tier.getId(), i);
				schedule(tier.getId(), 1, CloudSimTags.VM_CREATE, v);
 			}
 			else if (utilizationPerVm < this.lowerThreshold && runningVms > 1) {
 				// destroy vm
 				Log.printLine("Too many Vms... destroying");
				//schedule(tier.getId(), 1, CloudSimTags.VM_DESTROY);
 			}
 			//TODO calc new util
 			schedule(tier.getId(),1, UtilManager.CLOUDLET_UPDATE, cpuUtils.get(i));
 			
 			this.i++;
 			
 			if(i < cpuUtils.size()) {
 				schedule(getId(), 2, RUN);
 			} else {
 				schedule(tier.getId(), 2, UTIL_SIM_FINISHED);
 			}
 
 		}
 		
 
 	}
 	
 
 
 	@Override
 	public void shutdownEntity() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 //	public int getBrokerId() {
 //		return brokerId;
 //	}
 //
 //	public void setBrokerId(int brokerId) {
 //		this.brokerId = brokerId;
 //	}
 
 }
