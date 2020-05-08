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
 package org.jdesktop.wonderland.modules.microphone.client.cell;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.sun.sgs.client.ClientChannel;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jdesktop.wonderland.common.messages.Message;
 
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.Cell.RendererType;
 import org.jdesktop.wonderland.client.cell.CellCache;
 import org.jdesktop.wonderland.client.cell.CellManager;
 import org.jdesktop.wonderland.client.cell.CellRenderer;
 import org.jdesktop.wonderland.client.cell.CellStatusChangeListener;
 
 import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellStatus;
 
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 
 import org.jdesktop.wonderland.client.comms.ClientConnection;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 
 import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
 import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
 import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
 import org.jdesktop.wonderland.modules.microphone.common.MicrophoneCellClientState;
 
 /**
  *
  * @author jkaplan
  */
 public class MicrophoneCell extends Cell implements CellStatusChangeListener {
 
     private static final Logger logger =
             Logger.getLogger(MicrophoneCell.class.getName());
     private MicrophoneMessageHandler microphoneMessageHandler;
 
     public MicrophoneCell(CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
 
         logger.fine("CREATED NEW CONEOFSILENCE CELL " + cellID);
 
         CellManager.getCellManager().addCellStatusChangeListener(this);
     }
 
     public void cellStatusChanged(Cell cell, CellStatus status) {
         logger.fine("got status " + status + " for cell " + cell.getCellID());
 
         if (cell.getCellID() != getCellID()) {
             return;
         }
 
         if (status.equals(CellStatus.INACTIVE) && microphoneMessageHandler == null) {
             microphoneMessageHandler = new MicrophoneMessageHandler(this);
         } else if (status.equals(CellStatus.DISK) && microphoneMessageHandler != null) {
             microphoneMessageHandler.done();
             microphoneMessageHandler = null;
         }
     }
 
     /**
      * Called when the cell is initially created and any time there is a 
      * major configuration change. The cell will already be attached to it's parent
      * before the initial call of this method
      * 
      * @param setupData
      */
     @Override
     public void setClientState(CellClientState cellClientState) {
         super.setClientState(cellClientState);
 
         MicrophoneCellClientState microphoneCellClientState = (MicrophoneCellClientState) cellClientState;
     }
 
     public WonderlandSession getSession() {
         return getCellCache().getSession();
     }
 
     @Override
     protected CellRenderer createCellRenderer(RendererType rendererType) {
         if (rendererType == RendererType.RENDERER_JME) {
             try {
                 DeployedModel m =
                       LoaderManager.getLoaderManager().getLoaderFromDeployment(AssetUtils.getAssetURL("wla://microphone/pwl_3d_mic-stand_014.dae/pwl_3d_mic-stand_014.dae.gz.dep"));
                 return new ModelRenderer(this, m);
             } catch (MalformedURLException ex) {
                 Logger.getLogger(MicrophoneCell.class.getName()).log(Level.SEVERE, "Failed to load microphone model", ex);
             } catch(IOException ioe) {
                 Logger.getLogger(MicrophoneCell.class.getName()).log(Level.SEVERE, "Failed to load microphone model", ioe);
             }
         }
 
         logger.warning(this.getClass().getName() + " does not support " + rendererType);
         return null;
     }
 }
