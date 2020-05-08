 package gov.nih.nci.cagrid.introduce.extensions.metadata.upgrade;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.introduce.beans.extension.ExtensionType;
 import gov.nih.nci.cagrid.introduce.common.ServiceInformation;
 import gov.nih.nci.cagrid.introduce.extension.ExtensionsLoader;
 import gov.nih.nci.cagrid.introduce.extension.utils.ExtensionUtilities;
 import gov.nih.nci.cagrid.introduce.extensions.metadata.common.MetadataExtensionHelper;
 import gov.nih.nci.cagrid.introduce.extensions.metadata.constants.MetadataConstants;
 import gov.nih.nci.cagrid.introduce.upgrade.common.StatusBase;
 import gov.nih.nci.cagrid.introduce.upgrade.one.x.ExtensionUpgraderBase;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * MetadataUpgrade1pt0to1pt1 copies metadata descriptions
  * 
  * @author oster
  * @created Apr 9, 2007 11:21:24 AM
  * @version $Id: multiscaleEclipseCodeTemplates.xml,v 1.1 2007/03/02 14:35:01
  *          dervin Exp $
  */
 public class MetadataUpgrade1pt3 extends ExtensionUpgraderBase {
 
     private static final String CAGRID_1_3_METADATA_JAR_PREFIX = "caGrid-metadata";
     private static final String CAGRID_1_3_METADATA_JAR_SUFFIX = "-1.3.jar";
 
    private static final String CAGRID_1_3_METADATA_VALIDATOR_JAR_PREFIX = "caGrid-metadata-validator";
    private static final String CAGRID_1_3_METADATA_VALIDATOR_JAR_SUFFIX = "-1.3.jar";

     protected MetadataExtensionHelper helper;
     protected static Log LOG = LogFactory.getLog(MetadataUpgrade1pt3.class.getName());
 
 
     /**
      * @param extensionType
      * @param serviceInfo
      * @param servicePath
      * @param fromVersion
      * @param toVersion
      */
     public MetadataUpgrade1pt3(ExtensionType extensionType, ServiceInformation serviceInfo, String servicePath,
         String fromVersion, String toVersion) {
         super("MetadataUpgrade1pt3", extensionType, serviceInfo, servicePath, fromVersion, toVersion);
         this.helper = new MetadataExtensionHelper(serviceInfo);
     }
 
 
     @Override
     protected void upgrade() throws Exception {
         if (this.helper.getExistingServiceMetdata() == null) {
             LOG.info("Unable to locate service metdata; no metadata upgrade will be performed.");
             getStatus().addDescriptionLine("Unable to locate service metdata; no metadata upgrade will be performed.");
             getStatus().setStatus(StatusBase.UPGRADE_OK);
             return;
         }
 
         upgradeJars();
         getStatus().setStatus(StatusBase.UPGRADE_OK);
     }
 
 
     /**
      * Upgrade the jars which are required for metadata
      */
     private void upgradeJars() {
         FileFilter metadataLibFilter = new FileFilter() {
             public boolean accept(File pathname) {
                 String name = pathname.getName();
                 return name.endsWith(CAGRID_1_3_METADATA_JAR_SUFFIX) && name.startsWith(CAGRID_1_3_METADATA_JAR_PREFIX)
                     && !name.startsWith(CAGRID_1_3_METADATA_JAR_PREFIX + "-data")
                     && !name.startsWith(CAGRID_1_3_METADATA_JAR_PREFIX + "-security");
             }
         };
         FileFilter newMetadataLibFilter = new FileFilter() {
             public boolean accept(File pathname) {
                 String name = pathname.getName();
                 return name.endsWith(".jar") && name.startsWith(MetadataConstants.METADATA_JAR_PREFIX)
                     && !name.startsWith(MetadataConstants.METADATA_JAR_PREFIX + "-data")
                     && !name.startsWith(MetadataConstants.METADATA_JAR_PREFIX + "-security");
             }
         };

        FileFilter validatorLibFilter = new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(CAGRID_1_3_METADATA_VALIDATOR_JAR_SUFFIX)
                    && name.startsWith(CAGRID_1_3_METADATA_VALIDATOR_JAR_PREFIX);
            }
        };

        // locate the old service libs in the service
         File serviceLibDir = new File(getServicePath() + File.separator + "lib");
         File[] serviceMetadataLibs = serviceLibDir.listFiles(metadataLibFilter);
         // delete the old libraries
         for (File oldLib : serviceMetadataLibs) {
             oldLib.delete();
            getStatus().addDescriptionLine("caGrid 1.4 library " + oldLib.getName() + " removed fron lib.");
         }
         // copy new libraries in
         File extLibDir = new File(ExtensionsLoader.EXTENSIONS_DIRECTORY + File.separator + "lib");
         File[] metadataLibs = extLibDir.listFiles(newMetadataLibFilter);
         List<File> outLibs = new ArrayList<File>(metadataLibs.length);
         for (File newLib : metadataLibs) {
             File out = new File(serviceLibDir.getAbsolutePath() + File.separator + newLib.getName());
             try {
                 Utils.copyFile(newLib, out);
                 getStatus().addDescriptionLine("caGrid 1.4 library " + newLib.getName() + " added");
             } catch (IOException ex) {
                 // TODO: change this to use a better exception
                 throw new RuntimeException("Error copying new metadata library: " + ex.getMessage(), ex);
             }
             outLibs.add(out);
         }
 
        // locate the old service tools libs in the service
        File serviceToolsLibDir = new File(getServicePath() + File.separator + "tools" + File.separator + "lib");
        File[] serviceToolsLibs = serviceToolsLibDir.listFiles(validatorLibFilter);
        // delete the old libraries
        for (File oldLib : serviceToolsLibs) {
            oldLib.delete();
            getStatus().addDescriptionLine("caGrid 1.4 library " + oldLib.getName() + " removed from tools lib.");
        }

         // copy in the deployment validator stuff
         File toToolsDir = new File(getServicePath() + File.separator + "tools" + File.separator + "lib");
         if (!toToolsDir.exists()) {
             toToolsDir.mkdirs();
         }
         // from the extension lib directory to the tools lib directory
         File toolslibDir = new File(ExtensionsLoader.EXTENSIONS_DIRECTORY + File.separator
             + MetadataConstants.EXTENSION_NAME + File.separator + "lib");
         File[] toolslibs = toolslibDir.listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 String name = pathname.getName();
                 return (name.endsWith(".jar"));
             }
         });
 
         if (toolslibs != null) {
             File[] copiedLibs = new File[toolslibs.length];
             for (int i = 0; i < toolslibs.length; i++) {
                 File outFile = new File(toToolsDir + File.separator + toolslibs[i].getName());
                 copiedLibs[i] = outFile;
                 try {
                     Utils.copyFile(toolslibs[i], outFile);
                     getStatus().addDescriptionLine(
                         "caGrid 1.4 library " + outFile.getName() + " added, for deploytime validation.");
 
                 } catch (IOException e) {
                     // TODO: change this to use a better exception
                     throw new RuntimeException("Error adding deployment validator: " + e.getMessage(), e);
                 }
             }
         }

         // update the Eclipse .classpath file
         File classpathFile = new File(getServicePath() + File.separator + ".classpath");
         File[] outLibArray = new File[metadataLibs.length];
         outLibs.toArray(outLibArray);
         try {
             ExtensionUtilities.syncEclipseClasspath(classpathFile, outLibArray);
             getStatus().addDescriptionLine("Eclipse .classpath file updated");
         } catch (Exception ex) {
             // TODO: change this to use a better exception
             throw new RuntimeException("Error updating Eclipse .classpath file: " + ex.getMessage(), ex);
         }
 
     }
 }
