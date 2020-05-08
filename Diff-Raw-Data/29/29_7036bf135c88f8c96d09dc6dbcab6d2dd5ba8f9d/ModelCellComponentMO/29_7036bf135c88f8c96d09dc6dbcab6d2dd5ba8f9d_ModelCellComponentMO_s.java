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
 package org.jdesktop.wonderland.server.cell;
 
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.ClientCapabilities;
 import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellComponentClientState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellComponentServerState;
 import org.jdesktop.wonderland.server.comms.WonderlandClientID;
 
 /**
  *
  * @author paulby
  */
 public class ModelCellComponentMO extends CellComponentMO {
 
     protected ModelCellComponentServerState serverState = null;
 
     public ModelCellComponentMO(CellMO cell) {
         super(cell);
     }
     @Override
     protected String getClientClass() {
         return "org.jdesktop.wonderland.client.cell.ModelCellComponent";
     }
 
     @Override
     public void setServerState(CellComponentServerState state) {
         if (!(state instanceof ModelCellComponentServerState)) {
             Logger.getLogger(this.getClass().getName()).warning("Incorrect server state passed to setServerState "+state.getClass().getName());
             return;
         }
         this.serverState = (ModelCellComponentServerState) state;
     }
 
     @Override
     public CellComponentServerState getServerState(CellComponentServerState state) {
         return serverState.clone(state);
     }
 
     @Override
     public CellComponentClientState getClientState(CellComponentClientState state,
             WonderlandClientID clientID,
             ClientCapabilities capabilities) {
 
 
         // If the given cellClientState is null, create a new one
         if (state == null) {
             state = new ModelCellComponentClientState();
         }
 
         serverState.setClientState((ModelCellComponentClientState)state);
         System.err.println("******** getClientState "+serverState.getDeployedModelURL());
 
         return state;
     }
 }
