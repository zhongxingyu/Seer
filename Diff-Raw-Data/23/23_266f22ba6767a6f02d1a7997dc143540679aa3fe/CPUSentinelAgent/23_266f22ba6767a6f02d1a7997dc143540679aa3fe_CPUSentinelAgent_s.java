 
 package com.zanoccio.axirassa.services.sentinel;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.hyperic.sigar.CpuPerc;
 import org.hyperic.sigar.SigarException;
 
 public class CPUSentinelAgent extends AbstractSentinelStatisticsAgent {
 
 	private final ArrayList<CPUStatistic> cpustats = new ArrayList<CPUStatistic>();
 
 
 	@Override
 	public void execute() throws SigarException {
 		cpustats.clear();
 
 		CpuPerc[] cpus = getSigar().getCpuPercList();
 		cpustats.ensureCapacity(cpus.length);
 
 		int cpuid = 0;
 		for (CpuPerc cpu : cpus) {
 			cpustats.add(new CPUStatistic(getMachineID(), getDate(), cpuid, cpu.getSys(), cpu.getUser()));
 			cpuid++;
 		}
 	}
 
 
 	@Override
 	public Collection<CPUStatistic> getStatistics() {
 		return cpustats;
 	}

 }
