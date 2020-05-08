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
 package org.jdesktop.wonderland.modules.avatarbase.client.basic;
 
 import com.jme.scene.Spatial;
 import com.jme.util.export.binary.BinaryImporter;
 import com.jme.util.resource.ResourceLocator;
 import com.jme.util.resource.ResourceLocatorTool;
 import imi.character.CharacterParams;
 import imi.character.MaleAvatarParams;
 import imi.scene.PMatrix;
 import imi.scene.PScene;
 import imi.scene.polygonmodel.PPolygonMesh;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.mtgame.WorldManager;
 import org.jdesktop.wonderland.client.cell.Cell;
 import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
 import org.jdesktop.wonderland.modules.avatarbase.client.loader.spi.AvatarLoaderSPI;
 import org.jdesktop.wonderland.modules.avatarbase.common.cell.AvatarConfigInfo;
 
 /**
  * Loads basic (static Collada model) avatars on the client and generates an
  * avatar character from it.
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class BasicAvatarLoader implements AvatarLoaderSPI {
 
     private static Logger logger = Logger.getLogger(BasicAvatarLoader.class.getName());
 
     /**
      * {@inheritDoc}
      */
     public WlAvatarCharacter getAvatarCharacter(Cell avatarCell,
             String userName, AvatarConfigInfo info) {
 
         WorldManager wm = ClientContextJME.getWorldManager();
         CharacterParams attributes = new MaleAvatarParams(userName);
 
         // Formulate the base URL for all IMI avatar assets
         String baseURL = null;
         try {
             ServerSessionManager manager = avatarCell.getCellCache().getSession().getSessionManager();
             String serverHostAndPort = manager.getServerNameAndPort();
             URL tmpURL = AssetUtils.getAssetURL("wla://avatarbaseart/", serverHostAndPort);
             baseURL = tmpURL.toExternalForm();
         } catch (MalformedURLException ex) {
             logger.log(Level.WARNING, "Unable to form base url", ex);
             return null;
         }
 
         // Setup simple model, needs to actually have something to
         // play well with the system
         PScene simpleScene = new PScene(ClientContextJME.getWorldManager());
         simpleScene.addMeshInstance(new PPolygonMesh("PlaceholderMesh"), new PMatrix());
         attributes.setUseSimpleStaticModel(true, simpleScene);
         attributes.setBaseURL(baseURL);
 
         // Create the avatar character, but don't add it to the world just yet.
         WlAvatarCharacter avatar =
                 new WlAvatarCharacter.WlAvatarCharacterBuilder(attributes, wm).addEntity(false).build();
 
         // Load the avatar as a static Collada model.
        Spatial placeHolder = null;
         try {
             URL url = new URL(baseURL + "assets/models/collada/Avatars/StoryTeller.kmz/models/StoryTeller.wbm");
             ResourceLocator resourceLocator = new RelativeResourceLocator(url, avatarCell);
 
             ResourceLocatorTool.addThreadResourceLocator(
                     ResourceLocatorTool.TYPE_TEXTURE,
                     resourceLocator);
            placeHolder = (Spatial) BinaryImporter.getInstance().load(url);
             ResourceLocatorTool.removeThreadResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, resourceLocator);
         } catch (IOException excp) {
             logger.log(Level.WARNING, "Unable to load avatar character", excp);
             return avatar;
         }
 
         //checkBounds(placeHolder);
         //placeHolder.updateModelBound();
         //placeHolder.updateWorldBound();
 
         //System.out.println("Default Model Bounds: " + placeHolder.getWorldBound());
         //placeHolder.lockBounds();
        avatar.getJScene().getExternalKidsRoot().attachChild(placeHolder);
         avatar.getJScene().setExternalKidsChanged(true);
         return avatar;
     }
 
     /**
      * A texture resource locator that looks relative to the path of the model.
      * 
      * Hack for the binary loader, this will need to be made general purpose once
      * we implement a core binary loader
      */
     class RelativeResourceLocator implements ResourceLocator {
 
         private String modulename;
         private String path;
         private String protocol;
         private Cell viewCell;
 
         /**
          * Locate resources for the given file
          * @param url
          */
         public RelativeResourceLocator(URL url, Cell viewCell) {
             // The modulename can either be in the "user info" field or the
             // "host" field. If "user info" is null, then use the host name.
             this.viewCell = viewCell;
             if (url.getUserInfo() == null) {
                 modulename = url.getHost();
             }
             else {
                 modulename = url.getUserInfo();
             }
             path = url.getPath();
             path = path.substring(0, path.lastIndexOf('/')+1);
             protocol = url.getProtocol();
         }
 
         public URL locateResource(String resource) {
             try {
                 String urlStr = trimUrlStr(protocol + "://" + modulename + path + ".." + resource);
                 URL url = AssetUtils.getAssetURL(urlStr, viewCell);
                 return url;
             } catch (MalformedURLException ex) {
                 logger.log(Level.SEVERE, "Unable to locateResource "+resource, ex);
                 return null;
             }
         }
 
         /**
          * Trim ../ from url
          */
         private String trimUrlStr(String urlStr) {
             int pos = urlStr.indexOf("/../");
             if (pos == -1)
                 return urlStr;
 
             StringBuilder buf = new StringBuilder(urlStr);
             int start = pos;
             while (buf.charAt(--start) != '/') {
             }
             buf.replace(start, pos + 4, "/");
             return buf.toString();
         }
     }
 }
