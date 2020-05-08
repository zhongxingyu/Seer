 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.entities;
 
 import java.util.HashMap;
 import java.util.Map;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Oct 2, 2011
  * @author double-u
  */
 public class VM extends XenApiEntity {
 
     protected int userVersion;
     protected ShutdownAction actionsAfterReboot, actionsAfterShutdown;
     protected CrashedAction actionsAfterCrash;
     protected int startupVCPUs, maxVCPUs;
     protected int minimumStaticMemory, maximumStaticMemory;
     protected int maximumDynamicMemory;
     protected int domainId;
     protected boolean isTemplate, isControlDomain;
     protected String poolName;
     protected boolean autoPowerOn;
     protected String PVargs, PVramdisk, PVbootloader, PVkernel;
     protected PowerState powerState;
     protected String HVMbootPolicy;
     protected String nameLabel, nameDescription;
 
     public VM(String ref, boolean autoFill) {
         super(ref, autoFill);
     }
 
     public VM(String ref) {
         super(ref);
     }
 
     @Override
     protected String getAPIName() {
         return "VM";
     }
 
     public void start(boolean startPaused) {
         try {
            dispatch("VM.start", startPaused);
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might be already running";
                     break;
                 case "VM_HVM_REQUIRED":
                     errMsg = "Your CPU(s) do not support VT-x or AMD-v, which this VM requires";
                     break;
                 case "UNKNOWN_BOOTLOADER":
                     errMsg = "Unknown bootloader";
                     break;
                 case "NO_HOST_AVAILABLE":
                     errMsg = "There are no hosts available for this machine to run on";
                     break;
                 default:
                     errMsg = ex.toString();
 
             }
 
             Logger.getLogger(getClass()).error(errMsg, ex);
         }
     }
 
     public void pause() {
         try {
            dispatch("VM.pause");
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might be already paused";
                 default:
                     errMsg = ex.toString();
 
             }
 
             Logger.getLogger(getClass()).error(errMsg, ex);
         }
     }
 
     public void resume() {
         try {
            dispatch("VM.unpause");
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might not be paused";
                 default:
                     errMsg = ex.toString();
 
             }
 
             Logger.getLogger(getClass()).error(errMsg, ex);
         }
     }
 
     /**
      * Stop the VM
      * @param polite it's up to you to keep your manners
      */
     public void stop(boolean polite) {
         try {
             if (polite) {
                dispatch("VM.clean_shutdown");
             } else {
                dispatch("VM.hard_shutdown");
             }
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might not be running";
                 default:
                     errMsg = ex.toString();
             }
 
             Logger.getLogger(getClass()).error(errMsg, ex);
         }
     }
     
     /**
      * Reboot the VM
      * @param polite it's up to you to keep your manners
      */
     public void reboot(boolean polite) {
         try {
             if (polite) {
                dispatch("VM.clean_reboot");
             } else {
                dispatch("VM.hard_reboot");
             }
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might not be running";
                 default:
                     errMsg = ex.toString();
             }
 
             Logger.getLogger(getClass()).error(errMsg, ex);
         }
     }
     
     public void suspend() {
         try {
            dispatch("VM.suspend");
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might be already paused";
                 default:
                     errMsg = ex.toString();
 
             }
 
             Logger.getLogger(getClass()).error(errMsg);
         }
     }
     
     public void wake(boolean startPaused, boolean force) {
         try {
            dispatch("VM.resume", startPaused, force);
         } catch (BadAPICallException ex) {
             String errMsg = "";
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     errMsg = "The VM has a bad power state. It might be already paused";
                 default:
                     errMsg = ex.toString();
 
             }
 
             Logger.getLogger(getClass()).error(errMsg);
         }
     }
 
     @Override
     protected Map<String, String> interpretation() {
         HashMap<String, String> map = new HashMap<>();
         map.put("startupVCPUcount", "VCPUs_at_startup");
         map.put("minimumStaticMemory", "memory_static_min");
         map.put("maximumStaticMemory", "memory_static_max");
         map.put("maximumDynamicMemory", "memory_dynamic_max");
         map.put("isTemplate", "is_a_template");
         map.put("domainId", "domid");
         map.put("PVargs", "PV_args");
         map.put("PVramdisk", "PV_ramdisk");
         map.put("PVkernel", "PV_kernel");
         map.put("PVbootloader", "PV_bootloader");
         map.put("HVMbootPolicy", "HVM_boot_policy");
         map.put("startupVCPUs", "VCPUs_at_startup");
         map.put("maxVCPUs", "VCPUs_max");
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
          * VM state has been saved to disk and it is nolonger running. Note that disks remain in-use while the VM is suspended.
          */
         SUSPENDED
     };
 }
