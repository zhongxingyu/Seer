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
 package org.jdesktop.wonderland.modules.appbase.server;
 
 import java.io.Serializable;
 import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellClientState;
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.modules.appbase.common.AppConventionalCellServerState;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.CommsManager;
 import org.jdesktop.wonderland.server.WonderlandContext;
 
 /**
  * TODO: rework
  * The server-side cell for an 2D conventional application.
  *
  * This cell can be created in two different ways:
  * <br><br>
  * 1. World-launched App
  * <br><br>
  * When WFS launches the app it uses the default constructor and
  * calls <code>setServerState</code> to transfer the information from the wlc file
  * into the cell. 
  * <br><br>
  * In this case the wlc <code>setServerState</code> must specify:
  * <ol>
  * + command: The command to execute. This must not be a non-empty string.         
  * </ol>
  * The wlc <code>setServerState</code> can optionally specify:
  * <ol>
  * + <code>appName</code>: The name of the application (Default: "NoName").
  * </ol>
  * <ol>
  * + <code>pixelScaleX</code>: The number of world units per pixel in the cell local X direction (Default: 0.01).
  * </ol>
  * <ol>
  * + <code>pixelScaleY</code> The number of world units per pixel in the cell local Y direction (Default: 0.01).
  * </ol>
  * In this case <code>userLaunched</code> is set to false.
  *<br><br>
  * 2. User-launched App
  *<br><br> 
  * When the user launches an app it sends a command to the server. The handler for
  * this command uses the non-default constructor of this class to provide the
  * necessary information to the client
  *<br><br>
  * In this case <code>userLaunched</code> is set to true.
  *
  * @author deronj
  */
 @ExperimentalAPI
 public abstract class AppConventionalCellMO extends App2DCellMO {
 
     private static final Logger logger = Logger.getLogger(AppConventionalCellMO.class.getName());
     /** The parameters from the WFS file. */
     AppConventionalCellServerState serverState;
     /** The parameters given to the client. */
     AppConventionalCellClientState clientState;
     /** Whether the client connection handler has been registered. */
     private boolean connectionHandlerRegistered;
     /** Subclass-specific data for making a peer-to-peer connection between master and slave. */
     protected Serializable connectionInfo;
 
     /** Create an instance of AppConventionalCellMO. */
     public AppConventionalCellMO() {
         super();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void setLive(boolean live) {
        super.setLive(live);

         // Register the connection handler when the first cell is created
         if (live) {
             if (!connectionHandlerRegistered) {
                 CommsManager cm = WonderlandContext.getCommsManager();
                 //TODO: cm.registerClientHandler(new AppConnectionHandlerXrw());
                 connectionHandlerRegistered = true;
             }
         } else {
             if (connectionHandlerRegistered) {
                 // TODO: how to unregister?
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setServerState(CellServerState state) {
         super.setServerState(state);
         serverState = (AppConventionalCellServerState) state;
 
         // Validate WFS parameters
         // TODO: what is the proper way to signal this error which is non-fatal to the server?
 
         /*
         String appName = serverState.getAppName();
         if (appName == null || appName.length() <= 0) {
             String msg = "Invalid app name";
             logger.severe(msg);
             throw new RuntimeException(msg);
         }
 
         String launchLocation = serverState.getLaunchLocation();
         if (!"user".equalsIgnoreCase(launchLocation) &&
             !"server".equalsIgnoreCase(launchLocation)) {
             String msg = "Invalid launch location: " + launchLocation;
             logger.severe(msg);
             throw new RuntimeException(msg);
         }
 
         if ("server".equalsIgnoreCase(launchLocation)) {
             String launchUser = serverState.getLaunchUser();
             if (launchUser == null || launchUser.length() <= 0) {
                 String msg = "Invalid app launch user";
                 logger.severe(msg);
                 throw new RuntimeException(msg);
             }
         }
 
         String command = serverState.getCommand();
         if (command == null || command.length() <= 0) {
             String msg = "Invalid app command";
             logger.severe(msg);
             throw new RuntimeException(msg);
         }
         */
     }
 
     /** 
      * {@inheritDoc}
      */
     @Override
     protected CellClientState getClientState(CellClientState cellClientState,
             WonderlandClientID clientID, ClientCapabilities capabilities) {
         if (clientState == null) {
             clientState = new AppConventionalCellClientState();
         }
         populateClientState(clientState);
         return clientState;
     }
 
     /**
      * Fill in the given client state with this cell's state.
      * @param clientState The client state whose properties are to be set.
      */
     protected void populateClientState(AppConventionalCellClientState clientState) {
         super.populateClientState(clientState);
         clientState.setAppName(serverState.getAppName());
         clientState.setLaunchLocation(serverState.getLaunchLocation());
         clientState.setLaunchUser(serverState.getLaunchUser());
         clientState.setBestView(serverState.isBestView());
         clientState.setCommand(serverState.getCommand());
         clientState.setConnectionInfo(connectionInfo);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public CellServerState getServerState(CellServerState stateToFill) {
         if (stateToFill == null) {
             return null;
         }
 
         super.getServerState(stateToFill);
 
         AppConventionalCellServerState state = (AppConventionalCellServerState) stateToFill;
         state.setAppName(serverState.getAppName());
         state.setLaunchLocation(serverState.getLaunchLocation());
         state.setLaunchUser(serverState.getLaunchUser());
         state.setBestView(serverState.isBestView());
         state.setCommand(serverState.getCommand());
 
         return stateToFill;
     }
 }
