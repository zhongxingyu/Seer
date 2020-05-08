 /*
    Copyright (C) 2012-2013 Anton Lobov <zhuravlik> <ahmad200512[at]yandex.ru>
 
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.
 
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
 
    You should have received a copy of the GNU Lesser General
    Public License along with this library; if not, write to the
    Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA 02110-1301 USA
  */
 
 package zhuravlik.ant.vix;
 
 import com.sun.jna.Pointer;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import zhuravlik.ant.vix.tasks.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Main VIX Ant task, representing abstraction over single Virtual Machine
  * All operations with Virtual Machine are implemented as subtasks
  */
 public class VixTask extends Task {
 
     private List<VixAction> actions = new ArrayList<VixAction>();
 
     boolean ignoreError = false;
 
     boolean logError = false;
 
     public boolean isLogError() {
         return logError;
     }
 
     public void setLogError(boolean logError) {
         this.logError = logError;
     }
 
     public boolean isIgnoreError() {
         return ignoreError;
     }
 
     public void setIgnoreError(boolean ignoreError) {
         this.ignoreError = ignoreError;
     }
 
     public void checkError(int err) throws BuildException {
         if (Vix.VIX_OK != err) {
 
             if (!ignoreError)
                 throw new BuildException("VMWare error: " + LibraryHelper.getInstance().Vix_GetErrorText(err, null));
             else
                 log("VMWare error: " + LibraryHelper.getInstance().Vix_GetErrorText(err, null), logError ? Project.MSG_ERR : Project.MSG_WARN);
         }
     }
 
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     private String host;
     private int port;
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     private String user;
     private String password;
 
 
     public String getServiceProvider() {
         return serviceProvider;
     }
 
     public void setServiceProvider(String serviceProvider) {
         this.serviceProvider = serviceProvider;
     }
 
     private String serviceProvider;
 
     public String getPath() {
         return path;
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 
     private String path;
 
     public String getVixPath() {
         return vixPath;
     }
 
     public void setVixPath(String vixPath) {
         this.vixPath = vixPath;
     }
 
     private String vixPath;
 
     private String operation = "OpenAndRun";
 
     private String callbackTargetName = null;
 
     private String findType = "registered";
 
     public String getOperation() {
         return operation;
     }
 
     public void setOperation(String op) {
         operation = op;
     }
 
     public String getCallbackTargetName() {
         return callbackTargetName;
     }
 
     public void setCallbackTargetName(String callbackTargetName) {
         this.callbackTargetName = callbackTargetName;
     }
 
     public String getFindType() {
         return findType;
     }
 
     public void setFindType(String findType) {
         //if (findType != "running" && findType != "registered")
         //    throw new BuildException("Invalid VM find type: " + findType);
 
         this.findType = findType;
     }
 
     public void addCaptureScreen(CaptureScreen captureScreen) {
         actions.add(captureScreen);
     }
 
     public void addDeleteDirectory(DeleteDirectory deleteDirectory) {
         actions.add(deleteDirectory);
     }
 
     public void addDeleteFile(DeleteFile deleteFile) {
         actions.add(deleteFile);
     }
 
     public void addInstallTools(InstallTools installTools) {
         actions.add(installTools);
     }
 
     public void addKillProcess(KillProcess killProcess) {
         actions.add(killProcess);
     }
 
     public void addLogout(Logout logout) {
         actions.add(logout);
     }
 
     public void addMoveFile(MoveFile moveFile) {
         actions.add(moveFile);
     }
 
     public void addRunScript(RunScript runScript) {
         actions.add(runScript);
     }
 
     public void addToggleSharedFolders(ToggleSharedFolders toggleSharedFolders) {
         actions.add(toggleSharedFolders);
     }
 
     public void addWaitForPowerState(WaitForPowerState waitForPowerState) {
         actions.add(waitForPowerState);
     }
 
     public void addWaitForProcess(WaitForProcess waitForProcess) {
         actions.add(waitForProcess);
     }
 
     public void addWaitForTools(WaitForTools waitForTools) {
         actions.add(waitForTools);
     }
 
     public void addPowerOn(PowerOn powerOn) {
         actions.add(powerOn);
     }
 
     public void addPowerOff(PowerOff stop) {
         actions.add(stop);
     }
 
     public void addTakeSnapshot(TakeSnapshot start) {
         actions.add(start);
     }
 
     public void addRevertToSnapshot(RevertToSnapshot start) {
         actions.add(start);
     }
 
     public void addDeleteSnapshot(DeleteSnapshot start) {
         actions.add(start);
     }
 
     public void addLogin(Login vixAction) {
         actions.add(vixAction);
     }
 
     public void addCreateDirectory(CreateDirectory vixAction) {
         actions.add(vixAction);
     }
 
     public void addPutFile(PutFile vixAction) {
         actions.add(vixAction);
     }
 
     public void addGetFile(GetFile vixAction) {
         actions.add(vixAction);
     }
 
     public void addRunProgram(RunProgram vixAction) {
         actions.add(vixAction);
     }
     
     public void addAddSharedFolder(AddSharedFolder vixAction) {
         actions.add(vixAction);
     }
     
     public void addClone(Clone vixAction) {
         actions.add(vixAction);
     }
     
     public void addDelete(Delete vixAction) {
         actions.add(vixAction);
     }
     
     public void addPause(Pause vixAction) {
         actions.add(vixAction);
     }
     
     public void addRemoveSharedFolder(RemoveSharedFolder vixAction) {
         actions.add(vixAction);
     }
     
     public void addReset(Reset vixAction) {
         actions.add(vixAction);
     }
     
     public void addSuspend(Suspend vixAction) {
         actions.add(vixAction);
     }
     
     public void addUnpause(Unpause vixAction) {
         actions.add(vixAction);
     }
     
     public void addReadVariable(ReadVariable vixAction) {
         actions.add(vixAction);
     }
     
     public void addSetSharedFolderWriteable(SetSharedFolderWriteable vixAction) {
         actions.add(vixAction);
     }
     
     public void addUpdateVirtualHardware(UpdateVirtualHardware vixAction) {
         actions.add(vixAction);
     }
     
     public void addWaitForDirectory(WaitForDirectory vixAction) {
         actions.add(vixAction);
     }
     
     public void addWaitForFile(WaitForFile vixAction) {
         actions.add(vixAction);
     }
     
     public void addWaitForVariableValue(WaitForVariableValue vixAction) {
         actions.add(vixAction);
     }
     
     public void addWriteVariable(WriteVariable vixAction) {
         actions.add(vixAction);
     }
 
     /*
      * Implementation of Ant task execute method
      */
     public void execute() throws BuildException {
 
         // set init params for helper
         if (vixPath != null && vixPath.length() > 0) {
             LibraryHelper.path = vixPath;
         }
         LibraryHelper.provider = serviceProvider == null ? "workstation" : serviceProvider;
 
         // get VIX provider code for specified string
         int provider;
         if (serviceProvider.equals("vi"))  {
             provider = Vix.VIX_SERVICEPROVIDER_VMWARE_VI_SERVER;
             log("VIX Service Provider is VI Server or VMWare Server 2.0", Project.MSG_INFO);
         }
         else if (serviceProvider.equals("server")) {
             provider = Vix.VIX_SERVICEPROVIDER_VMWARE_SERVER;
             log("VIX Service Provider is VMWare Server 1.0", Project.MSG_INFO);
         }
         else if (serviceProvider.equals("workstation")) {
             provider = Vix.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION;
             log("VIX Service Provider is VMWare Workstation", Project.MSG_INFO);
         }
         else if (serviceProvider.equals("workstation_shared")) {
             provider = Vix.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION_SHARED;
             log("VIX Service Provider is VMWare Workstation (shared mode)", Project.MSG_INFO);
         }
         else if (serviceProvider.equals("player")) {
             provider = Vix.VIX_SERVICEPROVIDER_VMWARE_PLAYER;
             log("VIX Service Provider is VMWare Player", Project.MSG_INFO);
         }
         else {
             log("Unknown service provider: " + serviceProvider + ", assuming workstation", Project.MSG_WARN);
             provider = Vix.VIX_SERVICEPROVIDER_VMWARE_WORKSTATION;
         }
 
         // if host is specified, it is remote session
         if (host != null) {
             log("Running remote session for host " + host + ":" + port, Project.MSG_INFO);
         }
         else {
             log("Running local session", Project.MSG_INFO);
             port = 0;
             user = null;
             password = null;
         }
         
         int jobHandle = Vix.VIX_INVALID_HANDLE;
         IntByReference hostHandlePtr = new IntByReference();
 
         log("Connecting", Project.MSG_INFO);
                 
         jobHandle = LibraryHelper.getInstance().VixHost_Connect(Vix.VIX_API_VERSION, provider,
                 host, port, user, password, 0, Vix.VIX_INVALID_HANDLE, null, null);
 
         int err = LibraryHelper.getInstance().VixJob_Wait(jobHandle,
                 Vix.VIX_PROPERTY_JOB_RESULT_HANDLE,
                 hostHandlePtr,
                 Vix.VIX_PROPERTY_NONE);
 
         LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
         checkError(err);
 
         if (operation.equals("OpenAndRun"))  {
             log("Opening VM [" + path + "]");
 
             Callback callback = new Callback(Vix.VIX_EVENTTYPE_JOB_COMPLETED, callbackTargetName,
                     getProject());
 
             jobHandle = LibraryHelper.getInstance().VixHost_OpenVM(hostHandlePtr.getValue(), path,
                     Vix.VIX_VMOPEN_NORMAL, Vix.VIX_INVALID_HANDLE,
                     callbackTargetName != null ? callback : null, null);
 
             IntByReference vmHandlePtr = new IntByReference();
 
             err = LibraryHelper.getInstance().VixJob_Wait(jobHandle,
                     Vix.VIX_PROPERTY_JOB_RESULT_HANDLE,
                     vmHandlePtr,
                     Vix.VIX_PROPERTY_NONE);
 
             LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
 
             checkError(err);
 
             // execute all subtasks
             for (VixAction vixAction: actions) {
                 vixAction.executeAction(vmHandlePtr.getValue());
             }
         }
         else if (operation.equals("Register")) {
             log("Registering VM [" + path + "]");
 
             Callback callback = new Callback(Vix.VIX_EVENTTYPE_JOB_COMPLETED, callbackTargetName,
                     getProject());
 
             jobHandle = LibraryHelper.getInstance().VixHost_RegisterVM(hostHandlePtr.getValue(), path,
                     callbackTargetName != null ? callback : null, null);
 
             err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
 
             LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
 
             checkError(err);
         }
         else if (operation.equals("Unregister")) {
             log("Unregistering VM [" + path + "]");
 
             Callback callback = new Callback(Vix.VIX_EVENTTYPE_JOB_COMPLETED, callbackTargetName,
                     getProject());
 
             jobHandle = LibraryHelper.getInstance().VixHost_UnregisterVM(hostHandlePtr.getValue(), path,
                     callbackTargetName != null ? callback : null, null);
 
             err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
 
             LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
 
             checkError(err);
         }
         else if (operation.equals("Foreach") && callbackTargetName != null) {
             log("Executing callback task " + callbackTargetName + " for all " + findType + " machines");
 
            foundVMs.clear();

            Callback callback = new Callback(Vix.VIX_EVENTTYPE_FIND_ITEM, null,
                     getProject(), new VixDiscoverySetPropertiesProc());
 
             jobHandle = LibraryHelper.getInstance().VixHost_FindItems(hostHandlePtr.getValue(),
                     findType.equals("running") ? Vix.VIX_FIND_RUNNING_VMS :
                         Vix.VIX_FIND_REGISTERED_VMS, Vix.VIX_INVALID_HANDLE, -1,
                    callback, null);
 
             err = LibraryHelper.getInstance().VixJob_Wait(jobHandle, Vix.VIX_PROPERTY_NONE);
 
             LibraryHelper.getInstance().Vix_ReleaseHandle(jobHandle);
 
             checkError(err);

            for (String vm : foundVMs) {
                getProject().setProperty("vix.found.vm", vm);
                if (callbackTargetName != null)
                    getProject().executeTarget(callbackTargetName);
            }
         }
         
         LibraryHelper.getInstance().VixHost_Disconnect(hostHandlePtr.getValue());
         LibraryHelper.getInstance().Vix_ReleaseHandle(hostHandlePtr.getValue());
     }
 
    private List<String> foundVMs = new ArrayList<String>();

     private class VixDiscoverySetPropertiesProc implements Callback.SetPropertiesProc {
 
         @Override
         public void setProperties(int handle, int eventType, int moreEventInfo, Pointer clientData) {
             PointerByReference url = new PointerByReference();
             int err = LibraryHelper.getInstance().Vix_GetProperties(moreEventInfo, Vix.VIX_PROPERTY_FOUND_ITEM_LOCATION,
                     url, Vix.VIX_PROPERTY_NONE);
             checkError(err);
 
            foundVMs.add(url.getValue().getString(0));
         }
     }
 
 
 }
