 package at.ac.tuwien.infosys.lsdc.cloud.cluster;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.exceptions.PhysicalMachineException;
 import at.ac.tuwien.infosys.lsdc.scheduler.IJobEventListener;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.InsourcedJob;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.PhysicalMachine;
 import at.ac.tuwien.infosys.lsdc.scheduler.objects.VirtualMachine;
 import at.ac.tuwien.infosys.lsdc.scheduler.statistics.PhysicalMachineUsage;
 
 public class CloudCluster implements IJobEventListener {
 
 	private HashMap<Integer, PhysicalMachine> physicalMachines = null;
 	private HashMap<Integer, PhysicalMachine> runningMachines = null;
 	private HashMap<Integer, PhysicalMachine> offlineMachines = null;
 
 	// store the overall available resources in the cloud, as long as they are
 	// enough for a job, it should
 	// be possible to migrate some jobs to other machines to fit the waiting job
 	// in somewhere
 	private Integer currentUsedMemory = 0;
 	private Integer currentUsedCPUs = 0;
 	private Integer currentUsedDiskMemory = 0;
 
 	private Integer totalMemory = 0;
 	private Integer totalCPUs = 0;
 	private Integer totalDiskMemory = 0;
 	
 	public void cleanup() {
 		Map<Integer,PhysicalMachine> synchedRunningMachinesMap = Collections.synchronizedMap(runningMachines);
 		Map<Integer,PhysicalMachine> synchedStoppedMachinesMap = Collections.synchronizedMap(offlineMachines);
 		
 		synchronized (synchedRunningMachinesMap) {
 			synchronized (synchedStoppedMachinesMap) {
				ArrayList<PhysicalMachine> toRemoveMachines = new ArrayList<PhysicalMachine>();
 				for (PhysicalMachine currentMachine : synchedRunningMachinesMap.values()) {
 					if (currentMachine.cleanupMachine()) {
						//synchedRunningMachinesMap.remove(currentMachine.getId());
						toRemoveMachines.add(currentMachine);
 						synchedStoppedMachinesMap.put(currentMachine.getId(), currentMachine);
 					}
 				}
				for (PhysicalMachine currentToRemove : toRemoveMachines) {
					synchedRunningMachinesMap.remove(currentToRemove.getId());
				}
 				
 			}
 		}
 	}
 
 	public CloudCluster(HashMap<Integer, PhysicalMachine> physicalMachines) {
 		this.physicalMachines = physicalMachines;
 		this.offlineMachines = new HashMap<Integer, PhysicalMachine>();
 		this.runningMachines = new HashMap<Integer, PhysicalMachine>();
 		PhysicalMachine[] machines = physicalMachines.values().toArray(
 				new PhysicalMachine[physicalMachines.values().size()]);
 
 		for (PhysicalMachine currentMachine : machines) {
 			totalCPUs += currentMachine.getCPUs();
 			totalDiskMemory += currentMachine.getDiskMemory();
 			totalMemory += currentMachine.getMemory();
 
 			offlineMachines.put(currentMachine.getId(), currentMachine);
 		}
 
 	}
 
 	public void addPhysicalMachine(PhysicalMachine machine) {
 		this.physicalMachines.put(machine.getId(), machine);
 		this.offlineMachines.put(machine.getId(), machine);
 	}
 
 	private Integer[] getRegisteredMachines() {
 		return (Integer[]) physicalMachines.keySet().toArray();
 	}
 
 	public Boolean jobFits(InsourcedJob job) {
 		return (totalCPUs - currentUsedCPUs >= job.getConsumedCPUs() &&
 		totalDiskMemory - currentUsedDiskMemory >= job.getConsumedDiskMemory() &&
 		totalMemory - currentUsedMemory >= job.getConsumedMemory());
 
 	}
 
 	/**
 	 * determines all possible candidates to host a job passed by the argument
 	 * job
 	 * 
 	 * @param job
 	 *            the job to be hosted
 	 * @returns an array of candidates to host a job, the best candidate will be
 	 *          determined by a best fit heuristic. the set of candidates can
 	 *          also be zero
 	 */
 
 	public PhysicalMachine[] getRunningHostingCandidates(InsourcedJob job) {
 		ArrayList<PhysicalMachine> candidates = getHostingCandidates(job,runningMachines.values());
 		return candidates.toArray(new PhysicalMachine[candidates.size()]);
 	}
 	
 	public PhysicalMachine[] getStoppedHostingCandidates(InsourcedJob job) {
 		ArrayList<PhysicalMachine> candidates = getHostingCandidates(job, offlineMachines.values());
 		return candidates.toArray(new PhysicalMachine[candidates.size()]);
 	}
 	
 	public VirtualMachine[] getVirtualHostingCandidates(InsourcedJob job) {
 		ArrayList<VirtualMachine> allCandidates = new ArrayList<VirtualMachine>();
 		for (PhysicalMachine currentMachine : runningMachines.values()) {
 			ArrayList<VirtualMachine> machineHostingCandidates = currentMachine
 					.getVirtualHostingCandidates(job);
 			if (machineHostingCandidates.size() > 0) {
 				allCandidates.addAll(machineHostingCandidates);
 			}
 		}
 		return allCandidates.toArray(new VirtualMachine[allCandidates.size()]);
 	}
 
 	private ArrayList<PhysicalMachine> getHostingCandidates(InsourcedJob job,Collection<PhysicalMachine> machines) {
 
 		ArrayList<PhysicalMachine> candidates = new ArrayList<PhysicalMachine>();
 
 		for (PhysicalMachine currentMachine : machines) {
 			if (currentMachine.canHostJob(job)) {
 				candidates.add(currentMachine);
 			}
 		}
 		return candidates;
 	}
 	
 	public PhysicalMachine startMachine(PhysicalMachine machine) {
 		runningMachines.put(machine.getId(), offlineMachines.remove(machine.getId()));
 		return machine;
 	}
 	
 	public void stopMachine(PhysicalMachine pm) throws PhysicalMachineException{
 		if (!runningMachines.values().contains(pm)){
 			throw new PhysicalMachineException("Could not stop: Physical machine with id: " + pm.getId() + " is not running.");
 
 		}
 		if (pm.getVirtualMachines().size() != 0) {
 			throw new PhysicalMachineException(
 					"Could not stop: Physical machine with id: " + pm.getId()
 							+ " has running virtual machines");
 		}
 		runningMachines.remove(pm.getId());
 		offlineMachines.put(pm.getId(), pm);
 	}
 
 	public PhysicalMachine getPhysicalMachine(Integer id) {
 		return physicalMachines.get(id);
 	}
 
 	public PhysicalMachine[] getRunningMachines() {
 		return runningMachines.values().toArray(
 				new PhysicalMachine[runningMachines.values().size()]);
 	}
 
 	public PhysicalMachine[] getOfflineMachines() {
 		return offlineMachines.values().toArray(
 				new PhysicalMachine[offlineMachines.values().size()]);
 	}
 
 	public ArrayList<PhysicalMachineUsage> getUsage() {
 		ArrayList<PhysicalMachineUsage> physicalMachineUsage = new ArrayList<PhysicalMachineUsage>();
 		for (PhysicalMachine currentPM : runningMachines.values()) {
 			physicalMachineUsage.add(new PhysicalMachineUsage(currentPM
 					.getUsedResources(), currentPM.getTotalResources()));
 
 		}
 		return physicalMachineUsage;
 	}
 
 	@Override
 	public void jobAdded(InsourcedJob job) {
 		currentUsedCPUs += job.getConsumedCPUs();
 		currentUsedDiskMemory += job.getConsumedDiskMemory();
 		currentUsedMemory += job.getConsumedMemory();
 	}
 
 	@Override
 	public void jobCompleted(InsourcedJob job) {
 		currentUsedCPUs -= job.getConsumedCPUs();
 		currentUsedDiskMemory -= job.getConsumedDiskMemory();
 		currentUsedMemory -= job.getConsumedMemory();
 		job.getCurrentVirtualMachineEnvironment().removeJob(job);
 	}
 }
