 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.monitoring;
 
 import java.util.Collection;
 import java.util.Map;
 import net.wgr.xenmaster.api.VM;
 import net.wgr.xenmaster.api.VMMetrics;
 import org.apache.commons.math.MathException;
 import org.apache.commons.math.distribution.NormalDistributionImpl;
 import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 6, 2011
  * @author double-u
  */
 public class Record {
 
     protected double CPUusage;
     protected int memoryUsage;
     protected int memoryTotal;
     protected String reference;
     protected boolean vm;
 
     public Record(float CPUusage, int memoryUsage, int memoryTotal, String ref, boolean isVM) {
         this.CPUusage = CPUusage;
         this.memoryTotal = memoryTotal;
         this.memoryUsage = memoryUsage;
         this.reference = ref;
         this.vm = isVM;
     }
 
     public Record(String ref, boolean isVM) {
         this.reference = ref;
         this.vm = isVM;
 
         VM vm = null;
         VMMetrics vmr = null;
         if (isVM) {
             vm = new VM(ref);
             vmr = vm.getMetrics();
         }
 
         Map<Integer, Double> utils = vmr.getVCPUutilisation();
         applyStatistics(utils.values());
 
         memoryUsage = (vmr.getActualMemory() / 1024*1024);
        memoryTotal = (vm.getMaximumDynamicMemory() / 1024*1024);
 
         // In dom0, all memory is used by default
         if (memoryUsage / memoryTotal > 0.9 && isVM) {
             Logger.getLogger(getClass()).info("VM " + reference + " has less than 10% free memory and will start swapping, causing a severe performance impact");
         }
     }
 
     protected final void applyStatistics(Collection<Double> values) {
         // Let's get statistical
         DescriptiveStatistics ds = new DescriptiveStatistics();
 
         for (double util : values) {
             ds.addValue(util);
         }
 
         CPUusage = ds.getMean();
         double stdDev = ds.getStandardDeviation();
 
         // TODO: actually test this and generate warning
         // Check if all vCPUs have a fair load, e.g. [45, 60, 50] would be fair, [90, 4, 2] indicates you should learn threading
         if (stdDev > 0.8) {
             Logger.getLogger(getClass()).info((vm ? "VM" : "Host") + " " + reference + " has an unfair load distribution");
         }
 
         if (stdDev > 0) {
             try {
                 NormalDistributionImpl ndi = new NormalDistributionImpl(ds.getMean(), stdDev);
                 double cp = ndi.cumulativeProbability(90);
                 if (cp > 0.8) {
                     // 80% of the CPUs have a >90% load
                     // TODO warning
                     Logger.getLogger(getClass()).info((vm ? "VM" : "Host") + " " + reference + " has a load >=90% on 80% of the available CPUs");
                 }
             } catch (MathException ex) {
                 Logger.getLogger(getClass()).error("Flawed maths", ex);
             }
         }
     }
 }
