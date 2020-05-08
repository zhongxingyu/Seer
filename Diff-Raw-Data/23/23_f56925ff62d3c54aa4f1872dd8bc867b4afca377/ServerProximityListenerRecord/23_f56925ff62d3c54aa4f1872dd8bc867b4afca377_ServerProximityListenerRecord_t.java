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
 
 import com.jme.bounding.BoundingVolume;
 import com.sun.sgs.app.AppContext;
 import com.sun.sgs.app.DataManager;
 import com.sun.sgs.app.ManagedReference;
 import com.sun.sgs.app.util.ScalableHashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 import org.jdesktop.wonderland.common.cell.ProximityListenerRecord;
 import org.jdesktop.wonderland.server.spatial.UniverseManager;
 import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;
 
 /**
  * This listener record provides the server specific hooks for the generic 
  * proximity listener code, which is shared with the client code.
  *
  * @author paulby
  */
 public class ServerProximityListenerRecord extends ProximityListenerRecord implements TransformChangeListenerSrv, ViewUpdateListener {
     private static final Logger logger =
             Logger.getLogger(ServerProximityListenerRecord.class.getName());
 
     private static final String BINDING_NAME = ServerProximityListenerRecord.class.getName();
     private ServerProximityListenerWrapper wrapper;
     private String id;
 
     ServerProximityListenerRecord(ServerProximityListenerWrapper proximityListener, BoundingVolume[] localBounds, String id) {
         super(proximityListener, localBounds);        
 
         this.wrapper = proximityListener;
         this.id = id;
     }
 
     public void viewTransformChanged(CellID cell, CellID viewCellID, CellTransform viewWorldTransform) {
         viewCellMoved(viewCellID, viewWorldTransform);
     }
 
     public void viewLoggedIn(CellID cell, CellID viewCellID) {
         // ignore
     }
 
     public void viewLoggedOut(CellID cell, CellID viewCellID) {
         logger.finest("View cell " + viewCellID + " exited on " + this);
 
         // remove view
         viewCellExited(viewCellID);
 
         // the wrapper may have more information in this case, for example
         // after a warm start, listeners persisted in the datastore
         wrapper.viewCellExited(viewCellID, getWorldBounds());
     }
 
     public void transformChanged(ManagedReference<CellMO> cellRef, CellTransform localTransform, CellTransform worldTransform) {
         if (logger.isLoggable(Level.FINEST)) {
             logger.finest("Transform changed: " + localTransform + " " +
                           worldTransform + " on " + this);
         }
 
         updateWorldBounds(worldTransform);
     }
 
     void setLive(boolean isLive, final CellMO cell, UniverseManager mgr) {
         DataManager dm = AppContext.getDataManager();
 
         if (isLive) {
             Map<CellID, Integer> indexMap = new ScalableHashMap<CellID, Integer>();
             dm.setBinding(BINDING_NAME + id, indexMap);
 
             mgr.addTransformChangeListener(cell, this);
             mgr.addViewUpdateListener(cell, this);
 
            // Issue #721: we need to set the transform to the cell's transform
            // here, since the cell may already exist and we can't count on
            // getting a transform changed notification until the cell moves
            updateWorldBounds(cell.getWorldTransform(null));
         } else {
             mgr.removeTransformChangeListener(cell, this);
             mgr.removeViewUpdateListener(cell, this);
 
             dm.removeBinding(BINDING_NAME + id);
         }
     }
 
     /**
      * Internal structure containing the array of bounds for a given listener.
      * This class keeps a map from cell id to bounds index for all listeners
      * in the bounds of this cell.  This map is kept as a Darkstar managed
      * object, and is used during warm restart to remember what listeners
      * were previously available and clean them up (during the call to
      * viewCellExited() when the view for that cell is cleaned up).  The map is
      * kept here because the listener is only notified when there is a change,
      * so this minimizes the number of calls to Darkstar managed objects.
      */
     static class ServerProximityListenerWrapper implements ProximityListenerRecord.ProximityListenerWrapper {
 
         private ProximityListenerSrv listener;
         private CellID cellID;
         private String id;
 
         public ServerProximityListenerWrapper(CellID cell, ProximityListenerSrv listener, String id) {
             this.listener = listener;
             this.cellID = cell;
             this.id = id;
         }
 
         CellID getCellID() {
             return cellID;
         }
 
         public void viewEnterExit(boolean enter,
                                   BoundingVolume proximityVolume,
                                   int proximityIndex,
                                   CellID viewCellID)
         {
             listener.viewEnterExit(enter, cellID, viewCellID, proximityVolume,
                                    proximityIndex);
         
             int curIndex = proximityIndex;
             if (!enter) {
                 curIndex -= 1;
             }
             
             if (curIndex == -1) {
                 getIndexMap().remove(viewCellID);
             } else {
                 getIndexMap().put(viewCellID, curIndex);
             }
         }
 
         public void viewCellExited(CellID viewCellID, BoundingVolume[] bounds) {
             if (getIndexMap().containsKey(viewCellID)) {
                 int lastIndex = getIndexMap().remove(viewCellID);
 
                 // notify exit for each bounds
                 for (int i = lastIndex; i >= 0; i--) {
                     listener.viewEnterExit(false, cellID, viewCellID,
                             bounds[i], i);
                 }
             }
         }
 
         Map<CellID, Integer> getIndexMap() {
             DataManager dm = AppContext.getDataManager();
             return (Map<CellID, Integer>) dm.getBinding(BINDING_NAME + id);
         }
 
         @Override
         public boolean equals(Object o) {
             if (!(o instanceof ServerProximityListenerWrapper))
                 return false;
 
             return (((ServerProximityListenerWrapper)o).listener == listener);
         }
 
         @Override
         public int hashCode() {
             int hash = 3;
             hash = 53 * hash + (this.listener != null ? this.listener.hashCode() : 0);
             return hash;
         }
     }
 }
 
