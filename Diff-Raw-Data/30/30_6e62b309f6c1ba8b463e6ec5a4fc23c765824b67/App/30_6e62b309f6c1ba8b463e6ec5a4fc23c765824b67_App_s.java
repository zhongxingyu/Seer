 package at.ac.tuwien.lsdc.resources;
 
 import java.util.LinkedList;
 
 import at.ac.tuwien.lsdc.generator.Request;
 
 public class App extends Resource {
 	// how many ticks the App will run
 	private int ticks;
 	
 	// extra ticks that the App cannot run.
 	// If this is > 0 the App is suspended and the extraTicks should be decremented.
 	private int extraTicks = 0;
 	
 	// SLAs
 	private int cpu;
 	private int memory;
 	private int storage;
 	
 	private int cpuSlaErrorcount;
 	
 	public int getCpuSlaErrorcount() {
 		return cpuSlaErrorcount;
 	}
 
 	public void setCpuSlaErrorcount(int cpuSlaErrorcount) {
 		this.cpuSlaErrorcount = cpuSlaErrorcount;
 	}
 
 	private int memorySlaErrorcount;
 	private int storageSlaErrorcount;
 
 	// used resources per tick
 	private LinkedList<Integer> cpuUsage;
 	private LinkedList<Integer> memoryUsage;
 	private LinkedList<Integer> storageUsage;
 	
 	//sla errors 
 	private LinkedList<Integer> slaErrorsPerTick = new LinkedList<Integer>();
 	
 	// link to the hosting vm
 	private VirtualMachine vm;
 	
 	private Request originalRequest;
 	
 	public Request getOriginalRequest() {
 		return originalRequest;
 	}
 
 	public void setOriginalRequest(Request originalRequest) {
 		this.originalRequest = originalRequest;
 	}
 
 	public VirtualMachine getVm() {
 		return vm;
 	}
 
 	public void setVm(VirtualMachine vm) {
 		this.vm = vm;
 	}
 
 	public App(int cpu, int memory, int storage, LinkedList<Integer> cpuUsage, LinkedList<Integer> memoryUsage, LinkedList<Integer> storageUsage, Request request) {
 		this.setNewAppId();
 		this.cpu= cpu;
 		this.memory = memory;
 		this.storage = storage;
 		
 		this.cpuUsage = cpuUsage;
 		this.memoryUsage = memoryUsage;
 		this.storageUsage = storageUsage;
 		this.ticks = this.cpuUsage.size();
 		
 		this.originalRequest = request;
 	}
 	
 	public int getTicks() {
 		return ticks;
 	}
 
 	public void setTicks(int ticks) {
 		this.ticks = ticks;
 	}
 
 
 	public int getExtraTicks() {
 		return extraTicks;
 	}
 
 	public void setExtraTicks(int extraTicks) {
 		this.extraTicks = extraTicks;
 	}
 
 	public int getCpu() {
 		return cpu;
 	}
 
 	public void setCpu(int cpu) {
 		this.cpu = cpu;
 	}
 
 	public int getMemory() {
 		return memory;
 	}
 
 	public void setMemory(int memory) {
 		this.memory = memory;
 	}
 	
 	public boolean isCpuSlaViolated() {
 		boolean result = false;
 		
 		if(isSomeThingSuspended()){
 			return true;
 		}
 		
 		if (this.getVm().getCurrentCpuAllocation() < this.getVm().getCurrentCpuUsage()) {
 			if (this.getCpu() > this.getCurrentCpuUsage()) {
 				result = true;
 			}
 		}
 		
 		return result;
 	}
 	
 	//gets the number of SLA violations for a certain number of past ticks for this app
 	public int getNumberOfSlaViolations(int maxTicks) {
 		int ret = 0;
 		if(this.getVm()!= null){
 			
 			LinkedList<Integer> slaErrors  = Resource.getLastEntriesUtil(slaErrorsPerTick, maxTicks);
 			for (Integer i : slaErrors) {
 				ret+=i;
 			}
 			return ret;
 		}
 		return 0;
 	}
 	
 	public boolean isMemorySlaViolated() {
 		boolean result = false;
 		
 		if(isSomeThingSuspended()){
 			return true;
 		}
 		
 		if (this.getVm().getCurrentMemoryAllocation() < this.getVm().getCurrentMemoryUsage()) {
 			if (this.getMemory() > this.getCurrentMemoryUsage()) {
 				result = true;
 			}
 		}
 		
 		return result;
 	}
 	
 	public boolean isSomeThingSuspended(){
 		if (this.suspendedTicks>0){
 			return true;
 		}
 		if (this.getVm()!=null){
 			if(this.getVm().suspendedTicks>0){
 				return true;
 			}
 			if(this.getVm().getPm()!=null){
 				if(this.getVm().getPm().suspendedTicks>0){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isStorageSlaViolated() {
 		boolean result = false;
 		
 		if(isSomeThingSuspended()){
 			return true;
 		}
 		
 		if (this.getVm().getCurrentStorageAllocation() < this.getVm().getCurrentStorageUsage()) {
 			if (this.getStorage() > this.getCurrentStorageUsage()) {
 				result = true;
 			}
 		}
 		
 		return result;
 	}
 
 	public int getStorage() {
 		return storage;
 	}
 
 	public void setStorage(int storage) {
 		this.storage = storage;
 	}
 
 	public Integer getCurrentCpuUsage() {
 		if(this.runningTicks>0 && this.runningTicks<=this.getCpuUsage().size()) {
 			return  this.getCpuUsage().get(this.runningTicks-1);
 		}
 		else {
 			return 0;
 		}
 	}
 
 	public Integer getCurrentMemoryUsage() {
 		if(this.runningTicks>0 && this.runningTicks<=this.getMemoryUsage().size()) {
 			return  this.getMemoryUsage().get(this.runningTicks-1);
 		}
 		else{
 			return 0;
 		}
 	}
 
 	public Integer getCurrentStorageUsage() {
 		if(this.runningTicks>0 && this.runningTicks<=this.getStorageUsage().size()) {
 			return this.getStorageUsage().get(this.runningTicks-1);
 		}
 		else {
 			return 0;
 		}
 	}
 
 	public LinkedList<Integer> getCpuUsage() {
 		return cpuUsage;
 	}
 
 	public void setCpuUsage(LinkedList<Integer> cpuUsage) {
 		this.cpuUsage = cpuUsage;
 	}
 
 	public LinkedList<Integer> getMemoryUsage() {
 		return memoryUsage;
 	}
 
 	public void setMemoryUsage(LinkedList<Integer> memoryUsage) {
 		this.memoryUsage = memoryUsage;
 	}
 
 	public LinkedList<Integer> getStorageUsage() {
 		return storageUsage;
 	}
 
 	public void setStorageUsage(LinkedList<Integer> storageUsage) {
 		this.storageUsage = storageUsage;
 	}
 	
 	public LinkedList<Integer> getCpuUsageHistory(int maxNumberOfEntries) {
 		if(this.runningTicks>0 && this.runningTicks<=this.getCpuUsage().size()) {
 			return getLastEntriesUtil(cpuUsage, maxNumberOfEntries, runningTicks-1 );
 		}
 		else {
 			return new LinkedList<Integer>();
 		}
 	}
 	
 	public LinkedList<Integer> getMemoryUsageHistory(int maxNumberOfEntries) {
 		if(this.runningTicks>0 && this.runningTicks<=this.getMemoryUsage().size()) {
 			return getLastEntriesUtil(memoryUsage, maxNumberOfEntries, runningTicks-1);
 		}
 		else {
 			return new LinkedList<Integer>();
 		}
 	}
 	
 	public LinkedList<Integer> getStorageUsageHistory(int maxNumberOfEntries) {
 		if(this.runningTicks>0 && this.runningTicks<=this.getStorageUsage().size()) {
 			return getLastEntriesUtil(storageUsage, maxNumberOfEntries,runningTicks-1);
 		}
 		else {
 			return new LinkedList<Integer>();
 		}
 	}
 
 	@Override
 	//at app level equivalent to usage 
 	public Integer getCurrentCpuAllocation() {
 		return this.getCurrentCpuUsage();
 	}
 
 	@Override
 	//at app level equivalent to usage
 	public Integer getCurrentMemoryAllocation() {
 		return this.getCurrentMemoryUsage();
 	}
 
 	@Override
 	//at app level equivalent to usage
 	public Integer getCurrentStorageAllocation() {
 		return this.getCurrentStorageUsage();
 	}
 
 	@Override
 	public void nextTick() {
 		//System.out.println("APP "+ this.getResourceId()+ ": next tick");
 		if (suspendedTicks>0) {
 			//System.out.println("APP Suspended" + this.getResourceId());
 			suspendedTicks--;
 		}
 		else {
 			if(getActionLock()>0){
 				actionLock--;
 			}
 			runningTicks++;
 			}
 	}
 
 	@Override
 	//at app level equivalent to usage
 	public LinkedList<Integer> getStorageAllocationHistory(int maxEntries) {
 		return this.getStorageUsageHistory(maxEntries);
 	}
 
 	@Override	
 	//at app level equivalent to usage	
 	public LinkedList<Integer> getCpuAllocationHistory(int maxEntries) {
 		return this.getCpuUsageHistory(maxEntries);
 	}
 
 	@Override
 	//at app level equivalent to usage	
 	public LinkedList<Integer> getMemoryAllocationHistory(int maxEntries) {
 		return this.getMemoryUsageHistory(maxEntries);
 	}
 
 	public int getMemorySlaErrorcount() {
 		return memorySlaErrorcount;
 	}
 
 	public void setMemorySlaErrorcount(int memorySlaErrorcount) {
 		this.memorySlaErrorcount = memorySlaErrorcount;
 	}
 
 	public int getStorageSlaErrorcount() {
 		return storageSlaErrorcount;
 	}
 
 	public void setStorageSlaErrorcount(int storageSlaErrorcount) {
 		this.storageSlaErrorcount = storageSlaErrorcount;
 	}
 	
 	public void logSlaViolations(){
 		int localcount =0;
 		if (isCpuSlaViolated()) {
 			cpuSlaErrorcount++;
 			localcount++;
 		}
 		
 		if (isMemorySlaViolated()) {
 			memorySlaErrorcount++;
 			localcount++;
 		}
 		
 		if (isStorageSlaViolated()) {
 			storageSlaErrorcount++;
 			localcount++;
 		}
 		
 		slaErrorsPerTick.add(localcount);
 		
 		//terminate?
 		if(runningTicks>ticks){
 			vm.getToRemoveList().add(this);
 		}
 
 	}
 
 }
