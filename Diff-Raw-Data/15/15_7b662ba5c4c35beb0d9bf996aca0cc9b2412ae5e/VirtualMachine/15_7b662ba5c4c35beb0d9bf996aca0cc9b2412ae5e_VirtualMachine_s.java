 package at.ac.tuwien.infosys.lsdc.scheduler.objects;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.IResourceInformation;
 import at.ac.tuwien.infosys.lsdc.cloud.cluster.Resource;
 
 public class VirtualMachine extends Machine implements IResourceInformation, Cloneable{
 	private Integer totalAvailableMemory = null;
 	private Integer totalAvailableCPUs = null;
 	private Integer totalAvailableDiskMemory = null;
 	
 	private Integer currentUsedTotalMemory = 0;
 	private Integer currentUsedTotalCPUs = 0;
 	private Integer currentUsedTotalDiskMemory = 0;
 	
 	private PhysicalMachine host = null;
 	
 	private HashMap<InsourcedJob, Thread> runningJobThreads = new HashMap<InsourcedJob, Thread>();
 	
 	Map map = Collections.synchronizedMap(runningJobThreads);
 	
 	private Integer id = 0;
 	
 	public VirtualMachine(){
 	}
 
 	public VirtualMachine(PhysicalMachine host, Integer id, Integer diskSize, Integer memorySize, Integer numCPUs){
 		this.totalAvailableCPUs = numCPUs;
 		this.totalAvailableDiskMemory = diskSize;
 		this.totalAvailableMemory = memorySize;
 		this.id = id;
 		this.host = host;
 	}
 	
 	public synchronized void addJob(InsourcedJob job){
 		Thread jobThread = new Thread(job);
 		runningJobThreads.put(job, jobThread);
 		currentUsedTotalCPUs += job.getConsumedCPUs();
 		currentUsedTotalDiskMemory += job.getConsumedDiskMemory();
 		currentUsedTotalMemory += job.getConsumedMemory();
 		job.setCurrentVirtualMachineEnvironment(this);
 		jobThread.start();
 	}
 	
 	public void jobCompleted(InsourcedJob job) {
 		runningJobThreads.remove(job);
 		currentUsedTotalCPUs -= job.getConsumedCPUs();
 		currentUsedTotalDiskMemory -= job.getConsumedDiskMemory();
 		currentUsedTotalMemory -= job.getConsumedMemory();
 	}
 	
 	public void stopJobs(){
 		for (Thread currentJobThread : runningJobThreads.values()){
			currentJobThread.interrupt();
 		}
 	}
 	
 	public void resumeJobs(){
 		for (InsourcedJob currentJob : runningJobThreads.keySet()){
 			Thread newJobThread = new Thread(currentJob);
 			runningJobThreads.put(currentJob, newJobThread);
 			newJobThread.start();
 		}
 	}
 	
 	
 
 	public Integer getTotalAvailableMemory() {
 		return totalAvailableMemory;
 	}
 
 	public Integer getTotalAvailableCPUs() {
 		return totalAvailableCPUs;
 	}
 
 	public Integer getTotalAvailableDiskMemory() {
 		return totalAvailableDiskMemory;
 	}
 
 	public Integer getCurrentUsedTotalMemory() {
 		return currentUsedTotalMemory;
 	}
 
 	public Integer getCurrentUsedTotalCPUs() {
 		return currentUsedTotalCPUs;
 	}
 
 	public Integer getCurrentUsedTotalDiskMemory() {
 		return currentUsedTotalDiskMemory;
 	}
 	
 	public boolean canHostJob(InsourcedJob job) {
 		return job.getConsumedCPUs() <= (totalAvailableCPUs - currentUsedTotalCPUs) && 
 				job.getConsumedMemory() <= (totalAvailableMemory - currentUsedTotalMemory) &&
 				job.getConsumedDiskMemory() <= (totalAvailableDiskMemory - currentUsedTotalDiskMemory);
 	}
 	
 	public void removeJob(InsourcedJob job){
 		currentUsedTotalCPUs -= job.getConsumedCPUs();
 		currentUsedTotalDiskMemory -= job.getConsumedDiskMemory();
 		currentUsedTotalMemory -= job.getConsumedMemory();
 		runningJobThreads.remove(job);
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	@Override
 	public Resource getUsedResources() {
 		return new Resource(id, currentUsedTotalCPUs, currentUsedTotalMemory, currentUsedTotalDiskMemory);
 	}
 
 	@Override
 	public Resource getTotalResources() {
 		return new Resource(id, totalAvailableCPUs, totalAvailableMemory, totalAvailableDiskMemory);
 	}
 
 	public PhysicalMachine getHost() {
 		return host;
 	}
 
 	public void setTotalAvailableMemory(Integer totalAvailableMemory) {
 		this.totalAvailableMemory = totalAvailableMemory;
 	}
 
 	public void setTotalAvailableCPUs(Integer totalAvailableCPUs) {
 		this.totalAvailableCPUs = totalAvailableCPUs;
 	}
 
 	public void setTotalAvailableDiskMemory(Integer totalAvailableDiskMemory) {
 		this.totalAvailableDiskMemory = totalAvailableDiskMemory;
 	}
 
 	public void setCurrentUsedTotalMemory(Integer currentUsedTotalMemory) {
 		this.currentUsedTotalMemory = currentUsedTotalMemory;
 	}
 
 	public void setCurrentUsedTotalCPUs(Integer currentUsedTotalCPUs) {
 		this.currentUsedTotalCPUs = currentUsedTotalCPUs;
 	}
 
 	public void setCurrentUsedTotalDiskMemory(Integer currentUsedTotalDiskMemory) {
 		this.currentUsedTotalDiskMemory = currentUsedTotalDiskMemory;
 	}
 
 	public void setRunningJobThreads(HashMap<InsourcedJob, Thread> runningJobThreads) {
 		this.runningJobThreads = runningJobThreads;
 	}
 
 	public HashMap<InsourcedJob, Thread> getRunningJobs() {
 		return runningJobThreads;
 	}
 
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		VirtualMachine clonedMachine = new VirtualMachine();
 		clonedMachine.setId(id);
 		clonedMachine.setTotalAvailableCPUs(totalAvailableCPUs);
 		clonedMachine.setTotalAvailableDiskMemory(totalAvailableDiskMemory);
 		clonedMachine.setTotalAvailableMemory(totalAvailableMemory);
 		
 		clonedMachine.setCurrentUsedTotalCPUs(new Integer(currentUsedTotalCPUs));
 		clonedMachine.setCurrentUsedTotalDiskMemory(new Integer(currentUsedTotalDiskMemory));
 		clonedMachine.setCurrentUsedTotalMemory(new Integer(currentUsedTotalMemory));
 		
 		clonedMachine.setRunningJobThreads((HashMap<InsourcedJob, Thread>)runningJobThreads.clone());
 		
 		return clonedMachine;
 	}
 
 	@Override
 	public boolean cleanupMachine() {
 		return runningJobThreads.isEmpty();
 	}
 	
 	
 }
