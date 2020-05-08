 package at.ac.tuwien.lsdc.mape;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 import at.ac.tuwien.lsdc.generator.RequestGenerator;
 import at.ac.tuwien.lsdc.resources.App;
 import at.ac.tuwien.lsdc.resources.PhysicalMachine;
 import at.ac.tuwien.lsdc.resources.VirtualMachine;
 
 public class Monitor {
 	private int globalTicks =0;
 	private PrintWriter pmLog;
 	private PrintWriter vmLog;
 	private PrintWriter appLog;
 	private UUID executionUuid;
 	
 	public int getGlobalTicks() {
 		return globalTicks;
 	}
 
 	public void setGlobalTicks(int globalTicks) {
 		this.globalTicks = globalTicks;
 	}
 
 	private List<PhysicalMachine> pms = new LinkedList<PhysicalMachine>();
 	
 	static volatile Monitor instance;
 	
 	private Monitor() {
 		try {
 			executionUuid = UUID.randomUUID();
 			pmLog = new PrintWriter("log/pmlog.txt");
 			vmLog = new PrintWriter("log/vmlog.txt");
 			appLog = new PrintWriter("log/applog.txt");
 			pmLog.append(getLogHeader());
 			vmLog.append(getLogHeader());
 			appLog.append(getLogHeader());
 			
 		} catch (IOException e) {
 		
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static Monitor getInstance() {
 		if (Monitor.instance == null) {
 			synchronized (Monitor.class) {
 				if (Monitor.instance == null) {
 					Monitor.instance = new Monitor();
 				}
 			}
 		}
 		
 		return instance;
 	}
 	
 	public void nextTick() {
 		this.globalTicks++;
 		for (PhysicalMachine pm : pms){
 			pm.nextTick();
 		}
 	}
 	
 	public void getNewStati() {
 		// updates PMs, which update VMs, which update Apps
 		for (PhysicalMachine pm : this.pms) {
 			pm.nextTick();
 		}
 	}
 
 	public List<PhysicalMachine> getPms() {
 		return pms;
 	}
 
 	public void setPms(List<PhysicalMachine> pms) {
 		this.pms = pms;
 	}
 	
 	public void addPm(PhysicalMachine pm) {
 		this.pms.add(pm);
 	}
 
 	public void logSystemStatus() throws IOException {
 		for (PhysicalMachine pm : pms) {
 			StringBuffer sb = new StringBuffer();
 			sb.append(executionUuid); 
 			sb.append(";");
 			sb.append(pm.getResourceId()); 
 			sb.append(";");
 			sb.append(";");
 			sb.append(globalTicks); 
 			sb.append(";");
 			sb.append(pm.getRunningTicks()); 
 			sb.append(";");
 			sb.append(pm.getSuspendedTicks()); 
 			sb.append(";");
 			sb.append(pm.getCurrentCpuAllocation()); 
 			sb.append(";");
 			sb.append(pm.getCurrentMemoryAllocation()); 
 			sb.append(";");
 			sb.append(pm.getCurrentStorageAllocation()); 
 			sb.append(";");
 			sb.append(pm.getCurrentCpuUsage()); 
 			sb.append(";");
 			sb.append(pm.getCurrentMemoryUsage()); 
 			sb.append(";");
 			sb.append(pm.getCurrentStorageUsage()); 
 
 			pmLog.println(sb.toString());
 			
 			for (VirtualMachine vm : pm.getVms()) {
 				sb = new StringBuffer();
 				sb.append(executionUuid); 
 				sb.append(";");
 				sb.append(vm.getResourceId()); 
 				sb.append(";");
 				sb.append(vm.getPm().getResourceId()); 
 				sb.append(";");
 				sb.append(globalTicks); 
 				sb.append(";");
 				sb.append(vm.getRunningTicks()); 
 				sb.append(";");
 				sb.append(vm.getSuspendedTicks()); 
 				sb.append(";");
 				sb.append(vm.getCurrentCpuAllocation()); 
 				sb.append(";");
 				sb.append(vm.getCurrentMemoryAllocation()); 
 				sb.append(";");
 				sb.append(vm.getCurrentStorageAllocation()); 
 				sb.append(";");
 				sb.append(vm.getCurrentCpuUsage()); 
 				sb.append(";");
 				sb.append(vm.getCurrentMemoryUsage()); 
 				sb.append(";");
 				sb.append(vm.getCurrentStorageUsage()); 
 				
 				vmLog.println(sb.toString());
 				
 				for(App a : vm.getApps()){
 					sb = new StringBuffer();
 					sb.append(executionUuid); 
 					sb.append(";");
 					sb.append(a.getResourceId()); 
 					sb.append(";");
 					sb.append(a.getVm().getResourceId()); 
 					sb.append(";");
 					sb.append(globalTicks); 
 					sb.append(";");
 					sb.append(a.getRunningTicks()); 
 					sb.append(";");
 					sb.append(a.getSuspendedTicks()); 
 					sb.append(";");
 					sb.append(a.getCurrentCpuAllocation()); 
 					sb.append(";");
 					sb.append(a.getCurrentMemoryAllocation()); 
 					sb.append(";");
 					sb.append(a.getCurrentStorageAllocation()); 
 					sb.append(";");
 					sb.append(a.getCurrentCpuUsage()); 
 					sb.append(";");
 					sb.append(a.getCurrentMemoryUsage()); 
 					sb.append(";");
 					sb.append(a.getCurrentStorageUsage()); 
 					
 					appLog.println(sb.toString());
 				}
 			}
 		}
 		
 	}
 	
 	private String getLogHeader() {
 		return "ExecutionID;ID;RootID;GlobalTick;RunningTicks;SuspendedTicks;AllocatedCpu;AllocatedMemory;AllocatedStorage;UsedCpu;UsedMemory;UsedStorage";
 	}
 	
 }
