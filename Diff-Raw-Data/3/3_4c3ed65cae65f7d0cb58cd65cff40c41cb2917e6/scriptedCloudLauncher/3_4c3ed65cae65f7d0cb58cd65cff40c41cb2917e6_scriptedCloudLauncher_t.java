 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jenkinsci.plugins.scripted_cloud;
 import hudson.Util;
 import hudson.slaves.ComputerLauncher;
 import hudson.slaves.SlaveComputer;
 import hudson.model.TaskListener;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.slaves.Cloud;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 
 import hudson.Launcher.LocalLauncher;
 import hudson.tasks.Shell;
 import hudson.AbortException;
 import hudson.Extension;
 import hudson.FilePath;
 import java.io.IOException;
 import java.util.Collections;
 
 import hudson.tasks.BatchFile;
 import hudson.tasks.CommandInterpreter;
 import hudson.tasks.Shell;
 
 import static hudson.model.TaskListener.NULL;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 /**
  *
  * @author Admin
  */
 public class scriptedCloudLauncher extends ComputerLauncher {
 
     private ComputerLauncher delegate;
     private Boolean forceLaunch;
     private String vsDescription;
     private String vmName;
     private String vmPlatform;
     private String vmGroup;
     private String snapName;
     private Boolean isStarting = Boolean.FALSE;
     private Boolean isDisconnecting = Boolean.FALSE;
     private MACHINE_ACTION idleAction;
     private scriptedCloud vs = null;
     private int LimitedTestRunCount = 0;
 
     public enum MACHINE_ACTION {
         SHUTDOWN,
        REVERT,
         RESET,
         NOTHING
     }
 
     @DataBoundConstructor
     public scriptedCloudLauncher(ComputerLauncher delegate,
             String vsDescription
             , String vmName, String vmPlatform, String vmGroup
             , String snapName
             , Boolean forceLaunch
             , String idleOption
             ,String LimitedTestRunCount
             ) {
         super();
         this.delegate = delegate;
         this.forceLaunch = forceLaunch;
         this.vsDescription = vsDescription;
         this.vmName = vmName;
         this.vmPlatform = vmPlatform;
         this.vmGroup = vmGroup;
         this.snapName = snapName;
         this.isStarting = Boolean.FALSE;
         if ("Shutdown".equals(idleOption)) {
             idleAction = MACHINE_ACTION.SHUTDOWN;
         } else if ("Shutdown and Revert".equals(idleOption)) {
             idleAction = MACHINE_ACTION.REVERT;
         } else if ("Reset".equals(idleOption)) {
             idleAction = MACHINE_ACTION.RESET;            
         } else {
             idleAction = MACHINE_ACTION.NOTHING;
         }
         this.LimitedTestRunCount = Util.tryParseNumber(LimitedTestRunCount, 0).intValue();
         vs = findOurVsInstance();
     }
 
     public scriptedCloud findOurVsInstance() throws RuntimeException {
         if (vsDescription != null && vmName != null) {
             scriptedCloud vs = null;
             for (Cloud cloud : Hudson.getInstance().clouds) {
                 if (cloud instanceof scriptedCloud && ((scriptedCloud) cloud).getVsDescription().equals(vsDescription)) {
                     vs = (scriptedCloud) cloud;
                     return vs;
                 }
             }
         }
         scriptedCloud.Log("Could not find our scripted Cloud instance!");
         throw new RuntimeException("Could not find our scripted Cloud instance!");
     }
    
     private CommandInterpreter getCommandInterpreter(String script) {
         if (Hudson.getInstance().isWindows()) {
         	scriptedCloud.Log("its windows..");
             return new BatchFile(script);
         }
         scriptedCloud.Log("its unix..");
         return new Shell(script);
     }
 
     @Override
     public void launch(SlaveComputer slaveComputer, TaskListener listener)
     throws IOException, InterruptedException {
     	scriptedCloud.Log("launch enter:" + vs.getStartScriptFile());
     	scriptedCloud.Log("launch enter:slaveComputer:" + slaveComputer);
 
     	try {
     		if (slaveComputer.isTemporarilyOffline()) {
     			scriptedCloud.Log(slaveComputer, listener, "Not launching VM because it's not accepting tasks; temporarily offline"); 
     			return;
     		}
 
     		// Slaves that take a while to start up make get multiple launch
     		// requests from Jenkins.  
     		if (isStarting == Boolean.TRUE) {
     			scriptedCloud.Log(slaveComputer, listener, "VM is already being launched");
     			return;
     		}
 
     		isStarting = Boolean.TRUE;
     		File f = new File(vs.getStartScriptFile());
     		CommandInterpreter shell = getCommandInterpreter(vs.getStartScriptFile());
     		scriptedCloud.Log("script file:" + vs.getStartScriptFile());
     		//FilePath root = Hudson.getInstance().getRootPath();
     		scriptedCloud.Log("file.getpath:" + f.getParent());
     		FilePath root = new FilePath(new File("/"));
     		FilePath script = shell.createScriptFile(root);
     		scriptedCloud.Log("root path:" + root + ", script:" + script);
     		//shell.buildCommandLine(script);
     		listener.getLogger().println("running start script");
     		int r = 0;
     		HashMap envMap = new HashMap();
     		envMap.put("SCVM_ACTION","start");
     		envMap.put("SCVM_NAME", this.vmName);
     		envMap.put("SCVM_SNAPNAME", this.snapName);
     		envMap.put("SCVM_PLATFORM", this.vmPlatform);
     		envMap.put("SCVM_GROUP", this.vmGroup);
     		if (forceLaunch == Boolean.TRUE) {
     			envMap.put("SCVM_FORCESTART", "yes");
     		}
     		scriptedCloud.Log("env:" + envMap);
     		shell.buildCommandLine(script);
     		//scriptedCloud.Log("launching:shell:" + shell.getContents());
     		
     		r = root.createLauncher(listener).launch().cmds(shell.buildCommandLine(script))
     		//.envs(Collections.singletonMap("LABEL","s"))
     		.envs(envMap)
     		.stdout(listener).pwd(root).join();
     		if (r!=0)
     			throw new AbortException("The script failed:" + r + ", " + vs.getStartScriptFile());
     		scriptedCloud.Log("script done:" + vs.getStartScriptFile());
     		//delegate.launch(slaveComputer, listener);
     		scriptedCloud.Log("launch exit:"+ vs.getStartScriptFile());
     	} catch (Exception e) {    		
     		scriptedCloud.Log("launch error:"+ e);
     		throw new RuntimeException(e);            
     	}
     	finally {
     		isStarting = Boolean.FALSE;
     	}
     }
 
     public void startSlave(SlaveComputer slaveComputer) {
     	scriptedCloud.Log("startSlave enter");
     	
     	HashMap envMap = new HashMap();
     	envMap.put("SCVM_ACTION","start");
     	runScript(slaveComputer, vs.getStartScriptFile(), envMap);
     	scriptedCloud.Log("startSlave exit");
     }
 
     public void stopSlave(scriptedCloudSlaveComputer slaveComputer, boolean forced) {
     	scriptedCloud.Log("stopSlave processing called");
     	if (forced == false && idleAction == MACHINE_ACTION.NOTHING) {
     		scriptedCloud.Log("Do nothing for this slave");
     		return;
     	}
     	slaveComputer.disconnect();
     	scriptedCloud.Log("disconnected slave.");
     }
 
 	public synchronized void runScript(SlaveComputer slaveComputer
     		, String scriptToRun, HashMap envMap) {
     	scriptedCloud.Log("runScript:" + scriptToRun);        	
         try {
             scriptedCloud.Log( "running script");
             File f = new File(scriptToRun);
         	//CommandInterpreter shell = getCommandInterpreter(f.getName());
         	CommandInterpreter shell = getCommandInterpreter(scriptToRun);
             scriptedCloud.Log("sript file:" + scriptToRun);
             scriptedCloud.Log("file.getpath:" + f.getParent());
             //FilePath root = new FilePath(new File(f.getParent()));
             FilePath root = new FilePath(new File("/"));
             FilePath script = shell.createScriptFile(root);
         	scriptedCloud.Log("root path:" + root + ", script:" + script);
             //shell.buildCommandLine(script);
             int r = root.createLauncher(NULL).launch().cmds(shell.buildCommandLine(script))
                     .envs(envMap /*Collections.singletonMap("LABEL","s")*/)
                     .stdout(NULL).pwd(root).join();
             if (r!=0)
                 throw new AbortException("The script failed:" + r);            
             scriptedCloud.Log("script done");
         } catch (Throwable t) {
             scriptedCloud.Log("Got an exception");
             scriptedCloud.Log(t.toString());
             scriptedCloud.Log("Printed exception");
             //taskListener.fatalError(t.getMessage(), t);
         } finally {
             isDisconnecting = Boolean.FALSE;
         }
     }
 
     @Override
     public synchronized void afterDisconnect(SlaveComputer slaveComputer,
             TaskListener taskListener) {
     	scriptedCloud.Log("afterDisconnect ...., isDisconnecting=" + isDisconnecting);
         if (isDisconnecting == Boolean.TRUE) {
         	scriptedCloud.Log(slaveComputer, taskListener, "Already disconnecting on a separate thread");
             return;
         }
         
         if (slaveComputer.isTemporarilyOffline()) {
         	scriptedCloud.Log(slaveComputer, taskListener, "Not disconnecting VM because it's not accepting tasks"); 
            return;
         }
             
         try {
             isDisconnecting = Boolean.TRUE;
             scriptedCloud.Log(slaveComputer, taskListener, "Running disconnect procedure...");
             delegate.afterDisconnect(slaveComputer, taskListener);
             scriptedCloud.Log(slaveComputer, taskListener, "Shutting down Virtual Machine...");
             //*********************************
             HashMap envMap = new HashMap();
     		envMap.put("SCVM_NAME", this.vmName);
     		envMap.put("SCVM_SNAPNAME", this.snapName);
     		envMap.put("SCVM_PLATFORM", this.vmPlatform);
     		envMap.put("SCVM_GROUP", this.vmGroup);
         	if (idleAction == MACHINE_ACTION.SHUTDOWN) {
     	    	envMap.put("SCVM_ACTION","stop");
         	}
         	if (idleAction == MACHINE_ACTION.REVERT) {
     	    	envMap.put("SCVM_ACTION","revert");
         	}
         	if (idleAction == MACHINE_ACTION.RESET) {
     	    	envMap.put("SCVM_ACTION","reset");
         	}
         	String scriptToRun = vs.getStopScriptFile();
         	scriptedCloud.Log("runScript:" + scriptToRun);        	
             scriptedCloud.Log( "running script");
             File f = new File(scriptToRun);
         	//CommandInterpreter shell = getCommandInterpreter(f.getName());
         	CommandInterpreter shell = getCommandInterpreter(scriptToRun);
             scriptedCloud.Log("sript file:" + scriptToRun);
             scriptedCloud.Log("file.getpath:" + f.getParent());
             //FilePath root = new FilePath(new File(f.getParent()));
             FilePath root = new FilePath(new File("/"));
             FilePath script = shell.createScriptFile(root);
         	scriptedCloud.Log("root path:" + root + ", script:" + script);
             //shell.buildCommandLine(script);
             int r = root.createLauncher(taskListener).launch().cmds(shell.buildCommandLine(script))
                     .envs(envMap /*Collections.singletonMap("LABEL","s")*/)
                     .stdout(NULL).pwd(root).join();
             if (r!=0)
                 throw new AbortException("The script failed:" + r);            
             scriptedCloud.Log("script done:" + scriptToRun);            
             //**********************************
         } catch (Throwable t) {
         	scriptedCloud.Log(slaveComputer, taskListener, "Got an exception");
         	scriptedCloud.Log(slaveComputer, taskListener, t.toString());
         	scriptedCloud.Log(slaveComputer, taskListener, "Printed exception");
             taskListener.fatalError(t.getMessage(), t);
         } finally {
         	scriptedCloud.Log("finally setting isDisconnecting to FALSE");
             isDisconnecting = Boolean.FALSE;
         }
     }
    	
     public ComputerLauncher getDelegate() {
         return delegate;
     }
 
     public String getVmName() {
         return vmName;
     }
 
     public String getVsDescription() {
         return vsDescription;
     }
 
     public MACHINE_ACTION getIdleAction() {
         return idleAction;
     }
 
     public void setIdleAction(MACHINE_ACTION idleAction) {
         this.idleAction = idleAction;
     }
 
     public Boolean getforceLaunch() {
         return forceLaunch;
     }
 
     public void setforceLaunch(Boolean forceLaunch) {
         this.forceLaunch = forceLaunch;
     }
 
     public Integer getLimitedTestRunCount() {
         return LimitedTestRunCount;
     }
 
     
     @Override
     public void beforeDisconnect(SlaveComputer slaveComputer, TaskListener taskListener) {
         delegate.beforeDisconnect(slaveComputer, taskListener);
     }
 
     @Override
     public Descriptor<ComputerLauncher> getDescriptor() {
         // Don't allow creation of launcher from UI
         throw new UnsupportedOperationException();
     }
 }
