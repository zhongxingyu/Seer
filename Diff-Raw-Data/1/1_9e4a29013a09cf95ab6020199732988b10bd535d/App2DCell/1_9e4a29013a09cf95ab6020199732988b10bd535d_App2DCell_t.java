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
 package org.jdesktop.wonderland.modules.appbase.client;
 
 import com.jme.math.Vector2f;
 import org.jdesktop.wonderland.common.cell.CellID;
 import org.jdesktop.wonderland.common.cell.state.CellClientState;
 import org.jdesktop.wonderland.modules.appbase.common.App2DCellClientState;
 import org.jdesktop.wonderland.client.cell.CellCache;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 
 /**
  * The generic 2D application superclass. It's only extra attribute is the
  * pixel scale for all app windows created in the cell. The pixel scale is a Vector2f. 
  * The x component specifies the size (in local cell coordinates) of the windows along 
  * the local cell X axis. The y component specifies the same along the local cell 
  * Y axis. The pixel scale is in the cell client data (which must be of type 
  * <code>App2DCellClientState</code>) sent by the server when it instantiates this cell.
  *
  * @author deronj
  */ 
 
 @ExperimentalAPI
 public abstract class App2DCell extends AppCell {
 
     /** The number of world units per pixel in the cell local X and Y directions */
     protected Vector2f pixelScale = new Vector2f();
 
     /** 
      * Creates a new instance of App2DCell.
      *
      * @param cellID The ID of the cell.
      * @param cellCache the cell cache which instantiated, and owns, this cell.
      */
     public App2DCell (CellID cellID, CellCache cellCache) {
         super(cellID, cellCache);
     }
 
     /**
      * {@inheritDoc}
      */
     public void cleanup () {
 	super.cleanup();
 	pixelScale = null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setClientState(CellClientState clientState) {
	super.setClientState(clientState);
 	App2DCellClientState appClientState = (App2DCellClientState) clientState;
 	pixelScale = appClientState.getPixelScale();
     }
 
     /**
      * Returns the pixel scale.
      */
     public Vector2f getPixelScale () {
 	return pixelScale;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         StringBuffer buf = new StringBuffer();
         buf.append(super.toString());
 	buf.append(" pixelScale: ");
 	buf.append(pixelScale);
 	return buf.toString();
     }
 }
