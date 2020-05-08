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
 package org.jdesktop.wonderland.modules.coneofsilence.client.cell;
 
 import com.jme.bounding.BoundingSphere;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.shape.Cone;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.WireframeState;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
 
 /**
  * @author jkaplan
  */
 public class ConeOfSilenceCellRenderer extends BasicRenderer {
     
     public ConeOfSilenceCellRenderer(Cell cell) {
         super(cell);
     }
     
     protected Node createSceneGraph(Entity entity) {
 
         /* Fetch the basic info about the cell */
         String name = cell.getCellID().toString();
 
         /* Create the scene graph object and set its wireframe state */
         float radius = ((BoundingSphere)cell.getLocalBounds()).getRadius();
         Cone cone = new Cone(name, 30, 30, radius, (float) 1.0 * radius);
         Node node = new Node();
         node.attachChild(cone);
 
         // Raise the cone off of the floor, and rotate it about the +x axis 90
         // degrees so it faces the proper way
        Vector3f translation = new Vector3f(0.0f, 2.5f, 0.0f);
         Vector3f axis = new Vector3f(1.0f, 0.0f, 0.0f);
         float angle = (float)Math.toRadians(90);
         Quaternion rotation = new Quaternion().fromAngleAxis(angle, axis);
         node.setLocalTranslation(translation);
         node.setLocalRotation(rotation);
         node.setModelBound(new BoundingSphere());
         node.updateModelBound();
 
         WireframeState wiState = (WireframeState)ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
         wiState.setEnabled(true);
         node.setRenderState(wiState);
         node.setName("Cell_"+cell.getCellID()+":"+cell.getName());
 
         return node;
     }
 
 }
