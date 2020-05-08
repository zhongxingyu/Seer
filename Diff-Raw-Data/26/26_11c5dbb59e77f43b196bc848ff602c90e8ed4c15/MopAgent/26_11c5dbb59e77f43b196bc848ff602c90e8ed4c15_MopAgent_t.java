 /**************************************************************************************
  * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
  * http://fusesource.com                                                              *
  * ---------------------------------------------------------------------------------- *
  * The software in this package is published under the terms of the AGPL license      *
  * a copy of which has been included with this distribution in the license.txt file.  *
  **************************************************************************************/
 package org.fusesource.cloudmix.agent.mop;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.fusesource.cloudmix.agent.Feature;
 import org.fusesource.cloudmix.agent.InstallerAgent;
 import org.fusesource.cloudmix.common.dto.ProvisioningAction;
 import org.fusesource.mop.com.google.common.collect.ImmutableMap;
 import org.fusesource.mop.com.google.common.collect.Lists;
 import org.fusesource.mop.com.google.common.collect.Maps;
 
 /**
  * @version $Revision: 1.1 $
  */
 public class MopAgent extends InstallerAgent {
     public static final String MOP_URI_PREFIX = "mop:";
 
     private Map<String, MopProcess> processes = Maps.newHashMap();
     private ClassLoader mopClassLoader;
 
     public MopAgent() {
         // lets default the profile
         setProfile("*");
         mopClassLoader = getClass().getClassLoader();
     }
 
     @Override
     public File getWorkDirectory() {
         File answer = super.getWorkDirectory();
         if (answer == null) {
             setWorkDirectory(new File("mopAgentWork"));
             answer = super.getWorkDirectory();
         }
         return answer;
     }
 
     /**
      * Returns the currently active processes this agent is running
      */
     public ImmutableMap<String, MopProcess> getProcesses() {
         synchronized (processes) {
             // lets remove all the dead processes
             List<MopProcess> list = Lists.newArrayList(processes.values());
             for (MopProcess process : list) {
                 if (process.isCompleted()) {
                     processes.remove(process.getId());
                 }
             }
             return ImmutableMap.copyOf(processes);
         }
     }
 
     /**
      * A helper method for testing mostly
      *
      * @param commandLine
      */
     public void installMopFeature(String commandLine) throws Exception {
         String resource = MOP_URI_PREFIX + commandLine;
         String feature = "feature:" + UUID.randomUUID().toString();
         ProvisioningAction action = new  ProvisioningAction(feature, resource);
         String credentials = "";
         installFeatures(action, credentials, resource);
     }
 
     @Override
     public void init() throws Exception {
        System.out.println("==================== CREATING MOP AGENT " + this 
                           + " = " + System.identityHashCode(this));
         super.init();
         mopClassLoader = Thread.currentThread().getContextClassLoader();
     }
 
     @Override
     protected void installFeatures(ProvisioningAction action, 
                                    String credentials, 
                                    String resource) throws Exception {
         System.out.println("Installing FEATURES: " + resource);
 
         if (resource.startsWith(MOP_URI_PREFIX)) {
             String commandLine = resource.substring(MOP_URI_PREFIX.length());
 
             // Lets set the context class loader to the one that loaded me!
             Thread currentThread = Thread.currentThread();
             ClassLoader oldClassLoader = currentThread.getContextClassLoader();
             currentThread.setContextClassLoader(mopClassLoader);
             try {
                 MopProcess process = new MopProcess(this, action, credentials, commandLine, mopClassLoader);
 
                 String id = process.getId();
                 synchronized (processes) {
                     MopProcess oldProcess = processes.get(id);
                     if (oldProcess != null) {
                         oldProcess.stop();
                     }
                     processes.put(id, process);
                 }
 
                 process.start();
 
 
                 // now lets update the current features
 
                 Feature feature = new Feature(id);
                 addAgentFeature(feature);
 
                 // TODO also need to update the current features!
             } finally {
                 currentThread.setContextClassLoader(oldClassLoader);
             }
         }
     }
 
 
     @Override
     protected void uninstallFeature(Feature feature) throws Exception {
         System.out.println("Uninstalling FEATURE: " + feature);
 
         String id = feature.getName();
         synchronized (processes) {
             MopProcess oldProcess = processes.get(id);
             if (oldProcess != null) {
                 oldProcess.stop();
             }
         }
 
         super.uninstallFeature(feature);
         removeFeatureId(id);
     }
 
     public File createProcessDirectory(ProvisioningAction action) {
         return new File(getWorkDirectory(), action.getId());
     }
 }
