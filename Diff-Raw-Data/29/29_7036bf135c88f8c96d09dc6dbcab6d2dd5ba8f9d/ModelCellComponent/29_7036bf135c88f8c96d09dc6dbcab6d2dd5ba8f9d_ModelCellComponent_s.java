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
 package org.jdesktop.wonderland.client.cell;
 
 import com.jme.scene.Node;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBException;
 import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
 import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
 import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
 import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
 import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellComponentClientState;
 
 /**
  * A Component that represents a deployed model.
  *
  * @author paulby
  */
 public class ModelCellComponent extends CellComponent {
     private String deployedModelURL = null;   // URL of .dep file
     protected DeployedModel deployedModel = null;
     protected ModelRenderer renderer = null;
 
     public ModelCellComponent(Cell cell) {
         super(cell);
     }
 
     /**
      * Instantiate and return the cell renderer
      * @param type
      * @param cell
      * @return
      */
     public CellRenderer getCellRenderer(Cell.RendererType type, Cell cell) {
         synchronized(this) {
             if (renderer==null) {
                 if (deployedModel==null) {
                     getDeployedModel();
                 }
                 renderer = new ModelRenderer(cell, deployedModel);
             }
             return renderer;
         }
     }
 
     private void getDeployedModel() {
         try {
             URL url = AssetUtils.getAssetURL(getDeployedModelURL());
             BufferedInputStream in = new BufferedInputStream(url.openStream());
             deployedModel = DeployedModel.decode(in);
             in.close();
         } catch (MalformedURLException ex) {
             Logger.getLogger(ModelCellComponent.class.getName()).log(Level.SEVERE, "Failed to load deployment descriptor "+getDeployedModelURL(), ex);
         } catch(IOException ex) {
             Logger.getLogger(ModelCellComponent.class.getName()).log(Level.SEVERE, "Failed to load deployment descriptor "+getDeployedModelURL(), ex);
         } catch (JAXBException ex) {
             Logger.getLogger(ModelCellComponent.class.getName()).log(Level.SEVERE, "Failed to load deployment descriptor "+getDeployedModelURL(), ex);
         }
     }
 
     /**
      * Load and return the model. The node returned is the model BG
      *
      * @return
      */
     public Node loadModel() {
         Node ret = new Node();
 
        getDeployedModel();
 
         ModelLoader loader = deployedModel.getModelLoader();
         Node model = loader.loadDeployedModel(deployedModel);
         if (model != null) {
             model.setName(deployedModel.getModelURL());
             ret.attachChild(model);
         }
         return ret;
     }
 
     @Override
     public void setClientState(CellComponentClientState clientState) {
         super.setClientState(clientState);
 
         ModelCellComponentClientState state = (ModelCellComponentClientState) clientState;
         setDeployedModelURL(state.getDeployedModelURL());
      }
 
     /**
      * @return the deployedModelURL
      */
     public String getDeployedModelURL() {
         return deployedModelURL;
     }
 
     protected void setDeployedModelURL(String url) {
         this.deployedModelURL = url;
     }
 }
