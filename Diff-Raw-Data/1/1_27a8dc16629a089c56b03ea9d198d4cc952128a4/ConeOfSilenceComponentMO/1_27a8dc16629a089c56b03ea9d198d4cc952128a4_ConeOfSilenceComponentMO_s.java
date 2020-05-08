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
 package org.jdesktop.wonderland.modules.audiomanager.server;
 
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 
 import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.CellComponentMO;
 import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
 
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.ConeOfSilenceComponentServerState;
 
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 
 import com.jme.math.Vector3f;
 
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedReference;
 
 /**
  *
  * @author jprovino
  */
 public class ConeOfSilenceComponentMO extends CellComponentMO {
 
     private static final Logger logger =
             Logger.getLogger(ConeOfSilenceComponentMO.class.getName());
 
     private String name = "COS";
 
     private boolean useCellBounds;
 
     private double fullVolumeRadius = 1.6;
 
     private double outsideAudioVolume = 0;
 
     //ConeOfSilenceProximityListener proximityListener;
 
     /**
      * Create a ConeOfSilenceComponent for the given cell. 
      * @param cell
      */
     public ConeOfSilenceComponentMO(CellMO cellMO) {
         super(cellMO);
 
         // The Cone of Silence Components depends upon the Proximity Component.
         // We add this component as a dependency if it does not yet exist
         if (cellMO.getComponent(ProximityComponentMO.class) == null) {
             cellMO.addComponent(new ProximityComponentMO(cellMO));
         }
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     public void setServerState(CellComponentServerState serverState) {
         super.setServerState(serverState);
 
         // Fetch the component-specific state and set member variables
         ConeOfSilenceComponentServerState cs = (ConeOfSilenceComponentServerState) serverState;
 
 	name = cs.getName();
 
 	useCellBounds = cs.getUseCellBounds();
 
         fullVolumeRadius = cs.getFullVolumeRadius();
 
 	outsideAudioVolume = cs.getOutsideAudioVolume();
 
 	addProximityListener(isLive());
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     public CellComponentServerState getServerState(CellComponentServerState serverState) {
 	ConeOfSilenceComponentServerState state = (ConeOfSilenceComponentServerState) serverState;
 
         // Create the proper server state object if it does not yet exist
         if (state == null) {
             state = new ConeOfSilenceComponentServerState();
         }
 
         state.setName(name);
 	state.setUseCellBounds(useCellBounds);
         state.setFullVolumeRadius(fullVolumeRadius);
         state.setOutsideAudioVolume(outsideAudioVolume);
 
         return super.getServerState(state);
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     public CellComponentClientState getClientState(CellComponentClientState state,
             WonderlandClientID clientID,
             ClientCapabilities capabilities) {
 
         // TODO: Create own client state object?
         return super.getClientState(state, clientID, capabilities);
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     protected String getClientClass() {
         return "org.jdesktop.wonderland.modules.audiomanager.client.ConeOfSilenceComponent";
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     public void setLive(boolean live) {
         super.setLive(live);
 
 	addProximityListener(live);
     }
 
     ConeOfSilenceProximityListener proximityListener;
 
     private void addProximityListener(boolean live) {
         // Fetch the proximity component, we will need this below. If it does
         // not exist (it should), then log an error
         ProximityComponentMO component = cellRef.get().getComponent(ProximityComponentMO.class);
         if (component == null) {
             logger.warning("The Cone of Silence Component does not have a " +
                     "Proximity Component for Cell ID " + cellID);
             return;
         }
 
 	if (proximityListener != null) {
 	    component.removeProximityListener(proximityListener);
 	}
 
         // If we are making this component live, then add a listener to the proximity component.
         if (live == true) {
             BoundingVolume[] bounds = new BoundingVolume[1];
 
 	    if (useCellBounds == true) {
 		bounds[0] = cellRef.get().getLocalBounds();
 		logger.warning("COS Using cell bounds:  " + bounds[0]);
 	    } else {
                 bounds[0] = new BoundingSphere((float) fullVolumeRadius, new Vector3f());
 		logger.warning("COS Using radius:  " + bounds[0]);
 	    }
 
             proximityListener = new ConeOfSilenceProximityListener(cellRef.get(), name, outsideAudioVolume);
 
             component.addProximityListener(proximityListener, bounds);
         }
     }
 
 }
