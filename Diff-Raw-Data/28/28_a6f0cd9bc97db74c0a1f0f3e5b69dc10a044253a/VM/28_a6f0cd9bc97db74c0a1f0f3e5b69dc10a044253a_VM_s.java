 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.controller.Controller;
 import org.apache.commons.collections.CollectionUtils;
 
 /**
  * 
  * @created Oct 2, 2011
  * @author double-u
  */
 public class VM extends NamedEntity {
 
     @ConstructorArgument
     protected int userVersion = 1;
     @ConstructorArgument
     protected ShutdownAction actionsAfterReboot = ShutdownAction.RESTART;
     @ConstructorArgument
     protected ShutdownAction actionsAfterShutdown = ShutdownAction.DESTROY;
     @ConstructorArgument
     protected CrashedAction actionsAfterCrash = CrashedAction.COREDUMP_AND_RESTART;
     @ConstructorArgument
     protected int startupVCPUs, maxVCPUs;
     @ConstructorArgument
     protected long minimumStaticMemory, minimumDynamicMemory;
     @ConstructorArgument
     protected long maximumStaticMemory;
     @ConstructorArgument
     protected long maximumDynamicMemory;
     protected int domainId;
     @ConstructorArgument
     protected boolean template;
     protected boolean controlDomain;
     protected String poolName;
     protected boolean autoPowerOn;
     @ConstructorArgument
     protected String PVargs, PVramdisk, PVbootloader, PVkernel, PVbootloaderArgs;
     protected PowerState powerState;
     @ConstructorArgument
     protected String HVMbootPolicy;
     @Fill
     @ConstructorArgument
     protected Map<String, String> HVMbootParams;
     @ConstructorArgument
     @Fill
     protected Map<String, String> platform;
     @ConstructorArgument
     protected String PCIbus;
     protected String metrics, guestMetrics;
     protected String host;
     @ConstructorArgument
     protected String hostAffinity;
     @Fill
     protected Object[] VBDs, VIFs;
     @Fill
     @ConstructorArgument
     protected Map<String, String> VCPUparams;
     @Fill
     @ConstructorArgument
     protected Map<String, String> otherConfig;
     @ConstructorArgument
     protected String recommendations;
     protected static int MEGABYTE = 1024 * 1024;
 
     public VM() {
         
     }
     
     public VM(String ref, boolean autoFill) {
         super(ref, autoFill);
     }
 
     public VM(String ref) {
         super(ref);
     }
 
     public String create(int maxVCPUs) throws BadAPICallException {
         this.maxVCPUs = maxVCPUs;
 
         if (startupVCPUs == 0) startupVCPUs = maxVCPUs;
         
         if (startupVCPUs < 1 || maxVCPUs < 1 || startupVCPUs > maxVCPUs) {
             throw new IllegalArgumentException("VM CPU count is zero or startup VCPU count is larger than max VCPU count");
         }
 
        HashMap<String, Object> ctorArgs = collectConstructorArgs();
         // Not putting legacy args in the model, we don't do legacy
         ctorArgs.put("PV_legacy_args", "");
 
         this.reference = (String) dispatch("create", ctorArgs);
         return this.reference;
     }
 
     public void destroy() throws BadAPICallException {
         dispatch("destroy");
     }
 
     public void start(boolean startPaused, boolean force) throws BadAPICallException {
         start(startPaused, force, null);
     }
 
     public void start(boolean startPaused, boolean force, Host host) throws BadAPICallException {
         try {
             if (host != null) {
                 dispatch("start", host.getReference(), startPaused, force);
             } else {
                 dispatch("start", startPaused, force);
             }
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might be already running");
                     break;
                 case "VM_HVM_REQUIRED":
                     ex.setErrorDescription("Your CPU(s) does not support VT-x or AMD-v, which this VM requires");
                     break;
                 case "NO_HOST_AVAILABLE":
                     ex.setErrorDescription("There are no hosts available for this machine to run on");
                     break;
             }
 
             throw ex;
         }
     }
 
     public void pause() throws BadAPICallException {
         try {
             dispatch("pause");
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might be already paused");
             }
 
             throw ex;
         }
     }
 
     public void resume() throws BadAPICallException {
         try {
             dispatch("unpause");
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might be already running");
             }
 
             throw ex;
         }
     }
 
     /**
      * Stop the VM
      * @param polite it's up to you to keep your manners
      */
     public void stop(boolean polite) throws BadAPICallException {
         try {
             if (polite) {
                 dispatch("clean_shutdown");
             } else {
                 dispatch("hard_shutdown");
             }
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     /**
      * Reboot the VM
      * @param polite it's up to you to keep your manners
      */
     public void reboot(boolean polite) throws BadAPICallException {
         try {
             if (polite) {
                 dispatch("clean_reboot");
             } else {
                 dispatch("hard_reboot");
             }
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     public void suspend() throws BadAPICallException {
         try {
             dispatch("suspend");
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     public void wake(boolean startPaused, boolean force) throws BadAPICallException {
         try {
             dispatch("resume", startPaused, force);
         } catch (BadAPICallException ex) {
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be suspended");
             }
 
             throw ex;
         }
     }
 
     public void migrateInsidePool(Host host, Map<String, String> options) throws BadAPICallException {
         if (options == null) {
             options = new HashMap<>();
         }
         try {
             dispatch("pool_migrate", host, options);
         } catch (BadAPICallException ex) {
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     public int computeMemoryOverhead() throws BadAPICallException {
         return (int) dispatch("compute_memory_overhead");
     }
 
     public void sendSysRq(String sysrq) throws BadAPICallException {
         dispatch("send_sysrq", sysrq);
     }
 
     public void sendTrigger(String trigger) throws BadAPICallException {
         dispatch("send_trigger", trigger);
     }
 
     // todo check what this does and decide on good name
 //    public int computeMaximumAvailableMemory() throws BadAPICallException {
 //        
 //    }
     public List<Object> getDataSources() throws BadAPICallException {
         Object[] result = (Object[]) dispatch("get_data_sources");
         ArrayList<Object> arrr = new ArrayList<>();
         CollectionUtils.addAll(arrr, result);
         return arrr;
     }
 
     public VMMetrics getMetrics() {
         this.metrics = value(this.metrics, "get_vm_metrics");
         return new VMMetrics(this.metrics);
     }
 
     public GuestMetrics getGuestMetrics() {
         this.guestMetrics = value(this.guestMetrics, "get_guest_metrics");
         return new GuestMetrics(this.guestMetrics);
     }
 
     public static List<VM> getAll() throws BadAPICallException {
         Map<String, Object> records = (Map) Controller.dispatch("VM.get_all_records");
         ArrayList<VM> objects = new ArrayList<>();
         for (Map.Entry<String, Object> entry : records.entrySet()) {
             VM vm = new VM(entry.getKey(), false);
             vm.fillOut((Map) entry.getValue());
             objects.add(vm);
         }
         return objects;
     }
 
     public List<VBD> getVBDs() {
         this.VBDs = value(this.VBDs, "get_VBDs");
         ArrayList<VBD> vbds = new ArrayList<>();
         for (Object o : this.VBDs) {
             vbds.add(new VBD((String) o));
         }
         return vbds;
     }
 
     public List<VIF> getVIFs() {
         ArrayList<VIF> objs = new ArrayList<>();
         for (Object o : this.VIFs) {
             objs.add(new VIF((String) o));
         }
         return objs;
     }
 
     public Map<String, String> getVCPUParams() {
         if (VCPUparams == null) {
             VCPUparams = new HashMap<>();
         }
         return VCPUparams;
     }
 
     public void setMemoryLimits(double maxStaticMemMb, double minStaticMemMb, double maxDynMemMb, double minDynMemMb) throws BadAPICallException {
         dispatch("set_memory_limits", minStaticMemMb * MEGABYTE, maxStaticMemMb * MEGABYTE, minDynMemMb * MEGABYTE, maxDynMemMb * MEGABYTE);
     }
 
     public String getHVMBootPolicy() {
         return HVMbootPolicy;
     }
 
     public void setDefaultHVMBootPolicy() throws BadAPICallException {
         setHVMBootPolicy("BIOS order");
     }
 
     public void setHVMBootPolicy(String policy) throws BadAPICallException {
         HVMbootPolicy = setter(policy, "set_HVM_boot_policy");
     }
 
     public Map<String, String> getHVMBootParams() {
         return HVMbootParams;
     }
 
     public void setHVMBootParams(Map<String, String> params) {
         HVMbootParams = params;
     }
 
     public String getPVargs() {
         return PVargs;
     }
 
     public String getPVBootloader() {
         return PVbootloader;
     }
 
     public void setPVBootloader(String bootloader) throws BadAPICallException {
         PVbootloader = setter(bootloader, "set_PV_bootloader");
     }
 
     public String getPVKernel() {
         return PVkernel;
     }
 
     public void setPVKernel(String kernel) throws BadAPICallException {
         PVkernel = setter(kernel, "set_PV_kernel");
     }
 
     public String getPVRamdisk() {
         return PVramdisk;
     }
 
     public void setPVRamdisk(String ramdisk) throws BadAPICallException {
         PVramdisk = setter(ramdisk, "set_PV_ramdisk");
     }
 
     public CrashedAction getActionsAfterCrash() {
         return actionsAfterCrash;
     }
 
     public ShutdownAction getActionsAfterReboot() {
         return actionsAfterReboot;
     }
 
     public ShutdownAction getActionsAfterShutdown() {
         return actionsAfterShutdown;
     }
 
     public boolean isAutoPowerOn() {
         return autoPowerOn;
     }
 
     public int getDomainId() {
         return domainId;
     }
 
     public boolean isControlDomain() {
         return controlDomain;
     }
 
     public boolean isTemplate() {
         return template;
     }
 
     public int getMaximumVCPUs() {
         return maxVCPUs;
     }
 
     public int getStartupVCPUs() {
         return value(startupVCPUs, "getVCPUs_at_startup");
     }
 
     public void setStartupVCPUs(int startupVCPUs) throws BadAPICallException {
         this.startupVCPUs = setter(startupVCPUs, "set_VCPUs_at_startup");
     }
 
     public long getMaximumDynamicMemory() {
         return maximumDynamicMemory;
     }
     
     public void setMaximumDynamicMemory(double mdmMb) throws BadAPICallException {
         this.maximumDynamicMemory = setter((long) mdmMb * MEGABYTE, "set_memory_dynamic_max");
     }
 
     public long getMaximumStaticMemory() {
         return value(maximumStaticMemory, "get_memory_static_max");
     }
     
     public void setMaximumStaticMemory(double msmMb) throws BadAPICallException {
         this.maximumStaticMemory = setter((long) msmMb * MEGABYTE, "set_memory_static_max");
     }
 
     public long getMinimumStaticMemory() {
         return minimumStaticMemory;
     }
 
     public String getPoolName() {
         poolName = value(poolName, "get_pool_name");
         return poolName;
     }
 
     public PowerState getPowerState() {
         powerState = value(powerState, "get_power_state");
         return powerState;
     }
 
     public int getVCPUs() {
         return startupVCPUs;
     }
 
     public void setVCPUs(int count, boolean live) throws BadAPICallException {
         if (getPowerState() == PowerState.RUNNING && live) {
             dispatch("set_VCPUs_number_live", count);
             startupVCPUs = count;
         } else {
             startupVCPUs = count;
         }
     }
 
     public int getUserVersion() {
         return userVersion;
     }
 
     public Host getHost() {
         return new Host(value(host, "get_resident_on"));
     }
 
     public Host getAffinityHost() {
         return new Host(hostAffinity);
     }
 
     @Override
     protected Map<String, String> interpretation() {
         HashMap<String, String> map = (HashMap<String, String>) super.interpretation();
         map.put("startupVCPUcount", "VCPUs_at_startup");
         map.put("minimumStaticMemory", "memory_static_min");
         map.put("maximumStaticMemory", "memory_static_max");
         map.put("maximumDynamicMemory", "memory_dynamic_max");
         map.put("minimumDynamicMemory", "memory_dynamic_min");
         map.put("template", "is_a_template");
         map.put("controlDomain", "is_control_domain");
         map.put("domainId", "domid");
         map.put("PVargs", "PV_args");
         map.put("PVramdisk", "PV_ramdisk");
         map.put("PVkernel", "PV_kernel");
         map.put("PVbootloader", "PV_bootloader");
         map.put("PVbootloaderArgs", "PV_bootloader_args");
         map.put("HVMbootPolicy", "HVM_boot_policy");
         map.put("HVMbootParams", "HVM_boot_params");
         map.put("startupVCPUs", "VCPUs_at_startup");
         map.put("maxVCPUs", "VCPUs_max");
         map.put("host", "resident_on");
         map.put("hostAffinity", "affinity");
         map.put("VCPUparams", "VCPUs_params");
         map.put("PCIbus", "PCI_bus");
 
         return map;
     }
 
     public enum ShutdownAction {
 
         /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * destroy the VM state
          */
         DESTROY,
         /**
          * restart the VM
          */
         RESTART
     };
 
     public enum CrashedAction {
 
         /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * destroy the VM state
          */
         DESTROY,
         /**
          * record a coredump and then destroy the VM state
          */
         COREDUMP_AND_DESTROY,
         /**
          * restart the VM
          */
         RESTART,
         /**
          * record a coredump and then restart the VM
          */
         COREDUMP_AND_RESTART,
         /**
          * leave the crashed VM paused
          */
         PRESERVE,
         /**
          * rename the crashed VM and start a new copy
          */
         RENAME_RESTART
     };
 
     public enum PowerState {
 
         /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * VM is offline and not using any resources
          */
         HALTED,
         /**
          * All resources have been allocated but the VM itself is paused and its vCPUs are not running
          */
         PAUSED,
         /**
          * Running
          */
         RUNNING,
         /**
          * VM state has been saved to disk and it is no longer running. Note that disks remain in-use while the VM is suspended.
          */
         SUSPENDED
     };
 }
