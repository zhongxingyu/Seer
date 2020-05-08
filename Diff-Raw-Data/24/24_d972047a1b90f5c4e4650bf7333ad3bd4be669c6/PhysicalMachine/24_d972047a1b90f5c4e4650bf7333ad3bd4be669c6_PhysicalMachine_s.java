 package at.ac.tuwien.infosys.lsdc.scheduler.objects;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.CloudCluster;
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.IResourceInformation;
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.Resource;
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.exceptions.PhysicalMachineException;
 import at.ac.tuwien.infosys.lsdc.scheduler.JobScheduler;
 
 
 public class PhysicalMachine extends Machine implements IResourceInformation, Cloneable{
 	private Integer id = null;
 	private Integer CPUs = null;
 	private Integer memory = null;
 	private Integer diskMemory = null;
 	private Integer pricePerCycle = null;
 	
 	private transient Integer usedCPUs = 0;
 	private transient Integer usedMemory = 0;
 	private transient Integer usedDiskMemory = 0;
 	
 	private transient HashMap<Integer,VirtualMachine> virtualMachines = new HashMap<Integer,VirtualMachine>();
 	
 	private transient Integer currentMaxVMId = 0;
 	
 	public synchronized VirtualMachine startVirtualMachine(Integer neededDiskMemory,Integer neededMemory, Integer neededCPUS) {
 		usedCPUs += neededCPUS;
 		usedMemory += neededMemory;
 		usedDiskMemory += neededDiskMemory;
 		
 		VirtualMachine vm = new VirtualMachine(this,createVMId(),neededDiskMemory, neededMemory, neededCPUS);
 		getVirtualMachines().put(vm.getId(), vm);
 		//virtualMachines.put(vm.getId(), vm);
 		return vm;
 	}
 	
 	public synchronized Map<Integer, VirtualMachine> getVirtualMachines() {
 		return Collections.synchronizedMap(virtualMachines);
 	}
 
 	private Integer createVMId() {
 		return ++currentMaxVMId;
 		
 	}
 	
 	public Resource getUsedResources() {
 		return new Resource(id, usedCPUs, usedMemory, usedDiskMemory);
 	}
 	
 	public Resource getTotalResources() {
 		return new Resource(id, CPUs, memory, diskMemory);
 	}
 
 	
 	public Integer getPricePerCycle() {
 
 		return pricePerCycle;
 	}
 
 
 	public void setPricePerCycle(Integer pricePerCycle) {
 		this.pricePerCycle = pricePerCycle;
 	}
 	
 	public Integer getCPUs() {
 		return CPUs;
 	}
 	
 	public Integer getMemory() {
 		return memory;
 	}
 	
 	public Integer getDiskMemory() {
 		return diskMemory;
 	}
 	
 	public void setCPUs(Integer cPUs) {
 		CPUs = cPUs;
 	}
 	
 	public void setMemory(Integer memory) {
 		this.memory = memory;
 	}
 	public void setDiskMemory(Integer diskMemory) {
 		this.diskMemory = diskMemory;
 	}
 	
 	public boolean canHostJob(InsourcedJob job) {
 		return job.getConsumedCPUs() <= (CPUs - usedCPUs) && 
 				job.getConsumedMemory() <= (memory - usedMemory) &&
 				job.getConsumedDiskMemory() <= (diskMemory - usedDiskMemory);
 	}
 	
 	public ArrayList<VirtualMachine> getVirtualHostingCandidates(InsourcedJob job) {
 		ArrayList<VirtualMachine> candidates = new ArrayList<VirtualMachine>();
 		for (VirtualMachine currentCandidate : virtualMachines.values()) {
 			if (currentCandidate.canHostJob(job)) {
 				candidates.add(currentCandidate);
 			}
 		}
 		return candidates;
 	}
 	
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	@Override
 	public String toString() {
 		return "PhysicalMachine [CPUs=" + CPUs + ", memory=" + memory
 				+ ", diskMemory=" + diskMemory + ", pricePerCycle="
 				+ pricePerCycle + "]";
 	}
 	
 	public void setUsedCPUs(Integer usedCPUs) {
 		this.usedCPUs = usedCPUs;
 	}
 
 	public void setUsedMemory(Integer usedMemory) {
 		this.usedMemory = usedMemory;
 	}
 
 	public void setUsedDiskMemory(Integer usedDiskMemory) {
 		this.usedDiskMemory = usedDiskMemory;
 	}
 	
 	public void setVirtualMachines(HashMap<Integer, VirtualMachine> virtualMachines) {
 		this.virtualMachines = virtualMachines;
 	}
 
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		PhysicalMachine clonedMachine = new PhysicalMachine();
 		clonedMachine.setId(id);
 		clonedMachine.setCPUs(CPUs);
 		clonedMachine.setDiskMemory(diskMemory);
 		clonedMachine.setMemory(memory);
 		clonedMachine.setPricePerCycle(pricePerCycle);
 		
 		clonedMachine.setUsedCPUs(new Integer(usedCPUs));
 		clonedMachine.setUsedDiskMemory(new Integer(usedDiskMemory));
 		clonedMachine.setUsedMemory(new Integer(usedMemory));
 		
 		clonedMachine.setVirtualMachines((HashMap<Integer, VirtualMachine>)virtualMachines.clone());		
 		return clonedMachine;
 	}
 
 	public void removeVM(VirtualMachine VM) {
 
 		usedCPUs -= VM.getTotalAvailableCPUs();
 		usedMemory -= VM.getTotalAvailableMemory();
 		usedDiskMemory -= VM.getTotalAvailableDiskMemory();
 		
 		getVirtualMachines().remove(VM.getId());
 	}
 
 	public void shutdown() {
 		CloudCluster cluster = JobScheduler.getInstance().getCluster();
 		try {
 			cluster.stopMachine(this);
 		} catch (PhysicalMachineException e) {
 			System.out.println("Ohoh, stopping machine not possible?" + e.getMessage());
 		}
 	}
 
 	@Override
 	public synchronized boolean cleanupMachine() {
 		Map<Integer, VirtualMachine> cleanupMachines = getVirtualMachines();
 		synchronized (cleanupMachines) { 
 			for (VirtualMachine currentVM : cleanupMachines.values()) {
 				if (currentVM.cleanupMachine()) {
					removeVM(currentVM);
 				}
 			}
 		}
 		return virtualMachines.isEmpty();
 	}
 	
 //	@Override
 //	public boolean equals(Object o){
 //		if (o instanceof PhysicalMachine){
 //			PhysicalMachine machine = (PhysicalMachine)o;
 //			if (this.id.equals(machine.getId())){
 //				return true;
 //			}
 //		}
 //		return false;
 //	}
 	
 }
