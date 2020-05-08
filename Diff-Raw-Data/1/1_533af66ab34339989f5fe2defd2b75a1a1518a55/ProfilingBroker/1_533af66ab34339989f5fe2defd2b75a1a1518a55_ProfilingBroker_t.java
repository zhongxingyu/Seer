 package de.hpi_web.cloudSim.profiling.datacenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cloudbus.cloudsim.Cloudlet;
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.UtilizationModel;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 import org.cloudbus.cloudsim.core.CloudSimTags;
 import org.cloudbus.cloudsim.core.SimEvent;
 import org.cloudbus.cloudsim.lists.VmList;
 
 import de.hpi_web.cloudSim.profiling.observer.Observable;
 import de.hpi_web.cloudSim.profiling.observer.Observer;
 import de.hpi_web.cloudSim.profiling.utilization.UtilManager;
 import de.hpi_web.cloudSim.profiling.utilization.UtilizationModelFixed;
 
 public class ProfilingBroker extends DatacenterBroker implements Observable{
 	
 	private List<Cloudlet> cloudlets;
 	private List<Observer> observers;
 
 	public ProfilingBroker(String name) throws Exception {
 		super(name);
 		observers = new ArrayList<Observer>();
 		cloudlets = new ArrayList<Cloudlet>();
 		// TODO Auto-generated constructor stub
 	}
 	
 	public void processOtherEvent(SimEvent ev) {
 		switch (ev.getTag()) {
 		// Resource characteristics request
 			case UtilManager.CLOUDLET_UPDATE:
 				processCloudletUpdate(ev);
 				break;
 			case UtilManager.UTIL_SIM_FINISHED:
 				processUtilFinished(ev);
 				break;
 			case CloudSimTags.VM_CREATE:
 				processNewVm(ev);
 				break;
 			case CloudSimTags.VM_DESTROY:
 				processDestroyVm(ev);
 				break;
 		}
 	}
 	
 	private void processDestroyVm(SimEvent ev) {
 		// pick one VM and destroy it. Doesnt matter which one
 		// TODO could pick VM through cloudlet list... getCloudletSubmittedList().get(0).getVmId()
 		Vm vm = getVmsCreatedList().get(0);
 		for (Cloudlet c : getCloudletSubmittedList()) {
 			if (c.getVmId() == vm.getId()) {
 				getCloudletSubmittedList().remove(c);
				cloudletsSubmitted--;
 				break;
 			}
 		}
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
 		sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
 
 		getVmsCreatedList().remove(0);
 	}
 
 	private void processNewVm(SimEvent ev) {
 		Vm vm = (Vm) ev.getData();
 		getVmList().add(vm);
 		// TODO choose datacenter here
 		int datacenterId = getDatacenterIdsList().get(0);
 		
 		String datacenterName = CloudSim.getEntityName(datacenterId);
 		if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
 					+ " in " + datacenterName);
 			schedule(datacenterId, 0, CloudSimTags.VM_CREATE_ACK, vm);
 			//sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
 		}
 
 		getDatacenterRequestedIdsList().add(datacenterId);
 		
 		setVmsRequested(getVmsRequested()+1);
 		setVmsAcks(getVmsAcks()+1);
 	}
 
 	private void processUtilFinished(SimEvent ev) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Finishing ");
 		for (int datacenter : getDatacenterIdsList()) {
 			sendNow(datacenter, UtilManager.UTIL_SIM_FINISHED, null);
 		}
 		
 	}
 
 	private void processCloudletUpdate(SimEvent ev) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Updating Cloudlets for next round ");
 		double cpuUtil = Double.parseDouble(ev.getData().toString());
 		if(cloudletsSubmitted < getVmsCreatedList().size()) {
 			List<Integer> vmsWithCloudletIds = getVmsWithCloudletIds();
 			List<Vm> vms = getVmsCreatedList();
 			List<Vm> missingCloudletVms = getMissingCloudletVms(vmsWithCloudletIds, vms);
 			for (Vm vm : missingCloudletVms) {
 			    Cloudlet cloudlet = createCloudlet(vm, cpuUtil);
 				cloudlet.setVmId(vm.getId());
 				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
 				//cloudlets.add(cloudlet);
 				getCloudletSubmittedList().add(cloudlet);
 				cloudletsSubmitted++;
 				// remove submitted cloudlets from waiting list
 				for (Cloudlet submittedCloudlet : getCloudletSubmittedList()) {
 					getCloudletList().remove(submittedCloudlet);
 				}
 			}
 			
 		}
 		
 		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
 			cloudlet.setUtilizationModelCpu(new UtilizationModelFixed(cpuUtil/(double)cloudletsSubmitted));
 		}
 		
 		for(Cloudlet cloudlet : cloudletSubmittedList) {
 			Log.printLine(CloudSim.clock() + ": " + getName() + " : at VM" + cloudlet.getVmId() + " : cloudlet at CPU util  "+ cloudlet.getUtilizationOfCpu(CloudSim.clock()));
 		}
 		
 		notifyObservers();
 
 	    sendNow(ev.getSource(), UtilManager.ROUND_COMPLETED, null);
 	}
 
 	private List<Vm> getMissingCloudletVms(List<Integer> vmsWithCloudletIds,
 			List<Vm> vms) {
 		List<Vm> vmsWithoutCloudlet = new ArrayList<Vm>();
 		//check every vm for a cloudlet
 		for (Vm vm : vms) {
 			//does the vm have a cloudlet?
 			boolean toAdd = true;
 			for (int vmId :  vmsWithCloudletIds) {
 				if(vmId == vm.getId()) {
 					toAdd = false;
 					break;
 				}
 			}
 			
 			//vm has no cloudlet yet, so save it
 			if(toAdd)
 			  vmsWithoutCloudlet.add(vm);
 		}
 		return vmsWithoutCloudlet;
 	}
 
 	private List<Integer> getVmsWithCloudletIds() {
 		List<Integer> vmIds = new ArrayList<Integer>();
 		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
 			vmIds.add(cloudlet.getVmId());
 		}
 		return vmIds;
 	}
 
 	private Cloudlet createCloudlet(Vm vm, double cpuUtil) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Creating Cloudlet ");
 	  // Cloudlet properties
 		
 	  int id = 0;
 	  int pesNumber = 1;
 	  long length = 100000000; //TODO calc it
 	  double utilizationPerVm = (cpuUtil/(double)getVmsCreatedList().size());	// util = 1 means 100% utilization
 
 	  long fileSize = 300;
 	  long outputSize = 300;
 	  UtilizationModel utilizationModel = new UtilizationModelFixed(utilizationPerVm);
 	  Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
 	  cloudlet.setUserId(getId());
 	  return cloudlet;
 	}
 
 	@Override
 	public void notifyObservers() {
 		for(Observer obs : this.observers) {
 			obs.refreshData(this);
 		}
 		
 	}
 
 	@Override
 	public void register(Observer obs) {
 		observers.add(obs);
 		
 	}
 
 	@Override
 	public void unRegister(Observer obs) {
 		observers.remove(obs);
 		
 	}
 
 }
