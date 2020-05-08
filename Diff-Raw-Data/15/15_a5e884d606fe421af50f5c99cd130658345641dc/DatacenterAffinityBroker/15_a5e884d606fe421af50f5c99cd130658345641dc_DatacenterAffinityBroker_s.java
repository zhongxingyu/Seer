 package de.hpi_web.cloudSim.multitier.datacenter;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cloudbus.cloudsim.Cloudlet;
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.DatacenterCharacteristics;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.UtilizationModel;
 import org.cloudbus.cloudsim.UtilizationModelFull;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 import org.cloudbus.cloudsim.core.CloudSimTags;
 import org.cloudbus.cloudsim.core.SimEvent;
 import org.cloudbus.cloudsim.lists.VmList;
 
 import de.hpi_web.cloudSim.multitier.MultiTierCloudTags;
 import de.hpi_web.cloudSim.multitier.MultiTierCloudlet;
 /*
  * DataCenterController
  * Override default behavior and limit the datacenter => vm mapping to only one possible datacenter.
  * Despite that properties/behaviour is introduced to fit the multiple tier architecture.
  */
 public class DatacenterAffinityBroker extends DatacenterBroker {
 	
 	private int tier;
 	private List<Integer> dcAffinity;
 	private DatacenterAffinityBroker successor;
 	private LoadBalancer loadBalancer;
 	
 	//TODO how to disallow multiple datacenters?
 
 	public DatacenterAffinityBroker(String name, int tier, int datacenterId) throws Exception {
 		super(name);
 		this.tier = tier;
 		this.dcAffinity = new ArrayList<Integer>();
 		this.loadBalancer = new FirstAvailableLoadBalancer(this);
 		addAffinity(datacenterId);
 	}
 	
 	public DatacenterAffinityBroker(String name, int tier) throws Exception {
 		super(name);
 		this.tier = tier;
 		this.dcAffinity = new ArrayList<Integer>();
 		this.loadBalancer = new FirstAvailableLoadBalancer(this);
 	}
 	
 	@Override
 	public void processOtherEvent(SimEvent ev) {
 		Object payload = ev.getData();
 		
 		switch (ev.getTag()) {
 			// Request
 			case MultiTierCloudTags.REQUEST_TAG:
 				processRequestTag(ev);
 				break;
 //			case CloudSimTags.CLOUDLET_SUBMIT:
 //				processRequestTag(ev);
 //				break;
 		}
 		
 		//TODO process new events like request/response
 		
 
 	}
 
 	private void processRequestTag(SimEvent ev) {
 		// gets the Cloudlet object
 		MultiTierCloudlet cl = (MultiTierCloudlet) ev.getData();
 		
 		getCloudletList().add(cl);
 		submitCloudlets();
 		processFurtherLoad(cl);
 		
 	}
 
 	public DatacenterAffinityBroker getSuccessor() {
 		return successor;
 	}
 
 	public void setSuccessor(DatacenterAffinityBroker successor) {
 		this.successor = successor;
 	}
 
 	private void processFurtherLoad(MultiTierCloudlet parent) {
 		if(this.successor != null) {
 	        for (MultiTierCloudlet child : parent.getChildren()) {
 				CloudSim.send(getId(), this.successor.getId(), 0, MultiTierCloudTags.REQUEST_TAG, child);
 	        }
			int parentDatacenterId = vmsToDatacentersMap.get(parent.getVmId());
	        sendNow(parentDatacenterId, CloudSimTags.CLOUDLET_PAUSE, parent);
 		}
 	}
 
 	public void setDcAffinityList(List<Integer> dcAffinity) {
 		this.dcAffinity = dcAffinity;
 	}
 	public List<Integer> getDcAffinityList() {
 		return dcAffinity;
 	}
 	public void addAffinity(int datacenterId) {
 		if (!dcAffinity.contains(datacenterId))
 			dcAffinity.add(datacenterId);
 	}
 	
 	/**
 	 * Process a cloudlet return event.
 	 * 
 	 * @param ev a SimEvent object
 	 * @pre ev != $null
 	 * @post $none
 	 */
 	@Override
 	protected void processCloudletReturn(SimEvent ev) {
 		Log.printLine("Returning Cloudlet " + CloudSim.clock());
 		MultiTierCloudlet cloudlet = (MultiTierCloudlet) ev.getData();
 		
 		//TODO only do it if cloudlet is paused
 		if(cloudlet.getParent() != null) {
 			Log.printLine("Resuming old Cloudlet" + CloudSim.clock());
 			DatacenterAffinityBroker parentBroker = (DatacenterAffinityBroker) CloudSim.getEntity(cloudlet.getParent().getUserId());
			sendNow(parentBroker.dcAffinity.get(0), CloudSimTags.CLOUDLET_RESUME, cloudlet.getParent());
 		}
 
 		getCloudletReceivedList().add(cloudlet);
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
 				+ " received");
 		cloudletsSubmitted--;
 		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed so far.");
 		} else { // some cloudlets haven't finished yet
 			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
 				// all the cloudlets sent finished. It means that some bount
 				// cloudlet is waiting its VM be created
 				clearDatacenters();
 				createVmsInDatacenter(0);
 			}
 
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see cloudsim.core.SimEntity#shutdownEntity()
 	 */
 	@Override
 	public void shutdownEntity() {
 		Log.printLine(getName() + " is shutting down...");
 		clearDatacenters();
 		finishExecution();
 	}
 	
 ////////////////////////////////////////////////////////////////////////////////////
 ///Methods which have to be overridden to realize a Broker 1:x datacenter Mapping///
 ////////////////////////////////////////////////////////////////////////////////////
 
 	@Override
 	protected void processResourceCharacteristics(SimEvent ev) {
 		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
 		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);
 
 		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
 			setDatacenterRequestedIdsList(new ArrayList<Integer>());
 			if(dcAffinity.isEmpty())
 				createVmsInDatacenter(getDatacenterIdsList().get(0));
 			else {
 				for (int id : getDatacenterIdsList()) {
 					if (dcAffinity.contains(id)) {
 						createVmsInDatacenter(id);
 					}
 				}
 			}
 		}
 	}
 	/*
 	@Override
 	protected void processResourceCharacteristicsRequest(SimEvent ev) {
 		//dont get all datacenters, just take the the one which is passed by the conctructor
 		setDatacenterIdsList(CloudSim.getCloudResourceList());  
 		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
 
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
 				+ getDatacenterIdsList().size() + " resource(s)");
 
 		for (Integer datacenterId : getDatacenterIdsList()) {
 			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
 		}
 	}*/
 	
 	@Override
 	protected void submitCloudlets() {
 		int vmIndex = 0;
 		
 		for (Cloudlet cloudlet : getCloudletList()) {
 			Vm vm;
 			// if user didn't bind this cloudlet and it has not been executed yet
 			if (cloudlet.getVmId() == -1) {
 				//TODO Load Balancer HERE. He determines what vm to use.
 				//vm = loadBalancer.getNextVm();
 				vm = getVmsCreatedList().get(vmIndex);
 			} else { // submit to the specific vm
 				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
 				if (vm == null) { // vm was not created
 					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
 							+ cloudlet.getCloudletId() + ": bount VM not available");
 					continue;
 				}
 			}
 
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
 					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
 			cloudlet.setVmId(vm.getId());
 
 			if(!dcAffinity.isEmpty()) {
 				int datacenterId;
 				try {
 					datacenterId = vmsToDatacentersMap.get(cloudlet.getVmId());
 				} catch (Exception e) {
 					Log.printLine("No datacenters in broker.");
 					break;
 				}
 				sendNow(datacenterId, CloudSimTags.CLOUDLET_SUBMIT, cloudlet);	
 			}
 			
 			cloudletsSubmitted++;
 			
 			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
 			getCloudletSubmittedList().add(cloudlet);
 			
 			processFurtherLoad(new MultiTierCloudlet(cloudlet));
 		}
 
 		// remove submitted cloudlets from waiting list
 		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
 			getCloudletList().remove(cloudlet);
 		}
 		
 
 	}
 	
 	/**
 	 * Destroy the virtual machines running in datacenters.
 	 * 
 	 * @pre $none
 	 * @post $none
 	 */
 	@Override
 	protected void clearDatacenters() {
 		for(int datacenterId : dcAffinity) {
 			for (Vm vm : getVmsCreatedList()) {
 				Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
 				sendNow(datacenterId, CloudSimTags.VM_DESTROY, vm);
 			}
 		}
 
 
 		getVmsCreatedList().clear();
 	}
 	
 	@Override
 	protected Map<Integer, Integer> getVmsToDatacentersMap() {
 		//throw new NotImplementedException();
 		return super.getVmsToDatacentersMap();
 	}
 	
 	/**
 	 * Create the virtual machines in a datacenter.
 	 * 
 	 * @param datacenterId Id of the chosen PowerDatacenter
 	 * @pre $none
 	 * @post $none
 	 */
 	@Override
 	protected void createVmsInDatacenter(int datacenterId) {
 		if(!dcAffinity.contains(datacenterId)) {
 			Log.printLine("Warning: Datacenter not found.");
 			return;
 		}
 		// send as much vms as possible for this datacenter before trying the next one
 		int requestedVms = 0;
 		// TODO: when (hard) affinity is provided and the DCs are full, throw an exception
 		
 		
 		String datacenterName = CloudSim.getEntityName(datacenterId);
 		for (Vm vm : getVmList()) {
 			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
 				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
 						+ " in " + datacenterName);
 				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
 				requestedVms++;
 			}
 		}
 
 		getDatacenterRequestedIdsList().add(datacenterId);
 
 		setVmsRequested(requestedVms);
 		setVmsAcks(0);
 	}
 	
 	/**
 	 * Process the ack received due to a request for VM creation.
 	 * 
 	 * @param ev a SimEvent object
 	 * @pre ev != null
 	 * @post $none
 	 */
 	@Override
 	protected void processVmCreate(SimEvent ev) {
 		int[] data = (int[]) ev.getData();
 		int datacenterId = data[0]; //TODO override
 		int vmId = data[1];
 		int result = data[2];
 
 		if (result == CloudSimTags.TRUE) {
 			getVmsToDatacentersMap().put(vmId, datacenterId);
 			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
 					+ " has been created in Datacenter #" + datacenterId + ", Host #"
 					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
 		} else {
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
 					+ " failed in Datacenter #" + datacenterId);
 		}
 
 		incrementVmsAcks();
 
 		// all the requested VMs have been created
 		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
 			submitCloudlets();
 		} else {
 			// all the acks received, but some VMs were not created
 			if (getVmsRequested() == getVmsAcks()) {
 				// find id of the next datacenter that has not been tried
 				for (int nextDatacenterId : getDatacenterIdsList()) {
 					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
 						createVmsInDatacenter(nextDatacenterId);
 						return;
 					}
 				}
 
 				// all datacenters already queried
 				if (getVmsCreatedList().size() > 0) { // if some vm were created
 					submitCloudlets();
 				} else { // no vms created. abort
 					Log.printLine(CloudSim.clock() + ": " + getName()
 							+ ": none of the required VMs could be created. Aborting");
 					finishExecution();
 				}
 			}
 		}
 	}
 
 }
