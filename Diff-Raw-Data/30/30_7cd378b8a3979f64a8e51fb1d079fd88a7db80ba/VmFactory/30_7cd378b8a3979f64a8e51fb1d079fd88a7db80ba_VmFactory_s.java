 package de.hpi_web.cloudSim.multitier.staticTier;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
 import org.cloudbus.cloudsim.Vm;
 
 public class VmFactory {
 	// default values taken from http://aws.amazon.com/ec2/instance-types/
 	// => Standard instance (small) as of May 2012
 	public static final int  DEFAULT_PES  =       1;		// number of CPUS
 	public static final int  DEFAULT_MIPS =    1200;		// MIPS per CPU
 	public static final int  DEFAULT_RAM  =    1700;  		// vm memory (MB)
 	public static final long DEFAULT_SIZE =  160000; 		// image size (MB)
 	public static final long DEFAULT_BW   =    1000;		// total bandwith available
 	
 	public static final String VMM = "Xen";					// virtual machine manager (hypervisor)
 	//public static final String DEFAULT_NAME = "EC2-Standard(Small)";
 	
 	
 	public static Vm createVm(int brokerId) {
 		return defaultVm(0, brokerId);
 	}

 	public static List<Vm> createVms(int startId, int count, int brokerId) {
 		List<Vm> vms = new ArrayList<Vm>();
 		for (int i = startId; i < startId + count; i++)
 			vms.add(defaultVm(i, brokerId));
 		return vms;
 	}
 	
 	private static Vm defaultVm(int vmid, int brokerId) {
 		return new Vm(vmid, brokerId, DEFAULT_MIPS, DEFAULT_PES, DEFAULT_RAM, DEFAULT_BW, DEFAULT_SIZE, VMM, new CloudletSchedulerTimeShared());
 	}
 	
 	public static Vm customVm(
 			String vmm, 
 			int vmId, 
 			int pesNumber, 
 			int mips, 
 			long size, 
 			int ram, 
 			long bw, 
 			int brokerId) {
 		return new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
 	}
 }
