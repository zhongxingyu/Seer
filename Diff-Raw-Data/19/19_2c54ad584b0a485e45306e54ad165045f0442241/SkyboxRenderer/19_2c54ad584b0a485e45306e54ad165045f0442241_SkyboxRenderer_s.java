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
 package org.jdesktop.wonderland.modules.testcells.client.jme.cellrenderer;
 
 import com.jme.image.Texture;
 import com.jme.scene.Skybox;
 import com.jme.scene.Spatial;
 import com.jme.scene.Spatial.TextureCombineMode;
 import com.jme.scene.state.CullState;
 import com.jme.scene.state.FogState;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.ZBufferState;
 import com.jme.util.TextureManager;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.SkyboxComponent;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
 import org.jdesktop.wonderland.client.login.LoginManager;
 import org.jdesktop.wonderland.common.AssetURI;
 import org.jdesktop.wonderland.common.cell.CellTransform;
 
 /**
  *
  * @author paulby
  */
 public class SkyboxRenderer implements CellRendererJME {
 
     private Entity entity = null;
     private Cell cell = null;
    
     public SkyboxRenderer(Cell cell) {
         this.cell = cell;
     }
 
     public Entity getEntity() {
         if (entity==null)
             entity = createEntity();
         
         return entity;
     }
     
     private Entity createEntity() {
         try {
             /* Form the asset URIs */
             WonderlandSession session = cell.getCellCache().getSession();
             LoginManager manager = LoginManager.find(session);
             String serverURL = manager.getServerURL();
             
             AssetURI northURI = new AssetURI("wla://testcells/art/skybox1/1.jpg", serverURL);
            AssetURI southURI = new AssetURI("wla://testcells/art/skybox1/2.jpg", serverURL);
            AssetURI eastURI = new AssetURI("wla://testcells/art/skybox1/3.jpg", serverURL);
             AssetURI westURI = new AssetURI("wla://testcells/art/skybox1/4.jpg", serverURL);
             AssetURI downURI = new AssetURI("wla://testcells/art/skybox1/5.jpg", serverURL);
             AssetURI upURI = new AssetURI("wla://testcells/art/skybox1/6.jpg", serverURL);
 
             WorldManager wm = ClientContextJME.getWorldManager();
             Skybox skybox = new Skybox("skybox", 1000, 1000, 1000);
             String dir = "jmetest/data/skybox1/";
             Texture north = TextureManager.loadTexture(northURI.toURL(), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
             Texture south = TextureManager.loadTexture(southURI.toURL(), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
             Texture east = TextureManager.loadTexture(eastURI.toURL(), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
             Texture west = TextureManager.loadTexture(westURI.toURL(), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
             Texture up = TextureManager.loadTexture(upURI.toURL(), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
             Texture down = TextureManager.loadTexture(downURI.toURL(), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
             skybox.setTexture(Skybox.Face.North, north);
             skybox.setTexture(Skybox.Face.West, west);
             skybox.setTexture(Skybox.Face.South, south);
             skybox.setTexture(Skybox.Face.East, east);
             skybox.setTexture(Skybox.Face.Up, up);
             skybox.setTexture(Skybox.Face.Down, down);
             //skybox.preloadTextures();
             CullState cullState = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);
             cullState.setEnabled(true);
             skybox.setRenderState(cullState);
             ZBufferState zState = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
             //zState.setEnabled(false);
             skybox.setRenderState(zState);
             FogState fs = (FogState) wm.getRenderManager().createRendererState(RenderState.RS_FOG);
             fs.setEnabled(false);
             skybox.setRenderState(fs);
             skybox.setLightCombineMode(Spatial.LightCombineMode.Off);
             skybox.setCullHint(Spatial.CullHint.Never);
             skybox.setTextureCombineMode(TextureCombineMode.Replace);
             skybox.updateRenderState();
             skybox.lockBounds();
             //skybox.lockMeshes();
             Entity e = new Entity("Skybox");
             SkyboxComponent sbc = wm.getRenderManager().createSkyboxComponent(skybox, true);
             e.addComponent(SkyboxComponent.class, sbc);
             return e;
         } catch (MalformedURLException ex) {
             Logger.getLogger(SkyboxRenderer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (URISyntaxException ex) {
             Logger.getLogger(SkyboxRenderer.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public void cellTransformUpdate(CellTransform cellLocal2World) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
