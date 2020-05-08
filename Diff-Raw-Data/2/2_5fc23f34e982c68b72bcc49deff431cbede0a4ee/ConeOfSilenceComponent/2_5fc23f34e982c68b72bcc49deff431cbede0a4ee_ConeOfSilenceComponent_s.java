 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.audiomanager.client;
 
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.CellComponent;
 import org.jdesktop.wonderland.client.cell.ChannelComponent;
 import org.jdesktop.wonderland.client.cell.ProximityComponent;
 import org.jdesktop.wonderland.client.cell.ProximityListener;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 import org.jdesktop.wonderland.common.cell.messages.CellMessage;
 import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
 import com.jme.bounding.BoundingVolume;
 
 import org.jdesktop.wonderland.common.cell.messages.CellServerComponentMessage;
 
 /**
  * A component that provides a cone of silence
  * 
  * @author jprovino
  */
 @ExperimentalAPI
 public class ConeOfSilenceComponent extends CellComponent implements ProximityListener {
     
     private static Logger logger = Logger.getLogger(ConeOfSilenceComponent.class.getName());
 
     private ChannelComponent channelComp;
     private ChannelComponent.ComponentMessageReceiver msgReceiver;
     
     public ConeOfSilenceComponent(Cell cell) {
         super(cell);
 
 	ProximityComponent comp = new ProximityComponent(cell);
 
 	BoundingVolume[] boundingVolume = new BoundingVolume[1];
 
 	boundingVolume[0] = cell.getLocalBounds();
 
 	comp.addProximityListener(this, boundingVolume);
 
 	cell.addComponent(comp);
     }
     
     @Override
     public void setStatus(CellStatus status) {
         switch(status) {
 	case DISK:
 	    if (msgReceiver != null) {
                 channelComp.removeMessageReceiver(CellServerComponentMessage.class);
                 msgReceiver = null;
             }
             break;
 
 	case BOUNDS:
 	    if (msgReceiver==null) {
                 msgReceiver = new ChannelComponent.ComponentMessageReceiver() {
                     public void messageReceived(CellMessage message) {
                     }
                }
 	    }
 
             channelComp = cell.getComponent(ChannelComponent.class);
             channelComp.addMessageReceiver(CellServerComponentMessage.class, msgReceiver);
             break;
         }
     }
 
     @Override
     public void setClientState(CellComponentClientState clientState) {
         super.setClientState(clientState);
     }
 
     public void viewEnterExit(boolean entered, Cell cell, CellID viewCellID, BoundingVolume proximityVolume,
             int proximityIndex) {
 
         logger.info("COS cellID " + cell.getCellID() + " viewCellID " + viewCellID
  + " entered = " + entered);
     }
 
 }
