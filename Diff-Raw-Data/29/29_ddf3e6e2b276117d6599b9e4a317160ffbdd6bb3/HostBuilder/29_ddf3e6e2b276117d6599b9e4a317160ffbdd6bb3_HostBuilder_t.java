 package de.hpi_web.cloudSim.profiling.builders;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cloudbus.cloudsim.Host;
 import org.cloudbus.cloudsim.Pe;
 import org.cloudbus.cloudsim.VmSchedulerTimeShared;
 import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
 import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
 import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
 
 public class HostBuilder {
 	
 	/* Host Configuration */
 	public static final int  DEFAULT_PES  =       4;		// number of CPUS
 	public static final int  DEFAULT_MIPS =    1200;		// MIPS per CPU
 	public static final int  DEFAULT_RAM  =    4096;  		// vm memory (MB)
 	public static final long DEFAULT_STORAGE = 1600000; 	// image size (MB)
 	public static final long DEFAULT_BW   =   10000;		// total bandwith available, 10GBit/s
 	
 	private static int hostIdCounter = 0;
 	
 //	private RamProvisioner ramProvisioner;
 //	private BwProvisioner bwProvisioner;
 	private int pes;
 	private int mips;
 	private int ram;
 	private long storage;
 	private long bandwidth;
 //	private List<Pe> peList;
 //	private VmScheduler vmScheduler;
 	private int hostId;
 	
 	public HostBuilder(int hostId) {
 		setDefaults();
 		this.hostId = hostId;
 	}
 	
 	public HostBuilder() {
 		setDefaults();
 	}
 	
 	Host build() {
		hostId = hostIdCounter;
 		List<Pe> peList = buildPeList();
 		Host host = new Host(
 				hostId,
 				new RamProvisionerSimple(ram),
 				new BwProvisionerSimple(bandwidth),
 				storage,
 				peList,
 				new VmSchedulerTimeShared(peList)
 			);
 		hostIdCounter++;
 		return host;
 	}
 	
 	private List<Pe> buildPeList() {
 		List<Pe> peList = new ArrayList<Pe>();
 		for (int i = 0; i < pes; i++)
 			peList.add(new Pe(i, new PeProvisionerSimple(mips)));
 		
 		return peList;
 	}
 
 	public int getPes() {
 		return pes;
 	}
 
 	public HostBuilder setPes(int pes) {
 		this.pes = pes;
 		return this;
 	}
 
 	public int getMips() {
 		return mips;
 	}
 
 	public HostBuilder setMips(int mips) {
 		this.mips = mips;
 		return this;
 	}
 
 	public int getRam() {
 		return ram;
 	}
 
 	public HostBuilder setRam(int ram) {
 		this.ram = ram;
 		return this;
 	}
 
 	public long getStorage() {
 		return storage;
 	}
 
 	public HostBuilder setStorage(long storage) {
 		this.storage = storage;
 		return this;
 	}
 
 	public long getBandWidth() {
 		return bandwidth;
 	}
 
 	public HostBuilder setBandwidth(long bandwidth) {
 		this.bandwidth = bandwidth;
 		return this;
 	}
 	
 	private void setDefaults() {
 		this.hostId = -1;
 		this.pes = DEFAULT_PES;
 		this.mips = DEFAULT_MIPS;
 		this.ram = DEFAULT_RAM;
 		this.storage = DEFAULT_STORAGE;
 		this.bandwidth = DEFAULT_BW;
 	}
 }
