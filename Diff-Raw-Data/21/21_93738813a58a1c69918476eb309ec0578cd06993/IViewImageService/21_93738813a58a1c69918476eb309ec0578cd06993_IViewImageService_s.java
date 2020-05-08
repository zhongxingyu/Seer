 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2012-2012 R. van Twisk. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.iview.rules;
 
 import javax.jcr.Node;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import java.io.*;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.drools.ObjectFilter;
 import org.drools.spi.KnowledgeHelper;
 import org.jahia.api.Constants;
 import org.jahia.services.content.JCRContentUtils;
 import org.jahia.services.content.JCRNodeWrapper;
 import org.jahia.services.content.JCRPropertyWrapper;
 import org.jahia.services.content.JCRSessionWrapper;
 import org.jahia.services.content.rules.AddedNodeFact;
 import org.jahia.services.content.rules.ChangedPropertyFact;
 import org.jahia.services.image.Image;
 import org.jahia.services.image.ImageMagickImage;
 import org.jahia.services.image.JahiaImageService;
 import org.jahia.settings.SettingsBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Imaging service.
  * User: toto
  * Date: Oct 21, 2008
  * Time: 5:25:31 PM
  */
 public class IViewImageService {
 
     private static final Logger logger = LoggerFactory.getLogger(IViewImageService.class);
     public static final String IVIEW_NODE_NAME_PREFIX = "jahia-iview";
 
     private JahiaImageService imageService;
 
     public void setImageService(JahiaImageService imageService) {
         this.imageService = imageService;
     }
 
     private static IViewImageService instance;
 
     private static File contentTempFolder;
 
     public static IViewImageService getInstance() {
         if (instance == null) {
             synchronized (IViewImageService.class) {
                 if (instance == null) {
                     instance = new IViewImageService();
                     contentTempFolder = new File(SettingsBean.getInstance().getTmpContentDiskPath());
                     if (!contentTempFolder.exists()) {
                         contentTempFolder.mkdirs();
                     }
                 }
             }
         }
         return instance;
     }
 
     private Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
         Node contentNode = node.getNode(Constants.JCR_CONTENT);
         String fileExtension = FilenameUtils.getExtension(node.getName());
         File tmp = File.createTempFile("image", StringUtils.isNotEmpty(fileExtension) ? "."
                 + fileExtension : null);
         InputStream is = contentNode.getProperty(Constants.JCR_DATA).getStream();
         OutputStream os = new BufferedOutputStream(new FileOutputStream(tmp));
         try {
             IOUtils.copy(is, os);
         } finally {
             IOUtils.closeQuietly(os);
             IOUtils.closeQuietly(is);
         }
         return new ImageMagickImage(tmp, tmp.getPath());
     }
 
     /**
      * Get image from imageNpde
      *
      * @param imageNode
      * @param drools
      * @return
      * @throws Exception
      */
     private Image getImageWrapper(final AddedNodeFact imageNode, KnowledgeHelper drools) throws Exception {
         Iterator<?> it = drools.getWorkingMemory().iterateObjects(new ObjectFilter() {
             public boolean accept(Object o) {
                 if (o instanceof Image) {
                     try {
                         return (((Image) o).getPath().equals(imageNode.getPath()));
                     } catch (RepositoryException e) {
                         e.printStackTrace();
                     }
                 }
                 return false;
             }
         });
         if (it.hasNext()) {
             return (Image) it.next();
         }
         Image iw = getImage(imageNode.getNode());
         if (iw == null) {
             return null;
         }
         drools.insertLogical(iw);
         return iw;
     }
 
     /**
      * Cropscale image rule for iView module
      *
      * @param propertyWrapper
      * @param name
      * @param drools
      * @throws Exception
      */
     public void cropScale(ChangedPropertyFact propertyWrapper, String name, KnowledgeHelper drools) throws Exception {
 //        final JCRPropertyWrapper property = propertyWrapper.getProperty();
 //        final JCRSessionWrapper session = property.getSession();
 //        JCRNodeWrapper node = session.getNodeByIdentifier(property.getString());
 
         JCRNodeWrapper definitionNode = propertyWrapper.getNode().getNode();
         final JCRPropertyWrapper property = definitionNode.getProperty(propertyWrapper.getName());
         final JCRSessionWrapper session = property.getSession();
         JCRNodeWrapper node = session.getNodeByIdentifier(property.getString());
 
 
         JCRNodeWrapper bannerNodeTrans = property.getParent();
         JCRNodeWrapper sliderNode;
         if (bannerNodeTrans.getParent().isNodeType("jnt:iViewSlider")) {
             sliderNode = bannerNodeTrans.getParent();
         } else {
             sliderNode = bannerNodeTrans.getParent().getParent();
         }
 
         int border = 0;
         int width = 0;
         int height = 0;
         try {
             // Calculate needed with/height of the image
             border = (int) sliderNode.getProperty("border").getLong();
             width = (int) sliderNode.getProperty("width").getLong() - border * 2;
             height = (int) sliderNode.getProperty("height").getLong() - border * 2;
 
             // Create a new image
             cropScale(new AddedNodeFact(node), IViewImageService.iViewNodeName(bannerNodeTrans), width, height, session, drools);
         } catch (PathNotFoundException e) {
             logger.error("Property to process image not found" + e.getMessage());
         } catch (Exception e) {
             logger.error("General exception processing image" + e.getMessage());
         }
 
     }
 
     /**
      * Retreive a name for this crop scaled image
      *
      * @param bannerNode
      * @return
      * @throws Exception
      */
     public static String iViewNodeName(JCRNodeWrapper bannerNode) throws Exception {
         JCRNodeWrapper sliderNode = bannerNode.getParent();
 
        int border = (int) sliderNode.getProperty("border").getLong();
        int width = (int) sliderNode.getProperty("width").getLong() - border * 2;
        int height = (int) sliderNode.getProperty("height").getLong() - border * 2;
 
         return IVIEW_NODE_NAME_PREFIX + "_" + width + "x" + height;
     }
 
     private void cropScale(AddedNodeFact imageNode, String nodeName, int width, int height, JCRSessionWrapper session, KnowledgeHelper drools) throws Exception {
         long timer = System.currentTimeMillis();
 
         if (imageNode.getNode().hasNode(nodeName)) {
             JCRNodeWrapper node = imageNode.getNode().getNode(nodeName);
             Calendar thumbDate = node.getProperty("jcr:lastModified").getDate();
             Calendar contentDate = imageNode.getNode().getNode("jcr:content").getProperty("jcr:lastModified").getDate();
             if (contentDate.after(thumbDate)) {
                 AddedNodeFact thumbNode = new AddedNodeFact(node);
                 File f = crop(imageNode, 0, 0, width, height, drools);
                 if (f == null) {
                     return;
                 }
                 drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_DATA, f, drools));
                 drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools));
             }
         } else {
 
             File f = crop(imageNode, 0, 0, width, height, drools);
             if (f == null) {
                 return;
             }
 
             AddedNodeFact thumbNode = new AddedNodeFact(imageNode, nodeName, "jnt:resource", drools);
             if (thumbNode.getNode() != null) {
                 drools.insert(thumbNode);
                 drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_DATA, f, drools));
                 drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_MIMETYPE, imageNode.getMimeType(), drools));
                 drools.insert(new ChangedPropertyFact(thumbNode, Constants.JCR_LASTMODIFIED, new GregorianCalendar(), drools));
             }
         }
 
         if (logger.isDebugEnabled()) {
             logger.debug("crop for node {} created in {} ms", new Object[]{
                     imageNode.getNode().getPath(), System.currentTimeMillis() - timer});
         }
     }
 
     /**
      * Test if node is smaller or equal then width/height
      *
      * @param node
      * @param width
      * @param height
      * @return
      */
     protected boolean isSmallerThan(JCRNodeWrapper node, int width, int height) {
         long iWidth = 0;
         long iHeight = 0;
 
         try {
             iWidth = node.getProperty("j:width").getLong();
             iHeight = node.getProperty("j:height").getLong();
         } catch (PathNotFoundException e) {
             // no size properties found
         } catch (RepositoryException e) {
             if (logger.isDebugEnabled()) {
                 logger.warn("Error reading j:width/j:height properties on node " + node.getPath(),
                         e);
             } else {
                 logger.warn("Error reading j:width/j:height properties on node " + node.getPath()
                         + ". Casue: " + e.getMessage());
             }
         }
 
         return iWidth > 0 && iHeight > 0 && iWidth <= width && iHeight <= height;
     }
 
     /**
      * Crop a given image within AddedNodeFact and create a new image with suffix
      *
      * @param imageNode
      * @param top
      * @param left
      * @param width
      * @param height
      * @param drools
      * @return
      * @throws Exception
      */
     public File crop(AddedNodeFact imageNode, int top, int left, int width, int height, KnowledgeHelper drools) throws Exception {
         String fileExtension = FilenameUtils.getExtension(imageNode.getName());
 
         if (isSmallerThan(imageNode.getNode(), width, height)) {
             logger.info("Selected image was smaller then required image of {}x{} not cropping this image", width, height);
 
             // no need to resize the small image for the cropped
             final File f = File.createTempFile("thumb", StringUtils.isNotEmpty(fileExtension) ? "." + fileExtension : null, contentTempFolder);
             JCRContentUtils.downloadFileContent(imageNode.getNode(), f);
             f.deleteOnExit();
 
             return f;
         }
 
         Image iw = getImageWrapper(imageNode, drools);
         if (iw == null) {
             return null;
         }
 
         final File f = File.createTempFile("iview", StringUtils.isNotEmpty(fileExtension) ? "." + fileExtension : null, contentTempFolder);
 
         if (imageService.cropImage(iw, f, top, left, width, height)) {
             f.deleteOnExit();
             return f;
         } else {
             f.deleteOnExit();
             return null;
         }
     }
 
 }
