 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 package org.jdesktop.wonderland.modules.sas.server;
 
 import java.io.Serializable;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO;
 import org.jdesktop.wonderland.modules.appbase.server.cell.AppConventionalCellMO.AppServerLauncher;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 import com.sun.sgs.app.ManagedObject;
 import com.sun.sgs.app.AppContext;
 import java.util.HashMap;
 import java.util.LinkedList;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.CellManagerMO;
 
 /**
  * Provides the main server-side logic for SAS. This singleton contains the Registry,
  * the Distributor and all of the server-side communications components for SAS.
  *
  * @author deronj
  */
 
 @ExperimentalAPI
 public class SasServer implements ManagedObject, Serializable, AppServerLauncher {
 
     private static final Logger logger = Logger.getLogger(SasServer.class.getName());
 
     /** Helps clean up app info when the app is stopped by the cell. */
     private static class SasLaunchInfo implements Serializable{
         private AppConventionalCellMO cell;
         private String executionCapability;
         private ProviderProxy provider;
         private SasLaunchInfo (AppConventionalCellMO cell, String executionCapability, 
                                ProviderProxy provider) {
             this.cell = cell;
             this.executionCapability = executionCapability;
             this.provider = provider;
         }
     }
 
     static class LaunchRequest implements Serializable {
         CellID cellID;
         String executionCapability;
         String appName;
         String command;
         ProviderProxy provider;
 
         LaunchRequest (CellID cellID, String executionCapability, String appName, String command) {
             this.cellID = cellID;
             this.executionCapability = executionCapability;
             this.appName = appName;
             this.command = command;
         }
 
         void setProvider (ProviderProxy provider) {
             this.provider = provider;
         }
 
         @Override
         public String toString () {
             return "cell = " + cellID + ", executionCapability = " + executionCapability +
                 "appName = " + appName + ", command = " + command;
         }
     }
 
     /**
      * A map of the app launch requests in flight for various cells.
      * "In flight" means that the request has been sent to a provider.
      * Note: We manage things so that only one launch request can be in flight at a time 
      * for a particular app cell.
      */
     private HashMap<CellID,LaunchRequest> launchesInFlight = new HashMap<CellID,LaunchRequest>();
 
     /** 
      * A map of the SAS providers which have connected, indexed by their execution capabilities
      * Note: because a provider may support multiple capabilities it may appear on more than one list.
      */
     private HashMap<String,LinkedList<ProviderProxy>> execCapToProviderList = 
         new HashMap<String,LinkedList<ProviderProxy>>();
 
     /**
      * A list of the app launch requests which still must be honored.
      */
     private LaunchList pendingLaunches = new LaunchList();
 
     /**
      * A list of the app launch requests that have succeeded. This is the list
      * of currently running apps.
      */
      private LaunchList runningLaunches = new LaunchList();
 
     /**
      * Called when a new provider client connects to the SAS server.
      */
     public void providerConnected(WonderlandClientSender sender, WonderlandClientID clientID) {
         logger.info("**** Sas provider connected, clientID = " + clientID);
         
         // TODO: for now everything uses xremwin
         String execCap = "xremwin";
 
         ProviderProxy provider = new ProviderProxy(clientID, sender);
         provider.addExecutionCapability(execCap);
 
         // Add to execution capability list
         LinkedList<ProviderProxy> providers = execCapToProviderList.get(execCap);
         if (providers == null) {
             providers = new LinkedList<ProviderProxy>();
             execCapToProviderList.put(execCap, providers);
         }
         providers.add(provider);
 
         logger.info("**** provider added to xremwin list, clientID = " + clientID);
 
         // See if there are any pending launches
         try {
             tryPendingLaunches(execCap);
         } catch (InstantiationException ie) {
             logger.warning("Exception during new provider connection execution of pending launches.");
             logger.warning("Exception = " + ie);
         }
 
         // Mark server modified
         AppContext.getDataManager().markForUpdate(this);
     }
 
     /**
      * Called when provider client disconnects from the SAS server.
      */
     ProviderProxy providerDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
         logger.info("**** Sas provider disconnnected, clientID = " + clientID);
         ProviderProxy providerToRemove = null;
 
         // TODO: for now everything uses xremwin
         String execCap = "xremwin";
 
         // Remove provider from execution capability list 
         LinkedList<ProviderProxy> providers = execCapToProviderList.get(execCap);
         if (providers != null) {
             for (ProviderProxy provider : providers) {
                 if (provider.getClientID().equals(clientID)) {
                     providerToRemove = provider;
                     provider.cleanup();
                     persistProviderApps(provider, execCap);
                     break;
                 }
             }
             if (providerToRemove != null) {
                 providers.remove(providerToRemove);
             }
 
             if (providers.size() <= 0) {
                 execCapToProviderList.remove(execCap);
             }
         }
 
         // Mark server modified
         AppContext.getDataManager().markForUpdate(this);
 
         return providerToRemove;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object appLaunch (AppConventionalCellMO cell, String executionCapability, String appName, 
                              String command) 
         throws InstantiationException 
     {
         logger.info("***** appLaunch, command = " + command);
 
         CellID cellID = cell.getCellID();
 
         // Construct the launch request
         LaunchRequest launchReq = new LaunchRequest(cellID, executionCapability, appName, command);
 
         // TODO: someday: allow multiple apps to be launched per cell.
 
         LinkedList<ProviderProxy> providers = execCapToProviderList.get(executionCapability);
         if (providers == null || providers.size() <= 0) {
             // No provider. Launch must pend
             logger.warning("No SAS provider for " + executionCapability + " is available.");
             logger.warning("Launch attempt will pend.");
             pendingLaunches.add(launchReq);
             AppContext.getDataManager().markForUpdate(this);
             return new SasLaunchInfo(cell, executionCapability, null);
         }
 
         // TODO: someday: Right now we just try only the first provider. Eventually try multiple providers.
         ProviderProxy provider = providers.getFirst();
         if (provider == null) {
             throw new InstantiationException("Cannot find a provider for " + executionCapability);
         }
 
         // Now request the provider to launch the app
         launchReq.setProvider(provider);
         launchesInFlight.put(cellID, launchReq);
         provider.tryLaunch(cellID, executionCapability, appName, command);
 
         return new SasLaunchInfo(cell, executionCapability, provider);
     }
         
     /**
      * Called by the provider proxy to report the result of a launch
      */
     public void appLaunchResult (AppServerLauncher.LaunchStatus status, CellID cellID, String connInfo) {
         logger.info("############### SasServer: Launch result received");
         logger.info("status = " + status);
         logger.info("cellID = " + cellID);
         logger.info("connInfo = " + connInfo);
 
         // Get the request that we used to launch the app
         LaunchRequest launchReq = launchesInFlight.get(cellID);
         if (launchReq == null) {
             logger.warning("Cannot app request launch for cell " + cellID);
             return;
         }
         launchesInFlight.remove(cellID);
         AppContext.getDataManager().markForUpdate(this);
 
         CellMO cell = CellManagerMO.getCell(cellID);
         if (cell == null) {
             logger.warning("Cannot find cell to which to report app launch result, launch request = " + 
                            launchReq);
             return;
         }
         if (!(cell instanceof AppConventionalCellMO)) {
             logger.warning("Cell reported in app launch result is not an AppConventionalMO, launch request = " + 
                            launchReq);
             return;
         }
 
         // TODO: someday: probably shouldn't do this if the provider tried to launch and it failed.
         // Probably should only do this if the provider wouldn't or couldn't launch for some reason.
         if (status != AppServerLauncher.LaunchStatus.SUCCESS || connInfo == null) {
             // The provider we tried cannot launch. Launch must pend.
             logger.warning("SAS provider launch failed with status " + status + 
                            " and connection info " + connInfo +
                            " for launch request = " + launchReq);
             logger.warning("Launch attempt will pend until a provider is found.");
             pendingLaunches.add(launchReq);
             // Note: server has already been marked for update above
 
             // TODO: someday: at some point we need to give up and call cell.appLaunchResult with a failure 
             // status. Need to implement a timeout.
             return;
         }
 
         // The app is now running
         runningLaunches.add(launchReq);
 
         ((AppConventionalCellMO)cell).appLaunchResult(status, connInfo);
     }
     
     /**
      * {@inheritDoc}
      */
     public void appStop (Object launchInfo) {
         SasLaunchInfo sasLaunchInfo = (SasLaunchInfo) launchInfo;
 
         CellID cellID = sasLaunchInfo.cell.getCellID();
 
         // First, remove cell from the launches in flight map.
         launchesInFlight.remove(cellID);
 
         // Next, remove cell from the list of running apps
         runningLaunches.remove(cellID, sasLaunchInfo.executionCapability);
 
         // Next, remove cell from pending launch list. 
         // TODO: someday: For now, this code assumes only one app launch per cell.
         pendingLaunches.remove(cellID, sasLaunchInfo.executionCapability);
 
         AppContext.getDataManager().markForUpdate(this);
 
         // Finally tell the provider to stop the app.
         // 
         // If the provider was determined when appLaunch() was called, tell it to stop the app. */
         // But if the app had to pend waiting for a provider, we must notify all providers to 
         // see which one launched the app.
         if (sasLaunchInfo.provider != null) {
             sasLaunchInfo.provider.appStop(sasLaunchInfo.cell);
         } else {
             LinkedList<ProviderProxy> providers = 
                 execCapToProviderList.get(sasLaunchInfo.executionCapability);
             if (providers != null) {
                 for (ProviderProxy provider : providers) {
                     provider.appStop(sasLaunchInfo.cell);
                 }
             }
         }
     }
 
     private void tryPendingLaunches (String executionCapability) throws InstantiationException {
         LinkedList<LaunchRequest> reqs = pendingLaunches.getLaunches(executionCapability);
         if (reqs == null) {
             return;
         }
         LinkedList<LaunchRequest> reqsForTraversal = (LinkedList<LaunchRequest>) reqs.clone();
         
         for (LaunchRequest req : reqsForTraversal) {
 
             // TODO: Some of this code is dup from above in tryLaunch; share it
 
             // See if there are any more providers to try
             LinkedList<ProviderProxy> providers = execCapToProviderList.get(executionCapability);
             if (providers == null || providers.size() <= 0) {
                 continue;
             }
             // TODO: someday: weed out providers already tried
             ProviderProxy provider = providers.getFirst();
 
             // Remove request from pending list while it is in flight */
             reqs.remove(req);
 
             // Now request the newly selected provider to launch the app
             launchesInFlight.put(req.cellID, req);
             provider.tryLaunch(req.cellID, req.executionCapability, req.appName, req.command);
         }
     }
 
     /**
      * Make the currently running apps persist by transferring them to the pending
      * launches list. They will be rerun the next time a suitable provider connects.
      * @param provider The provider whose running apps should be persisted.
      * @param execCap The execution capability of the running apps that should be persisted.
      */
     private void persistProviderApps (ProviderProxy provider, String execCap) {
 
         // First, persist the in-flight launches (i.e. the launches that have been requested
         // but have not yet occurred.
         LinkedList<CellID> cellsToRemove = new LinkedList<CellID>();
         for (CellID cellID : launchesInFlight.keySet()) {
             LaunchRequest launchReq = launchesInFlight.get(cellID);
             if (launchReq != null && launchReq.cellID == cellID && launchReq.provider == provider) {
                 logger.warning("Persisting in-flight app to pending list, appName = " + launchReq.appName);
                 pendingLaunches.add(launchReq);
                 cellsToRemove.add(cellID);
             }
         }
         for (CellID cellID : cellsToRemove) {
             launchesInFlight.remove(cellID);
         }
         cellsToRemove.clear();
 
         // Next, persist the running apps.
         LinkedList<LaunchRequest> launches = runningLaunches.getLaunches(execCap);
        for (LaunchRequest launchReq : launches) {
            logger.warning("Persisting running app to pending list, appName = " + launchReq.appName);
            pendingLaunches.add(launchReq);
            cellsToRemove.add(launchReq.cellID);
        }
         for (CellID cellID : cellsToRemove) {
            runningLaunches.remove(cellID, execCap);
         }
        cellsToRemove.clear();
 
         AppContext.getDataManager().markForUpdate(this);
     }
 }
 
