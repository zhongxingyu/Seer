 package at.ac.tuwien.lsdc.resources;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import at.ac.tuwien.lsdc.Configuration;
 
 public class VirtualMachine extends Resource {
 	private static int cpuOverhead;
 	private static int memoryOverhead;
 	private static int storageOverhead;
 	
 	// resources the VM has allocated on the PM (Apps + overhead of VM)
 	private LinkedList<Integer> allocatedCpu = new LinkedList<Integer>();
 	private LinkedList<Integer> allocatedMemory = new LinkedList<Integer>();
 	private LinkedList<Integer> allocatedStorage = new LinkedList<Integer>();
 
 	LinkedList<App> toRemoveList = new LinkedList<App>();
 	
 	private PhysicalMachine pm;
 
 	private LinkedList<App> apps = new LinkedList<App>();
 	
 	public static int getCpuOverhead() {
 		return cpuOverhead;
 	}
 
 	public static void setCpuOverhead(int cpuOverhead) {
 		VirtualMachine.cpuOverhead = cpuOverhead;
 	}
 
 	public static int getMemoryOverhead() {
 		return memoryOverhead;
 	}
 
 	public static void setMemoryOverhead(int memoryOverhead) {
 		VirtualMachine.memoryOverhead = memoryOverhead;
 	}
 
 	public static int getStorageOverhead() {
 		return storageOverhead;
 	}
 
 	public static void setStorageOverhead(int storageOverhead) {
 		VirtualMachine.storageOverhead = storageOverhead;
 	}
 	
 	public LinkedList<App> getToRemoveList() {
 		return toRemoveList;
 	}
 
 	public void setToRemoveList(LinkedList<App> toRemoveList) {
 		this.toRemoveList = toRemoveList;
 	}
 	
 	public PhysicalMachine getPm() {
 		return pm;
 	}
 
 	public void setPm(PhysicalMachine pm) {
 		this.pm = pm;
 	}
 	
 	public VirtualMachine(int initialCpu, int initialMemory, int initialStorage, int startupTime) {
 		// set init values
 		VirtualMachine.setCpuOverhead(Configuration.getInstance().getVmCpuOverhead());
 		VirtualMachine.setMemoryOverhead(Configuration.getInstance().getVmMemoryOverhead());
 		VirtualMachine.setStorageOverhead(Configuration.getInstance().getVmStorageOverhead());
 		
 		setNewVmId();
 		this.allocatedCpu.addLast(initialCpu + getCpuOverhead());
 		this.allocatedMemory.addLast(initialMemory + getMemoryOverhead());
 		this.allocatedStorage.addLast(initialStorage + getStorageOverhead());
 		this.suspendedTicks = startupTime;
 	}
 	
 	@SuppressWarnings("unused")
 	private VirtualMachine() {
 	}
 	
 	/**
 	 * Retrieves the current CPU allocation of all Apps running on this VM + Overhead
 	 */
 	public Integer getCurrentCpuAllocation() {
 		return allocatedCpu.getLast();
 	}
 	
 	//returns the last n cpu - allocation values 
 	public LinkedList<Integer> getCpuAllocationHistory(int maxNumberOfEntries) {
 		return getLastEntriesUtil(allocatedCpu, maxNumberOfEntries);
 	}
 	
 	//returns the last n memory - allocation values
 	public LinkedList<Integer> getMemoryAllocationHistory(int maxNumberOfEntries) {
 		return getLastEntriesUtil(allocatedMemory, maxNumberOfEntries);
 	}
 	
 	//returns the last n storage allocation values
 	public LinkedList<Integer> getStorageAllocationHistory(int maxNumberOfEntries) {
 		return getLastEntriesUtil(allocatedStorage, maxNumberOfEntries);
 	}
 	
 	//returns the last n cpu - usage parameters
 	public LinkedList<Integer> getCpuUsageHistory(int maxNumberOfEntries) {
 		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
 		values.add(dummyList(maxNumberOfEntries));
 		for (App a: apps) {
 			values.add(a.getCpuUsageHistory(maxNumberOfEntries));
 		}
 		
 		return aggregateValues(values);
 	}
 	
 	//returns the last n memory - usage parameters
 	public LinkedList<Integer> getMemoryUsageHistory(int maxNumberOfEntries) {
 		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
 		values.add(dummyList(maxNumberOfEntries));
 		for (App a: apps) {
 			values.add(a.getMemoryUsageHistory(maxNumberOfEntries));
 		}
 		
 		return aggregateValues(values);
 	}
 	
 	//returns the last n storage - usage parameters
 	public LinkedList<Integer> getStorageUsageHistory(int maxNumberOfEntries) {
 		LinkedList<LinkedList<Integer>> values = new LinkedList<LinkedList<Integer>>();
 		values.add(dummyList(maxNumberOfEntries));
 		for (App a: apps) {
 			values.add(a.getStorageUsageHistory(maxNumberOfEntries));
 		}
 			
 		return aggregateValues(values);
 	}
 	
 	/**
 	 * get CPU usage produced by apps + overhead
 	 */
 	public Integer getCurrentCpuUsage() {
 		int cpuusage = 0 ;
 		for (App a : apps) {
 			cpuusage += a.getCurrentCpuUsage();
 		}
 		return cpuusage + getCpuOverhead();
 	}
 	
 	/**
 	 * get memory usage produced by apps + overhead
 	 */
 	public Integer getCurrentMemoryUsage() {
 		int memusage = 0 ;
 		for (App a : apps) {
 			memusage += a.getCurrentMemoryUsage();
 		}
 		return memusage + getMemoryOverhead();
 	}
 	
 	//set cpu allocation
 	public void setCurrentCpuAlloction(int newCpuConfiguration) {
 		//int newCpuAllocation = allocatedCpu.getLast().intValue() + newCpuConfiguration;
 		allocatedCpu.set(allocatedCpu.size()-1, newCpuConfiguration);
 	}
 	
 	//set Memory allocation
 	public void setCurrentMemoryAlloction(int newMemoryConfiguration) {
 		//int newMemoryAllocation = allocatedMemory.getLast().intValue() + newMemoryConfiguration;
 		allocatedMemory.set(allocatedMemory.size()-1, newMemoryConfiguration);
 	}
 
 	//set storage allocation
 	public void setCurrentStorageAlloction(int newStorageConfiguration) {
 		//int newStorageAllocation = allocatedStorage.getLast().intValue() + newStorageConfiguration;
 		allocatedStorage.set(allocatedStorage.size()-1, newStorageConfiguration);
 	}
 	
 	/**
 	 * Retrieves the storage usage produced by the Apps in the VM + overhead 
 	 */
 	public Integer getCurrentStorageUsage() {
 		int storusage = 0 ;
 		for (App a : apps) {
 			storusage += a.getCurrentStorageUsage();
 		}
 		return storusage + getStorageOverhead();
 	}
 	
 	/**
 	 * Retrieves the memory allocated by the Apps in the VM + overhead
 	 */
 	public Integer getCurrentMemoryAllocation() {
 		return allocatedMemory.getLast();
 	}
 	
 	/**
 	 * Retrieves the storage allocated by the Apps in the VM + overhead
 	 */
 	public Integer getCurrentStorageAllocation()  {
 		return allocatedStorage.getLast();
 	}
 	
 	public void nextTick(){
 		toRemoveList = new LinkedList<App>();
 		if (suspendedTicks > 0) {
 			//System.out.println("VM Suspended");
 			suspendedTicks--;
 		} else {
 			if(getActionLock()>0){
 				actionLock--;
 			}
 			this.allocatedCpu.add(getCurrentCpuAllocation());
 			this.allocatedMemory.add(getCurrentMemoryAllocation());
 			this.allocatedStorage.add(getCurrentStorageAllocation());
 			
 			runningTicks++;
 			for (App a: apps) {
 				a.nextTick();
 			}
 			
			boolean test = false;
			if (toRemoveList.size() > 0) {
				System.out.println("remove list = "+ toRemoveList.size() +", execution list: "+ apps.size());
				test = true;
			}
			
 			for (App r: toRemoveList) {
 				apps.remove(r);
 			}
 			
			if (test) {
				System.out.println("new execution list: "+ apps.size());
			}
			
 			if(apps.size()==0) {
 				this.terminate();
 			}
 		}
 	}
 	
 	public App createApp(App a) {
 		this.getApps().add(a);
 		a.setVm(this);
 		return a;
 	}
 	
 	public void terminate() {
 		pm.getToRemoveList().add(this);
 	}
 
 	public List<App> getApps() {
 		return this.apps;
 	}
 	
 	/**
 	 * Get the number of SLA violations of the apps in this VM for the last n ticks
 	 * 
 	 * @param ticks How many ticks to look in the past
 	 * @return number of SLA violations
 	 */
 	public int getNumberOfSlaViolations(int ticks) {
 		int violations = 0;
 		for (App app : this.apps) {
 			violations += app.getNumberOfSlaViolations(ticks);
 		}
 		
 		return violations;
 	}
 	
 	/**
 	 * Sums up all CPU SLAs of all apps on this VM
 	 * 
 	 * @return Combined SLAs with overhead. Can be greater than 100
 	 */
 	public int getCpuSla() {
 		int slas = VirtualMachine.getCpuOverhead();
 		for (App app : this.apps) {
 			slas += app.getCpu();
 		}
 		
 		return slas;
 	}
 	
 	/**
 	 * Sums up all memory SLAs of all apps on this VM
 	 * 
 	 * @return Combined SLAs with overhead. Can be greater than 100
 	 */
 	public int getMemorySla() {
 		int slas = VirtualMachine.getMemoryOverhead();
 		for (App app : this.apps) {
 			slas += app.getMemory();
 		}
 		
 		return slas;
 	}
 	
 	/**
 	 * Sums up all storage SLAs of all apps on this VM
 	 * 
 	 * @return Combined SLAs with overhead. Can be greater than 100
 	 */
 	public int getStorageSla() {
 		int slas = VirtualMachine.getStorageOverhead();
 		for (App app : this.apps) {
 			slas += app.getStorage();
 		}
 		
 		return slas;
 	}
 }
