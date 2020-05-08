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
 package org.jdesktop.wonderland.modules.artimport.client;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import java.awt.Image;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
 import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
 import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
 import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
 import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
 import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
 import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.ModelCellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.ModelCellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
 
 /**
  * A Cell Factory that loads deployed model (.dep) files. This does not appear
  * in the Cell Palette, but supports creation via DnD or the Content Browser.
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 @CellFactory
 public class DeployedModelCellFactory implements CellFactorySPI {
 
     // The error logger
     private static final Logger LOGGER =
             Logger.getLogger(DeployedModelCellFactory.class.getName());
 
     /**
      * {@inheritDoc}
      */
     public String[] getExtensions() {
         return new String[] { "dep" };
     }
 
     /**
      * {@inheritDoc}
      */
     public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
         // Fetch the URI of the model to load via the "content-uri" property.
         // If not present return null. Convert into a URL
         URL url = null;
         if (props != null) {
             String uri = props.getProperty("content-uri");
             if (uri != null) {
                 try {
                     url = AssetUtils.getAssetURL(uri);
                 } catch (MalformedURLException excp) {
                     LOGGER.log(Level.WARNING, "Unable to form asset URI from " +
                             uri, excp);
                     url = null;
                 }
             }
         }
 
         // Check to make sure the URL is not null. If so, then just return
         // null
         if (url == null) {
             LOGGER.warning("The URL is null");
             return null;
         }
 
 
         LOGGER.warning("Loading URL " + url.toExternalForm());
 
         // Simply create a new ModelCell by creating a ModelCellServerState
         // with the URL passed in via the properties. First load the deployed
         // model from the given URL.
         LoaderManager lm = LoaderManager.getLoaderManager();
         DeployedModel dm = null;
         try {
             dm = lm.getLoaderFromDeployment(url);
         } catch (IOException excp) {
             LOGGER.log(Level.WARNING, "Unable to load deployed model from " +
                     url.toExternalForm(), excp);
             return null;
         }
         
         // Get the loader from the deployed model. Use that to create a
         // server state
        ModelCellServerState cellSetup = new ModelCellServerState();
        ModelCellComponentServerState setup = new ModelCellComponentServerState();
        setup.setDeployedModelURL(url.toExternalForm());

        cellSetup.addComponentServerState(setup);
        cellSetup.setName("todo...");        // TODO correct name
//        cellSetup.setBoundingVolumeHint(new BoundingVolumeHint(false, importedModel.getModelBG().getWorldBound()));

//        Vector3f offset = new Vector3f();
//        PositionComponentServerState position = new PositionComponentServerState();
//        Vector3f boundsCenter = new Vector3f();
//
//        offset.subtractLocal(boundsCenter);

        return (T)cellSetup;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getDisplayName() {
         // Should not appear in the Cell Palette so return null
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Image getPreviewImage() {
         // Does not appear in the Cell Palette, so no preview image
         return null;
     }
 
 }
