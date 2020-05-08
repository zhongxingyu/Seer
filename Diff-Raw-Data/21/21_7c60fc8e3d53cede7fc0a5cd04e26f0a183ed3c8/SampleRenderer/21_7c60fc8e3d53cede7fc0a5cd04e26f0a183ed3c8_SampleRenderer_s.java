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
 package org.jdesktop.wonderland.modules.sample.client;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.TriMesh;
 import com.jme.scene.shape.Box;
 import com.jme.scene.shape.Sphere;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
 
 /**
  * An example of a cell renderer
  * 
  * @author jkaplan
  */
 public class SampleRenderer extends BasicRenderer {
     private Node node = null;
 
     public SampleRenderer(Cell cell) {
         super(cell);
     }
 
     public void updateShape() {
         String name = cell.getCellID().toString();
         String shapeType = ((SampleCell) cell).getShapeType();
 
         node.detachAllChildren();
         node.attachChild(this.getShapeMesh(name, shapeType));
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
 
         ClientContextJME.getWorldManager().addToUpdateList(node);
     }
 
      private TriMesh getShapeMesh(String name, String shapeType) {
         /* Create the new object -- either a Box or Sphere */
         TriMesh mesh = null;
         if (shapeType != null && shapeType.equals("BOX") == true) {
             mesh = new Box(name, new Vector3f(), 2, 2, 2);
         }
         else if (shapeType != null && shapeType.equals("SPHERE") == true) {
             mesh = new Sphere(name, new Vector3f(), 25, 25, 2);
         }
         else {
             logger.warning("Unsupported Shape type " +cell.getLocalBounds().getClass().getName());
         }
         return mesh;
     }
 
     protected Node createSceneGraph(Entity entity) {
         /* Fetch the basic info about the cell */
         String name = cell.getCellID().toString();
         String shapeType = ((SampleCell)cell).getShapeType();
 
         /* Create the new mesh for the shape */
         TriMesh mesh = this.getShapeMesh(name, shapeType);
         if (mesh == null) {
           node = new Node();
           return node;
         }
 
         /* Create the scene graph object and set its wireframe state */
         node = new Node();
         node.attachChild(mesh);
        node.setModelBound(new BoundingBox());
        node.updateModelBound();
         node.setName("Cell_"+cell.getCellID()+":"+cell.getName());
 
         return node;
     }
 }
