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
 package org.jdesktop.wonderland.modules.imageviewer.client.cell.jme.cellrenderer;
 
 import com.jme.bounding.BoundingSphere;
 import com.jme.image.Image;
 import com.jme.image.Texture;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.shape.Box;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.TextureState;
 import com.jme.util.TextureManager;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
 import org.jdesktop.wonderland.modules.imageviewer.client.cell.ImageViewerCell;
 
 /**
  * @author jkaplan
  */
 public class ImageViewerCellRenderer extends BasicRenderer {
 
     private Node node = null;
     
     public ImageViewerCellRenderer(Cell cell) {
         super(cell);
     }
 
     protected Node createSceneGraph(Entity entity) {
 
         // Create a new root node
         node = new Node("Image Viewer Node");
 
         // First load the texture to figure out its size
         String textureURI = ((ImageViewerCell)cell).getImageURI();
         if (textureURI == null) {
             logger.warning("Invalid texture URI given to cell");
             return new Node();
         }
         
         // Conver the uri given to a proper url to download
         URL url = null;
         try {
             url = getAssetURL(textureURI);
         } catch (MalformedURLException ex) {
             Logger.getLogger(ImageViewerCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         // Load the texture first to get the image size
         Texture texture = TextureManager.loadTexture(url);
         texture.setWrap(Texture.WrapMode.BorderClamp);
         texture.setTranslation(new Vector3f());
 
        // Figure out what the size of the texture is
         Image image = texture.getImage();
        int width = image.getWidth();
        int height = image.getHeight();
 
         // Create a box of suitable dimensions
         Box box = new Box("Box", new Vector3f(0, 0, 0), width, height, 0.1f);
         node.attachChild(box);
        node.setLocalScale(new Vector3f(0.01f, 0.01f, 1.0f));
        box.setModelBound(new BoundingSphere());
        box.updateModelBound();
 
         // Set the texture on the node
         TextureState ts = (TextureState) ClientContextJME.getWorldManager().getRenderManager().createRendererState(RenderState.RS_TEXTURE);
         ts.setTexture(texture);
         ts.setEnabled(true);
         box.setRenderState(ts);
 
         return node;
     }
 }
