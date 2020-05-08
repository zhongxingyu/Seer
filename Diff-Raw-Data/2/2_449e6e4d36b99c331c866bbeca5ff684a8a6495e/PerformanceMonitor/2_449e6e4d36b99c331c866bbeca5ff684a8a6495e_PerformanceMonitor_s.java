 package com.thefind.util;
 
 import java.util.*;
 import java.util.concurrent.Semaphore;
 
 import java.lang.management.GarbageCollectorMXBean;
 import java.lang.management.ManagementFactory;
 import java.lang.management.MemoryPoolMXBean;
 import java.lang.management.MemoryType;
 import java.lang.management.MemoryUsage;
 
 import com.sun.management.OperatingSystemMXBean;
 import java.lang.management.ThreadMXBean;
 
 /**
  * Class to monitor the memory usage (average and peak),
  * as well as CPU usage and GC.
  *
  * Very lightweight, because it uses JMX beans, which are updated automatically
  * by the JVM.
  *
  * <code>
  * // Initialize the monitor
  * PerformanceMonitor mon = new PerformanceMonitor();
  * while (...) {
  *   // do some work
  *   mon.measure(); // periodically look at the memory, optional
  *   ...
  * }
  * mon.measure(); // one last measure, optional
  * // print the results
  * System.err.println(mon.toString());
  * </code>
  *
  * @author Eric Gaudet
  */
 public class PerformanceMonitor
 {
   protected final static long MB_SHIFT = 20L;
   protected final static long NANO_TO_MILLI = 1000L*1000L;
 
   protected final List<MemoryPoolMXBean> hbean_;
   protected final int num_mem_;
 
   protected final List<MemoryUsage> heap_start_;
   protected List<MemoryUsage> heap_peak_;
   protected List<MemoryUsage> heap_last_;
 
   protected long heap_committed_sum_;
   protected long heap_used_sum_;
   protected int  heap_cnt_;
 
   protected final List<GarbageCollectorMXBean> gcbeans_;
   protected final int num_gc_;
   protected final long[] gc_time_;
   protected final long[] gc_cnt_;
 
   protected final long time_start_;
   protected long time_last_;
   protected final long cpu_start_;
   protected long cpu_last_;
   protected final int num_cpu_;
 
   protected double load_last_;
   protected final long swap_total_;
   protected long swap_last_;
 
   protected int th_daemon_;
   protected int th_peak_;
   protected int th_count_;
   protected long th_total_started_;
 
   private static Semaphore _measuring = new Semaphore(1);
 
   protected final OperatingSystemMXBean osbean_;
   protected final ThreadMXBean thbean_;
 
   public PerformanceMonitor()
   {
     ArrayList<MemoryPoolMXBean> hb = new ArrayList();
     for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
       if (p.getType() == MemoryType.HEAP) {
         hb.add(p);
       }
     }
     hb.trimToSize();
     hbean_ = hb;
     num_mem_ = hbean_.size();
 
     heap_start_ = new ArrayList(num_mem_);
     heap_peak_  = new ArrayList(num_mem_);
     heap_last_  = new ArrayList(num_mem_);
 
     heap_committed_sum_ = 0;
     heap_used_sum_ = 0;
 
     for (int i=0 ; i<num_mem_ ; i++) {
       MemoryPoolMXBean mb = hbean_.get(i);
       mb.resetPeakUsage();
       heap_peak_.add(mb.getPeakUsage());
 
       MemoryUsage mu = mb.getUsage();
       heap_start_.add(mu);
       heap_last_.add(mu);
       heap_committed_sum_ += mu.getCommitted()>>>MB_SHIFT;
       heap_used_sum_ += mu.getUsed()>>>MB_SHIFT;
     }
     heap_cnt_ = 1;
 
     gcbeans_ = ManagementFactory.getGarbageCollectorMXBeans();
     num_gc_ = gcbeans_.size();
     gc_time_ = new long[num_gc_];
     gc_cnt_  = new long[num_gc_];
     for (int i=0 ; i<num_gc_ ; i++) {
       GarbageCollectorMXBean gc = gcbeans_.get(i);
       gc_time_[i] = gc.getCollectionTime();
       gc_cnt_[i]  = gc.getCollectionCount();
     }
 
     osbean_ = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
     cpu_start_  = osbean_.getProcessCpuTime();
     cpu_last_   = cpu_start_;
     num_cpu_    = osbean_.getAvailableProcessors();
     load_last_  = osbean_.getSystemLoadAverage();
     swap_total_ = osbean_.getTotalSwapSpaceSize();
     swap_last_  = osbean_.getFreeSwapSpaceSize();
 
     thbean_ = (ThreadMXBean) ManagementFactory.getThreadMXBean();
     th_daemon_ = thbean_.getDaemonThreadCount();
     th_peak_   = thbean_.getPeakThreadCount();
     th_count_  = thbean_.getThreadCount();
     th_total_started_ = thbean_.getTotalStartedThreadCount();
 
     time_start_ = System.currentTimeMillis();
     time_last_  = time_start_;
   }
 
   /**
    * Take a measure of the cpu and memory average and peak.
    * Max 1 measure every 10 seconds.
    * @return whether the measure was taken or not
    */
   public boolean measure()
   {
     if (_measuring.tryAcquire()) {
       try {
         long now = System.currentTimeMillis();
         if ((now - time_last_) > (1000L*10)) { // 10 seconds
           time_last_ = now;
           cpu_last_  = osbean_.getProcessCpuTime();
           load_last_  = osbean_.getSystemLoadAverage();
           swap_last_  = osbean_.getFreeSwapSpaceSize();
 
           th_daemon_ = thbean_.getDaemonThreadCount();
           th_peak_   = thbean_.getPeakThreadCount();
           th_count_  = thbean_.getThreadCount();
           th_total_started_ = thbean_.getTotalStartedThreadCount();
 
           heap_peak_.clear();
           heap_last_.clear();
           heap_cnt_ ++;
           for (int i=0 ; i<num_mem_ ; i++) {
             MemoryPoolMXBean mb = hbean_.get(i);
             heap_peak_.add(mb.getPeakUsage());
 
             MemoryUsage mu = mb.getUsage();
             heap_last_.add(mu);
             heap_committed_sum_ += mu.getCommitted()>>>MB_SHIFT;
             heap_used_sum_ += mu.getUsed()>>>MB_SHIFT;
           }
           return true;
         }
       }
       finally {
         _measuring.release();
       }
     }
     return false;
   }
 
   protected int getUsed(List<MemoryUsage> mem)
   {
     int ret = 0;
     for (int i=0 ; i<mem.size() ; i++) {
       ret += mem.get(i).getUsed()>>>MB_SHIFT;
     }
     return ret;
   }
 
   protected int getCommitted(List<MemoryUsage> mem)
   {
     int ret = 0;
     for (int i=0 ; i<mem.size() ; i++) {
       ret += mem.get(i).getCommitted()>>>MB_SHIFT;
     }
     return ret;
   }
 
   public long getHeapCommittedAverage()
   { return heap_committed_sum_/heap_cnt_; }
 
   public long getHeapUsedAverage()
   { return heap_used_sum_/heap_cnt_; }
 
   public long getHeapCommittedStart()
   { return getCommitted(heap_start_); }
 
   public long getHeapUsedStart()
   { return getUsed(heap_start_); }
 
   public long getHeapCommittedLast()
   { return getCommitted(heap_last_); }
 
   public long getHeapUsedLast()
   { return getUsed(heap_last_); }
 
   public long getHeapCommittedPeak()
   { return getCommitted(heap_peak_); }
 
   public long getHeapUsedPeak()
   { return getUsed(heap_peak_); }
 
   public double getLoad()
   { return load_last_ / num_cpu_; }
 
   public double getSwap()
   { return 1.0 - (1.0 * swap_last_ / swap_total_); }
 
   public int getNumGc()
   { return num_gc_; }
 
   public long getGcCount(int i)
   { return gcbeans_.get(i).getCollectionCount() - gc_cnt_[i]; }
 
   public long getGcTime(int i)
   { return gcbeans_.get(i).getCollectionTime() - gc_time_[i]; }
 
   public long getGcTime()
   {
     long ret = 0L;
     for (int i=0 ; i<num_gc_ ; i++) {
       ret += gcbeans_.get(i).getCollectionTime() - gc_time_[i];
     }
     return ret;
   }
 
   public long getCpuTime()
   { return (cpu_last_ - cpu_start_)/NANO_TO_MILLI; }
 
   public double getCpuUsage()
   { return 1.0*(cpu_last_ - cpu_start_)/NANO_TO_MILLI/getRunningTime()/num_cpu_; }
 
   public long getRunningTime()
   { return time_last_ - time_start_; }
 
   @Override
   public String toString()
   {
     if (heap_cnt_==1) {
       return String.format("[PerformanceMonitor] Heap: %,dMB / %,dMB ", getHeapUsedStart()>>>MB_SHIFT, getHeapCommittedStart()>>>MB_SHIFT);
     }
     else {
       long runtime = getRunningTime();
       StringBuilder sb = new StringBuilder("[PerformanceMonitor] ");
       sb.append(String.format(  "Start: %,dMB / %,dMB",   getHeapUsedStart(),   getHeapCommittedStart()))
         .append(String.format(", Average: %,dMB / %,dMB", getHeapUsedAverage(), getHeapCommittedAverage()))
         .append(String.format(", Peak: %,dMB / %,dMB",    getHeapUsedPeak(),    getHeapCommittedPeak()))
        .append(String.format(", Last: %,dMB / %,dMB",     getHeapUsedLast(),    getHeapCommittedLast()));
       long total_gc = 0L;
       for (int i=0 ; i<num_gc_ ; i++) {
         long gctime = getGcTime(i);
         if (gctime>0) {
           total_gc += gctime;
           GarbageCollectorMXBean gc = gcbeans_.get(i);
           sb.append("; GC \"")
             .append(gc.getName())
             .append(String.format("\" count: %,d", getGcCount(i)))
             .append(", time: ")
             .append(StringUtil.readableTime(gctime))
             .append(String.format(" (%1.2f%%)", (100.0*gctime/runtime)));
         }
       }
       sb.append(String.format("; Threads: %,d+%,dd (peak:%,d, started: %,d)",
                 th_count_-th_daemon_, th_daemon_, th_peak_, th_total_started_))
         .append(String.format("; Load: %1.3f", getLoad()))
         .append(String.format("; Swap: %1.2f%%", 100.0*getSwap()))
         .append(String.format("; Total GC: %1.2f%%", 100.0*total_gc/runtime))
         .append("; CPUx")
         .append(num_cpu_)
         .append(String.format(": %1.2f%%; ", 100.0*getCpuUsage()))
         .append(String.format("%,d", heap_cnt_))
         .append(" measures")
         .append("; Running time: ")
         .append(StringUtil.readableTime(runtime));
 
       return sb.toString();
     }
   }
 }
 
