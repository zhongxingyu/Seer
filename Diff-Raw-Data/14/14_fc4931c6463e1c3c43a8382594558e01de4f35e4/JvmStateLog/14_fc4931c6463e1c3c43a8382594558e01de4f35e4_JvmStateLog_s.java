 //
 // A Java VM status Watcher for Fluent
 //
 // Copyright (C) 2013 - 2013 Masayuki Miyake
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package org.fluentd.jvmwatcher.data;
 
 import java.io.IOException;
 import java.lang.management.ClassLoadingMXBean;
 import java.lang.management.CompilationMXBean;
 import java.lang.management.GarbageCollectorMXBean;
 import java.lang.management.MemoryMXBean;
 import java.lang.management.MemoryUsage;
 import java.lang.management.OperatingSystemMXBean;
 import java.lang.management.RuntimeMXBean;
 import java.lang.management.ThreadMXBean;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fluentd.jvmwatcher.proxy.JvmClientProxy;
 import org.fluentd.jvmwatcher.proxy.MemoryPoolClientProxy;
 
 /**
  * @author miyake
  *
  */
 public final class JvmStateLog
 {
     private static  Log log = LogFactory.getLog(JvmStateLog.class);
 
     /**
     *
     */
    public enum ProcessState {
        /**
         * 
         */
        START_PROCESS,
        /**
         * 
         */
        LIVE_PROCESS,
        /**
         * 
         */
        END_PROCESS
    }
    
    /**
     * 
     */
    private ProcessState    procState_ = ProcessState.LIVE_PROCESS;
     
     /**
      * 
      */
     private long        logDateTime_ = 0L;
     
     // ClassLoadingMXBean
     private int         classLoadedCount_ = -1;
     private long        classUnloadedCount_ = -1L;
     private long        classTotalLoadedCount_ = -1L;
     // CompilationMXBean
     private long        compileTime_ = -1L;
     // MemoryMXBean
     private MemoryUsage heapSize_ = null;
     private MemoryUsage notheapSize_ = null;
     private int         pendingFinalizationCount_ = -1;
     // ThreadMXBean
     private int         threadCount_ = -1;
     private int         daemonThreadCount_ = -1;
     private int         peakThreadCount_ = -1;
     // OperatingSystemMXBean
     private int         osAvailableProcessors_ = 0;
     private double      osSystemLoadAverage_ = 0.0;
     // com.sun.management.OperatingSystemMXBean
     private long        committedVirtualMemorySize_ = -1L;
     private long        freePhysicalMemorySize_ = -1L;
     private long        freeSwapSpaceSize_ = -1L;
     private long        processCpuTime_ = -1L;
     private long        totalPhysicalMemorySize_ = -1L;
     private long        totalSwapSpaceSize_ = -1L;
     // RuntimeMXBean
     private long        jvmUpTime_ = -1L;
     // MemoryPoolMXBean
    private Collection<MemoryPoolState>         memoryPoolStateColl_ = new ArrayList<MemoryPoolState>();;
     // GarbageCollectorMXBean
    private Collection<GarbageCollectorState>   gcCollectorState_ = new ArrayList<GarbageCollectorState>();;
     
     // CPU usage
     private float       cpuUsage_ = 0.0F;
 
     /**
      * Constructor<BR>
      * This constructor uses only by the unit test.
      * 
      * @param procState
      * @param logDateTime
      * @param classLoadedCount
      * @param classUnloadedCount
      * @param classTotalLoadedCount
      * @param compileTime
      * @param heapSize
      * @param notheapSize
      * @param pendingFinalizationCount
      * @param threadCount
      * @param daemonThreadCount
      * @param peakThreadCount
      * @param osAvailableProcessors
      * @param osSystemLoadAverage
      * @param committedVirtualMemorySize
      * @param freePhysicalMemorySize
      * @param freeSwapSpaceSize
      * @param processCpuTime
      * @param totalPhysicalMemorySize
      * @param totalSwapSpaceSize
      * @param jvmUpTime
      * @param memoryPoolStateColl
      * @param gcCollectorState
      * @param cpuUsage
      */
     private JvmStateLog(ProcessState procState,
                         long logDateTime,    
                         int classLoadedCount,
                         long classUnloadedCount,
                         long classTotalLoadedCount,
                         long compileTime,
                         MemoryUsage heapSize,
                         MemoryUsage notheapSize,
                         int pendingFinalizationCount,
                         int threadCount,
                         int daemonThreadCount,
                         int peakThreadCount,
                         int osAvailableProcessors,
                         double osSystemLoadAverage,
                         long committedVirtualMemorySize,
                         long freePhysicalMemorySize,
                         long freeSwapSpaceSize,
                         long processCpuTime,
                         long totalPhysicalMemorySize,
                         long totalSwapSpaceSize,
                         long jvmUpTime,
                         Collection<MemoryPoolState> memoryPoolStateColl,
                         Collection<GarbageCollectorState> gcCollectorState,
                         float cpuUsage)
     {
         this.procState_ = procState;
         this.logDateTime_ = logDateTime;
         this.classLoadedCount_ = classLoadedCount;
         this.classUnloadedCount_ = classUnloadedCount;
         this.classTotalLoadedCount_ = classTotalLoadedCount;
         this.compileTime_ = compileTime;
         this.heapSize_ = heapSize;
         this.notheapSize_ = notheapSize;
         this.pendingFinalizationCount_ = pendingFinalizationCount;
         this.threadCount_ = threadCount;
         this.daemonThreadCount_ = daemonThreadCount;
         this.peakThreadCount_ = peakThreadCount;
         this.osAvailableProcessors_ = osAvailableProcessors;
         this.osSystemLoadAverage_ = osSystemLoadAverage;
         this.committedVirtualMemorySize_ = committedVirtualMemorySize;
         this.freePhysicalMemorySize_ = freePhysicalMemorySize;
         this.freeSwapSpaceSize_ = freeSwapSpaceSize;
         this.processCpuTime_ = processCpuTime;
         this.totalPhysicalMemorySize_ = totalPhysicalMemorySize;
         this.totalSwapSpaceSize_ = totalSwapSpaceSize;
         this.jvmUpTime_ = jvmUpTime;
         this.memoryPoolStateColl_ = memoryPoolStateColl;
         this.gcCollectorState_ = gcCollectorState;
         this.cpuUsage_ = cpuUsage;
     }
     
     /**
      * Default Constructor
      */
     private JvmStateLog()
     {
         
     }
     
     /**
      * @param clientPrixy
      * @return
      */
     public static JvmStateLog makeJvmStateLog(JvmClientProxy clientProxy)
     {
         JvmStateLog     ret = new JvmStateLog();
 
         try
         {
             // set log time
             ret.logDateTime_ = System.currentTimeMillis();
             
             // ClassLoadingMXBean
             ClassLoadingMXBean  classLoadingBean = clientProxy.getClassLoadingMXBean();
             if (null != classLoadingBean)
             {
                 ret.classLoadedCount_ = classLoadingBean.getLoadedClassCount();
                 ret.classUnloadedCount_ = classLoadingBean.getUnloadedClassCount();
                 ret.classTotalLoadedCount_ = classLoadingBean.getTotalLoadedClassCount();
             }
 
             // CompilationMXBean
             CompilationMXBean   compilationBean = clientProxy.getCompilationMXBean();
             if (null != compilationBean)
             {
                 ret.compileTime_ = compilationBean.getTotalCompilationTime();
             }
 
             // MemoryMXBean
             MemoryMXBean  memoryBean = clientProxy.getMemoryMXBean();
             if (null != memoryBean)
             {
                 ret.heapSize_ = memoryBean.getHeapMemoryUsage();
                 ret.notheapSize_ = memoryBean.getNonHeapMemoryUsage();
                 ret.pendingFinalizationCount_ = memoryBean.getObjectPendingFinalizationCount();
             }
 
             // ThreadMXBean
             ThreadMXBean  threadBean = clientProxy.getThreadMXBean();
             if (null != threadBean)
             {
                 ret.threadCount_ = threadBean.getThreadCount();
                 ret.daemonThreadCount_ = threadBean.getDaemonThreadCount();
                 ret.peakThreadCount_ = threadBean.getPeakThreadCount();
             }
 
             // OperatingSystemMXBean
             OperatingSystemMXBean  OpeSysBean = clientProxy.getOperatingSystemMXBean();
             if (null != OpeSysBean)
             {
                 ret.osAvailableProcessors_ = OpeSysBean.getAvailableProcessors();
                 ret.osSystemLoadAverage_ = OpeSysBean.getSystemLoadAverage();
             }
 
             // com.sun.management.OperatingSystemMXBean
             com.sun.management.OperatingSystemMXBean  sunOpeSysBean = clientProxy.getSunOperatingSystemMXBean();
             if (null != sunOpeSysBean)
             {
                 ret.committedVirtualMemorySize_ = sunOpeSysBean.getCommittedVirtualMemorySize();
                 ret.freePhysicalMemorySize_ = sunOpeSysBean.getFreePhysicalMemorySize();
                 ret.freeSwapSpaceSize_ = sunOpeSysBean.getFreeSwapSpaceSize();
                 ret.processCpuTime_ = sunOpeSysBean.getProcessCpuTime();
                 ret.totalPhysicalMemorySize_ = sunOpeSysBean.getTotalPhysicalMemorySize();
                 ret.totalSwapSpaceSize_ = sunOpeSysBean.getTotalSwapSpaceSize();
             }
 
             // RuntimeMXBean
             RuntimeMXBean  runtimeBean = clientProxy.getRuntimeMXBean();
             if (null != runtimeBean)
             {
                 ret.jvmUpTime_ = runtimeBean.getUptime();
             }
 
             // MemoryPoolMXBean
             Collection<MemoryPoolClientProxy>  memoryPoolBeansColl = clientProxy.getMemoryPoolClientProxies();
             if (null != memoryPoolBeansColl)
             {
                 for (MemoryPoolClientProxy elem : memoryPoolBeansColl)
                 {
                     if (null != elem)
                     {
                         MemoryPoolState     state = elem.getStat();
                         if (null != state)
                         {
                             // add MemoryPoolState
                             ret.memoryPoolStateColl_.add(state);
                         }
                     }
                  }
             }
 
             // GarbageCollectorMXBean
             Collection<GarbageCollectorMXBean>  garbageCollBeansColl = clientProxy.getGarbageCollectorMXBeans();
             if (null != garbageCollBeansColl)
             {
                 for (GarbageCollectorMXBean elem : garbageCollBeansColl)
                 {
                     if (null != elem)
                     {
                         long    collectionCount = elem.getCollectionCount();
                         long    collectionTime = elem.getCollectionTime();
                         String  memoryManagerName = elem.getName();
                         GarbageCollectorState   state = new GarbageCollectorState(memoryManagerName, collectionCount, collectionTime);
                         // add GarbageCollectorState
                         ret.gcCollectorState_.add(state);
                     }
                 }
             }
         }
         catch (IOException ex)
         {
             log.error(ex);
             // close JvmClientProxy
             clientProxy.disconnect();
         }
         catch (Exception ex)
         {
             log.error(ex);
             // close JvmClientProxy
             clientProxy.disconnect();
         }
         
         return ret;
     }
 
     /**
      * @param procState
      */
     public void setProcState(ProcessState procState)
     {
         this.procState_ = procState;
     }
 
     /**
      * @return procState
      */
     public ProcessState getProcState()
     {
         return procState_;
     }
     
     /**
      * @param cpuUsage
      */
     public void setCpuUsage(float cpuUsage)
     {
         this.cpuUsage_ = cpuUsage;
     }
     
     /**
      * @return
      */
     public float getCpuUsage()
     {
         return this.cpuUsage_;
     }
 
     /**
      * @return logDateTime
      */
     public long getLogDateTime()
     {
         return logDateTime_;
     }
 
     /**
      * @return classLoadedCount
      */
     public int getClassLoadedCount()
     {
         return classLoadedCount_;
     }
 
     /**
      * @return classUnloadedCount
      */
     public long getClassUnloadedCount()
     {
         return classUnloadedCount_;
     }
 
     /**
      * @return classTotalLoadedCount
      */
     public long getClassTotalLoadedCount()
     {
         return classTotalLoadedCount_;
     }
 
     /**
      * @return compileTime
      */
     public long getCompileTime()
     {
         return compileTime_;
     }
 
     /**
      * @return heapSize
      */
     public MemoryUsage getHeapSize()
     {
         return heapSize_;
     }
 
     /**
      * @return notheapSize
      */
     public MemoryUsage getNotheapSize()
     {
         return notheapSize_;
     }
 
     /**
      * @return pendingFinalizationCount
      */
     public int getPendingFinalizationCount_()
     {
         return pendingFinalizationCount_;
     }
 
     /**
      * @return osAvailableProcessors
      */
     public int getOsAvailableProcessors()
     {
         return osAvailableProcessors_;
     }
 
     /**
      * @return osSystemLoadAverage
      */
     public double getOsSystemLoadAverage()
     {
         return osSystemLoadAverage_;
     }
 
     /**
      * @return committedVirtualMemorySize
      */
     public long getCommittedVirtualMemorySize()
     {
         return committedVirtualMemorySize_;
     }
 
     /**
      * @return freePhysicalMemorySize
      */
     public long getFreePhysicalMemorySize()
     {
         return freePhysicalMemorySize_;
     }
 
     /**
      * @return freeSwapSpaceSize
      */
     public long getFreeSwapSpaceSize()
     {
         return freeSwapSpaceSize_;
     }
 
     /**
      * @return processCpuTime
      */
     public long getProcessCpuTime()
     {
         return processCpuTime_;
     }
 
     /**
      * @return totalPhysicalMemorySize
      */
     public long getTotalPhysicalMemorySize()
     {
         return totalPhysicalMemorySize_;
     }
 
     /**
      * @return totalSwapSpaceSize
      */
     public long getTotalSwapSpaceSize()
     {
         return totalSwapSpaceSize_;
     }
 
     /**
      * @return jvmUpTime
      */
     public long getJvmUpTime()
     {
         return jvmUpTime_;
     }
     
     /**
      * @return threadCount
      */
     public int getThreadCount()
     {
         return threadCount_;
     }
 
     /**
      * @return daemonThreadCount
      */
     public int getDaemonThreadCount()
     {
         return daemonThreadCount_;
     }
 
     /**
      * @return peakThreadCount
      */
     public int getPeakThreadCount()
     {
         return peakThreadCount_;
     }
 
     /**
      * @return memoryPoolStateColl
      */
     public Collection<MemoryPoolState> getMemoryPoolStateCollection()
     {
         return memoryPoolStateColl_;
     }
 
     /**
      * @return gcCollectorState
      */
     public Collection<GarbageCollectorState> getGcStateCollection()
     {
         return gcCollectorState_;
     }
     
 }
