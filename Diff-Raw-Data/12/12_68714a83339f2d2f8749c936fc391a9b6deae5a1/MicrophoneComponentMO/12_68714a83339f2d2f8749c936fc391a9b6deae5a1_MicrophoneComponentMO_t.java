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
 
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 
 import org.jdesktop.wonderland.server.cell.CellMO;
 import org.jdesktop.wonderland.server.cell.CellComponentMO;
 import org.jdesktop.wonderland.server.cell.ProximityComponentMO;
 
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
 
 import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState;
 import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.MicrophoneBoundsType;
 import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.ActiveArea;
 import org.jdesktop.wonderland.modules.audiomanager.common.MicrophoneComponentServerState.FullVolumeArea;
 
 import java.util.logging.Logger;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.bounding.BoundingVolume;
 
 import com.jme.math.Vector3f;
 
 /**
  * A server component that provides microphone functionality
  * @author jprovino
  */
 public class MicrophoneComponentMO extends CellComponentMO {
 
     private static final Logger logger =
         Logger.getLogger(MicrophoneComponentMO.class.getName());
      
     private String name = "MICROPHONE";
 
     private double volume = 1;
 
     private FullVolumeArea fullVolumeArea = new FullVolumeArea();
 
     private ActiveArea activeArea = new ActiveArea();
 
     private MicrophoneEnterProximityListener enterProximityListener;
     private MicrophoneActiveAreaProximityListener activeAreaProximityListener;
 
     public MicrophoneComponentMO(CellMO cellMO) {
         super(cellMO);
 
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
 
 	MicrophoneComponentServerState state = (MicrophoneComponentServerState) serverState;
 
	name = state.getName() + "-" + cellRef.get().getCellID();
	
 	System.out.println("name " + state.getName() + " volume " + state.getVolume()
 	    + " fva " + state.getFullVolumeArea() + " aa " + state.getActiveArea());
 
 	volume = state.getVolume();
 
 	fullVolumeArea = state.getFullVolumeArea();
 
 	activeArea = state.getActiveArea();
 
 	addProximityListeners(isLive());
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     public CellComponentServerState getServerState(CellComponentServerState serverState) {
         MicrophoneComponentServerState state = (MicrophoneComponentServerState) serverState;
 
         // Create the proper server state object if it does not yet exist
         if (state == null) {
             state = new MicrophoneComponentServerState();
         }
 
 	state.setName(name);
 	state.setVolume(volume);
 	state.setFullVolumeArea(fullVolumeArea);
 	state.setActiveArea(activeArea);
 
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
         return "org.jdesktop.wonderland.modules.audiomanager.client.MicrophoneComponent";
     }
 
     /**
      * @{inheritDoc}
      */
     @Override
     public void setLive(boolean live) {
         super.setLive(live);
 
         addProximityListeners(live);
     }
 
     private void addProximityListeners(boolean live) {
         // Fetch the proximity component, we will need this below. If it does
         // not exist (it should), then log an error
         ProximityComponentMO component = cellRef.get().getComponent(ProximityComponentMO.class);
         if (component == null) {
             logger.warning("The Microphone Component does not have a " +
                     "Proximity Component for Cell ID " + cellID);
             return;
         }
 
         if (enterProximityListener != null) {
             component.removeProximityListener(enterProximityListener);
             component.removeProximityListener(activeAreaProximityListener);
         }
 
         // If we are making this component live, then add a listener to the proximity component.
         if (live == true) {
             BoundingVolume[] bounds = new BoundingVolume[1];
 
             if (fullVolumeArea.boundsType.equals(MicrophoneBoundsType.CELL_BOUNDS)) {
                 bounds[0] = cellRef.get().getLocalBounds();
                 logger.warning("Microphone Using cell bounds:  " + bounds[0]);
             } else if (fullVolumeArea.boundsType.equals(MicrophoneBoundsType.SPHERE)) {
                 bounds[0] = new BoundingSphere((float) fullVolumeArea.bounds.getX() / 2, 
 		    new Vector3f());
                 logger.warning("Microphone Using radius:  " + bounds[0]);
             } else {
                 bounds[0] = new BoundingBox(new Vector3f(), fullVolumeArea.bounds.getX(),
                     fullVolumeArea.bounds.getY(), fullVolumeArea.bounds.getZ());
             }
 
             enterProximityListener = new MicrophoneEnterProximityListener(cellRef.get(), name, volume);
             component.addProximityListener(enterProximityListener, bounds);
 
             Vector3f activeOrigin = new Vector3f((float) activeArea.activeAreaOrigin.getX(),
                                                  (float) activeArea.activeAreaOrigin.getY(),
                                                  (float) activeArea.activeAreaOrigin.getZ());
 
             if (activeArea.activeAreaBoundsType.equals(MicrophoneBoundsType.SPHERE)) {
                 bounds[0] = new BoundingSphere((float) 2, activeOrigin);
             } else {
                 bounds[0] = new BoundingBox(activeOrigin, (float) activeArea.activeAreaBounds.getX(),
                                                           (float) activeArea.activeAreaBounds.getY(),
                                                           (float) activeArea.activeAreaBounds.getZ());
             }
 
             activeAreaProximityListener = new MicrophoneActiveAreaProximityListener(cellRef.get(), name, volume);
             component.addProximityListener(activeAreaProximityListener, bounds);
         } else {
             if (enterProximityListener != null) {
                 enterProximityListener.remove();
                 enterProximityListener = null;
                 activeAreaProximityListener.remove();
                 activeAreaProximityListener = null;
             }
         }
     }
 
 }
