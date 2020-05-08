 /*
  * Copyright (c) 2007 Avaya Inc.
  *
  * All rights reserved.
  */
 
 package hudson.plugins.vmware;
 
 import hudson.plugins.vmware.vix.Vix;
 
import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Logger;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author connollys
  * @since 28-Sep-2007 09:57:07
  */
 public class Host {
     private static final Logger LOGGER = Logger.getLogger(Host.class.getName());
     private int handle = 0;
     private final VMware lib;
    private final AtomicInteger usageCount = new AtomicInteger(0);
 
     Host(VMware library, HostType hostType, String hostName, int hostPort, String userName, String password) {
         this.lib = library;
         LOGGER.info("Connecting to " + hostName);
         int jobHandle = 0;
         try {
             switch (hostType) {
                 case VMWARE_SERVER:
                 default:
                     jobHandle = lib.getInstance().VixHost_Connect(1, Vix.ServiceProvider.VMWARE_SERVER,
                             hostName, hostPort, userName, password, 0, Vix.Handle.INVALID, null, null);
                     break;
             }
             if (jobHandle == 0) {
                 throw new VMwareRuntimeException("Unknown error");
             }
 
             LOGGER.fine("Waiting to connect...");
 
             handle = lib.waitForJobAndGetJobResultHandle(jobHandle);
 
             LOGGER.info("Connected");
         } finally {
             lib.getInstance().Vix_ReleaseHandle(jobHandle);
         }
     }
 
     public enum HostType {
         VMWARE_SERVER;
     }
 
     public void disconnect() {
        if (usageCount.decrementAndGet() <= 0) {
            lib.getInstance().VixHost_Disconnect(handle);
            handle = 0;
        }
     }
 
     public VirtualMachine open(String configFileHostPath) {
         if (handle == 0 || lib == null) {
             throw new VMwareRuntimeException("Not connected.");
         }
        final VirtualMachine machine = new VirtualMachine(lib, handle, configFileHostPath);
        usageCount.getAndIncrement();
        return machine;
     }
 }
