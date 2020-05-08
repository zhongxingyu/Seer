 package de.hpi_web.cloudSim.profiling.builders;
 
 import org.cloudbus.cloudsim.CloudletScheduler;
 import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
 import org.cloudbus.cloudsim.Vm;
 
 public class VmBuilder {
 	// default values taken from http://aws.amazon.com/ec2/instance-types/
 	// => Standard instance (small) as of May 2012
 	public static final int  DEFAULT_PES  =       1;		// number of CPUS
 	public static final int  DEFAULT_MIPS =    1200;		// MIPS per CPU
 	public static final int  DEFAULT_RAM  =    1700;  		// vm memory (MB)
 	public static final long DEFAULT_SIZE =  160000; 		// image size (MB)
 	public static final long DEFAULT_BW   =    1000;		// total bandwith available
 	
 	public static final String VMM = "Xen";					// virtual machine manager (hypervisor)
	
	private static int idCounter = 0;
 
 	private int id;
 	private int userId;
 	private double mips;
 	private int pes;
 	private int ram;
 	private long bandwidth;
 	private long size;
 	private String vmm;
 	private CloudletScheduler cloudletScheduler;
 
 
 	VmBuilder(int id) {
 		this.id = id;
 		this.pes = DEFAULT_PES;
 		this.mips = DEFAULT_MIPS;
 		this.ram = DEFAULT_RAM;
 		this.size = DEFAULT_SIZE;
 		this.bandwidth = DEFAULT_BW;
 		this.cloudletScheduler = new CloudletSchedulerTimeShared();
	}	
	
	VmBuilder() {
		this.id = -1;
		this.pes = DEFAULT_PES;
		this.mips = DEFAULT_MIPS;
		this.ram = DEFAULT_RAM;
		this.size = DEFAULT_SIZE;
		this.bandwidth = DEFAULT_BW;
		this.cloudletScheduler = new CloudletSchedulerTimeShared();
 	}
 	
 	Vm build() {
		if (id < 0)
			id = idCounter;
 		return new Vm(id, userId, mips, pes, ram, bandwidth, size, vmm, cloudletScheduler);
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public VmBuilder setId(int id) {
 		this.id = id;
 		return this;
 	}
 
 	public int getUserId() {
 		return userId;
 	}
 
 	public VmBuilder setUserId(int userId) {
 		this.userId = userId;
 		return this;
 	}
 
 	public double getMips() {
 		return mips;
 	}
 
 	public VmBuilder setMips(double mips) {
 		this.mips = mips;
 		return this;
 	}
 
 	public int getPes() {
 		return pes;
 	}
 
 	public VmBuilder setPes(int pes) {
 		this.pes = pes;
 		return this;
 	}
 
 	public int getRam() {
 		return ram;
 	}
 
 	public VmBuilder setRam(int ram) {
 		this.ram = ram;
 		return this;
 	}
 
 	public long getBandwidth() {
 		return bandwidth;
 	}
 
 	public VmBuilder setBandwidth(long bandwidth) {
 		this.bandwidth = bandwidth;
 		return this;
 	}
 
 	public long getSize() {
 		return size;
 	}
 
 	public VmBuilder setSize(long size) {
 		this.size = size;
 		return this;
 	}
 }
