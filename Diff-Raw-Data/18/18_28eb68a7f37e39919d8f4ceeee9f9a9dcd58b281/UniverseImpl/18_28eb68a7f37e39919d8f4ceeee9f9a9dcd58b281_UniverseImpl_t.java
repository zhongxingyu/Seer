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
 package org.jdesktop.wonderland.server.spatial.impl;
 
 import com.sun.sgs.auth.Identity;
 import com.sun.sgs.kernel.ComponentRegistry;
 import com.sun.sgs.kernel.KernelRunnable;
 import com.sun.sgs.kernel.TaskScheduler;
 import com.sun.sgs.kernel.TransactionScheduler;
 import com.sun.sgs.service.DataService;
 import com.sun.sgs.service.TransactionProxy;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.server.cell.TransformChangeListenerSrv;
 import org.jdesktop.wonderland.server.spatial.ViewUpdateListener;
 import org.jdesktop.wonderland.server.cell.view.ViewCellMO;
 
 /**
  *
  * @author paulby
  */
 public class UniverseImpl implements Universe {
 
     private SpaceManager spaceManager = new SpaceManagerGridImpl();
     private final HashMap<CellID, SpatialCell> cells = new HashMap();
     private static UniverseImpl universe;
     private TransactionProxy transactionProxy;
     private DataService dataService;
     private TransactionScheduler transactionScheduler;
 
     private static final Logger logger = Logger.getLogger(UniverseImpl.class.getName());
 
     public UniverseImpl(ComponentRegistry componentRegistry, TransactionProxy transactionProxy) {
         this.transactionProxy = transactionProxy;
         this.dataService = transactionProxy.getService(DataService.class);
         this.transactionScheduler = componentRegistry.getComponent(TransactionScheduler.class);
         universe = this;
     }
 
     public static UniverseImpl getUniverse() {
         return universe;
     }
 
     SpaceManager getSpaceManager() {
         return spaceManager;
     }
 
     public TransactionProxy getTransactionProxy() {
         return transactionProxy;
     }
 
     public DataService getDataService() {
         return dataService;
     }
 
     public void scheduleTransaction(KernelRunnable transaction, Identity identity) {
         transactionScheduler.scheduleTask(transaction, identity);
     }
 
     public void addRootSpatialCell(CellID cellID, Identity identity) {
         SpatialCellImpl cellImpl = (SpatialCellImpl)getSpatialCell(cellID);
 
         // Set the root, which causes the WorldBounds to be evaluated and
         // the graph added to spaces & view caches.
         // No need to lock for this call because until it returns there
         // is no root to lock
         cellImpl.setRoot(cellImpl, identity);        
     }
 
     public void removeRootSpatialCell(CellID cellID, Identity identity) {
         logger.fine("removeSpatialCell "+cellID);
         SpatialCellImpl cellImpl = (SpatialCellImpl)getSpatialCell(cellID);
 
         if (cellImpl==null) {
             logger.warning("removeRootSpatialCell FAILED, unable to find cell "+cellID);
             return;
         }
 
         // Set the root node to null. Internally this method will lock correctly
         // and ensure the graph is removed from spaces etc
         cellImpl.setRoot(null, identity);
 
     }
 
     public SpatialCellImpl createSpatialCell(CellID id, BigInteger dsID, Class cellClass) {
         logger.fine("createSpatialCell "+id+"   dsID "+dsID);
         SpatialCellImpl ret;
         if (ViewCellMO.class.isAssignableFrom(cellClass)) {
             ret = new ViewCellImpl(id, spaceManager, dsID);
         } else
             ret = new SpatialCellImpl(id, dsID);
         synchronized(cells) {
             cells.put(id, ret);
         }
 
         return ret;
     }
 
     public void revalidateCell(CellID id) {
         SpatialCell cell = getSpatialCell(id);
        if (cell == null) {
            logger.warning("Attempt to revalidate non-existant cell " + id);
        } else {
            cell.revalidate();
        }
     }
 
     public void removeCell(CellID id) {
         // TODO remove from caches
         logger.fine("removeCell "+id);
         SpatialCell cell = getSpatialCell(id);
         
         cell.destroy();
 
         synchronized(cells) {
             cells.remove(id);
         }
     }
 
     public SpatialCell getSpatialCell(CellID cellID) {
         synchronized(cells) {
             return cells.get(cellID);
         }
     }
 
     public void viewLogin(CellID viewCellID, BigInteger cellCacheId, Identity identity) {
         ViewCellImpl viewCell;
         synchronized(cells) {
             viewCell = (ViewCellImpl) cells.get(viewCellID);
         }
 
         logger.fine("ViewLogin ViewCell="+viewCell+"  ds ID="+viewCellID);
         
         ViewCache viewCache = new ViewCache(viewCell, spaceManager, identity, cellCacheId);
         viewCell.setViewCache(viewCache);
         viewCache.login();
     }
 
     public void viewRevalidate(CellID viewCellID) {
         ViewCellImpl viewCell;
         synchronized(cells) {
             viewCell = (ViewCellImpl) cells.get(viewCellID);
         }
 
         // it's possible the viewcell won't be in our list of cells.  For
         // example when there is a warm restart, this node will see all the
         // clients log out, even though it never saw them log in.  This is
         // fine, just ignore the request.
         if (viewCell != null && viewCell.getViewCache() != null) {
             viewCell.getViewCache().revalidate();
         }
     }
 
     public void viewLogout(CellID viewCellID) {
         logger.fine("ViewLogout viewCell="+viewCellID);
         ViewCellImpl viewCell;
         synchronized(cells) {
             viewCell = (ViewCellImpl) cells.get(viewCellID);
         }
         
         // it's possible the viewcell won't be in our list of cells.  For
         // example when there is a warm restart, this node will see all the
         // clients log out, even though it never saw them log in.  This is
         // fine, just ignore the request.
         if (viewCell != null && viewCell.getViewCache() != null) {
             viewCell.getViewCache().logout();
         }
     }
 
     public void addTransformChangeListener(CellID cellID, TransformChangeListenerSrv listener) {
         synchronized(cells) {
             SpatialCellImpl cell = (SpatialCellImpl) cells.get(cellID);
             if (cell!=null)
                 cell.addTransformChangeListener(listener);
         }
     }
 
     public void removeTransformChangeListener(CellID cellID, TransformChangeListenerSrv listener) {
         synchronized(cells) {
             SpatialCellImpl cell = (SpatialCellImpl) cells.get(cellID);
             if (cell!=null)
                 cell.removeTransformChangeListener(listener);
         }
     }
 
     public void addViewUpdateListener(CellID cellID, ViewUpdateListener viewUpdateListener) {
         synchronized(cells) {
             SpatialCell cell = cells.get(cellID);
             if (cell!=null)
                 cell.addViewUpdateListener(viewUpdateListener);
         }
     }
 
     public void removeViewUpdateListener(CellID cellID, ViewUpdateListener viewUpdateListener) {
         synchronized(cells) {
             SpatialCell cell = cells.get(cellID);
             if (cell!=null)
                 cell.removeViewUpdateListener(viewUpdateListener);
         }
     }
 
 
 
 }
