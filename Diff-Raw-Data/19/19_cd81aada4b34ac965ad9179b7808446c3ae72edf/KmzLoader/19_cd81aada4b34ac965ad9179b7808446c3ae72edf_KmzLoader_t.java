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
 package org.jdesktop.wonderland.modules.kmzloader.client;
 
import com.jme.math.Quaternion;
 import com.jme.scene.Node;
 import com.jme.util.resource.ResourceLocator;
 import com.jme.util.resource.ResourceLocatorTool;
 import com.jmex.model.collada.ColladaImporter;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import org.jdesktop.wonderland.client.jme.artimport.ImportSessionFrame;
 import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
 import org.jdesktop.wonderland.client.protocols.wlzip.WlzipManager;
 import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.setup.JMEColladaCellSetup;
 import org.jdesktop.wonderland.modules.kmzloader.client.kml_21.FeatureType;
 import org.jdesktop.wonderland.modules.kmzloader.client.kml_21.FolderType;
 import org.jdesktop.wonderland.modules.kmzloader.client.kml_21.GeometryType;
 import org.jdesktop.wonderland.modules.kmzloader.client.kml_21.KmlType;
 import org.jdesktop.wonderland.modules.kmzloader.client.kml_21.ModelType;
 import org.jdesktop.wonderland.modules.kmzloader.client.kml_21.PlacemarkType;
 
 /**
  *
  * Loader for SketchUp .kmz files
  * 
  * @author paulby
  */
 class KmzLoader implements ModelLoader {
 
     private static final Logger logger = Logger.getLogger(KmzLoader.class.getName());
         
     private HashMap<URL, ZipEntry> textureFiles = new HashMap();
     
     // The original file the user loaded
     private File origFile;
     
     private ArrayList<String> modelFiles = new ArrayList();
     
     /**
      * Load a SketchUP KMZ file and return the graph root
      * @param file
      * @return
      */
     public Node importModel(File file) throws IOException {
         Node ret = null;
         origFile = file;
         
         try {
             ZipFile zipFile = new ZipFile(file);
             ZipEntry docKmlEntry = zipFile.getEntry("doc.kml");
             JAXBContext jc = JAXBContext.newInstance("org.jdesktop.wonderland.modules.kmzloader.client.kml_21",
                                                      getClass().getClassLoader());
             Unmarshaller u = jc.createUnmarshaller();
             JAXBElement docKml = (JAXBElement) u.unmarshal(zipFile.getInputStream(docKmlEntry));
             
             KmlType kml = (KmlType) docKml.getValue();
             
             ArrayList<ModelType> models=new ArrayList();
             FeatureType feature = kml.getFeature().getValue();
             if (feature instanceof FolderType) {
                 findModels(models, (FolderType)feature);
             }
             
             if (models.size()==0) {
                 logger.severe("No models found in KMZ File");
                 return null;
             }
             
             if (models.size()==1) {
                ret = load(zipFile, models.get(0));
             } else {
                 ret = new Node();
                 for(ModelType model : models) {
                    ret.attachChild(load(zipFile, model));
                 }
             }
             
         } catch (ZipException ex) {
             logger.log(Level.SEVERE, null, ex);
             throw new IOException("Zip Error");
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
             throw ex;
         } catch (JAXBException ex) {
             logger.log(Level.SEVERE, null, ex);
             throw new IOException("JAXB Error");
         }
         
         return ret;
     }
     
    private Node load(ZipFile zipFile, ModelType model) throws IOException {

        String filename = model.getLink().getHref();
         String zipHost = WlzipManager.getWlzipManager().addZip(zipFile);
         ZipResourceLocator zipResource = new ZipResourceLocator(zipHost, zipFile);
         ResourceLocatorTool.addResourceLocator(
                 ResourceLocatorTool.TYPE_TEXTURE,
                 zipResource);
 
         logger.info("Loading MODEL " + filename);
         modelFiles.add(filename);
         
         ZipEntry modelEntry = zipFile.getEntry(filename);
         BufferedInputStream in = new BufferedInputStream(zipFile.getInputStream(modelEntry));
         
         Node ret;
         ColladaImporter.load(in, filename);
         ret = ColladaImporter.getModel();
 
         ColladaImporter.cleanUp();
         
         ResourceLocatorTool.removeResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, zipResource);
         WlzipManager.getWlzipManager().removeZip(zipHost, zipFile);
 
        // Correctly orient model
        ret.setLocalRotation(new Quaternion(new float[] {-(float)Math.PI/2, 0f, 0f}));

         return ret;
     }
     
     /**
      * Search kmz folders adding any ModelTypes found to the models list
      * @param models
      * @param folder
      */
     private void findModels(ArrayList<ModelType> models, FolderType folder) {
         List<JAXBElement<? extends FeatureType>> features = folder.getFeature();
         for(JAXBElement<? extends FeatureType> featureJAXB : features) {
             FeatureType feature = featureJAXB.getValue();
             
             if (feature instanceof FolderType) {
                 findModels(models, (FolderType)feature);
             } else if (feature instanceof PlacemarkType) {
                 if (((PlacemarkType)feature).getGeometry()!=null) {
                     GeometryType geometryType = ((PlacemarkType)feature).getGeometry().getValue();
                     if (geometryType instanceof ModelType) {
                         models.add((ModelType)geometryType);
                     } else {
                         logger.info("Unsupported GeometryType "+geometryType);
                     }
                 }
             } else
                 logger.info("Skipping feature "+feature);
         }
     }
     
     public void deployToModule(File moduleRootDir) throws IOException {
         try {
             String modelName = origFile.getName();
             ZipFile zipFile = new ZipFile(origFile);
             
             // TODO replace getName with getModuleName(moduleRootDir)
             String moduleName = moduleRootDir.getName();
 
             String targetDirName = moduleRootDir.getAbsolutePath()+File.separator+"art"+ File.separator + modelName;
             File targetDir = new File(targetDirName);
             targetDir.mkdir();
 
             deployTextures(zipFile, targetDir);
             deployModels(zipFile, targetDir);
 
             if (modelFiles.size() > 1) {
                 logger.warning("Multiple models not supported during deploy");
             }
             JMEColladaCellSetup setup = new JMEColladaCellSetup();
             setup.setModel("wlm://"+moduleName+"/"+modelName+"/"+modelFiles.get(0));
             
             File wfsFile = new File(targetDirName+File.separator+"test.wfs");
             Writer w = new FileWriter(wfsFile);
             try {
                 setup.encode(w, getClass().getClassLoader());
             } catch (JAXBException ex) {
                 logger.log(Level.SEVERE, null, ex);
             }
             w.close();
             
         } catch (ZipException ex) {
             logger.log(Level.SEVERE, null, ex);
             throw new IOException("Zip error");
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
             throw ex;
         }
         
         
     }
     
     /**
      * KMZ files keep all the models in the /models directory, copy all the
      * models into the module
      * @param moduleArtRootDir
      */
     private void deployModels(ZipFile zipFile, File targetDir) {
         
         // TODO update collada files with module relative texture paths
         
         try {
             String targetDirName = targetDir.getAbsolutePath();
             
             Enumeration<? extends ZipEntry> entries = zipFile.entries();
             while(entries.hasMoreElements()) {
                 ZipEntry entry = entries.nextElement();
                 if (entry.getName().endsWith(".dae")) {
                     File target = new File(targetDirName+File.separator+entry.getName());
                     target.getParentFile().mkdirs();
                     target.createNewFile();
                     
                     copyAsset(zipFile, entry, target);
                 }
             }
             
             
         } catch (ZipException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         
     }
     
     /**
      * Deploys the textures into the art directory, placing them in a directory
      * with the name of the original model.
      * @param moduleArtRootDir
      */
     private void deployTextures(ZipFile zipFile, File targetDir) {
         try {
             // TODO generate checksums to check for image duplication
             String targetDirName = targetDir.getAbsolutePath();
 
             for (Map.Entry<URL, ZipEntry> t : textureFiles.entrySet()) {
                 File target = new File(targetDirName + File.separator + t.getKey().getPath());
                 target.getParentFile().mkdirs();
                 target.createNewFile();
 //                logger.fine("Texture file " + target.getAbsolutePath());
                 copyAsset(zipFile, t.getValue(), target);
             }
         } catch (ZipException ex) {
             logger.log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Copy the asset from the zipEntry to the target file
      * @param zipFile the zipFile that contains the zipEntry
      * @param zipEntry entry to copy from
      * @param target file to copy to
      */
     private void copyAsset(ZipFile zipFile, ZipEntry zipEntry, File target) {
         InputStream in = null;
         OutputStream out = null;
         try {
             in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
             out = new BufferedOutputStream(new FileOutputStream(target));
             
             org.jdesktop.wonderland.common.FileUtils.copyFile(in, out);
             
             
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if (in!=null)
                     in.close();
                 if (out!=null)
                     out.close();
             } catch (IOException ex) {
                 logger.log(Level.SEVERE, null, ex);
             }
         }
     }
 
     class ZipResourceLocator implements ResourceLocator {
 
         private String zipHost;
         private ZipFile zipFile;
         
         public ZipResourceLocator(String zipHost, ZipFile zipFile) {
             this.zipHost = zipHost;
             this.zipFile = zipFile;
         }
         
         public URL locateResource(String filename) {
             // Texture paths seem to be relative to the model directory....
             if (filename.startsWith("../")) {
                 filename = filename.substring(3);
             }
             if (filename.startsWith("/")) {
                 filename = filename.substring(1);
             }
             
             ZipEntry entry = zipFile.getEntry(filename);
             if (entry==null) {
                 logger.severe("Unable to locate texture "+filename);
                 return null;
             }
             
             try {
                 URL url = new URL("wlzip", zipHost, "/"+filename);
                 textureFiles.put(url, entry);
                 return url;
             } catch (MalformedURLException ex) {
                 Logger.getLogger(KmzLoader.class.getName()).log(Level.SEVERE, null, ex);
             }
             return null;
         }
     }
     
 
 }
