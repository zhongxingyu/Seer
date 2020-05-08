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
 
 import com.jme.bounding.BoundingVolume;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 /**
  *
  * @author paulby
  */
 class Space {
 
     private BoundingVolume worldBounds;
 
     private final HashSet<SpatialCellImpl> rootCells = new HashSet();
     private final HashSet<ViewCache> viewCaches = new HashSet();
     private String name;
 
     private static final Logger logger = Logger.getLogger(Space.class.getName());
 
     public Space(BoundingVolume worldBounds, String name) {
         this.worldBounds = worldBounds;
         this.name = name;
     }
 
     public BoundingVolume getWorldBounds() {
         return worldBounds;
     }
 
     public String getName() {
         return name;
     }
 
     public void addRootSpatialCell(SpatialCellImpl cell) {
         logger.fine("Adding cell "+cell.getCellID()+" to space "+getName());
         synchronized(rootCells) {
             rootCells.add(cell);
         }
         
         synchronized(viewCaches) {
             cell.addViewCache(viewCaches, this);
 
            // issue 754: avoid concurrent modification
            ViewCache[] caches = viewCaches.toArray(new ViewCache[viewCaches.size()]);
            for(ViewCache cache : caches)
                 cache.rootCellAdded(cell);
         }
     }
 
     public void removeRootSpatialCell(SpatialCellImpl cell) {
         logger.fine("Removing cell "+cell.getCellID()+" from space "+getName());
         synchronized(rootCells) {
             rootCells.remove(cell);
         }
         
         synchronized(viewCaches) {
             cell.removeViewCache(viewCaches, this);

            // issue 754: avoid concurrent modification
            ViewCache[] caches = viewCaches.toArray(new ViewCache[viewCaches.size()]);
            for(ViewCache cache : caches)
                 cache.rootCellRemoved(cell);
         }
     }
 
     public void addViewCache(ViewCache cache) {
         synchronized(viewCaches) {
             viewCaches.add(cache);
             ArrayList<ViewCache> tmp = new ArrayList();
             tmp.add(cache);
             synchronized(rootCells) {
                 for(SpatialCellImpl rootCell : rootCells) {
                     rootCell.addViewCache(tmp, this);
                 }
             }
         }
     }
 
     public void removeViewCache(ViewCache cache) {
         synchronized(viewCaches) {
             viewCaches.remove(cache);
         }
     }
 
     /**
      * Returns a snapshot of the set of root cells (no need to hold a lock when
      * calling this method)
      * @return
      */
     public Collection<SpatialCellImpl> getRootCells() {
         synchronized(rootCells) {
             return (Collection<SpatialCellImpl>) rootCells.clone();
         }
     }
 
 }
