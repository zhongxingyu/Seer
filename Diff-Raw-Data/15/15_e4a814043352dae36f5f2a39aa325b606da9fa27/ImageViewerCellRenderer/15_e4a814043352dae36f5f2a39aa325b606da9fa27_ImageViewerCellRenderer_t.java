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
 package org.jdesktop.wonderland.modules.imageviewer.client.cell.jme.cellrenderer;
 
 import com.jme.bounding.BoundingSphere;
 import com.jme.image.Image;
 import com.jme.image.Texture;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.shape.Box;
 import com.jme.scene.state.RenderState.StateType;
 import com.jme.scene.state.TextureState;
 import com.jme.util.TextureManager;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.RenderManager;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.cellrenderer.BasicRenderer;
 import org.jdesktop.wonderland.modules.imageviewer.client.cell.ImageViewerCell;
 
 /**
  * A cell renderer that displays an image.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class ImageViewerCellRenderer extends BasicRenderer {
 
     /** The depth of the box that displays the image */
     public static final float IMAGE_DEPTH = 0.1f;
 
     public ImageViewerCellRenderer(Cell cell) {
         super(cell);
     }
 
     /**
      * {@inheritDoc}
      */
     protected Node createSceneGraph(Entity entity) {
 
         // Create a new root node
         Node node = new Node("Image Viewer Node");
 
         // First load the texture to figure out its size
         String textureURI = ((ImageViewerCell)cell).getImageURI();
         if (textureURI == null) {
             logger.warning("Invalid texture URI given to cell");
             return node;
         }
         
         // Convert the uri given to a proper url to download
         URL url = null;
         try {
             url = getAssetURL(textureURI);
         } catch (MalformedURLException ex) {
             logger.log(Level.WARNING, "Unable to form asset url from " +
                     textureURI, ex);
             return node;
         }
 
         // Load the texture first to get the image size
         Texture texture = TextureManager.loadTexture(url);
         texture.setWrap(Texture.WrapMode.BorderClamp);
         texture.setTranslation(new Vector3f());
 
         // Figure out what the size of the texture is, scale it down to something
         // reasonable.
         Image image = texture.getImage();
         float width = image.getWidth() * ImageViewerCell.WIDTH_SCALE_FACTOR;
         float height = image.getHeight() * ImageViewerCell.HEIGHT_SCALE_FACTOR;
 
         // Create a box of suitable dimensions
         Box box = new Box("Box", new Vector3f(0, 0, 0), width, height, IMAGE_DEPTH);
         node.attachChild(box);
         node.setModelBound(new BoundingSphere());
         node.updateModelBound();
 
         // Set the texture on the node
         RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
         TextureState ts = (TextureState)rm.createRendererState(StateType.Texture);
         ts.setTexture(texture);
         ts.setEnabled(true);
         box.setRenderState(ts);
 
        // Make sure we do not cache the texture in memory, this will mess
        // up asset caching with WL (if the URL stays the same, but the
        // underlying asset changes).
        TextureManager.releaseTexture(texture);

         return node;
     }
 }
