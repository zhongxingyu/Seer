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
 package org.jdesktop.wonderland.modules.jmecolladaloader.client;
 
 import com.jme.bounding.BoundingVolume;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.state.CullState;
 import com.jme.scene.state.RenderState;
 import com.jme.util.resource.ResourceLocator;
 import com.jme.util.resource.ResourceLocatorTool;
 import com.jme.util.resource.SimpleResourceLocator;
 import com.jmex.model.collada.ColladaImporter;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 import javax.xml.bind.JAXBException;
 import org.jdesktop.mtgame.RenderManager;
 import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
 import org.jdesktop.wonderland.client.jme.ClientContextJME;
 import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
 import org.jdesktop.wonderland.client.jme.artimport.ImportSettings;
 import org.jdesktop.wonderland.client.jme.artimport.ImportedModel;
 import org.jdesktop.wonderland.client.jme.artimport.ModelLoader;
 import org.jdesktop.wonderland.common.InternalAPI;
 import org.jdesktop.wonderland.common.cell.state.ModelCellServerState;
 import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
 import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellComponentServerState;
 import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.LoaderData;
 
 /**
  *
  * Loader for Collada files using JME loader
  * 
  * @author paulby
  */
 @InternalAPI
 public class JmeColladaLoader implements ModelLoader {
 
     private static final Logger logger = Logger.getLogger(JmeColladaLoader.class.getName());
         
     /**
      * Load a Collada file and return the graph root
      * @param file
      * @return
      */
     public ImportedModel importModel(ImportSettings settings) throws IOException {
         Node modelNode = null;
         URL origFile = settings.getModelURL();
 
         HashMap<URL, String> textureFilesMapping = new HashMap();
         ImportedModel importedModel = new ImportedModel(origFile, textureFilesMapping);
         SimpleResourceLocator resourceLocator=null;
         try {
            URL baseDir = new URL(origFile.toExternalForm().substring(0, origFile.toExternalForm().lastIndexOf('/')+1));
            resourceLocator = new RecordingResourceLocator(baseDir, textureFilesMapping);
             ResourceLocatorTool.addThreadResourceLocator(
                     ResourceLocatorTool.TYPE_TEXTURE,
                     resourceLocator);
         } catch (URISyntaxException ex) {
             Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
         
         logger.info("Loading MODEL " + origFile.toExternalForm());
         BufferedInputStream in = new BufferedInputStream(origFile.openStream());
 
         modelNode = loadModel(in, getFilename(origFile), true);
         in.close();
         
         ResourceLocatorTool.removeThreadResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, resourceLocator);
 
         importedModel.setModelBG(modelNode);
         importedModel.setModelLoader(this);
         importedModel.setImportSettings(settings);
 
         return importedModel;
     }
 
     private Node loadModel(InputStream in, String name, boolean applyColladaAxisAndScale) {
         Node modelNode;
         ColladaImporter.load(in, name);
         modelNode = ColladaImporter.getModel();
 
         RenderManager rm = ClientContextJME.getWorldManager().getRenderManager();
         CullState culls = (CullState) rm.createRendererState(RenderState.StateType.Cull);
         culls.setCullFace(CullState.Face.Back);
         modelNode.setRenderState(culls);
 
         if (applyColladaAxisAndScale) {
             // Adjust the scene transform to match the scale and axis specified in
             // the collada file
             float unitMeter = ColladaImporter.getInstance().getUnitMeter();
             modelNode.setLocalScale(unitMeter);
 
             String upAxis = ColladaImporter.getInstance().getUpAxis();
             if ("Z_UP".equals(upAxis)) {
                 modelNode.setLocalRotation(new Quaternion(new float[] {-(float)Math.PI/2, 0f, 0f}));
             } else if ("X_UP".equals(upAxis)) {
                 modelNode.setLocalRotation(new Quaternion(new float[] {0f, 0f, (float)Math.PI/2}));
             } // Y_UP is the Wonderland default
         }
 
         ColladaImporter.cleanUp();
 
 //        TreeScan.findNode(modelNode, new ProcessNodeInterface() {
 //
 //            public boolean processNode(Spatial node) {
 //                System.err.println(node);
 //                return true;
 //            }
 //
 //        });
 
         return modelNode;
     }
 
     /**
      * Get the url for the deployment data file associated with this model
      * 
      * @param model
      * @return
      */
     protected String getLoaderDataURL(DeployedModel model) {
         return model.getDeployedURL()+".ldr";
     }
 
     public Node loadDeployedModel(DeployedModel model) {
         InputStream in = null;
         try {
             LoaderData data=null;
//            System.err.println("LOADING DEPLOYED MODEL "+model.getDeployedURL());
             URL url = AssetUtils.getAssetURL(getLoaderDataURL(model));
             in = url.openStream();
             if (in==null) {
                 logger.severe("Unable to get deployment data "+url.toExternalForm());
             } else {
                 try {
                     data = LoaderData.decode(in);
                 } catch (JAXBException ex) {
                     Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, "Error parsing deployment data "+url.toExternalForm(), ex);
                 }
                 in.close();
             }
 
             if (model.getDeployedURL().endsWith(".gz"))
                 in = new GZIPInputStream(AssetUtils.getAssetURL(model.getDeployedURL()).openStream());
             else
                 in = AssetUtils.getAssetURL(model.getDeployedURL()).openStream();
 
             String baseURL = model.getDeployedURL();
             baseURL = baseURL.substring(0, baseURL.lastIndexOf('/'));
 
             Node modelBG;
             Map<String, String> deployedTextures = null;
             if (data!=null)
                 deployedTextures = data.getDeployedTextures();
 
             ResourceLocator resourceLocator = getDeployedResourceLocator(deployedTextures, baseURL);
 
             if (resourceLocator!=null) {
                 ResourceLocatorTool.addThreadResourceLocator(
                         ResourceLocatorTool.TYPE_TEXTURE,
                         resourceLocator);
             }
 
             modelBG = loadModel(in, getFilename(model.getDeployedURL()), false);
 
             if (resourceLocator!=null) {
                 ResourceLocatorTool.removeThreadResourceLocator(
                         ResourceLocatorTool.TYPE_TEXTURE,
                         resourceLocator);
             }
  
             return modelBG;
         } catch (IOException ex) {
             Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if (in!=null)
                     in.close();
             } catch (IOException ex) {
                 Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         return null;
     }
 
     protected ResourceLocator getDeployedResourceLocator(Map<String, String> deployedTextures, String baseURL) {
        if (deployedTextures==null)
            return null;
        return new DeployedResourceLocator(deployedTextures, baseURL);
     }
 
     public DeployedModel deployToModule(File moduleRootDir, ImportedModel importedModel) throws IOException {
             String modelName = getFilename(importedModel.getOriginalURL());
             
             HashMap<String, String> textureDeploymentMapping = new HashMap();
             DeployedModel deployedModel = new DeployedModel(importedModel.getOriginalURL(), this);
             LoaderData data = new LoaderData();
             data.setDeployedTextures(textureDeploymentMapping);
             data.setModelLoaderClassname(this.getClass().getName());
             deployedModel.setLoaderData(data);
             
             // TODO replace getName with getModuleName(moduleRootDir)
             String moduleName = moduleRootDir.getName();
 
             String targetDirName = moduleRootDir.getAbsolutePath()+File.separator+"art"+ File.separator + modelName;
             File targetDir = new File(targetDirName);
             targetDir.mkdirs();
 
             // Must deploy textures before models so we have the deployment url mapping
             deployTextures(targetDir, textureDeploymentMapping, importedModel);
 
             deployModels(targetDir, moduleName, deployedModel, importedModel, textureDeploymentMapping);
  
 //            if (modelFiles.size() > 1) {
 //                logger.warning("Multiple models not supported during deploy");
 //            }
 
             ModelCellServerState cellSetup = new ModelCellServerState();
             JmeColladaCellComponentServerState setup = new JmeColladaCellComponentServerState();
             cellSetup.addComponentServerState(setup);
 
             setup.setModel(deployedModel.getDeployedURL());
             setup.setModelScale(importedModel.getModelBG().getLocalScale());
             setup.setModelRotation(importedModel.getModelBG().getLocalRotation());
 
             Vector3f offset = importedModel.getRootBG().getLocalTranslation();
             PositionComponentServerState position = new PositionComponentServerState();
             Vector3f boundsCenter = importedModel.getRootBG().getWorldBound().getCenter();
 
             offset.subtractLocal(boundsCenter);
 
             setup.setModelTranslation(offset);
             setup.setModelLoaderClassname(importedModel.getModelLoader().getClass().getName());
 
 //            System.err.println("BOUNDS CENTER "+boundsCenter);
 //            System.err.println("OFfset "+offset);
 //            System.err.println("Cell origin "+boundsCenter);
             position.setTranslation(boundsCenter);
 
             // The cell bounds already have the rotation and scale applied, so these
             // values must not go in the Cell transform. Instead they go in the
             // JME cell setup so that the model is correctly oriented and thus
             // matches the bounds in the cell.
 
             // Center the worldBounds on the cell (ie 0,0,0)
             BoundingVolume worldBounds = importedModel.getModelBG().getWorldBound();
             worldBounds.setCenter(new Vector3f(0,0,0));
             position.setBounds(worldBounds);
             cellSetup.addComponentServerState(position);
 
             deployedModel.recordModelBGTransform(importedModel.getModelBG());
             deployedModel.addCellServerState(cellSetup);
 
//            System.err.println("DEPLOYING "+deployedModel);
 
             return deployedModel;
     
     }
 
     protected void deployDeploymentData(File targetDir, DeployedModel deployedModel, String filename) {
         LoaderData data = (LoaderData) deployedModel.getLoaderData();
         File deploymentDataFile = new File(targetDir, filename+".dep");
         File loaderDataFile = new File(targetDir, filename+".ldr");
         try {
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(deploymentDataFile));
             try {
                 deployedModel.encode(out);
             } catch (JAXBException ex) {
                 Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
             }
             out.close();
         } catch(IOException e) {
 
         }
 
         try {
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(loaderDataFile));
             try {
                 data.encode(out);
             } catch (JAXBException ex) {
                 Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
             }
             out.close();
         } catch(IOException e) {
 
         }
     }
 
     /**
      * Deploy the dae files to the server, source[0] is the primary file.
      * @param moduleArtRootDir
      */
     protected void deployModels(File targetDir,
             String moduleName,
             DeployedModel deployedModel,
             ImportedModel importedModel,
             HashMap<String, String> deploymentMapping) {
         URL[] source = importedModel.getAllOriginalModels();
         
         String filename = getFilename(importedModel.getOriginalURL());
         String filenameGZ = filename+".gz";
         File targetFile = new File(targetDir, filenameGZ);
         try {
             targetFile.createNewFile();
             // TODO compress the dae file using gzip stream
             copyAsset(source[0], targetFile, true); // TODO handle multiple dae files
             deployedModel.setDeployedURL("wla://"+moduleName+"/"+filename+"/"+filenameGZ);
 
             deployDeploymentData(targetDir, deployedModel, filenameGZ);
 
             // Decided not to do this for deployment. Instead we will create and
             // manage the binary form in the client asset cache. The binary
             // files are only slightly smaller than compresses collada.
             
             // Fix the texture references in the graph to the deployed URL's
 //            TreeScan.findNode(importedModel.getModelBG(), Geometry.class, new ProcessNodeInterface() {
 //                public boolean processNode(Spatial node) {
 //                    Geometry g = (Geometry)node;
 //                    TextureState ts = (TextureState)g.getRenderState(StateType.Texture);
 //                    if (ts!=null) {
 //                        Texture texture = ts.getTexture();
 ////                        System.err.println("Graph Texture "+texture.getImageLocation());
 //                        try {
 //                            String originalURL = importedModel.getTextureFiles().get(new URL(texture.getImageLocation()));
 //                            String deployedURL = "wla://"+moduleName+"/"+deploymentMapping.get(originalURL);
 //                            if (deployedURL!=null)
 //                                texture.setImageLocation(deployedURL);
 ////                            System.err.println("DeployedURL "+deployedURL);
 //                        } catch (MalformedURLException ex) {
 //                            Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
 //                        }
 //
 //                    }
 //                    return true;
 //                }
 //
 //            }, false, false);
 //
 //            DeployStorage binaryModelFile = targetDir.createChildFile(filename+".wbm");
 //            OutputStream binaryModelStream = binaryModelFile.getOutputStream();
 //            BinaryExporter.getInstance().save(importedModel.getModelBG(), binaryModelStream);
 //            binaryModelStream.close();
 
         } catch (IOException ex) {
             Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, "Unable to create file "+targetFile, ex);
         }
 
     }
 
     /**
      * Return the filename for this url, excluding the path
      * @param url
      * @return
      */
     private String getFilename(URL url) {
         String t = url.getPath();
         return t.substring(t.lastIndexOf('/')+1);
     }
 
     private String getFilename(String str) {
         return str.substring(str.lastIndexOf('/')+1);
     }
 
     /**
      * Deploys the textures into the art directory, placing them in a directory
      * with the name of the original model.
      * @param moduleArtRootDir
      */
     protected void deployTextures(File targetDir, Map<String, String> deploymentMapping, ImportedModel loadedModel) {
         try {
             // TODO generate checksums to check for image duplication
 //            String targetDirName = targetDir.getAbsolutePath();
 
             for (Map.Entry<URL, String> t : loadedModel.getTextureFiles().entrySet()) {
                 File target=null;
                 String targetFilename = t.getValue();
                 String deployFilename=null;
                 if (targetFilename.startsWith("/")) {
                     targetFilename = targetFilename.substring(targetFilename.lastIndexOf('/'));
                     if (targetFilename==null) {
                         targetFilename = t.getValue();
                     }
                 } else {
                     // Relative path
                     if (targetFilename.startsWith("..")) {
                         deployFilename = targetFilename.substring(3);
                         target = new File(targetDir, deployFilename);
                     }
                 }
 
                 if (target==null) {
                     deployFilename = targetFilename;
                     target = new File(targetDir, targetFilename);
                 }
 
 //                logger.info("Texture file " + target.getAbsolutePath());
                 target.getParentFile().mkdirs();
                 target.createNewFile();
                 copyAsset(t.getKey(), target, false);
 
                 // Lookup the url that was in the collada file and store the mapping
                 // between that and the deployed url
                 String colladaURL = loadedModel.getTextureFiles().get(t.getKey());
                 deploymentMapping.put(colladaURL, deployFilename);
             }
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Copy the asset from the source url to the target file
      * @param source the source file to copy from
      * @param target file to copy to
      */
     private void copyAsset(URL source, File targetFile, boolean compress) {
         InputStream in = null;
         OutputStream out = null;
         try {
             in = new BufferedInputStream(source.openStream());
             if (compress)
                 out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(targetFile)));
             else
                 out = new BufferedOutputStream(new FileOutputStream(targetFile));
             
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
 
     public ModelCellServerState getCellServerState(
             String deployedURL,
             Vector3f modelTranslation,
             Quaternion modelRotation,
             Vector3f modelScale,
             Map<String, Object> properties) {
         ModelCellServerState cellSetup = new ModelCellServerState();
         JmeColladaCellComponentServerState setup = new JmeColladaCellComponentServerState();
         cellSetup.addComponentServerState(setup);
 
         setup.setModel(deployedURL);
         setup.setModelScale(modelScale);
         setup.setModelRotation(modelRotation);
         setup.setModelTranslation(modelTranslation);
         setup.setModelLoaderClassname(this.getClass().getName());
 
         return cellSetup;
     }
 
     /**
      * Locate resource for deployed models
      */
     class DeployedResourceLocator implements ResourceLocator {
         private Map<String, String> textureUrlMapping;
         private String baseURL;
 
         public DeployedResourceLocator(Map<String, String> textureUrlMapping, String baseURL) {
             this.textureUrlMapping = new HashMap(textureUrlMapping);
             this.baseURL = baseURL;
        }
 
         public URL locateResource(String resourceName) {
             String t = textureUrlMapping.get(resourceName);
 
              if (t==null)
                 return null;
 
             URL ret=null;
             try {
                 ret = AssetUtils.getAssetURL(baseURL + "/" + t);
                 textureUrlMapping.put(ret.getPath(), t);  // JME may ask for the texture again, using the new path
             } catch (MalformedURLException ex) {
                 Logger.getLogger(JmeColladaLoader.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             return ret;
         }
     }
 
     class RecordingResourceLocator extends SimpleResourceLocator {
         private Map<URL, String> resourceSet;
         public RecordingResourceLocator(URI baseDir, Map<URL, String> resourceSet) {
             super(baseDir);
             this.resourceSet = resourceSet;
         }
 
         public RecordingResourceLocator(URL baseDir, Map<URL, String> resourceSet) throws URISyntaxException {
             super(baseDir);
             this.resourceSet = resourceSet;
         }
 
         @Override
         public URL locateResource(String resourceName) {
             URL ret = locateResourceImpl(resourceName);
             if (!resourceSet.containsKey(ret)) {
                 resourceSet.put(ret, resourceName);
             }
 
             return ret;
         }
 
         // Copied directly from SimpleResourceLocator
         public URL locateResourceImpl(String resourceName) {
             // Trim off any prepended local dir.
             while (resourceName.startsWith("./") && resourceName.length() > 2) {
                 resourceName = resourceName.substring(2);
             }
             while (resourceName.startsWith(".\\") && resourceName.length() > 2) {
                 resourceName = resourceName.substring(2);
             }
 
             // Try to locate using resourceName as is.
             try {
                 String spec = URLEncoder.encode( resourceName, "UTF-8" );
                 //this fixes a bug in JRE1.5 (file handler does not decode "+" to spaces)
                 spec = spec.replaceAll( "\\+", "%20" );
 
                 URL rVal = new URL( baseDir.toURL(), spec );
                 // open a stream to see if this is a valid resource
                 // XXX: Perhaps this is wasteful?  Also, what info will determine validity?
                 rVal.openStream().close();
                 return rVal;
             } catch (IOException e) {
                 // URL wasn't valid in some way, so try up a path.
             } catch (IllegalArgumentException e) {
                 // URL wasn't valid in some way, so try up a path.
             }
 
             resourceName = trimResourceName(resourceName);
             if (resourceName == null) {
                 return null;
             } else {
                 return locateResourceImpl(resourceName);
             }
         }
     }
 }
